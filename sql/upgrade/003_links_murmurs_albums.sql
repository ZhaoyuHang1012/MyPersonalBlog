-- =============================================
-- 第二阶段 · 批次4 增量脚本：友链表 + 说说表 + 相册表
-- 执行方式: mysql -uroot -p blog < 003_links_murmurs_albums.sql
-- =============================================
USE blog;

-- 友链表
CREATE TABLE IF NOT EXISTS friend_links (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL COMMENT '站点名称',
    url         VARCHAR(255)    NOT NULL COMMENT '站点地址',
    description VARCHAR(200)    DEFAULT NULL COMMENT '站点描述',
    status      TINYINT         NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过',
    sort        INT             NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '友链表';

-- 说说（树洞）表
CREATE TABLE IF NOT EXISTS murmurs (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    content    TEXT            NOT NULL COMMENT '说说内容',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '说说表';

-- 相册表（复用媒体库上传的图片）
CREATE TABLE IF NOT EXISTS albums (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    url         VARCHAR(255)    NOT NULL COMMENT '图片地址（/uploads/...）',
    description VARCHAR(200)    DEFAULT NULL COMMENT '图片描述',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '相册表';
