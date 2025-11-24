package com.cco.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 字段展示配置DTO
 * 
 * @author CCO Team
 * @since 2025-11-22
 */
public class FieldDisplayConfigDTO {

    /**
     * 场景类型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "场景类型")
    public static class SceneType {
        @Schema(description = "场景key")
        private String key;
        
        @Schema(description = "场景名称")
        private String name;
        
        @Schema(description = "场景描述")
        private String description;
    }

    /**
     * 可用字段
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "可用字段")
    public static class AvailableField {
        @Schema(description = "字段标识")
        private String fieldKey;
        
        @Schema(description = "字段名称")
        private String fieldName;
        
        @Schema(description = "字段类型")
        private String fieldType;
        
        @Schema(description = "字段来源: standard/custom")
        private String fieldSource;
        
        @Schema(description = "所属分组ID")
        private Long fieldGroupId;
        
        @Schema(description = "是否为拓展字段")
        private Boolean isExtended;
        
        @Schema(description = "字段描述")
        private String description;
    }

    /**
     * 创建配置DTO
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "创建字段展示配置")
    public static class Create {
        @Schema(description = "甲方ID", required = true)
        private Long tenantId;
        
        @Schema(description = "场景类型", required = true, example = "admin_case_list")
        private String sceneType;
        
        @Schema(description = "场景名称", required = true)
        private String sceneName;
        
        @Schema(description = "字段标识", required = true)
        private String fieldKey;
        
        @Schema(description = "字段名称", required = true)
        private String fieldName;
        
        @Schema(description = "字段数据类型")
        private String fieldDataType;
        
        @Schema(description = "字段来源: standard/extended/custom")
        private String fieldSource;
        
        @Schema(description = "排序顺序", example = "0")
        private Integer sortOrder = 0;
        
        @Schema(description = "显示宽度(0表示自动)", example = "0")
        private Integer displayWidth = 0;
        
        @Schema(description = "颜色类型: normal/red/yellow/green", example = "normal")
        private String colorType = "normal";
        
        @Schema(description = "颜色规则")
        private Map<String, Object> colorRule;
        
        @Schema(description = "隐藏规则")
        private Map<String, Object> hideRule;
        
        @Schema(description = "对队列隐藏(队列ID数组)")
        private List<Long> hideForQueues;
        
        @Schema(description = "对机构隐藏(机构ID数组)")
        private List<Long> hideForAgencies;
        
        @Schema(description = "对小组隐藏(小组ID数组)")
        private List<Long> hideForTeams;
        
        @Schema(description = "格式化规则")
        private Map<String, Object> formatRule;
        
        @Schema(description = "是否可搜索", example = "false")
        private Boolean isSearchable = false;
        
        @Schema(description = "是否可筛选", example = "false")
        private Boolean isFilterable = false;
        
        @Schema(description = "是否支持范围检索", example = "false")
        private Boolean isRangeSearchable = false;
        
        @Schema(description = "创建人")
        private String createdBy;
    }

    /**
     * 更新配置DTO
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "更新字段展示配置")
    public static class Update {
        @Schema(description = "字段名称")
        private String fieldName;
        
        @Schema(description = "排序顺序")
        private Integer sortOrder;
        
        @Schema(description = "显示宽度(0表示自动)")
        private Integer displayWidth;
        
        @Schema(description = "颜色类型: normal/red/yellow/green")
        private String colorType;
        
        @Schema(description = "颜色规则")
        private Map<String, Object> colorRule;
        
        @Schema(description = "隐藏规则")
        private Map<String, Object> hideRule;
        
        @Schema(description = "对队列隐藏(队列ID数组)")
        private List<Long> hideForQueues;
        
        @Schema(description = "对机构隐藏(机构ID数组)")
        private List<Long> hideForAgencies;
        
        @Schema(description = "对小组隐藏(小组ID数组)")
        private List<Long> hideForTeams;
        
        @Schema(description = "格式化规则")
        private Map<String, Object> formatRule;
        
        @Schema(description = "是否可搜索")
        private Boolean isSearchable;
        
        @Schema(description = "是否可筛选")
        private Boolean isFilterable;
        
        @Schema(description = "是否支持范围检索")
        private Boolean isRangeSearchable;
        
        @Schema(description = "更新人")
        private String updatedBy;
    }

    /**
     * 批量更新配置DTO
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "批量更新字段展示配置")
    public static class BatchUpdate {
        @Schema(description = "配置项列表", required = true)
        private List<ConfigUpdate> configs;
    }

    /**
     * 单个配置更新项
     */
    @Data
    @NoArgsConstructor
    @Schema(description = "单个配置更新项")
    public static class ConfigUpdate {
        @Schema(description = "配置ID", required = true)
        private Long id;
        
        @Schema(description = "排序顺序")
        private Integer sortOrder;
        
        @Schema(description = "显示宽度")
        private Integer displayWidth;
        
        @Schema(description = "颜色类型")
        private String colorType;
        
        @Schema(description = "是否可搜索")
        private Boolean isSearchable;
        
        @Schema(description = "是否可筛选")
        private Boolean isFilterable;
        
        @Schema(description = "是否支持范围检索")
        private Boolean isRangeSearchable;
    }
}






