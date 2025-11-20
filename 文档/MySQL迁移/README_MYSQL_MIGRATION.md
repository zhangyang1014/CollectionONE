# MySQL 迁移 - 快速参考

## 🚀 三步完成迁移

```bash
# 1️⃣ 创建 MySQL 数据库和用户
python3 test_mysql_connection.py

# 2️⃣ 迁移数据
python3 migrate_to_mysql.py

# 3️⃣ 重启服务
bash restart_backend.sh
```

## 📝 详细说明

### 步骤 1: 创建数据库
运行后按提示输入:
- 主机: 回车 (localhost)
- 端口: 回车 (3306)
- 用户: 回车 (root)
- **密码**: 输入 MySQL root 密码

自动完成:
- ✅ 创建数据库 `cco_system`
- ✅ 创建用户 `cco_user`
- ✅ 授予权限
- ✅ 创建 `.env` 文件

### 步骤 2: 迁移数据
输入 `yes` 确认

自动完成:
- ✅ 创建所有表
- ✅ 迁移数据
- ✅ 验证结果

### 步骤 3: 重启服务
后端使用 MySQL 启动

## 🔍 验证

```bash
# 连接 MySQL
mysql -u cco_user -p cco_system
# 密码: cco_password

# 查看表
SHOW TABLES;

# 查看通知模板
SELECT COUNT(*) FROM notification_templates;
```

## 📊 数据库信息

- **数据库**: cco_system
- **用户**: cco_user
- **密码**: cco_password
- **主机**: localhost:3306

## 🔄 回滚

```bash
echo "DATABASE_URL=sqlite:///./cco_test.db" > .env
bash restart_backend.sh
```

## 📚 完整文档

- `MySQL迁移指南.md` - 详细指南
- `一键迁移到MySQL.md` - 快速步骤
- `MySQL迁移-总结报告.md` - 完整报告

## ⚠️ 注意事项

1. 需要知道 MySQL root 密码
2. 确保 MySQL 服务运行中
3. 迁移前建议备份数据
4. 生产环境请修改默认密码

## ✅ 成功标志

- 后端日志: `Connected to MySQL`
- 前端通知模板: 显示 10 条数据
- 所有功能: 正常工作

---

**预计耗时**: 5-10 分钟  
**难度**: ⭐⭐☆☆☆ (简单)

