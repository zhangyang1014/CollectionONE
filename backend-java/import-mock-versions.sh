#!/bin/bash

# ================================================================
# 导入版本管理Mock数据脚本
# ================================================================

echo "======================================"
echo "导入版本管理Mock数据"
echo "======================================"
echo ""

# 数据库配置
DB_NAME="cco_db"
DB_USER="root"
SQL_FILE="src/main/resources/sql/init_tenant_field_upload_mock_versions.sql"

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "❌ 错误：找不到SQL文件: $SQL_FILE"
    exit 1
fi

echo "📁 SQL文件: $SQL_FILE"
echo "🗄️  数据库: $DB_NAME"
echo "👤 用户: $DB_USER"
echo ""
echo "请输入MySQL密码："

# 执行SQL文件
mysql -u $DB_USER -p $DB_NAME < $SQL_FILE

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "✅ Mock数据导入成功！"
    echo "======================================"
    echo ""
    echo "正在验证数据..."
    echo ""
    
    # 验证数据
    mysql -u $DB_USER -p $DB_NAME -e "
    SELECT 
      version AS '版本号',
      fields_count AS '字段数',
      uploaded_by_name AS '上传人',
      DATE_FORMAT(uploaded_at, '%Y-%m-%d %H:%i') AS '上传时间',
      CASE WHEN is_active THEN '✅ 当前' ELSE '⚪ 历史' END AS '状态'
    FROM tenant_field_uploads
    WHERE tenant_id = '1' AND scene = 'list'
    ORDER BY version DESC;
    "
    
    echo ""
    echo "======================================"
    echo "🎉 完成！现在可以刷新前端页面查看版本管理数据了"
    echo "======================================"
else
    echo ""
    echo "❌ 导入失败，请检查错误信息"
    exit 1
fi
