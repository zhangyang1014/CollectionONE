-- 创建甲方管理员表
-- 根据PRD要求，甲方管理员在创建甲方时同时创建
CREATE TABLE IF NOT EXISTS `tenant_admins` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` INT NOT NULL COMMENT '所属甲方ID',
    `account_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '账号编码',
    `account_name` VARCHAR(200) NOT NULL COMMENT '账号名称（管理员姓名）',
    `login_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '登录ID（唯一）',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt加密）',
    `email` VARCHAR(100) COMMENT '邮箱',
    `mobile` VARCHAR(50) COMMENT '手机号',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `last_login_at` DATETIME COMMENT '最近登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_id` (`login_id`),
    UNIQUE KEY `uk_account_code` (`account_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_tenant_admin_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方管理员表';

