package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人资料更新请求
 */
@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    /** 头像 URL */
    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
