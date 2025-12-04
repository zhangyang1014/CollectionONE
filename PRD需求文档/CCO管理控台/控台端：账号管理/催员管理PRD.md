# 控台端：催员管理 PRD

## 1. 功能概述

为CCO催收系统提供完整的催员管理功能，支持管理员在控台端创建和管理催员账号，配置催员工作参数，管理催员登录安全（IP白名单、人脸识别），统计催员绩效数据，实现催员全生命周期管理，为催收业务提供人力资源基础支撑。

**核心价值**：
- 标准化催员账号管理，规范催员信息维护
- 提升催员管理效率，支持批量导出账号信息
- 强化登录安全控制，支持IP白名单和人脸识别
- 支持绩效统计分析，为人员评估提供数据支撑
- 权限精细化管理，实现按甲方/机构/小组的数据隔离

## 2. 核心功能模块

### 2.1 催员列表页面

#### 2.1.1 列表展示

**功能要求：**
- 以表格形式展示当前权限范围内的所有催员
- 支持多维度筛选（甲方、机构、小组、状态）
- 支持搜索（催员登录ID、催员姓名）
- 支持分页展示（默认每页20条）
- 显示催员统计信息
- 支持新增、编辑、密码重置、启用/禁用操作

**列表字段：**

| 字段 | 说明 | 宽度 |
|------|------|------|
| 催员登录ID | 催员的登录账号（含甲方前缀） | 120px |
| 催员名 | 催员的真实姓名 | 120px |
| 最近登录时间 | 最后一次登录的时间 | 140px |
| 所属甲方 | 催员所属的甲方名称 | 130px |
| 所属机构 | 催员所属的机构名称 | 130px |
| 所属小组 | 催员所属的小组名称 | 130px |
| 创建时间 | 账号创建时间 | 160px |
| 最近修改时间 | 账号最近一次修改时间 | 160px |
| 状态 | 启用/禁用 | 100px |
| 操作 | 编辑/修改密码/登录人脸查询/查看IM端/启用-禁用 | 460px |

**最近登录时间显示规则：**
- 日期行：显示年-月-日（如：2025-12-04）
- 时间行：显示时:分:秒（如：09:15:30），灰色字体
- 未登录过：显示"--"

#### 2.1.2 筛选器配置

**筛选条件：**

| 筛选项 | 类型 | 选项 | 默认值 |
|--------|------|------|--------|
| 搜索框 | 文本输入 | 支持催员登录ID、催员名模糊搜索 | 空 |
| 状态筛选 | 下拉单选 | 全部、启用、禁用 | 启用 |
| 机构筛选 | 下拉单选 | 全部机构、具体机构列表 | 全部机构 |
| 小组筛选 | 下拉单选 | 全部小组、具体小组列表 | 全部小组 |

**筛选逻辑：**
- 多个筛选条件使用AND逻辑组合
- 搜索框支持模糊匹配（催员登录ID或催员名）
- 机构筛选变化时，自动重置小组筛选为"全部小组"
- 筛选后保持分页状态重置到第一页
- 权限控制：
  - 甲方管理员：显示本甲方所有机构和小组
  - 机构管理员：仅显示本机构和所属小组，机构筛选器隐藏
  - 小组管理员：仅显示本小组，机构和小组筛选器均隐藏

**催员统计信息：**
- 位置：列表表格上方
- 显示内容："当前筛选条件下，共有 X 位催员"
- 样式：灰色背景卡片，主色调数字

#### 2.1.3 操作按钮

**创建催员：**
- 位置：列表页面右上角
- 按钮文字："创建催员"
- 按钮样式：主要按钮（蓝色）
- 权限控制：需要选择甲方后才能点击
- 点击后：打开催员配置表单（新增模式）

**导出催员账号与密码：**
- 位置：列表页面右上角，创建催员按钮旁边
- 按钮文字："导出催员账号与密码"
- 按钮样式：成功按钮（绿色）
- 权限控制：需要选择甲方后才能点击
- 点击后：导出当前筛选条件下的所有催员账号信息，注意要全部分页内容！（Excel格式）
- 导出内容：催员登录ID、催员姓名、所属机构、所属小组、邮箱、创建时间、密码（明文，仅创建时可导出）

**编辑催员：**
- 位置：每行操作列
- 按钮文字："编辑"
- 按钮样式：链接按钮（主色调）
- 点击后：打开催员配置表单（编辑模式），回填数据

**修改密码：**
- 位置：每行操作列
- 按钮文字："修改密码"
- 按钮样式：链接按钮（警告色）
- 点击后：打开密码重置对话框
- 确认提示：输入新密码和确认密码

**登录人脸查询：**
- 位置：每行操作列
- 按钮文字："登录人脸查询"
- 按钮样式：链接按钮（信息色）
- 点击后：打开登录人脸记录查询对话框
- 显示内容：该催员的所有登录人脸记录（时间轴展示）
	- 下文详细描述

**查看IM端：**
- 位置：每行操作列
- 按钮文字："查看IM端"
- 按钮样式：链接按钮（成功色）
- 点击后：在新标签页打开IM端登录页面，传递催员ID和甲方ID参数，支持模拟登录

**启用/禁用：**
- 位置：每行操作列
- 按钮文字：根据当前状态显示"启用"或"禁用"
- 按钮样式：链接按钮（警告色或成功色）
- 点击后：二次确认弹窗
- 确认提示：
  - 禁用："确定要禁用催员【{催员姓名}】吗？禁用后该催员将无法登录系统。"
  - 启用："确定要启用催员【{催员姓名}】吗？"

### 2.2 催员配置表单

#### 2.2.1 表单布局

采用对话框（Dialog）展示，宽度600px，分为以下部分：
- 基础信息（催员登录ID、催员姓名、所属机构、所属小组）
- 账号信息（登录密码、邮箱、备注）
- 状态配置（是否启用）

#### 2.2.2 基础信息

| 字段 | 类型 | 是否必填 | 说明 | 校验规则 |
|------|------|---------|------|----------|
| 催员登录ID | 文本输入 | 必填 | 催员登录账号，用于登录系统 | 含甲方前缀，长度2-50字符，系统内唯一，编辑时不可修改 |
| 催员姓名 | 文本输入 | 必填 | 催员真实姓名 | 长度2-50字符，支持中英文 |
| 所属机构 | 下拉选择 | 必填 | 选择催员所属机构 | 从当前甲方的机构列表中选择，不可为空 |
| 所属小组 | 下拉选择 | 必填 | 选择催员所属小组 | 从选定机构的小组列表中选择，不可为空 |

**催员登录ID说明：**
- **新增模式**：
  - 输入框默认填充"甲方编码-"前缀（如：`ABC-`）
  - 用户只需输入后缀部分（如：`col001`）
  - 系统自动拼接为完整登录ID（如：`ABC-col001`）
  - 占位符提示："格式：{甲方编码}-自定义部分（如：{甲方编码}-col001）"
- **编辑模式**：
  - 显示完整登录ID（如：`ABC-col001`）
  - 字段为只读状态，禁用编辑
  - 提示文字："登录ID创建后不可修改"
- **格式要求**：
  - 前缀：必须以甲方编码开头，后接"-"
  - 后缀：2-45字符，支持字母、数字、下划线
  - 示例：`ABC-col001`、`TENANT01-collector01`
- **唯一性验证**：
  - 失焦时实时验证是否重复
  - 如重复，显示错误提示："该登录ID已存在"

**所属机构和小组联动：**
- **选择机构时**：
  - 自动加载该机构下的小组列表
  - 如之前已选择小组，清空小组选择
  - 小组下拉框变为可用状态
- **切换机构时**：
  - 清空已选择的小组
  - 重新加载新机构的小组列表
  - 提示用户："机构已切换，请重新选择小组"
- **编辑模式**：
  - 回填时自动加载对应机构的小组列表
  - 自动选中催员当前所属小组
  - 支持修改所属机构和小组

#### 2.2.3 账号信息

| 字段 | 类型 | 是否必填 | 说明 | 校验规则 |
|------|------|---------|------|----------|
| 登录密码 | 密码输入 | 新增时必填 | 催员登录密码 | 最少6位字符，支持显示/隐藏 |
| 邮箱 | 文本输入 | 必填 | 催员联系邮箱，用于接收系统通知 | 最长100字符，必须符合邮箱格式 |
| 备注 | 多行文本 | 选填 | 催员的备注信息，如工作职责、特长等 | 最长500字符 |

**登录密码管理：**
- **新增模式**：
  - 密码字段为必填项
  - 提供"生成密码"按钮，点击自动生成6-8位随机密码（包含大小写字母+数字）
  - 支持手动输入密码
  - 支持显示/隐藏密码（眼睛图标切换）
  - 密码强度提示：输入时显示密码强度（弱/中/强）
- **编辑模式**：
  - 不显示密码字段（密码通过"修改密码"功能单独管理）
  - 保持原密码不变
  - 如需修改密码，点击列表页"修改密码"按钮

**邮箱说明：**
- **用途**：
  - 接收系统通知（如密码重置、账号状态变更等）
  - 作为催员联系方式之一
  - 用于找回密码（预留功能）
- **格式要求**：
  - 必须符合标准邮箱格式（如：`example@domain.com`）
  - 失焦时实时验证格式
  - 格式错误时显示："请输入正确的邮箱格式"
- **唯一性**：
  - 建议不重复，但系统不强制校验唯一性
  - 允许多个催员使用相同邮箱（如团队共用邮箱）

**备注字段说明：**
- **输入类型**：多行文本框（Textarea）
- **显示行数**：3行（可滚动）
- **最大字符数**：500字符
- **字符计数**：实时显示已输入字符数（如："已输入 50 / 500 字符"）
- **占位符**："请输入备注信息，如工作职责、特长、注意事项等"
- **用途示例**：
  - 催员的工作职责或分工
  - 催员的特长或擅长领域
  - 特殊注意事项
  - 临时备注信息
- **显示位置**：
  - 新增/编辑表单：显示在账号信息区域
  - 催员列表：不显示（鼠标悬停可显示tooltip）
  - 催员详情：完整显示备注内容




#### 2.2.4 状态配置

| 字段 | 类型 | 是否必填 | 默认值 | 说明 |
|------|------|---------|--------|------|
| 是否启用 | 开关 | 必填 | 启用 | 控制催员是否可登录系统 |

**是否启用说明：**
- **启用状态**（开关打开）：
  - 催员可以正常登录系统
  - 可以被分配案件
  - 可以进行催收工作
- **禁用状态**（开关关闭）：
  - 催员无法登录系统
  - 无法被分配新案件
  - 已分配的案件保持不变（需手动重新分配）
  - 历史数据完整保留
- **默认值**：新增催员时默认为"启用"状态

#### 2.2.5 表单操作按钮

**保存：**
- 按钮文字："保存"
- 按钮样式：主要按钮（蓝色）
- 点击后：
  1. 校验所有必填字段
  2. 校验催员登录ID格式（含甲方前缀）
  3. 提交到后端API
  4. 成功后：显示成功提示，关闭表单，刷新列表
  5. 失败后：显示错误信息

**取消：**
- 按钮文字："取消"
- 按钮样式：普通按钮
- 点击后：
  1. 如果有未保存修改，二次确认："当前有未保存的修改，确定要离开吗？"
  2. 确认后关闭表单

### 2.3 密码管理

#### 2.3.1 密码重置对话框

**对话框布局：**
- 标题："修改密码"
- 宽度：500px
- 显示催员姓名和登录ID

**表单字段：**

| 字段 | 类型 | 是否必填 | 说明 | 校验规则 |
|------|------|---------|------|----------|
| 新密码 | 密码输入 | 必填 | 输入新密码 | 最少6位字符，支持显示/隐藏 |
| 确认密码 | 密码输入 | 必填 | 再次输入新密码 | 必须与新密码一致 |

**密码校验规则：**
- 最少6位字符
- 确认密码必须与新密码一致
- 支持显示/隐藏密码

**操作按钮：**
- 确定：提交新密码，成功后关闭对话框，显示"密码修改成功"
- 取消：关闭对话框，不保存修改

#### 2.3.2 随机密码生成

**生成规则：**
- 长度：6-8位（随机）
- 字符组成：
  - 至少包含1个大写字母
  - 至少包含1个小写字母
  - 至少包含1个数字
- 生成后自动填充到密码输入框

**生成逻辑示例：**
```javascript
function generatePassword() {
  const lowercase = 'abcdefghijklmnopqrstuvwxyz'
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const numbers = '0123456789'
  const allChars = lowercase + uppercase + numbers
  
  // 确保至少包含一个大写字母、一个小写字母和一个数字
  let password = ''
  password += uppercase[Math.floor(Math.random() * uppercase.length)]
  password += lowercase[Math.floor(Math.random() * lowercase.length)]
  password += numbers[Math.floor(Math.random() * numbers.length)]
  
  // 随机生成剩余字符，总长度在6-8位之间
  const length = Math.floor(Math.random() * 3) + 6
  for (let i = password.length; i < length; i++) {
    password += allChars[Math.floor(Math.random() * allChars.length)]
  }
  
  // 打乱字符顺序
  password = password.split('').sort(() => Math.random() - 0.5).join('')
  
  return password
}
```

#### 2.3.3 密码加密存储

**加密算法：**
- 使用BCrypt算法加密存储
- 加密强度：默认10轮（cost factor）
- 不可逆加密，无法解密获取原始密码

**安全要求：**
- 日志中不记录完整密码
- 仅在创建时导出明文密码（供初次登录使用）
- 密码重置后不提供明文密码


### 2.5 登录人脸识别

#### 2.5.1 功能概述

催员登录时进行人脸识别，记录登录人脸照片和人脸ID，管理员可以查询催员的历史登录人脸记录，用于身份验证和安全审计。

**功能特性：**
- IM端登录时自动拍照并进行人脸识别
- 控台端可查询催员的所有登录人脸记录
- 时间轴展示，按登录时间倒序
- 显示人脸照片和人脸ID
- 管理员所在的  小组管理员、机构管理、甲方管理员、superadmin都可以查看

#### 2.5.2 登录人脸记录查询

**访问入口：**
- 位置：催员列表操作列的"登录人脸查询"按钮
- 权限：甲方管理员、机构管理员、系统超级管理员

**查询对话框：**
- 标题："{催员姓名} - 登录人脸查询"
- 宽度：80%
- 展示方式：时间轴（Timeline）组件

**记录展示：**
- 每条记录一个时间轴节点
- 卡片式布局：照片 + 信息
- 照片：150x150px，圆角，带边框
- 信息：
  - 登录时间（时间戳显示）
  - 人脸ID（标签显示）
  - 记录创建时间

**空状态处理：**
- 无记录时显示空状态提示："暂无登录记录"
- 加载时显示loading动画

**图片加载错误处理：**
- 显示占位图（灰色背景+文字"图片未加载"）

#### 2.5.3 人脸识别流程（IM端）
详见：https://www.tapd.cn/67450206/markdown_wikis/show/#1167450206001002764


### 2.6 案件分配与统计

#### 2.6.1 案件分配入口

**分配方式：**
- 手动分配：在案件列表页面，选择案件后分配给指定催员
- 自动分配：按照分配规则自动分配案件给催员

**分配规则：**
- 均衡分配：按催员当前案件数平均分配
- 随机分配：随机分配给催员
- 技能匹配：根据催员擅长领域分配

#### 2.6.2 案件数量统计

**统计维度：**
- 当前案件数：催员当前负责的案件数量（不含已结清）
- 最大案件数：催员可承接的最大案件数量
- 今日分配：今日新分配的案件数量
- 本周分配：本周新分配的案件数量
- 本月结清：本月结清的案件数量

**显示位置：**
- 催员列表页面：可选列（默认不显示）
- 催员详情页面：显示完整统计信息


--- 
仅供参考
## 3. 数据库设计

### 3.1 collectors 表（催员表）

```sql
CREATE TABLE collectors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
  agency_id BIGINT NOT NULL COMMENT '所属机构ID',
  team_id BIGINT NOT NULL COMMENT '所属小组ID',
  
  -- 基础信息
  collector_code VARCHAR(100) NOT NULL UNIQUE COMMENT '催员编码（含甲方前缀，如：ABC-col001）',
  collector_name VARCHAR(50) NOT NULL COMMENT '催员姓名',
  login_id VARCHAR(100) NOT NULL UNIQUE COMMENT '登录ID（含甲方前缀）',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt加密）',
  email VARCHAR(100) NOT NULL COMMENT '邮箱（必填）',
  
  -- 状态字段
  status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/休假/离职',
  is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  remark TEXT COMMENT '备注信息',
  last_login_at DATETIME COMMENT '最后登录时间',
  
  -- 时间戳
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_tenant_id (tenant_id),
  INDEX idx_agency_id (agency_id),
  INDEX idx_team_id (team_id),
  INDEX idx_collector_code (collector_code),
  INDEX idx_login_id (login_id),
  INDEX idx_is_active (is_active),
  UNIQUE KEY uk_collector_code (collector_code),
  UNIQUE KEY uk_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催员表';
```

**字段说明：**

| 字段 | 类型 | 说明 | 业务规则 |
|------|------|------|----------|
| id | BIGINT | 主键ID | 自增 |
| tenant_id | BIGINT | 所属甲方ID | 数据隔离，必填 |
| agency_id | BIGINT | 所属机构ID | 必填 |
| team_id | BIGINT | 所属小组ID | 必填 |
| collector_code | VARCHAR(100) | 催员编码 | 含甲方前缀，系统内唯一 |
| collector_name | VARCHAR(50) | 催员姓名 | 必填 |
| login_id | VARCHAR(100) | 登录ID | 含甲方前缀，系统内唯一 |
| password_hash | VARCHAR(255) | 密码哈希 | BCrypt加密，不可逆 |
| email | VARCHAR(100) | 邮箱 | 必填，用于接收系统通知 |
| status | VARCHAR(20) | 状态 | active/休假/离职，默认active |
| is_active | BOOLEAN | 是否启用 | 控制登录权限 |
| remark | TEXT | 备注信息 | 选填 |
| last_login_at | DATETIME | 最后登录时间 | 登录时更新 |

### 3.2 collector_login_whitelist 表（登录白名单）

```sql
CREATE TABLE collector_login_whitelist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
  is_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用白名单IP登录管理',
  ip_address VARCHAR(50) NOT NULL COMMENT '白名单IP地址（支持IPv4和CIDR格式）',
  description VARCHAR(200) COMMENT 'IP地址描述/备注',
  is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_tenant_id (tenant_id),
  INDEX idx_is_enabled (is_enabled),
  INDEX idx_is_active (is_active),
  UNIQUE KEY uk_tenant_ip (tenant_id, ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催员登录白名单IP配置表';
```

### 3.3 collector_login_face_records 表（人脸记录）

```sql
CREATE TABLE collector_login_face_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  collector_id BIGINT NOT NULL COMMENT '催员ID',
  tenant_id BIGINT NOT NULL COMMENT '甲方ID',
  login_time DATETIME NOT NULL COMMENT '登录时间',
  face_image TEXT COMMENT '人脸照片（base64或URL）',
  face_id VARCHAR(100) NOT NULL COMMENT '人脸ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  -- 索引
  INDEX idx_collector_login (collector_id, login_time DESC),
  INDEX idx_face_id (face_id),
  INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催员登录人脸记录表';
```

### 3.4 collector_performance_stats 表（绩效统计）

```sql
CREATE TABLE collector_performance_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  collector_id BIGINT NOT NULL COMMENT '催员ID',
  tenant_id BIGINT NOT NULL COMMENT '甲方ID',
  agency_id BIGINT NOT NULL COMMENT '机构ID',
  team_id BIGINT NOT NULL COMMENT '小组ID',
  queue_ids JSON COMMENT '队列ID数组',
  
  -- 统计周期
  stat_date DATE NOT NULL COMMENT '统计日期',
  stat_period VARCHAR(20) NOT NULL COMMENT '统计周期：daily/weekly/monthly',
  
  -- 业绩总览
  assigned_cases INT DEFAULT 0 COMMENT '应催案件数',
  collected_cases INT DEFAULT 0 COMMENT '收回案件数',
  case_collection_rate DECIMAL(5, 2) COMMENT '案件催回率（%）',
  assigned_amount DECIMAL(15, 2) DEFAULT 0 COMMENT '应催金额',
  collected_amount DECIMAL(15, 2) DEFAULT 0 COMMENT '收回金额',
  amount_collection_rate DECIMAL(5, 2) COMMENT '金额催回率（%）',
  
  -- PTP指标
  ptp_count INT DEFAULT 0 COMMENT 'PTP数量',
  ptp_amount DECIMAL(15, 2) DEFAULT 0 COMMENT 'PTP金额',
  ptp_fulfilled_count INT DEFAULT 0 COMMENT 'PTP履约数量',
  ptp_fulfillment_rate DECIMAL(5, 2) COMMENT 'PTP履约率（%）',
  
  -- 沟通覆盖
  self_contact_rate DECIMAL(5, 2) COMMENT '本人沟通率（%）',
  total_contact_rate DECIMAL(5, 2) COMMENT '总联系人沟通率（%）',
  
  -- 电话指标
  self_call_rate DECIMAL(5, 2) COMMENT '本人接通率（%）',
  total_call_rate DECIMAL(5, 2) COMMENT '总接通率（%）',
  
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_collector (collector_id, stat_date DESC),
  INDEX idx_tenant (tenant_id, stat_date DESC),
  INDEX idx_agency (agency_id, stat_date DESC),
  INDEX idx_team (team_id, stat_date DESC),
  INDEX idx_stat_period (stat_period, stat_date DESC),
  UNIQUE KEY uk_collector_date_period (collector_id, stat_date, stat_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催员绩效统计表';
```

## 4. 接口设计

### 4.1 控台端接口（管理端）

#### 4.1.1 获取催员列表

```
GET /api/v1/teams/{team_id}/collectors
```

**请求参数：**
```json
{
  "teamId": 1,
  "keyword": "col001",
  "isActive": true,
  "agencyId": 1,
  "page": 1,
  "pageSize": 20
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
      "collectorCode": "ABC-col001",
      "collectorName": "张三",
      "tenantId": 1,
      "tenantName": "示例甲方A",
      "agencyId": 1,
      "agencyName": "示例机构A",
      "teamId": 1,
      "teamName": "示例小组A",
      "loginId": "ABC-col001",
      "email": "zhangsan@example.com",
      "remark": "负责高额案件，经验丰富",
      "isActive": true,
      "lastLoginAt": "2025-12-04 09:15:30",
      "createdAt": "2025-01-01 10:00:00",
      "updatedAt": "2025-12-04 09:15:30"
    }
  ]
}
```

#### 4.1.2 创建催员

```
POST /api/v1/collectors
```

**请求参数：**
```json
{
  "tenantId": 1,
  "agencyId": 1,
  "teamId": 1,
  "collectorCode": "ABC-col001",
  "collectorName": "张三",
  "loginId": "ABC-col001",
  "password": "Abc123",
  "email": "zhangsan@example.com",
  "isActive": true,
  "remark": "新入职催员，擅长高额案件处理"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "collectorCode": "ABC-col001",
    "collectorName": "张三"
  }
}
```

**错误码：**

| HTTP状态码 | 业务错误码 | 错误信息 | 说明 |
|-----------|----------|---------|------|
| 400 | 400 | Collector code already exists | 催员编码已存在 |
| 400 | 400 | Login ID already exists | 登录ID已存在 |
| 400 | 400 | Invalid collector code format | 催员编码格式错误（缺少甲方前缀） |
| 400 | 400 | Password too short | 密码长度不足6位 |

#### 4.1.3 更新催员

```
PUT /api/v1/collectors/{id}
```

**请求参数：** 同创建接口（不包含密码字段）

**响应数据：** 同创建接口

#### 4.1.4 重置密码

```
PUT /api/v1/collectors/{id}/password
```

**请求参数：**
```json
{
  "newPassword": "NewPass123"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "Password reset successfully",
  "data": {
    "collectorId": 1
  }
}
```

#### 4.1.5 启用/禁用催员

```
PUT /api/v1/collectors/{id}/status
```

**请求参数：**
```json
{
  "isActive": false
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "collectorId": 1,
    "isActive": false
  }
}
```

#### 4.1.6 获取登录人脸记录

```
GET /api/v1/collectors/{id}/login-face-records
```

**请求参数：**
```json
{
  "collectorId": 1,
  "page": 1,
  "pageSize": 10
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
      "collectorId": 1,
      "loginTime": "2025-12-04 09:15:30",
      "faceImage": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
      "faceId": "FACE_20251204_001",
      "createdAt": "2025-12-04 09:15:35"
    }
  ]
}
```

#### 4.1.7 导出催员账号

```
GET /api/v1/collectors/export-accounts
```

**请求参数：**
```json
{
  "tenantId": 1,
  "agencyId": 1,
  "teamId": 1,
  "isActive": true
}
```

**响应数据：**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 文件名：`collectors_export_{timestamp}.xlsx`
- 内容：催员账号信息Excel表格

**导出字段：**
- 催员登录ID
- 催员姓名
- 所属甲方
- 所属机构
- 所属小组
- 邮箱
- 备注
- 创建时间
- 密码（仅新创建的催员，历史催员不导出密码）

**导出说明：**
- 导出全部分页内容，不受当前页码限制
- 按筛选条件导出（如只导出已启用的催员）
- Excel格式，UTF-8编码
- 文件名格式：`催员账号_{甲方名称}_{时间戳}.xlsx`

### 4.2 白名单管理接口

#### 4.2.1 获取白名单配置列表

```
GET /api/v1/collector-login-whitelist
```

**请求参数：**
```json
{
  "tenantId": 1,
  "page": 1,
  "pageSize": 20
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "list": [
      {
        "id": 1,
        "tenantId": 1,
        "isEnabled": true,
        "ipAddress": "192.168.1.1",
        "description": "办公室IP",
        "isActive": true,
        "createdAt": "2025-01-01 10:00:00"
      }
    ]
  }
}
```

#### 4.2.2 创建白名单IP配置

```
POST /api/v1/collector-login-whitelist
```

**请求参数：**
```json
{
  "tenantId": 1,
  "ipAddress": "192.168.1.1",
  "description": "办公室IP",
  "isActive": true
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

#### 4.2.3 更新白名单IP配置

```
PUT /api/v1/collector-login-whitelist/{id}
```

**请求参数：** 同创建接口

**响应数据：** 同创建接口

#### 4.2.4 删除白名单IP配置

```
DELETE /api/v1/collector-login-whitelist/{id}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success"
}
```

#### 4.2.5 启用/禁用白名单管理

```
PUT /api/v1/collector-login-whitelist/enable
```

**请求参数：**
```json
{
  "tenantId": 1,
  "enabled": true
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success"
}
```

### 4.3 绩效统计接口

#### 4.3.1 获取催员绩效

```
GET /api/v1/collectors/{id}/performance
```

**请求参数：**
```json
{
  "collectorId": 1,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "statPeriod": "monthly"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "collectorId": 1,
    "collectorName": "张三",
    "statPeriod": "monthly",
    "statDate": "2025-12-01",
    "assignedCases": 100,
    "collectedCases": 45,
    "caseCollectionRate": 45.00,
    "assignedAmount": 1000000.00,
    "collectedAmount": 450000.00,
    "amountCollectionRate": 45.00,
    "ptpCount": 30,
    "ptpAmount": 300000.00,
    "ptpFulfilledCount": 25,
    "ptpFulfillmentRate": 83.33,
    "selfContactRate": 75.00,
    "totalContactRate": 85.00
  }
}
```

#### 4.3.2 获取催员排名

```
GET /api/v1/collectors/{id}/ranking
```

**请求参数：**
```json
{
  "collectorId": 1,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "scope": "team",
  "rankBy": "case_collection_rate"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "collectorId": 1,
    "collectorName": "张三",
    "scope": "team",
    "rankBy": "case_collection_rate",
    "rank": 3,
    "total": 10,
    "value": 45.00,
    "topCollector": {
      "collectorId": 5,
      "collectorName": "李四",
      "value": 55.00
    }
  }
}
```

## 5. 业务规则与边界

### 5.1 核心业务规则

**权限隔离规则：**
- 数据按甲方（tenant_id）隔离
- 甲方管理员只能管理本甲方的催员
- 机构管理员只能管理本机构的催员
- 小组管理员只能管理本小组的催员

**编码规范规则：**
- 催员编码必须以"甲方编码-"开头（如：`ABC-col001`）
- 催员登录ID必须以"甲方编码-"开头（如：`ABC-collector01`）
- 前缀自动填充：前端创建表单自动填充甲方编码前缀
- 后端验证：创建时验证编码格式是否符合规范
- 编码唯一性：在系统内全局唯一（跨甲方不重复）

**密码管理规则：**
- 密码最少6位字符
- 使用BCrypt加密存储（cost factor = 10）
- 密码哈希不可逆，无法解密
- 创建时可导出明文密码（供初次登录）
- 密码重置后不提供明文密码

**唯一性规则：**
- 催员编码在系统内唯一（uk_collector_code）
- 催员登录ID在系统内唯一（uk_login_id）
- IP地址在同一甲方内唯一（uk_tenant_ip）

**启用/禁用规则：**
- 禁用催员：催员无法登录，但历史数据保留
- 启用催员：恢复登录权限
- 禁用时二次确认："确定要禁用催员【{催员姓名}】吗？禁用后该催员将无法登录系统。"

### 5.2 IP白名单规则

**白名单控制流程：**
1. 催员登录时，系统获取客户端IP地址
2. 检查该甲方是否启用了白名单IP登录管理
3. 如果未启用，允许所有IP登录
4. 如果已启用：
   - 检查IP是否在白名单中
   - 如果在白名单中且is_active=true，允许登录
   - 如果不在白名单中，返回403错误

**IP地址格式：**
- 支持IPv4格式：`192.168.1.1`
- 支持CIDR格式：`192.168.1.0/24`
- CIDR掩码范围：0-32

**CIDR网段匹配：**
- `192.168.1.0/24` 匹配 `192.168.1.1` 到 `192.168.1.254`
- `10.0.0.0/8` 匹配 `10.0.0.0` 到 `10.255.255.255`

**降级策略：**
- 数据库表不存在时：默认禁用白名单（允许所有IP登录）
- 数据库查询失败时：默认允许登录（避免影响正常业务）

### 5.3 人脸识别规则

**人脸识别流程：**
- IM端登录时可选进行人脸识别
- 开发测试模式：人脸识别失败不影响登录
- 生产模式：可配置为必填项

**数据存储：**
- 人脸照片：base64格式或上传到OSS存储URL
- 人脸ID：由人脸检测服务返回，相同人脸返回相同ID
- 数据加密：人脸照片建议加密存储

**隐私保护：**
- 访问权限：仅甲方管理员、机构管理员可查看
- 数据保留：建议设置保留期限（如90天）
- 合规性：遵守《个人信息保护法》等法律法规

### 5.4 范围边界

**本次需求范围内：**
- ✅ 催员的创建、编辑、查询、启用/禁用
- ✅ 密码管理和重置
- ✅ 登录IP白名单管理
- ✅ 登录人脸识别记录查询
- ✅ 基本的权限控制（基于角色的数据隔离）
- ✅ 编码规范的系统实现和验证
- ✅ 催员账号导出功能

**本次需求范围外：**
- ⏳ 催员绩效统计报表（后端接口已设计，前端页面待实现）
- ⏳ 催员排名功能（后端接口已设计，前端页面待实现）
- ⏳ 案件分配功能（在案件管理模块实现）
- ⏳ 批量导入催员（后续需求）
- ⏳ 催员工作量监控（后续需求）
- ⏳ 催员考勤管理（后续需求）
- ❌ 催员薪资管理（不在系统范围内）

## 6. 非功能性要求（Non-Functional Requirements）

### 6.1 性能要求

**响应时间：**
- 催员列表查询：≤ 500ms
- 创建/更新催员：≤ 1s
- 密码重置：≤ 500ms
- 导出账号：≤ 3s（1000条催员）
- 人脸记录查询：≤ 500ms

**并发支持：**
- 支持100+ 管理员同时操作
- 支持1000+ 催员同时登录

**数据量支持：**
- 单个甲方支持10,000+ 催员
- 全系统支持100,000+ 催员记录
- 人脸记录支持1,000,000+ 记录

### 6.2 可用性要求

**SLA：**
- 服务可用性：≥ 99.9%
- 数据持久化：≥ 99.99%

**降级策略：**
- IP白名单服务异常时：默认允许登录
- 人脸识别服务异常时：允许跳过人脸识别
- 绩效统计服务异常时：返回空数据，不影响核心功能

### 6.3 安全要求

**权限控制：**
- 接口级别鉴权：所有接口需要登录Token
- 数据级别隔离：按tenant_id隔离
- 操作级别审计：记录创建人、更新人

**密码安全：**
- BCrypt加密存储（不可逆）
- 日志中不记录完整密码
- 密码传输使用HTTPS加密
- 密码重置需要管理员权限

**数据脱敏：**
- 日志中手机号脱敏（显示前3后4位）
- 日志中邮箱脱敏（显示前3位+***+域名）
- 错误信息不暴露敏感字段

**IP白名单：**
- 支持IP地址访问控制
- 支持CIDR网段配置
- 登录时实时验证IP

**人脸识别：**
- 人脸照片加密存储
- 仅授权管理员可查看
- 数据保留期限可配置

### 6.4 扩展性要求

**催员规模扩展：**
- 支持水平扩展（增加服务器节点）
- 支持数据库分库分表（按tenant_id）

**功能扩展：**
- 预留绩效统计扩展字段
- 支持自定义催员属性（扩展字段）

## 7. 交互与信息展示（UX & UI Brief）

### 7.1 列表页面交互

**筛选器交互：**
- 筛选器默认展开
- 选择筛选条件后立即刷新列表
- 机构筛选变化时，自动重置小组筛选
- 清空按钮：一键清空所有筛选条件

**表格交互：**
- 支持按列排序（创建时间、最近登录时间）
- 鼠标悬停行：高亮显示
- 最近登录时间：分两行显示（日期+时间）
- 分页：底部居中显示，支持跳转指定页

**搜索交互：**
- 实时搜索：输入即搜索（防抖300ms）
- 搜索范围：催员登录ID、催员姓名
- 支持模糊匹配

### 7.2 表单交互

**表单布局：**
- 采用对话框（Dialog）展示，宽度600px
- 分为3个区域：基础信息、账号信息、状态配置
- 每个区域用分割线或标题分隔
- 区域标题：14px，加粗，灰色字体

**实时校验：**
- 催员登录ID：失焦时校验是否重复
- 密码：输入时显示强度提示
- 邮箱：失焦时校验格式
- 必填项：显示红色星号

**联动交互：**
- 选择机构后：自动加载该机构的小组列表
- 切换机构时：清空已选小组，重新加载小组列表
- 编辑模式：禁用催员登录ID字段

**密码管理交互：**
- 新增模式：显示密码字段，提供"生成密码"按钮
- 编辑模式：隐藏密码字段（通过"修改密码"功能单独管理）
- 生成密码：点击按钮自动填充随机密码
- 显示/隐藏：眼睛图标切换密码可见性

### 7.3 密码重置交互

**对话框布局：**
- 标题："修改密码"
- 显示催员信息："{催员姓名}（{催员登录ID}）"
- 宽度：500px

**表单交互：**
- 新密码：失焦时校验长度
- 确认密码：失焦时校验是否一致
- 支持显示/隐藏密码

### 7.4 人脸记录查询交互

**对话框布局：**
- 标题："{催员姓名} - 登录人脸查询"
- 宽度：80%
- 使用时间轴组件展示

**时间轴展示：**
- 按登录时间倒序展示
- 每条记录显示：照片（150x150px）+ 信息
- 照片加载错误时：显示占位图
- 空状态：显示"暂无登录记录"

### 7.5 样式规范

**颜色规范：**
- 主色调：#409EFF（Element Plus蓝）
- 成功色：#67C23A（启用状态）
- 危险色：#F56C6C（删除按钮）
- 警告色：#E6A23C（警告提示）
- 文字色：#303133（主要文字）
- 次要文字色：#909399（提示文字）

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

**组件规范：**
- 按钮高度：32px
- 输入框高度：32px
- 表格行高：48px
- 对话框圆角：4px

## 8. 测试策略与验收标准（Test Plan & Acceptance Criteria）

### 8.1 测试用例

**功能测试：**
- 创建催员：成功创建，列表显示，编码含甲方前缀
- 编辑催员：更新成功，内容变更，登录ID不可修改
- 重置密码：密码重置成功，可用新密码登录
- 启用/禁用催员：状态切换，影响登录权限
- 筛选功能：按机构、小组、状态筛选，结果正确
- 搜索功能：关键词匹配，结果正确
- 导出账号：文件生成正确，包含所有催员信息
- 随机密码生成：密码符合规则（6-8位，含大小写字母+数字）
- IP白名单：添加/编辑/删除IP，启用/禁用白名单
- 人脸记录查询：显示催员登录人脸记录，时间轴展示

**边界测试：**
- 催员登录ID长度：超过50字符，提示错误
- 密码长度：少于6位，提示错误
- IP地址格式：输入非法IP，提示错误
- CIDR格式：输入非法CIDR，提示错误
- 重复催员编码：提示"催员编码已存在"
- 重复登录ID：提示"登录ID已存在"
- 重复IP地址：提示"IP地址已存在"

**权限测试：**
- 甲方管理员：可管理本甲方所有催员
- 机构管理员：仅可管理本机构催员
- 小组管理员：仅可管理本小组催员
- 跨甲方访问：返回403错误

**性能测试：**
- 1000条催员列表查询：响应时间 < 500ms
- 并发创建催员：100并发，成功率 > 95%
- 导出1000条催员：响应时间 < 3s

### 8.2 验收标准

**核心验收标准：**
1. ✅ 控台端可创建、编辑、删除催员
2. ✅ 支持多维度筛选和搜索
3. ✅ 支持密码管理和重置
4. ✅ 支持随机密码生成
5. ✅ 支持IP白名单控制
6. ✅ 支持人脸识别记录查询
7. ✅ 支持催员账号导出
8. ✅ 权限隔离正常（按甲方、按机构、按小组）
9. ✅ 编码规范正常（含甲方前缀）
10. ✅ 接口响应时间符合要求
11. ✅ 数据持久化正常

## 9. 日志埋点与监控告警（Logging, Metrics & Alerting）

### 9.1 关键日志

**操作日志：**
- 创建催员：记录创建人、催员登录ID、所属机构
- 更新催员：记录更新人、更新字段、更新前后值
- 删除催员：记录删除人、催员信息（软删除）
- 重置密码：记录操作人、催员ID（不记录密码）
- 启用/禁用：记录操作人、催员ID、操作类型
- IP白名单操作：记录操作人、IP地址、操作类型

**错误日志：**
- 催员创建失败：记录请求参数、错误原因
- 催员查询失败：记录查询条件、错误堆栈
- 密码重置失败：记录催员ID、错误原因
- IP白名单验证失败：记录客户端IP、错误原因

### 9.2 监控指标

**业务指标：**
- 催员总数：按甲方、机构、小组统计
- 催员创建量：每日新增催员数
- 催员删除量：每日删除催员数
- 密码重置次数：每日密码重置次数
- IP白名单拦截次数：每日拦截登录次数

**性能指标：**
- 接口响应时间：P50、P95、P99
- 接口成功率：成功请求数 / 总请求数
- 数据库查询耗时：慢查询统计

### 9.3 告警规则

| 告警项 | 阈值 | 级别 | 处理方式 |
|--------|------|------|----------|
| 接口成功率 | < 95% | 严重 | 立即通知开发团队 |
| 接口响应时间 | P95 > 2s | 警告 | 发送告警通知 |
| 数据库查询耗时 | > 5s | 警告 | 记录慢查询日志 |
| 催员创建失败率 | > 10% | 警告 | 检查数据库连接 |
| IP白名单拦截率 | > 50% | 警告 | 检查白名单配置 |

## 10. 发布计划与回滚预案（Release Plan & Rollback）

### 10.1 发布策略

**分阶段发布：**
1. **阶段1**：数据库表创建（无业务影响）
   - 创建collectors表
   - 创建collector_login_whitelist表
   - 创建collector_login_face_records表
   - 创建collector_performance_stats表
2. **阶段2**：后端接口上线（灰度10%流量）
   - 催员CRUD接口
   - 密码管理接口
   - IP白名单接口
   - 人脸记录查询接口
3. **阶段3**：控台端页面上线（管理员可见）
   - 催员列表页面
   - 催员配置表单
   - 密码重置功能
   - IP白名单管理页面
   - 人脸记录查询功能
4. **阶段4**：全量发布

**发布时间：**
- 建议时间：工作日晚上22:00-24:00
- 避免时间：业务高峰期（9:00-18:00）

### 10.2 回滚预案

**回滚触发条件：**
- 接口成功率 < 90%
- 数据库异常导致无法创建催员
- 催员无法登录（IP白名单误拦截）
- 密码重置功能异常

**回滚步骤：**
1. 立即切换开关：`collector.management.enabled = false`
2. 催员登录自动降级：临时禁用IP白名单验证
3. 回滚代码：切换到上一个稳定版本
4. 数据库回滚：如需要，恢复快照

**回滚责任人：**
- 技术负责人：负责决策是否回滚
- 后端负责人：负责接口回滚
- 前端负责人：负责页面回滚
- DBA：负责数据库回滚

### 10.3 数据迁移计划

**现有数据迁移：**
- 如果系统已有催员数据（其他表），需要迁移到collectors表
- 迁移脚本：`scripts/migrate_collectors.sql`
- 迁移步骤：
  1. 备份现有数据
  2. 执行迁移脚本
  3. 验证数据完整性
  4. 更新关联表的外键

**数据校验：**
- 迁移后校验催员总数是否一致
- 校验催员编码和登录ID是否唯一
- 校验所属机构和小组是否正确

---

