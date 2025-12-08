package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.FieldDisplayConfigDTO;
import com.cco.model.entity.TenantFieldDisplayConfig;
import com.cco.service.FieldDisplayConfigService;
import com.cco.service.TenantFieldUploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    /**
     * 必须展示的字段key列表
     */
    private static final Set<String> REQUIRED_FIELD_KEYS = new HashSet<>(Set.of(
            "case_code",
            "user_name",
            "loan_amount",
            "outstanding_amount",
            "overdue_days",
            "case_status",
            "due_date"
    ));

    private static final Path VERSION_BASE = Paths.get(System.getProperty("user.home"), ".cco-storage", "case-list-display-versions");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FieldDisplayConfigService fieldDisplayConfigService;
    private final TenantFieldUploadService tenantFieldUploadService;

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

        // 先尝试从展示配置的本地版本文件获取当前激活版本
        if (list == null || list.isEmpty()) {
            List<TenantFieldDisplayConfig> versionConfigs = loadActiveVersion(finalTenantId, finalSceneType);
            if (versionConfigs != null && !versionConfigs.isEmpty()) {
                list = versionConfigs;
            }
        }

        // 如果仍为空，则回退到“案件列表字段映射配置”最新版本的字段列表
        if ((list == null || list.isEmpty()) && tenantFieldUploadService != null) {
            try {
                Map<String, Object> latest = tenantFieldUploadService.getCurrentVersionFields(finalTenantId.toString(), "list");
                if (latest != null && latest.get("fields") instanceof List<?> rawFields) {
                    list = convertUploadFields(rawFields, finalTenantId, finalSceneType);
                }
            } catch (Exception e) {
                log.warn("回退读取映射版本字段失败: {}", e.getMessage());
            }
        }

        return ResponseData.success(list != null ? list : new ArrayList<>());
    }

    /**
     * 获取当前基于的“案件列表字段映射配置”版本信息
     */
    @GetMapping("/version")
    public ResponseData<Map<String, Object>> getVersionInfo(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType
    ) {
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null && !sceneType.isEmpty() ? sceneType : DEFAULT_SCENE;

        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("tenant_id", finalTenantId);
        versionInfo.put("scene_type", finalSceneType);
        versionInfo.put("source", "mock");
        versionInfo.put("version", 0);
        versionInfo.put("fetched_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 尝试读取本地版本文件（展示配置）
        Map<String, Object> active = readVersionMeta(finalTenantId, finalSceneType);
        if (!active.isEmpty()) {
            versionInfo.putAll(active);
            versionInfo.put("source", active.getOrDefault("source", "local"));
        }

        if (tenantFieldUploadService != null) {
            try {
                // 映射配置使用 scene = list
                Map<String, Object> latest = tenantFieldUploadService.getCurrentVersionFields(finalTenantId.toString(), "list");
                if (latest != null && !latest.isEmpty()) {
                    versionInfo.put("source", "upload");
                    versionInfo.put("version", Optional.ofNullable(latest.get("version")).orElse(0));
                    if (latest.get("fetched_at") != null) {
                        versionInfo.put("fetched_at", latest.get("fetched_at"));
                    }
                }
            } catch (Exception e) {
                log.warn("获取映射配置版本信息失败: {}", e.getMessage());
            }
        }

        return ResponseData.success(versionInfo);
    }

    /**
     * 保存当前配置为新版本（本地文件）
     */
    @PostMapping("/version/save")
    public ResponseData<Map<String, Object>> saveVersion(@RequestBody Map<String, Object> body) {
        Long tenantId = body.get("tenant_id") != null ? Long.valueOf(body.get("tenant_id").toString()) : 1L;
        String sceneType = body.get("scene_type") != null ? body.get("scene_type").toString() : DEFAULT_SCENE;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> configs = (List<Map<String, Object>>) body.get("configs");
        String operator = body.getOrDefault("operator", "admin").toString();
        String note = body.getOrDefault("note", "").toString();

        if (configs == null || configs.isEmpty()) {
            return ResponseData.error("配置不能为空");
        }

        try {
            Map<String, Object> fileData = readVersionFile(tenantId, sceneType);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> versions = (List<Map<String, Object>>) fileData.getOrDefault("versions", new ArrayList<>());
            int nextVersion = versions.stream()
                    .map(v -> Integer.parseInt(v.getOrDefault("version", 0).toString()))
                    .max(Integer::compareTo)
                    .orElse(0) + 1;

            Map<String, Object> record = new HashMap<>();
            record.put("version", nextVersion);
            record.put("saved_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            record.put("operator", operator);
            record.put("note", note);
            record.put("configs", configs);

            versions.add(record);
            fileData.put("versions", versions);
            fileData.put("activeVersion", nextVersion);

            writeVersionFile(tenantId, sceneType, fileData);

            Map<String, Object> resp = new HashMap<>();
            resp.put("version", nextVersion);
            resp.put("saved_at", record.get("saved_at"));
            return ResponseData.success("保存成功", resp);
        } catch (Exception e) {
            log.error("保存展示配置版本失败", e);
            return ResponseData.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 获取版本历史（本地文件）
     */
    @GetMapping("/version/history")
    public ResponseData<List<Map<String, Object>>> getVersionHistory(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String sceneType
    ) {
        Long finalTenantId = tenantId != null ? tenantId : 1L;
        String finalSceneType = sceneType != null && !sceneType.isEmpty() ? sceneType : DEFAULT_SCENE;
        try {
            Map<String, Object> fileData = readVersionFile(finalTenantId, finalSceneType);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> versions = (List<Map<String, Object>>) fileData.getOrDefault("versions", new ArrayList<>());
            return ResponseData.success(versions);
        } catch (Exception e) {
            log.error("读取版本历史失败", e);
            return ResponseData.error("读取失败：" + e.getMessage());
        }
    }

    /**
     * 激活指定版本（本地文件），并返回该版本配置
     */
    @PostMapping("/version/activate")
    public ResponseData<Map<String, Object>> activateVersion(@RequestBody Map<String, Object> body) {
        Long tenantId = body.get("tenant_id") != null ? Long.valueOf(body.get("tenant_id").toString()) : 1L;
        String sceneType = body.get("scene_type") != null ? body.get("scene_type").toString() : DEFAULT_SCENE;
        Integer version = body.get("version") != null ? Integer.valueOf(body.get("version").toString()) : null;
        if (version == null) {
            return ResponseData.error("version不能为空");
        }

        try {
            Map<String, Object> fileData = readVersionFile(tenantId, sceneType);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> versions = (List<Map<String, Object>>) fileData.getOrDefault("versions", new ArrayList<>());
            Map<String, Object> target = versions.stream()
                    .filter(v -> version.equals(Integer.valueOf(v.getOrDefault("version", 0).toString())))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return ResponseData.error("版本不存在");
            }
            fileData.put("activeVersion", version);
            writeVersionFile(tenantId, sceneType, fileData);

            Map<String, Object> resp = new HashMap<>();
            resp.put("version", version);
            resp.put("configs", target.get("configs"));
            return ResponseData.success("激活成功", resp);
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return ResponseData.error("激活失败：" + e.getMessage());
        }
    }

    /**
     * 获取场景类型列表（仅列表相关场景）
     */
    @GetMapping("/scene-types")
    public ResponseData<List<Map<String, String>>> getSceneTypes() {
        List<Map<String, String>> sceneTypes = new java.util.ArrayList<>();
        sceneTypes.add(scene("admin_case_detail", "控台案件列表"));
        sceneTypes.add(scene("collector_case_detail", "IM端案件列表"));
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
                return "控台案件列表";
            case "collector_case_detail":
                return "IM端案件列表";
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

    /**
     * 将上传版本中的字段转换为列表配置实体（仅内存返回，不入库）
     */
    private List<TenantFieldDisplayConfig> convertUploadFields(List<?> rawFields, Long tenantId, String sceneType) {
        List<TenantFieldDisplayConfig> result = new ArrayList<>();
        int idx = 0;
        for (Object obj : rawFields) {
            if (!(obj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> field = (Map<String, Object>) obj;

            TenantFieldDisplayConfig cfg = new TenantFieldDisplayConfig();
            cfg.setTenantId(tenantId);
            cfg.setSceneType(sceneType);
            cfg.setSceneName(getSceneName(sceneType));
            cfg.setFieldKey(String.valueOf(field.getOrDefault("field_key", "")));
            cfg.setFieldName(String.valueOf(field.getOrDefault("field_name", "")));
            cfg.setFieldDataType(String.valueOf(field.getOrDefault("field_type", "")));
            cfg.setFieldSource(String.valueOf(field.getOrDefault("field_source", "standard")));
            
            // 处理枚举选项（支持enum_options和enum_values两种字段名）
            Object enumOptions = field.get("enum_options");
            if (enumOptions == null) {
                enumOptions = field.get("enum_values");
            }
            if (enumOptions != null) {
                cfg.setEnumOptions(enumOptions);
            }

            Integer sort = null;
            Object sortObj = field.get("sort_order");
            if (sortObj instanceof Number) {
                sort = ((Number) sortObj).intValue();
            }
            cfg.setSortOrder(sort != null ? sort : ++idx);
            cfg.setDisplayWidth(120);
            cfg.setColorType("normal");
            cfg.setColorRule(null);
            cfg.setHideRule(null);
            cfg.setHideForQueues(new ArrayList<>());
            cfg.setHideForAgencies(new ArrayList<>());
            cfg.setHideForTeams(new ArrayList<>());
            cfg.setFormatRule(null);

            Boolean isRequired = null;
            Object requiredObj = field.get("is_required");
            if (requiredObj instanceof Boolean) {
                isRequired = (Boolean) requiredObj;
            }
            cfg.setIsRequired(isRequired != null ? isRequired : REQUIRED_FIELD_KEYS.contains(cfg.getFieldKey()));

            // 控台案件列表：可筛选、范围检索默认开启，其他拓展字段默认关闭可搜索
            cfg.setIsSearchable(Boolean.FALSE);
            cfg.setIsFilterable(Boolean.TRUE);
            cfg.setIsRangeSearchable(Boolean.TRUE);
            cfg.setCreatedBy("system");
            cfg.setUpdatedBy(null);

            result.add(cfg);
        }
        // 根据 sort_order 再排序一次
        result.sort((a, b) -> {
            int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(sa, sb);
        });
        return result;
    }

    /**
     * 读取当前激活版本的配置（本地文件）
     */
    private List<TenantFieldDisplayConfig> loadActiveVersion(Long tenantId, String sceneType) {
        try {
            Map<String, Object> fileData = readVersionFile(tenantId, sceneType);
            Integer activeVersion = (Integer) fileData.get("activeVersion");
            if (activeVersion == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> versions = (List<Map<String, Object>>) fileData.getOrDefault("versions", new ArrayList<>());
            Map<String, Object> target = versions.stream()
                    .filter(v -> activeVersion.equals(Integer.valueOf(v.getOrDefault("version", 0).toString())))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = (List<Map<String, Object>>) target.get("configs");
            return convertUploadFields(raw, tenantId, sceneType);
        } catch (Exception e) {
            log.warn("读取激活版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 读取版本文件
     */
    private Map<String, Object> readVersionFile(Long tenantId, String sceneType) throws Exception {
        if (!Files.exists(VERSION_BASE)) {
            Files.createDirectories(VERSION_BASE);
        }
        Path file = VERSION_BASE.resolve(tenantId + "_" + sceneType + ".json");
        if (!Files.exists(file)) {
            Map<String, Object> init = new HashMap<>();
            init.put("versions", new ArrayList<>());
            init.put("activeVersion", null);
            return init;
        }
        byte[] bytes = Files.readAllBytes(file);
        return OBJECT_MAPPER.readValue(bytes, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 写入版本文件
     */
    private void writeVersionFile(Long tenantId, String sceneType, Map<String, Object> data) throws Exception {
        if (!Files.exists(VERSION_BASE)) {
            Files.createDirectories(VERSION_BASE);
        }
        Path file = VERSION_BASE.resolve(tenantId + "_" + sceneType + ".json");
        byte[] bytes = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        Files.write(file, bytes);
    }

    /**
     * 读取版本元信息（用于提示）
     */
    private Map<String, Object> readVersionMeta(Long tenantId, String sceneType) {
        Map<String, Object> meta = new HashMap<>();
        try {
            Map<String, Object> data = readVersionFile(tenantId, sceneType);
            Integer activeVersion = (Integer) data.get("activeVersion");
            meta.put("version", activeVersion != null ? activeVersion : 0);
            meta.put("source", activeVersion != null ? "local" : "mock");
            meta.put("fetched_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            meta.put("version", 0);
            meta.put("source", "mock");
            meta.put("fetched_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return meta;
    }
}

