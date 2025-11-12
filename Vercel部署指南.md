# Vercel 前后端部署指南

## 📋 概述

本指南将帮助您将 CCO 催收操作系统部署到 Vercel，包括前端（Vue 3）和后端（FastAPI）的全栈部署。

---

## 🏗️ 项目结构

```
CloudunCollectionONE/
├── api/
│   ├── index.py              # Vercel Serverless Function 入口
│   └── requirements.txt      # Python 依赖
├── backend/                  # FastAPI 后端代码
│   ├── app/
│   └── requirements.txt
├── frontend/                  # Vue 3 前端代码
│   ├── src/
│   └── package.json
└── vercel.json               # Vercel 配置文件
```

---

## 📝 部署前准备

### 1. 检查文件

确保以下文件已创建：

- ✅ `vercel.json` - Vercel 配置
- ✅ `api/index.py` - Serverless Function 入口
- ✅ `api/requirements.txt` - Python 依赖
- ✅ `backend/requirements.txt` - 已添加 `mangum`

### 2. 数据库准备

**重要**：Vercel Serverless Functions 不支持 SQLite（文件系统只读），需要：

- **选项 1**：使用 Vercel Postgres（推荐）
  - 在 Vercel 项目设置中创建 Postgres 数据库
  - 获取连接字符串

- **选项 2**：使用外部数据库服务
  - Railway Postgres
  - Supabase
  - PlanetScale（MySQL）
  - 其他云数据库服务

---

## 🚀 部署步骤

### 步骤 1：连接 GitHub 仓库

1. 访问 [Vercel Dashboard](https://vercel.com/dashboard)
2. 点击 "Add New..." → "Project"
3. 选择 GitHub 仓库：`zhangyang1014/CollectionONE`
4. 点击 "Import"

### 步骤 2：配置项目设置

#### Framework Preset
- 选择：**Other** 或 **Vite**（如果可用）

#### Root Directory
- 留空（使用项目根目录）

#### Build and Output Settings

**Build Command**：
```bash
cd frontend && npm install && npm run build
```

或者（如果跳过 TypeScript 检查）：
```bash
cd frontend && npm install && vite build
```

**Output Directory**：
```
frontend/dist
```

**Install Command**：
```bash
npm install
```
（留空也可以，Vercel 会自动检测）

### 步骤 3：配置环境变量

在 Vercel 项目设置 → Environment Variables 中添加：

#### 必需的环境变量

```bash
# 数据库连接（PostgreSQL）
DATABASE_URL=postgresql://user:password@host:port/database

# JWT 密钥（生产环境请使用强密钥）
SECRET_KEY=your-super-secret-key-change-in-production

# CORS 配置（JSON 格式）
BACKEND_CORS_ORIGINS=["https://your-app.vercel.app","http://localhost:5173"]

# API 版本
API_V1_STR=/api/v1

# 项目名称
PROJECT_NAME=CCO System
```

#### Redis 配置（可选）

```bash
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_DB=0
```

#### 前端环境变量

```bash
# API 基础 URL（部署后会自动设置）
VITE_API_BASE_URL=https://your-app.vercel.app
```

### 步骤 4：部署

1. 点击 **"Deploy"** 按钮
2. 等待构建完成
3. 查看部署日志，确认无错误

---

## 🔧 配置说明

### vercel.json 配置解析

```json
{
  "version": 2,
  "builds": [
    {
      "src": "api/index.py",
      "use": "@vercel/python"  // Python Serverless Function
    },
    {
      "src": "frontend/package.json",
      "use": "@vercel/static-build",  // 静态前端构建
      "config": {
        "distDir": "dist"  // 构建输出目录
      }
    }
  ],
  "routes": [
    {
      "src": "/api/(.*)",
      "dest": "/api/index.py"  // API 请求路由到 Serverless Function
    },
    {
      "src": "/(.*)",
      "dest": "/frontend/$1"  // 其他请求路由到前端
    }
  ]
}
```

### API 路由说明

- `/api/*` → 路由到 `api/index.py`（FastAPI 后端）
- `/*` → 路由到 `frontend/dist`（Vue 前端）

---

## 🗄️ 数据库迁移

### 使用 Vercel Postgres

1. **创建数据库**
   - 在 Vercel 项目设置 → Storage → Create Database
   - 选择 "Postgres"
   - 创建后会自动添加 `POSTGRES_URL` 环境变量

2. **更新环境变量**
   ```bash
   DATABASE_URL=$POSTGRES_URL
   ```

3. **运行迁移**
   ```bash
   # 本地运行（需要连接到 Vercel Postgres）
   cd backend
   alembic upgrade head
   ```

### 使用外部数据库

1. **获取连接字符串**
   - 格式：`postgresql://user:password@host:port/database`

2. **设置环境变量**
   ```bash
   DATABASE_URL=postgresql://...
   ```

3. **运行迁移**
   ```bash
   cd backend
   alembic upgrade head
   ```

---

## 🐛 常见问题

### 1. 构建失败：TypeScript 错误

**问题**：`npm run build` 因 TypeScript 类型错误失败

**解决方案**：
- 修改 Build Command 为：`cd frontend && vite build`（跳过类型检查）
- 或修复所有 TypeScript 错误

### 2. API 路由 404

**问题**：访问 `/api/*` 返回 404

**解决方案**：
- 检查 `vercel.json` 中的路由配置
- 确认 `api/index.py` 文件存在
- 检查构建日志中的错误

### 3. 数据库连接失败

**问题**：`DATABASE_URL` 未设置或格式错误

**解决方案**：
- 检查环境变量是否正确设置
- 确认数据库服务可访问
- 检查连接字符串格式

### 4. CORS 错误

**问题**：前端无法访问后端 API

**解决方案**：
- 在 `BACKEND_CORS_ORIGINS` 中添加前端域名
- 格式：`["https://your-app.vercel.app"]`

### 5. 导入错误：找不到模块

**问题**：`ModuleNotFoundError: No module named 'app'`

**解决方案**：
- 检查 `api/index.py` 中的路径设置
- 确认 `backend` 目录结构正确

---

## 📊 部署后验证

### 1. 检查前端

访问：`https://your-app.vercel.app`

应该能看到前端页面。

### 2. 检查后端 API

访问：`https://your-app.vercel.app/api/v1/health`

应该返回：
```json
{"status": "healthy"}
```

### 3. 检查 API 文档

访问：`https://your-app.vercel.app/api/v1/openapi.json`

应该能看到 OpenAPI 文档。

---

## 🔄 更新部署

### 自动部署

- 推送到 `main` 分支会自动触发部署
- Vercel 会检测更改并重新构建

### 手动部署

1. 在 Vercel Dashboard 中
2. 选择项目
3. 点击 "Redeploy"

---

## 📈 性能优化

### 1. 启用缓存

Vercel 会自动缓存静态资源。

### 2. Serverless Function 优化

- 减少冷启动时间：使用较小的依赖
- 优化数据库查询：使用连接池
- 启用 Edge Functions（如果适用）

### 3. 前端优化

- 代码分割
- 图片优化
- CDN 加速（自动）

---

## 🔒 安全建议

### 1. 环境变量

- ✅ 不要将敏感信息提交到代码仓库
- ✅ 使用 Vercel 环境变量管理
- ✅ 生产环境使用强密钥

### 2. API 安全

- ✅ 启用 HTTPS（Vercel 自动）
- ✅ 配置 CORS 白名单
- ✅ 使用 JWT 认证
- ✅ 验证输入数据

### 3. 数据库安全

- ✅ 使用连接字符串加密
- ✅ 限制数据库访问 IP
- ✅ 定期备份数据

---

## 📚 相关资源

- [Vercel 文档](https://vercel.com/docs)
- [Vercel Python Runtime](https://vercel.com/docs/functions/runtimes/python)
- [Mangum 文档](https://mangum.io/)
- [FastAPI 文档](https://fastapi.tiangolo.com/)

---

## 🆘 获取帮助

如果遇到问题：

1. 查看 Vercel 构建日志
2. 检查环境变量配置
3. 查看本文档的"常见问题"部分
4. 访问 [Vercel 社区](https://github.com/vercel/vercel/discussions)

---

## ✅ 部署检查清单

- [ ] `vercel.json` 已创建
- [ ] `api/index.py` 已创建
- [ ] `api/requirements.txt` 已创建
- [ ] `backend/requirements.txt` 已添加 `mangum`
- [ ] 数据库已准备（PostgreSQL）
- [ ] 环境变量已配置
- [ ] GitHub 仓库已连接
- [ ] 构建命令已配置
- [ ] 部署成功
- [ ] 前端可访问
- [ ] 后端 API 可访问
- [ ] 数据库连接正常

---

**最后更新**：2025-11-12  
**版本**：v1.0

