# 参考内容 - 字段集合说明

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| 1.0.0 | 2025-12-11 | 初始创建参考内容说明文档 | 大象 |
| 1.1.0 | 2025-12-11 | 新增案件列表标准字段CSV | 大象 |

## 概述

本文件夹包含CCO催收系统的各类字段集合CSV文件，用作系统开发和数据对接的参考。

## 文件清单

### 1. CCO案件列表标准字段.csv ⭐新增

**文件说明**: 案件列表页面的标准字段定义（管理端）

**字段数量**: 19个

**更新日期**: 2025-12-11

**字段列表**:
1. 案件编号 (case_code) - 必填
2. **用户id (user_id) - 必填** ⭐新增
3. 客户 (user_name) - 必填
4. 手机号 (mobile_number) - 必填
5. **首复借类型 (collection_type) - 选填** ⭐新增
6. 贷款金额 (loan_amount) - 必填
7. 未还金额 (outstanding_amount) - 必填
8. **减免金额 (waived_amount) - 选填** ⭐新增
9. 逾期天数 (overdue_days) - 必填
10. 案件状态 (case_status) - 必填
11. 到期日期 (due_date) - 必填
12. 期数 (total_installments) - 必填
13. 当期天数 (term_days) - 必填
14. 所属系统 (system_name) - 必填
15. 产品 (product_name) - 必填
16. APP (app_name) - 必填
17. 商户 (merchant_name) - 必填
18. **结清时间 (settlement_time) - 选填** ⭐新增
19. **结清方式 (settlement_method) - 选填** ⭐新增

**用途**:
- 前端开发参考
- 数据导入模板
- 字段映射配置参考
- 甲方数据对接标准

### 2. CCO催收字段集合-客户基础信息.csv

**文件说明**: 客户基础信息字段集合

**用途**: 案件详情页面 - 客户信息展示

**包含内容**:
- 基础身份信息
- 教育信息
- 职业信息
- 用户行为与信用
- 联系方式

### 3. CCO催收字段集合-贷款详情.csv

**文件说明**: 贷款详情字段集合

**用途**: 案件详情页面 - 贷款信息展示

**包含内容**:
- 贷款基本信息
- 金额相关字段
- 催收相关字段
- 还款方式信息

### 4. CCO催收字段集合-分期详情.csv

**文件说明**: 分期详情字段集合

**用途**: 案件详情页面 - 分期信息展示

**包含内容**:
- 分期编号和状态
- 应还金额和日期
- 实际还款信息
- 各项费用明细

### 5. CCO催收字段集合-还款记录.csv

**文件说明**: 还款记录字段集合

**用途**: 案件详情页面 - 还款历史记录

**包含内容**:
- 还款时间和类型
- 还款渠道和金额
- 交易流水信息
- 手续费和减免信息

### 6. CCO催收字段集合-历史借款记录.csv

**文件说明**: 历史借款记录字段集合

**用途**: 案件详情页面 - 历史借款查询

**包含内容**:
- 历史贷款信息
- 用户信息
- 案件状态
- 催收记录

## CSV文件格式说明

### 标准格式

所有CSV文件采用统一格式，包含以下列：

```csv
字段名称,英文名,类型,示例,说明,拓展字段
```

**列说明**:

1. **字段名称**: 中文显示名称
2. **英文名**: 字段标识（field_key），用于API和数据库
3. **类型**: 数据类型（String/Integer/Decimal/Date/Datetime/Enum/Boolean/Button/FileList）
4. **示例**: 字段值示例
5. **说明**: 字段用途说明
6. **拓展字段**: 是否为拓展字段（是/否）

### 数据类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| String | 字符串 | "Juan Dela Cruz" |
| Integer | 整数 | 3 |
| Decimal | 小数/金额 | 1000.50 |
| Date | 日期 | 2025/3/10 |
| Datetime | 日期时间 | 2025/3/10 14:30:00 |
| Enum | 枚举/下拉选项 | 首催 / 复催 |
| Boolean | 布尔值 | TRUE / FALSE |
| Button | 按钮 | - |
| FileList | 文件列表 | - |

### 拓展字段说明

- **否**: 标准字段，所有甲方必须提供
- **是**: 拓展字段，可选提供

## 使用方法

### 1. 前端开发参考

```typescript
// 导入CSV数据作为参考
import { standardFields } from '@/data/standardFieldsFromCSV'

// 使用字段定义
const fieldConfig = {
  field_key: 'user_id',
  field_name: '用户id',
  field_type: 'String',
  required: true
}
```

### 2. 数据导入模板

甲方可以根据CSV文件准备数据，按照以下格式：

```csv
case_code,user_id,user_name,mobile_number,...
CASE001,5983,Juan Dela Cruz,+63 9123456789,...
```

### 3. 字段映射配置

系统支持将甲方字段映射到标准字段：

```json
{
  "tenant_field_key": "customer_id",
  "standard_field_key": "user_id",
  "field_name": "客户ID"
}
```

### 4. API开发参考

后端API应该按照CSV定义返回字段：

```java
// 案件列表标准字段
@GetMapping("/case-list")
public ResponseData<List<CaseStandardFieldVO>> getCaseListFields() {
    // 返回19个标准字段
    return ResponseData.success(fields);
}
```

## 数据验证规则

### 必填字段

以下字段在数据导入时必须提供：

- 案件编号 (case_code)
- 用户id (user_id) ⭐新增
- 客户 (user_name)
- 手机号 (mobile_number)
- 贷款金额 (loan_amount)
- 未还金额 (outstanding_amount)
- 逾期天数 (overdue_days)
- 案件状态 (case_status)
- 到期日期 (due_date)
- 期数 (total_installments)
- 当期天数 (term_days)
- 所属系统 (system_name)
- 产品 (product_name)
- APP (app_name)
- 商户 (merchant_name)

### 选填字段

以下字段可以为空：

- 首复借类型 (collection_type) ⭐新增
- 减免金额 (waived_amount) ⭐新增
- 结清时间 (settlement_time) ⭐新增
- 结清方式 (settlement_method) ⭐新增

### 数据类型验证

- **String**: 不超过255字符
- **Integer**: 整数，范围 -2147483648 到 2147483647
- **Decimal**: 小数，精度到2位
- **Date**: 格式 YYYY/MM/DD 或 YYYY-MM-DD
- **Datetime**: 格式 YYYY/MM/DD HH:mm:ss 或 YYYY-MM-DD HH:mm:ss
- **Enum**: 必须是预定义的枚举值之一

## 更新历史

### 2025-12-11

#### 新增字段（案件列表标准字段）

1. **用户id (user_id)** - 必填
   - 位置: 第2位（客户字段前）
   - 说明: 系统内部唯一用户标识

2. **首复借类型 (collection_type)** - 选填
   - 位置: 第5位（贷款金额前）
   - 说明: 标记案件是否为首次或再次催收
   - 枚举值: 首催 / 复催

3. **减免金额 (waived_amount)** - 选填
   - 位置: 第8位（未还金额后）
   - 说明: 费用减免金额

4. **结清时间 (settlement_time)** - 选填
   - 位置: 第18位（最后第二位）
   - 说明: 最后一次结清操作时间

5. **结清方式 (settlement_method)** - 选填
   - 位置: 第19位（最后）
   - 说明: 最终还款方式
   - 枚举值: 自动扣款 / 手动转账 / 第三方收款

## 注意事项

1. **编码格式**: 所有CSV文件使用 UTF-8 编码
2. **字段顺序**: CSV中的顺序与系统显示顺序一致
3. **只读参考**: CSV文件仅供参考，实际字段定义以代码为准
4. **数据同步**: CSV文件与 `CaseStandardFieldProvider.java` 保持同步
5. **版本控制**: CSV文件纳入Git版本控制

## 相关文档

- [案件列表标准字段新增说明](../说明文档/功能说明/案件列表标准字段新增说明.md)
- [案件列表标准字段新增完成报告](../说明文档/完成报告/案件列表标准字段新增完成报告.md)
- [标准字段查看功能说明](../说明文档/前端/标准字段CSV查看功能说明.md)

## 联系方式

如有疑问，请联系：
- 技术负责人: 大象
- 文档更新日期: 2025-12-11
