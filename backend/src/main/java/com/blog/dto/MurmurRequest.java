package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 说说发布请求
 */
@Data
public class MurmurRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不能超过 2000 个字符")
    private String content;
}
