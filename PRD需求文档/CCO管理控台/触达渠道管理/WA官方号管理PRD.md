# WA 官方号管理 PRD

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.0 | 2026-03-05 | 初版：整合 PHONE 管理、IP 管理、采购渠道管理、WA 配置四大模块 | 大象 |

---

## 1. 概述

### 1.1 背景

催收业务中，催员通过 WhatsApp（以下简称 WA）与债务人进行即时通讯触达。WA 账号的生命周期管理（从云机号码采购、IP 代理配置、号码激活投养到分配给催员使用，以及封号后的申诉处理）是保障催收通道畅通的核心运营能力。

### 1.2 目标

- 建立 WA 官方号从**采购 → 配置 → 投养 → 分配 → 使用 → 异常处理**的全生命周期管理能力
- 通过**统计看板**实时掌握号码资产的健康状况
- 通过**自动化配置**降低运营人力成本
- 通过**渠道质量监控**评估供应商质量，优化采购决策

### 1.3 使用角色

| 角色 | 使用场景 |
|------|----------|
| **运营主管** | 配置全局参数、管理采购渠道、查看号码总览、审批分配策略 |
| **运营专员** | 日常操作：登记号码、绑定IP、激活号码、分配号码、处理申诉 |
| **运维人员** | IP资源管理、IP健康度监控、IP换绑处理 |

### 1.4 模块入口

WA 管理模块位于控台左侧菜单「WA管理」分组下，包含四个子菜单：

| 菜单项 | 路由路径 | 功能定位 |
|--------|----------|----------|
| PHONE管理 | `/wa-management/phones` | 号码全生命周期管理（核心） |
| IP管理 | `/wa-management/ips` | 代理 IP 资源管理与健康度监控 |
| PHONE购买渠道管理 | `/wa-management/purchase-channels` | 云机供应商渠道管理与质量评估 |
| 配置 | `/wa-management/config` | 全局参数与自动化开关 |

---

## 2. 核心概念与术语

| 术语 | 说明 |
|------|------|
| **Instant（实例）** | Geelark 云机平台上的一个虚拟手机实例，对应一个 `instantId` |
| **Phone** | Instant 激活后获得的 WhatsApp 手机号码 |
| **IP（代理IP）** | WA 账号运行所需的网络代理地址，每个 IP 可承载多个 Phone |
| **投养（Nurture）** | 新号码激活后进入的"养号"阶段，通过模拟正常社交行为提升号码信任度 |
| **获客数量** | 投养期间通过该号码主动添加的联系人数量 |
| **健康度** | IP 的综合质量评分（0–100），由封号率、负载率、在线稳定性等综合计算 |

---

## 3. 数据模型

### 3.1 状态枚举定义

#### PHONE 状态（phoneStatus）

| 枚举值 | 显示名称 | 说明 |
|--------|----------|------|
| `PENDING_IP` | 待绑定IP | 号码已登记，等待分配代理IP |
| `PENDING_ACTIVATION` | 待激活 | 已绑定IP，等待在 Geelark 平台激活获取手机号 |
| `ACTIVATED` | 已激活 | 激活成功，已获得手机号 |

#### 投养状态（nurtureStatus）

| 枚举值 | 显示名称 | 说明 |
|--------|----------|------|
| `PENDING` | 待投养 | 已激活但尚未开始投养 |
| `NURTURING` | 投养中 | 正在执行养号流程 |
| `COMPLETED` | 投养完成 | 养号完成，可以投入使用 |

#### WA 号码状态（waStatus）

| 枚举值 | 显示名称 | 说明 |
|--------|----------|------|
| `NORMAL` | 正常 | WA 账号正常可用 |
| `BANNED` | 封号待申诉 | WA 账号被封禁，等待运营发起申诉 |
| `APPEALING` | 申诉中 | 已提交申诉，等待 WA 官方处理结果 |
| `DISABLED` | 已停用 | 永久停用（申诉失败或手动停用） |

#### 分配状态（assignStatus）

| 枚举值 | 显示名称 | 说明 |
|--------|----------|------|
| `UNASSIGNED` | 待分配 | 未分配给任何催员 |
| `ASSIGNED` | 已分配 | 已分配给催员使用 |

#### IP 状态（ipStatus）

| 枚举值 | 显示名称 | 说明 |
|--------|----------|------|
| `ACTIVE` | 在线 | IP 代理正常运行 |
| `INACTIVE` | 离线 | IP 代理不可用 |

#### 申诉结果（appealResult）

| 枚举值 | 说明 | 后续状态 |
|--------|------|----------|
| `SUCCESS` | 申诉成功，账号恢复 | waStatus → `NORMAL` |
| `FAILURE` | 申诉失败，永久封禁 | waStatus → `DISABLED` |

### 3.2 核心实体

#### WaPhone（号码）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 主键 |
| instantId | string | Geelark 实例 ID |
| purchaseChannelId | number | 采购渠道 ID |
| purchaseChannelName | string | 采购渠道名称 |
| phone | string | WA 手机号码（激活后填入） |
| ipId | number / null | 绑定的 IP 主键 |
| ipAddress | string | 绑定的 IP 地址 |
| phoneStatus | PhoneStatus | PHONE 状态 |
| nurtureStatus | NurtureStatus | 投养状态 |
| waStatus | WaStatus | WA 号码状态 |
| activatedAt | string | 激活时间 |
| nurtureStartedAt | string | 投养开始时间 |
| nurtureDays | number | 已投养天数 |
| acquisitionCount | number | 投养期间获客数量 |
| assignStatus | AssignStatus | 分配状态 |
| assignedAt | string | 分配时间 |
| assignedCollectorId | number / null | 分配催员 ID |
| assignedCollectorName | string | 分配催员姓名 |
| cumulativeUsageHours | number | 累计使用时长（小时） |
| offlineAt | string | 掉线时间（有值表示当前掉线） |
| appealedAt | string | 申诉提交时间 |

#### WaIp（IP 资源）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 主键 |
| ipAddress | string | IP 地址 |
| port | number | 端口号 |
| accountName | string | 代理登录账号 |
| password | string | 代理登录密码 |
| status | IpStatus | IP 状态 |
| onlineAt | string | 上线时间 |
| cumulativeServiceHours | number | 累计服务时长（小时） |
| healthScore | number | 健康度评分（0–100） |
| linkedPhoneCount | number | 关联 Phone 数 |
| banRate | number | 封号率（0–1） |
| loadRate | number | 负载率（0–1） |
| offlinePhoneCount | number | 该 IP 下掉线的 Phone 数 |
| avgSurvivalHours | number | WA 平均存活时长 |
| avgUsageHours | number | WA 平均使用时长 |

#### WaConfig（全局配置）

| 字段 | 类型 | 说明 |
|------|------|------|
| phonesPerIp | number | 单个 IP 可绑定的 Phone 数量上限（1–20） |
| autoIpAssign | boolean | 是否自动分配 IP |
| autoActivate | boolean | 是否自动激活（调用 Geelark API） |
| autoNurture | boolean | 是否自动进入投养 |
| autoAssign | boolean | 投养完成后是否自动分配催员 |
| autoAssignRule | string | 自动分配规则 |

#### WaPurchaseChannel（采购渠道）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 主键 |
| channelName | string | 渠道名称 |
| description | string | 渠道说明 |
| isEnabled | boolean | 是否启用 |

---

## 4. 号码全生命周期状态流转

### 4.1 主流程状态机

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                                号码生命周期主流程                                      │
│                                                                                      │
│  ┌────────┐    绑定IP     ┌─────────────┐    激活      ┌───────────┐                 │
│  │待绑定IP │ ──────────→  │  待激活       │ ─────────→  │  已激活    │                 │
│  │PENDING  │              │  PENDING     │             │ ACTIVATED │                 │
│  │  _IP    │              │ _ACTIVATION  │             └─────┬─────┘                 │
│  └────────┘              └─────────────┘                    │                        │
│                                                              ▼                        │
│                                                        ┌──────────┐                  │
│                                                        │  待投养   │                  │
│                                                        │ PENDING  │                  │
│                                                        └────┬─────┘                  │
│                                                              │ 开始投养                │
│                                                              ▼                        │
│                                                        ┌──────────┐                  │
│                                                        │  投养中   │                  │
│                                                        │NURTURING │                  │
│                                                        └────┬─────┘                  │
│                                                              │ 投养完成                │
│                                                              ▼                        │
│                        分配催员                         ┌──────────┐                  │
│                   ┌──────────────────────────────────  │  待分配   │                  │
│                   │                                    │COMPLETED │                  │
│                   ▼                                    │UNASSIGNED│                  │
│              ┌──────────┐                              └──────────┘                  │
│              │  使用中   │                                                            │
│              │ ASSIGNED │                                                            │
│              │ NORMAL   │                                                            │
│              └────┬─────┘                                                            │
│                   │                                                                   │
│         ┌────────┴────────┐                                                          │
│         │                  │                                                          │
│         ▼                  ▼                                                          │
│    ┌────────┐        ┌──────────┐                                                    │
│    │ 掉线中  │        │  封号     │                                                    │
│    │offlineAt│        │ BANNED   │                                                    │
│    │ 有值    │        └────┬─────┘                                                    │
│    └───┬────┘              │ 发起申诉                                                 │
│        │ 恢复               ▼                                                         │
│        │ 在线         ┌──────────┐                                                    │
│        ▼              │  申诉中   │                                                    │
│    ┌────────┐         │APPEALING │                                                    │
│    │ 正常   │         └────┬─────┘                                                    │
│    │使用中  │              │                                                           │
│    └────────┘     ┌───────┴───────┐                                                  │
│                   │                │                                                   │
│                   ▼                ▼                                                   │
│             ┌──────────┐    ┌──────────┐                                              │
│             │ 申诉成功  │    │ 申诉失败  │                                              │
│             │→ NORMAL  │    │→ DISABLED│                                              │
│             └──────────┘    └──────────┘                                              │
│                                                                                       │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 状态流转详细说明

| 序号 | 触发动作 | 前置状态 | 后置状态 | 操作人 | 说明 |
|------|----------|----------|----------|--------|------|
| 1 | 云号码登记 | （新建） | phoneStatus=`PENDING_IP` | 运营专员 | 从采购渠道导入 instantId |
| 2 | 绑定 IP | phoneStatus=`PENDING_IP` | phoneStatus=`PENDING_ACTIVATION` | 运营专员/系统 | 选择可用 IP 进行绑定 |
| 3 | 激活号码 | phoneStatus=`PENDING_ACTIVATION` | phoneStatus=`ACTIVATED`, nurtureStatus=`PENDING` | 运营专员/系统 | 在 Geelark 平台激活后填入手机号 |
| 4 | 开始投养 | nurtureStatus=`PENDING` | nurtureStatus=`NURTURING` | 系统 | 自动或手动触发养号流程 |
| 5 | 投养完成 | nurtureStatus=`NURTURING` | nurtureStatus=`COMPLETED` | 系统 | 达到投养天数后自动完成 |
| 6 | 分配催员 | nurtureStatus=`COMPLETED`, assignStatus=`UNASSIGNED` | assignStatus=`ASSIGNED` | 运营专员/系统 | 将号码分配给催员使用 |
| 7 | 号码掉线 | waStatus=`NORMAL` | offlineAt 赋值 | 系统 | 检测到 WA 连接中断 |
| 8 | 恢复在线 | offlineAt 有值 | offlineAt 清空 | 系统 | 连接恢复 |
| 9 | WA 封号 | waStatus=`NORMAL` | waStatus=`BANNED` | 系统（外部触发） | WA 官方封禁账号 |
| 10 | 发起申诉 | waStatus=`BANNED` | waStatus=`APPEALING` | 运营专员 | 向 WA 官方提交申诉 |
| 11 | 申诉成功 | waStatus=`APPEALING` | waStatus=`NORMAL` | 运营专员（填写结果） | 账号恢复正常 |
| 12 | 申诉失败 | waStatus=`APPEALING` | waStatus=`DISABLED` | 运营专员（填写结果） | 永久停用 |
| 13 | 手动停用 | 任意 | waStatus=`DISABLED` | 运营主管 | 人工判定号码不可用 |
| 14 | 回收号码 | assignStatus=`ASSIGNED` | assignStatus=`UNASSIGNED` | 运营专员 | 从催员处回收号码（封号/离职等） |
| 15 | IP 换绑 | 已有 IP | 更换新 IP | 运营/运维 | 原 IP 异常时切换 |

### 4.3 生命周期事件类型

每个号码的所有关键操作都会记录为生命周期事件，可在「生命周期」弹窗中以时间线形式查看：

| 事件类型 | 事件名称 | 触发时机 |
|----------|----------|----------|
| `REGISTERED` | 号码登记 | 通过云号码登记功能导入 instantId |
| `IP_BOUND` | IP 绑定 | 首次绑定代理 IP |
| `IP_CHANGED` | IP 变更 | 换绑到新的 IP（记录新旧 IP） |
| `ACTIVATED` | 激活 | 在 Geelark 激活并获得手机号 |
| `NURTURE_STARTED` | 开始投养 | 进入养号流程 |
| `NURTURE_COMPLETED` | 投养完成 | 养号结束 |
| `ASSIGNED` | 分配催员 | 分配给催员（记录催员信息） |
| `RECLAIMED` | 回收号码 | 从催员处回收 |
| `OFFLINE` | 号码掉线 | WA 连接中断 |
| `BACK_ONLINE` | 恢复在线 | WA 连接恢复 |
| `BANNED` | WA 封号 | WA 账号被封禁 |
| `APPEALED` | 提交申诉 | 运营发起封号申诉 |
| `DISABLED` | 停用 | 号码永久停用 |

---

## 5. 功能模块详述

### 5.1 PHONE 管理（核心模块）

#### 5.1.1 使用场景

- **场景 A：新号码批量入库**——运营从渠道采购一批云机实例后，批量登记到系统中
- **场景 B：日常运营流转**——逐步完成 IP 绑定 → 激活 → 投养 → 分配的全流程
- **场景 C：问题号码处理**——封号后发起申诉，跟踪申诉结果并更新状态
- **场景 D：资产总览**——通过统计面板掌握号码生命周期分布和健康状态

#### 5.1.2 页面结构

页面自上而下分为三个区域：

**区域一：统计面板（两行卡片）**

第一行——**号码生命周期分布**（互斥分桶，合计 = 总 Instant 数）：

| 指标 | 统计口径 | 颜色 |
|------|----------|------|
| 云机配置中 | phoneStatus = `PENDING_IP` 或 `PENDING_ACTIVATION`，且 waStatus = `NORMAL` | 灰色 |
| 待投养 | phoneStatus = `ACTIVATED` + nurtureStatus = `PENDING` + waStatus = `NORMAL` | 浅蓝 |
| 投养中 | nurtureStatus = `NURTURING` + waStatus = `NORMAL` | 橙黄 |
| 待分配 | nurtureStatus = `COMPLETED` + assignStatus = `UNASSIGNED` + waStatus = `NORMAL` | 紫色（可点击跳转） |
| 使用中 | assignStatus = `ASSIGNED` + waStatus = `NORMAL`（含掉线） | 绿色 |
| 问题号 | waStatus ∈ {`BANNED`, `APPEALING`, `DISABLED`}（**最高优先级**，优先于其他分桶） | 红色 |

> **互斥分桶逻辑**：waStatus 异常（BANNED/APPEALING/DISABLED）的号码一律归入"问题号"桶，不再出现在其他桶中。

第二行——**健康状态**（独立统计，各指标可重叠）：

| 指标 | 统计口径 | 百分比公式 |
|------|----------|-----------|
| 已养成 | nurtureStatus = `COMPLETED` | 已养成 ÷ 已激活 |
| 正常使用中 | assignStatus = `ASSIGNED` + waStatus = `NORMAL` 且 offlineAt 为空 | 正常使用中 ÷ 已养成 |
| 掉线中 | offlineAt 非空 + waStatus = `NORMAL` | 掉线中 ÷ 使用中 |
| 封号中 | waStatus = `BANNED` | 封号中 ÷ 已养成 |
| 解封中 | waStatus = `APPEALING` | 解封中 ÷ 问题号总数 |
| 彻底停用 | waStatus = `DISABLED` | 停用 ÷ 已养成 |

**区域二：筛选与操作栏**

筛选器：

| 筛选项 | 类型 | 选项 |
|--------|------|------|
| 关键字搜索 | 文本输入 | 搜索 Instant ID / 手机号 / 催员姓名 |
| 购买渠道 | 下拉选择 | 动态加载启用的渠道列表 |
| PHONE 状态 | 下拉选择 | 待绑定IP / 待激活 / 已激活 |
| 投养状态 | 下拉选择 | 待投养 / 投养中 / 投养完成 |
| WA 状态 | 下拉选择 | 正常 / 封号待申诉 / 申诉中 / 已停用 |
| 分配状态 | 下拉选择 | 待分配 / 已分配 |

快捷 Tab（切换时自动设置对应筛选条件）：

| Tab 名称 | 对应筛选条件 |
|----------|-------------|
| 全部 | 无筛选 |
| 待绑定IP | phoneStatus = `PENDING_IP` |
| 待激活 | phoneStatus = `PENDING_ACTIVATION` |
| 待投养 | phoneStatus = `ACTIVATED` + nurtureStatus = `PENDING` |
| 投养中 | phoneStatus = `ACTIVATED` + nurtureStatus = `NURTURING` |
| 待分配 | phoneStatus = `ACTIVATED` + nurtureStatus = `COMPLETED` + waStatus = `NORMAL` + assignStatus = `UNASSIGNED` |
| 封号待申诉 | waStatus = `BANNED` |
| 等待申诉结果 | waStatus = `APPEALING` |

> Tab 上方带 Badge 数字提示的：待绑定IP、待分配、等待申诉结果。

批量操作按钮：

| 按钮 | 可见条件 | 可用条件 | 功能 |
|------|----------|----------|------|
| 云号码登记 | 始终可见 | 始终可用 | 打开云号码登记弹窗 |
| 批量绑定IP | 始终可见 | 勾选 ≥ 1 行 | 打开 IP 绑定弹窗（批量模式） |
| 批量分配 | 始终可见 | 勾选 ≥ 1 行 | 打开分配弹窗（批量模式） |
| 批量申诉 | 仅「封号待申诉」Tab | 勾选行全部为 BANNED | 批量提交申诉 |
| 批量填写申诉结果 | 仅「等待申诉结果」Tab | 勾选行全部为 APPEALING | 打开批量申诉结果弹窗 |

**区域三：数据表格**

表格采用**三组分列**设计，每组可独立折叠/展开：

| 分组 | 颜色主题 | 包含列 | 折叠时保留列 |
|------|----------|--------|-------------|
| 云机信息 | 蓝色 | Instant ID、渠道、关联IP、PHONE状态、Phone | Instant ID、Phone |
| WA 投养 | 橙色 | 投养状态、激活时间、投养时间、投养天数、获客数量 | 投养状态 |
| WA 使用 | 紫色 | WA号码状态、分配状态、分配时间、当前CCO员工、累计使用时间、掉线时间 | WA号码状态、分配状态 |

> Tab 切换时自动调整各分组的默认折叠状态，聚焦当前阶段相关信息。例如在"投养中"Tab下，云机信息折叠、WA投养展开、WA使用折叠。

行样式规则：
- 掉线行（offlineAt 有值且 waStatus = NORMAL）：浅红底色
- 封号行（waStatus = BANNED）：浅橙底色

行内操作按钮：

| 按钮 | 显示条件 | 功能 |
|------|----------|------|
| 绑定IP / 换绑IP | 始终显示（已有IP则显示"换绑"） | 打开 IP 绑定弹窗（单号模式） |
| IP记录 | 始终显示 | 查看该号码的 IP 变更历史 |
| 分配记录 | 始终显示 | 查看该号码的分配/回收历史 |
| 生命周期 | 始终显示 | 查看完整生命周期时间线 |
| 激活 | phoneStatus = `PENDING_ACTIVATION` | 打开激活弹窗，填入手机号 |
| 分配 | nurtureStatus = `COMPLETED` 且无催员 | 打开分配弹窗（单号模式） |
| 申诉 | waStatus = `BANNED` | 确认后发起申诉 |
| 填写申诉结果 | waStatus = `APPEALING` | 打开申诉结果弹窗 |

#### 5.1.3 弹窗交互

**云号码登记弹窗（PhoneRegisterDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 批量导入新购买的云机 Instant ID |
| 输入项 | ① 选择购买渠道（下拉，必选）② 导入方式：文件上传（.txt / .xlsx / .csv）或手动输入（每行一个 instantId） |
| 输出 | 创建 N 条 phoneStatus = `PENDING_IP` 的号码记录 |
| 校验 | instantId 不能重复 |

**IP 绑定弹窗（IpBindDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 为号码绑定或换绑代理 IP |
| 模式 | 单号模式（行操作触发）/ 批量模式（勾选多行触发） |
| IP 选择 | 展示可用 IP 列表，显示：IP 地址、已绑定数/上限、健康度 |
| 副作用 | 如果是首次绑定（PENDING_IP），状态变为 `PENDING_ACTIVATION`；如果是换绑，记录 IP 变更日志 |

**激活弹窗（内联于 PhoneManagement）**

| 项目 | 说明 |
|------|------|
| 用途 | 号码在 Geelark 激活后，录入获得的手机号码 |
| 前置条件 | phoneStatus = `PENDING_ACTIVATION` |
| 输入项 | 手机号码（格式如 `+52 55 1234 5678`） |
| 输出 | phoneStatus → `ACTIVATED`，nurtureStatus → `PENDING`，记录 activatedAt |
| 提示信息 | "请先到 Geelark 平台完成激活，获取手机号后填入下方" |

**分配弹窗（PhoneAssignDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 将投养完成的号码分配给催员 |
| 模式 | 单号模式 / 批量模式 |
| 催员选择 | 展示催员列表，支持搜索，显示：姓名、所属小组、业绩评分 |
| 输出 | assignStatus → `ASSIGNED`，记录 assignedAt、assignedCollectorId、assignedCollectorName |

**申诉结果弹窗（内联于 PhoneManagement）**

| 项目 | 说明 |
|------|------|
| 用途 | 运营在 WA 中查看申诉结果后，回到系统中记录 |
| 前置条件 | waStatus = `APPEALING` |
| 展示信息 | 手机号、申诉时间、已等待时长 |
| 操作 | 两张卡片式选择——"申诉成功"或"申诉失败"，点击后二次确认 |
| 申诉成功 | waStatus → `NORMAL`，号码恢复可用 |
| 申诉失败 | waStatus → `DISABLED`，号码永久停用 |
| 判定标准 | 成功：打开 WA 账号正常可用；失败：① 显示"已被永久封禁" ② 换设备提示"账号已被禁止登录" |

**批量申诉结果弹窗**

| 项目 | 说明 |
|------|------|
| 用途 | 对多个申诉中号码统一填写结果 |
| 前提 | 勾选行全部为 waStatus = `APPEALING` |
| 操作 | 同单号申诉结果，但统一应用到所有选中号码 |

**IP 变更记录弹窗（IpChangeLogDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 查看某号码的 IP 换绑历史 |
| 展示字段 | 旧 IP、新 IP、变更原因、操作人、变更时间 |

**分配记录弹窗（PhoneAssignLogDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 查看某号码的分配/回收历史 |
| 展示字段 | 催员姓名、所属小组、分配时间、回收时间、使用时长、操作人、备注 |

**生命周期弹窗（PhoneLifecycleDialog）**

| 项目 | 说明 |
|------|------|
| 用途 | 以时间线形式展示号码从注册到当前的全部关键事件 |
| 展示内容 | 事件类型图标、事件名称、详细描述、操作人、时间 |

---

### 5.2 IP 管理

#### 5.2.1 使用场景

- **场景 A：IP 资源规划**——根据 Instant 总数和配置的每 IP 承载量，计算 IP 缺口
- **场景 B：IP 质量监控**——通过健康度、封号率、负载率等指标评估 IP 质量
- **场景 C：IP 资源维护**——新增、编辑、删除 IP 资源

#### 5.2.2 页面结构

**IP 需求推算卡片**

| 指标 | 计算方式 |
|------|----------|
| 总 Instant 数 | 系统中所有 Phone 的数量 |
| 每 IP 可用 Phone 数 | 来自 WA 配置中的 `phonesPerIp` |
| 需要 IP 总数 | ⌈ 总 Instant 数 ÷ 每 IP 可用 Phone 数 ⌉ |
| 当前活跃 IP 数 | status = `ACTIVE` 的 IP 数量 |
| 需新增 IP 数 | max(0, 需要 IP 总数 - 当前活跃 IP 数)。**大于 0 时红色高亮** |

**IP 列表**

| 列名 | 说明 | 特殊展示 |
|------|------|----------|
| IP 地址 | IP 地址 | — |
| 端口 | 端口号 | — |
| 账号名 | 代理登录账号 | — |
| 状态 | ACTIVE / INACTIVE | Tag 标签 |
| 上线时间 | IP 首次上线时间 | — |
| 累计服务时间 | IP 累计在线服务时长 | 格式化为"X天Y小时" |
| 封号率 | 该 IP 下被封号码数 ÷ 总绑定数 | 有封号时红色标签 |
| 负载率 | 已绑定 Phone 数 ÷ 每 IP 上限数 | 进度条（>80% 橙色，满载红色） |
| 健康度 | 综合评分 0–100 | 进度条（≥80 绿，≥60 橙，<60 红） |
| WA 存活时间 | 该 IP 下号码平均存活时长 | 仅统计存活 > 3 天或已封号的号码 |
| WA 使用时间 | 该 IP 下号码平均使用时长 | 仅统计存活 > 3 天或已封号的号码 |
| 关联 Phone | 当前绑定的 Phone 数量 | — |
| 操作 | 编辑 / 删除 | 有关联 Phone 时不可删除 |

> 复选框：仅允许勾选无关联 Phone 的行，支持批量删除。

#### 5.2.3 IP 健康度评分算法

基础分 100 分，按以下规则增减：

| 序号 | 规则 | 分值变化 |
|------|------|----------|
| ① | IP 状态为 INACTIVE | -40 |
| ② | 封号率扣分 | -(banRate × 40)，最多扣 40 |
| ③ | 负载率 > 80% | -10 |
| ③ | 负载率 = 100%（满载） | -20（替代上条） |
| ④ | 累计服务时长 > 720h（30天） | +5 |
| ④ | 累计服务时长 > 2160h（90天） | +10（替代上条） |
| ⑤ | 掉线 Phone 数 | 每个 -3，最多扣 15 |

最终得分限制在 [0, 100] 区间。

#### 5.2.4 新增/编辑 IP 弹窗

| 字段 | 类型 | 校验 |
|------|------|------|
| IP 地址 | 文本输入 | 必填，格式 `X.X.X.X` |
| 端口 | 数字输入 | 必填，范围 1–65535，默认 8443 |
| 账号 | 文本输入 | 必填 |
| 密码 | 密码输入 | 必填 |

---

### 5.3 PHONE 购买渠道管理

#### 5.3.1 使用场景

- **场景 A：渠道维护**——新增、编辑、启用/禁用、删除采购渠道
- **场景 B：渠道质量评估**——通过封号率、投养完成率、可用率、平均存活/使用时长对比各渠道质量
- **场景 C：号码溯源**——点击渠道下的 Instant 数量，跳转到 PHONE 管理页面并自动筛选该渠道

#### 5.3.2 页面结构

**渠道列表**

| 列名 | 说明 | 特殊展示 |
|------|------|----------|
| 渠道名称 | 供应商名称 | — |
| 说明 | 渠道描述 | — |
| Instant 数量 | 该渠道下注册的 Phone 总数 | **可点击**，跳转 PHONE 管理并筛选 |
| 封号率 | 已封号 ÷ 总数 | < 10% 绿色，10%–30% 橙色，≥ 30% 红色 |
| 投养完成率 | nurtureStatus = COMPLETED ÷ 总数 | 进度条（≥ 70% 绿，≥ 40% 橙，< 40% 红） |
| 可用率 | waStatus = NORMAL ÷ 总数 | ≥ 80% 绿色，50%–80% 橙色，< 50% 红色 |
| 均存活时长 | 平均存活时长 | 仅统计存活 > 3 天或已封号的号码 |
| 均使用时长 | 平均使用时长 | 仅统计存活 > 3 天或已封号的号码 |
| 状态 | 启用/禁用开关 | Switch 组件 |
| 创建时间 | — | — |
| 操作 | 编辑 / 删除 | — |

#### 5.3.3 新增/编辑渠道弹窗

| 字段 | 类型 | 校验 |
|------|------|------|
| 渠道名称 | 文本输入 | 必填 |
| 说明 | 多行文本输入 | 选填 |

---

### 5.4 WA 配置

#### 5.4.1 使用场景

- **场景 A：基础参数配置**——设置每个 IP 可承载的 Phone 数量上限
- **场景 B：自动化流程配置**——根据运营团队成熟度，逐步开启自动化开关

#### 5.4.2 配置项说明

**基础配置**

| 配置项 | 说明 | 范围 | 影响 |
|--------|------|------|------|
| 一个 IP 可被多少 Phone 使用 | 单个 IP 可绑定的 Phone 数量上限 | 1–20 | 影响 IP 需求计算、负载率计算 |

**自动化配置**

| 配置项 | 开启效果 | 关闭效果 |
|--------|----------|----------|
| 自动 IP 分配 | 系统自动按阈值分配 IP 给新登记的 Phone | 需运营手动给 instant 分配 IP |
| 自动激活 | 调用 Geelark API 自动激活 | 需运营去 Geelark 平台手动激活 |
| 自动进入投养 | 激活后自动调用 API 进入投养流程 | 需手动触发投养 |
| 自动分配投养完成的账号 | 投养完成后自动分配给 CCO 坐席 | 需运营手动分配 |

**自动分配规则**（仅在"自动分配"开启时可见）

| 规则 | 说明 |
|------|------|
| 高业绩催员优先 | 按催员业绩评分降序分配 |
| 低负载催员优先 | 按催员当前持有号码数升序分配 |
| 轮询分配 | 按顺序循环分配 |

---

## 6. 统计指标计算口径

### 6.1 WA 存活/使用时长统计口径

以下统计仅纳入符合条件的号码：

**纳入条件**（满足任一即纳入统计样本）：
1. waStatus = `BANNED`（已封号的号码）
2. 存活时间超过 3 天（72 小时）：`当前时间（或下线时间）- activatedAt ≥ 72h`

**平均存活时长计算**：

```
avgSurvivalHours = Σ(endTime - activatedAt) / 样本数

其中 endTime = offlineAt（有值时） 或 当前时间（无值时）
```

**平均使用时长计算**：

```
avgUsageHours = Σ(cumulativeUsageHours) / 样本数
```

### 6.2 健康状态百分比口径

| 指标 | 分子 | 分母 |
|------|------|------|
| 养成率 | 已养成数 | 已激活数（总数 - 云机配置中） |
| 使用率 | 正常使用中数 | 已养成数 |
| 掉线率 | 掉线中数 | 使用中数（含掉线） |
| 封号率 | 封号中数 | 已养成数 |
| 申诉率 | 解封中数 | 问题号总数 |
| 停用率 | 彻底停用数 | 已养成数 |

---

## 7. 接口清单

### 7.1 Phone 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `getPhoneList(params)` | GET | 分页查询号码列表，支持多维度筛选 |
| `getPhoneStats()` | GET | 获取号码统计数据（两行看板） |
| `registerPhones(req)` | POST | 云号码批量登记 |
| `bindPhoneIp(phoneId, ipId)` | POST | 单号绑定/换绑 IP |
| `batchBindIp(phoneIds, ipId)` | POST | 批量绑定 IP |
| `activatePhone(phoneId, phoneNumber)` | POST | 激活号码（填入手机号） |
| `assignPhone(phoneId, collectorId, collectorName)` | POST | 单号分配催员 |
| `batchAssignPhones(phoneIds, collectorId, collectorName)` | POST | 批量分配催员 |
| `appealPhone(phoneId)` | POST | 发起申诉 |
| `batchAppealPhones(phoneIds)` | POST | 批量发起申诉 |
| `submitAppealResult(phoneId, result)` | POST | 填写单号申诉结果 |
| `batchResolveAppeal(phoneIds, result)` | POST | 批量填写申诉结果 |
| `disablePhone(phoneId)` | POST | 停用号码 |
| `getIpChangeLogs(phoneId)` | GET | 查询号码 IP 变更记录 |
| `getPhoneAssignLogs(phoneId)` | GET | 查询号码分配记录 |
| `getPhoneLifecycle(phoneId)` | GET | 查询号码生命周期事件 |

### 7.2 IP 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `getIpList()` | GET | 获取 IP 列表（含动态计算的健康度等指标） |
| `createIp(req)` | POST | 新增 IP |
| `updateIp(id, data)` | PUT | 更新 IP 信息 |
| `deleteIp(id)` | DELETE | 删除 IP（需无关联 Phone） |
| `getIpDemand()` | GET | IP 需求推算 |

### 7.3 配置与渠道接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `getWaConfig()` | GET | 获取 WA 全局配置 |
| `updateWaConfig(data)` | PUT | 更新 WA 全局配置 |
| `getPurchaseChannels()` | GET | 获取采购渠道列表 |
| `createPurchaseChannel(data)` | POST | 新增采购渠道 |
| `updatePurchaseChannel(id, data)` | PUT | 更新采购渠道 |
| `deletePurchaseChannel(id)` | DELETE | 删除采购渠道 |
| `getChannelStats()` | GET | 获取各渠道质量统计 |
| `getCollectorsForAssign()` | GET | 获取可分配催员列表 |

---

## 8. 业务规则汇总

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-01 | 号码登记时状态初始化 | phoneStatus = `PENDING_IP`，nurtureStatus = `PENDING`，waStatus = `NORMAL`，assignStatus = `UNASSIGNED` |
| BR-02 | 绑定 IP 后状态变更 | 仅当 phoneStatus = `PENDING_IP` 时变为 `PENDING_ACTIVATION` |
| BR-03 | 激活后状态变更 | phoneStatus → `ACTIVATED`，nurtureStatus → `PENDING` |
| BR-04 | 申诉前置条件 | 仅 waStatus = `BANNED` 的号码可发起申诉 |
| BR-05 | 申诉结果不可撤销 | 填写申诉结果后，状态变更不可逆（成功 → NORMAL，失败 → DISABLED） |
| BR-06 | 分配前置条件 | 号码需 nurtureStatus = `COMPLETED` 且未分配 |
| BR-07 | IP 删除限制 | 有关联 Phone 的 IP 不允许删除 |
| BR-08 | 渠道跳转 | 点击渠道 Instant 数量跳转 Phone 管理页并自动筛选 purchaseChannelId |
| BR-09 | 问题号优先级 | 统计分桶时 waStatus 异常的号码优先归入"问题号"桶 |
| BR-10 | 统计样本过滤 | 存活/使用时长统计仅纳入存活 > 3 天或已封号的号码 |

---

## 9. 当前实现状态

| 模块 | 前端 | 后端 | 说明 |
|------|------|------|------|
| PHONE 管理 | ✅ 已完成 | ⚠️ 前端 Mock | 全部接口为前端 Mock 实现 |
| IP 管理 | ✅ 已完成 | ⚠️ 前端 Mock | 含健康度算法 |
| 采购渠道管理 | ✅ 已完成 | ⚠️ 前端 Mock | 含质量统计 |
| WA 配置 | ✅ 已完成 | ⚠️ 前端 Mock | — |
| 后端 Java API | ❌ 未开发 | ❌ 未开发 | 需对接后端真实接口 |
| 与 Geelark 集成 | ❌ 未开发 | ❌ 未开发 | 自动激活、投养等需调用 Geelark API |

---

## 10. 后续待优化项

| 编号 | 优化项 | 优先级 | 说明 |
|------|--------|--------|------|
| TODO-01 | 停用按钮 | P2 | `disablePhone` API 已实现但 UI 未暴露，建议在"问题号"/"申诉失败"场景增加停用入口 |
| TODO-02 | 号码回收 | P1 | 催员离职/封号后需要从催员处回收号码的操作入口和流程 |
| TODO-03 | 投养质量评分 | P2 | 投养完成时计算质量评分并展示 |
| TODO-04 | IP 自动切换 | P2 | IP 异常时自动将其下号码迁移到健康 IP |
| TODO-05 | 操作日志审计 | P1 | 所有关键操作记录操作人和操作时间，支持审计追溯 |
| TODO-06 | 数据导出 | P2 | 号码列表、IP 列表支持 Excel 导出 |
| TODO-07 | 告警通知 | P1 | IP 健康度低于阈值、封号率超标时自动告警 |
