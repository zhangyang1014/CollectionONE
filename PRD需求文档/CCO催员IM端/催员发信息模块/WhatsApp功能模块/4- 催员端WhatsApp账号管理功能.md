**文档版本**：1.1.0
**最后更新**：2025-12-03（新增技术实现注意事项）
**文档作者**：CCO产品团队

# 催员端WhatsApp账号管理功能 PRD

## 添加个人WA和个人WA掉线处理

## 一、产品需求（Product Requirements）

### 1. 项目背景与目标（Background & Goals）

催员端WhatsApp账号管理功能允许催员绑定和管理个人WhatsApp账号，作为公司WA的补充发送渠道。通过CWA云设备绑定机制，催员可以快速添加最多3个个人WA账号，并在账号掉线时可进行重新绑定或绑定新的账号。

**业务痛点**：
- 个人账号养号时间短，公式wa可以进行养号后再分配给催员
- 公司WA账号数量有限，催员需要使用个人WA提高触达率
- 账号掉线后无法及时发现和处理
- 需要明确的账号状态标识和操作指引

**预期影响的核心指标**：
- 个人WA绑定成功率：≥90%
- 账号掉线检测延迟：≤30秒
- 重新绑定成功率：≥85%
- 用户操作满意度：绑定流程简单，状态提示清晰

----
### 2. 业务场景与用户画像（Business Scenario & User）

#### 2.1 典型使用场景

**场景1：添加个人WA账号**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域 → 点击"+"号按钮
- **触发时机**：催员需要绑定新的个人WhatsApp账号
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：1. 点击"+"号按钮
	2. 系统验证个人WA账号额度（最多3个）
	3. 如果已达上限，提示："Maximum 3 personal WhatsApp accounts allowed."，操作结束
	4. 如果未达上限，调用CWA服务端API"新增WA云设备"
	5. 服务端返回云设备ID和待绑定二维码
	6. 弹窗显示二维码给催员
	7. 催员打开WhatsApp → 设置 → 已连接的设备 → 连接设备
	8. 使用WhatsApp扫描二维码
	9. 轮询查询绑定状态（每2秒）
	10. 绑定成功：Toast提示"绑定成功"（Binding successful），自动关闭弹窗，个人WA区域新增账号头像，默认选中新账号
	11. 绑定失败：显示CWA后端返回的错误信息
	12. 绑定超时（120秒）：显示提示"Binding timeout. Please try again."可点击刷新二维码
- **额度限制**：每个催员最多绑定3个个人WA账号

**场景2：查看个人WA账号列表**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域
- **触发时机**：催员需要查看已绑定的个人WA账号
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：1. 查看个人WA区域
	2. 显示已绑定账号列表（最多3个）
	3. 每个账号显示：- WA头像（圆形，40x40px），悬浮出现
		- 手机号
		- 在线状态（绿色圆点 = 在线，红色图标 = 掉线）
	4. 当前选中账号：绿色边框高亮
	5. 显示"+"号按钮（如果未达上限）

**场景3：切换个人WA账号**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域
- **触发时机**：催员需要使用不同的个人WA发送消息
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：1. 点击其他个人WA账号头像
	2. 检查账号状态（是否在线）
	3. 如果在线，切换成功，绿色边框移动到新账号
	4. 如果掉线，显示提示："Account is offline. Please rebind or select another account."

**场景4：个人WA掉线检测**
- **入口**：系统后台定期检测（每30秒）
- **触发时机**：个人WA账号状态变为"unpaired"（掉线）
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：1. 系统后台定期检测WA账号状态（每30秒）
	2. 检测到个人WA账号状态变为"unpaired"
	3. 更新账号状态到数据库
	4. 前端获取WA账号列表时发现掉线账号
	5. 在掉线账号头像上显示掉线标识：- 半透明灰色遮罩（opacity: 0.5）
		- 右上角显示红色警告图标
	6. 鼠标悬停提示："The account has disconnected. Click to rebind or create a new account."

**场景5：个人WA掉线重新绑定**
- **入口**：催员工作台 → WhatsApp标签页 → 个人WA区域 → 点击掉线的WA账号头像
- **触发时机**：个人WA账号掉线后需要重新绑定
- **所在页面**：IM面板 - WhatsApp聊天窗口
- **流程节点**：1. 点击掉线的账号头像
	2. 弹出选择对话框：- 标题："WhatsApp account has disconnected."
		- 调用API：`POST /api/v1/wa/devices/{deviceId}/rebind`
		- 使用原云设备ID
		- 生成新的二维码
		- 显示二维码绑定弹窗
		- 催员扫码绑定
		- 绑定成功后更新账号状态为"paired"
		- Toast提示："绑定成功"
	3. **选择"取消"**：- 关闭弹窗
		- 账号保持掉线状态

#### 2.2 主要用户类型


|用户类型|角色标识|核心诉求|使用场景|
|:-:|:-:|:-:|:-:|
|催员|Collector|快速绑定个人WA，提高消息触达率|日常催收沟通|


----


### 3. 关键业务流程（Business Flow）

#### 3.0 企业WA账号分配和状态更新流程（新增）

```mermaid
==== 控台端：分配企业WA账号 ====
管理员在控台端操作（具体流程待实现）
    ↓
选择催员或团队
    ↓
分配指定数量的企业WA账号
    ↓
调用API：POST /api/v1/wa/enterprise/allocate
请求参数：
    - collectorId: 催员ID
    - count: 分配数量
    - timezone: 机构时区（例如："Asia/Kolkata"）
    ↓
CWA服务端创建企业WA云设备
    ↓
返回数据：
    - deviceIds: 分配的设备ID列表
    - allocatedAt: 分配时间（机构时区）
    - monthlyCount: 本月已分配总数
    ↓
数据库记录：
    - collector_id: 催员ID
    - device_id: 设备ID
    - allocated_month: 分配月份（YYYY-MM，按机构时区）
    - status: "paired"（初始状态）
    - allocated_at: 分配时间
    ↓
==== IM端：显示企业WA账号状态 ====
    ↓
催员打开IM端WhatsApp标签页
    ↓
调用API：GET /api/v1/wa/enterprise/accounts
    ↓
返回数据：
    - activeCount: 当前活跃账号数（status = "paired"）
    - monthlyAllocatedCount: 本月已分配总数
    - currentMonth: 当前月份（YYYY-MM，按机构时区）
    - timezone: 机构时区
    ↓
显示在企业WA区域：
    - 绿色WA图标
    - 数字显示："{activeCount} / {monthlyAllocatedCount}"
    - 示例："5 / 8"
    ↓
==== 企业WA账号掉线检测 ====
    ↓
系统后台定时任务（每30秒执行）
    ↓
调用CWA API查询所有企业设备状态
API：GET /api/v1/wa/devices/status?type=enterprise
    ↓
检测到状态变为"unpaired"
    ↓
更新数据库：
    - status: "unpaired"
    - unpaired_at: NOW()
    ↓
触发实时通知（WebSocket推送到前端）
    ↓
前端更新显示：
    - 重新计算activeCount（减1）
    - monthlyAllocatedCount保持不变
    - 数字更新：从"5 / 8"变为"4 / 8"
    ↓
[如果activeCount = 0]
    → 显示警告："所有企业账号已掉线，请联系管理员"
```

#### 3.1 添加个人WA账号流程

```
催员点击个人WA区域的"+"号
    ↓
前端验证账号数量
    ↓
[已有3个账号] → 提示："Maximum 3 personal WhatsApp accounts allowed."
    ↓
[未达上限] → 调用API：POST /api/v1/wa/devices/create
请求参数：
    - collectorId: 催员ID
    - deviceType: "personal_wa"
    ↓
CWA服务端创建云设备
    ↓
返回数据：
    - deviceId: 云设备ID（例如："device_abc123"）
    - qrCode: Base64编码的二维码图片
    - expiresAt: 二维码过期时间（通常5分钟）
    ↓
前端显示二维码绑定弹窗：
    - 标题："绑定个人WhatsApp账号"
    - 二维码图片（居中显示，300x300px）
    - 操作说明：
      "1. 打开WhatsApp → 设置 → 已连接的设备
       2. 点击"连接设备"
       3. 扫描上方二维码"
    - 状态提示："等待扫码绑定..."
    - 倒计时：显示剩余时间"4:59"
    - 刷新按钮：二维码过期后可刷新
    - 取消按钮：关闭弹窗
    ↓
启动绑定状态轮询（每2秒）
    ↓
调用API：GET /api/v1/wa/devices/{deviceId}/status
    ↓
返回状态：
    - status: "pending"（待绑定）/ "paired"（已绑定）/ "failed"（绑定失败）
    - phoneNumber: 绑定的手机号（如果已绑定）
    - errorMessage: 错误信息（如果失败）
    ↓
[绑定成功：status = "paired"]
    - Toast提示："绑定成功"（Binding successful）
    - 自动关闭弹窗
    - 调用API获取最新的WA账号列表
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

#### 3.2 个人WA掉线检测流程

```mermaid
系统后台定时任务（每30秒执行）
    ↓
调用CWA API查询所有云设备状态
API：GET /api/v1/wa/devices/status
    ↓
CWA返回所有设备状态列表：
[
  {
    "deviceId": "device_abc123",
    "status": "paired",
    "phoneNumber": "+919876543210",
    "lastSeen": "2025-01-20T10:30:00Z"
  },
  {
    "deviceId": "device_xyz789",
    "status": "unpaired",
    "phoneNumber": "+919876543211",
    "lastSeen": "2025-01-20T09:15:00Z"
  }
]
    ↓
遍历所有设备，检测状态变化
    ↓
[发现状态变为"unpaired"]
    ↓
更新数据库中的WA账号状态：
    - status: "unpaired"
    - unpairedAt: NOW()
    ↓
触发实时通知（WebSocket推送到前端）
    ↓
前端接收掉线通知
    ↓
更新UI显示：
    - 在该账号头像上添加掉线标识
    - 半透明灰色遮罩（opacity: 0.5）
    - 右上角显示红色警告图标
    - 如果当前正在使用该账号，自动切换到可用账号
    ↓
鼠标悬停提示："账号已经掉线，点击后重新绑定或绑定新账号"
```

#### 3.3 个人WA重新绑定流程

```
催员点击掉线的账号头像
    ↓
调用API：POST /api/v1/wa/devices/{deviceId}/rebind
请求参数：
    - deviceId: 原云设备ID
    ↓
CWA服务端重新生成二维码（使用原设备ID）
    ↓
返回数据：
    - deviceId: 原云设备ID
    - qrCode: 新的Base64编码二维码
    - expiresAt: 二维码过期时间
    ↓
显示二维码绑定弹窗：
    - 标题："Rebind WhatsApp Account"
    - 二维码图片（居中显示，300x300px）
    - 操作说明：
      "1. Open WhatsApp → Settings → Linked Devices
       2. Tap 'Link a Device'
       3. Scan the QR code above"
    - 状态提示："Waiting for scanning..."
    - 倒计时：显示剩余时间"4:59"
    - 刷新按钮：二维码过期后可刷新
    - 取消按钮：关闭弹窗
    ↓
启动绑定状态轮询（每2秒）
    ↓
[绑定成功]
    - 更新账号状态为"paired"
    - 移除掉线标识
    - Toast提示："Binding successful"
    - 自动关闭弹窗
    ↓
[绑定失败]
    - 显示CWA返回的错误信息
    - 提供"重试"按钮
    ↓
[超时或取消]
    - 关闭弹窗
    - 账号保持掉线状态
```

---

### 4. 业务规则与边界（Business Rules & Scope）

#### 4.0 企业WA账号管理规则

**账号分配来源**：
- 来源：控台端"CWA企业WA分配"功能（具体待实现）
- 分配权限：仅管理员可分配
- 分配对象：催员或催员团队
- 分配方式：按需分配，无固定上限

**账号状态统计规则**：
- **活跃账号数**：实时统计status = "paired"的账号数量
- **本月分配数**：统计当前自然月分配的账号总数（包括已掉线的）
- **自然月计算**：
  - 按照机构配置的时区计算（例如：亚洲/加尔各答 UTC+5:30）
  - 每个自然月1日00:00（机构时区）重置计数
  - 示例：2025-01-01 00:00 (Asia/Kolkata) → 2025-02-01 00:00 (Asia/Kolkata)

**显示格式**：
- 格式："{活跃数} / {本月分配数}"
- 示例："5 / 8"（当前5个活跃，本月已分配8个）
- 位置：企业WA区域，绿色图标右侧
- 颜色：
  - 活跃数 > 0：绿色
  - 活跃数 = 0：红色（警告状态）

**掉线处理规则**：
- 企业WA账号掉线后：
  - 活跃账号数减1
  - 本月分配数不变
  - 如果活跃账号数 = 0，显示警告提示
- 企业WA账号无法在IM端重新绑定（需要控台端处理）
- 掉线账号占用本月分配数（用于成本核算）

**月度统计规则**：
- 每月1日00:00（机构时区）：
  - 本月分配数重置为0
  - 活跃账号数保持不变（继续使用上月分配的活跃账号）
- 跨月账号：
  - 上月分配且仍活跃的账号，不计入新月的分配数
  - 仅新月新分配的账号计入新月分配数

#### 4.1 个人WA账号管理规则

**账号数量限制**：
- 最大数量：3个
- 包含状态：在线账号 + 掉线账号总数 ≤ 3
- 超限提示："Maximum 3 personal WhatsApp accounts allowed."

**账号额度显示**：
- 显示格式："可用数 / 总数"
- 示例："2 / 3"（表示已绑定2个，还可以绑定1个）
- 位置：个人WA区域标题旁

**账号状态定义**：

| 状态码 | 状态名称 | 说明 | 图标显示 |
|--------|---------|------|---------|
| pending | 待绑定 | 二维码已生成，等待扫码 | 无（不在列表中显示） |
| paired | 已绑定 | 账号正常可用 | 正常头像，绿色边框（选中时） |
| unpaired | 已掉线 | 账号断开连接 | 半透明遮罩 + 红色警告图标 |
| binding | 绑定中 | 扫码后等待确认 | 加载动画 |
| failed | 绑定失败 | 绑定过程失败 | 无（不在列表中显示） |

**掉线账号处理规则**：
- 掉线账号保留在列表中（不自动删除）
- 显示半透明灰色遮罩（opacity: 0.5）
- 右上角显示红色警告图标
- 不可用于发送消息
- 点击后可选择重新绑定或绑定新账号
- 占用账号额度（计入3个上限）

**账号解绑规则**：
- 暂不支持手动解绑（后续迭代实现）
- 仅通过掉线自动标记为"unpaired"

#### 4.2 二维码生成和绑定规则

**二维码有效期**：
- 默认：5分钟（300秒）
- 过期后显示："QR code expired. Click to refresh."
- 刷新按钮：点击重新生成二维码

**绑定状态轮询规则**：
- 轮询间隔：2秒
- 最大轮询次数：60次（120秒）
- 轮询停止条件：
  - 状态变为"paired"（成功）
  - 状态变为"failed"（失败）
  - 超过最大轮询次数（超时）
  - 用户关闭弹窗

**绑定超时处理**：
- 超时时间：120秒
- 超时提示："Binding timeout. Please try again."
- 操作：自动关闭弹窗，停止轮询

#### 4.3 范围边界

**本次需求范围内**：

**企业WA账号管理**：
- ✅ 查看企业WA账号状态（活跃数 / 本月分配数）
- ✅ 企业WA账号掉线检测（每30秒）
- ✅ 企业WA账号掉线后状态更新（活跃数减少）
- ✅ 企业WA账号全部掉线警告提示
- ✅ 按机构时区计算自然月
- ✅ 月度统计数据展示
- ✅ 账号状态WebSocket实时推送

**个人WA账号管理**：
- ✅ 添加个人WA账号（最多3个）
- ✅ 二维码绑定流程
- ✅ 绑定状态轮询
- ✅ 个人WA掉线检测（每30秒）
- ✅ 掉线账号标识显示
- ✅ 个人WA重新绑定
- ✅ 账号额度限制和提示
- ✅ 绑定超时处理

**本次需求范围外**：

**企业WA账号管理（待实现）**：
- ❌ 控台端企业WA账号分配功能（具体待实现）
- ❌ 企业WA账号详细列表查看（待实现）
- ❌ 企业WA账号重新绑定（需控台端处理）
- ❌ 企业WA账号成本报表（待实现）

**个人WA账号管理（待实现）**：
- ❌ 手动解绑个人WA账号（待实现）
- ❌ 账号重命名功能（待实现）
- ❌ 账号备注功能（待实现）
- ❌ 批量管理个人WA账号（待实现）

---

### 5. 数据字段与口径（Data Definition）

#### 5.0 企业WA账号数据字段（新增）

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deviceId | String | 是 | 云设备ID | "enterprise_device_001" |
| collectorId | String | 是 | 催员ID | "collector_001" |
| accountType | String | 是 | 账号类型（固定为"enterprise"） | "enterprise" |
| status | String | 是 | 账号状态：paired/unpaired | "paired" |
| allocatedMonth | String | 是 | 分配月份（YYYY-MM，按机构时区） | "2025-01" |
| allocatedAt | String | 是 | 分配时间（ISO 8601） | "2025-01-15T10:30:00+05:30" |
| unpairedAt | String | 否 | 掉线时间 | "2025-01-20T14:20:10+05:30" |
| lastSeen | String | 否 | 最后在线时间 | "2025-01-21T15:30:00+05:30" |
| timezone | String | 是 | 机构时区 | "Asia/Kolkata" |
| createdAt | String | 是 | 创建时间 | "2025-01-15T10:30:00+05:30" |
| updatedAt | String | 是 | 更新时间 | "2025-01-21T14:20:10+05:30" |

#### 5.1 个人WA账号数据字段

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deviceId | String | 是 | 云设备ID | "device_abc123" |
| collectorId | String | 是 | 催员ID | "collector_001" |
| phoneNumber | String | 否 | 绑定的手机号 | "+919876543210" |
| accountName | String | 否 | 账号名称（可选） | "My WA" |
| status | String | 是 | 账号状态：pending/paired/unpaired/binding/failed | "paired" |
| qrCode | String | 否 | 二维码（Base64，仅绑定时） | "data:image/png;base64,..." |
| qrCodeExpiresAt | String | 否 | 二维码过期时间 | "2025-01-20T10:35:00Z" |
| pairedAt | String | 否 | 绑定成功时间 | "2025-01-20T10:30:25Z" |
| unpairedAt | String | 否 | 掉线时间 | "2025-01-21T14:20:10Z" |
| lastSeen | String | 否 | 最后在线时间 | "2025-01-21T15:30:00Z" |
| createdAt | String | 是 | 创建时间 | "2025-01-20T10:25:00Z" |
| updatedAt | String | 是 | 更新时间 | "2025-01-21T14:20:10Z" |

---

### 6. 交互与信息展示（UX & UI Brief）

#### 6.0 企业WA区域布局（新增）

**区域位置**：
- 位置：WhatsApp聊天窗口上方，个人WA区域上方
- 布局：水平排列

**区域组成**：
- 绿色WA图标：圆形，40x40px
- 账号状态数字（右侧）：
  - 格式："{活跃数} / {本月分配数}"
  - 示例："5 / 8"
  - 字体：14px，加粗
  - 颜色：
    - 活跃数 > 0：#25D366（绿色）
    - 活跃数 = 0：#FF3B30（红色）

**数字显示样式**：

**正常状态（活跃数 > 0）**：
```
┌─────────────────┐
│    5 / 8      │
│  WA  ↑   ↑      │
│      活跃 本月   │
└─────────────────┘
```
- 数字颜色：绿色 #25D366
- 鼠标悬停提示："当前可用5个企业账号，本月已分配8个"

**警告状态（活跃数 = 0）**：
```mermaid
┌─────────────────┐
│    0 / 8  ⚠️  │
│  WA  ↑   ↑      │
│      活跃 本月   │
└─────────────────┘
```
- 数字颜色：红色 #FF3B30
- 右侧显示警告图标
- 鼠标悬停提示："所有企业账号已掉线，请联系管理员"

**月初重置显示**：
- 每月1日00:00（机构时区）后首次访问：
  - 显示通知："已进入新月，企业账号分配数已重置"
  - 数字示例："5 / 0"（5个活跃，新月尚未分配）

#### 6.1 个人WA区域布局

**区域位置**：
- 位置：WhatsApp聊天窗口底部，公司WA右边
- 布局：水平排列

**区域组成**：
- 标题："个人WA"
- 账号额度："2 / 3"（右侧）
- 账号列表：水平排列，每个账号40x40px圆形头像
- "+"号按钮：圆形，40x40px，虚线边框（如果未达上限）

#### 6.2 账号头像样式

**正常状态（paired）**：
- 形状：圆形，40x40px
- 边框：2px solid transparent（默认）
- 边框（选中）：2px solid #25D366（绿色）
- 背景色：头像图片或默认渐变色
- 在线标识：右下角绿色圆点（6x6px）
- 鼠标悬停显示：手机号（例如："+91 98765 43210"）

**掉线状态（unpaired）**：
- 形状：圆形，40x40px
- 遮罩：半透明灰色，opacity: 0.5
- 警告图标：右上角红色感叹号（12x12px）
- 边框：2px solid #FF3B30（红色）
- 鼠标悬停提示："The account has disconnected. Click to rebind or create a new account."

**"+"号按钮样式**：
- 形状：圆形，40x40px
- 边框：2px dashed #CCCCCC（虚线）
- 背景色：透明
- 图标："+"号，灰色
- 鼠标悬停：边框变为实线，#25D366（绿色）

#### 6.3 二维码绑定弹窗样式

**弹窗尺寸**：
- 宽度：500px
- 高度：自适应

**弹窗内容**：
- 标题："Bind Personal WhatsApp Account"（添加账号）或"Rebind WhatsApp Account"（重新绑定）
- 二维码图片：居中显示，300x300px
- 操作说明（文字）：
  ```
  1. Open WhatsApp → Settings → Linked Devices
  2. Tap 'Link a Device'
  3. Scan the QR code above
  ```
- 状态提示："Waiting for scanning..."
- 倒计时："4:59"（右上角，绿色）
- 刷新按钮："Refresh QR Code"（二维码下方，仅过期后显示）
- 取消按钮："Cancel"（底部右侧，灰色）

**倒计时颜色**：
- > 60秒：绿色 #25D366
- 30-60秒：橙色 #FF9500
- < 30秒：红色 #FF3B30

---

## 二、数据需求（Data Requirements）

### 1. 埋点需求（Tracking Requirements）

| 触发时间点/条件 | 埋点中文说明 | 埋点英文ID | 关键属性 |
|----------------|------------|-----------|---------|
| 查看企业WA账号状态 | 查看企业WA账号状态 | enterprise_wa_view | activeCount: 活跃数, monthlyCount: 本月分配数 |
| 企业WA账号掉线 | 企业WA账号掉线 | enterprise_wa_offline | deviceId: 设备ID, remainingCount: 剩余活跃数 |
| 企业WA账号全部掉线 | 企业WA账号全部掉线警告 | enterprise_wa_all_offline | monthlyAllocatedCount: 本月分配总数 |
| 点击添加个人WA | 点击添加个人WA | personal_wa_add_click | currentCount: 当前账号数 |
| 二维码生成成功 | 二维码生成成功 | qr_code_generated | deviceId: 设备ID |
| 二维码生成失败 | 二维码生成失败 | qr_code_generate_failed | errorMessage: 错误信息 |
| 个人WA绑定成功 | 个人WA绑定成功 | personal_wa_bind_success | deviceId: 设备ID, phoneNumber: 手机号, bindTime: 绑定耗时 |
| 个人WA绑定失败 | 个人WA绑定失败 | personal_wa_bind_fail | deviceId: 设备ID, errorMessage: 错误信息 |
| 个人WA绑定超时 | 个人WA绑定超时 | personal_wa_bind_timeout | deviceId: 设备ID |
| 检测到个人WA掉线 | 个人WA掉线 | personal_wa_offline | deviceId: 设备ID, phoneNumber: 手机号 |
| 点击重新绑定 | 点击重新绑定 | personal_wa_rebind_click | deviceId: 设备ID, action: rebind/newAccount |
| 重新绑定成功 | 重新绑定成功 | personal_wa_rebind_success | deviceId: 设备ID |
| 刷新二维码 | 刷新二维码 | qr_code_refresh | deviceId: 设备ID |

---

## 三、技术部分描述（Technical Requirements / TRD）

### 1. 接口设计（API Design）

#### 1.0 企业WA账号相关接口（新增）

##### 1.0.1 获取企业WA账号状态接口

**接口路径**：`GET /api/v1/wa/enterprise/accounts`

**请求参数**：
```
?collectorId=collector_001
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activeCount": 5,
    "monthlyAllocatedCount": 8,
    "currentMonth": "2025-01",
    "timezone": "Asia/Kolkata",
    "accounts": [
      {
        "deviceId": "enterprise_device_001",
        "status": "paired",
        "allocatedAt": "2025-01-15T10:30:00+05:30",
        "lastSeen": "2025-01-21T15:30:00+05:30"
      },
      {
        "deviceId": "enterprise_device_002",
        "status": "unpaired",
        "allocatedAt": "2025-01-15T10:30:00+05:30",
        "unpairedAt": "2025-01-20T14:20:10+05:30",
        "lastSeen": "2025-01-20T14:19:55+05:30"
      }
    ]
  }
}
```

##### 1.0.2 分配企业WA账号接口（控台端使用，待实现）

**接口路径**：`POST /api/v1/wa/enterprise/allocate`

**请求参数**：
```json
{
  "collectorId": "collector_001",
  "count": 3,
  "timezone": "Asia/Kolkata"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceIds": [
      "enterprise_device_001",
      "enterprise_device_002",
      "enterprise_device_003"
    ],
    "allocatedAt": "2025-01-15T10:30:00+05:30",
    "allocatedMonth": "2025-01",
    "monthlyAllocatedCount": 3
  }
}
```

#### 1.1 创建个人WA云设备接口

**接口路径**：`POST /api/v1/wa/devices/create`

**请求参数**：
```json
{
  "collectorId": "collector_001",
  "deviceType": "personal_wa"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_abc123",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg...",
    "expiresAt": "2025-01-20T10:35:00Z"
  }
}
```

#### 1.2 查询云设备绑定状态接口

**接口路径**：`GET /api/v1/wa/devices/{deviceId}/status`

**请求参数**：无（deviceId在路径中）

**响应数据**（待绑定）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_abc123",
    "status": "pending",
    "phoneNumber": null
  }
}
```

**响应数据**（已绑定）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_abc123",
    "status": "paired",
    "phoneNumber": "+919876543210",
    "pairedAt": "2025-01-20T10:30:25Z"
  }
}
```

**响应数据**（绑定失败）：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_abc123",
    "status": "failed",
    "errorMessage": "Invalid QR code scan"
  }
}
```

#### 1.3 重新绑定云设备接口

**接口路径**：`POST /api/v1/wa/devices/{deviceId}/rebind`

**请求参数**：
```json
{
  "deviceId": "device_abc123"
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "device_abc123",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg...",
    "expiresAt": "2025-01-21T14:25:00Z"
  }
}
```

#### 1.4 查询个人WA账号列表接口

**接口路径**：`GET /api/v1/wa/accounts/personal`

**请求参数**：
```
?collectorId=collector_001
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accounts": [
      {
        "deviceId": "device_abc123",
        "phoneNumber": "+919876543210",
        "accountName": "My WA",
        "status": "paired",
        "pairedAt": "2025-01-20T10:30:25Z",
        "lastSeen": "2025-01-21T15:30:00Z"
      },
      {
        "deviceId": "device_xyz789",
        "phoneNumber": "+919876543211",
        "accountName": null,
        "status": "unpaired",
        "pairedAt": "2025-01-19T09:15:00Z",
        "unpairedAt": "2025-01-21T14:20:10Z",
        "lastSeen": "2025-01-21T14:19:55Z"
      }
    ],
    "totalCount": 2,
    "maxCount": 3
  }
}
```

---

### 2. 前端实现细节（Frontend Implementation）

#### 2.0 企业WA账号状态管理函数（新增）

##### 2.0.1 获取企业WA账号状态

```typescript
// 企业WA账号状态数据
const enterpriseWAStatus = ref({
  activeCount: 0,
  monthlyAllocatedCount: 0,
  currentMonth: '',
  timezone: 'Asia/Kolkata'
})

// 获取企业WA账号状态
const fetchEnterpriseWAStatus = async () => {
  try {
    const res = await getEnterpriseWAAccountsAPI({
      collectorId: currentCollector.value.id
    })
    
    enterpriseWAStatus.value = {
      activeCount: res.data.activeCount,
      monthlyAllocatedCount: res.data.monthlyAllocatedCount,
      currentMonth: res.data.currentMonth,
      timezone: res.data.timezone
    }
    
    // 埋点
    trackEvent('enterprise_wa_view', {
      activeCount: res.data.activeCount,
      monthlyCount: res.data.monthlyAllocatedCount
    })
  } catch (error: any) {
    console.error('Failed to fetch enterprise WA status:', error)
  }
}

// 计算显示文本
const enterpriseWADisplayText = computed(() => {
  return `${enterpriseWAStatus.value.activeCount} / ${enterpriseWAStatus.value.monthlyAllocatedCount}`
})

// 计算悬停提示
const enterpriseWATooltip = computed(() => {
  if (enterpriseWAStatus.value.activeCount === 0) {
    return '所有企业账号已掉线，请联系管理员'
  }
  return `当前可用${enterpriseWAStatus.value.activeCount}个企业账号，本月已分配${enterpriseWAStatus.value.monthlyAllocatedCount}个`
})
```

##### 2.0.2 企业WA账号掉线处理

```typescript
// 监听企业WA账号掉线通知
websocket.on('enterprise_wa_offline', (data) => {
  // 更新活跃账号数
  enterpriseWAStatus.value.activeCount = Math.max(0, enterpriseWAStatus.value.activeCount - 1)
  
  // 埋点
  trackEvent('enterprise_wa_offline', {
    deviceId: data.deviceId,
    remainingCount: enterpriseWAStatus.value.activeCount
  })
  
  // 如果活跃账号数为0，显示警告
  if (enterpriseWAStatus.value.activeCount === 0) {
    ElNotification.error({
      title: '企业账号全部掉线',
      message: '所有企业WhatsApp账号已断开连接，请联系管理员处理',
      duration: 0, // 不自动关闭
      showClose: true
    })
    
    // 埋点
    trackEvent('enterprise_wa_all_offline', {
      monthlyAllocatedCount: enterpriseWAStatus.value.monthlyAllocatedCount
    })
  } else {
    ElNotification.warning({
      title: '企业账号掉线',
      message: `1个企业账号已断开，剩余${enterpriseWAStatus.value.activeCount}个可用`,
      duration: 5000
    })
  }
})

// 组件挂载时获取状态
onMounted(() => {
  fetchEnterpriseWAStatus()
  
  // 定期刷新状态（每60秒）
  const refreshInterval = setInterval(() => {
    fetchEnterpriseWAStatus()
  }, 60000)
  
  // 清理
  onUnmounted(() => {
    if (refreshInterval) {
      clearInterval(refreshInterval)
    }
  })
})
```

#### 2.1 添加个人WA函数

```typescript
const addPersonalWA = async () => {
  // 1. 检查账号数量
  if (personalWAAccounts.value.length >= 3) {
    ElMessage.warning('Maximum 3 personal WhatsApp accounts allowed.')
    return
  }
  
  // 2. 调用创建云设备API
  try {
    const res = await createWADeviceAPI({
      collectorId: currentCollector.value.id,
      deviceType: 'personal_wa'
    })
    
    // 3. 显示二维码绑定弹窗
    qrCodeDialogVisible.value = true
    currentDeviceId.value = res.data.deviceId
    qrCodeData.value = res.data.qrCode
    qrCodeExpiresAt.value = res.data.expiresAt
    
    // 4. 启动倒计时
    startQRCodeCountdown(res.data.expiresAt)
    
    // 5. 启动绑定状态轮询
    startBindingStatusPolling(res.data.deviceId)
  } catch (error: any) {
    ElMessage.error('Failed to generate QR code. Please try again.')
  }
}
```

#### 2.2 绑定状态轮询函数

```typescript
const startBindingStatusPolling = (deviceId: string) => {
  let pollingCount = 0
  const maxPollingCount = 60 // 最多轮询60次（120秒）
  
  const pollingInterval = setInterval(async () => {
    pollingCount++
    
    try {
      const res = await getDeviceStatusAPI(deviceId)
      const status = res.data.status
      
      if (status === 'paired') {
        // 绑定成功
        clearInterval(pollingInterval)
        qrCodeDialogVisible.value = false
        ElMessage.success('Binding successful')
        
        // 刷新个人WA账号列表
        await refreshPersonalWAAccounts()
        
        // 默认选中新绑定的账号
        selectedWAAccount.value = {
          type: 'personal',
          id: deviceId
        }
      } else if (status === 'failed') {
        // 绑定失败
        clearInterval(pollingInterval)
        ElMessage.error(res.data.errorMessage || 'Binding failed')
      }
      
      // 超时处理
      if (pollingCount >= maxPollingCount) {
        clearInterval(pollingInterval)
        qrCodeDialogVisible.value = false
        ElMessage.warning('Binding timeout. Please try again.')
      }
    } catch (error) {
      console.error('Failed to poll binding status:', error)
    }
  }, 2000) // 每2秒轮询一次
}
```

#### 2.3 个人WA掉线检测函数

```typescript
// 后端定时任务（每30秒执行）
const detectWAAccountOffline = async () => {
  // 1. 调用CWA API查询所有设备状态
  const devices = await cwaAPI.getDevicesStatus()
  
  // 2. 检查状态变化
  for (const device of devices) {
    const existingAccount = await getWAAccountByDeviceId(device.deviceId)
    
    if (existingAccount && existingAccount.status === 'paired' && device.status === 'unpaired') {
      // 3. 检测到掉线
      await updateWAAccountStatus(device.deviceId, 'unpaired')
      
      // 4. 触发WebSocket通知前端
      websocket.emit('wa_account_offline', {
        deviceId: device.deviceId,
        phoneNumber: device.phoneNumber,
        unpairedAt: new Date().toISOString()
      })
    }
  }
}

// 前端接收掉线通知
websocket.on('wa_account_offline', (data) => {
  // 更新UI显示掉线标识
  const account = personalWAAccounts.value.find(a => a.deviceId === data.deviceId)
  if (account) {
    account.status = 'unpaired'
    account.unpairedAt = data.unpairedAt
    
    // 如果当前正在使用该账号，自动切换到可用账号
    if (selectedWAAccount.value?.id === data.deviceId) {
      switchToAvailableAccount()
    }
    
    ElNotification.warning({
      title: 'WhatsApp账号已掉线',
      message: `账号 ${data.phoneNumber} 已断开连接`,
      duration: 5000
    })
  }
})
```

---

## 四、测试用例（Test Cases）

### 1. 功能测试用例

| 测试用例ID | 测试场景 | 前置条件 | 测试步骤 | 预期结果 |
|----------|---------|---------|---------|---------|
| TC000 | 查看企业WA账号状态 | 已分配企业账号 | 1. 打开WhatsApp标签页 | 显示"活跃数 / 本月分配数"（例如："5 / 8"） |
| TC000.1 | 企业WA账号掉线 | 有5个活跃账号 | 1. 后端检测到1个账号掉线 | 数字从"5 / 8"变为"4 / 8" |
| TC000.2 | 企业WA全部掉线 | 仅剩1个活跃账号 | 1. 最后1个账号掉线 | 显示"0 / 8"（红色），弹出警告通知 |
| TC000.3 | 月初状态重置 | 跨月访问 | 1. 新月1日首次访问 | 显示通知"已进入新月"，数字示例："5 / 0" |
| TC000.4 | 悬停提示（正常） | 有5个活跃账号 | 1. 鼠标悬停在企业WA数字上 | 提示："当前可用5个企业账号，本月已分配8个" |
| TC000.5 | 悬停提示（警告） | 0个活跃账号 | 1. 鼠标悬停在企业WA数字上 | 提示："所有企业账号已掉线，请联系管理员" |
| TC001 | 添加第1个个人WA | 当前0个账号 | 1. 点击"+"按钮 | 显示二维码绑定弹窗 |
| TC002 | 添加超限（第4个） | 当前已有3个账号 | 1. 点击"+"按钮 | 提示："Maximum 3 personal WhatsApp accounts allowed." |
| TC003 | 二维码倒计时 | 二维码弹窗已打开 | 1. 观察倒计时 | 从"4:59"倒数到"0:00"，颜色从绿色变为红色 |
| TC004 | 二维码过期刷新 | 二维码已过期 | 1. 点击"刷新二维码" | 生成新的二维码，重新开始倒计时 |
| TC005 | 扫码绑定成功 | 二维码弹窗已打开 | 1. 扫描二维码<br>2. WhatsApp确认绑定 | Toast提示"绑定成功"，弹窗关闭，新账号显示在列表 |
| TC006 | 绑定超时 | 120秒未绑定 | 1. 等待120秒 | 提示"Binding timeout"，弹窗关闭 |
| TC007 | 切换个人WA账号 | 已有2个在线账号 | 1. 点击另一个账号 | 绿色边框切换到新账号 |
| TC008 | 悬浮显示手机号 | 已有在线账号 | 1. 鼠标悬停在账号头像上 | 显示手机号（例如："+91 98765 43210"） |
| TC009 | 账号掉线检测 | 账号在线 | 1. 后端检测到掉线 | 账号显示掉线标识（半透明 + 红色图标） |
| TC010 | 点击掉线账号重新绑定 | 账号已掉线 | 1. 点击掉线账号<br>2. 扫描二维码 | 显示二维码弹窗，绑定成功后恢复在线 |
| TC011 | 默认选中新账号 | 绑定成功 | 1. 查看选中状态 | 新绑定的账号有绿色边框 |

---

## 五、附录（Appendix）

### 1. 术语表（Glossary）

| 术语 | 英文 | 说明 |
|------|------|------|
| 云设备 | Cloud Device | CWA提供的虚拟WhatsApp设备 |
| 二维码 | QR Code | 用于绑定WhatsApp账号的二维码 |
| Unpaired | Unpaired | WhatsApp账号掉线状态 |
| Paired | Paired | WhatsApp账号已绑定状态 |

### 2. 参考文档（References）

- 主需求文档：`PRD需求文档/CCO催员IM端/WhatsApp信息收发功能PRD.md`
- CWA API文档：待补充
- 控台端企业WA分配功能：待实现（待补充文档链接）
- 实现完成报告：`说明文档/前端/WhatsApp个人账号管理功能优化完成报告.md`
- BUG修复报告：`说明文档/前端/WhatsApp功能BUG修复完成报告.md`

### 3. 技术实现注意事项（Implementation Notes）⚠️

**重要提醒**：以下是在实际开发中发现并修复的关键问题，开发者必须注意：

#### 3.1 避免重复函数定义 

**问题**：在同一个组件中定义了两个同名函数（如`refreshQRCode`），导致后定义的函数覆盖前面的，造成功能失效。

**错误示例**：
```typescript
// ❌ 错误：第一个定义（正确的实现）
const refreshQRCode = async () => {
  const res = await rebindWADevice(currentDeviceId.value)
  qrCodeData.value = res.qrCode
  // ...真实逻辑
}

// ... 后面又定义了一次（Mock代码）
const refreshQRCode = () => {
  // ❌ 这个会覆盖上面的！导致功能失效
  qrCodePattern.value = Array.from({ length: 25 }, () => Math.random() > 0.5)
}
```

**解决方案**：
- 使用IDE的查找功能搜索重复的函数名
- 删除所有Mock/测试代码，只保留真实实现
- 启用ESLint检查重复定义

#### 3.2 组件生命周期钩子只能定义一次 

**问题**：Vue 3中，同一个生命周期钩子（如`onUnmounted`）被定义多次时，只有最后一个会生效，导致资源清理不完整，造成内存泄漏。

**错误示例**：
```typescript
// ❌ 第一个onUnmounted（会被覆盖，永远不执行）
onUnmounted(() => {
  if (limitTimer) {
    clearInterval(limitTimer) // ❌ 永远不会执行！
  }
})

// ❌ 第二个onUnmounted（实际生效的）
onUnmounted(() => {
  stopMessagePolling()
  // 缺少清理limitTimer！
})
```

**解决方案**：
```typescript
// ✅ 正确：只定义一次，清理所有资源
onUnmounted(() => {
  // 清理所有定时器
  stopMessagePolling()
  stopAllMessageStatusPolling()
  stopBindingStatusPolling()
  
  if (limitTimer) {
    clearInterval(limitTimer)
    limitTimer = null
  }
  
  // 关闭所有弹窗
  qrCodeDialogVisible.value = false
  rebindDialogVisible.value = false
  
  // 移除事件监听
  window.removeEventListener('online', handleNetworkOnline)
})
```

#### 3.3 错误处理必须关闭Loading消息 

**问题**：在try-catch中创建loading消息，如果在catch块中没有关闭，会导致loading一直显示。

**错误示例**：
```typescript
// ❌ 错误
try {
  const loadingMsg = ElMessage.loading('Processing...')
  await someAPI()
  loadingMsg.close()
} catch (error) {
  // ❌ loadingMsg在这里不可访问，无法关闭！
  ElMessage.error('Failed')
}
```

**解决方案**：
```typescript
// ✅ 正确
let loadingMsg: any = null
try {
  loadingMsg = ElMessage.loading('Processing...')
  await someAPI()
  loadingMsg.close()
  loadingMsg = null
} catch (error) {
  // ✅ 确保关闭loading
  if (loadingMsg) {
    loadingMsg.close()
  }
  ElMessage.error('Failed')
}
```

#### 3.4 绑定成功后必须获取完整数据 

**问题**：绑定成功后直接设置`selectedWAAccount`只包含type和id，缺少name、phoneNumber等字段。

**错误示例**：
```typescript
// ❌ 错误：数据不完整
if (status === 'paired') {
  selectedWAAccount.value = {
    type: 'personal',
    id: deviceId
    // ❌ 缺少name、phoneNumber等字段
  }
}
```

**解决方案**：
```typescript
// ✅ 正确：从列表中获取完整信息
if (status === 'paired') {
  await refreshPersonalWAAccounts()
  
  const newAccount = personalWAAccounts.value.find(a => a.deviceId === deviceId)
  if (newAccount) {
    selectedWAAccount.value = {
      type: 'personal',
      id: newAccount.deviceId,
      name: newAccount.accountName || newAccount.phoneNumber
    }
  }
}
```

#### 3.5 二维码过期后必须停止轮询 

**问题**：二维码倒计时归零后，绑定状态轮询还在继续，浪费资源。

**解决方案**：
```typescript
// ✅ 倒计时归零时停止轮询
if (seconds <= 0) {
  qrCodeCountdown.value = 0
  if (qrCodeCountdownTimer) {
    clearInterval(qrCodeCountdownTimer)
  }
  // ✅ 停止绑定轮询
  stopBindingStatusPolling()
}
```

#### 3.6 使用明确的类型定义，避免any 

**问题**：使用`any`类型会失去TypeScript的类型检查，容易出错。

**错误示例**：
```typescript
// ❌ 错误
const selectWAAccount = async (account: any, type: 'platform' | 'personal') => {
  // 无类型检查
}
```

**解决方案**：
```typescript
// ✅ 正确：定义明确的接口
interface WAAccountSelection {
  id: string
  name?: string
}

const selectWAAccount = async (
  account: WAAccountSelection, 
  type: 'platform' | 'personal'
) => {
  if (!account?.id) {
    ElMessage.warning('Invalid account')
    return
  }
  // ...
}
```

#### 3.7 添加网络异常处理 

**必须实现**：
1. 连续错误计数（建议MAX=3）
2. 达到上限后停止轮询
3. 监听网络恢复事件（`window.addEventListener('online')`）
4. 网络恢复后自动重启轮询

**参考实现**：
```typescript
let consecutiveErrors = 0
const MAX_CONSECUTIVE_ERRORS = 3

const pollNewMessages = async () => {
  try {
    // ... 轮询逻辑
    consecutiveErrors = 0 // 成功后重置
  } catch (error) {
    consecutiveErrors++
    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
      stopMessagePolling()
      ElMessage.warning('网络连接异常，消息轮询已暂停')
    }
  }
}

// 监听网络恢复
window.addEventListener('online', () => {
  consecutiveErrors = 0
  startMessagePolling()
  ElMessage.success('网络已恢复')
})
```

#### 3.8 快速切换时的防抖处理 

**问题**：用户快速切换联系人时可能创建多个轮询实例。

**解决方案**：
```typescript
watch(selectedContactId, (newId, oldId) => {
  // 先停止旧的轮询
  stopMessagePolling()
  
  // 延迟启动新轮询，避免抖动
  if (newId) {
    setTimeout(() => {
      if (selectedContactId.value === newId) {
        startMessagePolling()
      }
    }, 100)
  }
})
```

#### 3.9 代码质量检查清单 ✅

在提交代码前，必须检查：

- [ ] 无重复的函数定义（使用全局搜索检查）
- [ ] 只有一个`onMounted`和一个`onUnmounted`
- [ ] 所有定时器都在`onUnmounted`中清理
- [ ] 所有弹窗都在`onUnmounted`中关闭
- [ ] 所有事件监听都在`onUnmounted`中移除
- [ ] Loading消息在catch块中能够关闭
- [ ] 二维码过期后停止轮询
- [ ] 使用明确的TypeScript类型，避免`any`
- [ ] 添加网络异常处理和恢复机制
- [ ] 快速操作时的防抖处理
- [ ] 运行Linter检查无错误

---


