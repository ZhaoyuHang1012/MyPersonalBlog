package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请码
 */
@Data
@TableName("invite_codes")
public class InviteCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    /** 0 未使用 1 已使用 */
    private Integer used;

    private String usedBy;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}
