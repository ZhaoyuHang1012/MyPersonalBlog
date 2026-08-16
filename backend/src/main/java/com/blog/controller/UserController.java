package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.AlbumService;
import com.blog.service.FriendService;
import com.blog.service.MurmurService;
import com.blog.service.PostService;
import com.blog.util.SecurityUtil;
import com.blog.vo.AlbumGroupVO;
import com.blog.vo.MurmurVO;
import com.blog.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    private final MurmurService murmurService;
    private final AlbumService albumService;
    private final FriendService friendService;

    @GetMapping("/{username}")
    public Result<Map<String, Object>> info(@PathVariable String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        Long viewerId = SecurityUtil.currentUserId();
        // 文章数按查看者权限统计：公共 + （好友或本人）仅好友可见 + （本人）仅自己可见
        long postCount = postMapper.selectCount(new QueryWrapper<Post>()
                .eq("user_id", user.getId()).eq("status", 1).eq("visibility", 1));
        if (viewerId != null && viewerId.equals(user.getId())) {
            postCount += postMapper.selectCount(new QueryWrapper<Post>()
                    .eq("user_id", user.getId()).eq("status", 1).in("visibility", 0, 2));
        } else if (viewerId != null && friendService.isFriendOf(viewerId, user.getId())) {
            postCount += postMapper.selectCount(new QueryWrapper<Post>()
                    .eq("user_id", user.getId()).eq("status", 1).eq("visibility", 2));
        }
        return Result.ok(Map.of(
                "id", user.getId(),
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
        return Result.ok(postService.listUserPosts(username, page, size, SecurityUtil.currentUserId()));
    }

    @GetMapping("/{username}/murmurs")
    public Result<PageResult<MurmurVO>> murmurs(@PathVariable String username,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(murmurService.listUserMurmurs(username, page, size, SecurityUtil.currentUserId()));
    }

    @GetMapping("/{username}/albums")
    public Result<List<AlbumGroupVO>> albums(@PathVariable String username) {
        return Result.ok(albumService.listUserAlbums(username, SecurityUtil.currentUserId()));
    }
}
