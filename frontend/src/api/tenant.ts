import request from '@/utils/request'

// 甲方API
export function getTenants(params?: any) {
  return request({
    url: '/api/v1/tenants',
    method: 'get',
    params,
  })
}

export function getTenant(id: number) {
  return request({
    url: `/api/v1/tenants/${id}`,
    method: 'get',
  })
}

export function createTenant(data: any) {
  return request({
    url: '/api/v1/tenants',
    method: 'post',
    data,
  })
}

export function updateTenant(id: number, data: any) {
  return request({
    url: `/api/v1/tenants/${id}`,
    method: 'put',
    data,
  })
}

export function deleteTenant(id: number) {
  return request({
    url: `/api/v1/tenants/${id}`,
    method: 'delete',
  })
}

// 甲方字段配置API
export function getTenantFieldConfigs(tenantId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs`,
    method: 'get',
  })
}

export function createTenantFieldConfig(tenantId: number, data: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs`,
    method: 'post',
    data,
  })
}

export function updateTenantFieldConfig(tenantId: number, configId: number, data: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs/${configId}`,
    method: 'put',
    data,
  })
}

export function deleteTenantFieldConfig(tenantId: number, configId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/field-configs/${configId}`,
    method: 'delete',
  })
}

