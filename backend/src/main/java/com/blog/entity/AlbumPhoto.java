package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册照片（图片或视频）
 */
@Data
@TableName("album_photos")
public class AlbumPhoto {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    private String url;

    /** image / video */
    private String mediaType;

    private String description;

    private LocalDateTime createdAt;
}
