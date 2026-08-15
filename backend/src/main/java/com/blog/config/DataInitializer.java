package com.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.entity.Category;
import com.blog.entity.Post;
import com.blog.entity.PostTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.PostMapper;
import com.blog.mapper.PostTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动初始化：创建默认管理员、示例文章
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PasswordEncoder passwordEncoder;
    private final MarkdownService markdownService;

    @Override
    public void run(ApplicationArguments args) {
        initAdmin();
        initSamplePost();
    }

    private void initAdmin() {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("博主");
        admin.setCreatedAt(LocalDateTime.now());
        userMapper.insert(admin);
        log.info("已创建默认管理员账号：admin / admin123，请尽快修改密码");
    }

    private void initSamplePost() {
        if (postMapper.selectCount(null) > 0) {
            return;
        }
        String md = """
                # 欢迎使用我的博客平台
                
                这是一篇示例文章，用来演示 Markdown 渲染效果。你可以在后台的「文章管理」中编辑或删除它。
                
                ## 代码高亮
                
                ```java
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello, Blog!");
                    }
                }
                ```
                
                ## 表格支持
                
                | 功能 | 状态 |
                | ---- | ---- |
                | 文章管理 | 已支持 |
                | Markdown 编辑器 | 已支持 |
                | 分类与标签 | 已支持 |
                
                ## 更多排版
                
                - 支持**粗体**、*斜体*、~~删除线~~
                - 支持[链接](https://www.baidu.com)和 `行内代码`
                
                > 现在就去后台写下你的第一篇文章吧！
                """;
        Post post = new Post();
        post.setTitle("欢迎使用我的博客平台");
        post.setSummary("这是一篇示例文章，介绍博客平台的基础功能。");
        post.setContentMd(md);
        post.setContentHtml(markdownService.render(md));
        post.setStatus(1);
        post.setIsTop(1);
        post.setViewCount(0L);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setPublishedAt(LocalDateTime.now());
        Category category = categoryMapper.selectOne(new QueryWrapper<Category>().orderByAsc("id").last("LIMIT 1"));
        post.setCategoryId(category == null ? null : category.getId());
        postMapper.insert(post);

        Tag tag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("name", "技术"));
        if (tag != null) {
            PostTag pt = new PostTag();
            pt.setPostId(post.getId());
            pt.setTagId(tag.getId());
            postTagMapper.insert(pt);
        }
        log.info("已创建示例文章：欢迎使用我的博客平台");
    }
}
