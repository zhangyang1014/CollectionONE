-- ============================================================================
-- 数据库迁移脚本：为甲方表添加默认语言字段
-- 版本: V20251204_001
-- 日期: 2025-12-04
-- 作者: CCO Team
-- 描述: 为tenants表添加default_language字段，用于配置甲方的默认语言
--       该语言将作为该甲方下所有催员登录IM端时的默认界面语言
-- ============================================================================

-- 添加default_language字段
ALTER TABLE tenants 
ADD COLUMN default_language VARCHAR(10) COMMENT '默认语言（Locale，如zh-CN、en-US、es-MX等）';

-- 为现有数据设置默认值
-- 根据国家代码推测合理的默认语言
UPDATE tenants SET default_language = 'zh-CN' WHERE country_code = 'CN' AND default_language IS NULL;
UPDATE tenants SET default_language = 'en-US' WHERE country_code = 'US' AND default_language IS NULL;
UPDATE tenants SET default_language = 'es-MX' WHERE country_code = 'MX' AND default_language IS NULL;
UPDATE tenants SET default_language = 'pt-BR' WHERE country_code = 'BR' AND default_language IS NULL;
UPDATE tenants SET default_language = 'id-ID' WHERE country_code = 'ID' AND default_language IS NULL;
UPDATE tenants SET default_language = 'th-TH' WHERE country_code = 'TH' AND default_language IS NULL;
UPDATE tenants SET default_language = 'vi-VN' WHERE country_code = 'VN' AND default_language IS NULL;

-- 其他国家默认使用英语
UPDATE tenants SET default_language = 'en-US' WHERE default_language IS NULL;

-- 设置NOT NULL约束（建议在生产环境执行前备份数据）
-- 注意：如果有数据质量问题，可能需要先修复数据再执行此步骤
ALTER TABLE tenants 
MODIFY COLUMN default_language VARCHAR(10) NOT NULL COMMENT '默认语言（Locale，如zh-CN、en-US、es-MX等）';

-- ============================================================================
-- 回滚脚本（如需回滚，请执行以下语句）
-- ============================================================================
-- ALTER TABLE tenants DROP COLUMN default_language;
-- ============================================================================

