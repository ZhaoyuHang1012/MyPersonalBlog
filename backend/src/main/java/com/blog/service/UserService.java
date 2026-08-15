package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.blog.common.BizException;
import com.blog.dto.AdminUserUpdateRequest;
import com.blog.dto.PasswordUpdateRequest;
import com.blog.dto.ProfileUpdateRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务：个人资料、修改密码
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserVO getById(Long id) {
        return toVO(requireUser(id));
    }

    @Transactional
    public UserVO updateProfile(Long id, ProfileUpdateRequest request) {
        requireUser(id);
        String nickname = request.getNickname().trim();
        if (userMapper.selectCount(new QueryWrapper<User>().eq("nickname", nickname).ne("id", id)) > 0) {
            throw new BizException("昵称已被使用，请换一个");
        }
        // 显式 set，支持将头像清空（updateById 默认跳过 null 字段）
        String avatar = (request.getAvatar() == null || request.getAvatar().isBlank())
                ? null : request.getAvatar().trim();
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", id)
                .set("nickname", nickname)
                .set("avatar", avatar));
        return getById(id);
    }

    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest request) {
        User user = requireUser(id);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    // ==================== 管理员管理 ====================

    /**
     * 管理员修改任意用户信息（昵称/头像/角色/配额）
     */
    @Transactional
    public UserVO adminUpdate(Long operatorId, Long targetId, AdminUserUpdateRequest request) {
        User target = requireUser(targetId);
        UpdateWrapper<User> uw = new UpdateWrapper<User>().eq("id", targetId);
        boolean hasUpdate = false;

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            String nickname = request.getNickname().trim();
            if (userMapper.selectCount(new QueryWrapper<User>()
                    .eq("nickname", nickname).ne("id", targetId)) > 0) {
                throw new BizException("昵称已被使用，请换一个");
            }
            uw.set("nickname", nickname);
            hasUpdate = true;
        }
        if (request.getAvatar() != null) {
            uw.set("avatar", request.getAvatar().isBlank() ? null : request.getAvatar().trim());
            hasUpdate = true;
        }
        if (request.getQuota() != null) {
            if (request.getQuota() <= 0) {
                throw new BizException("配额必须大于 0");
            }
            uw.set("quota", request.getQuota());
            hasUpdate = true;
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            String role = request.getRole().trim();
            if (!"ADMIN".equals(role) && !"USER".equals(role)) {
                throw new BizException("角色仅支持 ADMIN / USER");
            }
            if (targetId.equals(operatorId)) {
                throw new BizException("不能修改自己的角色");
            }
            // 防止把最后一个管理员降级
            if ("USER".equals(role) && "ADMIN".equals(target.getRole())) {
                Long adminCount = userMapper.selectCount(new QueryWrapper<User>().eq("role", "ADMIN"));
                if (adminCount <= 1) {
                    throw new BizException("系统至少需要保留一名管理员");
                }
            }
            uw.set("role", role);
            hasUpdate = true;
        }
        if (hasUpdate) {
            userMapper.update(null, uw);
        }
        return getById(targetId);
    }

    /** 管理员重置任意用户密码 */
    @Transactional
    public void adminResetPassword(Long targetId, String newPassword) {
        requireUser(targetId);
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", targetId)
                .set("password", passwordEncoder.encode(newPassword)));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setQuota(user.getQuota());
        return vo;
    }
}
