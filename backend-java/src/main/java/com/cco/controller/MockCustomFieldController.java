package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 自定义字段Mock控制器
 * 提供自定义字段相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/custom-fields")
public class MockCustomFieldController {

    /**
     * 获取自定义字段列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getCustomFields(
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(value = "field_group_id", required = false) Long fieldGroupId,
            @RequestParam(value = "tenant_id", required = false) Long tenantId,
            @RequestParam(value = "include_deleted", required = false) String includeDeletedStr
    ) {
        // 处理include_deleted参数（可能是字符串"true"/"false"或布尔值）
        Boolean includeDeleted = false;
        if (includeDeletedStr != null) {
            includeDeleted = Boolean.parseBoolean(includeDeletedStr);
        }
        System.out.println("===============================================");
        System.out.println("[自定义字段API] 接收参数:");
        System.out.println("  skip = " + skip);
        System.out.println("  limit = " + limit);
        System.out.println("  fieldGroupId = " + fieldGroupId);
        System.out.println("  tenantId = " + tenantId);
        System.out.println("  includeDeleted = " + includeDeleted);
        System.out.println("===============================================");
        
        List<Map<String, Object>> allFields = generateMockFields(tenantId);
        
        // 根据field_group_id过滤
        if (fieldGroupId != null) {
            allFields = allFields.stream()
                    .filter(field -> fieldGroupId.equals(field.get("field_group_id")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 根据tenant_id过滤
        if (tenantId != null) {
            allFields = allFields.stream()
                    .filter(field -> tenantId.equals(field.get("tenant_id")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 过滤已删除的字段
        if (!includeDeleted) {
            allFields = allFields.stream()
                    .filter(field -> !Boolean.TRUE.equals(field.get("is_deleted")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 按sort_order排序
        allFields.sort((a, b) -> {
            Integer orderA = (Integer) a.getOrDefault("sort_order", 0);
            Integer orderB = (Integer) b.getOrDefault("sort_order", 0);
            return orderA.compareTo(orderB);
        });
        
        // 应用分页
        int start = Math.min(skip, allFields.size());
        int end = Math.min(skip + limit, allFields.size());
        List<Map<String, Object>> pagedFields = allFields.subList(start, end);
        
        return ResponseData.success(pagedFields);
    }
    
    /**
     * 生成Mock自定义字段数据
     */
    private List<Map<String, Object>> generateMockFields(Long tenantId) {
        List<Map<String, Object>> fields = new ArrayList<>();
        Long defaultTenantId = tenantId != null ? tenantId : 1L;
        
        // 为每个甲方创建一些自定义字段示例
        fields.add(createField(1L, "custom_remark", "备注", "Remark", "String", 1L, defaultTenantId, false, "自定义备注字段", "这是备注", 1));
        fields.add(createField(2L, "custom_priority", "优先级", "Priority", "Enum", 1L, defaultTenantId, false, "自定义优先级字段", null, 2));
        fields.add(createField(3L, "custom_score", "评分", "Score", "Integer", 1L, defaultTenantId, false, "自定义评分字段", "85", 3));
        
        return fields;
    }
    
    /**
     * 创建自定义字段对象
     */
    private Map<String, Object> createField(
            Long id,
            String fieldKey,
            String fieldName,
            String fieldNameEn,
            String fieldType,
            Long fieldGroupId,
            Long tenantId,
            Boolean isRequired,
            String description,
            String exampleValue,
            Integer sortOrder
    ) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("field_key", fieldKey);
        field.put("field_name", fieldName);
        field.put("field_name_en", fieldNameEn);
        field.put("field_type", fieldType);
        field.put("field_group_id", fieldGroupId);
        field.put("tenant_id", tenantId);
        field.put("is_required", isRequired);
        field.put("description", description);
        field.put("example_value", exampleValue);
        field.put("validation_rules", new HashMap<>());
        
        // 如果是Enum类型，添加枚举选项
        if ("Enum".equals(fieldType)) {
            List<Map<String, Object>> enumOptions = new ArrayList<>();
            if ("custom_priority".equals(fieldKey)) {
                enumOptions.add(createEnumOption("high", "高"));
                enumOptions.add(createEnumOption("medium", "中"));
                enumOptions.add(createEnumOption("low", "低"));
            }
            field.put("enum_options", enumOptions);
        } else {
            field.put("enum_options", null);
        }
        
        field.put("sort_order", sortOrder);
        field.put("is_active", true);
        field.put("is_deleted", false);
        field.put("deleted_at", null);
        field.put("created_at", LocalDateTime.now().minusDays(30).toString());
        field.put("updated_at", LocalDateTime.now().toString());
        
        return field;
    }
    
    /**
     * 创建枚举选项
     */
    private Map<String, Object> createEnumOption(String value, String label) {
        Map<String, Object> option = new HashMap<>();
        option.put("value", value);
        option.put("label", label);
        return option;
    }
    
    /**
     * 获取单个自定义字段
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getCustomField(@PathVariable Long id) {
        List<Map<String, Object>> allFields = generateMockFields(null);
        Map<String, Object> field = allFields.stream()
                .filter(f -> id.equals(f.get("id")))
                .findFirst()
                .orElse(null);
        
        if (field == null) {
            return ResponseData.error(404, "自定义字段不存在");
        }
        
        return ResponseData.success(field);
    }
    
    /**
     * 创建自定义字段
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createCustomField(@RequestBody Map<String, Object> request) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", System.currentTimeMillis());
        field.put("field_key", request.get("field_key"));
        field.put("field_name", request.get("field_name"));
        field.put("field_name_en", request.get("field_name_en"));
        field.put("field_type", request.get("field_type"));
        field.put("field_group_id", request.get("field_group_id"));
        field.put("tenant_id", request.get("tenant_id"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value"));
        field.put("validation_rules", request.getOrDefault("validation_rules", new HashMap<>()));
        field.put("enum_options", request.get("enum_options"));
        field.put("sort_order", request.getOrDefault("sort_order", 0));
        field.put("is_active", request.getOrDefault("is_active", true));
        field.put("is_deleted", false);
        field.put("deleted_at", null);
        field.put("created_at", LocalDateTime.now().toString());
        field.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(field);
    }
    
    /**
     * 更新自定义字段
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateCustomField(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        List<Map<String, Object>> allFields = generateMockFields(null);
        Map<String, Object> field = allFields.stream()
                .filter(f -> id.equals(f.get("id")))
                .findFirst()
                .orElse(new HashMap<>());
        
        // 更新字段
        if (request.containsKey("field_name")) {
            field.put("field_name", request.get("field_name"));
        }
        if (request.containsKey("field_name_en")) {
            field.put("field_name_en", request.get("field_name_en"));
        }
        if (request.containsKey("field_type")) {
            field.put("field_type", request.get("field_type"));
        }
        if (request.containsKey("field_group_id")) {
            field.put("field_group_id", request.get("field_group_id"));
        }
        if (request.containsKey("is_required")) {
            field.put("is_required", request.get("is_required"));
        }
        if (request.containsKey("description")) {
            field.put("description", request.get("description"));
        }
        if (request.containsKey("example_value")) {
            field.put("example_value", request.get("example_value"));
        }
        if (request.containsKey("validation_rules")) {
            field.put("validation_rules", request.get("validation_rules"));
        }
        if (request.containsKey("enum_options")) {
            field.put("enum_options", request.get("enum_options"));
        }
        if (request.containsKey("sort_order")) {
            field.put("sort_order", request.get("sort_order"));
        }
        if (request.containsKey("is_active")) {
            field.put("is_active", request.get("is_active"));
        }
        
        field.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(field);
    }
    
    /**
     * 删除自定义字段（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteCustomField(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "自定义字段删除成功");
        result.put("id", id);
        
        return ResponseData.success(result);
    }
}

