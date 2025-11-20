#!/bin/bash

# 后端服务重启脚本

echo "============================================================"
echo "CCO 后端服务重启脚本"
echo "============================================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 进入backend目录
cd "$(dirname "$0")"

echo ""
echo "${YELLOW}步骤1: 停止现有服务...${NC}"
pkill -f "uvicorn app.main:app"
sleep 2
echo "${GREEN}✓ 已停止现有服务${NC}"

echo ""
echo "${YELLOW}步骤2: 检查Python环境...${NC}"
if [ ! -f "venv/bin/python3" ]; then
    echo "${RED}✗ 虚拟环境不存在${NC}"
    echo "请先创建虚拟环境: python3 -m venv venv"
    exit 1
fi
echo "${GREEN}✓ 虚拟环境存在${NC}"

echo ""
echo "${YELLOW}步骤3: 检查依赖...${NC}"
if ! venv/bin/python3 -c "import uvicorn" 2>/dev/null; then
    echo "${YELLOW}安装依赖...${NC}"
    venv/bin/pip install -r requirements.txt
fi
echo "${GREEN}✓ 依赖已安装${NC}"

echo ""
echo "${YELLOW}步骤4: 启动后端服务...${NC}"
venv/bin/python3 -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000 &
BACKEND_PID=$!

sleep 3

# 检查服务是否启动成功
if ps -p $BACKEND_PID > /dev/null; then
    echo "${GREEN}✓ 后端服务启动成功 (PID: $BACKEND_PID)${NC}"
    
    # 测试API
    echo ""
    echo "${YELLOW}步骤5: 测试API...${NC}"
    sleep 2
    
    RESPONSE=$(curl -s http://localhost:8000/ 2>&1)
    if echo "$RESPONSE" | grep -q "CCO System API"; then
        echo "${GREEN}✓ API响应正常${NC}"
        
        # 测试通知模板API
        TEMPLATE_RESPONSE=$(curl -s http://localhost:8000/api/v1/notification-templates 2>&1)
        if echo "$TEMPLATE_RESPONSE" | grep -q "Not Found"; then
            echo "${RED}✗ 通知模板API返回404${NC}"
            echo "${YELLOW}提示: 请确保已执行SQL创建数据库表${NC}"
        else
            echo "${GREEN}✓ 通知模板API正常${NC}"
        fi
    else
        echo "${RED}✗ API响应异常${NC}"
    fi
    
    echo ""
    echo "${GREEN}============================================================${NC}"
    echo "${GREEN}✓ 后端服务已启动！${NC}"
    echo "${GREEN}============================================================${NC}"
    echo ""
    echo "服务信息:"
    echo "  - PID: $BACKEND_PID"
    echo "  - 地址: http://localhost:8000"
    echo "  - 文档: http://localhost:8000/docs"
    echo ""
    echo "停止服务:"
    echo "  kill $BACKEND_PID"
    echo "  或"
    echo "  pkill -f 'uvicorn app.main:app'"
    echo ""
else
    echo "${RED}✗ 后端服务启动失败${NC}"
    echo "请检查日志或手动启动:"
    echo "  venv/bin/python3 -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"
    exit 1
fi

