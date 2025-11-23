# 调试登录问题 - Network 面板检查指南

## 📋 问题

登录后跳回登录页，需要检查 Network 面板中的登录请求。

---

## 🔍 如何查看 Network 面板

### 步骤1: 打开开发者工具

1. 按 `F12` 或 `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
2. 点击 **Network** 标签页

### 步骤2: 清空并开始记录

1. 点击 **清除** 按钮（🚫 图标）清空之前的请求
2. 确保 **Preserve log** 已勾选（保留日志）
3. 确保 **Disable cache** 已勾选（禁用缓存）

### 步骤3: 执行登录操作

1. 填写登录表单
2. 点击"登录"按钮
3. 观察 Network 面板中的请求

---

## 🔎 需要检查的关键请求

### 1. 登录 API 请求

**请求名称**: `/api/v1/im/auth/login`  
**请求方法**: `POST`  
**状态码**: 应该是 `200` (成功)

**检查点**:
- ✅ 请求是否发送？
- ✅ 状态码是什么？（200 = 成功，401 = 未授权，500 = 服务器错误）
- ✅ 请求体（Request Payload）是否正确？
- ✅ 响应体（Response）是什么？

**预期请求体**:
```json
{
  "tenantId": "1",
  "collectorId": "BTQ001",
  "password": "123456"
}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "MOCK_IM_TOKEN_...",
    "user": {
      "id": "BTQ001",
      "collectorId": "BTQ001",
      "tenantId": "1",
      "username": "催员BTQ001",
      "role": "collector",
      "whatsappConnected": true
    }
  }
}
```

### 2. 路由跳转请求

**请求名称**: `/im/workspace`  
**请求方法**: `GET`  
**状态码**: 应该是 `200` (成功)

**检查点**:
- ✅ 是否发送了跳转请求？
- ✅ 状态码是什么？
- ✅ 是否有重定向？

---

## 🐛 常见问题排查

### 问题1: 登录 API 请求未发送

**症状**: Network 面板中没有 `/api/v1/im/auth/login` 请求

**可能原因**:
- 表单验证失败
- JavaScript 错误阻止了请求
- 按钮点击事件未触发

**解决**:
1. 查看 Console 面板，检查是否有错误
2. 检查表单验证是否通过
3. 检查登录按钮是否被禁用

### 问题2: 登录 API 返回错误状态码

**症状**: `/api/v1/im/auth/login` 返回 401、403、404 或 500

**可能原因**:
- 账号密码错误
- 后端服务未启动
- API 路径错误

**解决**:
1. 检查后端服务是否运行（端口 8080）
2. 检查 API 路径是否正确
3. 检查请求体格式是否正确

### 问题3: 登录 API 返回成功，但状态未更新

**症状**: `/api/v1/im/auth/login` 返回 200，但页面未跳转

**可能原因**:
- 响应格式不正确
- 状态更新逻辑有问题
- 路由守卫拦截

**解决**:
1. 查看响应体（Response），检查格式是否正确
2. 查看 Console 面板，检查是否有 `[IM Store] 登录成功` 日志
3. 查看 Application → Local Storage，检查是否有 `im_token` 和 `im_user`

### 问题4: 路由跳转被拦截

**症状**: 登录成功，但跳转到 `/im/workspace` 后又被重定向回 `/im/login`

**可能原因**:
- 路由守卫检查失败
- localStorage 状态未同步
- Token 格式不正确

**解决**:
1. 查看 Console 面板，检查路由守卫日志
2. 查看 Application → Local Storage，检查 `im_token` 和 `im_user` 是否存在
3. 检查路由守卫逻辑

---

## 📊 Network 面板检查清单

### 登录前

- [ ] 打开 Network 面板
- [ ] 清空请求记录
- [ ] 勾选 "Preserve log"
- [ ] 勾选 "Disable cache"

### 登录时

- [ ] 点击"登录"按钮
- [ ] 观察是否有 `/api/v1/im/auth/login` 请求
- [ ] 检查请求状态码（应该是 200）
- [ ] 检查请求体（Request Payload）是否正确
- [ ] 检查响应体（Response）是否正确

### 登录后

- [ ] 检查是否有 `/im/workspace` 请求
- [ ] 检查是否有重定向到 `/im/login`
- [ ] 查看 Console 面板的日志
- [ ] 查看 Application → Local Storage

---

## 🔧 详细检查步骤

### 步骤1: 检查登录请求

1. 在 Network 面板中，找到 `/api/v1/im/auth/login` 请求
2. 点击该请求，查看详情：
   - **Headers**: 检查请求头是否正确
   - **Payload**: 检查请求体是否正确
   - **Response**: 检查响应是否正确
   - **Preview**: 查看格式化的响应

### 步骤2: 检查响应格式

**正确的响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "MOCK_IM_TOKEN_...",
    "user": {
      "id": "BTQ001",
      "collectorId": "BTQ001",
      "tenantId": "1",
      "username": "催员BTQ001",
      "role": "collector",
      "whatsappConnected": true
    }
  }
}
```

**如果响应格式不正确**:
- 检查后端 API 实现
- 检查响应拦截器逻辑

### 步骤3: 检查 Console 日志

打开 Console 面板，查找以下日志：

```
[IM Store] 登录成功，状态已更新: { isLoggedIn: true, hasToken: true, hasUser: true }
[Login] 登录成功，准备跳转，当前状态: { isLoggedIn: true, hasToken: true, hasUser: true }
[IM路由守卫] 当前路径: /im/workspace
[IM路由守卫] 状态检查: { isLoggedIn: true, hasToken: true, hasUser: true, ... }
[IM路由守卫] 检查通过，放行到: /im/workspace
```

**如果缺少某些日志**:
- 说明某个步骤未执行
- 根据缺少的日志定位问题

### 步骤4: 检查 Local Storage

1. 打开 Application 面板
2. 展开 Local Storage → `http://localhost:5173`
3. 检查是否有以下键值对：
   - `im_token`: 应该是一个字符串（Token）
   - `im_user`: 应该是一个 JSON 对象（用户信息）

**如果 Local Storage 中没有数据**:
- 说明状态保存失败
- 检查 `imUserStore.login()` 方法

---

## 📸 截图示例

### 正确的登录请求

**Request**:
```
POST /api/v1/im/auth/login
Status: 200 OK
```

**Request Payload**:
```json
{
  "tenantId": "1",
  "collectorId": "BTQ001",
  "password": "123456"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "MOCK_IM_TOKEN_...",
    "user": { ... }
  }
}
```

---

## 🚨 常见错误响应

### 错误1: 404 Not Found

**响应**:
```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/im/auth/login"
}
```

**原因**: API 路径错误或后端服务未启动

**解决**: 检查后端服务是否运行在端口 8080

### 错误2: 401 Unauthorized

**响应**:
```json
{
  "code": 401,
  "message": "未授权"
}
```

**原因**: 账号密码错误或 Token 无效

**解决**: 检查账号密码是否正确

### 错误3: 500 Internal Server Error

**响应**:
```json
{
  "code": 500,
  "message": "服务器错误"
}
```

**原因**: 后端服务器错误

**解决**: 检查后端日志，查看具体错误

---

## 📝 调试信息收集

请提供以下信息以便调试：

1. **Network 面板截图**:
   - 登录请求的详情（Headers、Payload、Response）

2. **Console 面板日志**:
   - 所有 `[IM Store]` 和 `[IM路由守卫]` 相关的日志

3. **Local Storage 内容**:
   - `im_token` 的值
   - `im_user` 的值

4. **错误信息**:
   - 任何红色错误信息

---

**创建时间**: 2025-11-22  
**用途**: 调试登录跳转问题

