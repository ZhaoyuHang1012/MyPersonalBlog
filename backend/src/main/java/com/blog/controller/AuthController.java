package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginRequest;
import com.blog.service.AuthService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(authService.getUserById(uid));
    }
}
