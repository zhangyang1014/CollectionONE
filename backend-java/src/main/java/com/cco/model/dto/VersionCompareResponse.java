package com.cco.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 版本对比响应DTO
 *
 * @author CCO Team
 * @since 2025-12-08
 */
@Data
public class VersionCompareResponse {

    /**
     * 版本1信息
     */
    private VersionInfo version1;

    /**
     * 版本2信息
     */
    private VersionInfo version2;

    /**
     * 对比摘要
     */
    private Summary summary;

    /**
     * 对比详情
     */
    private Details details;

    @Data
    public static class VersionInfo {
        private Integer version;
        private LocalDateTime uploadedAt;
        private Integer fieldsCount;
    }

    @Data
    public static class Summary {
        /**
         * 新增字段数
         */
        private Integer added;

        /**
         * 删除字段数
         */
        private Integer removed;

        /**
         * 修改字段数
         */
        private Integer modified;

        /**
         * 未变更字段数
         */
        private Integer unchanged;
    }

    @Data
    public static class Details {
        /**
         * 新增的字段列表
         */
        private List<Map<String, Object>> added;

        /**
         * 删除的字段列表
         */
        private List<Map<String, Object>> removed;

        /**
         * 修改的字段列表
         */
        private List<FieldChange> modified;

        /**
         * 未变更的字段列表（可选）
         */
        private List<Map<String, Object>> unchanged;
    }

    @Data
    public static class FieldChange {
        /**
         * 字段标识
         */
        private String fieldKey;

        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 变更列表
         */
        private List<PropertyChange> changes;
    }

    @Data
    public static class PropertyChange {
        /**
         * 属性名称
         */
        private String property;

        /**
         * 旧值
         */
        private Object oldValue;

        /**
         * 新值
         */
        private Object newValue;
    }
}

