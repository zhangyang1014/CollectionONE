package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Infinity外呼系统Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/infinity")
public class InfinityController {

    /**
     * 获取甲方的Infinity配置
     */
    @GetMapping("/configs/{tenantId}")
    public ResponseData<Map<String, Object>> getInfinityConfigByTenant(@PathVariable Long tenantId) {
        log.info("========== 获取Infinity配置，tenantId={} ==========", tenantId);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", 1L);
        config.put("tenant_id", tenantId);
        config.put("api_url", "https://api.infinity.com");
        config.put("access_token", "token_" + tenantId);
        config.put("app_id", "app_" + tenantId);
        config.put("caller_number_range_start", "1000");
        config.put("caller_number_range_end", "9999");
        config.put("callback_url", "https://callback.example.com/infinity");
        config.put("max_concurrent_calls", 100);
        config.put("call_timeout_seconds", 60);
        config.put("is_active", true);
        config.put("created_at", "2025-01-01T00:00:00");
        config.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(config);
    }

    /**
     * 根据配置ID获取Infinity配置
     */
    @GetMapping("/configs/id/{configId}")
    public ResponseData<Map<String, Object>> getInfinityConfigById(@PathVariable Long configId) {
        log.info("========== 获取Infinity配置，configId={} ==========", configId);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", configId);
        config.put("tenant_id", 1L);
        config.put("api_url", "https://api.infinity.com");
        config.put("access_token", "token_" + configId);
        config.put("app_id", "app_" + configId);
        config.put("caller_number_range_start", "1000");
        config.put("caller_number_range_end", "9999");
        config.put("callback_url", "https://callback.example.com/infinity");
        config.put("max_concurrent_calls", 100);
        config.put("call_timeout_seconds", 60);
        config.put("is_active", true);
        config.put("created_at", "2025-01-01T00:00:00");
        config.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(config);
    }

    /**
     * 创建Infinity配置
     */
    @PostMapping("/configs")
    public ResponseData<Map<String, Object>> createInfinityConfig(@RequestBody Map<String, Object> request) {
        log.info("========== 创建Infinity配置，request={} ==========", request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", System.currentTimeMillis());
        config.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        config.put("api_url", request.get("api_url") != null ? request.get("api_url") : request.get("apiUrl"));
        config.put("access_token", request.get("access_token") != null ? request.get("access_token") : request.get("accessToken"));
        config.put("app_id", request.get("app_id") != null ? request.get("app_id") : request.get("appId"));
        config.put("caller_number_range_start", request.get("caller_number_range_start") != null ? request.get("caller_number_range_start") : request.get("callerNumberRangeStart"));
        config.put("caller_number_range_end", request.get("caller_number_range_end") != null ? request.get("caller_number_range_end") : request.get("callerNumberRangeEnd"));
        config.put("callback_url", request.get("callback_url") != null ? request.get("callback_url") : request.get("callbackUrl"));
        config.put("max_concurrent_calls", request.getOrDefault("max_concurrent_calls", request.getOrDefault("maxConcurrentCalls", 100)));
        config.put("call_timeout_seconds", request.getOrDefault("call_timeout_seconds", request.getOrDefault("callTimeoutSeconds", 60)));
        config.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        config.put("created_at", new Date().toString());
        config.put("updated_at", new Date().toString());
        
        return ResponseData.success(config);
    }

    /**
     * 更新Infinity配置
     */
    @PutMapping("/configs/{configId}")
    public ResponseData<Map<String, Object>> updateInfinityConfig(
            @PathVariable Long configId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新Infinity配置，configId={}, request={} ==========", configId, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", configId);
        config.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        config.put("api_url", request.get("api_url") != null ? request.get("api_url") : request.get("apiUrl"));
        config.put("access_token", request.get("access_token") != null ? request.get("access_token") : request.get("accessToken"));
        config.put("app_id", request.get("app_id") != null ? request.get("app_id") : request.get("appId"));
        config.put("caller_number_range_start", request.get("caller_number_range_start") != null ? request.get("caller_number_range_start") : request.get("callerNumberRangeStart"));
        config.put("caller_number_range_end", request.get("caller_number_range_end") != null ? request.get("caller_number_range_end") : request.get("callerNumberRangeEnd"));
        config.put("callback_url", request.get("callback_url") != null ? request.get("callback_url") : request.get("callbackUrl"));
        config.put("max_concurrent_calls", request.getOrDefault("max_concurrent_calls", request.getOrDefault("maxConcurrentCalls", 100)));
        config.put("call_timeout_seconds", request.getOrDefault("call_timeout_seconds", request.getOrDefault("callTimeoutSeconds", 60)));
        config.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        config.put("updated_at", new Date().toString());
        
        return ResponseData.success(config);
    }

    /**
     * 删除Infinity配置
     */
    @DeleteMapping("/configs/{configId}")
    public ResponseData<String> deleteInfinityConfig(@PathVariable Long configId) {
        log.info("========== 删除Infinity配置，configId={} ==========", configId);
        return ResponseData.success("删除成功");
    }

    /**
     * 测试连接
     */
    @PostMapping("/configs/{configId}/test-connection")
    public ResponseData<Map<String, Object>> testConnection(
            @PathVariable Long configId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 测试Infinity连接，configId={}, request={} ==========", configId, request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "连接成功");
        result.put("response_time", 150); // 毫秒
        
        return ResponseData.success(result);
    }

    /**
     * 获取分机列表
     */
    @GetMapping("/extensions/{tenantId}")
    public ResponseData<List<Map<String, Object>>> getExtensions(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long config_id,
            @RequestParam(required = false) String status) {
        log.info("========== 获取分机列表，tenantId={}, config_id={}, status={} ==========", 
                tenantId, config_id, status);
        
        List<Map<String, Object>> extensions = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> ext1 = new HashMap<>();
        ext1.put("id", 1L);
        ext1.put("tenant_id", tenantId);
        ext1.put("config_id", config_id != null ? config_id : 1L);
        ext1.put("infinity_extension_number", "1001");
        ext1.put("status", status != null ? status : "available");
        ext1.put("current_collector_id", null);
        ext1.put("assigned_at", null);
        ext1.put("released_at", null);
        ext1.put("last_used_at", "2025-11-24T10:00:00");
        ext1.put("created_at", "2025-01-01T00:00:00");
        ext1.put("updated_at", "2025-11-25T00:00:00");
        extensions.add(ext1);
        
        Map<String, Object> ext2 = new HashMap<>();
        ext2.put("id", 2L);
        ext2.put("tenant_id", tenantId);
        ext2.put("config_id", config_id != null ? config_id : 1L);
        ext2.put("infinity_extension_number", "1002");
        ext2.put("status", "in_use");
        ext2.put("current_collector_id", 1L);
        ext2.put("assigned_at", "2025-11-25T09:00:00");
        ext2.put("released_at", null);
        ext2.put("last_used_at", "2025-11-25T09:00:00");
        ext2.put("created_at", "2025-01-01T00:00:00");
        ext2.put("updated_at", "2025-11-25T09:00:00");
        extensions.add(ext2);
        
        Map<String, Object> ext3 = new HashMap<>();
        ext3.put("id", 3L);
        ext3.put("tenant_id", tenantId);
        ext3.put("config_id", config_id != null ? config_id : 1L);
        ext3.put("infinity_extension_number", "1003");
        ext3.put("status", "offline");
        ext3.put("current_collector_id", null);
        ext3.put("assigned_at", null);
        ext3.put("released_at", null);
        ext3.put("last_used_at", "2025-11-23T15:00:00");
        ext3.put("created_at", "2025-01-01T00:00:00");
        ext3.put("updated_at", "2025-11-23T15:00:00");
        extensions.add(ext3);
        
        // 过滤逻辑
        if (config_id != null) {
            extensions.removeIf(e -> !config_id.equals(e.get("config_id")));
        }
        
        if (status != null) {
            extensions.removeIf(e -> !status.equals(e.get("status")));
        }
        
        log.info("========== 返回分机列表，数量={} ==========", extensions.size());
        return ResponseData.success(extensions);
    }

    /**
     * 获取分机使用统计
     */
    @GetMapping("/extensions/statistics/{tenantId}")
    public ResponseData<Map<String, Object>> getExtensionStatistics(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long config_id) {
        log.info("========== 获取分机统计，tenantId={}, config_id={} ==========", tenantId, config_id);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("tenant_id", tenantId);
        statistics.put("config_id", config_id != null ? config_id : 1L);
        statistics.put("total_extensions", 100);
        statistics.put("available_count", 75);
        statistics.put("in_use_count", 20);
        statistics.put("offline_count", 5);
        statistics.put("usage_rate", 25.0); // 使用率百分比
        
        log.info("========== 返回分机统计 ==========");
        return ResponseData.success(statistics);
    }

    /**
     * 批量导入分机
     */
    @PostMapping("/extensions/batch-import")
    public ResponseData<Map<String, Object>> batchImportExtensions(
            @RequestBody Map<String, Object> request) {
        log.info("========== 批量导入分机，request={} ==========", request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("imported_count", 0);
        
        @SuppressWarnings("unchecked")
        List<String> extensionNumbers = (List<String>) request.get("extension_numbers");
        if (extensionNumbers != null) {
            result.put("imported_count", extensionNumbers.size());
        }
        
        return ResponseData.success(result);
    }

    /**
     * 更新分机状态
     */
    @PutMapping("/extensions/{extensionId}")
    public ResponseData<Map<String, Object>> updateExtension(
            @PathVariable Long extensionId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新分机，extensionId={}, request={} ==========", extensionId, request);
        
        Map<String, Object> extension = new HashMap<>();
        extension.put("id", extensionId);
        extension.put("status", request.get("status"));
        extension.put("current_collector_id", request.get("current_collector_id"));
        extension.put("updated_at", new Date().toString());
        
        return ResponseData.success(extension);
    }

    /**
     * 删除分机
     */
    @DeleteMapping("/extensions/{extensionId}")
    public ResponseData<String> deleteExtension(@PathVariable Long extensionId) {
        log.info("========== 删除分机，extensionId={} ==========", extensionId);
        return ResponseData.success("删除成功");
    }
}


