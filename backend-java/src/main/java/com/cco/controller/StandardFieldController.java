package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准字段管理Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/standard-fields")
public class StandardFieldController {

    // 使用Mock数据模式，不依赖Service

    /**
     * 获取标准字段列表
     * 支持按分组ID过滤
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getStandardFields(
            @RequestParam(required = false) Long field_group_id
    ) {
        log.info("========== 获取标准字段列表，field_group_id={} ==========", field_group_id);
        
        // Mock数据模式 - 不依赖Service
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Mock数据 - 基本信息分组字段
        if (field_group_id == null || field_group_id == 1) {
            Map<String, Object> field1 = new HashMap<>();
            field1.put("id", 1L);
            field1.put("field_key", "case_number");  // 使用下划线格式，匹配前端期望
            field1.put("field_name", "案件编号");
            field1.put("field_name_en", "Case Number");
            field1.put("field_type", "text");
            field1.put("field_group_id", 1L);
            field1.put("is_required", true);
            field1.put("is_extended", false);
            field1.put("description", "案件唯一编号");
            field1.put("example_value", "CASE001");
            field1.put("sort_order", 1);
            field1.put("is_active", true);
            result.add(field1);
            
            Map<String, Object> field2 = new HashMap<>();
            field2.put("id", 2L);
            field2.put("field_key", "debtor_name");
            field2.put("field_name", "债务人姓名");
            field2.put("field_name_en", "Debtor Name");
            field2.put("field_type", "text");
            field2.put("field_group_id", 1L);
            field2.put("is_required", true);
            field2.put("is_extended", false);
            field2.put("description", "债务人姓名");
            field2.put("example_value", "张三");
            field2.put("sort_order", 2);
            field2.put("is_active", true);
            result.add(field2);
        }
        
        // Mock数据 - 联系信息分组字段
        if (field_group_id == null || field_group_id == 2) {
            Map<String, Object> field3 = new HashMap<>();
            field3.put("id", 3L);
            field3.put("field_key", "mobile");
            field3.put("field_name", "手机号");
            field3.put("field_name_en", "Mobile");
            field3.put("field_type", "text");
            field3.put("field_group_id", 2L);
            field3.put("is_required", false);
            field3.put("is_extended", false);
            field3.put("description", "债务人手机号");
            field3.put("example_value", "13800138000");
            field3.put("sort_order", 1);
            field3.put("is_active", true);
            result.add(field3);
            
            Map<String, Object> field4 = new HashMap<>();
            field4.put("id", 4L);
            field4.put("field_key", "email");
            field4.put("field_name", "邮箱");
            field4.put("field_name_en", "Email");
            field4.put("field_type", "text");
            field4.put("field_group_id", 2L);
            field4.put("is_required", false);
            field4.put("is_extended", false);
            field4.put("description", "债务人邮箱");
            field4.put("example_value", "test@example.com");
            field4.put("sort_order", 2);
            field4.put("is_active", true);
            result.add(field4);
        }
        
        // Mock数据 - 财务信息分组字段
        if (field_group_id == null || field_group_id == 3) {
            Map<String, Object> field5 = new HashMap<>();
            field5.put("id", 5L);
            field5.put("field_key", "loan_amount");
            field5.put("field_name", "贷款金额");
            field5.put("field_name_en", "Loan Amount");
            field5.put("field_type", "number");
            field5.put("field_group_id", 3L);
            field5.put("is_required", true);
            field5.put("is_extended", false);
            field5.put("description", "贷款总金额");
            field5.put("example_value", "10000.00");
            field5.put("sort_order", 1);
            field5.put("is_active", true);
            result.add(field5);
            
            Map<String, Object> field6 = new HashMap<>();
            field6.put("id", 6L);
            field6.put("field_key", "outstanding_amount");
            field6.put("field_name", "未还金额");
            field6.put("field_name_en", "Outstanding Amount");
            field6.put("field_type", "number");
            field6.put("field_group_id", 3L);
            field6.put("is_required", true);
            field6.put("is_extended", false);
            field6.put("description", "未还金额");
            field6.put("example_value", "5000.00");
            field6.put("sort_order", 2);
            field6.put("is_active", true);
            result.add(field6);
        }
        
        log.info("========== 返回标准字段列表，数量={} ==========", result.size());
        return ResponseData.success(result);
    }

    /**
     * 获取标准字段详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getStandardField(@PathVariable Long id) {
        log.info("========== 获取标准字段详情，id={} ==========", id);
        
        // Mock数据
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("field_key", "field_" + id);
        field.put("field_name", "标准字段" + id);
        field.put("field_name_en", "Standard Field " + id);
        field.put("field_type", "text");
        field.put("field_group_id", 1L);
        field.put("is_required", false);
        field.put("is_extended", false);
        field.put("description", "标准字段描述");
        field.put("example_value", "示例值");
        field.put("sort_order", id.intValue());
        field.put("is_active", true);
        
        return ResponseData.success(field);
    }

    /**
     * 创建标准字段
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createStandardField(@RequestBody Map<String, Object> request) {
        log.info("========== 创建标准字段，request={} ==========", request);
        
        // Mock数据模式
        Map<String, Object> field = new HashMap<>();
        field.put("id", System.currentTimeMillis());
        field.put("field_key", request.get("field_key"));
        field.put("field_name", request.get("field_name"));
        field.put("field_name_en", request.get("field_name_en"));
        field.put("field_type", request.get("field_type"));
        field.put("field_group_id", request.get("field_group_id"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("is_extended", request.getOrDefault("is_extended", false));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value"));
        field.put("sort_order", request.getOrDefault("sort_order", 0));
        field.put("is_active", true);
        
        return ResponseData.success(field);
    }

    /**
     * 更新标准字段
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateStandardField(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        log.info("========== 更新标准字段，id={}, request={} ==========", id, request);
        
        // Mock数据模式
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("field_key", request.getOrDefault("field_key", "field_" + id));
        field.put("field_name", request.getOrDefault("field_name", "标准字段" + id));
        field.put("field_name_en", request.getOrDefault("field_name_en", "Standard Field " + id));
        field.put("field_type", request.getOrDefault("field_type", "text"));
        field.put("field_group_id", request.get("field_group_id"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("is_extended", request.getOrDefault("is_extended", false));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value"));
        field.put("sort_order", request.getOrDefault("sort_order", id.intValue()));
        field.put("is_active", request.getOrDefault("is_active", true));
        
        return ResponseData.success(field);
    }

    /**
     * 删除标准字段（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteStandardField(@PathVariable Long id) {
        log.info("========== 删除标准字段，id={} ==========", id);
        // Mock数据模式 - 直接返回成功
        return ResponseData.success("删除成功");
    }

    /**
     * 更新字段排序
     */
    @PutMapping("/sort")
    public ResponseData<String> updateFieldSort(@RequestBody Map<String, Object> request) {
        log.info("========== 更新字段排序，request={} ==========", request);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) request.get("fields");
        
        if (fields != null) {
            log.info("更新{}个字段的排序", fields.size());
        }
        
        // Mock数据模式 - 直接返回成功
        return ResponseData.success("排序更新成功");
    }

}




