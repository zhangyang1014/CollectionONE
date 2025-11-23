# Figma MCP 服务器安装指南

## 📋 概述

根据 [Cursor Web 开发指南](https://cursor.com/cn/docs/cookbook/web-development)，Figma MCP 服务器允许您在 Cursor 中直接访问并操作 Figma 设计文件。

---

## ✅ 配置已完成

已在 `~/.cursor/mcp.json` 中添加 Figma 配置：

```json
{
  "mcpServers": {
    "Figma": {
      "url": "http://127.0.0.1:3845/sse"
    }
  }
}
```

---

## 🚀 安装步骤

### 步骤1: 安装 Figma Dev Mode MCP Server

根据 [Figma Dev Mode MCP Server 官方文档](https://help.figma.com/hc/en-us/articles/32132100833559-Guide-to-the-Dev-Mode-MCP-Server)，需要先安装并启动本地服务器。

**推荐方法: 使用 npm 安装**

```bash
# 全局安装（推荐）
npm install -g @figma/dev-mode-mcp-server

# 或使用 npx（无需全局安装，但每次都需要下载）
npx @figma/dev-mode-mcp-server
```

**验证安装**:
```bash
# 如果全局安装，检查命令是否可用
which figma-dev-mode-mcp-server
```

### 步骤2: 启动 Figma MCP 服务器

服务器需要在本地运行在 `http://127.0.0.1:3845/sse`。

**启动命令**:

```bash
# 如果全局安装
figma-dev-mode-mcp-server

# 或使用 npx
npx @figma/dev-mode-mcp-server
```

**预期输出**:
```
Figma Dev Mode MCP Server running on http://127.0.0.1:3845/sse
Listening for connections...
```

**重要**: 服务器需要**持续运行**，不要关闭终端窗口。如果需要后台运行，可以使用：

```bash
# macOS/Linux: 使用 nohup 后台运行
nohup figma-dev-mode-mcp-server > /tmp/figma-mcp.log 2>&1 &

# 或使用 screen/tmux
screen -S figma-mcp
figma-dev-mode-mcp-server
# 按 Ctrl+A 然后 D 退出 screen（服务器继续运行）
```

### 步骤3: 在 Figma 中启用 Dev Mode

1. 打开 Figma 应用或网页版
2. 打开您要使用的设计文件
3. 确保已启用 **Dev Mode**（开发者模式）
4. 在 Figma 中，Dev Mode 通常可以通过右上角的切换按钮启用

### 步骤4: 在 Cursor 中启用 Figma MCP

1. **重启 Cursor**（如果已打开）
2. 打开 Cursor 设置
3. 找到 **MCP** 设置
4. 启用 **Figma** 服务器
5. 确认状态显示为已连接

---

## 🧪 验证安装

### 测试1: 检查服务器是否运行

```bash
# 检查端口是否被占用
lsof -i :3845

# 或使用 curl 测试
curl http://127.0.0.1:3845/sse
```

### 测试2: 在 Cursor 中测试

在 Cursor Chat 中尝试：

```
获取 Figma 中当前所选内容的设计
```

或

```
显示 Figma 设计文件的组件信息
```

如果配置正确，应该能看到 Figma 设计的相关信息。

---

## 📚 官方文档

详细的安装和配置说明请参考：

- [Figma Dev Mode MCP Server 官方指南](https://help.figma.com/hc/en-us/articles/32132100833559-Guide-to-the-Dev-Mode-MCP-Server)
- [Cursor Web 开发指南](https://cursor.com/cn/docs/cookbook/web-development)

---

## 🎯 使用场景

### 在 Cursor 中使用 Figma

Figma MCP 服务器提供多种工具，可用于：

- **获取设计信息**: 查看 Figma 中的设计元素
- **提取设计规范**: 获取颜色、字体、间距等设计规范
- **查看组件信息**: 了解设计系统中的组件
- **同步设计与代码**: 确保实现与设计一致

### 示例命令

```
获取 Figma 中当前选中元素的设计规范
```

```
显示这个设计文件的颜色系统
```

```
列出设计文件中的所有组件
```

---

## 🔍 故障排查

### 问题1: 无法连接到 Figma 服务器

**症状**: Cursor 中显示 Figma MCP 服务器未连接

**解决**:
1. 确认 Figma MCP 服务器正在运行
   ```bash
   lsof -i :3845
   ```
2. 检查服务器是否在正确的端口（3845）
3. 确认防火墙没有阻止连接

### 问题2: 服务器启动失败

**症状**: 运行启动命令后报错

**解决**:
1. 检查 Node.js 版本（建议 18+）
   ```bash
   node --version
   ```
2. 尝试使用 npx 而不是全局安装
3. 检查是否有其他进程占用 3845 端口

### 问题3: Figma Dev Mode 未启用

**症状**: 无法获取设计信息

**解决**:
1. 在 Figma 中打开设计文件
2. 确保已启用 Dev Mode
3. 检查是否有权限访问该文件

### 问题4: 需要 Figma API Token

某些配置可能需要 Figma API Token：

1. 访问 [Figma Account Settings](https://www.figma.com/settings)
2. 找到 "Personal access tokens" 部分
3. 创建新的 token
4. 在环境变量中设置：
   ```bash
   export FIGMA_API_TOKEN="your_token_here"
   ```

---

## 📝 配置文件位置

**文件**: `~/.cursor/mcp.json`

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
    },
    "Figma": {
      "url": "http://127.0.0.1:3845/sse"
    }
  }
}
```

---

## 🎓 完整工作流示例

### 1. 启动 Figma MCP 服务器

```bash
# 在终端中运行（保持运行）
npx @figma/dev-mode-mcp-server
```

### 2. 在 Figma 中打开设计文件

- 打开 Figma 应用
- 打开您的设计文件
- 启用 Dev Mode

### 3. 在 Cursor 中使用

- 重启 Cursor
- 启用 Figma MCP 服务器
- 在 Chat 中请求设计信息

### 4. 获取设计规范

```
获取当前 Figma 设计中按钮组件的样式规范
```

---

## ✅ 安装检查清单

- [ ] 已安装 Figma Dev Mode MCP Server
- [ ] Figma MCP 服务器正在运行（端口 3845）
- [ ] 已在 Cursor 中启用 Figma MCP
- [ ] Figma 中已启用 Dev Mode
- [ ] 可以成功获取 Figma 设计信息

---

**配置完成时间**: 2025-11-22  
**配置文件**: `~/.cursor/mcp.json`  
**状态**: ✅ 已配置，等待安装服务器并启用

---

## 📌 下一步

1. **安装 Figma Dev Mode MCP Server**:
   ```bash
   npm install -g @figma/dev-mode-mcp-server
   ```

2. **启动服务器**:
   ```bash
   figma-dev-mode-mcp-server
   ```

3. **重启 Cursor 并启用 Figma MCP**

4. **在 Figma 中打开设计文件并启用 Dev Mode**

5. **在 Cursor 中测试连接**

---

**现在您可以开始安装 Figma MCP 服务器了！** 🚀

