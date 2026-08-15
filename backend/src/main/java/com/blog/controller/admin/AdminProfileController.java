package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.PasswordUpdateRequest;
import com.blog.dto.ProfileUpdateRequest;
import com.blog.service.UserService;
import com.blog.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台个人资料接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserService userService;

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(userService.updateProfile(uid, request));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(uid, request);
        return Result.ok();
    }
}
