package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.MurmurService;
import com.blog.vo.MurmurVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台说说接口（全站公开动态流）
 */
@RestController
@RequestMapping("/api/murmurs")
@RequiredArgsConstructor
public class MurmurController {

    private final MurmurService murmurService;

    @GetMapping
    public Result<PageResult<MurmurVO>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(murmurService.listPublic(page, size));
    }
}
