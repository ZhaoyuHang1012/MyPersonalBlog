package com.blog.dto;

import lombok.Data;

/**
 * 站点设置更新请求（字段为空表示不修改）
 */
@Data
public class SiteUpdateRequest {

    private String title;

    private String subtitle;

    private String author;

    /** 备案号 */
    private String icp;

    /** 页脚文案 */
    private String footer;

    /** 公告（显示在首页顶部） */
    private String announcement;

    /** 关于页 Markdown 内容 */
    private String aboutMd;

    /** 是否允许评论：1 允许 0 关闭 */
    private Integer allowComments;
}
