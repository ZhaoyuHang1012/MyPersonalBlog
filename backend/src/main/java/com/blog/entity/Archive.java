package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 归档收藏
 */
@Data
@TableName("archives")
public class Archive {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** post / murmur / album */
    private String targetType;

    private Long targetId;

    private LocalDateTime createdAt;
}
