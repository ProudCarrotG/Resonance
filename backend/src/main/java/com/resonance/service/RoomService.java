package com.resonance.service;

import com.resonance.domain.Room;

public interface RoomService {
    /**
     * 创建一个新的听歌房间
     * * @param roomName 房间名称（如："周杰伦专场"）
     * @param hostId   房主ID
     * @return 创建好的房间完整信息
     */
    Room createRoom(String roomName, String hostId);
}
