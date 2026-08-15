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

/**
 * 大厅接口：所有用户「开放」的文章流
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
                                           @RequestParam(required = false) Long tagId,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(postService.listHall(page, size, categoryId, tagId, keyword));
    }
}
