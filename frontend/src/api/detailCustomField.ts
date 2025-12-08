import request from '@/utils/request'

export function getDetailCustomFields(params?: { tenantId?: number; fieldGroupId?: number; isActive?: boolean }) {
  return request({
    url: '/api/v1/detail-custom-fields',
    method: 'get',
    params
  })
}

export function createDetailCustomField(data: any) {
  return request({
    url: '/api/v1/detail-custom-fields',
    method: 'post',
    data
  })
}

export function updateDetailCustomField(id: number, data: any) {
  return request({
    url: `/api/v1/detail-custom-fields/${id}`,
    method: 'put',
    data
  })
}

export function deleteDetailCustomField(id: number, tenantId?: number) {
  return request({
    url: `/api/v1/detail-custom-fields/${id}`,
    method: 'delete',
    params: { tenantId }
  })
}

