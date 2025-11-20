#!/bin/bash

# 更新小组群和小组的表结构

echo "🔧 开始更新小组群和小组表结构..."

# 数据库文件路径
DB_FILE="cco_test.db"

if [ ! -f "$DB_FILE" ]; then
    echo "❌ 错误: 数据库文件 $DB_FILE 不存在"
    exit 1
fi

echo "📝 执行SQL脚本..."
sqlite3 "$DB_FILE" < update_team_group_structure.sql 2>&1 | grep -v "duplicate column name"

echo ""
echo "✅ 数据库表结构更新完成！"
echo ""
echo "📊 查看更新后的表结构："
echo ""
echo "1. team_groups表："
sqlite3 "$DB_FILE" "PRAGMA table_info(team_groups);"
echo ""
echo "2. team_admin_accounts表："
sqlite3 "$DB_FILE" "PRAGMA table_info(team_admin_accounts);"
echo ""
echo "3. collection_teams表："
sqlite3 "$DB_FILE" "PRAGMA table_info(collection_teams);"

echo ""
echo "🎉 表结构更新完成！"
echo ""
echo "更新内容："
echo "  1. ✓ team_admin_accounts 表添加 team_group_id 字段（支持SPV账号）"
echo "  2. ✓ collection_teams 表添加 queue_id 字段（必选）"
echo "  3. ✓ 相关索引已创建"
echo ""
echo "下一步："
echo "  1. 重启后端服务: ./restart_backend.sh"
echo "  2. 刷新前端页面，测试新功能"
echo ""
echo "说明："
echo "  - 小组群的SPV现在通过创建管理员账号实现"
echo "  - 小组必须关联一个催收队列"
echo "  - 小组群不再直接关联队列"

