#!/bin/bash

# 创建催员登录白名单IP配置表的脚本
# 使用方法: ./create_collector_login_whitelist_table.sh

# 数据库配置（根据实际情况修改）
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="cco_system"
DB_USER="root"
DB_PASS="root"

echo "========== 开始创建催员登录白名单IP配置表 =========="

# 执行SQL脚本
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} ${DB_NAME} < src/main/resources/db/migration/create_collector_login_whitelist_table.sql

if [ $? -eq 0 ]; then
    echo "========== 表创建成功 =========="
    echo "表名: collector_login_whitelist"
    echo "功能: 催员登录白名单IP管理"
else
    echo "========== 表创建失败 =========="
    exit 1
fi



