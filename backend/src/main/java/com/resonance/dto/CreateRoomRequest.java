package com.resonance.dto;

import lombok.Data;

//这个类专门用来接受前端发来的JSON数据
@Data
public class CreateRoomRequest {
    private String roomName;
    private String hostId;

}
