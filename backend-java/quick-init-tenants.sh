#!/bin/bash

# ============================================================================
# 快速初始化甲方Mock数据脚本
# ============================================================================

echo "========================================="
echo "开始初始化甲方Mock数据..."
echo "========================================="

# 数据库配置（请根据实际情况修改）
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="cco_dev"
DB_USER="root"
DB_PASSWORD=""

# 1. 检查并添加 default_language 字段
echo "1. 检查并添加 default_language 字段..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} ${DB_PASSWORD:+-p${DB_PASSWORD}} ${DB_NAME} <<EOF
-- 添加default_language字段（如果不存在）
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS default_language VARCHAR(10) COMMENT '默认语言（Locale）';

-- 查看表结构
DESC tenants;
EOF

echo ""
echo "2. 插入Mock甲方数据..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} ${DB_PASSWORD:+-p${DB_PASSWORD}} ${DB_NAME} <<EOF
-- 插入Mock甲方数据
INSERT INTO tenants (
    id, 
    tenant_code, 
    tenant_name, 
    tenant_name_en, 
    country_code, 
    timezone, 
    currency_code, 
    default_language,
    is_active, 
    created_at, 
    updated_at
) VALUES
(1, 'BTQ', '百腾企业', 'Baiteng Enterprise', 'MX', 'America/Mexico_City', 'MXN', 'es-MX', 1, NOW(), NOW()),
(2, 'BTSK', 'BTSK机构', 'BTSK Organization', 'CN', 'Asia/Shanghai', 'CNY', 'zh-CN', 1, NOW(), NOW()),
(3, 'DEMO', '演示甲方', 'Demo Tenant', 'US', 'America/New_York', 'USD', 'en-US', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    tenant_name = VALUES(tenant_name),
    tenant_name_en = VALUES(tenant_name_en),
    country_code = VALUES(country_code),
    timezone = VALUES(timezone),
    currency_code = VALUES(currency_code),
    default_language = VALUES(default_language),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- 查看结果
SELECT * FROM tenants;
EOF

echo ""
echo "========================================="
echo "初始化完成！"
echo "========================================="
echo ""
echo "请刷新浏览器查看甲方列表"

