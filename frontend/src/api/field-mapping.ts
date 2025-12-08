/**
 * 案件列表字段映射配置 API
 * 用于标准字段与甲方字段的映射管理
 */

import request from '@/utils/request'

// ==================== 字段映射配置接口 ====================

/**
 * 获取字段映射列表
 */
export function getFieldConfigs(tenantId: number, params?: {
  field_group_id?: number
  mapping_status?: 'unmapped' | 'auto_mapped' | 'manual_mapped'
}) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs`,
    method: 'get',
    params
  })
}

/**
 * 创建或更新字段映射
 */
export function saveFieldConfig(tenantId: number, data: {
  field_key: string
  tenant_field_key?: string
  tenant_field_id?: string
  mapping_status?: string
  enum_mapping?: Record<string, string>
}) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs`,
    method: 'put',
    data
  })
}

/**
 * 删除字段映射
 */
export function deleteFieldConfig(tenantId: number, id: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs/${id}`,
    method: 'delete'
  })
}

/**
 * 一键建议映射未匹配字段
 */
export function autoSuggestMapping(tenantId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs/auto-suggest`,
    method: 'post'
  })
}

/**
 * 批量确认映射建议
 */
export function batchConfirmMapping(tenantId: number, mappings: Array<{
  field_key: string
  tenant_field_key: string
  tenant_field_id?: string
  mapping_status?: string
}>) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs/batch-confirm`,
    method: 'post',
    data: { mappings }
  })
}

// ==================== 拓展字段管理接口 ====================

/**
 * 获取拓展字段列表
 */
export function getExtendedFields(tenantId: number, params?: {
  field_group_id?: number
}) {
  return request({
    url: `/api/v1/tenants/${tenantId}/extended-fields`,
    method: 'get',
    params
  })
}

/**
 * 创建拓展字段
 */
export function createExtendedField(tenantId: number, data: {
  field_alias: string
  tenant_field_key: string
  tenant_field_name: string
  field_type: string
  field_group_id?: number
  privacy_label: 'PII' | '敏感' | '公开'
  is_required?: boolean
  description?: string
}) {
  return request({
    url: `/api/v1/tenants/${tenantId}/extended-fields`,
    method: 'post',
    data
  })
}

/**
 * 更新拓展字段
 */
export function updateExtendedField(tenantId: number, id: number, data: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/extended-fields/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除拓展字段
 */
export function deleteExtendedField(tenantId: number, id: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/extended-fields/${id}`,
    method: 'delete'
  })
}

// ==================== 未使用字段接口 ====================

/**
 * 获取未使用的甲方字段列表
 */
export function getUnmappedFields(tenantId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/unmapped-fields`,
    method: 'get'
  })
}

// ==================== 甲方字段版本管理接口 ====================

/**
 * 获取甲方字段JSON（当前生效版本）
 */
export function getTenantFieldsJson(tenantId: number, scene: 'list' | 'detail' = 'list') {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json`,
    method: 'get',
    params: { scene }
  })
}

/**
 * 上传甲方字段JSON文件
 */
export function uploadFieldsJson(tenantId: number, formData: FormData) {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/upload`,
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 获取上传历史记录
 */
export function getFieldsJsonHistory(tenantId: number, params: {
  scene: 'list' | 'detail'
  page?: number
  page_size?: number
}) {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/history`,
    method: 'get',
    params
  })
}

/**
 * 获取特定版本详情
 */
export function getFieldsJsonVersion(tenantId: number, version: number, scene: 'list' | 'detail' = 'list') {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/version/${version}`,
    method: 'get',
    params: { scene }
  })
}

/**
 * 版本对比
 */
export function compareFieldsJsonVersions(
  tenantId: number, 
  version1: number, 
  version2: number, 
  scene: 'list' | 'detail' = 'list'
) {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/compare`,
    method: 'get',
    params: { scene, version1, version2 }
  })
}

/**
 * 设置当前版本
 */
export function activateFieldsJsonVersion(
  tenantId: number, 
  version: number, 
  data: {
    operator_id: string
    reason?: string
  },
  scene: 'list' | 'detail' = 'list'
) {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/activate/${version}`,
    method: 'put',
    params: { scene },
    data
  })
}

/**
 * 下载历史版本JSON
 */
export function downloadFieldsJsonVersion(tenantId: number, version: number, scene: 'list' | 'detail' = 'list') {
  return request({
    url: `/api/v1/tenants/${tenantId}/fields-json/download/${version}`,
    method: 'get',
    params: { scene },
    responseType: 'blob'
  })
}

/**
 * 下载JSON模板
 */
export function downloadFieldsJsonTemplate(scene: 'list' | 'detail' = 'list') {
  return request({
    url: '/api/v1/tenants/fields-json/template',
    method: 'get',
    params: { scene },
    responseType: 'blob'
  })
}
