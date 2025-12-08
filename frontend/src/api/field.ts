import request from '@/utils/request'

// 字段分组API
export function getFieldGroups(params?: any) {
  return request({
    url: '/api/v1/field-groups',
    method: 'get',
    params,
  })
}

export function getFieldGroup(id: number) {
  return request({
    url: `/api/v1/field-groups/${id}`,
    method: 'get',
  })
}

export function createFieldGroup(data: any) {
  return request({
    url: '/api/v1/field-groups',
    method: 'post',
    data,
  })
}

export function updateFieldGroup(id: number, data: any) {
  return request({
    url: `/api/v1/field-groups/${id}`,
    method: 'put',
    data,
  })
}

export function deleteFieldGroup(id: number) {
  return request({
    url: `/api/v1/field-groups/${id}`,
    method: 'delete',
  })
}

export function updateFieldGroupsSort(data: { id: number; sort_order: number }[]) {
  return request({
    url: '/api/v1/field-groups/sort',
    method: 'put',
    data: { groups: data },
  })
}

// 标准字段API
export function getStandardFields(params?: any) {
  return request({
    url: '/api/v1/standard-fields',
    method: 'get',
    params,
  })
}

export function getStandardField(id: number) {
  return request({
    url: `/api/v1/standard-fields/${id}`,
    method: 'get',
  })
}

export function createStandardField(data: any) {
  return request({
    url: '/api/v1/standard-fields',
    method: 'post',
    data,
  })
}

export function updateStandardField(id: number, data: any) {
  return request({
    url: `/api/v1/standard-fields/${id}`,
    method: 'put',
    data,
  })
}

export function deleteStandardField(id: number) {
  return request({
    url: `/api/v1/standard-fields/${id}`,
    method: 'delete',
  })
}

// 案件列表标准字段（只读）
export function getCaseListStandardFields() {
  return request({
    url: '/api/v1/standard-fields/case-list',
    method: 'get',
  })
}

// 案件详情标准字段（只读）
export function getCaseDetailStandardFields() {
  return request({
    url: '/api/v1/standard-fields/case-detail',
    method: 'get',
  })
}

// 自定义字段API
export function getCustomFields(params: any) {
  return request({
    url: '/api/v1/custom-fields',
    method: 'get',
    params,
  })
}

export function getCustomField(id: number) {
  return request({
    url: `/api/v1/custom-fields/${id}`,
    method: 'get',
  })
}

export function createCustomField(data: any) {
  return request({
    url: '/api/v1/custom-fields',
    method: 'post',
    data,
  })
}

export function updateCustomField(id: number, data: any) {
  return request({
    url: `/api/v1/custom-fields/${id}`,
    method: 'put',
    data,
  })
}

export function deleteCustomField(id: number) {
  return request({
    url: `/api/v1/custom-fields/${id}`,
    method: 'delete',
  })
}

