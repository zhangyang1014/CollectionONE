#!/bin/bash
# Figma MCP 服务器安装脚本

echo "🚀 开始安装 Figma Dev Mode MCP Server..."
echo ""

# 检查 npm
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到 npm，请先安装 Node.js"
    exit 1
fi

echo "✅ npm 已安装: $(npm --version)"
echo ""

# 安装 Figma Dev Mode MCP Server
echo "📦 正在安装 @figma/dev-mode-mcp-server..."
npm install -g @figma/dev-mode-mcp-server

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Figma Dev Mode MCP Server 安装成功！"
    echo ""
    echo "📝 下一步："
    echo "1. 启动服务器:"
    echo "   figma-dev-mode-mcp-server"
    echo ""
    echo "2. 在另一个终端窗口，重启 Cursor"
    echo ""
    echo "3. 在 Cursor 设置中启用 Figma MCP 服务器"
    echo ""
    echo "4. 在 Figma 中打开设计文件并启用 Dev Mode"
    echo ""
else
    echo ""
    echo "❌ 安装失败，请检查错误信息"
    exit 1
fi


