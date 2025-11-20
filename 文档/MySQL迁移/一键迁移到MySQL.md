# 一键迁移到 MySQL

## 🚀 快速开始

按照以下步骤,5分钟内完成 MySQL 迁移:

### 步骤 1: 运行 MySQL 连接测试和设置

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend
source venv/bin/activate
python3 test_mysql_connection.py
```

**按照提示输入:**
- 主机: 直接按回车 (使用默认 localhost)
- 端口: 直接按回车 (使用默认 3306)
- 用户名: 直接按回车 (使用默认 root)
- 密码: 输入您的 MySQL root 密码 (如果没有设置密码,直接按回车)

**这个脚本会自动:**
1. ✅ 测试 MySQL 连接
2. ✅ 创建数据库 `cco_system`
3. ✅ 创建用户 `cco_user` (密码: `cco_password`)
4. ✅ 授予权限
5. ✅ 创建 `.env` 配置文件

### 步骤 2: 运行数据迁移

```bash
python3 migrate_to_mysql.py
```

**按照提示:**
- 输入 `yes` 确认迁移

**这个脚本会自动:**
1. ✅ 在 MySQL 中创建所有表结构
2. ✅ 迁移 SQLite 数据 (如果存在)
3. ✅ 验证迁移结果
4. ✅ 显示统计信息

### 步骤 3: 重启后端服务

```bash
bash restart_backend.sh
```

### 步骤 4: 验证功能

1. 访问前端: http://localhost:5173
2. 登录系统
3. 检查通知模板页面是否显示10条数据

## ✅ 完成!

如果一切正常,您已成功迁移到 MySQL!

## 🔍 验证 MySQL 数据

```bash
# 连接到 MySQL
mysql -u cco_user -p cco_system
# 密码: cco_password

# 在 MySQL 命令行中:
SHOW TABLES;
SELECT COUNT(*) FROM notification_templates;
SELECT id, template_name FROM notification_templates;
```

## 🐛 如果遇到问题

### 问题 1: MySQL root 密码不知道

**解决方法 A: 重置密码**
```bash
# 停止 MySQL
brew services stop mysql

# 安全模式启动
mysqld_safe --skip-grant-tables &

# 连接并重置密码
mysql -u root
```

在 MySQL 中执行:
```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
EXIT;
```

重启 MySQL:
```bash
pkill mysqld
brew services start mysql
```

**解决方法 B: 使用空密码**

如果您的 MySQL 是新安装的,可能没有设置密码,直接按回车即可。

### 问题 2: 连接被拒绝

```bash
# 检查 MySQL 是否运行
brew services list | grep mysql

# 如果未运行,启动它
brew services start mysql

# 等待几秒后重试
sleep 3
python3 test_mysql_connection.py
```

### 问题 3: 迁移失败

```bash
# 删除 MySQL 数据库重新开始
mysql -u root -p -e "DROP DATABASE IF EXISTS cco_system;"

# 重新运行步骤 1
python3 test_mysql_connection.py
```

## 📊 迁移前后对比

| 项目 | SQLite | MySQL |
|------|--------|-------|
| 数据库文件 | `cco_test.db` | MySQL 服务器 |
| 连接方式 | 文件路径 | 网络连接 |
| 并发性能 | 低 | 高 |
| 数据完整性 | 一般 | 强 |
| 管理工具 | 少 | 丰富 |
| 生产环境 | 不推荐 | 推荐 |

## 🎉 恭喜!

您已成功将 CCO System 升级到 MySQL 数据库!

系统现在具备:
- ✅ 更好的性能
- ✅ 更强的可靠性
- ✅ 更好的扩展性
- ✅ 生产环境就绪

