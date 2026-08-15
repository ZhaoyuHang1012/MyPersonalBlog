package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.common.Result;
import com.blog.dto.AdminPasswordResetRequest;
import com.blog.dto.AdminUserUpdateRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.FriendService;
import com.blog.service.UserService;
import com.blog.util.SecurityUtil;
import com.blog.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台用户管理接口（仅管理员，最高权限管理）
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final FriendService friendService;

    @GetMapping
    public Result<List<UserVO>> list() {
        List<User> users = userMapper.selectList(new QueryWrapper<User>().orderByAsc("id"));
        return Result.ok(users.stream().map(u -> {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setRole(u.getRole());
            vo.setQuota(u.getQuota());
            return vo;
        }).collect(Collectors.toList()));
    }

    /** 修改用户信息（昵称/头像/角色/配额） */
    @PutMapping("/{id}")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        return Result.ok(userService.adminUpdate(SecurityUtil.currentUserId(), id, request));
    }

    /** 重置用户密码 */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody AdminPasswordResetRequest request) {
        userService.adminResetPassword(id, request.getNewPassword());
        return Result.ok();
    }

    /** 查看某用户的好友列表 */
    @GetMapping("/{id}/friends")
    public Result<List<Map<String, Object>>> friends(@PathVariable Long id) {
        if (userMapper.selectById(id) == null) {
            throw new BizException(404, "用户不存在");
        }
        return Result.ok(friendService.listFriends(id));
    }

    /** 删除某用户的一条好友关系（双向） */
    @DeleteMapping("/friends/{userId}/{friendId}")
    public Result<Void> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
        return Result.ok();
    }
}
