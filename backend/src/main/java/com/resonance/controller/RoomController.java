package com.resonance.controller;

import com.resonance.common.ApiResponse;
import com.resonance.domain.Room;
import com.resonance.dto.CreateRoomRequest;
import com.resonance.service.RoomService;
import com.resonance.service.impl.RoomServiceImpl;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResponse<Room> createRoom(@RequestBody CreateRoomRequest request){
        //Controller 本身不写业务逻辑，只负责“呼叫” Service 去干活
        Room room =  roomService.createRoom(request.getRoomName(),request.getHostId());

        return ApiResponse.success(room);
    }
    /**
     * 查询房间状态的 API 接口
     * 访问路径：GET http://localhost:8080/api/rooms/{roomId}
     */
    @GetMapping("/{roomId}")
    public ApiResponse<Room> getRoom(@PathVariable String roomId) {
        return ApiResponse.success(roomService.getRoom(roomId));
    }

    /**
     * 获取当前活跃的大厅
     */

    @GetMapping("/active")
    public ApiResponse<List<Room>> getActiveRooms(){
        return ApiResponse.success(roomService.getActiveRoomList());
    }

    /**
     * 加入房间
     */

    @PostMapping("/{roomId}/join")

    public ApiResponse<Boolean> joinRoom(@PathVariable String roomId,@RequestParam String userId ){
        return ApiResponse.success(roomService.joinRoom(roomId,userId));
    }


}
