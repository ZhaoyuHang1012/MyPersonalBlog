package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 归档请求
 */
@Data
public class ArchiveRequest {

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "目标 ID 不能为空")
    private Long targetId;
}
