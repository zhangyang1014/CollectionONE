# 字段流转 - Java后端实现说明

## 📋 概述

本文档说明Java后端对"甲方字段展示配置"功能的完整支持,包括实体类、API接口、服务层实现和数据库初始化。

---

## 🏗️ 架构层次

```
Controller层 (API接口)
    ↓
Service层 (业务逻辑)
    ↓
Mapper层 (数据访问)
    ↓
Entity层 (实体类)
    ↓
Database (MySQL数据库)
```

---

## 📦 核心组件

### 1. 实体类 (Entity)

**文件**: `TenantFieldDisplayConfig.java`

**说明**: 甲方字段展示配置实体类,对应数据库表 `tenant_field_display_configs`

**主要字段**:

```java
@Data
@TableName("tenant_field_display_configs")
public class TenantFieldDisplayConfig extends BaseEntity {
    private Long id;                        // 主键ID
    private Long tenantId;                  // 甲方ID
    private String sceneType;               // 场景类型
    private String sceneName;               // 场景名称
    private String fieldKey;                // 字段标识
    private String fieldName;               // 字段名称
    private String fieldDataType;           // 字段数据类型
    private String fieldSource;             // 字段来源
    private Integer sortOrder;              // 排序顺序
    private Integer displayWidth;           // 显示宽度
    private String colorType;               // 颜色类型
    
    // JSON字段
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> colorRule;  // 颜色规则
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> hideRule;   // 隐藏规则
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForQueues;       // 对队列隐藏
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForAgencies;     // 对机构隐藏
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> hideForTeams;        // 对小组隐藏
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> formatRule; // 格式化规则
    
    // 筛选功能
    private Boolean isSearchable;           // 是否可搜索
    private Boolean isFilterable;           // 是否可筛选
    private Boolean isRangeSearchable;      // 是否支持范围检索
    
    // 审计字段
    private String createdBy;               // 创建人
    private String updatedBy;               // 更新人
}
```

**注意事项**:
- JSON字段使用 `JacksonTypeHandler` 进行序列化/反序列化
- 继承 `BaseEntity` 自动获得 `createdAt` 和 `updatedAt` 字段
- 使用 `@TableName` 指定表名
- 使用 `autoResultMap = true` 支持JSON字段映射

---

### 2. Mapper接口

**文件**: `TenantFieldDisplayConfigMapper.java`

**说明**: MyBatis-Plus Mapper接口,提供基础CRUD操作

```java
@Mapper
public interface TenantFieldDisplayConfigMapper 
    extends BaseMapper<TenantFieldDisplayConfig> {
    // 继承BaseMapper即可获得基础CRUD方法
}
```

**可用方法** (继承自BaseMapper):
- `insert(entity)` - 插入
- `deleteById(id)` - 根据ID删除
- `updateById(entity)` - 根据ID更新
- `selectById(id)` - 根据ID查询
- `selectList(queryWrapper)` - 条件查询
- 等等...

---

### 3. Service层

#### 3.1 服务接口

**文件**: `FieldDisplayConfigService.java`

**方法定义**:

```java
public interface FieldDisplayConfigService {
    // 查询
    List<TenantFieldDisplayConfig> list(Long tenantId, String sceneType, String fieldKey);
    TenantFieldDisplayConfig getById(Long id);
    List<AvailableField> getAvailableFields(Long tenantId);
    
    // 创建
    TenantFieldDisplayConfig create(FieldDisplayConfigDTO.Create dto);
    
    // 更新
    TenantFieldDisplayConfig update(Long id, FieldDisplayConfigDTO.Update dto);
    void batchUpdate(FieldDisplayConfigDTO.BatchUpdate dto);
    
    // 删除
    void deleteById(Long id);
}
```

#### 3.2 服务实现

**文件**: `FieldDisplayConfigServiceImpl.java`

**核心逻辑**:

##### 查询配置列表

```java
@Override
public List<TenantFieldDisplayConfig> list(
    Long tenantId, String sceneType, String fieldKey
) {
    LambdaQueryWrapper<TenantFieldDisplayConfig> wrapper = new LambdaQueryWrapper<>();
    
    // 动态条件
    if (tenantId != null) {
        wrapper.eq(TenantFieldDisplayConfig::getTenantId, tenantId);
    }
    if (sceneType != null && !sceneType.isEmpty()) {
        wrapper.eq(TenantFieldDisplayConfig::getSceneType, sceneType);
    }
    if (fieldKey != null && !fieldKey.isEmpty()) {
        wrapper.eq(TenantFieldDisplayConfig::getFieldKey, fieldKey);
    }
    
    // 排序: 场景类型 > 排序顺序 > ID
    wrapper.orderByAsc(
        TenantFieldDisplayConfig::getSceneType,
        TenantFieldDisplayConfig::getSortOrder,
        TenantFieldDisplayConfig::getId
    );
    
    return this.list(wrapper);
}
```

##### 获取可用字段

```java
@Override
public List<AvailableField> getAvailableFields(Long tenantId) {
    List<AvailableField> result = new ArrayList<>();
    
    // 1. 添加所有标准字段
    List<StandardField> standardFields = standardFieldService.list();
    for (StandardField field : standardFields) {
        AvailableField availableField = new AvailableField();
        availableField.setFieldKey(field.getFieldKey());
        availableField.setFieldName(field.getFieldName());
        availableField.setFieldType(field.getFieldType());
        availableField.setFieldSource("standard");
        // ...
        result.add(availableField);
    }
    
    // 2. 如果指定了甲方,添加自定义字段
    if (tenantId != null) {
        List<CustomField> customFields = customFieldService.listByTenantId(tenantId);
        for (CustomField field : customFields) {
            AvailableField availableField = new AvailableField();
            availableField.setFieldKey(field.getFieldKey());
            availableField.setFieldName(field.getFieldName());
            availableField.setFieldType(field.getFieldType());
            availableField.setFieldSource("custom");
            // ...
            result.add(availableField);
        }
    }
    
    return result;
}
```

##### 批量更新

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void batchUpdate(BatchUpdate dto) {
    if (dto.getConfigs() == null || dto.getConfigs().isEmpty()) {
        return;
    }
    
    for (ConfigUpdate item : dto.getConfigs()) {
        TenantFieldDisplayConfig config = this.getById(item.getId());
        if (config == null) continue;
        
        // 只更新非null字段
        if (item.getSortOrder() != null) 
            config.setSortOrder(item.getSortOrder());
        if (item.getDisplayWidth() != null) 
            config.setDisplayWidth(item.getDisplayWidth());
        // ...
        
        this.updateById(config);
    }
}
```

---

### 4. Controller层

**文件**: `FieldDisplayConfigController.java`

**API端点**:

#### 4.1 获取场景类型

```java
@GetMapping("/scene-types")
public Result<?> getSceneTypes() {
    List<SceneType> sceneTypes = List.of(
        new SceneType("admin_case_list", "控台案件管理列表", "..."),
        new SceneType("collector_case_list", "催员案件列表", "..."),
        new SceneType("collector_case_detail", "催员案件详情", "...")
    );
    return Result.success(sceneTypes);
}
```

**请求**: `GET /api/v1/field-display-configs/scene-types`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "key": "admin_case_list",
      "name": "控台案件管理列表",
      "description": "管理后台的案件列表页面"
    },
    ...
  ]
}
```

#### 4.2 获取配置列表

```java
@GetMapping("")
public Result<?> getFieldDisplayConfigs(
    @RequestParam(required = false) Long tenantId,
    @RequestParam(required = false) String sceneType,
    @RequestParam(required = false) String fieldKey
) {
    List<TenantFieldDisplayConfig> configs = 
        fieldDisplayConfigService.list(tenantId, sceneType, fieldKey);
    return Result.success(configs);
}
```

**请求**: `GET /api/v1/field-display-configs?tenant_id=1&scene_type=admin_case_list`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "tenantId": 1,
      "sceneType": "admin_case_list",
      "sceneName": "控台案件管理列表",
      "fieldKey": "case_code",
      "fieldName": "案件编号",
      "fieldDataType": "String",
      "fieldSource": "standard",
      "sortOrder": 1,
      "displayWidth": 180,
      "colorType": "normal",
      "isSearchable": true,
      "isFilterable": false,
      "isRangeSearchable": false,
      "createdAt": "2025-11-22 10:00:00",
      "updatedAt": "2025-11-22 10:00:00"
    },
    ...
  ]
}
```

#### 4.3 创建配置

```java
@PostMapping("")
public Result<?> createFieldDisplayConfig(
    @RequestBody FieldDisplayConfigDTO.Create dto
) {
    TenantFieldDisplayConfig config = fieldDisplayConfigService.create(dto);
    return Result.success(config);
}
```

**请求**: `POST /api/v1/field-display-configs`

**请求体**:
```json
{
  "tenantId": 1,
  "sceneType": "admin_case_list",
  "sceneName": "控台案件管理列表",
  "fieldKey": "new_field",
  "fieldName": "新字段",
  "fieldDataType": "String",
  "fieldSource": "standard",
  "sortOrder": 100,
  "displayWidth": 120,
  "colorType": "normal",
  "isSearchable": true,
  "isFilterable": false,
  "isRangeSearchable": false,
  "createdBy": "admin"
}
```

#### 4.4 批量更新

```java
@PutMapping("/batch")
public Result<?> batchUpdateFieldDisplayConfigs(
    @RequestBody FieldDisplayConfigDTO.BatchUpdate dto
) {
    fieldDisplayConfigService.batchUpdate(dto);
    return Result.success("批量更新成功");
}
```

**请求**: `PUT /api/v1/field-display-configs/batch`

**请求体**:
```json
{
  "configs": [
    {
      "id": 1,
      "sortOrder": 2,
      "displayWidth": 150
    },
    {
      "id": 2,
      "sortOrder": 1,
      "isSearchable": false
    }
  ]
}
```

#### 4.5 获取可用字段

```java
@GetMapping("/available-fields")
public Result<?> getAvailableFields(
    @RequestParam(required = false) Long tenantId
) {
    List<AvailableField> fields = 
        fieldDisplayConfigService.getAvailableFields(tenantId);
    return Result.success(fields);
}
```

**请求**: `GET /api/v1/field-display-configs/available-fields?tenant_id=1`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "fieldKey": "case_code",
      "fieldName": "案件编号",
      "fieldType": "String",
      "fieldSource": "standard",
      "fieldGroupId": 1,
      "isExtended": false,
      "description": "案件的唯一编号"
    },
    ...
  ]
}
```

---

### 5. DTO类

**文件**: `FieldDisplayConfigDTO.java`

包含多个内部类:

#### 5.1 SceneType - 场景类型

```java
@Data
public static class SceneType {
    private String key;         // 场景key
    private String name;        // 场景名称
    private String description; // 场景描述
}
```

#### 5.2 AvailableField - 可用字段

```java
@Data
public static class AvailableField {
    private String fieldKey;        // 字段标识
    private String fieldName;       // 字段名称
    private String fieldType;       // 字段类型
    private String fieldSource;     // 字段来源
    private Long fieldGroupId;      // 所属分组ID
    private Boolean isExtended;     // 是否为拓展字段
    private String description;     // 字段描述
}
```

#### 5.3 Create - 创建DTO

```java
@Data
public static class Create {
    private Long tenantId;                      // 必填
    private String sceneType;                   // 必填
    private String sceneName;                   // 必填
    private String fieldKey;                    // 必填
    private String fieldName;                   // 必填
    private String fieldDataType;               // 可选
    private String fieldSource;                 // 可选
    private Integer sortOrder = 0;              // 默认0
    private Integer displayWidth = 0;           // 默认0
    private String colorType = "normal";        // 默认normal
    private Map<String, Object> colorRule;      // 可选
    private Map<String, Object> hideRule;       // 可选
    private List<Long> hideForQueues;           // 可选
    private List<Long> hideForAgencies;         // 可选
    private List<Long> hideForTeams;            // 可选
    private Map<String, Object> formatRule;     // 可选
    private Boolean isSearchable = false;       // 默认false
    private Boolean isFilterable = false;       // 默认false
    private Boolean isRangeSearchable = false;  // 默认false
    private String createdBy;                   // 可选
}
```

#### 5.4 Update - 更新DTO

```java
@Data
public static class Update {
    private String fieldName;               // 所有字段都可选
    private Integer sortOrder;              // 只更新非null字段
    private Integer displayWidth;
    private String colorType;
    private Map<String, Object> colorRule;
    private Map<String, Object> hideRule;
    private List<Long> hideForQueues;
    private List<Long> hideForAgencies;
    private List<Long> hideForTeams;
    private Map<String, Object> formatRule;
    private Boolean isSearchable;
    private Boolean isFilterable;
    private Boolean isRangeSearchable;
    private String updatedBy;
}
```

#### 5.5 BatchUpdate - 批量更新DTO

```java
@Data
public static class BatchUpdate {
    private List<ConfigUpdate> configs;  // 配置项列表
}

@Data
public static class ConfigUpdate {
    private Long id;                     // 必填
    private Integer sortOrder;           // 可选
    private Integer displayWidth;        // 可选
    private String colorType;            // 可选
    private Boolean isSearchable;        // 可选
    private Boolean isFilterable;        // 可选
    private Boolean isRangeSearchable;   // 可选
}
```

---

## 🗄️ 数据库

### 表结构

**表名**: `tenant_field_display_configs`

**说明**: 已在 `schema.sql` 中定义,无需修改

**字段列表**:

| 字段名 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| id | BIGINT | 主键ID | AUTO_INCREMENT |
| tenant_id | BIGINT | 甲方ID | NOT NULL |
| scene_type | VARCHAR(50) | 场景类型 | NOT NULL |
| scene_name | VARCHAR(100) | 场景名称 | NOT NULL |
| field_key | VARCHAR(100) | 字段标识 | NOT NULL |
| field_name | VARCHAR(200) | 字段名称 | NOT NULL |
| field_data_type | VARCHAR(50) | 字段数据类型 | NULL |
| field_source | VARCHAR(20) | 字段来源 | NULL |
| sort_order | INT | 排序顺序 | 0 |
| display_width | INT | 显示宽度 | 0 |
| color_type | VARCHAR(20) | 颜色类型 | 'normal' |
| color_rule | JSON | 颜色规则 | NULL |
| hide_rule | JSON | 隐藏规则 | NULL |
| hide_for_queues | JSON | 对队列隐藏 | NULL |
| hide_for_agencies | JSON | 对机构隐藏 | NULL |
| hide_for_teams | JSON | 对小组隐藏 | NULL |
| format_rule | JSON | 格式化规则 | NULL |
| is_searchable | TINYINT(1) | 是否可搜索 | 0 |
| is_filterable | TINYINT(1) | 是否可筛选 | 0 |
| is_range_searchable | TINYINT(1) | 是否范围检索 | 0 |
| created_at | DATETIME | 创建时间 | CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | CURRENT_TIMESTAMP |
| created_by | VARCHAR(100) | 创建人 | NULL |
| updated_by | VARCHAR(100) | 更新人 | NULL |

**索引**:
- PRIMARY KEY: `id`
- INDEX: `idx_tenant_scene` (`tenant_id`, `scene_type`)
- INDEX: `idx_field_key` (`field_key`)
- FOREIGN KEY: `fk_display_config_tenant` (`tenant_id` -> `tenants.id`)

### 初始化数据

**文件**: `init_field_display_configs.sql`

**说明**: 为甲方A (tenant_id=1) 创建三个场景的默认配置

**数据量**:
- 控台案件管理列表: 10条
- 催员案件列表: 8条
- 催员案件详情: 12条
- **总计: 30条**

**示例数据**:

```sql
-- 控台案件管理列表 - 案件编号
INSERT INTO `tenant_field_display_configs` 
(`tenant_id`, `scene_type`, `scene_name`, `field_key`, `field_name`, 
 `field_data_type`, `field_source`, `sort_order`, `display_width`, 
 `color_type`, `is_searchable`, `created_by`) 
VALUES
(1, 'admin_case_list', '控台案件管理列表', 'case_code', '案件编号', 
 'String', 'standard', 1, 180, 'normal', 1, 'system');
```

---

## 🔄 完整API列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/field-display-configs/scene-types` | 获取所有场景类型 |
| GET | `/api/v1/field-display-configs` | 获取配置列表(支持筛选) |
| GET | `/api/v1/field-display-configs/{id}` | 获取指定配置 |
| POST | `/api/v1/field-display-configs` | 创建配置 |
| PUT | `/api/v1/field-display-configs/{id}` | 更新配置 |
| PUT | `/api/v1/field-display-configs/batch` | 批量更新配置 |
| DELETE | `/api/v1/field-display-configs/{id}` | 删除配置 |
| GET | `/api/v1/field-display-configs/available-fields` | 获取可用字段 |

---

## 🚀 使用示例

### 前端调用示例

```typescript
// 获取控台案件列表的字段配置
const response = await fetch(
  '/api/v1/field-display-configs?tenant_id=1&scene_type=admin_case_list'
);
const configs = await response.json();

// 批量更新配置
await fetch('/api/v1/field-display-configs/batch', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    configs: [
      { id: 1, sortOrder: 2 },
      { id: 2, sortOrder: 1 }
    ]
  })
});
```

---

## 📝 开发注意事项

1. **JSON字段处理**
   - 使用 `JacksonTypeHandler` 自动序列化
   - 在实体类上添加 `@TableField(typeHandler = JacksonTypeHandler.class)`
   - 确保 MyBatis-Plus 配置正确

2. **事务管理**
   - 批量更新操作使用 `@Transactional`
   - 异常时自动回滚 (`rollbackFor = Exception.class`)

3. **Null值处理**
   - Update DTO中null值不覆盖原有值
   - 使用条件判断避免空指针

4. **排序规则**
   - 默认按 `sceneType` > `sortOrder` > `id` 排序
   - 确保同一场景内字段有序

---

## 🔗 相关文档

- [字段流转完整性校准报告](./字段流转完整性校准报告.md)
- [字段流转使用指南](./字段流转使用指南-快速开始.md)
- [字段流转优化实施总结](./字段流转优化-实施总结.md)

---

**文档版本**: 1.0  
**最后更新**: 2025-11-22  
**维护人**: AI Assistant











































