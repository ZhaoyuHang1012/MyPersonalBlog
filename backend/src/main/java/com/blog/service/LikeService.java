package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.entity.Like;
import com.blog.entity.User;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.MurmurMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 点赞服务：文章/说说
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private static final Set<String> TYPES = Set.of("post", "murmur");

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final MurmurMapper murmurMapper;
    private final UserMapper userMapper;

    @Transactional
    public void add(Long userId, String type, Long targetId) {
        checkType(type);
        checkTargetExists(type, targetId);
        Long count = likeMapper.selectCount(new QueryWrapper<Like>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId));
        if (count > 0) {
            return; // 已点赞，幂等
        }
        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(type);
        like.setTargetId(targetId);
        like.setCreatedAt(LocalDateTime.now());
        likeMapper.insert(like);
        incrCount(type, targetId, 1);
    }

    @Transactional
    public void remove(Long userId, String type, Long targetId) {
        checkType(type);
        Long deleted = Long.valueOf(likeMapper.delete(new QueryWrapper<Like>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId)));
        if (deleted > 0) {
            incrCount(type, targetId, -1);
        }
    }

    public Map<String, Object> status(Long userId, String type, Long targetId) {
        checkType(type);
        boolean liked = userId != null && likeMapper.selectCount(new QueryWrapper<Like>()
                .eq("user_id", userId).eq("target_type", type).eq("target_id", targetId)) > 0;
        long count = likeMapper.selectCount(new QueryWrapper<Like>()
                .eq("target_type", type).eq("target_id", targetId));
        return Map.of("liked", liked, "count", count);
    }

    /** 点赞用户列表（头像昵称） */
    public List<Map<String, Object>> likers(String type, Long targetId) {
        List<Like> likes = likeMapper.selectList(new QueryWrapper<Like>()
                .eq("target_type", type).eq("target_id", targetId)
                .orderByDesc("id").last("LIMIT 50"));
        if (likes.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = likes.stream().map(Like::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> users = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return likes.stream().map(l -> {
            User u = users.get(l.getUserId());
            if (u == null) {
                return null;
            }
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar() == null ? "" : u.getAvatar());
            return m;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    private void incrCount(String type, Long targetId, int delta) {
        if ("post".equals(type)) {
            if (delta > 0) {
                postMapper.incrLikeCount(targetId);
            } else {
                postMapper.decrLikeCount(targetId);
            }
        } else {
            if (delta > 0) {
                murmurMapper.incrLikeCount(targetId);
            } else {
                murmurMapper.decrLikeCount(targetId);
            }
        }
    }

    private void checkType(String type) {
        if (type == null || !TYPES.contains(type)) {
            throw new BizException("点赞类型仅支持 post / murmur");
        }
    }

    private void checkTargetExists(String type, Long targetId) {
        boolean exists = "post".equals(type)
                ? postMapper.selectById(targetId) != null
                : murmurMapper.selectById(targetId) != null;
        if (!exists) {
            throw new BizException(404, "点赞目标不存在");
        }
    }
}
