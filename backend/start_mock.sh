#!/bin/bash
# 启动简化的Mock API服务器
# 无需安装任何依赖，适合快速开发和测试

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "🚀 启动 CCO Mock API 服务器..."
echo "📡 监听地址: http://localhost:8000"
echo "📝 API端点: http://localhost:8000/api/v1/"
echo "🔄 前端地址: http://localhost:5173"
echo ""
echo "按 Ctrl+C 停止服务器"
echo ""

python3 simple_server.py

