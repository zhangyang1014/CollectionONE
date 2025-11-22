/**
 * 权限管理 API
 */
import request from '@/utils/request'
import type {
  PermissionModule,
  PermissionItem,
  RolePermissionConfig,
  PermissionMatrixData,
  BatchUpdatePermissionRequest,
  ConfigurableRolesResponse
} from '@/types/permission'

/**
 * 获取所有权限模块
 */
export function getPermissionModules(isActive?: boolean) {
  return request<PermissionModule[]>({
    url: '/api/v1/permissions/modules',
    method: 'get',
    params: { is_active: isActive }
  })
}

/**
 * 获取权限项列表
 */
export function getPermissionItems(moduleId?: number, isActive?: boolean) {
  return request<PermissionItem[]>({
    url: '/api/v1/permissions/items',
    method: 'get',
    params: {
      module_id: moduleId,
      is_active: isActive
    }
  })
}

/**
 * 获取角色权限配置
 */
export function getPermissionConfigs(tenantId?: number | null, roleCode?: string) {
  return request<RolePermissionConfig[]>({
    url: '/api/v1/permissions/configs',
    method: 'get',
    params: {
      tenant_id: tenantId,
      role_code: roleCode
    }
  })
}

/**
 * 批量更新权限配置
 */
export function batchUpdatePermissionConfigs(data: BatchUpdatePermissionRequest) {
  return request<{
    success: boolean
    message: string
    updated: number
    created: number
    errors?: string[]
  }>({
    url: '/api/v1/permissions/configs',
    method: 'put',
    data
  })
}

/**
 * 获取当前角色可配置的角色列表
 */
export function getConfigurableRoles(currentRole: string) {
  return request<ConfigurableRolesResponse>({
    url: '/api/v1/permissions/configurable-roles',
    method: 'get',
    params: { current_role: currentRole }
  })
}

/**
 * 获取完整的权限矩阵数据
 */
export function getPermissionMatrix(tenantId?: number | null) {
  return request<PermissionMatrixData & { tenant_id?: number | null }>({
    url: '/api/v1/permissions/matrix',
    method: 'get',
    params: { tenant_id: tenantId }
  })
}

/**
 * 删除权限配置
 */
export function deletePermissionConfig(configId: number) {
  return request<{ success: boolean; message: string }>({
    url: `/api/v1/permissions/configs/${configId}`,
    method: 'delete'
  })
}

