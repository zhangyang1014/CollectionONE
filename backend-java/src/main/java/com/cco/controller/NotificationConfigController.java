package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 通知配置Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/notification-configs")
public class NotificationConfigController {

    /**
     * 获取通知配置列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getNotificationConfigs(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) String notification_type) {
        log.info("========== 获取通知配置列表，tenant_id={}, notification_type={} ==========", 
                tenant_id, notification_type);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // Mock数据 - unreplied配置
        Map<String, Object> config1 = new HashMap<>();
        config1.put("id", 1L);
        config1.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        config1.put("notification_type", "unreplied");
        config1.put("is_enabled", true);
        Map<String, Object> configData1 = new HashMap<>();
        configData1.put("notify_roles", Arrays.asList("Collector", "TeamAdmin"));
        configData1.put("notify_channels", Arrays.asList("app", "sms"));
        configData1.put("priority", "medium");
        configData1.put("trigger_delay_minutes", 30);
        configData1.put("monitored_channels", Arrays.asList("im", "call"));
        configData1.put("repeat_interval_minutes", 60);
        configData1.put("max_notify_count", 3);
        Map<String, Object> notifyTimeRange1 = new HashMap<>();
        notifyTimeRange1.put("type", "working_hours");
        configData1.put("notify_time_range", notifyTimeRange1);
        config1.put("config_data", configData1);
        config1.put("created_at", "2025-01-01T00:00:00");
        config1.put("updated_at", "2025-11-25T00:00:00");
        configs.add(config1);
        
        // Mock数据 - nudge配置
        Map<String, Object> config2 = new HashMap<>();
        config2.put("id", 2L);
        config2.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        config2.put("notification_type", "nudge");
        config2.put("is_enabled", true);
        Map<String, Object> configData2 = new HashMap<>();
        configData2.put("notify_roles", Arrays.asList("Collector"));
        Map<String, Object> ptpConfig = new HashMap<>();
        ptpConfig.put("advance_notify_minutes", 60);
        ptpConfig.put("repeat_interval_minutes", 120);
        ptpConfig.put("max_notify_count", 5);
        ptpConfig.put("notify_roles", Arrays.asList("Collector"));
        configData2.put("ptp", ptpConfig);
        Map<String, Object> followUpConfig = new HashMap<>();
        followUpConfig.put("advance_notify_minutes", 30);
        followUpConfig.put("repeat_interval_minutes", 60);
        followUpConfig.put("max_notify_count", 3);
        followUpConfig.put("notify_roles", Arrays.asList("Collector"));
        configData2.put("follow_up", followUpConfig);
        config2.put("config_data", configData2);
        config2.put("created_at", "2025-01-01T00:00:00");
        config2.put("updated_at", "2025-11-25T00:00:00");
        configs.add(config2);
        
        // Mock数据 - case_update配置
        Map<String, Object> config3 = new HashMap<>();
        config3.put("id", 3L);
        config3.put("tenant_id", tenant_id != null ? tenant_id : 1L);
        config3.put("notification_type", "case_update");
        config3.put("is_enabled", true);
        Map<String, Object> configData3 = new HashMap<>();
        Map<String, Object> caseAssigned = new HashMap<>();
        caseAssigned.put("enabled", true);
        caseAssigned.put("notify_roles", Arrays.asList("Collector"));
        caseAssigned.put("template", "案件 {case_id} 已分配给您");
        configData3.put("case_assigned", caseAssigned);
        Map<String, Object> paymentReceived = new HashMap<>();
        paymentReceived.put("enabled", true);
        paymentReceived.put("amount_threshold", 1000);
        paymentReceived.put("notify_roles", Arrays.asList("Collector", "TeamAdmin"));
        paymentReceived.put("template", "案件 {case_id} 收到还款 {amount} 元");
        configData3.put("payment_received", paymentReceived);
        Map<String, Object> tagUpdated = new HashMap<>();
        tagUpdated.put("enabled", true);
        tagUpdated.put("notify_roles", Arrays.asList("Collector"));
        tagUpdated.put("template", "案件 {case_id} 标签已更新为 {tag_name}");
        configData3.put("tag_updated", tagUpdated);
        config3.put("config_data", configData3);
        config3.put("created_at", "2025-01-01T00:00:00");
        config3.put("updated_at", "2025-11-25T00:00:00");
        configs.add(config3);
        
        // 过滤逻辑
        if (tenant_id != null) {
            configs.removeIf(c -> !tenant_id.equals(c.get("tenant_id")));
        }
        
        if (notification_type != null) {
            configs.removeIf(c -> !notification_type.equals(c.get("notification_type")));
        }
        
        log.info("========== 返回通知配置列表，数量={} ==========", configs.size());
        return ResponseData.success(configs);
    }

    /**
     * 获取单个通知配置
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getNotificationConfig(@PathVariable Long id) {
        log.info("========== 获取通知配置详情，id={} ==========", id);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", id);
        config.put("tenant_id", 1L);
        config.put("notification_type", "unreplied");
        config.put("is_enabled", true);
        Map<String, Object> configData = new HashMap<>();
        configData.put("notify_roles", Arrays.asList("Collector"));
        configData.put("notify_channels", Arrays.asList("app"));
        configData.put("priority", "medium");
        config.put("config_data", configData);
        config.put("created_at", "2025-01-01T00:00:00");
        config.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(config);
    }

    /**
     * 创建通知配置
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createNotificationConfig(@RequestBody Map<String, Object> request) {
        log.info("========== 创建通知配置，request={} ==========", request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", System.currentTimeMillis());
        config.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        config.put("notification_type", request.get("notification_type") != null ? request.get("notification_type") : request.get("notificationType"));
        config.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        config.put("config_data", request.get("config_data") != null ? request.get("config_data") : request.get("configData"));
        config.put("created_at", new Date().toString());
        config.put("updated_at", new Date().toString());
        
        return ResponseData.success(config);
    }

    /**
     * 更新通知配置
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateNotificationConfig(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新通知配置，id={}, request={} ==========", id, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", id);
        config.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : 1L);
        config.put("notification_type", "unreplied");
        config.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        config.put("config_data", request.get("config_data") != null ? request.get("config_data") : request.get("configData"));
        config.put("updated_at", new Date().toString());
        
        return ResponseData.success(config);
    }

    /**
     * 删除通知配置
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteNotificationConfig(@PathVariable Long id) {
        log.info("========== 删除通知配置，id={} ==========", id);
        return ResponseData.success("删除成功");
    }
}

































