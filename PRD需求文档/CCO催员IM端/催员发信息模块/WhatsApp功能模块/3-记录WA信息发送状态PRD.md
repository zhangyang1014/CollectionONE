# 记录WhatsApp信息发送状态功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

WhatsApp消息发送状态记录是确保消息可追踪的关键功能。通过实时更新和显示消息状态（发送中、已发送、已送达、已读、失败），催员可以清楚了解消息是否成功到达客户，客户是否已查看，从而调整催收策略。

**业务痛点**：
- 催员不确定消息是否成功发送
- 无法判断客户是否已查看消息
- 消息发送失败时不知道具体原因
- 需要实时跟踪消息状态变化

**预期影响的核心指标**：
- 状态更新延迟：≤5秒
- 状态准确率：≥99%
- 状态显示清晰度：催员能快速理解状态含义

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：查看消息发送状态**
- **入口**：催员工作台 → WhatsApp聊天窗口 → 查看已发送消息
- **触发时机**：催员发送消息后，关注消息是否成功送达
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 发送消息后，消息气泡右下角显示状态图标
  2. 初始状态：钟表图标（发送中）
  3. 发送成功：单灰色对勾（已发送到WhatsApp服务器）
  4. 送达成功：双灰色对勾（已送达客户设备）
  5. 客户已读：双蓝色对勾（客户已打开并阅读）
  6. 发送失败：红色感叹号 + 错误提示

**场景2：鼠标悬停查看详细状态**
- **入口**：消息气泡状态图标
- **触发时机**：催员想了解状态的具体含义
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 鼠标悬停在状态图标上
  2. 显示Tooltip提示框
  3. 说明状态含义：
     - 单灰色对勾："已发送到WhatsApp服务器"
     - 双灰色对勾："已送达客户设备"
     - 双蓝色对勾："客户已阅读"
     - 红色感叹号："发送失败：{错误原因}"
     - 钟表图标："正在发送中..."

**场景3：处理发送失败消息**
- **入口**：显示红色感叹号的消息
- **触发时机**：消息发送失败
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 消息显示红色感叹号
  2. 鼠标悬停查看失败原因
  3. 点击感叹号或消息气泡
  4. 弹出操作菜单："重试发送" / "取消"
  5. 选择"重试发送"
  6. 重新发送消息
  7. 状态更新为"发送中"（钟表图标）

**场景4：群发消息状态跟踪**
- **入口**：催员工作台 → 群发消息列表
- **触发时机**：催员批量发送消息后，查看每条消息的状态
- **所在页面**：群发消息统计页面
- **流程节点**：
  1. 查看群发消息列表
  2. 每条消息显示当前状态
  3. 统计：成功数、送达数、已读数、失败数
  4. 点击查看详情

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用场景 |
|---------|---------|---------|---------|
| 催员 | Collector | 了解消息是否成功送达和被阅读 | 日常催收沟通 |

---

### 3. 关键业务流程（Business Flow）

#### 3.1 消息状态生命周期

```
消息创建
    ↓
状态1：sending（发送中）
    - 图标：钟表图标 ⏰
    - 说明：消息正在从设备发送到WhatsApp服务器
    - 持续时间：通常 < 1秒
    ↓
状态2：sent（已发送）
    - 图标：单灰色对勾 ✓（灰色）
    - 说明：消息已成功发送到WhatsApp服务器
    - 持续时间：直到客户设备接收
    ↓
状态3：delivered（已送达）
    - 图标：双灰色对勾 ✓✓（灰色）
    - 说明：消息已成功送达客户设备
    - 特殊情况：
      * 群聊中：所有成员都已收到消息
      * 单聊中：客户设备已接收消息
    - 持续时间：直到客户打开并阅读
    ↓
状态4：read（已读）
    - 图标：双蓝色对勾 ✓✓（蓝色 #25D366）
    - 说明：客户已打开并阅读消息
    - 特殊情况：
      * 群聊中：所有成员都已阅读消息
      * 单聊中：客户已阅读消息
    - 持续时间：终态
    ↓
状态5：failed（发送失败）
    - 图标：红色感叹号 !（红色 #FF3B30）
    - 说明：消息发送失败
    - 常见原因：
      * 网络连接失败
      * WA账号异常（被封/掉线）
      * 接收方号码无效
      * 渠道触达限制
    - 操作：点击可重试发送
    - 持续时间：终态（直到重试）
```

#### 3.2 消息状态轮询更新流程

```
消息发送成功（状态：sent）
    ↓
启动状态轮询
    ↓
轮询间隔：5秒
    ↓
调用API：GET /api/v1/im/messages/{messageId}/status
    ↓
返回最新状态：delivered
    ↓
前端更新状态图标：单灰 → 双灰
    ↓
继续轮询
    ↓
调用API：GET /api/v1/im/messages/{messageId}/status
    ↓
返回最新状态：read
    ↓
前端更新状态图标：双灰 → 双蓝
    ↓
停止轮询（已到达终态：read）
    ↓
【特殊情况1：失败】
如果返回状态：failed
    → 更新为红色感叹号
    → 停止轮询（终态）
    → 显示错误提示
    ↓
【特殊情况2：超时】
轮询超过120次（10分钟）仍为sent/delivered状态
    → 停止轮询
    → 保持当前状态
    ↓
【特殊情况3：页面关闭】
用户关闭页面或切换联系人
    → 停止轮询
    → 下次打开时重新查询最新状态
```

#### 3.3 后端状态更新流程（Webhook）

```
WhatsApp服务器检测到状态变化
    ↓
发送Webhook通知到CCO后端
    ↓
Webhook接口：POST /api/v1/webhooks/whatsapp/status
    ↓
请求参数：
    - messageId: WhatsApp消息ID
    - status: delivered / read / failed
    - timestamp: 状态更新时间
    - error: 错误信息（如果失败）
    ↓
后端验证Webhook签名
    ↓
查询消息记录（根据WhatsApp messageId）
    ↓
更新消息状态到数据库
    ↓
触发实时通知（WebSocket推送到前端）
    ↓
前端接收状态更新
    ↓
更新UI显示
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 状态显示规则

**状态图标规则**：
- **位置**：消息气泡右下角
- **大小**：16x16px
- **颜色**：
  - 发送中：灰色（#8696a0）
  - 已发送：灰色（#8696a0）
  - 已送达：灰色（#8696a0）
  - 已读：蓝色（#25D366，WhatsApp绿色改为蓝色以示区别）
  - 失败：红色（#FF3B30）

**图标切换动画**：
- 状态变化时：淡入淡出动画（300ms）
- 发送中：钟表图标缓慢旋转动画

**Tooltip提示规则**：
- 鼠标悬停延迟：500ms
- 提示内容：
  - 发送中："Sending..."
  - 已发送："Sent to WhatsApp server"
  - 已送达："Delivered to recipient's device"
  - 已读："Read by recipient"
  - 失败："Failed: {errorMessage}"

#### 4.2 轮询规则

**轮询启动条件**：
- 消息状态为 sent 或 delivered
- 聊天窗口在焦点

**轮询停止条件**：
- 状态变为 read（已读，终态）
- 状态变为 failed（失败，终态）
- 轮询超过120次（10分钟）
- 用户关闭页面或切换联系人

**轮询间隔**：
- 默认：5秒
- 前30秒：每5秒轮询一次（前6次）
- 30秒-2分钟：每10秒轮询一次
- 2分钟后：每30秒轮询一次

#### 4.3 状态枚举定义

| 状态码 | 状态名称 | 图标 | 颜色 | 说明 | 是否终态 |
|--------|---------|------|------|------|---------|
| sending | 发送中 | 钟表 ⏰ | 灰色 | 消息正在发送到WhatsApp服务器 | 否 |
| sent | 已发送 | 单对勾 ✓ | 灰色 | 消息已发送到WhatsApp服务器 | 否 |
| delivered | 已送达 | 双对勾 ✓✓ | 灰色 | 消息已送达客户设备 | 否 |
| read | 已读 | 双对勾 ✓✓ | 蓝色 | 客户已阅读消息 | 是 |
| failed | 发送失败 | 感叹号 ! | 红色 | 消息发送失败 | 是 |

#### 4.4 错误提示规则

**失败状态错误提示**（英文）：

| 错误类型 | 错误提示 |
|---------|---------|
| 网络连接失败 | "Network connection failed. Click to retry." |
| WA账号异常 | "WhatsApp online status is abnormal. Please refresh the page." |
| 接收方号码无效 | "Recipient phone number is invalid." |
| 渠道触达限制 | "Daily limit per case exceeded." / "Send interval limit." |
| 其他错误 | "Message send failed. Click to retry." |

#### 4.5 范围边界

**本次需求范围内**：
- ✅ 5种消息状态显示（sending/sent/delivered/read/failed）
- ✅ 状态图标实时更新
- ✅ 轮询查询消息状态
- ✅ Tooltip提示状态含义
- ✅ 失败消息重试功能
- ✅ 状态切换动画效果

**本次需求范围外**：
- ❌ 消息撤回功能（WhatsApp暂不支持）
- ❌ 消息已读回执关闭（由客户端控制）
- ❌ 消息送达时间详细记录（仅记录状态更新时间）
- ❌ 群发消息状态统计（待实现）

---

### 5. 数据字段与口径（Data Definition）

#### 5.1 消息状态数据字段

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | String | 是 | 消息ID | "msg_123456" |
| status | String | 是 | 消息状态：sending/sent/delivered/read/failed | "delivered" |
| sentAt | String | 是 | 发送时间（ISO 8601格式） | "2025-01-20T10:30:25Z" |
| deliveredAt | String | 否 | 送达时间 | "2025-01-20T10:30:30Z" |
| readAt | String | 否 | 阅读时间 | "2025-01-20T10:35:10Z" |
| failedAt | String | 否 | 失败时间 | "2025-01-20T10:30:27Z" |
| errorCode | String | 否 | 错误码（如果失败） | "NETWORK_ERROR" |
| errorMessage | String | 否 | 错误信息（如果失败） | "Network connection failed" |

#### 5.2 状态更新记录字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | String | 是 | 消息ID |
| fromStatus | String | 是 | 原状态 |
| toStatus | String | 是 | 新状态 |
| updatedAt | String | 是 | 更新时间 |
| source | String | 是 | 更新来源：webhook/polling/manual |

---

### 6. 交互与信息展示（UX & UI Brief）

#### 6.1 状态图标样式

**发送中（sending）**：
- 图标：`<Clock />` 钟表图标
- 颜色：#8696a0（灰色）
- 动画：缓慢旋转（2秒一圈）

**已发送（sent）**：
- 图标：`<Select />` 单对勾
- 颜色：#8696a0（灰色）
- 动画：无

**已送达（delivered）**：
- 图标：`<CircleCheck />` 双对勾
- 颜色：#8696a0（灰色）
- 动画：无

**已读（read）**：
- 图标：`<Select />` 双对勾
- 颜色：#25D366（蓝色）
- 动画：状态切换时淡入效果

**发送失败（failed）**：
- 图标：`<Warning />` 感叹号
- 颜色：#FF3B30（红色）
- 动画：无
- 交互：点击可重试

#### 6.2 Tooltip提示样式

**样式**：
- 背景色：rgba(0, 0, 0, 0.75)
- 文字颜色：#FFFFFF
- 字号：12px
- 内边距：8px 12px
- 圆角：4px
- 箭头：指向状态图标
- 最大宽度：200px

**显示位置**：
- 优先显示在图标上方
- 如果空间不足，显示在下方

#### 6.3 重试发送交互

**触发方式**：
- 点击红色感叹号图标
- 或右键点击消息气泡 → 选择"重试发送"

**重试确认**：
- 方式1：直接重试（推荐）
- 方式2：弹出确认对话框："确认重新发送此消息？"

**重试后状态**：
- 状态更新为"sending"（发送中）
- 原失败消息保留（显示为灰色，已重试标记）
- 新消息添加到最新位置

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 消息状态更新 | 消息状态更新 | message_status_update | messageId: 消息ID, fromStatus: 原状态, toStatus: 新状态, updateTime: 更新耗时 |
| 状态轮询成功 | 状态轮询成功 | status_polling_success | messageId: 消息ID, pollingCount: 轮询次数 |
| 状态轮询失败 | 状态轮询失败 | status_polling_failure | errorMessage: 错误信息 |
| 点击重试发送 | 点击重试发送 | message_retry_click | messageId: 原消息ID, failureReason: 失败原因 |
| 重试发送成功 | 重试发送成功 | message_retry_success | originalMessageId: 原消息ID, newMessageId: 新消息ID |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 接口设计（API Design）

#### 1.1 查询消息状态接口

**接口路径**：`GET /api/v1/im/messages/{messageId}/status`

**请求参数**：
```
无（messageId在路径中）
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messageId": "msg_123456",
    "status": "delivered",
    "sentAt": "2025-01-20T10:30:25Z",
    "deliveredAt": "2025-01-20T10:30:30Z",
    "readAt": null,
    "failedAt": null,
    "errorCode": null,
    "errorMessage": null
  }
}
```

#### 1.2 Webhook状态更新接口（后端实现）

**接口路径**：`POST /api/v1/webhooks/whatsapp/status`

**请求参数**（WhatsApp推送）：
```json
{
  "messageId": "wamid.xyz123",
  "status": "delivered",
  "timestamp": "2025-01-20T10:30:30Z"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success"
}
```

---

### 2. 前端实现细节（Frontend Implementation）

#### 2.1 状态轮询函数

```typescript
const pollMessageStatus = (messageId: string) => {
  let pollingCount = 0
  const maxPollingCount = 120 // 最多轮询120次（10分钟）
  
  const pollingInterval = setInterval(async () => {
    pollingCount++
    
    try {
      const res = await getMessageStatusAPI(messageId)
      const newStatus = res.data.status
      
      // 更新消息状态
      const message = mockMessages.value.find(m => m.id === messageId)
      if (message) {
        message.status = newStatus
        
        // 如果有送达时间、阅读时间，也更新
        if (res.data.deliveredAt) message.deliveredAt = res.data.deliveredAt
        if (res.data.readAt) message.readAt = res.data.readAt
      }
      
      // 如果状态为终态，停止轮询
      if (newStatus === 'read' || newStatus === 'failed') {
        clearInterval(pollingInterval)
        console.log(`Status polling stopped for ${messageId}: ${newStatus}`)
      }
      
      // 如果超过最大轮询次数，停止轮询
      if (pollingCount >= maxPollingCount) {
        clearInterval(pollingInterval)
        console.log(`Status polling timeout for ${messageId}`)
      }
    } catch (error) {
      console.error('Failed to poll message status:', error)
    }
  }, 5000) // 每5秒轮询一次
  
  // 保存轮询定时器ID，用于页面关闭时清理
  pollingTimers.value[messageId] = pollingInterval
}

// 页面关闭时清理所有轮询
onUnmounted(() => {
  Object.values(pollingTimers.value).forEach(timer => {
    clearInterval(timer)
  })
})
```

#### 2.2 状态图标渲染函数

```typescript
const getStatusIcon = (status: string) => {
  switch (status) {
    case 'sending':
      return { component: Clock, color: '#8696a0', tooltip: 'Sending...' }
    case 'sent':
      return { component: Select, color: '#8696a0', tooltip: 'Sent to WhatsApp server' }
    case 'delivered':
      return { component: CircleCheck, color: '#8696a0', tooltip: "Delivered to recipient's device" }
    case 'read':
      return { component: Select, color: '#25D366', tooltip: 'Read by recipient' }
    case 'failed':
      return { component: Warning, color: '#FF3B30', tooltip: 'Failed: Click to retry' }
    default:
      return { component: Clock, color: '#8696a0', tooltip: '' }
  }
}
```

#### 2.3 重试发送函数

```typescript
const retryMessage = async (message: any) => {
  try {
    // 更新原消息状态为"已重试"
    message.retried = true
    message.status = 'retried'
    
    // 重新发送消息
    const res = await sendMessageAPI({
      contactId: message.contactId,
      messageType: message.type,
      content: message.content,
      waAccountType: message.waAccountType,
      waAccountId: message.waAccountId,
      caseId: message.caseId,
      tenantId: message.tenantId,
      queueId: message.queueId
    })
    
    // 添加新消息
    const newMessage = {
      ...message,
      id: res.data.messageId,
      status: 'sent',
      sentAt: res.data.sentAt,
      retried: false,
      originalMessageId: message.id
    }
    
    mockMessages.value.push(newMessage)
    
    ElMessage.success('Message resent successfully')
    
    // 开始轮询新消息状态
    pollMessageStatus(res.data.messageId)
  } catch (error: any) {
    ElMessage.error('Failed to resend message')
  }
}
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC001 | 消息发送中状态 | 无 | 1. 发送消息 | 显示钟表图标，旋转动画 |
| TC002 | 状态更新为已发送 | 消息发送中 | 1. 等待1秒 | 更新为单灰色对勾 |
| TC003 | 状态更新为已送达 | 消息已发送 | 1. 等待5秒 | 更新为双灰色对勾 |
| TC004 | 状态更新为已读 | 消息已送达 | 1. 客户阅读消息 | 更新为双蓝色对勾 |
| TC005 | 消息发送失败 | WA账号异常 | 1. 发送消息 | 显示红色感叹号 |
| TC006 | 鼠标悬停查看状态 | 消息已发送 | 1. 悬停在状态图标上 | 显示Tooltip："Sent to WhatsApp server" |
| TC007 | 点击重试发送 | 消息发送失败 | 1. 点击红色感叹号<br>2. 确认重试 | 创建新消息，状态为"发送中" |
| TC008 | 轮询停止-已读 | 消息已送达，客户已读 | 1. 等待状态更新 | 状态更新为"已读"，停止轮询 |
| TC009 | 轮询停止-失败 | 消息发送失败 | 1. 状态更新为失败 | 显示红色感叹号，停止轮询 |
| TC010 | 轮询超时 | 消息10分钟未变为已读 | 1. 等待10分钟 | 停止轮询，保持当前状态 |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 单对勾 | Single Check | 消息已发送到WhatsApp服务器 |
| 双对勾 | Double Check | 消息已送达客户设备 |
| 蓝色对勾 | Blue Double Check | 消息已被客户阅读 |
| 轮询 | Polling | 定期查询消息状态 |
| Webhook | Webhook | WhatsApp推送状态更新的回调接口 |

### 2. 参考文档（References）

- 主需求文档：`PRD需求文档/CCO催员IM端/WhatsApp信息收发功能PRD.md`
- 发送消息：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/1-催员端发送WA信息PRD.md`
- WhatsApp官方文档：https://developers.facebook.com/docs/whatsapp/

---

**文档版本**：1.0.0  
**最后更新**：2025-01-20  
**文档作者**：CCO产品团队




