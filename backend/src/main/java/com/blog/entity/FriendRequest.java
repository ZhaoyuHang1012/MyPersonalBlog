package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请
 */
@Data
@TableName("friend_requests")
public class FriendRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String message;

    /** 0 待处理 1 已同意 2 已拒绝 */
    private Integer status;

    private LocalDateTime createdAt;
}
