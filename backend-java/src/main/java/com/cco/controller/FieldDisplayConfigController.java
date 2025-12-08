package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 字段展示配置Controller - 使用文件存储（持久化）
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/field-display-configs")
public class FieldDisplayConfigController {

    /**
     * 文件存储路径
     */
    private static final String STORAGE_DIR = System.getProperty("user.home") + "/.cco-storage";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取存储文件路径
     */
    private String getStorageFilePath(Long tenantId, String sceneType) {
        return STORAGE_DIR + "/field-display-configs_" + tenantId + "_" + sceneType + ".json";
    }

    /**
     * 从文件读取配置
     */
    private List<Map<String, Object>> loadFromFile(Long tenantId, String sceneType) {
        try {
            String filePath = getStorageFilePath(tenantId, sceneType);
            File file = new File(filePath);
            
            if (!file.exists()) {
                return null;
            }
            
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            List<Map<String, Object>> configs = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            
            log.info("从文件加载配置，tenantId={}, sceneType={}, 共{}条", tenantId, sceneType, configs.size());
            return configs;
        } catch (Exception e) {
            log.warn("从文件加载配置失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存配置到文件
     */
    private void saveToFile(Long tenantId, String sceneType, List<Map<String, Object>> configs) {
        try {
            // 确保目录存在
            File dir = new File(STORAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String filePath = getStorageFilePath(tenantId, sceneType);
            String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configs);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }
            
            log.info("配置已保存到文件，tenantId={}, sceneType={}, 共{}条，文件路径={}", 
                tenantId, sceneType, configs.size(), filePath);
        } catch (Exception e) {
            log.error("保存配置到文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取字段展示配置列表
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getFieldDisplayConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String fieldKey
    ) {
        log.info("========== 获取字段展示配置列表，tenantId={}, sceneType={}, fieldKey={} ==========", tenantId, sceneType, fieldKey);
        
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null ? sceneType : "admin_case_list";
        
        try {
            // 优先从文件读取
            List<Map<String, Object>> configs = loadFromFile(finalTenantId, finalSceneType);
            
            if (configs == null || configs.isEmpty()) {
                // 如果文件不存在，生成Mock数据
                log.info("========== 文件不存在，生成Mock数据，tenantId={}, sceneType={} ==========", finalTenantId, finalSceneType);
                configs = generateMockConfigs(finalTenantId, finalSceneType);
                
                // 保存到文件
                saveToFile(finalTenantId, finalSceneType, configs);
            } else {
                log.info("========== 从文件读取配置，tenantId={}, sceneType={}, 共{}条 ==========", 
                    finalTenantId, finalSceneType, configs.size());
                // 确保数据按sort_order排序
                configs.sort((a, b) -> {
                    Integer orderA = getSortOrder(a);
                    Integer orderB = getSortOrder(b);
                    return Integer.compare(orderA, orderB);
                });
            }
            
            log.info("成功获取{}条字段展示配置", configs.size());
            return ResponseData.success(configs);
        } catch (Exception e) {
            log.error("获取字段展示配置失败", e);
            e.printStackTrace();
            // 即使出错也返回Mock数据
            try {
                List<Map<String, Object>> mockConfigs = generateMockConfigs(finalTenantId, finalSceneType);
                return ResponseData.success(mockConfigs);
            } catch (Exception mockError) {
                log.error("生成Mock数据也失败", mockError);
                return ResponseData.success(new ArrayList<>());
            }
        }
    }

    /**
     * 获取场景类型列表
     */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        try {
            log.info("获取场景类型列表");
            
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
            
            log.info("成功返回场景类型列表，共{}个", sceneTypes.size());
            return ResponseData.success(sceneTypes);
        } catch (Exception e) {
            log.error("获取场景类型列表失败", e);
            e.printStackTrace();
            List<Map<String, String>> defaultSceneTypes = new ArrayList<>();
            Map<String, String> type1 = new HashMap<>();
            type1.put("key", "admin_case_list");
            type1.put("name", "控台案件管理列表");
            type1.put("description", "管理后台的案件列表页面");
            defaultSceneTypes.add(type1);
            return ResponseData.success(defaultSceneTypes);
        }
    }

    /**
     * 批量创建或更新字段展示配置
     */
    @PostMapping("/batch")
    public ResponseData<String> batchCreateOrUpdateConfigs(@RequestBody Map<String, Object> request) {
        log.info("批量创建或更新字段展示配置，request={}", request);
        
        try {
            // 处理tenant_id可能是字符串或数字的情况
            Object tenantIdObj = request.get("tenant_id");
            Long tenantId = null;
            if (tenantIdObj != null) {
                if (tenantIdObj instanceof Number) {
                    tenantId = ((Number) tenantIdObj).longValue();
                } else {
                    tenantId = Long.valueOf(tenantIdObj.toString());
                }
            }
            
            String sceneType = (String) request.get("scene_type");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> configsList = (List<Map<String, Object>>) request.get("configs");
            
            if (tenantId == null || sceneType == null || sceneType.isEmpty()) {
                log.warn("参数不完整: tenantId={}, sceneType={}", tenantId, sceneType);
                return ResponseData.error("参数不完整: tenantId和sceneType不能为空");
            }
            
            if (configsList == null || configsList.isEmpty()) {
                log.warn("配置列表为空");
                return ResponseData.success("批量保存成功（无配置需要保存）");
            }
            
            // 使用文件存储（持久化）
            log.info("批量保存 - 文件存储模式，tenantId={}, sceneType={}, 收到{}条配置", 
                tenantId, sceneType, configsList.size());
            
            // 深拷贝配置列表，避免引用问题
            List<Map<String, Object>> savedConfigs = new ArrayList<>();
            for (Map<String, Object> configMap : configsList) {
                Map<String, Object> savedConfig = new HashMap<>();
                // 复制所有字段
                savedConfig.putAll(configMap);
                // 确保sort_order是Integer类型
                if (savedConfig.containsKey("sort_order")) {
                    Object sortOrderObj = savedConfig.get("sort_order");
                    if (sortOrderObj instanceof Number) {
                        savedConfig.put("sort_order", ((Number) sortOrderObj).intValue());
                    } else if (sortOrderObj != null) {
                        try {
                            savedConfig.put("sort_order", Integer.valueOf(sortOrderObj.toString()));
                        } catch (Exception e) {
                            log.warn("无法转换sort_order: {}", sortOrderObj);
                            savedConfig.put("sort_order", 0);
                        }
                    }
                }
                savedConfigs.add(savedConfig);
                log.info("保存配置: field_key={}, sort_order={}", 
                    savedConfig.get("field_key"), savedConfig.get("sort_order"));
            }
            
            // 按sort_order排序（保持用户设置的排序）
            savedConfigs.sort((a, b) -> {
                Integer orderA = getSortOrder(a);
                Integer orderB = getSortOrder(b);
                return Integer.compare(orderA, orderB);
            });
            
            // 保存到文件
            saveToFile(tenantId, sceneType, savedConfigs);
            
            log.info("批量保存成功，tenantId={}, sceneType={}, 共{}条配置，排序后的顺序:", 
                tenantId, sceneType, savedConfigs.size());
            for (Map<String, Object> config : savedConfigs) {
                log.info("  - {} (sort_order={})", config.get("field_key"), config.get("sort_order"));
            }
            
            return ResponseData.success("批量保存成功");
        } catch (Exception e) {
            log.error("批量保存失败", e);
            e.printStackTrace();
            return ResponseData.error("批量保存失败: " + e.getMessage());
        }
    }

    /**
     * 复制场景配置
     */
    @PostMapping("/copy-scene")
    public ResponseData<String> copySceneConfig(@RequestBody Map<String, Object> request) {
        log.info("复制场景配置，request={}", request);
        
        try {
            String fromScene = (String) request.get("from_scene");
            String toScene = (String) request.get("to_scene");
            Long tenantId = request.get("tenant_id") != null ? 
                Long.valueOf(request.get("tenant_id").toString()) : null;
            
            if (fromScene == null || toScene == null || tenantId == null) {
                return ResponseData.error("参数不完整");
            }
            
            // 从文件读取源场景配置
            List<Map<String, Object>> sourceConfigs = loadFromFile(tenantId, fromScene);
            if (sourceConfigs == null || sourceConfigs.isEmpty()) {
                return ResponseData.error("源场景配置不存在");
            }
            
            // 复制到目标场景
            List<Map<String, Object>> targetConfigs = new ArrayList<>();
            for (Map<String, Object> sourceConfig : sourceConfigs) {
                Map<String, Object> targetConfig = new HashMap<>(sourceConfig);
                targetConfig.put("scene_type", toScene);
                targetConfig.put("scene_name", getSceneName(toScene));
                targetConfigs.add(targetConfig);
            }
            
            // 保存到文件
            saveToFile(tenantId, toScene, targetConfigs);
            
            log.info("复制场景配置成功，从{}复制到{}", fromScene, toScene);
            return ResponseData.success("复制成功");
        } catch (Exception e) {
            log.error("复制场景配置失败", e);
            return ResponseData.error("复制失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用字段选项
     */
    @GetMapping("/available-fields")
    public ResponseData<List<Map<String, Object>>> getAvailableFields(
            @RequestParam(required = false) Long tenantId
    ) {
        log.info("获取可用字段选项，tenantId={}", tenantId);
        
        try {
            // 返回Mock数据
            List<Map<String, Object>> fields = new ArrayList<>();
            
            // 标准字段
            String[] standardKeys = {"case_code", "user_name", "mobile", "loan_amount", "outstanding_amount", 
                                     "overdue_days", "case_status", "product_name", "app_name", "due_date"};
            String[] standardNames = {"案件编号", "客户姓名", "手机号码", "贷款金额", "未还金额", 
                                      "逾期天数", "案件状态", "产品名称", "App名称", "到期日期"};
            String[] standardTypes = {"String", "String", "String", "Decimal", "Decimal", 
                                      "Integer", "Enum", "String", "String", "Date"};
            
            for (int i = 0; i < standardKeys.length; i++) {
                Map<String, Object> field = new HashMap<>();
                field.put("field_key", standardKeys[i]);
                field.put("field_name", standardNames[i]);
                field.put("field_data_type", standardTypes[i]);
                field.put("field_type", standardTypes[i]);
                field.put("field_source", "standard");
                field.put("field_group_name", "基础信息");
                fields.add(field);
            }
            
            log.info("成功获取{}个可用字段", fields.size());
            return ResponseData.success(fields);
        } catch (Exception e) {
            log.error("获取可用字段失败", e);
            return ResponseData.error("获取可用字段失败: " + e.getMessage());
        }
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
     * 生成Mock字段展示配置数据（当文件不存在时使用）
     */
    private List<Map<String, Object>> generateMockConfigs(Long tenantId, String sceneType) {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        String sceneName = getSceneName(sceneType);
        
        if ("admin_case_list".equals(sceneType)) {
            String[] fieldKeys = {"case_code", "user_name", "mobile_number", "loan_amount", "outstanding_amount",
                                  "overdue_days", "case_status", "due_date", "total_installments", "term_days",
                                  "system_name", "product_name", "app_name", "merchant_name"};
            String[] fieldNames = {"案件编号", "客户", "手机号", "贷款金额", "未还金额",
                                   "逾期天数", "案件状态", "到期日期", "期数", "当期天数",
                                   "所属系统", "产品", "APP", "商户"};
            String[] fieldTypes = {"String", "String", "String", "Decimal", "Decimal",
                                   "Integer", "Enum", "Date", "Integer", "Integer",
                                   "String", "String", "String", "String"};
            boolean[] isRequired = {true, true, true, true, true, true, true, true, true, true, true, true, true, true};
            boolean[] isSearchable = {false, false, false, false, false, false, false, false, false, false, false, false, false, false};
            boolean[] isFilterable = {false, false, false, false, false, false, false, false, false, false, false, false, false, false};
            boolean[] isRangeSearchable = {false, false, false, false, false, false, false, false, false, false, false, false, false, false};
            String[] colorTypes = {"normal", "normal", "normal", "normal", "normal",
                                   "normal", "normal", "normal", "normal", "normal",
                                   "normal", "normal", "normal", "normal"};
            int[] widths = {140, 140, 150, 120, 120, 100, 110, 120, 100, 110, 120, 130, 120, 120};

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
            case "admin_case_list":
                return "控台案件管理列表";
            case "collector_case_list":
                return "催员案件列表";
            case "collector_case_detail":
                return "催员案件详情";
            default:
                return sceneType;
        }
    }
}
