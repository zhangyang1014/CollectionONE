package com.cco.controller;

import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 字段展示配置Mock控制器
 * @deprecated 已替换为FieldDisplayConfigController，从数据库读取真实数据
 * 保留此类仅用于参考，不再使用
 */
@Deprecated
// @RestController  // 已禁用，使用真实的FieldDisplayConfigController
@RequestMapping("/api/v1/field-display-configs-deprecated")
public class MockFieldDisplayConfigController {
    
    /**
     * 获取字段展示配置列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getFieldDisplayConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType
    ) {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // 创建Mock配置数据
        String[] fieldKeys = {"case_code", "user_name", "mobile", "overdue_days", "outstanding_amount"};
        String[] fieldNames = {"案件编号", "客户姓名", "手机号", "逾期天数", "逾期金额"};
        String[] fieldTypes = {"String", "String", "String", "Integer", "Decimal"};
        
        // 根据场景类型设置场景名称
        String currentSceneType = sceneType != null ? sceneType : "collector_case_list";
        String sceneName = "催员案件列表";
        if ("admin_case_list".equals(currentSceneType)) {
            sceneName = "控台案件管理列表";
        } else if ("collector_case_list".equals(currentSceneType)) {
            sceneName = "催员案件列表";
        } else if ("collector_case_detail".equals(currentSceneType)) {
            sceneName = "催员案件详情";
        }
        
        for (int i = 0; i < fieldKeys.length; i++) {
            Map<String, Object> config = new HashMap<>();
            config.put("id", (long) (i + 1));
            config.put("tenant_id", String.valueOf(tenantId != null ? tenantId : 1L));
            config.put("scene_type", currentSceneType);
            config.put("scene_name", sceneName);
            config.put("field_key", fieldKeys[i]);
            config.put("field_name", fieldNames[i]);
            config.put("field_data_type", fieldTypes[i]);
            config.put("field_source", "standard");
            config.put("sort_order", i + 1);
            config.put("display_width", 150);
            config.put("color_type", "normal");
            config.put("color_rule", null);
            config.put("hide_rule", null);
            config.put("hide_for_queues", new ArrayList<>());
            config.put("hide_for_agencies", new ArrayList<>());
            config.put("hide_for_teams", new ArrayList<>());
            config.put("format_rule", null);
            config.put("is_searchable", fieldTypes[i].equals("String"));
            config.put("is_filterable", fieldTypes[i].equals("Enum"));
            config.put("is_range_searchable", fieldTypes[i].equals("Integer") || fieldTypes[i].equals("Decimal") || fieldTypes[i].equals("Date"));
            config.put("created_at", new Date().toString());
            config.put("updated_at", new Date().toString());
            
            configs.add(config);
        }
        
        return ResponseData.success(configs);
    }
    
    /**
     * 获取场景类型列表
     */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        List<Map<String, String>> sceneTypes = new ArrayList<>();
        
        Map<String, String> type1 = new HashMap<>();
        type1.put("key", "admin_case_list");
        type1.put("name", "控台案件管理列表");
        type1.put("description", "管理后台的案件列表页面");
        
        Map<String, String> type2 = new HashMap<>();
        type2.put("key", "collector_case_list");
        type2.put("name", "催员案件列表");
        type2.put("description", "催员端的案件列表页面");
        
        Map<String, String> type3 = new HashMap<>();
        type3.put("key", "collector_case_detail");
        type3.put("name", "催员案件详情");
        type3.put("description", "催员端的案件详情页面");
        
        sceneTypes.add(type1);
        sceneTypes.add(type2);
        sceneTypes.add(type3);
        
        return ResponseData.success(sceneTypes);
    }
    
    /**
     * 批量创建或更新字段展示配置
     */
    @PostMapping("/batch")
    public ResponseData<String> batchCreateOrUpdateConfigs(@RequestBody Map<String, Object> request) {
        // Mock实现：返回成功
        return ResponseData.success("批量保存成功");
    }
    
    /**
     * 复制场景配置
     */
    @PostMapping("/copy-scene")
    public ResponseData<String> copySceneConfig(@RequestBody Map<String, Object> request) {
        // Mock实现：返回成功
        return ResponseData.success("复制成功");
    }
    
    /**
     * 获取可用字段选项
     */
    @GetMapping("/available-fields")
    public ResponseData<List<Map<String, Object>>> getAvailableFields(
            @RequestParam(required = false) Long tenantId
    ) {
        List<Map<String, Object>> fields = new ArrayList<>();
        
        // 标准字段
        String[] standardKeys = {"case_code", "user_name", "mobile", "overdue_days", "outstanding_amount", 
                                 "loan_amount", "loan_date", "due_date", "settlement_date", "case_status"};
        String[] standardNames = {"案件编号", "客户姓名", "手机号", "逾期天数", "逾期金额", 
                                  "贷款金额", "放款日期", "到期日", "结清日期", "案件状态"};
        String[] standardTypes = {"String", "String", "String", "Integer", "Decimal", 
                                  "Decimal", "Date", "Date", "Date", "Enum"};
        
        for (int i = 0; i < standardKeys.length; i++) {
            Map<String, Object> field = new HashMap<>();
            field.put("field_key", standardKeys[i]);
            field.put("field_name", standardNames[i]);
            field.put("field_data_type", standardTypes[i]);
            field.put("field_source", "standard");
            field.put("group", "基础信息");
            fields.add(field);
        }
        
        return ResponseData.success(fields);
    }
}


