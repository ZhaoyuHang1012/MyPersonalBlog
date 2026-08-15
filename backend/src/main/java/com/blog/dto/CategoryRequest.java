package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类保存请求
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "分类名不能为空")
    private String name;

    private Integer sort;
}
