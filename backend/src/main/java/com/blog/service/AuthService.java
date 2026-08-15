package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.dto.LoginRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.security.JwtUtil;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(user.getId(), user.getUsername()));
        vo.setUser(toUserVO(user));
        return vo;
    }

    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserVO(user);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
