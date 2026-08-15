package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站点设置（key-value）
 */
@Data
@TableName("settings")
public class Setting {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键 */
    private String skey;

    /** 配置值 */
    private String svalue;

    /** 说明 */
    private String remark;

    private LocalDateTime updatedAt;
}
