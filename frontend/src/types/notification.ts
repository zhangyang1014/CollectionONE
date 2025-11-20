/**
 * 通知配置相关类型定义
 */

export type NotificationType = 'unreplied' | 'nudge' | 'case_update' | 'performance' | 'timeout'

export interface NotificationConfig {
  id: number
  tenant_id?: number | null
  notification_type: NotificationType
  is_enabled: boolean
  config_data: NotificationConfigData
  created_at: string
  updated_at: string
}

// 通知配置数据（根据类型不同而不同）
export interface NotificationConfigData {
  // 通用配置
  notify_roles?: string[]
  notify_channels?: string[]
  priority?: 'high' | 'medium' | 'low'
  template?: string
  
  // unreplied 特定配置
  trigger_delay_minutes?: number
  monitored_channels?: string[]
  repeat_interval_minutes?: number
  max_notify_count?: number
  notify_time_range?: {
    type: 'working_hours' | 'all_day' | 'custom'
    custom_start?: string
    custom_end?: string
  }
  
  // nudge 特定配置
  ptp?: {
    advance_notify_minutes: number
    repeat_interval_minutes: number
    max_notify_count: number
    notify_roles: string[]
  }
  follow_up?: {
    advance_notify_minutes: number
    repeat_interval_minutes: number
    max_notify_count: number
    notify_roles: string[]
  }
  
  // case_update 特定配置
  case_assigned?: {
    enabled: boolean
    notify_roles: string[]
    template: string
  }
  payment_received?: {
    enabled: boolean
    amount_threshold?: number | null
    notify_roles: string[]
    template: string
  }
  tag_updated?: {
    enabled: boolean
    notify_roles: string[]
    template: string
  }
  
  // performance 特定配置
  amount_threshold?: number
  notify_scope?: 'self' | 'team' | 'agency' | 'all'
  notify_frequency_minutes?: number
  
  // timeout 特定配置
  timeout_levels?: Array<{
    minutes: number
    repeat_interval_minutes: number
    notify_roles: string[]
  }>
  escalation_minutes?: number
}

export interface NotificationConfigCreate {
  tenant_id?: number | null
  notification_type: NotificationType
  is_enabled: boolean
  config_data: NotificationConfigData
}

export interface NotificationConfigUpdate {
  is_enabled?: boolean
  config_data?: NotificationConfigData
}

// ===== 公共通知类型定义 =====
export interface PublicNotification {
  id: number
  tenant_id?: number | null
  agency_id?: number | null
  title: string
  content: string
  h5_content?: string | null
  carousel_interval_seconds: number
  is_forced_read: boolean
  is_enabled: boolean
  
  // 非强制阅读时的配置
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  
  effective_start_time?: string | null
  effective_end_time?: string | null
  notify_roles?: string[]
  sort_order: number
  created_at: string
  updated_at: string
  created_by?: number | null
}

export interface PublicNotificationCreate {
  tenant_id?: number | null
  agency_id?: number | null
  title: string
  content: string
  h5_content?: string | null
  carousel_interval_seconds?: number
  is_forced_read?: boolean
  is_enabled?: boolean
  
  // 非强制阅读时的配置
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  
  effective_start_time?: string | null
  effective_end_time?: string | null
  notify_roles?: string[]
  sort_order?: number
}

export interface PublicNotificationUpdate {
  title?: string
  content?: string
  h5_content?: string | null
  carousel_interval_seconds?: number
  is_forced_read?: boolean
  is_enabled?: boolean
  
  // 非强制阅读时的配置
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  
  effective_start_time?: string | null
  effective_end_time?: string | null
  notify_roles?: string[]
  sort_order?: number
}

// ===== 通知模板类型定义 =====
export interface NotificationTemplate {
  id: number
  tenant_id?: string | null
  template_id: string
  template_name: string
  template_type: string
  description?: string | null
  content_template: string
  jump_url_template?: string | null
  
  // 发送对象配置
  target_type: 'agency' | 'team' | 'collector'
  target_agencies?: string[] | null
  target_teams?: string[] | null
  target_collectors?: string[] | null
  
  // 阅读机制配置
  is_forced_read: boolean
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  
  // 优先级和展示
  priority: number  // 1=最高 2=中等 3=最低
  display_duration_seconds: number
  
  // 启用状态
  is_enabled: boolean
  
  // 可用变量
  available_variables?: string[] | null
  
  // 统计信息
  total_sent: number
  total_read: number
  
  created_at: string
  updated_at: string
  created_by?: number | null
}

export interface NotificationTemplateCreate {
  tenant_id?: string | null
  template_id: string
  template_name: string
  template_type: string
  description?: string | null
  content_template: string
  jump_url_template?: string | null
  target_type?: 'agency' | 'team' | 'collector'
  target_agencies?: string[] | null
  target_teams?: string[] | null
  target_collectors?: string[] | null
  is_forced_read?: boolean
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  priority?: number
  display_duration_seconds?: number
  is_enabled?: boolean
  available_variables?: string[] | null
}

export interface NotificationTemplateUpdate {
  template_name?: string
  template_type?: string
  description?: string | null
  content_template?: string
  jump_url_template?: string | null
  target_type?: 'agency' | 'team' | 'collector'
  target_agencies?: string[] | null
  target_teams?: string[] | null
  target_collectors?: string[] | null
  is_forced_read?: boolean
  repeat_interval_minutes?: number | null
  max_remind_count?: number | null
  notify_time_start?: string | null
  notify_time_end?: string | null
  priority?: number
  display_duration_seconds?: number
  is_enabled?: boolean
  available_variables?: string[] | null
}

export interface TemplateType {
  value: string
  label: string
  description: string
  variables: Record<string, string>
}


