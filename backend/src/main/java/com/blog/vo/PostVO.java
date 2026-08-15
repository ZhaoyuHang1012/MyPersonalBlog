package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章信息
 */
@Data
public class PostVO {

    private Long id;

    private String title;

    private String summary;

    /** 仅详情接口返回 */
    private String contentMd;

    /** 仅详情接口返回 */
    private String contentHtml;

    private Long categoryId;

    private String categoryName;

    private Integer status;

    private Integer isTop;

    private Long viewCount;

    private Long commentCount;

    private List<TagVO> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    /** 上一篇 */
    private LinkVO prev;

    /** 下一篇 */
    private LinkVO next;

    @Data
    @AllArgsConstructor
    public static class LinkVO {
        private Long id;
        private String title;
    }
}
