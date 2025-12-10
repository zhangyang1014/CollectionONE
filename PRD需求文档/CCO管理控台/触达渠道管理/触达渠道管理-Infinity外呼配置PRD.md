### 变更记录
| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| V1.0 | 2025-12-09 | 初始版本创建 | 大象 |

---

# 触达渠道管理 - Infinity外呼配置 PRD

---

# 一、产品需求（Product Requirements）

## 1. 项目背景与目标（Background & Goals）

随着催收业务的规模化发展，外呼渠道作为重要的客户触达方式，需要统一的配置管理平台。目前 Infinity 外呼系统已接入，但缺乏集中化的配置管理界面，导致外呼参数调整依赖人工修改配置文件，效率低下且容易出错。

本需求旨在为 CCO 管理控台新增 Infinity 外呼配置模块，实现外呼渠道的可视化配置管理，包括外呼账号配置、呼叫参数设置、发送限制规则等，预期提升运营配置效率 80%，减少因配置错误导致的外呼失败率 50%。

## 2. 业务场景与用户画像（Business Scenario & User）

**使用场景：**
- 入口：CCO 管理控台 → 账号管理 → 触达渠道管理 → Infinity外呼配置
- 触发时机：运营人员需要新增/修改外呼渠道配置时
- 所在页面：渠道配置管理页面

**用户画像：**
- 主要用户：催收运营人员、系统管理员
- 核心诉求：
  - 快速配置和调整 Infinity 外呼参数
  - 设置外呼发送限制规则，避免过度骚扰用户
  - 监控外呼渠道状态，及时发现和处理异常
  - 支持多账号管理，实现负载均衡

## 3. 关键业务流程（Business Flow）

```
开始
  ↓
进入触达渠道管理
  ↓
选择 Infinity外呼配置
  ↓
配置管理操作 ────┬──→ 新增外呼账号
                 ├──→ 编辑外呼参数
                 ├──→ 设置发送限制
                 ├──→ 启用/禁用账号
                 └──→ 查看配置历史
  ↓
保存配置
  ↓
系统校验 ──→ 校验失败 ──→ 提示错误信息 ──→ 返回修改
  ↓
校验通过
  ↓
配置生效
  ↓
记录操作日志
  ↓
结束
```

**关键步骤说明：**
1. 用户登录 CCO 控台，进入"账号管理 > 触达渠道管理"
2. 选择"Infinity外呼配置"菜单
3. 查看当前已配置的外呼账号列表
4. 执行配置操作（新增/编辑/启用/禁用）
5. 填写配置参数（账号信息、呼叫参数、限制规则等）
6. 系统校验配置有效性
7. 保存并使配置生效
8. 记录操作日志供审计

## 4. 业务规则与边界（Business Rules & Scope）

**核心业务规则：**

1. **账号管理规则：**
   - 支持多个 Infinity 外呼账号配置
   - 每个账号需配置唯一的账号标识
   - 账号状态包括：启用、禁用、测试中
   - 支持账号优先级设置，用于负载均衡

2. **发送限制规则：**
   - 单用户单日最大外呼次数限制
   - 外呼时间段限制（例如：9:00-20:00）
   - 外呼间隔时间限制（例如：同一用户两次外呼间隔至少2小时）
   - 支持按案件逾期天数设置不同的外呼频率

3. **呼叫参数配置：**
   - 外呼超时时长设置
   - 最大重拨次数
   - 语音文件配置
   - 回调 URL 配置

4. **权限控制：**
   - 仅管理员和授权的运营人员可访问
   - 配置变更需要二次确认
   - 关键操作需记录操作日志

**范围界定：**

**范围内：**
- Infinity 外呼账号的新增、编辑、删除、启用/禁用
- 外呼发送限制规则的配置
- 外呼呼叫参数的设置
- 配置历史记录查询
- 账号状态监控

**范围外：**
- 实时外呼执行监控（属于催收管理模块）
- 外呼话术内容管理（属于内容管理模块）
- 外呼数据统计分析（属于数据报表模块）
- 第三方外呼平台对接开发

## 5. 合规与风控要求（Compliance & Risk Control）

**合规要求：**

1. **通信合规：**
   - 严格遵守《电信条例》和《骚扰电话整治方案》
   - 外呼时间段必须在 8:00-21:00 之间
   - 支持用户设置"拒绝外呼"白名单
   - 外呼频率需符合监管要求（单日不超过3次）

2. **数据隐私：**
   - 敏感配置信息（如 API Key、密钥）需加密存储
   - 配置信息访问需脱敏展示
   - 操作日志需记录完整的操作轨迹

3. **权限管理：**
   - 支持基于角色的权限控制（RBAC）
   - 关键配置变更需审批流程
   - 支持配置变更回滚

**风控策略：**

1. **异常监控：**
   - 监控外呼成功率，低于阈值触发告警
   - 监控账号可用余额，不足时及时通知
   - 监控外呼频率异常，防止过度骚扰

2. **降级策略：**
   - 主账号不可用时自动切换备用账号
   - 支持手动关闭外呼功能
   - 支持按案件类型分配不同外呼账号

## 6. 资金路径与结算规则（Funding Flow & Settlement）

本需求不涉及资金流转，本节不适用。

## 7. 数据字段与口径（Data Definition）

**核心数据字段：**

| 字段名 | 字段说明 | 数据类型 | 来源系统 | 更新频率 |
|--------|----------|----------|----------|----------|
| channel_id | 渠道ID（主键） | VARCHAR(32) | CCO控台 | 新增时生成 |
| channel_name | 渠道名称 | VARCHAR(100) | 用户输入 | 按需更新 |
| account_id | Infinity账号标识 | VARCHAR(50) | 用户输入 | 按需更新 |
| api_key | API密钥 | VARCHAR(200) | 用户输入 | 按需更新 |
| api_secret | API密钥密文 | VARCHAR(200) | 用户输入 | 按需更新 |
| callback_url | 回调地址 | VARCHAR(500) | 用户输入 | 按需更新 |
| status | 渠道状态 | TINYINT | 系统设置 | 实时更新 |
| priority | 优先级 | INT | 用户输入 | 按需更新 |
| daily_limit | 单日最大外呼次数 | INT | 用户输入 | 按需更新 |
| user_daily_limit | 单用户单日最大次数 | INT | 用户输入 | 按需更新 |
| call_timeout | 外呼超时时长（秒） | INT | 用户输入 | 按需更新 |
| max_retry | 最大重拨次数 | INT | 用户输入 | 按需更新 |
| time_range_start | 外呼时间段-开始 | TIME | 用户输入 | 按需更新 |
| time_range_end | 外呼时间段-结束 | TIME | 用户输入 | 按需更新 |
| interval_minutes | 外呼间隔（分钟） | INT | 用户输入 | 按需更新 |
| created_by | 创建人 | VARCHAR(50) | 系统记录 | 创建时生成 |
| created_at | 创建时间 | DATETIME | 系统记录 | 创建时生成 |
| updated_by | 更新人 | VARCHAR(50) | 系统记录 | 更新时生成 |
| updated_at | 更新时间 | DATETIME | 系统记录 | 更新时生成 |

**字段口径说明：**
- `status` 状态值：0-禁用，1-启用，2-测试中
- `priority` 优先级：数值越小优先级越高
- `daily_limit`：0 表示不限制
- 时间字段统一使用系统当前时间，无延迟

## 8. 交互与信息展示（UX & UI Brief）

**页面布局：**

1. **列表页面：**
   - 显示所有已配置的 Infinity 外呼账号
   - 支持按渠道名称、状态筛选
   - 操作按钮：新增、编辑、启用/禁用、删除
   - 显示字段：渠道名称、账号标识、状态、优先级、单日限制、创建时间、操作

2. **配置表单：**
   - 基本信息区：渠道名称、账号标识、API密钥、回调地址
   - 呼叫参数区：超时时长、最大重拨次数、优先级
   - 发送限制区：单日限制、单用户限制、外呼时间段、外呼间隔
   - 操作按钮：保存、取消

**交互说明：**
- API密钥默认脱敏显示，点击"显示"按钮后展示完整内容
- 配置变更需二次确认弹窗
- 表单校验提示友好的错误信息
- 操作成功后显示成功提示并刷新列表

**文案要求：**
- 页面标题：Infinity外呼配置
- 新增按钮：+ 新增外呼账号
- 状态标签：启用（绿色）、禁用（灰色）、测试中（橙色）
- 确认删除弹窗：确定要删除该外呼账号吗？删除后不可恢复。

## 9. 配置项与运营开关（Config & Operation Switches）

**可配置参数：**

| 配置项 | 配置说明 | 默认值 | 可选值/范围 | 配置入口 |
|--------|----------|--------|------------|----------|
| 外呼时间段 | 允许外呼的时间范围 | 9:00-20:00 | 8:00-21:00 | 配置表单 |
| 单日最大外呼次数 | 单个渠道单日最大外呼总数 | 10000 | 0-100000 | 配置表单 |
| 单用户单日限制 | 同一用户单日最大接听次数 | 3 | 1-10 | 配置表单 |
| 外呼间隔 | 同一用户两次外呼最小间隔 | 120分钟 | 30-480分钟 | 配置表单 |
| 超时时长 | 单次外呼最大等待时长 | 60秒 | 30-180秒 | 配置表单 |
| 最大重拨次数 | 外呼失败最大重试次数 | 2 | 0-5 | 配置表单 |

**运营开关：**
- 全局外呼开关：支持一键关闭所有外呼渠道
- 渠道级开关：支持单独启用/禁用某个外呼账号
- 测试模式开关：测试模式下仅发送到测试号码

**变更流程：**
1. 运营人员提交配置变更申请
2. 系统自动校验配置有效性
3. 配置立即生效（无需审批）
4. 记录操作日志
5. 关键配置变更需通知相关人员

**灰度策略：**
- 新增外呼账号可先设置为"测试中"状态进行验证
- 支持按案件比例分配到不同外呼账号
- 支持按案件标签（AB Test）使用不同配置

---

# 二、数据需求（Data Requirements）

## 1. 埋点需求（Tracking Requirements）

| 触发时机 | 埋点说明 | 埋点ID | 关键属性 |
|---------|---------|--------|----------|
| 进入配置页面 | 用户进入Infinity外呼配置页面 | infinity_config_page_view | user_id, timestamp |
| 点击新增按钮 | 用户点击新增外呼账号按钮 | infinity_config_add_click | user_id, timestamp |
| 提交配置表单 | 用户提交外呼配置信息 | infinity_config_submit | user_id, channel_id, operation_type, timestamp |
| 配置保存成功 | 外呼配置保存成功 | infinity_config_save_success | user_id, channel_id, config_data, timestamp |
| 配置保存失败 | 外呼配置保存失败 | infinity_config_save_failed | user_id, channel_id, error_code, error_msg, timestamp |
| 启用/禁用账号 | 用户启用或禁用外呼账号 | infinity_config_status_change | user_id, channel_id, old_status, new_status, timestamp |
| 删除配置 | 用户删除外呼配置 | infinity_config_delete | user_id, channel_id, timestamp |
| 配置校验失败 | 表单校验未通过 | infinity_config_validate_failed | user_id, field_name, error_msg, timestamp |

**埋点数据用途：**
- 监控配置使用频率
- 分析配置错误类型，优化表单校验
- 统计外呼账号使用情况
- 为产品迭代提供数据支持

---

# 三、技术部分描述（Technical Requirements / TRD）

## 1. 系统架构与模块划分（System Architecture & Modules）

**架构图：**

```
┌─────────────────────────────────────────────────────┐
│                   CCO 管理控台前端                    │
│                  (Vue3 + Element Plus)               │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/HTTPS
                   ↓
┌─────────────────────────────────────────────────────┐
│              CCO 控台后端 (Java)                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  触达渠道管理模块                              │  │
│  │  - InfinityConfigController                  │  │
│  │  - InfinityConfigService                     │  │
│  │  - InfinityConfigMapper                      │  │
│  └──────────────────────────────────────────────┘  │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│                   MySQL 数据库                       │
│  - channel_config 表（渠道配置主表）                  │
│  - channel_config_log 表（配置变更日志表）            │
└─────────────────────────────────────────────────────┘
```

**模块职责：**

1. **前端模块（frontend/src/views/channel/）：**
   - InfinityConfig.vue：外呼配置主页面
   - InfinityConfigForm.vue：配置表单组件
   - 提供用户交互界面和表单校验

2. **后端模块（backend-java/src/main/java/com/cco/channel/）：**
   - InfinityConfigController：处理前端请求
   - InfinityConfigService：业务逻辑处理
   - InfinityConfigMapper：数据库操作
   - 提供配置的增删改查接口

3. **数据库模块：**
   - channel_config：存储外呼配置信息
   - channel_config_log：记录配置变更历史

## 2. 接口设计与系统依赖（API Design & Dependencies）

**后端 API 接口：**

### 2.1 获取外呼配置列表

**接口路径：** `GET /api/v1/channel/infinity/list`

**请求参数：**
```json
{
  "channel_name": "string, optional, 渠道名称模糊查询",
  "status": "integer, optional, 状态筛选",
  "page": "integer, 页码，默认1",
  "page_size": "integer, 每页条数，默认20"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "channel_id": "ch_001",
        "channel_name": "Infinity外呼账号1",
        "account_id": "infinity_001",
        "status": 1,
        "priority": 1,
        "daily_limit": 10000,
        "user_daily_limit": 3,
        "created_at": "2025-12-01 10:00:00",
        "updated_at": "2025-12-09 15:30:00"
      }
    ]
  }
}
```

### 2.2 获取外呼配置详情

**接口路径：** `GET /api/v1/channel/infinity/{channel_id}`

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "channel_id": "ch_001",
    "channel_name": "Infinity外呼账号1",
    "account_id": "infinity_001",
    "api_key": "ak_***************",
    "callback_url": "https://api.example.com/callback",
    "status": 1,
    "priority": 1,
    "daily_limit": 10000,
    "user_daily_limit": 3,
    "call_timeout": 60,
    "max_retry": 2,
    "time_range_start": "09:00:00",
    "time_range_end": "20:00:00",
    "interval_minutes": 120,
    "created_by": "admin",
    "created_at": "2025-12-01 10:00:00",
    "updated_by": "operator",
    "updated_at": "2025-12-09 15:30:00"
  }
}
```

### 2.3 新增外呼配置

**接口路径：** `POST /api/v1/channel/infinity`

**请求参数：**
```json
{
  "channel_name": "Infinity外呼账号1",
  "account_id": "infinity_001",
  "api_key": "ak_xxxxxxxxxxx",
  "api_secret": "as_xxxxxxxxxxx",
  "callback_url": "https://api.example.com/callback",
  "priority": 1,
  "daily_limit": 10000,
  "user_daily_limit": 3,
  "call_timeout": 60,
  "max_retry": 2,
  "time_range_start": "09:00:00",
  "time_range_end": "20:00:00",
  "interval_minutes": 120
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "配置创建成功",
  "data": {
    "channel_id": "ch_002"
  }
}
```

### 2.4 更新外呼配置

**接口路径：** `PUT /api/v1/channel/infinity/{channel_id}`

**请求参数：** 同新增接口

**响应示例：**
```json
{
  "code": 200,
  "message": "配置更新成功",
  "data": null
}
```

### 2.5 删除外呼配置

**接口路径：** `DELETE /api/v1/channel/infinity/{channel_id}`

**响应示例：**
```json
{
  "code": 200,
  "message": "配置删除成功",
  "data": null
}
```

### 2.6 启用/禁用外呼配置

**接口路径：** `PUT /api/v1/channel/infinity/{channel_id}/status`

**请求参数：**
```json
{
  "status": 1  // 0-禁用，1-启用，2-测试中
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": null
}
```

**接口通用规范：**
- 超时时长：5秒
- 重试策略：失败后不重试（配置变更操作需保证幂等性）
- 幂等性要求：所有接口支持幂等操作
- 失败降级：接口失败返回友好错误提示，不影响系统其他功能

**系统依赖：**
- 依赖 CCO 控台用户权限系统（获取当前操作用户信息）
- 依赖 CCO 控台日志系统（记录操作日志）

## 3. 数据存储与模型依赖（Data Storage & Model Dependencies）

### 3.1 数据表结构

**表名：channel_config（渠道配置表）**

```sql
CREATE TABLE `channel_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '渠道ID',
  `channel_type` varchar(20) NOT NULL DEFAULT 'infinity' COMMENT '渠道类型',
  `channel_name` varchar(100) NOT NULL COMMENT '渠道名称',
  `account_id` varchar(50) NOT NULL COMMENT 'Infinity账号标识',
  `api_key` varchar(200) NOT NULL COMMENT 'API密钥',
  `api_secret` varchar(200) NOT NULL COMMENT 'API密钥密文',
  `callback_url` varchar(500) DEFAULT NULL COMMENT '回调地址',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-禁用，1-启用，2-测试中',
  `priority` int(11) NOT NULL DEFAULT '100' COMMENT '优先级，数值越小优先级越高',
  `daily_limit` int(11) NOT NULL DEFAULT '0' COMMENT '单日最大外呼次数，0表示不限制',
  `user_daily_limit` int(11) NOT NULL DEFAULT '3' COMMENT '单用户单日最大接听次数',
  `call_timeout` int(11) NOT NULL DEFAULT '60' COMMENT '外呼超时时长（秒）',
  `max_retry` int(11) NOT NULL DEFAULT '2' COMMENT '最大重拨次数',
  `time_range_start` time NOT NULL DEFAULT '09:00:00' COMMENT '外呼时间段-开始',
  `time_range_end` time NOT NULL DEFAULT '20:00:00' COMMENT '外呼时间段-结束',
  `interval_minutes` int(11) NOT NULL DEFAULT '120' COMMENT '外呼间隔（分钟）',
  `created_by` varchar(50) NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_id` (`channel_id`),
  KEY `idx_status` (`status`),
  KEY `idx_channel_type` (`channel_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道配置表';
```

**表名：channel_config_log（配置变更日志表）**

```sql
CREATE TABLE `channel_config_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '渠道ID',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：create/update/delete/status_change',
  `before_data` text COMMENT '变更前数据（JSON格式）',
  `after_data` text COMMENT '变更后数据（JSON格式）',
  `operator` varchar(50) NOT NULL COMMENT '操作人',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_channel_id` (`channel_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置变更日志表';
```

### 3.2 索引说明

- `uk_channel_id`：唯一索引，保证渠道ID唯一性
- `idx_status`：状态索引，用于按状态筛选查询
- `idx_channel_type`：渠道类型索引，用于多渠道类型扩展
- `idx_operation_time`：操作时间索引，用于日志查询

### 3.3 模型依赖

本需求不依赖算法模型或评分卡。

## 4. 非功能性要求（Non-Functional Requirements）

**性能要求：**
- QPS：峰值 100 QPS（配置变更操作频率较低）
- 响应时间：接口平均响应时间 < 500ms，P99 < 1s
- 峰值容量：支持同时配置 100 个外呼渠道

**可用性要求：**
- SLA：99.9%
- 降级策略：配置服务不可用时，使用缓存配置继续外呼
- 容错机制：单个配置异常不影响其他配置正常使用

**安全要求：**
- 加密存储：API密钥字段需加密存储（AES-256）
- 接口鉴权：所有接口需验证用户登录态和权限
- 脱敏展示：API密钥默认脱敏显示，仅显示前6位和后4位
- 操作审计：所有配置变更需记录操作日志

**扩展性要求：**
- 支持未来接入其他外呼平台（如腾讯云呼叫中心、阿里云呼叫中心）
- 配置表结构支持扩展字段（预留 ext_data JSON 字段）
- 支持多租户隔离（通过 tenant_id 字段）

## 5. 日志埋点与监控告警（Logging, Metrics & Alerting）

**关键日志记录：**

| 日志类型 | 记录内容 | 日志级别 | 用途 |
|---------|---------|---------|------|
| 请求日志 | 接口路径、请求参数、响应结果、耗时 | INFO | 请求追踪、性能分析 |
| 配置变更日志 | 变更前数据、变更后数据、操作人、操作时间 | INFO | 审计、回滚 |
| 错误日志 | 异常堆栈、错误码、错误信息 | ERROR | 问题排查 |
| 校验失败日志 | 校验字段、校验规则、失败原因 | WARN | 优化校验逻辑 |

**监控指标：**

| 指标名称 | 指标说明 | 采集频率 | 告警阈值 |
|---------|---------|---------|---------|
| api_request_count | 接口请求量 | 1分钟 | - |
| api_request_duration | 接口响应时长 | 1分钟 | P99 > 1s |
| api_error_rate | 接口错误率 | 1分钟 | > 5% |
| config_change_count | 配置变更次数 | 1小时 | > 100次（异常频繁变更） |
| db_connection_count | 数据库连接数 | 1分钟 | > 80%（连接池） |

**告警规则：**

1. **接口异常告警：**
   - 条件：接口错误率 > 5% 持续 5 分钟
   - 级别：P2（重要）
   - 通知方式：企业微信、短信

2. **性能异常告警：**
   - 条件：接口 P99 响应时长 > 2s 持续 10 分钟
   - 级别：P3（一般）
   - 通知方式：企业微信

3. **数据库异常告警：**
   - 条件：数据库连接数 > 80% 持续 5 分钟
   - 级别：P2（重要）
   - 通知方式：企业微信、短信

## 6. 测试策略与验收标准（Test Plan & Acceptance Criteria）

**测试类型：**

1. **单元测试：**
   - 覆盖所有 Service 层方法
   - 覆盖率要求：> 80%

2. **接口测试：**
   - 覆盖所有 API 接口
   - 测试正常场景和异常场景
   - 验证参数校验逻辑

3. **集成测试：**
   - 前后端联调测试
   - 数据库读写正确性测试

4. **安全测试：**
   - SQL 注入测试
   - XSS 攻击测试
   - 权限控制测试

5. **性能测试：**
   - 压测峰值 QPS 是否达标
   - 响应时长是否符合要求

**验收标准：**

1. ✅ 功能完整性：所有配置功能（增删改查、启用禁用）正常工作
2. ✅ 数据准确性：配置保存后能正确读取，变更日志记录完整
3. ✅ 权限控制：未授权用户无法访问配置页面和接口
4. ✅ 性能达标：接口响应时长 P99 < 1s
5. ✅ 安全合规：敏感数据加密存储，展示脱敏处理
6. ✅ 日志完整：所有操作记录完整的操作日志
7. ✅ 异常处理：各类异常场景有友好的错误提示

## 7. 发布计划与回滚预案（Release Plan & Rollback）

**发布策略：**

1. **灰度发布：**
   - 阶段一（10%）：内部测试账号，验证基本功能，持续1天
   - 阶段二（50%）：部分运营人员，验证业务场景，持续2天
   - 阶段三（100%）：全量发布

2. **发布步骤：**
   ```
   1. 发布前准备
      - 数据库表结构创建
      - 数据库初始化脚本执行
      - 配置项检查
   
   2. 后端发布
      - 部署新版本后端服务
      - 验证接口可用性
   
   3. 前端发布
      - 部署新版本前端资源
      - 验证页面访问正常
   
   4. 功能验证
      - 执行冒烟测试
      - 检查监控指标
      - 确认日志正常
   ```

3. **监控观察：**
   - 观察指标：接口成功率、响应时长、错误日志
   - 观察时长：每个阶段观察 24 小时
   - 异常标准：错误率 > 5% 或 P99 响应时长 > 2s

**回滚预案：**

1. **回滚触发条件：**
   - 接口错误率 > 10%
   - 接口不可用时长 > 5 分钟
   - 发现严重数据错误

2. **回滚步骤：**
   ```
   1. 前端回滚
      - 回滚到上一版本前端代码
      - 清理浏览器缓存
   
   2. 后端回滚
      - 回滚到上一版本后端服务
      - 重启服务验证
   
   3. 数据修复
      - 如有脏数据，执行数据修复脚本
      - 验证数据一致性
   
   4. 验证确认
      - 执行回归测试
      - 确认功能恢复正常
   ```

3. **应急预案：**
   - 如配置服务完全不可用，启用降级开关，使用缓存配置
   - 如数据库异常，切换到备库
   - 紧急情况联系人：研发负责人、运维负责人

**责任人：**
- 发布负责人：研发 Leader
- 验证负责人：测试 Leader
- 应急联系人：研发负责人（电话：xxx）、运维负责人（电话：xxx）

---

## 附录

### A. 名词解释

- **Infinity**：某外呼平台名称
- **外呼渠道**：用于拨打电话的服务账号
- **发送限制**：为避免过度骚扰用户而设置的外呼频率限制规则
- **优先级**：当有多个外呼渠道时，按优先级分配外呼任务
- **脱敏展示**：隐藏敏感信息的部分内容，仅展示部分字符

### B. 参考文档

- 《触达渠道管理功能设计文档》
- 《CCO控台用户权限管理规范》
- 《数据安全与隐私保护规范》
- 《电信条例与骚扰电话整治方案》

### C. 变更记录

本节与文档开头的变更记录表格一致，便于版本追溯。

---

**文档结束**
