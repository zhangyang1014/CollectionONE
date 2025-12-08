# 字段配置管理功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

字段配置管理是CCO管理控台的基础功能之一，用于管理系统标准字段、字段分组、自定义拓展字段以及甲方字段展示配置。不同甲方客户的业务场景和字段需求差异很大，系统需要支持灵活的字段配置，让管理员能够根据实际业务需求配置字段的展示、排序、筛选等属性。同时，支持甲方通过JSON文件上传字段定义，系统自动解析并与当前版本进行对比，确保字段配置的准确性和一致性。

**业务痛点**：
- 不同甲方字段命名和结构差异大，需要灵活的字段映射机制
- 标准字段无法覆盖所有业务场景，需要支持自定义拓展字段
- 字段配置变更频繁，需要版本管理和变更对比功能
- 甲方通过API推送字段定义，需要支持JSON文件导入和解析
- 字段展示配置复杂，需要支持不同场景下的字段展示规则

**预期影响的核心指标**：
- 字段配置效率：标准字段配置完成时间≤5分钟/字段
- 字段映射准确率：字段映射成功率≥99%
- JSON文件解析成功率：格式正确的JSON文件解析成功率≥100%
- 版本对比准确率：版本差异识别准确率≥100%
- 用户满意度：管理员对字段配置功能满意度≥90%

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：管理标准字段**
- **入口**：管理控台菜单 → 字段配置 → 标准字段管理
- **触发时机**：需要新增、修改或删除系统标准字段时
- **所在页面**：`/field-config/standard`
- **流程节点**：选择字段分组 → 查看字段列表 → 添加/编辑/删除字段 → 配置字段属性

**场景2：管理字段分组**
- **入口**：管理控台菜单 → 字段配置 → 字段分组管理
- **触发时机**：需要调整字段分组结构或排序时
- **所在页面**：`/field-config/groups`
- **流程节点**：查看分组树 → 添加/编辑/删除分组 → 拖拽调整排序

**场景3：管理自定义拓展字段**
- **入口**：管理控台菜单 → 字段配置 → 字段映射配置 → 拓展字段Tab
- **触发时机**：甲方有特殊字段需求，无法映射到标准字段时
- **所在页面**：`/field-config/custom` → 拓展字段Tab
- **流程节点**：选择甲方 → 选择字段分组 → 添加拓展字段 → 配置字段属性（类型、隐私标签等）

**场景4：配置甲方字段展示规则**
- **入口**：管理控台菜单 → 字段配置 → 甲方字段展示配置
- **触发时机**：需要配置不同场景下字段的展示、排序、筛选等属性时
- **所在页面**：`/field-config/display`
- **流程节点**：选择甲方 → 选择场景类型 → 选择字段 → 配置展示属性（排序、筛选、范围检索、隐私设置）

**场景5：查看甲方字段并上传JSON文件**
- **入口**：管理控台菜单 → 字段配置 → 甲方字段查看
- **触发时机**：需要查看甲方字段定义，或甲方提供新的JSON文件需要导入时
- **所在页面**：`/field-config/tenant-fields-view`
- **流程节点**：选择甲方 → 查看字段列表 → 点击"上传JSON文件" → 选择文件 → 校验格式 → 对比版本差异 → 确认保存

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用频率 |
|---------|---------|---------|---------|
| 超级管理员 | SuperAdmin | 管理系统标准字段和字段分组，配置所有甲方的字段展示规则 | 高 |
| 甲方管理员 | TenantAdmin | 配置本甲方的字段映射、拓展字段和字段展示规则，上传JSON文件 | 高 |
| 机构管理员 | AgencyAdmin | 查看字段配置，了解字段定义 | 低 |
| 小组管理员 | TeamLeader | 查看字段配置，了解字段定义 | 低 |

---

### 3. 关键业务流程（Business Flow）

#### 3.1 标准字段管理流程

```
管理员进入标准字段管理页面
    ↓
加载字段分组树（左侧）
    ↓
自动选中第一个分组
    ↓
加载该分组下的标准字段列表（右侧）
    ↓
支持操作：
  - 添加字段：点击"添加字段" → 填写字段信息 → 保存
  - 编辑字段：点击"编辑" → 修改字段信息 → 保存
  - 删除字段：点击"删除" → 确认删除
  - 拖拽排序：拖拽字段行 → 自动保存排序
  - 启用/禁用：切换开关 → 自动保存
```

#### 3.2 字段分组管理流程

```
管理员进入字段分组管理页面
    ↓
加载字段分组树（支持父子分组）
    ↓
支持操作：
  - 添加一级分组：点击"添加一级分组" → 填写分组信息 → 保存
  - 添加子分组：选中分组 → 点击"添加子分组" → 填写分组信息 → 保存
  - 编辑分组：点击"编辑" → 修改分组信息 → 保存
  - 删除分组：点击"删除" → 检查是否有字段关联 → 确认删除
  - 拖拽排序：拖拽分组节点 → 自动保存排序
```

#### 3.3 自定义拓展字段管理流程

```
管理员进入字段映射配置页面
    ↓
选择甲方（必选）
    ↓
切换到"拓展字段"Tab
    ↓
选择字段分组（左侧树）
    ↓
查看该分组下的拓展字段列表
    ↓
支持操作：
  - 添加拓展字段：点击"添加拓展字段" → 填写字段信息 → 保存
  - 编辑拓展字段：点击"编辑" → 修改字段信息 → 保存
  - 删除拓展字段：点击"删除" → 确认删除
  - 配置隐私标签：选择PII/敏感/公开
```

#### 3.4 甲方字段展示配置流程

```
管理员进入甲方字段展示配置页面
    ↓
选择甲方（必选）
    ↓
选择场景类型（admin_case_list/collector_case_list/collector_case_detail）
    ↓
加载可用字段列表（标准字段 + 自定义字段）
    ↓
选择字段添加到配置
    ↓
配置字段属性：
  - 排序：拖拽调整或输入数字
  - 是否可筛选：开关（仅枚举字段）
  - 是否支持范围检索：开关（仅数字/日期字段）
  - 隐私设置：配置字段的隐私级别
    ↓
批量保存配置
```

#### 3.5 甲方字段JSON文件上传流程

```
管理员进入甲方字段查看页面
    ↓
选择甲方（必选）
    ↓
查看当前字段列表（从API获取或从数据库加载）
    ↓
点击"上传JSON文件"按钮
    ↓
打开文件选择器（仅.json格式）
    ↓
选择JSON文件
    ↓
前端读取文件内容，进行基础格式校验
    ↓
发送到后端进行详细校验
    ↓
[格式错误] → 显示错误列表（字段路径 + 错误原因）
    ↓
[格式正确] → 获取当前版本JSON（is_current=1）
    ↓
进行版本对比分析
    ↓
返回差异结果：
  - 新增字段（绿色标记）
  - 删除字段（红色删除线）
  - 修改字段（红色高亮修改部分）
  - 枚举值变化（详细对比）
    ↓
前端展示对比结果（diff视图）
    ↓
用户确认/取消
    ↓
[确认] → 保存新版本JSON
  • 将当前版本标记为历史（is_current=0）
  • 保存新版本为当前（is_current=1）
    ↓
[取消] → 关闭弹窗，不保存
```

#### 3.6 JSON文件格式校验流程

```
读取JSON文件内容
    ↓
解析JSON格式（检查是否为有效JSON）
    ↓
[格式错误] → 返回"JSON格式错误：第X行，第Y列"
    ↓
[格式正确] → 校验根节点字段
    ↓
校验必填字段：
  - version（版本号）
  - sync_time（同步时间，ISO8601格式）
  - fields（字段数组）
    ↓
[缺少必填字段] → 返回"缺少必填字段：xxx"
    ↓
遍历fields数组，校验每个字段
    ↓
校验字段必填属性：
  - field_key（字段标识）
  - field_name（字段名称）
  - field_type（字段类型）
  - updated_at（更新时间）
    ↓
校验字段类型：
  - field_type必须是：String/Integer/Decimal/Date/Datetime/Boolean/Enum
    ↓
[类型错误] → 返回"字段xxx的field_type无效：xxx"
    ↓
[类型为Enum] → 校验enum_values数组
  - 必须存在enum_values字段
  - enum_values必须是数组
  - 每个枚举项必须有value和label
    ↓
[枚举值格式错误] → 返回"字段xxx的enum_values格式错误：xxx"
    ↓
校验字段分组ID：
  - field_group_id必须在系统中存在
    ↓
[分组ID不存在] → 返回"字段xxx的field_group_id不存在：xxx"
    ↓
校验字段唯一性：
  - 同一JSON中field_key不能重复
    ↓
[重复] → 返回"字段标识重复：xxx"
    ↓
[全部通过] → 返回校验成功
```

#### 3.7 版本对比分析流程

```
获取当前版本JSON（is_current=1）
    ↓
解析当前版本和新版本的fields数组
    ↓
构建字段映射（以field_key为key）
    ↓
对比分析：
  1. 新增字段：新版本有，当前版本没有
  2. 删除字段：当前版本有，新版本没有
  3. 修改字段：field_key相同，但其他属性不同
    ↓
对于修改字段，详细对比：
  - field_name变化
  - field_type变化
  - is_required变化
  - field_group_id变化
  - enum_values变化（枚举值增删改）
    ↓
构建差异结果对象
    ↓
返回差异结果：
  {
    "added_fields": [...],      // 新增字段列表
    "deleted_fields": [...],     // 删除字段列表
    "modified_fields": [         // 修改字段列表
      {
        "field_key": "xxx",
        "changes": {
          "field_name": {"old": "...", "new": "..."},
          "enum_values": {
            "added": [...],       // 新增的枚举值
            "deleted": [...],     // 删除的枚举值
            "modified": [...]     // 修改的枚举值
          }
        }
      }
    ]
  }
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 标准字段管理规则

**字段属性规则**：
- 字段标识（field_key）：必须唯一，只能包含小写字母、数字和下划线，必须以小写字母开头
- 字段名称（field_name）：必填，长度1-200个字符
- 字段类型（field_type）：必填，必须是：String/Integer/Decimal/Date/Datetime/Boolean/Enum
- 所属分组（field_group_id）：必填，必须在系统中存在
- 是否必填（is_required）：默认false
- 是否拓展字段（is_extended）：默认false（标准字段为false）
- 排序顺序（sort_order）：数字，支持拖拽自动更新

**操作规则**：
- 标准字段可以编辑（字段名称、类型、分组、必填等）
- 标准字段可以启用/禁用
- 标准字段可以删除（软删除，is_deleted=true）
- 标准字段支持拖拽排序，自动更新sort_order

**范围边界**：
- ✅ 支持字段的增删改查
- ✅ 支持字段分组管理
- ✅ 支持字段排序
- ❌ 不支持字段值的编辑（字段值在案件数据中管理）

#### 4.2 字段分组管理规则

**分组属性规则**：
- 分组标识（group_key）：必须唯一，只能包含小写字母、数字和下划线
- 分组名称（group_name）：必填，长度1-200个字符
- 父分组（parent_id）：可选，支持多级分组
- 排序顺序（sort_order）：数字，支持拖拽自动更新

**操作规则**：
- 支持添加一级分组和子分组
- 支持编辑分组信息
- 支持删除分组（需检查是否有字段关联）
- 支持拖拽调整分组顺序和层级

**范围边界**：
- ✅ 支持分组的增删改查
- ✅ 支持多级分组（父子关系）
- ✅ 支持分组排序
- ❌ 不支持分组合并

#### 4.3 自定义拓展字段管理规则

**字段属性规则**：
- 字段标识（field_key）：在甲方内必须唯一
- 字段名称（field_name）：必填
- 字段类型（field_type）：必填，同标准字段类型
- 所属分组（field_group_id）：必填
- 隐私标签（privacy_label）：必填，PII/敏感/公开
- 是否必填（is_required）：默认false

**操作规则**：
- 拓展字段完全由甲方管理员控制
- 支持添加、编辑、删除拓展字段
- 拓展字段不映射到标准字段

**范围边界**：
- ✅ 支持拓展字段的完全管理
- ✅ 支持隐私标签配置
- ❌ 不支持拓展字段映射到标准字段

#### 4.4 甲方字段展示配置规则

**配置属性规则**：
- 场景类型（scene_type）：必填，admin_case_list/collector_case_list/collector_case_detail
- 字段标识（field_key）：必填
- 排序顺序（sort_order）：数字，支持拖拽调整
- 是否可筛选（is_filterable）：仅枚举字段可配置
- 是否支持范围检索（is_range_searchable）：仅数字/日期字段可配置
- 隐私设置：配置字段的隐私级别和脱敏规则

**配置优先级**：
1. 队列字段配置（queue_field_configs）- 最高优先级
2. 甲方字段配置（tenant_field_configs）- 中优先级
3. 字段默认配置（standard_fields/custom_fields）- 最低优先级

**范围边界**：
- ✅ 支持字段排序配置
- ✅ 支持字段筛选配置（枚举字段）
- ✅ 支持范围检索配置（数字/日期字段）
- ✅ 支持隐私设置配置
- ❌ 不支持字段值的编辑

#### 4.5 甲方字段JSON文件上传规则

**文件格式规则**：
- 文件格式：必须是.json文件
- JSON结构：必须包含version、sync_time、fields三个根节点
- 字段必填：fields数组中每个字段必须包含field_key、field_name、field_type、updated_at
- 字段类型：field_type必须是系统支持的类型
- 枚举值：Enum类型字段必须包含enum_values数组

**版本管理规则**：
- 只保存当前版本和1个历史版本
- 上传新版本时，自动将当前版本标记为历史（is_current=0）
- 新版本自动标记为当前（is_current=1）
- 历史版本保留，用于对比和回滚

**权限规则**：
- SuperAdmin：可以上传所有甲方的JSON文件
- TenantAdmin：只能上传自己甲方的JSON文件
- 其他角色：无权限上传

**对比规则**：
- 对比维度：新增字段、删除字段、修改字段
- 修改字段对比：field_name、field_type、is_required、field_group_id、enum_values
- 枚举值对比：新增、删除、修改（value和label变化）

**范围边界**：
- ✅ 支持JSON文件上传和解析
- ✅ 支持格式校验和错误提示
- ✅ 支持版本对比和差异展示
- ✅ 支持确认保存和取消操作
- ❌ 不支持批量上传多个文件
- ❌ 不支持自动合并冲突字段

---

### 5. 合规与风控要求（Compliance & Risk Control）

#### 5.1 数据隐私保护

**敏感字段处理**：
- PII字段（个人身份信息）：身份证号、手机号等需要脱敏显示
- 敏感字段：收入、地址等需要权限控制
- 公开字段：公司名称等可以正常显示

**隐私设置规则**：
- 支持按字段配置隐私级别
- 支持按角色配置字段可见性
- 支持按队列配置字段隐藏规则

#### 5.2 数据安全

**接口安全**：
- 所有接口需要Token验证
- JSON文件上传接口需要权限验证（SuperAdmin或TenantAdmin）
- 文件大小限制：最大10MB

**数据校验**：
- JSON格式校验：防止恶意文件上传
- 字段类型校验：防止类型错误导致系统异常
- 字段分组校验：防止引用不存在的分组

#### 5.3 审计要求

**操作日志**：
- 记录字段配置的所有变更操作
- 记录JSON文件上传操作（上传人、上传时间、版本号）
- 记录版本对比和保存操作

---

### 6. 资金路径与结算规则（Funding Flow & Settlement）

**不适用**：本功能不涉及资金流转。

---

### 7. 数据字段与口径（Data Definition）

#### 7.1 标准字段表（standard_fields）

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| field_key | VARCHAR(100) | 字段唯一标识 | 管理员输入 | 创建时设置 |
| field_name | VARCHAR(200) | 字段名称（中文） | 管理员输入 | 可编辑 |
| field_name_en | VARCHAR(200) | 字段名称（英文） | 管理员输入 | 可编辑 |
| field_type | VARCHAR(50) | 字段类型 | 管理员选择 | 可编辑 |
| field_group_id | BIGINT | 所属分组ID | 管理员选择 | 可编辑 |
| is_required | BOOLEAN | 是否必填 | 管理员配置 | 可编辑 |
| is_extended | BOOLEAN | 是否为拓展字段 | 系统默认false | 不可编辑 |
| description | TEXT | 字段说明 | 管理员输入 | 可编辑 |
| example_value | TEXT | 示例值 | 管理员输入 | 可编辑 |
| enum_options | JSON | 枚举选项 | 管理员配置 | 可编辑 |
| sort_order | INT | 排序顺序 | 系统自动/手动 | 可编辑 |
| is_active | BOOLEAN | 是否启用 | 管理员配置 | 可编辑 |
| is_deleted | BOOLEAN | 软删除标记 | 系统自动 | 删除时设置 |

#### 7.2 字段分组表（field_groups）

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| group_key | VARCHAR(100) | 分组标识 | 管理员输入 | 创建时设置 |
| group_name | VARCHAR(200) | 分组名称（中文） | 管理员输入 | 可编辑 |
| group_name_en | VARCHAR(200) | 分组名称（英文） | 管理员输入 | 可编辑 |
| parent_id | BIGINT | 父分组ID | 管理员选择 | 可编辑 |
| sort_order | INT | 排序顺序 | 系统自动/手动 | 可编辑 |
| is_active | BOOLEAN | 是否启用 | 管理员配置 | 可编辑 |

#### 7.3 自定义字段表（custom_fields）

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| tenant_id | BIGINT | 所属甲方ID | 系统自动 | - |
| field_key | VARCHAR(100) | 字段唯一标识 | 管理员输入 | 创建时设置 |
| field_name | VARCHAR(200) | 字段名称 | 管理员输入 | 可编辑 |
| field_type | VARCHAR(50) | 字段类型 | 管理员选择 | 可编辑 |
| field_group_id | BIGINT | 所属分组ID | 管理员选择 | 可编辑 |
| is_required | BOOLEAN | 是否必填 | 管理员配置 | 可编辑 |
| description | TEXT | 字段说明 | 管理员输入 | 可编辑 |
| enum_options | JSON | 枚举选项 | 管理员配置 | 可编辑 |
| sort_order | INT | 排序顺序 | 系统自动/手动 | 可编辑 |
| is_active | BOOLEAN | 是否启用 | 管理员配置 | 可编辑 |

#### 7.4 甲方字段JSON版本表（tenant_fields_json）

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| tenant_id | BIGINT | 甲方ID | 系统自动 | - |
| version | VARCHAR(20) | JSON版本号 | JSON文件 | 上传时设置 |
| sync_time | DATETIME | 同步时间（版本时间） | JSON文件 | 上传时设置 |
| fields_json | JSON | 字段定义JSON（完整JSON） | JSON文件 | 上传时设置 |
| is_current | TINYINT(1) | 是否当前版本 | 系统自动 | 上传时更新 |
| uploaded_by | VARCHAR(100) | 上传人 | 系统自动 | 上传时设置 |
| uploaded_at | DATETIME | 上传时间 | 系统自动 | 上传时设置 |

#### 7.5 甲方字段展示配置表（tenant_field_display_configs）

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| tenant_id | BIGINT | 所属甲方ID | 系统自动 | - |
| scene_type | VARCHAR(50) | 场景类型 | 管理员选择 | 可编辑 |
| field_key | VARCHAR(100) | 字段标识 | 管理员选择 | 可编辑 |
| field_source | VARCHAR(20) | 字段来源 | 系统自动 | - |
| sort_order | INT | 排序顺序 | 管理员配置 | 可编辑 |
| is_filterable | BOOLEAN | 是否可筛选 | 管理员配置 | 可编辑 |
| is_range_searchable | BOOLEAN | 是否支持范围检索 | 管理员配置 | 可编辑 |
| privacy_settings | JSON | 隐私设置 | 管理员配置 | 可编辑 |

---

### 8. 交互与信息展示（UX & UI Brief）

#### 8.1 标准字段管理页面

**布局**：
- 左侧：字段分组树形结构（可折叠）
- 右侧：字段列表（表格形式）

**字段列表表格列**：
- 拖拽手柄（用于排序）
- 字段名称
- 字段标识
- 字段类型
- 所属分组
- 是否必填
- 是否拓展字段
- 排序
- 操作（编辑、启用/禁用、删除）

**功能**：
- 支持按分组筛选
- 支持字段搜索
- 支持拖拽排序
- 支持批量启用/禁用

#### 8.2 字段分组管理页面

**布局**：
- 树形结构展示分组
- 支持添加、编辑、删除分组
- 支持拖拽调整分组顺序和层级

**分组表单**：
- 分组标识（必填，英文）
- 分组名称（必填，中文）
- 分组名称（英文）
- 父分组（下拉选择，可选）
- 排序（数字输入）

#### 8.3 自定义拓展字段管理页面

**布局**：
- 左侧：字段分组树形结构
- 右侧：拓展字段列表（表格形式）

**字段列表表格列**：
- 扩展字段别名
- 甲方原始字段（字段名称 + 字段标识）
- 类型
- 隐私标签（PII/敏感/公开）
- 是否必填
- 操作（编辑、删除）

#### 8.4 甲方字段展示配置页面

**布局**：
- 顶部：甲方选择器、场景类型选择器
- 左侧：可用字段列表（按分组展示）
- 右侧：已配置字段列表（可拖拽排序）

**配置项**：
- 排序：拖拽调整或输入数字
- 是否可筛选：开关（仅枚举字段）
- 是否支持范围检索：开关（仅数字/日期字段）
- 隐私设置：配置字段的隐私级别和脱敏规则

#### 8.5 甲方字段查看页面（JSON文件上传）

**布局**：
- 顶部：甲方选择器、"上传JSON文件"按钮
- 左侧：字段分组树形结构
- 右侧：字段列表（表格形式）

**上传JSON文件弹窗**：

```
┌─────────────────────────────────────────────────────┐
│  为甲方"XXX"上传JSON文件                    [×]      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  [选择文件] tenant_fields_v2.json                    │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │ 版本对比结果                                    │ │
│  ├────────────────────────────────────────────────┤ │
│  │                                                 │ │
│  │ 📊 统计信息：                                    │ │
│  │ • 新增字段：2个                                  │ │
│  │ • 删除字段：1个                                  │ │
│  │ • 修改字段：3个                                  │ │
│  │                                                 │ │
│  │ ✅ 新增字段（2个）：                             │ │
│  │   • company_name - 公司名称 (String)            │ │
│  │   • employee_id - 员工编号 (String)             │ │
│  │                                                 │ │
│  │ ❌ 删除字段（1个）：                             │ │
│  │   • old_field - 旧字段 (已删除)                 │ │
│  │                                                 │ │
│  │ ⚠️  修改字段（3个）：                            │ │
│  │   • user_name                                   │ │
│  │     - 字段名称: "用户姓名" → "客户姓名"         │ │
│  │     - 字段类型: "String" → "Text"               │ │
│  │   • case_status                                 │ │
│  │     - 枚举值变化:                                │ │
│  │       + 新增: "已结清"                           │ │
│  │       - 删除: "待处理"                           │ │
│  │       ~ 修改: "进行中" → "处理中"                │ │
│  │                                                 │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  [取消]  [确认保存]                                  │
└─────────────────────────────────────────────────────┘
```

**错误提示格式**：

```
┌─────────────────────────────────────────────────────┐
│  JSON格式校验失败                                    │
├─────────────────────────────────────────────────────┤
│                                                      │
│  发现以下错误：                                       │
│                                                      │
│  1. 缺少必填字段：fields[0].field_key                │
│  2. 字段类型无效：fields[1].field_type = "Invalid"  │
│  3. 枚举值格式错误：fields[2].enum_values           │
│     - 缺少value字段                                  │
│  4. 字段分组不存在：fields[3].field_group_id = 999  │
│  5. 字段标识重复：fields[4].field_key = "USER_ID"   │
│                                                      │
│  [关闭]                                             │
└─────────────────────────────────────────────────────┘
```

---

### 9. 配置项与运营开关（Config & Operation Switches）

#### 9.1 字段类型配置

**可配置的字段类型**：
- String（文本）
- Integer（整数）
- Decimal（小数）
- Date（日期）
- Datetime（日期时间）
- Boolean（布尔）
- Enum（枚举）

**配置入口**：标准字段管理 → 添加/编辑字段 → 字段类型下拉选择

#### 9.2 字段分组配置

**默认分组**：
- 客户基础信息
- 贷款详情
- 借款记录
- 还款记录
- 分期详情

**配置入口**：字段分组管理 → 添加/编辑分组

#### 9.3 隐私标签配置

**可配置的隐私标签**：
- PII（个人身份信息）
- 敏感
- 公开

**配置入口**：自定义拓展字段管理 → 添加/编辑字段 → 隐私标签选择

#### 9.4 JSON文件上传配置

**文件大小限制**：最大10MB（可配置）

**配置入口**：系统设置 → 文件上传配置

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 进入标准字段管理页面 | 查看标准字段管理页面 | view_standard_fields_page | tenant_id, user_id |
| 添加标准字段 | 添加标准字段 | add_standard_field | tenant_id, user_id, field_key, field_type |
| 编辑标准字段 | 编辑标准字段 | edit_standard_field | tenant_id, user_id, field_id, field_key |
| 删除标准字段 | 删除标准字段 | delete_standard_field | tenant_id, user_id, field_id |
| 拖拽字段排序 | 拖拽字段排序 | drag_field_sort | tenant_id, user_id, field_id, new_sort_order |
| 进入字段分组管理页面 | 查看字段分组管理页面 | view_field_groups_page | tenant_id, user_id |
| 添加字段分组 | 添加字段分组 | add_field_group | tenant_id, user_id, group_key, parent_id |
| 进入拓展字段管理页面 | 查看拓展字段管理页面 | view_extended_fields_page | tenant_id, user_id |
| 添加拓展字段 | 添加拓展字段 | add_extended_field | tenant_id, user_id, field_key, field_type |
| 进入字段展示配置页面 | 查看字段展示配置页面 | view_field_display_config_page | tenant_id, user_id, scene_type |
| 配置字段展示属性 | 配置字段展示属性 | config_field_display | tenant_id, user_id, scene_type, field_key, config_type |
| 进入甲方字段查看页面 | 查看甲方字段页面 | view_tenant_fields_page | tenant_id, user_id |
| 点击上传JSON文件 | 点击上传JSON文件按钮 | click_upload_json_file | tenant_id, user_id |
| 选择JSON文件 | 选择JSON文件 | select_json_file | tenant_id, user_id, file_name, file_size |
| JSON格式校验失败 | JSON格式校验失败 | json_validation_failed | tenant_id, user_id, error_count, error_details |
| JSON格式校验成功 | JSON格式校验成功 | json_validation_success | tenant_id, user_id, field_count |
| 版本对比完成 | 版本对比完成 | version_compare_complete | tenant_id, user_id, added_count, deleted_count, modified_count |
| 确认保存JSON | 确认保存JSON文件 | confirm_save_json | tenant_id, user_id, version, field_count |
| 取消保存JSON | 取消保存JSON文件 | cancel_save_json | tenant_id, user_id |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 系统架构与模块划分（System Architecture & Modules）

#### 1.1 模块划分

```
字段配置管理模块
│
├── 标准字段管理模块
│   ├── StandardFieldController（标准字段API）
│   ├── StandardFieldService（标准字段业务逻辑）
│   └── StandardFieldMapper（标准字段数据访问）
│
├── 字段分组管理模块
│   ├── FieldGroupController（字段分组API）
│   ├── FieldGroupService（字段分组业务逻辑）
│   └── FieldGroupMapper（字段分组数据访问）
│
├── 自定义字段管理模块
│   ├── CustomFieldController（自定义字段API）
│   ├── CustomFieldService（自定义字段业务逻辑）
│   └── CustomFieldMapper（自定义字段数据访问）
│
├── 甲方字段展示配置模块
│   ├── FieldDisplayConfigController（字段展示配置API）
│   ├── FieldDisplayConfigService（字段展示配置业务逻辑）
│   └── TenantFieldDisplayConfigMapper（字段展示配置数据访问）
│
└── 甲方字段JSON管理模块
    ├── TenantFieldsJsonController（JSON文件上传API）
    ├── TenantFieldsJsonService（JSON解析和对比业务逻辑）
    └── TenantFieldsJsonMapper（JSON版本数据访问）
```

#### 1.2 调用关系

```
前端页面
    ↓
Controller层（API接口）
    ↓
Service层（业务逻辑）
    ↓
Mapper层（数据访问）
    ↓
数据库（MySQL）
```

---

### 2. 接口设计与系统依赖（API Design & Dependencies）

#### 2.1 标准字段管理接口

**2.1.1 获取标准字段列表**
- **接口**：`GET /api/v1/standard-fields`
- **请求参数**：
  - `field_group_id`（可选）：字段分组ID，用于筛选
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "field_key": "user_id",
      "field_name": "用户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "is_extended": false,
      "sort_order": 1,
      "is_active": true
    }
  ]
}
```

**2.1.2 创建标准字段**
- **接口**：`POST /api/v1/standard-fields`
- **请求体**：
```json
{
  "field_key": "new_field",
  "field_name": "新字段",
  "field_type": "String",
  "field_group_id": 1,
  "is_required": false,
  "description": "字段说明",
  "sort_order": 10
}
```
- **响应格式**：同获取接口

**2.1.3 更新标准字段**
- **接口**：`PUT /api/v1/standard-fields/{id}`
- **请求体**：同创建接口
- **响应格式**：同获取接口

**2.1.4 删除标准字段**
- **接口**：`DELETE /api/v1/standard-fields/{id}`
- **响应格式**：
```json
{
  "code": 200,
  "message": "删除成功"
}
```

**2.1.5 批量更新字段排序**
- **接口**：`PUT /api/v1/standard-fields/sort`
- **请求体**：
```json
{
  "sorts": [
    {"id": 1, "sort_order": 1},
    {"id": 2, "sort_order": 2}
  ]
}
```

#### 2.2 字段分组管理接口

**2.2.1 获取字段分组列表**
- **接口**：`GET /api/v1/field-groups`
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "group_key": "customer_basic",
      "group_name": "客户基础信息",
      "parent_id": null,
      "sort_order": 1,
      "children": [...]
    }
  ]
}
```

**2.2.2 创建字段分组**
- **接口**：`POST /api/v1/field-groups`
- **请求体**：
```json
{
  "group_key": "new_group",
  "group_name": "新分组",
  "parent_id": null,
  "sort_order": 10
}
```

**2.2.3 更新字段分组**
- **接口**：`PUT /api/v1/field-groups/{id}`
- **请求体**：同创建接口

**2.2.4 删除字段分组**
- **接口**：`DELETE /api/v1/field-groups/{id}`
- **前置检查**：检查是否有字段关联该分组

**2.2.5 批量更新分组排序**
- **接口**：`PUT /api/v1/field-groups/sort`
- **请求体**：
```json
{
  "sorts": [
    {"id": 1, "sort_order": 1, "parent_id": null},
    {"id": 2, "sort_order": 2, "parent_id": 1}
  ]
}
```

#### 2.3 自定义拓展字段管理接口

**2.3.1 获取拓展字段列表**
- **接口**：`GET /api/v1/tenants/{tenantId}/extended-fields`
- **请求参数**：
  - `field_group_id`（可选）：字段分组ID
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "tenant_id": 1,
      "field_alias": "company_name",
      "tenant_field_key": "COMPANY_NAME",
      "tenant_field_name": "公司名称",
      "field_type": "String",
      "field_group_id": 1,
      "privacy_label": "公开",
      "is_required": false
    }
  ]
}
```

**2.3.2 创建拓展字段**
- **接口**：`POST /api/v1/tenants/{tenantId}/extended-fields`
- **请求体**：
```json
{
  "field_alias": "company_name",
  "tenant_field_key": "COMPANY_NAME",
  "tenant_field_name": "公司名称",
  "field_type": "String",
  "field_group_id": 1,
  "privacy_label": "公开",
  "is_required": false
}
```

**2.3.3 更新拓展字段**
- **接口**：`PUT /api/v1/tenants/{tenantId}/extended-fields/{id}`
- **请求体**：同创建接口

**2.3.4 删除拓展字段**
- **接口**：`DELETE /api/v1/tenants/{tenantId}/extended-fields/{id}`

#### 2.4 甲方字段展示配置接口

**2.4.1 获取字段展示配置列表**
- **接口**：`GET /api/v1/field-display-configs`
- **请求参数**：
  - `tenant_id`（必填）：甲方ID
  - `scene_type`（可选）：场景类型
  - `field_key`（可选）：字段标识
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "tenant_id": 1,
      "scene_type": "admin_case_list",
      "field_key": "user_name",
      "field_name": "用户姓名",
      "field_source": "standard",
      "sort_order": 1,
      "is_filterable": false,
      "is_range_searchable": false
    }
  ]
}
```

**2.4.2 批量创建或更新字段展示配置**
- **接口**：`PUT /api/v1/field-display-configs/batch`
- **请求体**：
```json
{
  "tenant_id": 1,
  "scene_type": "admin_case_list",
  "configs": [
    {
      "field_key": "user_name",
      "sort_order": 1,
      "is_filterable": false,
      "is_range_searchable": false
    }
  ]
}
```

#### 2.5 甲方字段JSON管理接口

**2.5.1 获取甲方字段JSON数据**
- **接口**：`GET /api/v1/tenants/{tenantId}/fields-json`
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fetched_at": "2025-11-25T10:30:00",
    "fields": [
      {
        "id": 1,
        "field_key": "user_id",
        "field_name": "用户ID",
        "field_type": "String",
        "field_group_id": 1,
        "is_required": true
      }
    ]
  }
}
```

**2.5.2 校验JSON文件格式**
- **接口**：`POST /api/v1/tenants/{tenantId}/fields-json/validate`
- **请求体**（FormData）：
  - `file`：JSON文件（multipart/form-data）
- **响应格式（成功）**：
```json
{
  "code": 200,
  "message": "校验通过",
  "data": {
    "valid": true,
    "field_count": 50,
    "version": "1.0",
    "sync_time": "2025-11-25T10:30:00Z"
  }
}
```
- **响应格式（失败）**：
```json
{
  "code": 400,
  "message": "校验失败",
  "data": {
    "valid": false,
    "errors": [
      {
        "path": "fields[0].field_key",
        "message": "缺少必填字段：field_key"
      },
      {
        "path": "fields[1].field_type",
        "message": "字段类型无效：Invalid，必须是String/Integer/Decimal/Date/Datetime/Boolean/Enum"
      }
    ]
  }
}
```

**2.5.3 对比版本差异**
- **接口**：`POST /api/v1/tenants/{tenantId}/fields-json/compare`
- **请求体**：
```json
{
  "fields_json": {
    "version": "1.0",
    "sync_time": "2025-11-25T10:30:00Z",
    "fields": [...]
  }
}
```
- **响应格式**：
```json
{
  "code": 200,
  "message": "对比完成",
  "data": {
    "added_fields": [
      {
        "field_key": "company_name",
        "field_name": "公司名称",
        "field_type": "String"
      }
    ],
    "deleted_fields": [
      {
        "field_key": "old_field",
        "field_name": "旧字段",
        "field_type": "String"
      }
    ],
    "modified_fields": [
      {
        "field_key": "user_name",
        "changes": {
          "field_name": {
            "old": "用户姓名",
            "new": "客户姓名"
          },
          "field_type": {
            "old": "String",
            "new": "Text"
          }
        }
      },
      {
        "field_key": "case_status",
        "changes": {
          "enum_values": {
            "added": [
              {"value": "settled", "label": "已结清"}
            ],
            "deleted": [
              {"value": "pending", "label": "待处理"}
            ],
            "modified": [
              {
                "old": {"value": "processing", "label": "进行中"},
                "new": {"value": "processing", "label": "处理中"}
              }
            ]
          }
        }
      }
    ]
  }
}
```

**2.5.4 上传并保存JSON文件**
- **接口**：`POST /api/v1/tenants/{tenantId}/fields-json/upload`
- **请求体**：
```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [...]
}
```
- **响应格式**：
```json
{
  "code": 200,
  "message": "保存成功",
  "data": {
    "id": 1,
    "tenant_id": 1,
    "version": "1.0",
    "sync_time": "2025-11-25T10:30:00Z",
    "is_current": true,
    "uploaded_at": "2025-11-25T10:35:00"
  }
}
```

**2.5.5 获取历史版本列表**
- **接口**：`GET /api/v1/tenants/{tenantId}/fields-json/history`
- **响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "version": "1.0",
      "sync_time": "2025-11-25T10:30:00Z",
      "is_current": true,
      "uploaded_by": "admin",
      "uploaded_at": "2025-11-25T10:35:00"
    },
    {
      "id": 2,
      "version": "0.9",
      "sync_time": "2025-11-20T08:00:00Z",
      "is_current": false,
      "uploaded_by": "admin",
      "uploaded_at": "2025-11-20T08:05:00"
    }
  ]
}
```

#### 2.6 接口依赖

**内部依赖**：
- 权限验证服务：验证用户权限（SuperAdmin或TenantAdmin）
- 字段分组服务：校验field_group_id是否存在
- 标准字段服务：获取标准字段列表用于对比

**外部依赖**：
- 无

#### 2.7 超时与重试策略

- **超时时间**：30秒
- **重试策略**：不重试（文件上传操作不支持重试）
- **幂等要求**：JSON文件上传接口需要幂等（相同version和sync_time不重复保存）

#### 2.8 失败降级逻辑

- **JSON格式校验失败**：返回详细错误信息，不保存
- **版本对比失败**：返回错误信息，不保存
- **保存失败**：返回错误信息，不更新数据库

---

### 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

#### 3.1 新增表结构

**3.1.1 甲方字段JSON版本表（tenant_fields_json）**

```sql
CREATE TABLE IF NOT EXISTS `tenant_fields_json` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '甲方ID',
    `version` VARCHAR(20) COMMENT 'JSON版本号',
    `sync_time` DATETIME NOT NULL COMMENT '同步时间（版本时间）',
    `fields_json` JSON NOT NULL COMMENT '字段定义JSON（完整JSON）',
    `is_current` TINYINT(1) DEFAULT 1 COMMENT '是否当前版本（1=当前，0=历史）',
    `uploaded_by` VARCHAR(100) COMMENT '上传人',
    `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_is_current` (`is_current`),
    KEY `idx_uploaded_at` (`uploaded_at`),
    FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方字段JSON版本表';
```

**主键/唯一键说明**：
- 主键：`id`（自增）
- 索引：`tenant_id`、`is_current`、`uploaded_at`
- 外键：`tenant_id` → `tenants.id`

**数据保留策略**：
- 只保留当前版本和1个历史版本
- 上传新版本时，自动将当前版本标记为历史（is_current=0）
- 如果已有历史版本，删除最旧的历史版本

#### 3.2 依赖的现有表

- `standard_fields`：标准字段定义表
- `field_groups`：字段分组表
- `custom_fields`：自定义字段表
- `tenant_field_display_configs`：甲方字段展示配置表
- `tenants`：甲方配置表

#### 3.3 版本管理方式

- **版本标识**：使用`version`字段（JSON文件中的版本号）
- **当前版本标识**：使用`is_current`字段（1=当前，0=历史）
- **版本切换**：上传新版本时，自动切换版本标识
- **回滚方案**：支持将历史版本恢复为当前版本（通过更新`is_current`字段）

---

### 4. 非功能性要求（Non-Functional Requirements）

#### 4.1 性能目标

- **JSON文件解析性能**：解析10MB的JSON文件≤2秒
- **版本对比性能**：对比100个字段的差异≤1秒
- **接口响应时间**：所有接口响应时间≤500ms（除文件上传）
- **文件上传性能**：上传10MB文件≤5秒

#### 4.2 可用性要求

- **SLA**：接口可用性≥99.9%
- **降级策略**：JSON文件上传失败时，返回详细错误信息，不保存数据
- **错误处理**：所有错误都有明确的错误提示

#### 4.3 安全要求

- **接口鉴权**：所有接口需要Token验证
- **权限控制**：JSON文件上传接口需要SuperAdmin或TenantAdmin权限
- **文件大小限制**：JSON文件最大10MB
- **文件类型限制**：只允许.json文件
- **数据加密**：敏感字段（如隐私标签）需要加密存储（可选）

#### 4.4 扩展性要求

- **字段类型扩展**：支持新增字段类型（通过配置）
- **场景类型扩展**：支持新增场景类型（通过配置）
- **版本历史扩展**：支持保留多个历史版本（通过配置）

---

### 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

#### 5.1 关键日志

**必须记录的日志**：
- JSON文件上传请求（包含文件大小、文件名）
- JSON格式校验结果（包含错误详情）
- 版本对比结果（包含差异统计）
- JSON文件保存结果（包含版本号、字段数量）
- 权限验证结果（包含用户角色）

**日志格式**：
```
[INFO] JSON文件上传请求 - tenantId=1, fileName=tenant_fields_v2.json, fileSize=1024KB, userId=admin
[INFO] JSON格式校验成功 - tenantId=1, fieldCount=50, version=1.0
[INFO] 版本对比完成 - tenantId=1, addedCount=2, deletedCount=1, modifiedCount=3
[INFO] JSON文件保存成功 - tenantId=1, version=1.0, id=1, isCurrent=true
[ERROR] JSON格式校验失败 - tenantId=1, errorCount=3, errors=[...]
```

#### 5.2 监控指标

**关键指标**：
- JSON文件上传成功率
- JSON格式校验失败率
- 版本对比耗时
- 文件上传耗时
- 接口错误率

**告警规则**：
- JSON文件上传失败率>5%：发送告警
- 版本对比耗时>3秒：发送告警
- 接口错误率>1%：发送告警

---

### 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

#### 6.1 测试类型

**单元测试**：
- JSON格式校验逻辑测试
- 版本对比算法测试
- 字段类型校验测试
- 枚举值对比测试

**集成测试**：
- JSON文件上传接口测试
- 版本对比接口测试
- 权限验证测试

**回归测试**：
- 标准字段管理功能回归
- 字段分组管理功能回归
- 自定义字段管理功能回归

#### 6.2 验收标准

**关键验收标准**：
1. ✅ JSON格式校验准确率100%：所有格式错误的JSON都能准确识别并提示
2. ✅ 版本对比准确率100%：所有字段差异都能准确识别
3. ✅ 文件上传成功率≥99%：格式正确的JSON文件上传成功率≥99%
4. ✅ 权限控制有效：只有SuperAdmin和TenantAdmin可以上传JSON文件
5. ✅ 版本管理正确：只保留当前版本和1个历史版本

---

### 7. 发布计划与回滚预案（Release Plan & Rollback）

#### 7.1 发布策略

**灰度发布**：
- 第一阶段：内部测试环境验证（1周）
- 第二阶段：小范围甲方测试（选择1-2个甲方，1周）
- 第三阶段：全量发布（所有甲方）

**流量比例**：
- 内部测试：100%
- 小范围测试：10%
- 全量发布：100%

#### 7.2 配置/开关切换步骤

**功能开关**：
- `field_config.json_upload_enabled`：JSON文件上传功能开关（默认开启）
- `field_config.json_max_size`：JSON文件最大大小（默认10MB）

**切换步骤**：
1. 检查功能开关状态
2. 验证数据库表结构
3. 验证接口权限配置
4. 逐步开启功能

#### 7.3 回滚方案

**回滚步骤**：
1. 关闭JSON文件上传功能开关
2. 隐藏前端"上传JSON文件"按钮
3. 保留已上传的JSON数据（不删除）
4. 修复问题后重新发布

**应急联系人**：
- 技术负责人：XXX
- 产品负责人：XXX
- 运维负责人：XXX

---

## 四、功能清单

### 1. 标准字段管理
- ✅ 标准字段的增删改查
- ✅ 字段分组筛选
- ✅ 字段搜索
- ✅ 拖拽排序
- ✅ 批量启用/禁用

### 2. 字段分组管理
- ✅ 字段分组的增删改查
- ✅ 支持多级分组（父子关系）
- ✅ 拖拽调整分组顺序和层级
- ✅ 分组删除前检查字段关联

### 3. 自定义拓展字段管理
- ✅ 拓展字段的增删改查
- ✅ 字段分组筛选
- ✅ 隐私标签配置（PII/敏感/公开）
- ✅ 字段类型配置

### 4. 甲方字段展示配置
- ✅ 字段排序配置
- ✅ 字段筛选配置（枚举字段）
- ✅ 范围检索配置（数字/日期字段）
- ✅ 隐私设置配置

### 5. 甲方字段查看（JSON文件上传）
- ✅ 查看甲方字段列表
- ✅ 按分组筛选字段
- ✅ 上传JSON文件
- ✅ JSON格式校验
- ✅ 版本对比分析
- ✅ 差异展示（新增/删除/修改）
- ✅ 枚举值对比
- ✅ 确认保存/取消操作
- ✅ 权限控制（SuperAdmin/TenantAdmin）

---

## 五、附录

### A. JSON文件格式示例

```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [
    {
      "field_key": "USER_ID",
      "field_name": "用户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z",
      "description": "用户的唯一标识",
      "example_value": "12345678"
    },
    {
      "field_key": "CASE_STATUS",
      "field_name": "案件状态",
      "field_type": "Enum",
      "field_group_id": 2,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z",
      "enum_values": [
        {
          "value": "PENDING",
          "label": "待还款"
        },
        {
          "value": "PARTIAL",
          "label": "部分还款"
        },
        {
          "value": "SETTLED",
          "label": "正常结清"
        }
      ]
    }
  ]
}
```

### B. 字段类型枚举值

- `String`：文本类型
- `Integer`：整数类型
- `Decimal`：小数类型
- `Date`：日期类型（格式：YYYY-MM-DD）
- `Datetime`：日期时间类型（格式：ISO8601）
- `Boolean`：布尔类型（true/false）
- `Enum`：枚举类型（需提供enum_values）

### C. 场景类型枚举值

- `admin_case_list`：控台案件管理列表
- `collector_case_list`：催员案件列表
- `collector_case_detail`：催员案件详情

---

**文档版本**：v1.0.0  
**创建日期**：2025-11-25  
**最后更新**：2025-11-25  
**状态**：待评审

