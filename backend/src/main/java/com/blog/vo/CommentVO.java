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

    /** 评论者用户 ID（用于识别文章作者） */
    private Long userId;

    /** 评论者头像（实时读取用户当前头像，用户更换头像后评论展示随之更新） */
    private String avatar;

    private String content;

    /** 0 待审核 1 已通过 2 垃圾 */
    private Integer status;

    private LocalDateTime createdAt;

    /** 后台列表用：所属文章标题 */
    private String postTitle;

    /** 楼中楼子回复 */
    private List<CommentVO> children;
}
