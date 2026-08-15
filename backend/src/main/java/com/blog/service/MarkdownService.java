package com.blog.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Markdown 渲染服务（commonmark-java，默认转义内嵌 HTML，防 XSS）
 */
@Service
public class MarkdownService {

    private final List<Extension> extensions = List.of(TablesExtension.create());
    private final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

    public String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
