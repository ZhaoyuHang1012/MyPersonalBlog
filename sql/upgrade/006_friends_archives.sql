-- =============================================
-- 第三阶段 · 好友与归档 迁移脚本
-- 执行方式: mysql -uroot -p blog < 006_friends_archives.sql
-- =============================================
USE blog;

-- 好友关系（同意时插入双向两行）
CREATE TABLE IF NOT EXISTS friends (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL COMMENT '用户',
    friend_id  BIGINT UNSIGNED NOT NULL COMMENT '好友',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pair (user_id, friend_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '好友关系';

-- 好友申请
CREATE TABLE IF NOT EXISTS friend_requests (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    from_user_id BIGINT UNSIGNED NOT NULL COMMENT '申请人',
    to_user_id   BIGINT UNSIGNED NOT NULL COMMENT '接收人',
    message      VARCHAR(200)    DEFAULT NULL COMMENT '附言',
    status       TINYINT         NOT NULL DEFAULT 0 COMMENT '0待处理 1已同意 2已拒绝',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_to (to_user_id, status),
    KEY idx_from (from_user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '好友申请';

-- 归档收藏（文章/说说/相册）
CREATE TABLE IF NOT EXISTS archives (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '收藏者',
    target_type VARCHAR(10)     NOT NULL COMMENT 'post / murmur / album',
    target_id   BIGINT UNSIGNED NOT NULL COMMENT '目标 ID',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_target (user_id, target_type, target_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '归档收藏表';
