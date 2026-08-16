-- =============================================
-- 个人博客平台 - 数据库初始化脚本
-- 执行方式: mysql -uroot -p < init.sql
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE blog;

-- 用户表（管理员/普通用户）
-- 注意：role/quota 等新列由 sql/upgrade/004 增量脚本添加，本文件保持原始结构
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)     NOT NULL COMMENT '登录名',
    password   VARCHAR(100)    NOT NULL COMMENT 'BCrypt 密码',
    nickname   VARCHAR(50)     NOT NULL COMMENT '昵称',
    avatar     VARCHAR(255)    DEFAULT NULL COMMENT '头像地址',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)     NOT NULL COMMENT '分类名',
    sort       INT             NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)     NOT NULL COMMENT '标签名',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '标签表';

-- 文章表
-- 注意：user_id/visibility/comment_count/like_count 等新列由 sql/upgrade/ 增量脚本添加
CREATE TABLE IF NOT EXISTS posts (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200)    NOT NULL COMMENT '标题',
    summary      VARCHAR(500)    DEFAULT NULL COMMENT '摘要',
    content_md   MEDIUMTEXT      NOT NULL COMMENT 'Markdown 原文',
    content_html MEDIUMTEXT      DEFAULT NULL COMMENT '渲染后的 HTML',
    category_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '分类 ID',
    status       TINYINT         NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
    is_top       TINYINT         NOT NULL DEFAULT 0 COMMENT '是否置顶',
    view_count   INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '浏览量',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at DATETIME        DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (id),
    KEY idx_status_top (status, is_top),
    KEY idx_category (category_id),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章表';

-- 文章-标签关联表
CREATE TABLE IF NOT EXISTS post_tag (
    post_id BIGINT UNSIGNED NOT NULL,
    tag_id  BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (post_id, tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章-标签关联表';

-- 评论表（第二阶段启用）
CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    post_id    BIGINT UNSIGNED NOT NULL COMMENT '文章 ID',
    parent_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论 ID（楼中楼）',
    nickname   VARCHAR(50)     NOT NULL COMMENT '昵称',
    email      VARCHAR(100)    NOT NULL COMMENT '邮箱',
    website    VARCHAR(200)    DEFAULT NULL COMMENT '个人网站',
    content    TEXT            NOT NULL COMMENT '评论内容',
    status     TINYINT         NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2垃圾',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评论表';

-- 初始分类与标签
INSERT INTO categories (name, sort) VALUES ('默认分类', 0)
    ON DUPLICATE KEY UPDATE name = name;
INSERT INTO tags (name) VALUES ('随笔'), ('技术')
    ON DUPLICATE KEY UPDATE name = name;

-- 说明：管理员账号由后端启动时自动初始化（admin / admin123），
-- 示例文章也由后端启动时自动创建。
