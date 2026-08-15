package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论信息（前台树形展示 / 后台列表展示）
 */
@Data
public class CommentVO {

    private Long id;

    private Long postId;

    private Long parentId;

    private String nickname;

    private String website;

    private String content;

    private LocalDateTime createdAt;

    /** 后台列表用：所属文章标题 */
    private String postTitle;

    /** 楼中楼子回复 */
    private List<CommentVO> children;
}
