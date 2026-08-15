-- =============================================
-- 第三阶段 · 昵称唯一约束 迁移脚本
-- 执行方式: mysql -uroot -p blog < 008_nickname_unique.sql
-- =============================================
USE blog;

-- 昵称唯一（执行前请确认无重复昵称）
ALTER TABLE users
    ADD UNIQUE KEY uk_nickname (nickname);
