package com.cco.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 案件标准字段只读视图对象
 */
@Data
@Builder
public class CaseStandardFieldVO {
    private Long id;
    private String sceneType;
    private String sceneName;
    private String fieldKey;
    private String fieldName;
    private String fieldDataType;
    private String fieldSource;
    private Integer sortOrder;
    private Integer displayWidth;
    private Boolean required;
    private Boolean searchable;
    private Boolean filterable;
    private Boolean rangeSearchable;
    private String colorType;
    private String description;
}
