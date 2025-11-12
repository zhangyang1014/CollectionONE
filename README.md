# CCO催收操作系统

基于 Vue 3 + Python FastAPI + MySQL 的催收操作系统

## 项目结构

```
CloudunCollectionONE/
├── backend/           # Python FastAPI 后端
│   ├── app/
│   │   ├── api/      # API路由
│   │   ├── core/     # 核心配置
│   │   ├── models/   # 数据库模型
│   │   ├── schemas/  # Pydantic模式
│   │   └── main.py   # 主应用
│   ├── alembic/      # 数据库迁移
│   └── requirements.txt
├── frontend/          # Vue 3 前端
│   ├── src/
│   │   ├── api/      # API封装
│   │   ├── components/  # 组件
│   │   ├── views/    # 页面
│   │   ├── stores/   # Pinia状态管理
│   │   ├── router/   # 路由配置
│   │   └── i18n/     # 国际化
│   └── package.json
└── README.md
```

## 技术栈

### 后端
- Python 3.10+
- FastAPI
- SQLAlchemy
- MySQL
- Redis
- Alembic

### 前端
- Vue 3
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Vue I18n

## 快速开始

### 后端启动

```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 功能模块

1. 字段配置系统
   - 标准字段管理
   - 自定义字段管理
   - 字段分组管理
   - 字段联动配置

2. 案件管理
   - 案件列表
   - 案件详情
   - 动态表单

3. 甲方管理
   - 甲方配置
   - 字段启用配置

4. 权限管理
   - 角色管理
   - 权限配置
   - 字段权限

5. 审计日志
   - 操作日志
   - 字段变更日志

6. 系统设置
   - 国际化配置
   - 时区设置

