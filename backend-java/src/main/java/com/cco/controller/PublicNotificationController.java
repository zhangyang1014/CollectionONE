package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 公共通知Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/public-notifications")
public class PublicNotificationController {

    /**
     * 获取公共通知列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getPublicNotifications(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Boolean is_enabled) {
        log.info("========== 获取公共通知列表，tenant_id={}, agency_id={}, is_enabled={} ==========", 
                tenant_id, agency_id, is_enabled);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> notification1 = new HashMap<>();
        notification1.put("id", 1L);
        notification1.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        notification1.put("agency_id", agency_id);
        notification1.put("title", "系统维护通知");
        notification1.put("content", "系统将于今晚22:00-24:00进行维护，期间可能无法正常使用，请提前做好准备。");
        notification1.put("h5_content", null);
        notification1.put("carousel_interval_seconds", 30);
        notification1.put("is_forced_read", false);
        notification1.put("is_enabled", true);
        notification1.put("repeat_interval_minutes", 60);
        notification1.put("max_remind_count", 3);
        notification1.put("notify_time_start", "09:00");
        notification1.put("notify_time_end", "18:00");
        notification1.put("effective_start_time", "2025-11-25T00:00:00");
        notification1.put("effective_end_time", "2025-12-25T23:59:59");
        notification1.put("notify_roles", Arrays.asList("Collector", "TeamAdmin"));
        notification1.put("sort_order", 1);
        notification1.put("created_at", "2025-11-25T00:00:00");
        notification1.put("updated_at", "2025-11-25T00:00:00");
        notification1.put("created_by", 1L);
        notifications.add(notification1);
        
        Map<String, Object> notification2 = new HashMap<>();
        notification2.put("id", 2L);
        notification2.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        notification2.put("agency_id", null);
        notification2.put("title", "重要提醒：请及时处理逾期案件");
        notification2.put("content", "请各位催员及时处理逾期案件，确保催收效率。");
        notification2.put("h5_content", "https://example.com/notice/2");
        notification2.put("carousel_interval_seconds", 20);
        notification2.put("is_forced_read", true);
        notification2.put("is_enabled", true);
        notification2.put("repeat_interval_minutes", null);
        notification2.put("max_remind_count", null);
        notification2.put("notify_time_start", null);
        notification2.put("notify_time_end", null);
        notification2.put("effective_start_time", "2025-11-25T00:00:00");
        notification2.put("effective_end_time", null);
        notification2.put("notify_roles", Arrays.asList("Collector"));
        notification2.put("sort_order", 2);
        notification2.put("created_at", "2025-11-25T00:00:00");
        notification2.put("updated_at", "2025-11-25T00:00:00");
        notification2.put("created_by", 1L);
        notifications.add(notification2);
        
        // 过滤逻辑
        if (tenant_id != null) {
            notifications.removeIf(n -> {
                Object nTenantId = n.get("tenant_id");
                return nTenantId != null && !tenant_id.equals(nTenantId);
            });
        }
        
        if (agency_id != null) {
            notifications.removeIf(n -> {
                Object nAgencyId = n.get("agency_id");
                return nAgencyId != null && !agency_id.equals(nAgencyId);
            });
        }
        
        if (is_enabled != null) {
            notifications.removeIf(n -> !is_enabled.equals(n.get("is_enabled")));
        }
        
        log.info("========== 返回公共通知列表，数量={} ==========", notifications.size());
        return ResponseData.success(notifications);
    }

    /**
     * 获取单个公共通知
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getPublicNotification(@PathVariable Long id) {
        log.info("========== 获取公共通知详情，id={} ==========", id);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("tenant_id", 1L);
        notification.put("agency_id", null);
        notification.put("title", "系统通知");
        notification.put("content", "这是一条系统通知");
        notification.put("h5_content", null);
        notification.put("carousel_interval_seconds", 30);
        notification.put("is_forced_read", false);
        notification.put("is_enabled", true);
        notification.put("repeat_interval_minutes", 60);
        notification.put("max_remind_count", 3);
        notification.put("notify_time_start", "09:00");
        notification.put("notify_time_end", "18:00");
        notification.put("effective_start_time", "2025-11-25T00:00:00");
        notification.put("effective_end_time", "2025-12-25T23:59:59");
        notification.put("notify_roles", Arrays.asList("Collector"));
        notification.put("sort_order", 1);
        notification.put("created_at", "2025-11-25T00:00:00");
        notification.put("updated_at", "2025-11-25T00:00:00");
        notification.put("created_by", 1L);
        
        return ResponseData.success(notification);
    }

    /**
     * 创建公共通知
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createPublicNotification(@RequestBody Map<String, Object> request) {
        log.info("========== 创建公共通知，request={} ==========", request);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis());
        notification.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        notification.put("agency_id", request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        notification.put("title", request.get("title"));
        notification.put("content", request.get("content"));
        notification.put("h5_content", request.get("h5_content") != null ? request.get("h5_content") : request.get("h5Content"));
        notification.put("carousel_interval_seconds", request.getOrDefault("carousel_interval_seconds", request.getOrDefault("carouselIntervalSeconds", 30)));
        notification.put("is_forced_read", request.getOrDefault("is_forced_read", request.getOrDefault("isForcedRead", false)));
        notification.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        notification.put("repeat_interval_minutes", request.get("repeat_interval_minutes") != null ? request.get("repeat_interval_minutes") : request.get("repeatIntervalMinutes"));
        notification.put("max_remind_count", request.get("max_remind_count") != null ? request.get("max_remind_count") : request.get("maxRemindCount"));
        notification.put("notify_time_start", request.get("notify_time_start") != null ? request.get("notify_time_start") : request.get("notifyTimeStart"));
        notification.put("notify_time_end", request.get("notify_time_end") != null ? request.get("notify_time_end") : request.get("notifyTimeEnd"));
        notification.put("effective_start_time", request.get("effective_start_time") != null ? request.get("effective_start_time") : request.get("effectiveStartTime"));
        notification.put("effective_end_time", request.get("effective_end_time") != null ? request.get("effective_end_time") : request.get("effectiveEndTime"));
        notification.put("notify_roles", request.get("notify_roles") != null ? request.get("notify_roles") : request.get("notifyRoles"));
        notification.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        notification.put("created_at", new Date().toString());
        notification.put("updated_at", new Date().toString());
        notification.put("created_by", 1L);
        
        return ResponseData.success(notification);
    }

    /**
     * 更新公共通知
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updatePublicNotification(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新公共通知，id={}, request={} ==========", id, request);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("tenant_id", 1L);
        notification.put("agency_id", request.get("agency_id") != null ? request.get("agency_id") : request.get("agencyId"));
        notification.put("title", request.getOrDefault("title", "系统通知"));
        notification.put("content", request.getOrDefault("content", "通知内容"));
        notification.put("h5_content", request.get("h5_content") != null ? request.get("h5_content") : request.get("h5Content"));
        notification.put("carousel_interval_seconds", request.getOrDefault("carousel_interval_seconds", request.getOrDefault("carouselIntervalSeconds", 30)));
        notification.put("is_forced_read", request.getOrDefault("is_forced_read", request.getOrDefault("isForcedRead", false)));
        notification.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        notification.put("repeat_interval_minutes", request.get("repeat_interval_minutes") != null ? request.get("repeat_interval_minutes") : request.get("repeatIntervalMinutes"));
        notification.put("max_remind_count", request.get("max_remind_count") != null ? request.get("max_remind_count") : request.get("maxRemindCount"));
        notification.put("notify_time_start", request.get("notify_time_start") != null ? request.get("notify_time_start") : request.get("notifyTimeStart"));
        notification.put("notify_time_end", request.get("notify_time_end") != null ? request.get("notify_time_end") : request.get("notifyTimeEnd"));
        notification.put("effective_start_time", request.get("effective_start_time") != null ? request.get("effective_start_time") : request.get("effectiveStartTime"));
        notification.put("effective_end_time", request.get("effective_end_time") != null ? request.get("effective_end_time") : request.get("effectiveEndTime"));
        notification.put("notify_roles", request.get("notify_roles") != null ? request.get("notify_roles") : request.get("notifyRoles"));
        notification.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        notification.put("updated_at", new Date().toString());
        
        return ResponseData.success(notification);
    }

    /**
     * 删除公共通知
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deletePublicNotification(@PathVariable Long id) {
        log.info("========== 删除公共通知，id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 更新公共通知排序
     */
    @PutMapping("/{id}/sort")
    public ResponseData<String> updatePublicNotificationSort(
            @PathVariable Long id,
            @RequestParam Integer sort_order) {
        log.info("========== 更新公共通知排序，id={}, sort_order={} ==========", id, sort_order);
        return ResponseData.success("排序更新成功");
    }
}































