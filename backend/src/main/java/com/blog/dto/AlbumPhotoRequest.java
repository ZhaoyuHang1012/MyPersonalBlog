package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 相册照片保存请求
 */
@Data
public class AlbumPhotoRequest {

    @NotBlank(message = "文件地址不能为空")
    @Size(max = 255, message = "文件地址过长")
    private String url;

    /** image / video，默认 image */
    private String mediaType;

    @Size(max = 200, message = "描述不能超过 200 个字符")
    private String description;
}
