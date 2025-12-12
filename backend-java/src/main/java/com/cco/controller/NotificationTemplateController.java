package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 通知模板Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/notification-templates")
public class NotificationTemplateController {

    /**
     * 获取通知模板列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getNotificationTemplates(
            @RequestParam(required = false) Long tenant_id,
            @RequestParam(required = false) String template_type,
            @RequestParam(required = false) Boolean is_enabled) {
        log.info("========== 获取通知模板列表，tenant_id={}, template_type={}, is_enabled={} ==========", 
                tenant_id, template_type, is_enabled);
        
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // Mock数据 - 案件标签变更模板
        Map<String, Object> template1 = new HashMap<>();
        template1.put("id", 1L);
        template1.put("tenant_id", tenant_id != null ? String.valueOf(tenant_id) : "1");
        template1.put("template_id", "case_tag_change_001");
        template1.put("template_name", "案件标签变更通知");
        template1.put("template_type", "case_tag_change");
        template1.put("description", "当案件标签发生变更时发送此通知");
        template1.put("content_template", "案件 {case_id} 的标签已从 {old_tag} 变更为 {new_tag}");
        template1.put("jump_url_template", "/cases/{case_id}");
        template1.put("target_type", "collector");
        template1.put("target_agencies", null);
        template1.put("target_teams", null);
        template1.put("target_collectors", Arrays.asList("1", "2", "3"));
        template1.put("is_forced_read", false);
        template1.put("repeat_interval_minutes", 60);
        template1.put("max_remind_count", 3);
        template1.put("notify_time_start", "09:00");
        template1.put("notify_time_end", "18:00");
        template1.put("priority", 2);
        template1.put("display_duration_seconds", 5);
        template1.put("is_enabled", true);
        template1.put("available_variables", Arrays.asList("case_id", "old_tag", "new_tag"));
        template1.put("total_sent", 150);
        template1.put("total_read", 120);
        template1.put("created_at", "2025-01-01T00:00:00");
        template1.put("updated_at", "2025-11-25T00:00:00");
        template1.put("created_by", 1L);
        templates.add(template1);
        
        // Mock数据 - 还款记录模板
        Map<String, Object> template2 = new HashMap<>();
        template2.put("id", 2L);
        template2.put("tenant_id", tenant_id != null ? String.valueOf(tenant_id) : "1");
        template2.put("template_id", "case_payment_001");
        template2.put("template_name", "案件还款通知");
        template2.put("template_type", "case_payment");
        template2.put("description", "当案件收到还款时发送此通知");
        template2.put("content_template", "案件 {case_id} 收到还款 {amount} 元，还款时间：{payment_time}");
        template2.put("jump_url_template", "/cases/{case_id}/payments");
        template2.put("target_type", "team");
        template2.put("target_agencies", null);
        template2.put("target_teams", Arrays.asList("1", "2"));
        template2.put("target_collectors", null);
        template2.put("is_forced_read", true);
        template2.put("repeat_interval_minutes", null);
        template2.put("max_remind_count", null);
        template2.put("notify_time_start", null);
        template2.put("notify_time_end", null);
        template2.put("priority", 1);
        template2.put("display_duration_seconds", 10);
        template2.put("is_enabled", true);
        template2.put("available_variables", Arrays.asList("case_id", "amount", "payment_time"));
        template2.put("total_sent", 300);
        template2.put("total_read", 280);
        template2.put("created_at", "2025-01-01T00:00:00");
        template2.put("updated_at", "2025-11-25T00:00:00");
        template2.put("created_by", 1L);
        templates.add(template2);
        
        // Mock数据 - 用户访问模板
        Map<String, Object> template3 = new HashMap<>();
        template3.put("id", 3L);
        template3.put("tenant_id", tenant_id != null ? String.valueOf(tenant_id) : "1");
        template3.put("template_id", "user_app_visit_001");
        template3.put("template_name", "用户APP访问通知");
        template3.put("template_type", "user_app_visit");
        template3.put("description", "当用户访问APP时发送此通知");
        template3.put("content_template", "用户 {user_name} 在 {visit_time} 访问了APP");
        template3.put("jump_url_template", "/users/{user_id}");
        template3.put("target_type", "agency");
        template3.put("target_agencies", Arrays.asList("1"));
        template3.put("target_teams", null);
        template3.put("target_collectors", null);
        template3.put("is_forced_read", false);
        template3.put("repeat_interval_minutes", 30);
        template3.put("max_remind_count", 5);
        template3.put("notify_time_start", "08:00");
        template3.put("notify_time_end", "20:00");
        template3.put("priority", 3);
        template3.put("display_duration_seconds", 3);
        template3.put("is_enabled", true);
        template3.put("available_variables", Arrays.asList("user_name", "visit_time", "user_id"));
        template3.put("total_sent", 500);
        template3.put("total_read", 450);
        template3.put("created_at", "2025-01-01T00:00:00");
        template3.put("updated_at", "2025-11-25T00:00:00");
        template3.put("created_by", 1L);
        templates.add(template3);
        
        // 过滤逻辑
        if (tenant_id != null) {
            templates.removeIf(t -> {
                Object tTenantId = t.get("tenant_id");
                return tTenantId != null && !String.valueOf(tenant_id).equals(tTenantId.toString());
            });
        }
        
        if (template_type != null) {
            templates.removeIf(t -> !template_type.equals(t.get("template_type")));
        }
        
        if (is_enabled != null) {
            templates.removeIf(t -> !is_enabled.equals(t.get("is_enabled")));
        }
        
        log.info("========== 返回通知模板列表，数量={} ==========", templates.size());
        return ResponseData.success(templates);
    }

    /**
     * 获取单个通知模板详情
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getNotificationTemplate(@PathVariable Long id) {
        log.info("========== 获取通知模板详情，id={} ==========", id);
        
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("tenant_id", "1");
        template.put("template_id", "template_" + id);
        template.put("template_name", "通知模板" + id);
        template.put("template_type", "case_tag_change");
        template.put("description", "模板描述");
        template.put("content_template", "通知内容模板 {case_id}");
        template.put("jump_url_template", "/cases/{case_id}");
        template.put("target_type", "collector");
        template.put("target_agencies", null);
        template.put("target_teams", null);
        template.put("target_collectors", Arrays.asList("1"));
        template.put("is_forced_read", false);
        template.put("repeat_interval_minutes", 60);
        template.put("max_remind_count", 3);
        template.put("notify_time_start", "09:00");
        template.put("notify_time_end", "18:00");
        template.put("priority", 2);
        template.put("display_duration_seconds", 5);
        template.put("is_enabled", true);
        template.put("available_variables", Arrays.asList("case_id"));
        template.put("total_sent", 0);
        template.put("total_read", 0);
        template.put("created_at", "2025-01-01T00:00:00");
        template.put("updated_at", "2025-11-25T00:00:00");
        template.put("created_by", 1L);
        
        return ResponseData.success(template);
    }

    /**
     * 创建通知模板
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createNotificationTemplate(@RequestBody Map<String, Object> request) {
        log.info("========== 创建通知模板，request={} ==========", request);
        
        Map<String, Object> template = new HashMap<>();
        template.put("id", System.currentTimeMillis());
        template.put("tenant_id", request.get("tenant_id") != null ? String.valueOf(request.get("tenant_id")) : request.get("tenantId") != null ? String.valueOf(request.get("tenantId")) : "1");
        template.put("template_id", request.get("template_id") != null ? request.get("template_id") : request.get("templateId"));
        template.put("template_name", request.get("template_name") != null ? request.get("template_name") : request.get("templateName"));
        template.put("template_type", request.get("template_type") != null ? request.get("template_type") : request.get("templateType"));
        template.put("description", request.get("description"));
        template.put("content_template", request.get("content_template") != null ? request.get("content_template") : request.get("contentTemplate"));
        template.put("jump_url_template", request.get("jump_url_template") != null ? request.get("jump_url_template") : request.get("jumpUrlTemplate"));
        template.put("target_type", request.get("target_type") != null ? request.get("target_type") : request.get("targetType"));
        template.put("target_agencies", request.get("target_agencies") != null ? request.get("target_agencies") : request.get("targetAgencies"));
        template.put("target_teams", request.get("target_teams") != null ? request.get("target_teams") : request.get("targetTeams"));
        template.put("target_collectors", request.get("target_collectors") != null ? request.get("target_collectors") : request.get("targetCollectors"));
        template.put("is_forced_read", request.getOrDefault("is_forced_read", request.getOrDefault("isForcedRead", false)));
        template.put("repeat_interval_minutes", request.get("repeat_interval_minutes") != null ? request.get("repeat_interval_minutes") : request.get("repeatIntervalMinutes"));
        template.put("max_remind_count", request.get("max_remind_count") != null ? request.get("max_remind_count") : request.get("maxRemindCount"));
        template.put("notify_time_start", request.get("notify_time_start") != null ? request.get("notify_time_start") : request.get("notifyTimeStart"));
        template.put("notify_time_end", request.get("notify_time_end") != null ? request.get("notify_time_end") : request.get("notifyTimeEnd"));
        template.put("priority", request.getOrDefault("priority", 2));
        template.put("display_duration_seconds", request.getOrDefault("display_duration_seconds", request.getOrDefault("displayDurationSeconds", 5)));
        template.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        template.put("available_variables", request.get("available_variables") != null ? request.get("available_variables") : request.get("availableVariables"));
        template.put("total_sent", 0);
        template.put("total_read", 0);
        template.put("created_at", new Date().toString());
        template.put("updated_at", new Date().toString());
        template.put("created_by", 1L);
        
        return ResponseData.success(template);
    }

    /**
     * 更新通知模板
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateNotificationTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新通知模板，id={}, request={} ==========", id, request);
        
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("tenant_id", "1");
        template.put("template_id", request.get("template_id") != null ? request.get("template_id") : "template_" + id);
        template.put("template_name", request.get("template_name") != null ? request.get("template_name") : request.get("templateName"));
        template.put("template_type", request.get("template_type") != null ? request.get("template_type") : request.get("templateType"));
        template.put("description", request.get("description"));
        template.put("content_template", request.get("content_template") != null ? request.get("content_template") : request.get("contentTemplate"));
        template.put("jump_url_template", request.get("jump_url_template") != null ? request.get("jump_url_template") : request.get("jumpUrlTemplate"));
        template.put("target_type", request.get("target_type") != null ? request.get("target_type") : request.get("targetType"));
        template.put("target_agencies", request.get("target_agencies") != null ? request.get("target_agencies") : request.get("targetAgencies"));
        template.put("target_teams", request.get("target_teams") != null ? request.get("target_teams") : request.get("targetTeams"));
        template.put("target_collectors", request.get("target_collectors") != null ? request.get("target_collectors") : request.get("targetCollectors"));
        template.put("is_forced_read", request.getOrDefault("is_forced_read", request.getOrDefault("isForcedRead", false)));
        template.put("repeat_interval_minutes", request.get("repeat_interval_minutes") != null ? request.get("repeat_interval_minutes") : request.get("repeatIntervalMinutes"));
        template.put("max_remind_count", request.get("max_remind_count") != null ? request.get("max_remind_count") : request.get("maxRemindCount"));
        template.put("notify_time_start", request.get("notify_time_start") != null ? request.get("notify_time_start") : request.get("notifyTimeStart"));
        template.put("notify_time_end", request.get("notify_time_end") != null ? request.get("notify_time_end") : request.get("notifyTimeEnd"));
        template.put("priority", request.getOrDefault("priority", 2));
        template.put("display_duration_seconds", request.getOrDefault("display_duration_seconds", request.getOrDefault("displayDurationSeconds", 5)));
        template.put("is_enabled", request.getOrDefault("is_enabled", request.getOrDefault("isEnabled", true)));
        template.put("available_variables", request.get("available_variables") != null ? request.get("available_variables") : request.get("availableVariables"));
        template.put("updated_at", new Date().toString());
        
        return ResponseData.success(template);
    }

    /**
     * 删除通知模板
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteNotificationTemplate(@PathVariable Long id) {
        log.info("========== 删除通知模板，id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取所有可用的模板类型
     */
    @GetMapping("/types/list")
    public ResponseData<Map<String, List<Map<String, Object>>>> getTemplateTypes() {
        log.info("========== 获取模板类型列表 ==========");
        
        List<Map<String, Object>> types = new ArrayList<>();
        
        // 案件标签变更
        Map<String, Object> type1 = new HashMap<>();
        type1.put("value", "case_tag_change");
        type1.put("label", "案件标签变更");
        type1.put("description", "当案件标签发生变更时触发");
        Map<String, String> variables1 = new HashMap<>();
        variables1.put("case_id", "案件ID");
        variables1.put("old_tag", "旧标签");
        variables1.put("new_tag", "新标签");
        type1.put("variables", variables1);
        types.add(type1);
        
        // 案件还款
        Map<String, Object> type2 = new HashMap<>();
        type2.put("value", "case_payment");
        type2.put("label", "案件还款");
        type2.put("description", "当案件收到还款时触发");
        Map<String, String> variables2 = new HashMap<>();
        variables2.put("case_id", "案件ID");
        variables2.put("amount", "还款金额");
        variables2.put("payment_time", "还款时间");
        type2.put("variables", variables2);
        types.add(type2);
        
        // 用户APP访问
        Map<String, Object> type3 = new HashMap<>();
        type3.put("value", "user_app_visit");
        type3.put("label", "用户APP访问");
        type3.put("description", "当用户访问APP时触发");
        Map<String, String> variables3 = new HashMap<>();
        variables3.put("user_name", "用户姓名");
        variables3.put("visit_time", "访问时间");
        variables3.put("user_id", "用户ID");
        type3.put("variables", variables3);
        types.add(type3);
        
        // 用户还款页面访问
        Map<String, Object> type4 = new HashMap<>();
        type4.put("value", "user_payment_page_visit");
        type4.put("label", "用户还款页面访问");
        type4.put("description", "当用户访问还款页面时触发");
        Map<String, String> variables4 = new HashMap<>();
        variables4.put("user_name", "用户姓名");
        variables4.put("case_id", "案件ID");
        variables4.put("visit_time", "访问时间");
        type4.put("variables", variables4);
        types.add(type4);
        
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("types", types);
        
        log.info("========== 返回模板类型列表，数量={} ==========", types.size());
        return ResponseData.success(result);
    }
}






































