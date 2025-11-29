package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.entity.CaseQueue;
import com.cco.model.entity.Tenant;
import com.cco.model.entity.TenantAdmin;
import com.cco.service.QueueService;
import com.cco.service.TenantService;
import com.cco.service.TenantAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 甲方管理Controller - Mock实现
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/tenants")
public class TenantController {
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private TenantAdminService tenantAdminService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取甲方列表
     */
    @GetMapping
    public ResponseData<List<Tenant>> getTenants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        log.info("========== 获取甲方列表，keyword={}, isActive={} ==========", keyword, isActive);
        
        List<Tenant> tenants = tenantService.searchTenants(keyword, isActive);
        
        log.info("========== 返回甲方列表，数量={} ==========", tenants.size());
        return ResponseData.success(tenants);
    }

    /**
     * 获取甲方详情
     */
    @GetMapping("/{id}")
    public ResponseData<Tenant> getTenant(@PathVariable Long id) {
        log.info("========== 获取甲方详情，id={} ==========", id);
        
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return ResponseData.error(404, "甲方不存在");
        }
        
        return ResponseData.success(tenant);
    }

    /**
     * 创建甲方（同时创建甲方管理员账号）
     * 根据PRD要求，创建甲方时必须同时创建甲方管理员账号
     */
    @PostMapping
    @Transactional
    public ResponseData<Map<String, Object>> createTenant(@RequestBody Map<String, Object> request) {
        log.info("========== 创建甲方，request={} ==========", request);
        
        // 获取甲方编码
        String tenantCode = (String) (request.get("tenant_code") != null ? request.get("tenant_code") : request.get("tenantCode"));
        if (tenantCode == null || tenantCode.isEmpty()) {
            return ResponseData.error(400, "甲方编码不能为空");
        }
        
        // 检查甲方编码是否已存在
        if (tenantService.existsByTenantCode(tenantCode, null)) {
            return ResponseData.error(400, "甲方编码已存在：" + tenantCode);
        }
        
        // 创建甲方实体
        Tenant tenant = new Tenant();
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName((String) (request.get("tenant_name") != null ? request.get("tenant_name") : request.get("tenantName")));
        tenant.setTenantNameEn((String) (request.get("tenant_name_en") != null ? request.get("tenant_name_en") : request.get("tenantNameEn")));
        tenant.setCountryCode((String) (request.get("country_code") != null ? request.get("country_code") : request.get("countryCode")));
        Object timezoneObj = request.get("timezone");
        if (timezoneObj != null) {
            if (timezoneObj instanceof Integer) {
                tenant.setTimezone((Integer) timezoneObj);
            } else if (timezoneObj instanceof String) {
                // 如果是字符串，尝试解析
                try {
                    tenant.setTimezone(Integer.parseInt((String) timezoneObj));
                } catch (NumberFormatException e) {
                    log.warn("时区格式错误：{}", timezoneObj);
                }
            }
        }
        tenant.setCurrencyCode((String) (request.get("currency_code") != null ? request.get("currency_code") : request.get("currencyCode")));
        tenant.setIsActive((Boolean) request.getOrDefault("is_active", request.getOrDefault("isActive", true)));
        
        // 保存甲方
        tenantService.save(tenant);
        Long tenantId = tenant.getId();
        log.info("========== 甲方创建成功，tenantId={} ==========", tenantId);
        
        // 处理管理员信息（admin_info）
        Map<String, Object> adminInfo = null;
        if (request.containsKey("admin_info")) {
            adminInfo = (Map<String, Object>) request.get("admin_info");
        } else if (request.containsKey("adminInfo")) {
            adminInfo = (Map<String, Object>) request.get("adminInfo");
        }
        
        TenantAdmin admin = null;
        // 如果提供了管理员信息，同时创建管理员账号
        if (adminInfo != null && !adminInfo.isEmpty()) {
            String loginId = (String) (adminInfo.get("username") != null ? adminInfo.get("username") : adminInfo.get("login_id"));
            if (loginId == null || loginId.isEmpty()) {
                return ResponseData.error(400, "管理员登录ID不能为空");
            }
            
            // 检查登录ID是否已存在
            if (tenantAdminService.existsByLoginId(loginId, null)) {
                return ResponseData.error(400, "管理员登录ID已存在：" + loginId);
            }
            
            String password = (String) adminInfo.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseData.error(400, "管理员密码不能为空");
            }
            
            // 创建管理员实体
            admin = new TenantAdmin();
            admin.setTenantId(tenantId);
            admin.setAccountCode("TENANT_ADMIN_" + tenantId);
            admin.setAccountName((String) (adminInfo.get("name") != null ? adminInfo.get("name") : adminInfo.get("account_name")));
            admin.setLoginId(loginId);
            admin.setPasswordHash(passwordEncoder.encode(password)); // BCrypt加密
            admin.setEmail((String) adminInfo.get("email"));
            admin.setMobile((String) adminInfo.get("mobile"));
            admin.setIsActive(true);
            
            // 保存管理员
            tenantAdminService.save(admin);
            log.info("========== 甲方管理员创建成功，adminId={} ==========", admin.getId());
        }
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("id", tenant.getId());
        result.put("tenant_id", tenant.getId());
        result.put("tenant_code", tenant.getTenantCode());
        result.put("tenant_name", tenant.getTenantName());
        result.put("tenant_name_en", tenant.getTenantNameEn());
        result.put("country_code", tenant.getCountryCode());
        result.put("timezone", tenant.getTimezone());
        result.put("currency_code", tenant.getCurrencyCode());
        result.put("is_active", tenant.getIsActive());
        result.put("created_at", tenant.getCreatedAt());
        result.put("updated_at", tenant.getUpdatedAt());
        
        if (admin != null) {
            Map<String, Object> adminMap = new HashMap<>();
            adminMap.put("id", admin.getId());
            adminMap.put("tenant_id", admin.getTenantId());
            adminMap.put("account_code", admin.getAccountCode());
            adminMap.put("account_name", admin.getAccountName());
            adminMap.put("login_id", admin.getLoginId());
            adminMap.put("email", admin.getEmail());
            adminMap.put("is_active", admin.getIsActive());
            result.put("admin", adminMap);
        }
        
        return ResponseData.success(result);
    }

    /**
     * 更新甲方
     */
    @PutMapping("/{id}")
    public ResponseData<Tenant> updateTenant(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新甲方，id={}, request={} ==========", id, request);
        
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return ResponseData.error(404, "甲方不存在");
        }
        
        // 更新字段
        if (request.containsKey("tenant_name") || request.containsKey("tenantName")) {
            tenant.setTenantName((String) (request.get("tenant_name") != null ? request.get("tenant_name") : request.get("tenantName")));
        }
        if (request.containsKey("tenant_name_en") || request.containsKey("tenantNameEn")) {
            tenant.setTenantNameEn((String) (request.get("tenant_name_en") != null ? request.get("tenant_name_en") : request.get("tenantNameEn")));
        }
        if (request.containsKey("country_code") || request.containsKey("countryCode")) {
            tenant.setCountryCode((String) (request.get("country_code") != null ? request.get("country_code") : request.get("countryCode")));
        }
        if (request.containsKey("timezone")) {
            Object timezoneObj = request.get("timezone");
            if (timezoneObj instanceof Integer) {
                tenant.setTimezone((Integer) timezoneObj);
            }
        }
        if (request.containsKey("currency_code") || request.containsKey("currencyCode")) {
            tenant.setCurrencyCode((String) (request.get("currency_code") != null ? request.get("currency_code") : request.get("currencyCode")));
        }
        if (request.containsKey("is_active") || request.containsKey("isActive")) {
            tenant.setIsActive((Boolean) request.getOrDefault("is_active", request.get("isActive")));
        }
        
        tenantService.updateById(tenant);
        log.info("========== 甲方更新成功，id={} ==========", id);
        
        return ResponseData.success(tenant);
    }

    /**
     * 删除甲方（软删除：设置为禁用状态）
     */
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteTenant(@PathVariable Long id) {
        log.info("========== 删除甲方，id={} ==========", id);
        
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            return ResponseData.error(404, "甲方不存在");
        }
        
        // 软删除：设置为禁用状态
        tenant.setIsActive(false);
        tenantService.updateById(tenant);
        
        log.info("========== 甲方删除成功（已禁用），id={} ==========", id);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取甲方字段配置列表
     */
    @GetMapping("/{tenantId}/field-configs")
    public ResponseData<List<Map<String, Object>>> getTenantFieldConfigs(@PathVariable Long tenantId) {
        log.info("========== 获取甲方字段配置，tenantId={} ==========", tenantId);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        // Mock数据
        return ResponseData.success(configs);
    }

    /**
     * 创建甲方字段配置
     */
    @PostMapping("/{tenantId}/field-configs")
    public ResponseData<Map<String, Object>> createTenantFieldConfig(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 创建甲方字段配置，tenantId={}, request={} ==========", tenantId, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", System.currentTimeMillis());
        config.put("tenantId", tenantId);
        config.putAll(request);
        
        return ResponseData.success(config);
    }

    /**
     * 更新甲方字段配置
     */
    @PutMapping("/{tenantId}/field-configs/{configId}")
    public ResponseData<Map<String, Object>> updateTenantFieldConfig(
            @PathVariable Long tenantId,
            @PathVariable Long configId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新甲方字段配置，tenantId={}, configId={}, request={} ==========", tenantId, configId, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", configId);
        config.put("tenantId", tenantId);
        config.putAll(request);
        
        return ResponseData.success(config);
    }

    /**
     * 删除甲方字段配置
     */
    @DeleteMapping("/{tenantId}/field-configs/{configId}")
    public ResponseData<String> deleteTenantFieldConfig(
            @PathVariable Long tenantId,
            @PathVariable Long configId) {
        log.info("========== 删除甲方字段配置，tenantId={}, configId={} ==========", tenantId, configId);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取甲方未映射的字段列表
     */
    @GetMapping("/{tenantId}/unmapped-fields")
    public ResponseData<List<Map<String, Object>>> getUnmappedTenantFields(@PathVariable Long tenantId) {
        log.info("========== 获取甲方未映射字段，tenantId={} ==========", tenantId);
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // Mock数据 - 返回一些未映射的字段
        // 注意：字段名必须匹配前端期望的格式（tenant_field_name, tenant_field_key, tenant_updated_at）
        Map<String, Object> field1 = new HashMap<>();
        field1.put("id", 101L);
        field1.put("tenant_field_key", "TEXT_FIELD");
        field1.put("tenant_field_name", "文本字段");
        field1.put("field_type", "text");
        field1.put("field_group_id", 1L);
        field1.put("is_required", false);
        field1.put("tenant_updated_at", "2025-11-28T10:30:00Z");
        fields.add(field1);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("id", 102L);
        field2.put("tenant_field_key", "NUMBER_FIELD");
        field2.put("tenant_field_name", "数字字段");
        field2.put("field_type", "number");
        field2.put("field_group_id", 2L);
        field2.put("is_required", false);
        field2.put("tenant_updated_at", "2025-11-28T11:00:00Z");
        fields.add(field2);
        
        log.info("========== 返回未映射字段列表，数量={} ==========", fields.size());
        return ResponseData.success(fields);
    }

    /**
     * 获取甲方拓展字段列表（自定义字段）
     */
    @GetMapping("/{tenantId}/extended-fields")
    public ResponseData<List<Map<String, Object>>> getExtendedFields(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long field_group_id) {
        log.info("========== 获取甲方拓展字段列表，tenantId={}, field_group_id={} ==========", tenantId, field_group_id);
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // Mock数据 - 拓展字段（自定义字段）
        Map<String, Object> field1 = new HashMap<>();
        field1.put("id", 1L);
        field1.put("tenant_id", tenantId);
        field1.put("field_alias", "company_name");
        field1.put("tenant_field_key", "COMPANY_NAME");
        field1.put("tenant_field_name", "公司名称");
        field1.put("field_type", "String");
        field1.put("field_group_id", field_group_id != null ? field_group_id : 1L);
        field1.put("privacy_label", "公开");
        field1.put("is_required", false);
        field1.put("created_at", "2025-01-01T00:00:00");
        field1.put("updated_at", "2025-11-25T00:00:00");
        fields.add(field1);
        
        Map<String, Object> field2 = new HashMap<>();
        field2.put("id", 2L);
        field2.put("tenant_id", tenantId);
        field2.put("field_alias", "employee_id");
        field2.put("tenant_field_key", "EMPLOYEE_ID");
        field2.put("tenant_field_name", "员工编号");
        field2.put("field_type", "String");
        field2.put("field_group_id", field_group_id != null ? field_group_id : 1L);
        field2.put("privacy_label", "敏感");
        field2.put("is_required", false);
        field2.put("created_at", "2025-01-02T00:00:00");
        field2.put("updated_at", "2025-11-25T00:00:00");
        fields.add(field2);
        
        Map<String, Object> field3 = new HashMap<>();
        field3.put("id", 3L);
        field3.put("tenant_id", tenantId);
        field3.put("field_alias", "income_amount");
        field3.put("tenant_field_key", "INCOME_AMOUNT");
        field3.put("tenant_field_name", "收入金额");
        field3.put("field_type", "Decimal");
        field3.put("field_group_id", field_group_id != null ? field_group_id : 2L);
        field3.put("privacy_label", "敏感");
        field3.put("is_required", false);
        field3.put("created_at", "2025-01-03T00:00:00");
        field3.put("updated_at", "2025-11-25T00:00:00");
        fields.add(field3);
        
        // 如果指定了分组ID，进行过滤
        if (field_group_id != null) {
            fields.removeIf(f -> !field_group_id.equals(f.get("field_group_id")));
        }
        
        log.info("========== 返回拓展字段列表，数量={} ==========", fields.size());
        return ResponseData.success(fields);
    }

    /**
     * 创建甲方拓展字段
     */
    @PostMapping("/{tenantId}/extended-fields")
    public ResponseData<Map<String, Object>> createExtendedField(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> request) {
        log.info("========== 创建甲方拓展字段，tenantId={}, request={} ==========", tenantId, request);
        
        Map<String, Object> field = new HashMap<>();
        field.put("id", System.currentTimeMillis());
        field.put("tenant_id", tenantId);
        field.put("field_alias", request.get("field_alias"));
        field.put("tenant_field_key", request.get("tenant_field_key"));
        field.put("tenant_field_name", request.get("tenant_field_name"));
        field.put("field_type", request.get("field_type"));
        field.put("field_group_id", request.get("field_group_id") != null ? request.get("field_group_id") : request.get("field_group_path"));
        field.put("privacy_label", request.get("privacy_label"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("created_at", new Date().toString());
        field.put("updated_at", new Date().toString());
        
        return ResponseData.success(field);
    }

    /**
     * 更新甲方拓展字段
     */
    @PutMapping("/{tenantId}/extended-fields/{id}")
    public ResponseData<Map<String, Object>> updateExtendedField(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("========== 更新甲方拓展字段，tenantId={}, id={}, request={} ==========", tenantId, id, request);
        
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("tenant_id", tenantId);
        field.put("field_alias", request.getOrDefault("field_alias", "field_" + id));
        field.put("tenant_field_key", request.getOrDefault("tenant_field_key", "FIELD_" + id));
        field.put("tenant_field_name", request.getOrDefault("tenant_field_name", "拓展字段" + id));
        field.put("field_type", request.getOrDefault("field_type", "String"));
        field.put("field_group_id", request.get("field_group_id") != null ? request.get("field_group_id") : request.get("field_group_path"));
        field.put("privacy_label", request.getOrDefault("privacy_label", "公开"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("updated_at", new Date().toString());
        
        return ResponseData.success(field);
    }

    /**
     * 删除甲方拓展字段
     */
    @DeleteMapping("/{tenantId}/extended-fields/{id}")
    public ResponseData<String> deleteExtendedField(
            @PathVariable Long tenantId,
            @PathVariable Long id) {
        log.info("========== 删除甲方拓展字段，tenantId={}, id={} ==========", tenantId, id);
        return ResponseData.success("删除成功");
    }

    /**
     * 获取指定甲方的机构列表
     */
    @GetMapping("/{tenantId}/agencies")
    public ResponseData<List<Map<String, Object>>> getTenantAgencies(@PathVariable Long tenantId) {
        log.info("========== 获取甲方机构列表，tenantId={} ==========", tenantId);
        
        List<Map<String, Object>> agencies = new ArrayList<>();
        
        // Mock数据
        Map<String, Object> agency1 = new HashMap<>();
        agency1.put("id", 1L);
        agency1.put("tenant_id", tenantId);
        agency1.put("agency_code", "AGENCY001");
        agency1.put("agency_name", "测试机构1");
        agency1.put("agency_name_en", "Test Agency 1");
        agency1.put("contact_person", "张三");
        agency1.put("contact_phone", "13800138001");
        agency1.put("contact_email", "agency1@test.com");
        agency1.put("address", "北京市朝阳区");
        agency1.put("description", "测试机构1的描述");
        agency1.put("agency_type", "real");
        agency1.put("timezone", 8);
        agency1.put("sort_order", 1);
        agency1.put("is_active", true);
        agency1.put("team_count", 2);
        agency1.put("collector_count", 10);
        agency1.put("case_count", 100);
        agency1.put("created_at", "2025-01-01T00:00:00");
        agency1.put("updated_at", "2025-11-25T00:00:00");
        agencies.add(agency1);
        
        Map<String, Object> agency2 = new HashMap<>();
        agency2.put("id", 2L);
        agency2.put("tenant_id", tenantId);
        agency2.put("agency_code", "AGENCY002");
        agency2.put("agency_name", "测试机构2");
        agency2.put("agency_name_en", "Test Agency 2");
        agency2.put("contact_person", "李四");
        agency2.put("contact_phone", "13800138002");
        agency2.put("contact_email", "agency2@test.com");
        agency2.put("address", "上海市浦东新区");
        agency2.put("description", "测试机构2的描述");
        agency2.put("agency_type", "real");
        agency2.put("timezone", 8);
        agency2.put("sort_order", 2);
        agency2.put("is_active", true);
        agency2.put("team_count", 3);
        agency2.put("collector_count", 15);
        agency2.put("case_count", 150);
        agency2.put("created_at", "2025-01-02T00:00:00");
        agency2.put("updated_at", "2025-11-25T00:00:00");
        agencies.add(agency2);
        
        log.info("========== 返回甲方机构列表，数量={} ==========", agencies.size());
        return ResponseData.success(agencies);
    }

    /**
     * 获取指定甲方的队列列表
     */
    @GetMapping("/{tenantId}/queues")
    public ResponseData<List<Map<String, Object>>> getTenantQueues(@PathVariable Long tenantId) {
        log.info("========== 获取甲方队列列表，tenantId={} ==========", tenantId);
        
        // 从数据库查询该甲方的队列列表
        List<CaseQueue> queues = queueService.listByTenantId(tenantId, true);
        
        // 转换为Map格式（保持前端兼容）
        List<Map<String, Object>> result = queues.stream()
                .map(this::convertQueueToMap)
                .collect(Collectors.toList());
        
        log.info("========== 返回甲方队列列表，数量={} ==========", result.size());
        return ResponseData.success(result);
    }
    
    /**
     * 将CaseQueue实体转换为Map（保持前端兼容）
     */
    private Map<String, Object> convertQueueToMap(CaseQueue queue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", queue.getId());
        map.put("tenant_id", queue.getTenantId());
        map.put("queue_code", queue.getQueueCode());
        map.put("queue_name", queue.getQueueName());
        map.put("queue_name_en", queue.getQueueNameEn());
        map.put("queue_description", queue.getQueueDescription());
        map.put("overdue_days_start", queue.getOverdueDaysStart());
        map.put("overdue_days_end", queue.getOverdueDaysEnd());
        map.put("sort_order", queue.getSortOrder());
        map.put("is_active", queue.getIsActive());
        map.put("case_count", 0);  // TODO: 统计案件数量
        if (queue.getCreatedAt() != null) {
            map.put("created_at", queue.getCreatedAt().toString());
        }
        if (queue.getUpdatedAt() != null) {
            map.put("updated_at", queue.getUpdatedAt().toString());
        }
        return map;
    }
}

