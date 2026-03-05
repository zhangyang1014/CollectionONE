# Java后端Mock控制器检查清单

## ✅ 已创建的Mock控制器

### 核心功能控制器
1. **MockAdminAuthController** - `/api/v1/admin/auth/*`
   - 登录、登出、获取用户信息

2. **MockCaseController** - `/api/v1/cases/*`
   - 案件列表、案件详情、案件统计、案件分配等

3. **MockTenantController** - `/api/v1/tenants/*`
   - 甲方管理、甲方字段配置

4. **MockAgencyController** - `/api/v1/agencies/*`
   - 催收机构管理、机构作息时间、机构下的小组列表

5. **MockQueueController** - `/api/v1/queues/*` ✅ **新创建**
   - 队列管理、队列字段配置

6. **MockCollectorController** - `/api/v1/collectors/*` ✅ **新创建**
   - 催员管理、催员详情、催员登录人脸记录

### 字段配置控制器
7. **MockFieldGroupController** - `/api/v1/field-groups/*` ✅ **已创建**
   - 字段分组管理

8. **MockStandardFieldController** - `/api/v1/standard-fields/*` ✅ **已创建**
   - 标准字段管理

9. **MockCustomFieldController** - `/api/v1/custom-fields/*` ✅ **新创建**
   - 自定义字段管理

10. **MockFieldDisplayConfigController** - `/api/v1/field-display-configs/*`
    - 字段展示配置

### 其他功能控制器
11. **MockCommunicationController** - `/api/v1/communications/*`
    - 通信记录

12. **MockChannelSupplierController** - `/api/v1/channel-suppliers/*`
    - 渠道供应商管理

13. **MockInfinityController** - `/api/v1/infinity/*`
    - Infinity外呼系统配置

14. **MockNotificationController** - `/api/v1/notification-configs/*`
    - 通知配置

15. **MockPaymentController** - `/api/v1/payments/*`
    - 支付相关

16. **MockPermissionController** - `/api/v1/permissions/*`
    - 权限管理

17. **MockImController** - `/api/v1/im/*`
    - 催员端IM相关

## ⚠️ 部分实现的接口

### 在MockAgencyController中已实现
- `/api/v1/agencies/{id}/teams` - 获取机构下的小组列表 ✅

### 在MockTenantController中已实现
- `/api/v1/tenants/{tenantId}/queues` - 获取甲方队列列表 ✅
- `/api/v1/tenants/{tenantId}/agencies` - 获取甲方机构列表 ✅

## ❌ 尚未创建的Mock控制器（可选，根据实际使用情况）

### 数据看板相关
1. **PTP管理** - `/api/v1/ptp/*`
   - 创建PTP承诺、获取PTP列表、更新PTP状态、PTP统计

2. **质检** - `/api/v1/quality-inspections/*`
   - 创建质检记录、获取质检记录列表、质检统计

3. **催员绩效** - `/api/v1/performance/*`
   - 获取催员绩效看板、趋势数据、排名、案件明细

4. **自定义维度分析** - `/api/v1/analytics/*`
   - 可分析字段列表、统计数据、图表数据

5. **预警** - `/api/v1/alerts/*`
   - 催员预警、小组预警、机构预警

6. **空闲催员监控** - `/api/v1/idle-monitor/*`
   - 空闲监控配置、总览数据、详情列表、趋势数据

### 其他功能
7. **小组群** - `/api/v1/team-groups/*`
   - 小组群管理（如果前端使用）

8. **催收小组** - `/api/v1/teams/*`
   - 催收小组管理（部分在MockAgencyController中）

9. **通知模板** - `/api/v1/notification-templates/*`
   - 通知模板管理

10. **公共通知** - `/api/v1/public-notifications/*`
    - 公共通知管理

## 📝 注意事项

1. **所有Mock控制器都使用 `ResponseData` 统一响应格式**
2. **所有接口都添加了日志输出，方便调试**
3. **Mock数据尽量模拟真实数据结构**
4. **支持常见的查询参数（分页、过滤等）**

## 🔍 检查方法

如果前端调用某个接口出现500错误，检查步骤：
1. 查看浏览器控制台的错误信息
2. 查看后端日志，确认是否收到请求
3. 检查是否有对应的Mock控制器
4. 如果没有，创建对应的Mock控制器
5. 重启后端服务

## 🚀 使用说明

所有Mock控制器都在 `backend-java/src/main/java/com/cco/controller/` 目录下。

创建新的Mock控制器时，请遵循以下规范：
1. 类名格式：`Mock{功能名}Controller`
2. 使用 `@RestController` 注解
3. 使用 `@RequestMapping(Constants.API_V1_PREFIX + "/{path}")` 定义路径
4. 返回格式统一使用 `ResponseData.success(data)` 或 `ResponseData.error(code, message)`
5. 添加日志输出，方便调试
6. 支持常见的查询参数（skip, limit, filter等）



















































