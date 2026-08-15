package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.PostService;
import com.blog.util.SecurityUtil;
import com.blog.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大厅接口：登录用户展示自己+好友的开放内容；未登录展示全部开放内容
 */
@RestController
@RequestMapping("/api/hall")
@RequiredArgsConstructor
public class HallController {

    private final PostService postService;

    @GetMapping
    public Result<PageResult<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String tagIds,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(postService.listHall(page, size, categoryId,
                parseIds(tagIds), keyword, SecurityUtil.currentUserId()));
    }

    static List<Long> parseIds(String tagIds) {
        if (tagIds == null || tagIds.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tagIds.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toList());
    }
}
