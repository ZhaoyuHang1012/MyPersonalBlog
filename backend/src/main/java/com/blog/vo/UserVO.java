package com.blog.vo;

import lombok.Data;

/**
 * 用户信息
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    /** ADMIN / USER */
    private String role;

    /** 存储配额（字节） */
    private Long quota;
}
