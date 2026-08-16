package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.dto.MurmurRequest;
import com.blog.entity.Comment;
import com.blog.entity.Murmur;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.MurmurMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.MurmurVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 说说服务：个人动态（公开大厅可见，私密仅自己），支持配图
 */
@Service
@RequiredArgsConstructor
public class MurmurService {

    private static final int MAX_IMAGES = 9;

    private final MurmurMapper murmurMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ObjectMapper objectMapper;
    private final FriendService friendService;

    /** 大厅：登录用户展示自己+好友的说说（公共+仅好友可见）；未登录仅展示公共说说 */
    public PageResult<MurmurVO> listPublic(int page, int size, Long viewerId) {
        QueryWrapper<Murmur> qw = new QueryWrapper<>();
        if (viewerId == null) {
            qw.eq("visibility", 1);
        } else {
            qw.in("visibility", 1, 2);
            qw.and(w -> w.eq("user_id", viewerId)
                    .or().inSql("user_id",
                            "SELECT friend_id FROM friends WHERE user_id = " + viewerId));
        }
        qw.orderByDesc("id");
        Page<Murmur> result = murmurMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    /** 某用户的说说列表（个人博客页，按查看者权限过滤） */
    public PageResult<MurmurVO> listUserMurmurs(String username, int page, int size, Long viewerId) {
        User author = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (author == null) {
            throw new BizException(404, "用户不存在");
        }
        QueryWrapper<Murmur> qw = new QueryWrapper<>();
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
        Page<Murmur> result = murmurMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    /** 按 ID 列表组装 VO（归档列表等场景） */
    public List<MurmurVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Murmur> murmurs = murmurMapper.selectBatchIds(ids);
        Map<Long, Murmur> byId = murmurs.stream().collect(Collectors.toMap(Murmur::getId, m -> m));
        List<Murmur> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
        return toVOList(ordered);
    }

    /** 我的/全部说说（普通用户仅自己的，管理员可选按作者筛选） */
    public PageResult<MurmurVO> listAdmin(Long operatorId, boolean isAdmin, int page, int size, Long authorId) {
        QueryWrapper<Murmur> qw = new QueryWrapper<>();
        if (!isAdmin) {
            qw.eq("user_id", operatorId);
        } else if (authorId != null) {
            qw.eq("user_id", authorId);
        }
        qw.orderByDesc("id");
        Page<Murmur> result = murmurMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    @Transactional
    public MurmurVO create(Long userId, MurmurRequest request) {
        List<String> images = normalizeImages(request.getImages());
        if (images.size() > MAX_IMAGES) {
            throw new BizException("配图最多 " + MAX_IMAGES + " 张");
        }
        Murmur murmur = new Murmur();
        murmur.setUserId(userId);
        murmur.setContent(request.getContent().trim());
        murmur.setVisibility(request.getVisibility() == null ? 1 : request.getVisibility());
        murmur.setImages(images.isEmpty() ? null : writeImages(images));
        murmur.setCreatedAt(LocalDateTime.now());
        murmurMapper.insert(murmur);
        return toVOList(List.of(murmur)).get(0);
    }

    /** 编辑说说（内容/配图/可见性） */
    @Transactional
    public MurmurVO update(Long operatorId, boolean isAdmin, Long id, MurmurRequest request) {
        Murmur murmur = murmurMapper.selectById(id);
        if (murmur == null) {
            throw new BizException(404, "说说不存在");
        }
        if (!isAdmin && !operatorId.equals(murmur.getUserId())) {
            throw new BizException(403, "无权修改他人说说");
        }
        List<String> images = normalizeImages(request.getImages());
        if (images.size() > MAX_IMAGES) {
            throw new BizException("配图最多 " + MAX_IMAGES + " 张");
        }
        murmur.setContent(request.getContent().trim());
        murmur.setVisibility(request.getVisibility() == null ? 1 : request.getVisibility());
        murmur.setImages(images.isEmpty() ? null : writeImages(images));
        murmurMapper.updateById(murmur);
        return toVOList(List.of(murmur)).get(0);
    }

    @Transactional
    public void delete(Long operatorId, boolean isAdmin, Long id) {
        Murmur murmur = murmurMapper.selectById(id);
        if (murmur == null) {
            throw new BizException(404, "说说不存在");
        }
        if (!isAdmin && !operatorId.equals(murmur.getUserId())) {
            throw new BizException(403, "无权删除他人说说");
        }
        murmurMapper.deleteById(id);
        // 级联删除说说下的评论
        commentMapper.delete(new QueryWrapper<Comment>().eq("murmur_id", id));
    }

    private List<String> normalizeImages(List<String> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream().filter(Objects::nonNull)
                .map(String::trim).filter(s -> !s.isEmpty())
                .filter(s -> s.startsWith("/uploads/") || s.startsWith("http"))
                .distinct().collect(Collectors.toList());
    }

    private String writeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> readImages(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<MurmurVO> toVOList(List<Murmur> murmurs) {
        if (murmurs == null || murmurs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = murmurs.stream().map(Murmur::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // HashMap 而非 Map.of()：Map.of() 对 null key 会抛 NPE
        Map<Long, User> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            users.putAll(userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u)));
        }
        return murmurs.stream().map(m -> {
            MurmurVO vo = new MurmurVO();
            vo.setId(m.getId());
            vo.setUserId(m.getUserId());
            vo.setContent(m.getContent());
            vo.setVisibility(m.getVisibility());
            vo.setImages(readImages(m.getImages()));
            vo.setLikeCount(m.getLikeCount() == null ? 0L : m.getLikeCount());
            vo.setCommentCount(m.getCommentCount() == null ? 0L : m.getCommentCount());
            vo.setCreatedAt(m.getCreatedAt());
            User u = users.get(m.getUserId());
            if (u != null) {
                MurmurVO.AuthorVO av = new MurmurVO.AuthorVO();
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
