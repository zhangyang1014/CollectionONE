#!/bin/bash
# MySQL 数据库设置脚本

echo "=========================================="
echo "CCO系统 - MySQL数据库设置"
echo "=========================================="
echo ""

# 检查MySQL是否运行
if ! pgrep -x "mysqld" > /dev/null; then
    echo "❌ MySQL服务未运行"
    echo "请先启动MySQL服务："
    echo "  brew services start mysql"
    exit 1
fi

echo "✅ MySQL服务正在运行"
echo ""

# 提示用户输入密码
echo "请选择操作："
echo "1) 我知道MySQL root密码，直接输入"
echo "2) 我不知道密码，需要重置"
echo ""
read -p "请选择 (1 或 2): " choice

if [ "$choice" = "1" ]; then
    # 用户知道密码
    echo ""
    read -sp "请输入MySQL root密码: " MYSQL_PASSWORD
    echo ""
    
    # 测试密码
    if mysql -u root -p"$MYSQL_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ 密码正确"
    else
        echo "❌ 密码错误，请重新运行脚本"
        exit 1
    fi
    
elif [ "$choice" = "2" ]; then
    # 重置密码
    echo ""
    echo "重置MySQL root密码..."
    echo ""
    
    # 停止MySQL服务
    brew services stop mysql
    sleep 2
    
    # 启动MySQL（跳过密码验证）
    echo "启动MySQL（跳过密码验证）..."
    mysqld_safe --skip-grant-tables &
    MYSQL_PID=$!
    sleep 5
    
    # 重置密码为空
    mysql -u root -e "FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY '';" 2>/dev/null
    
    # 停止临时MySQL
    kill $MYSQL_PID 2>/dev/null
    pkill mysqld
    sleep 2
    
    # 重新启动MySQL服务
    brew services start mysql
    sleep 3
    
    MYSQL_PASSWORD=""
    echo "✅ 密码已重置为空"
    
else
    echo "无效选择"
    exit 1
fi

echo ""
echo "=========================================="
echo "创建数据库和表"
echo "=========================================="
echo ""

# 创建数据库
echo "1. 创建数据库 cco_system..."
if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
else
    mysql -u root -p"$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

if [ $? -eq 0 ]; then
    echo "✅ 数据库创建成功"
else
    echo "❌ 数据库创建失败"
    exit 1
fi

# 初始化表结构
echo ""
echo "2. 初始化数据库表结构..."
cd "$(dirname "$0")"

if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u root cco_system < src/main/resources/db/migration/schema.sql
else
    mysql -u root -p"$MYSQL_PASSWORD" cco_system < src/main/resources/db/migration/schema.sql
fi

if [ $? -eq 0 ]; then
    echo "✅ 数据库表创建成功"
else
    echo "❌ 数据库表创建失败"
    exit 1
fi

# 更新application-dev.yml配置
echo ""
echo "3. 更新数据库配置..."

if [ -z "$MYSQL_PASSWORD" ]; then
    # 空密码
    cat > src/main/resources/application-dev.yml << EOF
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cco_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 

logging:
  level:
    root: INFO
    com.cco: DEBUG
    com.baomidou.mybatisplus: DEBUG
EOF
else
    # 有密码
    cat > src/main/resources/application-dev.yml << EOF
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cco_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: $MYSQL_PASSWORD

logging:
  level:
    root: INFO
    com.cco: DEBUG
    com.baomidou.mybatisplus: DEBUG
EOF
fi

echo "✅ 配置文件已更新"

echo ""
echo "=========================================="
echo "✅ 数据库设置完成！"
echo "=========================================="
echo ""
echo "接下来的步骤："
echo "1. 重启后端服务"
echo "2. 测试字段排序保存功能"
echo ""





































