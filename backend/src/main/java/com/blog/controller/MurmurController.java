package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CommentRequest;
import com.blog.service.CommentService;
import com.blog.service.MurmurService;
import com.blog.util.SecurityUtil;
import com.blog.vo.CommentVO;
import com.blog.vo.MurmurVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台说说接口（大厅：登录用户自己+好友的公开动态；未登录全部公开）
 * 附：说说评论接口（与文章评论同款，后台统一管理）
 */
@RestController
@RequestMapping("/api/murmurs")
@RequiredArgsConstructor
public class MurmurController {

    private final MurmurService murmurService;
    private final CommentService commentService;

    @GetMapping
    public Result<PageResult<MurmurVO>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(murmurService.listPublic(page, size, SecurityUtil.currentUserId()));
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> comments(@PathVariable Long id) {
        return Result.ok(commentService.listByMurmur(id));
    }

    @PostMapping("/{id}/comments")
    public Result<Void> submitComment(@PathVariable Long id,
                                      @RequestBody CommentRequest request) {
        commentService.submitMurmur(id, request, SecurityUtil.currentUserId());
        return Result.ok();
    }
}
