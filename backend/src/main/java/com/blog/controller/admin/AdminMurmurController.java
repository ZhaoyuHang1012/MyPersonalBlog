package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.MurmurRequest;
import com.blog.service.MurmurService;
import com.blog.util.SecurityUtil;
import com.blog.vo.MurmurVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的说说管理接口（普通用户仅自己的；管理员全部）
 */
@RestController
@RequestMapping("/api/admin/murmurs")
@RequiredArgsConstructor
public class AdminMurmurController {

    private final MurmurService murmurService;

    @GetMapping
    public Result<PageResult<MurmurVO>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(murmurService.listAdmin(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), page, size));
    }

    @PostMapping
    public Result<MurmurVO> create(@Valid @RequestBody MurmurRequest request) {
        return Result.ok(murmurService.create(SecurityUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<MurmurVO> update(@PathVariable Long id, @Valid @RequestBody MurmurRequest request) {
        return Result.ok(murmurService.update(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        murmurService.delete(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id);
        return Result.ok();
    }
}
