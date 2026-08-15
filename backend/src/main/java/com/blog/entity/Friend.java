package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系
 */
@Data
@TableName("friends")
public class Friend {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long friendId;

    private LocalDateTime createdAt;
}
