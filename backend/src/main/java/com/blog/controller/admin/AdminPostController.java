package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.PostSaveRequest;
import com.blog.service.PostService;
import com.blog.util.SecurityUtil;
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
 * 后台文章管理接口（普通用户仅能管理自己的文章）
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
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long authorId) {
        return Result.ok(postService.listAdmin(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(),
                page, size, status, keyword, authorId));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.getDetailAdmin(id, SecurityUtil.currentUserId(), SecurityUtil.isAdmin()));
    }

    @PostMapping
    public Result<PostVO> create(@Valid @RequestBody PostSaveRequest request) {
        return Result.ok(postService.create(SecurityUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<PostVO> update(@PathVariable Long id, @Valid @RequestBody PostSaveRequest request) {
        return Result.ok(postService.update(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id);
        return Result.ok();
    }
}
