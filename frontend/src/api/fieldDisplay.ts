/**
 * 甲方字段展示配置API
 */
import request from '@/utils/request'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'

/**
 * 获取字段展示配置列表
 */
export async function getFieldDisplayConfigs(params: {
  tenant_id?: number | string
  scene_type?: string
  field_key?: string
}): Promise<FieldDisplayConfig[]> {
  const response = await request({
    url: '/api/v1/field-display-configs',
    method: 'get',
    params
  })
  // 兼容Java后端格式（ResponseData）和直接数组格式
  return Array.isArray(response) ? response : (response.data || [])
}

/**
 * 获取指定甲方和场景的字段展示配置
 */
export async function getSceneFieldDisplayConfigs(
  tenantId: number | string,
  sceneType: 'admin_case_list' | 'collector_case_list' | 'collector_case_detail'
): Promise<FieldDisplayConfig[]> {
  const response = await request({
    url: '/api/v1/field-display-configs',
    method: 'get',
    params: {
      tenant_id: tenantId,
      scene_type: sceneType
    }
  })
  return Array.isArray(response) ? response : (response.data || [])
}

/**
 * 创建字段展示配置
 */
export function createFieldDisplayConfig(data: any) {
  return request({
    url: '/api/v1/field-display-configs',
    method: 'post',
    data
  })
}

/**
 * 更新字段展示配置
 */
export function updateFieldDisplayConfig(id: number, data: any) {
  return request({
    url: `/api/v1/field-display-configs/${id}`,
    method: 'put',
    data
  })
}

/**
 * 批量更新字段展示配置
 */
export function batchUpdateFieldDisplayConfigs(data: {
  configs: Array<{ id: number; [key: string]: any }>
}) {
  return request({
    url: '/api/v1/field-display-configs/batch',
    method: 'put',
    data
  })
}

/**
 * 删除字段展示配置
 */
export function deleteFieldDisplayConfig(id: number) {
  return request({
    url: `/api/v1/field-display-configs/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有场景类型
 */
export function getSceneTypes() {
  return request({
    url: '/api/v1/field-display-configs/scene-types',
    method: 'get'
  })
}

/**
 * 获取可用字段选项
 */
export function getAvailableFields(tenantId?: number) {
  return request({
    url: '/api/v1/field-display-configs/available-fields',
    method: 'get',
    params: tenantId ? { tenant_id: tenantId } : undefined
  })
}

/**
 * 批量创建或更新字段展示配置
 */
export function batchCreateOrUpdateConfigs(
  tenantId: number | string,
  sceneType: string,
  configs: any[]
) {
  return request({
    url: '/api/v1/field-display-configs/batch',
    method: 'post',
    data: {
      tenant_id: tenantId,
      scene_type: sceneType,
      configs: configs
    }
  })
}

/**
 * 复制场景配置
 */
export function copySceneConfig(
  fromScene: string,
  toScene: string,
  tenantId: number | string
) {
  return request({
    url: '/api/v1/field-display-configs/copy-scene',
    method: 'post',
    data: {
      from_scene: fromScene,
      to_scene: toScene,
      tenant_id: tenantId
    }
  })
}
