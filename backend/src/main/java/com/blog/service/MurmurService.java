package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.common.BizException;
import com.blog.entity.Murmur;
import com.blog.mapper.MurmurMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 说说服务
 */
@Service
@RequiredArgsConstructor
public class MurmurService {

    private final MurmurMapper murmurMapper;

    /** 前台：全部说说（倒序，最多 200 条） */
    public List<Murmur> list() {
        return murmurMapper.selectList(new QueryWrapper<Murmur>()
                .orderByDesc("id")
                .last("LIMIT 200"));
    }

    @Transactional
    public Murmur create(String content) {
        Murmur murmur = new Murmur();
        murmur.setContent(content.trim());
        murmur.setCreatedAt(LocalDateTime.now());
        murmurMapper.insert(murmur);
        return murmur;
    }

    @Transactional
    public void delete(Long id) {
        if (murmurMapper.deleteById(id) == 0) {
            throw new BizException(404, "说说不存在");
        }
    }
}
