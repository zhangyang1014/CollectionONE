package com.cco.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cco.common.exception.BusinessException;
import com.cco.mapper.FieldGroupMapper;
import com.cco.mapper.TenantFieldsJsonMapper;
import com.cco.model.dto.*;
import com.cco.model.entity.FieldGroup;
import com.cco.model.entity.TenantFieldsJson;
import com.cco.service.TenantFieldsJsonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 甲方字段JSON管理服务实现类
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class TenantFieldsJsonServiceImpl implements TenantFieldsJsonService {
    
    private static final List<String> VALID_FIELD_TYPES = Arrays.asList(
        "String", "Integer", "Decimal", "Date", "Datetime", "Boolean", "Enum"
    );
    
    private static final DateTimeFormatter ISO8601_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH
    );
    
    @Autowired
    private TenantFieldsJsonMapper tenantFieldsJsonMapper;
    
    @Autowired
    private FieldGroupMapper fieldGroupMapper;
    
    @Override
    public TenantFieldsJsonValidateResponse validateJson(Long tenantId, Map<String, Object> fieldsJson) {
        log.info("========== 校验JSON文件格式，tenantId={} ==========", tenantId);
        
        List<TenantFieldsJsonValidateResponse.ValidationError> errors = new ArrayList<>();
        
        // 1. 校验根节点字段
        if (!fieldsJson.containsKey("version")) {
            errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                "version", "缺少必填字段：version"
            ));
        }
        
        if (!fieldsJson.containsKey("sync_time")) {
            errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                "sync_time", "缺少必填字段：sync_time"
            ));
        }
        
        if (!fieldsJson.containsKey("fields")) {
            errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                "fields", "缺少必填字段：fields"
            ));
        }
        
        if (!errors.isEmpty()) {
            return new TenantFieldsJsonValidateResponse(false, 0, null, null, errors);
        }
        
        // 2. 校验sync_time格式（ISO8601）
        String syncTime = (String) fieldsJson.get("sync_time");
        try {
            LocalDateTime.parse(syncTime, ISO8601_FORMATTER);
        } catch (Exception e) {
            errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                "sync_time", "sync_time格式错误，必须是ISO8601格式（如：2025-11-25T10:30:00Z）"
            ));
        }
        
        // 3. 校验fields数组
        Object fieldsObj = fieldsJson.get("fields");
        if (!(fieldsObj instanceof List)) {
            errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                "fields", "fields必须是数组类型"
            ));
            return new TenantFieldsJsonValidateResponse(false, 0, null, null, errors);
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) fieldsObj;
        Set<String> fieldKeys = new HashSet<>();
        
        // 4. 遍历fields数组，校验每个字段
        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> field = fields.get(i);
            String fieldPath = "fields[" + i + "]";
            
            // 校验必填字段
            if (!field.containsKey("field_key")) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".field_key", "缺少必填字段：field_key"
                ));
            }
            
            if (!field.containsKey("field_name")) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".field_name", "缺少必填字段：field_name"
                ));
            }
            
            if (!field.containsKey("field_type")) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".field_type", "缺少必填字段：field_type"
                ));
            }
            
            if (!field.containsKey("updated_at")) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".updated_at", "缺少必填字段：updated_at"
                ));
            }
            
            // 如果缺少必填字段，跳过后续校验
            if (!field.containsKey("field_key") || !field.containsKey("field_type")) {
                continue;
            }
            
            String fieldKey = (String) field.get("field_key");
            String fieldType = (String) field.get("field_type");
            
            // 校验字段唯一性
            if (fieldKeys.contains(fieldKey)) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".field_key", "字段标识重复：" + fieldKey
                ));
            } else {
                fieldKeys.add(fieldKey);
            }
            
            // 校验字段类型
            if (!VALID_FIELD_TYPES.contains(fieldType)) {
                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                    fieldPath + ".field_type", 
                    "字段类型无效：" + fieldType + "，必须是String/Integer/Decimal/Date/Datetime/Boolean/Enum"
                ));
            }
            
            // 如果是Enum类型，校验enum_values
            if ("Enum".equals(fieldType)) {
                if (!field.containsKey("enum_values")) {
                    errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                        fieldPath + ".enum_values", "Enum类型字段必须包含enum_values数组"
                    ));
                } else {
                    Object enumValuesObj = field.get("enum_values");
                    if (!(enumValuesObj instanceof List)) {
                        errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                            fieldPath + ".enum_values", "enum_values必须是数组类型"
                        ));
                    } else {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> enumValues = (List<Map<String, Object>>) enumValuesObj;
                        for (int j = 0; j < enumValues.size(); j++) {
                            Map<String, Object> enumValue = enumValues.get(j);
                            if (!enumValue.containsKey("value") || !enumValue.containsKey("label")) {
                                errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                                    fieldPath + ".enum_values[" + j + "]", 
                                    "枚举值必须包含value和label字段"
                                ));
                            }
                        }
                    }
                }
            }
            
            // 校验field_group_id是否存在
            if (field.containsKey("field_group_id")) {
                Object groupIdObj = field.get("field_group_id");
                if (groupIdObj instanceof Number) {
                    Long groupId = ((Number) groupIdObj).longValue();
                    FieldGroup group = fieldGroupMapper.selectById(groupId);
                    if (group == null) {
                        errors.add(new TenantFieldsJsonValidateResponse.ValidationError(
                            fieldPath + ".field_group_id", 
                            "字段分组不存在：" + groupId
                        ));
                    }
                }
            }
        }
        
        // 5. 返回校验结果
        if (!errors.isEmpty()) {
            return new TenantFieldsJsonValidateResponse(false, fields.size(), null, null, errors);
        }
        
        String version = (String) fieldsJson.get("version");
        return new TenantFieldsJsonValidateResponse(true, fields.size(), version, syncTime, null);
    }
    
    @Override
    public TenantFieldsJsonCompareResponse compareVersions(Long tenantId, Map<String, Object> newFieldsJson) {
        log.info("========== 对比版本差异，tenantId={} ==========", tenantId);
        
        // 1. 获取当前版本
        TenantFieldsJson currentVersion = null;
        try {
            currentVersion = tenantFieldsJsonMapper.selectCurrentVersion(tenantId);
        } catch (Exception e) {
            log.warn("获取当前版本失败（可能是表不存在），视为首次上传。错误：{}", e.getMessage());
        }
        
        if (currentVersion == null) {
            // 如果没有当前版本，所有字段都是新增的
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newFields = (List<Map<String, Object>>) newFieldsJson.get("fields");
            List<TenantFieldsJsonCompareResponse.FieldInfo> addedFields = newFields.stream()
                .map(field -> new TenantFieldsJsonCompareResponse.FieldInfo(
                    (String) field.get("field_key"),
                    (String) field.get("field_name"),
                    (String) field.get("field_type")
                ))
                .collect(Collectors.toList());
            
            return new TenantFieldsJsonCompareResponse(
                addedFields,
                new ArrayList<>(),
                new ArrayList<>()
            );
        }
        
        // 2. 解析当前版本和新版本的fields数组
        @SuppressWarnings("unchecked")
        Map<String, Object> currentFieldsJson = (Map<String, Object>) currentVersion.getFieldsJson();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> currentFields = (List<Map<String, Object>>) currentFieldsJson.get("fields");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> newFields = (List<Map<String, Object>>) newFieldsJson.get("fields");
        
        // 3. 构建字段映射（以field_key为key）
        Map<String, Map<String, Object>> currentFieldsMap = currentFields.stream()
            .collect(Collectors.toMap(
                field -> (String) field.get("field_key"),
                field -> field,
                (v1, v2) -> v1
            ));
        
        Map<String, Map<String, Object>> newFieldsMap = newFields.stream()
            .collect(Collectors.toMap(
                field -> (String) field.get("field_key"),
                field -> field,
                (v1, v2) -> v1
            ));
        
        // 4. 对比分析
        List<TenantFieldsJsonCompareResponse.FieldInfo> addedFields = new ArrayList<>();
        List<TenantFieldsJsonCompareResponse.FieldInfo> deletedFields = new ArrayList<>();
        List<TenantFieldsJsonCompareResponse.ModifiedField> modifiedFields = new ArrayList<>();
        
        // 4.1 找出新增字段
        for (Map<String, Object> newField : newFields) {
            String fieldKey = (String) newField.get("field_key");
            if (!currentFieldsMap.containsKey(fieldKey)) {
                addedFields.add(new TenantFieldsJsonCompareResponse.FieldInfo(
                    fieldKey,
                    (String) newField.get("field_name"),
                    (String) newField.get("field_type")
                ));
            }
        }
        
        // 4.2 找出删除字段
        for (Map<String, Object> currentField : currentFields) {
            String fieldKey = (String) currentField.get("field_key");
            if (!newFieldsMap.containsKey(fieldKey)) {
                deletedFields.add(new TenantFieldsJsonCompareResponse.FieldInfo(
                    fieldKey,
                    (String) currentField.get("field_name"),
                    (String) currentField.get("field_type")
                ));
            }
        }
        
        // 4.3 找出修改字段
        for (Map<String, Object> newField : newFields) {
            String fieldKey = (String) newField.get("field_key");
            Map<String, Object> currentField = currentFieldsMap.get(fieldKey);
            
            if (currentField != null) {
                Map<String, TenantFieldsJsonCompareResponse.FieldChange> changes = new HashMap<>();
                
                // 对比field_name
                String currentName = (String) currentField.get("field_name");
                String newName = (String) newField.get("field_name");
                if (!Objects.equals(currentName, newName)) {
                    changes.put("field_name", new TenantFieldsJsonCompareResponse.FieldChange(
                        currentName, newName
                    ));
                }
                
                // 对比field_type
                String currentType = (String) currentField.get("field_type");
                String newType = (String) newField.get("field_type");
                if (!Objects.equals(currentType, newType)) {
                    changes.put("field_type", new TenantFieldsJsonCompareResponse.FieldChange(
                        currentType, newType
                    ));
                }
                
                // 对比is_required
                Boolean currentRequired = (Boolean) currentField.get("is_required");
                Boolean newRequired = (Boolean) newField.get("is_required");
                if (!Objects.equals(currentRequired, newRequired)) {
                    changes.put("is_required", new TenantFieldsJsonCompareResponse.FieldChange(
                        currentRequired, newRequired
                    ));
                }
                
                // 对比field_group_id
                Object currentGroupId = currentField.get("field_group_id");
                Object newGroupId = newField.get("field_group_id");
                if (!Objects.equals(currentGroupId, newGroupId)) {
                    changes.put("field_group_id", new TenantFieldsJsonCompareResponse.FieldChange(
                        currentGroupId, newGroupId
                    ));
                }
                
                // 对比enum_values（如果是Enum类型）
                if ("Enum".equals(currentType) || "Enum".equals(newType)) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> currentEnumValues = 
                        (List<Map<String, Object>>) currentField.getOrDefault("enum_values", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> newEnumValues = 
                        (List<Map<String, Object>>) newField.getOrDefault("enum_values", new ArrayList<>());
                    
                    TenantFieldsJsonCompareResponse.EnumValueChange enumChange = compareEnumValues(
                        currentEnumValues, newEnumValues
                    );
                    
                    if (enumChange != null && (!enumChange.getAdded().isEmpty() || 
                        !enumChange.getDeleted().isEmpty() || !enumChange.getModified().isEmpty())) {
                        // 将EnumValueChange作为Object放入FieldChange
                        // old为当前版本的枚举值，new_为EnumValueChange对象
                        changes.put("enum_values", new TenantFieldsJsonCompareResponse.FieldChange(
                            currentEnumValues, enumChange
                        ));
                    }
                }
                
                if (!changes.isEmpty()) {
                    modifiedFields.add(new TenantFieldsJsonCompareResponse.ModifiedField(
                        fieldKey, changes
                    ));
                }
            }
        }
        
        return new TenantFieldsJsonCompareResponse(addedFields, deletedFields, modifiedFields);
    }
    
    /**
     * 对比枚举值变化
     */
    private TenantFieldsJsonCompareResponse.EnumValueChange compareEnumValues(
            List<Map<String, Object>> currentEnumValues,
            List<Map<String, Object>> newEnumValues) {
        
        Map<String, Map<String, Object>> currentMap = currentEnumValues.stream()
            .collect(Collectors.toMap(
                ev -> (String) ev.get("value"),
                ev -> ev,
                (v1, v2) -> v1
            ));
        
        Map<String, Map<String, Object>> newMap = newEnumValues.stream()
            .collect(Collectors.toMap(
                ev -> (String) ev.get("value"),
                ev -> ev,
                (v1, v2) -> v1
            ));
        
        List<Map<String, Object>> added = new ArrayList<>();
        List<Map<String, Object>> deleted = new ArrayList<>();
        List<TenantFieldsJsonCompareResponse.EnumValueModification> modified = new ArrayList<>();
        
        // 找出新增的枚举值
        for (Map<String, Object> newEnumValue : newEnumValues) {
            String value = (String) newEnumValue.get("value");
            if (!currentMap.containsKey(value)) {
                added.add(newEnumValue);
            }
        }
        
        // 找出删除的枚举值
        for (Map<String, Object> currentEnumValue : currentEnumValues) {
            String value = (String) currentEnumValue.get("value");
            if (!newMap.containsKey(value)) {
                deleted.add(currentEnumValue);
            }
        }
        
        // 找出修改的枚举值（value相同但label不同）
        for (Map<String, Object> newEnumValue : newEnumValues) {
            String value = (String) newEnumValue.get("value");
            Map<String, Object> currentEnumValue = currentMap.get(value);
            if (currentEnumValue != null) {
                String currentLabel = (String) currentEnumValue.get("label");
                String newLabel = (String) newEnumValue.get("label");
                if (!Objects.equals(currentLabel, newLabel)) {
                    modified.add(new TenantFieldsJsonCompareResponse.EnumValueModification(
                        currentEnumValue, newEnumValue
                    ));
                }
            }
        }
        
        if (added.isEmpty() && deleted.isEmpty() && modified.isEmpty()) {
            return null;
        }
        
        return new TenantFieldsJsonCompareResponse.EnumValueChange(added, deleted, modified);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadJson(Long tenantId, TenantFieldsJsonUploadRequest request, String uploadedBy) {
        log.info("========== 上传并保存JSON文件，tenantId={}, version={} ==========", tenantId, request.getVersion());
        
        // 1. 将当前版本标记为历史版本
        tenantFieldsJsonMapper.updateCurrentToHistory(tenantId);
        
        // 2. 删除最旧的历史版本（只保留1个历史版本）
        tenantFieldsJsonMapper.deleteOldestHistoryVersion(tenantId);
        
        // 3. 构建完整的JSON对象
        Map<String, Object> fieldsJson = new HashMap<>();
        fieldsJson.put("version", request.getVersion());
        fieldsJson.put("sync_time", request.getSyncTime());
        fieldsJson.put("fields", request.getFields());
        
        // 4. 解析sync_time
        LocalDateTime syncTime;
        try {
            syncTime = LocalDateTime.parse(request.getSyncTime(), ISO8601_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException(400, "sync_time格式错误，必须是ISO8601格式");
        }
        
        // 5. 保存新版本
        TenantFieldsJson newVersion = new TenantFieldsJson();
        newVersion.setTenantId(tenantId);
        newVersion.setVersion(request.getVersion());
        newVersion.setSyncTime(syncTime);
        newVersion.setFieldsJson(fieldsJson);
        newVersion.setIsCurrent(true);
        newVersion.setUploadedBy(uploadedBy);
        newVersion.setUploadedAt(LocalDateTime.now());
        
        tenantFieldsJsonMapper.insert(newVersion);
        
        // 6. 返回保存结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", newVersion.getId());
        result.put("tenant_id", newVersion.getTenantId());
        result.put("version", newVersion.getVersion());
        result.put("sync_time", newVersion.getSyncTime().toString());
        result.put("is_current", newVersion.getIsCurrent());
        result.put("uploaded_at", newVersion.getUploadedAt().toString());
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getHistoryVersions(Long tenantId) {
        log.info("========== 获取历史版本列表，tenantId={} ==========", tenantId);
        
        try {
            List<TenantFieldsJson> versions = tenantFieldsJsonMapper.selectHistoryVersions(tenantId);
            
            return versions.stream().map(version -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", version.getId());
                result.put("version", version.getVersion());
                result.put("sync_time", version.getSyncTime().toString());
                result.put("is_current", version.getIsCurrent());
                result.put("uploaded_by", version.getUploadedBy());
                result.put("uploaded_at", version.getUploadedAt() != null ? version.getUploadedAt().toString() : null);
                return result;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            // 如果表不存在或其他数据库错误，返回空列表
            log.warn("获取历史版本列表失败（可能是表不存在），返回空列表。错误：{}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getCurrentVersion(Long tenantId) {
        log.info("========== 获取当前版本JSON数据，tenantId={} ==========", tenantId);
        
        try {
            TenantFieldsJson currentVersion = tenantFieldsJsonMapper.selectCurrentVersion(tenantId);
            if (currentVersion == null) {
                // 如果没有当前版本，返回null，让Controller返回空数据
                return null;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("fetched_at", LocalDateTime.now().toString());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldsJson = (Map<String, Object>) currentVersion.getFieldsJson();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) fieldsJson.get("fields");
            result.put("fields", fields);
            
            return result;
        } catch (Exception e) {
            // 如果表不存在或其他数据库错误，记录日志并返回null
            log.warn("获取当前版本失败（可能是表不存在），返回空数据。错误：{}", e.getMessage());
            return null;
        }
    }
}

