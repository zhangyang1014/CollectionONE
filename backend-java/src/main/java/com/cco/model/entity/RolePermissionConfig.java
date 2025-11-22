package com.cco.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色权限配置表
 * 对应Python: app/models/permission.py - RolePermissionConfig
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_permission_configs")
public class RolePermissionConfig extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 甲方ID，NULL表示系统默认配置
     */
    private Long tenantId;

    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 权限项ID
     */
    private Long permissionItemId;

    /**
     * 权限级别: none/readonly/editable
     */
    private String permissionLevel;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

}

