package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.PostService;
import com.blog.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台文章接口
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public Result<PageResult<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Long tagId,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(postService.listPublished(page, size, categoryId, tagId, keyword));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.getDetail(id, false));
    }
}
