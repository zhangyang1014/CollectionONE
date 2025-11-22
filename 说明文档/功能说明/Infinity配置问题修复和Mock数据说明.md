# Infinity配置问题修复和Mock数据说明

## 📋 问题总结

用户在尝试保存 Infinity 外呼配置时遇到"请求的资源不存在"和"保存配置失败"的错误。

## 🔧 修复内容

### 1. 替换HTTP客户端库

**问题**: 后端使用了 `requests` 库，但该库未安装，导致API路由被注释。

**解决方案**: 将 `requests` 替换为项目已安装的 `httpx` 库。

**修改文件**:
- `backend/app/api/infinity_config.py`
- `backend/app/api/infinity_call.py`

```python
# 旧代码
import requests
response = requests.post(url, data=data, timeout=10)

# 新代码
import httpx
with httpx.Client() as client:
    response = client.post(url, data=data, timeout=10.0)
```

### 2. 启用Infinity API路由

**问题**: API路由在 `main.py` 中被注释，导致前端无法访问。

**解决方案**: 取消注释并启用路由。

**修改文件**: `backend/app/main.py`

```python
# 启用 Infinity API 路由
from app.api import infinity_config, infinity_extension, infinity_call

app.include_router(infinity_config.router, prefix=settings.API_V1_STR)
app.include_router(infinity_extension.router, prefix=settings.API_V1_STR)
app.include_router(infinity_call.router, prefix=settings.API_V1_STR)
```

### 3. 修复数据库主键类型

**问题**: 使用 `BigInteger` 作为主键类型在 SQLite 中不会自动递增。

**解决方案**: 将主键类型改为 `Integer`。

**修改文件**:
- `backend/app/models/infinity_call_config.py`
- `backend/app/models/infinity_extension_pool.py`

```python
# 旧代码
id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)

# 新代码
id = Column(Integer, primary_key=True, index=True, autoincrement=True)
```

### 4. 数据库表重建

由于主键类型改变，需要删除并重新创建表：

```sql
DROP TABLE IF EXISTS infinity_extension_pool;
DROP TABLE IF EXISTS infinity_call_configs;
-- 然后通过 SQLAlchemy 重新创建
```

## 📊 Mock数据

### 创建脚本

创建了 `backend/create_infinity_mock_data.py` 脚本来生成测试数据。

### 运行方式

```bash
cd backend
source venv/bin/activate
python create_infinity_mock_data.py
```

### Mock数据内容

#### 1. Infinity外呼配置

| 字段 | 值 | 说明 |
|------|-----|------|
| ID | 1 | 配置ID |
| 甲方ID | 1 | 关联到"百腾企业" |
| API地址 | http://127.0.0.1:8080 | Infinity API地址 |
| 访问令牌 | test_token_123456 | 测试用令牌 |
| 应用ID | btq | 应用标识 |
| 号段起始 | 1 | 外显号码起始 |
| 号段结束 | 133 | 外显号码结束 |
| 回调地址 | http://your-domain.com/api/v1/infinity/callback/call-record | 通话记录回调URL |
| 录音回调地址 | http://your-domain.com/api/v1/infinity/callback/recording | 录音回调URL |
| 最大并发呼叫数 | 100 | 同时外呼上限 |
| 呼叫超时时间 | 60秒 | 呼叫超时设置 |
| 状态 | 启用 | 配置启用状态 |

#### 2. 分机池 (10个分机)

| 分机号 | 状态 | 当前催员 |
|--------|------|----------|
| 8001 | 可用 | - |
| 8002 | 可用 | - |
| 8003 | 可用 | - |
| 8004 | 可用 | - |
| 8005 | 可用 | - |
| 8006 | 可用 | - |
| 8007 | 可用 | - |
| 8008 | 可用 | - |
| 8009 | 可用 | - |
| 8010 | 可用 | - |

#### 3. 催员回呼号码

自动为前5个催员设置了回呼号码格式：`138001380XX`（XX为催员ID的后两位）

### API测试

#### 获取配置

```bash
curl -X GET "http://127.0.0.1:8000/api/v1/infinity/configs/1"
```

**响应示例**:
```json
{
  "tenant_id": 1,
  "supplier_id": null,
  "api_url": "http://127.0.0.1:8080",
  "access_token": "test_token_123456",
  "app_id": "btq",
  "caller_number_range_start": "1",
  "caller_number_range_end": "133",
  "callback_url": "http://your-domain.com/api/v1/infinity/callback/call-record",
  "recording_callback_url": "http://your-domain.com/api/v1/infinity/callback/recording",
  "max_concurrent_calls": 100,
  "call_timeout_seconds": 60,
  "is_active": true,
  "id": 1,
  "created_at": "2025-11-21T08:49:11",
  "updated_at": "2025-11-21T08:49:11",
  "created_by": null
}
```

#### 获取分机池

```bash
curl -X GET "http://127.0.0.1:8000/api/v1/infinity/extensions/1?config_id=1"
```

## ✅ 验证步骤

### 1. 后端验证

```bash
# 检查后端服务状态
curl http://127.0.0.1:8000/health

# 检查Infinity配置API
curl http://127.0.0.1:8000/api/v1/infinity/configs/1

# 检查API文档
open http://127.0.0.1:8000/docs
```

### 2. 前端验证

1. **刷新浏览器页面**
2. **进入"甲方渠道管理"**
3. **选择甲方"百腾企业"**
4. **点击"Infinity外呼配置"标签页**
5. **应该能看到已创建的配置和分机池**

### 3. 功能测试

#### 查看配置
- 配置详情应该正确显示
- 号段范围显示为 "1 ~ 133"
- 状态显示为"启用"

#### 查看分机池
- 总分机数：10
- 可用分机：10
- 使用中分机：0
- 离线分机：0
- 分机列表显示8001-8010

#### 编辑配置（可选）
- 点击"编辑配置"按钮
- 修改配置信息
- 保存应该成功

#### 批量导入分机（可选）
- 点击"批量导入分机"按钮
- 输入新分机号（如8011-8020）
- 导入应该成功

## 🚀 下一步

### 1. 前端刷新
刷新前端页面，查看Infinity配置和分机池

### 2. 外呼测试
- 在IM催收面板中测试发起外呼
- 系统会自动从分机池分配分机
- 呼叫结束后分机会自动释放

### 3. 回调测试
- 配置实际的Infinity系统地址
- 测试通话记录回调
- 测试录音文件回调

## 📝 注意事项

### 1. 生产环境配置

在生产环境中，请修改以下配置：

- **API地址**: 改为实际的Infinity服务器地址
- **访问令牌**: 使用实际的API令牌
- **回调地址**: 配置为公网可访问的回调URL
- **号段范围**: 配置为实际购买的号码段

### 2. 数据库类型

- 当前使用 SQLite 数据库（开发环境）
- 生产环境建议使用 MySQL
- MySQL中可以使用 `BIGINT` 作为主键类型
- SQLite中必须使用 `INTEGER` 作为主键自增类型

### 3. 安全性

- 访问令牌应该加密存储
- 回调接口需要增加签名验证
- API调用需要增加访问控制

## 🔗 相关文件

### 前端文件
- `/frontend/src/views/channel-config/InfinityCallConfigContent.vue` - 配置页面组件
- `/frontend/src/views/channel-config/InfinityCallConfig.vue` - 独立配置页面
- `/frontend/src/api/infinity.ts` - API调用封装
- `/frontend/src/types/infinity.ts` - TypeScript类型定义

### 后端文件
- `/backend/app/api/infinity_config.py` - 配置管理API
- `/backend/app/api/infinity_extension.py` - 分机池管理API
- `/backend/app/api/infinity_call.py` - 外呼核心API
- `/backend/app/models/infinity_call_config.py` - 配置模型
- `/backend/app/models/infinity_extension_pool.py` - 分机池模型
- `/backend/app/schemas/infinity.py` - Pydantic Schema
- `/backend/create_infinity_mock_data.py` - Mock数据生成脚本

---

**更新时间**: 2025-11-21  
**更新人员**: AI Assistant  
**版本**: 1.0

