package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码请求
 */
@Data
public class AdminPasswordResetRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度需在 6-50 位之间")
    private String newPassword;
}
