package com.resonance.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resonance.common.ApiResponse;
import com.resonance.domain.Room;
import com.resonance.dto.RoomMessage;
import com.resonance.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.JspTemplateAvailabilityProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    public static final Logger log = LoggerFactory.getLogger(RoomWebSocketHandler.class);
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
            if("PING".equals(roomMessage.getType())){
                //记录用户最后的活跃时间
                session.getAttributes().put("lastActiveTime", System.currentTimeMillis());
                RoomMessage pongMessage = new RoomMessage();
                pongMessage.setType("PONG");

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pongMessage)));
                return;
            }
            //2.如果是"JOIN"指令，要在他的电话线上贴个专属标签
            if("JOIN".equals(roomMessage.getType())){
                session.getAttributes().put("roomId",roomId);
                session.getAttributes().put("userId",roomMessage.getUserId());
                try{
                    //查询redis中房间的信息
                    Room room = roomService.getRoom(roomId);
                    //同步房间信息
                    RoomMessage syncMessage = new RoomMessage();
                    syncMessage.setType("ROOM_SYNC");
                    syncMessage.setRoomId(room.getRoomId());
                    syncMessage.setData(room);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(syncMessage)));
                }catch (Exception e){
                    RoomMessage errorMsg = new RoomMessage();
                    errorMsg.setType("ERROR");
                    errorMsg.setData("加入失败！找不到房间或房间已解散");
                    log.error("加入房间失败" , e);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
                }
                System.out.println("👋 用户 " + roomMessage.getUserId() + " 加入了房间: " + roomId);
            }
            if("PLAY".equals(roomMessage.getType()) || "PAUSE".equals(roomMessage.getType()) || "SEEK".equals(roomMessage.getType())||"SWITCH".equals(roomMessage.getType())){
                String userId = (String)session.getAttributes().get("userId");
                try{
                    roomService.updateRoomState(roomId,userId,roomMessage);
                } catch (Exception e) {
                    RoomMessage errMessage = new RoomMessage();
                    errMessage.setType("ERROR");

                    errMessage.setData(e.getMessage());

                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errMessage)));

                    return;
                }

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
            log.error("消息解析或处理失败" + e.getMessage());
        }

    }

    /**
     * 当有用户断开连接时触发（比如关掉了浏览器页面）
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws JsonProcessingException, IOException {
        // 1. 先把这个断开的人从花名册划掉
        sessions.remove(session.getId());

        // 2. 摸一下他的便签纸，看看他是哪个房间的谁
        String roomId = (String) session.getAttributes().get("roomId");
        String userId = (String) session.getAttributes().get("userId");


        if (roomId != null && userId != null) {


            // 3. 呼叫大管家：看看这个退出的家伙是不是房主，需不需要炸毁房间？
            boolean isDisbanded = roomService.disbandRoomIfHost(roomId, userId);

            if (isDisbanded) {
                System.out.println("💥 房主 " + userId + " 离开，房间 " + roomId + " 已被彻底销毁！");

                // 4. 组装一条“房间解散”的专属暗号
                RoomMessage disbandMsg = new RoomMessage();
                disbandMsg.setType("ROOM_DISBANDED");
                disbandMsg.setRoomId(roomId);
                String disbandPayload = objectMapper.writeValueAsString(disbandMsg);

                // 5. 广播给这个房间里所有还在眼巴巴等着的听众
                for (WebSocketSession s : sessions.values()) {
                    if (s.isOpen()) {
                        String targetRoomId = (String) s.getAttributes().get("roomId");
                        if (roomId.equals(targetRoomId)) {
                            s.sendMessage(new TextMessage(disbandPayload));
                        }
                    }
                }
            }else{
                roomService.leaveRoom(roomId,userId);
            }
        }
    }
}
