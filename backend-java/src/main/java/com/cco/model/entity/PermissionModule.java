package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限模块表
 * 对应Python: app/models/permission.py - PermissionModule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission_modules")
public class PermissionModule extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模块键（唯一标识）
     */
    private String moduleKey;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

}

