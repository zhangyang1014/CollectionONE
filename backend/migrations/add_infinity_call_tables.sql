-- Infinity外呼系统集成 - 数据库迁移脚本
-- 创建时间: 2025-11-21
-- 说明: 添加Infinity外呼配置、分机池管理等相关表

-- 1. 创建 Infinity 外呼配置表
CREATE TABLE IF NOT EXISTS `infinity_call_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `supplier_id` BIGINT NULL COMMENT '关联的渠道供应商ID（外键到 channel_suppliers）',
    
    -- API配置
    `api_url` VARCHAR(500) NOT NULL COMMENT 'Infinity API地址',
    `access_token` VARCHAR(500) NOT NULL COMMENT 'API访问令牌',
    `app_id` VARCHAR(100) NOT NULL COMMENT '应用ID（必填）',
    
    -- 号码配置
    `caller_number_range_start` VARCHAR(50) NULL COMMENT '号段起始（如：4001234000）',
    `caller_number_range_end` VARCHAR(50) NULL COMMENT '号段结束（如：4001234999）',
    
    -- 回调配置
    `callback_url` VARCHAR(500) NULL COMMENT '通话记录回调地址',
    `recording_callback_url` VARCHAR(500) NULL COMMENT '录音回调地址',
    
    -- 限制配置
    `max_concurrent_calls` INT DEFAULT 100 COMMENT '最大并发呼叫数',
    `call_timeout_seconds` INT DEFAULT 60 COMMENT '呼叫超时时间（秒）',
    
    -- 状态
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    
    -- 时间戳
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT NULL COMMENT '创建人ID',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_is_active` (`is_active`),
    
    CONSTRAINT `fk_infinity_config_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_infinity_config_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `channel_suppliers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Infinity外呼配置表';

-- 2. 创建 Infinity 分机池表
CREATE TABLE IF NOT EXISTS `infinity_extension_pool` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `config_id` BIGINT NOT NULL COMMENT '关联的 Infinity 配置ID',
    
    -- 分机信息
    `infinity_extension_number` VARCHAR(50) NOT NULL COMMENT '分机号（如 "8001"）',
    
    -- 状态信息
    `status` VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '状态：available/in_use/offline',
    `current_collector_id` BIGINT NULL COMMENT '当前使用的催员ID（空闲时为NULL）',
    
    -- 时间戳
    `assigned_at` DATETIME NULL COMMENT '分配时间',
    `released_at` DATETIME NULL COMMENT '释放时间',
    `last_used_at` DATETIME NULL COMMENT '最后使用时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_extension` (`tenant_id`, `infinity_extension_number`),
    KEY `idx_config_id` (`config_id`),
    KEY `idx_status` (`status`),
    KEY `idx_collector_id` (`current_collector_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    
    CONSTRAINT `fk_extension_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_extension_config` FOREIGN KEY (`config_id`) REFERENCES `infinity_call_configs` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_extension_collector` FOREIGN KEY (`current_collector_id`) REFERENCES `collectors` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Infinity分机池表';

-- 3. 扩展 collectors 表（添加外呼相关字段）
ALTER TABLE `collectors`
ADD COLUMN `callback_number` VARCHAR(50) NULL COMMENT '回呼号码（催员手机/座机，用于接收外呼）' AFTER `mobile`,
ADD COLUMN `infinity_extension_number` VARCHAR(50) NULL COMMENT '当前占用的分机号（仅通话中有值）' AFTER `callback_number`,
ADD KEY `idx_callback_number` (`callback_number`);

-- 4. 扩展 communication_records 表（添加外呼供应商相关字段）
ALTER TABLE `communication_records`
ADD COLUMN `supplier_id` BIGINT NULL COMMENT '供应商ID（标识使用的外呼供应商）' AFTER `channel`,
ADD COLUMN `infinity_extension_number` VARCHAR(50) NULL COMMENT '使用的分机号' AFTER `supplier_id`,
ADD COLUMN `call_uuid` VARCHAR(100) NULL COMMENT 'Infinity返回的通话唯一标识' AFTER `infinity_extension_number`,
ADD COLUMN `custom_params` JSON NULL COMMENT '自定义参数（JSON，存储 userid、memberid 等）' AFTER `call_uuid`,
ADD KEY `idx_supplier_id` (`supplier_id`),
ADD KEY `idx_call_uuid` (`call_uuid`);

-- 添加外键约束（如果 channel_suppliers 表存在）
ALTER TABLE `communication_records`
ADD CONSTRAINT `fk_comm_record_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `channel_suppliers` (`id`) ON DELETE SET NULL;

-- 创建分机使用统计视图（可选，用于监控）
CREATE OR REPLACE VIEW `v_infinity_extension_statistics` AS
SELECT 
    iep.tenant_id,
    iep.config_id,
    COUNT(*) as total_extensions,
    SUM(CASE WHEN iep.status = 'available' THEN 1 ELSE 0 END) as available_count,
    SUM(CASE WHEN iep.status = 'in_use' THEN 1 ELSE 0 END) as in_use_count,
    SUM(CASE WHEN iep.status = 'offline' THEN 1 ELSE 0 END) as offline_count,
    ROUND(SUM(CASE WHEN iep.status = 'in_use' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as usage_rate
FROM infinity_extension_pool iep
GROUP BY iep.tenant_id, iep.config_id;

-- 插入示例注释
-- 注意：实际使用时，请根据具体环境调整
-- 1. callback_number 格式示例：+52 55 1234 5678（包含国家代码）
-- 2. infinity_extension_number 格式示例：8001, 8002, 8003（纯数字分机号）
-- 3. caller_number_range_start 和 caller_number_range_end 示例：
--    起始：4001234000
--    结束：4001234999
-- 4. custom_params JSON 示例：{"userid": "123", "memberid": "456", "case_priority": "high"}

