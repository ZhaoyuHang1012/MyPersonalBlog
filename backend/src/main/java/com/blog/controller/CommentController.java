package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.CommentRequest;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台评论接口（浏览公开，发表需登录）
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public Result<List<CommentVO>> list(@PathVariable Long postId) {
        return Result.ok(commentService.listByPost(postId));
    }

    @PostMapping("/{postId}/comments")
    public Result<Void> submit(@PathVariable Long postId,
                               @RequestBody CommentRequest request) {
        commentService.submit(postId, request, currentUserId());
        return Result.ok();
    }

    /** 从 SecurityContext 获取当前登录用户 ID（接口已受 Spring Security 保护，此处仅作兜底） */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long uid) {
            return uid;
        }
        return null;
    }
}
