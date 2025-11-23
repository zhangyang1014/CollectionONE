#!/bin/bash
# 在浏览器中打开前端页面

FRONTEND_URL="http://localhost:5173"

echo "🚀 正在打开前端页面..."
echo "📍 地址: $FRONTEND_URL"
echo ""

# 检查前端服务是否运行
if lsof -i :5173 > /dev/null 2>&1; then
    echo "✅ 前端服务正在运行"
    echo ""
    # 在 macOS 上打开浏览器
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open "$FRONTEND_URL"
        echo "✅ 已在浏览器中打开前端页面"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        xdg-open "$FRONTEND_URL" 2>/dev/null || \
        sensible-browser "$FRONTEND_URL" 2>/dev/null || \
        echo "❌ 无法自动打开浏览器，请手动访问: $FRONTEND_URL"
    else
        echo "❌ 不支持的操作系统，请手动访问: $FRONTEND_URL"
    fi
else
    echo "❌ 前端服务未运行"
    echo ""
    echo "请先启动前端服务:"
    echo "  cd frontend && npm run dev"
    echo ""
    echo "或使用重启脚本:"
    echo "  ./frontend/restart_frontend.sh"
    echo ""
    exit 1
fi

