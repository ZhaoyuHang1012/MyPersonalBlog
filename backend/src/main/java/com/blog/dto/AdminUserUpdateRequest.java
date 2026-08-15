package com.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员修改用户信息请求（字段为空表示不修改）
 */
@Data
public class AdminUserUpdateRequest {

    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    /** ADMIN / USER */
    private String role;

    /** 存储配额（字节），需大于 0 */
    private Long quota;
}
