-- 催员登录白名单IP配置表
CREATE TABLE IF NOT EXISTS `collector_login_whitelist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
  `is_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用白名单IP登录管理（0-否，1-是）',
  `ip_address` VARCHAR(50) NOT NULL COMMENT '白名单IP地址（支持IPv4和IPv6，支持CIDR格式）',
  `description` VARCHAR(200) COMMENT 'IP地址描述/备注',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用（0-否，1-是）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_ip` (`tenant_id`, `ip_address`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_is_enabled` (`is_enabled`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='催员登录白名单IP配置表';



