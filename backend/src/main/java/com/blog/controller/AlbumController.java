package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Album;
import com.blog.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台相册接口
 */
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Result<List<Album>> list() {
        return Result.ok(albumService.list());
    }
}
