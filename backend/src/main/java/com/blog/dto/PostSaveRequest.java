package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 文章保存请求（新建/编辑共用）
 */
@Data
public class PostSaveRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    @NotBlank(message = "内容不能为空")
    private String contentMd;

    private Long categoryId;

    private List<Long> tagIds;

    /** 0 草稿 1 发布 */
    private Integer status;

    /** 0 否 1 置顶 */
    private Integer isTop;
}
