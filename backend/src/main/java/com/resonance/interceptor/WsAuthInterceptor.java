package com.resonance.interceptor;

import com.resonance.exception.TokenInvalidException;
import com.resonance.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

public class WsAuthInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(WsAuthInterceptor.class.getName());
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) throws Exception {

        // 1. 获取 URL 里的 token 参数：ws://.../ws/room?token=xxx
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && !token.trim().isEmpty()) {
                // 2. 从 Token 中解析出真实的 userId
                try{
                    String userId = JwtUtils.parseToken(token);
                    // 3. 🌟 核心魔法：把 userId 塞进 WebSocketSession 的 attributes (背包) 里！
                    attributes.put("userId", userId);
                    return true; // 允许握手，建立 WS 连接
                } catch (TokenInvalidException e) {
                    throw new TokenInvalidException(e.getMessage(),e);
                }
            }else{
                log.warn("Token 为空");
            }
        }
        return false; // Token 无效，直接拒绝建立连接！
    }
    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {}
}