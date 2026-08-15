package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.PostSaveRequest;
import com.blog.service.PostService;
import com.blog.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台文章管理接口
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @GetMapping
    public Result<PageResult<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(postService.listAdmin(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.getDetailAdmin(id));
    }

    @PostMapping
    public Result<PostVO> create(@Valid @RequestBody PostSaveRequest request) {
        return Result.ok(postService.create(request));
    }

    @PutMapping("/{id}")
    public Result<PostVO> update(@PathVariable Long id, @Valid @RequestBody PostSaveRequest request) {
        return Result.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.ok();
    }
}
