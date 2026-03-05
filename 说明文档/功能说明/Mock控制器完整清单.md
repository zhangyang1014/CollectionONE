# Java后端Mock控制器完整清单

## 📋 概述

**更新时间**：2025-11-22 11:40  
**服务端口**：8000  
**认证模式**：已禁用（允许所有请求）

本文档列出所有已实现的Mock控制器及其API端点。

---

## 🎯 已实现的Mock控制器

### 1. MockTenantController - 甲方管理

**文件**：`backend-java/src/main/java/com/cco/controller/MockTenantController.java`

| 方法 | 路径 | 功能 | 返回数据 |
|------|------|------|----------|
| GET | `/api/v1/tenants` | 获取甲方列表 | 3个甲方 |
| GET | `/api/v1/tenants/{id}` | 获取甲方详情 | 单个甲方详情 |
| GET | `/api/v1/tenants/{id}/queues` | 获取甲方队列 | 队列列表 |
| GET | `/api/v1/tenants/{id}/agencies` | 获取甲方机构 | 机构列表 |

**Mock数据**：
- 百熵企业 (ID: 1, tenant_code: baishang)
- 测试甲方A (ID: 2, tenant_code: test_a)
- 测试甲方B (ID: 3, tenant_code: test_b)

### 2. MockPermissionController - 权限管理

**文件**：`backend-java/src/main/java/com/cco/controller/MockPermissionController.java`

| 方法 | 路径 | 功能 | 返回数据 |
|------|------|------|----------|
| GET | `/api/v1/permissions/modules` | 获取权限模块 | 3个模块 |
| GET | `/api/v1/permissions/items` | 获取权限项 | 权限项列表 |
| GET | `/api/v1/permissions/configs` | 获取权限配置 | 权限配置 |
| PUT | `/api/v1/permissions/configs` | 批量更新权限 | 更新结果 |
| GET | `/api/v1/permissions/configurable-roles` | 获取可配置角色 | 角色列表 |
| GET | `/api/v1/permissions/matrix` | 获取权限矩阵 | 完整矩阵 |
| DELETE | `/api/v1/permissions/configs/{id}` | 删除权限配置 | 删除结果 |

**权限模块**：
- 案件管理 (case_management)
- 催收管理 (collection_management)
- 系统管理 (system_management)

**角色列表**：
- SuperAdmin - 超级管理员
- TenantAdmin - 甲方管理员
- AgencyAdmin - 机构管理员
- TeamAdmin - 小组管理员
- Collector - 催员

### 3. MockCaseController - 案件管理

**文件**：`backend-java/src/main/java/com/cco/controller/MockCaseController.java`

| 方法 | 路径 | 功能 | 返回数据 |
|------|------|------|----------|
| GET | `/api/v1/cases` | 获取案件列表 | 分页案件列表 |
| GET | `/api/v1/cases/{id}` | 获取案件详情 | 案件详细信息 |
| GET | `/api/v1/cases/statistics` | 获取案件统计 | 统计数据 |
| POST | `/api/v1/cases/{id}/assign` | 分配案件 | 分配结果 |
| PUT | `/api/v1/cases/{id}/status` | 更新案件状态 | 更新结果 |
| GET | `/api/v1/cases/{id}/collection-records` | 获取案件催记 | 催记列表 |
| POST | `/api/v1/cases/{id}/collection-records` | 添加催记 | 添加结果 |
| POST | `/api/v1/cases/batch-import` | 批量导入案件 | 导入结果 |

**查询参数**：
- `tenant_id` - 甲方ID
- `queue_id` - 队列ID
- `status` - 案件状态
- `page` - 页码（默认1）
- `pageSize` - 每页数量（默认20）

**Mock数据特点**：
- 每次请求返回10条案件
- 总计100条案件（模拟）
- 支持分页
- 包含完整的客户信息、贷款详情

---

## 🔧 API测试示例

### 测试甲方列表
```bash
curl http://localhost:8000/api/v1/tenants
```

### 测试案件列表（带参数）
```bash
curl "http://localhost:8000/api/v1/cases?tenant_id=1&page=1&pageSize=10"
```

### 测试案件详情
```bash
curl http://localhost:8000/api/v1/cases/1
```

### 测试权限模块
```bash
curl http://localhost:8000/api/v1/permissions/modules
```

### 测试案件统计
```bash
curl http://localhost:8000/api/v1/cases/statistics
```

### 测试催记列表
```bash
curl http://localhost:8000/api/v1/cases/1/collection-records
```

---

## 📊 响应格式

所有API统一使用以下响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 实际数据
  }
}
```

### 成功响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "page_size": 20,
    "total_pages": 5
  }
}
```

### 错误响应示例
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

---

## ⚠️ Mock数据限制

### 不支持的功能
- ❌ **数据持久化**：所有修改在重启后丢失
- ❌ **真实验证**：不进行数据库校验
- ❌ **关联查询**：不检查关联数据存在性
- ❌ **事务控制**：不支持事务回滚

### 支持的功能
- ✅ **查询接口**：所有GET请求正常工作
- ✅ **接口格式**：响应格式与真实API一致
- ✅ **分页功能**：支持分页参数
- ✅ **参数过滤**：接受查询参数（但不实际过滤）
- ✅ **前端开发**：完全满足前端UI开发需求

---

## 🚀 启动命令

```bash
cd /Users/zhangyang/Documents/GitHub/CollectionONE/backend-java
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
java -jar target/cco-backend-1.0.0.jar --server.port=8000
```

---

## 📁 相关配置文件

### Security配置
**文件**：`backend-java/src/main/java/com/cco/common/config/SecurityConfig.java`
- ✅ 已禁用JWT认证
- ✅ 允许所有请求
- ✅ CORS已配置

### CORS配置
**文件**：`backend-java/src/main/java/com/cco/common/config/CorsConfig.java`
- ✅ 允许前端5173端口访问
- ✅ 允许所有HTTP方法
- ✅ 允许携带认证信息

### 主应用
**文件**：`backend-java/src/main/java/com/cco/CcoApplication.java`
- ✅ 已排除数据库自动配置
- ✅ 可以在无数据库环境运行

---

## 🔄 迁移到真实后端

当需要切换到真实的数据库后端时：

### 1. 恢复真实控制器
```bash
cd backend-java/src/main/java/com/cco/controller
mv PermissionController.java.bak PermissionController.java
mv AuthController.java.bak AuthController.java
mv StandardFieldController.java.bak StandardFieldController.java
```

### 2. 删除Mock控制器
```bash
rm MockPermissionController.java
rm MockTenantController.java
rm MockCaseController.java
```

### 3. 恢复Service实现
```bash
cd backend-java/src/main/java/com/cco/service/impl
mv PermissionServiceImpl.java.bak PermissionServiceImpl.java
mv StandardFieldServiceImpl.java.bak StandardFieldServiceImpl.java
```

### 4. 恢复Security配置
在 `SecurityConfig.java` 中恢复原来的认证规则：
```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/", "/health", "/api/v1/admin/auth/**").permitAll()
    .anyRequest().authenticated()
)
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

### 5. 恢复数据库配置
在 `CcoApplication.java` 中移除exclude：
```java
@SpringBootApplication  // 移除exclude参数
@MapperScan("com.cco.mapper")
public class CcoApplication {
```

### 6. 配置数据库
在 `application-dev.yml` 中配置正确的数据库连接信息。

---

## 🛑 停止服务

```bash
# 查找进程
ps aux | grep cco-backend | grep -v grep

# 停止服务
kill <PID>

# 或强制停止所有
pkill -f "cco-backend"
```

---

## ✅ 验证清单

在前端刷新页面后，应该能看到：

- ✅ 甲方下拉列表正常显示3个甲方
- ✅ 案件列表正常显示10条案件
- ✅ 案件详情可以正常查看
- ✅ 权限配置页面正常加载
- ✅ 无404错误
- ✅ 无500错误
- ✅ 无认证错误

---

## 📞 故障排除

### 如果前端仍然报错

1. **检查后端是否运行**
```bash
curl http://localhost:8000/api/v1/tenants
```

2. **查看后端日志**
```bash
tail -f backend-java/backend.log
```

3. **检查端口占用**
```bash
lsof -i:8000
```

4. **重启后端**
```bash
pkill -f "cco-backend"
cd backend-java
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
java -jar target/cco-backend-1.0.0.jar --server.port=8000
```

---

**创建时间**：2025-11-22  
**最后更新**：2025-11-22 11:40  
**状态**：✅ 所有Mock API正常工作






















































