#!/bin/bash
# ============================================================================
# CCO System - 数据库完整导出脚本
# 版本: 1.0.0
# 日期: 2025-12-12
# 作者: 大象
# ============================================================================

# 配置
DB_NAME="cco_system"
DB_USER="root"
DB_PASSWORD="20150501Home"
OUTPUT_DIR="database-export"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
OUTPUT_FILE="${OUTPUT_DIR}/cco_system_complete_${TIMESTAMP}.sql"

# 创建导出目录
mkdir -p "$OUTPUT_DIR"

echo "=========================================="
echo "CCO System - 数据库完整导出"
echo "=========================================="
echo ""
echo "📊 导出信息："
echo "  数据库名: $DB_NAME"
echo "  输出目录: $OUTPUT_DIR"
echo "  输出文件: $OUTPUT_FILE"
echo ""

# 检查MySQL是否可用
if ! command -v mysqldump &> /dev/null; then
    echo "❌ 错误: mysqldump 命令不可用"
    echo "请确保已安装MySQL客户端工具"
    exit 1
fi

# 检查数据库连接
echo "🔍 检查数据库连接..."
if ! mysql -u "$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null; then
    echo "❌ 错误: 无法连接到数据库 $DB_NAME"
    echo "请检查数据库配置和密码是否正确"
    exit 1
fi
echo "✅ 数据库连接成功"
echo ""

# 显示数据库统计信息
echo "📈 数据库统计信息："
mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
    SELECT 
        COUNT(*) as 'Total Tables'
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = '$DB_NAME';
" 2>/dev/null | tail -n +2 | while read count; do
    echo "  表数量: $count"
done

mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
    SELECT 
        TABLE_NAME as 'Table',
        TABLE_ROWS as 'Rows'
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = '$DB_NAME' 
    AND TABLE_TYPE = 'BASE TABLE'
    ORDER BY TABLE_ROWS DESC;
" 2>/dev/null

echo ""
echo "🚀 开始导出..."

# 使用 mysqldump 导出完整数据库
# --single-transaction: 保证数据一致性（InnoDB）
# --routines: 导出存储过程和函数
# --triggers: 导出触发器
# --events: 导出事件
# --add-drop-database: 添加 DROP DATABASE 语句
# --add-drop-table: 添加 DROP TABLE 语句
# --comments: 添加注释
# --complete-insert: 使用完整的 INSERT 语句（包含列名）
# --hex-blob: 以十六进制格式导出BLOB字段
# --default-character-set: 设置字符集

mysqldump -u "$DB_USER" -p"$DB_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --add-drop-database \
  --add-drop-table \
  --comments \
  --complete-insert \
  --hex-blob \
  --default-character-set=utf8mb4 \
  --databases "$DB_NAME" > "$OUTPUT_FILE" 2>&1

# 检查导出结果
if [ $? -eq 0 ] && [ -f "$OUTPUT_FILE" ]; then
    FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
    LINE_COUNT=$(wc -l < "$OUTPUT_FILE")
    
    echo ""
    echo "=========================================="
    echo "✅ 导出成功！"
    echo "=========================================="
    echo ""
    echo "📦 文件信息："
    echo "  文件路径: $OUTPUT_FILE"
    echo "  文件大小: $FILE_SIZE"
    echo "  行数: $LINE_COUNT"
    echo ""
    echo "📝 使用方法："
    echo ""
    echo "  1. 导入到新数据库:"
    echo "     mysql -u root -p < $OUTPUT_FILE"
    echo ""
    echo "  2. 导入到指定数据库:"
    echo "     mysql -u root -p cco_system < $OUTPUT_FILE"
    echo ""
    echo "  3. 创建新数据库并导入:"
    echo "     mysql -u root -p -e \"DROP DATABASE IF EXISTS cco_system;\""
    echo "     mysql -u root -p -e \"CREATE DATABASE cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""
    echo "     mysql -u root -p cco_system < $OUTPUT_FILE"
    echo ""
    echo "=========================================="
else
    echo ""
    echo "=========================================="
    echo "❌ 导出失败"
    echo "=========================================="
    echo ""
    if [ -f "$OUTPUT_FILE" ]; then
        echo "错误信息："
        tail -20 "$OUTPUT_FILE"
    fi
    exit 1
fi
