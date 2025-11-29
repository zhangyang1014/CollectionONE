-- CCO System MySQL Database Schema
-- Version: 1.0.0
-- Date: 2025-11-20

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cco_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cco_system`;

-- 1. 甲方配置表
CREATE TABLE IF NOT EXISTS `tenants` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '甲方编码',
    `tenant_name` VARCHAR(200) NOT NULL COMMENT '甲方名称',
    `tenant_name_en` VARCHAR(200) COMMENT '甲方名称（英文）',
    `country_code` VARCHAR(10) COMMENT '国家代码',
    `timezone` INT DEFAULT 0 COMMENT '时区偏移量（UTC偏移，范围-12到+14）',
    `currency_code` VARCHAR(10) DEFAULT 'USD' COMMENT '货币代码',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_code` (`tenant_code`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方配置表';

-- 2. 字段分组表
CREATE TABLE IF NOT EXISTS `field_groups` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '分组标识',
    `group_name` VARCHAR(200) NOT NULL COMMENT '分组名称（中文）',
    `group_name_en` VARCHAR(200) COMMENT '分组名称（英文）',
    `parent_id` BIGINT COMMENT '父分组ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_group_key` (`group_key`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段分组表';

-- 3. 标准字段定义表
CREATE TABLE IF NOT EXISTS `standard_fields` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `field_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '字段唯一标识',
    `field_name` VARCHAR(200) NOT NULL COMMENT '字段名称（中文）',
    `field_name_en` VARCHAR(200) COMMENT '字段名称（英文）',
    `field_type` VARCHAR(50) NOT NULL COMMENT '字段类型',
    `field_group_id` BIGINT NOT NULL COMMENT '所属分组ID',
    `is_required` TINYINT(1) DEFAULT 0 COMMENT '是否必填',
    `is_extended` TINYINT(1) DEFAULT 0 COMMENT '是否为拓展字段',
    `description` TEXT COMMENT '字段说明',
    `example_value` TEXT COMMENT '示例值',
    `validation_rules` JSON COMMENT '验证规则（JSON格式）',
    `enum_options` JSON COMMENT '枚举选项（如果是Enum类型）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '软删除标记',
    `deleted_at` DATETIME COMMENT '删除时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_field_key` (`field_key`),
    KEY `idx_field_group_id` (`field_group_id`),
    CONSTRAINT `fk_standard_field_group` FOREIGN KEY (`field_group_id`) REFERENCES `field_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准字段定义表';

-- 4. 自定义字段定义表
CREATE TABLE IF NOT EXISTS `custom_fields` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `field_key` VARCHAR(100) NOT NULL COMMENT '字段唯一标识',
    `field_name` VARCHAR(200) NOT NULL COMMENT '字段名称',
    `field_name_en` VARCHAR(200) COMMENT '字段名称（英文）',
    `field_type` VARCHAR(50) NOT NULL COMMENT '字段类型',
    `field_group_id` BIGINT NOT NULL COMMENT '所属分组ID',
    `is_required` TINYINT(1) DEFAULT 0 COMMENT '是否必填',
    `description` TEXT COMMENT '字段说明',
    `example_value` TEXT COMMENT '示例值',
    `validation_rules` JSON COMMENT '验证规则',
    `enum_options` JSON COMMENT '枚举选项（如果是Enum类型）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '软删除标记',
    `deleted_at` DATETIME COMMENT '删除时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_field_group_id` (`field_group_id`),
    CONSTRAINT `fk_custom_field_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
    CONSTRAINT `fk_custom_field_group` FOREIGN KEY (`field_group_id`) REFERENCES `field_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义字段定义表';

-- 5. 甲方字段启用配置表
CREATE TABLE IF NOT EXISTS `tenant_field_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '甲方ID',
    `field_id` BIGINT NOT NULL COMMENT '字段ID（标准字段或自定义字段）',
    `field_type` VARCHAR(20) NOT NULL COMMENT '字段类型：standard/custom',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `is_required` TINYINT(1) DEFAULT 0 COMMENT '是否必填',
    `is_readonly` TINYINT(1) DEFAULT 0 COMMENT '是否只读',
    `is_visible` TINYINT(1) DEFAULT 1 COMMENT '是否可见',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    CONSTRAINT `fk_tenant_field_config` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方字段启用配置表';

-- 6. 甲方字段展示配置表
CREATE TABLE IF NOT EXISTS `tenant_field_display_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `scene_type` VARCHAR(50) NOT NULL COMMENT '场景类型',
    `scene_name` VARCHAR(100) NOT NULL COMMENT '场景名称',
    `field_key` VARCHAR(100) NOT NULL COMMENT '字段标识',
    `field_name` VARCHAR(200) NOT NULL COMMENT '字段名称',
    `field_data_type` VARCHAR(50) COMMENT '字段数据类型',
    `field_source` VARCHAR(20) COMMENT '字段来源：standard/custom',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `display_width` INT NOT NULL DEFAULT 0 COMMENT '显示宽度（像素），0表示自动',
    `color_type` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '颜色类型',
    `color_rule` JSON COMMENT '颜色规则（条件表达式）',
    `hide_rule` JSON COMMENT '隐藏规则',
    `hide_for_queues` JSON COMMENT '对哪些队列隐藏（队列ID数组）',
    `hide_for_agencies` JSON COMMENT '对哪些机构隐藏（机构ID数组）',
    `hide_for_teams` JSON COMMENT '对哪些小组隐藏（小组ID数组）',
    `format_rule` JSON COMMENT '格式化规则',
    `is_searchable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可搜索',
    `is_filterable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可筛选',
    `is_range_searchable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否支持范围检索',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(100) COMMENT '创建人',
    `updated_by` VARCHAR(100) COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_scene` (`tenant_id`, `scene_type`),
    KEY `idx_field_key` (`field_key`),
    CONSTRAINT `fk_display_config_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方字段展示配置表';

-- 7. 催收机构表
CREATE TABLE IF NOT EXISTS `collection_agencies` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_code` VARCHAR(100) NOT NULL COMMENT '机构编码',
    `agency_name` VARCHAR(200) NOT NULL COMMENT '机构名称',
    `agency_name_en` VARCHAR(200) COMMENT '机构名称（英文）',
    `contact_person` VARCHAR(100) COMMENT '联系人',
    `contact_phone` VARCHAR(50) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `address` TEXT COMMENT '机构地址',
    `description` TEXT COMMENT '机构描述',
    `timezone` INT COMMENT '时区偏移量（UTC偏移，范围-12到+14）',
    `agency_type` VARCHAR(20) DEFAULT 'real' COMMENT '机构类型：real=真实机构，virtual=虚拟机构',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_agency_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='催收机构表';

-- 8. 小组群表
CREATE TABLE IF NOT EXISTS `team_groups` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_id` BIGINT NOT NULL COMMENT '所属催收机构ID',
    `group_code` VARCHAR(100) NOT NULL COMMENT '小组群编码',
    `group_name` VARCHAR(200) NOT NULL COMMENT '小组群名称',
    `group_name_en` VARCHAR(200) COMMENT '小组群名称（英文）',
    `spv_id` BIGINT COMMENT '小组群长SPV ID（催员ID）',
    `description` TEXT COMMENT '小组群描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_team_group_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
    CONSTRAINT `fk_team_group_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组群表';

-- 9. 案件队列表
CREATE TABLE IF NOT EXISTS `case_queues` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `queue_code` VARCHAR(100) NOT NULL COMMENT '队列编码（如：M1, M2, M3+, LEGAL）',
    `queue_name` VARCHAR(200) NOT NULL COMMENT '队列名称',
    `queue_name_en` VARCHAR(200) COMMENT '队列名称（英文）',
    `queue_description` TEXT COMMENT '队列描述',
    `overdue_days_start` INT COMMENT '逾期天数起始值（null表示负无穷）',
    `overdue_days_end` INT COMMENT '逾期天数结束值（null表示正无穷）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_sort_order` (`sort_order`),
    CONSTRAINT `fk_queue_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件队列表';

-- 10. 催收小组表
CREATE TABLE IF NOT EXISTS `collection_teams` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_id` BIGINT NOT NULL COMMENT '所属催收机构ID',
    `team_group_id` BIGINT COMMENT '所属小组群ID',
    `queue_id` BIGINT NOT NULL COMMENT '关联的催收队列ID（必选）',
    `team_code` VARCHAR(100) NOT NULL COMMENT '小组编码',
    `team_name` VARCHAR(200) NOT NULL COMMENT '小组名称',
    `team_name_en` VARCHAR(200) COMMENT '小组名称（英文）',
    `team_leader_id` BIGINT COMMENT '组长ID（催员ID）',
    `team_type` VARCHAR(50) COMMENT '小组类型',
    `description` TEXT COMMENT '小组描述',
    `max_case_count` INT DEFAULT 0 COMMENT '最大案件数量（0表示不限制）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_team_group_id` (`team_group_id`),
    KEY `idx_queue_id` (`queue_id`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_team_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
    CONSTRAINT `fk_team_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`),
    CONSTRAINT `fk_team_group` FOREIGN KEY (`team_group_id`) REFERENCES `team_groups` (`id`),
    CONSTRAINT `fk_team_queue` FOREIGN KEY (`queue_id`) REFERENCES `case_queues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='催收小组表';

-- 11. 催员表
CREATE TABLE IF NOT EXISTS `collectors` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_id` BIGINT NOT NULL COMMENT '所属机构ID',
    `team_id` BIGINT NOT NULL COMMENT '所属小组ID',
    `collector_code` VARCHAR(100) NOT NULL COMMENT '催员编码',
    `collector_name` VARCHAR(100) NOT NULL COMMENT '催员姓名',
    `login_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '登录ID',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `mobile` VARCHAR(50) COMMENT '手机号码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `employee_no` VARCHAR(50) COMMENT '工号',
    `collector_level` VARCHAR(50) COMMENT '催员等级',
    `max_case_count` INT DEFAULT 100 COMMENT '最大案件数量',
    `current_case_count` INT DEFAULT 0 COMMENT '当前案件数量',
    `specialties` JSON COMMENT '擅长领域（JSON数组）',
    `performance_score` DECIMAL(5, 2) COMMENT '绩效评分',
    `status` VARCHAR(50) DEFAULT 'active' COMMENT '状态',
    `hire_date` DATE COMMENT '入职日期',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_id` (`login_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_team_id` (`team_id`),
    KEY `idx_collector_code` (`collector_code`),
    KEY `idx_status` (`status`),
    KEY `idx_is_active` (`is_active`),
    CONSTRAINT `fk_collector_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
    CONSTRAINT `fk_collector_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`),
    CONSTRAINT `fk_collector_team` FOREIGN KEY (`team_id`) REFERENCES `collection_teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='催员表';

-- 12. 案件主表
CREATE TABLE IF NOT EXISTS `cases` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '案件唯一标识',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_id` BIGINT COMMENT '所属催收机构ID',
    `team_id` BIGINT COMMENT '所属催收小组ID',
    `collector_id` BIGINT COMMENT '分配催员ID',
    `queue_id` BIGINT COMMENT '所属队列ID',
    `user_id` VARCHAR(100) COMMENT '用户编号',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `mobile` VARCHAR(50) COMMENT '手机号',
    `case_status` VARCHAR(50) COMMENT '案件状态',
    `overdue_days` INT COMMENT '逾期天数（用于自动分配队列）',
    `loan_amount` DECIMAL(15, 2) COMMENT '贷款金额',
    `repaid_amount` DECIMAL(15, 2) DEFAULT 0 COMMENT '已还款金额',
    `outstanding_amount` DECIMAL(15, 2) COMMENT '逾期金额',
    `due_date` DATETIME COMMENT '到期日期',
    `settlement_date` DATETIME COMMENT '结清日期',
    `assigned_at` DATETIME COMMENT '分配时间',
    `last_contact_at` DATETIME COMMENT '最后联系时间',
    `next_follow_up_at` DATETIME COMMENT '下次跟进时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_code` (`case_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_team_id` (`team_id`),
    KEY `idx_collector_id` (`collector_id`),
    KEY `idx_queue_id` (`queue_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_case_status` (`case_status`),
    KEY `idx_overdue_days` (`overdue_days`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_assigned_at` (`assigned_at`),
    CONSTRAINT `fk_case_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
    CONSTRAINT `fk_case_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`),
    CONSTRAINT `fk_case_team` FOREIGN KEY (`team_id`) REFERENCES `collection_teams` (`id`),
    CONSTRAINT `fk_case_collector` FOREIGN KEY (`collector_id`) REFERENCES `collectors` (`id`),
    CONSTRAINT `fk_case_queue` FOREIGN KEY (`queue_id`) REFERENCES `case_queues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件主表';

-- 13. 通知模板表
CREATE TABLE IF NOT EXISTS `notification_templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT COMMENT '甲方ID（NULL表示全局模板）',
    `template_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '模板ID（唯一标识）',
    `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型',
    `description` TEXT COMMENT '模板描述',
    `content_template` TEXT NOT NULL COMMENT '通知正文模板',
    `jump_url_template` TEXT COMMENT '点击后跳转的URL模板',
    `target_type` VARCHAR(20) NOT NULL DEFAULT 'agency' COMMENT '发送对象类型',
    `target_agencies` JSON COMMENT '目标机构ID列表（JSON数组）',
    `target_teams` JSON COMMENT '目标小组ID列表（JSON数组）',
    `target_collectors` JSON COMMENT '目标催员ID列表（JSON数组）',
    `is_forced_read` TINYINT(1) DEFAULT 0 COMMENT '是否强制阅读',
    `repeat_interval_minutes` INT COMMENT '非强制阅读时的重复提醒间隔（分钟）',
    `max_remind_count` INT COMMENT '非强制阅读时的最大提醒次数',
    `notify_time_start` VARCHAR(5) COMMENT '通知时间范围开始（HH:MM）',
    `notify_time_end` VARCHAR(5) COMMENT '通知时间范围结束（HH:MM）',
    `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级',
    `display_duration_seconds` INT DEFAULT 5 COMMENT '展示时长（秒）',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `available_variables` JSON COMMENT '可用变量列表及说明（JSON）',
    `total_sent` INT DEFAULT 0 COMMENT '累计发送次数',
    `total_read` INT DEFAULT 0 COMMENT '累计阅读次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_id` (`template_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_template_type` (`template_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 14. 通知配置表
CREATE TABLE IF NOT EXISTS `notification_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT COMMENT '甲方ID（NULL表示全局配置）',
    `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `config_data` JSON NOT NULL COMMENT '配置数据（JSON格式）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_notification_type` (`notification_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知配置表';

-- 15. 公共通知表
CREATE TABLE IF NOT EXISTS `public_notifications` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT COMMENT '甲方ID（NULL表示全局通知）',
    `agency_id` BIGINT COMMENT '机构ID（NULL表示甲方级别通知）',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知正文内容',
    `h5_content` TEXT COMMENT 'H5链接地址（可选）',
    `carousel_interval_seconds` INT DEFAULT 30 COMMENT '轮播间隔（秒）',
    `is_forced_read` TINYINT(1) DEFAULT 0 COMMENT '是否强制阅读',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `repeat_interval_minutes` INT COMMENT '重复提醒时间间隔（分钟）',
    `max_remind_count` INT COMMENT '最大提醒次数',
    `notify_time_start` VARCHAR(5) COMMENT '通知时间范围开始（HH:MM）',
    `notify_time_end` VARCHAR(5) COMMENT '通知时间范围结束（HH:MM）',
    `effective_start_time` DATETIME COMMENT '生效开始时间',
    `effective_end_time` DATETIME COMMENT '生效结束时间',
    `notify_roles` TEXT COMMENT '通知对象角色列表（JSON字符串）',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共通知表';

-- 16. 案件联系人表
CREATE TABLE IF NOT EXISTS `case_contacts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_id` BIGINT NOT NULL COMMENT '案件ID',
    `contact_name` VARCHAR(100) NOT NULL COMMENT '联系人姓名',
    `phone_number` VARCHAR(50) NOT NULL COMMENT '联系电话',
    `relation` VARCHAR(50) NOT NULL COMMENT '关系（本人/配偶/朋友/同事/家人等）',
    `is_primary` TINYINT(1) DEFAULT 0 COMMENT '是否本人',
    `available_channels` JSON COMMENT '可用通信渠道，格式：["whatsapp", "sms", "call"]',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_case_id` (`case_id`),
    KEY `idx_is_primary` (`is_primary`),
    CONSTRAINT `fk_case_contact_case` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件联系人表';

-- 17. 通信记录表
CREATE TABLE IF NOT EXISTS `communication_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_id` BIGINT NOT NULL COMMENT '案件ID',
    `collector_id` BIGINT NOT NULL COMMENT '催员ID',
    `contact_person_id` BIGINT COMMENT '联系人ID（本人或其他联系人）',
    `channel` VARCHAR(50) NOT NULL COMMENT '通信渠道: phone, whatsapp, sms, rcs',
    `direction` VARCHAR(20) NOT NULL COMMENT '通信方向: inbound, outbound',
    `supplier_id` BIGINT COMMENT '供应商ID（标识使用的外呼供应商）',
    `infinity_extension_number` VARCHAR(50) COMMENT 'Infinity返回的分机号',
    `call_uuid` VARCHAR(100) COMMENT 'Infinity返回的通话唯一标识',
    `custom_params` JSON COMMENT '自定义参数（JSON格式）',
    `call_duration` INT COMMENT '通话时长（秒）- 电话专属',
    `is_connected` TINYINT(1) COMMENT '是否接通 - 电话专属',
    `call_record_url` VARCHAR(500) COMMENT '录音链接 - 电话专属',
    `is_replied` TINYINT(1) COMMENT '是否回复 - 消息专属（WhatsApp/SMS/RCS）',
    `message_content` TEXT COMMENT '消息内容 - 消息专属',
    `contact_result` VARCHAR(50) COMMENT '联系结果: contacted(可联), connected(已接通), not_connected(未接通), refused(拒绝), invalid_number(无效号码) 等',
    `ttfc_seconds` INT COMMENT '首次触达时长（秒，从案件分配到首次有效触达）',
    `remark` TEXT COMMENT '备注',
    `contacted_at` DATETIME NOT NULL COMMENT '触达时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_case_id` (`case_id`),
    KEY `idx_collector_id` (`collector_id`),
    KEY `idx_contact_person_id` (`contact_person_id`),
    KEY `idx_channel` (`channel`),
    KEY `idx_contact_result` (`contact_result`),
    KEY `idx_contacted_at` (`contacted_at`),
    CONSTRAINT `fk_communication_case` FOREIGN KEY (`case_id`) REFERENCES `cases` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_communication_collector` FOREIGN KEY (`collector_id`) REFERENCES `collectors` (`id`),
    CONSTRAINT `fk_communication_contact` FOREIGN KEY (`contact_person_id`) REFERENCES `case_contacts` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通信记录表';

-- 注意：还有其他表（案件字段值表、PTP记录表、质检记录表等）
-- 这里为了简洁，只列出了核心的表
-- 在实际部署时，需要根据Python模型创建完整的表结构

-- 创建索引优化查询性能（如果索引不存在，需要手动检查）
-- ALTER TABLE `tenants` ADD INDEX `idx_created_at` (`created_at`);
-- ALTER TABLE `cases` ADD INDEX `idx_created_at` (`created_at`);
-- ALTER TABLE `collectors` ADD INDEX `idx_created_at` (`created_at`);

