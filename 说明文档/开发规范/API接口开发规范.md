# API接口开发规范

**创建日期**: 2025-11-25  
**版本**: 1.0.0  
**状态**: ✅ 已生效

---

## 📋 问题总结

### 问题1: 字段格式不一致（驼峰 vs 下划线）

**问题描述**:
- 前端代码期望所有API返回的字段名使用**下划线格式（snake_case）**
- Java后端很多Controller返回的是**驼峰格式（camelCase）**
- 导致前端无法正确读取字段数据，页面显示"没有字段内容"

**影响范围**:
- 字段映射配置页面
- 字段分组管理页面
- 标准字段管理页面
- 自定义字段管理页面
- 甲方管理页面

**修复方法**:
- 统一所有Controller返回的字段名使用下划线格式
- 修复了 `StandardFieldController`, `CustomFieldController`, `FieldGroupController`, `TenantController` 等

---

### 问题2: 缺失的API接口

**问题描述**:
- 前端调用了大量API接口，但Java后端没有实现
- 导致前端页面出现大量500错误

**缺失的接口**:
1. `/api/v1/tenants/{tenantId}/agencies` - 获取甲方机构列表
2. `/api/v1/tenants/{tenantId}/queues` - 获取甲方队列列表
3. `/api/v1/tenants/{tenantId}/fields-json` - 获取甲方字段JSON
4. `/api/v1/tenants/{tenantId}/unmapped-fields` - 获取未映射字段
5. `/api/v1/agencies` - 机构管理接口
6. `/api/v1/agencies/{agencyId}/teams` - 获取机构小组列表
7. `/api/v1/team-groups` - 小组群管理接口
8. `/api/v1/teams` - 小组管理接口
9. `/api/v1/queues` - 队列管理接口
10. `/api/v1/performance/collector/{collectorId}` - 催员绩效接口
11. `/api/v1/field-groups` - 字段分组接口
12. `/api/v1/custom-fields` - 自定义字段接口
13. `/api/v1/standard-fields` - 标准字段接口

**修复方法**:
- 创建了 `AgencyController`, `TeamGroupController`, `TeamController`, `QueueController` 等
- 在 `TenantController` 中添加了缺失的子接口

---

### 问题3: parent_id字段返回null vs undefined

**问题描述**:
- 后端返回的 `parent_id` 为 `null`
- 前端类型定义为 `parent_id?: number`（`number | undefined`）
- 虽然运行时可用，但类型不一致

**修复方法**:
- 一级分组的 `parent_id` 字段不设置（undefined），而不是设置为 `null`

---

## ✅ 开发规范

### 1. 字段命名规范

#### 1.1 统一使用下划线格式（snake_case）

**规则**: 所有API返回的字段名**必须**使用下划线格式

**正确示例**:
```java
Map<String, Object> field = new HashMap<>();
field.put("field_key", "case_number");
field.put("field_name", "案件编号");
field.put("field_type", "text");
field.put("field_group_id", 1L);
field.put("is_required", true);
field.put("sort_order", 1);
field.put("is_active", true);
field.put("created_at", "2025-01-01T00:00:00");
field.put("updated_at", "2025-11-25T00:00:00");
```

**错误示例**:
```java
// ❌ 错误：使用驼峰格式
field.put("fieldKey", "case_number");
field.put("fieldName", "案件编号");
field.put("fieldType", "text");
field.put("fieldGroupId", 1L);
field.put("isRequired", true);
field.put("sortOrder", 1);
field.put("isActive", true);
field.put("createdAt", "2025-01-01T00:00:00");
field.put("updatedAt", "2025-11-25T00:00:00");
```

#### 1.2 常见字段名对照表

| 业务含义 | 正确格式（下划线） | 错误格式（驼峰） |
|---------|------------------|----------------|
| 字段标识 | `field_key` | `fieldKey` |
| 字段名称 | `field_name` | `fieldName` |
| 字段英文名 | `field_name_en` | `fieldNameEn` |
| 字段类型 | `field_type` | `fieldType` |
| 字段分组ID | `field_group_id` | `fieldGroupId` |
| 是否必填 | `is_required` | `isRequired` |
| 是否扩展 | `is_extended` | `isExtended` |
| 示例值 | `example_value` | `exampleValue` |
| 排序顺序 | `sort_order` | `sortOrder` |
| 是否启用 | `is_active` | `isActive` |
| 甲方编码 | `tenant_code` | `tenantCode` |
| 甲方名称 | `tenant_name` | `tenantName` |
| 机构编码 | `agency_code` | `agencyCode` |
| 机构名称 | `agency_name` | `agencyName` |
| 小组编码 | `team_code` | `teamCode` |
| 小组名称 | `team_name` | `teamName` |
| 队列编码 | `queue_code` | `queueCode` |
| 队列名称 | `queue_name` | `queueName` |
| 创建时间 | `created_at` | `createdAt` |
| 更新时间 | `updated_at` | `updatedAt` |

#### 1.3 请求参数兼容性

**规则**: 接收请求参数时，可以兼容两种格式（下划线优先，驼峰作为备选）

**示例**:
```java
@PostMapping
public ResponseData<Map<String, Object>> createField(@RequestBody Map<String, Object> request) {
    Map<String, Object> field = new HashMap<>();
    // 优先使用下划线格式，如果没有则使用驼峰格式
    field.put("field_key", request.get("field_key") != null ? 
        request.get("field_key") : request.get("fieldKey"));
    field.put("field_name", request.get("field_name") != null ? 
        request.get("field_name") : request.get("fieldName"));
    field.put("is_required", request.getOrDefault("is_required", 
        request.getOrDefault("isRequired", false)));
    // ...
}
```

---

### 2. 可选字段处理规范

#### 2.1 null vs undefined

**规则**: 
- 如果字段是可选的（前端类型为 `field?: type`），不设置该字段（undefined），而不是设置为 `null`
- 如果字段是必需的但可能为空，使用 `null`

**正确示例**:
```java
// 一级分组，parent_id不设置（undefined）
Map<String, Object> group = new HashMap<>();
group.put("id", 1L);
group.put("group_name", "基本信息");
// parent_id不设置，表示一级分组
group.put("sort_order", 1);
```

**错误示例**:
```java
// ❌ 错误：设置为null
group.put("parent_id", null);  // 应该不设置这个字段
```

---

### 3. API接口完整性检查

#### 3.1 开发前检查清单

在开发新功能前，必须检查：

- [ ] 前端调用的所有API接口是否都已实现
- [ ] 所有接口的字段格式是否使用下划线格式
- [ ] 所有接口的响应格式是否统一（`ResponseData<T>`）
- [ ] 可选字段是否正确处理（undefined vs null）

#### 3.2 接口实现检查

**步骤1**: 检查前端API调用
```bash
# 搜索前端API调用
grep -r "api/v1" frontend/src/api/
grep -r "request.*url.*api" frontend/src/views/
```

**步骤2**: 检查后端Controller
```bash
# 检查后端Controller
ls backend-java/src/main/java/com/cco/controller/
grep -r "@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" backend-java/src/main/java/com/cco/controller/
```

**步骤3**: 验证字段格式
```bash
# 检查是否有驼峰格式字段
grep -r "put(\".*[A-Z][a-z]" backend-java/src/main/java/com/cco/controller/ | grep -v "//"
```

---

### 4. 代码审查检查点

#### 4.1 字段格式检查

**使用grep检查驼峰格式**:
```bash
cd backend-java
grep -r "put(\".*[A-Z][a-z]" src/main/java/com/cco/controller/ | \
  grep -E "fieldKey|fieldName|fieldType|fieldGroupId|isRequired|sortOrder|tenantCode|tenantName|countryCode|currencyCode|groupKey|groupName|parentId|createdAt|updatedAt" | \
  grep -v "//"
```

**应该返回0个结果**，如果发现驼峰格式，必须修复。

#### 4.2 接口完整性检查

**检查缺失的接口**:
```bash
# 1. 列出前端调用的所有接口
grep -r "api/v1" frontend/src/api/ | grep -o "api/v1/[^\"]*" | sort -u > frontend_apis.txt

# 2. 列出后端实现的所有接口
grep -r "@RequestMapping\|@GetMapping\|@PostMapping" backend-java/src/main/java/com/cco/controller/ | \
  grep -o "api/v1/[^\"]*" | sort -u > backend_apis.txt

# 3. 对比差异
diff frontend_apis.txt backend_apis.txt
```

---

### 5. 测试验证规范

#### 5.1 接口测试

**每个新接口必须测试**:
```bash
# 测试接口返回格式
curl -s "http://localhost:8080/api/v1/xxx" | python3 -m json.tool | head -30

# 检查字段格式
curl -s "http://localhost:8080/api/v1/xxx" | python3 -c "
import sys, json
data = json.load(sys.stdin)
# 检查是否有驼峰格式字段
fields = str(data)
if 'fieldKey' in fields or 'fieldName' in fields:
    print('❌ 发现驼峰格式字段')
    sys.exit(1)
else:
    print('✅ 字段格式正确')
"
```

#### 5.2 前端页面测试

**测试清单**:
- [ ] 页面能正常加载，无500错误
- [ ] 数据能正常显示
- [ ] 表格列能正确显示字段内容
- [ ] 表单能正常提交
- [ ] 浏览器控制台无错误

---

### 6. 常见错误及修复

#### 错误1: 字段格式不一致

**症状**: 页面显示"没有字段内容"，表格列为空

**原因**: 后端返回驼峰格式，前端期望下划线格式

**修复**:
```java
// 修复前
field.put("fieldName", "案件编号");

// 修复后
field.put("field_name", "案件编号");
```

#### 错误2: 接口404/500错误

**症状**: 浏览器控制台显示 `GET http://localhost:8080/api/v1/xxx 500 (Internal Server Error)`

**原因**: 接口未实现或实现有误

**修复**:
1. 检查后端是否有对应的Controller
2. 检查Controller中是否有对应的方法
3. 检查方法路径是否正确
4. 检查方法参数是否正确

#### 错误3: parent_id类型不匹配

**症状**: TypeScript类型检查警告

**原因**: 后端返回 `null`，前端期望 `undefined`

**修复**:
```java
// 修复前
group.put("parent_id", null);

// 修复后
// 不设置parent_id字段（一级分组）
```

---

### 7. 开发流程

#### 7.1 新功能开发流程

1. **需求分析**
   - 分析前端需要哪些API接口
   - 分析每个接口需要返回哪些字段
   - 分析字段的数据类型和格式

2. **接口设计**
   - 设计接口路径（遵循RESTful规范）
   - 设计请求参数格式
   - 设计响应数据格式（使用下划线格式）

3. **后端实现**
   - 创建或修改Controller
   - 实现接口方法
   - 确保字段格式使用下划线格式
   - 确保可选字段正确处理

4. **测试验证**
   - 使用curl测试接口
   - 检查返回数据格式
   - 检查字段格式
   - 前端页面测试

5. **代码审查**
   - 检查字段格式
   - 检查接口完整性
   - 检查错误处理

#### 7.2 修改现有功能流程

1. **检查影响范围**
   - 检查哪些接口会被影响
   - 检查哪些前端页面会被影响

2. **修改代码**
   - 修改后端Controller
   - 确保字段格式不变（或统一改为下划线格式）
   - 确保接口路径不变

3. **测试验证**
   - 测试所有受影响的接口
   - 测试所有受影响的页面

---

### 8. 工具和脚本

#### 8.1 字段格式检查脚本

创建 `scripts/check-field-format.sh`:
```bash
#!/bin/bash
# 检查Controller中是否有驼峰格式字段

echo "检查字段格式..."
grep -r "put(\".*[A-Z][a-z]" backend-java/src/main/java/com/cco/controller/ | \
  grep -E "fieldKey|fieldName|fieldType|fieldGroupId|isRequired|sortOrder|tenantCode|tenantName|countryCode|currencyCode|groupKey|groupName|parentId|createdAt|updatedAt" | \
  grep -v "//" | \
  grep -v "request.get" | \
  wc -l

if [ $? -eq 0 ]; then
    echo "✅ 未发现驼峰格式字段"
else
    echo "❌ 发现驼峰格式字段，请修复"
    exit 1
fi
```

#### 8.2 接口完整性检查脚本

创建 `scripts/check-api-completeness.sh`:
```bash
#!/bin/bash
# 检查前端调用的接口是否都已实现

echo "检查接口完整性..."
# 实现检查逻辑
```

---

### 9. 参考文档

- [字段格式统一修复说明](../问题分析/字段格式统一修复说明.md)
- [前端API调用规则](../../.cursor/rules/frontend-api.mdc)
- [后端API开发规则](../../.cursor/rules/backend-api.mdc)

---

### 10. 检查清单模板

#### 新接口开发检查清单

- [ ] 接口路径遵循RESTful规范
- [ ] 所有返回字段使用下划线格式
- [ ] 可选字段不设置（undefined），不设置为null
- [ ] 接口已测试，返回格式正确
- [ ] 前端页面能正常调用接口
- [ ] 浏览器控制台无错误

#### 修改现有接口检查清单

- [ ] 字段格式未改变（或统一改为下划线格式）
- [ ] 接口路径未改变
- [ ] 请求参数格式未改变（或兼容旧格式）
- [ ] 响应数据格式未改变
- [ ] 所有受影响的页面已测试

---

## 📝 总结

### 核心原则

1. **统一字段格式**: 所有API返回的字段名必须使用下划线格式（snake_case）
2. **接口完整性**: 前端调用的所有接口都必须实现
3. **类型一致性**: 可选字段使用undefined，不使用null
4. **测试验证**: 每个接口都必须测试验证

### 避免重复发生

1. **开发前检查**: 使用检查清单确保不遗漏
2. **代码审查**: 使用grep检查字段格式
3. **自动化检查**: 使用脚本自动检查
4. **文档沉淀**: 及时更新开发规范文档

---

**最后更新**: 2025-11-25  
**维护人员**: 开发团队  
**版本**: 1.0.0













































