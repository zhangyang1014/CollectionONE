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
        // C队列：未逾期，提前还款客户（-∞ ~ -1天）
        queues.add(createQueue(1L, tenantId, "C队列", "C", null, -1, 1, true));
        // S0队列：当日到期，需重点关注（0 ~ 0天）
        queues.add(createQueue(2L, tenantId, "S0队列", "S0", 0, 0, 2, true));
        // S1队列：轻度逾期，友好提醒（1 ~ 5天）
        queues.add(createQueue(3L, tenantId, "S1队列", "S1", 1, 5, 3, true));
        // L1队列：中度逾期，加强催收（6 ~ 90天）
        queues.add(createQueue(4L, tenantId, "L1队列", "L1", 6, 90, 4, true));
        // M1队列：重度逾期，专项处理（91 ~ +∞天）
        queues.add(createQueue(5L, tenantId, "M1队列", "M1", 91, null, 5, true));
        
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

    /**
     * 获取甲方字段JSON数据
     * GET /api/v1/tenants/{tenantId}/fields-json
     */
    @GetMapping("/{tenantId}/fields-json")
    public ResponseData<Map<String, Object>> getTenantFieldsJson(@PathVariable Long tenantId) {
        log.info("获取甲方字段JSON数据（Mock），tenantId={}", tenantId);
        
        // 生成Mock字段数据（基于标准字段）
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // 客户基础信息字段
        fields.add(createTenantField(1L, "用户编号", "user_id", "String", 1L, true, 1));
        fields.add(createTenantField(2L, "客户姓名", "user_name", "String", 1L, true, 2));
        fields.add(createTenantField(3L, "手机号", "mobile", "String", 1L, true, 3));
        fields.add(createTenantField(4L, "身份证号", "id_card", "String", 1L, false, 4));
        fields.add(createTenantField(5L, "地址", "address", "String", 1L, false, 5));
        
        // 联系方式字段
        fields.add(createTenantField(6L, "邮箱", "email", "String", 4L, false, 1));
        fields.add(createTenantField(7L, "微信号", "wechat", "String", 4L, false, 2));
        
        // 身份信息字段
        fields.add(createTenantField(8L, "性别", "gender", "Enum", 5L, false, 1, 
            Arrays.asList(
                createEnumValue("male", "男"),
                createEnumValue("female", "女")
            )));
        fields.add(createTenantField(9L, "年龄", "age", "Integer", 5L, false, 2));
        fields.add(createTenantField(10L, "职业", "occupation", "String", 5L, false, 3));
        
        // 贷款详情字段
        fields.add(createTenantField(11L, "贷款编号", "loan_id", "String", 2L, true, 1));
        fields.add(createTenantField(12L, "贷款金额", "loan_amount", "Decimal", 2L, true, 2));
        fields.add(createTenantField(13L, "放款日期", "loan_date", "Date", 2L, true, 3));
        fields.add(createTenantField(14L, "到期日期", "due_date", "Date", 2L, true, 4));
        fields.add(createTenantField(15L, "产品名称", "product_name", "String", 2L, false, 5));
        
        // 贷款基本信息字段
        fields.add(createTenantField(16L, "利率", "interest_rate", "Decimal", 6L, false, 1));
        fields.add(createTenantField(17L, "贷款期限", "loan_term", "Integer", 6L, false, 2));
        
        // 还款信息字段
        fields.add(createTenantField(18L, "已还金额", "repaid_amount", "Decimal", 7L, false, 1));
        fields.add(createTenantField(19L, "未还金额", "outstanding_amount", "Decimal", 7L, true, 2));
        fields.add(createTenantField(20L, "逾期天数", "overdue_days", "Integer", 7L, true, 3));
        fields.add(createTenantField(21L, "逾期金额", "overdue_amount", "Decimal", 7L, false, 4));
        
        // 催收信息字段
        fields.add(createTenantField(22L, "案件编号", "case_code", "String", 3L, true, 1));
        fields.add(createTenantField(23L, "案件状态", "case_status", "Enum", 3L, true, 2,
            Arrays.asList(
                createEnumValue("pending_repayment", "待还款"),
                createEnumValue("partial_repayment", "部分还款"),
                createEnumValue("normal_settlement", "正常结清"),
                createEnumValue("extension_settlement", "展期结清")
            )));
        fields.add(createTenantField(24L, "催员ID", "collector_id", "Integer", 3L, false, 3));
        fields.add(createTenantField(25L, "催员姓名", "collector_name", "String", 3L, false, 4));
        
        // 催收联系记录字段
        fields.add(createTenantField(26L, "最后联系时间", "last_contact_at", "Date", 8L, false, 1));
        fields.add(createTenantField(27L, "联系次数", "contact_count", "Integer", 8L, false, 2));
        fields.add(createTenantField(28L, "联系渠道", "contact_channel", "Enum", 8L, false, 3,
            Arrays.asList(
                createEnumValue("phone", "电话"),
                createEnumValue("sms", "短信"),
                createEnumValue("wechat", "微信"),
                createEnumValue("email", "邮箱")
            )));
        
        // 催收状态字段
        fields.add(createTenantField(29L, "催收阶段", "collection_stage", "Enum", 9L, false, 1,
            Arrays.asList(
                createEnumValue("early_stage", "早期阶段"),
                createEnumValue("middle_stage", "中期阶段"),
                createEnumValue("late_stage", "后期阶段")
            )));
        fields.add(createTenantField(30L, "下次跟进时间", "next_follow_up_at", "Date", 9L, false, 2));
        
        Map<String, Object> result = new HashMap<>();
        result.put("fetched_at", java.time.LocalDateTime.now().toString());
        result.put("fields", fields);
        
        return ResponseData.success(result);
    }
    
    /**
     * 创建甲方字段对象
     */
    private Map<String, Object> createTenantField(
            Long id,
            String fieldName,
            String fieldKey,
            String fieldType,
            Long fieldGroupId,
            Boolean isRequired,
            Integer sortOrder
    ) {
        return createTenantField(id, fieldName, fieldKey, fieldType, fieldGroupId, isRequired, sortOrder, null);
    }
    
    /**
     * 创建甲方字段对象（带枚举值）
     */
    private Map<String, Object> createTenantField(
            Long id,
            String fieldName,
            String fieldKey,
            String fieldType,
            Long fieldGroupId,
            Boolean isRequired,
            Integer sortOrder,
            List<Map<String, String>> enumValues
    ) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("field_name", fieldName);
        field.put("field_key", fieldKey);
        field.put("field_type", fieldType);
        field.put("field_group_id", fieldGroupId);
        field.put("is_required", isRequired);
        field.put("sort_order", sortOrder);
        field.put("enum_values", enumValues != null ? enumValues : new ArrayList<>());
        field.put("description", fieldName + "的描述");
        field.put("example_value", fieldType.equals("String") ? "示例值" : 
                  fieldType.equals("Integer") ? "100" : 
                  fieldType.equals("Decimal") ? "100.00" : null);
        return field;
    }
    
    /**
     * 创建枚举值
     */
    private Map<String, String> createEnumValue(String value, String label) {
        Map<String, String> enumValue = new HashMap<>();
        enumValue.put("value", value);
        enumValue.put("label", label);
        return enumValue;
    }
    
    /**
     * 获取甲方字段配置列表
     * GET /api/v1/tenants/{tenantId}/field-configs
     */
    @GetMapping("/{tenantId}/field-configs")
    public ResponseData<List<Map<String, Object>>> getTenantFieldConfigs(@PathVariable Long tenantId) {
        log.info("获取甲方字段配置列表（Mock），tenantId={}", tenantId);
        
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // 生成Mock字段配置数据
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> config = new HashMap<>();
            config.put("id", (long) i);
            config.put("tenant_id", tenantId);
            config.put("field_id", (long) i);
            config.put("field_type", i % 2 == 0 ? "standard" : "custom");
            config.put("is_enabled", true);
            config.put("is_required", i % 3 == 0);
            config.put("is_readonly", false);
            config.put("is_visible", true);
            config.put("sort_order", i);
            config.put("created_at", "2025-11-22T10:00:00");
            config.put("updated_at", "2025-11-22T10:00:00");
            configs.add(config);
        }
        
        return ResponseData.success(configs);
    }
    
    /**
     * 创建甲方字段配置
     * POST /api/v1/tenants/{tenantId}/field-configs
     */
    @PostMapping("/{tenantId}/field-configs")
    public ResponseData<Map<String, Object>> createTenantFieldConfig(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> request
    ) {
        log.info("创建甲方字段配置（Mock），tenantId={}, request={}", tenantId, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", System.currentTimeMillis());
        config.put("tenant_id", tenantId);
        config.put("field_id", request.get("field_id"));
        config.put("field_type", request.get("field_type"));
        config.put("is_enabled", request.getOrDefault("is_enabled", true));
        config.put("is_required", request.getOrDefault("is_required", false));
        config.put("is_readonly", request.getOrDefault("is_readonly", false));
        config.put("is_visible", request.getOrDefault("is_visible", true));
        config.put("sort_order", request.getOrDefault("sort_order", 0));
        config.put("created_at", java.time.LocalDateTime.now().toString());
        config.put("updated_at", java.time.LocalDateTime.now().toString());
        
        return ResponseData.success(config);
    }
    
    /**
     * 更新甲方字段配置
     * PUT /api/v1/tenants/{tenantId}/field-configs/{configId}
     */
    @PutMapping("/{tenantId}/field-configs/{configId}")
    public ResponseData<Map<String, Object>> updateTenantFieldConfig(
            @PathVariable Long tenantId,
            @PathVariable Long configId,
            @RequestBody Map<String, Object> request
    ) {
        log.info("更新甲方字段配置（Mock），tenantId={}, configId={}, request={}", tenantId, configId, request);
        
        Map<String, Object> config = new HashMap<>();
        config.put("id", configId);
        config.put("tenant_id", tenantId);
        config.put("field_id", request.getOrDefault("field_id", 1L));
        config.put("field_type", request.getOrDefault("field_type", "standard"));
        config.put("is_enabled", request.getOrDefault("is_enabled", true));
        config.put("is_required", request.getOrDefault("is_required", false));
        config.put("is_readonly", request.getOrDefault("is_readonly", false));
        config.put("is_visible", request.getOrDefault("is_visible", true));
        config.put("sort_order", request.getOrDefault("sort_order", 0));
        config.put("created_at", "2025-11-22T10:00:00");
        config.put("updated_at", java.time.LocalDateTime.now().toString());
        
        return ResponseData.success(config);
    }
    
    /**
     * 删除甲方字段配置
     * DELETE /api/v1/tenants/{tenantId}/field-configs/{configId}
     */
    @DeleteMapping("/{tenantId}/field-configs/{configId}")
    public ResponseData<Map<String, Object>> deleteTenantFieldConfig(
            @PathVariable Long tenantId,
            @PathVariable Long configId
    ) {
        log.info("删除甲方字段配置（Mock），tenantId={}, configId={}", tenantId, configId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "甲方字段配置删除成功");
        result.put("id", configId);
        
        return ResponseData.success(result);
    }
    
    /**
     * 获取未映射的甲方字段
     * GET /api/v1/tenants/{tenantId}/unmapped-fields
     */
    @GetMapping("/{tenantId}/unmapped-fields")
    public ResponseData<List<Map<String, Object>>> getUnmappedTenantFields(@PathVariable Long tenantId) {
        log.info("获取未映射的甲方字段（Mock），tenantId={}", tenantId);
        
        List<Map<String, Object>> unmappedFields = new ArrayList<>();
        
        // 生成一些未映射的字段示例
        unmappedFields.add(createUnmappedField("EXTRA_FIELD_1", "额外字段1", "String", false));
        unmappedFields.add(createUnmappedField("EXTRA_FIELD_2", "额外字段2", "Integer", false));
        unmappedFields.add(createUnmappedField("EXTRA_FIELD_3", "额外字段3", "Decimal", false));
        unmappedFields.add(createUnmappedField("EXTRA_FIELD_4", "额外字段4", "Date", false));
        unmappedFields.add(createUnmappedField("EXTRA_FIELD_5", "额外字段5", "Enum", false));
        
        return ResponseData.success(unmappedFields);
    }
    
    /**
     * 创建未映射字段对象
     */
    private Map<String, Object> createUnmappedField(
            String tenantFieldKey,
            String tenantFieldName,
            String fieldType,
            Boolean isRequired
    ) {
        Map<String, Object> field = new HashMap<>();
        field.put("tenant_field_key", tenantFieldKey);
        field.put("tenant_field_name", tenantFieldName);
        field.put("field_type", fieldType);
        field.put("is_required", isRequired);
        field.put("description", tenantFieldName + "的描述");
        return field;
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
                                           String code, Integer overdueDaysStart, 
                                           Integer overdueDaysEnd, Integer sortOrder, Boolean isActive) {
        Map<String, Object> queue = new HashMap<>();
        queue.put("id", id);
        queue.put("tenant_id", tenantId);
        queue.put("queue_name", name);
        queue.put("queue_code", code);
        queue.put("overdue_days_start", overdueDaysStart);  // null表示负无穷
        queue.put("overdue_days_end", overdueDaysEnd);      // null表示正无穷
        queue.put("sort_order", sortOrder);
        queue.put("is_active", isActive);
        queue.put("created_at", "2025-11-22T10:00:00");
        queue.put("updated_at", "2025-11-22T10:00:00");
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

