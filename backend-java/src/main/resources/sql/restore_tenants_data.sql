-- 恢复甲方数据
-- 创建日期：2025-11-29
-- 说明：恢复甲方基础数据

-- 插入甲方数据（如果不存在）
INSERT INTO `tenants` (`id`, `tenant_code`, `tenant_name`, `tenant_name_en`, `country_code`, `timezone`, `currency_code`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'BTQ', '百腾企业', 'Baiteng Enterprise', 'CN', 8, 'CNY', 1, NOW(), NOW()),
(2, 'BTSK', '百腾数科', 'Baiteng Digital', 'CN', 8, 'CNY', 1, NOW(), NOW()),
(3, 'XYQY', '星耀企业', 'Xingyao Enterprise', 'CN', 8, 'CNY', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    `tenant_name` = VALUES(`tenant_name`),
    `tenant_name_en` = VALUES(`tenant_name_en`),
    `country_code` = VALUES(`country_code`),
    `timezone` = VALUES(`timezone`),
    `currency_code` = VALUES(`currency_code`),
    `is_active` = VALUES(`is_active`),
    `updated_at` = NOW();


























