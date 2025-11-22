# Infinity渠道配置404错误修复说明

## 修复时间
2025-11-21

## 问题描述

在访问"甲方渠道管理"页面的"短信"标签时，前端报错：

```
GET http://localhost:8000/infinity/configs/1 404 (Not Found)
```

错误发生在 `infinity.ts:36` 的 `getInfinityConfigByTenant` 方法中。

## 问题原因

**前端API路径缺少 `/api/v1` 前缀**

- 前端请求的URL：`http://localhost:8000/infinity/configs/1`
- 后端实际路由：`http://localhost:8000/api/v1/infinity/configs/1`

后端在 `backend/app/main.py` 第101行注册路由时使用了：
```python
app.include_router(infinity_config.router, prefix=settings.API_V1_STR)
```

其中 `settings.API_V1_STR` 是 `/api/v1`，所以所有Infinity接口都需要加上这个前缀。

## 修复方案

### 修改文件：`frontend/src/api/infinity.ts`

为所有Infinity API接口添加 `/api/v1` 前缀：

#### 1. Infinity配置管理接口
- ✅ `createInfinityConfig`: `/infinity/configs` → `/api/v1/infinity/configs`
- ✅ `getInfinityConfigByTenant`: `/infinity/configs/${tenantId}` → `/api/v1/infinity/configs/${tenantId}`
- ✅ `getInfinityConfigById`: `/infinity/configs/id/${configId}` → `/api/v1/infinity/configs/id/${configId}`
- ✅ `updateInfinityConfig`: `/infinity/configs/${configId}` → `/api/v1/infinity/configs/${configId}`
- ✅ `deleteInfinityConfig`: `/infinity/configs/${configId}` → `/api/v1/infinity/configs/${configId}`
- ✅ `testInfinityConnection`: `/infinity/configs/test-connection` → `/api/v1/infinity/configs/test-connection`
- ✅ `toggleInfinityConfig`: `/infinity/configs/${configId}/toggle` → `/api/v1/infinity/configs/${configId}/toggle`

#### 2. 分机池管理接口
- ✅ `batchImportExtensions`: `/infinity/extensions/batch-import` → `/api/v1/infinity/extensions/batch-import`
- ✅ `getExtensions`: `/infinity/extensions/${tenantId}` → `/api/v1/infinity/extensions/${tenantId}`
- ✅ `getExtensionStatistics`: `/infinity/extensions/statistics/${tenantId}` → `/api/v1/infinity/extensions/statistics/${tenantId}`
- ✅ `updateExtension`: `/infinity/extensions/${extensionId}` → `/api/v1/infinity/extensions/${extensionId}`
- ✅ `releaseExtension`: `/infinity/extensions/${extensionId}/release` → `/api/v1/infinity/extensions/${extensionId}/release`
- ✅ `deleteExtension`: `/infinity/extensions/${extensionId}` → `/api/v1/infinity/extensions/${extensionId}`
- ✅ `batchDeleteExtensions`: `/infinity/extensions/batch-delete` → `/api/v1/infinity/extensions/batch-delete`
- ✅ `forceReleaseCollectorExtensions`: `/infinity/extensions/force-release-collector/${collectorId}` → `/api/v1/infinity/extensions/force-release-collector/${collectorId}`

#### 3. 外呼接口
- ✅ `makeCall`: `/infinity/make-call` → `/api/v1/infinity/make-call`

## 验证测试

### 后端接口测试（修复前）
```bash
# 直接访问后端API，返回正常
curl -X GET http://localhost:8000/api/v1/infinity/configs/1
```

返回结果：
```json
{
  "tenant_id":1,
  "supplier_id":null,
  "api_url":"http://127.0.0.1:8080",
  "access_token":"test_token_123456",
  "app_id":"btq",
  "caller_number_range_start":"1",
  "caller_number_range_end":"133",
  "callback_url":"http://your-domain.com/api/v1/infinity/callback/call-record",
  "recording_callback_url":"http://your-domain.com/api/v1/infinity/callback/recording",
  "max_concurrent_calls":100,
  "call_timeout_seconds":60,
  "is_active":true,
  "id":1,
  "created_at":"2025-11-21T08:49:11",
  "updated_at":"2025-11-21T08:49:11",
  "created_by":null
}
```

### 前端验证（修复后）
1. 刷新页面
2. 访问"渠道配置" > "甲方渠道管理"
3. 选择甲方后查看"短信"标签
4. 应该能正常加载Infinity配置数据

## 修复影响

### 影响范围
- ✅ 所有Infinity外呼系统相关功能
- ✅ 甲方渠道管理页面
- ✅ 分机池管理功能
- ✅ 外呼功能

### 兼容性
- ✅ 不影响其他功能
- ✅ 只修改前端API路径
- ✅ 后端无需修改

## 相关文件

### 修改的文件
- `frontend/src/api/infinity.ts` - 添加 `/api/v1` 前缀

### 相关后端文件（无需修改）
- `backend/app/api/infinity_config.py` - Infinity配置API
- `backend/app/api/infinity_extension.py` - 分机池管理API  
- `backend/app/api/infinity_call.py` - 外呼核心API
- `backend/app/main.py` - 路由注册（第101-103行）

## 后续建议

### 1. 统一API路径规范
建议在项目文档中明确规定：
- 所有后端API都应该使用 `/api/v1` 前缀
- 前端API调用时必须添加完整路径

### 2. 创建API路径常量
可以考虑在前端创建统一的API路径配置：

```typescript
// frontend/src/config/api.ts
export const API_PREFIX = '/api/v1'

// 使用示例
export function getInfinityConfigByTenant(tenantId: number) {
  return request<InfinityCallConfig>({
    url: `${API_PREFIX}/infinity/configs/${tenantId}`,
    method: 'get'
  })
}
```

### 3. 添加自动化测试
建议为Infinity相关功能添加端到端测试，确保前后端API路径一致。

## 总结

此次修复主要是统一前后端API路径格式，确保前端调用的URL与后端实际路由匹配。修复后，Infinity外呼系统的所有功能应该能够正常工作。

