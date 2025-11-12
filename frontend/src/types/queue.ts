/**
 * 队列管理相关类型定义
 */

// ===== 案件队列 =====
export interface CaseQueue {
  id: number
  tenant_id: number
  queue_code: string
  queue_name: string
  queue_name_en?: string
  queue_description?: string
  overdue_days_min?: number
  overdue_days_max?: number
  sort_order: number
  is_active: boolean
  case_count: number
  configured_field_count: number
  created_at: string
  updated_at: string
}

export interface CaseQueueCreate {
  tenant_id: number
  queue_code: string
  queue_name: string
  queue_name_en?: string
  queue_description?: string
  overdue_days_min?: number
  overdue_days_max?: number
  sort_order?: number
  is_active?: boolean
}

export interface CaseQueueUpdate {
  queue_name?: string
  queue_name_en?: string
  queue_description?: string
  overdue_days_min?: number
  overdue_days_max?: number
  sort_order?: number
  is_active?: boolean
}

// ===== 队列字段配置 =====
export interface QueueFieldConfig {
  id: number
  queue_id: number
  field_id: number
  field_type: 'standard' | 'custom'
  is_visible: boolean
  is_required?: boolean | null
  is_readonly: boolean
  is_editable: boolean
  sort_order: number
  field_key?: string
  field_name?: string
  field_data_type?: string
  field_source: string
  created_at: string
  updated_at: string
}

export interface QueueFieldConfigCreate {
  queue_id: number
  field_id: number
  field_type: 'standard' | 'custom'
  is_visible?: boolean
  is_required?: boolean | null
  is_readonly?: boolean
  is_editable?: boolean
  sort_order?: number
}

export interface QueueFieldConfigUpdate {
  is_visible?: boolean
  is_required?: boolean | null
  is_readonly?: boolean
  is_editable?: boolean
  sort_order?: number
}

export interface BatchUpdateQueueFieldConfig {
  fields: Array<{
    field_id: number
    field_type: 'standard' | 'custom'
    is_visible: boolean
    is_required?: boolean | null
    is_readonly: boolean
    is_editable: boolean
    sort_order: number
  }>
}

export interface CopyQueueFieldConfigRequest {
  source_queue_id: number
  copy_mode: 'merge' | 'replace'
}

// ===== 运行时字段配置 =====
export interface CaseFieldConfig {
  field_id: number
  field_key: string
  field_name: string
  field_type: string
  is_visible: boolean
  is_required?: boolean | null
  is_readonly: boolean
  is_editable: boolean
  sort_order: number
  value?: string
}

export interface FieldGroup {
  group_id: number
  group_name: string
  fields: CaseFieldConfig[]
}

export interface CaseFieldConfigResponse {
  case_id: string
  queue_id?: number
  queue_name?: string
  fields_by_group: FieldGroup[]
}

