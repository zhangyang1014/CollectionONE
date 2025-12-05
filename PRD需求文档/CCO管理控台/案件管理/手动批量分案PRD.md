# 手动批量分案PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

手动批量分案是CCO催收管理控台的核心功能之一，用于将案件批量分配给催员进行催收。通过灵活的分配策略和智能的队列匹配检查，实现高效、合理的案件分配，提升催收效率和回款率。

**业务痛点**：
- 案件量大时，逐个分配案件效率低下
- 案件与催员队列不匹配导致催收效果差
- 案件分配不均导致催员工作量失衡
- 已分配案件重复分配导致催员冲突

**预期业务目标**：
- **分案效率提升80%**：批量分案替代逐个分案，大幅提升效率
- **分配准确率>95%**：队列匹配检查确保案件分配到合适的催员
- **催员工作量均衡度>85%**：均分策略确保催员工作量相对均衡
- **分案冲突率<5%**：已分配案件保护机制减少重复分配

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：新案件批量分配**
- **场景描述**：新导入的案件需要分配给催员
- **入口**：案件列表 → 筛选未分配案件 → 多选案件 → 批量分案
- **触发时机**：每日案件导入后，需要快速分配给催员
- **所在页面**：案件列表页面
- **操作流程**：选择未分配案件 → 点击"批量分案" → 选择催员 → 选择分配策略 → 确认分配
- **涉及角色**：分案专员、机构管理员

**场景2：队列内案件均分**
- **场景描述**：将某个队列的案件均匀分配给该队列的催员
- **入口**：案件列表 → 筛选队列 → 全选案件 → 批量分案 → 按催员均分
- **触发时机**：队列案件积压，需要均衡分配
- **操作流程**：筛选队列 → 选择案件 → 选择该队列的催员 → 选择"按催员均分" → 确认分配
- **涉及角色**：运营经理、分案专员

**场景3：案件重新分配**
- **场景描述**：已分配案件需要重新分配给其他催员
- **入口**：案件列表 → 筛选已分配案件 → 多选案件 → 批量分案 → 强制重新分配
- **触发时机**：催员离职、催员请假、催收效果不佳需要调整
- **操作流程**：选择已分配案件 → 勾选"强制重新分配" → 选择新催员 → 确认分配
- **涉及角色**：运营经理、小组管理员

**场景4：指定催员批量分配**
- **场景描述**：将特定案件批量分配给指定的催员
- **入口**：案件列表 → 多选案件 → 批量分案 → 指定催员
- **触发时机**：需要将特殊案件分配给经验丰富的催员
- **操作流程**：选择案件 → 选择指定催员 → 确认分配
- **涉及角色**：分案专员、小组管理员

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用频率 | 典型场景 |
|---------|---------|---------|---------|---------|
| **超级管理员** | SuperAdmin | 监控整体分案情况，处理异常分案 | 低 | 分案监控、异常处理 |
| **甲方管理员** | TenantAdmin | 管理本甲方案件分配策略 | 中 | 策略调整、效果分析 |
| **分案专员** | CaseAssigner | 快速高效完成批量分案 | 高 | 每日批量分案 |
| **运营经理** | OperationManager | 优化分案策略，提升催收效率 | 中 | 策略优化、重新分配 |
| **机构管理员** | AgencyAdmin | 管理本机构案件分配 | 高 | 机构内案件分配 |
| **小组管理员** | TeamLeader | 管理本小组案件分配 | 高 | 小组内案件分配 |

### 3. 关键业务流程（Business Flow）

#### 3.1 批量分案核心流程

```
开始
  ↓
[用户] 进入案件列表页面
  ↓
[用户] 筛选目标案件（可选：队列、状态、催员等）
  ↓
[用户] 多选案件（勾选复选框）
  ↓
[用户] 点击"批量分案"按钮
  ↓
[系统] 打开批量分案对话框
  ↓
[系统] 显示已选案件数量和基本信息
  ↓
[用户] 选择分配方式
  ├─ 按催员均分
  ├─ 按队列均分
  └─ 指定分配
  ↓
[用户] 选择目标催员（单选或多选）
  ↓
[系统] 自动加载催员所属队列信息
  ↓
[系统] 执行队列匹配检查
  ├─ 检查案件队列与催员队列是否匹配
  └─ 检查案件是否已分配
  ↓
[系统] 显示检查结果
  ├─ [无问题] → 直接进入确认步骤
  ├─ [队列不匹配] → 显示不匹配案件列表
  │   ├─ [用户选择] 忽略限制继续分配
  │   ├─ [用户选择] 重新选择催员
  │   └─ [用户选择] 取消分配
  └─ [案件已分配] → 显示已分配案件列表
      ├─ [用户选择] 强制重新分配
      ├─ [用户选择] 仅分配未分配案件
      └─ [用户选择] 取消分配
  ↓
[用户] 确认分配信息
  ├─ 分配方式
  ├─ 目标催员
  ├─ 案件数量
  └─ 分配预览（每个催员分配多少案件）
  ↓
[用户] 点击"确认分配"按钮
  ↓
[系统] 执行批量分配
  ├─ 按分配策略计算每个催员的案件
  ├─ 更新案件的collector_id、team_id、agency_id
  ├─ 记录分配日志
  └─ 发送分配通知（可选）
  ↓
[系统] 显示分配结果
  ├─ 成功数量
  ├─ 失败数量
  └─ 失败详情（如果有）
  ↓
[系统] 关闭对话框，刷新案件列表
  ↓
结束
```

#### 3.2 队列匹配检查流程

```
开始（选择催员后自动触发）
  ↓
[系统] 获取已选案件列表
  ↓
[系统] 获取已选催员列表及其队列信息
  ↓
[系统] 遍历每个案件
  ↓
FOR EACH 案件:
  ├─ 获取案件的queue_id
  ├─ 获取分配目标催员的team_queue_id
  ├─ 判断：queue_id == team_queue_id ?
  │   ├─ [是] → 标记为"匹配"
  │   └─ [否] → 标记为"不匹配"，记录到不匹配列表
  └─ 判断：案件是否已分配（collector_id != NULL）?
      ├─ [是] → 标记为"已分配"，记录到已分配列表
      └─ [否] → 标记为"未分配"
  ↓
[系统] 统计检查结果
  ├─ 不匹配案件数量
  ├─ 已分配案件数量
  └─ 可正常分配案件数量
  ↓
[系统] 显示检查结果给用户
  ↓
结束
```

#### 3.3 分配策略执行流程

**策略1：按催员均分**
```
输入：案件列表（N个）、催员列表（M个）
  ↓
计算：每个催员应分配案件数 = N / M（向上取整）
  ↓
FOR EACH 催员:
  ├─ 从案件列表中取出对应数量的案件
  ├─ 分配给该催员（更新collector_id、team_id、agency_id）
  └─ 记录分配日志
  ↓
输出：分配结果（成功数、失败数、详情）
```

**策略2：按队列均分**
```
输入：案件列表（N个）、催员列表（M个）
  ↓
按案件队列分组：
  Group1: 队列Q1的案件（N1个）
  Group2: 队列Q2的案件（N2个）
  ...
  ↓
FOR EACH 队列分组:
  ├─ 筛选该队列的催员（queue_id = 队列ID）
  ├─ 计算每个催员应分配案件数 = 该队列案件数 / 该队列催员数
  ├─ 执行均分分配
  └─ 记录分配日志
  ↓
输出：分配结果
```

**策略3：指定分配**
```
输入：案件列表（N个）、催员（1个）
  ↓
FOR EACH 案件:
  ├─ 分配给指定催员
  ├─ 更新collector_id、team_id、agency_id
  └─ 记录分配日志
  ↓
输出：分配结果
```

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 分配前置条件

**案件筛选条件**：
- 案件必须属于当前选择的甲方
- 案件状态必须为"待还款"或"部分还款"（未结清）
- 停留案件（is_stay=true）不能被分配
- 可选：仅选择未分配案件（collector_id=NULL）

**催员筛选条件**：
- 催员必须是"启用"状态（is_active=true）
- 催员必须属于当前选择的甲方
- 催员必须有关联的小组和队列
- 可选：仅选择当前登录用户权限范围内的催员

#### 4.2 队列匹配规则

**匹配规则**：
- **严格匹配模式**（默认）：案件的queue_id必须等于催员小组的queue_id
- **宽松匹配模式**（可选）：允许跨队列分配，但会记录警告日志
- **豁免条件**：
  - 超级管理员可以忽略队列限制
  - 标记为"紧急"的案件可以跨队列分配
  - 特殊催收活动期间可以临时放宽限制

**不匹配处理**：
1. 显示不匹配案件列表（案件编号、案件队列、催员姓名、催员队列）
2. 提供三个选项：
   - **忽略限制继续分配**：记录警告日志，允许跨队列分配
   - **重新选择催员**：返回催员选择步骤
   - **取消分配**：关闭对话框

#### 4.3 已分配案件处理规则

**检查规则**：
- 案件的collector_id不为NULL，视为已分配
- 显示已分配案件的当前催员信息

**处理选项**：
1. **强制重新分配**（需要权限）：
   - 清除原催员的分配记录
   - 分配给新催员
   - 记录重新分配日志（包含原催员信息）
   - 发送通知给原催员和新催员（可选）

2. **仅分配未分配案件**：
   - 过滤掉已分配案件
   - 仅对未分配案件执行分配

3. **取消分配**：
   - 关闭对话框，不执行任何操作

**权限控制**：
- 系统管理员和运营经理：可以强制重新分配
- 分案专员：仅可分配未分配案件
- 小组管理员：可以重新分配本小组的案件

#### 4.4 分配策略规则

**按催员均分**：
- 案件总数N，催员总数M
- 每个催员分配数量 = ⌈N / M⌉（向上取整）
- 分配顺序：按催员ID升序分配
- 余数处理：最后一个催员可能分配较少案件
- 示例：100个案件，3个催员 → 34、33、33

**按队列均分**：
- 先将案件按队列分组
- 每个队列的案件只分配给该队列的催员
- 队列内按催员均分
- 跨队列案件不分配（需要用户手动处理）

**指定分配**：
- 所有案件分配给指定的单个催员
- 适用于特殊案件、VIP案件、专项催收案件

#### 4.5 并发控制规则

**分配锁定机制**：
- 案件正在被分配时，锁定案件状态
- 避免同一案件被多个用户同时分配
- 锁定超时时间：30秒

**冲突处理**：
- 案件已被其他用户分配：提示用户刷新列表
- 催员已被禁用：提示用户重新选择催员
- 队列配置已变更：提示用户重新检查匹配

#### 4.6 本次需求范围

**范围内（In Scope）**：
- ✅ 批量选择案件
- ✅ 选择目标催员（单选或多选）
- ✅ 三种分配策略（按催员均分、按队列均分、指定分配）
- ✅ 队列匹配检查
- ✅ 已分配案件检查
- ✅ 强制重新分配
- ✅ 分配预览
- ✅ 分配结果展示
- ✅ 分配日志记录

**范围外（Out of Scope）**：
- ❌ 智能分案（AI自动匹配催员）
- ❌ 定时自动分案
- ❌ 分案规则引擎（复杂的分案规则配置）
- ❌ 分案效果分析报表
- ❌ 催员工作量预警
- ❌ 案件优先级自动排序

### 5. 合规与风控要求（Compliance & Risk Control）

#### 5.1 合规要求

**操作留痕**：
- 所有分案操作记录日志（操作人、时间、案件、催员、分配策略）
- 强制重新分配必须记录原催员信息
- 忽略队列限制的操作记录警告日志
- 日志保留至少6个月

**权限控制**：
- 分案操作需要验证用户权限
- 强制重新分配需要更高权限
- 跨甲方分案禁止（不同甲方的案件和催员隔离）

**审计要求**：
- 支持按操作人、时间、甲方查询分案日志
- 异常分案（跨队列、强制重新分配）单独标记
- 分案统计报表（每日/每周/每月）

#### 5.2 风控要求

**业务风控**：
- **分案上限控制**：单次批量分案不超过1000个案件
- **催员负载控制**：单个催员案件数量预警（可配置阈值）
- **队列平衡检查**：警告队列内催员工作量严重不均（基尼系数>0.5）
- **重复分案保护**：同一批案件短时间内（1小时）重复分配记录警告

**系统风控**：
- **并发控制**：分案操作加分布式锁，避免并发冲突
- **事务完整性**：批量分案作为一个事务执行，失败全部回滚
- **数据一致性**：更新案件分配信息时同步更新相关统计数据
- **异常回滚**：分案失败时自动回滚，保证数据一致性

### 6. 资金路径与结算规则（Funding Flow & Settlement）

本功能不涉及资金流转，跳过此节。

### 7. 数据字段与口径（Data Definition）

#### 7.1 核心数据实体

**批量分案请求（BatchAssignRequest）**

| 字段名 | 数据类型 | 必填 | 说明 | 示例值 |
|-------|---------|-----|------|--------|
| case_ids | Array<Long> | ✅ | 案件ID列表 | [1001, 1002, 1003] |
| collector_ids | Array<Long> | ✅ | 催员ID列表 | [201, 202] |
| assign_strategy | String | ✅ | 分配策略 | "by_collector", "by_queue", "assign_all" |
| force_reassign | Boolean | ⬜ | 是否强制重新分配 | false |
| ignore_queue_limit | Boolean | ⬜ | 是否忽略队列限制 | false |
| operator_id | Long | ✅ | 操作人ID | 1001 |

**批量分案响应（BatchAssignResponse）**

| 字段名 | 数据类型 | 必填 | 说明 | 示例值 |
|-------|---------|-----|------|--------|
| success_count | Integer | ✅ | 成功数量 | 95 |
| failure_count | Integer | ✅ | 失败数量 | 5 |
| skipped_count | Integer | ✅ | 跳过数量（已分配） | 10 |
| details | Array<AssignDetail> | ✅ | 详细结果 | [...] |
| assign_preview | Array<AssignPreview> | ⬜ | 分配预览 | [...] |

**分配详情（AssignDetail）**

| 字段名 | 数据类型 | 必填 | 说明 | 示例值 |
|-------|---------|-----|------|--------|
| case_id | Long | ✅ | 案件ID | 1001 |
| case_code | String | ✅ | 案件编号 | "CASE20250101001" |
| collector_id | Long | ✅ | 催员ID | 201 |
| collector_name | String | ✅ | 催员姓名 | "张三" |
| status | String | ✅ | 分配状态 | "success", "failed", "skipped" |
| error_message | String | ⬜ | 错误信息 | "队列不匹配" |

**分配预览（AssignPreview）**

| 字段名 | 数据类型 | 必填 | 说明 | 示例值 |
|-------|---------|-----|------|--------|
| collector_id | Long | ✅ | 催员ID | 201 |
| collector_name | String | ✅ | 催员姓名 | "张三" |
| assign_count | Integer | ✅ | 将分配案件数 | 34 |
| current_count | Integer | ✅ | 当前案件数 | 120 |
| after_count | Integer | ✅ | 分配后案件数 | 154 |

**案件表更新字段（cases）**

| 字段名 | 数据类型 | 必填 | 说明 | 更新时机 |
|-------|---------|-----|------|---------|
| collector_id | BIGINT | ⬜ | 催员ID | 分配时更新 |
| team_id | BIGINT | ⬜ | 小组ID | 分配时更新 |
| agency_id | BIGINT | ⬜ | 机构ID | 分配时更新 |
| assigned_at | DATETIME | ⬜ | 分配时间 | 分配时更新 |
| assigned_by | BIGINT | ⬜ | 分配人ID | 分配时更新 |

#### 7.2 分配策略枚举

| 策略代码 | 策略名称 | 说明 | 适用场景 |
|---------|---------|------|---------|
| by_collector | 按催员均分 | 所有案件均分给所有催员 | 队列内案件均衡分配 |
| by_queue | 按队列均分 | 按队列分组后再均分 | 多队列案件批量分配 |
| assign_all | 指定分配 | 所有案件分配给指定催员 | 特殊案件、专项催收 |

#### 7.3 统计口径

**分案成功率**：
- 口径：（成功分配案件数 / 总案件数）× 100%
- 更新频率：实时计算
- 统计维度：操作人、甲方、日期

**催员工作量**：
- 口径：催员当前未结清案件数量
- 更新频率：分案后实时更新
- 统计维度：催员、小组、机构、甲方

**队列匹配率**：
- 口径：（队列匹配案件数 / 总案件数）× 100%
- 更新频率：实时计算
- 统计维度：甲方、队列、日期

### 8. 交互与信息展示（UX & UI Brief）

#### 8.1 批量分案对话框

**对话框标题**："批量分案"

**核心元素**：

1. **已选案件信息**（顶部）
   - 已选案件数量：`已选择 {N} 个案件`
   - 案件预览（最多显示5个）：案件编号、客户姓名、逾期天数、队列
   - "查看全部"链接（点击展开完整列表）

2. **分配策略选择**（单选按钮组）
   - ○ 按催员均分（默认）
     - 说明文字："将案件平均分配给所有选中的催员"
   - ○ 按队列均分
     - 说明文字："按案件队列分组，每个队列的案件分配给该队列的催员"
   - ○ 指定分配
     - 说明文字："将所有案件分配给单个指定的催员"

3. **催员选择**
   - 催员选择器（树形结构）
     - 机构 → 小组群 → 小组 → 催员
   - 支持搜索（按催员姓名、工号搜索）
   - 支持多选（"指定分配"时仅支持单选）
   - 显示催员当前案件数

4. **队列匹配检查结果**（自动触发）
   - ✅ 全部匹配：`{N} 个案件全部匹配，可以分配`（绿色）
   - ⚠️ 部分不匹配：`{M} 个案件队列不匹配`（橙色）
     - 展开显示不匹配案件列表
     - 提供选项：
       - [ ] 忽略限制继续分配
   - ⚠️ 部分已分配：`{K} 个案件已分配给其他催员`（橙色）
     - 展开显示已分配案件列表（案件编号、当前催员）
     - 提供选项：
       - [ ] 强制重新分配
       - [ ] 仅分配未分配案件

5. **分配预览**（展开/折叠）
   - 表格显示每个催员的分配情况：
     | 催员姓名 | 当前案件数 | 将分配数 | 分配后总数 |
     |---------|-----------|---------|-----------|
     | 张三    | 120       | 34      | 154       |
     | 李四    | 115       | 33      | 148       |
     | 王五    | 110       | 33      | 143       |

6. **操作按钮**
   - 确认分配（主按钮，蓝色）
   - 取消（次按钮，灰色）

#### 8.2 分配结果展示

**成功提示**：
```
分配成功！
成功分配 95 个案件
跳过 5 个已分配案件
失败 0 个案件
```

**部分成功提示**：
```
分配部分成功
成功分配 90 个案件
失败 10 个案件
点击查看详情 >
```

**失败详情对话框**：
| 案件编号 | 客户姓名 | 失败原因 |
|---------|---------|---------|
| CASE001 | 张三    | 队列不匹配 |
| CASE002 | 李四    | 催员已禁用 |

#### 8.3 交互细节

**催员选择交互**：
- 点击机构/小组群/小组节点：展开/折叠子节点
- 点击催员节点：勾选/取消勾选催员
- 支持快捷操作：
  - "全选"按钮：选择所有催员
  - "清空"按钮：取消所有选择
  - "仅选择当前小组"：选择当前展开小组的催员

**队列匹配检查交互**：
- 选择催员后自动触发检查
- 检查中显示Loading状态
- 检查完成显示结果（绿色/橙色/红色）
- 点击"查看详情"展开不匹配/已分配案件列表

**分配预览交互**：
- 默认折叠，点击"查看分配预览"展开
- 显示每个催员的分配数量和分配后总数
- 支持调整分配数量（高级功能，可选）

### 9. 配置项与运营开关（Config & Operation Switches）

#### 9.1 系统配置项

| 配置项 | 默认值 | 说明 | 配置入口 | 变更流程 |
|-------|-------|------|---------|---------|
| max_batch_assign_count | 1000 | 单次批量分案最大案件数 | 系统配置管理 | 需开发变更 |
| enable_queue_match_check | true | 是否启用队列匹配检查 | 系统配置管理 | 配置中心实时生效 |
| enable_force_reassign | true | 是否允许强制重新分配 | 系统配置管理 | 配置中心实时生效 |
| collector_case_limit | 500 | 催员案件数量上限（预警） | 系统配置管理 | 配置中心实时生效 |
| assign_lock_timeout | 30 | 分案锁定超时时间（秒） | 系统配置管理 | 需开发变更 |

#### 9.2 运营开关

| 开关名称 | 默认状态 | 说明 | 配置入口 | 影响范围 |
|---------|---------|------|---------|---------|
| 队列匹配检查 | 开启 | 是否检查案件队列与催员队列匹配 | 批量分案对话框 | 单次操作 |
| 强制重新分配 | 关闭 | 是否允许重新分配已分配案件 | 批量分案对话框 | 单次操作 |
| 催员负载预警 | 开启 | 催员案件数超过阈值时预警 | 系统配置 | 全局 |
| 分配通知 | 关闭 | 分案后是否发送通知给催员 | 系统配置 | 全局 |

#### 9.3 权限配置

| 权限项 | 权限代码 | 说明 | 默认角色 |
|-------|---------|------|---------|
| 批量分案 | case:batch_assign | 批量分配案件 | SuperAdmin、TenantAdmin、CaseAssigner、AgencyAdmin、TeamLeader |
| 强制重新分配 | case:force_reassign | 强制重新分配已分配案件 | SuperAdmin、OperationManager、TeamLeader |
| 忽略队列限制 | case:ignore_queue_limit | 忽略队列匹配限制分配 | SuperAdmin、OperationManager |
| 跨机构分案 | case:cross_agency_assign | 跨机构分配案件 | SuperAdmin、TenantAdmin |

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点 | 埋点说明 | 埋点ID | 关键属性 |
|-----------|---------|--------|---------|
| 点击"批量分案"按钮 | 批量分案入口点击 | `batch_assign_click` | user_id, tenant_id, selected_case_count, timestamp |
| 选择分配策略 | 分配策略选择 | `assign_strategy_select` | user_id, strategy, timestamp |
| 选择催员 | 催员选择 | `collector_select` | user_id, collector_ids, collector_count, timestamp |
| 队列匹配检查完成 | 队列匹配检查结果 | `queue_match_check` | user_id, total_cases, matched_count, unmatched_count, already_assigned_count, timestamp |
| 点击"忽略限制继续分配" | 忽略队列限制 | `ignore_queue_limit_click` | user_id, tenant_id, unmatched_count, timestamp |
| 点击"强制重新分配" | 强制重新分配 | `force_reassign_click` | user_id, tenant_id, reassign_count, timestamp |
| 提交批量分案 | 批量分案提交 | `batch_assign_submit` | user_id, tenant_id, case_count, collector_count, strategy, force_reassign, ignore_queue_limit, timestamp |
| 批量分案完成 | 批量分案结果 | `batch_assign_result` | user_id, tenant_id, success_count, failure_count, skipped_count, duration_ms, result(success/partial/failed), timestamp |
| 查看分配详情 | 查看分配失败详情 | `view_assign_detail` | user_id, failure_count, timestamp |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 系统架构与模块划分（System Architecture & Modules）

#### 1.1 高层架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    前端层（Frontend）                          │
│  ┌──────────────────────────────────────┐                   │
│  │      案件列表页面                     │                   │
│  │      CaseList.vue                    │                   │
│  │  ┌────────────────────────────────┐  │                   │
│  │  │  批量分案对话框                 │  │                   │
│  │  │  BatchAssignDialog.vue         │  │                   │
│  │  └────────────────────────────────┘  │                   │
│  └───────────────┬──────────────────────┘                   │
└──────────────────┼──────────────────────────────────────────┘
                   │ HTTP/JSON
┌──────────────────┼──────────────────────────────────────────┐
│                  ▼      后端层（Backend - Java）              │
│  ┌──────────────────────────────────────┐                   │
│  │       控制器层（Controller）           │                   │
│  │  ┌────────────────────────────────┐  │                   │
│  │  │    CaseController              │  │                   │
│  │  │  - batchAssign()               │  │                   │
│  │  │  - checkQueueLimit()           │  │                   │
│  │  │  - previewAssign()             │  │                   │
│  │  └────────────┬───────────────────┘  │                   │
│  └───────────────┼──────────────────────┘                   │
│                  │                                            │
│                  ▼                                            │
│  ┌──────────────────────────────────────┐                   │
│  │       业务层（Service）                │                   │
│  │  ┌────────────────────────────────┐  │                   │
│  │  │    CaseService                 │  │                   │
│  │  │  - batchAssignCases()          │  │                   │
│  │  │  - checkQueueLimit()           │  │                   │
│  │  │  - calculateAssignPreview()    │  │                   │
│  │  └────────────┬───────────────────┘  │                   │
│  │  ┌────────────┴───────────────────┐  │                   │
│  │  │    AssignStrategyService       │  │                   │
│  │  │  - assignByCollector()         │  │                   │
│  │  │  - assignByQueue()             │  │                   │
│  │  │  - assignAll()                 │  │                   │
│  │  └────────────────────────────────┘  │                   │
│  └─────────────────┬────────────────────┘                   │
│                    │                                          │
│                    ▼                                          │
│  ┌──────────────────────────────────────┐                   │
│  │       数据层（Mapper）                 │                   │
│  │  ┌────────────────┐  ┌─────────────┐ │                   │
│  │  │  CaseMapper    │  │QueueMapper  │ │                   │
│  │  └────────┬───────┘  └──────┬──────┘ │                   │
│  │  ┌────────┴──────────────────┴──────┐ │                   │
│  │  │  CollectorMapper                 │ │                   │
│  │  └──────────────────────────────────┘ │                   │
│  └─────────────────┬────────────────────┘                   │
└────────────────────┼──────────────────────────────────────┘
                     │ MyBatis-Plus
┌────────────────────┼──────────────────────────────────────┐
│                    ▼   数据层（MySQL）                       │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │    cases表       │  │  case_queues表   │                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  collectors表    │  │collection_teams表│                │
│  └──────────────────┘  └──────────────────┘                │
│  ┌──────────────────┐                                       │
│  │  assign_logs表   │（分案日志表）                          │
│  └──────────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

#### 1.2 模块职责

**前端模块**：
- `CaseList.vue`：案件列表页面，提供批量分案入口
- `BatchAssignDialog.vue`：批量分案对话框，处理分案交互逻辑
- `CollectorSelector.vue`：催员选择器组件（树形结构）
- `AssignPreview.vue`：分配预览组件

**后端模块**：
- `CaseController`：案件控制器，提供批量分案API
- `CaseService`：案件业务服务，处理分案核心逻辑
- `AssignStrategyService`：分配策略服务，实现不同分配算法
- `QueueService`：队列服务，提供队列匹配检查
- `CaseMapper`：案件数据访问层
- `CollectorMapper`：催员数据访问层

### 2. 接口设计与系统依赖（API Design & Dependencies）

#### 2.1 批量分案接口

**接口路径**：`POST /api/v1/cases/batch-assign`

**请求体**：
```json
{
  "case_ids": [1001, 1002, 1003],
  "collector_ids": [201, 202],
  "assign_strategy": "by_collector",
  "force_reassign": false,
  "ignore_queue_limit": false
}
```

**请求参数说明**：
| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| case_ids | Array<Long> | ✅ | 案件ID列表 | - |
| collector_ids | Array<Long> | ✅ | 催员ID列表 | - |
| assign_strategy | String | ✅ | 分配策略（by_collector/by_queue/assign_all） | by_collector |
| force_reassign | Boolean | ⬜ | 是否强制重新分配 | false |
| ignore_queue_limit | Boolean | ⬜ | 是否忽略队列限制 | false |

**响应示例（成功）**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success_count": 95,
    "failure_count": 0,
    "skipped_count": 5,
    "details": [
      {
        "case_id": 1001,
        "case_code": "CASE20250101001",
        "collector_id": 201,
        "collector_name": "张三",
        "status": "success"
      },
      {
        "case_id": 1002,
        "case_code": "CASE20250101002",
        "collector_id": 201,
        "collector_name": "张三",
        "status": "skipped",
        "error_message": "案件已分配"
      }
    ]
  }
}
```

**响应示例（部分失败）**：
```json
{
  "code": 200,
  "message": "部分成功",
  "data": {
    "success_count": 90,
    "failure_count": 10,
    "skipped_count": 0,
    "details": [
      {
        "case_id": 1001,
        "case_code": "CASE20250101001",
        "collector_id": 201,
        "collector_name": "张三",
        "status": "success"
      },
      {
        "case_id": 1010,
        "case_code": "CASE20250101010",
        "collector_id": 202,
        "collector_name": "李四",
        "status": "failed",
        "error_message": "队列不匹配"
      }
    ]
  }
}
```

**超时策略**：10秒超时（批量操作）

**幂等要求**：根据案件ID+催员ID去重，重复分配更新分配时间

**失败降级**：批量操作部分失败不回滚，返回详细失败信息

---

#### 2.2 队列限制检查接口

**接口路径**：`POST /api/v1/cases/check-queue-limit`

**请求体**：
```json
{
  "case_ids": [1001, 1002, 1003],
  "collector_ids": [201, 202]
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "has_limit": true,
    "unmatched_items": [
      {
        "case_id": 1001,
        "case_code": "CASE20250101001",
        "case_queue_id": 101,
        "case_queue_name": "M1队列",
        "collector_id": 201,
        "collector_name": "张三",
        "collector_team_queue_id": 102,
        "collector_team_queue_name": "S1队列"
      }
    ],
    "already_assigned_items": [
      {
        "case_id": 1002,
        "case_code": "CASE20250101002",
        "current_collector_id": 205,
        "current_collector_name": "王五"
      }
    ]
  }
}
```

**超时策略**：3秒超时

**失败降级**：返回has_limit=false，允许分配（记录警告日志）

---

#### 2.3 分配预览接口

**接口路径**：`POST /api/v1/cases/preview-assign`

**请求体**：
```json
{
  "case_ids": [1001, 1002, 1003],
  "collector_ids": [201, 202],
  "assign_strategy": "by_collector"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "collector_id": 201,
      "collector_name": "张三",
      "assign_count": 2,
      "current_count": 120,
      "after_count": 122
    },
    {
      "collector_id": 202,
      "collector_name": "李四",
      "assign_count": 1,
      "current_count": 115,
      "after_count": 116
    }
  ]
}
```

**超时策略**：2秒超时

---

### 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

#### 3.1 数据库表结构

**新增表：assign_logs（分案日志表）**

```sql
CREATE TABLE IF NOT EXISTS `assign_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '甲方ID',
    `case_id` BIGINT NOT NULL COMMENT '案件ID',
    `case_code` VARCHAR(100) COMMENT '案件编号',
    `collector_id` BIGINT NOT NULL COMMENT '催员ID',
    `collector_name` VARCHAR(100) COMMENT '催员姓名',
    `team_id` BIGINT COMMENT '小组ID',
    `agency_id` BIGINT COMMENT '机构ID',
    `assign_strategy` VARCHAR(50) COMMENT '分配策略',
    `previous_collector_id` BIGINT COMMENT '原催员ID（重新分配时）',
    `previous_collector_name` VARCHAR(100) COMMENT '原催员姓名',
    `force_reassign` TINYINT(1) DEFAULT 0 COMMENT '是否强制重新分配',
    `ignore_queue_limit` TINYINT(1) DEFAULT 0 COMMENT '是否忽略队列限制',
    `assigned_by` BIGINT NOT NULL COMMENT '分配操作人ID',
    `assigned_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_case_id` (`case_id`),
    KEY `idx_collector_id` (`collector_id`),
    KEY `idx_assigned_at` (`assigned_at`),
    KEY `idx_assigned_by` (`assigned_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分案日志表';
```

**索引说明**：
- `idx_tenant_id`：按甲方查询分案日志
- `idx_case_id`：查询案件的分案历史
- `idx_collector_id`：查询催员的分案历史
- `idx_assigned_at`：按时间查询分案日志
- `idx_assigned_by`：查询操作人的分案记录

---

**变更表：cases（案件表）**

```sql
-- 确保已有字段存在（如果没有则添加）
ALTER TABLE `cases` 
  ADD COLUMN IF NOT EXISTS `collector_id` BIGINT COMMENT '催员ID',
  ADD COLUMN IF NOT EXISTS `team_id` BIGINT COMMENT '小组ID',
  ADD COLUMN IF NOT EXISTS `agency_id` BIGINT COMMENT '机构ID',
  ADD COLUMN IF NOT EXISTS `assigned_at` DATETIME COMMENT '分配时间',
  ADD COLUMN IF NOT EXISTS `assigned_by` BIGINT COMMENT '分配人ID';

-- 添加索引（如果没有）
ALTER TABLE `cases` 
  ADD KEY IF NOT EXISTS `idx_collector_id` (`collector_id`),
  ADD KEY IF NOT EXISTS `idx_assigned_at` (`assigned_at`);
```

#### 3.2 分配算法模型

**按催员均分算法**：

```java
/**
 * 按催员均分分配案件
 * 
 * @param cases 案件列表
 * @param collectors 催员列表
 * @return 分配结果
 */
public BatchAssignResponse assignByCollector(List<Case> cases, List<Collector> collectors) {
    int caseCount = cases.size();
    int collectorCount = collectors.size();
    
    // 计算每个催员应分配的案件数
    int baseCount = caseCount / collectorCount; // 基础数量
    int remainder = caseCount % collectorCount; // 余数
    
    int caseIndex = 0;
    for (int i = 0; i < collectorCount; i++) {
        Collector collector = collectors.get(i);
        // 前remainder个催员多分配1个案件
        int assignCount = baseCount + (i < remainder ? 1 : 0);
        
        for (int j = 0; j < assignCount && caseIndex < caseCount; j++) {
            Case caseInfo = cases.get(caseIndex++);
            assignCaseToCollector(caseInfo, collector);
        }
    }
    
    return buildResponse();
}
```

**按队列均分算法**：

```java
/**
 * 按队列均分分配案件
 * 
 * @param cases 案件列表
 * @param collectors 催员列表
 * @return 分配结果
 */
public BatchAssignResponse assignByQueue(List<Case> cases, List<Collector> collectors) {
    // 按队列分组案件
    Map<Long, List<Case>> casesByQueue = cases.stream()
        .collect(Collectors.groupingBy(Case::getQueueId));
    
    // 按队列分组催员
    Map<Long, List<Collector>> collectorsByQueue = collectors.stream()
        .collect(Collectors.groupingBy(c -> c.getTeam().getQueueId()));
    
    // 遍历每个队列
    for (Map.Entry<Long, List<Case>> entry : casesByQueue.entrySet()) {
        Long queueId = entry.getKey();
        List<Case> queueCases = entry.getValue();
        List<Collector> queueCollectors = collectorsByQueue.get(queueId);
        
        if (queueCollectors == null || queueCollectors.isEmpty()) {
            // 该队列没有催员，记录警告
            log.warn("队列 {} 没有可用催员", queueId);
            continue;
        }
        
        // 队列内按催员均分
        assignByCollector(queueCases, queueCollectors);
    }
    
    return buildResponse();
}
```

### 4. 非功能性要求（Non-Functional Requirements）

#### 4.1 性能要求

| 接口 | QPS目标 | 响应时间（P99） | 峰值容量 |
|------|---------|--------------|---------|
| POST /api/v1/cases/batch-assign | 20 | <2000ms | 100 QPS |
| POST /api/v1/cases/check-queue-limit | 50 | <500ms | 200 QPS |
| POST /api/v1/cases/preview-assign | 30 | <300ms | 150 QPS |

**性能优化策略**：
- 批量分案采用批量更新SQL，减少数据库交互次数
- 队列匹配检查使用内存计算，避免多次数据库查询
- 分配预览使用缓存的催员案件数，避免实时统计
- 大批量分案（>500案件）建议分批处理

#### 4.2 可用性要求

**SLA目标**：
- 月度可用性：99.9%
- 故障恢复时间（MTTR）：<30分钟

**降级策略**：
1. **队列限制检查降级**：队列服务不可用时，跳过队列检查，允许分配
2. **分配预览降级**：统计服务不可用时，不显示预览，直接分配
3. **日志记录降级**：日志服务不可用时，降级为本地文件日志

#### 4.3 安全要求

**接口鉴权**：
- 所有批量分案接口需验证JWT Token
- 验证用户是否有批量分案权限
- 强制重新分配需要验证高级权限

**数据安全**：
- 分案日志加密存储敏感信息
- 防止SQL注入（使用参数化查询）
- 防止CSRF攻击（使用CSRF Token）

#### 4.4 扩展性要求

**水平扩展**：
- 应用服务器无状态设计，支持水平扩展
- 使用分布式锁（Redis）避免并发冲突

**容量规划**：
- 单次批量分案案件数上限：1000个
- 单个催员案件数上限：500个（预警）
- 系统总分案日志保留：6个月

### 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

#### 5.1 关键日志

**日志格式示例**：

1. **批量分案日志**
```
[INFO] ========== 批量分案开始，operated_by=1001, case_count=100, collector_count=3, strategy=by_collector ==========
[INFO] ========== 批量分案成功：case_id=1001, collector_id=201 ==========
[WARN] ========== 案件已分配，跳过：case_id=1002, current_collector_id=205 ==========
[ERROR] ========== 案件分配失败：case_id=1003, error=队列不匹配 ==========
[INFO] ========== 批量分案完成，success=95, failure=0, skipped=5, duration=1200ms ==========
```

2. **队列限制检查日志**
```
[INFO] ========== 队列限制检查，case_count=100, collector_count=3 ==========
[WARN] ========== 队列不匹配：case_id=1001, case_queue=M1, collector_queue=S1 ==========
[INFO] ========== 队列限制检查完成，matched=95, unmatched=5 ==========
```

3. **强制重新分配日志**
```
[WARN] ========== 强制重新分配，operated_by=1001, case_id=1001, old_collector=205, new_collector=201 ==========
```

#### 5.2 监控指标

| 指标名称 | 指标类型 | 采集维度 | 告警阈值 |
|---------|---------|---------|---------|
| batch_assign_qps | Counter | 接口+方法 | >100 QPS |
| batch_assign_response_time | Histogram | 接口+方法 | P99 > 3s |
| batch_assign_error_rate | Gauge | 接口+方法 | >10% |
| batch_assign_success_rate | Gauge | 甲方+操作人 | <90% |
| queue_mismatch_rate | Gauge | 甲方+队列 | >20% |
| force_reassign_count | Counter | 甲方+操作人 | >50次/日 |
| collector_case_count | Gauge | 催员 | >500个 |
| assign_lock_timeout_count | Counter | 全局 | >10次/小时 |

#### 5.3 告警规则

| 告警名称 | 触发条件 | 告警级别 | 通知方式 | 处理SLA |
|---------|---------|---------|---------|---------|
| 批量分案响应慢 | P99响应时间>3秒，持续5分钟 | P2（紧急） | 短信+钉钉 | 30分钟 |
| 批量分案错误率高 | 错误率>10%，持续3分钟 | P1（严重） | 电话+短信 | 15分钟 |
| 队列不匹配率高 | 不匹配率>20%，持续10分钟 | P3（警告） | 钉钉 | 2小时 |
| 强制重新分配频繁 | 1天内强制重新分配>50次（同一操作人） | P3（警告） | 邮件 | 1天 |
| 催员案件数超限 | 催员案件数>500个 | P4（提示） | 邮件 | 3天 |

### 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

#### 6.1 测试类型

**单元测试（覆盖率>80%）**：
- `assignByCollector()` 按催员均分算法
- `assignByQueue()` 按队列均分算法
- `assignAll()` 指定分配算法
- `checkQueueLimit()` 队列限制检查逻辑
- 边界值测试：0案件、1案件、大量案件（1000+）

**集成测试**：
- 批量分案接口端到端测试
- 队列限制检查+批量分案联合测试
- 强制重新分配流程测试
- 分配预览+确认分配流程测试

**性能测试**：
- 批量分案：20并发（100案件/批），响应时间<2秒
- 队列限制检查：50并发（100案件+10催员），响应时间<500ms
- 分配预览：30并发，响应时间<300ms

**业务场景测试**：
- 新案件批量分配
- 队列内案件均分
- 案件重新分配
- 跨队列分配（忽略限制）
- 大批量分案（1000案件）

**异常场景测试**：
- 案件已分配冲突处理
- 催员已禁用处理
- 队列不匹配处理
- 并发分案冲突处理
- 数据库连接失败处理

#### 6.2 验收标准

**关键验收标准（必须通过）**：

1. ✅ **功能完整性**
   - 三种分配策略（按催员均分、按队列均分、指定分配）正常工作
   - 队列匹配检查准确识别不匹配案件
   - 已分配案件检查正确识别已分配案件
   - 强制重新分配正确记录原催员信息
   - 分配预览准确显示每个催员的分配数量

2. ✅ **数据准确性**
   - 分配后案件的collector_id、team_id、agency_id正确更新
   - 分配日志完整记录所有分配操作
   - 统计数据（催员案件数）实时更新
   - 并发分案不会导致数据不一致

3. ✅ **性能指标**
   - 批量分案P99响应时间<2秒（100案件）
   - 队列限制检查P99响应时间<500ms
   - 分配预览P99响应时间<300ms
   - 支持20 QPS并发批量分案

4. ✅ **用户体验**
   - 分案对话框交互流畅，无卡顿
   - 队列匹配检查自动触发
   - 分配结果明确显示成功/失败/跳过数量
   - 失败详情清晰展示失败原因

5. ✅ **安全性**
   - 权限控制有效（强制重新分配需要高级权限）
   - 跨甲方分案被阻止
   - SQL注入防护有效
   - 分案日志完整记录操作人信息

### 7. 发布计划与回滚预案（Release Plan & Rollback）

#### 7.1 发布策略

**阶段1：灰度发布（1周）**
- **灰度对象**：2个测试甲方（案件量<500）
- **灰度步骤**：
  1. Day 1-2：灰度甲方1，开启批量分案功能
  2. Day 3-4：观察数据，无异常后灰度甲方2
  3. Day 5-7：评估灰度效果，决定是否扩大范围

**阶段2：小范围发布（2周）**
- **灰度对象**：10个甲方（案件量500-2000）
- **监控重点**：
  - 批量分案响应时间
  - 分案成功率
  - 队列匹配率
  - 用户反馈

**阶段3：全量发布（1周）**
- **灰度对象**：所有甲方
- **发布步骤**：
  1. Day 1-2：灰度50%甲方
  2. Day 3-4：观察数据，无异常后灰度至100%
  3. Day 5-7：全量监控，收集用户反馈

#### 7.2 回滚预案

**触发回滚条件**：
- 批量分案错误率>10%，持续10分钟
- 批量分案P99响应时间>5秒，持续10分钟
- 出现数据一致性问题（分案日志与案件状态不一致）
- 用户投诉量激增（>20个/小时）

**回滚方案**：

**方案1：配置回滚（优先，5分钟内完成）**
```yaml
# 关闭批量分案功能
feature.batch.assign.enabled: false
```

**方案2：代码回滚（30分钟内完成）**
```bash
# Git回退到上一稳定版本
git checkout v1.0.0-stable
# 重新编译部署
mvn clean package -Dmaven.test.skip=true
```

**方案3：数据回滚（60分钟内完成）**
```sql
-- 恢复数据库备份（慎用，会丢失数据）
mysql -u root -p < backup_before_release.sql
```

#### 7.3 应急联系人

| 角色 | 姓名 | 电话 | 职责 |
|------|------|------|------|
| 技术负责人 | 待指定 | - | 决策回滚，协调资源 |
| 后端开发 | 待指定 | - | 执行代码回滚，问题排查 |
| 前端开发 | 待指定 | - | 前端问题排查，配置调整 |
| DBA | 待指定 | - | 数据库回滚，数据恢复 |
| 运维 | 待指定 | - | 服务重启，监控告警 |
| 产品经理 | 待指定 | - | 用户沟通，需求调整 |

---

## 附录

### 附录A：术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| 批量分案 | Batch Assignment | 批量将案件分配给催员 |
| 分配策略 | Assignment Strategy | 案件分配的算法（均分、指定等） |
| 队列匹配 | Queue Matching | 案件队列与催员队列的匹配检查 |
| 强制重新分配 | Force Reassignment | 重新分配已分配案件 |
| 分配预览 | Assignment Preview | 分配前预览每个催员的分配数量 |
| 按催员均分 | Assign by Collector | 案件平均分配给所有催员 |
| 按队列均分 | Assign by Queue | 按队列分组后再均分 |
| 指定分配 | Assign All | 所有案件分配给指定催员 |

### 附录B：参考文档

1. **需求文档**：
   - [案件列表功能PRD.md](./案件列表功能PRD.md)
   - [控台端：甲方案件队列管理PRD.md](./控台端：甲方案件队列管理PRD.md)
   - [停留案件管理PRD.md](./停留案件管理PRD.md)

2. **技术文档**：
   - [后端API接口文档.md](../../../接口/1-管理控台API文档.md)
   - [数据库设计文档.md]
   - [前端组件开发规范.md]

3. **实现文档**：
   - 批量分案功能实现说明.md
   - 分配策略算法说明.md
   - 数据库迁移脚本.sql

### 附录C：变更历史

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|---------|--------|
| v1.0 | 2025-12-05 | 初始版本，完整PRD | AI Assistant |

---

**文档状态**：✅ 已完成  
**最后更新**：2025-12-05  
**版本**：v1.0.0

