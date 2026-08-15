package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论
 */
@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    /** 父评论 ID（楼中楼回复） */
    private Long parentId;

    private String nickname;

    private String email;

    /** 评论者用户 ID（登录用户评论时记录） */
    private Long userId;

    private String website;

    private String content;

    /** 0 待审核 1 已通过 2 垃圾 */
    private Integer status;

    private LocalDateTime createdAt;
}
