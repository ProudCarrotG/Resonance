package com.resonance.controller;  // ← 这里用你的基础包名 + .controller

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.resonance.common.ApiResponse;

@RestController                   // 告诉 Spring：这是一个 REST 接口类
@RequestMapping("/api")           // 这个类下面的接口，统一加上前缀 /api
public class HelloController {

    @GetMapping("/ping")          // 对应 GET /api/ping
    public ApiResponse<String> ping() {
        return ApiResponse.success("pong");            // 返回简单字符串
    }
}