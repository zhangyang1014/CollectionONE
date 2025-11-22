package com.cco.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cco.model.dto.permission.*;
import com.cco.model.entity.PermissionModule;
import com.cco.model.entity.PermissionItem;
import com.cco.model.entity.RolePermissionConfig;

import java.util.List;
import java.util.Map;

/**
 * 权限管理Service接口
 * 对应Python: app/api/permissions.py
 */
public interface IPermissionService extends IService<RolePermissionConfig> {

    /**
     * 获取所有权限模块
     *
     * @param isActive 是否只获取启用的模块
     * @return 权限模块列表
     */
    List<PermissionModuleDTO> getPermissionModules(Boolean isActive);

    /**
     * 获取权限项列表
     *
     * @param moduleId 模块ID（可选）
     * @param isActive 是否只获取启用的（可选）
     * @return 权限项列表
     */
    List<PermissionItemDTO> getPermissionItems(Long moduleId, Boolean isActive);

    /**
     * 获取角色权限配置
     *
     * @param tenantId 甲方ID（可选）
     * @param roleCode 角色代码（可选）
     * @return 权限配置列表
     */
    List<RolePermissionConfigDTO> getPermissionConfigs(Long tenantId, String roleCode);

    /**
     * 批量更新权限配置
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    Map<String, Object> batchUpdatePermissionConfigs(BatchUpdatePermissionRequest request);

    /**
     * 获取可配置角色列表
     *
     * @param currentRole 当前用户角色
     * @return 可配置角色列表
     */
    ConfigurableRolesResponse getConfigurableRoles(String currentRole);

    /**
     * 获取权限矩阵
     *
     * @param tenantId 甲方ID（可选）
     * @return 权限矩阵
     */
    PermissionMatrixResponse getPermissionMatrix(Long tenantId);

    /**
     * 删除权限配置
     *
     * @param configId 配置ID
     */
    void deletePermissionConfig(Long configId);

}

