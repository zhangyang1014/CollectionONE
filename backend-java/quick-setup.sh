#!/bin/bash
# 快速数据库设置脚本

echo "=========================================="
echo "尝试连接MySQL..."
echo "=========================================="

cd "$(dirname "$0")"

# 尝试1：空密码
echo "尝试1：空密码..."
if mysql -u root -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ 成功！MySQL root 无密码"
    PASSWORD=""
    
# 尝试2：密码为 root
elif mysql -u root -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ 成功！MySQL root 密码是 'root'"
    PASSWORD="root"
    
else
    echo ""
    echo "❌ 自动连接失败"
    echo ""
    echo "请手动执行以下命令："
    echo ""
    echo "1. 登录MySQL："
    echo "   mysql -u root -p"
    echo ""
    echo "2. 创建数据库："
    echo "   CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo ""
    echo "3. 退出MySQL："
    echo "   exit;"
    echo ""
    echo "4. 导入表结构："
    echo "   cd $PWD"
    echo "   mysql -u root -p cco_system < src/main/resources/db/migration/schema.sql"
    echo ""
    exit 1
fi

# 创建数据库
echo ""
echo "创建数据库..."
if [ -z "$PASSWORD" ]; then
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
else
    mysql -u root -p"$PASSWORD" -e "CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi

if [ $? -eq 0 ]; then
    echo "✅ 数据库创建成功"
else
    echo "❌ 数据库创建失败"
    exit 1
fi

# 导入表结构
echo ""
echo "导入表结构..."
if [ -z "$PASSWORD" ]; then
    mysql -u root cco_system < src/main/resources/db/migration/schema.sql
else
    mysql -u root -p"$PASSWORD" cco_system < src/main/resources/db/migration/schema.sql
fi

if [ $? -eq 0 ]; then
    echo "✅ 表结构导入成功"
else
    echo "❌ 表结构导入失败"
    exit 1
fi

# 更新配置
echo ""
echo "更新配置文件..."
cat > src/main/resources/application-dev.yml << EOF
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cco_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: $PASSWORD

logging:
  level:
    root: INFO
    com.cco: DEBUG
    com.baomidou.mybatisplus: DEBUG
EOF

echo "✅ 配置文件已更新"

echo ""
echo "=========================================="
echo "✅ 数据库设置完成！"
echo "=========================================="
echo ""
echo "MySQL密码：$PASSWORD"
echo ""





































