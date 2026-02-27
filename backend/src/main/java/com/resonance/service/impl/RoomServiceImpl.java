package com.resonance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resonance.domain.Room;
import com.resonance.service.RoomService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RoomServiceImpl implements RoomService {
    // Spring 官方提供的用来操作 Redis 的强大工具
    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;


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
            String redisKey = "resonance:room" + roomId;

            // 存入 Redis，并施加“阅后即焚”魔法：12小时后这个房间在内存中自动烟消云散！
            redisTemplate.opsForValue().set(redisKey, roomJson, 12, TimeUnit.HOURS);

        }catch (Exception e){
            throw new RuntimeException("系统开小差了");
        }

        return room;
    }

    @Override
    public Room getRoom(String roomId){
        //1.拼装redis的key
        String redisKey = "resonance:room" + roomId;
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
}
