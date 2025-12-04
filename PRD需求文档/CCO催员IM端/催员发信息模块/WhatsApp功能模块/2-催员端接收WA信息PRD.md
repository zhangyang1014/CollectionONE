# 催员端接收WhatsApp信息功能 PRD

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

催员端接收WhatsApp信息功能是实现双向沟通的关键能力，允许催员接收客户通过WhatsApp发送的文本、图片、视频和音频消息。系统需要实时接收消息并通知催员，确保沟通及时性。

**业务痛点**：
- 催员需要实时接收客户的回复消息
- 需要支持多种消息类型（文本、图片、视频、音频）
- 需要区分消息来源（客户本人、第三方）
- 需要记录消息接收时间，方便后续跟进

**预期影响的核心指标**：
- 消息接收延迟：≤3秒
- 消息接收成功率：≥99%
- 媒体消息加载成功率：≥95%
- 用户满意度：消息提醒及时，媒体播放流畅

---

### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：接收文本消息**
- **入口**：WhatsApp聊天窗口（自动接收）
- **触发时机**：客户通过WhatsApp发送文字消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 客户在WhatsApp上发送消息
  2. WhatsApp服务端通过Webhook推送消息到CCO后端
  3. 后端保存消息到数据库
  4. 前端通过轮询或WebSocket接收新消息
  5. 消息显示在聊天窗口左侧（灰色气泡）
  6. 显示发送人名称、时间
  7. 如果窗口不在焦点，显示桌面通知

**场景2：接收图片消息**
- **入口**：WhatsApp聊天窗口（自动接收）
- **触发时机**：客户发送图片（如身份证、收入证明等）
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 接收图片消息（同文本消息流程）
  2. 显示图片缩略图（最大200x200px）
  3. 催员点击图片放大查看
  4. 支持下载图片到本地

**场景3：接收视频消息**
- **入口**：WhatsApp聊天窗口（自动接收）
- **触发时机**：客户发送视频
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 接收视频消息
  2. 显示视频缩略图 + 播放按钮
  3. 催员点击播放按钮
  4. 在线播放视频（支持暂停、进度拖动、音量调节）
  5. 支持全屏播放

**场景4：接收音频消息**
- **入口**：WhatsApp聊天窗口（自动接收）
- **触发时机**：客户发送语音消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 接收音频消息
  2. 显示音频播放器（波形图 + 时长）
  3. 催员点击播放按钮
  4. 播放音频（支持暂停、进度拖动、倍速播放）
  5. 显示播放进度

**场景5：接收其他催员发送的消息**
- **入口**：WhatsApp聊天窗口（自动接收）
- **触发时机**：同一案件的其他催员向该联系人发送消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：
  1. 接收其他催员发送的消息
  2. 按时间维度插入到聊天窗口
  3. 显示发送人信息："催员：{催员姓名}（{催员ID}）"
  4. 显示消息状态（如果是WhatsApp消息）
- **说明**：同一案件下所有催员与该联系人的沟通记录按时间顺序展示在同一对话窗口

#### 2.2 主要用户类型

| 用户类型 | 角色标识 | 核心诉求 | 使用场景 |
|---------|---------|---------|---------|
| 催员 | Collector | 及时接收客户回复，了解客户诉求 | 日常催收沟通 |
| 客户 | Customer | 回复催员消息，提供证明材料 | 回复还款计划、发送证明文件 |

---

### 3. 关键业务流程（Business Flow）

#### 3.1 消息接收流程

```
客户在WhatsApp上发送消息
    ↓
WhatsApp服务端接收消息
    ↓
WhatsApp通过Webhook推送消息到CCO后端
    ↓
后端接收Webhook请求
    ↓
解析消息数据：
    - messageId: WhatsApp消息ID
    - from: 发送者手机号
    - type: 消息类型（text/image/video/audio）
    - content: 消息内容或媒体URL
    - timestamp: 发送时间
    ↓
查询联系人信息：
    - 根据手机号查询联系人
    - 确定所属案件和催员
    ↓
保存消息到数据库（messages表）：
    - id: 消息ID
    - contact_id: 联系人ID
    - type: 消息类型
    - content: 消息内容
    - sender_type: "customer"
    - sender_id: 联系人ID
    - sender_name: 联系人姓名
    - channel: "whatsapp"
    - status: "delivered"（已送达）
    - sent_at: 发送时间
    - case_id: 案件ID
    - tenant_id: 甲方ID
    ↓
触发实时通知：
    - 通过WebSocket推送新消息到前端
    - 或前端轮询查询新消息（GET /api/v1/im/messages/new）
    ↓
前端接收新消息
    ↓
根据消息类型渲染：
    - 文本消息：直接显示内容
    - 图片消息：显示缩略图
    - 视频消息：显示视频缩略图 + 播放按钮
    - 音频消息：显示音频播放器
    ↓
消息添加到聊天窗口（左侧灰色气泡）
    ↓
显示发送人信息和时间
    ↓
如果窗口不在焦点，显示桌面通知：
    - 标题："新消息 - {联系人姓名}"
    - 内容：消息预览（文本前50字符）
    - 点击通知跳转到聊天窗口
    ↓
自动滚动到最新消息
    ↓
更新未读消息计数
```

#### 3.2 媒体消息加载流程

```
接收到图片/视频/音频消息
    ↓
解析媒体URL（WhatsApp提供的URL）
    ↓
[图片消息]
    ↓
    加载图片缩略图（200x200px）
    ↓
    显示加载中占位符
    ↓
    [加载成功] → 显示图片缩略图
    ↓
    催员点击图片
    ↓
    打开图片预览弹窗（原始大小）
    ↓
    支持放大、缩小、下载
    ↓
[视频消息]
    ↓
    加载视频第一帧作为缩略图
    ↓
    显示播放按钮图标
    ↓
    催员点击播放
    ↓
    加载视频播放器
    ↓
    在线播放视频（HTML5 video标签）
    ↓
    支持播放控制：暂停/继续、进度拖动、音量调节、全屏
    ↓
[音频消息]
    ↓
    显示音频播放器UI：
    - 播放/暂停按钮
    - 波形图（可选）
    - 音频时长："0:00 / 1:23"
    - 进度条
    ↓
    催员点击播放
    ↓
    加载音频文件
    ↓
    播放音频（HTML5 audio标签）
    ↓
    支持播放控制：暂停/继续、进度拖动、倍速播放（1x/1.5x/2x）
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.1 消息接收规则

**接收范围**：
- ✅ 客户发送的所有消息（文本、图片、视频、音频）
- ✅ 同一案件下其他催员发送的消息
- ✅ 系统自动回复消息（如果有）

**消息来源标识**：
- 客户消息：`sender_type = "customer"`，显示联系人姓名
- 其他催员消息：`sender_type = "collector"`，显示"催员：{姓名}（{ID}）"
- 系统消息：`sender_type = "system"`，显示"系统"

**消息排序**：
- 按发送时间（sent_at）升序排列
- 所有来源的消息统一在一个对话窗口按时间展示

#### 4.2 媒体消息规则

**图片消息**：
- 支持格式：JPG、PNG、GIF、WebP
- 缩略图大小：最大200x200px
- 原图预览：原始大小
- 下载支持：是
- 加载失败处理：显示"图片加载失败"占位符

**视频消息**：
- 支持格式：MP4、WebM、OGG
- 缩略图大小：200x200px
- 播放方式：在线播放（HTML5 video）
- 控制功能：播放/暂停、进度条、音量、全屏
- 加载失败处理：显示"视频加载失败，点击重试"

**音频消息**：
- 支持格式：MP3、OGG、AAC、WAV
- 播放器UI：播放按钮、进度条、时长显示
- 播放控制：播放/暂停、进度拖动、倍速播放（1x/1.5x/2x）
- 加载失败处理：显示"音频加载失败，点击重试"

#### 4.3 实时通知规则

**桌面通知触发条件**：
- 聊天窗口未在焦点时
- 接收到新的客户消息（不包括催员自己发送的消息）
- 浏览器允许通知权限

**桌面通知内容**：
- 标题："新消息 - {联系人姓名}"
- 内容：
  - 文本消息：显示前50字符
  - 图片消息："[图片]"
  - 视频消息："[视频]"
  - 音频消息："[语音]"
- 图标：CCO系统图标

**未读消息计数**：
- 聊天窗口标签页显示未读数（红色徽章）
- 切换到该联系人时自动清零
- 最大显示数：99+

#### 4.4 范围边界

**本次需求范围内**：
- ✅ 接收文本消息
- ✅ 接收图片消息（预览、放大、下载）
- ✅ 接收视频消息（在线播放、播放控制）
- ✅ 接收音频消息（播放、倍速播放）
- ✅ 接收其他催员发送的消息（按时间展示）
- ✅ 桌面通知
- ✅ 未读消息计数
- ✅ 自动滚动到最新消息

**本次需求范围外**：
- ❌ 视频消息发送（仅接收）
- ❌ 音频消息发送（仅接收）
- ❌ 文件消息接收（待实现）
- ❌ 位置消息接收（待实现）
- ❌ 消息搜索功能（待实现）
- ❌ 消息已读回执（WhatsApp不支持客户端消息已读状态）

---

### 5. 数据字段与口径（Data Definition）

#### 5.1 接收消息数据结构

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | String | 是 | 消息ID | "msg_789012" |
| contactId | Integer | 是 | 联系人ID | 101 |
| type | String | 是 | 消息类型：text/image/video/audio | "text" |
| content | String | 是 | 消息内容（文本或媒体URL） | "I will pay tomorrow" |
| senderType | String | 是 | 发送者类型：customer/collector | "customer" |
| senderId | String | 是 | 发送者ID（客户ID或催员ID） | "101" |
| senderName | String | 是 | 发送者姓名 | "John Doe" |
| channel | String | 是 | 渠道：whatsapp | "whatsapp" |
| status | String | 是 | 状态：delivered | "delivered" |
| sentAt | String | 是 | 发送时间（ISO 8601格式） | "2025-01-20T10:35:30Z" |
| caseId | Integer | 是 | 案件ID | 12345 |
| tenantId | Integer | 是 | 甲方ID | 1 |

#### 5.2 媒体消息扩展字段

| 字段名 | 类型 | 必填 | 说明 | 适用类型 |
|--------|------|------|------|---------|
| mediaUrl | String | 是 | 媒体文件URL | image/video/audio |
| thumbnailUrl | String | 否 | 缩略图URL | video |
| mimeType | String | 是 | MIME类型 | image/video/audio |
| fileSize | Integer | 是 | 文件大小（字节） | image/video/audio |
| duration | Integer | 否 | 时长（秒） | video/audio |

---

### 6. 交互与信息展示（UX & UI Brief）

#### 6.1 消息气泡样式（接收的消息）

**气泡样式**：
- 位置：左侧对齐
- 背景色：#FFFFFF（白色）
- 边框：1px solid #E5E5EA（浅灰色）
- 文字颜色：#000000（黑色）
- 圆角：8px
- 内边距：8px 12px
- 最大宽度：70%
- 阴影：轻微阴影

**发送人信息显示**（气泡上方）：
- 客户消息：显示联系人姓名"{联系人姓名}"
- 其他催员消息：显示"催员：{催员姓名}（{催员ID}）"
- 系统消息：显示"系统"

**时间显示**（气泡下方）：
- 格式："10:35:30"
- 颜色：#8E8E93（灰色）
- 字号：12px

#### 6.2 图片消息样式

**缩略图**：
- 大小：最大200x200px（保持原始比例）
- 圆角：8px
- 边框：1px solid #E5E5EA
- 加载中：显示骨架屏
- 加载失败：显示"图片加载失败"占位符

**预览弹窗**：
- 背景：半透明黑色遮罩（opacity: 0.8）
- 图片：居中显示原始大小
- 控制按钮：关闭、下载、放大、缩小
- 关闭方式：点击遮罩、按ESC键、点击关闭按钮

#### 6.3 视频消息样式

**视频缩略图**：
- 大小：200x200px
- 圆角：8px
- 播放按钮：居中显示，半透明白色圆形背景 + 播放图标
- 时长显示：右下角显示"1:23"

**视频播放器**：
- 控制栏：播放/暂停、进度条、音量、全屏
- 进度条：拖动支持
- 加载状态：显示加载动画
- 播放失败：显示"视频加载失败，点击重试"

#### 6.4 音频消息样式

**音频播放器**：
- 布局：水平布局
- 播放按钮：左侧，圆形按钮
- 进度条：中间，可拖动
- 时长显示：右侧，"0:35 / 1:23"
- 倍速按钮：右侧，"1x"/"1.5x"/"2x"切换
- 波形图：可选，显示在进度条位置

#### 6.5 桌面通知样式

**通知内容**：
- 标题："新消息 - {联系人姓名}"
- 内容：消息预览（文本前50字符）或"[图片]"/"[视频]"/"[语音]"
- 图标：CCO系统图标
- 点击行为：聚焦到聊天窗口并滚动到最新消息

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 接收到新消息 | 消息接收 | message_received | messageType: 消息类型, senderType: 发送者类型 |
| 图片加载成功 | 图片加载成功 | image_loaded | imageUrl: 图片URL |
| 图片加载失败 | 图片加载失败 | image_load_failed | errorMessage: 错误信息 |
| 视频播放 | 视频播放 | video_play | videoUrl: 视频URL |
| 音频播放 | 音频播放 | audio_play | audioUrl: 音频URL, speed: 播放倍速 |
| 桌面通知显示 | 桌面通知显示 | desktop_notification_shown | contactName: 联系人姓名 |
| 点击桌面通知 | 点击桌面通知 | desktop_notification_clicked | contactId: 联系人ID |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 接口设计（API Design）

#### 1.1 查询新消息接口

**接口路径**：`GET /api/v1/im/messages/new`

**请求参数**：
```
?contactId=101&lastMessageId=msg_123456&limit=20
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messages": [
      {
        "id": "msg_789012",
        "contactId": 101,
        "type": "text",
        "content": "I will pay tomorrow",
        "senderType": "customer",
        "senderId": "101",
        "senderName": "John Doe",
        "channel": "whatsapp",
        "status": "delivered",
        "sentAt": "2025-01-20T10:35:30Z",
        "caseId": 12345,
        "tenantId": 1
      }
    ],
    "hasMore": false,
    "unreadCount": 1
  }
}
```

#### 1.2 Webhook接收消息接口（后端实现）

**接口路径**：`POST /api/v1/webhooks/whatsapp/messages`

**请求参数**（WhatsApp推送）：
```json
{
  "messageId": "wamid.xyz123",
  "from": "+919876543210",
  "type": "text",
  "text": {
    "body": "I will pay tomorrow"
  },
  "timestamp": "2025-01-20T10:35:30Z"
}
```

**后端处理流程**：
1. 验证Webhook签名
2. 解析消息数据
3. 查询联系人信息
4. 保存消息到数据库
5. 触发实时通知（WebSocket/轮询）

---

### 2. 前端实现细节（Frontend Implementation）

#### 2.1 消息轮询函数

```typescript
const pollNewMessages = async () => {
  if (!selectedContact.value) return
  
  try {
    const res = await getNewMessagesAPI({
      contactId: selectedContact.value.id,
      lastMessageId: mockMessages.value[mockMessages.value.length - 1]?.id,
      limit: 20
    })
    
    if (res.data.messages && res.data.messages.length > 0) {
      // 添加新消息到列表
      mockMessages.value.push(...res.data.messages)
      
      // 更新未读数
      unreadCount.value = res.data.unreadCount
      
      // 显示桌面通知
      if (!document.hasFocus()) {
        showDesktopNotification(res.data.messages[0])
      }
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('Failed to poll new messages:', error)
  }
}

// 每5秒轮询一次
setInterval(pollNewMessages, 5000)
```

#### 2.2 桌面通知函数

```typescript
const showDesktopNotification = (message: any) => {
  // 检查浏览器支持
  if (!('Notification' in window)) {
    return
  }
  
  // 请求通知权限
  if (Notification.permission === 'default') {
    Notification.requestPermission()
  }
  
  // 显示通知
  if (Notification.permission === 'granted') {
    const title = `新消息 - ${selectedContact.value.name}`
    const body = message.type === 'text' 
      ? message.content.substring(0, 50) 
      : `[${getMessageTypeLabel(message.type)}]`
    
    const notification = new Notification(title, {
      body: body,
      icon: '/cco-logo.png'
    })
    
    notification.onclick = () => {
      window.focus()
      scrollToBottom()
      notification.close()
    }
  }
}

const getMessageTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'image': '图片',
    'video': '视频',
    'audio': '语音'
  }
  return labels[type] || type
}
```

#### 2.3 媒体消息渲染函数

```typescript
// 图片消息
const renderImageMessage = (message: any) => {
  return `
    <div class="image-message" @click="showImagePreview('${message.content}')">
      <img 
        :src="${message.content}" 
        :alt="Image"
        style="max-width: 200px; max-height: 200px; border-radius: 8px;"
        @error="handleImageError"
      />
    </div>
  `
}

// 视频消息
const renderVideoMessage = (message: any) => {
  return `
    <div class="video-message">
      <video 
        :src="${message.content}" 
        controls
        style="max-width: 300px; border-radius: 8px;"
      >
        Your browser does not support the video tag.
      </video>
    </div>
  `
}

// 音频消息
const renderAudioMessage = (message: any) => {
  return `
    <div class="audio-message">
      <audio 
        :src="${message.content}" 
        controls
        style="width: 300px;"
      >
        Your browser does not support the audio element.
      </audio>
      <div class="audio-speed-control">
        <button @click="changeSpeed('1x')">1x</button>
        <button @click="changeSpeed('1.5x')">1.5x</button>
        <button @click="changeSpeed('2x')">2x</button>
      </div>
    </div>
  `
}
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC001 | 接收文本消息 | 聊天窗口已打开 | 1. 客户发送文本消息 | 消息显示在左侧灰色气泡，显示发送人和时间 |
| TC002 | 接收图片消息 | 聊天窗口已打开 | 1. 客户发送图片 | 显示图片缩略图（200x200px） |
| TC003 | 点击图片放大 | 已接收图片消息 | 1. 点击图片缩略图 | 打开预览弹窗，显示原始大小 |
| TC004 | 下载图片 | 图片预览弹窗已打开 | 1. 点击下载按钮 | 图片下载到本地 |
| TC005 | 接收视频消息 | 聊天窗口已打开 | 1. 客户发送视频 | 显示视频缩略图 + 播放按钮 |
| TC006 | 播放视频 | 已接收视频消息 | 1. 点击播放按钮 | 视频开始播放，显示控制栏 |
| TC007 | 接收音频消息 | 聊天窗口已打开 | 1. 客户发送语音 | 显示音频播放器 |
| TC008 | 播放音频 | 已接收音频消息 | 1. 点击播放按钮 | 音频开始播放，显示进度 |
| TC009 | 音频倍速播放 | 音频正在播放 | 1. 点击"1.5x"按钮 | 音频以1.5倍速播放 |
| TC010 | 接收其他催员消息 | 聊天窗口已打开 | 1. 其他催员发送消息 | 消息按时间插入，显示"催员：{姓名}（{ID}）" |
| TC011 | 桌面通知 | 聊天窗口未在焦点 | 1. 客户发送消息 | 显示桌面通知 |
| TC012 | 点击桌面通知 | 桌面通知已显示 | 1. 点击通知 | 窗口聚焦，滚动到最新消息 |
| TC013 | 未读消息计数 | 聊天窗口未在焦点 | 1. 接收3条新消息 | 未读数显示"3"（红色徽章） |
| TC014 | 清除未读数 | 未读数 > 0 | 1. 切换到该联系人 | 未读数清零 |
| TC015 | 媒体加载失败 | 网络异常 | 1. 接收图片消息<br>2. 图片加载失败 | 显示"图片加载失败"占位符 |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| Webhook | Webhook | WhatsApp推送消息的回调接口 |
| 桌面通知 | Desktop Notification | 浏览器桌面通知API |
| 倍速播放 | Playback Speed | 音频以不同速度播放（1x/1.5x/2x） |

### 2. 参考文档（References）

- 主需求文档：`PRD需求文档/CCO催员IM端/WhatsApp信息收发功能PRD.md`
- 发送消息：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/1-催员端发送WA信息PRD.md`
- 消息状态记录：`PRD需求文档/CCO催员IM端/WhatsApp功能模块/3-记录WA信息发送状态PRD.md`

---

**文档版本**：1.0.0  
**最后更新**：2025-01-20  
**文档作者**：CCO产品团队




