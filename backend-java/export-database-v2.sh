#!/bin/bash
# ============================================================================
# CCO System - 数据库完整导出脚本 V2
# 版本: 2.0.0
# 日期: 2025-12-12
# 作者: 大象
# 改进: 去除警告信息，添加更详细的验证
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
echo "CCO System - 数据库完整导出 V2"
echo "=========================================="
echo ""
echo "📊 导出配置："
echo "  数据库名: $DB_NAME"
echo "  输出目录: $OUTPUT_DIR"
echo "  输出文件: $(basename $OUTPUT_FILE)"
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
TABLE_COUNT=$(mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "
    SELECT COUNT(*) 
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = '$DB_NAME' AND TABLE_TYPE = 'BASE TABLE';
" 2>/dev/null)
echo "  总表数: $TABLE_COUNT 个"

echo ""
echo "📊 各表数据行数："
mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
    SELECT 
        TABLE_NAME as '表名',
        TABLE_ROWS as '行数'
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = '$DB_NAME' 
    AND TABLE_TYPE = 'BASE TABLE'
    ORDER BY TABLE_ROWS DESC, TABLE_NAME;
" 2>/dev/null | grep -v "表名"

TOTAL_ROWS=$(mysql -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "
    SELECT SUM(TABLE_ROWS)
    FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = '$DB_NAME' AND TABLE_TYPE = 'BASE TABLE';
" 2>/dev/null)
echo ""
echo "  数据总行数: $TOTAL_ROWS 行"

echo ""
echo "🚀 开始导出..."

# 使用 mysqldump 导出完整数据库
# 添加 --set-gtid-purged=OFF 和 --column-statistics=0 以避免警告
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
  --set-gtid-purged=OFF \
  --column-statistics=0 \
  --databases "$DB_NAME" 2>&1 | \
  grep -v "Using a password on the command line" | \
  grep -v "Warning:" > "$OUTPUT_FILE"

# 检查导出结果
if [ $? -eq 0 ] && [ -f "$OUTPUT_FILE" ] && [ -s "$OUTPUT_FILE" ]; then
    FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
    LINE_COUNT=$(wc -l < "$OUTPUT_FILE")
    
    # 验证文件完整性
    echo "✅ 导出完成"
    echo ""
    echo "🔍 验证导出文件..."
    
    # 检查是否包含 DROP DATABASE
    if grep -q "DROP DATABASE IF EXISTS" "$OUTPUT_FILE"; then
        echo "  ✅ 包含 DROP DATABASE 语句"
    fi
    
    # 检查是否包含 CREATE DATABASE
    if grep -q "CREATE DATABASE" "$OUTPUT_FILE"; then
        echo "  ✅ 包含 CREATE DATABASE 语句"
    fi
    
    # 统计 CREATE TABLE 数量
    CREATE_TABLE_COUNT=$(grep -c "CREATE TABLE" "$OUTPUT_FILE")
    echo "  ✅ 包含 $CREATE_TABLE_COUNT 个 CREATE TABLE 语句"
    
    # 统计 INSERT INTO 数量
    INSERT_COUNT=$(grep -c "INSERT INTO" "$OUTPUT_FILE")
    echo "  ✅ 包含 $INSERT_COUNT 个 INSERT INTO 语句"
    
    # 检查文件是否完整结束
    if grep -q "Dump completed" "$OUTPUT_FILE"; then
        echo "  ✅ 文件完整导出（有结束标记）"
    else
        echo "  ⚠️  警告: 未找到结束标记"
    fi
    
    echo ""
    echo "=========================================="
    echo "✅ 导出成功！"
    echo "=========================================="
    echo ""
    echo "📦 导出文件信息："
    echo "  文件路径: $OUTPUT_FILE"
    echo "  文件大小: $FILE_SIZE"
    echo "  文件行数: $LINE_COUNT 行"
    echo "  表结构数: $CREATE_TABLE_COUNT 个表"
    echo "  数据记录: $INSERT_COUNT 条语句"
    echo ""
    echo "📝 快速使用："
    echo ""
    echo "  # 查看文件内容"
    echo "  head -100 $OUTPUT_FILE"
    echo ""
    echo "  # 导入到当前数据库"
    echo "  mysql -u root -p < $OUTPUT_FILE"
    echo ""
    echo "  # 导入到新环境"
    echo "  mysql -u root -p cco_system < $OUTPUT_FILE"
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
    else
        echo "导出文件未生成"
    fi
    exit 1
fi
