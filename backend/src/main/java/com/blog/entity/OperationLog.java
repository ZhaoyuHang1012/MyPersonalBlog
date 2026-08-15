package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String method;

    private String uri;

    private String params;

    private String ip;

    private Integer durationMs;

    /** 1 成功 0 失败 */
    private Integer success;

    private LocalDateTime createdAt;
}
