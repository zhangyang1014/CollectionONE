/**
 * Infinity外呼系统API
 */
import request from '@/utils/request'
import type {
  InfinityCallConfig,
  InfinityCallConfigCreate,
  InfinityCallConfigUpdate,
  ExtensionPool,
  ExtensionPoolBatchImport,
  ExtensionPoolStatistics,
  MakeCallRequest,
  MakeCallResponse,
  TestConnectionRequest,
  TestConnectionResponse,
  ExtensionStatus
} from '@/types/infinity'

// ==================== Infinity 配置管理 ====================

/**
 * 创建Infinity配置
 */
export function createInfinityConfig(data: InfinityCallConfigCreate) {
  return request<InfinityCallConfig>({
    url: '/api/v1/infinity/configs',
    method: 'post',
    data
  })
}

/**
 * 获取甲方的Infinity配置
 */
export function getInfinityConfigByTenant(tenantId: number) {
  return request<InfinityCallConfig>({
    url: `/api/v1/infinity/configs/${tenantId}`,
    method: 'get'
  })
}

/**
 * 根据配置ID获取
 */
export function getInfinityConfigById(configId: number) {
  return request<InfinityCallConfig>({
    url: `/api/v1/infinity/configs/id/${configId}`,
    method: 'get'
  })
}

/**
 * 更新Infinity配置
 */
export function updateInfinityConfig(configId: number, data: InfinityCallConfigUpdate) {
  return request<InfinityCallConfig>({
    url: `/api/v1/infinity/configs/${configId}`,
    method: 'put',
    data
  })
}

/**
 * 删除Infinity配置
 */
export function deleteInfinityConfig(configId: number) {
  return request({
    url: `/api/v1/infinity/configs/${configId}`,
    method: 'delete'
  })
}

/**
 * 测试Infinity连接
 */
export function testInfinityConnection(data: TestConnectionRequest) {
  return request<TestConnectionResponse>({
    url: '/api/v1/infinity/configs/test-connection',
    method: 'post',
    data
  })
}

/**
 * 启用/禁用配置
 */
export function toggleInfinityConfig(configId: number, isActive: boolean) {
  return request<InfinityCallConfig>({
    url: `/api/v1/infinity/configs/${configId}/toggle`,
    method: 'post',
    params: { is_active: isActive }
  })
}

// ==================== 分机池管理 ====================

/**
 * 批量导入分机号
 */
export function batchImportExtensions(data: ExtensionPoolBatchImport) {
  return request({
    url: '/api/v1/infinity/extensions/batch-import',
    method: 'post',
    data
  })
}

/**
 * 查询分机池
 */
export function getExtensions(
  tenantId: number,
  configId?: number,
  status?: ExtensionStatus
) {
  return request<ExtensionPool[]>({
    url: `/api/v1/infinity/extensions/${tenantId}`,
    method: 'get',
    params: {
      config_id: configId,
      status
    }
  })
}

/**
 * 获取分机使用统计
 */
export function getExtensionStatistics(tenantId: number, configId?: number) {
  return request<ExtensionPoolStatistics>({
    url: `/api/v1/infinity/extensions/statistics/${tenantId}`,
    method: 'get',
    params: { config_id: configId }
  })
}

/**
 * 更新分机
 */
export function updateExtension(extensionId: number, data: any) {
  return request<ExtensionPool>({
    url: `/api/v1/infinity/extensions/${extensionId}`,
    method: 'put',
    data
  })
}

/**
 * 手动释放分机
 */
export function releaseExtension(extensionId: number) {
  return request({
    url: `/api/v1/infinity/extensions/${extensionId}/release`,
    method: 'post'
  })
}

/**
 * 删除分机
 */
export function deleteExtension(extensionId: number) {
  return request({
    url: `/api/v1/infinity/extensions/${extensionId}`,
    method: 'delete'
  })
}

/**
 * 批量删除分机
 */
export function batchDeleteExtensions(extensionIds: number[]) {
  return request({
    url: '/api/v1/infinity/extensions/batch-delete',
    method: 'post',
    data: { extension_ids: extensionIds }
  })
}

/**
 * 强制释放催员占用的分机
 */
export function forceReleaseCollectorExtensions(collectorId: number) {
  return request({
    url: `/api/v1/infinity/extensions/force-release-collector/${collectorId}`,
    method: 'post'
  })
}

// ==================== 外呼相关 ====================

/**
 * 发起外呼
 */
export function makeCall(data: MakeCallRequest) {
  return request<MakeCallResponse>({
    url: '/api/v1/infinity/make-call',
    method: 'post',
    data
  })
}

