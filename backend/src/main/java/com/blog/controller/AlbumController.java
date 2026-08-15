package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.FileService;
import com.blog.vo.UploadFileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台相册接口（展示媒体库全部图片，上传即入相册）
 */
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final FileService fileService;

    @GetMapping
    public Result<List<UploadFileVO>> list() {
        // 相册展示所有用户上传的图片（上传即入相册）
        return Result.ok(fileService.list(null, 1, 1000, true).getRecords());
    }
}
