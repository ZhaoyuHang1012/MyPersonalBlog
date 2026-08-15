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
}
