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
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.PostTagMapper;
import com.blog.mapper.TagMapper;
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
 * 文章服务
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final MarkdownService markdownService;

    // ==================== 前台 ====================

    /**
     * 已发布文章分页列表（支持分类/标签/关键词过滤）
     */
    public PageResult<PostVO> listPublished(int page, int size, Long categoryId, Long tagId, String keyword) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        if (tagId != null) {
            List<Long> postIds = postTagMapper.selectList(new QueryWrapper<PostTag>().eq("tag_id", tagId))
                    .stream().map(PostTag::getPostId).collect(Collectors.toList());
            if (postIds.isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0, page, size);
            }
            qw.in("id", postIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            // 全文搜索：标题或正文命中
            qw.and(w -> w.like("title", keyword).or().like("content_md", keyword));
        }
        qw.orderByDesc("is_top").orderByDesc("published_at").orderByDesc("id");

        Page<Post> result = postMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(toVOList(result.getRecords(), false),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 文章详情（前台访问自动增加浏览量，含上一篇/下一篇）
     */
    public PostVO getDetail(Long id, boolean admin) {
        Post post = postMapper.selectById(id);
        if (post == null || (post.getStatus() != 1 && !admin)) {
            throw new BizException(404, "文章不存在");
        }
        if (!admin) {
            postMapper.incrementViewCount(id);
            post.setViewCount(post.getViewCount() == null ? 1 : post.getViewCount() + 1);
        }
        PostVO vo = toVOList(List.of(post), true).get(0);

        QueryWrapper<Post> prevQw = new QueryWrapper<Post>().eq("status", 1)
                .lt("id", id).orderByDesc("id").last("LIMIT 1");
        QueryWrapper<Post> nextQw = new QueryWrapper<Post>().eq("status", 1)
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
     * 文章归档：按年月分组的已发布文章
     */
    public List<ArchiveGroupVO> listArchive() {
        List<Post> posts = postMapper.selectList(new QueryWrapper<Post>()
                .select("id", "title", "published_at")
                .eq("status", 1)
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

    public PageResult<PostVO> listAdmin(int page, int size, Integer status, String keyword) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
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

    public PostVO getDetailAdmin(Long id) {
        return getDetail(id, true);
    }

    @Transactional
    public PostVO create(PostSaveRequest request) {
        Post post = new Post();
        apply(post, request);
        postMapper.insert(post);
        syncTags(post.getId(), request.getTagIds());
        return getDetail(post.getId(), true);
    }

    @Transactional
    public PostVO update(Long id, PostSaveRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(404, "文章不存在");
        }
        apply(post, request);
        postMapper.updateById(post);
        syncTags(id, request.getTagIds());
        return getDetail(id, true);
    }

    @Transactional
    public void delete(Long id) {
        postMapper.deleteById(id);
        postTagMapper.delete(new QueryWrapper<PostTag>().eq("post_id", id));
    }

    private void apply(Post post, PostSaveRequest request) {
        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContentMd(request.getContentMd());
        post.setContentHtml(markdownService.render(request.getContentMd()));
        post.setCategoryId(request.getCategoryId());
        post.setStatus(request.getStatus() == null ? 0 : request.getStatus());
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
            vo.setTitle(p.getTitle());
            vo.setSummary(p.getSummary());
            if (withContent) {
                vo.setContentMd(p.getContentMd());
                vo.setContentHtml(p.getContentHtml());
            }
            vo.setCategoryId(p.getCategoryId());
            vo.setCategoryName(p.getCategoryId() == null ? null : categoryNames.get(p.getCategoryId()));
            vo.setStatus(p.getStatus());
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
