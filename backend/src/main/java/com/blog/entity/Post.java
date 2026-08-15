package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章
 */
@Data
@TableName("posts")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者 ID */
    private Long userId;

    private String title;

    private String summary;

    private String contentMd;

    private String contentHtml;

    private Long categoryId;

    /** 0 草稿 1 已发布 */
    private Integer status;

    /** 0 仅自己可见 1 开放 */
    private Integer visibility;

    /** 0 否 1 置顶 */
    private Integer isTop;

    private Long viewCount;

    private Long commentCount;

    private Long likeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}
