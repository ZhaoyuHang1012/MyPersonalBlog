package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台用户列表接口（仅管理员，供按用户筛选管理使用）
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

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
            return vo;
        }).collect(Collectors.toList()));
    }
}
