package com.resonance.service;

import com.resonance.domain.Room;
import com.resonance.dto.RoomMessage;

public interface RoomService {
    /**
     * 创建一个新的听歌房间
     * * @param roomName 房间名称（如："周杰伦专场"）
     * @param hostId   房主ID
     * @return 创建好的房间完整信息
     */
    Room createRoom(String roomName, String hostId);

    /**
     * 根据房间 ID 查询房间信息
     * @param roomId 房间的唯一标识
     * @return 房间完整信息
     */
    Room getRoom(String roomId);

    /**
     * 根据 WebSocket 传来的指令，实时更新 Redis 中的房间播放状态
     */
    void updateRoomState(String roomId, RoomMessage message);


    /**
     * 如果断开连接的是房主，则解散房间（从 Redis 中删除）
     * @return true表示房间被成功解散，false表示不需要解散
     */
    boolean disbandRoomIfHost(String roomId, String userId);
}
