package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.PostService;
import com.blog.vo.ArchiveGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台归档接口
 */
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final PostService postService;

    @GetMapping
    public Result<List<ArchiveGroupVO>> archive() {
        return Result.ok(postService.listArchive());
    }
}
