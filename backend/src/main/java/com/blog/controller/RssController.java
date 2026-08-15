package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.Post;
import com.blog.mapper.PostMapper;
import com.blog.service.SiteService;
import com.blog.vo.SiteInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * RSS 订阅（RSS 2.0）
 */
@RestController
@RequiredArgsConstructor
public class RssController {

    private static final DateTimeFormatter RFC822 =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final PostMapper postMapper;
    private final SiteService siteService;

    @Value("${blog.site-url:http://localhost:8080}")
    private String siteUrl;

    @GetMapping(value = "/api/rss", produces = "application/xml;charset=UTF-8")
    public String rss() {
        SiteInfoVO site = siteService.info();
        List<Post> posts = postMapper.selectList(new QueryWrapper<Post>()
                .select("id", "title", "content_html", "published_at")
                .eq("status", 1)
                .eq("visibility", 1)
                .orderByDesc("published_at")
                .last("LIMIT 20"));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\"><channel>\n");
        xml.append("<title>").append(escape(site.getTitle())).append("</title>\n");
        xml.append("<link>").append(escape(siteUrl)).append("</link>\n");
        xml.append("<description>").append(escape(site.getSubtitle())).append("</description>\n");
        xml.append("<language>zh-CN</language>\n");
        for (Post p : posts) {
            String link = siteUrl + "/post/" + p.getId();
            xml.append("<item>\n");
            xml.append("<title>").append(escape(p.getTitle())).append("</title>\n");
            xml.append("<link>").append(escape(link)).append("</link>\n");
            xml.append("<guid>").append(escape(link)).append("</guid>\n");
            xml.append("<pubDate>").append(formatRfc822(p.getPublishedAt())).append("</pubDate>\n");
            xml.append("<description><![CDATA[")
                    .append(p.getContentHtml() == null ? "" : p.getContentHtml())
                    .append("]]></description>\n");
            xml.append("</item>\n");
        }
        xml.append("</channel></rss>");
        return xml.toString();
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private String formatRfc822(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return RFC822.format(time.atZone(ZoneId.systemDefault()));
    }
}
