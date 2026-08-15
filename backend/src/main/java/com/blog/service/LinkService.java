package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.BizException;
import com.blog.common.PageResult;
import com.blog.dto.LinkApplyRequest;
import com.blog.dto.LinkSaveRequest;
import com.blog.entity.FriendLink;
import com.blog.mapper.FriendLinkMapper;
import com.blog.util.IpRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 友链服务：前台展示/申请，后台审核管理
 */
@Service
@RequiredArgsConstructor
public class LinkService {

    private final FriendLinkMapper linkMapper;
    private final IpRateLimiter rateLimiter;

    /** 前台：已通过友链列表 */
    public List<FriendLink> listApproved() {
        return linkMapper.selectList(new QueryWrapper<FriendLink>()
                .eq("status", 1)
                .orderByAsc("sort")
                .orderByAsc("id"));
    }

    /** 前台：提交友链申请（待审核） */
    public void apply(LinkApplyRequest request, String ip) {
        checkUrl(request.getUrl());
        if (!rateLimiter.tryAcquire(ip)) {
            throw new BizException("操作太频繁，请稍后再试");
        }
        FriendLink link = new FriendLink();
        link.setName(request.getName().trim());
        link.setUrl(request.getUrl().trim());
        link.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        link.setStatus(0);
        link.setSort(0);
        link.setCreatedAt(LocalDateTime.now());
        linkMapper.insert(link);
    }

    // ==================== 后台 ====================

    public PageResult<FriendLink> listAdmin(int page, int size, Integer status) {
        QueryWrapper<FriendLink> qw = new QueryWrapper<>();
        qw.eq(status != null, "status", status).orderByDesc("id");
        Page<FriendLink> result = linkMapper.selectPage(new Page<>(page, size), qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public FriendLink create(LinkSaveRequest request) {
        checkUrl(request.getUrl());
        FriendLink link = new FriendLink();
        applySave(link, request);
        link.setStatus(1);
        link.setCreatedAt(LocalDateTime.now());
        linkMapper.insert(link);
        return link;
    }

    @Transactional
    public void update(Long id, LinkSaveRequest request) {
        checkUrl(request.getUrl());
        FriendLink link = requireLink(id);
        applySave(link, request);
        linkMapper.updateById(link);
    }

    @Transactional
    public void approve(Long id) {
        FriendLink link = requireLink(id);
        link.setStatus(1);
        linkMapper.updateById(link);
    }

    @Transactional
    public void delete(Long id) {
        linkMapper.deleteById(id);
    }

    private void applySave(FriendLink link, LinkSaveRequest request) {
        link.setName(request.getName().trim());
        link.setUrl(request.getUrl().trim());
        link.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        if (request.getSort() != null) {
            link.setSort(request.getSort());
        }
    }

    private FriendLink requireLink(Long id) {
        FriendLink link = linkMapper.selectById(id);
        if (link == null) {
            throw new BizException(404, "友链不存在");
        }
        return link;
    }

    private void checkUrl(String url) {
        if (url == null || !url.trim().toLowerCase().startsWith("http")) {
            throw new BizException("站点地址必须以 http:// 或 https:// 开头");
        }
    }
}
