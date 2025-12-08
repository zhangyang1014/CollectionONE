package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.FieldDisplayConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 案件详情字段配置Controller - 使用文件存储（持久化）
 * 负责控台案件详情和IM端案件详情的字段配置
 * 
 * @author CCO Team
 * @since 2025-12-07
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/case-detail-field-configs")
@RequiredArgsConstructor
public class CaseDetailFieldConfigController {

    /**
     * 详情端现在读取“原列表”场景数据：admin_case_list / collector_case_list
     */
    private static final String DEFAULT_SCENE = "admin_case_list";
    private static final String ALT_SCENE = "collector_case_list";

    private final FieldDisplayConfigService fieldDisplayConfigService;

    /**
     * 获取案件详情字段配置
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getCaseDetailFieldConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String fieldKey
    ) {
        log.info("========== 获取案件详情字段配置，tenantId={}, sceneType={}, fieldKey={} ==========", tenantId, sceneType, fieldKey);
        
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null ? sceneType : "collector_case_detail";
        
        // 验证场景类型必须是详情相关场景
        if (!isValidDetailScene(finalSceneType)) {
            log.warn("无效的详情场景类型: {}", finalSceneType);
            finalSceneType = "collector_case_detail";
        }
        
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
            
            log.info("成功获取{}条案件详情字段配置", configs.size());
            return ResponseData.success(configs);
        } catch (Exception e) {
            log.error("获取案件详情字段配置失败", e);
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
     * 获取场景类型列表（仅详情相关场景）
     */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        try {
            log.info("获取案件详情场景类型列表");
            
            List<Map<String, String>> sceneTypes = new ArrayList<>();
            
            Map<String, String> type1 = new HashMap<>();
            type1.put("key", "admin_case_detail");
            type1.put("name", "控台案件详情");
            type1.put("description", "管理后台的案件详情页面");
            
            Map<String, String> type2 = new HashMap<>();
            type2.put("key", "collector_case_detail");
            type2.put("name", "IM端案件详情");
            type2.put("description", "催员端的案件详情页面");
            
            sceneTypes.add(type1);
            sceneTypes.add(type2);
            
            log.info("成功返回案件详情场景类型，共{}个", sceneTypes.size());
            return ResponseData.success(sceneTypes);
        } catch (Exception e) {
            log.error("获取场景类型列表失败", e);
            e.printStackTrace();
            List<Map<String, String>> defaultSceneTypes = new ArrayList<>();
            Map<String, String> type1 = new HashMap<>();
            type1.put("key", "collector_case_detail");
            type1.put("name", "IM端案件详情");
            type1.put("description", "催员端的案件详情页面");
            defaultSceneTypes.add(type1);
            return ResponseData.success(defaultSceneTypes);
        }
    }

    /**
     * 批量创建或更新案件详情字段配置
     */
    @PostMapping("/batch")
    public ResponseData<String> batchCreateOrUpdateConfigs(@RequestBody Map<String, Object> request) {
        log.info("批量创建或更新案件详情字段配置，request={}", request);
        
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
            
            // 验证场景类型必须是详情相关场景
            if (!isValidDetailScene(sceneType)) {
                log.warn("无效的详情场景类型: {}", sceneType);
                return ResponseData.error("场景类型必须是详情相关场景（admin_case_detail或collector_case_detail）");
            }
            
            if (configsList == null || configsList.isEmpty()) {
                log.warn("配置列表为空");
                return ResponseData.success("批量保存成功（无配置需要保存）");
            }
            
            // 使用文件存储（持久化）
            log.info("批量保存案件详情字段配置 - 文件存储模式，tenantId={}, sceneType={}, 收到{}条配置", 
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
        log.info("复制案件详情场景配置，request={}", request);
        
        try {
            String fromScene = (String) request.get("from_scene");
            String toScene = (String) request.get("to_scene");
            Long tenantId = request.get("tenant_id") != null ? 
                Long.valueOf(request.get("tenant_id").toString()) : null;
            
            if (fromScene == null || toScene == null || tenantId == null) {
                return ResponseData.error("参数不完整");
            }
            
            // 验证场景类型必须都是详情相关场景
            if (!isValidDetailScene(fromScene) || !isValidDetailScene(toScene)) {
                return ResponseData.error("场景类型必须是详情相关场景");
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
            
            log.info("复制案件详情场景配置成功，从{}复制到{}", fromScene, toScene);
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
        log.info("获取案件详情可用字段选项，tenantId={}", tenantId);
        
        try {
            // 返回Mock数据，按业务所需分组
            List<Map<String, Object>> fields = new ArrayList<>();

            // 1) 标准案件详情字段
            addFields(fields, "standard_detail", "标准案件详情字段",
                    new String[]{"case_code", "user_name", "mobile", "id_card", "loan_amount", "outstanding_amount",
                            "overdue_days", "case_status", "due_date", "product_name", "app_name",
                            "loan_date", "repayment_date", "collection_status", "last_call_time", "last_sms_time"},
                    new String[]{"案件编号", "客户姓名", "手机号码", "身份证号", "贷款金额", "未还金额",
                            "逾期天数", "案件状态", "到期日期", "产品名称", "App名称",
                            "放款日期", "还款日期", "催收状态", "最后通话时间", "最后短信时间"},
                    new String[]{"String", "String", "String", "String", "Decimal", "Decimal",
                            "Integer", "Enum", "Date", "String", "String",
                            "Date", "Date", "Enum", "DateTime", "DateTime"},
                    "standard");

            // 2) 甲方案件详情字段（示例）
            addFields(fields, "tenant_detail", "甲方案件详情字段",
                    new String[]{"tenant_code", "tenant_contact", "tenant_priority"},
                    new String[]{"甲方编码", "甲方联系人", "甲方优先级"},
                    new String[]{"String", "String", "Enum"},
                    "custom");

            // 3) 案件详情字段映射（示例映射字段）
            addFields(fields, "detail_mapping", "案件详情字段映射",
                    new String[]{"mapping_ext_id", "mapping_channel", "mapping_status"},
                    new String[]{"外部案件ID映射", "渠道映射", "状态映射"},
                    new String[]{"String", "String", "Enum"},
                    "system");

            // 4) 案件详情字段分组管理（示例分组用字段）
            addFields(fields, "detail_grouping", "案件详情字段分组管理",
                    new String[]{"group_basic", "group_finance", "group_contact"},
                    new String[]{"基础信息分组", "财务信息分组", "联系方式分组"},
                    new String[]{"String", "String", "String"},
                    "system");

            log.info("成功获取{}个可用字段（含分组）", fields.size());
            return ResponseData.success(fields);
        } catch (Exception e) {
            log.error("获取可用字段失败", e);
            return ResponseData.error("获取可用字段失败: " + e.getMessage());
        }
    }

    /**
     * 统一辅助方法：构造字段并追加到列表
     */
    private void addFields(List<Map<String, Object>> fields,
                           String groupKey,
                           String groupName,
                           String[] keys,
                           String[] names,
                           String[] types,
                           String source) {
        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> field = new HashMap<>();
            field.put("field_key", keys[i]);
            field.put("field_name", names[i]);
            field.put("field_data_type", types[i]);
            field.put("field_type", types[i]);
            field.put("field_source", source);
            field.put("field_group_name", groupName);
            // 记录所属逻辑分组，前端可用于分组显示
            field.put("group_key", groupKey);
            fields.add(field);
        }
    }

    /**
     * 验证是否是有效的详情场景
     */
    private boolean isValidDetailScene(String sceneType) {
        return "admin_case_detail".equals(sceneType) || "collector_case_detail".equals(sceneType);
    }

    /**
     * 文件读取：如不存在则返回null以触发Mock
     */
    private List<Map<String, Object>> loadFromFile(Long tenantId, String sceneType) {
        try {
            String filePath = getStorageFilePath(tenantId, sceneType);
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(content, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("loadFromFile失败，tenantId={}, sceneType={}, err={}", tenantId, sceneType, e.getMessage());
            return null;
        }
    }

    /**
     * 文件写入：失败仅记录日志
     */
    private void saveToFile(Long tenantId, String sceneType, List<Map<String, Object>> configs) {
        try {
            String filePath = getStorageFilePath(tenantId, sceneType);
            File dir = new File(filePath).getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String content = om.writerWithDefaultPrettyPrinter().writeValueAsString(configs);
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write(content);
            }
        } catch (Exception e) {
            log.warn("saveToFile失败，tenantId={}, sceneType={}, err={}", tenantId, sceneType, e.getMessage());
        }
    }

    private String getStorageFilePath(Long tenantId, String sceneType) {
        return System.getProperty("user.home") + "/.cco-storage/case-detail-field-configs_" + tenantId + "_" + sceneType + ".json";
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
     * 生成Mock案件详情字段配置数据（当文件不存在时使用）
     */
    private List<Map<String, Object>> generateMockConfigs(Long tenantId, String sceneType) {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        String sceneName = getSceneName(sceneType);
        
        // 详情页展示所有字段，包括手机号、身份证等敏感信息
        String[] fieldKeys = {
            "case_code", "user_name", "mobile", "id_card", "loan_amount", "outstanding_amount", 
            "overdue_days", "case_status", "due_date", "product_name", "app_name",
            "loan_date", "repayment_date", "collection_status"
        };
        String[] fieldNames = {
            "案件编号", "客户姓名", "手机号码", "身份证号", "贷款金额", "未还金额", 
            "逾期天数", "案件状态", "到期日期", "产品名称", "App名称",
            "放款日期", "还款日期", "催收状态"
        };
        String[] fieldTypes = {
            "String", "String", "String", "String", "Decimal", "Decimal", 
            "Integer", "Enum", "Date", "String", "String",
            "Date", "Date", "Enum"
        };
        String[] colorTypes = {
            "normal", "normal", "normal", "normal", "normal", "red", 
            "red", "normal", "normal", "normal", "normal",
            "normal", "normal", "normal"
        };
        
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
            config.put("display_width", 0); // 详情页宽度自适应
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
            
            config.put("is_searchable", false); // 详情页字段不需要搜索功能
            config.put("is_filterable", false);
            config.put("is_range_searchable", false);
            config.put("is_required", false); // 详情页没有必填字段限制
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
}

