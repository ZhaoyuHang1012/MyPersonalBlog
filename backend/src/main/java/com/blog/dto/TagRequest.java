package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签保存请求
 */
@Data
public class TagRequest {

    @NotBlank(message = "标签名不能为空")
    private String name;
}
