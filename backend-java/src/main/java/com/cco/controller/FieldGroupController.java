package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 字段分组Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/field-groups")
public class FieldGroupController {

    /**
     * 获取字段分组列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getFieldGroups(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long parentId) {
        log.info("========== 获取字段分组列表，isActive={}, parentId={} ==========", isActive, parentId);
        
        List<Map<String, Object>> groups = new ArrayList<>();
        
        // Mock数据 - 一级分组（根据历史SQL文件恢复）
        Map<String, Object> group1 = new HashMap<>();
        group1.put("id", 1L);
        group1.put("group_key", "customer_basic");
        group1.put("group_name", "客户基础信息");
        group1.put("group_name_en", "Customer Basic Information");
        // parent_id不设置，表示一级分组
        group1.put("sort_order", 1);
        group1.put("is_active", true);
        group1.put("created_at", "2025-01-01T00:00:00");
        group1.put("updated_at", "2025-11-25T00:00:00");
        groups.add(group1);
        
        Map<String, Object> group2 = new HashMap<>();
        group2.put("id", 2L);
        group2.put("group_key", "loan_details");
        group2.put("group_name", "贷款详情");
        group2.put("group_name_en", "Loan Details");
        // parent_id不设置，表示一级分组
        group2.put("sort_order", 2);
        group2.put("is_active", true);
        group2.put("created_at", "2025-01-01T00:00:00");
        group2.put("updated_at", "2025-11-25T00:00:00");
        groups.add(group2);
        
        Map<String, Object> group3 = new HashMap<>();
        group3.put("id", 3L);
        group3.put("group_key", "borrowing_records");
        group3.put("group_name", "借款记录");
        group3.put("group_name_en", "Borrowing Records");
        // parent_id不设置，表示一级分组
        group3.put("sort_order", 3);
        group3.put("is_active", true);
        group3.put("created_at", "2025-01-01T00:00:00");
        group3.put("updated_at", "2025-11-25T00:00:00");
        groups.add(group3);
        
        Map<String, Object> group4 = new HashMap<>();
        group4.put("id", 4L);
        group4.put("group_key", "repayment_records");
        group4.put("group_name", "还款记录");
        group4.put("group_name_en", "Repayment Records");
        // parent_id不设置，表示一级分组
        group4.put("sort_order", 4);
        group4.put("is_active", true);
        group4.put("created_at", "2025-01-01T00:00:00");
        group4.put("updated_at", "2025-11-25T00:00:00");
        groups.add(group4);
        
        Map<String, Object> group5 = new HashMap<>();
        group5.put("id", 5L);
        group5.put("group_key", "installment_details");
        group5.put("group_name", "分期详情");
        group5.put("group_name_en", "Installment Details");
        // parent_id不设置，表示一级分组
        group5.put("sort_order", 5);
        group5.put("is_active", true);
        group5.put("created_at", "2025-01-01T00:00:00");
        group5.put("updated_at", "2025-11-25T00:00:00");
        groups.add(group5);
        
        // Mock数据 - 二级分组（客户基础信息的子分组，根据历史SQL文件恢复）
        Map<String, Object> subGroup1 = new HashMap<>();
        subGroup1.put("id", 11L);
        subGroup1.put("group_key", "identity_info");
        subGroup1.put("group_name", "基础身份信息");
        subGroup1.put("group_name_en", "Identity Information");
        subGroup1.put("parent_id", 1L); // 属于客户基础信息（id=1）
        subGroup1.put("sort_order", 1);
        subGroup1.put("is_active", true);
        subGroup1.put("created_at", "2025-01-01T00:00:00");
        subGroup1.put("updated_at", "2025-11-25T00:00:00");
        groups.add(subGroup1);
        
        Map<String, Object> subGroup2 = new HashMap<>();
        subGroup2.put("id", 12L);
        subGroup2.put("group_key", "education");
        subGroup2.put("group_name", "教育信息");
        subGroup2.put("group_name_en", "Education");
        subGroup2.put("parent_id", 1L); // 属于客户基础信息（id=1）
        subGroup2.put("sort_order", 2);
        subGroup2.put("is_active", true);
        subGroup2.put("created_at", "2025-01-01T00:00:00");
        subGroup2.put("updated_at", "2025-11-25T00:00:00");
        groups.add(subGroup2);
        
        Map<String, Object> subGroup3 = new HashMap<>();
        subGroup3.put("id", 13L);
        subGroup3.put("group_key", "employment");
        subGroup3.put("group_name", "职业信息");
        subGroup3.put("group_name_en", "Employment");
        subGroup3.put("parent_id", 1L); // 属于客户基础信息（id=1）
        subGroup3.put("sort_order", 3);
        subGroup3.put("is_active", true);
        subGroup3.put("created_at", "2025-01-01T00:00:00");
        subGroup3.put("updated_at", "2025-11-25T00:00:00");
        groups.add(subGroup3);
        
        Map<String, Object> subGroup4 = new HashMap<>();
        subGroup4.put("id", 14L);
        subGroup4.put("group_key", "user_behavior");
        subGroup4.put("group_name", "用户行为与信用");
        subGroup4.put("group_name_en", "User Behavior & Credit");
        subGroup4.put("parent_id", 1L); // 属于客户基础信息（id=1）
        subGroup4.put("sort_order", 4);
        subGroup4.put("is_active", true);
        subGroup4.put("created_at", "2025-01-01T00:00:00");
        subGroup4.put("updated_at", "2025-11-25T00:00:00");
        groups.add(subGroup4);
        
        Map<String, Object> subGroup5 = new HashMap<>();
        subGroup5.put("id", 15L);
        subGroup5.put("group_key", "contact_info");
        subGroup5.put("group_name", "联系方式");
        subGroup5.put("group_name_en", "Contact Information");
        subGroup5.put("parent_id", 1L); // 属于客户基础信息（id=1）
        subGroup5.put("sort_order", 5);
        subGroup5.put("is_active", true);
        subGroup5.put("created_at", "2025-01-01T00:00:00");
        subGroup5.put("updated_at", "2025-11-25T00:00:00");
        groups.add(subGroup5);
        
        // 过滤逻辑
        if (isActive != null) {
            groups.removeIf(g -> !isActive.equals(g.get("is_active")));
        }
        
        // 只有当明确指定parentId时，才进行过滤
        // 如果没有指定parentId，返回所有分组（包括一级和二级）
        if (parentId != null) {
            groups.removeIf(g -> {
                Object gParentId = g.get("parent_id");
                // 检查parent_id是否存在且等于指定的parentId
                return gParentId == null || !parentId.equals(gParentId);
            });
        }
        // 如果parentId为null，不进行过滤，返回所有分组
        
        log.info("========== 返回字段分组列表，数量={} ==========", groups.size());
        return ResponseData.success(groups);
    }

    /**
     * 获取字段分组详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getFieldGroup(@PathVariable Long id) {
        log.info("========== 获取字段分组详情，id={} ==========", id);
        
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("group_key", "group_" + id);
        group.put("group_name", "字段分组" + id);
        group.put("group_name_en", "Field Group " + id);
        // parent_id不设置，表示一级分组
        group.put("sort_order", id.intValue());
        group.put("is_active", true);
        group.put("created_at", "2025-01-01T00:00:00");
        group.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(group);
    }

    /**
     * 创建字段分组
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createFieldGroup(@RequestBody Map<String, Object> request) {
        log.info("========== 创建字段分组，request={} ==========", request);
        
        Map<String, Object> group = new HashMap<>();
        group.put("id", System.currentTimeMillis());
        group.put("group_key", request.get("group_key") != null ? request.get("group_key") : request.get("groupKey"));
        group.put("group_name", request.get("group_name") != null ? request.get("group_name") : request.get("groupName"));
        group.put("group_name_en", request.get("group_name_en") != null ? request.get("group_name_en") : request.get("groupNameEn"));
        group.put("parent_id", request.get("parent_id") != null ? request.get("parent_id") : request.get("parentId"));
        group.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        group.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        group.put("created_at", new Date().toString());
        group.put("updated_at", new Date().toString());
        
        return ResponseData.success(group);
    }

    /**
     * 更新字段分组
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateFieldGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新字段分组，id={}, request={} ==========", id, request);
        
        Map<String, Object> group = new HashMap<>();
        group.put("id", id);
        group.put("group_key", request.get("group_key") != null ? request.get("group_key") : request.getOrDefault("groupKey", "group_" + id));
        group.put("group_name", request.get("group_name") != null ? request.get("group_name") : request.getOrDefault("groupName", "字段分组" + id));
        group.put("group_name_en", request.get("group_name_en") != null ? request.get("group_name_en") : request.getOrDefault("groupNameEn", "Field Group " + id));
        group.put("parent_id", request.get("parent_id") != null ? request.get("parent_id") : request.get("parentId"));
        group.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", id.intValue())));
        group.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        group.put("updated_at", new Date().toString());
        
        return ResponseData.success(group);
    }

    /**
     * 删除字段分组
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteFieldGroup(@PathVariable Long id) {
        log.info("========== 删除字段分组，id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 更新字段分组排序
     */
    @PutMapping("/sort")
    public ResponseData<String> updateFieldGroupsSort(@RequestBody Map<String, Object> request) {
        log.info("========== 更新字段分组排序，request={} ==========", request);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> groups = (List<Map<String, Object>>) request.get("groups");
        if (groups != null) {
            log.info("更新{}个字段分组的排序", groups.size());
        }
        
        return ResponseData.success("排序更新成功");
    }
}

