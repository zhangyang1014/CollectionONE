# MySQL 数据库手动设置指南

## 方案1：手动在终端中执行（推荐）

### 步骤1：登录MySQL

打开终端，执行：

```bash
mysql -u root -p
```

然后输入密码：`20150501Home`

如果提示密码错误，可能是：
- MySQL root 没有设置密码（直接按回车）
- 密码是其他值

### 步骤2：创建数据库

在MySQL命令行中执行：

```sql
CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES LIKE 'cco_system';
```

### 步骤3：退出MySQL

```sql
exit;
```

### 步骤4：导入表结构

在终端中执行：

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java
mysql -u root -p cco_system < src/main/resources/db/migration/schema.sql
```

输入MySQL密码后，表结构会自动创建。

### 步骤5：验证表是否创建

```bash
mysql -u root -p -e "USE cco_system; SHOW TABLES;"
```

应该看到类似这样的输出：
```
+----------------------------------+
| Tables_in_cco_system             |
+----------------------------------+
| tenants                          |
| tenant_field_display_configs     |
| collection_agencies              |
| ...                              |
+----------------------------------+
```

### 步骤6：更新配置文件

编辑 `backend-java/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cco_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
```

---

## 方案2：如果忘记密码，重置MySQL密码

### 步骤1：停止MySQL

```bash
brew services stop mysql
```

### 步骤2：跳过密码验证启动MySQL

```bash
mysqld_safe --skip-grant-tables &
```

### 步骤3：登录MySQL（无需密码）

```bash
mysql -u root
```

### 步骤4：重置密码

```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';
exit;
```

### 步骤5：重启MySQL

```bash
pkill mysqld
brew services start mysql
```

### 步骤6：用新密码登录

```bash
mysql -u root -p
# 输入新密码
```

---

## 方案3：使用空密码

有些MySQL安装默认root没有密码：

```bash
mysql -u root
```

如果能登录，说明密码为空，直接执行：

```sql
CREATE DATABASE IF NOT EXISTS cco_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

然后导入表结构：

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java
mysql -u root cco_system < src/main/resources/db/migration/schema.sql
```

---

## 完成后告诉我

执行完毕后，请告诉我：
1. 数据库是否创建成功
2. 表结构是否导入成功
3. 最终使用的MySQL密码是什么

我会帮您：
1. 更新配置文件
2. 重启后端服务
3. 测试字段排序保存功能




































