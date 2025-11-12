/**
 * 通知配置管理 API
 */
import request from '@/utils/request'
import type {
  NotificationConfig,
  NotificationConfigCreate,
  NotificationConfigUpdate,
  PublicNotification,
  PublicNotificationCreate,
  PublicNotificationUpdate
} from '@/types/notification'

/**
 * 获取通知配置列表
 */
export function getNotificationConfigs(params?: {
  tenant_id?: number
  notification_type?: string
}) {
  return request<NotificationConfig[]>({
    url: '/api/v1/notification-configs',
    method: 'get',
    params
  })
}

/**
 * 获取单个通知配置
 */
export function getNotificationConfig(configId: number) {
  return request<NotificationConfig>({
    url: `/api/v1/notification-configs/${configId}`,
    method: 'get'
  })
}

/**
 * 创建通知配置
 */
export function createNotificationConfig(data: NotificationConfigCreate) {
  return request<NotificationConfig>({
    url: '/api/v1/notification-configs',
    method: 'post',
    data
  })
}

/**
 * 更新通知配置
 */
export function updateNotificationConfig(configId: number, data: NotificationConfigUpdate) {
  return request<NotificationConfig>({
    url: `/api/v1/notification-configs/${configId}`,
    method: 'put',
    data
  })
}

/**
 * 删除通知配置
 */
export function deleteNotificationConfig(configId: number) {
  return request({
    url: `/api/v1/notification-configs/${configId}`,
    method: 'delete'
  })
}

// ===== 公共通知 API =====

/**
 * 获取公共通知列表
 */
export function getPublicNotifications(params?: {
  tenant_id?: number
  agency_id?: number
  is_enabled?: boolean
}) {
  return request<PublicNotification[]>({
    url: '/api/v1/public-notifications',
    method: 'get',
    params
  })
}

/**
 * 获取单个公共通知
 */
export function getPublicNotification(id: number) {
  return request<PublicNotification>({
    url: `/api/v1/public-notifications/${id}`,
    method: 'get'
  })
}

/**
 * 创建公共通知
 */
export function createPublicNotification(data: PublicNotificationCreate) {
  return request<PublicNotification>({
    url: '/api/v1/public-notifications',
    method: 'post',
    data
  })
}

/**
 * 更新公共通知
 */
export function updatePublicNotification(id: number, data: PublicNotificationUpdate) {
  return request<PublicNotification>({
    url: `/api/v1/public-notifications/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公共通知
 */
export function deletePublicNotification(id: number) {
  return request({
    url: `/api/v1/public-notifications/${id}`,
    method: 'delete'
  })
}

/**
 * 更新公共通知排序
 */
export function updatePublicNotificationSort(id: number, sortOrder: number) {
  return request({
    url: `/api/v1/public-notifications/${id}/sort`,
    method: 'put',
    params: { sort_order: sortOrder }
  })
}


