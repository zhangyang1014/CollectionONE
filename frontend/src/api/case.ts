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

/**
 * 批量标记案件为停留
 * @param caseIds 案件ID列表
 */
export function batchStayCases(caseIds: number[]) {
  return request({
    url: '/api/v1/cases/batch-stay',
    method: 'post',
    data: {
      caseIds: caseIds,  // Java后端使用驼峰命名
    },
  })
}

/**
 * 批量解放停留案件
 * @param caseIds 案件ID列表
 */
export function batchReleaseStayCases(caseIds: number[]) {
  return request({
    url: '/api/v1/cases/batch-release-stay',
    method: 'post',
    data: {
      caseIds: caseIds,  // Java后端使用驼峰命名
    },
  })
}

/**
 * 获取停留案件列表
 * @param params 查询参数
 */
export function getStayCases(params: any) {
  return request({
    url: '/api/v1/cases/stay',
    method: 'get',
    params,
  })
}

// ===== 案件重新分案配置 API =====

/**
 * 获取重新分案配置列表
 */
export function getCaseReassignConfigs(params?: {
  tenant_id?: number
  config_type?: string
}) {
  // 后端使用驼峰命名，需要转换
  const backendParams: any = {}
  if (params?.tenant_id) {
    backendParams.tenantId = params.tenant_id
  }
  if (params?.config_type) {
    backendParams.configType = params.config_type
  }
  return request({
    url: '/api/v1/case-reassign-configs',
    method: 'get',
    params: backendParams,
  })
}

/**
 * 获取重新分案配置详情
 */
export function getCaseReassignConfig(id: number) {
  return request({
    url: `/api/v1/case-reassign-configs/${id}`,
    method: 'get',
  })
}

/**
 * 创建重新分案配置
 */
export function createCaseReassignConfig(data: {
  tenant_id: number
  config_type: 'queue' | 'agency' | 'team'
  target_id: number
  team_ids?: number[]
  reassign_days: number
  is_active?: boolean
}, replace?: boolean) {
  // 后端使用驼峰命名，需要转换
  const params: any = {
    tenantId: data.tenant_id,
    configType: data.config_type,
    targetId: data.target_id,
    reassignDays: data.reassign_days,
    isActive: data.is_active !== undefined ? data.is_active : true
  }
  
  // 如果有teamIds，转换为JSON字符串
  if (data.team_ids && data.team_ids.length > 0) {
    params.teamIds = JSON.stringify(data.team_ids)
  }
  
  return request({
    url: '/api/v1/case-reassign-configs',
    method: 'post',
    params: replace ? { replace: 'true' } : undefined,
    data: params,
  })
}

/**
 * 更新重新分案配置
 */
export function updateCaseReassignConfig(id: number, data: {
  tenant_id?: number
  config_type?: 'queue' | 'agency' | 'team'
  target_id?: number
  team_ids?: number[]
  reassign_days?: number
  is_active?: boolean
}) {
  // 后端使用驼峰命名，需要转换
  const backendData: any = {}
  if (data.tenant_id !== undefined) {
    backendData.tenantId = data.tenant_id
  }
  if (data.config_type !== undefined) {
    backendData.configType = data.config_type
  }
  if (data.target_id !== undefined) {
    backendData.targetId = data.target_id
  }
  if (data.team_ids !== undefined) {
    if (data.team_ids && data.team_ids.length > 0) {
      backendData.teamIds = JSON.stringify(data.team_ids)
    } else {
      backendData.teamIds = null
    }
  }
  if (data.reassign_days !== undefined) {
    backendData.reassignDays = data.reassign_days
  }
  if (data.is_active !== undefined) {
    backendData.isActive = data.is_active
  }
  return request({
    url: `/api/v1/case-reassign-configs/${id}`,
    method: 'put',
    data: backendData,
  })
}

/**
 * 删除重新分案配置
 */
export function deleteCaseReassignConfig(id: number) {
  return request({
    url: `/api/v1/case-reassign-configs/${id}`,
    method: 'delete',
  })
}

