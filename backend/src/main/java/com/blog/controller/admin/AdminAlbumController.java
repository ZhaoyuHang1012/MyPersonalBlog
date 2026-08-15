package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.dto.AlbumRequest;
import com.blog.entity.Album;
import com.blog.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台相册管理接口
 */
@RestController
@RequestMapping("/api/admin/albums")
@RequiredArgsConstructor
public class AdminAlbumController {

    private final AlbumService albumService;

    @GetMapping
    public Result<List<Album>> list() {
        return Result.ok(albumService.list());
    }

    @PostMapping
    public Result<Album> create(@Valid @RequestBody AlbumRequest request) {
        return Result.ok(albumService.create(request.getUrl(), request.getDescription()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return Result.ok();
    }
}
