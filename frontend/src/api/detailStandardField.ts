import request from '@/utils/request'

export function getDetailStandardFields(params?: { field_group_id?: number; tenantId?: number }) {
  return request({
    url: '/api/v1/detail-standard-fields',
    method: 'get',
    params
  })
}

export function createDetailStandardField(data: any) {
  return request({
    url: '/api/v1/detail-standard-fields',
    method: 'post',
    data
  })
}

export function updateDetailStandardField(id: number, data: any) {
  return request({
    url: `/api/v1/detail-standard-fields/${id}`,
    method: 'put',
    data
  })
}

export function deleteDetailStandardField(id: number, tenantId?: number) {
  return request({
    url: `/api/v1/detail-standard-fields/${id}`,
    method: 'delete',
    params: { tenantId }
  })
}

