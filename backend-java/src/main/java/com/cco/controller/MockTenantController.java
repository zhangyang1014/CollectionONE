package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Mock甲方管理Controller（临时，用于前端开发）
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants")
public class MockTenantController {

    @GetMapping("")
    public ResponseData<List<Map<String, Object>>> getTenants(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("获取甲方列表（Mock），skip={}, limit={}", skip, limit);
        
        List<Map<String, Object>> tenants = new ArrayList<>();
        tenants.add(createTenant(1L, "百熵企业", "baishang", "百熵科技有限公司", "13800138001", true));
        tenants.add(createTenant(2L, "测试甲方A", "test_a", "测试公司A", "13800138002", true));
        tenants.add(createTenant(3L, "测试甲方B", "test_b", "测试公司B", "13800138003", true));
        
        return ResponseData.success(tenants);
    }

    @GetMapping("/{tenantId}")
    public ResponseData<Map<String, Object>> getTenant(@PathVariable Long tenantId) {
        log.info("获取甲方详情（Mock），tenantId={}", tenantId);
        
        Map<String, Object> tenant = createTenant(
            tenantId, 
            "百熵企业", 
            "baishang", 
            "百熵科技有限公司", 
            "13800138001", 
            true
        );
        
        return ResponseData.success(tenant);
    }

    @GetMapping("/{tenantId}/queues")
    public ResponseData<List<Map<String, Object>>> getTenantQueues(@PathVariable Long tenantId) {
        log.info("获取甲方队列列表（Mock），tenantId={}", tenantId);
        
        List<Map<String, Object>> queues = new ArrayList<>();
        queues.add(createQueue(1L, tenantId, "默认队列", "default", 1, true));
        queues.add(createQueue(2L, tenantId, "优先队列", "priority", 2, true));
        
        return ResponseData.success(queues);
    }

    @GetMapping("/{tenantId}/agencies")
    public ResponseData<List<Map<String, Object>>> getTenantAgencies(@PathVariable Long tenantId) {
        log.info("获取甲方机构列表（Mock），tenantId={}", tenantId);
        
        List<Map<String, Object>> agencies = new ArrayList<>();
        agencies.add(createAgency(1L, tenantId, "机构A", "agency_a", 1, true));
        agencies.add(createAgency(2L, tenantId, "机构B", "agency_b", 2, true));
        
        return ResponseData.success(agencies);
    }

    // Helper methods
    private Map<String, Object> createTenant(Long id, String name, String code, 
                                            String fullName, String contact, Boolean isActive) {
        Map<String, Object> tenant = new HashMap<>();
        tenant.put("id", id);
        tenant.put("tenant_name", name);
        tenant.put("tenant_code", code);
        tenant.put("full_name", fullName);
        tenant.put("contact_phone", contact);
        tenant.put("is_active", isActive);
        tenant.put("created_at", "2025-11-22T10:00:00");
        tenant.put("updated_at", "2025-11-22T10:00:00");
        return tenant;
    }

    private Map<String, Object> createQueue(Long id, Long tenantId, String name, 
                                           String code, Integer sortOrder, Boolean isActive) {
        Map<String, Object> queue = new HashMap<>();
        queue.put("id", id);
        queue.put("tenant_id", tenantId);
        queue.put("queue_name", name);
        queue.put("queue_code", code);
        queue.put("sort_order", sortOrder);
        queue.put("is_active", isActive);
        queue.put("created_at", "2025-11-22T10:00:00");
        return queue;
    }

    private Map<String, Object> createAgency(Long id, Long tenantId, String name, 
                                            String code, Integer sortOrder, Boolean isActive) {
        Map<String, Object> agency = new HashMap<>();
        agency.put("id", id);
        agency.put("tenant_id", tenantId);
        agency.put("agency_name", name);
        agency.put("agency_code", code);
        agency.put("sort_order", sortOrder);
        agency.put("is_active", isActive);
        agency.put("team_group_count", 2);
        agency.put("team_count", 5);
        agency.put("collector_count", 20);
        agency.put("created_at", "2025-11-22T10:00:00");
        return agency;
    }
}

