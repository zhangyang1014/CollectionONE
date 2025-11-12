#!/bin/bash
# CCO后端服务启动脚本（增强版）
# 自动处理依赖安装、pip升级和错误处理

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

set -e

echo "=========================================="
echo "🚀 CCO后端服务启动脚本"
echo "=========================================="
echo ""

# 检查Python版本
if ! command -v python3 &> /dev/null; then
    echo "❌ 错误：未找到python3，请先安装Python 3.7+"
    exit 1
fi

PYTHON_VERSION=$(python3 --version | cut -d' ' -f2)
echo "✅ Python版本: $PYTHON_VERSION"
echo ""

# 创建虚拟环境（如果不存在）
if [ ! -d "venv" ]; then
  echo "📦 创建Python虚拟环境..."
  python3 -m venv venv
  echo "✅ 虚拟环境创建完成"
fi

# 激活虚拟环境
echo "📦 激活虚拟环境..."
source venv/bin/activate

# 升级pip（解决版本过旧问题）
echo "⬆️  升级pip到最新版本..."
python3 -m pip install --upgrade pip --quiet || {
    echo "⚠️  pip升级失败，尝试继续安装依赖..."
}

# 显示当前pip版本
PIP_VERSION=$(pip --version | cut -d' ' -f2)
echo "✅ pip版本: $PIP_VERSION"
echo ""

# 处理网络代理问题（如果需要）
if [ -n "$http_proxy" ] || [ -n "$https_proxy" ]; then
    echo "⚠️  检测到代理设置，如果安装失败请尝试："
    echo "   unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY"
    echo ""
fi

# 安装依赖
echo "📥 安装Python依赖包..."
echo "   这可能需要几分钟时间..."
echo ""

# 尝试安装依赖，如果失败则提供详细错误信息
if ! pip install -r requirements.txt; then
    echo ""
    echo "❌ 依赖安装失败！"
    echo ""
    echo "🔧 故障排除建议："
    echo "1. 检查网络连接"
    echo "2. 如果使用代理，尝试取消代理设置："
    echo "   unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY"
    echo "3. 尝试使用国内镜像源："
    echo "   pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple"
    echo "4. 手动安装关键依赖："
    echo "   pip install fastapi uvicorn sqlalchemy pydantic pydantic-settings"
    echo ""
    exit 1
fi

echo ""
echo "✅ 依赖安装完成"
echo ""

# 验证关键依赖
echo "🔍 验证关键依赖..."
MISSING_DEPS=()
for dep in fastapi uvicorn sqlalchemy pydantic; do
    if ! python3 -c "import $dep" 2>/dev/null; then
        MISSING_DEPS+=("$dep")
    fi
done

if [ ${#MISSING_DEPS[@]} -gt 0 ]; then
    echo "❌ 缺少关键依赖: ${MISSING_DEPS[*]}"
    echo "   请手动安装: pip install ${MISSING_DEPS[*]}"
    exit 1
fi

echo "✅ 所有关键依赖已安装"
echo ""

# 检查.env文件
if [ ! -f ".env" ]; then
  echo "📝 创建默认.env配置文件..."
  cat <<'EOF' > .env
# Database configuration (adjust as needed)
DATABASE_URL=sqlite:///./cco_test.db
# Alternative PostgreSQL configuration
# DATABASE_URL=postgresql://user:password@localhost:5432/cco_db
# Alternative MySQL configuration
# DATABASE_URL=mysql+pymysql://user:password@localhost:3306/cco_db

# Redis configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0

# JWT / Security
SECRET_KEY=dev-secret-key-please-change-in-production
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# CORS origins (JSON array)
BACKEND_CORS_ORIGINS=["http://localhost:5173","http://localhost:3000"]
EOF
  echo "✅ .env文件已创建"
  echo ""
fi

# 检查API模块导入
echo "🔍 检查API模块..."
if ! python3 -c "from app.main import app" 2>/dev/null; then
    echo "⚠️  警告：无法导入app模块，尝试继续启动..."
    python3 -c "from app.main import app" 2>&1 || true
fi

echo ""
echo "=========================================="
echo "🚀 启动FastAPI后端服务"
echo "=========================================="
echo "📡 访问地址: http://localhost:8000"
echo "📝 API文档: http://localhost:8000/docs"
echo "📖 ReDoc文档: http://localhost:8000/redoc"
echo ""
echo "按 Ctrl+C 停止服务"
echo "=========================================="
echo ""

# 启动服务
exec uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
