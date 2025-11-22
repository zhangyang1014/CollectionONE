package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限项表
 * 对应Python: app/models/permission.py - PermissionItem
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission_items")
public class PermissionItem extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属模块ID
     */
    private Long moduleId;

    /**
     * 权限项键
     */
    private String itemKey;

    /**
     * 权限项名称
     */
    private String itemName;

    /**
     * 权限说明
     */
    private String description;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

