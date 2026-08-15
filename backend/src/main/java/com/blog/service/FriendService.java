package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.dto.FriendRequestSendRequest;
import com.blog.entity.Friend;
import com.blog.entity.FriendRequest;
import com.blog.entity.User;
import com.blog.mapper.FriendMapper;
import com.blog.mapper.FriendRequestMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.FriendRequestVO;
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
 * 好友服务：申请 / 同意 / 拒绝 / 列表 / 删除
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper requestMapper;
    private final UserMapper userMapper;

    /** 发送好友申请 */
    @Transactional
    public void sendRequest(Long fromUserId, FriendRequestSendRequest request) {
        Long toUserId = request.getToUserId();
        if (toUserId.equals(fromUserId)) {
            throw new BizException("不能添加自己为好友");
        }
        if (userMapper.selectById(toUserId) == null) {
            throw new BizException(404, "用户不存在");
        }
        if (isFriend(fromUserId, toUserId)) {
            throw new BizException("你们已经是好友了");
        }
        Long pending = requestMapper.selectCount(new QueryWrapper<FriendRequest>()
                .eq("from_user_id", fromUserId).eq("to_user_id", toUserId).eq("status", 0));
        if (pending > 0) {
            throw new BizException("已发送过好友申请，请等待对方处理");
        }
        FriendRequest fr = new FriendRequest();
        fr.setFromUserId(fromUserId);
        fr.setToUserId(toUserId);
        fr.setMessage(request.getMessage() == null ? null : request.getMessage().trim());
        fr.setStatus(0);
        fr.setCreatedAt(LocalDateTime.now());
        requestMapper.insert(fr);
    }

    /** 收到的待处理申请 */
    public List<FriendRequestVO> listReceived(Long userId) {
        List<FriendRequest> list = requestMapper.selectList(new QueryWrapper<FriendRequest>()
                .eq("to_user_id", userId).eq("status", 0).orderByDesc("id"));
        return toVOList(list);
    }

    /** 发出的申请 */
    public List<FriendRequestVO> listSent(Long userId) {
        List<FriendRequest> list = requestMapper.selectList(new QueryWrapper<FriendRequest>()
                .eq("from_user_id", userId).orderByDesc("id"));
        return toVOList(list);
    }

    /** 同意申请（双向建立好友关系） */
    @Transactional
    public void approve(Long userId, Long requestId) {
        FriendRequest fr = requireRequest(requestId);
        if (!fr.getToUserId().equals(userId)) {
            throw new BizException(403, "无权处理该申请");
        }
        if (fr.getStatus() != 0) {
            throw new BizException("该申请已处理");
        }
        fr.setStatus(1);
        requestMapper.updateById(fr);
        insertFriendPair(userId, fr.getFromUserId());
    }

    /** 拒绝申请 */
    @Transactional
    public void reject(Long userId, Long requestId) {
        FriendRequest fr = requireRequest(requestId);
        if (!fr.getToUserId().equals(userId)) {
            throw new BizException(403, "无权处理该申请");
        }
        if (fr.getStatus() != 0) {
            throw new BizException("该申请已处理");
        }
        fr.setStatus(2);
        requestMapper.updateById(fr);
    }

    /** 好友列表（含昵称头像） */
    public List<Map<String, Object>> listFriends(Long userId) {
        List<Friend> rows = friendMapper.selectList(new QueryWrapper<Friend>()
                .eq("user_id", userId).orderByAsc("id"));
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = rows.stream().map(Friend::getFriendId).distinct().collect(Collectors.toList());
        Map<Long, User> users = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return rows.stream().map(f -> {
            User u = users.get(f.getFriendId());
            if (u == null) {
                return null;
            }
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar() == null ? "" : u.getAvatar());
            m.put("friendSince", f.getCreatedAt());
            return m;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /** 删除好友（双向） */
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        friendMapper.delete(new QueryWrapper<Friend>()
                .eq("user_id", userId).eq("friend_id", friendId));
        friendMapper.delete(new QueryWrapper<Friend>()
                .eq("user_id", friendId).eq("friend_id", userId));
    }

    /** 与目标用户的关系：none / friend / requested / pending */
    public Map<String, Object> relation(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            return Map.of("relation", "self");
        }
        if (isFriend(userId, targetUserId)) {
            return Map.of("relation", "friend");
        }
        Long requested = requestMapper.selectCount(new QueryWrapper<FriendRequest>()
                .eq("from_user_id", userId).eq("to_user_id", targetUserId).eq("status", 0));
        if (requested > 0) {
            return Map.of("relation", "requested");
        }
        Long pending = requestMapper.selectCount(new QueryWrapper<FriendRequest>()
                .eq("from_user_id", targetUserId).eq("to_user_id", userId).eq("status", 0));
        if (pending > 0) {
            return Map.of("relation", "pending");
        }
        return Map.of("relation", "none");
    }

    private boolean isFriend(Long a, Long b) {
        return friendMapper.selectCount(new QueryWrapper<Friend>()
                .eq("user_id", a).eq("friend_id", b)) > 0;
    }

    /** 强制建立双向好友关系（幂等，供注册自动加管理员好友等场景） */
    @Transactional
    public void forceFriend(Long a, Long b) {
        if (a.equals(b)) {
            return;
        }
        insertFriendPair(a, b);
    }

    /** 按昵称/用户名/ID 搜索用户（附带与当前用户的关系状态） */
    public List<Map<String, Object>> search(String keyword, Long currentUserId) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        String kw = keyword.trim();
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (kw.matches("\\d+")) {
            qw.eq("id", Long.parseLong(kw))
                    .or().like("nickname", kw)
                    .or().like("username", kw);
        } else {
            qw.like("nickname", kw).or().like("username", kw);
        }
        qw.last("LIMIT 20");
        List<User> users = userMapper.selectList(qw);
        return users.stream().map(u -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar() == null ? "" : u.getAvatar());
            m.put("relation", relation(currentUserId, u.getId()).get("relation"));
            return m;
        }).collect(Collectors.toList());
    }

    private void insertFriendPair(Long a, Long b) {
        if (friendMapper.selectCount(new QueryWrapper<Friend>()
                .eq("user_id", a).eq("friend_id", b)) == 0) {
            Friend f1 = new Friend();
            f1.setUserId(a);
            f1.setFriendId(b);
            f1.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(f1);
        }
        if (friendMapper.selectCount(new QueryWrapper<Friend>()
                .eq("user_id", b).eq("friend_id", a)) == 0) {
            Friend f2 = new Friend();
            f2.setUserId(b);
            f2.setFriendId(a);
            f2.setCreatedAt(LocalDateTime.now());
            friendMapper.insert(f2);
        }
    }

    private FriendRequest requireRequest(Long id) {
        FriendRequest fr = requestMapper.selectById(id);
        if (fr == null) {
            throw new BizException(404, "申请不存在");
        }
        return fr;
    }

    private List<FriendRequestVO> toVOList(List<FriendRequest> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = new ArrayList<>();
        for (FriendRequest fr : list) {
            userIds.add(fr.getFromUserId());
            userIds.add(fr.getToUserId());
        }
        Map<Long, User> users = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        return list.stream().map(fr -> {
            FriendRequestVO vo = new FriendRequestVO();
            vo.setId(fr.getId());
            vo.setMessage(fr.getMessage());
            vo.setStatus(fr.getStatus());
            vo.setCreatedAt(fr.getCreatedAt());
            vo.setFromUser(toInfo(users.get(fr.getFromUserId())));
            vo.setToUser(toInfo(users.get(fr.getToUserId())));
            return vo;
        }).collect(Collectors.toList());
    }

    private FriendRequestVO.UserInfo toInfo(User u) {
        if (u == null) {
            return null;
        }
        FriendRequestVO.UserInfo info = new FriendRequestVO.UserInfo();
        info.setId(u.getId());
        info.setUsername(u.getUsername());
        info.setNickname(u.getNickname());
        info.setAvatar(u.getAvatar());
        return info;
    }
}
