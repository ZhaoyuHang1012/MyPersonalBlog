package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类信息（含文章数）
 */
@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Integer sort;

    private LocalDateTime createdAt;

    private Long postCount;
}
