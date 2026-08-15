package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说说（树洞）
 */
@Data
@TableName("murmurs")
public class Murmur {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private LocalDateTime createdAt;
}
