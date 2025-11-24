package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Mock渠道供应商管理Controller（临时，用于前端开发）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/channel-suppliers")
public class MockChannelSupplierController {

    /**
     * 获取渠道供应商列表
     * GET /api/v1/channel-suppliers/tenants/{tenantId}/channels/{channelType}/suppliers
     */
    @GetMapping("/tenants/{tenantId}/channels/{channelType}/suppliers")
    public ResponseData<List<Map<String, Object>>> getChannelSuppliers(
            @PathVariable Long tenantId,
            @PathVariable String channelType
    ) {
        log.info("获取渠道供应商列表（Mock），tenantId={}, channelType={}", tenantId, channelType);
        
        List<Map<String, Object>> suppliers = new ArrayList<>();
        
        // 根据渠道类型返回不同的Mock数据
        switch (channelType.toLowerCase()) {
            case "sms":
                suppliers.add(createSupplier(1L, tenantId, "sms", "SMS供应商A", 
                    "https://api.sms-supplier-a.com", "api_key_001", "secret_key_001", 1, true));
                suppliers.add(createSupplier(2L, tenantId, "sms", "SMS供应商B", 
                    "https://api.sms-supplier-b.com", "api_key_002", "secret_key_002", 2, true));
                break;
            case "rcs":
                suppliers.add(createSupplier(3L, tenantId, "rcs", "RCS供应商A", 
                    "https://api.rcs-supplier-a.com", "api_key_003", "secret_key_003", 1, true));
                suppliers.add(createSupplier(4L, tenantId, "rcs", "RCS供应商B", 
                    "https://api.rcs-supplier-b.com", "api_key_004", "secret_key_004", 2, true));
                break;
            case "whatsapp":
                suppliers.add(createSupplier(5L, tenantId, "whatsapp", "WhatsApp供应商A", 
                    "https://api.whatsapp-supplier-a.com", "api_key_005", "secret_key_005", 1, true));
                suppliers.add(createSupplier(6L, tenantId, "whatsapp", "WhatsApp供应商B", 
                    "https://api.whatsapp-supplier-b.com", "api_key_006", "secret_key_006", 2, true));
                break;
            case "call":
                suppliers.add(createSupplier(7L, tenantId, "call", "外呼供应商A", 
                    "https://api.call-supplier-a.com", "api_key_007", "secret_key_007", 1, true));
                suppliers.add(createSupplier(8L, tenantId, "call", "外呼供应商B", 
                    "https://api.call-supplier-b.com", "api_key_008", "secret_key_008", 2, true));
                break;
            default:
                // 默认返回空列表
                break;
        }
        
        return ResponseData.success(suppliers);
    }

    /**
     * 创建渠道供应商
     * POST /api/v1/channel-suppliers/tenants/{tenantId}/channels/{channelType}/suppliers
     */
    @PostMapping("/tenants/{tenantId}/channels/{channelType}/suppliers")
    public ResponseData<Map<String, Object>> createChannelSupplier(
            @PathVariable Long tenantId,
            @PathVariable String channelType,
            @RequestBody Map<String, Object> data
    ) {
        log.info("创建渠道供应商（Mock），tenantId={}, channelType={}, data={}", tenantId, channelType, data);
        
        Long id = System.currentTimeMillis(); // 使用时间戳作为ID
        String supplierName = (String) data.getOrDefault("supplier_name", "新供应商");
        String apiUrl = (String) data.getOrDefault("api_url", "");
        String apiKey = (String) data.getOrDefault("api_key", "");
        String secretKey = (String) data.getOrDefault("secret_key", "");
        Integer sortOrder = data.get("sort_order") != null ? 
            ((Number) data.get("sort_order")).intValue() : 999;
        Boolean isActive = data.get("is_active") != null ? 
            (Boolean) data.get("is_active") : true;
        String remark = (String) data.getOrDefault("remark", "");
        
        Map<String, Object> supplier = createSupplier(
            id, tenantId, channelType, supplierName, 
            apiUrl, apiKey, secretKey, sortOrder, isActive
        );
        if (remark != null && !remark.isEmpty()) {
            supplier.put("remark", remark);
        }
        
        return ResponseData.success(supplier);
    }

    /**
     * 获取单个渠道供应商
     * GET /api/v1/channel-suppliers/{supplierId}
     */
    @GetMapping("/{supplierId}")
    public ResponseData<Map<String, Object>> getChannelSupplier(
            @PathVariable Long supplierId
    ) {
        log.info("获取渠道供应商详情（Mock），supplierId={}", supplierId);
        
        Map<String, Object> supplier = createSupplier(
            supplierId, 1L, "sms", "供应商示例", 
            "https://api.example.com", "api_key", "secret_key", 1, true
        );
        
        return ResponseData.success(supplier);
    }

    /**
     * 更新渠道供应商
     * PUT /api/v1/channel-suppliers/{supplierId}
     */
    @PutMapping("/{supplierId}")
    public ResponseData<Map<String, Object>> updateChannelSupplier(
            @PathVariable Long supplierId,
            @RequestBody Map<String, Object> data
    ) {
        log.info("更新渠道供应商（Mock），supplierId={}, data={}", supplierId, data);
        
        // 获取现有数据（Mock）
        Map<String, Object> supplier = createSupplier(
            supplierId, 1L, "sms", "供应商示例", 
            "https://api.example.com", "api_key", "secret_key", 1, true
        );
        
        // 更新字段
        if (data.containsKey("supplier_name")) {
            supplier.put("supplier_name", data.get("supplier_name"));
        }
        if (data.containsKey("api_url")) {
            supplier.put("api_url", data.get("api_url"));
        }
        if (data.containsKey("api_key")) {
            supplier.put("api_key", data.get("api_key"));
        }
        if (data.containsKey("secret_key")) {
            supplier.put("secret_key", data.get("secret_key"));
        }
        if (data.containsKey("sort_order")) {
            supplier.put("sort_order", data.get("sort_order"));
        }
        if (data.containsKey("is_active")) {
            supplier.put("is_active", data.get("is_active"));
        }
        if (data.containsKey("remark")) {
            supplier.put("remark", data.get("remark"));
        }
        
        supplier.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseData.success(supplier);
    }

    /**
     * 删除渠道供应商
     * DELETE /api/v1/channel-suppliers/{supplierId}
     */
    @DeleteMapping("/{supplierId}")
    public ResponseData<Map<String, Object>> deleteChannelSupplier(
            @PathVariable Long supplierId
    ) {
        log.info("删除渠道供应商（Mock），supplierId={}", supplierId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "供应商已删除（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 批量更新供应商排序
     * PUT /api/v1/channel-suppliers/tenants/{tenantId}/channels/{channelType}/suppliers/order
     */
    @PutMapping("/tenants/{tenantId}/channels/{channelType}/suppliers/order")
    public ResponseData<Map<String, Object>> updateSupplierOrder(
            @PathVariable Long tenantId,
            @PathVariable String channelType,
            @RequestBody Map<String, Object> data
    ) {
        log.info("批量更新供应商排序（Mock），tenantId={}, channelType={}, data={}", 
            tenantId, channelType, data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "排序已更新（Mock）");
        
        return ResponseData.success(result);
    }

    /**
     * 创建供应商数据
     */
    private Map<String, Object> createSupplier(
            Long id, Long tenantId, String channelType, String supplierName,
            String apiUrl, String apiKey, String secretKey, 
            Integer sortOrder, Boolean isActive
    ) {
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("id", id);
        supplier.put("tenant_id", tenantId);
        supplier.put("channel_type", channelType);
        supplier.put("supplier_name", supplierName);
        supplier.put("api_url", apiUrl);
        supplier.put("api_key", apiKey);
        supplier.put("secret_key", secretKey);
        supplier.put("sort_order", sortOrder);
        supplier.put("is_active", isActive);
        supplier.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        supplier.put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return supplier;
    }
}




