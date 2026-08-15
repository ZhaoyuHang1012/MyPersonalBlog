package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册组信息
 */
@Data
public class AlbumGroupVO {

    private Long id;

    private Long userId;

    private String name;

    private String cover;

    private Integer visibility;

    private Long photoCount;

    private LocalDateTime createdAt;

    private AuthorVO author;

    @Data
    public static class AuthorVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}
