# 案件列表字段展示配置 PRD

## 版本记录

| 版本号 | 日期 | 作者 | 变更说明 |
|--------|------|------|---------|
| v2.0.0 | 2025-12-08 | 产品大象 | 新增版本管理功能、优化必显字段逻辑 |
| v1.0.0 | 2025-12-07 | 产品大象 | 初始版本，基础展示配置功能 |

---

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

案件列表字段展示配置是CCO管理控台的核心功能之一，用于配置控台和催员端案件列表页面的字段展示方式。该功能基于"案件列表字段映射配置"已完成匹配并保存的生效版本，允许管理员对每个字段的显示属性（宽度、颜色、筛选、范围检索等）进行精细化配置，并通过版本管理机制确保配置变更的可追溯性和可恢复性。

**业务痛点**：
- 控台和催员端案件列表字段展示需求差异大，需要独立配置机制
- 字段展示属性（宽度、颜色、筛选等）配置复杂，需要直观的配置界面
- 配置变更频繁，缺乏版本管理和变更追溯能力
- 字段来源于映射配置，需要明确展示基于哪个映射版本
- 配置调整需要立即生效并保留历史版本供回滚

**预期影响的核心指标**：
- 字段配置效率：单个字段展示属性配置完成时间≤30秒
- 配置准确率：字段展示配置准确率≥99.5%
- 版本管理效率：版本保存和恢复操作时间≤5秒
- 用户体验：字段展示配置对用户体验提升≥25%
- 用户满意度：管理员对配置功能满意度≥95%

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：配置控台案件列表字段展示**
- **入口**：管理控台菜单 → 字段配置 → 案件列表字段配置
- **触发时机**：需要配置控台案件列表页面的字段展示时
- **所在页面**：`/field-config/list` → 选择场景"控台案件列表"
- **流程节点**：查看当前映射版本 → 选择场景 → 配置字段属性 → 保存为新版本

**场景2：配置催员端案件列表字段展示**
- **入口**：管理控台菜单 → 字段配置 → 案件列表字段配置
- **触发时机**：需要配置催员端案件列表页面的字段展示时
- **所在页面**：`/field-config/list` → 选择场景"IM端案件列表"
- **流程节点**：查看当前映射版本 → 选择场景 → 配置字段属性 → 保存为新版本

**场景3：调整字段显示顺序**
- **入口**：案件列表字段配置页面 → 拖拽字段行
- **触发时机**：需要调整字段在列表中的显示顺序时
- **所在页面**：字段展示配置页面
- **流程节点**：拖拽字段行 → 查看新排序 → 保存为新版本

**场景4：配置字段可筛选和范围检索**
- **入口**：字段展示配置页面 → 切换字段行的开关
- **触发时机**：需要启用/禁用字段的筛选或范围检索功能时
- **所在页面**：字段展示配置页面（表格内联编辑）
- **流程节点**：切换开关 → 查看变更 → 保存为新版本

**场景5：查看和恢复历史版本**
- **入口**：字段展示配置页面 → 点击"版本历史"按钮
- **触发时机**：需要查看配置变更记录或恢复历史版本时
- **所在页面**：版本历史抽屉
- **流程节点**：查看版本列表 → 选择版本 → 恢复此版本 → 确认

**场景6：基于新映射版本刷新配置**
- **入口**：字段展示配置页面 → 映射配置更新后
- **触发时机**：案件列表字段映射配置保存新版本并激活后
- **所在页面**：字段展示配置页面
- **流程节点**：查看映射版本提示 → 刷新页面 → 自动加载最新映射版本的字段 → 调整展示属性 → 保存为新版本

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用频率 |
|---------|---------|---------|---------|
| 超级管理员 | SuperAdmin | 配置所有甲方的字段展示规则，管理版本 | 高 |
| 甲方管理员 | TenantAdmin | 配置本甲方的字段展示规则，管理版本 | 高 |
其他角色不可查看和操作

---

### 3. 关键业务流程（Business Flow）

#### 3.1 字段展示配置主流程

```text
管理员进入案件列表字段配置页面
    ↓
系统显示当前基于的映射配置版本（版本号、来源、拉取时间）
    ↓
选择场景类型（admin_case_detail/collector_case_detail）
    ↓
系统加载字段列表：
  - 优先加载本地激活的展示配置版本
  - 如无本地版本，则从映射配置最新版本获取字段列表
  - 如仍无数据，则使用内置Mock字段
    ↓
显示字段配置表格（字段名称、类型、枚举值、宽度、颜色、开关等）
    ↓
支持操作：
  - 编辑字段属性（显示宽度、颜色、开关）
  - 拖拽排序
  - 保存为新版本
  - 查看版本历史
  - 恢复历史版本
```text

#### 3.2 保存为新版本流程

```text
管理员修改字段配置（宽度、颜色、开关、排序等）
    ↓
点击"保存为新版本"按钮
    ↓
弹出保存对话框：
  - 显示当前配置的字段数量
  - 输入版本备注（可选，建议填写）
    ↓
点击"确定保存"
    ↓
系统执行保存：
  - 生成新版本号（自增）
  - 记录操作人、时间
  - 将当前配置保存为JSON快照
  - 自动激活新版本
  - 写入本地版本文件（~/.cco-storage/case-list-display-versions/{tenantId}_{sceneType}.json）
    ↓
显示保存成功提示（含版本号）
    ↓
刷新页面，显示最新版本配置
```

#### 3.3 查看版本历史流程

```text
管理员点击"版本历史"按钮
    ↓
打开版本历史抽屉（右侧滑出）
    ↓
系统加载版本历史列表：
  - 版本号
  - 保存时间
  - 操作人
  - 备注说明
  - 字段数量
  - 操作按钮（恢复此版本）
    ↓
列表按版本号倒序显示（最新版本在最上面）
    ↓
支持操作：
  - 恢复某个历史版本
  - 关闭抽屉
```text

#### 3.4 恢复历史版本流程

```text
管理员在版本历史抽屉中选择某个版本
    ↓
点击"恢复此版本"按钮
    ↓
系统弹出确认对话框：
  - 显示版本号和保存时间
  - 提示：恢复后当前配置将被替换，是否继续？
    ↓
[取消] → 返回版本历史抽屉
    ↓
[确认] → 系统执行恢复：
  - 将指定版本设为激活版本
  - 从版本快照恢复字段配置
  - 刷新页面显示
    ↓
显示恢复成功提示
    ↓
关闭版本历史抽屉
    ↓
刷新字段配置列表（显示恢复后的配置）
```

#### 3.5 编辑字段属性流程

```text
管理员点击字段行的"编辑"按钮
    ↓
打开编辑字段配置弹窗（预填充当前配置）
    ↓
配置基本信息Tab：
  - 查看场景类型（只读）
  - 查看字段标识（只读）
  - 查看字段类型（只读）
  - 修改字段名称（可选）
  - 修改显示宽度（0=自动）
    ↓
配置样式Tab（可选）：
  - 选择颜色类型（普通/红/黄/绿）
  - 配置颜色规则（条件表达式）
  - 配置格式化规则（日期/数字/货币等）
    ↓
配置检索Tab（可选）：
  - 是否可筛选（仅Enum类型）
  - 是否支持范围检索（仅Integer/Number/Decimal/Date/Datetime类型）
    ↓
点击"确定"保存
    ↓
字段配置更新到页面表格
    ↓
提示：修改后需点击"保存为新版本"才会正式生效
```

#### 3.6 拖拽排序流程

```text
管理员拖拽字段行的拖拽图标
    ↓
系统实时更新排序
    ↓
自动重新计算所有字段的sort_order
    ↓
表格顺序实时更新
    ↓
提示：拖拽成功，请点击"保存为新版本"保存更改
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 场景类型规则

**支持的场景类型**：
- `admin_case_detail`：控台案件列表（用于控台案件列表页面展示）
- `collector_case_detail`：IM端案件列表（用于催员端案件列表页面展示）

**场景说明**：
- 控台和催员端的列表字段可能不同，需要独立配置
- 每个场景的配置相互独立，互不影响
- 场景类型命名保留了历史的`_detail`后缀，但实际用于列表页面配置

**范围边界**：
- ✅ 支持2种场景类型的独立配置
- ✅ 支持场景切换查看不同配置
- ❌ 不支持场景配置复制（已移除）
- ❌ 不支持自定义场景类型

#### 4.2 字段来源规则

**字段数据来源优先级**：

1. **本地激活版本（优先级最高）**
   - 位置：本地文件 `~/.cco-storage/case-list-display-versions/{tenantId}_{sceneType}.json`
   - 条件：存在文件且有激活版本（activeVersion不为null）
   - 内容：管理员保存的展示配置版本快照

2. **映射配置最新版本（次优先级）**
   - 位置：`tenant_field_uploads` 表（scene='list'，is_active=true）
   - 条件：映射配置已完成匹配并保存生效版本
   - 内容：从映射配置的fields字段转换为展示配置
   - 转换规则：
     - 保留字段基本信息（field_key、field_name、field_type等）
     - 补充展示配置默认值（宽度120px、颜色normal、筛选和范围检索默认开启）

3. **内置Mock数据（兜底）**
   - 位置：后端代码内置
   - 条件：前两者都不存在时
   - 内容：系统预设的基础字段配置（12个常用字段）

**字段匹配规则**：
- 只有在"案件列表字段映射配置"中完成匹配的字段才会出现在展示配置中
- 未匹配的甲方字段不会显示在展示配置列表中
- 如果映射配置新增字段，刷新页面后会自动同步到展示配置

**范围边界**：
- ✅ 支持从映射配置自动获取字段列表
- ✅ 支持本地版本覆盖映射配置
- ✅ 支持内置Mock兜底
- ❌ 不支持手动添加字段（字段必须来自映射配置）
- ❌ 不支持删除字段（字段列表固定）

#### 4.3 版本控制规则

**版本生成规则**：
- 版本号：整数，从1开始自增
- 版本快照：包含完整的字段配置列表（JSON格式）
- 版本元信息：
  - `version`：版本号
  - `saved_at`：保存时间（ISO格式）
  - `operator`：操作人（当前登录用户）
  - `note`：版本备注说明（可选）
  - `configs`：字段配置快照（数组）

**版本激活规则**：
- 同一时刻只有一个版本处于激活状态（activeVersion）
- 保存新版本时自动激活该版本
- 恢复历史版本时将该版本设为激活版本
- 激活版本的配置用于页面展示

**版本存储规则**：
- 存储位置：本地文件系统
- 文件路径：`~/.cco-storage/case-list-display-versions/{tenantId}_{sceneType}.json`
- 文件格式：
```json
{
  "versions": [
    {
      "version": 1,
      "saved_at": "2025-12-08T10:30:00",
      "operator": "admin",
      "note": "初始配置",
      "configs": [
        {
          "field_key": "case_id",
          "field_name": "案件编号",
          "display_width": 120,
          "color_type": "normal",
          "is_filterable": true,
          "is_range_searchable": true,
          ...
        }
      ]
    }
  ],
  "activeVersion": 1
}
```

**版本历史查看规则**：
- 版本列表按版本号倒序显示（最新版本在最上面）
- 显示版本号、保存时间、操作人、备注、字段数量
- 支持恢复任意历史版本
- 恢复版本需要二次确认

**范围边界**：
- ✅ 支持版本自动递增
- ✅ 支持版本快照和恢复
- ✅ 支持版本历史查看
- ✅ 支持版本备注说明
- ❌ 不支持版本删除
- ❌ 不支持版本对比（未来可扩展）
- ❌ 不支持版本回滚限制（任意版本都可恢复）

#### 4.4 字段配置属性规则

**可配置属性清单**：

1. **基本属性**（所有字段）
   - `field_key`：字段标识（只读，来自映射配置）
   - `field_name`：字段名称（可编辑）
   - `field_data_type`：字段类型（只读，来自映射配置）
   - `field_source`：字段来源（只读，standard/custom）
   - `enum_options`：枚举值（只读，仅Enum类型显示）
   - `sort_order`：排序顺序（拖拽或手动设置）

2. **显示属性**（所有字段）
   - `display_width`：显示宽度（像素）
     - 范围：0-500
     - 0表示自动宽度
     - 默认值：120px
   - `color_type`：颜色类型
     - 选项：normal（普通）、red（红色）、yellow（黄色）、green（绿色）
     - 默认值：normal

3. **检索属性**（按字段类型）
   - `is_filterable`：是否可筛选（开关）
     - 适用类型：Enum（枚举类型）
     - 默认值：true（开启）
     - 说明：开启后字段在案件列表可下拉筛选

   - `is_range_searchable`：是否支持范围检索（开关）
     - 适用类型：Integer、Number、Decimal、Date、Datetime
     - 默认值：true（开启）
     - 说明：开启后字段支持最小值-最大值范围检索

**字段类型与检索支持对照表**：

| 字段类型 | 可筛选(is_filterable) | 范围检索(is_range_searchable) | 说明 |
|---------|---------------------|----------------------------|------|
| String | ❌ 不支持 | ❌ 不支持 | 文本字段使用搜索功能 |
| Enum | ✅ 支持 | ❌ 不支持 | 下拉筛选 |
| Integer | ❌ 不支持 | ✅ 支持 | 数值范围 |
| Number | ❌ 不支持 | ✅ 支持 | 数值范围 |
| Decimal | ❌ 不支持 | ✅ 支持 | 数值范围 |
| Date | ❌ 不支持 | ✅ 支持 | 日期范围 |
| Datetime | ❌ 不支持 | ✅ 支持 | 日期时间范围 |

**范围边界**：
- ✅ 支持7种字段类型的配置
- ✅ 支持按字段类型自动判断可配置属性
- ✅ 支持开关控制检索功能
- ❌ 不支持自定义颜色
- ❌ 不支持颜色规则表达式（已简化）
- ❌ 不支持隐藏规则（已移除）
- ❌ 不支持格式化规则（已简化）

#### 4.5 映射版本关联规则

**版本信息展示规则**：
- 页面顶部显示当前基于的映射配置版本
- 显示内容：
  - 版本号：`v0`（Mock）或 `v1`、`v2`等（上传版本）
  - 来源：`内置Mock` 或 `上传版本`
  - 拉取时间：当前时间（ISO格式）

**版本更新规则**：
- 映射配置保存新版本并激活后，展示配置不会自动同步
- 需要管理员手动刷新页面或切换场景来加载最新映射版本
- 加载最新映射版本后，字段列表自动更新，但展示属性保持默认值
- 管理员需要重新配置展示属性并保存为新版本

**版本冲突处理**：
- 如果本地有激活的展示配置版本，优先使用本地版本
- 如果映射配置更新后字段有增减，本地版本不会自动同步
- 建议管理员定期检查映射版本，确保展示配置与映射配置一致

**范围边界**：
- ✅ 支持映射版本信息展示
- ✅ 支持手动刷新同步映射版本
- ❌ 不支持映射版本自动同步
- ❌ 不支持映射版本变更提醒

#### 4.6 必显字段规则

**必显字段定义**：
- 必显字段（is_required=true）：案件列表中必须展示的字段，不可隐藏
- 必显字段清单（控台案件列表，共7个）：
  - `case_code`：案件编号
  - `user_name`：客户姓名
  - `loan_amount`：贷款金额
  - `outstanding_amount`：未还金额
  - `overdue_days`：逾期天数
  - `case_status`：案件状态
  - `due_date`：到期日期

**范围边界**：
- ✅ 支持必显字段标识
- ✅ 必显字段不可删除
- ❌ 必显字段列表不可自定义

---

## 二、功能设计（Feature Design）

### 1. 整体功能架构

```text
案件列表字段展示配置
├── 版本信息显示区
│   ├── 映射配置版本号（基于映射配置）
│   ├── 数据来源标识（上传版本/内置Mock）
│   └── 拉取时间
├── 操作按钮区
│   ├── 保存为新版本
│   └── 版本历史
├── 场景选择区
│   └── 场景类型下拉（admin_case_detail/collector_case_detail）
├── 字段配置表格区
│   ├── 拖拽列（拖拽图标）
│   ├── 序号列
│   ├── 字段名称列（标签显示）
│   ├── 字段标识列
│   ├── 字段类型列（标签显示）
│   ├── 枚举值列（仅Enum类型显示）
│   ├── 映射类型列（标准字段/自定义字段）
│   ├── 显示宽度列（数字输入框，0=自动）
│   ├── 颜色列（下拉选择）
│   ├── 可筛选列（开关，仅Enum类型）
│   ├── 范围检索列（开关，仅数字/日期类型）
│   └── 操作列（编辑按钮）
├── 编辑字段弹窗
│   ├── 基本信息Tab
│   ├── 样式配置Tab
│   └── 检索配置Tab
└── 版本历史抽屉
    ├── 版本列表表格
    └── 恢复版本按钮
```

### 2. 页面结构设计

#### 2.1 主页面布局

```text
┌─────────────────────────────────────────────────────────────────┐
│ 案件列表字段配置                                                  │
│ 【基于映射配置：v5】【来源：上传版本】拉取时间：2025-12-08 10:30:00 │
│                                   【保存为新版本】【版本历史】     │
├─────────────────────────────────────────────────────────────────┤
│ 场景类型：[控台案件列表 ▼]                                       │
├─────────────────────────────────────────────────────────────────┤
│ 拖拽 | 序号 | 字段名称 | 字段标识 | 字段类型 | 枚举值 | 映射类型 │
│      |      |          |          |          |        | 显示宽度 │
│      |      |          |          |          |        | 颜色     │
│      |      |          |          |          |        | 可筛选   │
│      |      |          |          |          |        | 范围检索 │
│      |      |          |          |          |        | 操作     │
├─────────────────────────────────────────────────────────────────┤
│ :::  |  1   | 案件编号  | case_id  | String  |   -    | 标准字段 │
│      |      |          |          |          |        | [120]    │
│      |      |          |          |          |        | [普通▼]  │
│      |      |          |          |          |        | -        │
│      |      |          |          |          |        | -        │
│      |      |          |          |          |        | [编辑]   │
├─────────────────────────────────────────────────────────────────┤
│ :::  |  2   | 案件状态  | case_status | Enum | 待分配, | 标准字段│
│      |      |          |          |          | 催收中, |          │
│      |      |          |          |          | 已完成  |          │
│      |      |          |          |          |        | [120]    │
│      |      |          |          |          |        | [普通▼]  │
│      |      |          |          |          |        | [○]开    │
│      |      |          |          |          |        | -        │
│      |      |          |          |          |        | [编辑]   │
├─────────────────────────────────────────────────────────────────┤
│ :::  |  3   | 逾期天数  | overdue_days | Integer | -  | 标准字段│
│      |      |          |          |          |        | [120]    │
│      |      |          |          |          |        | [普通▼]  │
│      |      |          |          |          |        | -        │
│      |      |          |          |          |        | [○]开    │
│      |      |          |          |          |        | [编辑]   │
└─────────────────────────────────────────────────────────────────┘
```

#### 2.2 版本信息区设计

**显示内容**：
```text
┌────────────────────────────────────────────────────────────┐
│ 案件列表字段配置                                            │
│ [基于映射配置：v5] [来源：上传版本] 拉取时间：2025-12-08 10:30:00 │
│                                        [保存为新版本] [版本历史] │
└────────────────────────────────────────────────────────────┘
```

**版本标签说明**：
- **基于映射配置：v{N}**
  - 蓝色标签（type="info"）
  - 显示当前字段列表来源的映射配置版本号
  - v0表示内置Mock

- **来源标签**
  - 绿色标签（上传版本，type="success"）
  - 橙色标签（内置Mock，type="warning"）
  - 明确字段数据来源

- **拉取时间**
  - 浅色小字
  - ISO格式：YYYY-MM-DDTHH:mm:ss

#### 2.3 字段配置表格设计

**表格列说明**：

1. **拖拽列（宽度60px）**
   - 图标：三横线拖拽图标
   - 功能：拖拽调整字段排序
   - 鼠标悬停时图标变蓝

2. **序号列（宽度60px）**
   - 自动递增序号
   - 与排序顺序一致

3. **字段名称列（宽度150px）**
   - 标签显示（el-tag）
   - 显示字段的中文名称

4. **字段标识列（宽度150px）**
   - 纯文本显示
   - 显示字段的唯一标识（field_key）

5. **字段类型列（宽度100px）**
   - 标签显示（el-tag type="info"）
   - 显示字段的数据类型

6. **枚举值列（宽度180px）**
   - 仅Enum类型字段显示
   - 格式：逗号分隔的枚举选项
   - 非Enum类型显示"-"

7. **映射类型列（宽度100px）**
   - 标签显示（el-tag）
   - 标准字段：绿色（type="success"）
   - 自定义字段：灰色（type="info"）

8. **显示宽度列（宽度150px）**
   - 数字输入框（el-input-number）
   - 范围：0-500
   - 步进：10
   - 后缀提示：（0=自动）

9. **颜色列（宽度120px）**
   - 下拉选择（el-select）
   - 选项：
     - 普通（灰色文字）
     - 红色（红色文字）
     - 黄色（黄色文字）
     - 绿色（绿色文字）

10. **可筛选列（宽度100px，居中）**
    - 开关（el-switch）
    - 仅Enum类型字段显示开关
    - 其他类型显示"-"

11. **范围检索列（宽度100px，居中）**
    - 开关（el-switch）
    - 仅Integer/Number/Decimal/Date/Datetime类型显示开关
    - 其他类型显示"-"

12. **操作列（宽度110px，固定右侧）**
    - 编辑按钮（蓝色链接）
    - 功能：打开编辑弹窗

**表格交互设计**：
- 支持拖拽排序（点击拖拽图标拖动）
- 支持宽度和颜色直接在表格中修改
- 支持开关直接在表格中切换
- 所有修改需要点击"保存为新版本"才正式生效

#### 2.4 编辑字段弹窗设计

**弹窗标题**：编辑字段配置

**弹窗宽度**：800px

**Tab页签**：

1. **基本信息Tab**
   - 场景类型（只读，灰色下拉）
   - 字段标识（只读，灰色输入框）
   - 字段名称（可编辑）
   - 字段类型（只读）
   - 字段来源（只读，标签显示）
   - 排序顺序（数字输入框，最小0）
   - 显示宽度（数字输入框，0-500，提示"0表示自动"）

2. **样式配置Tab**
   - 颜色类型（单选按钮组）
     - 普通（灰色预览）
     - 红色（红色预览）
     - 黄色（黄色预览）
     - 绿色（绿色预览）

3. **检索配置Tab**
   - 是否可筛选（开关，仅Enum类型）
   - 是否支持范围检索（开关，仅数字/日期类型）
   - 提示文字：
     - "可筛选：仅枚举字段支持，开启后可在列表页下拉筛选"
     - "范围检索：仅数字和日期字段支持，开启后可输入范围查询"

**弹窗底部**：
- 取消按钮（灰色）
- 确定按钮（蓝色主按钮）

#### 2.5 版本历史抽屉设计

**抽屉标题**：展示配置版本历史

**抽屉方向**：右侧（rtl）

**抽屉宽度**：800px

**版本列表表格**：

| 列名 | 宽度 | 类型 | 说明 |
|-----|------|------|------|
| 版本号 | 80px | 标签 | v1、v2等，蓝色主题标签 |
| 保存时间 | 180px | 文本 | YYYY-MM-DD HH:mm:ss格式 |
| 操作人 | 120px | 文本 | 操作人ID或姓名 |
| 备注说明 | 200px | 文本 | 版本备注，无备注显示"-" |
| 字段数 | 80px | 数字 | 该版本包含的字段配置数量 |
| 操作 | 120px | 按钮 | "恢复此版本"蓝色链接 |

**空状态**：
- 显示空状态图标和文字："暂无版本历史"

**排序规则**：
- 按版本号倒序（最新版本在最上面）

---

### 3. 接口设计（API Design）

#### 3.1 获取字段配置列表

**接口路径**：`GET /api/v1/case-list-field-configs`

**请求参数**：
```text
{
  tenantId?: number      // 甲方ID（可选，默认1）
  sceneType?: string     // 场景类型（可选，默认admin_case_detail）
  fieldKey?: string      // 字段标识（可选，用于单字段查询）
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "tenant_id": 1,
      "scene_type": "admin_case_detail",
      "scene_name": "控台案件列表",
      "field_key": "case_id",
      "field_name": "案件编号",
      "field_data_type": "String",
      "field_source": "standard",
      "enum_options": null,
      "sort_order": 1,
      "display_width": 120,
      "color_type": "normal",
      "color_rule": null,
      "hide_rule": null,
      "hide_for_queues": [],
      "hide_for_agencies": [],
      "hide_for_teams": [],
      "format_rule": null,
      "is_searchable": false,
      "is_filterable": true,
      "is_range_searchable": true,
      "is_required": true,
      "created_by": "system",
      "updated_by": null,
      "created_at": "2025-12-08T10:00:00",
      "updated_at": "2025-12-08T10:00:00"
    }
  ]
}
```

**业务逻辑**：
1. 优先从本地激活版本加载配置
2. 如无本地版本，从映射配置最新版本获取字段列表并补充默认展示属性
3. 如映射配置也无数据，返回内置Mock字段配置
4. 自动标识必显字段（is_required=true）

#### 3.2 获取映射配置版本信息

**接口路径**：`GET /api/v1/case-list-field-configs/version`

**请求参数**：
```text
{
  tenantId?: number      // 甲方ID（可选，默认1）
  sceneType?: string     // 场景类型（可选，默认admin_case_detail）
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "version": 5,
    "source": "upload",
    "fetched_at": "2025-12-08T10:30:00"
  }
}
```

**字段说明**：
- `version`：映射配置版本号（0表示Mock）
- `source`：数据来源（upload=上传版本，mock=内置Mock，local=本地展示配置版本）
- `fetched_at`：拉取时间

#### 3.3 保存为新版本

**接口路径**：`POST /api/v1/case-list-field-configs/version/save`

**请求参数**：
```json
{
  "tenant_id": 1,
  "scene_type": "admin_case_detail",
  "configs": [
    {
      "field_key": "case_id",
      "field_name": "案件编号",
      "field_data_type": "String",
      "field_source": "standard",
      "sort_order": 1,
      "display_width": 140,
      "color_type": "normal",
      "is_filterable": false,
      "is_range_searchable": false
    }
  ],
  "operator": "admin",
  "note": "调整案件编号显示宽度为140px"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "保存成功",
  "data": {
    "version": 3,
    "saved_at": "2025-12-08T10:35:00"
  }
}
```

**业务逻辑**：
1. 读取本地版本文件（如不存在则创建）
2. 计算下一个版本号（当前最大版本号+1）
3. 构建版本记录（版本号、时间、操作人、备注、配置快照）
4. 追加到版本列表
5. 设置新版本为激活版本（activeVersion）
6. 写入本地版本文件
7. 返回新版本号和保存时间

#### 3.4 获取版本历史

**接口路径**：`GET /api/v1/case-list-field-configs/version/history`

**请求参数**：
```text
{
  tenantId?: number      // 甲方ID（可选，默认1）
  sceneType?: string     // 场景类型（可选，默认admin_case_detail）
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "version": 3,
      "saved_at": "2025-12-08T10:35:00",
      "operator": "admin",
      "note": "调整案件编号显示宽度为140px",
      "configs": [...]
    },
    {
      "version": 2,
      "saved_at": "2025-12-08T09:20:00",
      "operator": "admin",
      "note": "开启所有字段的范围检索",
      "configs": [...]
    },
    {
      "version": 1,
      "saved_at": "2025-12-08T08:00:00",
      "operator": "system",
      "note": "初始配置版本",
      "configs": [...]
    }
  ]
}
```

**业务逻辑**：
1. 读取本地版本文件
2. 返回versions数组
3. 按版本号倒序排列（最新版本在最前）

#### 3.5 激活指定版本

**接口路径**：`POST /api/v1/case-list-field-configs/version/activate`

**请求参数**：
```json
{
  "tenant_id": 1,
  "scene_type": "admin_case_detail",
  "version": 2
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "激活成功",
  "data": {
    "version": 2,
    "configs": [...]
  }
}
```

**业务逻辑**：
1. 读取本地版本文件
2. 查找指定版本号的版本记录
3. 如版本不存在，返回错误
4. 将activeVersion设为指定版本号
5. 写入本地版本文件
6. 返回激活的版本配置

#### 3.6 获取场景类型列表

**接口路径**：`GET /api/v1/case-list-field-configs/scene-types`

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "key": "admin_case_detail",
      "name": "控台案件列表",
      "description": "控台案件列表页面字段展示配置"
    },
    {
      "key": "collector_case_detail",
      "name": "IM端案件列表",
      "description": "IM端案件列表页面字段展示配置"
    }
  ]
}
```

---

### 4. 数据模型（Data Model）

#### 4.1 字段展示配置实体（TenantFieldDisplayConfig）

**数据库表**：`tenant_field_display_configs`

**字段定义**：

| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | BIGINT | 是 | 主键ID，自增 |
| tenant_id | BIGINT | 是 | 所属甲方ID |
| scene_type | VARCHAR(50) | 是 | 场景类型 |
| scene_name | VARCHAR(100) | 是 | 场景名称 |
| field_key | VARCHAR(100) | 是 | 字段标识 |
| field_name | VARCHAR(200) | 是 | 字段名称 |
| field_data_type | VARCHAR(50) | 否 | 字段数据类型 |
| field_source | VARCHAR(20) | 否 | 字段来源（standard/custom） |
| sort_order | INT | 是 | 排序顺序，默认0 |
| display_width | INT | 是 | 显示宽度（像素），0表示自动 |
| color_type | VARCHAR(20) | 是 | 颜色类型，默认normal |
| color_rule | JSON | 否 | 颜色规则（条件表达式） |
| hide_rule | JSON | 否 | 隐藏规则 |
| hide_for_queues | JSON | 否 | 对哪些队列隐藏 |
| hide_for_agencies | JSON | 否 | 对哪些机构隐藏 |
| hide_for_teams | JSON | 否 | 对哪些小组隐藏 |
| format_rule | JSON | 否 | 格式化规则 |
| is_searchable | TINYINT(1) | 是 | 是否可搜索，默认0 |
| is_filterable | TINYINT(1) | 是 | 是否可筛选，默认0 |
| is_range_searchable | TINYINT(1) | 是 | 是否支持范围检索，默认0 |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |
| created_by | VARCHAR(100) | 否 | 创建人 |
| updated_by | VARCHAR(100) | 否 | 更新人 |

**注意**：
- `is_required` 字段在数据库表中不存在，是在Service层动态计算的非持久化字段

**索引设计**：
- 主键索引：`PRIMARY KEY (id)`
- 甲方场景索引：`KEY idx_tenant_scene (tenant_id, scene_type)`
- 字段标识索引：`KEY idx_field_key (field_key)`

#### 4.2 版本文件数据结构

**文件位置**：`~/.cco-storage/case-list-display-versions/{tenantId}_{sceneType}.json`

**文件格式**：
```json
{
  "versions": [
    {
      "version": 1,
      "saved_at": "2025-12-08T10:00:00",
      "operator": "system",
      "note": "初始配置版本",
      "configs": [
        {
          "field_key": "case_id",
          "field_name": "案件编号",
          "field_data_type": "String",
          "field_source": "standard",
          "enum_options": null,
          "sort_order": 1,
          "display_width": 120,
          "color_type": "normal",
          "is_filterable": false,
          "is_range_searchable": false
        }
      ]
    },
    {
      "version": 2,
      "saved_at": "2025-12-08T11:30:00",
      "operator": "admin",
      "note": "开启所有字段的范围检索",
      "configs": [...]
    }
  ],
  "activeVersion": 2
}
```

**字段说明**：
- `versions`：版本列表数组
  - `version`：版本号，整数，从1开始自增
  - `saved_at`：保存时间，ISO格式字符串
  - `operator`：操作人，字符串
  - `note`：版本备注，字符串，可空
  - `configs`：字段配置快照，数组，包含完整的字段配置

- `activeVersion`：当前激活的版本号，整数，null表示无激活版本

#### 4.3 前端数据类型

**字段展示配置类型**：
```typescript
interface FieldDisplayConfig {
  id?: number
  tenant_id?: number
  scene_type?: string
  scene_name?: string
  field_key: string
  field_name: string
  field_data_type?: string
  field_source?: string
  enum_options?: any[] | string[]  // 枚举选项
  sort_order?: number
  display_width?: number
  color_type?: string
  color_rule?: any
  hide_rule?: any
  hide_for_queues?: number[]
  hide_for_agencies?: number[]
  hide_for_teams?: number[]
  format_rule?: any
  is_searchable?: boolean
  is_filterable?: boolean
  is_range_searchable?: boolean
  is_required?: boolean  // 非持久化字段，Service层计算
  created_by?: string
  updated_by?: string
  created_at?: string
  updated_at?: string
}
```

**版本信息类型**：
```typescript
interface VersionInfo {
  version: number    // 映射配置版本号
  source: string     // 来源：upload/mock/local
  fetched_at: string // 拉取时间
}
```

**版本历史记录类型**：
```typescript
interface VersionRecord {
  version: number
  saved_at: string
  operator: string
  note?: string
  configs: FieldDisplayConfig[]
}
```

---

## 三、交互设计（Interaction Design）

### 1. 页面入口

**菜单路径**：管理控台 → 字段配置 → 案件列表字段配置

**路由路径**：`/field-config/list`

**页面标题**：案件列表字段配置

### 2. 核心交互流程

#### 2.1 初始加载交互

```text
用户进入页面
    ↓
显示加载动画（Loading）
    ↓
并行加载：
  - 场景类型列表
  - 映射配置版本信息
  - 默认场景的字段配置列表
    ↓
隐藏加载动画
    ↓
显示页面内容：
  - 顶部：映射配置版本信息（标签+文字）
  - 操作区：保存为新版本、版本历史按钮
  - 场景选择：下拉框，默认选中admin_case_detail
  - 表格：字段配置列表
```text

#### 2.2 场景切换交互

```text
用户点击场景下拉框
    ↓
显示场景选项列表：
  - 控台案件列表
  - IM端案件列表
    ↓
用户选择新场景
    ↓
触发场景切换事件
    ↓
显示加载动画
    ↓
加载新场景的字段配置
    ↓
刷新映射配置版本信息
    ↓
隐藏加载动画
    ↓
更新表格数据
    ↓
重新初始化拖拽排序
```

#### 2.3 编辑字段交互

```text
用户点击字段行的"编辑"按钮
    ↓
打开编辑弹窗（800px宽）
    ↓
预填充字段配置信息到表单
    ↓
显示3个Tab页签：
  - 基本信息（默认选中）
  - 样式配置
  - 检索配置
    ↓
用户在各Tab中修改配置
    ↓
实时校验输入（如宽度范围0-500）
    ↓
[点击取消] → 关闭弹窗，不保存
    ↓
[点击确定] → 关闭弹窗，更新表格中的配置
    ↓
显示提示："修改已更新，请点击'保存为新版本'使配置生效"
```

#### 2.4 拖拽排序交互

```text
用户鼠标悬停在拖拽图标上
    ↓
鼠标光标变为移动图标
    ↓
拖拽图标颜色变蓝
    ↓
用户按住鼠标拖动
    ↓
被拖拽行显示半透明蓝色背景
    ↓
占位行显示浅灰色背景
    ↓
用户释放鼠标
    ↓
字段行移动到新位置
    ↓
自动重新计算所有字段的sort_order（1, 2, 3...）
    ↓
显示成功提示："拖拽成功，请点击'保存为新版本'保存更改"
```

#### 2.5 保存为新版本交互

```text
用户修改配置后点击"保存为新版本"按钮
    ↓
弹出确认对话框：
  - 标题：保存为新版本
  - 内容：
    - 显示：当前配置包含 X 个字段
    - 输入框：版本备注说明（可选，placeholder："描述本次修改内容"）
  - 底部：取消 / 确定保存
    ↓
[点击取消] → 关闭对话框
    ↓
[点击确定] → 显示保存中加载状态
    ↓
调用保存接口
    ↓
[保存失败] → 显示错误提示
    ↓
[保存成功] → 显示成功提示："保存成功，新版本号：v3"
    ↓
关闭对话框
    ↓
刷新页面数据（重新加载配置列表）
```

#### 2.6 查看版本历史交互

```text
用户点击"版本历史"按钮
    ↓
从右侧滑出版本历史抽屉（800px宽）
    ↓
显示加载动画
    ↓
加载版本历史列表
    ↓
隐藏加载动画
    ↓
显示版本列表表格：
  - 最新版本在最上面
  - 每行显示：版本号、时间、操作人、备注、字段数、操作按钮
    ↓
[无版本历史] → 显示空状态："暂无版本历史"
    ↓
[有版本历史] → 显示版本列表
    ↓
用户可点击"恢复此版本"按钮
```

#### 2.7 恢复历史版本交互

```text
用户在版本历史抽屉中点击某版本的"恢复此版本"按钮
    ↓
弹出二次确认对话框：
  - 标题：确认恢复版本
  - 内容：
    - "您确定要恢复到版本 v2 吗？"
    - "保存时间：2025-12-08 09:20:00"
    - "备注：开启所有字段的范围检索"
    - 警告提示："恢复后当前配置将被替换，建议先保存当前配置"
  - 底部：取消 / 确定恢复
    ↓
[点击取消] → 关闭确认对话框
    ↓
[点击确定] → 显示恢复中加载状态
    ↓
调用激活版本接口
    ↓
[激活失败] → 显示错误提示
    ↓
[激活成功] → 显示成功提示："版本恢复成功"
    ↓
关闭确认对话框
    ↓
关闭版本历史抽屉
    ↓
刷新页面数据（加载恢复后的配置）
```

### 3. 页面状态管理

#### 3.1 加载状态

**主页面加载**：
- 初始加载时显示全局Loading
- 场景切换时显示表格Loading
- 保存版本时按钮显示Loading状态
- 恢复版本时确认对话框显示Loading

**异步操作反馈**：
- 所有异步操作（加载、保存、恢复）都有Loading提示
- 操作成功/失败都有Message提示
- 关键操作（保存、恢复）有二次确认

#### 3.2 错误状态

**错误处理策略**：
- 接口请求失败：显示错误提示，不影响页面其他功能
- 版本文件读取失败：回退到映射配置或Mock数据
- 版本保存失败：提示错误原因，不刷新页面
- 版本恢复失败：提示错误原因，保持当前配置

**容错机制**：
- 字段列表为空时显示空状态提示
- 版本历史为空时显示"暂无版本历史"
- 映射配置不存在时使用Mock数据兜底

---

## 四、技术实现说明

### 1. 后端实现关键点

#### 1.1 数据加载优先级

```text
// CaseListFieldConfigController.getCaseListFieldConfigs
List<TenantFieldDisplayConfig> list;

// 1. 尝试从本地展示配置版本加载
list = loadActiveVersion(tenantId, sceneType);

// 2. 如无本地版本，从映射配置获取
if (list == null || list.isEmpty()) {
    Map<String, Object> versionFields = tenantFieldUploadService.getCurrentVersionFields(tenantId, "list");
    if (versionFields != null && !versionFields.isEmpty()) {
        List<Map<String, Object>> fields = versionFields.get("fields");
        list = convertUploadFields(fields, tenantId, sceneType);
    }
}

// 3. 如仍为空，使用Mock数据
if (list == null || list.isEmpty()) {
    list = generateMockFields(tenantId, sceneType);
}

// 4. 补充必显字段标识
list.forEach(config -> {
    config.setIsRequired(REQUIRED_FIELD_KEYS.contains(config.getFieldKey()));
});
```

#### 1.2 映射字段转换逻辑

```java
// 从映射配置的字段转换为展示配置
private List<TenantFieldDisplayConfig> convertUploadFields(
    List<Map<String, Object>> uploadFields,
    Long tenantId,
    String sceneType
) {
    List<TenantFieldDisplayConfig> result = new ArrayList<>();
    int sortOrder = 1;

    for (Map<String, Object> uploadField : uploadFields) {
        TenantFieldDisplayConfig config = new TenantFieldDisplayConfig();
        config.setTenantId(tenantId);
        config.setSceneType(sceneType);
        config.setSceneName(getSceneName(sceneType));
        config.setFieldKey(uploadField.get("field_key"));
        config.setFieldName(uploadField.get("field_name"));
        config.setFieldDataType(uploadField.get("field_type"));
        config.setFieldSource("standard");

        // 展示配置默认值
        config.setSortOrder(sortOrder++);
        config.setDisplayWidth(120);
        config.setColorType("normal");
        config.setIsSearchable(false);
        config.setIsFilterable(true);  // 默认开启
        config.setIsRangeSearchable(true);  // 默认开启

        // 初始化空数组
        config.setHideForQueues(new ArrayList<>());
        config.setHideForAgencies(new ArrayList<>());
        config.setHideForTeams(new ArrayList<>());
        config.setCreatedBy("system");

        result.add(config);
    }

    return result;
}
```

#### 1.3 版本文件管理

**读取版本文件**：
```java
private Map<String, Object> readVersionFile(Long tenantId, String sceneType) throws Exception {
    Path file = VERSION_BASE.resolve(tenantId + "_" + sceneType + ".json");

    if (!Files.exists(file)) {
        // 返回空版本结构
        Map<String, Object> init = new HashMap<>();
        init.put("versions", new ArrayList<>());
        init.put("activeVersion", null);
        return init;
    }

    byte[] bytes = Files.readAllBytes(file);
    return OBJECT_MAPPER.readValue(bytes, new TypeReference<Map<String, Object>>() {});
}
```

**写入版本文件**：
```java
private void writeVersionFile(Long tenantId, String sceneType, Map<String, Object> data) throws Exception {
    if (!Files.exists(VERSION_BASE)) {
        Files.createDirectories(VERSION_BASE);
    }

    Path file = VERSION_BASE.resolve(tenantId + "_" + sceneType + ".json");
    byte[] bytes = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
    Files.write(file, bytes);
}
```

### 2. 前端实现关键点

#### 2.1 版本信息加载

```typescript
// 加载映射配置版本信息
const loadVersionInfo = async () => {
  try {
    const data = await getCaseListFieldConfigVersion({
      tenantId: currentTenantId.value,
      sceneType: currentScene.value
    })
    versionInfo.value = data?.data || { version: 0, source: 'mock', fetched_at: '' }
  } catch (error: any) {
    console.error('加载版本信息失败:', error)
    versionInfo.value = { version: 0, source: 'mock', fetched_at: '' }
  }
}
```

#### 2.2 保存版本逻辑

```typescript
const handleSaveVersion = async () => {
  try {
    // 弹出输入框让用户填写备注
    const { value } = await ElMessageBox.prompt(
      `当前配置包含 ${configs.value.length} 个字段，请填写版本备注（可选）`,
      '保存为新版本',
      {
        confirmButtonText: '确定保存',
        cancelButtonText: '取消',
        inputPlaceholder: '描述本次修改内容，如：调整字段宽度、开启范围检索等'
      }
    )

    saveVersionLoading.value = true

    const res = await saveCaseListFieldConfigVersion({
      tenant_id: Number(currentTenantId.value),
      scene_type: currentScene.value,
      configs: configs.value.map(c => ({
        field_key: c.field_key,
        field_name: c.field_name,
        field_data_type: c.field_data_type,
        field_source: c.field_source,
        sort_order: c.sort_order,
        display_width: c.display_width,
        color_type: c.color_type,
        is_filterable: c.is_filterable,
        is_range_searchable: c.is_range_searchable
      })),
      operator: 'admin',
      note: value || ''
    })

    ElMessage.success(`保存成功，新版本号：v${res.data.version}`)
    loadConfigs() // 刷新配置列表
    loadVersionInfo() // 刷新版本信息
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败：' + error.message)
    }
  } finally {
    saveVersionLoading.value = false
  }
}
```

#### 2.3 版本历史加载

```typescript
const handleShowHistory = async () => {
  historyVisible.value = true

  try {
    const data = await getCaseListFieldConfigVersionHistory({
      tenantId: currentTenantId.value,
      sceneType: currentScene.value
    })
    versionHistory.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (error: any) {
    ElMessage.error('加载版本历史失败：' + error.message)
  }
}
```

#### 2.4 恢复版本逻辑

```typescript
const handleRestoreVersion = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `您确定要恢复到版本 v${row.version} 吗？\n保存时间：${row.saved_at}\n备注：${row.note || '无'}\n\n恢复后当前配置将被替换，建议先保存当前配置。`,
      '确认恢复版本',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await activateCaseListFieldConfigVersion({
      tenant_id: Number(currentTenantId.value),
      scene_type: currentScene.value,
      version: row.version
    })

    ElMessage.success('版本恢复成功')
    historyVisible.value = false
    loadConfigs() // 刷新配置列表
    loadVersionInfo() // 刷新版本信息
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败：' + error.message)
    }
  }
}
```

### 3. 组件复用

**表格组件**：
- 使用 Element Plus `el-table` 组件
- 开启边框（border）
- 支持Loading状态
- 固定右侧操作列

**拖拽排序**：
- 使用 `sortablejs` 库
- 绑定拖拽句柄（.drag-handle）
- 动画效果：150ms
- 拖拽结束后自动重新计算排序

**表单验证**：
- 宽度范围：0-500
- 必填字段：场景类型、字段标识、字段名称

---

## 五、数据流转说明

### 1. 字段数据流转

```text
案件列表字段映射配置（上游）
    ↓
甲方上传Excel → 匹配标准字段 → 保存映射版本 → 激活版本
    ↓
tenant_field_uploads表（scene='list', is_active=true）
    ↓
存储字段映射关系：
  - field_name: 甲方字段名
  - field_key: 映射到的标准字段key
  - field_type: 字段类型
  - enum_values: 枚举值（如有）
    ↓
案件列表字段展示配置（本功能）
    ↓
读取映射配置最新版本 → 转换为展示配置 → 补充展示属性默认值
    ↓
管理员配置展示属性：
  - 显示宽度
  - 颜色
  - 可筛选开关
  - 范围检索开关
    ↓
保存为展示配置新版本
    ↓
本地版本文件（~/.cco-storage/case-list-display-versions/）
    ↓
激活版本 → 案件列表页面读取 → 控制字段展示
```

### 2. 版本数据流转

```text
展示配置版本文件（本地JSON）
    ↓
{
  "versions": [v1, v2, v3, ...],
  "activeVersion": 3
}
    ↓
保存新版本：
  - 读取文件
  - 计算版本号（max+1）
  - 追加版本记录
  - 设为激活版本
  - 写入文件
    ↓
查看历史：
  - 读取文件
  - 返回versions数组
  - 按版本号倒序
    ↓
恢复版本：
  - 读取文件
  - 查找目标版本
  - 设为激活版本
  - 写入文件
    ↓
加载配置：
  - 读取文件
  - 查找activeVersion
  - 返回对应版本的configs
```text

### 3. 数据同步机制

**映射配置更新后同步**：
```text
映射配置保存新版本（v6）并激活
    ↓
展示配置页面仍显示基于v5的配置
    ↓
用户刷新页面或切换场景
    ↓
系统检测：
  - 本地有激活版本 → 使用本地版本（仍基于v5）
  - 本地无激活版本 → 使用映射v6
    ↓
如需同步映射v6：
  - 方案1：删除本地版本文件，刷新页面
  - 方案2：在版本历史中"基于最新映射创建版本"（未来扩展）
```typescript

**建议同步策略**：
- 映射配置有重大变更（新增/删除字段）时，建议重新配置展示属性
- 映射配置仅修改字段名称等信息时，展示配置可继续使用
- 提供"检测映射版本变更"提示（未来扩展）

---

## 六、测试验证点

### 1. 功能测试

**基础功能测试**：
- [ ] 页面正常加载，显示字段配置列表
- [ ] 映射配置版本信息正确显示
- [ ] 场景切换正常工作
- [ ] 字段表格正确显示12个字段
- [ ] 枚举值列正确显示枚举选项

**配置编辑测试**：
- [ ] 编辑按钮打开弹窗，预填充正确
- [ ] 修改宽度、颜色等属性成功保存
- [ ] 可筛选开关仅Enum类型显示
- [ ] 范围检索开关仅数字/日期类型显示

**拖拽排序测试**：
- [ ] 拖拽图标正常显示和交互
- [ ] 拖拽后排序顺序正确更新
- [ ] sort_order自动重新计算

**版本保存测试**：
- [ ] 保存按钮弹出确认对话框
- [ ] 输入备注并保存成功
- [ ] 保存后生成新版本号
- [ ] 保存后配置自动刷新

**版本历史测试**：
- [ ] 版本历史按钮打开抽屉
- [ ] 版本列表按倒序显示
- [ ] 版本信息（时间、操作人、备注）正确

**版本恢复测试**：
- [ ] 恢复按钮弹出二次确认
- [ ] 恢复后配置正确切换
- [ ] 恢复后页面自动刷新

### 2. 边界测试

**数据边界**：
- [ ] 映射配置不存在时显示Mock数据
- [ ] 本地版本文件不存在时创建新文件
- [ ] 版本列表为空时显示空状态
- [ ] 字段列表为空时显示空状态

**权限测试**：
- [ ] SuperAdmin可配置所有甲方
- [ ] TenantAdmin仅可配置本甲方

**异常测试**：
- [ ] 接口超时或失败时有错误提示
- [ ] 版本文件损坏时有容错处理
- [ ] 并发保存版本时版本号不冲突

### 3. 性能测试

**加载性能**：
- [ ] 页面首次加载时间 < 2秒
- [ ] 场景切换加载时间 < 1秒
- [ ] 版本历史加载时间 < 0.5秒

**保存性能**：
- [ ] 保存版本时间 < 1秒
- [ ] 恢复版本时间 < 1秒

---

## 七、产品验收标准

### 1. 功能完整性

- [x] 支持2个场景的独立配置（控台、催员端）
- [x] 支持基于映射配置版本获取字段列表
- [x] 支持字段展示属性配置（宽度、颜色、开关）
- [x] 支持拖拽排序
- [x] 支持保存为新版本
- [x] 支持查看版本历史
- [x] 支持恢复历史版本
- [x] 支持映射版本信息展示
- [x] 支持枚举值显示

### 2. 交互体验

- [x] 操作流程清晰，符合用户习惯
- [x] 加载状态有Loading提示
- [x] 操作成功/失败有Message反馈
- [x] 关键操作有二次确认
- [x] 表格支持拖拽排序，交互流畅
- [x] 版本信息展示清晰，易于理解

### 3. 数据准确性

- [x] 字段列表来源准确（优先级：本地版本 > 映射版本 > Mock）
- [x] 版本号自增正确，无重复
- [x] 版本快照完整，恢复后配置一致
- [x] 必显字段标识准确
- [x] 枚举值解析正确

### 4. 兼容性

- [x] 支持映射配置不存在时的Mock兜底
- [x] 支持本地版本文件不存在时的创建
- [x] 支持版本文件损坏时的容错处理
- [x] 支持与案件列表页面的数据对接

---

## 八、上线计划

### 1. 开发排期

| 阶段 | 任务 | 预计工期 | 负责人 |
|-----|------|---------|--------|
| 第1阶段 | 后端接口开发（版本管理、字段加载） | 2天 | 后端开发 |
| 第2阶段 | 前端页面开发（配置表格、版本历史） | 2天 | 前端开发 |
| 第3阶段 | 联调测试与Bug修复 | 1天 | 全员 |
| 第4阶段 | 上线部署与验证 | 0.5天 | 运维+产品 |

### 2. 上线检查清单

**代码质量**：
- [ ] 代码通过Lint检查
- [ ] 关键方法有注释
- [ ] 异常处理完善
- [ ] 日志记录完整

**功能验证**：
- [ ] 所有功能测试通过
- [ ] 边界测试通过
- [ ] 性能测试达标

**文档完善**：
- [ ] PRD文档完整
- [ ] 接口文档更新
- [ ] 操作手册编写

---

## 九、附录

### 1. 关键字段说明

**字段标识（field_key）**：
- 字段的唯一标识符
- 来自映射配置，与标准字段或自定义字段的field_key一致
- 示例：`case_id`、`case_status`、`loan_amount`

**字段类型（field_data_type / field_type）**：
- String：文本类型
- Enum：枚举类型
- Integer：整数类型
- Number：数值类型（通用）
- Decimal：小数类型
- Date：日期类型
- Datetime：日期时间类型

**场景类型（scene_type）**：
- `admin_case_detail`：控台案件列表（用于控台案件列表页面）
- `collector_case_detail`：IM端案件列表（用于催员端案件列表页面）

### 2. Mock字段配置示例

```typescript
const mockFields = [
  { field_key: 'case_id', field_name: '案件编号', field_type: 'String', is_required: true },
  { field_key: 'case_status', field_name: '案件状态', field_type: 'Enum', enum_options: ['待分配', '催收中', '已完成'], is_required: true },
  { field_key: 'overdue_days', field_name: '逾期天数', field_type: 'Integer', is_required: false },
  { field_key: 'loan_amount', field_name: '借款金额', field_type: 'Number', is_required: true },
  { field_key: 'outstanding_amount', field_name: '未还金额', field_type: 'Number', is_required: true },
  { field_key: 'customer_name', field_name: '客户姓名', field_type: 'String', is_required: true },
  { field_key: 'phone', field_name: '联系电话', field_type: 'String', is_required: true },
  { field_key: 'product_name', field_name: '产品名称', field_type: 'String', is_required: false },
  { field_key: 'app_name', field_name: 'APP名称', field_type: 'String', is_required: false },
  { field_key: 'merchant_name', field_name: '商户名称', field_type: 'String', is_required: false },
  { field_key: 'due_date', field_name: '到期日期', field_type: 'Date', is_required: true },
  { field_key: 'collector', field_name: '催收员', field_type: 'String', is_required: false }
]
```

### 3. 版本文件示例

**文件路径**：`~/.cco-storage/case-list-display-versions/1_admin_case_detail.json`

**文件内容**：
```json
{
  "versions": [
    {
      "version": 1,
      "saved_at": "2025-12-08T10:00:00",
      "operator": "system",
      "note": "初始配置版本",
      "configs": [
        {
          "field_key": "case_id",
          "field_name": "案件编号",
          "field_data_type": "String",
          "field_source": "standard",
          "sort_order": 1,
          "display_width": 120,
          "color_type": "normal",
          "is_filterable": false,
          "is_range_searchable": false
        }
      ]
    },
    {
      "version": 2,
      "saved_at": "2025-12-08T11:30:00",
      "operator": "admin",
      "note": "调整案件编号宽度，开启范围检索",
      "configs": [
        {
          "field_key": "case_id",
          "field_name": "案件编号",
          "field_data_type": "String",
          "field_source": "standard",
          "sort_order": 1,
          "display_width": 140,
          "color_type": "normal",
          "is_filterable": false,
          "is_range_searchable": false
        }
      ]
    }
  ],
  "activeVersion": 2
}
```

### 4. 常见问题（FAQ）

**Q1：展示配置的字段列表从哪里来？**
A：字段列表来源于"案件列表字段映射配置"已完成匹配并保存的生效版本。只有在映射配置中完成匹配的字段才会出现在展示配置中。如果映射配置不存在，系统会使用内置Mock字段列表。

**Q2：为什么页面显示"基于映射配置：v5"，但字段配置是v2？**
A：这两个版本号是独立的。"基于映射配置：v5"表示字段列表来源于映射配置的第5个版本；展示配置的版本号是本功能独立维护的，用于管理展示属性的变更历史。

**Q3：如何确保展示配置与映射配置同步？**
A：当映射配置更新后，建议删除本地展示配置版本文件，刷新页面即可自动加载最新映射配置的字段列表。未来可扩展"基于最新映射创建版本"功能。

**Q4：版本保存后可以删除吗？**
A：目前不支持版本删除。所有历史版本都会保留，方便随时回滚。如果版本过多影响性能，可手动清理本地版本文件。

**Q5：恢复历史版本后，原来的配置会丢失吗？**
A：恢复版本只是将activeVersion指向历史版本，不会删除任何版本记录。如果担心配置丢失，可在恢复前先保存当前配置为新版本。

**Q6：为什么不能添加或删除字段？**
A：字段列表是由映射配置控制的，展示配置只负责配置字段的展示属性。如需增减字段，应在"案件列表字段映射配置"中操作。

**Q7：可筛选和范围检索的区别是什么？**
A：
- 可筛选（is_filterable）：仅枚举字段支持，在列表页提供下拉筛选框
- 范围检索（is_range_searchable）：仅数字/日期字段支持，在列表页提供范围输入框（最小值-最大值）

**Q8：必显字段（is_required）有什么用？**
A：必显字段是案件列表中必须展示的核心字段（如案件编号、客户姓名等），不可删除、不可隐藏。这是后端自动标识的，前端只用于显示。

---

## 十、未来扩展

### 1. 短期扩展（1-2个月）

- 版本对比功能：对比两个版本的配置差异
- 版本删除功能：删除不需要的历史版本
- 映射版本变更提醒：映射配置更新后提示用户同步
- 批量操作：批量设置字段属性

### 2. 长期扩展（3-6个月）

- 配置模板功能：预设常用配置模板
- 字段分组展示：按字段分组折叠显示
- 高级筛选规则：支持复杂条件表达式
- 颜色规则表达式：恢复动态颜色规则配置
- 权限细化：不同角色配置不同字段的权限

---

