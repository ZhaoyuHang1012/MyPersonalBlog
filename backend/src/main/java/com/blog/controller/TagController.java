package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.TagService;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台标签接口
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<TagVO>> list() {
        return Result.ok(tagService.list());
    }
}
