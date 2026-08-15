package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档分组（年-月 -> 文章列表）
 */
@Data
public class ArchiveGroupVO {

    private Integer year;

    private Integer month;

    private List<ArchiveItemVO> posts;

    @Data
    @AllArgsConstructor
    public static class ArchiveItemVO {
        private Long id;
        private String title;
        private LocalDateTime publishedAt;
    }
}
