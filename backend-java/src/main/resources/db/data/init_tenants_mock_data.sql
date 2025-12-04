-- ============================================================================
-- Mock甲方数据初始化脚本
-- 用于开发和测试环境
-- ============================================================================

-- 插入Mock甲方数据
INSERT INTO `tenants` (
    `id`, 
    `tenant_code`, 
    `tenant_name`, 
    `tenant_name_en`, 
    `country_code`, 
    `timezone`, 
    `currency_code`, 
    `default_language`,
    `is_active`, 
    `created_at`, 
    `updated_at`
) VALUES
(1, 'BTQ', '百腾企业', 'Baiteng Enterprise', 'MX', 'America/Mexico_City', 'MXN', 'es-MX', 1, NOW(), NOW()),
(2, 'BTSK', 'BTSK机构', 'BTSK Organization', 'CN', 'Asia/Shanghai', 'CNY', 'zh-CN', 1, NOW(), NOW()),
(3, 'DEMO', '演示甲方', 'Demo Tenant', 'US', 'America/New_York', 'USD', 'en-US', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    `tenant_name` = VALUES(`tenant_name`),
    `tenant_name_en` = VALUES(`tenant_name_en`),
    `country_code` = VALUES(`country_code`),
    `timezone` = VALUES(`timezone`),
    `currency_code` = VALUES(`currency_code`),
    `default_language` = VALUES(`default_language`),
    `is_active` = VALUES(`is_active`),
    `updated_at` = NOW();

-- 提示信息
SELECT 'Mock甲方数据初始化完成' as message;

