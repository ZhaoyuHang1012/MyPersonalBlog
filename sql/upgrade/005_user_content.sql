-- =============================================
-- 第三阶段 · 内容个人化 迁移脚本
-- 执行方式: mysql -uroot -p blog < 005_user_content.sql
-- =============================================
USE blog;

-- 说说个人化：归属 + 可见性 + 配图
ALTER TABLE murmurs
    ADD COLUMN user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '发布者' AFTER id,
    ADD COLUMN visibility TINYINT NOT NULL DEFAULT 1 COMMENT '0仅自己可见 1开放' AFTER content,
    ADD COLUMN images TEXT COMMENT '配图 URL JSON 数组' AFTER visibility;

-- 旧相册表弃用（空表，已由相册组/照片表替代）
DROP TABLE IF EXISTS albums;

-- 相册组（用户可创建多个相册）
CREATE TABLE IF NOT EXISTS album_groups (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id    BIGINT UNSIGNED NOT NULL COMMENT '所属用户',
    name       VARCHAR(50)     NOT NULL COMMENT '相册名称',
    cover      VARCHAR(255)    DEFAULT NULL COMMENT '封面 URL',
    visibility TINYINT         NOT NULL DEFAULT 1 COMMENT '0仅自己可见 1开放',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '相册组';

-- 相册照片（支持图片与视频）
CREATE TABLE IF NOT EXISTS album_photos (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    group_id    BIGINT UNSIGNED NOT NULL COMMENT '所属相册组',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '所属用户',
    url         VARCHAR(255)    NOT NULL COMMENT '文件地址',
    media_type  VARCHAR(10)     NOT NULL DEFAULT 'image' COMMENT 'image / video',
    description VARCHAR(200)    DEFAULT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_group (group_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '相册照片';
