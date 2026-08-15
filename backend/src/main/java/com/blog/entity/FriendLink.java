package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链
 */
@Data
@TableName("friend_links")
public class FriendLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String url;

    private String description;

    /** 0 待审核 1 已通过 */
    private Integer status;

    private Integer sort;

    private LocalDateTime createdAt;
}
