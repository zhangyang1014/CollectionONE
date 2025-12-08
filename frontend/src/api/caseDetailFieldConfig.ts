/**
 * 案件详情字段配置API
 * 负责控台案件详情和IM端案件详情的字段配置
 * 
 * @author CCO Team
 * @since 2025-12-07
 */

import request from '@/utils/request'

/**
 * 获取案件详情字段配置
 */
export function getCaseDetailFieldConfigs(params?: {
  tenantId?: number
  sceneType?: string
  fieldKey?: string
}) {
  return request({
    url: '/api/v1/case-detail-field-configs',
    method: 'get',
    params
  })
}

/**
 * 批量保存案件详情字段配置
 */
export function batchSaveCaseDetailFieldConfigs(data: {
  tenant_id: number
  scene_type: string
  configs: any[]
}) {
  return request({
    url: '/api/v1/case-detail-field-configs/batch',
    method: 'post',
    data
  })
}

/**
 * 复制案件详情场景配置
 */
export function copyCaseDetailScene(data: {
  tenant_id: number
  from_scene: string
  to_scene: string
}) {
  return request({
    url: '/api/v1/case-detail-field-configs/copy-scene',
    method: 'post',
    data
  })
}

/**
 * 获取案件详情场景类型列表
 */
export function getCaseDetailSceneTypes() {
  return request({
    url: '/api/v1/case-detail-field-configs/scene-types',
    method: 'get'
  })
}

/**
 * 获取案件详情可用字段选项
 */
export function getAvailableFieldsForDetail(params?: {
  tenantId?: number
}) {
  return request({
    url: '/api/v1/case-detail-field-configs/available-fields',
    method: 'get',
    params
  })
}

