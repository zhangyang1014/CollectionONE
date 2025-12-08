/**
 * 案件列表字段配置API
 * 负责控台案件列表和IM端案件列表的字段配置
 * 
 * @author CCO Team
 * @since 2025-12-07
 */

import request from '@/utils/request'

/**
 * 获取案件列表字段配置
 */
export function getCaseListFieldConfigs(params?: {
  tenantId?: number
  sceneType?: string
  fieldKey?: string
}) {
  return request({
    url: '/api/v1/case-list-field-configs',
    method: 'get',
    params
  })
}

/**
 * 批量保存案件列表字段配置
 */
export function batchSaveCaseListFieldConfigs(data: {
  tenant_id: number
  scene_type: string
  configs: any[]
}) {
  return request({
    url: '/api/v1/case-list-field-configs/batch',
    method: 'post',
    data
  })
}

/**
 * 复制案件列表场景配置
 */
export function copyCaseListScene(data: {
  tenant_id: number
  from_scene: string
  to_scene: string
}) {
  return request({
    url: '/api/v1/case-list-field-configs/copy-scene',
    method: 'post',
    data
  })
}

/**
 * 获取案件列表场景类型列表
 */
export function getCaseListSceneTypes() {
  return request({
    url: '/api/v1/case-list-field-configs/scene-types',
    method: 'get'
  })
}

/**
 * 获取案件列表字段配置所依赖的映射版本信息
 */
export function getCaseListFieldConfigVersion(params?: {
  tenantId?: number
  sceneType?: string
}) {
  return request({
    url: '/api/v1/case-list-field-configs/version',
    method: 'get',
    params
  })
}

/**
 * 保存展示配置为新版本
 */
export function saveCaseListFieldConfigVersion(data: {
  tenant_id: number
  scene_type: string
  configs: any[]
  operator?: string
  note?: string
}) {
  return request({
    url: '/api/v1/case-list-field-configs/version/save',
    method: 'post',
    data
  })
}

/**
 * 获取展示配置版本历史
 */
export function getCaseListFieldConfigVersionHistory(params?: {
  tenantId?: number
  sceneType?: string
}) {
  return request({
    url: '/api/v1/case-list-field-configs/version/history',
    method: 'get',
    params
  })
}

/**
 * 激活指定展示配置版本
 */
export function activateCaseListFieldConfigVersion(data: {
  tenant_id: number
  scene_type: string
  version: number
}) {
  return request({
    url: '/api/v1/case-list-field-configs/version/activate',
    method: 'post',
    data
  })
}

/**
 * 获取案件列表可用字段选项
 */
export function getAvailableFieldsForList(params?: {
  tenantId?: number
}) {
  return request({
    url: '/api/v1/case-list-field-configs/available-fields',
    method: 'get',
    params
  })
}

