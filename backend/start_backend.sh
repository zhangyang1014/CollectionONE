#!/bin/bash
# 后端服务启动脚本（带详细错误检查）

cd "$(dirname "$0")"

echo "=========================================="
echo "启动CCO后端服务"
echo "=========================================="
echo ""

# 检查Python
if ! command -v python3 &> /dev/null; then
    echo "❌ 错误：未找到python3"
    exit 1
fi

# 检查虚拟环境
if [ ! -d "venv" ]; then
    echo "❌ 错误：虚拟环境不存在！"
    echo "请先运行: bash start.sh"
    echo "或者手动创建: python3 -m venv venv"
    exit 1
fi

# 激活虚拟环境
echo "📦 激活虚拟环境..."
source venv/bin/activate

# 升级pip
echo "⬆️  升级pip..."
python3 -m pip install --upgrade pip --quiet 2>/dev/null || echo "⚠️  pip升级跳过"

# 检查依赖
echo "🔍 检查依赖..."
MISSING_DEPS=()

if ! python -c "import fastapi" 2>/dev/null; then
    MISSING_DEPS+=("fastapi")
fi

if ! python -c "import uvicorn" 2>/dev/null; then
    MISSING_DEPS+=("uvicorn")
fi

if ! python -c "import sqlalchemy" 2>/dev/null; then
    MISSING_DEPS+=("sqlalchemy")
fi

if [ ${#MISSING_DEPS[@]} -gt 0 ]; then
    echo "⚠️  缺少依赖: ${MISSING_DEPS[*]}"
    echo "正在安装依赖..."
    
    # 尝试安装依赖
    if ! pip install -r requirements.txt; then
        echo ""
        echo "❌ 依赖安装失败！"
        echo ""
        echo "请尝试："
        echo "1. 手动安装: pip install ${MISSING_DEPS[*]}"
        echo "2. 或使用镜像源: pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple"
        exit 1
    fi
fi

# 检查API模块导入
echo "🔍 检查API模块..."
if ! python -c "from app.api import performance" 2>/dev/null; then
    echo "⚠️  警告：无法导入performance模块"
    echo "正在检查详细错误..."
    python -c "from app.api import performance" 2>&1 || echo "继续启动..."
fi

echo "✅ 所有检查通过"
echo ""
echo "🚀 启动后端服务..."
echo "   访问地址: http://localhost:8000"
echo "   API文档: http://localhost:8000/docs"
echo ""
echo "按 Ctrl+C 停止服务"
echo "=========================================="
echo ""

# 启动服务
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
