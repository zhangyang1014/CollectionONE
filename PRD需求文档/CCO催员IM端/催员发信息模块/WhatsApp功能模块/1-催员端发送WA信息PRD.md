# 催员端发送WhatsApp信息功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

催员端发送WhatsApp信息功能是催员与客户沟通的核心能力，支持催员通过公司WA或个人WA账号向客户发送文本和图片消息。本功能需要确保消息成功发送，并提供清晰的发送反馈。

**业务痛点**：
- 催员需要快速向客户发送催收通知和还款提醒
- 需要支持文本和图片两种消息类型（视频和音频仅接收，不发送）
- 需要区分使用公司WA还是个人WA发送
- 需要处理发送失败的各种异常情况

**预期影响的核心指标**：
- 消息发送成功率：≥95%
- 发送响应时间：≤2秒
- 用户操作满意度：发送流程顺畅

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：发送文本消息**
- **入口**：催员工作台 → 选择联系人 → WhatsApp标签页
- **触发时机**：催员需要向客户发送文字消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 输入消息内容（最大1000字符）
  2. 选择WA账号（公司WA/个人WA）
  3. 点击发送按钮（或按Ctrl+Enter）
  4. 显示"发送中"状态（钟表图标）
  5. 发送成功后显示单灰色对勾
  6. 消息添加到聊天窗口（右侧绿色气泡）

**场景2：发送图片消息**
- **入口**：催员工作台 → 选择联系人 → WhatsApp标签页 → 点击"图片"按钮
- **触发时机**：催员需要向客户发送图片（如还款码、合同截图等）
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 点击"图片"按钮
  2. 选择图片文件（JPG/PNG/GIF，最大10MB）
  3. 预览图片（可选）
  4. 点击发送
  5. 图片上传中显示进度
  6. 发送成功后显示图片缩略图（最大200x200px）
  7. 支持点击放大查看

---

### 3. 关键业务流程（Business Flow）

#### 3.1 文本消息发送流程

```
催员输入消息内容
    ↓
前端验证：内容长度（≤1000字符）、内容非空
    ↓
[验证失败] → 显示提示："Message content is invalid"
    ↓
[验证成功] → 获取当前选中的WA账号
    ↓
检查WA账号状态
    ↓
[账号不可用] → 显示错误："WhatsApp online status is abnormal. Please refresh the page."
    ↓
[账号可用] → 点击发送按钮
    ↓
调用发送消息API：POST /api/v1/im/messages/send
请求参数：
    - contactId: 联系人ID
    - messageType: "text"
    - content: 消息内容
    - waAccountType: "platform" / "personal"
    - waAccountId: WA账号ID（个人WA必填）
    - caseId: 案件ID
    - tenantId: 甲方ID
    - queueId: 队列ID
    ↓
显示"发送中"状态（钟表图标）
    ↓
后端处理：
    1. 检查渠道触达限制（每日每案件、每日每联系人、发送时间间隔）
    2. 检查WA账号状态
    3. 调用WhatsApp Business API发送消息
    4. 保存消息记录到数据库
    5. 更新渠道触达限制计数
    ↓
[发送失败] → 返回错误码和错误信息
    - 前端显示红色感叹号
    - 鼠标悬停显示错误详情
    ↓
[发送成功] → 返回消息ID和初始状态
    - 更新消息状态为"sent"（单灰色对勾）
    - 添加消息到聊天窗口
    - 清空输入框
    - 滚动到底部
    ↓
开始轮询消息状态（详见"消息状态记录"子需求）
```

#### 3.2 图片消息发送流程

```
催员点击"图片"按钮
    ↓
打开文件选择器
    ↓
选择图片文件
    ↓
前端验证：
    - 文件格式（JPG/PNG/GIF）
    - 文件大小（≤10MB）
    ↓
[验证失败] → 显示提示："Invalid image format or size exceeds 10MB"
    ↓
[验证成功] → 显示图片预览（可选）
    ↓
点击发送
    ↓
上传图片到文件服务器
    - 显示上传进度条
    - 生成图片URL
    ↓
[上传失败] → 显示提示："Failed to upload image. Please try again."
    ↓
[上传成功] → 调用发送消息API
请求参数：
    - contactId: 联系人ID
    - messageType: "image"
    - content: 图片URL
    - waAccountType: WA账号类型
    - waAccountId: WA账号ID
    - caseId, tenantId, queueId
    ↓
后端处理：同文本消息发送流程
    ↓
发送成功：
    - 显示图片缩略图（200x200px）
    - 显示单灰色对勾
    - 支持点击放大查看
    ↓
发送失败：显示红色感叹号 + 错误提示
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 消息内容规则

**文本消息**：
- 必填：是
- 最大长度：1000字符（硬编码，不可配置）
- 最小长度：1字符（不允许空消息）
- 支持换行：是
- 支持Emoji：是

**图片消息**：
- 支持格式：JPG、PNG、GIF
- 最大大小：10MB
- 尺寸限制：无
- 预览尺寸：最大200x200px（聊天窗口内）
- 上传方式：先上传到文件服务器，再发送图片URL

**不支持发送的消息类型**：
- ❌ 视频消息（仅支持接收）
- ❌ 音频消息（仅支持接收）
- ❌ 文件消息（待实现）
- ❌ 位置消息（待实现）

#### 4.2 WA账号选择规则

**公司WA**：
- 显示标识："公司WA"（不显示账号ID）
- 默认选择：是（优先使用公司WA）
- 可用性：由平台统一管理，催员无需绑定

**个人WA**：
- 显示标识："个人WA（账号ID）"或"个人WA（账号名称）"
- 绑定要求：催员需要先绑定个人WA账号（详见"账号管理"子需求）
- 账号上限：最多3个
- 账号切换：发送前可以手动切换WA账号

**账号自动切换**：
- 如果当前选中的WA账号不可用（掉线/被封），系统自动切换到可用的账号
- 切换时显示提示："Current account is unavailable. Switched to {accountName}."

#### 4.3 渠道触达限制规则

所有WhatsApp消息发送都会经过**CCO渠道触达限制系统**进行校验：

**限制规则**（通过后台配置）：
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

**限制信息显示**：
- 位置：发送按钮左侧
- 格式："已发送数 / 最大限制数"
- 示例："45 / 100"
- 下次可发送时间：如果有间隔限制，显示倒计时"15秒后"

#### 4.4 范围边界

**本次需求范围内**：
- ✅ 文本消息发送
- ✅ 图片消息发送
- ✅ WA账号选择（公司WA/个人WA）
- ✅ 渠道触达限制校验
- ✅ 发送失败错误处理
- ✅ 消息添加到聊天窗口
- ✅ 快捷键支持（Ctrl+Enter）

**本次需求范围外**：
- ❌ 视频消息发送（仅接收）
- ❌ 音频消息发送（仅接收）
- ❌ 消息模板功能（已在其他功能中实现）
- ❌ 消息状态显示（详见"消息状态记录"子需求）
- ❌ 消息接收（详见"接收WA信息"子需求）
- ❌ 个人WA账号绑定（详见"账号管理"子需求）

---

### 5. 数据字段与口径（Data Definition）

#### 5.1 发送消息请求字段

| 字段名 | 类型 | 必填 | 说明 | 来源 |
|--------|------|------|------|------|
| contactId | Integer | 是 | 联系人ID | 前端选择 |
| messageType | String | 是 | 消息类型：text/image | 前端判断 |
| content | String | 是 | 消息内容（文本或图片URL） | 用户输入/上传 |
| waAccountType | String | 是 | WA账号类型：platform/personal | 前端选择 |
| waAccountId | String | 否 | WA账号ID（个人WA必填） | 前端选择 |
| senderId | String | 是 | 发送人催员ID | 系统获取 |
| caseId | Integer | 是 | 案件ID | 前端当前案件 |
| tenantId | Integer | 是 | 甲方ID | 前端当前甲方 |
| queueId | Integer | 是 | 队列ID | 前端当前队列 |

#### 5.2 发送消息响应字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| messageId | String | 消息ID |
| status | String | 初始状态：sent |
| sentAt | String | 发送时间（ISO 8601格式） |
| waAccountType | String | 使用的WA账号类型 |
| waAccountId | String | 使用的WA账号ID |

#### 5.3 错误码列表

| HTTP状态码 | 错误码标识 | 英文错误信息 | 适用场景 |
|-----------|-----------|-------------|---------|
| 400 | INVALID_CONTENT | Message content is invalid | 消息内容为空或超长 |
| 400 | INVALID_MESSAGE_TYPE | Invalid message type | 消息类型不支持 |
| 400 | INVALID_RECIPIENT | Recipient phone number is invalid. Please verify the number. | 接收方号码无效 |
| 403 | DAILY_LIMIT_PER_CASE_EXCEEDED | Daily limit per case exceeded. You have sent {count} messages to this case today. | 超过每日每案件限制 |
| 403 | DAILY_LIMIT_PER_CONTACT_EXCEEDED | Daily limit per contact exceeded. You have sent {count} messages to this contact today. | 超过每日每联系人限制 |
| 403 | SEND_INTERVAL_LIMIT | Send interval limit. Please wait {seconds} seconds before sending again. | 发送时间间隔限制 |
| 500 | WA_ACCOUNT_UNPAIRED | WhatsApp online status is abnormal. Please refresh the page. | WA账号被封或掉线 |
| 500 | NO_AVAILABLE_WA_ACCOUNT | No available WhatsApp account. Please contact administrator. | 无可用的WA账号 |
| 500 | NETWORK_ERROR | Network connection failed. Please check your network and try again. | 网络连接失败 |

---

### 6. 交互与信息展示（UX & UI Brief）

#### 6.1 输入区域

**文本输入框**：
- 类型：多行文本框（textarea）
- 行数：2行（默认）
- 最大高度：自适应（最多显示6行）
- 占位符："输入消息..."
- 字数统计：显示剩余字符数（1000 - 已输入字符数）

**工具栏（左侧）**：
- 模板按钮：点击选择消息模板
- 图片按钮：点击选择图片文件
- 表情按钮：点击选择Emoji表情

**工具栏（右侧）**：
- 渠道限制信息：显示"45 / 100"和"15秒后"
- 发送按钮：绿色按钮，图标 + 文字"发送 (Ctrl+Enter)"

#### 6.2 消息气泡样式（发送的消息）

**气泡样式**：
- 位置：右侧对齐
- 背景色：#DCF8C6（浅绿色，WhatsApp风格）
- 文字颜色：#000000（黑色）
- 圆角：8px
- 内边距：8px 12px
- 最大宽度：70%
- 阴影：轻微阴影

**元数据显示**（气泡下方）：
- WA账号标识："公司WA" 或 "个人WA（账号名）"
- 发送时间："10:30:25"
- 状态图标：单灰色对勾（初始状态）

---

### 7. 配置项与运营开关（Config & Operation Switches）

#### 7.1 消息发送配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| message.text.maxLength | 1000 | 文本消息最大长度（字符），硬编码 |
| message.image.maxSize | 10485760 | 图片最大大小（字节，10MB） |
| message.image.supportedFormats | "jpg,png,gif" | 支持的图片格式 |

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 用户点击发送按钮 | 消息发送 | message_send | messageType: 消息类型, waAccountType: WA账号类型, contentLength: 内容长度 |
| 消息发送成功 | 消息发送成功 | message_send_success | messageId: 消息ID, messageType: 消息类型 |
| 消息发送失败 | 消息发送失败 | message_send_failure | errorCode: 错误码, errorMessage: 错误信息 |
| 图片上传成功 | 图片上传成功 | image_upload_success | imageUrl: 图片URL, imageSize: 图片大小 |
| 图片上传失败 | 图片上传失败 | image_upload_failure | errorMessage: 错误信息 |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 接口设计（API Design）

#### 1.1 发送消息接口

**接口路径**：`POST /api/v1/im/messages/send`

**请求参数**：
```json
{
  "contactId": 1,
  "messageType": "text",
  "content": "您好，请问您什么时候可以还款？",
  "waAccountType": "platform",
  "waAccountId": null,
  "caseId": 12345,
  "tenantId": 1,
  "queueId": 101
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

---

### 2. 前端实现细节（Frontend Implementation）

#### 2.1 发送消息函数

```typescript
const sendMessage = async () => {
  // 1. 验证消息内容
  if (!messageInput.value.trim()) {
    ElMessage.warning('Message content is required')
    return
  }
  
  if (messageInput.value.length > 1000) {
    ElMessage.warning('Message content exceeds 1000 characters')
    return
  }
  
  // 2. 验证联系人
  if (!selectedContact.value) {
    ElMessage.warning('Please select a contact')
    return
  }
  
  // 3. 验证WA账号
  if (!selectedWAAccount.value) {
    ElMessage.warning('Please select a WhatsApp account')
    return
  }
  
  // 4. 调用发送API
  try {
    const res = await sendMessageAPI({
      contactId: selectedContact.value.id,
      messageType: 'text',
      content: messageInput.value,
      waAccountType: selectedWAAccount.value.type,
      waAccountId: selectedWAAccount.value.id,
      caseId: caseData.value.id,
      tenantId: caseData.value.tenant_id,
      queueId: caseData.value.queue_id
    })
    
    // 5. 发送成功，添加到消息列表
    const newMessage = {
      id: res.data.messageId,
      contactId: selectedContact.value.id,
      type: 'text',
      content: messageInput.value,
      senderType: 'collector',
      senderId: currentCollector.value.id,
      senderName: currentCollector.value.name,
      channel: 'whatsapp',
      status: 'sent',
      sentAt: res.data.sentAt,
      waAccountType: res.data.waAccountType,
      waAccountId: res.data.waAccountId,
      tool: res.data.waAccountType === 'platform' ? '公司WA' : `个人WA（${res.data.waAccountId}）`
    }
    
    mockMessages.value.push(newMessage)
    messageInput.value = ''
    
    ElMessage.success('Message sent successfully')
    
    // 6. 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
    
    // 7. 开始轮询状态（详见"消息状态记录"子需求）
    pollMessageStatus(res.data.messageId)
  } catch (error: any) {
    // 8. 错误处理
    handleSendError(error)
  }
}
```

#### 2.2 图片发送函数

```typescript
const handleImageSelect = async (file: any) => {
  // 1. 验证文件格式
  const validFormats = ['image/jpeg', 'image/png', 'image/gif']
  if (!validFormats.includes(file.raw.type)) {
    ElMessage.error('Invalid image format. Only JPG, PNG, GIF are supported.')
    return
  }
  
  // 2. 验证文件大小
  if (file.raw.size > 10485760) { // 10MB
    ElMessage.error('Image size exceeds 10MB limit.')
    return
  }
  
  // 3. 上传图片
  try {
    const uploadRes = await uploadImage(file.raw)
    const imageUrl = uploadRes.data.url
    
    // 4. 发送消息
    const res = await sendMessageAPI({
      contactId: selectedContact.value.id,
      messageType: 'image',
      content: imageUrl,
      waAccountType: selectedWAAccount.value.type,
      waAccountId: selectedWAAccount.value.id,
      caseId: caseData.value.id,
      tenantId: caseData.value.tenant_id,
      queueId: caseData.value.queue_id
    })
    
    // 5. 添加到消息列表
    addImageMessage(res.data, imageUrl)
    
    ElMessage.success('Image sent successfully')
  } catch (error: any) {
    if (error.message?.includes('upload')) {
      ElMessage.error('Failed to upload image. Please try again.')
    } else {
      handleSendError(error)
    }
  }
}
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC001 | 发送文本消息 | 已选择联系人和WA账号 | 1. 输入文本<br>2. 点击发送 | 消息发送成功，显示在聊天窗口右侧，显示单灰色对勾 |
| TC002 | 发送空消息 | 已选择联系人 | 1. 不输入内容<br>2. 点击发送 | 提示："Message content is required" |
| TC003 | 发送超长消息 | 已选择联系人 | 1. 输入1001个字符<br>2. 点击发送 | 提示："Message content exceeds 1000 characters" |
| TC004 | 快捷键发送 | 已选择联系人，已输入内容 | 1. 按Ctrl+Enter | 消息发送成功 |
| TC005 | 发送图片消息 | 已选择联系人和WA账号 | 1. 点击图片按钮<br>2. 选择图片<br>3. 发送 | 图片上传成功，消息发送成功，显示图片缩略图 |
| TC006 | 发送超大图片 | 已选择联系人 | 1. 选择>10MB的图片 | 提示："Image size exceeds 10MB limit." |
| TC007 | 发送不支持格式图片 | 已选择联系人 | 1. 选择BMP格式图片 | 提示："Invalid image format. Only JPG, PNG, GIF are supported." |
| TC008 | 超过每日每案件限制 | 案件今日已发送达到限制 | 1. 发送消息 | 提示："Daily limit per case exceeded. You have sent {count} messages to this case today." |
| TC009 | 发送时间间隔限制 | 刚发送完消息 | 1. 立即再次发送 | 提示："Send interval limit. Please wait {seconds} seconds before sending again." |
| TC010 | WA账号不可用 | 当前WA账号掉线 | 1. 发送消息 | 提示："WhatsApp online status is abnormal. Please refresh the page." |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 公司WA | Platform WA | 企业WhatsApp账号 |
| 个人WA | Personal WA | 个人WhatsApp账号 |
| 渠道触达限制 | Channel Limit | 控制消息发送频率的限制规则 |

### 2. 参考文档（References）

- 主需求文档：`PRD需求文档/CCO催员IM端/WhatsApp信息收发功能PRD.md`
- 消息状态记录：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/3-记录WA信息发送状态PRD.md`
- 账号管理：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/4-催员端账号管理PRD.md`

---

**文档版本**：1.0.0  
**最后更新**：2025-01-20  
**文档作者**：CCO产品团队




