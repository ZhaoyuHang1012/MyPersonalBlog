package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.InviteCode;
import com.blog.entity.User;
import com.blog.mapper.InviteCodeMapper;
import com.blog.mapper.UserMapper;
import com.blog.security.JwtUtil;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务：登录 / 邀请码注册
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final FriendService friendService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${blog.quota-default:1073741824}")
    private long quotaDefault;

    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        return buildLoginVO(user);
    }

    /**
     * 邀请码注册（注册即登录；昵称唯一；自动与系统管理员互加好友）
     */
    @Transactional
    public LoginVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0) {
            throw new BizException("用户名已被注册");
        }
        if (userMapper.selectCount(new QueryWrapper<User>().eq("nickname", nickname)) > 0) {
            throw new BizException("昵称已被使用，请换一个");
        }
        // 校验邀请码
        InviteCode invite = inviteCodeMapper.selectOne(new QueryWrapper<InviteCode>()
                .eq("code", request.getInviteCode().trim().toUpperCase()));
        if (invite == null || invite.getUsed() == 1) {
            throw new BizException("邀请码无效或已被使用");
        }
        invite.setUsed(1);
        invite.setUsedBy(username);
        invite.setUsedAt(LocalDateTime.now());
        inviteCodeMapper.updateById(invite);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setRole("USER");
        user.setQuota(quotaDefault);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 自动与系统管理员互加好友
        User admin = userMapper.selectList(new QueryWrapper<User>()
                .eq("role", "ADMIN").orderByAsc("id").last("LIMIT 1"))
                .stream().findFirst().orElse(null);
        if (admin != null) {
            friendService.forceFriend(user.getId(), admin.getId());
        }
        return buildLoginVO(user);
    }

    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(user.getId(), user.getUsername(), user.getRole()));
        vo.setUser(toUserVO(user));
        return vo;
    }

    private UserVO toUserVO(User user) {
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
