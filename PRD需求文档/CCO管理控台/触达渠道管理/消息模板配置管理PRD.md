# 消息模板配置管理 PRD

| 版本 | 日期 | 变更内容 | 变更人 |
| --- | --- | --- | --- |
| 1.0.2 | 2025-12-10 | 新增渠道和供应商配置功能：支持多渠道区分、一模板多供应商映射、按优先级自动切换供应商 | 大象 |
| 1.0.1 | 2025-12-10 | 适用范围下沉到小组（含队列），机构仅用于筛选；前端移除个人模板选项；接口与数据结构新增team_ids/teamNames | 大象 |
| 1.0.0 | 2025-12-03 | 初始版本 | 大象 |

## 1. 功能概述

为CCO催收系统提供消息模板配置管理功能，支持管理员在控台端创建和管理催收沟通模板，催员在IM端可快速选择预配置的模板发送WhatsApp消息，通过变量替换实现个性化内容，提升催收效率和话术规范性。

**核心价值**：
- 标准化催收话术，提升沟通专业性
- 减少催员输入时间，提高工作效率
- 支持变量替换，实现个性化沟通
- 多维度分类管理，快速定位合适模板

## 2. 核心功能模块

### 2.1 模板列表页面

#### 2.1.1 列表展示

**功能要求：**
- 以表格形式展示当前甲方已配置的所有消息模板
- 支持按多维度筛选（案件阶段、模板类型、场景、时间点、启用状态）
- 支持搜索（模板名称、模板内容关键词）
- 支持分页展示（默认每页20条）
- 支持启用/禁用快速切换
- 支持新增、编辑、删除操作

**列表字段：**

| 字段 | 说明 | 宽度 |
|------|------|------|
| 模板名称 | 模板的显示名称 | 200px |
| 模板类型 | 仅保留组织模板 | 100px |
| 渠道 | 渠道类型标签（短信/RCS/WABA/WhatsApp/邮件/手机日历） | 100px |
| 供应商配置 | 显示配置的供应商数量，悬停展示供应商详情（名称、模板ID、优先级） | 120px |
| 适用小组 | 显示小组数量，如"3个小组"；悬停展示全量小组（含队列） | 150px |
| 案件阶段 | C/S0/S1-3/S3+ | 100px |
| 场景 | 问候/提醒/强度 | 80px |
| 时间点 | 上午/下午/晚上 | 80px |
| 模板内容预览 | 显示前50个字符 | 300px |
| 使用次数 | 统计模板使用次数 | 80px |
| 启用状态 | 开关控制 | 80px |
| 操作 | 编辑/删除按钮 | 120px |

**适用小组显示规则：**
- 选择全部小组：显示"全部小组"
- 选择部分小组：显示"X个小组"，悬停展示完整小组名称（含队列）

#### 2.1.2 筛选器配置

**筛选条件：**

| 筛选项 | 类型 | 选项 |
|--------|------|------|
| 案件阶段 | 下拉单选 | **全部阶段**、C（催收前）、S0（首次联系）、S1-3（初期）、S3+（后期） |
| 模板类型 | 下拉单选 | 全部、组织模板（个人模板入口暂时关闭） |
| 渠道 | 下拉单选 | **全部渠道**、短信、RCS、WABA、WhatsApp、邮件、手机日历 |
| 适用小组 | 多选下拉 | 全部小组、具体小组（含队列名） |
| 按机构筛选小组 | 下拉单选 | 全部、具体机构（仅用于过滤小组选项，不直接决定权限） |
| 场景 | 下拉单选 | **全部场景**、问候、提醒、强度 |
| 时间点 | 下拉单选 | **全部时间**、上午、下午、晚上 |
| 启用状态 | 下拉单选 | 全部、已启用、已禁用 |
| 搜索框 | 文本输入 | 支持模板名称、内容关键词搜索 |

**说明：**
- **案件阶段**：选项来自甲方案件队列管理（case_queues表的queue_code），当前版本使用固定枚举值（C、S0、S1-3、S3+），未来可扩展为动态读取
- **适用小组**：小组含队列属性，从组织架构管理的小组接口获取
- **按机构筛选小组**：仅用于下拉过滤，权限最终依据小组；机构字段不再单独控制可见范围
- **"全部"选项**：用于筛选时不限制该维度，value为空字符串


**筛选逻辑：**
- 多个筛选条件使用AND逻辑组合
- 搜索框支持模糊匹配
- 筛选后保持分页状态重置到第一页

#### 2.1.3 操作按钮

**新增模板：**
- 位置：列表页面右上角
- 按钮文字："新增模板"
- 按钮样式：主要按钮（蓝色）
- 点击后：打开模板配置表单（新增模式）

**编辑模板：**
- 位置：每行操作列
- 按钮文字："编辑"
- 按钮样式：文字按钮
- 点击后：打开模板配置表单（编辑模式），回填数据

**删除模板：**
- 位置：每行操作列
- 按钮文字："删除"
- 按钮样式：文字按钮（危险色）
- 点击后：二次确认弹窗
- 确认提示："确定删除模板【{模板名称}】吗？删除后催员将无法继续使用此模板。"

**启用/禁用切换：**
- 位置：启用状态列
- 控件类型：开关（Switch）
- 切换后：立即生效，影响IM端模板可见性

### 2.2 模板配置表单

#### 2.2.1 表单布局

采用抽屉式（Drawer）或对话框（Dialog）展示，分为以下部分：
- 基础信息
- 分类维度
- 模板内容
- 状态配置

#### 2.2.2 基础信息

| 字段 | 类型 | 是否必填 | 说明 | 校验规则 |
|------|------|---------|------|----------|
| 模板名称 | 文本输入 | 必填 | 便于识别的模板名称 | 长度2-100字符，不可重复 |
| 模板类型 | 单选 | 必填 | 仅保留组织模板（personal 暂不开放新增） | 固定为 organization |
| 适用小组 | 多选下拉 | 必填 | 选择可使用此模板的小组（含案件队列） | 至少选择1个小组 |
| 按机构筛选小组 | 下拉选择 | 选填 | 仅用于筛选小组选项，不直接决定权限 | - |

**模板类型说明：**
- **组织模板（organization）**：选定小组的催员可见可用，通常用于标准化话术
- **个人模板（personal）**：暂不支持新增；已有数据沿用但前端不再提供创建/筛选入口

**适用小组说明：**
- 下拉选项：从当前甲方的小组列表中选择（含所属机构与队列名）
- 支持多选：可同时选择多个小组
- 默认值：为空（需显式选择）
- 快捷操作：提供"全选"、"清空"按钮（基于当前机构筛选范围）
- 权限控制：仅选定小组的催员可见；机构字段仅用于筛选小组选项
- 兼容性：保存时自动反推所属机构ID列表（agency_ids）以兼容旧接口

#### 2.2.3 渠道配置

| 字段 | 类型 | 是否必填 | 说明 | 校验规则 |
|------|------|---------|------|----------|
| 渠道类型 | 下拉选择 | 必填 | 选择消息发送渠道 | 枚举值：sms/rcs/waba/whatsapp/email/mobile_calendar |
| 供应商配置 | 动态列表 | 必填 | 配置此渠道下的供应商及其模板ID | 至少配置1个供应商 |

**渠道类型枚举：**
- **sms（短信）**：传统短信渠道
- **rcs（RCS）**：富媒体消息渠道
- **waba（WABA）**：WhatsApp Business API官方渠道
- **whatsapp（WhatsApp）**：WhatsApp非官方渠道
- **email（邮件）**：电子邮件渠道
- **mobile_calendar（手机日历）**：日历提醒渠道

**供应商配置说明：**
- 数据来源：从"甲方触达渠道管理"读取当前渠道已配置的供应商列表
- 配置项：
  - **供应商**：下拉选择，显示供应商名称
  - **供应商侧模板ID**：文本输入，对应供应商系统中的模板标识（字符串格式）
  - **优先级**：数字输入（1-99），数值越小优先级越高
- 操作：
  - **添加供应商**：支持为同一模板配置多个供应商的模板ID
  - **删除**：移除某个供应商配置
- 校验规则：
  - 至少配置1个供应商
  - 每个供应商必须选择供应商名称
  - 每个供应商必须填写模板ID（不能为空）
  - 每个供应商必须设置优先级（≥1）
  - 同一模板内优先级不能重复

**供应商优先级说明：**
- IM端发送时优先使用优先级高（数值小）的供应商
- 主供应商发送失败后，自动切换到次优先级供应商
- 示例：优先级1的供应商A失败 → 自动切换到优先级2的供应商B

#### 2.2.4 分类维度（对应IM端筛选器）

| 字段 | 类型 | 是否必填 | 选项 | 说明 |
|------|------|---------|------|------|
| 案件阶段 | 下拉选择 | 必填 | C、S0、S1-3、S3+ | 决定模板适用的催收阶段（来自队列管理） |
| 场景 | 下拉选择 | 必填 | greeting、reminder、strong | 模板的使用场景分类 |
| 时间点 | 下拉选择 | 必填 | morning、afternoon、evening | 建议使用的时间段 |

**说明：**
- 表单中选择案件阶段时，**不包含**"全部阶段"选项（因为必须指定具体阶段）
- "全部"选项仅用于列表页面的筛选器

**案件阶段枚举：**
- **C（催收前）**：客户尚未逾期，预防性提醒
- **S0（首次联系）**：首次与客户沟通
- **S1-3（初期）**：逾期初期，温和催收
- **S3+（后期）**：逾期后期，强度催收

**数据来源：**
- 当前版本使用固定枚举值（C、S0、S1-3、S3+）
- 未来可扩展为从队列管理（case_queues表）动态读取queue_code

**场景枚举：**
- **greeting（问候）**：问候、建立联系类话术
- **reminder（提醒）**：还款提醒、承诺确认类话术
- **strong（强度）**：强度催收、后果告知类话术

**时间点枚举：**
- **morning（上午）**：8:00-12:00建议使用
- **afternoon（下午）**：12:00-18:00建议使用
- **evening（晚上）**：18:00-22:00建议使用

#### 2.2.4 模板内容

| 字段 | 类型 | 是否必填 | 说明 |
|------|------|---------|------|
| 模板内容 | 多行文本框 | 必填 | 支持变量占位符的模板内容 |
| 可用变量 | 标签选择器 | 选填 | 从预置变量列表中选择 |
| 内容预览 | 只读文本 | - | 实时渲染变量替换效果 |

**模板内容编辑器：**
- 输入框类型：Textarea
- 最小行数：5行
- 最大字符数：1000字符（WhatsApp限制）
- 支持变量插入：点击变量标签自动插入到光标位置
- 实时字符计数：显示"已输入 X / 1000 字符"

**变量插入方式：**
1. 方式二：点击变量标签，自动插入到光标位置

**内容预览：**
- 显示位置：模板内容输入框下方
- 预览效果：将变量占位符替换为示例值
- 示例数据：
  - `{客户名}` → "张三"
  - `{贷款编号}` → "BTSK-200100"
  - `{逾期天数}` → "23"
  - `{到期日期}` → "2025-01-15"
  - `{贷款金额}` → "50,000"
  - `{应还金额}` → "10,529"
  - `{本金}` → "50,000"
  - `{罚息}` → "529"
  - `{产品名称}` → "快速贷"
  - `{App名称}` → "MegaPeso"

**预览示例：**
```
输入内容：
您好{客户名}，早上好！这是早晨BTSK，您在我们的贷款{贷款编号}已逾期{逾期天数}天，应还金额{应还金额}，请尽快安排还款。谢谢您的配合！

预览效果：
您好张三，早上好！这是早晨BTSK，您在我们的贷款BTSK-200100已逾期23天，应还金额10,529，请尽快安排还款。谢谢您的配合！
```

#### 2.2.5 状态配置

| 字段 | 类型 | 是否必填 | 默认值 | 说明 |
|------|------|---------|--------|------|
| 启用状态 | 开关 | 必填 | 启用 | 控制模板是否在IM端可见 |
| 排序权重 | 数字输入 | 选填 | 0 | 数值越小排序越靠前 |

**排序规则：**
- 主排序：sort_order 升序（数值小的在前）
- 次排序：created_at 降序（新创建的在前）

#### 2.2.6 表单操作按钮

**保存：**
- 按钮文字："保存"
- 按钮样式：主要按钮（蓝色）
- 点击后：
  1. 校验所有必填字段
  2. 校验模板内容长度
  3. 提交到后端API
  4. 成功后：显示成功提示，关闭表单，刷新列表
  5. 失败后：显示错误信息

**取消：**
- 按钮文字："取消"
- 按钮样式：普通按钮
- 点击后：
  1. 如果有未保存修改，二次确认："当前有未保存的修改，确定要离开吗？"
  2. 确认后关闭表单

### 2.3 变量系统设计

#### 2.3.1 预置变量列表

**数据来源**：变量值来自"甲方字段展示配置"中定义的标准字段，通过field_key映射获取实际值。

| 变量名 | 变量标识 | 字段标识(field_key) | 数据类型 | 示例值 | 说明 |
|--------|---------|-------------------|---------|--------|------|
| 客户名 | `{客户名}` | user_name | String | 张三 | 客户真实姓名 |
| 贷款编号 | `{贷款编号}` | case_code | String | BTSK-200100 | 案件编号 |
| 逾期天数 | `{逾期天数}` | overdue_days | Integer | 23 | 当前逾期天数 |
| 到期日期 | `{到期日期}` | due_date | Date | 2025-01-15 | 应还款日期 |
| 贷款金额 | `{贷款金额}` | loan_amount | Decimal | 50,000 | 原始贷款金额 |
| 应还金额 | `{应还金额}` | outstanding_amount | Decimal | 10,529 | 应还未还金额 |
| 本金 | `{本金}` | principal_amount | Decimal | 50,000 | 贷款本金 |
| 罚息 | `{罚息}` | late_fee | Decimal | 529 | 逾期罚息 |
| 产品名称 | `{产品名称}` | product_name | String | 快速贷 | 贷款产品名称 |
| App名称 | `{App名称}` | app_name | String | MegaPeso | 借款App名称 |

#### 2.3.2 变量替换规则

**前端替换时机：**
- IM端催员选择模板后，在发送前实时替换变量为案件实际数据
- 替换失败（变量值为空）时，保留原始占位符

**替换逻辑：**
```javascript
// 伪代码示例
function replaceVariables(templateContent, caseData) {
  let content = templateContent
  
  // 替换客户名
  content = content.replace(/{客户名}/g, caseData.user_name || '{客户名}')
  
  // 替换贷款编号
  content = content.replace(/{贷款编号}/g, caseData.case_code || '{贷款编号}')
  
  // 替换逾期天数
  content = content.replace(/{逾期天数}/g, caseData.overdue_days || '{逾期天数}')
  
  // 替换到期日期
  content = content.replace(/{到期日期}/g, caseData.due_date || '{到期日期}')
  
  // 替换贷款金额（需要格式化）
  const loanAmount = caseData.loan_amount ? formatCurrency(caseData.loan_amount) : '{贷款金额}'
  content = content.replace(/{贷款金额}/g, loanAmount)
  
  // 替换应还金额（需要格式化）
  const outstandingAmount = caseData.outstanding_amount ? formatCurrency(caseData.outstanding_amount) : '{应还金额}'
  content = content.replace(/{应还金额}/g, outstandingAmount)
  
  // 替换本金
  const principal = caseData.principal_amount ? formatCurrency(caseData.principal_amount) : '{本金}'
  content = content.replace(/{本金}/g, principal)
  
  // 替换罚息
  const lateFee = caseData.late_fee ? formatCurrency(caseData.late_fee) : '{罚息}'
  content = content.replace(/{罚息}/g, lateFee)
  
  // 替换产品名称
  content = content.replace(/{产品名称}/g, caseData.product_name || '{产品名称}')
  
  // 替换App名称
  content = content.replace(/{App名称}/g, caseData.app_name || '{App名称}')
  
  return content
}
```

**说明：**
- 所有变量值从案件数据中通过field_key获取
- 金额类字段（贷款金额、应还金额、本金、罚息）需要格式化处理
- 字段标识与"甲方字段展示配置"中的field_key保持一致

**金额格式化规则：**
- 整数部分：每三位添加逗号分隔
- 小数部分：保留2位小数（如果有）
- 示例：10529.5 → "10,529.50"

#### 2.3.3 变量扩展机制

**未来扩展变量时：**
1. 在后端配置新变量定义（名称、标识、数据类型）
2. 在IM端添加变量替换逻辑
3. 在控台端"可用变量"列表中自动显示
4. 无需修改模板配置界面代码

### 2.4 数据库设计

#### 2.4.1 message_templates 表（消息模板配置表）

```sql
CREATE TABLE message_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '甲方ID',
  
  -- 基础信息
  template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
  template_type ENUM('organization', 'personal') NOT NULL COMMENT '模板类型：organization-组织模板，personal-个人模板',
  team_ids JSON COMMENT '适用小组ID列表，如[1,2,3]，NULL表示全部小组',
  agency_ids JSON COMMENT '适用机构ID列表，如[1,2,3]，NULL表示全部机构（兼容字段，保存时按team_ids反推）',
  
  -- 渠道配置
  channel_type VARCHAR(50) NOT NULL COMMENT '渠道类型：sms/rcs/waba/whatsapp/email/mobile_calendar',
  supplier_template_mappings JSON COMMENT '供应商模板映射，如[{"supplierId":1,"templateId":"wa_001","priority":1}]',
  
  -- 分类维度（对应IM端筛选器）
  case_stage VARCHAR(20) NOT NULL COMMENT '案件阶段：C/S0/S1-3/S3+',
  scene VARCHAR(50) NOT NULL COMMENT '场景：greeting/reminder/strong',
  time_slot VARCHAR(20) NOT NULL COMMENT '时间点：morning/afternoon/evening',
  
  -- 模板内容
  content TEXT NOT NULL COMMENT '模板内容，支持变量占位符',
  variables JSON COMMENT '可用变量列表，如["客户名","贷款编号","逾期天数"]',
  
  -- 状态与统计
  is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  sort_order INT DEFAULT 0 COMMENT '排序权重，越小越靠前',
  usage_count INT DEFAULT 0 COMMENT '使用次数统计',
  
  -- 时间戳
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT COMMENT '创建人ID（催员ID）',
  updated_by BIGINT COMMENT '更新人ID（催员ID）',
  
  -- 索引
  INDEX idx_tenant_type (tenant_id, template_type),
  INDEX idx_channel (channel_type),
  INDEX idx_stage_scene (case_stage, scene),
  INDEX idx_enabled (is_enabled),
  INDEX idx_sort (sort_order, created_at),
  UNIQUE KEY uk_tenant_name (tenant_id, template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板配置表';
```

**字段说明：**

| 字段 | 类型 | 说明 | 业务规则 |
|------|------|------|----------|
| id | BIGINT | 主键ID | 自增 |
| tenant_id | BIGINT | 甲方ID | 数据隔离，必填 |
| template_name | VARCHAR(200) | 模板名称 | 同一甲方内不可重复 |
| template_type | ENUM | 模板类型 | organization/personal（当前仅开放organization） |
| team_ids | JSON | 适用小组ID列表 | 数组格式，如[1,2,3]，NULL表示全部小组 |
| agency_ids | JSON | 适用机构ID列表 | 兼容旧字段，按team_ids自动反推；NULL表示全部机构 |
| channel_type | VARCHAR(50) | 渠道类型 | sms/rcs/waba/whatsapp/email/mobile_calendar，必填 |
| supplier_template_mappings | JSON | 供应商模板映射 | 数组格式，如[{"supplierId":1,"supplierName":"供应商A","templateId":"wa_001","priority":1}]，至少1个 |
| case_stage | VARCHAR(20) | 案件阶段 | C/S0/S1-3/S3+ |
| scene | VARCHAR(50) | 场景 | greeting/reminder/strong |
| time_slot | VARCHAR(20) | 时间点 | morning/afternoon/evening |
| content | TEXT | 模板内容 | 最长1000字符 |
| variables | JSON | 变量列表 | 数组格式，如["客户名","贷款编号"] |
| is_enabled | TINYINT(1) | 启用状态 | 1-启用，0-禁用 |
| sort_order | INT | 排序权重 | 越小越靠前，默认0 |
| usage_count | INT | 使用次数 | 每次催员选择模板时+1 |
| created_by | BIGINT | 创建人ID | 个人模板权限控制依据 |

**team_ids字段说明：**
- **NULL**：表示适用于全部小组
- **[1,2,3]**：仅适用于小组ID为1、2、3
- **[]**（空数组）：不适用于任何小组（不推荐）

**agency_ids字段说明（兼容）**
- 由后端/前端根据team_ids自动反推去重后的机构ID集合
- 查询与授权以team_ids为准；agency_ids仅供兼容旧逻辑或统计展示

#### 2.4.2 索引说明

| 索引名 | 字段 | 类型 | 用途 |
|--------|------|------|------|
| PRIMARY | id | 主键 | 主键查询 |
| idx_tenant_type | tenant_id, template_type | 普通索引 | 按甲方和类型查询 |
| idx_stage_scene | case_stage, scene | 普通索引 | 按阶段和场景筛选 |
| idx_enabled | is_enabled | 普通索引 | 查询启用的模板 |
| idx_sort | sort_order, created_at | 普通索引 | 排序查询 |
| uk_tenant_name | tenant_id, template_name | 唯一索引 | 保证同甲方内模板名称唯一 |

#### 2.4.3 初始化数据（Mock数据示例）

```sql
-- 组织模板示例（基于小组）
INSERT INTO message_templates (tenant_id, template_name, template_type, team_ids, agency_ids, case_stage, scene, time_slot, content, variables, is_enabled, sort_order, created_by) VALUES
(1, '早安问候 + 还款提醒', 'organization', NULL, NULL, 'S0', 'greeting', 'morning', '您好{客户名}，早上好！这是早晨BTSK，您在我们的贷款{贷款编号}已逾期{逾期天数}天，应还金额{应还金额}，请尽快安排还款。谢谢您的配合！', '["客户名","贷款编号","逾期天数","应还金额"]', 1, 10, 1),

(1, '下午催款提醒', 'organization', '[3,4,5]', '[1,2,3]', 'S1-3', 'reminder', 'afternoon', '{客户名}您好，您的贷款{贷款编号}逾期已{逾期天数}天，未还金额{应还金额}，请今日内完成还款，如有困难请联系我们！', '["客户名","贷款编号","逾期天数","应还金额"]', 1, 20, 1),

(1, '晚间强度提醒', 'organization', '[3,4]', '[1,2]', 'S3+', 'strong', 'evening', '{客户名}，您已严重逾期{逾期天数}天，如不立即还款{应还金额}，我们将采取法律措施。请立即联系我们：{联系电话}', '["客户名","逾期天数","应还金额","联系电话"]', 1, 30, 1);

-- 个人模板示例（保留兼容，前端不提供新建入口）
INSERT INTO message_templates (tenant_id, template_name, template_type, team_ids, agency_ids, case_stage, scene, time_slot, content, variables, is_enabled, sort_order, created_by) VALUES
(1, '个人问候（上午）', 'personal', NULL, NULL, 'S0', 'greeting', 'morning', '您好，早上好！我是催款官小王，关于您的还款事宜想跟您沟通一下，现在方便吗？', '["催员姓名"]', 1, 100, 1001);
```

### 2.5 接口设计

#### 2.5.1 控台端接口（管理端）

**1. 获取模板列表**

```
GET /api/v1/console/message-templates
```

**请求参数：**
```json
{
  "page": 1,
  "pageSize": 20,
  "tenantId": 1,
  "templateType": "organization",  // 可选：当前仅支持organization
  "channelType": "whatsapp",       // 可选：sms/rcs/waba/whatsapp/email/mobile_calendar
  "teamId": 11,                    // 可选：按小组ID筛选
  "agencyId": 1,                   // 可选：按机构筛选小组（兼容）
  "caseStage": "S0",              // 可选：C/S0/S1-3/S3+
  "scene": "greeting",             // 可选：greeting/reminder/strong
  "timeSlot": "morning",           // 可选：morning/afternoon/evening
  "isEnabled": true,               // 可选：true/false
  "keyword": "问候"                // 可选：搜索关键词
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "pageSize": 20,
    "list": [
      {
        "id": 1,
        "tenantId": 1,
        "templateName": "早安问候 + 还款提醒",
        "templateType": "organization",
        "channelType": "whatsapp",
        "supplierTemplateMappings": [
          {
            "supplierId": 1,
            "supplierName": "WhatsApp供应商A",
            "templateId": "wa_greeting_morning_001",
            "priority": 1
          },
          {
            "supplierId": 2,
            "supplierName": "WhatsApp供应商B",
            "templateId": "wa_greeting_morning_002",
            "priority": 2
          }
        ],
        "teamIds": null,                 // null表示全部小组
        "teamNames": "全部小组",          // 用于列表显示
        "agencyIds": null,               // 兼容字段，按teamIds反推
        "agencyNames": "全部机构",        // 兼容显示
        "caseStage": "S0",
        "scene": "greeting",
        "timeSlot": "morning",
        "content": "您好{客户名}，早上好！...",
        "variables": ["客户名", "贷款编号", "逾期天数", "应还金额"],
        "isEnabled": true,
        "sortOrder": 10,
        "usageCount": 152,
        "createdAt": "2025-01-01 10:00:00",
        "updatedAt": "2025-01-10 15:30:00",
        "createdBy": 1,
        "createdByName": "管理员"
      }
    ]
  }
}
```

**2. 创建模板**

```
POST /api/v1/console/message-templates
```

**请求参数：**
```json
{
  "tenantId": 1,
  "templateName": "早安问候 + 还款提醒",
  "templateType": "organization",
  "channelType": "whatsapp",
  "supplierTemplateMappings": [
    {
      "supplierId": 1,
      "supplierName": "WhatsApp供应商A",
      "templateId": "wa_greeting_morning_001",
      "priority": 1
    },
    {
      "supplierId": 2,
      "supplierName": "WhatsApp供应商B",
      "templateId": "wa_greeting_morning_002",
      "priority": 2
    }
  ],
  "teamIds": [11,12],                // null-全部小组，[11,12]-指定小组
  "agencyIds": null,                 // 可选兼容字段，后端可自动按teamIds推导
  "caseStage": "S0",
  "scene": "greeting",
  "timeSlot": "morning",
  "content": "您好{客户名}，早上好！这是早晨BTSK...",
  "variables": ["客户名", "贷款编号", "逾期天数", "应还金额"],
  "isEnabled": true,
  "sortOrder": 10
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "templateName": "早安问候 + 还款提醒",
    ...
  }
}
```

**错误码：**
| HTTP状态码 | 业务错误码 | 错误信息 | 说明 |
|-----------|----------|---------|------|
| 400 | 400 | Template name already exists | 模板名称已存在 |
| 400 | 400 | Content exceeds maximum length | 内容超过最大长度（1000字符） |
| 400 | 400 | Invalid template type | 模板类型无效 |
| 400 | 400 | Invalid case stage | 案件阶段无效 |

**3. 更新模板**

```
PUT /api/v1/console/message-templates/{id}
```

**请求参数：** 同创建接口

**响应数据：** 同创建接口

**4. 删除模板**

```
DELETE /api/v1/console/message-templates/{id}
```

**请求参数：**
```json
{
  "id": 1,
  "tenantId": 1
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "Template deleted successfully",
  "data": null
}
```

**5. 切换启用状态**

```
PATCH /api/v1/console/message-templates/{id}/toggle
```

**请求参数：**
```json
{
  "id": 1,
  "tenantId": 1,
  "isEnabled": false
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isEnabled": false
  }
}
```

#### 2.5.2 IM端接口（催员端）

**1. 获取可用模板列表**

```
GET /api/v1/message-templates
```

**请求参数：**
```json
{
  "tenantId": 1,
  "collectorId": 1001,
  "agencyId": 1,                  // 催员所属机构ID（必填，用于过滤）
  "teamId": 11,                   // 催员所属小组ID（可选，优先按小组过滤）
  "caseStage": "S0",              // 可选
  "templateType": "organization", // 可选，当前仅organization
  "scene": "greeting",            // 可选
  "timeSlot": "morning"           // 可选
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "早安问候 + 还款提醒",
      "type": "organization",
      "stage": "S0",
      "scene": "greeting",
      "timeSlot": "morning",
      "channelType": "whatsapp",
      "supplierTemplateMappings": [
        {
          "supplierId": 1,
          "supplierName": "WhatsApp供应商A",
          "templateId": "wa_greeting_morning_001",
          "priority": 1
        },
        {
          "supplierId": 2,
          "supplierName": "WhatsApp供应商B",
          "templateId": "wa_greeting_morning_002",
          "priority": 2
        }
      ],
      "content": "您好{客户名}，早上好！这是早晨BTSK，您在我们的贷款{贷款编号}已逾期{逾期天数}天，应还金额{应还金额}，请尽快安排还款。谢谢您的配合！",
      "variables": ["客户名", "贷款编号", "逾期天数", "应还金额"]
    }
  ]
}
```

**业务规则：**
- 仅返回 `is_enabled = 1` 的模板
- **组织模板（organization）**：
  - 如果 `team_ids = NULL`：全员可见
  - 如果 `team_ids = [1,2,3]`：仅小组ID在列表中的催员可见
  - 查询条件：`(team_ids IS NULL OR JSON_CONTAINS(team_ids, '[催员所属小组ID]'))`
  - agency_ids 仅用于兼容旧逻辑，不作为主过滤字段
- **个人模板（personal）**：仅创建人（created_by = collectorId）可见
- 按 `sort_order` 升序 + `created_at` 降序排序

**2. 获取可用变量列表**

```
GET /api/v1/message-templates/variables
```

**请求参数：** 无

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "name": "客户名",
      "key": "{客户名}",
      "type": "string",
      "example": "张三",
      "description": "客户真实姓名"
    },
    {
      "name": "贷款编号",
      "key": "{贷款编号}",
      "type": "string",
      "example": "BTSK-200100",
      "description": "案件编号/贷款ID"
    },
    {
      "name": "逾期天数",
      "key": "{逾期天数}",
      "type": "number",
      "example": "23",
      "description": "当前逾期天数"
    },
    {
      "name": "应还金额",
      "key": "{应还金额}",
      "type": "number",
      "example": "10,529",
      "description": "应还款金额（含格式化）"
    }
  ]
}
```

**3. 记录模板使用（可选）**

```
POST /api/v1/message-templates/{id}/usage
```

**请求参数：**
```json
{
  "templateId": 1,
  "collectorId": 1001,
  "caseId": 12345,
  "contactId": 67890
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务逻辑：**
- 每次催员选择模板并发送消息时调用
- 更新 `usage_count` 字段：`usage_count = usage_count + 1`
- 可用于统计分析

## 3. 业务规则与边界

### 3.1 核心业务规则

**权限隔离规则：**
- 数据按甲方（tenant_id）隔离
- 管理员只能管理本甲方的模板
- 个人模板仅创建人可见可用

**模板类型规则：**
- **组织模板（organization）**：
  - 可见范围由"适用小组"控制
  - 如果选择全部小组（team_ids = NULL）：全甲方催员可见
  - 如果选择部分小组（team_ids = [1,2,3]）：仅指定小组的催员可见
  - 通常用于标准化话术
  - 需要管理员权限创建
- **个人模板（personal）**：
  - 暂不开放创建入口；兼容历史数据仅在后端保留
  - 可见范围：仅创建人可见（若存在旧数据）

**小组范围控制规则：**
- **适用对象**：组织模板
- **权限判断**：IM端按催员所属小组ID过滤，team_ids 为主判定字段
- **全部小组**：team_ids = NULL 或 空数组时，表示全部小组可见
- **部分小组**：team_ids = [1,2,3] 时，仅小组ID在列表中的催员可见
- **机构字段**：agency_ids 为兼容字段，由 team_ids 自动反推

**渠道和供应商配置规则：**
- **渠道必选**：每个模板必须指定一个渠道类型（channel_type）
- **供应商多配置**：一个模板可以配置多个供应商的模板ID（一对多关系）
- **供应商来源**：从"甲方触达渠道管理"中读取当前渠道已配置的供应商列表
- **优先级设置**：
  - 每个供应商配置必须设置优先级（1-99，数值越小优先级越高）
  - 同一模板内优先级不能重复
  - 至少配置1个供应商
- **发送逻辑**：
  - IM端发送时，优先使用优先级最高（数值最小）的供应商
  - 主供应商发送失败后，自动切换到次优先级供应商
  - 按优先级顺序依次尝试，直到成功或全部失败
- **模板ID格式**：供应商侧模板ID为字符串类型，由各供应商系统定义

**内容限制规则：**
- 模板内容最大长度：1000字符（WhatsApp限制）
- 模板名称长度：2-100字符
- 同一甲方内模板名称不可重复

**启用状态规则：**
- 仅启用状态（is_enabled = 1）的模板在IM端可见
- 禁用模板（is_enabled = 0）不影响已发送的消息
- 禁用后立即生效

**排序规则：**
- 主排序：sort_order 升序（数值小的在前）
- 次排序：created_at 降序（新创建的在前）
- IM端展示时按此规则排序

**变量替换规则：**
- 变量格式：花括号包裹，如 `{客户名}`
- 替换时机：IM端催员选择模板后，发送前实时替换
- 替换失败（变量值为空）时：保留原始占位符
- 不支持嵌套变量

### 3.2 范围边界

**本次需求范围内：**
- ✅ 控台端模板配置管理（CRUD操作）
- ✅ 多渠道支持（短信/RCS/WABA/WhatsApp/邮件/手机日历）
- ✅ 供应商模板映射（一模板多供应商，按优先级自动切换）
- ✅ 小组范围控制（选择适用小组，精确到案件队列）
- ✅ 四维度分类（案件阶段、类型、场景、时间点）
- ✅ 变量系统（预置10个常用变量）
- ✅ 启用/禁用控制
- ✅ 使用次数统计
- ✅ 权限隔离（按甲方、按小组、按创建人）
- ✅ IM端模板选择（前端已实现）
- ✅ 变量替换（前端实现）

**本次需求范围外（待实现）：**
- ⏳ 模板审核流程（管理员审核通过后启用）
- ⏳ 模板版本管理（保留历史版本）
- ⏳ 模板效果分析（发送量、回复率、还款率、按供应商统计）
- ⏳ 模板导入/导出（批量管理）
- ⏳ 多语言支持（中文、英文、西班牙语等）
- ⏳ 富文本内容（图片、链接、按钮）
- ⏳ 动态变量（计算型变量，如 `{剩余天数}` = 还款日期 - 今天）
- ⏳ 供应商智能切换策略（根据成功率、响应时间动态调整优先级）
- ❌ WhatsApp官方模板审核（WhatsApp Business API）

## 4. 配置项与运营开关

### 4.1 系统配置

| 配置项 | 配置路径 | 默认值 | 说明 |
|--------|---------|--------|------|
| template.maxLength | application.yml | 1000 | 模板内容最大长度（字符） |
| template.nameMaxLength | application.yml | 100 | 模板名称最大长度（字符） |
| template.pageSize | application.yml | 20 | 列表分页大小 |
| template.personalQuota | application.yml | 50 | 每个催员最多创建个人模板数 |

### 4.2 运营开关

| 开关名称 | 默认值 | 说明 |
|---------|--------|------|
| template.enabled | true | 是否启用模板功能 |
| template.allowPersonalCreate | false | 是否允许催员创建个人模板 |
| template.showUsageCount | true | 是否显示使用次数 |
| template.enableAudit | false | 是否启用模板审核流程 |

## 5. 交互与信息展示（UX & UI Brief）

### 5.1 列表页面交互

**筛选器交互：**
- 筛选器默认收起（可配置）
- 点击"筛选"按钮展开/收起
- 选择筛选条件后立即刷新列表
- 清空按钮：一键清空所有筛选条件

**表格交互：**
- 支持按列排序（模板名称、使用次数、创建时间）
- 鼠标悬停行：高亮显示
- 模板内容预览：鼠标悬停显示完整内容
- 分页：底部居中显示，支持跳转指定页

**操作按钮交互：**
- 编辑按钮：打开抽屉，回填数据
- 删除按钮：二次确认弹窗
- 启用/禁用开关：点击立即切换，显示加载状态

### 5.2 表单交互

**表单布局：**
- 采用抽屉式（Drawer）展示，从右侧滑出
- 宽度：600px
- 分为4个区域：基础信息、分类维度、模板内容、状态配置

**实时校验：**
- 模板名称：失焦时校验是否重复
- 模板内容：输入时显示字符计数
- 必填项：显示红色星号

**变量插入交互：**
- 点击变量标签：插入到光标位置
- 快捷键 `@`：触发变量选择下拉框
- 变量高亮：编辑器中变量显示为蓝色标签

**内容预览交互：**
- 实时预览：输入时自动刷新预览
- 预览区域：灰色背景框，只读文本
- 示例数据：使用固定示例值

### 5.3 样式规范

**颜色规范：**
- 主色调：#409EFF（Element Plus蓝）
- 成功色：#67C23A（启用状态）
- 危险色：#F56C6C（删除按钮）
- 警告色：#E6A23C
- 文字色：#303133（主要文字）
- 次要文字色：#909399

**字体规范：**
- 主要字体：14px
- 标题字体：16px，加粗
- 表格字体：13px
- 提示文字：12px

**间距规范：**
- 页面边距：20px
- 卡片间距：16px
- 表单项间距：20px
- 按钮间距：8px

## 6. 非功能性要求（Non-Functional Requirements）

### 6.1 性能要求

**响应时间：**
- 列表查询：≤ 500ms
- 创建/更新模板：≤ 1s
- 删除模板：≤ 500ms
- IM端获取模板列表：≤ 300ms

**并发支持：**
- 支持100+ 管理员同时操作
- 支持1000+ 催员同时查询模板

**数据量支持：**
- 单个甲方支持1000+ 模板
- 全系统支持10万+ 模板记录

### 6.2 可用性要求

**SLA：**
- 服务可用性：≥ 99.9%
- 数据持久化：≥ 99.99%

**降级策略：**
- 模板服务异常时：IM端允许手动输入消息
- 数据库异常时：返回缓存数据（如果有）

### 6.3 安全要求

**权限控制：**
- 接口级别鉴权：所有接口需要登录Token
- 数据级别隔离：按tenant_id隔离
- 操作级别审计：记录创建人、更新人

**数据脱敏：**
- 日志中不记录模板完整内容
- 错误信息不暴露敏感字段

### 6.4 扩展性要求

**变量扩展：**
- 支持动态添加新变量
- 无需修改表结构

**渠道扩展：**
- 预留渠道字段（channel）
- 未来支持SMS、RCS等渠道

## 7. 日志埋点与监控告警（Logging, Metrics & Alerting）

### 7.1 关键日志

**操作日志：**
- 创建模板：记录创建人、模板名称、内容摘要
- 更新模板：记录更新人、更新字段、更新前后值
- 删除模板：记录删除人、模板信息
- 启用/禁用：记录操作人、模板ID、操作类型

**错误日志：**
- 模板创建失败：记录请求参数、错误原因
- 模板查询失败：记录查询条件、错误堆栈
- 变量替换失败：记录模板ID、缺失变量

### 7.2 监控指标

**业务指标：**
- 模板总数：按甲方、类型统计
- 模板使用量：按模板ID统计每日使用次数
- 模板创建量：每日新增模板数
- 模板删除量：每日删除模板数

**性能指标：**
- 接口响应时间：P50、P95、P99
- 接口成功率：成功请求数 / 总请求数
- 数据库查询耗时：慢查询统计

### 7.3 告警规则

| 告警项 | 阈值 | 级别 | 处理方式 |
|--------|------|------|----------|
| 接口成功率 | < 95% | 严重 | 立即通知开发团队 |
| 接口响应时间 | P95 > 2s | 警告 | 发送告警通知 |
| 数据库查询耗时 | > 5s | 警告 | 记录慢查询日志 |
| 模板创建失败率 | > 10% | 警告 | 检查数据库连接 |

## 8. 测试策略与验收标准（Test Plan & Acceptance Criteria）

### 8.1 测试用例

**功能测试：**
- 创建组织模板：成功创建，列表显示
- 渠道选择：选择渠道后自动加载对应供应商列表
- 供应商配置：可添加/删除多个供应商，配置模板ID和优先级
- 编辑模板：更新成功，内容变更，供应商配置正确回填
- 删除模板：二次确认，删除成功
- 启用/禁用模板：状态切换，IM端可见性变化
- 筛选功能：按各维度（含渠道）筛选，结果正确
- 搜索功能：关键词匹配，结果正确
- 变量替换：替换成功，格式正确
- 适用小组：按小组选中/全选/清空符合预期

**边界测试：**
- 模板内容长度：超过1000字符，提示错误
- 模板名称重复：同甲方内重复，提示错误
- 供应商配置不完整：未选择供应商/未填写模板ID，提示错误
- 供应商优先级重复：多个供应商使用相同优先级，提示错误
- 未配置供应商：至少需要1个供应商，提示错误
- 空变量值：替换失败，保留占位符

**性能测试：**
- 1000条模板列表查询：响应时间 < 500ms
- 并发创建模板：100并发，成功率 > 95%

### 8.2 验收标准

**核心验收标准：**
1. ✅ 控台端可创建、编辑、删除模板
2. ✅ 支持渠道选择和供应商模板ID配置
3. ✅ 一个模板可配置多个供应商（一对多）
4. ✅ 供应商优先级配置正常，不允许重复
5. ✅ 支持五维度分类和筛选（案件阶段、类型、渠道、场景、时间点）
6. ✅ 支持变量系统和内容预览
7. ✅ IM端可获取启用的模板列表（含供应商配置）
8. ✅ 变量替换功能正常
9. ✅ 权限隔离正常（按甲方、按小组）
10. ✅ 接口响应时间符合要求
11. ✅ 数据持久化正常

## 9. 发布计划与回滚预案（Release Plan & Rollback）

### 9.1 发布策略

**分阶段发布：**
1. **阶段1**：数据库表创建（无业务影响）
2. **阶段2**：后端接口上线（灰度10%流量）
3. **阶段3**：控台端页面上线（管理员可见）
4. **阶段4**：IM端集成（催员可用）
5. **阶段5**：全量发布

**发布时间：**
- 建议时间：工作日晚上22:00-24:00
- 避免时间：业务高峰期（9:00-18:00）

### 9.2 回滚预案

**回滚触发条件：**
- 接口成功率 < 90%
- 数据库异常导致无法创建模板
- IM端无法获取模板列表
- 变量替换功能异常

**回滚步骤：**
1. 立即切换开关：`template.enabled = false`
2. IM端自动降级：允许手动输入消息
3. 回滚代码：切换到上一个稳定版本
4. 数据库回滚：如需要，恢复快照

**回滚责任人：**
- 技术负责人：负责决策是否回滚
- 后端负责人：负责接口回滚
- 前端负责人：负责页面回滚
- DBA：负责数据库回滚

---

**文档版本**：1.0.2  
**最后更新**：2025-12-10  
**文档作者**：CCO产品团队

