package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Mock Infinity外呼系统Controller（临时，用于前端开发）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/infinity")
public class MockInfinityController {

    /**
     * 创建Infinity配置
     * POST /api/v1/infinity/configs
     */
    @PostMapping("/configs")
    public ResponseData<Map<String, Object>> createInfinityConfig(
            @RequestBody Map<String, Object> data
    ) {
        log.info("创建Infinity配置（Mock），data={}", data);
        
        Long id = System.currentTimeMillis();
        Long tenantId = data.get("tenant_id") != null ? 
            ((Number) data.get("tenant_id")).longValue() : 1L;
        
        Map<String, Object> config = createInfinityConfig(
            id, tenantId, data
        );
        
        return ResponseData.success(config);
    }

    /**
     * 获取甲方的Infinity配置
     * GET /api/v1/infinity/configs/{tenantId}
     */
    @GetMapping("/configs/{tenantId}")
    public ResponseData<Map<String, Object>> getInfinityConfigByTenant(
            @PathVariable Long tenantId
    ) {
        log.info("获取Infinity配置（Mock），tenantId={}", tenantId);
        
        Map<String, Object> config = createInfinityConfig(
            1L, tenantId, new HashMap<>()
        );
        
        return ResponseData.success(config);
    }

    /**
     * 根据配置ID获取
     * GET /api/v1/infinity/configs/id/{configId}
     */
    @GetMapping("/configs/id/{configId}")
    public ResponseData<Map<String, Object>> getInfinityConfigById(
            @PathVariable Long configId
    ) {
        log.info("根据ID获取Infinity配置（Mock），configId={}", configId);
        
        Map<String, Object> config = createInfinityConfig(
            configId, 1L, new HashMap<>()
        );
        
        return ResponseData.success(config);
    }

    /**
     * 更新Infinity配置
     * PUT /api/v1/infinity/configs/{configId}
     */
    @PutMapping("/configs/{configId}")
    public ResponseData<Map<String, Object>> updateInfinityConfig(
            @PathVariable Long configId,
            @RequestBody Map<String, Object> data
    ) {
        log.info("更新Infinity配置（Mock），configId={}, data={}", configId, data);
        
        Map<String, Object> config = createInfinityConfig(
            configId, 1L, new HashMap<>()
        );
        
        // 更新字段
        if (data.containsKey("supplier_id")) {
            config.put("supplier_id", data.get("supplier_id"));
        }
        if (data.containsKey("api_url")) {
            config.put("api_url", data.get("api_url"));
        }
        if (data.containsKey("access_token")) {
            config.put("access_token", data.get("access_token"));
        }
        if (data.containsKey("app_id")) {
            config.put("app_id", data.get("app_id"));
        }
        if (data.containsKey("caller_number_range_start")) {
            config.put("caller_number_range_start", data.get("caller_number_range_start"));
        }
        if (data.containsKey("caller_number_range_end")) {
            config.put("caller_number_range_end", data.get("caller_number_range_end"));
        }
        if (data.containsKey("callback_url")) {
            config.put("callback_url", data.get("callback_url"));
        }
        if (data.containsKey("recording_callback_url")) {
            config.put("recording_callback_url", data.get("recording_callback_url"));
        }
        if (data.containsKey("max_concurrent_calls")) {
            config.put("max_concurrent_calls", data.get("max_concurrent_calls"));
        }
        if (data.containsKey("call_timeout_seconds")) {
            config.put("call_timeout_seconds", data.get("call_timeout_seconds"));
        }
        if (data.containsKey("is_active")) {
            config.put("is_active", data.get("is_active"));
        }
        
        config.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseData.success(config);
    }

    /**
     * 删除Infinity配置
     * DELETE /api/v1/infinity/configs/{configId}
     */
    @DeleteMapping("/configs/{configId}")
    public ResponseData<Map<String, Object>> deleteInfinityConfig(
            @PathVariable Long configId
    ) {
        log.info("删除Infinity配置（Mock），configId={}", configId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置已删除（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 测试Infinity连接
     * POST /api/v1/infinity/configs/test-connection
     */
    @PostMapping("/configs/test-connection")
    public ResponseData<Map<String, Object>> testInfinityConnection(
            @RequestBody Map<String, Object> data
    ) {
        log.info("测试Infinity连接（Mock），data={}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "连接测试成功（Mock）");
        result.put("response_time_ms", 150);
        
        return ResponseData.success(result);
    }

    /**
     * 启用/禁用配置
     * POST /api/v1/infinity/configs/{configId}/toggle
     */
    @PostMapping("/configs/{configId}/toggle")
    public ResponseData<Map<String, Object>> toggleInfinityConfig(
            @PathVariable Long configId,
            @RequestParam(value = "is_active", required = false, defaultValue = "true") Boolean isActive
    ) {
        log.info("切换Infinity配置状态（Mock），configId={}, isActive={}", configId, isActive);
        
        Map<String, Object> config = createInfinityConfig(
            configId, 1L, new HashMap<>()
        );
        config.put("is_active", isActive);
        config.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseData.success(config);
    }

    /**
     * 批量导入分机号
     * POST /api/v1/infinity/extensions/batch-import
     */
    @PostMapping("/extensions/batch-import")
    public ResponseData<Map<String, Object>> batchImportExtensions(
            @RequestBody Map<String, Object> data
    ) {
        log.info("批量导入分机号（Mock），data={}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分机号导入成功（Mock）");
        result.put("imported_count", 10);
        
        return ResponseData.success(result);
    }

    /**
     * 查询分机池
     * GET /api/v1/infinity/extensions/{tenantId}
     */
    @GetMapping("/extensions/{tenantId}")
    public ResponseData<List<Map<String, Object>>> getExtensions(
            @PathVariable Long tenantId,
            @RequestParam(value = "config_id", required = false) Long configId,
            @RequestParam(required = false) String status
    ) {
        log.info("查询分机池（Mock），tenantId={}, configId={}, status={}", tenantId, configId, status);
        
        List<Map<String, Object>> extensions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            extensions.add(createExtension(
                (long) i, tenantId, configId != null ? configId : 1L,
                "100" + i, "available", null
            ));
        }
        
        return ResponseData.success(extensions);
    }

    /**
     * 获取分机使用统计
     * GET /api/v1/infinity/extensions/statistics/{tenantId}
     */
    @GetMapping("/extensions/statistics/{tenantId}")
    public ResponseData<Map<String, Object>> getExtensionStatistics(
            @PathVariable Long tenantId,
            @RequestParam(value = "config_id", required = false) Long configId
    ) {
        log.info("获取分机使用统计（Mock），tenantId={}, configId={}", tenantId, configId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("tenant_id", tenantId);
        statistics.put("config_id", configId != null ? configId : 1L);
        statistics.put("total_extensions", 10);
        statistics.put("available_count", 7);
        statistics.put("in_use_count", 2);
        statistics.put("offline_count", 1);
        statistics.put("usage_rate", 0.2);
        
        return ResponseData.success(statistics);
    }

    /**
     * 更新分机
     * PUT /api/v1/infinity/extensions/{extensionId}
     */
    @PutMapping("/extensions/{extensionId}")
    public ResponseData<Map<String, Object>> updateExtension(
            @PathVariable Long extensionId,
            @RequestBody Map<String, Object> data
    ) {
        log.info("更新分机（Mock），extensionId={}, data={}", extensionId, data);
        
        Map<String, Object> extension = createExtension(
            extensionId, 1L, 1L, "1001", "available", null
        );
        extension.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseData.success(extension);
    }

    /**
     * 手动释放分机
     * POST /api/v1/infinity/extensions/{extensionId}/release
     */
    @PostMapping("/extensions/{extensionId}/release")
    public ResponseData<Map<String, Object>> releaseExtension(
            @PathVariable Long extensionId
    ) {
        log.info("手动释放分机（Mock），extensionId={}", extensionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分机已释放（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 删除分机
     * DELETE /api/v1/infinity/extensions/{extensionId}
     */
    @DeleteMapping("/extensions/{extensionId}")
    public ResponseData<Map<String, Object>> deleteExtension(
            @PathVariable Long extensionId
    ) {
        log.info("删除分机（Mock），extensionId={}", extensionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分机已删除（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 批量删除分机
     * POST /api/v1/infinity/extensions/batch-delete
     */
    @PostMapping("/extensions/batch-delete")
    public ResponseData<Map<String, Object>> batchDeleteExtensions(
            @RequestBody Map<String, Object> data
    ) {
        log.info("批量删除分机（Mock），data={}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分机批量删除成功（Mock）");
        result.put("deleted_count", 5);
        
        return ResponseData.success(result);
    }

    /**
     * 强制释放催员占用的分机
     * POST /api/v1/infinity/extensions/force-release-collector/{collectorId}
     */
    @PostMapping("/extensions/force-release-collector/{collectorId}")
    public ResponseData<Map<String, Object>> forceReleaseCollectorExtensions(
            @PathVariable Long collectorId
    ) {
        log.info("强制释放催员占用的分机（Mock），collectorId={}", collectorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "分机已强制释放（Mock）");
        result.put("released_count", 2);
        
        return ResponseData.success(result);
    }

    /**
     * 发起外呼
     * POST /api/v1/infinity/make-call
     */
    @PostMapping("/make-call")
    public ResponseData<Map<String, Object>> makeCall(
            @RequestBody Map<String, Object> data
    ) {
        log.info("发起外呼（Mock），data={}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("call_id", System.currentTimeMillis());
        result.put("call_uuid", UUID.randomUUID().toString());
        result.put("extension_number", "1001");
        result.put("message", "外呼已发起（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 创建Infinity配置数据
     */
    private Map<String, Object> createInfinityConfig(
            Long id, Long tenantId, Map<String, Object> data
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put("id", id);
        config.put("tenant_id", tenantId);
        config.put("supplier_id", data.getOrDefault("supplier_id", 1L));
        config.put("api_url", data.getOrDefault("api_url", "https://api.infinity.example.com"));
        config.put("access_token", data.getOrDefault("access_token", "mock_access_token"));
        config.put("app_id", data.getOrDefault("app_id", "mock_app_id"));
        config.put("caller_number_range_start", data.getOrDefault("caller_number_range_start", "1000"));
        config.put("caller_number_range_end", data.getOrDefault("caller_number_range_end", "1999"));
        config.put("callback_url", data.getOrDefault("callback_url", "https://callback.example.com"));
        config.put("recording_callback_url", data.getOrDefault("recording_callback_url", "https://recording.example.com"));
        config.put("max_concurrent_calls", data.getOrDefault("max_concurrent_calls", 10));
        config.put("call_timeout_seconds", data.getOrDefault("call_timeout_seconds", 60));
        config.put("is_active", data.getOrDefault("is_active", true));
        config.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        config.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return config;
    }

    /**
     * 创建分机数据
     */
    private Map<String, Object> createExtension(
            Long id, Long tenantId, Long configId, String extensionNumber,
            String status, Long collectorId
    ) {
        Map<String, Object> extension = new HashMap<>();
        extension.put("id", id);
        extension.put("tenant_id", tenantId);
        extension.put("config_id", configId);
        extension.put("infinity_extension_number", extensionNumber);
        extension.put("status", status);
        if (collectorId != null) {
            extension.put("current_collector_id", collectorId);
        }
        extension.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        extension.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return extension;
    }
}





