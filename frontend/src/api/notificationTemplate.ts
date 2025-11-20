/**
 * 通知模板API
 */
import request from '@/utils/request'
import type {
  NotificationTemplate,
  NotificationTemplateCreate,
  NotificationTemplateUpdate,
  TemplateType
} from '@/types/notification'

/**
 * 获取通知模板列表
 */
export function getNotificationTemplates(params?: {
  tenant_id?: number
  template_type?: string
  is_enabled?: boolean
}) {
  return request<NotificationTemplate[]>({
    url: '/api/v1/notification-templates',
    method: 'get',
    params
  })
}

/**
 * 获取单个通知模板详情
 */
export function getNotificationTemplate(id: number) {
  return request<NotificationTemplate>({
    url: `/api/v1/notification-templates/${id}`,
    method: 'get'
  })
}

/**
 * 创建通知模板
 */
export function createNotificationTemplate(data: NotificationTemplateCreate) {
  return request<NotificationTemplate>({
    url: '/api/v1/notification-templates',
    method: 'post',
    data
  })
}

/**
 * 更新通知模板
 */
export function updateNotificationTemplate(id: number, data: NotificationTemplateUpdate) {
  return request<NotificationTemplate>({
    url: `/api/v1/notification-templates/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除通知模板
 */
export function deleteNotificationTemplate(id: number) {
  return request({
    url: `/api/v1/notification-templates/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有可用的模板类型
 */
export function getTemplateTypes() {
  return request<{ types: TemplateType[] }>({
    url: '/api/v1/notification-templates/types/list',
    method: 'get'
  })
}

