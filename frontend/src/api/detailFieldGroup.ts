import request from '@/utils/request'

export function getDetailFieldGroups(params?: { tenantId?: number }) {
  return request({
    url: '/api/v1/detail-field-groups',
    method: 'get',
    params
  })
}

export function createDetailFieldGroup(data: any) {
  return request({
    url: '/api/v1/detail-field-groups',
    method: 'post',
    data
  })
}

export function updateDetailFieldGroup(id: number, data: any) {
  return request({
    url: `/api/v1/detail-field-groups/${id}`,
    method: 'put',
    data
  })
}

export function deleteDetailFieldGroup(id: number, tenantId?: number) {
  return request({
    url: `/api/v1/detail-field-groups/${id}`,
    method: 'delete',
    params: { tenantId }
  })
}

