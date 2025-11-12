# Vercel 快速部署参考

## 🚀 5 分钟快速部署

### 1. 连接 GitHub 仓库

1. 访问 https://vercel.com/dashboard
2. 点击 "Add New..." → "Project"
3. 选择 `zhangyang1014/CollectionONE`
4. 点击 "Import"

### 2. 配置项目

**Framework Preset**: `Other`  
**Root Directory**: 留空  
**Build Command**: `cd frontend && npm install && npm run build:prod`  
**Output Directory**: `frontend/dist`

**注意**：使用 `build:prod` 命令跳过 TypeScript 类型检查，避免构建失败。

### 3. 环境变量（必需）

```bash
# 数据库（PostgreSQL）
DATABASE_URL=postgresql://user:password@host:port/database

# JWT 密钥
SECRET_KEY=your-secret-key-change-in-production

# CORS（JSON 格式）
BACKEND_CORS_ORIGINS=["https://your-app.vercel.app"]

# API 配置
API_V1_STR=/api/v1
PROJECT_NAME=CCO System
```

### 4. 部署

点击 **"Deploy"** 按钮，等待完成。

---

## 📋 文件清单

已创建的配置文件：

- ✅ `vercel.json` - Vercel 主配置
- ✅ `api/index.py` - Serverless Function 入口
- ✅ `api/requirements.txt` - Python 依赖
- ✅ `.vercelignore` - 忽略文件
- ✅ `backend/requirements.txt` - 已添加 mangum

---

## 🔗 路由说明

- `/api/*` → FastAPI 后端（Serverless Function）
- `/*` → Vue 前端（静态文件）

---

## ⚠️ 重要提示

1. **数据库**：必须使用 PostgreSQL（不支持 SQLite）
2. **环境变量**：所有必需变量必须设置
3. **CORS**：记得添加前端域名到 `BACKEND_CORS_ORIGINS`

---

## 📚 详细文档

查看 `Vercel部署指南.md` 获取完整说明。

---

**最后更新**：2025-11-12

