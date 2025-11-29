-- 甲方字段JSON版本表
CREATE TABLE IF NOT EXISTS `tenant_fields_json` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '甲方ID',
    `version` VARCHAR(20) COMMENT 'JSON版本号',
    `sync_time` DATETIME NOT NULL COMMENT '同步时间（版本时间）',
    `fields_json` JSON NOT NULL COMMENT '字段定义JSON（完整JSON）',
    `is_current` TINYINT(1) DEFAULT 1 COMMENT '是否当前版本（1=当前，0=历史）',
    `uploaded_by` VARCHAR(100) COMMENT '上传人',
    `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_is_current` (`is_current`),
    KEY `idx_uploaded_at` (`uploaded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方字段JSON版本表';

