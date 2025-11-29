# ✅ Figma MCP 配置完成！

## 📋 配置状态

根据 [Cursor Web 开发指南](https://cursor.com/cn/docs/cookbook/web-development)，已为您配置好 Figma MCP 服务器。

---

## ✅ 已完成的配置

### 配置文件

**文件**: `~/.cursor/mcp.json`

**已添加**:
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

## 🚀 快速安装（3步）

### 步骤1: 安装 Figma Dev Mode MCP Server

**方法1: 使用安装脚本（推荐）** ⭐

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE
./scripts/install-figma-mcp.sh
```

**方法2: 手动安装**

```bash
npm install -g @figma/dev-mode-mcp-server
```

### 步骤2: 启动 Figma MCP 服务器

```bash
figma-dev-mode-mcp-server
```

**预期输出**:
```
Figma Dev Mode MCP Server running on http://127.0.0.1:3845/sse
Listening for connections...
```

**重要**: 保持这个终端窗口打开，服务器需要持续运行。

### 步骤3: 在 Cursor 中启用

1. **重启 Cursor**（如果已打开）
2. 打开 Cursor 设置 → MCP
3. 启用 **Figma** 服务器
4. 确认状态显示为已连接

---

## 🎯 在 Figma 中设置

### 启用 Dev Mode

1. 打开 Figma 应用或网页版
2. 打开您的设计文件
3. 点击右上角的 **Dev Mode** 切换按钮
4. 确保 Dev Mode 已启用

---

## 🧪 验证安装

### 测试1: 检查服务器是否运行

```bash
# 检查端口是否被占用
lsof -i :3845

# 应该看到类似输出：
# COMMAND   PID  USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
# node    12345  user   23u  IPv4  ...      0t0  TCP *:3845 (LISTEN)
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

## 📚 详细文档

完整的安装和使用说明请查看：

📄 **`说明文档/Figma-MCP安装指南.md`**

包含：
- 详细安装步骤
- 故障排查
- 使用场景
- 完整工作流示例

---

## 🎯 使用场景

### 在 Cursor 中使用 Figma

Figma MCP 服务器允许您：

- ✅ **获取设计信息**: 查看 Figma 中的设计元素
- ✅ **提取设计规范**: 获取颜色、字体、间距等设计规范
- ✅ **查看组件信息**: 了解设计系统中的组件
- ✅ **同步设计与代码**: 确保实现与设计一致

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
4. 重启 Cursor

### 问题2: 服务器启动失败

**症状**: 运行启动命令后报错

**解决**:
1. 检查 Node.js 版本（建议 18+）
   ```bash
   node --version
   ```
2. 尝试重新安装：
   ```bash
   npm uninstall -g @figma/dev-mode-mcp-server
   npm install -g @figma/dev-mode-mcp-server
   ```
3. 检查是否有其他进程占用 3845 端口

### 问题3: Figma Dev Mode 未启用

**症状**: 无法获取设计信息

**解决**:
1. 在 Figma 中打开设计文件
2. 确保已启用 Dev Mode（右上角切换按钮）
3. 检查是否有权限访问该文件

---

## 📊 当前 MCP 服务器列表

您的 Cursor 现在配置了以下 MCP 服务器：

| 服务器 | 状态 | 用途 |
|--------|------|------|
| Excel | ✅ 已配置 | Excel 文件操作 |
| Linear | ✅ 已配置 | 项目管理工单 |
| BrowserTools | ✅ 已配置 | 浏览器调试 |
| Figma | ✅ 已配置 | 设计文件访问 |

---

## ✅ 安装检查清单

- [ ] 已安装 Figma Dev Mode MCP Server
- [ ] Figma MCP 服务器正在运行（端口 3845）
- [ ] 已在 Cursor 中启用 Figma MCP
- [ ] Figma 中已启用 Dev Mode
- [ ] 可以成功获取 Figma 设计信息

---

## 📌 下一步操作

### 立即执行

1. **安装服务器**:
   ```bash
   ./scripts/install-figma-mcp.sh
   ```

2. **启动服务器**（在新终端窗口）:
   ```bash
   figma-dev-mode-mcp-server
   ```

3. **重启 Cursor 并启用 Figma MCP**

4. **在 Figma 中打开设计文件并启用 Dev Mode**

5. **在 Cursor 中测试连接**

---

**配置完成时间**: 2025-11-22  
**配置文件**: `~/.cursor/mcp.json`  
**安装脚本**: `scripts/install-figma-mcp.sh`  
**状态**: ✅ 已配置，等待安装服务器并启用

---

**现在您可以运行安装脚本开始安装了！** 🚀

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE
./scripts/install-figma-mcp.sh
```


