#!/bin/bash

# 为小组群表添加queue_id字段

echo "🔧 开始更新小组群表结构，添加queue_id字段..."

# 数据库文件路径
DB_FILE="cco_test.db"

if [ ! -f "$DB_FILE" ]; then
    echo "❌ 错误: 数据库文件 $DB_FILE 不存在"
    exit 1
fi

echo "📝 执行SQL脚本..."
sqlite3 "$DB_FILE" < update_team_groups_add_queue.sql 2>&1 | grep -v "duplicate column name"

echo ""
echo "✅ 数据库表结构更新完成！"
echo ""
echo "📊 查看team_groups表结构："
sqlite3 "$DB_FILE" "PRAGMA table_info(team_groups);"

echo ""
echo "🎉 小组群表已添加queue_id字段！"
echo ""
echo "说明："
echo "  - 小组群表 (team_groups) 已添加 queue_id 字段"
echo "  - queue_id 可以关联到催收队列 (case_queues)"
echo "  - 相关索引已创建"
echo ""
echo "下一步："
echo "  1. 重启后端服务: ./restart_backend.sh"
echo "  2. 在小组群管理页面中可以选择关联的催收队列"

