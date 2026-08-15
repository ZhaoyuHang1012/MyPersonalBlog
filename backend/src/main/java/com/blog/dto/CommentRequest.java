package com.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论提交请求
 * 已登录用户无需填写身份信息（由后端从 JWT 自动识别）；未登录访客需填写昵称/邮箱
 */
@Data
public class CommentRequest {

    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过 100 个字符")
    private String email;

    @Size(max = 200, message = "网址不能超过 200 个字符")
    private String website;

    @Size(max = 1000, message = "评论内容不能超过 1000 个字符")
    private String content;

    /** 回复的评论 ID（楼中楼），为空表示顶级评论 */
    private Long parentId;
}
