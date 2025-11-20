#!/bin/bash

# 通知模板一键设置脚本
# 用于创建数据库表和插入Mock数据

echo "============================================================"
echo "通知模板一键设置脚本"
echo "============================================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 数据库配置
DB_NAME="cco_system"
DB_USER="root"

echo ""
echo "请输入MySQL密码："
read -s DB_PASSWORD

echo ""
echo "${YELLOW}步骤1: 检查数据库连接...${NC}"
mysql -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "${GREEN}✓ 数据库连接成功${NC}"
else
    echo "${RED}✗ 数据库连接失败，请检查密码和数据库是否存在${NC}"
    exit 1
fi

echo ""
echo "${YELLOW}步骤2: 执行SQL脚本...${NC}"
mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < mock_notification_templates.sql 2>/dev/null
if [ $? -eq 0 ]; then
    echo "${GREEN}✓ SQL脚本执行成功${NC}"
else
    echo "${RED}✗ SQL脚本执行失败${NC}"
    exit 1
fi

echo ""
echo "${YELLOW}步骤3: 验证数据...${NC}"
COUNT=$(mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -se "SELECT COUNT(*) FROM notification_templates;" 2>/dev/null)
if [ $? -eq 0 ]; then
    echo "${GREEN}✓ 数据验证成功，共有 $COUNT 条模板${NC}"
else
    echo "${RED}✗ 数据验证失败${NC}"
    exit 1
fi

echo ""
echo "${GREEN}============================================================${NC}"
echo "${GREEN}✓ 通知模板设置完成！${NC}"
echo "${GREEN}============================================================${NC}"
echo ""
echo "下一步："
echo "1. 重启后端服务（必须！）"
echo "   - 停止当前服务（Ctrl+C）"
echo "   - 运行: python3 -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"
echo ""
echo "2. 刷新浏览器查看通知模板"
echo ""

