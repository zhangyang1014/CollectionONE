# 标准字段管理SQL初始化说明

## 📋 概述

本文档说明如何使用基于CSV配置文件生成的SQL初始化脚本来恢复标准字段管理功能。

## 📁 文件位置

- **SQL初始化文件**: `backend-java/src/main/resources/db/data/init_standard_fields.sql`
- **CSV配置文件**: `参考内容/` 目录下的5个CSV文件
- **生成脚本**: `scripts/generate_standard_fields_sql.py`

## 📊 数据内容

### 字段分组（9个）

#### 一级分组（5个）
1. **客户基础信息** (customer_basic) - ID: 1
2. **贷款详情** (loan_details) - ID: 2
3. **借款记录** (borrowing_records) - ID: 3
4. **还款记录** (repayment_records) - ID: 4
5. **分期详情** (installment_details) - ID: 5

#### 二级分组（4个 - 客户基础信息的子分组）
1. **基础身份信息** (identity_info) - ID: 11, 父分组: 1
2. **教育信息** (education) - ID: 12, 父分组: 1
3. **职业信息** (employment) - ID: 13, 父分组: 1
4. **用户行为与信用** (user_behavior) - ID: 14, 父分组: 1

### 标准字段

基于以下CSV文件生成：

1. **CCO催收字段集合-客户基础信息.csv**
   - 基础身份信息：11个字段
   - 教育信息：3个字段
   - 职业信息：12个字段
   - 用户行为与信用：10个字段

2. **CCO催收字段集合-贷款详情.csv**
   - 23个字段

3. **CCO催收字段集合-借款记录.csv**
   - 24个字段

4. **CCO催收字段集合-分期详情.csv**
   - 16个字段

5. **CCO催收字段集合-还款记录.csv**
   - 13个字段

**总计**: 约112个标准字段

### Mock甲方数据

- **Mock甲方A** (ID: 1)
  - 自动启用所有标准字段
  - 配置存储在 `tenant_field_configs` 表中

## 🚀 使用方法

### 方法1: 直接执行SQL文件

```bash
# 连接到MySQL数据库
mysql -u root -p cco_system < backend-java/src/main/resources/db/data/init_standard_fields.sql
```

### 方法2: 在MySQL客户端中执行

```sql
-- 1. 连接到数据库
USE cco_system;

-- 2. 执行SQL文件
SOURCE /path/to/init_standard_fields.sql;
```

### 方法3: 使用数据库管理工具

1. 打开数据库管理工具（如Navicat、DBeaver等）
2. 连接到 `cco_system` 数据库
3. 打开 `init_standard_fields.sql` 文件
4. 执行整个脚本

## ⚠️ 注意事项

### 数据清空选项

SQL文件开头包含了清空现有数据的选项（已注释）：

```sql
-- 清空现有数据（可选，谨慎使用）
-- DELETE FROM `tenant_field_configs` WHERE `tenant_id` = 1;
-- DELETE FROM `standard_fields`;
-- DELETE FROM `field_groups` WHERE `id` IN (11, 12, 13, 14);
-- DELETE FROM `field_groups` WHERE `id` IN (1, 2, 3, 4, 5);
```

**如果需要清空现有数据重新初始化**，请取消这些注释。

### 数据冲突处理

SQL使用了 `ON DUPLICATE KEY UPDATE` 来处理数据冲突：

- **字段分组**: 如果ID已存在，会更新分组信息
- **标准字段**: 如果 `field_key` 已存在，会跳过插入
- **Mock甲方配置**: 如果配置已存在，会更新配置信息

### 字段ID连续性

- 字段ID从1开始连续递增
- 如果数据库中已有字段，可能会产生ID冲突
- 建议在清空数据后执行，或修改SQL中的ID值

## 🔄 重新生成SQL

如果CSV文件有更新，可以重新生成SQL文件：

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE
python3 scripts/generate_standard_fields_sql.py
```

## 📝 字段属性说明

### 字段类型

- **String**: 文本类型
- **Integer**: 整数类型
- **Decimal**: 小数类型
- **Date**: 日期类型
- **Datetime**: 日期时间类型
- **Boolean**: 布尔类型
- **Enum**: 枚举类型（包含枚举选项）
- **FileList**: 文件列表类型

### 字段属性

- **is_required**: 是否必填（0=否，1=是）
- **is_extended**: 是否为拓展字段（0=否，1=是）
- **is_active**: 是否启用（0=否，1=是）
- **is_deleted**: 是否删除（0=否，1=是，软删除）
- **sort_order**: 排序顺序

### 枚举字段

枚举类型字段的 `enum_options` 字段包含JSON格式的枚举选项：

```json
[
  {
    "standard_name": "Male",
    "standard_id": "male",
    "tenant_name": "Male",
    "tenant_id": "male"
  },
  {
    "standard_name": "Female",
    "standard_id": "female",
    "tenant_name": "Female",
    "tenant_id": "female"
  }
]
```

## ✅ 验证初始化结果

执行SQL后，可以通过以下方式验证：

### 1. 检查字段分组数量

```sql
SELECT COUNT(*) FROM field_groups WHERE is_active = 1;
-- 应该返回 9
```

### 2. 检查标准字段数量

```sql
SELECT COUNT(*) FROM standard_fields WHERE is_deleted = 0 AND is_active = 1;
-- 应该返回约 112
```

### 3. 按分组统计字段数量

```sql
SELECT 
    fg.group_name,
    COUNT(sf.id) as field_count
FROM field_groups fg
LEFT JOIN standard_fields sf ON sf.field_group_id = fg.id
WHERE sf.is_deleted = 0 AND sf.is_active = 1
GROUP BY fg.id, fg.group_name
ORDER BY fg.id;
```

### 4. 检查Mock甲方配置

```sql
SELECT COUNT(*) FROM tenant_field_configs WHERE tenant_id = 1;
-- 应该返回约 112
```

## 🔧 故障排查

### 问题1: 外键约束错误

**错误**: `Cannot add or update a child row: a foreign key constraint fails`

**解决**: 确保先执行字段分组的INSERT语句，再执行标准字段的INSERT语句。

### 问题2: 字段ID冲突

**错误**: `Duplicate entry '1' for key 'PRIMARY'`

**解决**: 
1. 清空现有数据（取消SQL文件开头的DELETE注释）
2. 或修改SQL中的ID值，使用更大的起始ID

### 问题3: 字符编码问题

**错误**: 中文显示乱码

**解决**: 确保数据库和表的字符集为 `utf8mb4`，排序规则为 `utf8mb4_unicode_ci`。

## 📚 相关文档

- [标准字段管理功能说明](../功能说明/标准字段管理功能说明.md)
- [字段分组管理功能说明](../功能说明/字段分组管理功能说明.md)
- [甲方字段管理功能说明](../功能说明/甲方字段管理功能说明.md)

## 📅 更新记录

- **2025-01-XX**: 初始版本，基于CSV文件生成SQL初始化脚本

