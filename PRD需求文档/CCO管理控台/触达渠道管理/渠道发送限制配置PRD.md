### 变更记录
| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.0.0 | 2025-12-10 | 初始版本 | 大象 |

---

# 一、产品需求（Product Requirements）

## 1. 项目背景与目标（Background & Goals）

### 1.1 业务背景
在催收业务中，需要通过多种渠道（短信、电话、WhatsApp等）触达债务人及其联系人。为避免过度催收、降低投诉风险、优化渠道成本，需要对各渠道的发送频率和数量进行精细化控制。

### 1.2 业务痛点
- **缺乏统一的发送限制机制**：不同渠道的发送行为无法统一管控，容易造成过度触达
- **无法按队列差异化配置**：不同催收队列的业务策略不同，需要灵活配置各自的限制规则
- **合规风险高**：缺乏发送频率控制可能引发客户投诉和监管风险
- **渠道成本难以控制**：无限制发送导致渠道费用支出不可控

### 1.3 核心目标
- 支持按**渠道类型**和**催收队列**维度配置发送限制
- 提供**每日每案件限制**、**每日每联系人限制**、**发送时间间隔**三种限制维度
- 支持**灵活开关**，可针对不同场景启用或禁用限制
- 降低**客户投诉率**和**合规风险**，优化**渠道成本**

---

## 2. 业务场景与用户画像（Business Scenario & User）

### 2.1 用户角色
| 角色 | 职责 | 核心诉求 |
|------|------|----------|
| 超级管理员 | 跨甲方的系统配置管理 | 为各甲方配置合理的渠道发送限制策略 |
| 甲方管理员 | 本甲方的业务运营配置 | 根据业务需求调整各队列的发送限制 |
| 催收主管 | 催收队列管理与策略制定 | 确保渠道发送符合业务策略和合规要求 |

### 2.2 典型场景
#### 场景1：新甲方上线时配置发送限制
- **触发时机**：新甲方接入系统，完成渠道供应商配置后
- **操作流程**：
  1. 进入「渠道发送限制配置」页面
  2. 查看自动生成的渠道-队列配置列表（基于已配置的渠道供应商）
  3. 为不同队列设置差异化的发送限制
  4. 保存配置并启用

#### 场景2：调整队列发送策略
- **触发时机**：业务策略调整，需要提高或降低某队列的触达频率
- **操作流程**：
  1. 通过筛选器定位到目标渠道和队列
  2. 调整每日限制数量或发送间隔
  3. 保存修改，实时生效

#### 场景3：应对投诉暂时关闭某渠道限制
- **触发时机**：收到客户投诉，需要临时收紧发送策略
- **操作流程**：
  1. 快速定位到投诉相关的渠道-队列配置
  2. 降低限制数量或延长发送间隔
  3. 或直接通过状态开关禁用该配置

---

## 3. 关键业务流程（Business Flow）

### 3.1 配置初始化流程
```
[选择甲方] 
  ↓
[加载该甲方的催收队列]
  ↓
[加载已配置的渠道类型]（基于"甲方渠道管理"）
  ↓
[自动生成渠道-队列组合配置]
  ↓
[展示配置列表，默认全部"不限制"]
```

### 3.2 配置修改流程
```
[用户筛选渠道/队列]
  ↓
[定位到目标配置行]
  ↓
[修改限制参数]（每日每案件/每日每联系人/发送间隔）
  ↓
[点击"保存"按钮]
  ↓
[校验参数合法性]
  ↓
[调用后端API保存]
  ↓
[更新本地数据，按钮变灰]
  ↓
[提示保存成功]
```

### 3.3 发送限制生效流程（系统侧）
```
[催收任务触发发送请求]
  ↓
[查询对应渠道-队列的限制配置]
  ↓
[检查状态是否启用] → 未启用 → [放行]
  ↓ 已启用
[检查每日每案件限制] → 超限 → [拒绝发送]
  ↓ 未超限
[检查每日每联系人限制] → 超限 → [拒绝发送]
  ↓ 未超限
[检查发送时间间隔] → 未到间隔 → [拒绝发送]
  ↓ 已到间隔
[允许发送]
  ↓
[记录发送日志，更新计数器]
```

---

## 4. 业务规则与边界（Business Rules & Scope）

### 4.1 渠道范围
配置范围基于**"甲方渠道管理"**中已配置的一级渠道大类：
- 短信（SMS）
- 电话外呼（Call）
- RCS
- WABA
- WhatsApp
- 邮件（Email）
- 手机日历（Mobile Calendar）

**重要规则**：
- 只有在"甲方渠道管理"中配置了供应商的渠道大类，才会在限制配置中显示
- 如果某个大类下未配置任何供应商，则该大类不出现在配置列表中

### 4.2 队列范围
- 配置基于该甲方下所有**已激活的催收队列**
- 每个队列与每个渠道类型形成一个独立的配置项

### 4.3 限制维度与规则

#### 4.3.1 每日每案件限制数量
- **定义**：同一案件在当前渠道每天最多可发送的次数
- **默认值**：不限制（null）
- **可设置范围**：0 - 100000
- **步长**：10
- **计数周期**：自然日（00:00 - 23:59）
- **计数维度**：案件ID + 渠道类型 + 队列

#### 4.3.2 每日每联系人限制数量
- **定义**：同一联系人在当前渠道每天最多可接收的次数
- **默认值**：不限制（null）
- **可设置范围**：0 - 100000
- **步长**：10
- **计数周期**：自然日（00:00 - 23:59）
- **计数维度**：联系人手机号/邮箱/WhatsApp ID + 渠道类型 + 队列

#### 4.3.3 发送时间间隔（秒）
- **定义**：同一联系人在当前渠道两次发送之间的最小时间间隔
- **默认值**：不限制（null）
- **可设置范围**：0 - 86400（0秒 - 24小时）
- **步长**：10秒
- **计数维度**：联系人手机号/邮箱/WhatsApp ID + 渠道类型 + 队列

#### 4.3.4 状态开关
- **定义**：该配置是否启用
- **默认值**：启用（true）
- **规则**：
  - 启用时：发送前强制检查限制规则
  - 禁用时：跳过限制检查，直接发送

### 4.4 配置优先级
当多个限制条件同时存在时，按以下顺序检查：
1. 状态开关（关闭则不检查其他条件）
2. 每日每案件限制
3. 每日每联系人限制
4. 发送时间间隔

**任一限制未通过，则拒绝发送**

### 4.5 本次需求范围内
✅ 支持配置7种一级渠道的发送限制  
✅ 支持按队列维度配置  
✅ 支持三种限制维度（每日每案件、每日每联系人、发送间隔）  
✅ 支持状态开关  
✅ 支持筛选和快速定位  
✅ 支持单行保存  

### 4.6 本次需求范围外
❌ 不支持按催收阶段配置（M1、M2等）  
❌ 不支持按案件金额区间配置  
❌ 不支持全局批量修改  
❌ 不支持配置模板复制  
❌ 不支持历史配置版本管理  
❌ 不支持发送限制的实时监控大盘  

---

## 5. 合规与风控要求（Compliance & Risk Control）

### 5.1 合规要求
| 合规项 | 要求说明 | 实现方式 |
|--------|----------|----------|
| 过度催收防范 | 避免短时间内高频触达客户 | 通过发送间隔和每日限制实现 |
| 数据隐私保护 | 配置数据仅限授权用户访问 | 基于角色权限控制（SuperAdmin/TenantAdmin） |
| 操作日志记录 | 所有配置变更需记录操作人和时间 | 后端自动记录修改日志 |
| 审计追溯 | 支持查询历史配置变更记录 | 预留审计日志接口（范围外） |

### 5.2 风控策略
- **默认"不限制"策略**：新配置默认不限制，由管理员根据业务需求主动设置
- **最小间隔建议**：建议发送时间间隔不低于600秒（10分钟），避免过度打扰
- **异常监控**：后端需监控触发限制拦截的频率，异常时告警

---

## 6. 资金路径与结算规则（Funding Flow & Settlement）
本需求不涉及资金流转，此节跳过。

---

## 7. 数据字段与口径（Data Definition）

### 7.1 核心字段定义
| 字段名 | 中文名 | 数据类型 | 说明 | 来源系统 |
|--------|--------|----------|------|----------|
| id | 配置ID | Integer | 配置记录的唯一标识 | 自增 |
| tenant_id | 甲方ID | Integer | 所属甲方 | 甲方管理系统 |
| channel | 渠道类型 | String | 触达渠道（sms/call/rcs/waba/whatsapp/email/mobile_calendar） | 枚举 |
| queue_id | 队列ID | Integer | 催收队列ID | 队列管理系统 |
| queue_code | 队列编码 | String | 队列唯一编码 | 队列管理系统 |
| queue_name | 队列名称 | String | 队列显示名称 | 队列管理系统 |
| daily_limit_per_case | 每日每案件限制数量 | Integer | null表示不限制 | 配置 |
| daily_limit_per_case_unlimited | 每日每案件不限制标识 | Boolean | true表示不限制 | 配置 |
| daily_limit_per_contact | 每日每联系人限制数量 | Integer | null表示不限制 | 配置 |
| daily_limit_per_contact_unlimited | 每日每联系人不限制标识 | Boolean | true表示不限制 | 配置 |
| send_interval | 发送时间间隔（秒） | Integer | null表示不限制 | 配置 |
| send_interval_unlimited | 发送间隔不限制标识 | Boolean | true表示不限制 | 配置 |
| enabled | 启用状态 | Boolean | true=启用，false=禁用 | 配置 |
| created_at | 创建时间 | Datetime | 配置创建时间 | 系统 |
| updated_at | 更新时间 | Datetime | 配置最后修改时间 | 系统 |

### 7.2 统计口径
- **每日计数重置时间**：每天00:00（服务器时区）
- **间隔时间计算**：基于联系人最后一次成功发送的时间戳
- **案件维度**：基于案件的唯一ID（case_id）
- **联系人维度**：基于联系人的唯一标识（手机号/邮箱/WhatsApp ID）

---

## 8. 交互与信息展示（UX & UI Brief）

### 8.1 页面结构
```
┌─────────────────────────────────────────────┐
│ 渠道发送限制配置                             │
├─────────────────────────────────────────────┤
│ [渠道: 全部▼] [队列: 全部▼] [重置]          │
├─────────────────────────────────────────────┤
│ 渠道 │ 队列 │ 每日每案件 │ 每日每联系人 │ ... │
│ 短信 │ M1  │ [100]☑不限 │ [50]☑不限    │     │
│ 短信 │ M2  │ [200]☐不限 │ [100]☐不限   │     │
│ 电话 │ M1  │ [10]☑不限  │ [5]☑不限     │     │
│ ...  │ ... │ ...        │ ...          │     │
└─────────────────────────────────────────────┘
```

### 8.2 交互说明
- **筛选器**：支持按渠道和队列快速筛选
- **数值输入**：支持手动输入或使用+/-按钮调整
- **不限制复选框**：勾选后，对应输入框禁用并清空
- **保存按钮状态**：
  - 有修改时：蓝色可点击
  - 无修改时：灰色禁用
- **标签颜色**：不同渠道使用不同颜色标签区分

---

## 9. 配置项与运营开关（Config & Operation Switches）

### 9.1 可配置参数
| 配置项 | 默认值 | 调整频率 | 配置入口 | 变更流程 |
|--------|--------|----------|----------|----------|
| 每日每案件限制数量 | 不限制 | 按需 | 配置页面 | 管理员直接修改 |
| 每日每联系人限制数量 | 不限制 | 按需 | 配置页面 | 管理员直接修改 |
| 发送时间间隔 | 不限制 | 按需 | 配置页面 | 管理员直接修改 |
| 启用状态 | 启用 | 按需 | 配置页面 | 管理员直接修改 |

### 9.2 灰度策略
- **首次上线**：默认所有配置为"不限制"，不影响现有发送逻辑
- **逐步启用**：建议先在测试队列启用，观察效果后再推广

---

# 二、数据需求（Data Requirements）

## 1. 埋点需求（Tracking Requirements）

| 触发时机 | 埋点说明 | 埋点ID | 关键属性 |
|----------|----------|--------|----------|
| 进入页面 | 渠道发送限制配置页面访问 | `channel_limit_config_view` | user_id, tenant_id |
| 保存配置 | 保存单行配置 | `channel_limit_config_save` | user_id, tenant_id, channel, queue_id, config_id |
| 筛选操作 | 使用筛选器 | `channel_limit_filter_use` | user_id, tenant_id, filter_type, filter_value |
| 切换状态 | 启用/禁用配置 | `channel_limit_toggle_status` | user_id, tenant_id, channel, queue_id, enabled |
| 勾选不限制 | 勾选/取消不限制复选框 | `channel_limit_toggle_unlimited` | user_id, tenant_id, channel, queue_id, field_name, unlimited |

---

# 三、技术部分描述（Technical Requirements / TRD）

## 1. 系统架构与模块划分（System Architecture & Modules）

### 1.1 系统架构图
```
┌─────────────┐
│  前端页面   │ (Vue3 + Element Plus)
└──────┬──────┘
       │ HTTP/REST
┌──────▼──────┐
│  业务中台   │ (Spring Boot + Java 17)
│ ChannelLimit│
│   Service   │
└──────┬──────┘
       │
┌──────▼──────┐
│   MySQL     │ (channel_limit_configs表)
└─────────────┘
```

### 1.2 模块划分
| 模块 | 职责 | 技术栈 |
|------|------|--------|
| 前端配置页面 | 渠道限制配置的展示与交互 | Vue3 + TypeScript + Element Plus |
| 后端配置服务 | 配置的CRUD操作 | Spring Boot + MyBatis-Plus |
| 限制校验服务 | 发送前的限制检查逻辑 | Spring Boot（范围外，预留接口） |
| 数据存储 | 配置数据持久化 | MySQL 8.0 |

---

## 2. 接口设计与系统依赖（API Design & Dependencies）

### 2.1 接口列表

#### 2.1.1 查询甲方的渠道限制配置
```
GET /api/v1/channel-limit-configs/tenants/{tenant_id}
```

**入参**：
- `tenant_id`（路径参数）：甲方ID

**出参**：
```json
[
  {
    "id": 1,
    "tenant_id": 10,
    "channel": "sms",
    "queue_id": 1,
    "queue_code": "M1",
    "queue_name": "M1队列",
    "daily_limit_per_case": 100,
    "daily_limit_per_case_unlimited": false,
    "daily_limit_per_contact": 50,
    "daily_limit_per_contact_unlimited": false,
    "send_interval": 600,
    "send_interval_unlimited": false,
    "enabled": true,
    "created_at": "2025-12-01T10:00:00",
    "updated_at": "2025-12-10T15:30:00"
  }
]
```

**超时时间**：3秒  
**重试策略**：不重试（查询操作）  
**降级逻辑**：返回空数组，前端自动生成默认配置

---

#### 2.1.2 创建渠道限制配置
```
POST /api/v1/channel-limit-configs
```

**入参**：
```json
{
  "tenant_id": 10,
  "channel": "sms",
  "queue_id": 1,
  "queue_code": "M1",
  "queue_name": "M1队列",
  "daily_limit_per_case": 100,
  "daily_limit_per_case_unlimited": false,
  "daily_limit_per_contact": 50,
  "daily_limit_per_contact_unlimited": false,
  "send_interval": 600,
  "send_interval_unlimited": false,
  "enabled": true
}
```

**出参**：
```json
{
  "id": 1,
  "tenant_id": 10,
  "channel": "sms",
  "queue_id": 1,
  "queue_code": "M1",
  "queue_name": "M1队列",
  "daily_limit_per_case": 100,
  "daily_limit_per_case_unlimited": false,
  "daily_limit_per_contact": 50,
  "daily_limit_per_contact_unlimited": false,
  "send_interval": 600,
  "send_interval_unlimited": false,
  "enabled": true,
  "created_at": "2025-12-10T15:30:00",
  "updated_at": "2025-12-10T15:30:00"
}
```

**超时时间**：5秒  
**重试策略**：客户端不自动重试，由用户手动重试  
**幂等要求**：基于 `tenant_id + channel + queue_id` 唯一约束，重复提交返回已存在记录  
**失败降级**：提示用户保存失败，不影响页面其他配置

---

#### 2.1.3 更新渠道限制配置
```
PUT /api/v1/channel-limit-configs/{id}
```

**入参**：
- `id`（路径参数）：配置ID
- 请求体同创建接口

**出参**：同创建接口

**超时时间**：5秒  
**重试策略**：客户端不自动重试  
**幂等要求**：同一ID多次PUT，最终结果一致  
**失败降级**：提示用户保存失败

---

### 2.2 依赖接口

#### 2.2.1 查询甲方队列列表
```
GET /api/v1/tenants/{tenant_id}/queues
```
**用途**：获取该甲方下的所有催收队列，用于生成配置列表

**出参示例**：
```json
[
  {
    "id": 1,
    "queue_code": "M1",
    "queue_name": "M1队列",
    "is_active": true
  }
]
```

---

## 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

### 3.1 数据表设计

#### 表名：`channel_limit_configs`
**用途**：存储渠道发送限制配置

**表结构**：
```sql
CREATE TABLE `channel_limit_configs` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` INT NOT NULL COMMENT '甲方ID',
  `channel` VARCHAR(50) NOT NULL COMMENT '渠道类型（sms/call/rcs/waba/whatsapp/email/mobile_calendar）',
  `queue_id` INT NOT NULL COMMENT '队列ID',
  `queue_code` VARCHAR(50) NOT NULL COMMENT '队列编码',
  `queue_name` VARCHAR(100) NOT NULL COMMENT '队列名称',
  `daily_limit_per_case` INT NULL COMMENT '每日每案件限制数量（null表示不限制）',
  `daily_limit_per_case_unlimited` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '每日每案件不限制标识',
  `daily_limit_per_contact` INT NULL COMMENT '每日每联系人限制数量（null表示不限制）',
  `daily_limit_per_contact_unlimited` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '每日每联系人不限制标识',
  `send_interval` INT NULL COMMENT '发送时间间隔（秒，null表示不限制）',
  `send_interval_unlimited` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '发送间隔不限制标识',
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '启用状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_channel_queue` (`tenant_id`, `channel`, `queue_id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_queue_id` (`queue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道发送限制配置表';
```

**索引说明**：
- **主键**：`id`
- **唯一键**：`uk_tenant_channel_queue` 确保同一甲方的同一渠道-队列组合只有一条配置
- **普通索引**：
  - `idx_tenant_id` 用于按甲方查询
  - `idx_queue_id` 用于按队列查询

### 3.2 模型依赖
本需求不依赖算法模型或评分卡。

---

## 4. 非功能性要求（Non-Functional Requirements）

### 4.1 性能要求
| 指标 | 目标值 | 说明 |
|------|--------|------|
| 页面加载时间 | < 2秒 | 包括获取队列和配置数据 |
| 配置保存响应时间 | < 1秒 | 单行保存操作 |
| 并发支持 | 10 QPS | 配置修改操作不频繁 |
| 数据库查询响应 | < 500ms | 基于索引优化 |

### 4.2 可用性要求
- **SLA**：99.5%（与整体系统一致）
- **降级策略**：
  - 查询失败时，前端使用缓存数据或默认配置
  - 保存失败时，提示用户稍后重试

### 4.3 安全要求
- **权限控制**：仅 SuperAdmin 和 TenantAdmin 可访问
- **数据加密**：敏感字段无需加密（配置数据非敏感）
- **接口鉴权**：基于 JWT Token 验证
- **操作审计**：记录所有配置变更日志

### 4.4 扩展性
- 支持未来新增渠道类型（通过枚举扩展）
- 支持未来新增限制维度（如按案件金额、催收阶段等）

---

## 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

### 5.1 关键日志
| 日志类型 | 记录内容 | 日志级别 |
|----------|----------|----------|
| 配置查询 | tenant_id, user_id, 查询耗时 | INFO |
| 配置创建 | tenant_id, user_id, channel, queue_id, 配置内容 | INFO |
| 配置更新 | config_id, user_id, 变更前后值 | INFO |
| 查询失败 | tenant_id, 错误信息, 堆栈 | ERROR |
| 保存失败 | config_id, 错误信息, 堆栈 | ERROR |

### 5.2 监控指标
| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| API成功率 | 配置API调用成功率 | < 95% |
| API响应时间 | P95响应时间 | > 2秒 |
| 数据库慢查询 | 查询耗时 > 1秒的SQL | 出现即告警 |
| 配置修改频率 | 每小时配置修改次数 | > 100次/小时（异常） |

### 5.3 告警规则
- **API成功率 < 95%**：发送钉钉通知给开发团队
- **响应时间 > 2秒**：发送钉钉通知
- **数据库慢查询**：记录日志并发送邮件
- **配置修改频率异常**：发送钉钉通知给运维团队

---

## 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

### 6.1 测试类型
| 测试类型 | 覆盖范围 | 负责人 |
|----------|----------|--------|
| 单元测试 | Service层业务逻辑 | 后端开发 |
| 接口测试 | API接口正确性 | 后端开发 + QA |
| 前端UI测试 | 页面交互、数据展示 | 前端开发 + QA |
| 集成测试 | 前后端联调 | QA |
| 性能测试 | 并发查询、保存操作 | QA |

### 6.2 测试用例（关键场景）
| 用例ID | 测试场景 | 预期结果 |
|--------|----------|----------|
| TC-001 | 新甲方首次进入页面 | 自动生成所有渠道-队列组合，默认不限制 |
| TC-002 | 修改配置后点击保存 | 保存成功，按钮变灰，提示成功 |
| TC-003 | 勾选"不限制"后取消勾选 | 输入框恢复可编辑，值为空 |
| TC-004 | 筛选渠道为"短信" | 只显示短信渠道的配置行 |
| TC-005 | 重置筛选器 | 恢复显示所有配置 |
| TC-006 | 未选择甲方进入页面 | 提示"请选择甲方" |
| TC-007 | 输入非法值（负数） | 输入框自动校正为最小值0 |
| TC-008 | 同时修改多行但只保存一行 | 只保存该行，其他行修改保留但未保存 |

### 6.3 验收标准
✅ 所有渠道-队列组合在页面正确展示  
✅ 配置修改后保存成功，刷新页面数据一致  
✅ "不限制"复选框与数值输入联动正常  
✅ 筛选器功能正常，可快速定位配置  
✅ 单行保存按钮状态（灰色/蓝色）正确  
✅ API响应时间符合性能要求  
✅ 无安全漏洞（SQL注入、XSS等）  

---

## 7. 发布计划与回滚预案（Release Plan & Rollback）

### 7.1 发布策略
| 阶段 | 内容 | 时间 | 负责人 |
|------|------|------|--------|
| 数据库变更 | 创建 `channel_limit_configs` 表 | 发布前1天 | DBA |
| 后端发布 | 部署配置API服务 | 发布当天10:00 | 后端负责人 |
| 前端发布 | 部署配置页面 | 发布当天11:00 | 前端负责人 |
| 灰度验证 | 在测试甲方验证功能 | 发布当天12:00 | QA |
| 全量开放 | 开放所有甲方使用 | 发布当天14:00 | 产品负责人 |

### 7.2 配置开关
| 开关名 | 用途 | 默认值 |
|--------|------|--------|
| `channel_limit_feature_enabled` | 渠道限制功能总开关 | false（发布后手动开启） |
| `channel_limit_check_enabled` | 发送前限制检查开关 | false（本期仅配置，不检查） |

### 7.3 回滚预案
| 问题级别 | 回滚方案 | 执行人 |
|----------|----------|--------|
| P0（页面崩溃） | 前端回滚到上一版本，隐藏菜单入口 | 前端负责人 |
| P1（保存失败） | 后端回滚到上一版本 | 后端负责人 |
| P2（性能问题） | 数据库添加索引，优化慢查询 | DBA + 后端负责人 |

### 7.4 应急联系人
| 角色 | 姓名 | 联系方式 |
|------|------|----------|
| 产品负责人 | 大象 | 138xxxx1234 |
| 后端负责人 | TBD | TBD |
| 前端负责人 | TBD | TBD |
| DBA | TBD | TBD |

---

## 附录

### A. 渠道类型枚举
```typescript
type ChannelType = 
  | 'sms'              // 短信
  | 'call'             // 电话外呼
  | 'rcs'              // RCS
  | 'waba'             // WABA
  | 'whatsapp'         // WhatsApp
  | 'email'            // 邮件
  | 'mobile_calendar'  // 手机日历
```

### B. 前端配置页面路由
```
/channel-config/limit
```

### C. 权限配置
```json
{
  "permission_code": "channel_limit_config",
  "permission_name": "渠道发送限制配置",
  "parent_permission": "channel_management",
  "allowed_roles": ["SuperAdmin", "TenantAdmin"]
}
```

---

**文档结束**
