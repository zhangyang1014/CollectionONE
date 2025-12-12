# 催员发送模板消息 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

**业务背景：**
催员在日常催收工作中需要频繁向借款人发送WhatsApp、短信等沟通消息。当前催员需要手动输入每条消息内容，存在以下痛点：
- 输入效率低下，重复劳动多
- 话术不规范，沟通质量参差不齐
- 缺乏标准化模板，新催员学习成本高
- 无法快速适配不同场景和案件阶段

**产品目标：**
为催员提供快速选择和发送消息模板的能力，通过控台端预配置的标准化话术模板，结合案件数据自动替换变量，实现：
- 催员发送消息效率提升50%以上
- 话术规范性提升，减少投诉率
- 新催员上手时间缩短30%
- 提升催收转化率和还款意愿

**核心指标：**
- 模板使用率：催员发送消息中使用模板的占比 ≥ 60%
- 发送效率：平均每条消息发送时间从30秒降低到10秒
- 话术合规率：投诉率下降20%
- 还款转化率：使用模板消息的案件还款率提升5-10%

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：首次联系借款人（S0阶段）**
- 入口：催员打开案件详情，点击"发送消息"
- 触发时机：案件分配后的第一次联系
- 使用流程：
  1. 催员点击消息输入框旁的"选择模板"按钮
  2. 系统智能推荐"S0阶段 + 上午/下午/晚上"的问候类模板
  3. 催员选择"早安问候 + 还款提醒"模板
  4. 系统自动替换变量（客户名、贷款编号、逾期天数等）
  5. 催员确认内容，选择发送渠道（WhatsApp/SMS/WABA/RCS）
  6. 点击发送

**场景2：逾期提醒（S1-3阶段）**
- 入口：催员在案件列表批量操作，或在IM面板逐个沟通
- 触发时机：借款人逾期3-7天，需要温和提醒
- 使用流程：
  1. 催员选择"下午催款提醒"模板
  2. 系统自动填充客户名、产品名称、逾期天数、贷款金额、应还金额等
  3. 催员手动微调内容（如添加个性化问候）
  4. 发送至WhatsApp

**场景3：强度催收（S3+阶段）**
- 入口：IM面板
- 触发时机：借款人严重逾期，需要强力催收
- 使用流程：
  1. 催员选择"晚间强度提醒"模板
  2. 系统填充客户名、App名称、贷款编号、逾期天数、应还金额
  3. 催员确认内容，发送

**场景4：催员使用个人模板**
- 入口：IM面板
- 触发时机：催员有自己常用的个性化话术
- 使用流程：
  1. 催员选择自己创建的个人模板
  2. 系统填充变量
  3. 发送

#### 2.2 用户画像

**主要用户：催员（Collector）**

**用户特征：**
- 角色：一线催收人员
- 工作强度：每天处理50-200个案件
- 技能水平：熟悉基础办公软件，但不具备技术背景
- 工作环境：使用PC端CCO管理控台 + 手机端IM工具
- 核心诉求：
  - 快速发送消息，减少重复输入
  - 话术规范，避免投诉
  - 适配不同场景和案件阶段
  - 支持个性化调整

**次要用户：催收主管（Team Leader）**
- 关注点：团队整体话术质量、合规性、转化率
- 诉求：能查看模板使用统计，优化话术策略

---

### 3. 关键业务流程（Business Flow）

#### 3.1 完整业务流程

```
[催员打开案件IM面板]
    ↓
[点击"选择模板"按钮]
    ↓
[模板选择对话框打开]
    ↓
[系统智能推荐] ← 根据案件阶段、当前时间智能筛选
    ↓
[催员筛选模板] ← 可按阶段/场景/时间点/关键词筛选
    ↓
[浏览模板列表] ← 显示模板名称、内容预览、标签、变量
    ↓
[点击选择模板]
    ↓
[系统获取案件数据] ← 调用API获取当前案件的字段值
    ↓
[自动替换变量] ← {客户名} → "张三"
    ↓
[内容填充到输入框]
    ↓
[催员确认/编辑内容] ← 可手动调整内容
    ↓
[选择发送渠道] ← WhatsApp/SMS/WABA/RCS
    ↓
[点击发送按钮]
    ↓
[消息发送] ← 调用对应渠道的发送接口
    ↓
[记录统计数据] ← 模板ID、催员ID、案件ID、渠道、是否成功
    ↓
[更新模板使用次数] ← message_templates.usage_count +1
    ↓
[完成]
```

#### 3.2 关键节点说明

**节点1：智能推荐**
- 根据当前案件阶段（cases.queue_id对应的stage）自动筛选对应阶段模板
- 根据当前时间推荐时间点：
  - 06:00-12:00 → 上午模板
  - 12:00-18:00 → 下午模板
  - 18:00-06:00 → 晚上模板
- 组织模板优先展示（个人模板排在后面）

**节点2：权限过滤**
- 仅显示催员所属机构可用的模板（根据agency_ids）
- 个人模板仅创建人可见
- 仅显示已启用的模板（is_enabled = 1）

**节点3：变量替换**
- 从案件数据中获取字段值（通过field_key映射）
- 支持的变量：
  - {客户名} → cases.user_name
  - {贷款编号} → cases.case_code
  - {逾期天数} → cases.overdue_days
  - {到期日期} → cases.due_date
  - {贷款金额} → cases.loan_amount
  - {应还金额} → cases.outstanding_amount
  - {本金} → cases.principal_amount
  - {罚息} → cases.late_fee
  - {产品名称} → cases.product_name
  - {App名称} → cases.app_name
- 如果字段值为空，保留变量占位符

**节点4：渠道选择**
- WhatsApp：优先渠道，支持富文本
- 短信（SMS）：备用渠道
- WABA（WhatsApp Business API）：企业级渠道
- RCS（富媒体短信）：支持图片、按钮等

**节点5：统计记录**
- 异步记录，不阻塞发送流程
- 记录数据：模板ID、催员ID、案件ID、发送渠道、实际内容、是否成功、时间戳

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 核心业务规则

**规则1：模板可见性规则**
- 租户隔离：催员仅可见本甲方（tenant_id）的模板
- 机构隔离：
  - 组织模板：`agency_ids IS NULL`（全机构可用）或 `JSON_CONTAINS(agency_ids, '催员机构ID')`
  - 个人模板：`created_by = 催员ID`
- 启用状态：仅显示 `is_enabled = 1` 的模板

**规则2：智能推荐规则**
- 案件阶段匹配：
  - 从当前案件所属队列（cases.queue_id）获取队列阶段（case_queues.queue_code）
  - 优先展示匹配该阶段的模板
  - 如果没有匹配模板，显示全部阶段模板
- 时间点匹配：
  - 获取当前系统时间的小时数
  - 推荐对应时间点的模板
  - 不做强制过滤，催员可选择其他时间点模板

**规则3：变量替换规则**
- 替换时机：催员点击选择模板后立即执行
- 数据来源：当前案件的字段值（基于甲方字段展示配置的field_key映射）
- 缺失值处理：
  - 如果字段值为 `null` 或空字符串，保留原始变量占位符
  - 示例：`{产品名称}` 无值 → 显示 `您在{App名称}的{产品名称}已逾期...`
  - 催员可手动删除占位符或填写实际值

**规则4：内容长度限制**
- WhatsApp：最长1000字符
- 短信（SMS）：最长500字符（超过按条数计费）
- WABA：最长1000字符
- RCS：最长2000字符
- 前端校验：超长时显示提示，禁用发送按钮

**规则5：发送渠道可用性**
- 根据案件客户的联系方式判断：
  - 有WhatsApp号码 → 可用WhatsApp/WABA
  - 有手机号 → 可用SMS/RCS
  - 优先级：WhatsApp > WABA > RCS > SMS
- 如果所有渠道不可用，提示催员补充联系方式

**规则6：模板使用统计规则**
- 记录触发时机：消息发送成功后
- 统计维度：
  - 模板维度：每个模板的总使用次数、成功率
  - 催员维度：每个催员最常用的模板Top 5
  - 渠道维度：各渠道的使用分布
  - 时间维度：按小时、日、周、月统计
- 统计用途：
  - 控台端展示模板使用次数（message_templates.usage_count）
  - 催收主管分析话术效果
  - 优化模板推荐策略

#### 4.2 范围说明

**本次需求范围内：**
- ✅ 催员选择和使用控台端配置的消息模板
- ✅ 自动替换变量为案件实际数据
- ✅ 支持催员手动编辑替换后的内容
- ✅ 支持多渠道发送（WhatsApp/SMS/WABA/RCS）
- ✅ 记录模板使用统计数据
- ✅ 智能推荐（根据案件阶段和当前时间）
- ✅ 权限过滤（机构隔离、个人模板隔离）

**范围外（后续迭代）：**
- ❌ 催员在IM端创建个人模板（需要在控台端创建）
- ❌ 批量发送模板消息
- ❌ 模板消息的AB测试
- ❌ 基于AI的智能话术生成
- ❌ 模板效果分析报表（还款转化率对比）
- ❌ 模板多语言支持

---

### 5. 合规与风控要求（Compliance & Risk Control）

#### 5.1 合规要求

**数据隐私保护：**
- 催员仅可访问被分配案件的客户数据
- 变量值获取需验证催员对该案件的访问权限
- 统计数据中的实际发送内容（content_sent）需脱敏存储（客户姓名、手机号打码）

**催收话术合规：**
- 禁止使用的词语：
  - 威胁性语言（"不还就上门"、"影响家人"）
  - 侮辱性语言
  - 虚假信息（"已起诉"、"法院传票"）
- 控台端模板审核：管理员创建模板时需经过合规审核
- IM端发送前校验：前端检测敏感词，后端记录原始内容供审计

**发送时间限制：**
- 禁止在晚上22:00-次日08:00发送催收消息（符合各国催收法规）
- 前端提示：超过时间范围时显示警告，但不强制拦截（允许催员选择定时发送）

#### 5.2 风控策略

**发送频次限制：**
- 单个案件每天最多发送5条模板消息
- 单个催员每小时最多发送100条消息（防止骚扰）
- 超过限制时前端提示，后端拦截

**异常行为监控：**
- 检测催员是否频繁发送相同模板（可能是误操作）
- 检测模板内容被大幅修改（偏离标准话术）
- 记录日志供风控团队分析

---

### 6. 资金路径与结算规则（Funding Flow & Settlement）

**不适用**：本功能不涉及资金流转。

---

### 7. 数据字段与口径（Data Definition）

#### 7.1 核心数据字段

**模板相关字段（来自 message_templates 表）：**

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 模板ID | 系统自增 | - |
| template_name | VARCHAR(200) | 模板名称 | 控台端配置 | 可编辑 |
| template_type | ENUM | 模板类型（organization/personal） | 控台端配置 | 可编辑 |
| agency_ids | JSON | 适用机构ID列表 | 控台端配置 | 可编辑 |
| case_stage | VARCHAR(20) | 案件阶段（C/S0/S1-3/S3+） | 控台端配置 | 可编辑 |
| scene | VARCHAR(50) | 场景（greeting/reminder/strong） | 控台端配置 | 可编辑 |
| time_slot | VARCHAR(20) | 时间点（morning/afternoon/evening） | 控台端配置 | 可编辑 |
| content | TEXT | 模板内容（含变量占位符） | 控台端配置 | 可编辑 |
| variables | JSON | 可用变量列表 | 控台端配置 | 可编辑 |
| is_enabled | TINYINT(1) | 是否启用 | 控台端配置 | 可编辑 |
| usage_count | INT | 使用次数统计 | 系统自动 | 实时更新 |

**案件变量字段（来自 cases 表）：**

| 字段名 | 类型 | 说明 | 来源 | 口径 |
|--------|------|------|------|------|
| user_name | VARCHAR(100) | 客户姓名 | 甲方推送 | - |
| case_code | VARCHAR(100) | 案件编号 | 系统生成 | 唯一标识 |
| overdue_days | INT | 逾期天数 | 系统计算 | 自然日，每日凌晨更新 |
| due_date | DATETIME | 到期日期 | 甲方推送 | - |
| loan_amount | DECIMAL(15,2) | 贷款金额 | 甲方推送 | 原始放款金额 |
| outstanding_amount | DECIMAL(15,2) | 应还金额 | 系统计算 | 未还本金+利息+罚息 |
| principal_amount | DECIMAL(15,2) | 本金 | 甲方推送 | 贷款本金 |
| late_fee | DECIMAL(15,2) | 罚息 | 系统计算 | 逾期罚息 |
| product_name | VARCHAR(100) | 产品名称 | 甲方推送 | - |
| app_name | VARCHAR(100) | App名称 | 甲方推送 | 借款来源平台 |

**统计数据字段（新增 message_template_usage_stats 表）：**

| 字段名 | 类型 | 说明 | 来源 | 更新频率 |
|--------|------|------|------|---------|
| id | BIGINT | 主键ID | 系统自增 | - |
| template_id | BIGINT | 模板ID | 催员选择 | - |
| collector_id | BIGINT | 催员ID | 当前登录用户 | - |
| case_id | BIGINT | 案件ID | 当前操作案件 | - |
| channel | VARCHAR(20) | 发送渠道 | 催员选择 | whatsapp/sms/waba/rcs |
| is_success | TINYINT(1) | 是否发送成功 | 发送结果回调 | 1-成功，0-失败 |
| content_sent | TEXT | 实际发送内容 | 替换变量后 | 脱敏存储 |
| used_at | TIMESTAMP | 使用时间 | 系统时间 | 发送时刻 |

#### 7.2 统计口径说明

**模板使用次数（usage_count）：**
- 统计口径：催员选择模板并成功发送的次数
- 更新时机：消息发送成功后 +1
- 更新方式：异步更新（通过消息队列或定时任务汇总）

**模板发送成功率：**
- 计算公式：`SUM(is_success = 1) / COUNT(*) * 100%`
- 统计周期：按日、周、月分别统计
- 用途：评估模板质量、渠道稳定性

**催员偏好模板Top 5：**
- 统计口径：每个催员使用次数最多的5个模板
- 统计周期：近30天
- 用途：个性化推荐

---

### 8. 交互与信息展示（UX & UI Brief）

#### 8.1 模板选择对话框

**入口：**
- 位置：IM消息输入框右侧
- 按钮文字："选择模板"
- 图标：📋（文档图标）

**对话框布局：**
- 标题：选择消息模板
- 宽度：900px
- 高度：自适应（最大70vh）

**筛选器区域：**
- 布局：横向排列4个下拉选择框 + 1个重置按钮
- 筛选项：
  1. 案件阶段（默认显示当前案件阶段）
  2. 组织/个人（默认"全部"）
  3. 场景（默认"全部"）
  4. 时间点（默认显示当前时间点）
  5. 重置按钮
- 交互：选择后立即筛选，无需点击"查询"按钮

**模板列表区域：**
- 布局：纵向滚动列表
- 高度：450px
- 空状态：显示"没有符合条件的模板"

**模板卡片：**
- 布局：
  ```
  +--------------------------------------------------+
  | [模板名称]                    [标签1] [标签2] [标签3] [标签4] |
  |--------------------------------------------------|
  | 模板内容预览（显示变量占位符格式，如{客户名}）            |
  | 最多显示3行，超出显示省略号...                        |
  |--------------------------------------------------|
  | 变量：[客户名] [贷款编号] [逾期天数] [应还金额]          |
  +--------------------------------------------------+
  ```
- 标签说明：
  - 标签1：案件阶段（S0/S1-3/S3+/C），蓝色
  - 标签2：模板类型（组织/个人），绿色/橙色
  - 标签3：场景（问候/提醒/强度），灰色
  - 标签4：时间点（上午/下午/晚上），灰色
- 交互：
  - 鼠标悬停：卡片阴影加深
  - 点击：选择模板，关闭对话框，内容填充到输入框

#### 8.2 消息输入框

**内容填充：**
- 选择模板后，替换后的内容自动填充到输入框
- 如果输入框已有内容，弹出确认提示："当前输入框有内容，是否覆盖？"

**编辑状态：**
- 催员可自由编辑内容
- 编辑后的内容仍然记录为"使用了模板"（记录模板ID）

**长度提示：**
- 实时显示当前字符数 / 最大字符数
- 示例：`245 / 1000`（WhatsApp）
- 超长时：字符数显示红色，禁用发送按钮

#### 8.3 发送渠道选择

**位置：**
- 消息输入框下方

**布局：**
- 横向排列4个单选按钮
- 选项：WhatsApp / SMS / WABA / RCS

**可用性标识：**
- 可用渠道：正常显示
- 不可用渠道：置灰，显示禁用图标，鼠标悬停提示原因（如"客户无WhatsApp号码"）

**默认选择：**
- 优先级：WhatsApp > WABA > RCS > SMS
- 自动选择第一个可用渠道

#### 8.4 发送按钮

**位置：**
- 消息输入框右下角

**状态：**
- 正常：蓝色主按钮，显示"发送"
- 禁用：灰色，禁用条件：
  - 输入框为空
  - 内容超长
  - 所有渠道不可用
  - 发送中（防止重复点击）
- 发送中：显示加载图标 + "发送中..."

**点击后：**
- 调用发送接口
- 成功：清空输入框，显示成功提示
- 失败：保留内容，显示错误提示

#### 8.5 智能推荐提示

**位置：**
- 模板选择对话框顶部

**显示条件：**
- 系统根据案件阶段和时间推荐模板时显示

**文案示例：**
- "当前案件为S0阶段，为您推荐首次联系模板"
- "当前为上午时段，为您推荐上午问候模板"

**交互：**
- 点击"查看全部"：清除筛选条件，显示所有模板

---

### 9. 配置项与运营开关（Config & Operation Switches）

#### 9.1 可配置参数

**系统级配置：**

| 配置项 | 说明 | 默认值 | 配置入口 |
|--------|------|--------|---------|
| template_cache_duration | 模板列表缓存时长（分钟） | 5 | 系统配置表 |
| max_content_length_whatsapp | WhatsApp最大字符数 | 1000 | 系统配置表 |
| max_content_length_sms | 短信最大字符数 | 500 | 系统配置表 |
| max_content_length_waba | WABA最大字符数 | 1000 | 系统配置表 |
| max_content_length_rcs | RCS最大字符数 | 2000 | 系统配置表 |
| daily_send_limit_per_case | 单案件每日发送上限 | 5 | 系统配置表 |
| hourly_send_limit_per_collector | 单催员每小时发送上限 | 100 | 系统配置表 |
| send_time_limit_start | 禁止发送开始时间 | 22:00 | 系统配置表 |
| send_time_limit_end | 禁止发送结束时间 | 08:00 | 系统配置表 |

**甲方级配置：**

| 配置项 | 说明 | 默认值 | 配置入口 |
|--------|------|--------|---------|
| enable_template_feature | 是否启用模板功能 | true | 甲方配置页 |
| enable_smart_recommend | 是否启用智能推荐 | true | 甲方配置页 |
| allow_edit_after_replace | 催员是否可编辑替换后内容 | true | 甲方配置页 |
| enable_send_time_check | 是否启用发送时间限制 | true | 甲方配置页 |
| enabled_channels | 可用发送渠道 | ["whatsapp","sms","waba","rcs"] | 甲方配置页 |

#### 9.2 运营开关

**功能开关（Feature Toggle）：**
- `template_feature_enabled`：模板功能总开关
  - 关闭后：催员端不显示"选择模板"按钮
  - 用途：紧急情况下快速关闭功能

**灰度发布开关：**
- `template_feature_agencies`：灰度机构列表
  - 配置格式：JSON数组，如 `[1, 2, 3]`
  - 用途：先对部分机构开放，验证后全量

**实验开关（AB测试）：**
- `template_recommend_strategy`：推荐策略
  - 策略A：根据案件阶段 + 时间推荐
  - 策略B：根据催员历史偏好推荐
  - 策略C：随机推荐
  - 用途：对比不同推荐策略的效果

#### 9.3 变更流程

**配置变更流程：**
1. 运营/产品经理提交变更申请
2. 技术负责人审核
3. 在测试环境验证
4. 在生产环境灰度发布
5. 监控关键指标（使用率、发送成功率）
6. 全量发布或回滚

**紧急回滚：**
- 发现严重问题时，可通过`template_feature_enabled`开关快速关闭
- 回滚时间：< 5分钟

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

#### 1.1 催员端埋点

| 触发时间点 | 埋点说明 | 埋点ID | 关键属性 |
|-----------|---------|--------|---------|
| 点击"选择模板"按钮 | 催员打开模板选择对话框 | `collector_open_template_dialog` | user_id, case_id, timestamp |
| 选择筛选条件 | 催员使用筛选器 | `collector_filter_template` | user_id, case_id, filter_type（stage/scene/time_slot）, filter_value, timestamp |
| 点击模板卡片 | 催员选择模板 | `collector_select_template` | user_id, case_id, template_id, template_name, template_type, timestamp |
| 编辑模板内容 | 催员修改替换后的内容 | `collector_edit_template_content` | user_id, case_id, template_id, edit_length（修改字符数）, timestamp |
| 点击发送按钮 | 催员发送模板消息 | `collector_send_template_message` | user_id, case_id, template_id, channel, content_length, is_edited（是否编辑过）, timestamp |
| 发送成功 | 消息发送成功 | `collector_send_template_success` | user_id, case_id, template_id, channel, timestamp |
| 发送失败 | 消息发送失败 | `collector_send_template_fail` | user_id, case_id, template_id, channel, error_code, error_message, timestamp |
| 关闭模板对话框（未选择） | 催员打开后未选择就关闭 | `collector_close_template_dialog` | user_id, case_id, duration（停留时长，秒）, timestamp |

#### 1.2 控台端埋点

| 触发时间点 | 埋点说明 | 埋点ID | 关键属性 |
|-----------|---------|--------|---------|
| 查看模板统计 | 主管查看模板使用统计 | `admin_view_template_stats` | user_id, template_id, stat_type（usage/success_rate/top_collectors）, timestamp |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 系统架构与模块划分（System Architecture & Modules）

#### 1.1 系统架构图

```
+------------------+        +-----------------------+
|  催员IM端         |        |  控台管理端            |
|  (Vue3 + TS)     |        |  (Vue3 + TS)         |
+------------------+        +-----------------------+
         |                             |
         | HTTP/REST                   | HTTP/REST
         |                             |
+--------------------------------------------------+
|           CCO业务中台（Spring Boot）              |
|  +--------------------------------------------+  |
|  | 模板管理模块                                |  |
|  | - MockMessageTemplateController (控台端)   |  |
|  | - IMMessageTemplateController (催员端，新增) | |
|  +--------------------------------------------+  |
|  | 权限管理模块                                |  |
|  | - 催员机构权限验证                          |  |
|  +--------------------------------------------+  |
|  | 案件管理模块                                |  |
|  | - 提供案件字段值（用于变量替换）              |  |
|  +--------------------------------------------+  |
|  | 消息发送模块                                |  |
|  | - WhatsApp发送                            |  |
|  | - SMS发送                                 |  |
|  | - WABA发送                                |  |
|  | - RCS发送                                 |  |
|  +--------------------------------------------+  |
|  | 统计分析模块                                |  |
|  | - 记录模板使用数据                          |  |
|  | - 生成统计报表                             |  |
|  +--------------------------------------------+  |
+--------------------------------------------------+
         |
         | JDBC
         |
+------------------+
|  MySQL数据库     |
|  - message_templates            |
|  - message_template_usage_stats |
|  - cases                        |
|  - collectors                   |
|  - agencies                     |
+------------------+
```

#### 1.2 新增/改造模块

**新增模块：**
1. **IMMessageTemplateController**（催员端模板控制器）
   - 职责：提供催员端获取模板列表、获取案件变量值、记录使用统计的API
   - 依赖：MessageTemplateService、CaseService、PermissionService

2. **TemplateVariableService**（变量替换服务）
   - 职责：根据案件ID和field_key获取字段值
   - 依赖：CaseService、TenantFieldDisplayConfigService

3. **TemplateUsageStatService**（使用统计服务）
   - 职责：记录模板使用数据、生成统计报表
   - 依赖：MessageTemplateUsageStatsRepository

**改造模块：**
1. **MessageTemplate实体类**
   - 新增方法：`incrementUsageCount()`（增加使用次数）

2. **IMPanel.vue前端组件**
   - 改造：将Mock数据替换为真实API调用
   - 新增：变量替换逻辑、统计记录逻辑

---

### 2. 接口设计与系统依赖（API Design & Dependencies）

#### 2.1 新增API接口

**接口1：获取催员可用模板列表**

```
GET /api/v1/im/message-templates
```

**请求参数（Query Parameters）：**
```
case_stage: string（可选）- 案件阶段，如"S0"
template_type: string（可选）- 模板类型，organization/personal
scene: string（可选）- 场景，greeting/reminder/strong
time_slot: string（可选）- 时间点，morning/afternoon/evening
keyword: string（可选）- 关键词搜索
page: int（可选）- 页码，默认1
page_size: int（可选）- 每页条数，默认20
```

**请求头：**
```
Authorization: Bearer {token}
```

**响应示例（成功）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "page": 1,
    "page_size": 20,
    "list": [
      {
        "id": 1,
        "template_name": "早安问候 + 还款提醒",
        "template_type": "organization",
        "case_stage": "S0",
        "scene": "greeting",
        "time_slot": "morning",
        "content": "您好{客户名}，早上好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}，请在{到期日期}前完成还款。谢谢您的配合！",
        "variables": ["客户名", "App名称", "产品名称", "贷款编号", "逾期天数", "应还金额", "到期日期"],
        "usage_count": 152,
        "sort_order": 10
      }
    ]
  }
}
```

**响应示例（失败）：**
```json
{
  "code": 401,
  "message": "Unauthorized: Invalid token",
  "data": null
}
```

**业务逻辑：**
1. 验证催员token，获取催员ID、甲方ID、机构ID
2. 查询模板：
   ```sql
   SELECT * FROM message_templates
   WHERE tenant_id = {催员所属甲方}
     AND is_enabled = 1
     AND (
       agency_ids IS NULL  -- 全机构可用
       OR JSON_CONTAINS(agency_ids, '{催员所属机构ID}')
       OR (template_type = 'personal' AND created_by = {催员ID})
     )
     AND ({case_stage} IS NULL OR case_stage = {case_stage})
     AND ({template_type} IS NULL OR template_type = {template_type})
     AND ({scene} IS NULL OR scene = {scene})
     AND ({time_slot} IS NULL OR time_slot = {time_slot})
     AND ({keyword} IS NULL OR template_name LIKE '%{keyword}%' OR content LIKE '%{keyword}%')
   ORDER BY sort_order ASC, created_at DESC
   LIMIT {offset}, {page_size}
   ```
3. 返回分页结果

**超时与重试：**
- 超时时间：3秒
- 重试策略：不重试（幂等查询，失败返回错误）
- 降级策略：返回空列表，前端使用本地缓存

**幂等性：**
- 幂等（查询接口）

---

**接口2：获取案件变量值**

```
GET /api/v1/im/cases/{case_id}/template-variables
```

**路径参数：**
```
case_id: long（必填）- 案件ID
```

**请求头：**
```
Authorization: Bearer {token}
```

**响应示例（成功）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user_name": "张三",
    "case_code": "BTSK-200100",
    "overdue_days": 23,
    "due_date": "2025-01-15",
    "loan_amount": "50,000",
    "outstanding_amount": "10,529",
    "principal_amount": "50,000",
    "late_fee": "529",
    "product_name": "快速贷",
    "app_name": "MegaPeso"
  }
}
```

**响应示例（失败）：**
```json
{
  "code": 403,
  "message": "Forbidden: 无权访问该案件",
  "data": null
}
```

**业务逻辑：**
1. 验证催员token，获取催员ID
2. 验证催员对该案件的访问权限（cases.collector_id = 催员ID）
3. 查询案件字段值：
   ```sql
   SELECT 
     user_name,
     case_code,
     overdue_days,
     due_date,
     loan_amount,
     outstanding_amount,
     principal_amount,
     late_fee,
     product_name,
     app_name
   FROM cases
   WHERE id = {case_id}
   ```
4. 格式化数值字段（金额添加千分位）
5. 返回字段值映射

**超时与重试：**
- 超时时间：2秒
- 重试策略：不重试
- 降级策略：返回空值，前端保留变量占位符

**幂等性：**
- 幂等（查询接口）

---

**接口3：记录模板使用统计**

```
POST /api/v1/im/message-templates/{template_id}/use
```

**路径参数：**
```
template_id: long（必填）- 模板ID
```

**请求头：**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求Body：**
```json
{
  "case_id": 12345,
  "channel": "whatsapp",
  "content_sent": "您好张三，早上好！您在MegaPeso的快速贷...",
  "is_success": true
}
```

**字段说明：**
- `case_id`：案件ID
- `channel`：发送渠道，枚举值：whatsapp/sms/waba/rcs
- `content_sent`：实际发送的内容（替换变量后）
- `is_success`：是否发送成功

**响应示例（成功）：**
```json
{
  "code": 200,
  "message": "统计记录成功",
  "data": null
}
```

**响应示例（失败）：**
```json
{
  "code": 400,
  "message": "Invalid channel: xxx",
  "data": null
}
```

**业务逻辑：**
1. 验证催员token，获取催员ID
2. 验证请求参数（channel必须为枚举值之一）
3. 插入统计记录：
   ```sql
   INSERT INTO message_template_usage_stats 
   (template_id, collector_id, case_id, channel, is_success, content_sent, used_at)
   VALUES 
   ({template_id}, {催员ID}, {case_id}, {channel}, {is_success}, {content_sent}, NOW())
   ```
4. 异步更新模板使用次数：
   ```sql
   UPDATE message_templates 
   SET usage_count = usage_count + 1 
   WHERE id = {template_id}
   ```
5. 返回成功响应

**超时与重试：**
- 超时时间：5秒
- 重试策略：失败不重试（防止重复记录）
- 降级策略：记录失败日志，不影响消息发送流程

**幂等性：**
- 非幂等（每次调用都会插入新记录）
- 建议：前端确保仅在消息发送成功后调用一次

---

#### 2.2 依赖的外部接口

**消息发送接口（已有）：**
- WhatsApp发送：`POST /api/v1/messages/whatsapp/send`
- SMS发送：`POST /api/v1/messages/sms/send`
- WABA发送：`POST /api/v1/messages/waba/send`
- RCS发送：`POST /api/v1/messages/rcs/send`

**说明：**
- 本需求复用现有的消息发送接口
- 模板功能仅负责生成消息内容，不改造发送逻辑

---

### 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

#### 3.1 新增数据表

**表名：message_template_usage_stats**

**用途：**记录模板使用统计数据，用于分析模板效果、催员偏好、渠道分布等

**DDL：**
```sql
CREATE TABLE IF NOT EXISTS message_template_usage_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  template_id BIGINT NOT NULL COMMENT '模板ID',
  collector_id BIGINT NOT NULL COMMENT '催员ID',
  case_id BIGINT NOT NULL COMMENT '案件ID',
  channel VARCHAR(20) NOT NULL COMMENT '发送渠道：whatsapp/sms/waba/rcs',
  is_success TINYINT(1) DEFAULT 1 COMMENT '是否发送成功：1-成功，0-失败',
  content_sent TEXT COMMENT '实际发送内容（替换变量后，需脱敏存储）',
  used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '使用时间',
  
  INDEX idx_template_time (template_id, used_at) COMMENT '模板统计索引',
  INDEX idx_collector_time (collector_id, used_at) COMMENT '催员统计索引',
  INDEX idx_case (case_id) COMMENT '案件索引',
  INDEX idx_channel (channel) COMMENT '渠道索引',
  INDEX idx_used_at (used_at) COMMENT '时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板使用统计表';
```

**主键/唯一键说明：**
- 主键：`id`（自增）
- 无唯一键（允许同一模板被多次使用）

**索引说明：**
- `idx_template_time`：用于按模板ID统计使用次数、成功率（按时间范围）
- `idx_collector_time`：用于按催员ID统计偏好模板（按时间范围）
- `idx_case`：用于查询某案件使用了哪些模板
- `idx_channel`：用于统计各渠道使用分布
- `idx_used_at`：用于按时间维度统计（日报、周报、月报）

**数据生命周期：**
- 保留时长：90天
- 清理策略：定时任务每周清理90天前的数据
- 归档策略：历史数据归档到大数据平台（用于长期分析）

#### 3.2 变更的数据表

**表名：message_templates**

**变更内容：**
- 新增字段：无
- 变更逻辑：`usage_count`字段需要实时更新（每次使用 +1）

**更新SQL：**
```sql
UPDATE message_templates 
SET usage_count = usage_count + 1 
WHERE id = {template_id}
```

**并发控制：**
- 使用乐观锁或原子更新（UPDATE ... SET usage_count = usage_count + 1）
- 不需要事务（允许偶尔丢失少量统计数据）

#### 3.3 依赖的算法模型

**智能推荐模型（V1.0 - 基于规则）：**
- 模型类型：规则引擎
- 输入特征：
  - 当前案件阶段（cases.queue_id → case_queues.queue_code）
  - 当前时间（系统时间 → 上午/下午/晚上）
  - 催员历史偏好（message_template_usage_stats聚合）
- 输出结果：推荐模板列表（按权重排序）
- 推荐规则：
  1. 案件阶段匹配权重 +10
  2. 时间点匹配权重 +5
  3. 催员历史偏好权重 +3
  4. 组织模板权重 +2
- 版本管理：存储在代码中（TemplateRecommendService.java）
- 回滚方案：通过配置开关`enable_smart_recommend`关闭，使用默认排序

**未来迭代（V2.0 - 基于机器学习）：**
- 模型类型：协同过滤 + XGBoost
- 输入特征：
  - 催员画像（工作时长、催收成功率、历史偏好）
  - 案件画像（逾期天数、金额、客户特征）
  - 模板画像（使用频次、成功率、场景标签）
- 输出结果：个性化推荐Top 10模板
- 负责团队：数据科学团队
- 版本管理：模型文件存储在OSS，通过版本号管理
- 回滚方案：切换到V1.0规则引擎

---

### 4. 非功能性要求（Non-Functional Requirements）

#### 4.1 性能要求

**响应时间：**
- 获取模板列表：P95 < 500ms，P99 < 1s
- 获取案件变量值：P95 < 300ms，P99 < 500ms
- 记录使用统计：P95 < 200ms，P99 < 500ms（异步处理）

**吞吐量（QPS）：**
- 获取模板列表：预计QPS 100（假设100个在线催员，每人每分钟打开1次）
- 获取案件变量值：预计QPS 100
- 记录使用统计：预计QPS 50（实际发送消息频率较低）

**峰值容量：**
- 支持500个催员同时在线
- 单催员每小时最多发送100条消息
- 高峰期QPS峰值：500

**数据量估算：**
- 模板数量：每个甲方平均50个模板，支持100个甲方 = 5000个模板
- 统计数据量：
  - 每天每个催员使用20次模板
  - 500个催员 × 20次/天 × 365天 = 365万条/年
  - 保留90天 ≈ 90万条

#### 4.2 可用性要求

**SLA（Service Level Agreement）：**
- 服务可用性：99.9%（每月允许停机43分钟）
- 数据持久性：99.999%（数据库主从复制 + 每日备份）

**降级策略：**
1. **模板列表加载失败**：
   - 降级方案：前端使用本地缓存（5分钟有效期）
   - 如果缓存也失效：显示空列表，提示催员稍后重试

2. **变量值获取失败**：
   - 降级方案：保留变量占位符，催员手动填写
   - 示例：`您好{客户名}，您的贷款...`

3. **统计记录失败**：
   - 降级方案：记录日志，异步重试（通过消息队列）
   - 不影响消息发送流程

**故障恢复：**
- 数据库故障：自动切换到从库（RTO < 1分钟）
- 应用服务故障：Kubernetes自动重启（RTO < 30秒）

#### 4.3 安全要求

**接口鉴权：**
- 所有API接口必须验证JWT Token
- Token有效期：2小时
- Token刷新机制：滑动窗口（每次请求自动延长30分钟）

**权限控制：**
- 催员仅可访问被分配案件的数据
- 后端验证：`cases.collector_id = 当前催员ID`
- 跨租户隔离：`cases.tenant_id = 催员所属甲方ID`

**数据脱敏：**
- 统计数据中的`content_sent`字段需脱敏存储：
  - 客户姓名：张** / Mr. Z**
  - 手机号：138****1234
  - 身份证号：42010119******1234
- 脱敏规则：仅保留首字符 + 中间打码 + 尾部字符

**SQL注入防护：**
- 所有数据库查询使用参数化查询（PreparedStatement）
- 禁止拼接SQL字符串

**XSS防护：**
- 前端显示模板内容时，转义HTML特殊字符
- 后端存储时，不做转义（保留原始内容）

#### 4.4 扩展性要求

**水平扩展：**
- 应用服务无状态设计，支持水平扩容
- 数据库读写分离，读库可扩展多个从库

**多机房部署：**
- 支持多区域部署（如东南亚、南亚、非洲）
- 数据库跨区域复制（延迟 < 5秒）

**容灾：**
- 主数据库故障：自动切换到从库
- 应用服务故障：Kubernetes自动重启
- 整个区域故障：流量切换到备用区域（RTO < 5分钟）

---

### 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

#### 5.1 关键日志

**应用日志（Application Log）：**

**日志级别：INFO**
```
[TemplateController] 催员 {collector_id} 获取模板列表，筛选条件：stage={stage}, scene={scene}, time_slot={time_slot}
[TemplateController] 催员 {collector_id} 选择模板 {template_id}（{template_name}）
[TemplateController] 催员 {collector_id} 获取案件 {case_id} 的变量值
[TemplateController] 催员 {collector_id} 使用模板 {template_id} 发送消息至案件 {case_id}，渠道：{channel}，是否成功：{is_success}
```

**日志级别：WARN**
```
[TemplateController] 催员 {collector_id} 无权访问案件 {case_id}
[TemplateController] 变量值获取失败，案件 {case_id} 不存在
[TemplateController] 统计记录失败，template_id={template_id}, error={error_message}
```

**日志级别：ERROR**
```
[TemplateController] 获取模板列表失败，error={error_message}, stack_trace={stack_trace}
[TemplateController] 数据库连接失败，error={error_message}
```

**三方调用日志：**
```
[WhatsAppClient] 发送消息至 {phone_number}，template_id={template_id}，响应：{response_code}
[SMSClient] 发送短信至 {phone_number}，template_id={template_id}，响应：{response_code}
```

#### 5.2 监控指标（Metrics）

**业务指标：**

| 指标名称 | 说明 | 统计周期 | 告警阈值 |
|---------|------|---------|---------|
| template_usage_count | 模板使用次数 | 1分钟 | < 10（业务低迷） |
| template_send_success_rate | 模板发送成功率 | 5分钟 | < 95% |
| template_list_load_time_p95 | 模板列表加载时长P95 | 1分钟 | > 500ms |
| template_variable_load_time_p95 | 变量值加载时长P95 | 1分钟 | > 300ms |
| template_cache_hit_rate | 模板列表缓存命中率 | 5分钟 | < 80% |
| channel_usage_distribution | 各渠道使用分布 | 1小时 | - |
| collector_usage_frequency | 催员使用频率 | 1小时 | - |

**系统指标：**

| 指标名称 | 说明 | 统计周期 | 告警阈值 |
|---------|------|---------|---------|
| api_qps | API请求QPS | 1分钟 | > 500 |
| api_error_rate | API错误率 | 5分钟 | > 5% |
| api_response_time_p99 | API响应时长P99 | 1分钟 | > 1s |
| db_connection_pool_usage | 数据库连接池使用率 | 1分钟 | > 80% |
| db_slow_query_count | 慢查询数量 | 5分钟 | > 10 |

#### 5.3 告警规则

**P0级告警（严重，立即处理）：**
- 模板列表接口5分钟内错误率 > 50%
- 数据库主库不可用
- 应用服务全部节点宕机

**P1级告警（重要，30分钟内处理）：**
- 模板发送成功率 < 80%
- API响应时长P99 > 2秒
- 数据库连接池使用率 > 90%

**P2级告警（一般，2小时内处理）：**
- 模板使用次数异常低（< 10次/分钟，且持续10分钟）
- 缓存命中率 < 70%
- 慢查询数量 > 20

**告警方式：**
- P0级：电话 + 短信 + 企业微信 + PagerDuty
- P1级：短信 + 企业微信
- P2级：企业微信

**告警接收人：**
- P0级：技术负责人 + 运维负责人 + 产品经理
- P1级：开发工程师 + 运维工程师
- P2级：开发工程师

---

### 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

#### 6.1 测试类型

**单元测试（Unit Test）：**
- 覆盖率要求：核心业务逻辑 ≥ 80%
- 测试内容：
  - TemplateRecommendService：推荐算法逻辑
  - TemplateVariableService：变量替换逻辑
  - TemplateUsageStatService：统计记录逻辑
- 工具：JUnit 5 + Mockito

**集成测试（Integration Test）：**
- 测试内容：
  - API接口调用（模拟催员登录，调用模板列表、变量值、统计接口）
  - 数据库读写（验证数据正确性、并发更新）
- 工具：Spring Boot Test + TestContainers（启动MySQL容器）

**接口测试（API Test）：**
- 测试内容：
  - 正常场景：获取模板列表、选择模板、发送消息
  - 异常场景：无权限访问、参数错误、数据库故障
  - 边界场景：空模板列表、变量值为空、超长内容
- 工具：Postman + Newman（自动化）

**前端测试（Frontend Test）：**
- 测试内容：
  - 模板选择对话框UI交互
  - 变量替换逻辑正确性
  - 发送渠道选择
- 工具：Vitest + Vue Test Utils

**性能测试（Performance Test）：**
- 测试内容：
  - 500个并发催员获取模板列表，P95响应时长 < 500ms
  - 单库支持1000 QPS（获取模板列表）
- 工具：JMeter / Gatling

**安全测试（Security Test）：**
- 测试内容：
  - SQL注入攻击测试
  - XSS攻击测试
  - 越权访问测试（催员A访问催员B的案件）
- 工具：OWASP ZAP / Burp Suite

#### 6.2 验收标准（Acceptance Criteria）

**关键验收标准：**

1. ✅ **功能完整性**
   - 催员可以打开模板选择对话框
   - 催员可以按4个维度筛选模板
   - 催员可以选择模板，内容自动填充到输入框
   - 变量自动替换为案件实际数据
   - 催员可以手动编辑替换后的内容
   - 催员可以选择发送渠道并发送消息
   - 统计数据正确记录（template_id、collector_id、case_id、channel、is_success）

2. ✅ **权限控制**
   - 催员仅可见本机构可用的模板
   - 个人模板仅创建人可见
   - 催员无法访问未分配案件的变量值

3. ✅ **性能达标**
   - 模板列表加载 P95 < 500ms
   - 变量值获取 P95 < 300ms
   - 统计记录 P95 < 200ms

4. ✅ **数据正确性**
   - 变量替换逻辑正确（10个变量全部验证）
   - 统计数据记录准确（与实际发送次数一致）
   - 模板使用次数（usage_count）正确累加

5. ✅ **异常处理**
   - API调用失败时，前端显示友好提示
   - 变量值获取失败时，保留占位符
   - 统计记录失败时，不影响消息发送

6. ✅ **兼容性**
   - 支持Chrome、Safari、Firefox最新版本
   - 支持移动端H5浏览器

7. ✅ **合规性**
   - 脱敏数据正确存储
   - 发送时间限制生效（22:00-08:00禁止发送）

---

### 7. 发布计划与回滚预案（Release Plan & Rollback）

#### 7.1 发布策略

**分阶段发布：**

**第1阶段：灰度发布（1天）**
- 时间：2025-12-10 10:00
- 范围：1个测试甲方（10个催员）
- 验证指标：
  - 模板使用率 ≥ 50%
  - 发送成功率 ≥ 95%
  - API错误率 < 1%
- 验证通过条件：无P0/P1告警，功能正常使用

**第2阶段：小范围发布（3天）**
- 时间：2025-12-11 10:00
- 范围：5个甲方（约50个催员）
- 验证指标：同第1阶段
- 验证通过条件：用户满意度 ≥ 4.0/5.0

**第3阶段：全量发布（7天）**
- 时间：2025-12-14 10:00
- 范围：所有甲方（约500个催员）
- 验证指标：同第1阶段
- 验证通过条件：核心指标达标（模板使用率 ≥ 60%，还款转化率提升 ≥ 5%）

**流量切换方式：**
- 通过配置开关`template_feature_agencies`控制灰度机构列表
- 前端根据催员所属机构ID判断是否显示"选择模板"按钮

#### 7.2 依赖配置

**数据库初始化：**
1. 执行DDL创建`message_template_usage_stats`表
2. 验证索引创建成功
3. 初始化测试数据（可选）

**配置项初始化：**
```sql
INSERT INTO system_configs (config_key, config_value, description) VALUES
('template_cache_duration', '5', '模板列表缓存时长（分钟）'),
('max_content_length_whatsapp', '1000', 'WhatsApp最大字符数'),
('daily_send_limit_per_case', '5', '单案件每日发送上限'),
('hourly_send_limit_per_collector', '100', '单催员每小时发送上限');
```

**开关配置：**
```sql
INSERT INTO feature_toggles (feature_key, is_enabled, agencies) VALUES
('template_feature_enabled', 1, NULL),  -- 全局开关，默认开启
('template_feature_agencies', 1, '[1]');  -- 灰度机构列表，初始仅测试甲方
```

#### 7.3 回滚预案

**回滚触发条件：**
- P0级告警且无法快速修复（30分钟内）
- 模板发送成功率 < 70%（持续10分钟）
- 用户大量投诉（> 10条/小时）
- 数据库性能严重下降（QPS下降 > 50%）

**回滚方案：**

**方案1：关闭功能开关（最快，< 5分钟）**
```sql
UPDATE feature_toggles 
SET is_enabled = 0 
WHERE feature_key = 'template_feature_enabled';
```
- 影响：所有催员无法使用模板功能，恢复到手动输入模式
- 优点：快速回滚，无数据丢失
- 缺点：影响用户体验

**方案2：切量到旧版本（< 10分钟）**
- 操作：
  1. 将灰度机构列表清空：`UPDATE feature_toggles SET agencies = '[]' WHERE feature_key = 'template_feature_agencies'`
  2. Kubernetes回滚到上一个版本：`kubectl rollout undo deployment/cco-backend`
- 影响：所有催员无法使用模板功能
- 优点：完全恢复到上一个稳定版本
- 缺点：需要运维操作

**方案3：降级到只读模式（< 5分钟）**
- 操作：关闭统计记录功能，仅保留模板选择和发送功能
- SQL：`UPDATE feature_toggles SET is_enabled = 0 WHERE feature_key = 'template_stats_enabled'`
- 影响：统计数据暂时无法记录
- 优点：核心功能可用
- 缺点：数据统计不完整

**回滚责任人：**
- 决策人：技术负责人 + 产品经理
- 执行人：运维工程师
- 通知人：客服团队（通知用户）

**回滚后验证：**
1. 验证功能开关生效（催员端不显示"选择模板"按钮）
2. 验证原有功能正常（消息手动输入和发送）
3. 验证数据库负载恢复正常
4. 收集回滚后的用户反馈

#### 7.4 应急联系人

| 角色 | 姓名 | 电话 | 微信 | 职责 |
|------|------|------|------|------|
| 技术负责人 | XXX | 138****1234 | xxx_tech | 技术决策、回滚审批 |
| 产品经理 | XXX | 139****5678 | xxx_pm | 业务决策、用户沟通 |
| 后端开发 | XXX | 137****9012 | xxx_backend | 后端问题排查 |
| 前端开发 | XXX | 136****3456 | xxx_frontend | 前端问题排查 |
| 运维工程师 | XXX | 135****7890 | xxx_ops | 服务器、数据库运维 |
| 客服经理 | XXX | 134****2468 | xxx_cs | 用户问题收集、通知 |

---

## 附录

### A. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 模板 | Template | 预配置的消息话术，包含变量占位符 |
| 变量 | Variable | 模板中可替换的占位符，如{客户名} |
| 变量替换 | Variable Replacement | 将变量占位符替换为案件实际数据的过程 |
| 组织模板 | Organization Template | 由管理员创建，全机构或部分机构催员可用的模板 |
| 个人模板 | Personal Template | 由催员个人创建，仅创建人可见的模板 |
| 案件阶段 | Case Stage | 催收案件所处的阶段，如C/S0/S1-3/S3+ |
| 场景 | Scene | 消息发送的场景，如问候、提醒、强度 |
| 时间点 | Time Slot | 消息发送的时间段，如上午、下午、晚上 |
| 智能推荐 | Smart Recommendation | 根据案件阶段、时间等自动推荐合适模板的功能 |
| 使用次数 | Usage Count | 模板被催员选择并发送的累计次数 |
| 发送成功率 | Send Success Rate | 模板发送成功的次数占总使用次数的百分比 |

### B. 变量映射表（Variable Mapping）

| 变量名 | 变量标识 | 字段标识(field_key) | 数据表 | 数据类型 | 示例值 |
|--------|---------|-------------------|--------|---------|--------|
| 客户名 | `{客户名}` | user_name | cases | String | 张三 |
| 贷款编号 | `{贷款编号}` | case_code | cases | String | BTSK-200100 |
| 逾期天数 | `{逾期天数}` | overdue_days | cases | Integer | 23 |
| 到期日期 | `{到期日期}` | due_date | cases | Date | 2025-01-15 |
| 贷款金额 | `{贷款金额}` | loan_amount | cases | Decimal | 50,000 |
| 应还金额 | `{应还金额}` | outstanding_amount | cases | Decimal | 10,529 |
| 本金 | `{本金}` | principal_amount | cases | Decimal | 50,000 |
| 罚息 | `{罚息}` | late_fee | cases | Decimal | 529 |
| 产品名称 | `{产品名称}` | product_name | cases | String | 快速贷 |
| App名称 | `{App名称}` | app_name | cases | String | MegaPeso |

### C. 模板示例（Template Examples）

**示例1：早安问候 + 还款提醒（S0阶段，上午，问候场景）**

**模板内容：**
```
您好{客户名}，早上好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}，请在{到期日期}前完成还款。谢谢您的配合！
```

**替换后内容：**
```
您好张三，早上好！您在MegaPeso的快速贷（贷款编号：BTSK-200100）已逾期23天，应还金额10,529，请在2025-01-15前完成还款。谢谢您的配合！
```

**示例2：下午催款提醒（S1-3阶段，下午，提醒场景）**

**模板内容：**
```
{客户名}您好，您的{产品名称}（贷款编号：{贷款编号}）逾期已{逾期天数}天，贷款金额{贷款金额}，未还金额{应还金额}（本金{本金}+罚息{罚息}），请今日内完成还款，如有困难请联系我们！
```

**替换后内容：**
```
张三您好，您的快速贷（贷款编号：BTSK-200100）逾期已23天，贷款金额50,000，未还金额10,529（本金50,000+罚息529），请今日内完成还款，如有困难请联系我们！
```

**示例3：晚间强度提醒（S3+阶段，晚上，强度场景）**

**模板内容：**
```
{客户名}，您在{App名称}的贷款（{贷款编号}）已严重逾期{逾期天数}天，未还金额{应还金额}，如不立即还款我们将采取法律措施！
```

**替换后内容：**
```
张三，您在MegaPeso的贷款（BTSK-200100）已严重逾期23天，未还金额10,529，如不立即还款我们将采取法律措施！
```

---

**文档版本：** V1.0.0  
**撰写日期：** 2025-12-03  
**撰写人：** AI Product Manager  
**审核人：** -  
**最后更新：** 2025-12-03



















