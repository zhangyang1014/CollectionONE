package com.cco.controller;

import com.cco.common.constant.Constants;
import com.cco.common.response.ResponseData;
import com.cco.model.dto.permission.*;
import com.cco.service.IPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理Controller
 * 对应Python: app/api/permissions.py
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_V1_PREFIX + "/permissions")
public class PermissionController {

    @Autowired
    private IPermissionService permissionService;

    /**
     * 获取所有权限模块
     * 对应Python: @router.get("/modules")
     *
     * @param isActive 是否只获取启用的模块
     * @return 权限模块列表
     */
    @GetMapping("/modules")
    public ResponseData<List<PermissionModuleDTO>> getPermissionModules(
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("获取权限模块列表，isActive={}", isActive);
        List<PermissionModuleDTO> modules = permissionService.getPermissionModules(isActive);
        return ResponseData.success(modules);
    }

    /**
     * 获取权限项列表
     * 对应Python: @router.get("/items")
     *
     * @param moduleId 模块ID（可选）
     * @param isActive 是否只获取启用的权限项（可选）
     * @return 权限项列表
     */
    @GetMapping("/items")
    public ResponseData<List<PermissionItemDTO>> getPermissionItems(
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("获取权限项列表，moduleId={}, isActive={}", moduleId, isActive);
        List<PermissionItemDTO> items = permissionService.getPermissionItems(moduleId, isActive);
        return ResponseData.success(items);
    }

    /**
     * 获取角色权限配置
     * 对应Python: @router.get("/configs")
     *
     * @param tenantId 甲方ID（可选，NULL表示系统默认配置）
     * @param roleCode 角色代码（可选）
     * @return 权限配置列表
     */
    @GetMapping("/configs")
    public ResponseData<List<RolePermissionConfigDTO>> getPermissionConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String roleCode
    ) {
        log.info("获取权限配置，tenantId={}, roleCode={}", tenantId, roleCode);
        List<RolePermissionConfigDTO> configs = permissionService.getPermissionConfigs(tenantId, roleCode);
        return ResponseData.success(configs);
    }

    /**
     * 批量更新权限配置
     * 对应Python: @router.put("/configs")
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @PutMapping("/configs")
    public ResponseData<Map<String, Object>> updatePermissionConfigs(
            @Valid @RequestBody BatchUpdatePermissionRequest request
    ) {
        log.info("批量更新权限配置，tenantId={}, updates.size={}",
                request.getTenantId(),
                request.getUpdates() != null ? request.getUpdates().size() : 0);
        Map<String, Object> result = permissionService.batchUpdatePermissionConfigs(request);
        return ResponseData.success(result);
    }

    /**
     * 获取可配置角色列表
     * 对应Python: @router.get("/configurable-roles")
     *
     * @param currentRole 当前用户角色
     * @return 可配置角色列表
     */
    @GetMapping("/configurable-roles")
    public ResponseData<ConfigurableRolesResponse> getConfigurableRoles(
            @RequestParam(name = "current_role") String currentRole
    ) {
        log.info("获取可配置角色列表，currentRole={}", currentRole);
        ConfigurableRolesResponse response = permissionService.getConfigurableRoles(currentRole);
        return ResponseData.success(response);
    }

    /**
     * 获取权限矩阵（用于前端展示）
     * 对应Python: @router.get("/matrix")
     *
     * @param tenantId 甲方ID（可选，NULL表示系统默认配置）
     * @return 权限矩阵数据
     */
    @GetMapping("/matrix")
    public ResponseData<PermissionMatrixResponse> getPermissionMatrix(
            @RequestParam(required = false) Long tenantId
    ) {
        log.info("获取权限矩阵，tenantId={}", tenantId);
        PermissionMatrixResponse matrix = permissionService.getPermissionMatrix(tenantId);
        return ResponseData.success(matrix);
    }

    /**
     * 删除权限配置
     * 对应Python: @router.delete("/configs/{config_id}")
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/configs/{configId}")
    public ResponseData<Map<String, Object>> deletePermissionConfig(
            @PathVariable Long configId
    ) {
        log.info("删除权限配置，configId={}", configId);
        permissionService.deletePermissionConfig(configId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置已删除");
        return ResponseData.success(result);
    }

}

