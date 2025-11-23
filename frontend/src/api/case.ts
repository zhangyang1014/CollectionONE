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

/**
 * 获取联系人电话状态
 * @param caseId 案件ID
 * @param contactId 联系人ID（可选）
 * @param phoneNumber 电话号码（可选）
 * @returns 电话状态信息
 */
export function getContactPhoneStatus(
  caseId: number,
  contactId?: number,
  phoneNumber?: string
) {
  const params: any = {}
  if (contactId !== undefined && contactId !== null) {
    params.contact_id = contactId
  }
  if (phoneNumber !== undefined && phoneNumber !== null && phoneNumber.trim() !== '') {
    params.phone_number = phoneNumber
  }
  
  // 调试日志
  console.log(`[API] 获取电话状态 - caseId: ${caseId}, params:`, params)
  
  return request({
    url: `/api/v1/cases/${caseId}/contacts/phone-status`,
    method: 'get',
    params,
  })
}

