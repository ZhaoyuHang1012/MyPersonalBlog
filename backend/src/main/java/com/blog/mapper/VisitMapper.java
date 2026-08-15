package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Visit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VisitMapper extends BaseMapper<Visit> {

    /** 按天统计访问量 */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM visits WHERE created_at >= #{start} " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY date ASC")
    List<Map<String, Object>> countByDay(@Param("start") LocalDateTime start);
}
