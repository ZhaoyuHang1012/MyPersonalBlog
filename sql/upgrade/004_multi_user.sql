-- =============================================
-- 第三阶段 · 多用户平台 迁移脚本
-- 执行方式: mysql -uroot -p blog < 004_multi_user.sql
-- =============================================
USE blog;

-- 用户表：角色与存储配额
ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'ADMIN 管理员 / USER 普通用户' AFTER avatar,
    ADD COLUMN quota BIGINT NOT NULL DEFAULT 1073741824 COMMENT '存储配额（字节），默认 1GB' AFTER role;

-- 已有管理员账号提升为 ADMIN
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';

-- 文章表：作者与可见性
ALTER TABLE posts
    ADD COLUMN user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '作者 ID' AFTER id,
    ADD COLUMN visibility TINYINT NOT NULL DEFAULT 1 COMMENT '0 仅自己可见 / 1 开放' AFTER status;

-- 存量文章归属管理员
UPDATE posts SET user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1)
WHERE user_id IS NULL;

-- 邀请码表
CREATE TABLE IF NOT EXISTS invite_codes (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code       VARCHAR(32)     NOT NULL COMMENT '邀请码',
    used       TINYINT         NOT NULL DEFAULT 0 COMMENT '0 未使用 1 已使用',
    used_by    VARCHAR(50)     DEFAULT NULL COMMENT '使用者用户名',
    used_at    DATETIME        DEFAULT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '邀请码表';
