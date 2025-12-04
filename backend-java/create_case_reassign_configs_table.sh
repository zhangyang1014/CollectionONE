#!/bin/bash

# 创建案件重新分案配置表
# 使用方法: ./create_case_reassign_configs_table.sh

DB_NAME="cco_system"
DB_USER="root"
DB_PASSWORD="root"

echo "正在创建 case_reassign_configs 表..."

mysql -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME} <<EOF
-- 案件重新分案配置表
CREATE TABLE IF NOT EXISTS \`case_reassign_configs\` (
  \`id\` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  \`tenant_id\` BIGINT NOT NULL COMMENT '所属甲方ID',
  \`config_type\` VARCHAR(20) NOT NULL COMMENT '配置类型: queue/agency/team',
  \`target_id\` BIGINT NOT NULL COMMENT '目标ID（队列ID/机构ID/小组ID）',
  \`reassign_days\` INT NOT NULL COMMENT '重新分案天数（整数）',
  \`is_active\` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  \`effective_date\` DATE COMMENT '生效日期（T+1日）',
  \`created_at\` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  \`updated_at\` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_tenant_type_target\` (\`tenant_id\`, \`config_type\`, \`target_id\`),
  KEY \`idx_tenant_id\` (\`tenant_id\`),
  KEY \`idx_effective_date\` (\`effective_date\`),
  KEY \`idx_config_type\` (\`config_type\`),
  KEY \`idx_is_active\` (\`is_active\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件重新分案配置表';
EOF

if [ $? -eq 0 ]; then
    echo "✅ 数据库表创建成功！"
else
    echo "❌ 数据库表创建失败，请检查数据库连接和权限"
    exit 1
fi


