# Vercel 全栈部署指南

## 🚀 两种部署方案选择

### 🎯 方案 A：全栈部署（前端+后端都在 Vercel）- 推荐

**适合场景**：简单项目，一站式部署，前后端都在同一个域名下

### 🎯 方案 B：前后端分离部署（前端 Vercel，后端其他服务）

**适合场景**：复杂项目，后端需要专用数据库或更高性能

---

## 📋 方案 A：全栈部署（推荐）

### 1. 连接 GitHub 仓库

1. 访问 [Vercel Dashboard](https://vercel.com/dashboard)
2. 点击 "Add New..." → "Project"
3. 选择你的 GitHub 仓库
4. 点击 "Import"

### 2. 配置项目设置

```
Framework Preset: Other
Root Directory: （留空，使用根目录）
```

Vercel 会自动检测 `vercel.json` 中的配置。

### 3. 配置环境变量

在 Vercel 项目设置 → Environment Variables 中添加：

```bash
# 数据库连接（PostgreSQL 必需）
DATABASE_URL=postgresql://user:password@host:port/database

# JWT 密钥（生产环境请使用强密钥）
SECRET_KEY=your-super-secret-key-change-in-production

# CORS 配置（包含你的前端域名）
BACKEND_CORS_ORIGINS=["https://your-app.vercel.app"]

# API 配置
API_V1_STR=/api/v1
PROJECT_NAME=CCO System
```

### 4. 部署

点击 **"Deploy"** 按钮，等待构建完成。

**构建过程**：
- ✅ 安装 Python 依赖 (`api/requirements.txt`)
- ✅ 安装 Node.js 依赖 (`frontend/package.json`)
- ✅ 构建前端静态文件
- ✅ 部署后端 Serverless Function

### 5. 验证部署

部署完成后访问：
- **前端**: `https://your-app.vercel.app`
- **后端API**: `https://your-app.vercel.app/api/v1/health`

---

## 📋 方案 B：前后端分离部署

### 前端部署到 Vercel

#### 1. 连接 GitHub 仓库

1. 访问 [Vercel Dashboard](https://vercel.com/dashboard)
2. 点击 "Add New..." → "Project"
3. 选择你的 GitHub 仓库
4. 点击 "Import"

#### 2. ⚠️ 设置 Root Directory

```
Framework Preset: Vue.js
Root Directory: frontend
```

#### 3. 配置环境变量

```bash
VITE_API_BASE_URL=https://your-backend-api.com/api/v1
```

#### 4. 部署前端

点击 "Deploy" 按钮。

### 后端部署到其他服务

选择以下任一服务：

- 🚂 **Railway**（推荐）
- 🎨 **Render**
- 🟣 **DigitalOcean App Platform**
- 🐙 **Heroku**

---

## ⚖️ 两种方案对比

| 特性 | 方案 A (全栈) | 方案 B (分离) |
|------|---------------|----------------|
| **复杂度** | 简单，一站式 | 需要分别管理 |
| **域名** | 前后端同域 | 前后端不同域 |
| **数据库** | Vercel Postgres | 各服务提供 |
| **性能** | Serverless 限制 | 更灵活配置 |
| **成本** | 相对较低 | 可能较高 |
| **维护** | 统一管理 | 分开维护 |

---

## 📚 相关文档

- [Vercel 完整部署指南](Vercel部署指南.md)
- [后端部署选项](部署指南.md)

---

**最后更新**：2025-11-14

