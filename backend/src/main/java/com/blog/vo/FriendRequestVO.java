package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请信息
 */
@Data
public class FriendRequestVO {

    private Long id;

    private String message;

    private Integer status;

    private LocalDateTime createdAt;

    private UserInfo fromUser;

    private UserInfo toUser;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}
