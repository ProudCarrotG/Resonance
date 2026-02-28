package com.resonance.controller;

import com.resonance.domain.Room;
import com.resonance.dto.CreateRoomRequest;
import com.resonance.service.RoomService;
import com.resonance.service.impl.RoomServiceImpl;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController //告诉 Spring：这是一个负责返回 JSON 数据的 API 接口类
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService){
        this.roomService = roomService;
    }

    /**
     * 创建房间的API接口
     * 访问路径 ： POST http://localhost:8080/api/rooms/create
     */

    @PostMapping("/create")
    public Room createRoom(@RequestBody CreateRoomRequest request){
        //Controller 本身不写业务逻辑，只负责“呼叫” Service 去干活
        return roomService.createRoom(request.getRoomName(),request.getHostId());
    }
    /**
     * 查询房间状态的 API 接口
     * 访问路径：GET http://localhost:8080/api/rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public Room getRoom(@PathVariable String roomId) {
        return roomService.getRoom(roomId);
    }
}
