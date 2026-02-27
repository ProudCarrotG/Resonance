package com.resonance.dto;

import lombok.Data;

@Data
public class RoomMessage {
    // 动作类型：比如 "JOIN"(加入房间), "PLAY"(播放), "PAUSE"(暂停), "SEEK"(拖动进度条)
    private String type;

    // 房间号：明确这个动作是在哪个包厢发生的
    private String roomId;

    // 用户ID：是谁干的
    private String userId;

    // 动作附带的具体数据：比如切歌时的 trackId，或者拖动进度条的毫秒数
    private Object data;
}
