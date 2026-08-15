package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传文件信息（媒体库）
 */
@Data
public class UploadFileVO {

    /** 相对路径，如 2024/08/uuid.png */
    private String name;

    /** 访问 URL，如 /uploads/2024/08/uuid.png */
    private String url;

    /** 文件大小（字节） */
    private long size;

    private LocalDateTime lastModified;
}
