package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.dto.PostSaveRequest;
import com.blog.entity.Category;
import com.blog.entity.Post;
import com.blog.entity.PostTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.PostTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.ArchiveGroupVO;
import com.blog.vo.PostVO;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务：大厅/个人博客页浏览、按归属的增删改
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final MarkdownService markdownService;

    // ==================== 前台 ====================

    /**
     * 大厅：登录用户展示自己+好友的公开文章；未登录展示全部公开文章
     * 支持多标签（同时包含所选全部标签）
     */
    public PageResult<PostVO> listHall(int page, int size, Long categoryId, List<Long> tagIds,
                                       String keyword, Long viewerId) {
        QueryWrapper<Post> qw = publicWrapper();
        if (viewerId != null) {
            qw.and(w -> w.eq("user_id", viewerId)
                    .or().inSql("user_id",
                            "SELECT friend_id FROM friends WHERE user_id = " + viewerId));
        }
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            String ids = tagIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            qw.inSql("id", "SELECT post_id FROM post_tag WHERE tag_id IN (" + ids + ") " +
                    "GROUP BY post_id HAVING COUNT(DISTINCT tag_id) = " + tagIds.size());
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("title", keyword).or().like("content_md", keyword));
        }
        qw.orderByDesc("is_top").orderByDesc("published_at").orderByDesc("id");
        Page<Post> result = postMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords(), false),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 按 ID 列表组装 VO（归档列表等场景） */
    public List<PostVO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Post> posts = postMapper.selectBatchIds(ids);
        Map<Long, Post> byId = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        List<Post> ordered = ids.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
        return toVOList(ordered, false);
    }

    /** 某用户的公开文章列表（个人博客页） */
    public PageResult<PostVO> listUserPosts(String username, int page, int size) {
        User author = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (author == null) {
            throw new BizException(404, "用户不存在");
        }
        QueryWrapper<Post> qw = publicWrapper();
        qw.eq("user_id", author.getId())
                .orderByDesc("is_top").orderByDesc("published_at").orderByDesc("id");
        Page<Post> result = postMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords(), false),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 文章详情（「仅自己可见」文章仅作者本人可看）
     */
    public PostVO getDetail(Long id, Long viewerId) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getStatus() != 1) {
            throw new BizException(404, "文章不存在");
        }
        boolean isOwner = viewerId != null && viewerId.equals(post.getUserId());
        if (post.getVisibility() != 1 && !isOwner) {
            throw new BizException(404, "文章不存在");
        }
        postMapper.incrementViewCount(id);
        post.setViewCount(post.getViewCount() == null ? 1 : post.getViewCount() + 1);

        PostVO vo = toVOList(List.of(post), true).get(0);

        QueryWrapper<Post> prevQw = new QueryWrapper<Post>().eq("status", 1).eq("visibility", 1)
                .lt("id", id).orderByDesc("id").last("LIMIT 1");
        QueryWrapper<Post> nextQw = new QueryWrapper<Post>().eq("status", 1).eq("visibility", 1)
                .gt("id", id).orderByAsc("id").last("LIMIT 1");
        Post prev = postMapper.selectOne(prevQw);
        Post next = postMapper.selectOne(nextQw);
        if (prev != null) {
            vo.setPrev(new PostVO.LinkVO(prev.getId(), prev.getTitle()));
        }
        if (next != null) {
            vo.setNext(new PostVO.LinkVO(next.getId(), next.getTitle()));
        }
        return vo;
    }

    /**
     * 文章归档（仅开放文章）
     */
    public List<ArchiveGroupVO> listArchive() {
        List<Post> posts = postMapper.selectList(new QueryWrapper<Post>()
                .select("id", "title", "published_at")
                .eq("status", 1).eq("visibility", 1)
                .orderByDesc("published_at"));
        Map<String, ArchiveGroupVO> groups = new LinkedHashMap<>();
        for (Post p : posts) {
            if (p.getPublishedAt() == null) {
                continue;
            }
            String key = p.getPublishedAt().getYear() + "-" + p.getPublishedAt().getMonthValue();
            ArchiveGroupVO group = groups.computeIfAbsent(key, k -> {
                ArchiveGroupVO g = new ArchiveGroupVO();
                g.setYear(p.getPublishedAt().getYear());
                g.setMonth(p.getPublishedAt().getMonthValue());
                g.setPosts(new ArrayList<>());
                return g;
            });
            group.getPosts().add(new ArchiveGroupVO.ArchiveItemVO(p.getId(), p.getTitle(), p.getPublishedAt()));
        }
        return new ArrayList<>(groups.values());
    }

    // ==================== 后台 ====================

    /** 文章管理列表：普通用户仅自己的；管理员可选按作者过滤 */
    public PageResult<PostVO> listAdmin(Long operatorId, boolean isAdmin, int page, int size,
                                        Integer status, String keyword, Long authorId) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        if (!isAdmin) {
            qw.eq("user_id", operatorId);
        } else if (authorId != null) {
            qw.eq("user_id", authorId);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword);
        }
        qw.orderByDesc("updated_at");
        Page<Post> result = postMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords(), false),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    public PostVO getDetailAdmin(Long id, Long operatorId, boolean isAdmin) {
        Post post = requirePost(id);
        if (!isAdmin && !operatorId.equals(post.getUserId())) {
            throw new BizException(403, "无权查看他人文章");
        }
        return toVOList(List.of(post), true).get(0);
    }

    @Transactional
    public PostVO create(Long userId, PostSaveRequest request) {
        Post post = new Post();
        post.setUserId(userId);
        apply(post, request);
        postMapper.insert(post);
        syncTags(post.getId(), request.getTagIds());
        return toVOList(List.of(post), true).get(0);
    }

    @Transactional
    public PostVO update(Long operatorId, boolean isAdmin, Long id, PostSaveRequest request) {
        Post post = requireOwnedPost(id, operatorId, isAdmin);
        apply(post, request);
        postMapper.updateById(post);
        syncTags(id, request.getTagIds());
        return toVOList(List.of(post), true).get(0);
    }

    @Transactional
    public void delete(Long operatorId, boolean isAdmin, Long id) {
        requireOwnedPost(id, operatorId, isAdmin);
        postMapper.deleteById(id);
        postTagMapper.delete(new QueryWrapper<PostTag>().eq("post_id", id));
    }

    private Post requirePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "文章不存在");
        }
        return post;
    }

    private Post requireOwnedPost(Long id, Long operatorId, boolean isAdmin) {
        Post post = requirePost(id);
        if (!isAdmin && !operatorId.equals(post.getUserId())) {
            throw new BizException(403, "无权操作他人文章");
        }
        return post;
    }

    private QueryWrapper<Post> publicWrapper() {
        return new QueryWrapper<Post>().eq("status", 1).eq("visibility", 1);
    }

    private void apply(Post post, PostSaveRequest request) {
        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(markdownService.render(request.getContentMd()));
        post.setCategoryId(request.getCategoryId());
        post.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        post.setVisibility(request.getVisibility() == null ? 1 : request.getVisibility());
        post.setIsTop(request.getIsTop() == null ? 0 : request.getIsTop());
        if (post.getStatus() == 1 && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
    }

    private void syncTags(Long postId, List<Long> tagIds) {
        postTagMapper.delete(new QueryWrapper<PostTag>().eq("post_id", postId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds.stream().distinct().collect(Collectors.toList())) {
            PostTag pt = new PostTag();
            pt.setPostId(postId);
            pt.setTagId(tagId);
            postTagMapper.insert(pt);
        }
    }

    private List<PostVO> toVOList(List<Post> posts, boolean withContent) {
        if (posts == null || posts.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 分类名
        List<Long> categoryIds = posts.stream().map(Post::getCategoryId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 作者
        List<Long> userIds = posts.stream().map(Post::getUserId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 标签
        List<PostTag> postTags = postTagMapper.selectList(new QueryWrapper<PostTag>().in("post_id", postIds));
        Map<Long, List<Long>> postTagIds = new HashMap<>();
        for (PostTag pt : postTags) {
            postTagIds.computeIfAbsent(pt.getPostId(), k -> new ArrayList<>()).add(pt.getTagId());
        }
        Set<Long> allTagIds = postTags.stream().map(PostTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = allTagIds.isEmpty() ? Map.of()
                : tagMapper.selectBatchIds(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        return posts.stream().map(p -> {
            PostVO vo = new PostVO();
            vo.setId(p.getId());
            vo.setUserId(p.getUserId());
            User author = users.get(p.getUserId());
            if (author != null) {
                PostVO.AuthorVO av = new PostVO.AuthorVO();
                av.setId(author.getId());
                av.setUsername(author.getUsername());
                av.setNickname(author.getNickname());
                av.setAvatar(author.getAvatar());
                vo.setAuthor(av);
            }
            vo.setTitle(p.getTitle());
            vo.setSummary(p.getSummary());
            if (withContent) {
                vo.setContentMd(p.getContentMd());
                vo.setContentHtml(p.getContentHtml());
            }
            vo.setCategoryId(p.getCategoryId());
            vo.setCategoryName(p.getCategoryId() == null ? null : categoryNames.get(p.getCategoryId()));
            vo.setStatus(p.getStatus());
            vo.setVisibility(p.getVisibility());
            vo.setIsTop(p.getIsTop());
            vo.setViewCount(p.getViewCount());
            vo.setCommentCount(p.getCommentCount() == null ? 0L : p.getCommentCount());
            vo.setCreatedAt(p.getCreatedAt());
            vo.setUpdatedAt(p.getUpdatedAt());
            vo.setPublishedAt(p.getPublishedAt());
            List<Long> tids = postTagIds.getOrDefault(p.getId(), List.of());
            vo.setTags(tids.stream().map(tagMap::get).filter(Objects::nonNull).map(t -> {
                TagVO tv = new TagVO();
                tv.setId(t.getId());
                tv.setName(t.getName());
                return tv;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }
}
