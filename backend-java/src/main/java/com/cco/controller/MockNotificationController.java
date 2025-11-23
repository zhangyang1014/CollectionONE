package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Mock 通知管理控制器
 * 提供通知模板、通知配置、公共通知等API的Mock实现
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX)
public class MockNotificationController {

    /**
     * 获取通知模板列表
     */
    @GetMapping("/notification-templates")
    public ResponseData<List<Map<String, Object>>> getNotificationTemplates(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) String template_type,
            @RequestParam(required = false) Boolean is_enabled
    ) {
        log.info("获取通知模板列表（Mock），tenant_id={}, template_type={}, is_enabled={}", 
                tenant_id, template_type, is_enabled);
        
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // Mock数据：案件标签变更通知模板
        templates.add(createNotificationTemplate(
            1L, tenant_id, "CASE_TAG_CHANGE_001", "案件标签变更通知",
            "case_tag_change", "案件 {case_id} 的标签已变更为 {tag_name}",
            "/cases/{case_id}", "agency",
            Arrays.asList(1L), Arrays.asList(1L), Arrays.asList(1L),
            false, 30, 3, "09:00", "18:00", "medium", 5, true
        ));
        
        // Mock数据：案件还款通知模板
        templates.add(createNotificationTemplate(
            2L, tenant_id, "CASE_PAYMENT_001", "案件还款通知",
            "case_payment", "案件 {case_id} 收到还款 {amount}",
            "/cases/{case_id}", "team",
            Arrays.asList(1L), Arrays.asList(1L), null,
            true, null, null, "00:00", "23:59", "high", 10, true
        ));
        
        // Mock数据：用户APP访问通知模板
        templates.add(createNotificationTemplate(
            3L, tenant_id, "USER_APP_VISIT_001", "用户APP访问通知",
            "user_app_visit", "用户 {user_id} 访问了APP",
            "/users/{user_id}", "collector",
            null, null, Arrays.asList(1L, 2L),
            false, 60, 5, "08:00", "22:00", "low", 3, true
        ));
        
        return ResponseData.success(templates);
    }

    /**
     * 获取模板类型列表
     */
    @GetMapping("/notification-templates/types/list")
    public ResponseData<Map<String, Object>> getTemplateTypes() {
        log.info("获取模板类型列表（Mock）");
        
        List<Map<String, Object>> types = new ArrayList<>();
        
        types.add(createTemplateType("case_tag_change", "案件标签变更", 
                "当案件标签发生变化时触发"));
        types.add(createTemplateType("case_payment", "案件还款", 
                "当案件收到还款时触发"));
        types.add(createTemplateType("user_app_visit", "用户APP访问", 
                "当用户访问APP时触发"));
        types.add(createTemplateType("user_payment_page_visit", "用户还款页访问", 
                "当用户访问还款页面时触发"));
        types.add(createTemplateType("case_assignment", "案件分配", 
                "当案件被分配给催员时触发"));
        types.add(createTemplateType("case_status_change", "案件状态变更", 
                "当案件状态发生变化时触发"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("types", types);
        
        return ResponseData.success(result);
    }

    /**
     * 获取通知配置列表
     */
    @GetMapping("/notification-configs")
    public ResponseData<List<Map<String, Object>>> getNotificationConfigs(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) String notification_type
    ) {
        log.info("获取通知配置列表（Mock），tenant_id={}, notification_type={}", 
                tenant_id, notification_type);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // Mock数据：未回复通知配置
        configs.add(createNotificationConfig(
            1L, tenant_id, "unreplied",
            Map.of(
                "enabled", true,
                "timeout_minutes", 30,
                "remind_interval_minutes", 15,
                "max_remind_count", 3
            )
        ));
        
        // Mock数据：催收提醒通知配置
        configs.add(createNotificationConfig(
            2L, tenant_id, "nudge",
            Map.of(
                "enabled", true,
                "nudge_interval_hours", 24,
                "max_nudge_count", 5
            )
        ));
        
        // Mock数据：案件更新通知配置
        configs.add(createNotificationConfig(
            3L, tenant_id, "case_update",
            Map.of(
                "enabled", true,
                "notify_on_status_change", true,
                "notify_on_assignment", true,
                "notify_on_payment", true
            )
        ));
        
        // Mock数据：业绩通知配置
        configs.add(createNotificationConfig(
            4L, tenant_id, "performance",
            Map.of(
                "enabled", true,
                "daily_report_time", "09:00",
                "weekly_report_day", "monday",
                "monthly_report_day", 1
            )
        ));
        
        // Mock数据：超时通知配置
        configs.add(createNotificationConfig(
            5L, tenant_id, "timeout",
            Map.of(
                "enabled", true,
                "timeout_minutes", 60,
                "notify_before_minutes", 10
            )
        ));
        
        return ResponseData.success(configs);
    }

    /**
     * 获取公共通知列表
     */
    @GetMapping("/public-notifications")
    public ResponseData<List<Map<String, Object>>> getPublicNotifications(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) Long agency_id,
            @RequestParam(required = false) Boolean is_enabled
    ) {
        log.info("获取公共通知列表（Mock），tenant_id={}, agency_id={}, is_enabled={}", 
                tenant_id, agency_id, is_enabled);
        
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // Mock数据：系统维护通知
        notifications.add(createPublicNotification(
            1L, tenant_id, 1L, "系统维护通知",
            "系统将于今晚22:00-24:00进行维护，期间可能无法正常使用",
            "info", "2025-01-15 10:00:00", "2025-01-20 23:59:59",
            1, true
        ));
        
        // Mock数据：重要公告
        notifications.add(createPublicNotification(
            2L, tenant_id, 1L, "重要公告",
            "请各位催员注意，新的催收流程已上线，请及时查看",
            "warning", "2025-01-10 09:00:00", "2025-01-25 23:59:59",
            2, true
        ));
        
        // Mock数据：功能更新
        notifications.add(createPublicNotification(
            3L, tenant_id, null, "功能更新",
            "案件列表新增筛选功能，支持多条件组合筛选",
            "success", "2025-01-12 14:00:00", "2025-01-30 23:59:59",
            3, true
        ));
        
        return ResponseData.success(notifications);
    }

    // Helper methods
    private Map<String, Object> createNotificationTemplate(
            Long id, Long tenantId, String templateId, String templateName,
            String templateType, String contentTemplate, String jumpUrlTemplate,
            String targetType, List<Long> targetAgencies, List<Long> targetTeams,
            List<Long> targetCollectors, Boolean isForcedRead, Integer repeatIntervalMinutes,
            Integer maxRemindCount, String notifyTimeStart, String notifyTimeEnd,
            String priority, Integer displayDurationSeconds, Boolean isEnabled
    ) {
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("tenant_id", tenantId);
        template.put("template_id", templateId);
        template.put("template_name", templateName);
        template.put("template_type", templateType);
        template.put("description", "Mock通知模板：" + templateName);
        template.put("content_template", contentTemplate);
        template.put("jump_url_template", jumpUrlTemplate);
        template.put("target_type", targetType);
        template.put("target_agencies", targetAgencies);
        template.put("target_teams", targetTeams);
        template.put("target_collectors", targetCollectors);
        template.put("is_forced_read", isForcedRead);
        template.put("repeat_interval_minutes", repeatIntervalMinutes);
        template.put("max_remind_count", maxRemindCount);
        template.put("notify_time_start", notifyTimeStart);
        template.put("notify_time_end", notifyTimeEnd);
        template.put("priority", priority);
        template.put("display_duration_seconds", displayDurationSeconds);
        template.put("is_enabled", isEnabled);
        template.put("available_variables", Map.of(
            "case_id", "案件ID",
            "amount", "金额",
            "tag_name", "标签名称",
            "user_id", "用户ID"
        ));
        template.put("total_sent", 0);
        template.put("total_read", 0);
        template.put("created_at", "2025-01-15T10:00:00");
        template.put("updated_at", "2025-01-15T10:00:00");
        return template;
    }

    private Map<String, Object> createTemplateType(String type, String name, String description) {
        Map<String, Object> templateType = new HashMap<>();
        templateType.put("type", type);
        templateType.put("name", name);
        templateType.put("description", description);
        return templateType;
    }

    private Map<String, Object> createNotificationConfig(
            Long id, Long tenantId, String notificationType, Map<String, Object> configData
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put("id", id);
        config.put("tenant_id", tenantId);
        config.put("notification_type", notificationType);
        config.put("is_enabled", true);
        config.put("config_data", configData);
        config.put("created_at", "2025-01-15T10:00:00");
        config.put("updated_at", "2025-01-15T10:00:00");
        return config;
    }

    private Map<String, Object> createPublicNotification(
            Long id, Long tenantId, Long agencyId, String title, String content,
            String type, String startTime, String endTime, Integer sortOrder, Boolean isEnabled
    ) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("tenant_id", tenantId);
        notification.put("agency_id", agencyId);
        notification.put("title", title);
        notification.put("content", content);
        notification.put("type", type);
        notification.put("start_time", startTime);
        notification.put("end_time", endTime);
        notification.put("sort_order", sortOrder);
        notification.put("is_enabled", isEnabled);
        notification.put("created_at", "2025-01-15T10:00:00");
        notification.put("updated_at", "2025-01-15T10:00:00");
        return notification;
    }
}

