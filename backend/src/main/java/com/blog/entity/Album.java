package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册
 */
@Data
@TableName("albums")
public class Album {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String url;

    private String description;

    private LocalDateTime createdAt;
}
