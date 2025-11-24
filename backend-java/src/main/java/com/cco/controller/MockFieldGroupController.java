package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 字段分组Mock控制器
 * 提供字段分组相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/field-groups")
public class MockFieldGroupController {

    /**
     * 获取字段分组列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getFieldGroups(
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        System.out.println("===============================================");
        System.out.println("[字段分组API] 接收参数:");
        System.out.println("  skip = " + skip);
        System.out.println("  limit = " + limit);
        System.out.println("===============================================");
        
        List<Map<String, Object>> groups = new ArrayList<>();
        
        // 创建Mock字段分组数据（树形结构）
        // 一级分组
        Map<String, Object> group1 = createGroup(1L, "customer_basic", "客户基础信息", "Customer Basic Information", null, 1);
        groups.add(group1);
        
        Map<String, Object> group2 = createGroup(2L, "loan_details", "贷款详情", "Loan Details", null, 2);
        groups.add(group2);
        
        Map<String, Object> group3 = createGroup(3L, "collection_info", "催收信息", "Collection Information", null, 3);
        groups.add(group3);
        
        // 二级分组（客户基础信息的子分组）
        Map<String, Object> group4 = createGroup(4L, "customer_contact", "联系方式", "Contact Information", 1L, 1);
        groups.add(group4);
        
        Map<String, Object> group5 = createGroup(5L, "customer_identity", "身份信息", "Identity Information", 1L, 2);
        groups.add(group5);
        
        // 二级分组（贷款详情的子分组）
        Map<String, Object> group6 = createGroup(6L, "loan_basic", "贷款基本信息", "Loan Basic Information", 2L, 1);
        groups.add(group6);
        
        Map<String, Object> group7 = createGroup(7L, "loan_repayment", "还款信息", "Repayment Information", 2L, 2);
        groups.add(group7);
        
        // 二级分组（催收信息的子分组）
        Map<String, Object> group8 = createGroup(8L, "collection_contact", "催收联系记录", "Collection Contact Records", 3L, 1);
        groups.add(group8);
        
        Map<String, Object> group9 = createGroup(9L, "collection_status", "催收状态", "Collection Status", 3L, 2);
        groups.add(group9);
        
        // 应用分页（虽然Mock数据不多，但保持接口一致性）
        int start = Math.min(skip, groups.size());
        int end = Math.min(skip + limit, groups.size());
        List<Map<String, Object>> pagedGroups = groups.subList(start, end);
        
        return ResponseData.success(pagedGroups);
    }
    
    /**
     * 创建字段分组对象
     */
    private Map<String, Object> createGroup(
            Long id, 
            String groupKey, 
            String groupName, 
            String groupNameEn, 
            Long parentId, 
            int sortOrder
    ) {
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("group_key", groupKey);
        group.put("group_name", groupName);
        group.put("group_name_en", groupNameEn);
        group.put("parent_id", parentId);
        group.put("sort_order", sortOrder);
        group.put("is_active", true);
        group.put("created_at", LocalDateTime.now().minusDays(30).toString());
        group.put("updated_at", LocalDateTime.now().toString());
        return group;
    }
    
    /**
     * 获取单个字段分组
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getFieldGroup(@PathVariable Long id) {
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("group_key", "customer_basic");
        group.put("group_name", "客户基础信息");
        group.put("group_name_en", "Customer Basic Information");
        group.put("parent_id", null);
        group.put("sort_order", 1);
        group.put("is_active", true);
        group.put("created_at", LocalDateTime.now().minusDays(30).toString());
        group.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(group);
    }
    
    /**
     * 创建字段分组
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createFieldGroup(@RequestBody Map<String, Object> request) {
        Map<String, Object> group = new HashMap<>();
        group.put("id", System.currentTimeMillis());
        group.put("group_key", request.get("group_key"));
        group.put("group_name", request.get("group_name"));
        group.put("group_name_en", request.get("group_name_en"));
        group.put("parent_id", request.get("parent_id"));
        group.put("sort_order", request.getOrDefault("sort_order", 0));
        group.put("is_active", request.getOrDefault("is_active", true));
        group.put("created_at", LocalDateTime.now().toString());
        group.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(group);
    }
    
    /**
     * 更新字段分组
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateFieldGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("group_key", request.getOrDefault("group_key", "customer_basic"));
        group.put("group_name", request.getOrDefault("group_name", "客户基础信息"));
        group.put("group_name_en", request.getOrDefault("group_name_en", "Customer Basic Information"));
        group.put("parent_id", request.get("parent_id"));
        group.put("sort_order", request.getOrDefault("sort_order", 1));
        group.put("is_active", request.getOrDefault("is_active", true));
        group.put("created_at", LocalDateTime.now().minusDays(30).toString());
        group.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(group);
    }
    
    /**
     * 删除字段分组
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteFieldGroup(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "字段分组删除成功");
        result.put("id", id);
        
        return ResponseData.success(result);
    }
    
    /**
     * 更新字段分组排序
     */
    @PutMapping("/sort")
    public ResponseData<Map<String, Object>> updateFieldGroupsSort(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "字段分组排序更新成功");
        
        return ResponseData.success(result);
    }
}




