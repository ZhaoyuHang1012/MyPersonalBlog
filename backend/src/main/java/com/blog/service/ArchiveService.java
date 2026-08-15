package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.entity.Archive;
import com.blog.entity.Post;
import com.blog.entity.Murmur;
import com.blog.entity.AlbumGroup;
import com.blog.mapper.ArchiveMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.MurmurMapper;
import com.blog.mapper.AlbumGroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 归档收藏服务：文章/说说/相册
 */
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private static final Set<String> TYPES = Set.of("post", "murmur", "album");

    private final ArchiveMapper archiveMapper;
    private final PostMapper postMapper;
    private final MurmurMapper murmurMapper;
    private final AlbumGroupMapper albumGroupMapper;

    @Transactional
    public void add(Long userId, String type, Long targetId) {
        checkType(type);
        checkTargetExists(type, targetId);
        Long count = archiveMapper.selectCount(new QueryWrapper<Archive>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId));
        if (count > 0) {
            return; // 已归档则忽略
        }
        Archive archive = new Archive();
        archive.setUserId(userId);
        archive.setTargetType(type);
        archive.setTargetId(targetId);
        archive.setCreatedAt(LocalDateTime.now());
        archiveMapper.insert(archive);
    }

    @Transactional
    public void remove(Long userId, String type, Long targetId) {
        checkType(type);
        archiveMapper.delete(new QueryWrapper<Archive>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId));
    }

    public boolean isArchived(Long userId, String type, Long targetId) {
        if (userId == null) {
            return false;
        }
        return archiveMapper.selectCount(new QueryWrapper<Archive>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId)) > 0;
    }

    /** 指定类型的归档目标 ID 列表 */
    public List<Long> targetIds(Long userId, String type) {
        return archiveMapper.selectList(new QueryWrapper<Archive>()
                .eq("user_id", userId).eq("target_type", type).orderByDesc("id"))
                .stream().map(Archive::getTargetId).collect(Collectors.toList());
    }

    /** 按类型返回归档目标原始实体（供各服务组装 VO） */
    public List<Post> archivedPosts(Long userId) {
        List<Long> ids = targetIds(userId, "post");
        return ids.isEmpty() ? List.of() : postMapper.selectBatchIds(ids);
    }

    public List<Murmur> archivedMurmurs(Long userId) {
        List<Long> ids = targetIds(userId, "murmur");
        return ids.isEmpty() ? List.of() : murmurMapper.selectBatchIds(ids);
    }

    public List<AlbumGroup> archivedAlbums(Long userId) {
        List<Long> ids = targetIds(userId, "album");
        return ids.isEmpty() ? List.of() : albumGroupMapper.selectBatchIds(ids);
    }

    private void checkType(String type) {
        if (type == null || !TYPES.contains(type)) {
            throw new BizException("归档类型仅支持 post / murmur / album");
        }
    }

    private void checkTargetExists(String type, Long targetId) {
        boolean exists = switch (type) {
            case "post" -> postMapper.selectById(targetId) != null;
            case "murmur" -> murmurMapper.selectById(targetId) != null;
            case "album" -> albumGroupMapper.selectById(targetId) != null;
            default -> false;
        };
        if (!exists) {
            throw new BizException(404, "归档目标不存在");
        }
    }
}
