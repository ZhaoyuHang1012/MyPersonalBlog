package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.InviteCode;
import com.blog.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台邀请码管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/admin/invites")
@RequiredArgsConstructor
public class AdminInviteController {

    private final InviteService inviteService;

    @PostMapping
    public Result<List<InviteCode>> generate(@RequestParam(defaultValue = "1") int count) {
        return Result.ok(inviteService.generate(count));
    }

    @GetMapping
    public Result<List<InviteCode>> list() {
        return Result.ok(inviteService.list());
    }
}
