-- =============================================
-- 第五阶段 · 说说评论 迁移脚本
-- 执行方式: mysql -uroot -p blog < 010_murmur_comments.sql
-- =============================================
USE blog;

-- 评论表支持说说评论：post_id 与 murmur_id 二选一
ALTER TABLE comments
    MODIFY post_id BIGINT UNSIGNED DEFAULT NULL COMMENT '文章 ID（说说评论时为 NULL）',
    ADD COLUMN murmur_id BIGINT UNSIGNED DEFAULT NULL COMMENT '说说 ID（文章评论时为 NULL）' AFTER post_id,
    ADD KEY idx_murmur (murmur_id);

-- 说说评论数
ALTER TABLE murmurs
    ADD COLUMN comment_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数' AFTER like_count;
