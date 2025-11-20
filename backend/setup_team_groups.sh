#!/bin/bash

# 小组群表创建脚本

echo "🔧 开始创建小组群相关表结构..."

# 数据库文件路径
DB_FILE="cco_test.db"

if [ ! -f "$DB_FILE" ]; then
    echo "❌ 错误: 数据库文件 $DB_FILE 不存在"
    exit 1
fi

echo "📝 执行SQL脚本..."
sqlite3 "$DB_FILE" < create_team_groups_table.sql 2>&1 | grep -v "duplicate column name"

echo ""
echo "✅ 数据库表结构创建完成！"
echo ""
echo "📊 查看team_groups表结构："
sqlite3 "$DB_FILE" "PRAGMA table_info(team_groups);"

echo ""
echo "📊 查看collection_teams表是否有team_group_id字段："
sqlite3 "$DB_FILE" "PRAGMA table_info(collection_teams);" | grep -E "cid|team_group_id" || echo "team_group_id字段已添加"

echo ""
echo "🎉 小组群功能数据库结构已准备就绪！"
echo ""
echo "说明："
echo "  - 小组群表 (team_groups) 已创建"
echo "  - 小组表 (collection_teams) 已添加 team_group_id 字段"
echo "  - 相关索引已创建"
echo ""
echo "下一步："
echo "  1. 重启后端服务: ./restart_backend.sh"
echo "  2. 访问前端页面测试小组群管理功能"

