package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.FieldDisplayConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 案件列表字段配置Controller - 使用文件存储（持久化）
 * 负责控台案件列表和IM端案件列表的字段配置
 * 
 * @author CCO Team
 * @since 2025-12-07
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/case-list-field-configs")
@RequiredArgsConstructor
public class CaseListFieldConfigController {

    /**
     * 列表端现在读取“原详情”场景数据：admin_case_detail / collector_case_detail
     */
    private static final String DEFAULT_SCENE = "admin_case_detail";
    private static final String ALT_SCENE = "collector_case_detail";

    private final FieldDisplayConfigService fieldDisplayConfigService;

    /**
     * 获取案件列表字段配置（现在从“原详情”场景读取：admin_case_detail / collector_case_detail）
     */
    @GetMapping
    public ResponseData<List<TenantFieldDisplayConfig>> getCaseListFieldConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String fieldKey
    ) {
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null && !sceneType.isEmpty() ? sceneType : DEFAULT_SCENE;
        if (!isValidListScene(finalSceneType)) {
            finalSceneType = DEFAULT_SCENE;
        }
        List<TenantFieldDisplayConfig> list = fieldDisplayConfigService.list(finalTenantId, finalSceneType, fieldKey);
        return ResponseData.success(list);
    }

    /**
     * 获取场景类型列表（仅列表相关场景）
     */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        List<Map<String, String>> sceneTypes = new java.util.ArrayList<>();
        sceneTypes.add(scene("admin_case_detail", "控台案件详情"));
        sceneTypes.add(scene("collector_case_detail", "IM端案件详情"));
        return ResponseData.success(sceneTypes);
    }

    /**
     * 批量创建或更新案件列表字段配置
     */
    @PostMapping("/batch")
    public ResponseData<String> batchCreateOrUpdateConfigs(@RequestBody FieldDisplayConfigDTO.BatchUpdate request) {
        if (request == null || request.getTenantId() == null || request.getSceneType() == null) {
            return ResponseData.error("参数不完整: tenantId 和 sceneType 不能为空");
        }
        String sceneType = request.getSceneType();
        if (!isValidListScene(sceneType)) {
            return ResponseData.error("场景类型必须是详情场景（admin_case_detail 或 collector_case_detail）");
        }
        fieldDisplayConfigService.batchUpdate(request);
        return ResponseData.success("批量保存成功");
    }

    /**
     * 复制场景配置
     */
    @PostMapping("/copy-scene")
    public ResponseData<String> copySceneConfig(@RequestBody Map<String, Object> request) {
        String fromScene = (String) request.get("from_scene");
        String toScene = (String) request.get("to_scene");
        Long tenantId = request.get("tenant_id") != null ? Long.valueOf(request.get("tenant_id").toString()) : null;
        if (fromScene == null || toScene == null || tenantId == null) {
            return ResponseData.error("参数不完整");
        }
        if (!isValidListScene(fromScene) || !isValidListScene(toScene)) {
            return ResponseData.error("场景类型必须是详情场景");
        }
        List<TenantFieldDisplayConfig> source = fieldDisplayConfigService.list(tenantId, fromScene, null);
        if (source == null || source.isEmpty()) {
            return ResponseData.error("源场景配置不存在");
        }
        // 简单拷贝
        for (TenantFieldDisplayConfig cfg : source) {
            FieldDisplayConfigDTO.Create dto = new FieldDisplayConfigDTO.Create();
            BeanUtils.copyProperties(cfg, dto);
            dto.setId(null);
            dto.setSceneType(toScene);
            dto.setSceneName(getSceneName(toScene));
            fieldDisplayConfigService.create(dto);
        }
        return ResponseData.success("复制成功");
    }

    /**
     * 获取可用字段选项
     */
    @GetMapping("/available-fields")
    public ResponseData<List<FieldDisplayConfigDTO.AvailableField>> getAvailableFields(
            @RequestParam(required = false) Long tenantId
    ) {
        List<FieldDisplayConfigDTO.AvailableField> fields = fieldDisplayConfigService.getAvailableFields(tenantId);
        return ResponseData.success(fields);
    }

    /**
     * 验证是否是有效的列表场景
     */
    private boolean isValidListScene(String sceneType) {
        return "admin_case_detail".equals(sceneType) || "collector_case_detail".equals(sceneType);
    }

    /**
     * 获取配置的排序顺序
     */
    private Integer getSortOrder(Map<String, Object> config) {
        Object sortOrderObj = config.get("sort_order");
        if (sortOrderObj == null) {
            return 0;
        }
        if (sortOrderObj instanceof Number) {
            return ((Number) sortOrderObj).intValue();
        }
        try {
            return Integer.valueOf(sortOrderObj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 生成Mock案件列表字段配置数据（当文件不存在时使用）
     */
    private List<Map<String, Object>> generateMockConfigs(Long tenantId, String sceneType) {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        String sceneName = getSceneName(sceneType);
        
        // 手机号不在列表中展示，但仍支持搜索功能(在搜索API中处理)
        String[] fieldKeys = {"case_code", "user_name", "loan_amount", "outstanding_amount", 
                             "overdue_days", "case_status", "due_date", "product_name", "app_name"};
        String[] fieldNames = {"案件编号", "客户", "贷款金额", "未还金额", 
                              "逾期天数", "案件状态", "到期日期", "产品名称", "App名称"};
        String[] fieldTypes = {"String", "String", "Decimal", "Decimal", 
                              "Integer", "Enum", "Date", "String", "String"};
        // 必须展示字段(不可配置隐藏): case_code, user_name, loan_amount, outstanding_amount, overdue_days, case_status, due_date
        boolean[] isRequired = {true, true, true, true, true, true, true, false, false};
        boolean[] isSearchable = {true, true, false, false, false, false, false, true, true};
        boolean[] isFilterable = {false, false, false, false, false, true, false, false, false};
        boolean[] isRangeSearchable = {false, false, true, true, true, false, true, false, false};
        String[] colorTypes = {"normal", "normal", "normal", "red", "red", "normal", "normal", "normal", "normal"};
        int[] widths = {180, 120, 120, 120, 100, 110, 120, 130, 130};
        
        for (int i = 0; i < fieldKeys.length; i++) {
            Map<String, Object> config = new HashMap<>();
            config.put("id", (long) (i + 1));
            config.put("tenant_id", String.valueOf(tenantId));
            config.put("scene_type", sceneType);
            config.put("scene_name", sceneName);
            config.put("field_key", fieldKeys[i]);
            config.put("field_name", fieldNames[i]);
            config.put("field_data_type", fieldTypes[i]);
            config.put("field_source", "standard");
            config.put("sort_order", i + 1);
            config.put("display_width", widths[i]);
            config.put("color_type", colorTypes[i]);
            config.put("color_rule", null);
            config.put("hide_rule", null);
            config.put("hide_for_queues", new ArrayList<>());
            config.put("hide_for_agencies", new ArrayList<>());
            config.put("hide_for_teams", new ArrayList<>());
            
            if ("Decimal".equals(fieldTypes[i]) && (fieldKeys[i].equals("loan_amount") || fieldKeys[i].equals("outstanding_amount"))) {
                Map<String, Object> formatRule = new HashMap<>();
                formatRule.put("format_type", "currency");
                formatRule.put("prefix", "¥");
                formatRule.put("suffix", "");
                config.put("format_rule", formatRule);
            } else {
                config.put("format_rule", null);
            }
            
            config.put("is_searchable", isSearchable[i]);
            config.put("is_filterable", isFilterable[i]);
            config.put("is_range_searchable", isRangeSearchable[i]);
            config.put("is_required", isRequired[i]);
            config.put("created_at", java.time.LocalDateTime.now().toString());
            config.put("updated_at", java.time.LocalDateTime.now().toString());
            config.put("created_by", "system");
            config.put("updated_by", null);
            
            configs.add(config);
        }
        
        return configs;
    }

    /**
     * 获取场景名称
     */
    private String getSceneName(String sceneType) {
        if (sceneType == null || sceneType.isEmpty()) {
            return "未知场景";
        }
        switch (sceneType) {
            case "admin_case_detail":
                return "控台案件详情";
            case "collector_case_detail":
                return "IM端案件详情";
            default:
                return sceneType;
        }
    }

    private Map<String, String> scene(String key, String name) {
        Map<String, String> m = new java.util.HashMap<>();
        m.put("key", key);
        m.put("name", name);
        m.put("description", name);
        return m;
    }
}

