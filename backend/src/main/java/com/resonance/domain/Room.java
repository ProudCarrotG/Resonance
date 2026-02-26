package com.resonance.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间领域模型 (Entity)
 * 代表一个供多人同步听歌的虚拟房间
 */
@Data
public class Room {

    private String roomId;          // 房间唯一标识（未来可用来生成邀请链接）
    private String roomName;        // 房间名称
    private String hostId;          // 房主标识（用来校验谁有权限切歌/控制进度）

    private String currentTrackId;  // 当前正在播放的歌曲ID
    private Long currentProgress;   // 当前播放进度（精确到毫秒，最核心的同步字段）
    private String playStatus;      // 播放状态（WAITING, PLAYING, PAUSED）

    private LocalDateTime createdAt; // 房间创建时间

}