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
import java.util.HashMap;
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
    private final FriendService friendService;

    // ==================== 前台 ====================

    /** 大厅：登录用户展示自己+好友的相册（公共+仅好友可见）；未登录仅展示公共相册 */
    public List<AlbumGroupVO> listPublic(Long viewerId) {
        QueryWrapper<AlbumGroup> qw = new QueryWrapper<>();
        if (viewerId == null) {
            qw.eq("visibility", 1);
        } else {
            qw.in("visibility", 1, 2);
            qw.and(w -> w.eq("user_id", viewerId)
                    .or().inSql("user_id",
                            "SELECT friend_id FROM friends WHERE user_id = " + viewerId));
        }
        qw.orderByDesc("id");
        return toVOList(groupMapper.selectList(qw));
    }

    /** 某用户的相册列表（个人博客页，按查看者权限过滤） */
    public List<AlbumGroupVO> listUserAlbums(String username, Long viewerId) {
        User author = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (author == null) {
            throw new BizException(404, "用户不存在");
        }
        QueryWrapper<AlbumGroup> qw = new QueryWrapper<>();
        qw.eq("user_id", author.getId());
        if (viewerId == null) {
            qw.eq("visibility", 1);
        } else if (viewerId.equals(author.getId())) {
            qw.in("visibility", 0, 1, 2);
        } else if (friendService.isFriendOf(viewerId, author.getId())) {
            qw.in("visibility", 1, 2);
        } else {
            qw.eq("visibility", 1);
        }
        qw.orderByDesc("id");
        return toVOList(groupMapper.selectList(qw));
    }

    /** 按 ID 列表组装 VO（归档列表等场景） */
    public List<AlbumGroupVO> listGroupsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<AlbumGroup> groups = groupMapper.selectBatchIds(ids);
        Map<Long, AlbumGroup> byId = groups.stream().collect(Collectors.toMap(AlbumGroup::getId, g -> g));
        List<AlbumGroup> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
        return toVOList(ordered);
    }

    /** 相册详情（公共任何人；仅好友可见仅作者与好友；仅自己可见仅所有者） */
    public Map<String, Object> detail(Long groupId, Long viewerId) {
        AlbumGroup group = requireGroup(groupId);
        if (!friendService.canViewContent(group.getVisibility(), group.getUserId(), viewerId)) {
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
        // HashMap 而非 Map.of()：Map.of() 对 null key 会抛 NPE
        Map<Long, User> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            users.putAll(userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u)));
        }
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
