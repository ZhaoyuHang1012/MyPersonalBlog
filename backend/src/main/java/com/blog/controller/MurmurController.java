package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Murmur;
import com.blog.service.MurmurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台说说接口
 */
@RestController
@RequestMapping("/api/murmurs")
@RequiredArgsConstructor
public class MurmurController {

    private final MurmurService murmurService;

    @GetMapping
    public Result<List<Murmur>> list() {
        return Result.ok(murmurService.list());
    }
}
