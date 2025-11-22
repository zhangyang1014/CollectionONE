/**
 * Infinity外呼系统相关类型定义
 */

// ==================== Infinity 配置相关 ====================

export interface InfinityCallConfig {
  id?: number
  tenant_id: number
  supplier_id?: number
  api_url: string
  access_token: string
  app_id: string
  caller_number_range_start?: string
  caller_number_range_end?: string
  callback_url?: string
  recording_callback_url?: string
  max_concurrent_calls: number
  call_timeout_seconds: number
  is_active: boolean
  created_at?: string
  updated_at?: string
  created_by?: number
}

export interface InfinityCallConfigCreate {
  tenant_id: number
  supplier_id?: number
  api_url: string
  access_token: string
  app_id: string
  caller_number_range_start?: string
  caller_number_range_end?: string
  callback_url?: string
  recording_callback_url?: string
  max_concurrent_calls?: number
  call_timeout_seconds?: number
  is_active?: boolean
}

export interface InfinityCallConfigUpdate {
  supplier_id?: number
  api_url?: string
  access_token?: string
  app_id?: string
  caller_number_range_start?: string
  caller_number_range_end?: string
  callback_url?: string
  recording_callback_url?: string
  max_concurrent_calls?: number
  call_timeout_seconds?: number
  is_active?: boolean
}

// ==================== 分机池相关 ====================

export type ExtensionStatus = 'available' | 'in_use' | 'offline'

export interface ExtensionPool {
  id: number
  tenant_id: number
  config_id: number
  infinity_extension_number: string
  status: ExtensionStatus
  current_collector_id?: number
  assigned_at?: string
  released_at?: string
  last_used_at?: string
  created_at: string
  updated_at: string
}

export interface ExtensionPoolBatchImport {
  tenant_id: number
  config_id: number
  extension_numbers: string[]
}

export interface ExtensionPoolStatistics {
  tenant_id: number
  config_id: number
  total_extensions: number
  available_count: number
  in_use_count: number
  offline_count: number
  usage_rate: number
}

// ==================== 外呼相关 ====================

export interface MakeCallRequest {
  case_id: number
  collector_id: number
  contact_number: string
  caller_number?: string
  custom_params?: Record<string, any>
}

export interface MakeCallResponse {
  success: boolean
  call_id?: number
  call_uuid?: string
  extension_number?: string
  message: string
}

export interface CallRecordCallback {
  call_uuid: string
  call_duration?: number
  is_connected: boolean
  call_record_url?: string
  contact_result?: string
  remark?: string
  custom_params?: Record<string, any>
}

export interface TestConnectionRequest {
  api_url: string
  access_token: string
}

export interface TestConnectionResponse {
  success: boolean
  message: string
  response_time_ms?: number
}

// ==================== 通话记录相关 ====================

export interface CallRecord {
  id: number
  case_id: number
  collector_id: number
  contact_person_id?: number
  channel: string
  direction: 'inbound' | 'outbound'
  supplier_id?: number
  infinity_extension_number?: string
  call_uuid?: string
  call_duration?: number
  is_connected: boolean
  call_record_url?: string
  contact_result?: string
  remark?: string
  contacted_at: string
  created_at: string
  updated_at: string
  // 关联数据
  case?: any
  collector?: any
  contact_person?: any
}

