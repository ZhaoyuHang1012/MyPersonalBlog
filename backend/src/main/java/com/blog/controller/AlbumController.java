package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.AlbumService;
import com.blog.util.SecurityUtil;
import com.blog.vo.AlbumGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 前台相册接口（全站公开相册）
 */
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Result<List<AlbumGroupVO>> list() {
        return Result.ok(albumService.listPublic());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(albumService.detail(id, SecurityUtil.currentUserId()));
    }
}
