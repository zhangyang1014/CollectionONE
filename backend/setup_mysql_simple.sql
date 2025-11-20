-- CCO System MySQL 数据库设置脚本
-- 使用方法: mysql -u root -p < setup_mysql_simple.sql

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户 (如果不存在)
CREATE USER IF NOT EXISTS 'cco_user'@'localhost' IDENTIFIED BY 'cco_password';

-- 授予权限
GRANT ALL PRIVILEGES ON cco_system.* TO 'cco_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 切换到数据库
USE cco_system;

-- 显示结果
SELECT 'Database created successfully!' AS Status;
SHOW DATABASES LIKE 'cco_system';
SELECT User, Host FROM mysql.user WHERE User = 'cco_user';

