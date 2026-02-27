package com.resonance.config;


import com.resonance.websocket.RoomWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final RoomWebSocketHandler roomWebSocketHandler;

    public WebSocketConfig(RoomWebSocketHandler roomWebSocketHandler) {
        this.roomWebSocketHandler = roomWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 告诉 Spring：当有前端请求 "ws://localhost:8080/ws/room" 这个地址时，
        // 把连接交给我们的 RoomWebSocketHandler 去处理！
        // setAllowedOrigins("*") 极其重要：允许所有前端跨域连接，否则前端连不上。
        registry.addHandler(roomWebSocketHandler, "/ws/room").setAllowedOrigins("*");
    }


}
