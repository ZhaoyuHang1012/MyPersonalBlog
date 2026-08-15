package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.ArchiveRequest;
import com.blog.entity.AlbumGroup;
import com.blog.entity.Murmur;
import com.blog.entity.Post;
import com.blog.service.AlbumService;
import com.blog.service.ArchiveService;
import com.blog.service.MurmurService;
import com.blog.service.PostService;
import com.blog.util.SecurityUtil;
import com.blog.vo.AlbumGroupVO;
import com.blog.vo.MurmurVO;
import com.blog.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 归档收藏接口（需登录）
 */
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final PostService postService;
    private final MurmurService murmurService;
    private final AlbumService albumService;

    /** 我的归档列表（按类型） */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "post") String type) {
        Long userId = SecurityUtil.currentUserId();
        return Result.ok(Map.of(
                "posts", type.equals("post") ? postService.listByIds(archiveService.targetIds(userId, "post")) : List.<PostVO>of(),
                "murmurs", type.equals("murmur") ? murmurService.listByIds(archiveService.targetIds(userId, "murmur")) : List.<MurmurVO>of(),
                "albums", type.equals("album") ? albumService.listGroupsByIds(archiveService.targetIds(userId, "album")) : List.<AlbumGroupVO>of()
        ));
    }

    /** 归档状态 */
    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam String targetType, @RequestParam Long targetId) {
        boolean archived = archiveService.isArchived(SecurityUtil.currentUserId(), targetType, targetId);
        return Result.ok(Map.of("archived", archived));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody ArchiveRequest request) {
        archiveService.add(SecurityUtil.currentUserId(), request.getTargetType(), request.getTargetId());
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> remove(@RequestParam String targetType, @RequestParam Long targetId) {
        archiveService.remove(SecurityUtil.currentUserId(), targetType, targetId);
        return Result.ok();
    }
}
