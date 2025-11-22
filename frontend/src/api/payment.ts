/**
 * 还款渠道和还款码相关API
 */
import request from '@/utils/request'

// ==================== 还款渠道配置API（管理控台） ====================

/**
 * 获取渠道列表
 */
export function getPaymentChannels(params: {
  party_id: number
  is_enabled?: boolean
  page?: number
  page_size?: number
}) {
  return request({
    url: '/api/admin/payment-channels',
    method: 'get',
    params
  })
}

/**
 * 获取渠道详情
 */
export function getPaymentChannelDetail(channelId: number) {
  return request({
    url: `/api/admin/payment-channels/${channelId}`,
    method: 'get'
  })
}

/**
 * 创建渠道
 */
export function createPaymentChannel(data: any) {
  return request({
    url: '/api/admin/payment-channels',
    method: 'post',
    data
  })
}

/**
 * 更新渠道
 */
export function updatePaymentChannel(channelId: number, data: any) {
  return request({
    url: `/api/admin/payment-channels/${channelId}`,
    method: 'put',
    data
  })
}

/**
 * 删除渠道
 */
export function deletePaymentChannel(channelId: number) {
  return request({
    url: `/api/admin/payment-channels/${channelId}`,
    method: 'delete'
  })
}

/**
 * 切换渠道启用状态
 */
export function togglePaymentChannel(channelId: number) {
  return request({
    url: `/api/admin/payment-channels/${channelId}/toggle`,
    method: 'post'
  })
}

/**
 * 批量更新渠道排序
 */
export function sortPaymentChannels(data: {
  party_id: number
  channel_ids: number[]
}) {
  return request({
    url: '/api/admin/payment-channels/sort',
    method: 'post',
    data
  })
}

// ==================== 还款码API（IM端） ====================

/**
 * 获取可用还款渠道（IM端）
 */
export function getAvailableChannels(partyId: number) {
  return request({
    url: '/api/im/payment-channels',
    method: 'get',
    params: { party_id: partyId }
  })
}

/**
 * 获取案件期数信息
 */
export function getCaseInstallments(caseId: number) {
  return request({
    url: `/api/im/cases/${caseId}/installments`,
    method: 'get'
  })
}

/**
 * 请求还款码
 */
export function requestPaymentCode(data: {
  case_id: number
  loan_id: number
  channel_id: number
  installment_number?: number
  amount: number
}) {
  return request({
    url: '/api/im/payment-codes/request',
    method: 'post',
    data
  })
}

/**
 * 查询还款码列表
 */
export function getPaymentCodes(params: {
  case_id?: number
  status?: string
  page?: number
  page_size?: number
}) {
  return request({
    url: '/api/im/payment-codes',
    method: 'get',
    params
  })
}

/**
 * 查询还款码详情
 */
export function getPaymentCodeDetail(codeNo: string) {
  return request({
    url: `/api/im/payment-codes/${codeNo}`,
    method: 'get'
  })
}

