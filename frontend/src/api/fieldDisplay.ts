/**
 * 字段展示配置API
 */
import request from '@/utils/request'
import type {
  FieldDisplayConfig,
  FieldDisplayConfigCreate,
  FieldDisplayConfigUpdate,
  FieldDisplayConfigQuery,
  SceneType,
  AvailableFieldOption
} from '@/types/fieldDisplay'

/**
 * 获取所有场景类型
 */
export function getSceneTypes() {
  return request<SceneType[]>({
    url: '/api/v1/field-display-configs/scene-types',
    method: 'get'
  })
}

/**
 * 获取字段展示配置列表
 */
export function getFieldDisplayConfigs(params?: FieldDisplayConfigQuery) {
  return request<FieldDisplayConfig[]>({
    url: '/api/v1/field-display-configs',
    method: 'get',
    params
  })
}

/**
 * 获取单个字段展示配置
 */
export function getFieldDisplayConfig(id: number) {
  return request<FieldDisplayConfig>({
    url: `/api/v1/field-display-configs/${id}`,
    method: 'get'
  })
}

/**
 * 创建字段展示配置
 */
export function createFieldDisplayConfig(data: FieldDisplayConfigCreate) {
  return request<FieldDisplayConfig>({
    url: '/api/v1/field-display-configs',
    method: 'post',
    data
  })
}

/**
 * 更新字段展示配置
 */
export function updateFieldDisplayConfig(id: number, data: FieldDisplayConfigUpdate) {
  return request<FieldDisplayConfig>({
    url: `/api/v1/field-display-configs/${id}`,
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
 * 批量创建或更新字段展示配置
 */
export function batchCreateOrUpdateConfigs(
  tenantId: string,
  sceneType: string,
  configs: FieldDisplayConfigCreate[]
) {
  return request<FieldDisplayConfig[]>({
    url: '/api/v1/field-display-configs/batch',
    method: 'post',
    params: {
      tenant_id: tenantId,
      scene_type: sceneType
    },
    data: configs
  })
}

/**
 * 复制场景配置
 */
export function copySceneConfig(fromScene: string, toScene: string, tenantId: string) {
  return request({
    url: '/api/v1/field-display-configs/copy',
    method: 'post',
    params: {
      from_scene: fromScene,
      to_scene: toScene,
      tenant_id: tenantId
    }
  })
}
/**
 * 获取可用字段选项（用于添加字段配置）
 */
export function getAvailableFields(tenantId?: string) {
  return request<AvailableFieldOption[]>({
    url: '/api/v1/field-display-configs/available-fields',
    method: 'get',
    params: {
      tenant_id: tenantId
    }
  })
}


