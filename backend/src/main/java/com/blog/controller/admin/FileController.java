package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.FileService;
import com.blog.vo.UploadFileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台媒体库接口
 */
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public Result<PageResult<UploadFileVO>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "24") int size) {
        return Result.ok(fileService.list(page, size));
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam("name") String name) {
        fileService.delete(name);
        return Result.ok();
    }
}
