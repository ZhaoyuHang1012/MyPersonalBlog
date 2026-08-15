package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务：前台浏览/提交（默认直接通过），后台审核管理
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final SiteService siteService;

    // ==================== 前台 ====================

    /**
     * 文章评论列表（仅已通过，两层树：顶级 + 楼中楼回复）
     */
    public List<CommentVO> listByPost(Long postId) {
        List<Comment> all = commentMapper.selectList(new QueryWrapper<Comment>()
                .eq("post_id", postId)
                .eq("status", 1)
                .orderByAsc("id")
                .last("LIMIT 500"));

        Map<Long, CommentVO> voMap = new LinkedHashMap<>();
        for (Comment c : all) {
            voMap.put(c.getId(), toVO(c));
        }
        List<CommentVO> roots = new ArrayList<>();
        for (Comment c : all) {
            CommentVO vo = voMap.get(c.getId());
            if (c.getParentId() != null && voMap.containsKey(c.getParentId())) {
                CommentVO parent = voMap.get(c.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }
        return roots;
    }

    /**
     * 提交评论（仅登录用户，身份自动识别，默认直接通过）
     */
    public void submit(Long postId, CommentRequest request, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BizException(404, "文章不存在");
        }
        if (siteService.info().getAllowComments() != 1) {
            throw new BizException("评论功能已关闭");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BizException("评论内容不能为空");
        }
        if (request.getContent().trim().length() > 1000) {
            throw new BizException("评论内容不能超过 1000 个字符");
        }
        if (userId == null) {
            throw new BizException(401, "请先登录后再评论");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "登录状态已失效，请重新登录");
        }
        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null || !parent.getPostId().equals(postId)) {
                throw new BizException("回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent().trim());
        comment.setNickname(user.getNickname());
        comment.setEmail("");
        comment.setWebsite(null);
        // 默认直接通过
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        postMapper.incrCommentCount(postId);
    }

    // ==================== 后台 ====================

    public PageResult<CommentVO> listAdmin(int page, int size, Integer status) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status).orderByDesc("id");
        Page<Comment> result = commentMapper.selectPage(new Page<>(page, size), qw);

        List<Long> postIds = result.getRecords().stream()
                .map(Comment::getPostId).distinct().collect(Collectors.toList());
        Map<Long, String> titles = postIds.isEmpty() ? Map.of()
                : postMapper.selectBatchIds(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Post::getTitle));

        List<CommentVO> vos = result.getRecords().stream().map(c -> {
            CommentVO vo = toVO(c);
            vo.setPostTitle(titles.getOrDefault(c.getPostId(), "（文章已删除）"));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void approve(Long id) {
        Comment comment = requireComment(id);
        if (comment.getStatus() == 1) {
            return;
        }
        comment.setStatus(1);
        commentMapper.updateById(comment);
        postMapper.incrCommentCount(comment.getPostId());
    }

    /** 退回：已通过 -> 待审核（前台隐藏） */
    @Transactional
    public void unapprove(Long id) {
        Comment comment = requireComment(id);
        if (comment.getStatus() != 1) {
            throw new BizException("仅已通过的评论可以退回");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
        postMapper.decrCommentCount(comment.getPostId());
    }

    @Transactional
    public void reject(Long id) {
        Comment comment = requireComment(id);
        Integer oldStatus = comment.getStatus();
        comment.setStatus(2);
        commentMapper.updateById(comment);
        if (oldStatus != null && oldStatus == 1) {
            postMapper.decrCommentCount(comment.getPostId());
        }
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = requireComment(id);
        // 子回复提升为顶级评论
        commentMapper.update(null, new UpdateWrapper<Comment>()
                .eq("parent_id", id).set("parent_id", null));
        commentMapper.deleteById(id);
        if (comment.getStatus() != null && comment.getStatus() == 1) {
            postMapper.decrCommentCount(comment.getPostId());
        }
    }

    private Comment requireComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(404, "评论不存在");
        }
        return comment;
    }

    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setParentId(c.getParentId());
        vo.setNickname(c.getNickname());
        vo.setWebsite(c.getWebsite());
        vo.setContent(c.getContent());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}
