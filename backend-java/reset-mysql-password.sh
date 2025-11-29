#!/bin/bash
echo "=========================================="
echo "重置MySQL root密码"
echo "=========================================="

# 停止MySQL服务
echo "1. 停止MySQL服务..."
brew services stop mysql
sleep 3

# 确保所有MySQL进程已停止
pkill mysqld
sleep 2

# 启动MySQL（跳过权限验证）
echo "2. 启动MySQL（跳过权限验证）..."
mysqld_safe --skip-grant-tables --skip-networking &
MYSQL_PID=$!
echo "MySQL进程ID: $MYSQL_PID"
sleep 5

# 重置密码
echo "3. 重置root密码..."
mysql -u root << SQL
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '20150501Home';
FLUSH PRIVILEGES;
SQL

# 停止临时MySQL
echo "4. 停止临时MySQL进程..."
kill $MYSQL_PID 2>/dev/null
pkill mysqld
sleep 3

# 重新启动MySQL服务
echo "5. 重新启动MySQL服务..."
brew services start mysql
sleep 5

echo ""
echo "=========================================="
echo "✅ 密码重置完成！"
echo "=========================================="
echo "新密码：20150501Home"
echo ""
echo "现在可以测试登录："
echo "  mysql -u root -p"
echo "  输入密码：20150501Home"
echo ""
