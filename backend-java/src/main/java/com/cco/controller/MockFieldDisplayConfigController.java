package com.cco.controller;

import com.cco.common.response.ResponseData;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 字段展示配置Mock控制器
 */
@RestController
@RequestMapping("/api/v1/field-display-configs")
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
        
        for (int i = 0; i < fieldKeys.length; i++) {
            Map<String, Object> config = new HashMap<>();
            config.put("id", (long) (i + 1));
            config.put("tenant_id", tenantId != null ? tenantId : 1L);
            config.put("scene_type", sceneType != null ? sceneType : "collector_case_list");
            config.put("scene_name", "催员案件列表");
            config.put("field_key", fieldKeys[i]);
            config.put("field_name", fieldNames[i]);
            config.put("field_data_type", fieldTypes[i]);
            config.put("field_source", "standard");
            config.put("sort_order", i + 1);
            config.put("display_width", 150);
            config.put("is_enabled", true);
            config.put("is_searchable", false);
            config.put("is_filterable", false);
            
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
}


