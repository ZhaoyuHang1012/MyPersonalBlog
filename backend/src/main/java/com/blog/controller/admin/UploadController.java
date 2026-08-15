package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.service.FileService;
import com.blog.vo.UploadFileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台文件上传接口
 */
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileService fileService;

    @PostMapping
    public Result<UploadFileVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileService.store(file));
    }
}
