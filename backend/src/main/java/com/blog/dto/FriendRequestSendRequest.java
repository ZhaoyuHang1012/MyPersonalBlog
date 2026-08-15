package com.blog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 好友申请请求
 */
@Data
public class FriendRequestSendRequest {

    @NotNull(message = "目标用户不能为空")
    private Long toUserId;

    @Size(max = 200, message = "附言不能超过 200 个字符")
    private String message;
}
