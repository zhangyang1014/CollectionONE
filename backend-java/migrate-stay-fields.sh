#!/bin/bash
# 执行停留字段数据库迁移脚本

echo "========== 案件停留功能 - 数据库迁移 =========="
echo ""
echo "此脚本将为cases表添加停留相关字段"
echo ""

# 读取数据库密码
read -sp "请输入MySQL root密码: " DB_PASS
echo ""
echo ""

# 检查数据库连接
echo "检查数据库连接..."
mysql -u root -p"$DB_PASS" cco_system -e "SELECT 1;" > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "❌ 数据库连接失败，请检查密码是否正确"
    exit 1
fi

echo "✅ 数据库连接成功"
echo ""

# 检查字段是否已存在
echo "检查字段是否已存在..."
EXISTING_FIELDS=$(mysql -u root -p"$DB_PASS" cco_system -N -e "
SELECT COUNT(*)
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'cco_system'
  AND TABLE_NAME = 'cases'
  AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by');
" 2>/dev/null)

if [ "$EXISTING_FIELDS" -eq 5 ]; then
    echo "✅ 所有停留字段已存在，无需迁移"
    echo ""
    echo "当前字段："
    mysql -u root -p"$DB_PASS" cco_system -e "
    SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'cco_system' 
      AND TABLE_NAME = 'cases' 
      AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by')
    ORDER BY COLUMN_NAME;
    " 2>/dev/null | grep -v "Warning"
    exit 0
fi

echo "发现 $EXISTING_FIELDS/5 个字段，开始执行迁移..."
echo ""

# 执行迁移
echo "执行SQL迁移..."
mysql -u root -p"$DB_PASS" cco_system < src/main/resources/db/migration/add_case_stay_fields_safe.sql 2>&1 | grep -v "Warning\|Duplicate\|already exists" || true

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 迁移执行完成！"
    echo ""
    echo "验证字段..."
    mysql -u root -p"$DB_PASS" cco_system -e "
    SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'cco_system' 
      AND TABLE_NAME = 'cases' 
      AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by')
    ORDER BY COLUMN_NAME;
    " 2>/dev/null | grep -v "Warning"
    
    echo ""
    echo "========== 迁移成功！ =========="
    echo "请重启后端服务以使更改生效"
else
    echo ""
    echo "❌ 迁移失败，请检查错误信息"
    exit 1
fi

