-- =============================================
-- 第三阶段 · 点赞功能 迁移脚本
-- 执行方式: mysql -uroot -p blog < 007_likes.sql
-- =============================================
USE blog;

-- 点赞表（文章/说说）
CREATE TABLE IF NOT EXISTS likes (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '点赞人',
    target_type VARCHAR(10)     NOT NULL COMMENT 'post / murmur',
    target_id   BIGINT UNSIGNED NOT NULL COMMENT '目标 ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_target (user_id, target_type, target_id),
    KEY idx_target (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '点赞表';

-- 点赞计数列
ALTER TABLE posts
    ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER comment_count;

ALTER TABLE murmurs
    ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER images;
