package com.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间线归档分组（年-月 -> 内容列表）
 */
@Data
public class TimelineGroupVO {

    private Integer year;

    private Integer month;

    private List<ItemVO> items = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class ItemVO {
        /** post / murmur / album */
        private String type;
        private Long id;
        /** 标题（说说是内容截断） */
        private String title;
        private LocalDateTime createdAt;
    }
}
