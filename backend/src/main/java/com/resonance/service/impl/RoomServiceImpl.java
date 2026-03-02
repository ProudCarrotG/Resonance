package com.resonance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resonance.domain.Room;
import com.resonance.domain.RoomHistory;
import com.resonance.dto.RoomMessage;
import com.resonance.mapper.RoomHistoryMapper;
import com.resonance.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.StyledEditorKit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RoomServiceImpl implements RoomService {
    // Spring 官方提供的用来操作 Redis 的强大工具
    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    private RoomHistoryMapper roomHistoryMapper;
    // 💡 构造器注入：Spring 官方极其推荐的写法，比加 @Autowired 注解更安全
    public RoomServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Room createRoom(String roomName,String hostId){
        //1.初始化一个全新的房间
        Room room = new Room();

        //生成一个没有横线的随机UUID作为房间专属ID

        String roomId = UUID.randomUUID().toString().replace("-","");

        room.setRoomName(roomName);
        room.setRoomId(roomId);
        room.setHostId(hostId);
        room.setCurrentProgress(0L); //刚开放，进度条为零
        room.setPlayStatus("WAITING"); //初始状态
        room.setCreatedAt(LocalDateTime.now());

        //2.将房间的数据打包进Redis

        try{
            //把java对象转换成一段通用的json字符串
            String roomJson = objectMapper.writeValueAsString(room);

            //规范的Redis Key命名法则 ： 项目名 ： 模块名 ： ID
            String redisKey = "resonance:room:" + roomId;

            // 存入 Redis，并施加“阅后即焚”魔法：12小时后这个房间在内存中自动烟消云散！
            redisTemplate.opsForValue().set(redisKey, roomJson, 12, TimeUnit.HOURS);

            //将数据存到mysql中

            RoomHistory roomHistory = new RoomHistory();

            roomHistory.setRoomName(roomName);
            roomHistory.setRoomId(roomId);
            roomHistory.setHostId(hostId);

            roomHistoryMapper.insert(roomHistory);

        }catch (Exception e){
            throw new RuntimeException("系统开小差了");
        }

        return room;
    }

    @Override
    public Room getRoom(String roomId){
        //1.拼装redis的key
        String redisKey = "resonance:room:" + roomId;
        //2.从redis中取出数据
        String roomJson = redisTemplate.opsForValue().get(redisKey);

        //3.判断是否有数据
        if(roomJson == null){
            throw new RuntimeException("房间不存在或已解散");
        }
        //4.将json字符串转换成java对象
        try{
            Room room = objectMapper.readValue(roomJson, Room.class);
            return room;
        }catch (Exception e){
            throw new RuntimeException("房间数据解析失败",e);
        }
    }

    @Override
    public void updateRoomState(String roomId, RoomMessage message) {
        String redisKey = "resonance:room:" + roomId;
        String lockKey = "resonance:lock:room:" + roomId;

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,"LOCKED",10,TimeUnit.SECONDS);

        if(Boolean.FALSE.equals(acquired)){
            throw new RuntimeException("当前有其他操作正在进行，请稍后再试");
        }

        try{
            String roomJson = redisTemplate.opsForValue().get(redisKey);
            //如果redis找不到房间，说明已经结束或者已经解散
            if(roomJson == null) return;
            Room room  = objectMapper.readValue(roomJson,Room.class);

            switch (message.getType()){
                case "PLAY":
                    room.setPlayStatus("PLAYING");
                    if(message.getData()!=null){
                        room.setCurrentTrackId(message.getData().toString());
                    }
                    break;
                case "PAUSE":
                    room.setPlayStatus("PAUSED");
                    break;
                case "SEEK"://拖动进度条
                    long progress = 0L;
                    if(message.getData()!=null && !message.getData().toString().isEmpty()){
                        progress = Long.parseLong(message.getData().toString());
                    }
                    room.setCurrentProgress(progress);
                    break;
                case "SWITCH": // 切歌指令
                    room.setPlayStatus("PLAYING"); // 切歌后通常自动播放
                    if (message.getData() != null) {
                        room.setCurrentTrackId(message.getData().toString());
                    }
                    room.setCurrentProgress(0L); // 切歌必须让进度条强行归零！(符合咱们空字段或重置字段按0处理的规则)
                    break;            }
            // 修改完之后需要重新将信息写回redis

            redisTemplate.opsForValue().set(redisKey,objectMapper.writeValueAsString(room));


        }catch(Exception e) {
            System.err.println("同步更新redis失败");
        }finally {
            redisTemplate.delete(lockKey);
        }
    }


    @Override
    public boolean disbandRoomIfHost(String roomId, String userId) {
        String redisKey = "resonance:room:" + roomId;
        String lockKey  = "resonance:lock:room:" + roomId;

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,"LOCKED",10,TimeUnit.SECONDS);

        if(Boolean.FALSE.equals(acquired)){
            throw new RuntimeException("当前有其他操作运行，请稍后再试");
        }


        try{
            String roomJson = redisTemplate.opsForValue().get(redisKey);
            if (roomJson == null) return false;
            Room room = objectMapper.readValue(roomJson,Room.class);
            if(userId.equals(room.getHostId())){
                redisTemplate.delete(redisKey);
                return true;
            }
            return false;

        }catch (Exception e){
            System.err.println("❌ 检查并解散房间失败：" + e.getMessage());
        }finally {
            redisTemplate.delete(lockKey);
        }
        return false;
    }

    @Override
    public boolean joinRoom(String roomId, String userId) {
        //1.组装两个key，一个是房间数据的key，一个是用来做pv操作锁的key

        String roomKey = "resonance:room:" + roomId;

        String lockKey = "resonance:lock:room:" + roomId;

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey,"LOCKED",10,TimeUnit.SECONDS);

        //拿不到锁，说明有线程或者服务器在使用

        if(Boolean.FALSE.equals(acquired)){
            //直接返回错误
            throw new RuntimeException("当前房间太火爆了");
        }

        try{
            String roomJson = redisTemplate.opsForValue().get(roomKey);

            if(roomJson == null){
                throw new RuntimeException("房间已经不存在或已经解散");
            }
            Room room = objectMapper.readValue(roomJson,Room.class);

            room.setParticipantCount(room.getParticipantCount()+1);

            redisTemplate.opsForValue().set(roomKey,objectMapper.writeValueAsString(room));
            return true;
        } catch (Exception e) {
            System.err.println("加入房间异常" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public List<Room> getActiveRoomList(){
        List<Room> roomList = new ArrayList<>();

        try{
            Set<String> keys = redisTemplate.keys("resonance:room:*");
            if(keys!=null && !keys.isEmpty()){
                keys.removeIf(key->key.contains(":lock:"));

                List<String> roomJsons = redisTemplate.opsForValue().multiGet(keys);

                if (roomJsons != null) {
                    for(String json : roomJsons){
                        roomList.add(objectMapper.readValue(json,Room.class));
                    }
                }
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return roomList;
    }
}
