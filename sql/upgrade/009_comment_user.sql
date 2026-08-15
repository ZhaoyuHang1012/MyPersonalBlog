-- =============================================
-- 第三阶段 · 评论记录作者 迁移脚本
-- 执行方式: mysql -uroot -p blog < 009_comment_user.sql
-- =============================================
USE blog;

-- 评论记录发布者 ID（识别文章作者的评论，显示 UP 标签）
ALTER TABLE comments
    ADD COLUMN user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '评论者用户 ID' AFTER email;
