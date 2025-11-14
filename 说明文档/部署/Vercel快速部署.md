# Vercel 前端快速部署指南

## 🚀 5 分钟快速部署（推荐方案）

### 1. 连接 GitHub 仓库

1. 访问 https://vercel.com/dashboard
2. 点击 "Add New..." → "Project"
3. 选择你的 GitHub 仓库
4. 点击 "Import"

### 2. ⚠️ 关键配置：设置 Root Directory

**这是最重要的步骤！**

在项目配置页面：

```
Framework Preset: Vue.js
Root Directory: frontend
```

> **为什么需要设置 Root Directory？**
>
> Vercel 默认在仓库根目录寻找 `package.json`，但你的前端代码在 `frontend/` 子目录中。
> 设置 `Root Directory: frontend` 后，Vercel 会在 `frontend/` 目录下运行构建命令。

### 3. 自动检测配置

设置 Root Directory 后，Vercel 会自动：

- ✅ 检测 `frontend/package.json`
- ✅ 运行 `npm install`
- ✅ 执行 `npm run build`
- ✅ 部署 `dist/` 目录

### 4. 部署

点击 **"Deploy"** 按钮，等待构建完成。

---

## 🔧 手动配置（如果自动检测失败）

如果 Vercel 没有自动检测到正确的配置：

```
Framework Preset: Other
Root Directory: frontend
Build Command: npm run build
Output Directory: dist
Install Command: npm install
```

---

## 📡 API 代理配置

前端部署完成后，你需要配置 API 调用指向你的后端服务。

### 方法一：修改前端环境变量

在 Vercel 项目设置中添加环境变量：

```
VITE_API_BASE_URL=https://your-backend-api.com/api/v1
```

### 方法二：更新 vercel.json（当前配置）

当前的 `vercel.json` 会将所有 `/api/*` 请求代理到你指定的后端：

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

**请将 `https://your-backend-api-url.com` 替换为你的实际后端 API 地址。**

---

## ⚠️ 重要提醒

1. **Root Directory 必须设置为 `frontend`** - 这是解决 "Could not read package.json" 错误的关键
2. **后端部署**：前端和后端需要分别部署，后端可以部署到 Railway、Render 或其他云服务
3. **环境变量**：根据你的后端需求配置相应的环境变量

---

## 📚 相关文档

- [Vercel 完整部署指南](Vercel部署指南.md)
- [后端部署选项](部署指南.md)

---

**最后更新**：2025-11-14

