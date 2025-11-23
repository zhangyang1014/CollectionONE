import imRequest from '@/utils/imRequest'

/**
 * 催员端专用案件API
 * 使用imRequest确保401错误时跳转到催员登录页
 */

// 获取案件列表
export function getCases(params: any) {
  return imRequest({
    url: '/api/v1/cases',
    method: 'get',
    params,
  })
}

// 获取案件详情
export function getCase(id: number) {
  return imRequest({
    url: `/api/v1/cases/${id}`,
    method: 'get',
  })
}

// 更新案件状态
export function updateCaseStatus(id: number, data: any) {
  return imRequest({
    url: `/api/v1/cases/${id}/status`,
    method: 'put',
    data,
  })
}

// 分配案件
export function assignCase(id: number, data: any) {
  return imRequest({
    url: `/api/v1/cases/${id}/assign`,
    method: 'post',
    data,
  })
}

// 添加催记
export function addCollectionRecord(caseId: number, data: any) {
  return imRequest({
    url: `/api/v1/cases/${caseId}/collection-records`,
    method: 'post',
    data,
  })
}

// 获取催记列表
export function getCollectionRecords(caseId: number) {
  return imRequest({
    url: `/api/v1/cases/${caseId}/collection-records`,
    method: 'get',
  })
}

// 获取案件统计
export function getCaseStatistics(params: any) {
  return imRequest({
    url: '/api/v1/cases/statistics',
    method: 'get',
    params,
  })
}


