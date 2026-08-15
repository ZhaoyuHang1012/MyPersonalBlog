package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.AlbumGroupRequest;
import com.blog.dto.AlbumPhotoRequest;
import com.blog.entity.AlbumPhoto;
import com.blog.service.AlbumService;
import com.blog.util.SecurityUtil;
import com.blog.vo.AlbumGroupVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 我的相册管理接口（普通用户仅自己的；管理员全部）
 */
@RestController
@RequestMapping("/api/admin/albums")
@RequiredArgsConstructor
public class AdminAlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Result<List<AlbumGroupVO>> list() {
        return Result.ok(albumService.listAdmin(SecurityUtil.currentUserId(), SecurityUtil.isAdmin()));
    }

    @PostMapping
    public Result<AlbumGroupVO> create(@Valid @RequestBody AlbumGroupRequest request) {
        return Result.ok(albumService.createGroup(SecurityUtil.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AlbumGroupRequest request) {
        albumService.updateGroup(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        albumService.deleteGroup(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/photos")
    public Result<AlbumPhoto> addPhoto(@PathVariable Long id, @Valid @RequestBody AlbumPhotoRequest request) {
        return Result.ok(albumService.addPhoto(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), id, request));
    }

    @DeleteMapping("/photos/{photoId}")
    public Result<Void> deletePhoto(@PathVariable Long photoId) {
        albumService.deletePhoto(SecurityUtil.currentUserId(), SecurityUtil.isAdmin(), photoId);
        return Result.ok();
    }
}
