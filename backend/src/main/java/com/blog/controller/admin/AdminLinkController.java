package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.LinkSaveRequest;
import com.blog.entity.FriendLink;
import com.blog.service.LinkService;
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
 * 后台友链管理接口
 */
@RestController
@RequestMapping("/api/admin/links")
@RequiredArgsConstructor
public class AdminLinkController {

    private final LinkService linkService;

    @GetMapping
    public Result<PageResult<FriendLink>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Integer status) {
        return Result.ok(linkService.listAdmin(page, size, status));
    }

    @PostMapping
    public Result<FriendLink> create(@Valid @RequestBody LinkSaveRequest request) {
        return Result.ok(linkService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody LinkSaveRequest request) {
        linkService.update(id, request);
        return Result.ok();
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        linkService.approve(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        linkService.delete(id);
        return Result.ok();
    }
}
