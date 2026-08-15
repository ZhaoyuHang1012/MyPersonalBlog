package com.blog.vo;

import lombok.Data;

/**
 * 站点信息（公开接口返回）
 */
@Data
public class SiteInfoVO {

    private String title;

    private String subtitle;

    private String author;

    /** 备案号 */
    private String icp;

    /** 页脚文案 */
    private String footer;

    /** 公告 */
    private String announcement;

    /** 关于页 Markdown 原文（后台编辑用） */
    private String aboutMd;

    /** 关于页渲染后 HTML（前台展示用） */
    private String aboutHtml;

    /** 是否允许评论：1 允许 0 关闭 */
    private Integer allowComments;
}
