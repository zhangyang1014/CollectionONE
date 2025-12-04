# IM端双时区显示功能说明

## 功能概述

在IM端（催员工作台）顶部显示两个时区和时间：
1. **我的时区**：催员登录的机构所在时区
2. **客户时区**：客户所在时区（同甲方时区）

## 实现逻辑

### 1. 后端时区信息返回

**文件**: `backend-java/src/main/java/com/cco/controller/MockImController.java`

在IM登录接口中，根据`tenantId`返回时区信息：

- **tenantId = 1 (BTQ - 墨西哥)**:
  - 机构时区: `America/Mexico_City` (CST, UTC-6)
  - 甲方时区: `America/Mexico_City` (CST, UTC-6)

- **tenantId = 2 (BTSK - 印度)**:
  - 机构时区: `Asia/Kolkata` (IST, UTC+5:30)
  - 甲方时区: `Asia/Kolkata` (IST, UTC+5:30)

登录响应中包含以下字段：
```json
{
  "user": {
    "agencyTimezone": "America/Mexico_City",
    "agencyTimezoneShort": "CST",
    "agencyTimezoneOffset": -6,
    "tenantTimezone": "America/Mexico_City",
    "tenantTimezoneShort": "CST",
    "tenantTimezoneOffset": -6
  }
}
```

### 2. 前端时区显示逻辑

**文件**: `frontend/src/views/im/CollectorWorkspace.vue`

#### 2.1 时区初始化

在组件挂载时（`onMounted`），调用`initTimezones()`函数：

1. **我的时区（机构时区）**:
   - 优先使用用户信息中的`agencyTimezone`
   - 如果没有，根据用户的`tenantId`获取默认时区

2. **客户时区（甲方时区）**:
   - 如果有选中的案件，使用案件的`tenant_id`获取时区
   - 如果没有选中案件，使用用户信息中的`tenantTimezone`
   - 如果都没有，根据用户的`tenantId`获取默认时区

#### 2.2 时区动态更新

当选中案件变化时（`watch selectedCase`），自动更新客户时区：

- 如果新选中的案件有`tenant_id`，根据该`tenant_id`更新客户时区
- 如果没有选中案件，恢复为用户信息中的甲方时区

#### 2.3 时间实时更新

使用`setInterval`每秒更新两个时区的时间显示。

### 3. 时区工具函数

**文件**: `frontend/src/utils/timezone.ts`

提供以下工具函数：

- `getTimezoneByTenantId(tenantId)`: 根据甲方ID获取时区信息
- `getTimezoneByOffset(offset)`: 根据UTC偏移量获取IANA时区名称
- `getTimezoneShort(timezone)`: 根据IANA时区名称获取时区缩写

### 4. 用户接口扩展

**文件**: `frontend/src/stores/imUser.ts`

在`ImUser`接口中添加了时区相关字段：

```typescript
export interface ImUser {
  // ... 其他字段
  agencyTimezone?: string        // 机构时区（IANA时区名称）
  agencyTimezoneShort?: string   // 机构时区缩写
  agencyTimezoneOffset?: number  // 机构时区UTC偏移量
  tenantTimezone?: string        // 甲方时区（IANA时区名称）
  tenantTimezoneShort?: string   // 甲方时区缩写
  tenantTimezoneOffset?: number  // 甲方时区UTC偏移量
}
```

## UI显示

在IM端顶部右侧，时区显示区域包含两个时区项：

```
[时钟图标] 我的时区: 14:30:25 [CST]  [时钟图标] 客户时区: 14:30:25 [CST]
```

每个时区项包含：
- 时钟图标
- 时区标签（"我的时区"或"客户时区"）
- 当前时间（实时更新，格式：HH:mm:ss）
- 时区缩写（可悬浮查看完整时区名称）

## 样式说明

时区显示区域使用以下样式类：

- `.timezone-display`: 时区容器，横向排列两个时区项
- `.timezone-item`: 单个时区项容器
- `.timezone-label`: 时区标签文字
- `.timezone-time`: 时间显示（等宽字体）
- `.timezone-short`: 时区缩写（带背景色，可悬浮）

## 测试场景

### 场景1: BTQ机构催员登录
- 我的时区: `America/Mexico_City` (CST)
- 客户时区: `America/Mexico_City` (CST)
- 两个时区显示相同时间

### 场景2: BTSK机构催员登录
- 我的时区: `Asia/Kolkata` (IST)
- 客户时区: `Asia/Kolkata` (IST)
- 两个时区显示相同时间

### 场景3: 切换案件
- 当选中不同甲方的案件时，客户时区会自动更新
- 我的时区保持不变（始终是催员机构时区）

## 注意事项

1. **时区数据来源**:
   - 优先使用后端返回的时区信息
   - 如果没有，使用前端默认映射（根据tenantId）

2. **印度时区处理**:
   - 印度时区是UTC+5:30，在代码中用`5`表示
   - dayjs会自动处理`Asia/Kolkata`时区的正确偏移量

3. **时区更新时机**:
   - 登录时初始化
   - 选中案件变化时更新客户时区
   - 每秒更新时间显示

4. **兼容性**:
   - 如果后端没有返回时区信息，使用默认值
   - 如果案件没有tenant_id，使用用户信息中的甲方时区

## 相关文件

- `backend-java/src/main/java/com/cco/controller/MockImController.java` - 后端登录接口
- `frontend/src/stores/imUser.ts` - 用户状态管理
- `frontend/src/views/im/CollectorWorkspace.vue` - IM端主页面
- `frontend/src/utils/timezone.ts` - 时区工具函数

---

**实现日期**: 2025-01-XX  
**版本**: 1.0.0




























