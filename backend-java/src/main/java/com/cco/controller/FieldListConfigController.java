package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 案件列表字段配置Controller - 使用文件存储（持久化）
 * 场景覆盖：
 *  - admin_case_list    控台案件列表
 *  - collector_case_list IM端案件列表
 */
@Slf4j
@RestController
@Deprecated // 保留旧实现，避免与新版 CaseListFieldConfigController 路由冲突
@RequestMapping(Constants.API_V1_PREFIX + "/legacy-case-list-field-configs")
public class FieldListConfigController {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 获取存储文件路径 */
    private String getStorageFilePath(Long tenantId, String sceneType) {
        return STORAGE_DIR + "/case-list-field-configs_" + tenantId + "_" + sceneType + ".json";
    }

    /** 从文件读取配置 */
    private List<Map<String, Object>> loadFromFile(Long tenantId, String sceneType) {
        try {
            String filePath = getStorageFilePath(tenantId, sceneType);
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            List<Map<String, Object>> configs = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            log.info("从文件加载案件列表字段配置，tenantId={}, sceneType={}, size={}", tenantId, sceneType, configs.size());
            return configs;
        } catch (Exception e) {
            log.warn("从文件加载案件列表字段配置失败: {}", e.getMessage());
            return null;
        }
    }

    /** 保存配置到文件 */
    private void saveToFile(Long tenantId, String sceneType, List<Map<String, Object>> configs) {
        try {
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = getStorageFilePath(tenantId, sceneType);
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configs);
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }
            log.info("案件列表字段配置已保存，tenantId={}, sceneType={}, size={}, path={}", tenantId, sceneType, configs.size(), filePath);
        } catch (Exception e) {
            log.error("保存案件列表字段配置失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存配置失败: " + e.getMessage());
        }
    }

    /** 获取案件列表字段配置 */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getCaseListFieldConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String fieldKey
    ) {
        log.info("获取案件列表字段配置 tenantId={}, sceneType={}, fieldKey={}", tenantId, sceneType, fieldKey);
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null ? sceneType : "admin_case_list";
        if (!isValidListScene(finalSceneType)) {
            log.warn("无效的列表场景类型: {}", finalSceneType);
            finalSceneType = "admin_case_list";
        }
        try {
            List<Map<String, Object>> configs = loadFromFile(finalTenantId, finalSceneType);
            if (configs == null || configs.isEmpty()) {
                configs = generateMockConfigs(finalTenantId, finalSceneType);
                saveToFile(finalTenantId, finalSceneType, configs);
            } else {
                configs.sort(Comparator.comparingInt(this::getSortOrder));
            }
            return ResponseData.success(configs);
        } catch (Exception e) {
            log.error("获取案件列表字段配置失败", e);
            try {
                List<Map<String, Object>> mockConfigs = generateMockConfigs(finalTenantId, finalSceneType);
                return ResponseData.success(mockConfigs);
            } catch (Exception mockError) {
                log.error("生成Mock数据失败", mockError);
                return ResponseData.success(new ArrayList<>());
            }
        }
    }

    /** 获取列表场景类型 */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        List<Map<String, String>> sceneTypes = new ArrayList<>();
        Map<String, String> type1 = new HashMap<>();
        type1.put("key", "admin_case_list");
        type1.put("name", "控台案件列表");
        type1.put("description", "管理后台的案件列表页面");

        Map<String, String> type2 = new HashMap<>();
        type2.put("key", "collector_case_list");
        type2.put("name", "IM端案件列表");
        type2.put("description", "催员端的案件列表页面");

        sceneTypes.add(type1);
        sceneTypes.add(type2);
        return ResponseData.success(sceneTypes);
    }

    /** 批量创建或更新 */
    @PostMapping("/batch")
    public ResponseData<String> batchCreateOrUpdateConfigs(@RequestBody Map<String, Object> request) {
        log.info("批量创建或更新案件列表字段配置，request={}", request);
        try {
            Object tenantIdObj = request.get("tenant_id");
            Long tenantId = tenantIdObj != null ? Long.valueOf(tenantIdObj.toString()) : null;
            String sceneType = (String) request.get("scene_type");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> configsList = (List<Map<String, Object>>) request.get("configs");
            if (tenantId == null || sceneType == null || sceneType.isEmpty()) {
                return ResponseData.error("参数不完整: tenantId和sceneType不能为空");
            }
            if (!isValidListScene(sceneType)) {
                return ResponseData.error("场景类型必须是列表相关场景（admin_case_list或collector_case_list）");
            }
            if (configsList == null || configsList.isEmpty()) {
                return ResponseData.success("批量保存成功（无配置需要保存）");
            }
            List<Map<String, Object>> savedConfigs = new ArrayList<>();
            for (Map<String, Object> configMap : configsList) {
                Map<String, Object> saved = new HashMap<>(configMap);
                if (saved.containsKey("sort_order")) {
                    Object sortOrderObj = saved.get("sort_order");
                    try {
                        saved.put("sort_order", Integer.valueOf(sortOrderObj.toString()));
                    } catch (Exception ignore) {
                        saved.put("sort_order", 0);
                    }
                }
                savedConfigs.add(saved);
            }
            savedConfigs.sort(Comparator.comparingInt(this::getSortOrder));
            saveToFile(tenantId, sceneType, savedConfigs);
            return ResponseData.success("批量保存成功");
        } catch (Exception e) {
            log.error("批量保存失败", e);
            return ResponseData.error("批量保存失败: " + e.getMessage());
        }
    }

    /** 复制场景配置 */
    @PostMapping("/copy-scene")
    public ResponseData<String> copySceneConfig(@RequestBody Map<String, Object> request) {
        log.info("复制案件列表场景配置，request={}", request);
        try {
            String fromScene = (String) request.get("from_scene");
            String toScene = (String) request.get("to_scene");
            Long tenantId = request.get("tenant_id") != null ? Long.valueOf(request.get("tenant_id").toString()) : null;
            if (fromScene == null || toScene == null || tenantId == null) {
                return ResponseData.error("参数不完整");
            }
            if (!isValidListScene(fromScene) || !isValidListScene(toScene)) {
                return ResponseData.error("场景类型必须是列表相关场景");
            }
            List<Map<String, Object>> sourceConfigs = loadFromFile(tenantId, fromScene);
            if (sourceConfigs == null || sourceConfigs.isEmpty()) {
                return ResponseData.error("源场景配置不存在");
            }
            List<Map<String, Object>> targetConfigs = new ArrayList<>();
            for (Map<String, Object> source : sourceConfigs) {
                Map<String, Object> target = new HashMap<>(source);
                target.put("scene_type", toScene);
                target.put("scene_name", getSceneName(toScene));
                targetConfigs.add(target);
            }
            saveToFile(tenantId, toScene, targetConfigs);
            return ResponseData.success("复制成功");
        } catch (Exception e) {
            log.error("复制场景配置失败", e);
            return ResponseData.error("复制失败: " + e.getMessage());
        }
    }

    /** 获取可用字段 */
    @GetMapping("/available-fields")
    public ResponseData<List<Map<String, Object>>> getAvailableFields(@RequestParam(required = false) Long tenantId) {
        log.info("获取案件列表可用字段，tenantId={}", tenantId);
        try {
            List<Map<String, Object>> fields = new ArrayList<>();
            String[] keys = {"case_code", "user_name", "mobile", "loan_amount", "outstanding_amount",
                    "overdue_days", "case_status", "product_name", "app_name", "due_date"};
            String[] names = {"案件编号", "客户姓名", "手机号码", "贷款金额", "未还金额",
                    "逾期天数", "案件状态", "产品名称", "App名称", "到期日期"};
            String[] types = {"String", "String", "String", "Decimal", "Decimal",
                    "Integer", "Enum", "String", "String", "Date"};
            for (int i = 0; i < keys.length; i++) {
                Map<String, Object> f = new HashMap<>();
                f.put("field_key", keys[i]);
                f.put("field_name", names[i]);
                f.put("field_data_type", types[i]);
                f.put("field_type", types[i]);
                f.put("field_source", "standard");
                f.put("field_group_name", "基础信息");
                fields.add(f);
            }
            return ResponseData.success(fields);
        } catch (Exception e) {
            log.error("获取可用字段失败", e);
            return ResponseData.error("获取可用字段失败: " + e.getMessage());
        }
    }

    /** 校验列表场景 */
    private boolean isValidListScene(String sceneType) {
        return "admin_case_list".equals(sceneType) || "collector_case_list".equals(sceneType);
    }

    /** sort_order 提取 */
    private Integer getSortOrder(Map<String, Object> config) {
        Object val = config.get("sort_order");
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.valueOf(val.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /** 生成Mock数据 */
    private List<Map<String, Object>> generateMockConfigs(Long tenantId, String sceneType) {
        List<Map<String, Object>> configs = new ArrayList<>();
        String sceneName = getSceneName(sceneType);
        String[] fieldKeys = {"case_code", "user_name", "loan_amount", "outstanding_amount",
                "overdue_days", "case_status", "due_date", "product_name", "app_name"};
        String[] fieldNames = {"案件编号", "客户", "贷款金额", "未还金额",
                "逾期天数", "案件状态", "到期日期", "产品名称", "App名称"};
        String[] fieldTypes = {"String", "String", "Decimal", "Decimal",
                "Integer", "Enum", "Date", "String", "String"};
        boolean[] isRequired = {true, true, true, true, true, true, true, false, false};
        boolean[] isSearchable = {true, true, false, false, false, false, false, true, true};
        boolean[] isFilterable = {false, false, false, false, false, true, false, false, false};
        boolean[] isRangeSearchable = {false, false, true, true, true, false, true, false, false};
        String[] colorTypes = {"normal", "normal", "normal", "red", "red", "normal", "normal", "normal", "normal"};
        int[] widths = {180, 120, 120, 120, 100, 110, 120, 130, 130};
        for (int i = 0; i < fieldKeys.length; i++) {
            Map<String, Object> cfg = new HashMap<>();
            cfg.put("id", (long) (i + 1));
            cfg.put("tenant_id", String.valueOf(tenantId));
            cfg.put("scene_type", sceneType);
            cfg.put("scene_name", sceneName);
            cfg.put("field_key", fieldKeys[i]);
            cfg.put("field_name", fieldNames[i]);
            cfg.put("field_data_type", fieldTypes[i]);
            cfg.put("field_source", "standard");
            cfg.put("sort_order", i + 1);
            cfg.put("display_width", widths[i]);
            cfg.put("color_type", colorTypes[i]);
            cfg.put("color_rule", null);
            cfg.put("hide_rule", null);
            cfg.put("hide_for_queues", new ArrayList<>());
            cfg.put("hide_for_agencies", new ArrayList<>());
            cfg.put("hide_for_teams", new ArrayList<>());
            if ("Decimal".equals(fieldTypes[i]) && ("loan_amount".equals(fieldKeys[i]) || "outstanding_amount".equals(fieldKeys[i]))) {
                Map<String, Object> formatRule = new HashMap<>();
                formatRule.put("format_type", "currency");
                formatRule.put("prefix", "¥");
                formatRule.put("suffix", "");
                cfg.put("format_rule", formatRule);
            } else {
                cfg.put("format_rule", null);
            }
            cfg.put("is_searchable", isSearchable[i]);
            cfg.put("is_filterable", isFilterable[i]);
            cfg.put("is_range_searchable", isRangeSearchable[i]);
            cfg.put("is_required", isRequired[i]);
            cfg.put("created_at", java.time.LocalDateTime.now().toString());
            cfg.put("updated_at", java.time.LocalDateTime.now().toString());
            cfg.put("created_by", "system");
            cfg.put("updated_by", null);
            configs.add(cfg);
        }
        return configs;
    }

    /** 场景名称 */
    private String getSceneName(String sceneType) {
        if (sceneType == null || sceneType.isEmpty()) return "未知场景";
        switch (sceneType) {
            case "admin_case_list":
                return "控台案件列表";
            case "collector_case_list":
                return "IM端案件列表";
            default:
                return sceneType;
        }
    }
}

