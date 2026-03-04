package com.resonance.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resonance.common.ApiResponse;
import com.resonance.domain.User;
import com.resonance.mapper.UserMapper;
import com.resonance.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping ("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @PostMapping("/login")
    public ApiResponse<Map<String,Object>> login(@RequestParam String username,@RequestParam String password){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);

        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            // 2. 没找到？说明是新用户，直接原地注册插入 MySQL！
            user = new User();
            user.setUsername(username);
            user.setPassword(password); // 真实环境这里必须用 BCrypt 加密，咱们先明文跑通
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 3. 找到了？校验密码
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("密码错误，请重试！");
            }
        }

        // 4. 无论登录还是刚注册，到这里肯定有 user 对象了，且拥有了 MySQL 分配的真实 UUID。
        // 调用制证机，把真实的 userId 锁进 Token 里！
        String token = JwtUtils.generateToken(user.getId());

        // 5. 把 Token 和用户信息一起打包发给前端
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());

        return ApiResponse.success(result);
    }
}
