package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段分组表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("field_groups")
public class FieldGroup extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    private String groupKey;

    /**
     * 分组名称（中文）
     */
    private String groupName;

    /**
     * 分组名称（英文）
     */
    private String groupNameEn;

    /**
     * 父分组ID
     */
    private Long parentId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

