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
  h5_content: string
  h5_content_type: 'url' | 'html'
  carousel_interval_seconds: number
  is_forced_read: boolean
  is_enabled: boolean
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
  h5_content: string
  h5_content_type?: 'url' | 'html'
  carousel_interval_seconds?: number
  is_forced_read?: boolean
  is_enabled?: boolean
  effective_start_time?: string | null
  effective_end_time?: string | null
  notify_roles?: string[]
  sort_order?: number
}

export interface PublicNotificationUpdate {
  title?: string
  h5_content?: string
  h5_content_type?: 'url' | 'html'
  carousel_interval_seconds?: number
  is_forced_read?: boolean
  is_enabled?: boolean
  effective_start_time?: string | null
  effective_end_time?: string | null
  notify_roles?: string[]
  sort_order?: number
}


