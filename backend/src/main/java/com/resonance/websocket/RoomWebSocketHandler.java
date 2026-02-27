package com.resonance.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resonance.dto.RoomMessage;
import com.resonance.service.RoomService;
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
    private final RoomService roomService;
    //引入json转换神器
    private final ObjectMapper objectMapper;

    public RoomWebSocketHandler(ObjectMapper objectMapper,RoomService roomService){
        this.objectMapper = objectMapper;
        this.roomService = roomService;
    }
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

        try{
            //1.将前端的发来的json字符串，转为RoomMessage
            RoomMessage roomMessage = objectMapper.readValue(payload,RoomMessage.class);
            String roomId = roomMessage.getRoomId();

            //2.如果是"JOIN"指令，要在他的电话线上贴个专属标签
            if("JOIN".equals(roomMessage.getType())){
                session.getAttributes().put("roomId",roomId);
                session.getAttributes().put("userId",roomMessage.getUserId());
                System.out.println("👋 用户 " + roomMessage.getUserId() + " 加入了房间: " + roomId);
            }
            if("PLAY".equals(roomMessage.getType()) || "PAUSE".equals(roomMessage.getType()) || "SEEK".equals(roomMessage.getType())||"SWITCH".equals(roomMessage.getType())){
                roomService.updateRoomState(roomId,roomMessage);
            }
            //3.定向广播

            //遍历花名册上的所有人，只有对方标签上的roomId和当前动作的roomId一样才进行转发
            for(WebSocketSession s : sessions.values()){
                if(s.isOpen()){
                    String targetRoomId = (String)s.getAttributes().get("roomId");
                    if(roomId.equals(targetRoomId)){
                        s.sendMessage(new TextMessage(payload));
                    }
                }
            }


        }catch (Exception e){
            System.err.println("消息解析或处理失败" + e.getMessage());
        }

    }

    /**
     * 当有用户断开连接时触发（比如关掉了浏览器页面）
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //把用户从花名册中移除
        sessions.remove(session.getId());
        String userId = (String)session.getAttributes().get("userId");
        System.out.println(" 用户" + userId+"断开连接！当前连接数：" + sessions.size());
    }
}
