package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 渠道供应商Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/channel-suppliers")
public class ChannelSupplierController {

    /**
     * 获取指定甲方和渠道类型的供应商列表
     */
    @GetMapping("/tenants/{tenantId}/channels/{channelType}/suppliers")
    public ResponseData<List<Map<String, Object>>> getChannelSuppliers(
            @PathVariable Long tenantId,
            @PathVariable String channelType) {
        log.info("========== 获取渠道供应商列表，tenantId={}, channelType={} ==========", tenantId, channelType);
        
        List<Map<String, Object>> suppliers = new ArrayList<>();
        
        // Mock数据 - 根据渠道类型返回不同的供应商
        Map<String, Object> supplier1 = new HashMap<>();
        supplier1.put("id", 1L);
        supplier1.put("tenant_id", tenantId);
        supplier1.put("channel_type", channelType);
        supplier1.put("supplier_name", "测试供应商1");
        supplier1.put("api_url", "https://api.supplier1.com");
        supplier1.put("api_key", "key_001");
        supplier1.put("secret_key", "secret_001");
        supplier1.put("is_active", true);
        supplier1.put("sort_order", 1);
        supplier1.put("remark", "测试供应商1的备注");
        supplier1.put("created_at", "2025-01-01T00:00:00");
        supplier1.put("updated_at", "2025-11-25T00:00:00");
        suppliers.add(supplier1);
        
        Map<String, Object> supplier2 = new HashMap<>();
        supplier2.put("id", 2L);
        supplier2.put("tenant_id", tenantId);
        supplier2.put("channel_type", channelType);
        supplier2.put("supplier_name", "测试供应商2");
        supplier2.put("api_url", "https://api.supplier2.com");
        supplier2.put("api_key", "key_002");
        supplier2.put("secret_key", "secret_002");
        supplier2.put("is_active", true);
        supplier2.put("sort_order", 2);
        supplier2.put("remark", "测试供应商2的备注");
        supplier2.put("created_at", "2025-01-02T00:00:00");
        supplier2.put("updated_at", "2025-11-25T00:00:00");
        suppliers.add(supplier2);
        
        log.info("========== 返回渠道供应商列表，数量={} ==========", suppliers.size());
        return ResponseData.success(suppliers);
    }

    /**
     * 获取单个渠道供应商详情
     */
    @GetMapping("/{supplierId}")
    public ResponseData<Map<String, Object>> getChannelSupplier(@PathVariable Long supplierId) {
        log.info("========== 获取渠道供应商详情，supplierId={} ==========", supplierId);
        
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("id", supplierId);
        supplier.put("tenant_id", 1L);
        supplier.put("channel_type", "sms");
        supplier.put("supplier_name", "测试供应商" + supplierId);
        supplier.put("api_url", "https://api.supplier" + supplierId + ".com");
        supplier.put("api_key", "key_" + String.format("%03d", supplierId));
        supplier.put("secret_key", "secret_" + String.format("%03d", supplierId));
        supplier.put("is_active", true);
        supplier.put("sort_order", supplierId.intValue());
        supplier.put("remark", "供应商" + supplierId + "的备注");
        supplier.put("created_at", "2025-01-01T00:00:00");
        supplier.put("updated_at", "2025-11-25T00:00:00");
        
        return ResponseData.success(supplier);
    }

    /**
     * 创建渠道供应商
     */
    @PostMapping("/tenants/{tenantId}/channels/{channelType}/suppliers")
    public ResponseData<Map<String, Object>> createChannelSupplier(
            @PathVariable Long tenantId,
            @PathVariable String channelType,
            @RequestBody Map<String, Object> request) {
        log.info("========== 创建渠道供应商，tenantId={}, channelType={}, request={} ==========", 
                tenantId, channelType, request);
        
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("id", System.currentTimeMillis());
        supplier.put("tenant_id", tenantId);
        supplier.put("channel_type", channelType);
        supplier.put("supplier_name", request.get("supplier_name") != null ? request.get("supplier_name") : request.get("supplierName"));
        supplier.put("api_url", request.get("api_url") != null ? request.get("api_url") : request.get("apiUrl"));
        supplier.put("api_key", request.get("api_key") != null ? request.get("api_key") : request.get("apiKey"));
        supplier.put("secret_key", request.get("secret_key") != null ? request.get("secret_key") : request.get("secretKey"));
        supplier.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        supplier.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", 0)));
        supplier.put("remark", request.get("remark"));
        supplier.put("created_at", new Date().toString());
        supplier.put("updated_at", new Date().toString());
        
        return ResponseData.success(supplier);
    }

    /**
     * 更新渠道供应商
     */
    @PutMapping("/{supplierId}")
    public ResponseData<Map<String, Object>> updateChannelSupplier(
            @PathVariable Long supplierId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新渠道供应商，supplierId={}, request={} ==========", supplierId, request);
        
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("id", supplierId);
        supplier.put("tenant_id", request.get("tenant_id") != null ? request.get("tenant_id") : request.get("tenantId"));
        supplier.put("channel_type", request.get("channel_type") != null ? request.get("channel_type") : request.get("channelType"));
        supplier.put("supplier_name", request.get("supplier_name") != null ? request.get("supplier_name") : request.getOrDefault("supplierName", "测试供应商" + supplierId));
        supplier.put("api_url", request.get("api_url") != null ? request.get("api_url") : request.get("apiUrl"));
        supplier.put("api_key", request.get("api_key") != null ? request.get("api_key") : request.get("apiKey"));
        supplier.put("secret_key", request.get("secret_key") != null ? request.get("secret_key") : request.get("secretKey"));
        supplier.put("is_active", request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        supplier.put("sort_order", request.getOrDefault("sort_order", request.getOrDefault("sortOrder", supplierId.intValue())));
        supplier.put("remark", request.get("remark"));
        supplier.put("updated_at", new Date().toString());
        
        return ResponseData.success(supplier);
    }

    /**
     * 删除渠道供应商
     */
    @DeleteMapping("/{supplierId}")
    public ResponseData<String> deleteChannelSupplier(@PathVariable Long supplierId) {
        log.info("========== 删除渠道供应商，supplierId={} ==========", supplierId);
        return ResponseData.success("删除成功");
    }

    /**
     * 批量更新供应商排序
     */
    @PutMapping("/batch-order")
    public ResponseData<String> batchUpdateSupplierOrder(@RequestBody Map<String, Object> request) {
        log.info("========== 批量更新供应商排序，request={} ==========", request);
        return ResponseData.success("排序更新成功");
    }
}

