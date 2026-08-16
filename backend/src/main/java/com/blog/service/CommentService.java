package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;
import com.blog.entity.Murmur;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.MurmurMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.UserMapper;
import com.blog.vo.CommentVO;
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
import java.util.stream.Collectors;

/**
 * 评论服务：前台浏览/提交（默认直接通过），后台审核管理
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final MurmurMapper murmurMapper;
    private final UserMapper userMapper;
    private final SiteService siteService;
    private final FriendService friendService;

    // ==================== 前台 ====================

    /**
     * 文章评论列表（仅已通过，两层树：顶级 + 楼中楼回复）
     * 与文章详情同权限：无法查看文章则同样无法查看评论
     */
    public List<CommentVO> listByPost(Long postId, Long viewerId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new BizException(404, "文章不存在");
        }
        if (!friendService.canViewContent(post.getVisibility(), post.getUserId(), viewerId)) {
            throw new BizException(404, "文章不存在");
        }
        List<Comment> all = commentMapper.selectList(new QueryWrapper<Comment>()
                .eq("post_id", postId)
                .eq("status", 1)
                .orderByAsc("id")
                .last("LIMIT 500"));
        return buildTree(all);
    }

    /**
     * 说说评论列表（仅已通过，两层树，与文章评论区同款）
     * 与说说同权限：无法查看说说则同样无法查看评论
     */
    public List<CommentVO> listByMurmur(Long murmurId, Long viewerId) {
        Murmur murmur = murmurMapper.selectById(murmurId);
        if (murmur == null) {
            throw new BizException(404, "说说不存在");
        }
        if (!friendService.canViewContent(murmur.getVisibility(), murmur.getUserId(), viewerId)) {
            throw new BizException(404, "说说不存在");
        }
        List<Comment> all = commentMapper.selectList(new QueryWrapper<Comment>()
                .eq("murmur_id", murmurId)
                .eq("status", 1)
                .orderByAsc("id")
                .last("LIMIT 500"));
        return buildTree(all);
    }

    /** 组装两层树（顶级 + 楼中楼） */
    private List<CommentVO> buildTree(List<Comment> all) {
        Map<Long, CommentVO> voMap = new LinkedHashMap<>();
        Map<Long, String> avatars = loadAvatars(all);
        for (Comment c : all) {
            voMap.put(c.getId(), toVO(c, avatars.get(c.getUserId())));
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
        // 可见性：公共任何人可评论；仅好友可见仅作者与好友；仅自己可见仅作者
        if (!friendService.canViewContent(post.getVisibility(), post.getUserId(), userId)) {
            throw new BizException(403, "没有权限评论该文章");
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
        comment.setUserId(userId);
        comment.setWebsite(null);
        // 默认直接通过
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        postMapper.incrCommentCount(postId);
    }

    /**
     * 提交说说评论（仅登录用户，身份自动识别，默认直接通过）
     */
    public void submitMurmur(Long murmurId, CommentRequest request, Long userId) {
        Murmur murmur = murmurMapper.selectById(murmurId);
        if (murmur == null) {
            throw new BizException(404, "说说不存在");
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
        // 可见性：公共任何人可评论；仅好友可见仅作者与好友；仅自己可见仅作者
        if (!friendService.canViewContent(murmur.getVisibility(), murmur.getUserId(), userId)) {
            throw new BizException(403, "没有权限评论该说说");
        }
        if (request.getParentId() != null) {
            Comment parent = commentMapper.selectById(request.getParentId());
            if (parent == null || !murmurId.equals(parent.getMurmurId())) {
                throw new BizException("回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setMurmurId(murmurId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent().trim());
        comment.setNickname(user.getNickname());
        comment.setEmail("");
        comment.setUserId(userId);
        comment.setWebsite(null);
        // 默认直接通过
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        murmurMapper.incrCommentCount(murmurId);
    }

    // ==================== 后台 ====================

    /**
     * 管理端评论列表：统一管理文章评论与说说评论（targetType 可选：post / murmur）
     */
    public PageResult<CommentVO> listAdmin(int page, int size, Integer status, String targetType) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status);
        if ("murmur".equals(targetType)) {
            qw.isNotNull("murmur_id");
        } else if ("post".equals(targetType)) {
            qw.isNotNull("post_id");
        }
        qw.orderByDesc("id");
        Page<Comment> result = commentMapper.selectPage(new Page<>(page, size), qw);

        List<Long> postIds = result.getRecords().stream()
                .map(Comment::getPostId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> titles = new HashMap<>();
        if (!postIds.isEmpty()) {
            titles.putAll(postMapper.selectBatchIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, Post::getTitle)));
        }
        List<Long> murmurIds = result.getRecords().stream()
                .map(Comment::getMurmurId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> murmurContents = new HashMap<>();
        if (!murmurIds.isEmpty()) {
            for (Murmur m : murmurMapper.selectBatchIds(murmurIds)) {
                murmurContents.put(m.getId(), m.getContent() == null ? "" : m.getContent());
            }
        }
        Map<Long, String> avatars = loadAvatars(result.getRecords());

        List<CommentVO> vos = result.getRecords().stream().map(c -> {
            CommentVO vo = toVO(c, avatars.get(c.getUserId()));
            if (c.getMurmurId() != null) {
                vo.setTargetType("murmur");
                String content = murmurContents.getOrDefault(c.getMurmurId(), "（说说已删除）");
                vo.setPostTitle(content.length() > 60 ? content.substring(0, 60) + "…" : content);
            } else {
                vo.setTargetType("post");
                vo.setPostTitle(titles.getOrDefault(c.getPostId(), "（文章已删除）"));
            }
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
        incrTargetCount(comment);
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
        decrTargetCount(comment);
    }

    @Transactional
    public void reject(Long id) {
        Comment comment = requireComment(id);
        Integer oldStatus = comment.getStatus();
        comment.setStatus(2);
        commentMapper.updateById(comment);
        if (oldStatus != null && oldStatus == 1) {
            decrTargetCount(comment);
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
            decrTargetCount(comment);
        }
    }

    /** 按评论归属（文章/说说）累加计数 */
    private void incrTargetCount(Comment c) {
        if (c.getMurmurId() != null) {
            murmurMapper.incrCommentCount(c.getMurmurId());
        } else if (c.getPostId() != null) {
            postMapper.incrCommentCount(c.getPostId());
        }
    }

    /** 按评论归属（文章/说说）扣减计数 */
    private void decrTargetCount(Comment c) {
        if (c.getMurmurId() != null) {
            murmurMapper.decrCommentCount(c.getMurmurId());
        } else if (c.getPostId() != null) {
            postMapper.decrCommentCount(c.getPostId());
        }
    }

    private Comment requireComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(404, "评论不存在");
        }
        return comment;
    }

    private CommentVO toVO(Comment c, String avatar) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setMurmurId(c.getMurmurId());
        vo.setParentId(c.getParentId());
        vo.setNickname(c.getNickname());
        vo.setWebsite(c.getWebsite());
        vo.setUserId(c.getUserId());
        vo.setAvatar(avatar);
        vo.setContent(c.getContent());
        vo.setStatus(c.getStatus());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    /**
     * 批量加载评论者的当前头像（历史评论、用户更换头像后都能实时跟随）
     */
    private Map<Long, String> loadAvatars(List<Comment> comments) {
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> avatars = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBatchIds(userIds)) {
                if (u.getAvatar() != null && !u.getAvatar().isEmpty()) {
                    avatars.put(u.getId(), u.getAvatar());
                }
            }
        }
        return avatars;
    }
}
