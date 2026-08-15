package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签信息（含文章数）
 */
@Data
public class TagVO {

    private Long id;

    private String name;

    private LocalDateTime createdAt;

    private Long postCount;
}
