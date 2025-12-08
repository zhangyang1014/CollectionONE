# 案件详情甲方字段查看 PRD

## 版本记录

| 版本号 | 日期 | 作者 | 变更说明 |
|--------|------|------|---------|
| v1.0.0 | 2025-12-08 | 产品大象 | 初始版本，基于分组的甲方字段查看和上传管理 |

---

## 1. 背景与目标

- 甲方通过上传字段JSON文件（含分组结构），供控台只读查看，便于对齐字段映射与展示。
- **区分场景**：本功能针对"案件详情"场景，与"案件列表甲方字段查看"区分开。
- **核心特点**：详情场景必须包含分组，一次上传含多个分组的完整字段配置。
- 提供规范的JSON模板（含分组结构）和上传管理功能，支持版本对比和切换。

## 2. 角色与范围

- 角色：CCO superadmin（可上传管理、版本切换）；甲方运营（只读查看）。
- 范围：控台"案件详情甲方字段查看"页面。
- 页面标题：**案件详情甲方字段查看**（区别于"案件列表甲方字段查看"）

## 3. 需求概述

- 读取接口 `/api/v1/tenants/{tenantId}/fields-json?scene=detail` 返回的字段数组（含分组结构）并展示。
- 若甲方未上传或返回空，则使用"案件详情标准字段管理"字段作为兜底mock展示。
- 支持分组树展示和分组筛选查看。
- 提供JSON模板下载、上传、历史记录查看、版本对比、版本切换等完整功能。

## 4. 页面布局

### 4.1 页面头部

```text
┌───────────────────────────────────────────────────────────────┐
│  案件详情甲方字段查看                                              │
│                                                                │
│  当前甲方：XX金融 (tenant_001)                                   │
│  当前版本：版本3 | 上传时间：2025-12-07 15:30:00 | 字段数：21个   │
│  分组数：5个                                                     │
│                                                                │
│  [下载JSON模板]  [上传JSON文件]  [版本管理]                       │
└───────────────────────────────────────────────────────────────┘
```

### 4.2 页面主体布局

```text
┌─────────────┬─────────────────────────────────────────────────┐
│ 分组树       │ 字段表格                                          │
│             │                                                   │
│ ▼ 基本信息  │ 序号 | 字段名称 | 字段标识 | 字段类型 | 枚举值 |  │
│   (4个字段) │  1   | 案件编号 | case_id  | String  |   -    |  │
│             │  2   | 客户姓名 | user_name| String  |   -    |  │
│ ▼ 借款信息  │  ...                                             │
│   (7个字段) │                                                   │
│             │                                                   │
│ ▼ 催收信息  │                                                   │
│   (3个字段) │                                                   │
│             │                                                   │
│ ▼ 联系信息  │                                                   │
│   (3个字段) │                                                   │
│             │                                                   │
│ ▼ 产品信息  │                                                   │
│   (4个字段) │                                                   │
└─────────────┴─────────────────────────────────────────────────┘
```

### 4.3 按钮说明

1. **下载JSON模板**（次要按钮）：所有用户可见
2. **上传JSON文件**（主要按钮）：仅superadmin可见
3. **版本管理**（次要按钮）：仅superadmin可见，点击显示版本管理面板

## 5. 分组树功能

### 5.1 分组树结构

```text
┌─────────────────────────┐
│ 全部字段 (21个)          │
├─────────────────────────┤
│ ▼ 基本信息 (4个字段)     │
│ ▼ 借款信息 (7个字段)     │
│ ▼ 催收信息 (3个字段)     │
│ ▼ 联系信息 (3个字段)     │
│ ▼ 产品信息 (4个字段)     │
└─────────────────────────┘
```

### 5.2 分组树交互

- 默认展开所有分组，右侧显示全部字段
- 点击"全部字段"节点，显示所有字段（不分组）
- 点击分组节点（如"基本信息"），右侧表格仅显示该分组下的字段
- 支持分组折叠/展开
- 显示每个分组包含的字段数量

## 6. 表格字段展示

### 6.1 表格列

- 序号
- 分组名称（仅"全部字段"视图显示此列）
- 字段名称 `field_name`
- 字段标识 `field_key`
- 字段类型 `field_type`
- 枚举值（如Enum展示前2个并提示剩余数量，如"待分配, 催收中 等4个"）
- 是否必填 `is_required`（显示 ✓ 或 -）
- 排序 `sort_order`（分组内排序）
- 描述 `description`（可选，鼠标悬停显示完整描述）

### 6.2 表格功能

- 支持按字段名称、字段标识搜索
- 支持按字段类型筛选
- 支持按是否必填筛选
- 每页显示20条，支持分页

## 7. JSON模板管理

### 7.1 JSON模板示例（含分组结构）

提供标准JSON模板供甲方下载参考，模板包含所有必需字段和分组结构：

```json
{
  "version": "1.0",
  "scene": "detail",
  "tenant_id": "示例甲方ID",
  "tenant_name": "示例甲方名称",
  "updated_at": "2025-12-08T10:00:00Z",
  "description": "案件详情字段配置 - 用于案件详情页面展示",
  "groups": [
    {
      "group_key": "basic_info",
      "group_name": "基本信息",
      "sort_order": 1,
      "description": "案件基础信息",
      "fields": [
        {
          "field_name": "案件编号",
          "field_key": "case_code",
          "field_type": "String",
          "enum_values": null,
          "is_required": true,
          "sort_order": 1,
          "description": "案件唯一标识，不可为空"
        },
        {
          "field_name": "客户姓名",
          "field_key": "user_name",
          "field_type": "String",
          "enum_values": null,
          "is_required": true,
          "sort_order": 2,
          "description": "借款人真实姓名"
        },
        {
          "field_name": "身份证号",
          "field_key": "id_card",
          "field_type": "String",
          "enum_values": null,
          "is_required": true,
          "sort_order": 3,
          "description": "借款人身份证号码"
        },
        {
          "field_name": "案件状态",
          "field_key": "case_status",
          "field_type": "Enum",
          "enum_values": ["待分配", "催收中", "已完成", "已关闭"],
          "is_required": true,
          "sort_order": 4,
          "description": "案件当前状态，用于流程跟踪"
        }
      ]
    },
    {
      "group_key": "loan_info",
      "group_name": "借款信息",
      "sort_order": 2,
      "description": "借款相关信息",
      "fields": [
        {
          "field_name": "贷款金额",
          "field_key": "loan_amount",
          "field_type": "Decimal",
          "enum_values": null,
          "is_required": true,
          "sort_order": 1,
          "description": "借款本金，单位：元"
        },
        {
          "field_name": "未还金额",
          "field_key": "outstanding_amount",
          "field_type": "Decimal",
          "enum_values": null,
          "is_required": true,
          "sort_order": 2,
          "description": "当前未还余额"
        },
        {
          "field_name": "逾期天数",
          "field_key": "overdue_days",
          "field_type": "Integer",
          "enum_values": null,
          "is_required": true,
          "sort_order": 3,
          "description": "当前逾期天数，自动计算"
        },
        {
          "field_name": "到期日期",
          "field_key": "due_date",
          "field_type": "Date",
          "enum_values": null,
          "is_required": true,
          "sort_order": 4,
          "description": "借款到期日"
        },
        {
          "field_name": "期数",
          "field_key": "total_installments",
          "field_type": "Integer",
          "enum_values": null,
          "is_required": true,
          "sort_order": 5,
          "description": "总期数"
        },
        {
          "field_name": "当期天数",
          "field_key": "term_days",
          "field_type": "Integer",
          "enum_values": null,
          "is_required": true,
          "sort_order": 6,
          "description": "当前期天数"
        },
        {
          "field_name": "关联借款ID",
          "field_key": "loan_id",
          "field_type": "String",
          "enum_values": null,
          "is_required": true,
          "sort_order": 7,
          "description": "关联主数据"
        }
      ]
    },
    {
      "group_key": "contact_info",
      "group_name": "联系信息",
      "sort_order": 3,
      "description": "联系人信息",
      "fields": [
        {
          "field_name": "手机号",
          "field_key": "mobile_number",
          "field_type": "String",
          "enum_values": null,
          "is_required": true,
          "sort_order": 1,
          "description": "借款人联系电话"
        },
        {
          "field_name": "紧急联系人",
          "field_key": "emergency_contact",
          "field_type": "String",
          "enum_values": null,
          "is_required": false,
          "sort_order": 2,
          "description": "紧急联系人姓名"
        },
        {
          "field_name": "紧急联系电话",
          "field_key": "emergency_phone",
          "field_type": "String",
          "enum_values": null,
          "is_required": false,
          "sort_order": 3,
          "description": "紧急联系人电话"
        }
      ]
    }
  ]
}
```

### 7.2 JSON结构说明

**顶层字段**：
- `version`: 版本标识（固定"1.0"）
- `scene`: 场景标识（固定"detail"）
- `tenant_id`: 甲方ID
- `tenant_name`: 甲方名称
- `updated_at`: 更新时间
- `description`: 配置说明
- `groups`: 分组数组（核心）

**分组对象字段**：
- `group_key`: 分组标识（建议参考标准分组）
- `group_name`: 分组名称
- `sort_order`: 分组排序
- `description`: 分组说明
- `fields`: 字段数组

**字段对象字段**：
- `field_name`: 字段名称
- `field_key`: 字段标识
- `field_type`: 字段类型（String/Integer/Number/Decimal/Boolean/Date/DateTime/Enum）
- `enum_values`: 枚举值（仅Enum类型需要）
- `is_required`: 是否必填
- `sort_order`: 字段排序（分组内排序）
- `description`: 字段说明

### 7.3 模板下载功能

- 在页面右上角提供"下载JSON模板"按钮
- 按钮样式：次要按钮，图标为下载图标 📥
- 点击后下载文件名：`tenant_fields_detail_template.json`
- 模板内容包含3个分组示例和详细说明

## 8. JSON上传功能

### 8.1 上传入口

- 在页面右上角提供"上传JSON文件"按钮（主要按钮）
- 按钮仅对 CCO superadmin 角色可见
- 点击后弹出上传弹窗

### 8.2 上传弹窗设计

#### 8.2.1 弹窗布局

```text
┌─────────────────────────────────────────────────────────────┐
│  上传案件详情甲方字段配置                                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  当前甲方信息                                                  │
│  ┌───────────────────────────────────────────────────┐      │
│  │ 甲方名称：XX金融                                      │      │
│  │ 甲方ID：tenant_001                                   │      │
│  │ 配置场景：案件详情                                     │      │
│  │ 当前生效版本：版本3                                    │      │
│  │ 当前字段数：21个                                       │      │
│  │ 当前分组数：5个                                        │      │
│  │ 最后更新：2025-12-07 15:30:00                        │      │
│  │ 更新人：管理员 (admin)                                │      │
│  └───────────────────────────────────────────────────┘      │
│                                                              │
│  上传历史记录（最近5次）                   [查看全部历史 >]      │
│  ┌───────────────────────────────────────────────────┐      │
│  │ ● 版本3 - 2025-12-07 15:30:00          [当前使用]  │      │
│  │   上传人：管理员 | 字段数：21 | 分组数：5            │      │
│  │   [查看] [下载] [对比]                              │      │
│  │                                                     │      │
│  │ ○ 版本2 - 2025-12-05 10:20:00                      │      │
│  │   上传人：管理员 | 字段数：18 | 分组数：5            │      │
│  │   [查看] [下载] [对比]                              │      │
│  │                                                     │      │
│  │ ○ 版本1 - 2025-12-01 09:00:00                      │      │
│  │   上传人：管理员 | 字段数：15 | 分组数：4            │      │
│  │   [查看] [下载] [对比]                              │      │
│  └───────────────────────────────────────────────────┘      │
│                                                              │
│  上传新文件                                                   │
│  ┌───────────────────────────────────────────────────┐      │
│  │         📤                                          │      │
│  │   [点击或拖拽上传JSON文件]                           │      │
│  │                                                     │      │
│  │   支持.json格式，文件大小不超过5MB                    │      │
│  │   必须包含分组结构（groups数组）                     │      │
│  │   上传后将创建新版本并自动设为当前使用版本             │      │
│  └───────────────────────────────────────────────────┘      │
│                                                              │
│  上传选项                                                     │
│  ☑ 上传前验证JSON格式和分组结构                                │
│  ☑ 上传成功后自动设为当前使用版本                              │
│  ☐ 上传后显示与上一版本的对比                                  │
│                                                              │
│                           [取消]  [确认上传]                  │
└─────────────────────────────────────────────────────────────┘
```

### 8.3 上传验证规则

#### 8.3.1 文件格式验证

- 文件扩展名必须为 `.json`
- 文件大小不超过 5MB（比列表场景大，因为包含分组）
- JSON格式必须合法（可解析）

#### 8.3.2 JSON内容验证

**必需字段检查**：
- `groups` 数组必须存在且非空
- 每个分组对象必须包含：
  - `group_key`（字符串，非空，符合变量命名规范）
  - `group_name`（字符串，非空，长度1-50字符）
  - `sort_order`（整数，大于0）
  - `fields`（数组，非空）

- 每个字段对象必须包含：
  - `field_name`（字符串，非空，长度1-50字符）
  - `field_key`（字符串，非空，符合变量命名规范）
  - `field_type`（字符串，必须是支持的类型之一）
  - `is_required`（布尔值）
  - `sort_order`（整数，大于0）

**可选字段**：
- `enum_values`（数组，当 field_type 为 Enum 时必需）
- `description`（字符串，长度不超过200字符）

#### 8.3.3 业务规则验证

- `group_key` 在同一JSON中不能重复
- `field_key` 在同一分组内不能重复（不同分组可以有相同field_key，但不推荐）
- `sort_order` 建议不重复（可警告但不阻止）
- 必填字段（`is_required: true`）至少应有1个
- Enum类型的 `enum_values` 数组至少包含2个元素
- **分组数量**：建议3-8个分组，不超过10个
- **每个分组字段数**：建议2-15个字段

#### 8.3.4 分组结构验证

- 分组必须至少包含1个字段
- 建议分组参考标准分组结构（basic_info、loan_info等）
- 分组名称应清晰表达业务含义

#### 8.3.5 验证失败提示

```text
验证失败示例：
❌ 缺少必需字段 groups 数组
❌ 第2个分组缺少必需属性 group_key
❌ 第1个分组的第3个字段缺少必需属性 field_key
❌ 第2个分组的第5个字段的 field_type "Text" 不在支持类型列表中
   支持的类型：String, Integer, Number, Decimal, Boolean, Date, DateTime, Enum
❌ 第3个分组的第8个字段的 field_type 为 Enum，但缺少 enum_values 或值少于2个
❌ 分组标识 "basic_info" 重复出现（第1和第3个分组）
❌ 第1个分组内字段标识 "case_id" 重复出现（第2和第7个字段）
❌ 第10个字段的 field_key "2case" 格式不正确，必须以字母开头
```

## 9. 版本管理功能

### 9.1 版本管理面板

点击"版本管理"按钮，在页面右侧弹出抽屉式面板：

```text
┌─────────────────────────────────────────────────────────────┐
│  版本管理 - 案件详情字段配置                          [×]      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  当前使用版本：版本3                                           │
│  ┌─────────────────────────────────────────────────┐        │
│  │ 搜索版本：[___________] 🔍                       │        │
│  │ 筛选：[全部版本 ▼] [全部上传人 ▼]                │        │
│  └─────────────────────────────────────────────────┘        │
│                                                              │
│  ┌─────────────────────────────────────────────────┐        │
│  │ ● 版本3                              [当前使用]  │        │
│  │   上传时间：2025-12-07 15:30:00                  │        │
│  │   上传人：管理员 (admin)                          │        │
│  │   字段数：21个 | 分组数：5个                      │        │
│  │   说明：新增紧急联系人字段                         │        │
│  │                                                   │        │
│  │   [查看详情] [下载JSON] [与上一版本对比]           │        │
│  └─────────────────────────────────────────────────┘        │
│                                                              │
│  ┌─────────────────────────────────────────────────┐        │
│  │ ○ 版本2                                          │        │
│  │   上传时间：2025-12-05 10:20:00                  │        │
│  │   上传人：管理员 (admin)                          │        │
│  │   字段数：18个 | 分组数：5个                      │        │
│  │   说明：调整分组结构，新增催收信息分组             │        │
│  │                                                   │        │
│  │   [查看详情] [下载JSON] [设为当前版本] [对比]      │        │
│  └─────────────────────────────────────────────────┘        │
│                                                              │
│  显示 1-10 / 共25条                      [上一页] [下一页]   │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 版本详情查看

点击"查看详情"按钮，在弹窗中展示该版本的完整字段列表（含分组）：

```text
┌─────────────────────────────────────────────────────────────┐
│  版本详情 - 版本2                                      [×]    │
├─────────────────────────────────────────────────────────────┤
│  上传时间：2025-12-05 10:20:00                               │
│  上传人：管理员 (admin)                                       │
│  字段数：18个 | 分组数：5个                                   │
│  说明：调整分组结构，新增催收信息分组                          │
│                                                              │
│  分组视图：[分组展示 ●] [平铺展示 ○]                          │
│                                                              │
│  ┌───────────────────────────────────────────────┐          │
│  │ ▼ 基本信息 (4个字段)                           │          │
│  │   序号 | 字段名称 | 字段标识 | 类型 | 必填      │          │
│  │    1  | 案件编号 | case_code  | String | ✓    │          │
│  │    2  | 客户姓名 | user_name  | String | ✓    │          │
│  │   ...                                          │          │
│  │                                                │          │
│  │ ▼ 借款信息 (7个字段)                           │          │
│  │   ...                                          │          │
│  └───────────────────────────────────────────────┘          │
│                                                              │
│                    [下载此版本] [关闭]                         │
└─────────────────────────────────────────────────────────────┘
```

### 9.3 版本对比功能

#### 9.3.1 对比结果展示

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│  版本对比：版本2 vs 版本3                                                   [×]  │
├─────────────────────────────────────────────────────────────────────────────────┤
│  版本2（基准版本）                    版本3（当前版本）                           │
│  2025-12-05 10:20:00                 2025-12-07 15:30:00                        │
│  18个字段 | 5个分组                   21个字段 | 5个分组                          │
│                                                                                  │
│  变更摘要：                                                                       │
│  • 新增字段：3个                                                                  │
│  • 删除字段：0个                                                                  │
│  • 修改字段：2个                                                                  │
│  • 分组变更：0个                                                                  │
│                                                                                  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                                                                  │
│  📊 详细对比（按分组）                                                             │
│                                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐        │
│  │ 🟢 新增字段（3个）                                                     │        │
│  ├────────────────────────────────────────────────────────────────────┤        │
│  │ 分组：联系信息                                                         │        │
│  │ + 紧急联系人 (emergency_contact)                                      │        │
│  │   类型：String | 必填：否 | 排序：2                                   │        │
│  │                                                                       │        │
│  │ + 紧急联系电话 (emergency_phone)                                      │        │
│  │   类型：String | 必填：否 | 排序：3                                   │        │
│  │                                                                       │        │
│  │ 分组：催收信息                                                         │        │
│  │ + 最后联系时间 (last_contact_time)                                    │        │
│  │   类型：Datetime | 必填：否 | 排序：3                                 │        │
│  └────────────────────────────────────────────────────────────────────┘        │
│                                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐        │
│  │ 🔴 删除字段（0个）                                                     │        │
│  ├────────────────────────────────────────────────────────────────────┤        │
│  │ 无删除字段                                                             │        │
│  └────────────────────────────────────────────────────────────────────┘        │
│                                                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐        │
│  │ 🟡 修改字段（2个）                                                     │        │
│  ├────────────────────────────────────────────────────────────────────┤        │
│  │ 分组：基本信息                                                         │        │
│  │ ≈ 案件状态 (case_status)                                              │        │
│  │   枚举值：                                                             │        │
│  │   - 版本2：["待分配", "催收中", "已完成"]                              │        │
│  │   + 版本3：["待分配", "催收中", "已完成", "已关闭"]                    │        │
│  │                                                                       │        │
│  │ 分组：借款信息                                                         │        │
│  │ ≈ 逾期天数 (overdue_days)                                             │        │
│  │   描述：                                                               │        │
│  │   - 版本2：逾期天数                                                    │        │
│  │   + 版本3：当前逾期天数，自动计算                                      │        │
│  └────────────────────────────────────────────────────────────────────┘        │
│                                                                                  │
│          [导出对比报告]  [下载版本2]  [下载版本3]  [关闭]                         │
└─────────────────────────────────────────────────────────────────────────────────┘
```

#### 9.3.2 对比维度

**分组级别对比**：
- 新增分组
- 删除分组
- 分组重命名
- 分组排序变更

**字段级别对比**：
- field_name（字段名称）
- field_type（字段类型）
- enum_values（枚举值）
- is_required（是否必填）
- sort_order（排序）
- description（描述）
- 所属分组变更

## 10. 前端要求

### 10.1 页面路由与标题

- 路由：`/field-config/tenant-fields-detail`（区别于列表场景）
- 页面标题：**案件详情甲方字段查看**
- 面包屑：字段管理 / 案件详情甲方字段查看

### 10.2 页面布局

- 左侧分组树（宽度200px，可折叠）
- 右侧字段表格（自适应宽度）
- 支持分组筛选查看
- 支持表格搜索（按字段名称、字段标识）

### 10.3 数据加载逻辑

```typescript
// 伪代码
async function loadTenantFields() {
  try {
    const response = await fetch(`/api/v1/tenants/${tenantId}/fields-json?scene=detail`)
    if (response.ok && response.data.groups && response.data.groups.length > 0) {
      // 显示甲方上传的字段（含分组）
      displayFieldsWithGroups(response.data.groups)
      showCurrentVersion({
        version: response.data.version,
        uploadedAt: response.data.fetched_at,
        fieldsCount: calculateTotalFields(response.data.groups),
        groupsCount: response.data.groups.length
      })
    } else {
      // 兜底：加载标准字段（含分组）
      const standardFields = await fetch('/api/v1/standard-fields/case-detail')
      displayFieldsWithGroups(standardFields.groups)
      showEmptyTip('当前甲方未上传字段配置，显示标准字段作为参考')
    }
  } catch (error) {
    handleError(error)
  }
}

function calculateTotalFields(groups) {
  return groups.reduce((sum, group) => sum + group.fields.length, 0)
}
```

### 10.4 分组树组件

```vue
<template>
  <el-tree
    :data="groupTreeData"
    :props="treeProps"
    @node-click="handleNodeClick"
    default-expand-all
  >
    <template #default="{ node, data }">
      <span class="custom-tree-node">
        <span>{{ data.label }}</span>
        <span class="field-count">{{ data.count }}个字段</span>
      </span>
    </template>
  </el-tree>
</template>

<script setup>
const groupTreeData = computed(() => {
  const totalCount = groups.value.reduce((sum, g) => sum + g.fields.length, 0)
  
  return [
    {
      id: 'all',
      label: '全部字段',
      count: totalCount,
      children: groups.value.map(g => ({
        id: g.group_key,
        label: g.group_name,
        count: g.fields.length
      }))
    }
  ]
})

function handleNodeClick(data) {
  if (data.id === 'all') {
    // 显示所有字段
    displayAllFields()
  } else {
    // 显示特定分组的字段
    displayGroupFields(data.id)
  }
}
</script>
```

## 11. 后端要求

### 11.1 接口设计

#### 11.1.1 获取甲方字段（含分组）

**接口**：GET /api/v1/tenants/{tenantId}/fields-json

**请求参数**：
- `scene`: 场景，固定值：`detail`

**返回格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tenant_id": "tenant_001",
    "tenant_name": "XX金融",
    "scene": "detail",
    "version": 3,
    "fetched_at": "2025-12-07T15:30:00Z",
    "fields_count": 21,
    "groups_count": 5,
    "uploaded_by": "admin",
    "uploaded_by_name": "管理员",
    "groups": [
      {
        "group_key": "basic_info",
        "group_name": "基本信息",
        "sort_order": 1,
        "description": "案件基础信息",
        "fields": [
          {
            "field_name": "案件编号",
            "field_key": "case_code",
            "field_type": "String",
            "enum_values": null,
            "is_required": true,
            "sort_order": 1,
            "description": "案件唯一标识"
          }
        ]
      }
    ]
  }
}
```

#### 11.1.2 上传JSON文件

**接口**：POST /api/v1/tenants/{tenantId}/fields-json/upload

**请求**：
- Content-Type: `multipart/form-data`
- 参数：
  - `file`: JSON文件
  - `scene`: 场景（固定值：detail）
  - `uploaded_by`: 上传人ID
  - `version_note`: 版本说明（可选）

**响应**：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "version": 4,
    "fields_count": 23,
    "groups_count": 6,
    "uploaded_at": "2025-12-08T10:30:00Z",
    "is_active": true
  }
}
```

### 11.2 数据库表设计

使用与列表场景相同的表 `tenant_field_uploads`，通过 `scene` 字段区分：

```sql
-- scene='detail' 表示案件详情场景
-- json_content 字段存储包含分组结构的完整JSON
{
  "groups": [
    {
      "group_key": "basic_info",
      "group_name": "基本信息",
      "sort_order": 1,
      "fields": [...]
    }
  ]
}
```

## 12. 验收标准

### 12.1 基础展示功能
- ✅ 页面标题为"案件详情甲方字段查看"
- ✅ 左侧分组树正确显示，默认展开所有分组
- ✅ 点击分组节点，右侧表格显示对应分组字段
- ✅ 点击"全部字段"节点，显示所有字段（含分组列）
- ✅ 有数据：表格正常显示甲方上传的全部字段（含分组）
- ✅ 无数据：表格显示标准字段兜底，含分组结构
- ✅ 页面无新增/编辑/删除/拖拽入口（仅查看）
- ✅ 枚举值超过2个时正确显示"等n个"
- ✅ 显示当前版本信息（含分组数）

### 12.2 JSON模板功能
- ✅ 能下载JSON模板文件（含分组结构）
- ✅ 模板包含完整的分组和字段示例
- ✅ 模板格式与实际上传要求一致
- ✅ 模板文件名包含场景标识（detail）

### 12.3 上传验证功能
- ✅ 正确验证文件格式（仅支持.json）
- ✅ 正确验证文件大小（不超过5MB）
- ✅ 正确验证JSON格式合法性
- ✅ 正确验证分组结构（groups数组必须存在）
- ✅ 正确验证分组必需字段完整性
- ✅ 正确验证字段必需字段完整性
- ✅ 正确验证字段类型有效性
- ✅ 正确验证group_key和field_key命名规范
- ✅ 正确验证Enum类型的enum_values
- ✅ 验证失败时显示详细错误提示（含分组和字段位置）

### 12.4 版本管理功能
- ✅ 版本管理面板正确显示所有版本（含分组数）
- ✅ 当前使用版本正确标识
- ✅ 能查看任意版本的详情（分组视图和平铺视图）
- ✅ 能下载任意版本的JSON文件
- ✅ 能切换到历史版本
- ✅ 版本切换需要二次确认
- ✅ 版本切换后页面自动刷新

### 12.5 版本对比功能
- ✅ 能对比任意两个版本
- ✅ 正确识别新增字段（按分组显示）
- ✅ 正确识别删除字段（按分组显示）
- ✅ 正确识别修改字段（包括所属分组变更）
- ✅ 正确识别分组变更（新增/删除/重命名）
- ✅ 正确统计未变更字段
- ✅ 对比结果用不同颜色标识
- ✅ 能导出对比报告（Markdown格式，含分组信息）

## 13. 与列表场景的区别

| 项目 | 列表场景 | 详情场景 |
|-----|---------|---------|
| 场景标识 | scene=list | scene=detail |
| 分组结构 | 无分组 | 有分组（必须） |
| JSON结构 | fields数组 | groups数组（含fields） |
| 文件大小限制 | 2MB | 5MB |
| 页面布局 | 单表格 | 分组树+表格 |
| 版本对比 | 字段级对比 | 分组+字段级对比 |
| 模板文件名 | tenant_fields_list_template.json | tenant_fields_detail_template.json |

---

**文档版本**: v1.0.0  
**最后更新**: 2025-12-08  
**更新人**: 产品大象  
**变更说明**: 初始版本，基于分组的甲方字段查看和上传管理
