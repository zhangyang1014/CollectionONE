/**
 * 队列管理 API
 */
import request from '@/utils/request'
import type {
  CaseQueue,
  CaseQueueCreate,
  CaseQueueUpdate,
  QueueFieldConfig,
  BatchUpdateQueueFieldConfig,
  CopyQueueFieldConfigRequest,
  CaseFieldConfigResponse
} from '@/types/queue'

// ===== 案件队列 API =====

/**
 * 获取队列列表
 */
export function getQueues(params: {
  tenant_id: number
  is_active?: boolean
}) {
  return request<{ items: CaseQueue[] }>({
    url: '/api/v1/queues',
    method: 'get',
    params
  })
}

/**
 * 获取指定甲方的队列列表
 */
export function getTenantQueues(tenantId: number) {
  return request<CaseQueue[]>({
    url: `/api/v1/tenants/${tenantId}/queues`,
    method: 'get'
  })
}

/**
 * 创建队列
 */
export function createQueue(data: CaseQueueCreate) {
  return request<CaseQueue>({
    url: '/api/v1/queues',
    method: 'post',
    data
  })
}

/**
 * 更新队列
 */
export function updateQueue(id: number, data: CaseQueueUpdate) {
  return request<CaseQueue>({
    url: `/api/v1/queues/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除队列
 */
export function deleteQueue(id: number) {
  return request({
    url: `/api/v1/queues/${id}`,
    method: 'delete'
  })
}

// ===== 队列字段配置 API =====

/**
 * 获取队列字段配置
 */
export function getQueueFieldConfigs(queueId: number) {
  return request<{
    queue_id: number
    queue_name: string
    fields: QueueFieldConfig[]
  }>({
    url: `/api/v1/queues/${queueId}/field-configs`,
    method: 'get'
  })
}

/**
 * 批量更新队列字段配置
 */
export function updateQueueFieldConfigs(
  queueId: number,
  data: BatchUpdateQueueFieldConfig
) {
  return request({
    url: `/api/v1/queues/${queueId}/field-configs`,
    method: 'put',
    data
  })
}

/**
 * 从其他队列复制字段配置
 */
export function copyQueueFieldConfigs(
  targetQueueId: number,
  data: CopyQueueFieldConfigRequest
) {
  return request<{
    copied_count: number
    skipped_count: number
  }>({
    url: `/api/v1/queues/${targetQueueId}/copy-field-configs`,
    method: 'post',
    data
  })
}

/**
 * 获取案件的字段配置（运行时）
 */
export function getCaseFieldConfigs(caseId: string) {
  return request<CaseFieldConfigResponse>({
    url: `/api/v1/cases/${caseId}/field-configs`,
    method: 'get'
  })
}

