package com.resonance.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {

    //私钥Key，密码长度必须大于256位，即32个字符
    private static final String SECRET_STRING = "ResonanceSuperSecretKeyForJwtAuthentication2026";

    // 把上面的字符串，转换成 JWT 底层密码学认定的 Key 对象
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());


    //定义Token的保质期 单位毫秒 设置 24小时
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;
    /**
     * 动作一：颁发身份证（登录成功后调用）
     * 把 userId 塞进 Token 里，并盖上不可篡改的防伪公章。
     */
    public static String generateToken(String userId){
        return Jwts.builder()
                .setSubject(userId) // Payload 核心：把 userId 存进去
                .setIssuedAt(new Date()) // 签发时间：现在
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间：24小时后
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // 核心防伪：用私钥和 HS256 算法生成签名
                .compact(); // 压缩打包成那串 xxxxx.yyyyy.zzzzz 的长字符串
    }

    /**
     * 动作二：查验身份证（拦截器里调用）
     * 只要 Token 被篡改过一个字母，或者时间过期了，这里会直接抛出极其严厉的异常！
     */
    public static String parseToken(String token) {
        // 这一步是极其严苛的安检过程
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 拿出咱们的机密钥匙去解密比对
                .build()
                .parseClaimsJws(token) // 🚨 核心排雷区：如果伪造或过期，这一步直接爆炸抛异常！
                .getBody();

        // 安检通过，把当初存进去的 userId 完好无损地取出来
        return claims.getSubject();
    }

}
