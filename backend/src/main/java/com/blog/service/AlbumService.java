package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.dto.AlbumGroupRequest;
import com.blog.dto.AlbumPhotoRequest;
import com.blog.entity.AlbumGroup;
import com.blog.entity.AlbumPhoto;
import com.blog.entity.User;
import com.blog.mapper.AlbumGroupMapper;
import com.blog.mapper.AlbumPhotoMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.AlbumGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 相册服务：用户创建相册组，上传照片/视频；公开相册在大厅展示
 */
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumGroupMapper groupMapper;
    private final AlbumPhotoMapper photoMapper;
    private final UserMapper userMapper;

    // ==================== 前台 ====================

    /** 大厅：全站公开相册组 */
    public List<AlbumGroupVO> listPublic() {
        List<AlbumGroup> groups = groupMapper.selectList(new QueryWrapper<AlbumGroup>()
                .eq("visibility", 1).orderByDesc("id"));
        return toVOList(groups);
    }

    /** 相册详情（公开组任何人可看；私有组仅所有者） */
    public Map<String, Object> detail(Long groupId, Long viewerId) {
        AlbumGroup group = requireGroup(groupId);
        boolean isOwner = viewerId != null && viewerId.equals(group.getUserId());
        if (group.getVisibility() != 1 && !isOwner) {
            throw new BizException(404, "相册不存在");
        }
        AlbumGroupVO vo = toVOList(List.of(group)).get(0);
        List<AlbumPhoto> photos = photoMapper.selectList(new QueryWrapper<AlbumPhoto>()
                .eq("group_id", groupId).orderByAsc("id"));
        return Map.of("group", vo, "photos", photos);
    }

    // ==================== 后台 ====================

    /** 我的相册组（管理员可看全部） */
    public List<AlbumGroupVO> listAdmin(Long operatorId, boolean isAdmin) {
        QueryWrapper<AlbumGroup> qw = new QueryWrapper<>();
        if (!isAdmin) {
            qw.eq("user_id", operatorId);
        }
        qw.orderByDesc("id");
        return toVOList(groupMapper.selectList(qw));
    }

    @Transactional
    public AlbumGroupVO createGroup(Long userId, AlbumGroupRequest request) {
        AlbumGroup group = new AlbumGroup();
        group.setUserId(userId);
        group.setName(request.getName().trim());
        group.setVisibility(request.getVisibility() == null ? 1 : request.getVisibility());
        group.setCreatedAt(LocalDateTime.now());
        groupMapper.insert(group);
        return toVOList(List.of(group)).get(0);
    }

    @Transactional
    public void updateGroup(Long operatorId, boolean isAdmin, Long id, AlbumGroupRequest request) {
        AlbumGroup group = requireOwnedGroup(id, operatorId, isAdmin);
        group.setName(request.getName().trim());
        if (request.getVisibility() != null) {
            group.setVisibility(request.getVisibility());
        }
        groupMapper.updateById(group);
    }

    @Transactional
    public void deleteGroup(Long operatorId, boolean isAdmin, Long id) {
        requireOwnedGroup(id, operatorId, isAdmin);
        groupMapper.deleteById(id);
        photoMapper.delete(new QueryWrapper<AlbumPhoto>().eq("group_id", id));
    }

    @Transactional
    public AlbumPhoto addPhoto(Long operatorId, boolean isAdmin, Long groupId, AlbumPhotoRequest request) {
        AlbumGroup group = requireOwnedGroup(groupId, operatorId, isAdmin);
        String mediaType = "video".equalsIgnoreCase(request.getMediaType()) ? "video" : "image";
        AlbumPhoto photo = new AlbumPhoto();
        photo.setGroupId(groupId);
        photo.setUserId(group.getUserId());
        photo.setUrl(request.getUrl().trim());
        photo.setMediaType(mediaType);
        photo.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        photo.setCreatedAt(LocalDateTime.now());
        photoMapper.insert(photo);
        if (group.getCover() == null || group.getCover().isBlank()) {
            group.setCover(photo.getUrl());
            groupMapper.updateById(group);
        }
        return photo;
    }

    @Transactional
    public void deletePhoto(Long operatorId, boolean isAdmin, Long photoId) {
        AlbumPhoto photo = photoMapper.selectById(photoId);
        if (photo == null) {
            throw new BizException(404, "照片不存在");
        }
        if (!isAdmin && !operatorId.equals(photo.getUserId())) {
            throw new BizException(403, "无权删除他人照片");
        }
        photoMapper.deleteById(photoId);
    }

    private AlbumGroup requireGroup(Long id) {
        AlbumGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new BizException(404, "相册不存在");
        }
        return group;
    }

    private AlbumGroup requireOwnedGroup(Long id, Long operatorId, boolean isAdmin) {
        AlbumGroup group = requireGroup(id);
        if (!isAdmin && !operatorId.equals(group.getUserId())) {
            throw new BizException(403, "无权操作他人相册");
        }
        return group;
    }

    private List<AlbumGroupVO> toVOList(List<AlbumGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> groupIds = groups.stream().map(AlbumGroup::getId).collect(Collectors.toList());
        List<Long> userIds = groups.stream().map(AlbumGroup::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long> counts = photoMapper.selectList(new QueryWrapper<AlbumPhoto>().in("group_id", groupIds))
                .stream().collect(Collectors.groupingBy(AlbumPhoto::getGroupId, Collectors.counting()));
        return groups.stream().map(g -> {
            AlbumGroupVO vo = new AlbumGroupVO();
            vo.setId(g.getId());
            vo.setUserId(g.getUserId());
            vo.setName(g.getName());
            vo.setCover(g.getCover());
            vo.setVisibility(g.getVisibility());
            vo.setPhotoCount(counts.getOrDefault(g.getId(), 0L));
            vo.setCreatedAt(g.getCreatedAt());
            User u = users.get(g.getUserId());
            if (u != null) {
                AlbumGroupVO.AuthorVO av = new AlbumGroupVO.AuthorVO();
                av.setId(u.getId());
                av.setUsername(u.getUsername());
                av.setNickname(u.getNickname());
                av.setAvatar(u.getAvatar());
                vo.setAuthor(av);
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
