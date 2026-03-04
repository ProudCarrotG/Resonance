package com.resonance.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    // 明确告诉 MyBatis-Plus：这是一个字符串类型的 ID，请帮我自动生成 UUID（去掉横线）
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String username;
    private String password;
    private LocalDateTime createdAt;
}