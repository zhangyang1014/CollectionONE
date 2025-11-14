# 数据看板API快速修复指南

## 🔍 问题诊断

根据错误信息，API返回404，说明：
1. 后端服务可能没有重启
2. 或者API路由没有正确注册

## ✅ 解决步骤

### 步骤1：确认后端服务正在运行

打开终端，检查8000端口：
```bash
lsof -i :8000
```

如果没有任何输出，说明后端没有运行。

### 步骤2：重启后端服务（重要！）

**必须重启后端服务才能使main.py的更改生效！**

```bash
cd "/Users/zhangyang/Library/Mobile Documents/com~apple~CloudDocs/2. 领域（Areas）/17 学习AI 与 编程/Code/CloudunCollectionONE/backend"

# 激活虚拟环境
source venv/bin/activate

# 停止旧服务（如果正在运行，按Ctrl+C）

# 重新启动
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 步骤3：验证API是否可用

打开浏览器访问：`http://localhost:8000/docs`

在API文档页面中，搜索以下端点，确认它们存在：
- `/api/v1/communications/`
- `/api/v1/ptp/`
- `/api/v1/performance/collector/{collector_id}`
- `/api/v1/alerts/collector/{collector_id}`

如果这些端点**不存在**，说明：
- 后端服务没有重启
- 或者main.py中的导入有错误

### 步骤4：检查导入错误

如果API文档中没有这些端点，检查后端启动日志是否有错误：

在运行后端的终端中，查看是否有类似这样的错误：
```
ImportError: cannot import name 'communications' from 'app.api'
```

如果有导入错误，需要修复。

### 步骤5：手动测试API

在浏览器中直接访问：
```
http://localhost:8000/api/v1/performance/collector/1?start_date=2025-01-01&end_date=2025-01-12&period=daily
```

如果返回404，说明路由确实没有注册。
如果返回其他错误（如500），说明路由已注册但代码有问题。

## 🎯 快速修复命令

如果后端正在运行，请按以下步骤操作：

1. **停止后端服务**（在运行后端的终端按 `Ctrl+C`）

2. **重新启动后端**：
```bash
cd backend
source venv/bin/activate
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

3. **等待看到**：
```
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000
```

4. **访问API文档**：`http://localhost:8000/docs`

5. **刷新前端页面**：按 `Cmd + Shift + R`

## ⚠️ 常见问题

### Q: 重启后端后还是404？
A: 检查main.py中是否正确导入了API模块：
```python
from app.api import (
    # ... 其他导入
    communications, ptp, quality_inspections, performance, analytics, alerts
)
```

### Q: 后端启动时报错？
A: 查看错误信息，可能是：
- 模块导入错误
- 数据库连接错误
- 语法错误

### Q: API文档中有端点但前端还是404？
A: 检查前端请求的URL是否正确：
- 应该是：`/api/v1/performance/collector/1`
- 不是：`/api/v1/collector/performance/1`

## 📝 验证清单

- [ ] 后端服务正在运行（端口8000）
- [ ] 访问 `http://localhost:8000/docs` 能看到API文档
- [ ] API文档中包含数据看板相关端点
- [ ] 后端启动日志没有错误
- [ ] 前端已刷新页面（强制刷新）

---

**重要提示**：每次修改 `main.py` 后，**必须重启后端服务**才能生效！

