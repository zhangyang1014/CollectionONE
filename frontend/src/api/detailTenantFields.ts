import request from '@/utils/request'

// 获取甲方详情字段JSON（当前激活版本）
export function getDetailTenantFieldsJson(tenantId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json`,
    method: 'get',
    params: { scene: 'detail' }
  })
}

// 上传甲方详情字段JSON（创建新版本）
export function uploadDetailTenantFieldsJson(tenantId: number, data: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json/upload`,
    method: 'post',
    data
  })
}

// 获取版本历史列表
export function getDetailFieldVersions(tenantId: number, params?: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json/versions`,
    method: 'get',
    params: { ...params, scene: 'detail' }
  })
}

// 获取特定版本详情
export function getDetailFieldVersion(tenantId: number, versionId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json/versions/${versionId}`,
    method: 'get'
  })
}

// 激活指定版本
export function activateDetailFieldVersion(tenantId: number, versionId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json/versions/${versionId}/activate`,
    method: 'post'
  })
}

// 对比两个版本
export function compareDetailFieldVersions(tenantId: number, versionId1: number, versionId2: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json/versions/compare`,
    method: 'get',
    params: { version1: versionId1, version2: versionId2 }
  })
}

// 下载JSON模板
export function downloadDetailFieldTemplate() {
  return request({
    url: '/api/v1/detail-fields-json/template',
    method: 'get',
    responseType: 'blob'
  })
}

// 验证JSON格式
export function validateDetailFieldJson(data: any) {
  return request({
    url: '/api/v1/detail-fields-json/validate',
    method: 'post',
    data
  })
}
