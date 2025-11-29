#!/bin/bash
# 快速修复停留字段脚本

echo "========== 检查数据库连接 =========="
read -p "请输入MySQL用户名 (默认: root): " DB_USER
DB_USER=${DB_USER:-root}

read -sp "请输入MySQL密码: " DB_PASS
echo ""

read -p "请输入数据库名 (默认: cco_system): " DB_NAME
DB_NAME=${DB_NAME:-cco_system}

echo ""
echo "========== 检查字段是否存在 =========="
mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = '$DB_NAME'
  AND TABLE_NAME = 'cases'
  AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by');
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ 数据库连接失败，请检查用户名和密码"
    exit 1
fi

FIELD_COUNT=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "
SELECT COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = '$DB_NAME'
  AND TABLE_NAME = 'cases'
  AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by');
" 2>/dev/null)

if [ "$FIELD_COUNT" -eq 5 ]; then
    echo "✅ 所有停留字段已存在，无需迁移"
    exit 0
fi

echo ""
echo "========== 字段不存在，开始执行迁移 =========="
echo "执行SQL迁移脚本..."

mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < src/main/resources/db/migration/add_case_stay_fields.sql 2>&1

if [ $? -eq 0 ]; then
    echo "✅ 迁移成功！"
    echo ""
    echo "========== 验证字段 =========="
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
    SELECT 
        COLUMN_NAME,
        COLUMN_TYPE,
        COLUMN_COMMENT
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = '$DB_NAME'
      AND TABLE_NAME = 'cases'
      AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by')
    ORDER BY COLUMN_NAME;
    "
    echo ""
    echo "✅ 请重启后端服务以使更改生效"
else
    echo "❌ 迁移失败，请检查错误信息"
    exit 1
fi

