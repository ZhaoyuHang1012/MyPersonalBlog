package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.FileService;
import com.blog.util.SecurityUtil;
import com.blog.vo.UploadFileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台媒体库接口（普通用户仅自己的；管理员可查看全部）
 */
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public Result<PageResult<UploadFileVO>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "24") int size,
                                                 @RequestParam(required = false) String username) {
        boolean all = SecurityUtil.isAdmin();
        // 普通用户忽略 username 参数，仅能查看自己的
        return Result.ok(fileService.list(SecurityUtil.currentUserId(), page, size, all,
                all ? username : null));
    }

    /** 存储空间用量 */
    @GetMapping("/usage")
    public Result<Map<String, Object>> usage() {
        Long userId = SecurityUtil.currentUserId();
        return Result.ok(Map.of(
                "usage", fileService.usage(userId),
                "quota", fileService.quotaOf(userId)
        ));
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam("name") String name) {
        fileService.delete(SecurityUtil.currentUserId(), name, SecurityUtil.isAdmin());
        return Result.ok();
    }
}
