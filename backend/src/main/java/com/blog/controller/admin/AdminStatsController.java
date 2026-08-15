package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.service.StatsService;
import com.blog.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台统计接口
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping
    public Result<StatsVO> stats() {
        return Result.ok(statsService.stats());
    }
}
