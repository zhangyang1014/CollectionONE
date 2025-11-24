package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 标准字段Mock控制器
 * 提供标准字段相关API的Mock数据
 */
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/standard-fields")
public class MockStandardFieldController {

    /**
     * 获取标准字段列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getStandardFields(
            @RequestParam(required = false, defaultValue = "0") Integer skip,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(value = "field_group_id", required = false) Long fieldGroupId,
            @RequestParam(value = "include_deleted", required = false) String includeDeletedStr
    ) {
        // 处理include_deleted参数（可能是字符串"true"/"false"或布尔值）
        Boolean includeDeleted = false;
        if (includeDeletedStr != null) {
            includeDeleted = Boolean.parseBoolean(includeDeletedStr);
        }
        System.out.println("===============================================");
        System.out.println("[标准字段API] 接收参数:");
        System.out.println("  skip = " + skip);
        System.out.println("  limit = " + limit);
        System.out.println("  fieldGroupId = " + fieldGroupId);
        System.out.println("  includeDeleted = " + includeDeleted);
        System.out.println("===============================================");
        
        List<Map<String, Object>> allFields = generateMockFields();
        
        // 根据field_group_id过滤
        if (fieldGroupId != null) {
            allFields = allFields.stream()
                    .filter(field -> fieldGroupId.equals(field.get("field_group_id")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 过滤已删除的字段
        if (!includeDeleted) {
            allFields = allFields.stream()
                    .filter(field -> !Boolean.TRUE.equals(field.get("is_deleted")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 按sort_order排序
        allFields.sort((a, b) -> {
            Integer orderA = (Integer) a.getOrDefault("sort_order", 0);
            Integer orderB = (Integer) b.getOrDefault("sort_order", 0);
            return orderA.compareTo(orderB);
        });
        
        // 应用分页
        int start = Math.min(skip, allFields.size());
        int end = Math.min(skip + limit, allFields.size());
        List<Map<String, Object>> pagedFields = allFields.subList(start, end);
        
        return ResponseData.success(pagedFields);
    }
    
    /**
     * 生成Mock标准字段数据
     */
    private List<Map<String, Object>> generateMockFields() {
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // 客户基础信息分组 (group_id = 1)
        fields.add(createField(1L, "user_id", "用户编号", "User ID", "String", 1L, true, false, "用户的唯一标识", "USER001", 1));
        fields.add(createField(2L, "user_name", "客户姓名", "Customer Name", "String", 1L, true, false, "客户的真实姓名", "张三", 2));
        fields.add(createField(3L, "mobile", "手机号", "Mobile Number", "String", 1L, true, false, "客户的手机号码", "13800138000", 3));
        fields.add(createField(4L, "id_card", "身份证号", "ID Card Number", "String", 1L, false, false, "客户的身份证号码", "320101199001010001", 4));
        fields.add(createField(5L, "address", "地址", "Address", "String", 1L, false, false, "客户的居住地址", "江苏省南京市玄武区", 5));
        
        // 联系方式子分组 (group_id = 15) - 基于CSV配置文件，同时保留前端需要的字段
        fields.add(createField(6L, "mobile_number", "手机号码", "Mobile Number", "String", 15L, false, false, "用户注册手机号", "+63 9123456789", 1));
        fields.add(createField(7L, "company_phone", "公司联系电话", "Company Phone", "String", 15L, false, false, "公司电话", "+63 2 9123456", 2));
        // 保留email字段以兼容前端显示（虽然CSV中没有，但前端需要）
        fields.add(createField(8L, "email", "邮箱", "Email", "String", 15L, false, false, "客户的邮箱地址", "test@example.com", 3));
        
        // 身份信息子分组 (group_id = 5)
        fields.add(createField(9L, "gender", "性别", "Gender", "Enum", 5L, false, false, "客户的性别", null, 1));
        fields.add(createField(10L, "age", "年龄", "Age", "Integer", 5L, false, false, "客户的年龄", "30", 2));
        fields.add(createField(11L, "occupation", "职业", "Occupation", "String", 5L, false, false, "客户的职业", "工程师", 3));
        
        // 贷款详情分组 (group_id = 2)
        fields.add(createField(12L, "loan_id", "贷款编号", "Loan ID", "String", 2L, true, false, "贷款的唯一标识", "LOAN001", 1));
        fields.add(createField(13L, "loan_amount", "贷款金额", "Loan Amount", "Decimal", 2L, true, false, "贷款的总额", "50000.00", 2));
        fields.add(createField(14L, "loan_date", "放款日期", "Loan Date", "Date", 2L, true, false, "贷款的放款日期", "2024-01-01", 3));
        fields.add(createField(15L, "due_date", "到期日期", "Due Date", "Date", 2L, true, false, "贷款的到期日期", "2024-12-31", 4));
        fields.add(createField(16L, "product_name", "产品名称", "Product Name", "String", 2L, false, false, "贷款产品名称", "信用贷款", 5));
        
        // 贷款基本信息子分组 (group_id = 6)
        fields.add(createField(17L, "interest_rate", "利率", "Interest Rate", "Decimal", 6L, false, false, "贷款利率", "0.12", 1));
        fields.add(createField(18L, "loan_term", "贷款期限", "Loan Term", "Integer", 6L, false, false, "贷款期限（月）", "12", 2));
        
        // 还款信息子分组 (group_id = 7)
        fields.add(createField(19L, "repaid_amount", "已还金额", "Repaid Amount", "Decimal", 7L, false, false, "已还款金额", "25000.00", 1));
        fields.add(createField(20L, "outstanding_amount", "未还金额", "Outstanding Amount", "Decimal", 7L, true, false, "未还款金额", "25000.00", 2));
        fields.add(createField(21L, "overdue_days", "逾期天数", "Overdue Days", "Integer", 7L, true, false, "逾期天数", "30", 3));
        fields.add(createField(22L, "overdue_amount", "逾期金额", "Overdue Amount", "Decimal", 7L, false, false, "逾期金额", "5000.00", 4));
        
        // 催收信息分组 (group_id = 3)
        fields.add(createField(23L, "case_code", "案件编号", "Case Code", "String", 3L, true, false, "案件的唯一编号", "CASE001", 1));
        fields.add(createField(24L, "case_status", "案件状态", "Case Status", "Enum", 3L, true, false, "案件的当前状态", null, 2));
        fields.add(createField(25L, "collector_id", "催员ID", "Collector ID", "Integer", 3L, false, false, "负责催收的催员ID", "1", 3));
        fields.add(createField(26L, "collector_name", "催员姓名", "Collector Name", "String", 3L, false, false, "负责催收的催员姓名", "催员1", 4));
        
        // 催收联系记录子分组 (group_id = 8)
        fields.add(createField(27L, "last_contact_at", "最后联系时间", "Last Contact Time", "Date", 8L, false, false, "最后一次联系客户的时间", "2024-11-20 10:00:00", 1));
        fields.add(createField(28L, "contact_count", "联系次数", "Contact Count", "Integer", 8L, false, false, "联系客户的次数", "5", 2));
        fields.add(createField(29L, "contact_channel", "联系渠道", "Contact Channel", "Enum", 8L, false, false, "联系客户的渠道", null, 3));
        
        // 催收状态子分组 (group_id = 9)
        fields.add(createField(30L, "collection_stage", "催收阶段", "Collection Stage", "Enum", 9L, false, false, "当前催收阶段", null, 1));
        fields.add(createField(31L, "next_follow_up_at", "下次跟进时间", "Next Follow Up Time", "Date", 9L, false, false, "下次跟进的时间", "2024-11-25 14:00:00", 2));
        
        return fields;
    }
    
    /**
     * 创建标准字段对象
     */
    private Map<String, Object> createField(
            Long id,
            String fieldKey,
            String fieldName,
            String fieldNameEn,
            String fieldType,
            Long fieldGroupId,
            Boolean isRequired,
            Boolean isExtended,
            String description,
            String exampleValue,
            Integer sortOrder
    ) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", id);
        field.put("field_key", fieldKey);
        field.put("field_name", fieldName);
        field.put("field_name_en", fieldNameEn);
        field.put("field_type", fieldType);
        field.put("field_group_id", fieldGroupId);
        field.put("is_required", isRequired);
        field.put("is_extended", isExtended);
        field.put("description", description);
        field.put("example_value", exampleValue);
        field.put("validation_rules", new HashMap<>());
        
        // 如果是Enum类型，添加枚举选项
        if ("Enum".equals(fieldType)) {
            List<Map<String, Object>> enumOptions = new ArrayList<>();
            if ("gender".equals(fieldKey)) {
                enumOptions.add(createEnumOption("male", "男"));
                enumOptions.add(createEnumOption("female", "女"));
            } else if ("case_status".equals(fieldKey)) {
                enumOptions.add(createEnumOption("pending_repayment", "待还款"));
                enumOptions.add(createEnumOption("partial_repayment", "部分还款"));
                enumOptions.add(createEnumOption("normal_settlement", "正常结清"));
                enumOptions.add(createEnumOption("extension_settlement", "展期结清"));
            } else if ("contact_channel".equals(fieldKey)) {
                enumOptions.add(createEnumOption("phone", "电话"));
                enumOptions.add(createEnumOption("sms", "短信"));
                enumOptions.add(createEnumOption("wechat", "微信"));
                enumOptions.add(createEnumOption("email", "邮箱"));
            } else if ("collection_stage".equals(fieldKey)) {
                enumOptions.add(createEnumOption("early_stage", "早期阶段"));
                enumOptions.add(createEnumOption("middle_stage", "中期阶段"));
                enumOptions.add(createEnumOption("late_stage", "后期阶段"));
            }
            field.put("enum_options", enumOptions);
        } else {
            field.put("enum_options", null);
        }
        
        field.put("sort_order", sortOrder);
        field.put("is_active", true);
        field.put("is_deleted", false);
        field.put("deleted_at", null);
        field.put("created_at", LocalDateTime.now().minusDays(30).toString());
        field.put("updated_at", LocalDateTime.now().toString());
        
        return field;
    }
    
    /**
     * 创建枚举选项
     */
    private Map<String, Object> createEnumOption(String value, String label) {
        Map<String, Object> option = new HashMap<>();
        option.put("value", value);
        option.put("label", label);
        return option;
    }
    
    /**
     * 获取单个标准字段
     */
    @GetMapping("/{id}")
    public ResponseData<Map<String, Object>> getStandardField(@PathVariable Long id) {
        List<Map<String, Object>> allFields = generateMockFields();
        Map<String, Object> field = allFields.stream()
                .filter(f -> id.equals(f.get("id")))
                .findFirst()
                .orElse(null);
        
        if (field == null) {
            return ResponseData.error(404, "标准字段不存在");
        }
        
        return ResponseData.success(field);
    }
    
    /**
     * 创建标准字段
     */
    @PostMapping
    public ResponseData<Map<String, Object>> createStandardField(@RequestBody Map<String, Object> request) {
        Map<String, Object> field = new HashMap<>();
        field.put("id", System.currentTimeMillis());
        field.put("field_key", request.get("field_key"));
        field.put("field_name", request.get("field_name"));
        field.put("field_name_en", request.get("field_name_en"));
        field.put("field_type", request.get("field_type"));
        field.put("field_group_id", request.get("field_group_id"));
        field.put("is_required", request.getOrDefault("is_required", false));
        field.put("is_extended", request.getOrDefault("is_extended", false));
        field.put("description", request.get("description"));
        field.put("example_value", request.get("example_value"));
        field.put("validation_rules", request.getOrDefault("validation_rules", new HashMap<>()));
        field.put("enum_options", request.get("enum_options"));
        field.put("sort_order", request.getOrDefault("sort_order", 0));
        field.put("is_active", request.getOrDefault("is_active", true));
        field.put("is_deleted", false);
        field.put("deleted_at", null);
        field.put("created_at", LocalDateTime.now().toString());
        field.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(field);
    }
    
    /**
     * 更新标准字段
     */
    @PutMapping("/{id}")
    public ResponseData<Map<String, Object>> updateStandardField(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        List<Map<String, Object>> allFields = generateMockFields();
        Map<String, Object> field = allFields.stream()
                .filter(f -> id.equals(f.get("id")))
                .findFirst()
                .orElse(new HashMap<>());
        
        // 更新字段
        if (request.containsKey("field_name")) {
            field.put("field_name", request.get("field_name"));
        }
        if (request.containsKey("field_name_en")) {
            field.put("field_name_en", request.get("field_name_en"));
        }
        if (request.containsKey("field_type")) {
            field.put("field_type", request.get("field_type"));
        }
        if (request.containsKey("field_group_id")) {
            field.put("field_group_id", request.get("field_group_id"));
        }
        if (request.containsKey("is_required")) {
            field.put("is_required", request.get("is_required"));
        }
        if (request.containsKey("is_extended")) {
            field.put("is_extended", request.get("is_extended"));
        }
        if (request.containsKey("description")) {
            field.put("description", request.get("description"));
        }
        if (request.containsKey("example_value")) {
            field.put("example_value", request.get("example_value"));
        }
        if (request.containsKey("validation_rules")) {
            field.put("validation_rules", request.get("validation_rules"));
        }
        if (request.containsKey("enum_options")) {
            field.put("enum_options", request.get("enum_options"));
        }
        if (request.containsKey("sort_order")) {
            field.put("sort_order", request.get("sort_order"));
        }
        if (request.containsKey("is_active")) {
            field.put("is_active", request.get("is_active"));
        }
        
        field.put("updated_at", LocalDateTime.now().toString());
        
        return ResponseData.success(field);
    }
    
    /**
     * 删除标准字段（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseData<Map<String, Object>> deleteStandardField(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "标准字段删除成功");
        result.put("id", id);
        
        return ResponseData.success(result);
    }
}

