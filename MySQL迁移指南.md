# CCO System - MySQL 迁移指南

## 📋 迁移概述

本指南将帮助您将 CCO System 从 SQLite 迁移到 MySQL 数据库。

### 为什么要迁移到 MySQL?

- ✅ 更好的并发性能
- ✅ 更强的数据完整性
- ✅ 支持更大的数据量
- ✅ 更好的生产环境支持
- ✅ 更丰富的管理工具

## 🔍 前置检查

### 1. 确认 MySQL 已安装并运行

```bash
# 检查 MySQL 版本
mysql --version

# 检查 MySQL 服务状态
brew services list | grep mysql

# 如果未运行,启动 MySQL
brew services start mysql
```

**您的 MySQL 版本**: `mysql Ver 9.5.0 for macos26.1 on arm64 (Homebrew)` ✅

## 🚀 迁移步骤

### 步骤 1: 安装 MySQL Python 驱动

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
source venv/bin/activate
pip install pymysql cryptography
```

### 步骤 2: 创建 MySQL 数据库和用户

**方法 A: 使用 SQL 脚本 (推荐)**

```bash
# 如果 root 用户有密码
mysql -u root -p < setup_mysql_simple.sql

# 如果 root 用户没有密码
mysql -u root < setup_mysql_simple.sql
```

**方法 B: 手动创建**

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行:
CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'cco_user'@'localhost' IDENTIFIED BY 'cco_password';
GRANT ALL PRIVILEGES ON cco_system.* TO 'cco_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 步骤 3: 创建 .env 配置文件

在 `backend/` 目录下创建 `.env` 文件:

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
cat > .env << 'EOF'
# 数据库配置 - MySQL
DATABASE_URL=mysql+pymysql://cco_user:cco_password@localhost:3306/cco_system?charset=utf8mb4

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
```

### 步骤 4: 运行数据迁移

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
source venv/bin/activate
python3 migrate_to_mysql.py
```

迁移脚本会:
1. 检查 SQLite 数据库是否存在
2. 在 MySQL 中创建所有表结构
3. 迁移所有数据 (如果 SQLite 数据库存在)
4. 验证迁移结果

### 步骤 5: 重启后端服务

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
bash restart_backend.sh
```

### 步骤 6: 验证功能

1. 访问前端: http://localhost:5173
2. 登录系统
3. 检查各个功能模块:
   - ✅ 用户登录
   - ✅ 案件列表
   - ✅ 通知配置
   - ✅ 通知模板 (10条测试数据)
   - ✅ 其他功能

## 📊 数据库信息

### 连接信息

- **数据库名**: `cco_system`
- **用户名**: `cco_user`
- **密码**: `cco_password`
- **主机**: `localhost`
- **端口**: `3306`
- **字符集**: `utf8mb4`

### 连接字符串

```
mysql+pymysql://cco_user:cco_password@localhost:3306/cco_system?charset=utf8mb4
```

### 直接连接 MySQL

```bash
mysql -u cco_user -p cco_system
# 密码: cco_password
```

## 🔧 常用 MySQL 命令

### 查看数据库和表

```sql
-- 显示所有数据库
SHOW DATABASES;

-- 使用 cco_system 数据库
USE cco_system;

-- 显示所有表
SHOW TABLES;

-- 查看表结构
DESCRIBE notification_templates;

-- 查看表记录数
SELECT COUNT(*) FROM notification_templates;
```

### 查看通知模板数据

```sql
USE cco_system;
SELECT id, template_name, template_type, is_enabled 
FROM notification_templates 
ORDER BY id;
```

### 备份和恢复

```bash
# 备份数据库
mysqldump -u cco_user -p cco_system > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u cco_user -p cco_system < backup_20231119.sql
```

## 🐛 故障排查

### 问题 1: 连接被拒绝

**错误**: `ERROR 1045 (28000): Access denied for user 'root'@'localhost'`

**解决**:
```bash
# 重置 MySQL root 密码
mysql.server stop
mysqld_safe --skip-grant-tables &
mysql -u root
# 在 MySQL 中:
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
EXIT;
# 重启 MySQL
mysql.server restart
```

### 问题 2: 数据库不存在

**错误**: `Unknown database 'cco_system'`

**解决**:
```bash
mysql -u root -p < setup_mysql_simple.sql
```

### 问题 3: 用户权限不足

**错误**: `Access denied for user 'cco_user'@'localhost'`

**解决**:
```sql
mysql -u root -p
GRANT ALL PRIVILEGES ON cco_system.* TO 'cco_user'@'localhost';
FLUSH PRIVILEGES;
```

### 问题 4: 字符集问题

**错误**: 中文显示乱码

**解决**:
```sql
ALTER DATABASE cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 问题 5: 表不存在

**错误**: `Table 'cco_system.xxx' doesn't exist`

**解决**:
```bash
# 重新运行迁移脚本
python3 migrate_to_mysql.py
```

## 📝 迁移检查清单

### 迁移前

- [ ] MySQL 服务正在运行
- [ ] 已安装 pymysql 驱动
- [ ] 已备份 SQLite 数据库 (如果有重要数据)
- [ ] 已记录当前系统状态

### 迁移中

- [ ] 成功创建 MySQL 数据库
- [ ] 成功创建用户和授权
- [ ] 成功创建 .env 文件
- [ ] 成功运行迁移脚本
- [ ] 验证表结构正确
- [ ] 验证数据完整性

### 迁移后

- [ ] 后端服务正常启动
- [ ] 前端可以正常访问
- [ ] 用户登录功能正常
- [ ] 案件管理功能正常
- [ ] 通知配置功能正常
- [ ] 通知模板显示10条数据
- [ ] 所有 CRUD 操作正常
- [ ] 无错误日志

## 🔄 回滚到 SQLite (如果需要)

如果迁移出现问题,可以快速回滚:

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend

# 1. 修改 .env 文件
cat > .env << 'EOF'
DATABASE_URL=sqlite:///./cco_test.db
EOF

# 2. 重启后端服务
bash restart_backend.sh
```

## 📈 性能优化建议

### 1. 添加索引

```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_template_type ON notification_templates(template_type);
CREATE INDEX idx_template_enabled ON notification_templates(is_enabled);
CREATE INDEX idx_case_number ON cases(case_number);
```

### 2. 配置 MySQL

编辑 MySQL 配置文件 (通常在 `/opt/homebrew/etc/my.cnf`):

```ini
[mysqld]
# 字符集
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

# 性能优化
max_connections=200
innodb_buffer_pool_size=256M
innodb_log_file_size=64M
```

### 3. 定期维护

```sql
-- 优化表
OPTIMIZE TABLE notification_templates;

-- 分析表
ANALYZE TABLE notification_templates;

-- 检查表
CHECK TABLE notification_templates;
```

## ✅ 验证成功标志

迁移成功后,您应该看到:

1. ✅ 后端日志显示连接到 MySQL
2. ✅ 前端页面正常显示
3. ✅ 通知模板页面显示10条数据
4. ✅ 所有功能正常工作
5. ✅ 无数据库连接错误
6. ✅ 无数据类型错误

## 🎉 完成!

恭喜!您已成功将 CCO System 迁移到 MySQL 数据库。

如有任何问题,请参考故障排查部分或查看日志文件。

