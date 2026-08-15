package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 相册组保存请求
 */
@Data
public class AlbumGroupRequest {

    @NotBlank(message = "相册名称不能为空")
    @Size(max = 50, message = "相册名称不能超过 50 个字符")
    private String name;

    /** 0 仅自己可见 1 开放 */
    private Integer visibility;
}
