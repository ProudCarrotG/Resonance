package com.resonance.inerceptor;


import com.resonance.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.http.HttpResponse;

/**
 * JWT 安检门：拦截所有请求，核验 Token
 */

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler)throws Exception{
        // 1. 对于跨域的预检请求 (OPTIONS)，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //2.从Http请求头获取Token
        // 业界标准：存放 Token 的头部字段名通常叫 "Authorization"

        String authHeader = request.getHeader("Authorization");

        if(authHeader==null || !authHeader.startsWith("Bearer")){
            throw new RuntimeException("无权访问:缺少令牌或格式错误");
        }

        // 4. 提取出真正的 Token 字符串 (去掉前缀 "Bearer ")
        String token = authHeader.substring(7);

        //验证Token
        try{
            String userId = JwtUtils.parseToken(token);

            // 6.查验成功后，把 userId 贴在这个请求的“脑门”上！
            // 这样等请求流转到 Controller 时，Controller 就能直接从 request 里拿到 userId 了。
            request.setAttribute("userId", userId);

            return true;
        }catch (Exception e){
            throw new RuntimeException("身份令牌已失效或被篡改，请重新登录！");
        }
    }
}