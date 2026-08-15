package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.CommentRequest;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台评论接口
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
                               @Valid @RequestBody CommentRequest request,
                               HttpServletRequest http) {
        commentService.submit(postId, request, clientIp(http));
        return Result.ok();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
