package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 站点基础信息（application.yml 中 blog.site 配置）
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.site")
public class SiteProperties {

    private String title = "我的博客";
    private String subtitle = "记录技术与生活";
    private String author = "博主";
}
