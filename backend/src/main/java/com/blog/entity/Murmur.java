package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说说（动态）
 */
@Data
@TableName("murmurs")
public class Murmur {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content;

    /** 0 仅自己可见 1 开放 */
    private Integer visibility;

    /** 配图 URL JSON 数组 */
    private String images;

    private Long likeCount;

    private Long commentCount;

    private LocalDateTime createdAt;
}
