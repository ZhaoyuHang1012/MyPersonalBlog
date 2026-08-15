package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 友链保存请求（后台）
 */
@Data
public class LinkSaveRequest {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 50, message = "站点名称不能超过 50 个字符")
    private String name;

    @NotBlank(message = "站点地址不能为空")
    @Size(max = 255, message = "站点地址不能超过 255 个字符")
    private String url;

    @Size(max = 200, message = "站点描述不能超过 200 个字符")
    private String description;

    private Integer sort;
}
