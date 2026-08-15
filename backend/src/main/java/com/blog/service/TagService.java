package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.entity.PostTag;
import com.blog.entity.Tag;
import com.blog.mapper.PostTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签服务
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;

    public List<TagVO> list() {
        return tagMapper.listWithPostCount();
    }

    @Transactional
    public Tag create(String name) {
        checkName(name);
        Tag tag = new Tag();
        tag.setName(name.trim());
        tagMapper.insert(tag);
        return tag;
    }

    @Transactional
    public void update(Long id, String name) {
        checkName(name);
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name.trim());
        tagMapper.updateById(tag);
    }

    @Transactional
    public void delete(Long id) {
        postTagMapper.delete(new QueryWrapper<PostTag>().eq("tag_id", id));
        tagMapper.deleteById(id);
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("标签名不能为空");
        }
        if (name.trim().length() > 50) {
            throw new BizException("标签名不能超过 50 个字符");
        }
    }
}
