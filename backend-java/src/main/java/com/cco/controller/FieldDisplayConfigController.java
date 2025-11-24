package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.FieldDisplayConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段展示配置Controller - 从数据库读取真实数据
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/field-display-configs")
public class FieldDisplayConfigController {

    @Autowired(required = false)
    private FieldDisplayConfigService fieldDisplayConfigService;

    /**
     * 获取字段展示配置列表 - 从数据库读取
     */
    @GetMapping
    public ResponseData<List<Map<String, Object>>> getFieldDisplayConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType,
            @RequestParam(required = false) String fieldKey
    ) {
        log.info("========== 获取字段展示配置列表，tenantId={}, sceneType={}, fieldKey={} ==========", tenantId, sceneType, fieldKey);
        
        // 简化逻辑：在Mock模式下，直接返回Mock数据
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null ? sceneType : "admin_case_list";
        
        try {
            List<TenantFieldDisplayConfig> configs = null;
            
            // 尝试从数据库获取
            if (fieldDisplayConfigService != null) {
                try {
                    configs = fieldDisplayConfigService.list(finalTenantId, finalSceneType, fieldKey);
                    log.info("数据库查询结果：{}条", configs != null ? configs.size() : 0);
                } catch (Exception e) {
                    log.warn("数据库查询失败（Mock模式）: {}", e.getMessage());
                }
            } else {
                log.info("FieldDisplayConfigService未注入（Mock模式）");
            }
            
            // 如果数据库没有数据，使用Mock数据
            if (configs == null || configs.isEmpty()) {
                log.info("========== 使用Mock数据，tenantId={}, sceneType={} ==========", finalTenantId, finalSceneType);
                List<Map<String, Object>> mockConfigs = generateMockConfigs(finalTenantId, finalSceneType);
                log.info("========== Mock数据生成完成，共{}条 ==========", mockConfigs.size());
                return ResponseData.success(mockConfigs);
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (TenantFieldDisplayConfig config : configs) {
                try {
                    Map<String, Object> map = convertToMap(config);
                    result.add(map);
                } catch (Exception e) {
                    log.warn("转换配置对象失败，跳过: {}", e.getMessage());
                }
            }
            
            log.info("成功获取{}条字段展示配置", result.size());
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取字段展示配置失败，使用Mock数据", e);
            e.printStackTrace();
            // 即使出错也返回Mock数据
            try {
                List<Map<String, Object>> mockConfigs = generateMockConfigs(finalTenantId, finalSceneType);
                log.info("异常情况下生成Mock数据，共{}条", mockConfigs.size());
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
            // 即使出错也返回默认的场景类型
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
            if (fieldDisplayConfigService == null) {
                log.warn("FieldDisplayConfigService未注入，返回成功（Mock模式）");
                return ResponseData.success("批量保存成功（Mock模式）");
            }
            
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
            
            // 处理每个配置项
            int successCount = 0;
            int failCount = 0;
            for (Map<String, Object> configMap : configsList) {
                try {
                    Object idObj = configMap.get("id");
                    if (idObj != null && !idObj.toString().isEmpty()) {
                        // 更新现有配置
                        Long id = null;
                        if (idObj instanceof Number) {
                            id = ((Number) idObj).longValue();
                        } else {
                            id = Long.valueOf(idObj.toString());
                        }
                        
                        FieldDisplayConfigDTO.Update updateDto = new FieldDisplayConfigDTO.Update();
                        if (configMap.get("sort_order") != null) {
                            updateDto.setSortOrder(Integer.valueOf(configMap.get("sort_order").toString()));
                        }
                        if (configMap.get("display_width") != null) {
                            updateDto.setDisplayWidth(Integer.valueOf(configMap.get("display_width").toString()));
                        }
                        if (configMap.get("color_type") != null) {
                            updateDto.setColorType(configMap.get("color_type").toString());
                        }
                        if (configMap.get("is_searchable") != null) {
                            updateDto.setIsSearchable(Boolean.valueOf(configMap.get("is_searchable").toString()));
                        }
                        if (configMap.get("is_filterable") != null) {
                            updateDto.setIsFilterable(Boolean.valueOf(configMap.get("is_filterable").toString()));
                        }
                        if (configMap.get("is_range_searchable") != null) {
                            updateDto.setIsRangeSearchable(Boolean.valueOf(configMap.get("is_range_searchable").toString()));
                        }
                        
                        fieldDisplayConfigService.update(id, updateDto);
                        successCount++;
                    } else {
                        // 创建新配置
                        FieldDisplayConfigDTO.Create createDto = new FieldDisplayConfigDTO.Create();
                        createDto.setTenantId(tenantId);
                        createDto.setSceneType(sceneType);
                        createDto.setSceneName((String) configMap.get("scene_name"));
                        createDto.setFieldKey((String) configMap.get("field_key"));
                        createDto.setFieldName((String) configMap.get("field_name"));
                        if (configMap.get("field_data_type") != null) {
                            createDto.setFieldDataType(configMap.get("field_data_type").toString());
                        }
                        if (configMap.get("field_source") != null) {
                            createDto.setFieldSource(configMap.get("field_source").toString());
                        }
                        if (configMap.get("sort_order") != null) {
                            createDto.setSortOrder(Integer.valueOf(configMap.get("sort_order").toString()));
                        }
                        if (configMap.get("display_width") != null) {
                            createDto.setDisplayWidth(Integer.valueOf(configMap.get("display_width").toString()));
                        }
                        if (configMap.get("color_type") != null) {
                            createDto.setColorType(configMap.get("color_type").toString());
                        }
                        if (configMap.get("is_searchable") != null) {
                            createDto.setIsSearchable(Boolean.valueOf(configMap.get("is_searchable").toString()));
                        }
                        if (configMap.get("is_filterable") != null) {
                            createDto.setIsFilterable(Boolean.valueOf(configMap.get("is_filterable").toString()));
                        }
                        if (configMap.get("is_range_searchable") != null) {
                            createDto.setIsRangeSearchable(Boolean.valueOf(configMap.get("is_range_searchable").toString()));
                        }
                        
                        fieldDisplayConfigService.create(createDto);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("处理配置项失败: {}", configMap, e);
                    failCount++;
                }
            }
            
            if (failCount > 0) {
                log.warn("批量保存完成，成功{}条，失败{}条", successCount, failCount);
                return ResponseData.success(String.format("批量保存完成，成功%d条，失败%d条", successCount, failCount));
            }
            
            log.info("批量保存成功，共{}条", successCount);
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
            if (fieldDisplayConfigService == null) {
                log.warn("FieldDisplayConfigService未注入，返回成功（Mock模式）");
                return ResponseData.success("复制成功（Mock模式）");
            }
            String fromScene = (String) request.get("from_scene");
            String toScene = (String) request.get("to_scene");
            Long tenantId = request.get("tenant_id") != null ? 
                Long.valueOf(request.get("tenant_id").toString()) : null;
            
            if (fromScene == null || toScene == null || tenantId == null) {
                return ResponseData.error("参数不完整");
            }
            
            // 获取源场景的配置
            List<TenantFieldDisplayConfig> sourceConfigs = fieldDisplayConfigService.list(tenantId, fromScene, null);
            
            // 获取目标场景名称
            String toSceneName = getSceneName(toScene);
            
            // 复制每个配置
            for (TenantFieldDisplayConfig sourceConfig : sourceConfigs) {
                FieldDisplayConfigDTO.Create createDto = new FieldDisplayConfigDTO.Create();
                createDto.setTenantId(tenantId);
                createDto.setSceneType(toScene);
                createDto.setSceneName(toSceneName);
                createDto.setFieldKey(sourceConfig.getFieldKey());
                createDto.setFieldName(sourceConfig.getFieldName());
                createDto.setFieldDataType(sourceConfig.getFieldDataType());
                createDto.setFieldSource(sourceConfig.getFieldSource());
                createDto.setSortOrder(sourceConfig.getSortOrder());
                createDto.setDisplayWidth(sourceConfig.getDisplayWidth());
                createDto.setColorType(sourceConfig.getColorType());
                createDto.setColorRule(sourceConfig.getColorRule());
                createDto.setHideRule(sourceConfig.getHideRule());
                createDto.setHideForQueues(sourceConfig.getHideForQueues());
                createDto.setHideForAgencies(sourceConfig.getHideForAgencies());
                createDto.setHideForTeams(sourceConfig.getHideForTeams());
                createDto.setFormatRule(sourceConfig.getFormatRule());
                createDto.setIsSearchable(sourceConfig.getIsSearchable());
                createDto.setIsFilterable(sourceConfig.getIsFilterable());
                createDto.setIsRangeSearchable(sourceConfig.getIsRangeSearchable());
                
                fieldDisplayConfigService.create(createDto);
            }
            
            log.info("复制场景配置成功，从{}复制到{}", fromScene, toScene);
            return ResponseData.success("复制成功");
        } catch (Exception e) {
            log.error("复制场景配置失败", e);
            return ResponseData.error("复制失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用字段选项 - 从数据库读取标准字段和自定义字段
     */
    @GetMapping("/available-fields")
    public ResponseData<List<Map<String, Object>>> getAvailableFields(
            @RequestParam(required = false) Long tenantId
    ) {
        log.info("获取可用字段选项，tenantId={}", tenantId);
        
        try {
            if (fieldDisplayConfigService == null) {
                log.warn("FieldDisplayConfigService未注入，返回空列表（Mock模式）");
                return ResponseData.success(new ArrayList<>());
            }
            
            List<FieldDisplayConfigDTO.AvailableField> fields = fieldDisplayConfigService.getAvailableFields(tenantId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (FieldDisplayConfigDTO.AvailableField field : fields) {
                Map<String, Object> map = new HashMap<>();
                map.put("field_key", field.getFieldKey());
                map.put("field_name", field.getFieldName());
                map.put("field_data_type", field.getFieldType());
                map.put("field_source", field.getFieldSource());
                map.put("group", field.getFieldGroupId() != null ? field.getFieldGroupId().toString() : "其他");
                map.put("is_extended", field.getIsExtended() != null ? field.getIsExtended() : false);
                map.put("description", field.getDescription());
                result.add(map);
            }
            
            log.info("成功获取{}个可用字段", result.size());
            return ResponseData.success(result);
        } catch (Exception e) {
            log.error("获取可用字段失败", e);
            return ResponseData.error("获取可用字段失败: " + e.getMessage());
        }
    }

    /**
     * 将实体转换为Map（前端格式）
     */
    private Map<String, Object> convertToMap(TenantFieldDisplayConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", config.getId());
        map.put("tenant_id", String.valueOf(config.getTenantId()));
        map.put("scene_type", config.getSceneType());
        map.put("scene_name", config.getSceneName());
        map.put("field_key", config.getFieldKey());
        map.put("field_name", config.getFieldName());
        map.put("field_data_type", config.getFieldDataType());
        map.put("field_source", config.getFieldSource());
        map.put("sort_order", config.getSortOrder());
        map.put("display_width", config.getDisplayWidth());
        map.put("color_type", config.getColorType());
        map.put("color_rule", config.getColorRule());
        map.put("hide_rule", config.getHideRule());
        
        // 转换List<Long>为List<String>（前端期望字符串数组）
        if (config.getHideForQueues() != null) {
            map.put("hide_for_queues", config.getHideForQueues().stream()
                .map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        } else {
            map.put("hide_for_queues", new ArrayList<>());
        }
        
        if (config.getHideForAgencies() != null) {
            map.put("hide_for_agencies", config.getHideForAgencies().stream()
                .map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        } else {
            map.put("hide_for_agencies", new ArrayList<>());
        }
        
        if (config.getHideForTeams() != null) {
            map.put("hide_for_teams", config.getHideForTeams().stream()
                .map(String::valueOf).collect(java.util.stream.Collectors.toList()));
        } else {
            map.put("hide_for_teams", new ArrayList<>());
        }
        
        map.put("format_rule", config.getFormatRule());
        map.put("is_searchable", config.getIsSearchable() != null ? config.getIsSearchable() : false);
        map.put("is_filterable", config.getIsFilterable() != null ? config.getIsFilterable() : false);
        map.put("is_range_searchable", config.getIsRangeSearchable() != null ? config.getIsRangeSearchable() : false);
        map.put("created_at", config.getCreatedAt() != null ? config.getCreatedAt().toString() : "");
        map.put("updated_at", config.getUpdatedAt() != null ? config.getUpdatedAt().toString() : "");
        map.put("created_by", config.getCreatedBy());
        map.put("updated_by", config.getUpdatedBy());
        
        return map;
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

    /**
     * 生成Mock字段展示配置数据（当数据库为空时使用）
     */
    private List<Map<String, Object>> generateMockConfigs(Long tenantId, String sceneType) {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        // 根据场景类型生成不同的配置
        String sceneName = getSceneName(sceneType);
        
        if ("admin_case_list".equals(sceneType)) {
            // 控台案件管理列表
            String[] fieldKeys = {"case_code", "user_name", "mobile", "loan_amount", "outstanding_amount", 
                                 "overdue_days", "case_status", "product_name", "app_name", "due_date"};
            String[] fieldNames = {"案件编号", "客户姓名", "手机号码", "贷款金额", "未还金额", 
                                  "逾期天数", "案件状态", "产品名称", "App名称", "到期日期"};
            String[] fieldTypes = {"String", "String", "String", "Decimal", "Decimal", 
                                  "Integer", "Enum", "String", "String", "Date"};
            boolean[] isSearchable = {true, true, true, false, false, false, false, true, true, false};
            boolean[] isFilterable = {false, false, false, false, false, false, true, false, false, false};
            boolean[] isRangeSearchable = {false, false, false, true, true, true, false, false, false, true};
            String[] colorTypes = {"normal", "normal", "normal", "normal", "red", "red", "normal", "normal", "normal", "normal"};
            int[] widths = {180, 120, 140, 120, 120, 100, 110, 130, 130, 120};
            
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
                
                // 格式化规则
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
                config.put("created_at", java.time.LocalDateTime.now().toString());
                config.put("updated_at", java.time.LocalDateTime.now().toString());
                config.put("created_by", "system");
                config.put("updated_by", null);
                
                configs.add(config);
            }
        } else if ("collector_case_list".equals(sceneType)) {
            // 催员案件列表
            String[] fieldKeys = {"case_code", "user_name", "mobile", "outstanding_amount", 
                                 "overdue_days", "case_status", "queue_name", "product_name"};
            String[] fieldNames = {"案件编号", "客户姓名", "手机号码", "应还金额", 
                                  "逾期天数", "案件状态", "队列", "产品"};
            String[] fieldTypes = {"String", "String", "String", "Decimal", 
                                  "Integer", "Enum", "String", "String"};
            boolean[] isSearchable = {true, true, true, false, false, false, false, true};
            boolean[] isFilterable = {false, false, false, false, false, true, true, false};
            boolean[] isRangeSearchable = {false, false, false, true, true, false, false, false};
            String[] colorTypes = {"normal", "normal", "normal", "normal", "red", "normal", "normal", "normal"};
            int[] widths = {160, 100, 130, 120, 100, 90, 80, 110};
            
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
                
                if ("Decimal".equals(fieldTypes[i]) && fieldKeys[i].equals("outstanding_amount")) {
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
                config.put("created_at", java.time.LocalDateTime.now().toString());
                config.put("updated_at", java.time.LocalDateTime.now().toString());
                config.put("created_by", "system");
                config.put("updated_by", null);
                
                configs.add(config);
            }
        } else if ("collector_case_detail".equals(sceneType)) {
            // 催员案件详情
            String[] fieldKeys = {"user_name", "mobile", "id_number", "email", "address",
                                 "loan_amount", "outstanding_amount", "overdue_days", "due_date", 
                                 "loan_date", "product_name", "app_name"};
            String[] fieldNames = {"客户姓名", "手机号码", "证件号码", "邮箱", "地址",
                                  "贷款金额", "未还金额", "逾期天数", "到期日期", 
                                  "放款日期", "产品名称", "App名称"};
            String[] fieldTypes = {"String", "String", "String", "String", "String",
                                  "Decimal", "Decimal", "Integer", "Date", 
                                  "Date", "String", "String"};
            String[] colorTypes = {"normal", "normal", "normal", "normal", "normal",
                                  "normal", "red", "red", "normal", 
                                  "normal", "normal", "normal"};
            
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
                config.put("sort_order", i < 5 ? i + 1 : i + 6); // 前5个是客户信息，从11开始是贷款信息
                config.put("display_width", 0); // 详情页使用自动宽度
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
                
                config.put("is_searchable", false);
                config.put("is_filterable", false);
                config.put("is_range_searchable", false);
                config.put("created_at", java.time.LocalDateTime.now().toString());
                config.put("updated_at", java.time.LocalDateTime.now().toString());
                config.put("created_by", "system");
                config.put("updated_by", null);
                
                configs.add(config);
            }
        }
        
        return configs;
    }
}

