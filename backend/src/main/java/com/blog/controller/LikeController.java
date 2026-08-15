package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.LikeService;
import com.blog.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 点赞接口（需登录点赞；状态与列表公开查询）
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody LikeRequest request) {
        likeService.add(SecurityUtil.currentUserId(), request.getTargetType(), request.getTargetId());
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> remove(@RequestParam String targetType, @RequestParam Long targetId) {
        likeService.remove(SecurityUtil.currentUserId(), targetType, targetId);
        return Result.ok();
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.ok(likeService.status(SecurityUtil.currentUserId(), targetType, targetId));
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> likers(@RequestParam String targetType, @RequestParam Long targetId) {
        return Result.ok(likeService.likers(targetType, targetId));
    }

    @Data
    public static class LikeRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;

        @NotNull(message = "目标 ID 不能为空")
        private Long targetId;
    }
}
