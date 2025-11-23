# Cursor MCP 配置说明

## 📋 配置内容

已为您配置了两个 MCP 服务器：

### 1. Linear MCP 服务器 ✅

**用途**: 在 Cursor 中管理 Linear 工单和问题

**配置**:
```json
"Linear": {
  "command": "npx",
  "args": [
    "-y",
    "mcp-remote",
    "https://mcp.linear.app/mcp"
  ]
}
```

**使用方法**:
1. 在 Cursor 中打开 MCP 设置
2. 启用 Linear 服务器
3. 浏览器会自动打开，提示您授权 Linear
4. 授权后即可在 Cursor 中使用

**示例命令**:
- "列出此项目的所有问题"
- "创建新的 Linear 工单"
- "查看 Linear 工单详情"

---

### 2. BrowserTools MCP 服务器 ✅

**用途**: 在 Cursor 中访问浏览器控制台日志和网络请求，实现浏览器调试

**配置**:
```json
"BrowserTools": {
  "command": "npx",
  "args": [
    "-y",
    "@agentdesk/browsertools-mcp"
  ]
}
```

**功能**:
- 监控浏览器控制台输出
- 查看网络请求
- 验证代码实现
- 调试前端问题

**使用方法**:
1. 在 Cursor 中打开 MCP 设置
2. 启用 BrowserTools 服务器
3. 在浏览器中打开您的应用
4. 在 Cursor 中可以通过 MCP 访问浏览器信息

---

## 🚀 启用步骤

### 步骤1: 重启 Cursor

配置修改后，需要重启 Cursor 才能生效。

1. 完全退出 Cursor
2. 重新打开 Cursor

### 步骤2: 启用 MCP 服务器

1. 打开 Cursor 设置
2. 找到 **MCP** 或 **Model Context Protocol** 设置
3. 启用以下服务器：
   - ✅ Linear
   - ✅ BrowserTools

### 步骤3: 授权 Linear（首次使用）

1. 启用 Linear 后，浏览器会自动打开
2. 按照提示授权 Linear 访问
3. 授权完成后，Linear 即可在 Cursor 中使用

### 步骤4: 验证配置

在 Cursor Chat 中尝试：

**测试 Linear**:
```
列出此项目的所有问题
```

**测试 BrowserTools**:
```
查看浏览器控制台日志
```

如果配置正确，应该能看到相应的响应。

---

## 📚 参考文档

- [Cursor Web 开发指南](https://cursor.com/cn/docs/cookbook/web-development)
- [Linear MCP 服务器文档](https://linear.app)
- [BrowserTools MCP 服务器](https://browsertools.agentdesk.ai/installation)

---

## 🔍 故障排查

### 问题1: MCP 服务器未显示

**解决**:
1. 确认 `~/.cursor/mcp.json` 文件已正确配置
2. 重启 Cursor
3. 检查 MCP 设置中是否显示服务器

### 问题2: Linear 授权失败

**解决**:
1. 确认网络连接正常
2. 检查 Linear 账号是否有效
3. 重新尝试授权流程

### 问题3: BrowserTools 无法连接

**解决**:
1. 确认浏览器已打开
2. 检查浏览器是否支持远程调试
3. 查看 Cursor 控制台是否有错误信息

---

## 📝 配置文件位置

**文件路径**: `~/.cursor/mcp.json`

**当前配置**:
```json
{
  "mcpServers": {
    "excel": {
      "command": "npx",
      "args": ["--yes", "@guillehr2/excel-mcp-server"]
    },
    "Linear": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "https://mcp.linear.app/mcp"
      ]
    },
    "BrowserTools": {
      "command": "npx",
      "args": [
        "-y",
        "@agentdesk/browsertools-mcp"
      ]
    }
  }
}
```

---

## 🎯 使用场景

### Linear 使用场景

- 查看项目工单列表
- 创建新的开发任务
- 更新工单状态
- 查看工单详情和评论

### BrowserTools 使用场景

- 调试前端错误
- 查看网络请求
- 监控控制台日志
- 验证代码实现效果
- 排查前端问题

---

**配置完成时间**: 2025-11-22  
**配置文件**: `~/.cursor/mcp.json`  
**状态**: ✅ 已配置，等待启用


