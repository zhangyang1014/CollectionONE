#!/bin/bash

echo "================================"
echo "重启前端服务"
echo "================================"

# 进入前端目录
cd "$(dirname "$0")"

# 1. 停止现有的前端服务
echo "1. 停止现有服务..."
pkill -f "vite.*5173" || echo "没有运行中的服务"

# 2. 清除Vite缓存
echo "2. 清除Vite缓存..."
rm -rf node_modules/.vite
rm -rf .vite
rm -rf dist
echo "✓ 缓存已清除"

# 3. 等待端口释放
echo "3. 等待端口释放..."
sleep 2

# 4. 启动前端服务
echo "4. 启动前端服务..."
npm run dev > /dev/null 2>&1 &

# 5. 等待服务启动
echo "5. 等待服务启动..."
sleep 3

# 6. 检查服务状态
if curl -s http://localhost:5173 > /dev/null; then
    echo ""
    echo "================================"
    echo "✓ 前端服务启动成功！"
    echo "================================"
    echo ""
    echo "访问地址: http://localhost:5173"
    echo ""
    echo "停止服务: pkill -f 'vite.*5173'"
    echo ""
else
    echo ""
    echo "================================"
    echo "✗ 前端服务启动失败"
    echo "================================"
    echo ""
    echo "请手动运行: npm run dev"
    echo ""
fi

