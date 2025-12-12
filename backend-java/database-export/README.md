# CCO System 数据库导出说明

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|---------|
| 1.0.0 | 2025-12-12 | 创建数据库导出说明文档 | 大象 |

## 📊 导出内容

- **数据库名**: `cco_system`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **导出方式**: mysqldump 完整导出

## 📦 导出文件说明

### `cco_system_complete_YYYYMMDD_HHMMSS.sql`

使用 mysqldump 导出的完整数据库备份文件，包含：

- ✅ 所有表结构定义（CREATE TABLE）
- ✅ 所有表索引和约束
- ✅ 所有外键关系
- ✅ 所有触发器（Triggers）
- ✅ 所有存储过程（Stored Procedures）
- ✅ 所有函数（Functions）
- ✅ 所有事件（Events）
- ✅ 当前数据库中的所有数据（INSERT）

**适用场景**：
- 数据库完整备份
- 迁移到新环境
- 灾难恢复
- 开发测试环境搭建

## 🗂️ 核心表结构清单

导出包含以下核心表：

### 1. 基础配置表（3张）
- `tenants` - 甲方配置表
- `field_groups` - 字段分组表
- `standard_fields` - 标准字段定义表

### 2. 字段管理表（3张）
- `custom_fields` - 自定义字段定义表
- `tenant_field_configs` - 甲方字段启用配置表
- `tenant_field_display_configs` - 甲方字段展示配置表

### 3. 组织架构表（4张）
- `collection_agencies` - 催收机构表
- `team_groups` - 小组群表
- `collection_teams` - 催收小组表
- `collectors` - 催员表

### 4. 案件管理表（3张）
- `case_queues` - 案件队列表
- `cases` - 案件主表
- `case_contacts` - 案件联系人表

### 5. 通信记录表（1张）
- `communication_records` - 通信记录表

### 6. 通知管理表（3张）
- `notification_templates` - 通知模板表
- `notification_configs` - 通知配置表
- `public_notifications` - 公共通知表

### 7. 扩展表（根据实际情况）
- `case_assignments` - 案件分配表
- `tenant_field_uploads` - 甲方字段上传表
- `payment_channels` - 还款渠道表
- `case_reassign_configs` - 案件重新分配配置表
- `collector_login_whitelist` - 催员登录白名单表
- `tenant_fields_json` - 甲方字段JSON表
- `tenant_admin` - 甲方管理员表
- `team_admin_account` - 小组管理员账号表
- `agency_working_hours` - 机构工作时间表
- 其他...

## 📥 导入方法

### 方法1: 使用 mysql 命令导入（推荐）

```bash
# 导入到现有数据库
mysql -u root -p cco_system < cco_system_complete_20251212_120000.sql
```

### 方法2: 创建新数据库并导入

```bash
# 删除旧数据库并创建新数据库
mysql -u root -p -e "DROP DATABASE IF EXISTS cco_system;"
mysql -u root -p -e "CREATE DATABASE cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据
mysql -u root -p cco_system < cco_system_complete_20251212_120000.sql
```

### 方法3: 直接执行SQL文件（包含数据库创建）

```bash
# SQL文件已包含 DROP DATABASE 和 CREATE DATABASE 语句
mysql -u root -p < cco_system_complete_20251212_120000.sql
```

### 方法4: 使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到目标数据库服务器
3. 选择菜单：**File** → **Run SQL Script**
4. 选择导出的SQL文件
5. 点击 **Run** 执行

### 方法5: 使用 source 命令

```bash
# 进入 MySQL 命令行
mysql -u root -p

# 在 MySQL 提示符下执行
mysql> DROP DATABASE IF EXISTS cco_system;
mysql> CREATE DATABASE cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql> USE cco_system;
mysql> SOURCE /path/to/cco_system_complete_20251212_120000.sql;
```

## 🚀 快速使用指南

### 导出数据库

```bash
# 1. 进入后端项目目录
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java

# 2. 给脚本添加执行权限
chmod +x export-database.sh

# 3. 执行导出脚本
./export-database.sh
```

### 查看导出文件

```bash
# 查看导出目录
ls -lh database-export/

# 查看SQL文件前50行
head -50 database-export/cco_system_complete_*.sql

# 统计SQL文件信息
wc -l database-export/cco_system_complete_*.sql
```

## ⚠️ 重要注意事项

### 1. 密码安全
- ❌ **不要将导出脚本提交到Git仓库**（包含数据库密码）
- ✅ 建议将 `export-database.sh` 添加到 `.gitignore`
- ✅ 或者在脚本中使用环境变量存储密码

### 2. 数据一致性
- 导出前建议暂停应用对数据库的写操作
- 使用 `--single-transaction` 确保InnoDB表的一致性
- 如果有MyISAM表，建议锁表后导出

### 3. 文件大小
- 数据量大时，SQL文件可能很大（几百MB甚至GB）
- 确保磁盘空间充足
- 可以使用 `gzip` 压缩：
  ```bash
  gzip database-export/cco_system_complete_*.sql
  ```

### 4. 字符编码
- 确保使用 **UTF-8** 编码查看和编辑SQL文件
- 导入时使用 `--default-character-set=utf8mb4`

### 5. 权限问题
- 确保MySQL用户有足够的权限（SELECT, LOCK TABLES, SHOW VIEW等）
- 如果导出存储过程，需要 `EXECUTE` 权限

### 6. 版本兼容性
- 导出和导入的MySQL版本应该兼容
- 跨大版本迁移时需要注意兼容性问题

## 🔧 故障排除

### 问题1: 权限不足

**错误信息**:
```
mysqldump: Got error: 1044: Access denied for user 'root'@'localhost'
```

**解决方案**:
```bash
# 检查用户权限
mysql -u root -p -e "SHOW GRANTS FOR 'root'@'localhost';"

# 授予所有权限
mysql -u root -p -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';"
mysql -u root -p -e "FLUSH PRIVILEGES;"
```

### 问题2: 连接失败

**错误信息**:
```
ERROR 2002 (HY000): Can't connect to local MySQL server
```

**解决方案**:
```bash
# 检查MySQL服务状态
brew services list | grep mysql

# 启动MySQL服务
brew services start mysql
```

### 问题3: 导入时外键约束错误

**错误信息**:
```
ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails
```

**解决方案**:
```bash
# 临时禁用外键检查
mysql -u root -p -e "SET FOREIGN_KEY_CHECKS=0;"
mysql -u root -p cco_system < cco_system_complete_*.sql
mysql -u root -p -e "SET FOREIGN_KEY_CHECKS=1;"
```

### 问题4: 字符集问题

**错误信息**:
```
Incorrect string value: '\xE6\x9D\x8E\xE5\x9B\x9B' for column 'name'
```

**解决方案**:
```bash
# 确保数据库使用utf8mb4
mysql -u root -p -e "ALTER DATABASE cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入时指定字符集
mysql -u root -p --default-character-set=utf8mb4 cco_system < cco_system_complete_*.sql
```

## 📊 验证导入结果

### 检查表数量

```bash
mysql -u root -p cco_system -e "
SELECT COUNT(*) AS table_count 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'cco_system';
"
```

### 检查数据行数

```bash
mysql -u root -p cco_system -e "
SELECT 
    TABLE_NAME, 
    TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'cco_system' 
    AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_ROWS DESC;
"
```

### 检查字符集

```bash
mysql -u root -p cco_system -e "
SELECT 
    DEFAULT_CHARACTER_SET_NAME,
    DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'cco_system';
"
```

## 📚 相关文档

- [MySQL 官方文档 - mysqldump](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html)
- [数据库设置脚本](../setup-database.sh)
- [后端启动脚本](../start.sh)
- [项目README](../../README.md)

## 📞 技术支持

如有问题，请联系：
- **开发者**: 大象
- **项目**: CollectionONE
- **文档更新**: 2025-12-12
