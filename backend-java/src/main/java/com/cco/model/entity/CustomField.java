package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 自定义字段定义表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "custom_fields", autoResultMap = true)
public class CustomField extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属甲方ID
     */
    private Long tenantId;

    /**
     * 字段唯一标识
     */
    private String fieldKey;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段名称（英文）
     */
    private String fieldNameEn;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 所属分组ID
     */
    private Long fieldGroupId;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 字段说明
     */
    private String description;

    /**
     * 示例值
     */
    private String exampleValue;

    /**
     * 验证规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> validationRules;

    /**
     * 枚举选项（如果是Enum类型）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> enumOptions;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 软删除标记
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

}

