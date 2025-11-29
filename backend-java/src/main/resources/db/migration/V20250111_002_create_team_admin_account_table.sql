-- 创建小组管理员账号表
-- 根据PRD要求，小组管理员可以管理小组下的催员
CREATE TABLE IF NOT EXISTS `team_admin_accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` INT NOT NULL COMMENT '所属甲方ID',
    `agency_id` INT NOT NULL COMMENT '所属机构ID',
    `team_group_id` INT COMMENT '所属小组群ID（可选）',
    `team_id` INT COMMENT '所属小组ID（可选，SPV可以不关联小组）',
    `account_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '账号编码',
    `account_name` VARCHAR(200) NOT NULL COMMENT '账号名称',
    `login_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '登录ID（唯一）',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt加密）',
    `role` VARCHAR(50) NOT NULL COMMENT '角色：spv/team_leader/quality_inspector/statistician',
    `mobile` VARCHAR(50) COMMENT '手机号码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `remark` TEXT COMMENT '备注',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `last_login_at` DATETIME COMMENT '最近登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_id` (`login_id`),
    UNIQUE KEY `uk_account_code` (`account_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_team_group_id` (`team_group_id`),
    KEY `idx_team_id` (`team_id`),
    KEY `idx_role` (`role`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_team_admin_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
    -- 注意：以下外键约束在相关表创建后需要手动添加
    -- CONSTRAINT `fk_team_admin_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`) ON DELETE CASCADE,
    -- CONSTRAINT `fk_team_admin_team_group` FOREIGN KEY (`team_group_id`) REFERENCES `team_groups` (`id`) ON DELETE SET NULL,
    -- CONSTRAINT `fk_team_admin_team` FOREIGN KEY (`team_id`) REFERENCES `collection_teams` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组管理员账号表';

