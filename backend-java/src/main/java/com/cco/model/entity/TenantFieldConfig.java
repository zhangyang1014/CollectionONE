package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 甲方字段启用配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tenant_field_configs")
public class TenantFieldConfig extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID
     */
    private Long tenantId;

    /**
     * 字段ID（标准字段或自定义字段）
     */
    private Long fieldId;

    /**
     * 字段类型：standard/custom
     */
    private String fieldType;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 是否只读
     */
    private Boolean isReadonly;

    /**
     * 是否可见
     */
    private Boolean isVisible;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

}

