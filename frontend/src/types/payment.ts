/**
 * 还款渠道和还款码相关类型定义
 */

// 支付类型
export type PaymentType = 'VA' | 'H5' | 'QR'

// 支付状态
export type PaymentStatus = 'PENDING' | 'PAID' | 'EXPIRED'

// 认证方式
export type AuthType = 'API_KEY' | 'BEARER' | 'BASIC'

// 请求方法
export type ApiMethod = 'GET' | 'POST'

/**
 * 还款渠道配置
 */
export interface PaymentChannel {
  id: number
  party_id: number
  channel_name: string
  channel_icon?: string
  channel_type: PaymentType
  service_provider?: string
  description?: string
  api_url: string
  api_method: ApiMethod
  auth_type: AuthType
  auth_config?: Record<string, any>
  request_params?: Record<string, any>
  is_enabled: boolean
  sort_order: number
  created_by?: number
  updated_by?: number
  created_at: string
  updated_at: string
}

/**
 * 创建/更新渠道的表单数据
 */
export interface PaymentChannelForm {
  party_id?: number
  channel_name: string
  channel_icon?: string
  channel_type: PaymentType
  service_provider?: string
  description?: string
  api_url: string
  api_method: ApiMethod
  auth_type: AuthType
  auth_config?: Record<string, any>
  request_params?: Record<string, any>
  is_enabled: boolean
  sort_order: number
}

/**
 * 简化的渠道信息（用于前端选择）
 */
export interface SimpleChannel {
  id: number
  channel_name: string
  channel_icon?: string
  channel_type: PaymentType
  service_provider?: string
  sort_order: number
}

/**
 * 还款码记录
 */
export interface PaymentCode {
  id: number
  code_no: string
  party_id: number
  channel_id: number
  case_id: number
  loan_id: number
  customer_id: number
  collector_id: number
  installment_number?: number
  amount: number
  currency: string
  payment_type: PaymentType
  payment_code?: string
  qr_image_url?: string
  status: PaymentStatus
  created_at: string
  expired_at?: string
  paid_at?: string
  third_party_order_id?: string
}

/**
 * 还款码列表项
 */
export interface PaymentCodeListItem {
  id: number
  code_no: string
  channel_name: string
  channel_icon?: string
  payment_type: PaymentType
  installment_number?: number
  amount: number
  currency: string
  status: PaymentStatus
  created_at: string
  expired_at?: string
  paid_at?: string
  remaining_seconds?: number
}

/**
 * 还款码详情
 */
export interface PaymentCodeDetail {
  code_no: string
  party_id: number
  channel_id: number
  channel_name: string
  channel_icon?: string
  service_provider?: string
  payment_type: PaymentType
  payment_code?: string
  qr_image_url?: string
  case_id: number
  case_no?: string
  loan_id: number
  loan_no?: string
  customer_name?: string
  installment_number?: number
  amount: number
  currency: string
  status: PaymentStatus
  created_at: string
  expired_at?: string
  paid_at?: string
}

/**
 * 请求还款码的参数
 */
export interface PaymentCodeRequest {
  case_id: number
  loan_id: number
  channel_id: number
  installment_number?: number
  amount: number
}

/**
 * 期数信息
 */
export interface InstallmentInfo {
  number: number
  status: 'PAID' | 'OVERDUE' | 'PENDING'
  due_date: string
  overdue_days?: number
  principal: number
  interest: number
  penalty: number
  fee: number
  total: number
}

/**
 * 期数列表响应
 */
export interface InstallmentListResponse {
  total_installments: number
  current_overdue?: number
  installments: InstallmentInfo[]
}

/**
 * 支付类型选项
 */
export const PAYMENT_TYPE_OPTIONS = [
  { label: 'VA码', value: 'VA' },
  { label: 'H5链接', value: 'H5' },
  { label: '二维码', value: 'QR' }
]

/**
 * 认证方式选项
 */
export const AUTH_TYPE_OPTIONS = [
  { label: 'API Key', value: 'API_KEY' },
  { label: 'Bearer Token', value: 'BEARER' },
  { label: 'Basic Auth', value: 'BASIC' }
]

/**
 * 请求方法选项
 */
export const API_METHOD_OPTIONS = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' }
]

/**
 * 支付状态选项
 */
export const PAYMENT_STATUS_OPTIONS = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'PENDING' },
  { label: '已支付', value: 'PAID' },
  { label: '已过期', value: 'EXPIRED' }
]

/**
 * 支付状态标签类型映射
 */
export const PAYMENT_STATUS_TAG_TYPE: Record<PaymentStatus, string> = {
  PENDING: 'primary',
  PAID: 'success',
  EXPIRED: 'info'
}

/**
 * 支付状态文本映射
 */
export const PAYMENT_STATUS_TEXT: Record<PaymentStatus, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  EXPIRED: '已过期'
}

/**
 * 支付类型文本映射
 */
export const PAYMENT_TYPE_TEXT: Record<PaymentType, string> = {
  VA: 'VA码',
  H5: 'H5链接',
  QR: '二维码'
}

