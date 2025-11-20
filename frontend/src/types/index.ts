// 字段分组
export interface FieldGroup {
  id: number
  group_key: string
  group_name: string
  group_name_en?: string
  parent_id?: number
  sort_order: number
  is_active: boolean
  created_at: string
  updated_at: string
}

// 标准字段
export interface StandardField {
  id: number
  field_key: string
  field_name: string
  field_name_en?: string
  field_type: string
  field_group_id: number
  is_required: boolean
  is_extended: boolean
  description?: string
  example_value?: string
  validation_rules?: any
  enum_options?: string[]
  sort_order: number
  is_active: boolean
  is_deleted: boolean
  deleted_at?: string
  created_at: string
  updated_at: string
}

// 自定义字段
export interface CustomField {
  id: number
  tenant_id: number
  field_key: string
  field_name: string
  field_name_en?: string
  field_type: string
  field_group_id: number
  is_required: boolean
  description?: string
  example_value?: string
  validation_rules?: any
  enum_options?: string[]
  sort_order: number
  is_active: boolean
  is_deleted: boolean
  deleted_at?: string
  created_at: string
  updated_at: string
}

// 甲方
export interface Tenant {
  id: number
  tenant_code: string
  tenant_name: string
  tenant_name_en?: string
  country_code?: string
  timezone: string
  currency_code: string
  is_active: boolean
  created_at: string
  updated_at: string
}

// 甲方字段配置
export interface TenantFieldConfig {
  id: number
  tenant_id: number
  field_id: number
  field_type: string
  is_enabled: boolean
  is_required: boolean
  is_readonly: boolean
  is_visible: boolean
  sort_order: number
  created_at: string
  updated_at: string
}

// 案件
export interface Case {
  id?: number
  case_id?: string
  tenant_id?: number
  loan_id?: string
  user_id?: string
  user_name?: string
  case_status?: string
  mobile_number?: string
  product_name?: string
  app_name?: string
  total_due_amount?: number
  outstanding_amount?: number
  overdue_days?: number
  contact_channels?: number
  standard_fields?: Record<string, any>
  custom_fields?: Record<string, any>
  created_at?: string
  updated_at?: string
}

