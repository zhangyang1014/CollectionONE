# 🚨 快速修复：甲方列表显示空白

## 问题
打开甲方管理页面，列表字段全部显示空白。

## 快速修复（3步）

### 步骤1：执行数据库迁移（添加字段）
```bash
mysql -u root -p cco_dev < src/main/resources/db/migration/V20251204_001__add_tenant_default_language.sql
```

### 步骤2：初始化Mock数据
```bash
./quick-init-tenants.sh
```

### 步骤3：刷新浏览器
打开浏览器，刷新甲方管理页面。

## 如果上述步骤失败

### 手动执行SQL（复制粘贴即可）

```sql
-- 连接数据库
USE cco_dev;

-- 添加字段
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS default_language VARCHAR(10) COMMENT '默认语言';

-- 插入数据
INSERT INTO tenants (id, tenant_code, tenant_name, tenant_name_en, country_code, timezone, currency_code, default_language, is_active, created_at, updated_at) VALUES
(1, 'BTQ', '百腾企业', 'Baiteng Enterprise', 'MX', 'America/Mexico_City', 'MXN', 'es-MX', 1, NOW(), NOW()),
(2, 'BTSK', 'BTSK机构', 'BTSK Organization', 'CN', 'Asia/Shanghai', 'CNY', 'zh-CN', 1, NOW(), NOW()),
(3, 'DEMO', '演示甲方', 'Demo Tenant', 'US', 'America/New_York', 'USD', 'en-US', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE tenant_name=VALUES(tenant_name), updated_at=NOW();

-- 查看结果
SELECT * FROM tenants;
```

## 完成！

刷新浏览器，应该能看到3条甲方数据了。

---

**注意**：如果数据库名不是 `cco_dev`，请替换为实际的数据库名。

