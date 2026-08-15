package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问记录
 */
@Data
@TableName("visits")
public class Visit {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章 ID（列表页访问为空） */
    private Long postId;

    private String path;

    private String ip;

    private LocalDateTime createdAt;
}
