-- =============================================
-- 第二阶段 · 批次1 增量脚本：站点设置表 + 文章评论数
-- 执行方式: mysql -uroot -p blog < 001_settings_comments.sql
-- =============================================
USE blog;

-- 站点设置表（key-value）
CREATE TABLE IF NOT EXISTS settings (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    skey       VARCHAR(100)    NOT NULL COMMENT '配置键',
    svalue     TEXT            COMMENT '配置值',
    remark     VARCHAR(200)    DEFAULT NULL COMMENT '说明',
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_skey (skey)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '站点设置表';

-- 文章表新增评论数字段
ALTER TABLE posts
    ADD COLUMN comment_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数' AFTER view_count;
