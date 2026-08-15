package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.TimelineService;
import com.blog.util.SecurityUtil;
import com.blog.vo.TimelineGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台时间线归档接口（文章/说说/相册按月份）
 */
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final TimelineService timelineService;

    @GetMapping
    public Result<List<TimelineGroupVO>> archive() {
        return Result.ok(timelineService.timeline(SecurityUtil.currentUserId()));
    }
}
