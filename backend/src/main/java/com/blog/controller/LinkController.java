package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LinkApplyRequest;
import com.blog.entity.FriendLink;
import com.blog.service.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台友链接口
 */
@RestController
@RequestMapping("/api/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    public Result<List<FriendLink>> list() {
        return Result.ok(linkService.listApproved());
    }

    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody LinkApplyRequest request, HttpServletRequest http) {
        linkService.apply(request, clientIp(http));
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
