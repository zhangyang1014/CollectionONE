# 案件详情标准字段管理 PRD

## 版本记录

| 版本号 | 日期 | 作者 | 变更说明 |
|--------|------|------|---------|
| v1.0.0 | 2025-12-08 | 产品大象 | 初始版本，基于分组的标准字段管理 |

---

## 1. 背景与目标

- 提供统一的标准字段定义（含分组结构），所有甲方继承，避免各自维护导致的接口与展示差异。
- **核心特点**：以分组为核心，标准字段按分组组织，形成官方配置模板。
- 来源：前端只读展示，后端统一下发，不允许新增、编辑、删除或排序。
- 用途：用于与甲方上传的字段做映射匹配，保证案件详情字段一致性。

## 2. 角色与范围

- 角色：CCO superadmin；甲方运营（只读）。
- 范围：控台"案件详情标准字段管理"页面，覆盖案件详情场景。

## 3. 需求概述

- 展示标准字段清单（含分组结构），字段属性：分组名称、字段名称、标识、数据类型、必填、来源、说明。
- 不展示可搜索/可筛选/范围检索/展示宽度等配置项（前端已去除）。
- 支持分组折叠/展开查看。

## 3.1 字段来源与映射用途

- **字段来源**：系统统一配置，默认继承给所有甲方，甲方端仅可查看不可编辑/覆盖。
- **分组结构**：标准字段按业务分组组织，如"基本信息"、"借款信息"、"催收信息"、"联系信息"等。
- **用途**：用于与甲方上传的字段做映射匹配，保证案件详情字段一致性。
- **映射匹配逻辑**：
  - 以标准字段标识 `field_key` 为唯一匹配主键。
  - 上传文件中的字段标识与标准 `field_key` 进行不区分大小写、下划线/中划线等分隔符的等价匹配；完全匹配则自动绑定。
  - 如未匹配到，再按预置同义别名表（如"mobile_number" ↔ "phone_number"）进行二级匹配。
  - 仍未匹配的字段标记为"待人工确认"，提示补充映射或新增甲方自定义字段（不影响标准字段只读）。

## 4. 分组与字段清单（示例）

### 4.1 分组结构

| 分组ID | 分组名称 | 分组标识 | 排序 | 说明 |
|--------|---------|---------|------|------|
| 1 | 基本信息 | basic_info | 1 | 案件基础信息 |
| 2 | 借款信息 | loan_info | 2 | 借款相关信息 |
| 3 | 催收信息 | collection_info | 3 | 催收过程信息 |
| 4 | 联系信息 | contact_info | 4 | 联系人信息 |
| 5 | 产品信息 | product_info | 5 | 产品与商户信息 |

### 4.2 字段清单（按分组）

**分组1：基本信息**
1) case_code / String / 必填 / 案件编号
2) user_name / String / 必填 / 客户姓名
3) id_card / String / 必填 / 身份证号
4) case_status / Enum / 必填 / 案件状态

**分组2：借款信息**
5) loan_amount / Decimal / 必填 / 贷款金额
6) outstanding_amount / Decimal / 必填 / 未还金额
7) overdue_days / Integer / 必填 / 逾期天数
8) due_date / Date / 必填 / 到期日期
9) total_installments / Integer / 必填 / 期数
10) term_days / Integer / 必填 / 当期天数
11) loan_id / String / 必填 / 关联借款ID

**分组3：催收信息**
12) collector_name / String / 可选 / 催收员姓名
13) collection_stage / Enum / 可选 / 催收阶段
14) last_contact_time / Datetime / 可选 / 最后联系时间

**分组4：联系信息**
15) mobile_number / String / 必填 / 手机号
16) emergency_contact / String / 可选 / 紧急联系人
17) emergency_phone / String / 可选 / 紧急联系电话

**分组5：产品信息**
18) system_name / String / 必填 / 所属系统
19) product_name / String / 必填 / 产品
20) app_name / String / 必填 / APP
21) merchant_name / String / 必填 / 商户

## 5. 前端要求

### 5.1 页面布局

- 路由：/field-config/standard-detail（案件详情标准字段管理）。
- 页面标题：案件详情标准字段管理
- 布局：左侧分组树 + 右侧字段表格

### 5.2 分组树

```text
┌─────────────────────────┐
│ 分组结构                 │
├─────────────────────────┤
│ ▼ 基本信息 (4个字段)     │
│ ▼ 借款信息 (7个字段)     │
│ ▼ 催收信息 (3个字段)     │
│ ▼ 联系信息 (3个字段)     │
│ ▼ 产品信息 (4个字段)     │
└─────────────────────────┘
```

- 支持折叠/展开
- 点击分组节点，右侧表格显示该分组下的字段
- 默认展开所有分组，右侧显示全部字段

### 5.3 字段表格

表格列：
- #（序号）
- 分组名称
- 字段名称
- 字段标识
- 数据类型
- 必填
- 来源（固定：系统统一配置）
- 说明

### 5.4 交互规则

- 无任何编辑、拖拽、分组管理、搜索配置入口。
- 表格纯展示，支持分组筛选查看。
- 支持搜索框按字段名称或标识搜索。

## 6. 后端要求

### 6.1 接口设计

**接口**：GET /api/v1/standard-fields/case-detail

**返回字段**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "groups": [
      {
        "group_id": 1,
        "group_name": "基本信息",
        "group_key": "basic_info",
        "sort_order": 1,
        "description": "案件基础信息",
        "fields": [
          {
            "id": 1,
            "field_key": "case_code",
            "field_name": "案件编号",
            "field_data_type": "String",
            "field_source": "system_unified",
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

### 6.2 业务规则

- field_source 固定为 system_unified（系统统一配置），不可被甲方覆盖。
- 分组结构固定，不可新增/删除/编辑分组。
- 映射逻辑：甲方上传字段时按"3.1 字段来源与映射用途"中的匹配规则返回匹配状态（自动匹配/别名匹配/待人工确认）。
- 只读：POST/PUT/DELETE/排序接口需返回错误提示"标准字段为统一定义，仅支持查看"。

## 7. 数据库设计

### 7.1 标准字段表

```sql
CREATE TABLE standard_fields_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  group_id BIGINT NOT NULL COMMENT '分组ID',
  field_key VARCHAR(100) NOT NULL COMMENT '字段标识',
  field_name VARCHAR(200) NOT NULL COMMENT '字段名称',
  field_data_type VARCHAR(20) NOT NULL COMMENT '数据类型',
  field_source VARCHAR(20) DEFAULT 'system_unified' COMMENT '字段来源',
  is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
  sort_order INT DEFAULT 0 COMMENT '排序顺序',
  description TEXT COMMENT '字段说明',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_field_key (field_key),
  INDEX idx_group_id (group_id)
) COMMENT '案件详情标准字段表';
```

### 7.2 字段分组表

```sql
CREATE TABLE field_groups_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  group_key VARCHAR(100) NOT NULL COMMENT '分组标识',
  group_name VARCHAR(200) NOT NULL COMMENT '分组名称',
  sort_order INT DEFAULT 0 COMMENT '排序顺序',
  description TEXT COMMENT '分组说明',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_group_key (group_key)
) COMMENT '案件详情字段分组表';
```

## 8. 验收标准

- 页面展示标准字段清单（含分组结构），顺序与案件详情一致。
- 左侧分组树正确显示，支持折叠/展开。
- 点击分组节点，右侧表格显示对应分组字段。
- 不出现可搜索/可筛选/范围检索/展示宽度列。
- 所有操作按钮均禁用或不存在；接口只读。
- 搜索功能正常工作。

## 9. 与列表字段管理的区别

| 项目 | 列表字段管理 | 详情字段管理 |
|-----|------------|------------|
| 分组结构 | 无分组 | 有分组（核心） |
| 字段数量 | 15个 | 约20个 |
| 页面布局 | 单表格 | 分组树+表格 |
| 场景 | 案件列表页 | 案件详情页 |

---

**文档版本**: v1.0.0  
**最后更新**: 2025-12-08  
**更新人**: 产品大象  
**变更说明**: 初始版本，基于分组的标准字段管理
