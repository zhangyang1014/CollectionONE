import request from '@/utils/request'

export function getDetailTenantFieldsJson(tenantId: number) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json`,
    method: 'get'
  })
}

export function saveDetailTenantFieldsJson(tenantId: number, data: any) {
  return request({
    url: `/api/v1/tenants/${tenantId}/detail-fields-json`,
    method: 'post',
    data
  })
}

