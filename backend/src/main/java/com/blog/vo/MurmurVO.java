package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 说说信息
 */
@Data
public class MurmurVO {

    private Long id;

    private Long userId;

    private String content;

    private Integer visibility;

    private List<String> images;

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
