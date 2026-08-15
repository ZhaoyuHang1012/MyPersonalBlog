package com.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论提交请求（仅登录用户可评论，身份由 JWT 自动识别，无需填写用户信息）
 */
@Data
public class CommentRequest {

    @Size(max = 1000, message = "评论内容不能超过 1000 个字符")
    private String content;

    /** 回复的评论 ID（楼中楼），为空表示顶级评论 */
    private Long parentId;
}
