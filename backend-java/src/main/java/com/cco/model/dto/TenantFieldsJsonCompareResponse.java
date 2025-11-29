package com.cco.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 版本对比响应DTO
 * 
 * @author CCO Team
 * @since 2025-11-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantFieldsJsonCompareResponse {
    
    /**
     * 新增字段列表
     */
    private List<FieldInfo> addedFields;
    
    /**
     * 删除字段列表
     */
    private List<FieldInfo> deletedFields;
    
    /**
     * 修改字段列表
     */
    private List<ModifiedField> modifiedFields;
    
    /**
     * 字段信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldInfo {
        private String fieldKey;
        private String fieldName;
        private String fieldType;
    }
    
    /**
     * 修改字段信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifiedField {
        private String fieldKey;
        private Map<String, FieldChange> changes;
    }
    
    /**
     * 字段变更信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldChange {
        private Object old;
        private Object new_;
    }
    
    /**
     * 枚举值变更信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumValueChange {
        private List<Map<String, Object>> added;
        private List<Map<String, Object>> deleted;
        private List<EnumValueModification> modified;
    }
    
    /**
     * 枚举值修改信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumValueModification {
        private Map<String, Object> old;
        private Map<String, Object> new_;
    }
}

