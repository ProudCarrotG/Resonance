package com.resonance.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.resonance.common.ApiResponse;
import com.resonance.domain.User;
import com.resonance.mapper.UserMapper;
import com.resonance.service.UserService;
import com.resonance.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping ("/api/users")
public class UserController {


    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }
    @PostMapping("/login")
    public ApiResponse<Map<String,Object>> login(@RequestParam String username,@RequestParam String password){
        return ApiResponse.success(userService.login(username,password));
    }
    @PostMapping("/register")
    public ApiResponse<Boolean> register(@RequestParam String username, @RequestParam String password){
        return ApiResponse.success(userService.register(username,password));
    }
}
