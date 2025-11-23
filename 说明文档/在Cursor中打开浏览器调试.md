# 在 Cursor 中打开浏览器调试

## 📋 概述

本文档介绍如何在 Cursor 中打开浏览器访问前端页面，以及如何使用 BrowserTools MCP 进行浏览器调试。

---

## 🚀 快速打开前端页面

### 方法1: 使用脚本（推荐）⭐

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE
./scripts/open-frontend.sh
```

### 方法2: 使用系统命令

**macOS**:
```bash
open http://localhost:5173
```

**Linux**:
```bash
xdg-open http://localhost:5173
```

**Windows**:
```bash
start http://localhost:5173
```

### 方法3: 手动访问

在浏览器地址栏输入：
```
http://localhost:5173
```

---

## 🔧 使用 BrowserTools MCP 进行调试

### 前提条件

1. ✅ BrowserTools MCP 已配置在 `~/.cursor/mcp.json`
2. ✅ 已在 Cursor 设置中启用 BrowserTools MCP 服务器
3. ✅ 前端服务正在运行（端口 5173）

### 在 Cursor Chat 中使用

BrowserTools MCP 提供以下功能：

#### 1. 打开浏览器并导航到页面

在 Cursor Chat 中尝试：

```
打开浏览器并访问 http://localhost:5173
```

或

```
在浏览器中打开前端页面
```

#### 2. 获取控制台日志

```
获取浏览器控制台的错误日志
```

#### 3. 监控网络请求

```
显示当前页面的网络请求
```

#### 4. 检查页面元素

```
获取页面中所有按钮元素的文本内容
```

#### 5. 执行 JavaScript

```
在浏览器控制台中执行: console.log('Hello from Cursor!')
```

---

## 📊 BrowserTools MCP 可用工具

根据 [BrowserTools 官方文档](https://browsertools.agentdesk.ai/installation)，BrowserTools MCP 提供以下工具：

| 工具 | 功能 | 示例 |
|------|------|------|
| `navigate` | 导航到指定URL | `navigate http://localhost:5173` |
| `screenshot` | 截取页面截图 | `screenshot` |
| `get_console_logs` | 获取控制台日志 | `get_console_logs` |
| `get_network_requests` | 获取网络请求 | `get_network_requests` |
| `click_element` | 点击页面元素 | `click_element button.login` |
| `fill_input` | 填写表单输入 | `fill_input input.username "admin"` |
| `execute_script` | 执行JavaScript | `execute_script "console.log('test')"` |
| `get_page_content` | 获取页面内容 | `get_page_content` |

---

## 🎯 完整调试工作流

### 步骤1: 确保服务运行

```bash
# 检查前端服务
lsof -i :5173

# 检查后端服务
lsof -i :8080
```

### 步骤2: 打开浏览器

**方法A: 使用脚本**
```bash
./scripts/open-frontend.sh
```

**方法B: 在 Cursor Chat 中使用 BrowserTools**
```
打开浏览器并访问 http://localhost:5173
```

### 步骤3: 进行调试

在 Cursor Chat 中可以：

```
获取浏览器控制台的所有错误
```

```
显示当前页面的网络请求，找出失败的API调用
```

```
截取当前页面的截图
```

```
点击登录按钮
```

---

## 🔍 故障排查

### 问题1: 前端服务未运行

**症状**: 浏览器显示 "无法访问此网站"

**解决**:
```bash
# 启动前端服务
cd frontend
npm run dev

# 或使用重启脚本
./frontend/restart_frontend.sh
```

### 问题2: BrowserTools MCP 未连接

**症状**: Cursor Chat 中无法使用浏览器工具

**解决**:
1. 检查 `~/.cursor/mcp.json` 配置
2. 重启 Cursor
3. 在 Cursor 设置中启用 BrowserTools MCP
4. 确认状态显示为已连接

### 问题3: 端口被占用

**症状**: 前端服务启动失败

**解决**:
```bash
# 查找占用端口的进程
lsof -i :5173

# 停止进程
kill <PID>

# 或使用重启脚本（会自动处理）
./frontend/restart_frontend.sh
```

---

## 📝 便捷脚本

### 打开前端页面

**文件**: `scripts/open-frontend.sh`

```bash
./scripts/open-frontend.sh
```

**功能**:
- ✅ 检查前端服务是否运行
- ✅ 自动在浏览器中打开前端页面
- ✅ 跨平台支持（macOS/Linux/Windows）

### 重启前端服务

**文件**: `frontend/restart_frontend.sh`

```bash
./frontend/restart_frontend.sh
```

**功能**:
- ✅ 停止现有服务
- ✅ 清除缓存
- ✅ 重新启动服务
- ✅ 验证服务状态

---

## 🎓 使用示例

### 示例1: 调试登录问题

```
1. 打开浏览器并访问 http://localhost:5173
2. 获取控制台日志，查看是否有错误
3. 尝试点击登录按钮
4. 显示网络请求，查看登录API的响应
```

### 示例2: 检查页面加载

```
1. 导航到 http://localhost:5173
2. 获取页面内容
3. 检查所有网络请求的状态
4. 截取页面截图
```

### 示例3: 测试表单提交

```
1. 打开登录页面
2. 填写用户名和密码
3. 点击提交按钮
4. 获取网络请求，查看提交的数据和响应
```

---

## 📚 相关文档

- [Cursor Web 开发指南](https://cursor.com/cn/docs/cookbook/web-development)
- [BrowserTools 官方文档](https://browsertools.agentdesk.ai/installation)
- [前端服务启动说明](../说明文档/部署/开发环境运行指南.md)

---

## ✅ 检查清单

- [ ] 前端服务正在运行（端口 5173）
- [ ] BrowserTools MCP 已配置并启用
- [ ] 可以在浏览器中访问前端页面
- [ ] 可以在 Cursor Chat 中使用浏览器工具

---

**创建时间**: 2025-11-22  
**前端地址**: http://localhost:5173  
**状态**: ✅ 已配置

---

**现在您可以在 Cursor 中打开浏览器并调试前端页面了！** 🚀

