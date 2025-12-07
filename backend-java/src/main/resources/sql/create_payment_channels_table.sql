-- 创建还款渠道配置表
-- 创建日期：2025-11-25
-- 说明：创建还款渠道配置表，用于管理控台配置还款渠道

CREATE TABLE IF NOT EXISTS `payment_channels` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `party_id` BIGINT NOT NULL COMMENT '甲方ID',
  `channel_name` VARCHAR(100) NOT NULL COMMENT '支付名称',
  `channel_icon` VARCHAR(500) COMMENT '图标URL',
  `channel_type` ENUM('VA', 'H5', 'QR') NOT NULL COMMENT '支付类型：VA-虚拟账户，H5-H5链接，QR-二维码',
  `service_provider` VARCHAR(100) COMMENT '服务公司',
  `description` TEXT COMMENT '渠道描述',
  `api_url` VARCHAR(500) NOT NULL COMMENT 'API地址',
  `api_method` ENUM('GET', 'POST') DEFAULT 'POST' COMMENT '请求方法',
  `auth_type` ENUM('API_KEY', 'BEARER', 'BASIC') NOT NULL COMMENT '认证方式',
  `auth_config` JSON COMMENT '认证配置（加密存储）',
  `request_params` JSON COMMENT '接口入参模板',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重，越小越靠前',
  `created_by` BIGINT COMMENT '创建人ID',
  `updated_by` BIGINT COMMENT '更新人ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_party_enabled` (`party_id`, `is_enabled`, `sort_order`),
  INDEX `idx_party_id` (`party_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款渠道配置表';



















