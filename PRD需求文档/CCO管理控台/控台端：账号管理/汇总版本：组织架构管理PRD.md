# 组织架构管理PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

随着催收业务规模的扩大和复杂度的提升，需要建立清晰、灵活的组织架构管理体系，以支持多甲方、多机构、多小组的催收业务场景。当前系统缺乏统一的组织架构管理能力，导致无法有效管理催收团队、分配案件、统计业绩，也无法灵活配置不同机构的业务规则（如作息时间、质检规则等）。本需求旨在构建完整的组织架构管理功能，实现从甲方到催员的五级管理体系，支持组织架构的创建、编辑、启用/禁用等全生命周期管理，并支持机构级别的业务规则配置（如作息时间），为后续的案件分配、业绩统计、权限管理等核心功能提供基础支撑。预期通过本功能提升组织管理效率30%以上，减少因组织架构不清晰导致的案件分配错误率，并为精细化运营提供数据基础。

### 2. 业务场景与用户画像（Business Scenario & User）

**典型使用场景：**

1. **系统初始化场景**：系统超级管理员（SuperAdmin）创建甲方，并创建甲方管理员账号；甲方管理员创建催收机构，并创建机构管理员账号；机构管理员创建小组群、小组，并创建小组管理员和催员账号。

2. **日常管理场景**：甲方管理员查看和管理本甲方下的所有机构；机构管理员查看和管理本机构下的所有小组群、小组和催员；小组管理员管理本小组下的催员。

3. **业务配置场景**：机构管理员配置机构的作息时间，用于质检、通知等环节的时间控制；系统根据机构作息时间判断是否在营业时间内，决定是否发送通知、是否进行质检等。

4. **组织调整场景**：因业务调整需要禁用某个机构，系统会级联影响其下所有小组群、小组和催员；需要将催员从一个小组调整到另一个小组。

**主要用户类型：**

- **系统超级管理员（SuperAdmin）**：系统级管理员，负责创建甲方和甲方管理员账号，拥有最高权限。
- **甲方管理员（TenantAdmin）**：甲方级别的管理员，负责管理本甲方的所有催收机构、小组群、小组、小组管理员和催员，配置本甲方的业务规则。
- **机构管理员（AgencyAdmin）**：机构级别的管理员，负责管理本机构下的所有小组群、小组和催员，配置机构的作息时间等业务规则。
- **小组管理员（TeamAdmin）**：小组级别的管理员，负责管理本小组下的催员，分配案件，查看小组业绩。

**核心诉求：**

- 能够清晰地查看和管理组织架构的各个层级
- 能够快速创建和配置组织架构
- 能够灵活调整组织架构（启用/禁用、调整归属关系）
- 能够配置机构级别的业务规则（如作息时间）
- 能够查看组织架构的统计信息（小组数量、催员数量等）

### 3. 关键业务流程（Business Flow）

#### 3.1 组织架构创建流程

```mermaid
[系统超级管理员]
    ↓ 创建甲方
[甲方] + [甲方管理员账号]
    ↓ 甲方管理员登录，创建机构
[催收机构] + [机构管理员账号]
    ↓ 机构管理员登录，创建小组群
[小组群] + [小组群长SPV]
    ↓ 创建小组
[催收小组] + [小组管理员账号]
    ↓ 创建催员
[催员账号]
```

**详细步骤：**

1. **创建甲方**（SuperAdmin操作）
   - 输入甲方基本信息：名称、编码、国家、时区、货币、**默认语言**（从国际化配置中选择）
   - 输入甲方管理员信息：账号名、登录ID、邮箱、密码、确认密码（必填）
   - 系统同时创建甲方记录和甲方管理员账号
   - 甲方管理员收到账号信息
   - **该甲方下所有催员登录IM端时，默认使用此语言**

2. **创建机构**（甲方管理员操作）
   - 选择所属甲方（通常为当前登录用户的甲方）
   - 输入机构信息：机构编码、机构名称、时区（必填）、联系方式
   - 输入机构管理员信息：账号名、登录ID、邮箱、密码、确认密码（必填）
   - 系统同时创建机构记录和机构管理员账号
   - 机构管理员收到账号信息

3. **配置机构作息时间**（机构管理员操作）
   - 进入机构管理页面
   - 选择目标机构，点击"配置作息时间"
   - 设置工作日、工作时间段（如：周一至周五 09:00-18:00）
   - 可设置多个时间段（如：上午 09:00-12:00，下午 14:00-18:00）
   - 保存配置，系统应用于质检、通知等环节

4. **创建小组群**（机构管理员操作）
   - 选择所属甲方和机构
   - 输入小组群信息：小组群编码、名称、描述
   - 输入小组群管理员信息：账号名、登录ID、邮箱、密码、确认密码（必填）
   - 系统同时创建小组群记录和小组群管理员账号

5. **创建小组**（机构管理员操作）
   - 选择所属甲方、机构和小组群（可选）
   - 输入小组信息：小组编码、名称
   - 选择小组组长（从催员中选择）
   - 系统创建小组记录

6. **创建小组管理员**（机构管理员或小组管理员操作）
   - 选择所属甲方、机构、小组
   - 输入管理员信息：账号名、登录ID、邮箱、密码
   - 系统创建小组管理员账号

7. **创建催员**（机构管理员或小组管理员操作）
   - 选择所属甲方、机构、小组
   - 输入催员信息：催员编码、姓名、登录账号、密码
   - 选择角色：催员/组长
   - 系统创建催员账号

#### 3.2 组织架构查询流程

```
[用户登录]
    ↓ 根据角色权限
[查看组织架构]
    ├── 甲方管理员：查看本甲方下所有机构
    ├── 机构管理员：查看本机构下所有小组群、小组、催员
    └── 小组管理员：查看本小组下所有催员
    ↓ 筛选和搜索
[展示列表]
    ├── 统计信息（小组数量、催员数量）
    ├── 状态信息（启用/禁用）
    └── 操作按钮（编辑、启用/禁用）
```

#### 3.3 组织架构调整流程

```
[选择目标组织单元]
    ↓
[执行操作]
    ├── 编辑：修改基本信息
    ├── 启用/禁用：改变状态
    └── 调整归属：修改上级关系
    ↓
[系统验证]
    ├── 检查是否有下级组织单元
    ├── 检查是否有案件关联
    └── 检查权限是否允许
    ↓
[执行操作并级联影响]
    ├── 禁用机构 → 禁用其下所有小组群、小组、催员
    ├── 禁用小组 → 禁用其下所有催员
    └── 调整催员归属 → 更新案件分配关系
```

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 组织架构层级规则

- **层级关系**：甲方 → 机构 → 小组群（可选）→ 小组 → 催员，必须严格按照层级关系创建和管理
- **唯一性规则**：
  - 甲方编码在系统内唯一
  - 机构编码在系统内唯一（通过"甲方编码-"前缀保证）
  - 小组群编码在系统内唯一（通过"甲方编码-"前缀保证）
  - 小组编码在系统内唯一（通过"甲方编码-"前缀保证）
  - 催员编码在系统内唯一（通过"甲方编码-"前缀保证）
  - 所有登录账号（login_id）在系统内唯一（通过"甲方编码-"前缀保证）
- **编码前缀规则**：详见 [4.7 编码规范](#47-编码规范)
- **必填字段**：
  - 甲方：名称、编码、国家、时区、货币、**默认语言**、管理员密码、管理员确认密码
  - 机构：编码（含甲方前缀）、名称、所属甲方、时区、管理员密码、管理员确认密码
  - 小组群：编码（含甲方前缀）、名称、所属机构、管理员密码、管理员确认密码
  - 小组：编码（含甲方前缀）、名称、所属机构
  - 催员：编码（含甲方前缀）、姓名、登录账号（含甲方前缀）、密码

#### 4.2 启用/禁用规则

- **禁用机构**：会级联禁用其下所有小组群、小组和催员，但不会删除数据
- **禁用小组群**：会级联禁用其下所有小组和催员
- **禁用小组**：会级联禁用其下所有催员
- **禁用催员**：催员无法登录系统，但历史数据保留
- **启用规则**：启用上级组织单元时，不会自动启用下级组织单元，需要手动启用

#### 4.3 删除规则

- **甲方和小组管理员**：不支持删除操作，只能通过启用/禁用来控制状态
- **其他组织单元**：支持删除，但有以下限制：
  - 如果组织单元下有关联的小组、催员或案件，不允许删除
  - 必须先删除或转移所有下级组织单元，才能删除上级组织单元
- **软删除**：系统采用软删除机制，禁用而非物理删除

#### 4.4 机构作息时间规则

- **配置范围**：每个机构可以独立配置作息时间
- **时间格式**：支持设置工作日（周一至周日）和工作时间段（如 09:00-18:00）
- **多时间段**：支持设置多个时间段（如：上午 09:00-12:00，下午 14:00-18:00）
- **应用场景**：
  - 质检：只在营业时间内进行质检
  - 通知：只在营业时间内发送通知
  - 案件分配：优先在营业时间内分配案件
- **默认值**：如果机构未配置作息时间，使用系统默认值（周一至周五 09:00-18:00）

#### 4.5 权限规则

- **SuperAdmin**：可以管理所有甲方
- **甲方管理员**：只能管理本甲方下的组织架构
- **机构管理员**：只能管理本机构下的组织架构
- **小组管理员**：只能管理本小组下的催员
- **数据隔离**：不同甲方的数据完全隔离，无法跨甲方查看或操作

#### 4.6 账号管理规则

- **账号创建**：
  - 创建甲方时，必须同时创建甲方管理员账号，密码和确认密码为必填项
  - 创建机构时，必须同时创建机构管理员账号，密码和确认密码为必填项
  - 创建小组群时，必须同时创建小组群管理员账号，密码和确认密码为必填项
  - 创建小组管理员和催员时，必须设置初始密码
- **密码规则**：
  - 最少6位字符
  - 密码和确认密码必须一致
  - 建议包含字母和数字
  - 支持管理员重置密码
- **账号唯一性**：登录账号在系统内唯一，不能重复
- **手机号字段**：所有管理员和催员角色均不包含手机号字段

#### 4.7 编码规范

为了快速识别和区分不同甲方下的组织架构数据，系统采用统一的编码规范：

**4.7.1 编码前缀规则**

- **甲方编码**：无前缀要求，建议使用有意义的简称（如：`TENA`、`CLIENT01`）
- **机构编码**：必须以"甲方编码-"开头，后接自定义部分（如：`TENA-AGENCY01`）
- **小组群编码**：必须以"甲方编码-"开头，后接自定义部分（如：`TENA-GROUP01`）
- **小组编码**：必须以"甲方编码-"开头，后接自定义部分（如：`TENA-TEAM01`）
- **催员编码**：必须以"甲方编码-"开头，后接自定义部分（如：`TENA-COL001`）

**4.7.2 登录ID规则**

- **甲方管理员登录ID**：必须以"甲方编码-"开头（如：`TENA-admin01`）
- **机构管理员登录ID**：必须以"甲方编码-"开头（如：`TENA-agadmin01`）
- **小组群管理员登录ID**：必须以"甲方编码-"开头（如：`TENA-spv01`）
- **小组管理员登录ID**：必须以"甲方编码-"开头（如：`TENA-teamadmin01`）
- **催员登录ID**：必须以"甲方编码-"开头（如：`TENA-collector01`）

**4.7.3 编码示例**

假设甲方编码为 `ABC`，则完整的编码体系如下：

```
甲方: ABC
  ├── 机构: ABC-AG001 (北京机构)
  │   ├── 小组群: ABC-GP001 (一组群)
  │   │   ├── 小组: ABC-TM001 (第一小组)
  │   │   │   ├── 小组管理员: ABC-admin001 (组长张三)
  │   │   │   ├── 催员: ABC-col001 (催员李四)
  │   │   │   └── 催员: ABC-col002 (催员王五)
  │   │   └── 小组: ABC-TM002 (第二小组)
  │   └── 小组群: ABC-GP002 (二组群)
  └── 机构: ABC-AG002 (上海机构)
```

**4.7.4 编码优势**

- **快速识别**：通过编码前缀立即识别数据归属的甲方
- **避免冲突**：不同甲方的编码不会重复，便于数据合并和迁移
- **数据追溯**：在日志和报表中快速定位问题数据的来源
- **批量操作**：支持按前缀批量查询和处理数据

**4.7.5 系统实现**

- **前端自动填充**：在创建表单中，编码和登录ID字段默认自动填入"甲方编码-"前缀
- **后端验证**：创建时验证编码格式是否符合规范，不符合则拒绝创建
- **编码唯一性**：在符合前缀规范的基础上，确保编码在对应范围内唯一

**4.7.6 特殊说明**

- 甲方编码一旦创建不可修改，因为会影响所有下级组织的编码
- 如需修改甲方编码，需要通过数据迁移工具批量更新所有关联数据
- 建议甲方编码使用2-6位大写字母或字母数字组合，简洁易识别

#### 4.8 范围边界

**本次需求范围内：**
- 组织架构的创建、编辑、查询、启用/禁用
- 机构作息时间的配置
- 组织架构的统计信息展示
- 基本的权限控制（基于角色的数据隔离）
- 编码规范的系统实现和验证

**本次需求范围外：**
- 案件分配功能（后续需求）
- 业绩统计功能（后续需求）
- 权限的细粒度配置（后续需求）
- 组织架构的批量导入/导出（后续需求）
- 组织架构的历史记录和审计日志（后续需求）
- 编码的批量修改工具（后续需求）

### 5. 合规与风控要求（Compliance & Risk Control）

#### 5.1 数据隐私保护

- **敏感信息加密**：密码必须加密存储，使用安全的哈希算法（如BCrypt）
- **数据脱敏**：在日志和监控中，敏感信息（如密码、手机号）需要脱敏处理
- **访问控制**：只有授权用户才能查看和管理组织架构信息

#### 5.2 账号安全

- **密码策略**：强制要求密码符合安全策略，支持密码强度验证
- **登录审计**：记录所有账号的登录时间、IP地址等信息
- **账号锁定**：支持账号锁定机制，防止暴力破解

#### 5.3 操作审计

- **操作日志**：记录所有组织架构的创建、编辑、启用/禁用操作
- **操作人记录**：记录每次操作的执行人、时间、操作内容
- **数据变更历史**：保留关键字段的变更历史（如机构名称、催员归属关系）

#### 5.4 数据备份与恢复

- **数据备份**：定期备份组织架构数据，确保数据安全
- **数据恢复**：支持从备份恢复数据，支持误操作的快速回滚

### 6. 资金路径与结算规则（Funding Flow & Settlement）

**无**：组织架构管理功能不涉及资金流转，本节不适用。

### 7. 数据字段与口径（Data Definition）

#### 7.1 甲方（Tenant）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| tenant_id | BigInteger | 甲方ID（主键） | 系统生成 | 不变 |
| tenant_code | String(100) | 甲方编码（唯一） | 用户输入 | 创建时设置，不可修改 |
| tenant_name | String(200) | 甲方名称 | 用户输入 | 可编辑 |
| tenant_name_en | String(200) | 甲方名称（英文） | 用户输入 | 可编辑 |
| country_code | String(50) | 国家代码 | 用户选择 | 可编辑 |
| timezone | Integer | 时区偏移量（UTC±小时） | 用户选择 | 可编辑 |
| currency_code | String(10) | 货币代码 | 用户选择 | 可编辑 |
| **default_language** | **String(10)** | **默认语言（Locale）** | **从国际化配置中选择** | **可编辑** |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：
- **default_language**：甲方的默认语言设置，从国际化配置管理中的"已启用语言"列表中选择
- **业务规则**：
  - 创建甲方时必须选择默认语言（必填项）
  - 该语言将作为该甲方下所有催员的默认界面语言
  - 催员登录IM端时，默认使用所属甲方的语言
  - 催员可在个人设置中切换为其他可用语言，但优先显示甲方默认语言

#### 7.2 甲方管理员（TenantAdmin）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键ID | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| account_code | String(100) | 账号编码（唯一） | 系统生成 | 创建时设置，不可修改 |
| account_name | String(200) | 账号名称（管理员姓名） | 用户输入 | 可编辑 |
| login_id | String(100) | 登录ID（唯一） | 用户输入 | 创建时设置，不可修改 |
| password_hash | String(255) | 密码哈希（BCrypt加密） | 系统生成 | 可编辑（重置密码） |
| email | String(100) | 邮箱 | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| last_login_at | DateTime | 最近登录时间 | 系统更新 | 登录时更新 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：甲方管理员在创建甲方时同时创建，密码和确认密码为必填项。

#### 7.3 机构（CollectionAgency）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| agency_id | BigInteger | 机构ID（主键） | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置，不可修改 |
| agency_code | String(100) | 机构编码（唯一） | 用户输入 | 创建时设置，不可修改 |
| agency_name | String(200) | 机构名称 | 用户输入 | 可编辑 |
| agency_name_en | String(200) | 机构名称（英文） | 用户输入 | 可编辑 |
| timezone | Integer | 时区偏移量（必填） | 用户输入 | 可编辑 |
| contact_person | String(100) | 联系人 | 用户输入 | 可编辑 |
| contact_phone | String(50) | 联系电话 | 用户输入 | 可编辑 |
| contact_email | String(100) | 联系邮箱 | 用户输入 | 可编辑 |
| address | String(500) | 机构地址 | 用户输入 | 可编辑 |
| description | Text | 机构描述 | 用户输入 | 可编辑 |
| agency_type | String(20) | 机构类型（real/virtual） | 用户选择 | 可编辑 |
| sort_order | Integer | 排序顺序 | 用户设置 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**统计字段（实时计算）：**
- team_count：小组数量（统计该机构下所有小组）
- collector_count：催员数量（统计该机构下所有催员）

#### 7.4 机构管理员（AgencyAdmin）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键ID | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置 |
| account_code | String(100) | 账号编码（唯一） | 系统生成 | 创建时设置，不可修改 |
| account_name | String(200) | 账号名称（管理员姓名） | 用户输入 | 可编辑 |
| login_id | String(100) | 登录ID（唯一） | 用户输入 | 创建时设置，不可修改 |
| password_hash | String(255) | 密码哈希（BCrypt加密） | 系统生成 | 可编辑（重置密码） |
| role | String(50) | 角色（agency_admin） | 系统设置 | 不可修改 |
| email | String(100) | 邮箱 | 用户输入 | 可编辑 |
| remark | Text | 备注 | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| last_login_at | DateTime | 最近登录时间 | 系统更新 | 登录时更新 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：机构管理员在创建机构时同时创建，密码和确认密码为必填项，不包含手机号字段。

#### 7.5 机构作息时间（AgencyWorkingHours）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键 | 系统生成 | 不变 |
| agency_id | BigInteger | 机构ID | 关联选择 | 创建时设置 |
| day_of_week | Integer | 星期几（1-7，1=周一） | 用户选择 | 可编辑 |
| start_time | Time | 开始时间（HH:MM） | 用户输入 | 可编辑 |
| end_time | Time | 结束时间（HH:MM） | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

#### 7.6 小组群（TeamGroup）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键 | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置，不可修改 |
| group_code | String(100) | 小组群编码（唯一） | 用户输入 | 创建时设置，不可修改 |
| group_name | String(200) | 小组群名称 | 用户输入 | 可编辑 |
| group_name_en | String(200) | 小组群名称（英文） | 用户输入 | 可编辑 |
| admin_id | BigInteger | 小组群管理员ID | 关联选择 | 可编辑 |
| description | Text | 小组群描述 | 用户输入 | 可编辑 |
| sort_order | Integer | 排序顺序 | 用户设置 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**统计字段（实时计算）：**
- team_count：小组数量（统计该小组群下所有小组）
- collector_count：催员数量（统计该小组群下所有催员）

#### 7.7 小组群管理员（TeamGroupAdmin）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键ID | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置 |
| team_group_id | BigInteger | 所属小组群ID | 关联选择 | 创建时设置 |
| account_code | String(100) | 账号编码（唯一） | 系统生成 | 创建时设置，不可修改 |
| account_name | String(200) | 账号名称（管理员姓名） | 用户输入 | 可编辑 |
| login_id | String(100) | 登录ID（唯一） | 用户输入 | 创建时设置，不可修改 |
| password_hash | String(255) | 密码哈希（BCrypt加密） | 系统生成 | 可编辑（重置密码） |
| role | String(50) | 角色（spv） | 系统设置 | 不可修改 |
| email | String(100) | 邮箱 | 用户输入 | 可编辑 |
| remark | Text | 备注 | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| last_login_at | DateTime | 最近登录时间 | 系统更新 | 登录时更新 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：小组群管理员（SPV）在创建小组群时同时创建，密码和确认密码为必填项，不包含手机号字段。

#### 7.8 小组（CollectionTeam）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| team_id | BigInteger | 小组ID（主键） | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置，不可修改 |
| team_group_id | BigInteger | 所属小组群ID（可选） | 关联选择 | 可编辑 |
| team_code | String(100) | 小组编码（唯一） | 用户输入 | 创建时设置，不可修改 |
| team_name | String(200) | 小组名称 | 用户输入 | 可编辑 |
| team_name_en | String(200) | 小组名称（英文） | 用户输入 | 可编辑 |
| leader_id | BigInteger | 小组组长ID | 关联选择 | 可编辑 |
| target_performance | Decimal | 业绩目标 | 用户输入 | 可编辑 |
| description | Text | 小组描述 | 用户输入 | 可编辑 |
| sort_order | Integer | 排序顺序 | 用户设置 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**统计字段（实时计算）：**
- collector_count：催员数量（统计该小组下所有催员）

#### 7.9 小组管理员（TeamAdmin）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| id | BigInteger | 主键ID | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置 |
| team_group_id | BigInteger | 所属小组群ID（可选） | 关联选择 | 可编辑 |
| team_id | BigInteger | 所属小组ID（可选） | 关联选择 | 创建时设置，可编辑 |
| login_id | String(100) | 登录ID（唯一，用作账号标识） | 用户输入 | 创建时设置，不可修改 |
| account_name | String(200) | 账号名称（管理员姓名） | 用户输入 | 可编辑 |
| password_hash | String(255) | 密码哈希（BCrypt加密） | 系统生成 | 可编辑（重置密码） |
| role | String(50) | 角色（team_leader/quality_inspector/statistician） | 用户选择 | 可编辑 |
| email | String(100) | 邮箱 | 用户输入 | 可编辑 |
| remark | Text | 备注 | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| last_login_at | DateTime | 最近登录时间 | 系统更新 | 登录时更新 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：
- 小组管理员不支持删除操作，只能通过启用/禁用来管理
- 不包含手机号字段
- login_id既是登录账号也是账号的唯一标识，不需要单独的account_code字段

#### 7.10 催员（Collector）核心字段

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|----------|
| collector_id | BigInteger | 催员ID（主键） | 系统生成 | 不变 |
| tenant_id | BigInteger | 所属甲方ID | 关联选择 | 创建时设置 |
| agency_id | BigInteger | 所属机构ID | 关联选择 | 创建时设置，可编辑 |
| team_id | BigInteger | 所属小组ID | 关联选择 | 创建时设置，可编辑 |
| collector_code | String(100) | 催员编码（唯一） | 用户输入 | 创建时设置，不可修改 |
| collector_name | String(200) | 催员姓名 | 用户输入 | 可编辑 |
| login_id | String(100) | 登录账号（唯一） | 用户输入 | 创建时设置，不可修改 |
| password_hash | String(255) | 密码哈希（BCrypt加密） | 系统生成 | 可编辑（重置密码） |
| role | String(50) | 角色（collector/leader） | 用户选择 | 可编辑 |
| email | String(100) | 邮箱 | 用户输入 | 可编辑 |
| employee_no | String(50) | 工号 | 用户输入 | 可编辑 |
| collector_level | String(50) | 催员等级（初级/中级/高级/资深） | 用户选择 | 可编辑 |
| max_case_count | Integer | 最大案件数量 | 用户输入 | 可编辑 |
| current_case_count | Integer | 当前案件数量 | 系统计算 | 实时更新 |
| performance_score | Decimal | 绩效评分 | 系统计算 | 定期更新 |
| status | String(20) | 状态（active/休假/离职等） | 用户选择 | 可编辑 |
| hire_date | Date | 入职日期 | 用户输入 | 可编辑 |
| is_active | Boolean | 是否启用 | 系统设置 | 可编辑 |
| last_login_at | DateTime | 最近登录时间 | 系统更新 | 登录时更新 |
| created_at | DateTime | 创建时间 | 系统生成 | 自动 |
| updated_at | DateTime | 更新时间 | 系统生成 | 自动 |

**说明**：催员不包含手机号字段，使用邮箱作为联系方式。

#### 7.11 统计口径说明

- **小组数量**：统计指定组织单元下直接关联的小组数量（不包括下级组织单元的小组）
- **催员数量**：统计指定组织单元下所有催员数量（包括所有下级组织单元的催员）
- **统计更新频率**：实时计算，每次查询时重新统计
- **统计范围**：只统计启用状态的组织单元和催员

### 8. 交互与信息展示（UX & UI Brief）

本项目前端交互已由 Web 团队实现，本节仅补充关键信息展示要求：

#### 8.1 甲方管理页面

- **列表展示**：甲方编码、名称、国家、时区、货币、状态、创建时间
- **操作按钮**：创建甲方、编辑、启用/禁用（不支持删除）
- **筛选功能**：按国家、状态筛选
- **创建表单**：必须同时填写甲方信息和管理员信息（包括密码和确认密码）

#### 8.2 机构管理页面

- **列表展示**：机构编码、名称、所属甲方、时区、管理员、小组数量、催员数量、状态、操作
- **筛选功能**：按甲方筛选
- **操作按钮**：创建机构、编辑、配置作息时间、启用/禁用
- **创建表单**：
  - 必须同时填写机构信息（包括时区）和管理员信息（包括密码和确认密码）
  - 机构编码和管理员登录ID自动填充"甲方编码-"前缀，用户补充后续部分
- **作息时间配置**：弹窗展示，支持设置工作日和工作时间段

#### 8.3 小组群管理页面

- **列表展示**：小组群编码、名称、所属机构、管理员、小组数量、催员数量、状态、操作
- **筛选功能**：按甲方、机构筛选
- **操作按钮**：创建小组群、编辑、启用/禁用
- **创建表单**：
  - 必须同时填写小组群信息和管理员信息（包括密码和确认密码）
  - 小组群编码和管理员登录ID自动填充"甲方编码-"前缀，用户补充后续部分

#### 8.4 小组管理页面

- **列表展示**：小组编码、名称、所属机构、所属小组群、组长、催员数量、状态、操作
- **筛选功能**：按甲方、机构、小组群筛选（三级联动）
- **操作按钮**：创建小组、编辑、启用/禁用
- **创建表单**：小组编码自动填充"甲方编码-"前缀，用户补充后续部分

#### 8.5 小组管理员管理页面

- **列表展示**：登录ID、姓名、所属小组、邮箱、最近登录时间、状态、操作
- **筛选功能**：按甲方、机构、小组筛选（三级联动）
- **操作按钮**：创建小组管理员、编辑、重置密码、启用/禁用（不支持删除）
- **创建表单**：登录ID自动填充"甲方编码-"前缀，用户补充后续部分
- **字段说明**：不包含手机号字段，login_id即为账号标识

#### 8.6 催员管理页面

- **列表展示**：催员编码、姓名、登录账号、所属小组、角色、最近登录时间、状态、操作
- **筛选功能**：按甲方、机构、小组筛选（三级联动）
- **操作按钮**：创建催员、编辑、重置密码、启用/禁用
- **创建表单**：
  - 催员编码和登录账号自动填充"甲方编码-"前缀，用户补充后续部分
  - 创建时设置初始密码，支持单独重置密码功能
- **字段说明**：不包含手机号字段

#### 8.7 通用交互要求

- **级联筛选**：上级筛选条件变化时，自动更新下级筛选选项
- **状态提示**：启用/禁用操作前显示确认提示，说明级联影响
- **统计信息**：实时显示统计信息，支持点击查看详情
- **批量操作**：支持批量启用/禁用（后续扩展）

### 9. 配置项与运营开关（Config & Operation Switches）

#### 9.1 系统级配置

| 配置项 | 说明 | 默认值 | 配置入口 | 变更流程 |
|--------|------|--------|----------|----------|
| 默认作息时间 | 机构未配置时的默认作息时间 | 周一至周五 09:00-18:00 | 系统配置 | 系统管理员修改 |
| 密码最小长度 | 账号密码的最小长度要求 | 6 | 系统配置 | 系统管理员修改 |
| 组织架构层级限制 | 是否限制组织架构的最大层级数 | 无限制 | 系统配置 | 系统管理员修改 |

#### 9.2 机构级配置

| 配置项 | 说明 | 默认值 | 配置入口 | 变更流程 |
|--------|------|--------|----------|----------|
| 机构作息时间 | 机构的工作时间配置 | 使用系统默认值 | 机构管理页面 | 机构管理员修改 |
| 机构时区 | 机构的时区设置 | 继承甲方时区 | 机构编辑页面 | 机构管理员修改 |

#### 9.3 功能开关

| 开关名称 | 说明 | 默认状态 | 影响范围 |
|--------|------|----------|----------|
| 组织架构编辑开关 | 是否允许编辑组织架构 | 开启 | 全局 |
| 机构作息时间功能开关 | 是否启用机构作息时间功能 | 开启 | 全局 |
| 小组群功能开关 | 是否启用小组群功能 | 开启 | 全局 |

#### 9.4 灰度/实验策略

- **新功能灰度**：新功能（如小组群）可以先在部分甲方启用，验证稳定后再全量开放
- **配置变更**：重要配置变更需要先在测试环境验证，再在预发布环境验证，最后在生产环境发布
- **回滚机制**：支持快速回滚配置变更，恢复到上一个稳定版本

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 创建甲方时 | 甲方创建埋点 | tenant_created | user_id（操作人ID）、tenant_id（甲方ID）、tenant_code（甲方编码） |
| 编辑甲方时 | 甲方编辑埋点 | tenant_updated | user_id、tenant_id、changed_fields（变更字段列表） |
| 启用/禁用甲方时 | 甲方状态变更埋点 | tenant_status_changed | user_id、tenant_id、old_status（原状态）、new_status（新状态） |
| 创建机构时 | 机构创建埋点 | agency_created | user_id、agency_id（机构ID）、tenant_id、agency_code（机构编码） |
| 编辑机构时 | 机构编辑埋点 | agency_updated | user_id、agency_id、changed_fields |
| 配置机构作息时间时 | 机构作息时间配置埋点 | agency_working_hours_configured | user_id、agency_id、working_hours（作息时间配置） |
| 启用/禁用机构时 | 机构状态变更埋点 | agency_status_changed | user_id、agency_id、old_status、new_status |
| 创建小组群时 | 小组群创建埋点 | team_group_created | user_id、team_group_id（小组群ID）、agency_id、group_code（小组群编码） |
| 编辑小组群时 | 小组群编辑埋点 | team_group_updated | user_id、team_group_id、changed_fields |
| 启用/禁用小组群时 | 小组群状态变更埋点 | team_group_status_changed | user_id、team_group_id、old_status、new_status |
| 创建小组时 | 小组创建埋点 | team_created | user_id、team_id（小组ID）、agency_id、team_code（小组编码） |
| 编辑小组时 | 小组编辑埋点 | team_updated | user_id、team_id、changed_fields |
| 启用/禁用小组时 | 小组状态变更埋点 | team_status_changed | user_id、team_id、old_status、new_status |
| 创建小组管理员时 | 小组管理员创建埋点 | team_admin_created | user_id、admin_id（管理员ID）、team_id、username（登录账号） |
| 编辑小组管理员时 | 小组管理员编辑埋点 | team_admin_updated | user_id、admin_id、changed_fields |
| 重置小组管理员密码时 | 小组管理员密码重置埋点 | team_admin_password_reset | user_id、admin_id |
| 启用/禁用小组管理员时 | 小组管理员状态变更埋点 | team_admin_status_changed | user_id、admin_id、old_status、new_status |
| 创建催员时 | 催员创建埋点 | collector_created | user_id、collector_id（催员ID）、team_id、collector_code（催员编码）、username |
| 编辑催员时 | 催员编辑埋点 | collector_updated | user_id、collector_id、changed_fields |
| 重置催员密码时 | 催员密码重置埋点 | collector_password_reset | user_id、collector_id |
| 调整催员归属时 | 催员归属调整埋点 | collector_reassigned | user_id、collector_id、old_team_id（原小组ID）、new_team_id（新小组ID） |
| 启用/禁用催员时 | 催员状态变更埋点 | collector_status_changed | user_id、collector_id、old_status、new_status |
| 查看组织架构列表时 | 组织架构列表查看埋点 | organization_list_viewed | user_id、view_type（查看类型：agency/team_group/team/collector）、filter_params（筛选参数） |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 系统架构与模块划分（System Architecture & Modules）

#### 1.1 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端管理控台                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ 甲方管理  │  │ 机构管理  │  │ 小组群管理 │  │ 小组管理  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│  ┌──────────┐  ┌──────────┐                               │
│  │小组管理员 │  │ 催员管理  │                               │
│  └──────────┘  └──────────┘                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Java后端服务 (Spring Boot)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           组织架构管理模块 (Organization Module)        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          │  │
│  │  │ 甲方服务  │  │ 机构服务  │  │ 小组群服务 │          │  │
│  │  └──────────┘  └──────────┘  └──────────┘          │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐          │  │
│  │  │ 小组服务  │  │小组管理员 │  │ 催员服务  │          │  │
│  │  └──────────┘  │  服务     │  └──────────┘          │  │
│  │                └──────────┘                        │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           权限管理模块 (Permission Module)            │  │
│  │  - 角色权限验证                                        │  │
│  │  - 数据隔离（基于tenant_id）                          │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           业务规则模块 (Business Rules Module)          │  │
│  │  - 机构作息时间管理                                    │  │
│  │  - 作息时间判断（用于质检、通知）                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ MyBatis-Plus
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      MySQL数据库                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ tenants  │  │agencies  │  │team_groups│  │  teams   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                │
│  │team_admins│  │collectors│  │agency_   │                │
│  │           │  │          │  │working_  │                │
│  │           │  │          │  │hours     │                │
│  └──────────┘  └──────────┘  └──────────┘                │
└─────────────────────────────────────────────────────────────┘
```

#### 1.2 模块职责边界

**前端模块：**
- **组织架构管理页面**：提供用户界面，处理用户交互，调用后端API
- **权限控制**：根据用户角色显示/隐藏功能按钮，控制页面访问权限

**后端模块：**
- **组织架构服务层**：处理业务逻辑，包括创建、编辑、查询、启用/禁用等操作
- **数据访问层**：使用MyBatis-Plus进行数据库操作
- **权限验证层**：验证用户权限，实现数据隔离
- **业务规则层**：管理机构作息时间，提供作息时间判断服务

**数据库层：**
- **组织架构表**：存储组织架构数据
- **账号表**：存储管理员和催员账号信息
- **业务规则表**：存储机构作息时间等配置

#### 1.3 调用关系

```
前端页面
  ↓ HTTP请求
Controller层（接收请求，参数验证）
  ↓
Service层（业务逻辑处理）
  ├── 调用 PermissionService（权限验证）
  ├── 调用 OrganizationService（组织架构操作）
  └── 调用 BusinessRuleService（业务规则处理）
  ↓
Mapper层（数据库操作）
  ↓
MySQL数据库
```

### 2. 接口设计与系统依赖（API Design & Dependencies）

#### 2.1 甲方管理接口

**基础路径**: `/api/v1/tenants`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取甲方列表 | tenant_id（可选）、is_active（可选）、skip、limit | 甲方列表 | 3s/不重试 | 是 |
| POST | `/` | 创建甲方（同时创建管理员） | tenant_code、tenant_name、country、timezone、currency、admin_info（包含password、confirm_password） | 创建的甲方信息 | 5s/不重试 | 是（基于tenant_code） |
| GET | `/{tenant_id}` | 获取甲方详情 | tenant_id | 甲方详情 | 3s/不重试 | 是 |
| PUT | `/{tenant_id}` | 更新甲方 | tenant_id、更新字段 | 更新后的甲方信息 | 5s/不重试 | 是 |
| PUT | `/{tenant_id}/status` | 启用/禁用甲方 | tenant_id、is_active | 操作结果 | 3s/不重试 | 是 |

**请求示例（创建甲方）：**
```json
{
  "tenant_code": "TENANT001",
  "tenant_name": "示例甲方",
  "country": "CN",
  "timezone": "Asia/Shanghai",
  "currency": "CNY",
  "admin_info": {
    "username": "admin001",
    "name": "管理员",
    "email": "admin@example.com",
    "password": "password123",
    "confirm_password": "password123"
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tenant_id": 1,
    "tenant_code": "TENANT001",
    "tenant_name": "示例甲方",
    "country": "CN",
    "timezone": "Asia/Shanghai",
    "currency": "CNY",
    "is_active": true,
    "created_at": "2025-01-11T10:00:00",
    "updated_at": "2025-01-11T10:00:00"
  }
}
```

#### 2.2 机构管理接口

**基础路径**: `/api/v1/agencies`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取机构列表 | tenant_id（必填）、is_active（可选）、skip、limit | 机构列表（含统计信息） | 3s/不重试 | 是 |
| POST | `/` | 创建机构（同时创建管理员） | tenant_id、agency_code、agency_name、timezone（必填）、admin_info（包含password、confirm_password） | 创建的机构信息 | 5s/不重试 | 是（基于agency_code） |
| GET | `/{agency_id}` | 获取机构详情 | agency_id | 机构详情 | 3s/不重试 | 是 |
| PUT | `/{agency_id}` | 更新机构 | agency_id、更新字段 | 更新后的机构信息 | 5s/不重试 | 是 |
| PUT | `/{agency_id}/status` | 启用/禁用机构 | agency_id、is_active | 操作结果 | 3s/不重试 | 是 |
| GET | `/{agency_id}/working-hours` | 获取机构作息时间 | agency_id | 作息时间配置 | 3s/不重试 | 是 |
| PUT | `/{agency_id}/working-hours` | 配置机构作息时间 | agency_id、working_hours | 操作结果 | 5s/不重试 | 是 |
| GET | `/{agency_id}/statistics` | 获取机构统计信息 | agency_id | 统计信息（小组数、催员数） | 3s/不重试 | 是 |

**请求示例（配置机构作息时间）：**
```json
{
  "working_hours": [
    {
      "day_of_week": 1,
      "start_time": "09:00",
      "end_time": "12:00",
      "is_active": true
    },
    {
      "day_of_week": 1,
      "start_time": "14:00",
      "end_time": "18:00",
      "is_active": true
    },
    {
      "day_of_week": 2,
      "start_time": "09:00",
      "end_time": "18:00",
      "is_active": true
    }
  ]
}
```

#### 2.3 小组群管理接口

**基础路径**: `/api/v1/team-groups`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取小组群列表 | tenant_id（必填）、agency_id（必填）、is_active（可选）、skip、limit | 小组群列表（含统计信息） | 3s/不重试 | 是 |
| POST | `/` | 创建小组群（同时创建管理员） | tenant_id、agency_id、group_code、group_name、admin_info（包含password、confirm_password） | 创建的小组群信息 | 5s/不重试 | 是（基于group_code） |
| GET | `/{team_group_id}` | 获取小组群详情 | team_group_id | 小组群详情 | 3s/不重试 | 是 |
| PUT | `/{team_group_id}` | 更新小组群 | team_group_id、更新字段 | 更新后的小组群信息 | 5s/不重试 | 是 |
| PUT | `/{team_group_id}/status` | 启用/禁用小组群 | team_group_id、is_active | 操作结果 | 3s/不重试 | 是 |
| DELETE | `/{team_group_id}` | 删除小组群 | team_group_id | 操作结果 | 5s/不重试 | 是（需先移除关联小组） |
| GET | `/{team_group_id}/teams` | 获取小组群下的小组 | team_group_id | 小组列表 | 3s/不重试 | 是 |
| GET | `/{team_group_id}/statistics` | 获取小组群统计信息 | team_group_id | 统计信息 | 3s/不重试 | 是 |

#### 2.4 小组管理接口

**基础路径**: `/api/v1/teams`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取小组列表 | tenant_id（必填）、agency_id（必填）、team_group_id（可选）、is_active（可选）、skip、limit | 小组列表（含统计信息） | 3s/不重试 | 是 |
| POST | `/` | 创建小组 | tenant_id、agency_id、team_group_id（可选）、team_code、team_name、leader_id（可选） | 创建的小组信息 | 5s/不重试 | 是（基于team_code） |
| GET | `/{team_id}` | 获取小组详情 | team_id | 小组详情 | 3s/不重试 | 是 |
| PUT | `/{team_id}` | 更新小组 | team_id、更新字段 | 更新后的小组信息 | 5s/不重试 | 是 |
| PUT | `/{team_id}/status` | 启用/禁用小组 | team_id、is_active | 操作结果 | 3s/不重试 | 是 |
| GET | `/{team_id}/statistics` | 获取小组统计信息 | team_id | 统计信息（催员数） | 3s/不重试 | 是 |

#### 2.5 小组管理员管理接口

**基础路径**: `/api/v1/team-admins`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取小组管理员列表 | tenant_id（必填）、agency_id（可选）、team_id（可选）、is_active（可选）、skip、limit | 小组管理员列表 | 3s/不重试 | 是 |
| POST | `/` | 创建小组管理员 | tenant_id、agency_id、team_id、username、name、email、password | 创建的小组管理员信息 | 5s/不重试 | 是（基于username） |
| GET | `/{admin_id}` | 获取小组管理员详情 | admin_id | 小组管理员详情 | 3s/不重试 | 是 |
| PUT | `/{admin_id}` | 更新小组管理员 | admin_id、更新字段 | 更新后的小组管理员信息 | 5s/不重试 | 是 |
| PUT | `/{admin_id}/password` | 重置密码 | admin_id、new_password | 操作结果 | 3s/不重试 | 是 |
| PUT | `/{admin_id}/status` | 启用/禁用小组管理员 | admin_id、is_active | 操作结果 | 3s/不重试 | 是 |

#### 2.6 催员管理接口

**基础路径**: `/api/v1/collectors`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/` | 获取催员列表 | tenant_id（必填）、agency_id（可选）、team_id（可选）、is_active（可选）、skip、limit | 催员列表 | 3s/不重试 | 是 |
| POST | `/` | 创建催员 | tenant_id、agency_id、team_id、collector_code、collector_name、username、password、role | 创建的催员信息 | 5s/不重试 | 是（基于username） |
| GET | `/{collector_id}` | 获取催员详情 | collector_id | 催员详情 | 3s/不重试 | 是 |
| PUT | `/{collector_id}` | 更新催员 | collector_id、更新字段 | 更新后的催员信息 | 5s/不重试 | 是 |
| PUT | `/{collector_id}/password` | 重置密码 | collector_id、new_password | 操作结果 | 3s/不重试 | 是 |
| PUT | `/{collector_id}/reassign` | 调整催员归属 | collector_id、new_team_id | 操作结果 | 5s/不重试 | 是 |
| PUT | `/{collector_id}/status` | 启用/禁用催员 | collector_id、is_active | 操作结果 | 3s/不重试 | 是 |

#### 2.7 业务规则接口

**基础路径**: `/api/v1/business-rules`

| 方法 | 路径 | 说明 | 主要入参 | 主要出参 | 超时/重试 | 幂等性 |
|------|------|------|---------|---------|----------|--------|
| GET | `/working-hours/check` | 检查是否在营业时间内 | agency_id、datetime | 是否在营业时间内 | 1s/不重试 | 是 |

#### 2.8 接口通用规范

**请求头：**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`（需要认证的接口）

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**错误响应：**
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

**超时与重试策略：**
- 查询接口：超时3秒，不重试
- 创建/更新接口：超时5秒，不重试
- 删除接口：超时5秒，不重试
- 业务规则接口：超时1秒，不重试

**幂等性要求：**
- 所有接口都要求幂等性
- 创建接口基于唯一字段（如code、username）实现幂等
- 更新接口基于ID实现幂等

**失败降级逻辑：**
- 查询接口失败：返回空列表或错误信息
- 创建/更新接口失败：返回错误信息，不执行操作
- 删除接口失败：返回错误信息，数据保持不变

#### 2.9 系统依赖

**内部依赖：**
- 权限管理模块：验证用户权限，实现数据隔离
- 账号管理模块：管理管理员和催员账号
- 业务规则模块：管理机构作息时间

**外部依赖：**
- 无外部系统依赖

### 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

#### 3.1 数据库表结构

**3.1.1 tenants表（甲方表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| tenant_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 甲方ID |
| tenant_code | VARCHAR(100) | UNIQUE, NOT NULL | 甲方编码（唯一） |
| tenant_name | VARCHAR(200) | NOT NULL | 甲方名称 |
| country | VARCHAR(50) | NOT NULL | 国家 |
| timezone | VARCHAR(50) | NOT NULL | 时区 |
| currency | VARCHAR(10) | NOT NULL | 货币 |
| **default_language** | **VARCHAR(10)** | **NOT NULL** | **默认语言（Locale，如zh-CN）** |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY: `tenant_id`
- UNIQUE KEY: `uk_tenant_code` (`tenant_code`)
- KEY: `idx_is_active` (`is_active`)

**3.1.2 collection_agencies表（机构表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| agency_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 机构ID |
| tenant_id | BIGINT | NOT NULL, FOREIGN KEY | 所属甲方ID |
| agency_code | VARCHAR(100) | NOT NULL | 机构编码 |
| agency_name | VARCHAR(200) | NOT NULL | 机构名称 |
| admin_id | BIGINT | NULL, FOREIGN KEY | 机构管理员ID |
| contact_phone | VARCHAR(50) | NULL | 联系电话 |
| contact_email | VARCHAR(100) | NULL | 联系邮箱 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY: `agency_id`
- UNIQUE KEY: `uk_tenant_agency_code` (`tenant_id`, `agency_code`)
- KEY: `idx_tenant_id` (`tenant_id`)
- KEY: `idx_is_active` (`is_active`)
- FOREIGN KEY: `fk_agency_tenant` (`tenant_id`) REFERENCES `tenants` (`tenant_id`)

**3.1.3 agency_working_hours表（机构作息时间表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| agency_id | BIGINT | NOT NULL, FOREIGN KEY | 机构ID |
| day_of_week | TINYINT | NOT NULL | 星期几（1-7，1=周一） |
| start_time | TIME | NOT NULL | 开始时间 |
| end_time | TIME | NOT NULL | 结束时间 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY: `id`
- KEY: `idx_agency_id` (`agency_id`)
- KEY: `idx_agency_day` (`agency_id`, `day_of_week`)
- FOREIGN KEY: `fk_working_hours_agency` (`agency_id`) REFERENCES `collection_agencies` (`agency_id`)

**3.1.4 team_groups表（小组群表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| tenant_id | BIGINT | NOT NULL, FOREIGN KEY | 所属甲方ID |
| agency_id | BIGINT | NOT NULL, FOREIGN KEY | 所属机构ID |
| group_code | VARCHAR(100) | NOT NULL | 小组群编码 |
| group_name | VARCHAR(200) | NOT NULL | 小组群名称 |
| group_name_en | VARCHAR(200) | NULL | 小组群名称（英文） |
| admin_id | BIGINT | NULL, FOREIGN KEY | 小组群管理员ID |
| description | TEXT | NULL | 小组群描述 |
| sort_order | INT | DEFAULT 0 | 排序顺序 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_agency_group_code` (`agency_id`, `group_code`)
- KEY: `idx_tenant_id` (`tenant_id`)
- KEY: `idx_agency_id` (`agency_id`)
- KEY: `idx_is_active` (`is_active`)
- FOREIGN KEY: `fk_team_group_tenant` (`tenant_id`) REFERENCES `tenants` (`tenant_id`)
- FOREIGN KEY: `fk_team_group_agency` (`agency_id`) REFERENCES `collection_agencies` (`agency_id`)

**3.1.5 collection_teams表（小组表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| team_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 小组ID |
| tenant_id | BIGINT | NOT NULL, FOREIGN KEY | 所属甲方ID |
| agency_id | BIGINT | NOT NULL, FOREIGN KEY | 所属机构ID |
| team_group_id | BIGINT | NULL, FOREIGN KEY | 所属小组群ID（可选） |
| team_code | VARCHAR(100) | NOT NULL | 小组编码 |
| team_name | VARCHAR(200) | NOT NULL | 小组名称 |
| leader_id | BIGINT | NULL, FOREIGN KEY | 小组组长ID |
| target_performance | DECIMAL(10,2) | NULL | 业绩目标 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引：**
- PRIMARY KEY: `team_id`
- UNIQUE KEY: `uk_agency_team_code` (`agency_id`, `team_code`)
- KEY: `idx_tenant_id` (`tenant_id`)
- KEY: `idx_agency_id` (`agency_id`)
- KEY: `idx_team_group_id` (`team_group_id`)
- KEY: `idx_is_active` (`is_active`)
- FOREIGN KEY: `fk_team_tenant` (`tenant_id`) REFERENCES `tenants` (`tenant_id`)
- FOREIGN KEY: `fk_team_agency` (`agency_id`) REFERENCES `collection_agencies` (`agency_id`)
- FOREIGN KEY: `fk_team_group` (`team_group_id`) REFERENCES `team_groups` (`id`)

**3.1.6 team_admins表（小组管理员表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键ID |
| tenant_id | BIGINT | NOT NULL, FOREIGN KEY | 所属甲方ID |
| agency_id | BIGINT | NOT NULL, FOREIGN KEY | 所属机构ID |
| team_id | BIGINT | NOT NULL, FOREIGN KEY | 所属小组ID |
| username | VARCHAR(100) | UNIQUE, NOT NULL | 登录账号（唯一） |
| name | VARCHAR(200) | NOT NULL | 管理员姓名 |
| email | VARCHAR(100) | NULL | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| last_login_at | DATETIME | NULL | 最近登录时间 |

**索引：**
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_username` (`username`)
- KEY: `idx_tenant_id` (`tenant_id`)
- KEY: `idx_agency_id` (`agency_id`)
- KEY: `idx_team_id` (`team_id`)
- KEY: `idx_is_active` (`is_active`)
- FOREIGN KEY: `fk_team_admin_tenant` (`tenant_id`) REFERENCES `tenants` (`tenant_id`)
- FOREIGN KEY: `fk_team_admin_agency` (`agency_id`) REFERENCES `collection_agencies` (`agency_id`)
- FOREIGN KEY: `fk_team_admin_team` (`team_id`) REFERENCES `collection_teams` (`team_id`)

**3.1.7 collectors表（催员表）**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| collector_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 催员ID |
| tenant_id | BIGINT | NOT NULL, FOREIGN KEY | 所属甲方ID |
| agency_id | BIGINT | NOT NULL, FOREIGN KEY | 所属机构ID |
| team_id | BIGINT | NOT NULL, FOREIGN KEY | 所属小组ID |
| collector_code | VARCHAR(100) | NOT NULL | 催员编码 |
| collector_name | VARCHAR(200) | NOT NULL | 催员姓名 |
| username | VARCHAR(100) | UNIQUE, NOT NULL | 登录账号（唯一） |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| role | VARCHAR(50) | DEFAULT 'collector' | 角色（collector/leader/spv） |
| email | VARCHAR(100) | NULL | 邮箱 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| last_login_at | DATETIME | NULL | 最近登录时间 |

**索引：**
- PRIMARY KEY: `collector_id`
- UNIQUE KEY: `uk_username` (`username`)
- UNIQUE KEY: `uk_collector_code` (`collector_code`)
- KEY: `idx_tenant_id` (`tenant_id`)
- KEY: `idx_agency_id` (`agency_id`)
- KEY: `idx_team_id` (`team_id`)
- KEY: `idx_is_active` (`is_active`)
- KEY: `idx_role` (`role`)
- FOREIGN KEY: `fk_collector_tenant` (`tenant_id`) REFERENCES `tenants` (`tenant_id`)
- FOREIGN KEY: `fk_collector_agency` (`agency_id`) REFERENCES `collection_agencies` (`agency_id`)
- FOREIGN KEY: `fk_collector_team` (`team_id`) REFERENCES `collection_teams` (`team_id`)

#### 3.2 数据模型依赖

**算法模型/评分卡依赖：**
- 无

**特征仓依赖：**
- 无

**版本管理：**
- 使用数据库迁移工具（Flyway）管理表结构变更
- 每个迁移脚本包含版本号和描述
- 支持回滚到指定版本

**回滚方案：**
- 数据库迁移支持向下迁移（rollback）
- 代码回滚：通过Git版本控制回滚到上一个稳定版本
- 数据回滚：通过数据库备份恢复数据

### 4. 非功能性要求（Non-Functional Requirements）

#### 4.1 性能目标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| QPS | 1000+ | 单个接口的每秒查询数 |
| 响应时间 | < 200ms（查询）< 500ms（创建/更新） | P95响应时间 |
| 峰值容量 | 支持10000+并发用户 | 系统峰值处理能力 |
| 数据库查询 | < 100ms | 单次数据库查询时间 |

#### 4.2 可用性要求

| 指标 | 目标值 | 说明 |
|------|--------|------|
| SLA | 99.9% | 系统可用性目标 |
| 降级策略 | 查询接口降级为缓存数据 | 当数据库不可用时 |
| 故障恢复时间 | < 5分钟 | 从故障发生到恢复的时间 |

#### 4.3 安全要求

| 要求 | 说明 |
|------|------|
| 加密存储 | 密码使用BCrypt加密存储，盐值随机生成 |
| 接口鉴权 | 所有接口需要JWT Token认证 |
| 数据隔离 | 基于tenant_id实现数据隔离，不同甲方数据完全隔离 |
| 脱敏展示 | 在日志和监控中，敏感信息（密码、手机号）需要脱敏 |
| SQL注入防护 | 使用参数化查询，防止SQL注入 |
| XSS防护 | 前端对用户输入进行转义，防止XSS攻击 |

#### 4.4 扩展性要求

| 要求 | 说明 |
|------|------|
| 水平扩展 | 支持多实例部署，通过负载均衡分发请求 |
| 数据库扩展 | 支持读写分离，支持分库分表（按tenant_id分片） |
| 缓存扩展 | 支持Redis集群，缓存组织架构数据 |
| 消息队列 | 支持异步处理（如组织架构变更通知） |

#### 4.5 容灾/多机房部署

| 要求 | 说明 |
|------|------|
| 数据备份 | 每日自动备份数据库，保留30天 |
| 异地容灾 | 支持多机房部署，主从数据库同步 |
| 故障切换 | 支持自动故障切换，切换时间 < 1分钟 |

### 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

#### 5.1 关键日志

| 日志类型 | 记录内容 | 日志级别 |
|---------|---------|---------|
| 请求日志 | 请求URL、方法、参数、响应时间、响应状态 | INFO |
| 操作日志 | 操作类型、操作人、操作对象、操作结果 | INFO |
| 错误日志 | 错误类型、错误信息、堆栈信息、请求参数 | ERROR |
| 安全日志 | 登录失败、权限拒绝、异常访问 | WARN |
| 性能日志 | 慢查询、超时请求、数据库连接池状态 | WARN |

**日志格式示例：**
```
[2025-01-11 10:00:00] [INFO] [OrganizationService] 创建机构成功: agency_id=1001, tenant_id=1, operator=admin001
[2025-01-11 10:00:01] [ERROR] [OrganizationService] 创建机构失败: tenant_id=1, error=机构编码已存在, operator=admin001
```

#### 5.2 监控指标

| 指标名称 | 指标类型 | 告警阈值 | 说明 |
|---------|---------|---------|------|
| 接口成功率 | 成功率 | < 99% | 接口调用成功率 |
| 接口响应时间 | 响应时间 | P95 > 500ms | 接口响应时间 |
| 数据库连接数 | 连接数 | > 80% | 数据库连接池使用率 |
| 错误率 | 错误率 | > 1% | 接口错误率 |
| 创建/更新操作失败率 | 失败率 | > 0.5% | 创建/更新操作失败率 |

#### 5.3 告警规则

| 告警规则 | 触发条件 | 告警级别 | 处理方式 |
|---------|---------|---------|---------|
| 接口成功率告警 | 成功率 < 99% 持续5分钟 | 严重 | 立即通知运维团队 |
| 响应时间告警 | P95响应时间 > 500ms 持续5分钟 | 警告 | 通知开发团队排查 |
| 数据库连接告警 | 连接数 > 80% | 警告 | 通知运维团队扩容 |
| 错误率告警 | 错误率 > 1% 持续5分钟 | 严重 | 立即通知开发团队 |
| 创建/更新失败告警 | 失败率 > 0.5% 持续5分钟 | 严重 | 立即通知开发团队 |

### 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

#### 6.1 测试类型

| 测试类型 | 说明 | 覆盖范围 |
|---------|------|---------|
| 单元测试 | 测试Service层和工具类 | 覆盖率 > 80% |
| 集成测试 | 测试Controller、Service、Mapper的集成 | 核心业务流程 |
| 接口测试 | 测试所有API接口 | 所有接口 |
| 性能测试 | 测试系统在高负载下的表现 | 关键接口 |
| 安全测试 | 测试权限控制、数据隔离、SQL注入防护 | 安全相关功能 |
| 回归测试 | 测试新功能不影响现有功能 | 所有功能 |

#### 6.2 关键验收标准

1. **功能完整性**
   - ✅ 能够创建、编辑、查询、启用/禁用所有组织架构层级（甲方、机构、小组群、小组、小组管理员、催员）
   - ✅ 能够配置机构作息时间，并正确应用于质检、通知等环节
   - ✅ 能够正确显示组织架构的统计信息（小组数量、催员数量）

2. **数据一致性**
   - ✅ 组织架构的层级关系正确，数据关联关系正确
   - ✅ 启用/禁用操作能够正确级联影响下级组织单元
   - ✅ 统计信息实时准确，与实际情况一致

3. **权限控制**
   - ✅ 不同角色的用户只能查看和管理权限范围内的数据
   - ✅ 数据隔离正确，不同甲方的数据完全隔离
   - ✅ 接口权限验证正确，未授权用户无法访问

4. **性能要求**
   - ✅ 查询接口响应时间 < 200ms（P95）
   - ✅ 创建/更新接口响应时间 < 500ms（P95）
   - ✅ 系统能够支持1000+ QPS

5. **安全性**
   - ✅ 密码加密存储，无法明文查看
   - ✅ 敏感信息在日志中脱敏
   - ✅ 接口需要认证，未授权访问被拒绝
   - ✅ SQL注入和XSS攻击被有效防护

### 7. 发布计划与回滚预案（Release Plan & Rollback）

#### 7.1 发布策略

**发布方式：** 灰度发布，分批上线

**发布步骤：**

1. **预发布环境验证**（1天）
   - 在预发布环境部署新版本
   - 执行完整的功能测试和性能测试
   - 验证数据迁移脚本正确性

2. **灰度发布**（3天）
   - 第1天：选择1个甲方进行灰度，流量比例10%
   - 第2天：如果无问题，扩大到3个甲方，流量比例30%
   - 第3天：如果无问题，扩大到所有甲方，流量比例100%

3. **全量发布**（1天）
   - 所有甲方切换到新版本
   - 监控系统指标，确保无异常

**支持多版本并行：**
- 通过功能开关控制新功能是否启用
- 支持新老版本API并存，逐步迁移

#### 7.2 配置/开关切换步骤

1. **功能开关配置**
   - 在系统配置中设置功能开关
   - 支持按甲方、按机构配置开关

2. **数据库迁移**
   - 执行数据库迁移脚本，创建新表
   - 迁移现有数据（如有）
   - 验证数据完整性

3. **代码部署**
   - 部署新版本代码
   - 重启服务
   - 验证服务正常启动

4. **功能验证**
   - 验证新功能正常
   - 验证现有功能不受影响
   - 验证性能指标正常

#### 7.3 回滚方案

**回滚触发条件：**
- 接口成功率 < 95%
- 出现严重bug影响业务
- 性能严重下降
- 数据丢失或损坏

**回滚步骤：**

1. **立即回滚**（< 5分钟）
   - 关闭功能开关，禁用新功能
   - 如果代码问题，回滚到上一个稳定版本
   - 重启服务

2. **数据回滚**（如需要）
   - 如果数据库结构变更，执行回滚脚本
   - 从备份恢复数据（如需要）

3. **验证回滚结果**
   - 验证系统功能正常
   - 验证数据完整性
   - 监控系统指标

**回滚责任人：**
- **开发负责人**：负责代码回滚和问题排查
- **运维负责人**：负责服务重启和数据库回滚
- **测试负责人**：负责验证回滚后的功能正常

**应急联系人：**
- 开发负责人：XXX（电话：XXX）
- 运维负责人：XXX（电话：XXX）
- 产品负责人：XXX（电话：XXX）

---

## 附录

### A. 术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| 甲方 | Tenant | 使用系统的客户，是组织架构的顶层 |
| 机构 | Agency | 催收机构，属于甲方 |
| 小组群 | TeamGroup | 小组的集合，属于机构 |
| 小组 | Team | 催收小组，属于机构或小组群 |
| 小组管理员 | TeamAdmin | 管理小组的管理员账号 |
| 催员 | Collector | 执行催收工作的员工账号 |
| SPV | Supervisor | 小组群长，管理多个小组 |
| 作息时间 | Working Hours | 机构的工作时间配置 |

### B. 参考文档

- [组织架构管理功能实现说明](../../说明文档/后端/组织架构管理功能实现说明.md)
- [小组群功能说明](../../文档/功能说明/小组群功能说明.md)
- [系统角色体系说明](../../说明文档/后端/系统角色体系说明.md)

### C. 版本历史

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0 | 2025-01-11 | 产品团队 | 初始版本 |
| v1.1 | 2025-12-04 | 产品团队 | 更新创建流程，补充关联创建管理员逻辑；机构时区必填；移除手机号字段；甲方和小组管理员不支持删除 |
| v1.2 | 2025-12-04 | 产品团队 | 新增编码规范章节（4.7），规定所有编码和登录ID必须以"甲方编码-"为前缀；小组管理员去掉account_code字段，只保留login_id作为唯一标识 |

---

**文档状态：** ✅ 已完成  
**最后更新：** 2025-12-04  
**审核状态：** 待审核