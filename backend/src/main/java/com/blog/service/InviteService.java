package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.InviteCode;
import com.blog.mapper.InviteCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 邀请码服务
 */
@Service
@RequiredArgsConstructor
public class InviteService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InviteCodeMapper inviteCodeMapper;

    /** 生成指定数量的邀请码 */
    @Transactional
    public List<InviteCode> generate(int count) {
        if (count < 1 || count > 50) {
            count = 1;
        }
        List<InviteCode> codes = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            InviteCode invite = new InviteCode();
            invite.setCode(randomCode(12));
            invite.setUsed(0);
            invite.setCreatedAt(LocalDateTime.now());
            inviteCodeMapper.insert(invite);
            codes.add(invite);
        }
        return codes;
    }

    public List<InviteCode> list() {
        return inviteCodeMapper.selectList(new QueryWrapper<InviteCode>().orderByDesc("id"));
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
