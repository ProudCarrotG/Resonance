package com.resonance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resonance.domain.User;
import com.resonance.mapper.UserMapper;
import com.resonance.service.UserService;
import com.resonance.utils.JwtUtils;
import com.resonance.utils.RedisKeyBuilder;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public UserServiceImpl(UserMapper userMapper,StringRedisTemplate redisTemplate){
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public Map<String, Object> login(String username, String password) {

        String failKey = RedisKeyBuilder.getLoginFailKey(username);

        // 🛡️ 1. 安全前置校验：看他是不是已经被关小黑屋了？
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= 5) {
            log.warn("⚠️ 账号被锁定，密码错误次数过多: {}", username);
            throw new RuntimeException("密码错误次数过多，账号已锁定，请 15 分钟后再试");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            recordLoginFail(failKey);
            // 防护细节：对外提示尽量模糊，不要明确告诉黑客“账号不存在”还是“密码错误”，统一说“账号或密码错误”是最安全的
            throw new RuntimeException("账号或密码错误，请重试");
        }

        // 🛡️ 核心改造：使用 BCrypt 比对密码！
        // 参数 1：前端传来的明文 (比如 "123456")
        // 参数 2：数据库里存的密文 (比如 "$2a$10$wYx...")
        boolean isMatch = BCrypt.checkpw(password, user.getPassword());

        if (!isMatch) {
            recordLoginFail(failKey);
            throw new RuntimeException("账号或密码错误，请重试");
        }

        redisTemplate.delete(failKey);

        String token = JwtUtils.generateToken(user.getId());

        // 5. 把 Token 和用户信息一起打包发给前端
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        return result;
    }

    @Override
    public Boolean register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        // BCrypt 密文加密
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());

        try {
            // 🚀 性能拉满：不去查数据库了，直接硬插！
            userMapper.insert(user);
            log.info("✨ 新用户注册成功: {}", username);
            return true;
        } catch (DuplicateKeyException e) {
            // 🛡️ 兜底防御：如果插入时 MySQL 报错说“重复了”，被我们精准捕获！
            log.warn("⚠️ 注册并发冲突，账号已被占用: {}", username);
            throw new RuntimeException("该账号已经被注册啦，换一个试试吧");
        }
    }
    /**
     * 辅助方法：记录登录失败次数
     */
    private void recordLoginFail(String failKey) {
        // Redis 的 increment 极其牛逼：如果 key 不存在，它会自动创建并设置为 1，返回 1
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            // 只有在第一次错误时，才设置 15 分钟的过期时间！
            redisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
        }
    }
}
