package com.cco.model.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 权限矩阵响应DTO
 * 对应Python: get_permission_matrix返回值
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMatrixResponse {

    /**
     * 权限模块列表
     */
    private List<PermissionModuleDTO> modules;

    /**
     * 权限项列表
     */
    private List<PermissionItemDTO> items;

    /**
     * 权限配置列表
     */
    private List<RolePermissionConfigDTO> configs;

    /**
     * 甲方ID
     */
    private Long tenantId;

}

