package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.PostService;
import com.blog.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户公开信息与个人博客页接口
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostService postService;

    @GetMapping("/{username}")
    public Result<Map<String, Object>> info(@PathVariable String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        long postCount = postMapper.selectCount(new QueryWrapper<Post>()
                .eq("user_id", user.getId()).eq("status", 1).eq("visibility", 1));
        return Result.ok(Map.of(
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatar() == null ? "" : user.getAvatar(),
                "postCount", postCount
        ));
    }

    @GetMapping("/{username}/posts")
    public Result<PageResult<PostVO>> posts(@PathVariable String username,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.listUserPosts(username, page, size));
    }
}
