package com.resonance.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {
    //这是一个及其重要的“花名册” ： 用来记住当前有哪些用户连着Session
    //使用ConcurrentHashMap，是为了保证多线程并发时的安全
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 当有新的连接建立时，会调用这个方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //把新来的用户加入到花名册中
        sessions.put(session.getId(), session);

        System.out.println(" 新用户加入！当前连接数：" + sessions.size());
    }
    /**
     * 当收到某个用户发来的消息时触发（比如房主发来了“切歌”指令）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println(" 收到消息：" + payload);
        for (WebSocketSession s : sessions.values()) {
            if(s.isOpen()){
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    /**
     * 当有用户断开连接时触发（比如关掉了浏览器页面）
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //把用户从花名册中移除
        sessions.remove(session.getId());
        System.out.println(" 用户断开连接！当前连接数：" + sessions.size());
    }
}
