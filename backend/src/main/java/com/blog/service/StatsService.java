package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.Post;
import com.blog.mapper.PostMapper;
import com.blog.mapper.VisitMapper;
import com.blog.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务：仪表盘数据
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final PostMapper postMapper;
    private final VisitMapper visitMapper;

    public StatsVO stats() {
        StatsVO vo = new StatsVO();
        vo.setPostTotal(postMapper.selectCount(null));
        vo.setPublished(postMapper.selectCount(new QueryWrapper<Post>().eq("status", 1)));
        vo.setDrafts(postMapper.selectCount(new QueryWrapper<Post>().eq("status", 0)));
        vo.setTotalVisits(visitMapper.selectCount(null));
        vo.setTodayVisits(visitMapper.selectCount(new QueryWrapper<com.blog.entity.Visit>()
                .ge("created_at", LocalDate.now().atStartOfDay())));
        vo.setTrend(buildTrend());
        vo.setTopPosts(buildTopPosts());
        return vo;
    }

    /** 近 7 天访问趋势（缺天补零） */
    private List<StatsVO.TrendItem> buildTrend() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        List<Map<String, Object>> rows = visitMapper.countByDay(startDate.atStartOfDay());
        Map<String, Long> byDate = rows.stream().collect(Collectors.toMap(
                r -> String.valueOf(r.get("date")),
                r -> ((Number) r.get("count")).longValue()));
        List<StatsVO.TrendItem> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            String date = startDate.plusDays(i).format(fmt);
            trend.add(new StatsVO.TrendItem(date, byDate.getOrDefault(date, 0L)));
        }
        return trend;
    }

    /** 文章浏览量 Top10 */
    private List<StatsVO.TopPost> buildTopPosts() {
        List<Post> posts = postMapper.selectList(new QueryWrapper<Post>()
                .select("title", "view_count")
                .eq("status", 1)
                .orderByDesc("view_count")
                .last("LIMIT 10"));
        return posts.stream().map(p -> {
            StatsVO.TopPost tp = new StatsVO.TopPost();
            tp.setTitle(p.getTitle());
            tp.setViewCount(p.getViewCount() == null ? 0 : p.getViewCount());
            return tp;
        }).collect(Collectors.toList());
    }
}
