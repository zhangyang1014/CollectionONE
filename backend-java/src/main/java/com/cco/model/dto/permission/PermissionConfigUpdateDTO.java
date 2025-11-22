package com.cco.model.dto.permission;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 单个权限配置更新DTO
 * 对应Python: PermissionConfigUpdate
 */
@Data
public class PermissionConfigUpdateDTO {

    /**
     * 角色代码
     */
    @NotBlank(message = "角色代码不能为空")
    private String roleCode;

    /**
     * 权限项ID
     */
    @NotNull(message = "权限项ID不能为空")
    private Long permissionItemId;

    /**
     * 权限级别: none/readonly/editable
     */
    @NotBlank(message = "权限级别不能为空")
    private String permissionLevel;

}

