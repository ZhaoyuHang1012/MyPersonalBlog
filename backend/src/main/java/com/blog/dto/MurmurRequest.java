package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 说说发布请求
 */
@Data
public class MurmurRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不能超过 2000 个字符")
    private String content;

    /** 配图 URL 列表（最多 9 张） */
    private List<String> images;

    /** 0 仅自己可见 1 开放 */
    private Integer visibility;
}
