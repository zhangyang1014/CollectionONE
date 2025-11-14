# Vercel 前端部署完整指南

## 📋 概述

本指南专门针对将 CCO 催收操作系统的前端（Vue 3）部署到 Vercel。采用前后端分离部署策略，后端需单独部署到其他云服务。

### 🎯 部署策略

- **前端**：部署到 Vercel（静态站点）
- **后端**：部署到 Railway、Render、Heroku 或其他支持 Python 的云服务

---

## 🏗️ 项目结构

```
CollectionONE/
├── frontend/                  # Vue 3 前端代码（需要部署）
│   ├── src/
│   ├── package.json          # Vercel 会检测此文件
│   └── dist/                 # 构建输出目录
├── backend/                   # FastAPI 后端代码（单独部署）
│   ├── app/
│   └── requirements.txt
├── 说明文档/                  # 项目文档
└── vercel.json               # Vercel 路由配置（可选）
```

---

## 📝 部署前准备

### 1. 确认前端项目结构

确保 `frontend/` 目录包含：

- ✅ `package.json` - Node.js 项目配置
- ✅ `src/` - 源代码目录
- ✅ `index.html` - HTML 入口文件
- ✅ `vite.config.ts` - Vite 构建配置

### 2. 后端部署准备

前端部署前，请先将后端部署到支持 Python 的云服务：

**推荐选项**：
- **Railway** - 简单易用，支持 FastAPI
- **Render** - 免费额度充足
- **Heroku** - 传统选择
- **DigitalOcean App Platform** - 性价比高

获取后端 API 地址，例如：`https://your-backend-app.com`

---

## 🚀 部署步骤

### 步骤 1：连接 GitHub 仓库

1. 访问 [Vercel Dashboard](https://vercel.com/dashboard)
2. 点击 "Add New..." → "Project"
3. 选择你的 GitHub 仓库
4. 点击 "Import"

### 步骤 2：⚠️ 关键配置 - 设置 Root Directory

**这是解决部署失败的关键步骤！**

在项目配置页面，找到 **"Root Directory"** 设置：

```
Root Directory: frontend
```

> **为什么必须设置这个？**
>
> Vercel 默认在仓库根目录寻找 `package.json`，但你的前端代码在 `frontend/` 子目录中。
> 不设置 Root Directory 会导致 "Could not read package.json" 错误。

### 步骤 3：确认自动检测的配置

设置 Root Directory 后，Vercel 会自动检测并填充：

```
Framework Preset: Vue.js  (或 Vite)
Build Command: npm run build
Output Directory: dist  (自动检测)
Install Command: npm install  (自动)
```

如果自动检测不正确，可以手动设置为：

```
Framework Preset: Vite
Build Command: npm run build
Output Directory: dist
```

### 步骤 4：配置环境变量

在 Vercel 项目设置 → Environment Variables 中添加：

#### 前端环境变量

```bash
# API 基础地址 - 指向你部署的后端服务（必需）
VITE_API_BASE_URL=https://your-backend-api.com/api/v1
```

**重要**：将 `https://your-backend-api.com` 替换为你的实际后端 API 地址。

#### 可选的环境变量

```bash
# 应用标题
VITE_APP_TITLE=CCO 催收系统

# 其他配置...
```

### 步骤 4：部署

1. 点击 **"Deploy"** 按钮
2. 等待构建完成
3. 查看部署日志，确认无错误

---

## 🔧 vercel.json 配置说明

当前的 `vercel.json` 只处理前端的路由重定向：

```json
{
  "version": 2,
  "rewrites": [
    {
      "source": "/api/(.*)",
      "destination": "https://your-backend-api-url.com/api/$1"
    },
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

### 配置说明

- **API 重定向**：所有 `/api/*` 请求会被重定向到你部署的后端服务
- **SPA 支持**：所有其他请求都返回 `index.html`，支持 Vue Router 的 history 模式

**重要**：记得将 `https://your-backend-api-url.com` 替换为你的实际后端 API 地址。

---

## 🔗 后端部署选项

由于采用前后端分离部署策略，后端需要单独部署。以下是推荐的部署选项：

### 🚂 Railway（推荐）

1. **连接 GitHub 仓库**
   - 访问 [Railway](https://railway.app)
   - 连接你的 GitHub 仓库
   - 选择 `backend/` 目录作为部署目录

2. **自动配置**
   - Railway 会自动检测 Python 项目
   - 自动安装 `requirements.txt` 依赖
   - 自动运行 FastAPI 应用

3. **数据库**
   - Railway 提供免费 PostgreSQL 数据库
   - 自动设置 `DATABASE_URL` 环境变量

### 🎨 Render

1. **创建服务**
   - 选择 "Web Service"
   - 连接 GitHub 仓库
   - 设置构建命令：`pip install -r requirements.txt`
   - 设置启动命令：`uvicorn app.main:app --host 0.0.0.0 --port $PORT`

2. **数据库**
   - 使用 Render PostgreSQL 或外部数据库

### 🟣 DigitalOcean App Platform

1. **创建应用**
   - 选择你的 GitHub 仓库
   - 设置源码目录为 `backend/`
   - 配置环境变量和数据库

3. **运行迁移**
   ```bash
   cd backend
   alembic upgrade head
   ```

---

## 🐛 常见问题

### 1. ❌ "Could not read package.json" 错误

**问题**：Vercel 构建失败，提示找不到 package.json

**解决方案**：
- ✅ **必须设置 Root Directory**：在 Vercel 项目设置中，将 Root Directory 设置为 `frontend`
- 这是解决此错误的关键步骤

### 2. ❌ 前端无法访问后端 API

**问题**：前端部署成功，但 API 调用失败

**解决方案**：
- ✅ 检查 `VITE_API_BASE_URL` 环境变量是否正确设置
- ✅ 确认后端服务正在运行且可访问
- ✅ 检查后端 CORS 设置是否包含前端域名

### 3. ❌ SPA 路由 404

**问题**：刷新页面或直接访问路由时返回 404

**解决方案**：
- ✅ `vercel.json` 中的 rewrites 配置会自动处理 SPA 路由
- ✅ 确保所有路由请求都返回 `index.html`

### 4. ❌ 构建失败：TypeScript 错误

**问题**：`npm run build` 因类型错误失败

**解决方案**：
- ✅ 使用 `npm run build:prod`（跳过类型检查）
- ✅ 或直接使用 `vite build`
- ✅ 长期方案：修复 TypeScript 类型错误

### 5. ❌ 环境变量不生效

**问题**：设置的环境变量在应用中不生效

**解决方案**：
- ✅ 重新部署项目（环境变量变更后需要重新部署）
- ✅ 检查变量名称是否正确（Vite 环境变量必须以 `VITE_` 开头）
- ✅ 检查 Vercel 控制台的环境变量设置

### 5. 导入错误：找不到模块

**问题**：`ModuleNotFoundError: No module named 'app'`

**解决方案**：
- 检查 `api/index.py` 中的路径设置
- 确认 `backend` 目录结构正确

---

## 📊 部署后验证

### 1. 检查前端部署

访问你的 Vercel 域名：`https://your-app.vercel.app`

应该能看到前端页面正常加载。

### 2. 检查 API 连接

确认前端能正确调用后端 API：

- ✅ 检查浏览器开发者工具的网络请求
- ✅ 确认 API 请求被重定向到你的后端服务
- ✅ 检查是否有 CORS 错误

### 3. 检查后端服务

直接访问你的后端 API：

访问：`https://your-backend-api.com/api/v1/health`

应该返回：
```json
{"status": "healthy"}
```

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

### 前端部署（Vercel）
- [ ] Root Directory 已设置为 `frontend`
- [ ] `vercel.json` 已更新为正确的重定向配置
- [ ] `VITE_API_BASE_URL` 环境变量已配置
- [ ] GitHub 仓库已连接到 Vercel
- [ ] 前端构建成功
- [ ] 前端页面可正常访问

### 后端部署（Railway/Render等）
- [ ] 后端已部署到云服务（如 Railway）
- [ ] 数据库已配置（PostgreSQL）
- [ ] 后端环境变量已设置
- [ ] 后端 API 可访问
- [ ] CORS 已配置允许前端域名

### 集成测试
- [ ] 前端能正常调用后端 API
- [ ] 用户登录功能正常
- [ ] 数据展示功能正常
- [ ] 所有主要功能都可使用

---

**最后更新**：2025-11-14
**版本**：v2.0（前后端分离）

