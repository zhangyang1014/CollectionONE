"""
Vercel Serverless Function 入口
将 FastAPI 应用适配为 Vercel Serverless Function
"""
import sys
import os

# 添加 backend 目录到 Python 路径
backend_path = os.path.join(os.path.dirname(__file__), '..', 'backend')
sys.path.insert(0, backend_path)

# 导入 FastAPI 应用
from app.main import app

# 使用 Mangum 将 FastAPI 应用适配为 ASGI
from mangum import Mangum

# 创建 Mangum 适配器
# Vercel Serverless Function 需要导出 handler 函数
handler = Mangum(app, lifespan="off")

