package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.blog.common.BizException;
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
        // 显式 set，支持将头像清空（updateById 默认跳过 null 字段）
        String avatar = (request.getAvatar() == null || request.getAvatar().isBlank())
                ? null : request.getAvatar().trim();
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", id)
                .set("nickname", request.getNickname().trim())
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

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
