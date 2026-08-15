package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相册组
 */
@Data
@TableName("album_groups")
public class AlbumGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String cover;

    /** 0 仅自己可见 1 开放 */
    private Integer visibility;

    private LocalDateTime createdAt;
}
