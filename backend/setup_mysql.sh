#!/bin/bash

# MySQL 数据库设置脚本
# 用于创建数据库、用户和导入数据

set -e

echo "============================================================"
echo "CCO System - MySQL 数据库设置"
echo "============================================================"

# 配置变量
DB_NAME="cco_system"
DB_USER="cco_user"
DB_PASSWORD="cco_password"
DB_HOST="localhost"
DB_PORT="3306"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}步骤1: 检查 MySQL 服务状态...${NC}"
if brew services list | grep -q "mysql.*started"; then
    echo -e "${GREEN}✓ MySQL 服务正在运行${NC}"
else
    echo -e "${RED}✗ MySQL 服务未运行${NC}"
    echo "正在启动 MySQL 服务..."
    brew services start mysql
    sleep 3
fi

echo ""
echo -e "${YELLOW}步骤2: 创建数据库和用户...${NC}"
echo "请输入 MySQL root 密码 (如果没有设置密码,直接按回车):"

# 创建数据库和用户的 SQL
mysql -u root -p << EOF
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';

-- 授权
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;

-- 显示数据库
SHOW DATABASES LIKE '${DB_NAME}';

-- 显示用户
SELECT User, Host FROM mysql.user WHERE User = '${DB_USER}';
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库和用户创建成功${NC}"
else
    echo -e "${RED}✗ 数据库和用户创建失败${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}步骤3: 创建 .env 配置文件...${NC}"
cat > .env << EOF
# 数据库配置
DATABASE_URL=mysql+pymysql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}?charset=utf8mb4

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0

# JWT 配置
SECRET_KEY=dev-secret-key-please-change-in-production
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# API 配置
API_V1_STR=/api/v1
PROJECT_NAME=CCO System

# CORS 配置
BACKEND_CORS_ORIGINS=["http://localhost:5173", "http://localhost:3000"]
EOF

echo -e "${GREEN}✓ .env 文件创建成功${NC}"

echo ""
echo -e "${YELLOW}步骤4: 安装 Python 依赖...${NC}"
source venv/bin/activate
pip install pymysql cryptography -q
echo -e "${GREEN}✓ MySQL 驱动安装成功${NC}"

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}✓ MySQL 设置完成!${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo "数据库信息:"
echo "  - 数据库名: ${DB_NAME}"
echo "  - 用户名: ${DB_USER}"
echo "  - 密码: ${DB_PASSWORD}"
echo "  - 主机: ${DB_HOST}"
echo "  - 端口: ${DB_PORT}"
echo ""
echo "连接字符串:"
echo "  DATABASE_URL=mysql+pymysql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}?charset=utf8mb4"
echo ""
echo "下一步:"
echo "  1. 运行数据迁移: python migrate_to_mysql.py"
echo "  2. 重启后端服务: bash restart_backend.sh"
echo ""

