-- =============================================
-- 第二阶段 · 批次3 增量脚本：访问记录表 + 操作日志表
-- 执行方式: mysql -uroot -p blog < 002_visits_logs.sql
-- =============================================
USE blog;

-- 访问记录表（用于统计）
CREATE TABLE IF NOT EXISTS visits (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    post_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '文章 ID（列表/首页访问为空）',
    path       VARCHAR(200)    NOT NULL COMMENT '请求路径',
    ip         VARCHAR(64)     DEFAULT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at),
    KEY idx_post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '访问记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED DEFAULT NULL,
    username    VARCHAR(50)     DEFAULT NULL,
    method      VARCHAR(10)     NOT NULL COMMENT 'HTTP 方法',
    uri         VARCHAR(200)    NOT NULL COMMENT '请求路径',
    params      TEXT            COMMENT '请求参数（敏感字段已打码）',
    ip          VARCHAR(64)     DEFAULT NULL,
    duration_ms INT             NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
    success     TINYINT         NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';
