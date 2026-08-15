package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 相册图片保存请求
 */
@Data
public class AlbumRequest {

    @NotBlank(message = "图片地址不能为空")
    @Size(max = 255, message = "图片地址过长")
    private String url;

    @Size(max = 200, message = "描述不能超过 200 个字符")
    private String description;
}
