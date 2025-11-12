import request from '@/utils/request'

// 案件API
export function getCases(params: any) {
  return request({
    url: '/api/v1/cases',
    method: 'get',
    params,
  })
}

export function getCase(id: number) {
  return request({
    url: `/api/v1/cases/${id}`,
    method: 'get',
  })
}

export function createCase(data: any) {
  return request({
    url: '/api/v1/cases',
    method: 'post',
    data,
  })
}

export function updateCase(id: number, data: any) {
  return request({
    url: `/api/v1/cases/${id}`,
    method: 'put',
    data,
  })
}

export function deleteCase(id: number) {
  return request({
    url: `/api/v1/cases/${id}`,
    method: 'delete',
  })
}

export function syncCases(data: any) {
  return request({
    url: '/api/v1/cases/sync',
    method: 'post',
    data,
  })
}

