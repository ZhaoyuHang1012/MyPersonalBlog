package com.blog.vo;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘统计
 */
@Data
public class StatsVO {

    private long postTotal;

    private long published;

    private long drafts;

    private long totalVisits;

    private long todayVisits;

    /** 近 7 天访问趋势 */
    private List<TrendItem> trend;

    /** 浏览量 Top10 */
    private List<TopPost> topPosts;

    @Data
    public static class TrendItem {
        private String date;
        private long count;

        public TrendItem(String date, long count) {
            this.date = date;
            this.count = count;
        }
    }

    @Data
    public static class TopPost {
        private String title;
        private long viewCount;
    }
}
