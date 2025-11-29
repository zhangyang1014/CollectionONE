package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 自定义字段Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/custom-fields")
public class CustomFieldController {

    /**
     * 获取自定义字段列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getCustomFields(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long fieldGroupId,
            @RequestParam(required = false) Boolean isActive) {
        log.info("========== 获取自定义字段列表，tenantId={}, fieldGroupId={}, isActive={} ==========", 
                tenantId, fieldGroupId, isActive);
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> field1 = new HashMap<>();
        field1.put("id", 1L);
        field1.put("tenant_id", tenantId != null ? tenantId : 1L);
        field1.put("field_key", "custom_field_1");
        field1.put("field_name", "自定义字段1");
        field1.put("field_name_en", "Custom Field 1");
        field1.put("field_type", "text");
        field1.put("field_group_id", fieldGroupId != null ? fieldGroupId : 1L);
        field1.put("is_required", false);
        field1.put("is_extended", false);
        field1.put("description", "这是一个自定义字段");
        field1.put("example_value", "示例值");
        field1.put("sort_order", 1);
        field1.put("is_active", true);
        field1.put("created_at", "2025-01-01T00:00:00");
        field1.put("updated_at", "2025-11-25T00:00:00");
        fields.add(field1);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("id", 2L);
        field2.put("tenant_id", tenantId != null ? tenantId : 1L);
        field2.put("field_key", "custom_field_2");
        field2.put("field_name", "自定义字段2");
        field2.put("field_name_en", "Custom Field 2");
        field2.put("field_type", "number");
        field2.put("field_group_id", fieldGroupId != null ? fieldGroupId : 2L);
        field2.put("is_required", true);
        field2.put("is_extended", false);
        field2.put("description", "这是另一个自定义字段");
        field2.put("example_value", "123");
        field2.put("sort_order", 2);
        field2.put("is_active", true);
        field2.put("created_at", "2025-01-02T00:00:00");
        field2.put("updated_at", "2025-11-25T00:00:00");
        fields.add(field2);
        
        // 过滤逻辑
        if (tenantId != null) {
            fields.removeIf(f -> !tenantId.equals(f.get("tenant_id")));
        }
        
        if (fieldGroupId != null) {
            fields.removeIf(f -> !fieldGroupId.equals(f.get("field_group_id")));
        }
        
        if (isActive != null) {
            fields.removeIf(f -> !isActive.equals(f.get("is_active")));
        }
        
        log.info("========== 返回自定义字段列表，数量={} ==========", fields.size());
        return ResponseData.success(fields);
    }

    /**
     * 获取自定义字段详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getCustomField(@PathVariable Long id) {
        log.info("========== 获取自定义字段详情，id={} ==========", id);
        
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("tenant_id", 1L);
        field.put("field_key", "custom_field_" + id);
        field.put("field_name", "自定义字段" + id);
        field.put("field_name_en", "Custom Field " + id);
        field.put("field_type", "text");
        field.put("field_group_id", 1L);
        field.put("is_required", false);
        field.put("is_extended", false);
        field.put("description", "自定义字段描述");
        field.put("example_value", "示例值");
        field.put("sort_order", id.intValue());
        field.put("is_active", true);
        field.put("created_at", "2025-01-01T00:00:00");
        field.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(field);
    }

    /**
     * 创建自定义字段
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createCustomField(@RequestBody Map<String, Object> request) {
        log.info("========== 创建自定义字段，request={} ==========", request);
        
        Map<String, Object> field = new HashMap<>();
        field.put("id", System.currentTimeMillis());
        field.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        field.put("field_key", request.get("field_key") != null ? request.get("field_key") : request.get("fieldKey"));
        field.put("field_name", request.get("field_name") != null ? request.get("field_name") : request.get("fieldName"));
        field.put("field_name_en", request.get("field_name_en") != null ? request.get("field_name_en") : request.get("fieldNameEn"));
        field.put("field_type", request.get("field_type") != null ? request.get("field_type") : request.get("fieldType"));
        field.put("field_group_id", request.get("field_group_id") != null ? request.get("field_group_id") : request.get("fieldGroupId"));
        field.put("is_required", request.getOrDefault("is_required", request.getOrDefault("isRequired", false)));
        field.put("is_extended", request.getOrDefault("is_extended", request.getOrDefault("isExtended", false)));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value") != null ? request.get("example_value") : request.get("exampleValue"));
        field.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        field.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        field.put("created_at", new Date().toString());
        field.put("updated_at", new Date().toString());
        
        return ResponseData.success(field);
    }

    /**
     * 更新自定义字段
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateCustomField(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新自定义字段，id={}, request={} ==========", id, request);
        
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.getOrDefault("tenantId", 1L));
        field.put("field_key", request.get("field_key") != null ? request.get("field_key") : request.getOrDefault("fieldKey", "custom_field_" + id));
        field.put("field_name", request.get("field_name") != null ? request.get("field_name") : request.getOrDefault("fieldName", "自定义字段" + id));
        field.put("field_name_en", request.get("field_name_en") != null ? request.get("field_name_en") : request.getOrDefault("fieldNameEn", "Custom Field " + id));
        field.put("field_type", request.get("field_type") != null ? request.get("field_type") : request.getOrDefault("fieldType", "text"));
        field.put("field_group_id", request.get("field_group_id") != null ? request.get("field_group_id") : request.get("fieldGroupId"));
        field.put("is_required", request.getOrDefault("is_required", request.getOrDefault("isRequired", false)));
        field.put("is_extended", request.getOrDefault("is_extended", request.getOrDefault("isExtended", false)));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value") != null ? request.get("example_value") : request.get("exampleValue"));
        field.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", id.intValue())));
        field.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        field.put("updated_at", new Date().toString());
        
        return ResponseData.success(field);
    }

    /**
     * 删除自定义字段
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteCustomField(@PathVariable Long id) {
        log.info("========== 删除自定义字段，id={} ==========", id);
        return ResponseData.success("删除成功");
    }
}

