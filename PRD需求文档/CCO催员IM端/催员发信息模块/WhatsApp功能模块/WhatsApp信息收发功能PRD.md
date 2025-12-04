# WhatsApp信息收发功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

WhatsApp信息收发功能是CCO催员工作台的核心沟通功能，允许催员通过WhatsApp渠道与客户进行实时沟通。系统支持通过公司WA（企业WhatsApp）和个人WA（个人WhatsApp账号）发送文本、图片、视频、音频等多种类型的消息，并实时跟踪消息发送状态。

**业务痛点**：
- 催员需要实时了解消息是否成功发送、送达和阅读
- 需要区分消息来自哪个WA账号（公司WA或个人WA）
- 需要支持多媒体消息（图片、视频、音频）的发送和接收
- 需要处理各种异常情况（账号被封、掉线、无可用账号等）
- 需要记录发送人信息，支持查看其他催员发送的消息

**预期影响的核心指标**：
- 消息发送成功率：≥95%
- 消息送达率：≥90%
- 消息状态更新延迟：≤5秒
- 错误处理响应时间：≤2秒
- 用户满意度：消息发送流程顺畅，状态显示清晰

---
### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：发送文本消息**
- **入口**：催员工作台 → 选择联系人 → WhatsApp标签页
- **触发时机**：催员需要向客户发送文字消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：输入消息内容 → 选择WA账号 → 点击发送 → 显示发送状态 → 状态更新（已发送→已送达→已读）

**场景2：发送图片消息**
- **入口**：催员工作台 → 选择联系人 → WhatsApp标签页 → 点击"图片"按钮
- **触发时机**：催员需要向客户发送图片（如还款码、合同截图等）
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：选择图片文件 → 预览图片 → 点击发送 → 显示发送状态 → 支持点击放大查看

**场景3：发送视频消息**
- **入口**：催员工作台 → 选择联系人 → WhatsApp标签页 → 上传视频文件
- **触发时机**：催员需要向客户发送视频内容
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：选择视频文件 → 上传 → 显示视频预览 → 点击发送 → 支持在线播放

**场景4：接收音频消息**
- **入口**：催员工作台 → WhatsApp聊天窗口
- **触发时机**：客户发送语音消息到催员
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：接收客户音频消息 → 显示音频播放器 → 点击播放 → 支持播放控制

**场景5：查看消息状态**
- **入口**：催员工作台 → WhatsApp聊天窗口 → 查看已发送消息
- **触发时机**：催员需要了解消息是否被客户查看
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：查看消息气泡右下角状态图标 → 鼠标悬停查看详细状态 → 状态自动更新

**场景6：处理发送失败**
- **入口**：消息发送后显示红色感叹号
- **触发时机**：消息发送失败（网络问题、账号异常等）
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：显示红色感叹号 → 鼠标悬停查看错误提示 → 点击重试或联系管理员

**场景7：查看其他催员发送的消息**
- **入口**：催员工作台 → WhatsApp聊天窗口 → 查看历史消息
- **触发时机**：查看之前其他催员与客户的沟通记录
- **所在页面**：IM面板 - WhatsApp聊天窗口（同一对话窗）
- **流程节点**：查看消息气泡 → 按时间维度显示所有催员的消息 → 显示发送人信息（催员ID/姓名）→ 显示发送时间和WA账号
- **说明**：在同一个案件下，所有催员与该联系人的沟通记录会在同一个对话窗口中按时间顺序展示

**场景8：添加个人WA账号**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域 → 点击"+"号按钮
- **触发时机**：催员需要绑定新的个人WhatsApp账号
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 点击"+"号按钮
  2. 系统验证个人WA账号额度（最多3个）
  3. 调用CWA服务端API"新增WA云设备"
  4. 服务端返回云设备ID和待绑定二维码
  5. 弹窗显示二维码给催员
  6. 催员打开WhatsApp → 设置 → 已连接的设备 → 连接设备
  7. 使用WhatsApp扫描二维码
  8. 绑定成功：Toast提示"绑定成功"，自动关闭弹窗，个人WA区域新增账号头像，默认选中新账号
  9. 绑定失败：显示CWA后端返回的错误信息
- **额度限制**：每个催员最多绑定3个个人WA账号

**场景9：个人WA掉线重新绑定**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域 → 点击掉线的WA账号头像
- **触发时机**：个人WA账号掉线后需要重新绑定
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 系统检测到个人WA账号状态为"unpaired"（掉线）
  2. 在该账号头像上显示掉线标识（半透明遮罩 + 警告图标）
  3. 鼠标悬停在掉线账号上显示提示："账号已经掉线，点击后重新绑定或绑定新账号"
  4. 点击掉线的账号头像
  5. 弹出选择对话框："重新绑定此账号" 或 "绑定新账号"
  6. 选择"重新绑定此账号"：调用CWA API重新生成二维码，使用原云设备ID
  7. 选择"绑定新账号"：调用CWA API新增云设备，生成新的云设备ID和二维码
  8. 显示二维码弹窗，催员扫码绑定
  9. 绑定成功：更新账号状态，移除掉线标识，Toast提示"重新绑定成功"
  10. 绑定失败：显示错误信息

**场景10：智能Chatting状态判断（优化）**
- **入口**：催员工作台 → 选择联系人（本人）
- **触发时机**：催员点击联系人（本人），需要判断该手机号是否注册WhatsApp
- **所在页面**：IM面板 - 联系人列表
- **业务背景**：直接调用CWA的checkPhone接口会造成频繁查询，触发WhatsApp官方服务端风控，因此需要通过缓存和历史消息记录来减少API调用
- **流程节点**（优先级顺序）：
  1. 点击联系人（本人）
  2. 执行查询1：查询`wa_relation`表
     - 条件：该手机号 + chatting状态 + 查询时间距今≤7天
     - 范围：允许跨甲方条线查询（全局缓存）
     - 结果：如果命中，直接使用缓存的chatting状态，不再继续查询
  3. 执行查询2：查询`message`表
     - 条件：该手机号有WhatsApp回复记录 + 回复时间距今≤7天
     - 范围：允许跨甲方条线查询
     - 结果：如果命中，判定为chatting状态
     - 操作：在`wa_relation`表中新增一条chatting记录（用于后续缓存）
  4. 执行查询3：调用CWA checkPhone接口
     - 条件：查询1和查询2都未命中
     - 操作：调用CWA的checkPhone接口进行实时查询
     - 结果：将查询结果存入`wa_relation`表（缓存）
- **优化效果**：大幅减少对CWA checkPhone接口的调用频率，降低WhatsApp官方风控风险

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用场景 |
|---------|---------|---------|---------|
| 催员 | Collector | 高效发送消息，了解消息状态 | 日常催收沟通 |
| 客户 | Customer | 接收消息，查看内容 | 接收催收通知 |

---

### 3. 关键业务流程（Business Flow）

#### 3.1 消息发送流程

```
催员输入消息内容
    ↓
选择WA账号（公司WA/个人WA）
    ↓
点击发送按钮
    ↓
前端验证：消息内容、WA账号状态、联系人信息
    ↓
[验证失败] → 显示错误提示，阻止发送
    ↓
[验证成功] → 调用发送消息API
    ↓
显示"发送中"状态（钟表图标）
    ↓
后端处理：验证WA账号状态、调用WhatsApp API
    ↓
[发送失败] → 显示红色感叹号 + 错误提示
    ↓
[发送成功] → 更新状态为"已发送"（单灰色对勾）
    ↓
轮询查询消息状态
    ↓
状态更新：已送达（双灰色对勾）→ 已读（双蓝色对勾）
```

#### 3.2 消息状态更新流程

```
消息发送成功
    ↓
初始状态：单灰色对勾（✓）- 已发送到服务器
    ↓
轮询查询状态（每5秒）
    ↓
[状态：已送达] → 更新为双灰色对勾（✓✓）
    ↓
[状态：已读] → 更新为双蓝色对勾（✓✓，蓝色）
    ↓
[状态：失败] → 更新为红色感叹号（!）
    ↓
停止轮询（已读或失败状态）
```

#### 3.3 消息接收流程

```
客户发送消息到WhatsApp
    ↓
WhatsApp Webhook通知后端
    ↓
后端处理：解析消息内容、识别消息类型（文本/图片/视频/音频）
    ↓
保存消息到数据库
    ↓
前端轮询或WebSocket推送新消息
    ↓
显示在聊天窗口（左侧气泡）
    ↓
根据消息类型渲染：
    - 文本：直接显示内容
    - 图片：显示缩略图，支持点击放大
    - 视频：显示视频播放器，支持在线播放
    - 音频：显示音频播放器，支持播放控制
    ↓
显示未读消息提示
```

#### 3.4 添加个人WA账号流程

```
催员点击个人WA区域的"+"号
    ↓
前端检查：个人WA账号数量是否已达上限（3个）
    ↓
[已达上限] → 显示提示："Maximum 3 personal WhatsApp accounts allowed."
    ↓
[未达上限] → 调用CWA API：POST /api/v1/wa/devices/create
    ↓
请求参数：{ collectorId: "催员ID", deviceType: "personal" }
    ↓
CWA后端处理：
    - 创建新的云设备
    - 生成云设备ID
    - 生成待绑定二维码
    ↓
返回响应：{ deviceId: "设备ID", qrCode: "二维码Base64" }
    ↓
前端显示绑定弹窗：
    - 标题："扫码绑定WhatsApp账号"
    - 显示二维码图片
    - 显示绑定说明："请使用WhatsApp扫描二维码\n打开WhatsApp → 设置 → 已连接的设备 → 连接设备"
    - 提供"刷新二维码"按钮
    ↓
催员使用WhatsApp扫描二维码
    ↓
前端轮询绑定状态：GET /api/v1/wa/devices/{deviceId}/status（每2秒查询一次）
    ↓
[绑定成功：status = "paired"]
    - Toast提示："绑定成功"（Binding successful）
    - 关闭绑定弹窗
    - 刷新个人WA账号列表
    - 新增账号头像显示在个人WA区域
    - 默认选中新绑定的账号
    - 停止轮询
    ↓
[绑定失败：status = "failed"]
    - 显示CWA返回的错误信息
    - 提供"重试"按钮
    ↓
[超时：轮询60次（120秒）后仍未成功]
    - 显示提示："Binding timeout. Please try again."
    - 关闭弹窗
    - 停止轮询
```

#### 3.5 个人WA掉线重新绑定流程

```
系统后台定期检测WA账号状态（每30秒）
    ↓
检测到个人WA账号状态变为"unpaired"
    ↓
更新账号状态到数据库
    ↓
前端获取WA账号列表时发现掉线账号
    ↓
在掉线账号头像上显示掉线标识：
    - 半透明灰色遮罩（opacity: 0.5）
    - 右上角显示红色警告图标
    - 鼠标悬停提示："账号已经掉线，点击后重新绑定或绑定新账号"
    ↓
催员点击掉线的账号头像
    ↓
显示操作选择弹窗：
    - 标题："WhatsApp账号已掉线"
    - 选项1："重新绑定此账号"（推荐）
    - 选项2："绑定新账号"
    - 取消按钮
    ↓
[选择：重新绑定此账号]
    - 调用API：POST /api/v1/wa/devices/{deviceId}/rebind
    - 使用原云设备ID
    - 生成新的二维码
    - 显示二维码绑定弹窗
    - 催员扫码绑定
    - 绑定成功后更新账号状态为"paired"
    - 移除掉线标识
    - Toast提示："重新绑定成功"（Rebinding successful）
    ↓
[选择：绑定新账号]
    - 检查个人WA账号数量（是否已达上限）
    - 如果未达上限，执行"添加个人WA账号流程"
    - 如果已达上限，提示："Maximum 3 personal WhatsApp accounts allowed. Please unbind an existing account first."
    ↓
[取消]
    - 关闭弹窗
    - 账号保持掉线状态
```

#### 3.6 智能Chatting状态判断流程（优化）

**业务背景**：
- 每次点击联系人（本人）时，需要判断该手机号是否注册了WhatsApp
- 直接调用CWA的checkPhone接口会导致频繁查询，触发WhatsApp官方服务端风控
- 通过缓存机制和历史消息记录来减少API调用，降低风控风险

**优化策略**：
- 引入`wa_relation`表作为缓存层，记录手机号与WhatsApp注册状态的关系
- 引入7天有效期机制，避免使用过期数据
- 优先使用缓存和历史消息记录，仅在必要时才调用checkPhone接口

```
催员点击联系人（本人）
    ↓
触发Chatting状态判断
    ↓
==== 查询1：检查wa_relation表缓存 ====
    ↓
查询条件：
    - 手机号 = 联系人手机号
    - status = 'chatting'
    - 查询时间距今 ≤ 7天（DATEDIFF(NOW(), checked_at) <= 7）
    - 允许跨甲方条线查询（全局缓存）
    ↓
[命中缓存]
    → 直接使用缓存的chatting状态
    → 更新last_used_at字段（记录最后使用时间）
    → 结束流程，不再继续查询
    ↓
[未命中缓存] → 继续查询2
    ↓
==== 查询2：检查message表历史回复记录 ====
    ↓
查询条件：
    - 手机号 = 联系人手机号
    - channel = 'whatsapp'
    - sender_type = 'customer'（客户回复的消息）
    - 回复时间距今 ≤ 7天（DATEDIFF(NOW(), sent_at) <= 7）
    - 允许跨甲方条线查询
    ↓
[命中消息记录]
    → 判定为chatting状态（有WhatsApp回复记录，说明已注册）
    → 在wa_relation表中插入新记录：
        - phone_number: 联系人手机号
        - status: 'chatting'
        - checked_at: NOW()
        - source: 'message_history'
        - tenant_id: NULL（跨甲方）
    → 返回chatting状态
    → 结束流程
    ↓
[未命中消息记录] → 继续查询3
    ↓
==== 查询3：调用CWA checkPhone接口（实时查询）====
    ↓
调用API：POST /api/v1/cwa/checkPhone
请求参数：{ phoneNumber: "联系人手机号" }
    ↓
CWA返回查询结果：
    - registered: true/false（是否注册WhatsApp）
    - chatting: true/false（是否可以聊天）
    ↓
将结果存入wa_relation表：
    - phone_number: 联系人手机号
    - status: chatting ? 'chatting' : 'not_registered'
    - checked_at: NOW()
    - source: 'cwa_api'
    - tenant_id: NULL（跨甲方）
    ↓
返回查询结果
    ↓
结束流程
```

**数据表设计**：

**wa_relation表结构**：
```sql
CREATE TABLE wa_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone_number VARCHAR(20) NOT NULL COMMENT '手机号',
  status VARCHAR(20) NOT NULL COMMENT '状态：chatting/not_registered',
  checked_at DATETIME NOT NULL COMMENT '查询时间',
  last_used_at DATETIME COMMENT '最后使用时间',
  source VARCHAR(20) NOT NULL COMMENT '来源：cwa_api/message_history',
  tenant_id BIGINT COMMENT '甲方ID（NULL表示跨甲方）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_phone_status (phone_number, status, checked_at),
  INDEX idx_checked_at (checked_at)
) COMMENT='WhatsApp手机号关系缓存表';
```

**缓存清理策略**：
- 定时任务：每天清理checked_at距今 > 30天的记录（保留30天历史）
- 避免表数据无限增长

**优化效果**：
- 第一次查询：调用CWA API（无缓存）
- 7天内重复查询：直接使用缓存，API调用量减少约90%+
- 有消息回复的联系人：自动缓存，进一步减少API调用

#### 3.4 错误处理流程

```
发送消息时检测到错误
    ↓
判断错误类型
    ↓
[WA账号被封/掉线] → 显示："WhatsApp online status is abnormal. Please refresh the page."
    ↓
[无可用WA账号] → 显示："No available WhatsApp account. Please contact administrator."
    ↓
[网络连接失败] → 显示："Network connection failed. Please check your network and try again."
    ↓
[消息内容违规] → 显示："Message content violates policy. Please modify and try again."
    ↓
[接收方号码无效] → 显示："Recipient phone number is invalid. Please verify the number."
    ↓
[超过每日每案件限制] → 显示："Daily limit per case exceeded. You have sent {count} messages to this case today."
    ↓
[超过每日每联系人限制] → 显示："Daily limit per contact exceeded. You have sent {count} messages to this contact today."
    ↓
[发送时间间隔限制] → 显示："Send interval limit. Please wait {seconds} seconds before sending again."
    ↓
记录错误日志
    ↓
前端显示错误提示（红色感叹号 + 悬浮提示）
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 消息发送规则

**消息内容规则**：
- 文本消息：必填，最大长度1000字符
- 图片消息：支持格式：JPG、PNG、GIF，最大大小10MB
- 视频消息：支持接收，支持格式：MP4、MOV，最大大小50MB
- 音频消息：支持接收，支持格式：MP3、WAV、OGG、AAC，最大大小10MB
- 催员只能发送文本和图片消息，视频和音频消息仅支持接收和播放

**WA账号选择规则**：
- 公司WA：不显示具体账号ID，统一显示为"公司WA"
- 个人WA：显示账号ID或名称，格式为"个人WA（账号ID）"
- 默认选择：优先使用公司WA，如果公司WA不可用，自动切换到个人WA
- 账号切换：发送前可以手动切换WA账号

**个人WA账号管理规则**：
- **账号数量限制**：每个催员最多绑定3个个人WA账号
- **额度显示**：显示格式为"可用数/总数"，例如："2/3"（前面是当前可用的，后面是今日绑定的总数）
- **绑定超限提示**：达到3个后，"+"号按钮点击时提示："Maximum 3 personal WhatsApp accounts allowed."
- **账号状态**：
  - `paired`：已绑定，正常可用
  - `unpaired`：已掉线，需要重新绑定
  - `binding`：绑定中（扫码后等待确认）
- **掉线账号处理**：
  - 显示半透明灰色遮罩（opacity: 0.5）
  - 右上角显示红色警告图标
  - 不可用于发送消息
  - 点击后可选择重新绑定或绑定新账号
- **账号解绑**：暂不支持手动解绑，仅通过掉线自动标记

**Chatting状态判断规则（优化）**：
- **缓存有效期**：7天（DATEDIFF(NOW(), checked_at) <= 7）
- **查询优先级**：
  1. 优先使用`wa_relation`表缓存（7天内）
  2. 其次使用`message`表历史回复记录（7天内）
  3. 最后调用CWA checkPhone接口（实时查询）
- **跨甲方查询**：`wa_relation`表和`message`表查询允许跨甲方条线，提高缓存命中率
- **缓存来源**：
  - `cwa_api`：来自CWA checkPhone接口查询
  - `message_history`：来自历史消息回复记录
- **缓存更新**：
  - 命中缓存时更新`last_used_at`字段（记录最后使用时间）
  - 不更新`checked_at`字段（保持原始查询时间）
- **缓存清理**：定时任务每天清理checked_at距今 > 30天的记录
- **风控优化目标**：减少90%+的CWA checkPhone API调用量

**发送人信息规则**：
- 当前催员发送：显示当前催员的ID或姓名
- 其他催员发送：显示发送消息的催员ID或姓名
- 显示位置：消息气泡下方或元数据区域

**发送时间规则**：
- 显示格式：HH:mm:ss（24小时制）
- 显示位置：消息气泡右下角，与状态图标并列
- 时间来源：服务器时间，确保准确性

#### 4.2 消息状态显示规则

**状态图标定义**：
- **钟表图标（⏰）**：消息正在发送中，尚未成功发送或送达
- **单灰色对勾（✓）**：消息已从设备成功发送到WhatsApp服务器
- **双灰色对勾（✓✓）**：消息已成功送达对方的设备，但对方尚未打开或阅读
- **双蓝色对勾（✓✓，蓝色）**：对方已打开并阅读消息
- **红色感叹号（!）**：消息发送失败，鼠标悬停显示错误提示

**状态更新规则**：
- 状态轮询：每5秒查询一次消息状态（可配置）
- 状态持久化：状态更新后保存到数据库
- 状态显示：仅对催员发送的消息显示状态图标
- 状态提示：鼠标悬停在状态图标上显示详细状态文本

**错误提示规则**：
- 红色感叹号：显示在消息气泡右下角
- 悬浮提示：鼠标悬停显示完整错误信息
- 错误信息格式："WhatsApp's online status is abnormal. Messages may have failed to send."
- 错误重试：支持点击重试发送（如果错误可恢复）

#### 4.3 媒体消息规则

**图片消息规则**：
- 支持预览：点击图片可以放大查看
- 支持缩放：支持鼠标滚轮缩放
- 显示尺寸：聊天窗口内最大200x200px，放大后支持全屏查看
- 加载优化：支持懒加载，提升性能

**视频消息规则**：
- 支持在线播放：点击视频可以在聊天窗口内播放
- 播放控制：支持播放/暂停、进度条、音量控制
- 视频预览：显示视频第一帧作为缩略图
- 播放尺寸：聊天窗口内最大400x300px

**音频消息规则**：
- 支持在线播放：点击播放按钮开始播放
- 播放控制：支持播放/暂停、进度条、音量控制
- 显示格式：显示音频图标 + "语音消息"文字
- 播放时长：显示音频时长（如果可用）

#### 4.4 WA账号状态规则

**账号状态类型**：
- **正常（paired）**：账号正常，可以发送消息
- **异常（unpaired）**：账号被封或掉线，无法发送消息
- **未知（unknown）**：状态未知，需要刷新获取最新状态

**状态检查规则**：
- 发送前检查：发送消息前必须检查WA账号状态
- 状态缓存：缓存WA账号状态，减少API调用
- 状态刷新：每30秒刷新一次WA账号状态（可配置）
- 状态提示：账号异常时显示明确提示

**账号选择规则**：
- 优先使用：优先使用公司WA账号
- 自动切换：如果公司WA不可用，自动切换到可用的个人WA
- 手动切换：催员可以手动切换WA账号
- 切换提示：切换账号时显示确认提示

#### 4.5 范围边界

**本次需求范围内**：
- ✅ WhatsApp文本消息发送和接收
- ✅ WhatsApp图片消息发送和接收（预览、放大查看）
- ✅ WhatsApp视频消息接收（在线播放、播放控制，仅接收不发送）
- ✅ WhatsApp音频消息接收（播放控制，仅接收不发送）
- ✅ 消息发送状态显示（单灰、双灰、双蓝、红色感叹号、钟表图标）
- ✅ 发送人信息显示（催员ID/姓名，支持其他催员消息在同一对话窗按时间展示）
- ✅ WA账号标识（公司WA/个人WA+账号ID）
- ✅ 发送时间显示
- ✅ 错误处理机制（WA状态异常、无可用账号、网络错误、渠道触达限制等）
- ✅ 渠道触达限制校验（每日每案件、每日每联系人、发送时间间隔）
- ✅ 消息状态轮询更新
- ✅ 所有错误提示使用英文
- ✅ 个人WA账号添加（扫码绑定，最多3个）
- ✅ 个人WA账号掉线检测和重新绑定
- ✅ 二维码绑定状态轮询
- ✅ 绑定超时处理（120秒）
- ✅ **智能Chatting状态判断优化**（三级缓存机制：wa_relation表 → message表 → CWA API）
- ✅ **wa_relation表缓存机制**（7天有效期，跨甲方查询）
- ✅ **CWA API调用优化**（减少90%+调用量，降低WhatsApp风控风险）

**本次需求范围外（待实现）**：
- ⏳ 消息内容敏感词过滤（后续实现，错误码已预留）
- ❌ 催员发送视频消息（仅支持接收）
- ❌ 催员发送音频消息（仅支持接收）
- ❌ 消息撤回功能
- ❌ 消息转发功能
- ❌ 消息搜索功能
- ❌ 群聊功能
- ❌ 消息模板变量替换（已在其他功能中实现）
- ❌ 消息已读回执的详细统计
- ❌ 消息发送的批量操作

---

### 5. 合规与风控要求（Compliance & Risk Control）

#### 5.1 消息内容安全

**内容审核**（⏳ 待实现）：
- 敏感词过滤：发送前检查消息内容是否包含敏感词
- 违规拦截：发现违规内容时阻止发送并提示
- 内容记录：所有消息内容记录到数据库，便于审计
- **错误提示**：`Message content violates policy. Please modify and try again.`

**渠道触达限制**（✅ 已实现）：
所有WhatsApp消息发送都会经过**CCO渠道触达限制系统**进行校验，确保发送行为符合配置规则。

**限制规则**（通过后台配置，详见`渠道限制配置`功能）：
1. **每日每案件限制（daily_limit_per_case）**：
   - 默认值：200条/天
   - 说明：同一案件在WhatsApp渠道每天最多发送的消息数
   - 超限提示：`Daily limit per case exceeded. You have sent {count} messages to this case today.`

2. **每日每联系人限制（daily_limit_per_contact）**：
   - 默认值：100条/天
   - 说明：同一联系人在WhatsApp渠道每天最多接收的消息数
   - 超限提示：`Daily limit per contact exceeded. You have sent {count} messages to this contact today.`

3. **发送时间间隔（send_interval）**：
   - 默认值：20秒
   - 说明：相邻两次消息发送之间的最小时间间隔
   - 超限提示：`Send interval limit. Please wait {seconds} seconds before sending again.`

**配置规则说明**：
- 渠道限制配置按**甲方（tenant）+ 渠道（channel）+ 队列（queue）**维度管理
- 支持设置为"不限制"（unlimited = true）
- 限制规则在发送消息前由后端校验，校验失败时返回错误码并阻止发送
- 前端在发送按钮旁显示当前渠道限制信息（已发送数/最大限制数）

#### 5.2 错误日志记录

**日志记录规则**：
- 记录所有发送失败的错误
- 记录WA账号状态异常事件
- 记录消息状态更新异常
- 日志保留30天（可配置）

**日志内容**：
- 错误类型：WA状态异常、网络错误、内容违规等
- 错误详情：错误码、错误信息、堆栈信息
- 上下文信息：催员ID、联系人ID、消息内容（脱敏）、WA账号ID

#### 5.3 数据隐私

**消息内容处理**：
- 消息内容加密存储（可选）
- 消息内容访问需要权限验证
- 消息内容不记录到前端日志

**用户信息保护**：
- 催员ID和姓名脱敏显示（可选）
- 客户手机号脱敏显示（后4位）

---

### 6. 资金路径与结算规则（Funding Flow & Settlement）

**不涉及资金流**，本节不适用。

---

### 7. 数据字段与口径（Data Definition）

#### 7.1 消息发送请求字段

| 字段名 | 类型 | 必填 | 说明 | 来源 |
|--------|------|------|------|------|
| contactId | Integer | 是 | 联系人ID | 前端选择 |
| messageType | String | 是 | 消息类型：text/image/video/audio | 前端选择 |
| content | String | 是 | 消息内容（文本或文件URL） | 用户输入/上传 |
| waAccountType | String | 是 | WA账号类型：platform/personal | 前端选择 |
| waAccountId | String | 否 | WA账号ID（个人WA必填） | 前端选择 |
| senderId | String | 是 | 发送人催员ID | 系统获取 |

#### 7.2 消息响应字段

| 字段名 | 类型 | 说明 | 更新频率 |
|--------|------|------|----------|
| messageId | String | 消息ID | 发送时生成 |
| status | String | 消息状态：sending/sent/delivered/read/failed | 实时更新 |
| sentAt | String | 发送时间（ISO 8601格式） | 发送时记录 |
| deliveredAt | String | 送达时间（ISO 8601格式） | 送达时更新 |
| readAt | String | 阅读时间（ISO 8601格式） | 阅读时更新 |
| waAccountType | String | WA账号类型 | 发送时记录 |
| waAccountId | String | WA账号ID | 发送时记录 |
| senderId | String | 发送人催员ID | 发送时记录 |
| senderName | String | 发送人催员姓名 | 发送时记录 |

#### 7.3 消息状态枚举定义

| 状态值 | 英文标识 | 显示图标 | 说明 |
|--------|---------|---------|------|
| sending | SENDING | 钟表图标（⏰） | 消息正在发送中 |
| sent | SENT | 单灰色对勾（✓） | 消息已发送到服务器 |
| delivered | DELIVERED | 双灰色对勾（✓✓） | 消息已送达对方设备 |
| read | READ | 双蓝色对勾（✓✓，蓝色） | 消息已被对方阅读 |
| failed | FAILED | 红色感叹号（!） | 消息发送失败 |

#### 7.4 错误码列表（Error Codes）

| HTTP状态码 | 业务错误码 (code) | 错误码标识 (errorCode) | 英文错误信息 (message) | 中文说明 | 适用场景 |
|-----------|-----------------|---------------------|---------------------|---------|---------|
| 400 | 400 | INVALID_REQUEST | Invalid request parameters | 请求参数错误 | 请求参数缺失或格式错误 |
| 400 | 400 | INVALID_MESSAGE_TYPE | Invalid message type | 消息类型无效 | 不支持的消息类型 |
| 400 | 400 | INVALID_CONTENT | Message content is invalid | 消息内容无效 | 消息内容为空或格式错误 |
| 400 | 400 | INVALID_RECIPIENT | Recipient phone number is invalid. Please verify the number. | 接收方号码无效 | 联系人手机号格式错误或不存在 |
| 400 | 400 | MAX_PERSONAL_WA_REACHED | Maximum 3 personal WhatsApp accounts allowed. | 个人WA账号已达上限 | 已绑定3个个人WA账号 |
| 403 | 403 | DAILY_LIMIT_PER_CASE_EXCEEDED | Daily limit per case exceeded. You have sent {count} messages to this case today. | 超过每日每案件限制 | 案件当天发送消息数超过配置限制 |
| 403 | 403 | DAILY_LIMIT_PER_CONTACT_EXCEEDED | Daily limit per contact exceeded. You have sent {count} messages to this contact today. | 超过每日每联系人限制 | 联系人当天接收消息数超过配置限制 |
| 403 | 403 | SEND_INTERVAL_LIMIT | Send interval limit. Please wait {seconds} seconds before sending again. | 发送时间间隔限制 | 距离上次发送时间未达到间隔要求 |
| 403 | 403 | CONTENT_VIOLATION | Message content violates policy. Please modify and try again. | 消息内容违规（待实现） | 消息内容包含敏感词或违规内容 |
| 500 | 500 | WA_ACCOUNT_UNPAIRED | WhatsApp online status is abnormal. Please refresh the page. | WhatsApp在线状态异常 | WA账号被封或掉线，CWA未获取到最新unpaired状态 |
| 500 | 500 | NO_AVAILABLE_WA_ACCOUNT | No available WhatsApp account. Please contact administrator. | 暂无可用的WhatsApp账号 | 没有可用的WA账号（公司WA和个人WA都不可用） |
| 500 | 500 | NETWORK_ERROR | Network connection failed. Please check your network and try again. | 网络连接失败 | 网络连接异常 |
| 500 | 500 | WA_API_ERROR | WhatsApp API error | WhatsApp API调用失败 | WhatsApp Business API返回错误 |
| 500 | 500 | WA_DEVICE_CREATE_FAILED | Failed to create WhatsApp cloud device. | 创建WA云设备失败 | CWA创建云设备失败 |
| 500 | 500 | WA_DEVICE_BIND_TIMEOUT | Binding timeout. Please try again. | 绑定超时 | 扫码绑定超时（超过120秒） |
| 500 | 500 | WA_DEVICE_BIND_FAILED | Failed to bind WhatsApp device. | 绑定失败 | 扫码绑定失败 |
| 500 | 500 | INTERNAL_SERVER_ERROR | Internal server error | 服务器内部错误 | 服务器处理异常 |

**错误响应格式**：
```json
{
  "code": 500,
  "message": "Whatsapp online status is abnormal. Please refresh the page.",
  "errorCode": "WA_ACCOUNT_UNPAIRED",
  "data": {
    "waAccountId": "platform_1",
    "waAccountType": "platform",
    "suggestedAction": "refresh_page"
  }
}
```

#### 7.5 消息数据结构

**发送的消息对象**：
```json
{
  "id": "msg_123456",
  "contactId": 1,
  "type": "text",
  "content": "您好，请问您什么时候可以还款？",
  "senderType": "collector",
  "senderId": "collector001",
  "senderName": "张三",
  "channel": "whatsapp",
  "status": "read",
  "sentAt": "2025-01-20T10:30:25Z",
  "deliveredAt": "2025-01-20T10:30:28Z",
  "readAt": "2025-01-20T10:35:12Z",
  "waAccountType": "platform",
  "waAccountId": null,
  "tool": "公司WA"
}
```

**接收的消息对象**：
```json
{
  "id": "msg_123457",
  "contactId": 1,
  "type": "text",
  "content": "我这两天就还",
  "senderType": "customer",
  "senderId": null,
  "senderName": "客户本人",
  "channel": "whatsapp",
  "status": null,
  "sentAt": "2025-01-20T10:35:12Z",
  "deliveredAt": null,
  "readAt": null,
  "waAccountType": null,
  "waAccountId": null,
  "tool": null
}
```

#### 7.6 WA账号数据字段

| 字段名 | 类型 | 说明 | 更新频率 |
|--------|------|------|----------|
| deviceId | String | WA云设备ID | 创建时生成 |
| collectorId | String | 催员ID | 创建时绑定 |
| deviceType | String | 设备类型：platform/personal | 创建时设置 |
| status | String | 账号状态：paired/unpaired/binding | 实时更新 |
| qrCode | String | 绑定二维码（Base64） | 创建/重新绑定时生成 |
| phoneNumber | String | WhatsApp手机号（绑定后获取） | 绑定成功后更新 |
| displayName | String | 显示名称 | 绑定成功后更新 |
| avatar | String | 头像URL | 绑定成功后更新 |
| pairedAt | String | 绑定时间（ISO 8601格式） | 绑定成功时记录 |
| unpairedAt | String | 掉线时间（ISO 8601格式） | 掉线时记录 |

#### 7.7 wa_relation表数据字段

| 字段名 | 类型 | 必填 | 说明 | 索引 |
|--------|------|------|------|------|
| id | BIGINT | 是 | 主键ID | PRIMARY KEY |
| phone_number | VARCHAR(20) | 是 | 手机号（国际格式） | idx_phone_status |
| status | VARCHAR(20) | 是 | 状态：chatting/not_registered | idx_phone_status |
| checked_at | DATETIME | 是 | 查询时间（用于判断缓存有效期） | idx_phone_status, idx_checked_at |
| last_used_at | DATETIME | 否 | 最后使用时间（缓存命中时更新） | - |
| source | VARCHAR(20) | 是 | 来源：cwa_api/message_history | - |
| tenant_id | BIGINT | 否 | 甲方ID（NULL表示跨甲方） | - |
| created_at | DATETIME | 是 | 创建时间 | - |
| updated_at | DATETIME | 是 | 更新时间 | - |

**索引说明**：
- `idx_phone_status (phone_number, status, checked_at)`：联合索引，支持快速查询缓存
- `idx_checked_at (checked_at)`：单独索引，支持定时清理任务

#### 7.8 统计口径

- **消息发送成功率**：发送成功的消息数 / 总发送消息数（按自然日统计）
- **消息送达率**：送达的消息数 / 发送成功的消息数（按自然日统计）
- **消息阅读率**：被阅读的消息数 / 送达的消息数（按自然日统计）
- **平均状态更新延迟**：状态更新时间 - 实际状态变更时间（按消息统计）
- **错误率**：发送失败的消息数 / 总发送消息数（按自然日统计）
- **个人WA绑定成功率**：绑定成功的个人WA数 / 绑定尝试次数（按自然日统计）
- **个人WA掉线率**：掉线的个人WA数 / 总绑定个人WA数（按自然日统计）
- **Chatting状态缓存命中率**：从缓存获取的查询数 / 总查询数（按自然日统计）
- **CWA API调用减少率**：(总查询数 - CWA API调用数) / 总查询数（按自然日统计）

---

### 8. 交互与信息展示（UX & UI Brief）

#### 8.1 消息气泡样式

**发送的消息（右侧，绿色气泡）**：
- 背景色：#DCF8C6（浅绿色）
- 文字颜色：#000000（黑色）
- 对齐方式：右对齐
- 圆角：8px
- 最大宽度：70%

**接收的消息（左侧，灰色气泡）**：
- 背景色：#FFFFFF（白色）
- 文字颜色：#000000（黑色）
- 对齐方式：左对齐
- 圆角：8px
- 最大宽度：70%

#### 8.2 消息元数据显示

**元数据区域**（消息气泡下方）：
- 显示内容：WA账号标识、发送时间、状态图标
- 字体大小：11px
- 字体颜色：#8696a0（灰色）
- 布局：左对齐，状态图标在右侧

**WA账号标识显示规则**：
- 公司WA：显示"公司WA"（不显示账号ID）
- 个人WA：显示"个人WA（账号ID）"或"个人WA（账号名称）"

**发送人信息显示**（如果来自其他催员）：
- 显示位置：消息气泡上方或元数据区域
- 显示格式："来自：催员姓名（催员ID）"
- 字体大小：10px
- 字体颜色：#8696a0（灰色）

#### 8.6 个人WA账号显示

**个人WA区域布局**：
- 区域标题："个人WA"
- 额度显示："2/3"（可用数/总数）
- 说明图标：悬停显示"前面的数字是当前可用的，后面的是今日绑定的wa的总数字"

**账号头像显示**：
- 头像大小：36x36px
- 头像样式：圆形，带2px边框
- 正常状态：显示WhatsApp头像或默认用户图标
- 选中状态：绿色边框（#25D366），带阴影
- 掉线状态：
  - 半透明灰色遮罩（opacity: 0.5）
  - 右上角显示红色警告图标（16x16px）
  - 不可点击选中（cursor: not-allowed）

**添加按钮样式**：
- 大小：36x36px
- 背景色：#f5f7fa
- 边框：2px虚线，颜色#dcdfe6
- 图标：加号（+），颜色#909399
- 悬停状态：
  - 背景色：#e8f5e9
  - 边框：2px实线，颜色#25D366
  - 图标颜色：#25D366

#### 8.7 二维码绑定弹窗

**弹窗标题**："扫码绑定WhatsApp账号"

**弹窗内容**：
- 二维码显示区域：
  - 尺寸：200x200px
  - 背景：白色
  - 边框：2px实线，颜色#25D366
  - 圆角：8px
- 说明文字：
  - 主标题："请使用WhatsApp扫描二维码"
  - 副标题："打开WhatsApp → 设置 → 已连接的设备 → 连接设备"
  - 字体大小：14px（主标题）/ 12px（副标题）
  - 字体颜色：#606266（主标题）/ #909399（副标题）
- 刷新按钮：
  - 文字："刷新二维码"
  - 宽度：100%
  - 类型：主要按钮（蓝色）
  - 图标：刷新图标

**绑定中状态**：
- 显示加载动画（Spinner）
- 提示文字："绑定中，请稍候..."

**绑定成功**：
- Toast提示："绑定成功"（Binding successful）
- 自动关闭弹窗（延迟500ms）
- 刷新个人WA账号列表
- 默认选中新绑定的账号

**绑定失败**：
- 显示错误信息（CWA返回的错误）
- 提供"重试"按钮

#### 8.8 掉线账号重新绑定

**掉线标识显示**：
- 在账号头像上显示半透明灰色遮罩
- 右上角显示红色警告图标（el-icon Warning）
- 图标大小：16x16px
- 图标背景：白色圆形（18x18px）

**悬停提示**：
- 触发：鼠标悬停在掉线账号头像上
- 内容："账号已经掉线，点击后重新绑定或绑定新账号"
- 样式：Tooltip组件

**点击掉线账号弹窗**：
- 标题："WhatsApp账号已掉线"
- 内容：
  - 提示文字："此WhatsApp账号已掉线，请选择操作："
  - 选项1按钮："重新绑定此账号"（推荐，主要按钮）
  - 选项2按钮："绑定新账号"（普通按钮）
  - 取消按钮
- 布局：垂直排列，按钮居中

#### 8.3 状态图标显示规则

**状态图标位置**：
- 位置：消息气泡右下角，与发送时间并列
- 图标大小：16x16px
- 间距：与发送时间间隔4px

**状态图标样式**：
- **钟表图标**：灰色（#8696a0），动画旋转
- **单灰色对勾**：灰色（#8696a0）
- **双灰色对勾**：灰色（#8696a0）
- **双蓝色对勾**：蓝色（#25D366，WhatsApp绿色）
- **红色感叹号**：红色（#F56C6C）

**状态提示**：
- 鼠标悬停：显示状态文本提示
- 提示内容：
  - sending: "发送中..."
  - sent: "已发送"
  - delivered: "已送达"
  - read: "已读"
  - failed: "发送失败" + 错误详情

#### 8.4 错误提示样式

**红色感叹号显示**：
- 位置：消息气泡右下角
- 颜色：红色（#F56C6C）
- 大小：16x16px

**悬浮提示**：
- 触发：鼠标悬停在红色感叹号上
- 显示内容：完整错误信息
- 样式：Tooltip组件，背景色#333，文字颜色#FFF
- 示例："WhatsApp's online status is abnormal. Messages may have failed to send."

**错误提示弹窗**（发送时错误）：
- 触发：发送消息时检测到错误
- 显示方式：ElMessage.error() 或 ElMessageBox.alert()
- 样式：红色警告图标 + 错误信息
- 示例："Whatsapp online status is abnormal. Please refresh the page."

#### 8.5 媒体消息显示

**图片消息**：
- 显示尺寸：最大200x200px（聊天窗口内）
- 点击放大：支持点击放大查看，全屏显示
- 加载状态：显示加载动画
- 错误处理：加载失败显示占位图

**视频消息**：
- 显示尺寸：最大400x300px（聊天窗口内）
- 视频预览：显示第一帧作为缩略图
- 播放控制：播放/暂停按钮、进度条、音量控制
- 全屏播放：支持全屏播放

**音频消息**：
- 显示格式：音频图标 + "语音消息"文字 + 播放控制
- 播放控制：播放/暂停按钮、进度条、音量控制
- 时长显示：显示音频时长（如果可用）

#### 8.6 交互细节

**发送消息**：
- 快捷键：Ctrl+Enter 发送消息
- 发送按钮：绿色按钮，带发送图标
- 发送中状态：按钮显示"发送中..."，禁用状态

**消息滚动**：
- 新消息到达：自动滚动到底部
- 手动滚动：用户手动滚动时，不自动滚动
- 滚动动画：平滑滚动效果

**消息加载**：
- 初始加载：加载最近50条消息（可配置）
- 滚动加载：向上滚动时加载更早的消息
- 加载状态：显示加载动画

---

### 9. 配置项与运营开关（Config & Operation Switches）

#### 9.1 消息发送配置

| 配置项 | 配置路径 | 默认值 | 说明 |
|--------|---------|--------|------|
| message.maxLength | 硬编码 | 1000 | 文本消息最大长度（字符），不可配置 |
| message.imageMaxSize | application.yml | 10485760 | 图片最大大小（字节，10MB） |
| message.videoMaxSize | application.yml | 52428800 | 视频最大大小（字节，50MB，仅接收） |
| message.audioMaxSize | application.yml | 10485760 | 音频最大大小（字节，10MB，仅接收） |

#### 9.2 状态轮询配置

| 配置项 | 配置路径 | 默认值 | 说明 |
|--------|---------|--------|------|
| status.pollInterval | application.yml | 5000 | 状态轮询间隔（毫秒，5秒） |
| status.maxPollCount | application.yml | 60 | 最大轮询次数（5分钟） |
| waAccount.statusRefreshInterval | application.yml | 30000 | WA账号状态刷新间隔（毫秒，30秒） |

#### 9.3 渠道触达限制配置

**配置方式**：通过管理控台"渠道限制配置"功能管理

| 配置项 | 配置路径 | 默认值 | 说明 |
|--------|---------|--------|------|
| daily_limit_per_case | 后台配置 | 200 | WhatsApp渠道每日每案件限制数量 |
| daily_limit_per_case_unlimited | 后台配置 | false | 是否不限制每日每案件 |
| daily_limit_per_contact | 后台配置 | 100 | WhatsApp渠道每日每联系人限制数量 |
| daily_limit_per_contact_unlimited | 后台配置 | false | 是否不限制每日每联系人 |
| send_interval | 后台配置 | 20 | WhatsApp渠道发送时间间隔（秒） |
| send_interval_unlimited | 后台配置 | false | 是否不限制发送间隔 |
| enabled | 后台配置 | true | 该队列的WhatsApp渠道是否启用 |

**配置维度**：
- 按"甲方（tenant_id）+ 渠道（channel）+ 队列（queue_id）"三个维度管理
- 不同队列可以配置不同的限制规则
- 支持独立设置每个限制项是否启用

#### 9.4 运营开关

| 开关名称 | 默认值 | 说明 |
|---------|--------|------|
| message.contentFilter | true | 是否启用消息内容敏感词过滤 |
| message.statusPolling | true | 是否启用消息状态轮询 |
| message.autoRetry | false | 是否自动重试发送失败的消息 |
| waAccount.autoSwitch | true | 是否自动切换WA账号（当当前账号不可用时） |

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 用户发送消息 | 消息发送 | message_send | messageType: 消息类型, waAccountType: WA账号类型, contactId: 联系人ID, senderId: 催员ID |
| 消息发送成功 | 消息发送成功 | message_send_success | messageId: 消息ID, status: 消息状态 |
| 消息发送失败 | 消息发送失败 | message_send_failure | messageId: 消息ID, errorCode: 错误码, errorMessage: 错误信息 |
| 消息状态更新 | 消息状态更新 | message_status_update | messageId: 消息ID, oldStatus: 旧状态, newStatus: 新状态 |
| 消息已读 | 消息已读 | message_read | messageId: 消息ID, readAt: 阅读时间 |
| 用户查看图片 | 图片查看 | image_view | messageId: 消息ID, imageUrl: 图片URL |
| 用户播放视频 | 视频播放 | video_play | messageId: 消息ID, videoUrl: 视频URL |
| 用户播放音频 | 音频播放 | audio_play | messageId: 消息ID, audioUrl: 音频URL |
| WA账号状态异常 | WA账号状态异常 | wa_account_error | waAccountId: WA账号ID, waAccountType: WA账号类型, errorCode: 错误码 |
| 无可用WA账号 | 无可用WA账号 | no_available_wa_account | contactId: 联系人ID, senderId: 催员ID |
| 点击添加个人WA | 添加个人WA点击 | personal_wa_add_click | collectorId: 催员ID |
| 个人WA绑定成功 | 个人WA绑定成功 | personal_wa_bind_success | deviceId: 设备ID, collectorId: 催员ID, bindTime: 绑定时长（秒） |
| 个人WA绑定失败 | 个人WA绑定失败 | personal_wa_bind_failure | deviceId: 设备ID, errorCode: 错误码, errorMessage: 错误信息 |
| 个人WA绑定超时 | 个人WA绑定超时 | personal_wa_bind_timeout | deviceId: 设备ID, timeout: 超时时间（秒） |
| 个人WA掉线 | 个人WA掉线 | personal_wa_offline | deviceId: 设备ID, offlineAt: 掉线时间 |
| 个人WA重新绑定 | 个人WA重新绑定 | personal_wa_rebind | deviceId: 设备ID, action: 操作类型（rebind/newAccount） |
| 刷新二维码 | 二维码刷新 | qr_code_refresh | deviceId: 设备ID |
| Chatting状态查询 | Chatting状态查询 | chatting_status_check | phoneNumber: 手机号, source: 数据来源（cache/message_history/cwa_api） |
| 缓存命中 | 缓存命中 | cache_hit | phoneNumber: 手机号, cacheAge: 缓存天数 |
| CWA API调用 | CWA API调用 | cwa_api_call | phoneNumber: 手机号, apiType: checkPhone |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 系统架构与模块划分（System Architecture & Modules）

#### 1.1 架构图

```
┌─────────────────┐
│   前端 (Vue3)   │
│  IMPanel.vue    │
│  消息发送界面    │
└────────┬────────┘
         │ HTTP/HTTPS
         │ POST /api/v1/im/messages/send
         │ GET /api/v1/im/messages/status
         ↓
┌─────────────────┐
│  Java后端        │
│  MessageController│
└────────┬────────┘
         │
         ├──→ MessageService (消息处理服务)
         ├──→ WAAccountService (WA账号状态检查)
         ├──→ WhatsAppAPIClient (WhatsApp API调用)
         └──→ MessageStatusService (消息状态查询)
         │
         ↓
┌─────────────────┐
│  WhatsApp API   │
│  Business API   │
└─────────────────┘
```

#### 1.2 模块职责

**前端模块**：
- `frontend/src/components/IMPanel.vue`：IM面板组件，包含WhatsApp聊天窗口
- `frontend/src/utils/imRequest.ts`：IM端请求工具，处理API调用
- `frontend/src/stores/message.ts`：消息状态管理（可选，使用Pinia）

**后端模块**：
- `MessageController`：消息发送和查询控制器
- `MessageService`：消息处理服务，处理消息发送逻辑
- `WAAccountService`：WA账号状态检查服务
- `WhatsAppAPIClient`：WhatsApp Business API客户端
- `MessageStatusService`：消息状态查询和更新服务

---

### 2. 接口设计与系统依赖（API Design & Dependencies）

#### 2.1 发送消息接口

**接口路径**：`POST /api/v1/im/messages/send`

**请求参数**：
```json
{
  "contactId": 1,
  "messageType": "text",
  "content": "您好，请问您什么时候可以还款？",
  "waAccountType": "platform",
  "waAccountId": null
}
```

**响应数据**（成功）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messageId": "msg_123456",
    "status": "sent",
    "sentAt": "2025-01-20T10:30:25Z",
    "waAccountType": "platform",
    "waAccountId": null
  }
}
```

**响应数据**（失败 - WA账号异常）：
```json
{
  "code": 500,
  "message": "Whatsapp online status is abnormal. Please refresh the page.",
  "errorCode": "WA_ACCOUNT_UNPAIRED",
  "data": {
    "waAccountId": "platform_1",
    "waAccountType": "platform",
    "suggestedAction": "refresh_page"
  }
}
```

**响应数据**（失败 - 无可用账号）：
```json
{
  "code": 500,
  "message": "No available WhatsApp account. Please contact administrator.",
  "errorCode": "NO_AVAILABLE_WA_ACCOUNT",
  "data": {
    "availablePlatformAccounts": 0,
    "availablePersonalAccounts": 0
  }
}
```

**响应数据**（失败 - 超过每日每案件限制）：
```json
{
  "code": 403,
  "message": "Daily limit per case exceeded. You have sent 200 messages to this case today.",
  "errorCode": "DAILY_LIMIT_PER_CASE_EXCEEDED",
  "data": {
    "sentCount": 200,
    "dailyLimit": 200,
    "caseId": 12345
  }
}
```

**响应数据**（失败 - 发送时间间隔限制）：
```json
{
  "code": 403,
  "message": "Send interval limit. Please wait 15 seconds before sending again.",
  "errorCode": "SEND_INTERVAL_LIMIT",
  "data": {
    "remainingSeconds": 15,
    "sendInterval": 20,
    "lastSentAt": "2025-01-20T10:30:25Z"
  }
}
```

#### 2.2 查询消息状态接口

**接口路径**：`GET /api/v1/im/messages/{messageId}/status`

**请求参数**：
- `messageId`：消息ID（路径参数）

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messageId": "msg_123456",
    "status": "read",
    "sentAt": "2025-01-20T10:30:25Z",
    "deliveredAt": "2025-01-20T10:30:28Z",
    "readAt": "2025-01-20T10:35:12Z"
  }
}
```

#### 2.3 查询WA账号状态接口

**接口路径**：`GET /api/v1/im/wa-accounts/status`

**请求参数**：无

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "platformAccounts": [
      {
        "id": "platform_1",
        "status": "paired",
        "available": true
      }
    ],
    "personalAccounts": [
      {
        "id": "personal_1",
        "name": "个人WA1",
        "status": "paired",
        "available": true
      }
    ]
  }
}
```

#### 2.4 创建WA云设备接口

**接口路径**：`POST /api/v1/wa/devices/create`

**请求参数**：
```json
{
  "collectorId": "collector001",
  "deviceType": "personal"
}
```

**响应数据**（成功）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_123456",
    "qrCode": "data:image/png;base64,iVBORw0KG...",
    "status": "binding",
    "expiresAt": "2025-01-20T10:40:25Z"
  }
}
```

**响应数据**（失败 - 已达上限）：
```json
{
  "code": 400,
  "message": "Maximum 3 personal WhatsApp accounts allowed.",
  "errorCode": "MAX_PERSONAL_WA_REACHED",
  "data": {
    "currentCount": 3,
    "maxCount": 3
  }
}
```

#### 2.5 查询设备绑定状态接口

**接口路径**：`GET /api/v1/wa/devices/{deviceId}/status`

**请求参数**：
- `deviceId`：设备ID（路径参数）

**响应数据**（绑定成功）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_123456",
    "status": "paired",
    "phoneNumber": "+919876543210",
    "displayName": "John Doe",
    "avatar": "https://example.com/avatar.jpg",
    "pairedAt": "2025-01-20T10:35:15Z"
  }
}
```

**响应数据**（绑定中）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_123456",
    "status": "binding"
  }
}
```

**响应数据**（绑定失败）：
```json
{
  "code": 500,
  "message": "Failed to bind WhatsApp device.",
  "errorCode": "WA_DEVICE_BIND_FAILED",
  "data": {
    "deviceId": "device_123456",
    "status": "failed",
    "reason": "QR code expired"
  }
}
```

#### 2.6 重新绑定设备接口

**接口路径**：`POST /api/v1/wa/devices/{deviceId}/rebind`

**请求参数**：
- `deviceId`：设备ID（路径参数）

**响应数据**（成功）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_123456",
    "qrCode": "data:image/png;base64,iVBORw0KG...",
    "status": "binding",
    "expiresAt": "2025-01-20T10:45:30Z"
  }
}
```

#### 2.7 获取WA账号列表接口

**接口路径**：`GET /api/v1/wa/accounts?collectorId={collectorId}`

**请求参数**：
- `collectorId`：催员ID（查询参数）

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "platformAccounts": [
      {
        "id": "platform_1",
        "type": "platform",
        "status": "paired",
        "available": true
      }
    ],
    "personalAccounts": [
      {
        "id": "device_123456",
        "type": "personal",
        "status": "paired",
        "phoneNumber": "+919876543210",
        "displayName": "John Doe",
        "avatar": "https://example.com/avatar.jpg",
        "pairedAt": "2025-01-20T10:35:15Z",
        "available": true
      },
      {
        "id": "device_789012",
        "type": "personal",
        "status": "unpaired",
        "phoneNumber": "+919876543211",
        "displayName": "Jane Doe",
        "avatar": "https://example.com/avatar2.jpg",
        "unpairedAt": "2025-01-20T09:15:30Z",
        "available": false
      }
    ],
    "availableCount": 1,
    "totalCount": 2,
    "maxCount": 3
  }
}
```

#### 2.8 检查手机号WhatsApp状态接口（优化）

**接口路径**：`POST /api/v1/wa/checkPhoneStatus`

**业务说明**：
- 该接口实现智能缓存机制，减少对CWA checkPhone接口的调用
- 优先使用缓存（wa_relation表）和历史消息记录（message表）
- 仅在必要时才调用CWA checkPhone接口

**请求参数**：
```json
{
  "phoneNumber": "+919876543210"
}
```

**响应数据**（命中缓存）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "cache",
    "checkedAt": "2025-01-18T10:30:25Z",
    "cacheAge": 2
  }
}
```

**响应数据**（命中消息记录）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "message_history",
    "lastReplyAt": "2025-01-19T15:20:10Z"
  }
}
```

**响应数据**（调用CWA API）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "chatting",
    "source": "cwa_api",
    "registered": true,
    "chatting": true,
    "checkedAt": "2025-01-20T10:35:30Z"
  }
}
```

**响应数据**（未注册WhatsApp）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "phoneNumber": "+919876543210",
    "status": "not_registered",
    "source": "cwa_api",
    "registered": false,
    "chatting": false,
    "checkedAt": "2025-01-20T10:35:30Z"
  }
}
```

#### 2.9 系统依赖

**第三方服务**：
- WhatsApp Business API：用于发送和接收消息
- WhatsApp Webhook：用于接收消息状态更新通知
- CWA（Cloud WhatsApp）服务：用于管理云设备、二维码绑定和手机号状态查询

**内部服务**：
- 用户服务：获取催员信息
- 联系人服务：获取联系人信息
- 消息存储服务：保存消息记录
- WA设备管理服务：管理云设备和绑定状态
- WA关系缓存服务：管理手机号与WhatsApp状态的关系缓存

---

### 3. 错误处理机制（Error Handling）

#### 3.1 WA账号状态异常处理

**场景**：当催员的WA被封或者掉线时，当CWA还没有获取到最新的unpaired状态，此时催员发送消息，就会触发系统报错。

**处理流程**：
```
发送消息请求
    ↓
检查WA账号状态（从缓存获取）
    ↓
[状态：paired] → 继续发送
    ↓
[状态：unpaired] → 返回错误："Whatsapp online status is abnormal. Please refresh the page."
    ↓
[状态：unknown] → 调用WhatsApp API查询最新状态
    ↓
[最新状态：unpaired] → 更新缓存 → 返回错误
    ↓
[最新状态：paired] → 更新缓存 → 继续发送
```

**前端处理**：
- 显示错误提示："Whatsapp online status is abnormal. Please refresh the page."
- 提供刷新按钮，点击后刷新WA账号状态
- 自动刷新WA账号状态（每30秒）

#### 3.2 无可用WA账号处理

**场景**：公司WA和个人WA都不可用（全部被封或掉线）。

**处理流程**：
```
发送消息请求
    ↓
检查可用WA账号
    ↓
[有可用账号] → 继续发送
    ↓
[无可用账号] → 返回错误："暂无可用的WhatsApp账号，请联系管理员"
```

**前端处理**：
- 显示错误提示："暂无可用的WhatsApp账号，请联系管理员"
- 禁用发送按钮
- 显示联系管理员提示

#### 3.3 网络连接失败处理

**场景**：网络连接异常，无法连接到服务器或WhatsApp API。

**处理流程**：
```
发送消息请求
    ↓
网络请求失败
    ↓
捕获网络错误
    ↓
返回错误："网络连接失败，请检查网络后重试"
```

**前端处理**：
- 显示错误提示："网络连接失败，请检查网络后重试"
- 提供重试按钮
- 自动重试（最多3次，可配置）

#### 3.4 消息内容违规处理

**场景**：消息内容包含敏感词或违规内容。

**处理流程**：
```
发送消息请求
    ↓
内容审核检查
    ↓
[内容合规] → 继续发送
    ↓
[内容违规] → 返回错误："消息内容不符合规范，请修改后重试"
```

**前端处理**：
- 显示错误提示："消息内容不符合规范，请修改后重试"
- 高亮显示违规内容（如果可能）
- 允许用户修改后重试

#### 3.5 接收方号码无效处理

**场景**：联系人手机号格式错误或不存在。

**处理流程**：
```
发送消息请求
    ↓
验证接收方号码
    ↓
[号码有效] → 继续发送
    ↓
[号码无效] → 返回错误："接收方号码无效，请确认号码是否正确"
```

**前端处理**：
- 显示错误提示："接收方号码无效，请确认号码是否正确"
- 允许用户检查并更新联系人信息

#### 3.6 渠道触达限制处理

**场景**：超过渠道触达限制（每日每案件/每日每联系人/发送时间间隔）。

**处理流程**：
```
发送消息请求
    ↓
调用渠道触达限制校验接口
    ↓
检查1：每日每案件限制
    ↓
[超限] → 返回错误："Daily limit per case exceeded. You have sent {count} messages to this case today."
    ↓
[未超限] → 检查2：每日每联系人限制
    ↓
[超限] → 返回错误："Daily limit per contact exceeded. You have sent {count} messages to this contact today."
    ↓
[未超限] → 检查3：发送时间间隔
    ↓
[超限] → 返回错误："Send interval limit. Please wait {seconds} seconds before sending again."
    ↓
[未超限] → 继续发送
```

**前端处理**：
- 显示错误提示（英文错误信息）
- 显示剩余等待时间（发送间隔限制）
- 在发送按钮旁显示当前渠道限制信息：
  - 已发送数/最大限制数
  - 下次可发送时间（如果有间隔限制）

---

### 4. 前端实现细节（Frontend Implementation）

#### 4.1 消息状态显示实现

**状态图标组件**：
```vue
<el-icon v-if="message.sender_type === 'collector'" class="message-status">
  <Clock v-if="message.status === 'sending'" style="color: #8696a0;" />
  <CircleCheck v-else-if="message.status === 'sent'" style="color: #8696a0;" />
  <Select v-else-if="message.status === 'delivered'" style="color: #8696a0;" />
  <Select v-else-if="message.status === 'read'" style="color: #25D366;" />
  <Warning v-else-if="message.status === 'failed'" style="color: #F56C6C;" />
</el-icon>
```

**状态轮询实现**：
```typescript
// 轮询查询消息状态
const pollMessageStatus = async (messageId: string) => {
  const maxPollCount = 60 // 最多轮询60次（5分钟）
  let pollCount = 0
  
  const poll = setInterval(async () => {
    pollCount++
    if (pollCount > maxPollCount) {
      clearInterval(poll)
      return
    }
    
    try {
      const res = await getMessageStatus(messageId)
      if (res.data.status === 'read' || res.data.status === 'failed') {
        clearInterval(poll)
      }
      // 更新消息状态
      updateMessageStatus(messageId, res.data.status)
    } catch (error) {
      console.error('查询消息状态失败:', error)
    }
  }, 5000) // 每5秒查询一次
}
```

#### 4.2 错误提示实现

**红色感叹号悬浮提示**：
```vue
<el-tooltip 
  v-if="message.status === 'failed'"
  :content="getErrorMessage(message)"
  placement="top"
>
  <el-icon class="message-status" style="color: #F56C6C;">
    <Warning />
  </el-icon>
</el-tooltip>
```

**发送时错误处理**：
```typescript
const sendMessage = async () => {
  try {
    const res = await sendMessageAPI({
      contactId: selectedContact.value.id,
      messageType: 'text',
      content: messageInput.value,
      waAccountType: selectedWAAccount.value?.type,
      waAccountId: selectedWAAccount.value?.id,
      caseId: caseData.value?.id,
      tenantId: caseData.value?.tenant_id,
      queueId: caseData.value?.queue_id
    })
    
    // 发送成功，添加到消息列表
    addMessage(res.data)
    
    // 开始轮询状态
    pollMessageStatus(res.data.messageId)
  } catch (error: any) {
    const errorCode = error.response?.data?.errorCode
    const errorMessage = error.response?.data?.message
    
    // 根据错误码显示不同的错误提示（全部使用英文）
    if (errorCode === 'WA_ACCOUNT_UNPAIRED') {
      ElMessageBox.alert(
        errorMessage || 'WhatsApp online status is abnormal. Please refresh the page.',
        'Error',
        { type: 'error' }
      )
    } else if (errorCode === 'NO_AVAILABLE_WA_ACCOUNT') {
      ElMessage.error(errorMessage || 'No available WhatsApp account. Please contact administrator.')
    } else if (errorCode === 'DAILY_LIMIT_PER_CASE_EXCEEDED') {
      ElMessage.error(errorMessage || 'Daily limit per case exceeded.')
    } else if (errorCode === 'DAILY_LIMIT_PER_CONTACT_EXCEEDED') {
      ElMessage.error(errorMessage || 'Daily limit per contact exceeded.')
    } else if (errorCode === 'SEND_INTERVAL_LIMIT') {
      ElMessage.error(errorMessage || 'Send interval limit. Please wait before sending again.')
    } else if (errorCode === 'CONTENT_VIOLATION') {
      ElMessage.error(errorMessage || 'Message content violates policy. Please modify and try again.')
    } else if (errorCode === 'NETWORK_ERROR') {
      ElMessage.error(errorMessage || 'Network connection failed. Please check your network and try again.')
    } else {
      ElMessage.error(errorMessage || 'Failed to send message.')
    }
  }
}
```

#### 4.3 媒体消息实现

**图片消息显示**：
```vue
<div v-else-if="message.type === 'image'" class="message-image">
  <el-image 
    :src="message.content" 
    fit="cover" 
    style="max-width: 200px; max-height: 200px;"
    :preview-src-list="[message.content]"
    preview-teleported
  />
</div>
```

**视频消息显示**：
```vue
<div v-else-if="message.type === 'video'" class="message-video">
  <video 
    :src="message.content" 
    controls
    style="max-width: 400px; max-height: 300px;"
  >
    您的浏览器不支持视频播放
  </video>
</div>
```

**音频消息显示**（仅接收）：
```vue
<div v-else-if="message.type === 'audio'" class="message-audio">
  <el-icon><Microphone /></el-icon>
  <span>语音消息</span>
  <audio :src="message.content" controls>
    您的浏览器不支持音频播放
  </audio>
</div>
```

#### 4.4 个人WA账号添加实现

**添加按钮点击处理**：
```typescript
const addPersonalWAAccount = async () => {
  // 检查个人WA账号数量
  if (personalWAAccounts.value.accounts.length >= 3) {
    ElMessage.warning('Maximum 3 personal WhatsApp accounts allowed.')
    return
  }
  
  try {
    // 调用CWA API创建新设备
    const res = await createWADevice({
      collectorId: currentCollector.value.id,
      deviceType: 'personal'
    })
    
    // 保存设备ID和二维码
    currentBindingDevice.value = {
      deviceId: res.data.deviceId,
      qrCode: res.data.qrCode,
      expiresAt: res.data.expiresAt
    }
    
    // 显示二维码绑定弹窗
    qrCodeDialogVisible.value = true
    
    // 开始轮询绑定状态
    startBindingStatusPolling(res.data.deviceId)
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || 'Failed to create WhatsApp device.'
    ElMessage.error(errorMessage)
  }
}

// 轮询绑定状态
const startBindingStatusPolling = (deviceId: string) => {
  const maxPollCount = 60 // 最多轮询60次（120秒）
  let pollCount = 0
  
  bindingPollInterval.value = setInterval(async () => {
    pollCount++
    
    if (pollCount > maxPollCount) {
      clearInterval(bindingPollInterval.value)
      ElMessage.error('Binding timeout. Please try again.')
      qrCodeDialogVisible.value = false
      return
    }
    
    try {
      const res = await getDeviceBindingStatus(deviceId)
      
      if (res.data.status === 'paired') {
        // 绑定成功
        clearInterval(bindingPollInterval.value)
        ElMessage.success('Binding successful')
        
        // 延迟500ms后关闭弹窗
        setTimeout(() => {
          qrCodeDialogVisible.value = false
          // 刷新个人WA账号列表
          refreshPersonalWAAccounts()
        }, 500)
      } else if (res.data.status === 'failed') {
        // 绑定失败
        clearInterval(bindingPollInterval.value)
        ElMessage.error(res.data.reason || 'Failed to bind WhatsApp device.')
        qrCodeDialogVisible.value = false
      }
    } catch (error) {
      console.error('Failed to check binding status:', error)
    }
  }, 2000) // 每2秒查询一次
}

// 刷新个人WA账号列表
const refreshPersonalWAAccounts = async () => {
  try {
    const res = await getWAAccounts(currentCollector.value.id)
    personalWAAccounts.value = {
      accounts: res.data.personalAccounts,
      available: res.data.availableCount,
      total: res.data.totalCount
    }
    
    // 默认选中新绑定的账号（列表最后一个）
    if (personalWAAccounts.value.accounts.length > 0) {
      const newAccount = personalWAAccounts.value.accounts[personalWAAccounts.value.accounts.length - 1]
      selectedWAAccount.value = { id: newAccount.id, type: 'personal' }
    }
  } catch (error) {
    console.error('Failed to refresh personal WA accounts:', error)
  }
}

// 刷新二维码
const refreshQRCode = async () => {
  if (!currentBindingDevice.value) return
  
  try {
    const res = await createWADevice({
      collectorId: currentCollector.value.id,
      deviceType: 'personal'
    })
    
    currentBindingDevice.value = {
      deviceId: res.data.deviceId,
      qrCode: res.data.qrCode,
      expiresAt: res.data.expiresAt
    }
    
    // 重新开始轮询
    if (bindingPollInterval.value) {
      clearInterval(bindingPollInterval.value)
    }
    startBindingStatusPolling(res.data.deviceId)
    
    ElMessage.success('QR code refreshed')
  } catch (error: any) {
    ElMessage.error('Failed to refresh QR code')
  }
}
```

#### 4.5 掉线账号重新绑定实现

**掉线账号显示**：
```vue
<div 
  v-for="account in personalWAAccounts.accounts" 
  :key="account.id"
  class="wa-avatar-item"
  :class="{ 
    active: selectedWAAccount?.id === account.id && selectedWAAccount?.type === 'personal',
    offline: account.status === 'unpaired'
  }"
  @click="handlePersonalWAClick(account)"
>
  <el-tooltip 
    v-if="account.status === 'unpaired'"
    content="账号已经掉线，点击后重新绑定或绑定新账号"
    placement="top"
  >
    <div class="wa-avatar-wrapper">
      <el-avatar :size="36" :src="account.avatar">
        <el-icon><User /></el-icon>
      </el-avatar>
      <div class="offline-overlay">
        <el-icon class="offline-icon"><Warning /></el-icon>
      </div>
    </div>
  </el-tooltip>
  
  <el-avatar 
    v-else
    :size="36" 
    :src="account.avatar"
  >
    <el-icon><User /></el-icon>
  </el-avatar>
</div>
```

**点击掉线账号处理**：
```typescript
const handlePersonalWAClick = async (account: any) => {
  if (account.status === 'unpaired') {
    // 显示掉线账号操作选择弹窗
    showOfflineAccountDialog(account)
  } else {
    // 正常选择账号
    selectWAAccount(account, 'personal')
  }
}

// 显示掉线账号操作选择弹窗
const showOfflineAccountDialog = (account: any) => {
  ElMessageBox.confirm(
    '此WhatsApp账号已掉线，请选择操作：',
    'WhatsApp账号已掉线',
    {
      distinguishCancelAndClose: true,
      confirmButtonText: '重新绑定此账号',
      cancelButtonText: '绑定新账号',
      type: 'warning',
      closeOnClickModal: false
    }
  ).then(async () => {
    // 选择：重新绑定此账号
    await rebindWAAccount(account.id)
  }).catch((action) => {
    if (action === 'cancel') {
      // 选择：绑定新账号
      addPersonalWAAccount()
    }
    // 否则是关闭弹窗，不做处理
  })
}

// 重新绑定WA账号
const rebindWAAccount = async (deviceId: string) => {
  try {
    const res = await rebindWADevice(deviceId)
    
    // 保存设备ID和二维码
    currentBindingDevice.value = {
      deviceId: res.data.deviceId,
      qrCode: res.data.qrCode,
      expiresAt: res.data.expiresAt
    }
    
    // 显示二维码绑定弹窗
    qrCodeDialogVisible.value = true
    
    // 开始轮询绑定状态
    startRebindingStatusPolling(res.data.deviceId)
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || 'Failed to rebind WhatsApp device.'
    ElMessage.error(errorMessage)
  }
}

// 轮询重新绑定状态
const startRebindingStatusPolling = (deviceId: string) => {
  const maxPollCount = 60
  let pollCount = 0
  
  bindingPollInterval.value = setInterval(async () => {
    pollCount++
    
    if (pollCount > maxPollCount) {
      clearInterval(bindingPollInterval.value)
      ElMessage.error('Rebinding timeout. Please try again.')
      qrCodeDialogVisible.value = false
      return
    }
    
    try {
      const res = await getDeviceBindingStatus(deviceId)
      
      if (res.data.status === 'paired') {
        // 重新绑定成功
        clearInterval(bindingPollInterval.value)
        ElMessage.success('Rebinding successful')
        
        setTimeout(() => {
          qrCodeDialogVisible.value = false
          refreshPersonalWAAccounts()
        }, 500)
      } else if (res.data.status === 'failed') {
        clearInterval(bindingPollInterval.value)
        ElMessage.error(res.data.reason || 'Failed to rebind WhatsApp device.')
        qrCodeDialogVisible.value = false
      }
    } catch (error) {
      console.error('Failed to check rebinding status:', error)
    }
  }, 2000)
}
```

**CSS样式**：
```css
/* 掉线账号样式 */
.wa-avatar-item.offline {
  cursor: not-allowed;
  opacity: 0.8;
}

.wa-avatar-item.offline:hover {
  cursor: pointer; /* 允许点击重新绑定 */
}

.wa-avatar-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
}

.offline-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.offline-icon {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 16px;
  color: #F56C6C;
  background: #FFFFFF;
  border-radius: 50%;
  padding: 1px;
}
```

---

### 5. 后端实现细节（Backend Implementation）

#### 5.1 WA账号状态检查

```java
@Service
public class WAAccountService {
    
    /**
     * 检查WA账号状态
     */
    public WAAccountStatus checkAccountStatus(String accountId, String accountType) {
        // 从缓存获取状态
        WAAccountStatus cachedStatus = getCachedStatus(accountId);
        if (cachedStatus != null && cachedStatus != WAAccountStatus.UNKNOWN) {
            return cachedStatus;
        }
        
        // 调用WhatsApp API查询最新状态
        WAAccountStatus latestStatus = queryWhatsAppAPIStatus(accountId, accountType);
        
        // 更新缓存
        updateCache(accountId, latestStatus);
        
        return latestStatus;
    }
    
    /**
     * 获取可用的WA账号
     */
    public WAAccount getAvailableAccount() {
        // 优先使用公司WA
        WAAccount platformAccount = getAvailablePlatformAccount();
        if (platformAccount != null) {
            return platformAccount;
        }
        
        // 如果没有公司WA，使用个人WA
        WAAccount personalAccount = getAvailablePersonalAccount();
        if (personalAccount != null) {
            return personalAccount;
        }
        
        // 没有可用账号
        throw new NoAvailableWAAccountException("暂无可用的WhatsApp账号");
    }
}
```

#### 5.2 消息发送服务

```java
@Service
public class MessageService {
    
    @Autowired
    private WAAccountService waAccountService;
    
    @Autowired
    private WhatsAppAPIClient whatsAppAPIClient;
    
    @Autowired
    private ChannelLimitService channelLimitService;
    
    /**
     * 发送消息
     */
    public MessageResponse sendMessage(MessageRequest request) {
        // 1. 检查WA账号状态
        WAAccount account = waAccountService.getAvailableAccount();
        WAAccountStatus status = waAccountService.checkAccountStatus(
            account.getId(), 
            account.getType()
        );
        
        if (status == WAAccountStatus.UNPAIRED) {
            throw new WAAccountUnpairedException(
                "WhatsApp online status is abnormal. Please refresh the page."
            );
        }
        
        // 2. 检查渠道触达限制
        ChannelLimitCheckResult limitCheck = channelLimitService.checkLimit(
            request.getTenantId(),
            request.getCaseId(),
            request.getContactId(),
            "whatsapp",
            request.getQueueId()
        );
        
        if (!limitCheck.isAllowed()) {
            // 超过限制，抛出对应的异常
            throw limitCheck.toException();
        }
        
        // 3. 调用WhatsApp API发送消息
        try {
            WhatsAppResponse response = whatsAppAPIClient.sendMessage(
                account.getId(),
                request.getContactPhone(),
                request.getContent(),
                request.getMessageType()
            );
            
            // 4. 保存消息到数据库
            Message message = saveMessage(request, account, response);
            
            // 5. 更新渠道触达限制计数
            channelLimitService.recordSent(
                request.getTenantId(),
                request.getCaseId(),
                request.getContactId(),
                "whatsapp",
                request.getQueueId()
            );
            
            return MessageResponse.builder()
                .messageId(message.getId())
                .status(MessageStatus.SENT)
                .sentAt(message.getSentAt())
                .waAccountType(account.getType())
                .waAccountId(account.getId())
                .build();
        } catch (WhatsAppAPIException e) {
            // 处理WhatsApp API错误
            throw new MessageSendException("Failed to send message: " + e.getMessage());
        }
    }
}
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

#### 1.1 消息发送测试

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC001 | 发送文本消息 | 已选择联系人，已选择WA账号 | 1. 输入文本消息<br>2. 点击发送 | 消息发送成功，显示单灰色对勾 |
| TC002 | 发送图片消息 | 已选择联系人，已选择WA账号 | 1. 点击图片按钮<br>2. 选择图片文件<br>3. 点击发送 | 图片上传成功，消息发送成功 |
| TC003 | 接收视频消息 | 客户发送视频消息 | 1. 查看聊天窗口 | 视频消息显示在聊天窗口，支持在线播放 |
| TC004 | 接收音频消息 | 客户发送音频消息 | 1. 查看聊天窗口 | 音频消息显示在聊天窗口，支持播放控制 |

#### 1.2 消息状态显示测试

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC005 | 状态更新为已送达 | 消息已发送 | 等待消息送达 | 状态图标更新为双灰色对勾 |
| TC006 | 状态更新为已读 | 消息已送达 | 等待客户阅读 | 状态图标更新为双蓝色对勾 |
| TC007 | 状态更新为失败 | 消息发送失败 | 查看失败消息 | 显示红色感叹号，鼠标悬停显示错误信息 |

#### 1.3 错误处理测试

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC008 | WA账号异常 | WA账号被封或掉线 | 1. 选择异常的WA账号<br>2. 发送消息 | 显示英文错误提示："WhatsApp online status is abnormal. Please refresh the page." |
| TC009 | 无可用WA账号 | 所有WA账号都不可用 | 1. 尝试发送消息 | 显示英文错误提示："No available WhatsApp account. Please contact administrator." |
| TC010 | 网络连接失败 | 网络断开 | 1. 尝试发送消息 | 显示英文错误提示："Network connection failed. Please check your network and try again." |
| TC011 | 超过每日每案件限制 | 案件今日已发送达到限制 | 1. 尝试发送消息 | 显示英文错误提示："Daily limit per case exceeded. You have sent {count} messages to this case today." |
| TC012 | 超过每日每联系人限制 | 联系人今日已接收达到限制 | 1. 尝试发送消息 | 显示英文错误提示："Daily limit per contact exceeded. You have sent {count} messages to this contact today." |
| TC013 | 发送时间间隔限制 | 距离上次发送未达到间隔 | 1. 连续快速发送消息 | 显示英文错误提示："Send interval limit. Please wait {seconds} seconds before sending again." |

#### 1.2 个人WA账号管理测试

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC014 | 添加个人WA账号 | 当前个人WA账号<3个 | 1. 点击"+"号<br>2. 显示二维码<br>3. 使用WhatsApp扫码 | 二维码显示正常，扫码后绑定成功，显示"Binding successful"，新账号头像出现在个人WA区域 |
| TC015 | 个人WA账号额度限制 | 已绑定3个个人WA账号 | 1. 点击"+"号 | 显示提示："Maximum 3 personal WhatsApp accounts allowed." |
| TC016 | 二维码刷新 | 二维码绑定弹窗已打开 | 1. 点击"刷新二维码"按钮 | 二维码重新加载，显示新的二维码 |
| TC017 | 绑定超时 | 显示二维码后120秒未扫码 | 1. 等待120秒 | 显示提示："Binding timeout. Please try again."，自动关闭弹窗 |
| TC018 | 绑定失败 | CWA返回绑定失败 | 1. 扫码后绑定失败 | 显示CWA返回的错误信息 |
| TC019 | 个人WA掉线标识 | 个人WA账号状态为unpaired | 1. 查看个人WA账号列表 | 掉线账号头像显示半透明遮罩和红色警告图标 |
| TC020 | 掉线账号悬停提示 | 个人WA账号已掉线 | 1. 鼠标悬停在掉线账号上 | 显示提示："账号已经掉线，点击后重新绑定或绑定新账号" |
| TC021 | 重新绑定掉线账号 | 个人WA账号已掉线 | 1. 点击掉线账号<br>2. 选择"重新绑定此账号"<br>3. 扫码绑定 | 显示二维码，扫码成功后显示"Rebinding successful"，账号状态更新为paired |
| TC022 | 掉线后绑定新账号 | 个人WA账号已掉线，总数<3 | 1. 点击掉线账号<br>2. 选择"绑定新账号"<br>3. 扫码绑定 | 创建新的云设备，显示二维码，绑定成功后新增账号 |
| TC023 | 选中新绑定账号 | 绑定新个人WA成功 | 1. 完成绑定流程 | 新绑定的账号默认被选中（绿色边框） |

#### 1.3 Chatting状态判断优化测试

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC024 | 缓存命中-查询1 | wa_relation表有7天内的记录 | 1. 点击联系人（本人） | 直接使用缓存数据，不调用CWA API，更新last_used_at字段 |
| TC025 | 缓存过期-查询1失败 | wa_relation表记录超过7天 | 1. 点击联系人（本人） | 缓存失效，继续执行查询2 |
| TC026 | 消息记录命中-查询2 | message表有7天内的WhatsApp回复记录 | 1. 点击联系人（本人） | 判定为chatting状态，在wa_relation表插入记录，source='message_history' |
| TC027 | 消息记录过期-查询2失败 | message表回复记录超过7天 | 1. 点击联系人（本人） | 消息记录失效，继续执行查询3 |
| TC028 | 调用CWA API-查询3 | 查询1和查询2都未命中 | 1. 点击联系人（本人） | 调用CWA checkPhone接口，将结果存入wa_relation表，source='cwa_api' |
| TC029 | 未注册WhatsApp | CWA返回registered=false | 1. 点击联系人（本人） | 在wa_relation表插入记录，status='not_registered' |
| TC030 | 跨甲方缓存命中 | 其他甲方已查询过该手机号 | 1. 点击联系人（本人） | 使用跨甲方缓存数据（tenant_id=NULL的记录） |
| TC031 | 缓存数据更新 | 命中缓存后再次点击 | 1. 第一次点击<br>2. 第二次点击 | last_used_at字段更新为最新时间，checked_at保持不变 |
| TC032 | API调用减少验证 | 连续点击同一联系人10次 | 1. 快速点击联系人10次 | 只调用1次CWA API，其余9次使用缓存 |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 公司WA | Platform WA | 企业WhatsApp账号，由平台统一管理 |
| 个人WA | Personal WA | 个人WhatsApp账号，由催员绑定 |
| 已发送 | Sent | 消息已从设备发送到WhatsApp服务器 |
| 已送达 | Delivered | 消息已送达对方设备 |
| 已读 | Read | 对方已阅读消息 |
| 发送失败 | Failed | 消息发送失败 |
| 账号状态 | Account Status | WA账号的连接状态（paired/unpaired） |

### 2. 参考文档（References）

- WhatsApp Business API 文档：https://developers.facebook.com/docs/whatsapp
- CCO系统架构文档：`文档/后端双系统架构说明.md`
- IM面板组件实现：`frontend/src/components/IMPanel.vue`

---

**文档版本**：1.0.0  
**最后更新**：2025-01-20  
**文档作者**：CCO产品团队


