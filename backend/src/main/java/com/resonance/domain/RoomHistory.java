package com.resonance.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("room_history")
@Data
public class RoomHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomId;
    private String roomName;
    private String hostId;
    private LocalDateTime createAt;

}
