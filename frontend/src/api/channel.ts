/**
 * 渠道管理 API
 */
import request from '@/utils/request'
import type {
  ChannelSupplier,
  ChannelSupplierCreate,
  ChannelSupplierUpdate,
  SupplierOrderBatchUpdate,
  ChannelType
} from '@/types/channel'

/**
 * 获取渠道供应商列表
 */
export function getChannelSuppliers(tenantId: number, channelType: ChannelType) {
  return request<ChannelSupplier[]>({
    url: `/api/v1/channel-suppliers/tenants/${tenantId}/channels/${channelType}/suppliers`,
    method: 'get'
  })
}

/**
 * 创建渠道供应商
 */
export function createChannelSupplier(
  tenantId: number,
  channelType: ChannelType,
  data: ChannelSupplierCreate
) {
  return request<ChannelSupplier>({
    url: `/api/v1/channel-suppliers/tenants/${tenantId}/channels/${channelType}/suppliers`,
    method: 'post',
    data
  })
}

/**
 * 获取单个渠道供应商
 */
export function getChannelSupplier(supplierId: number) {
  return request<ChannelSupplier>({
    url: `/api/v1/channel-suppliers/${supplierId}`,
    method: 'get'
  })
}

/**
 * 更新渠道供应商
 */
export function updateChannelSupplier(supplierId: number, data: ChannelSupplierUpdate) {
  return request<ChannelSupplier>({
    url: `/api/v1/channel-suppliers/${supplierId}`,
    method: 'put',
    data
  })
}

/**
 * 删除渠道供应商
 */
export function deleteChannelSupplier(supplierId: number) {
  return request({
    url: `/api/v1/channel-suppliers/${supplierId}`,
    method: 'delete'
  })
}

/**
 * 批量更新供应商排序
 */
export function updateSupplierOrder(
  tenantId: number,
  channelType: ChannelType,
  data: SupplierOrderBatchUpdate
) {
  return request({
    url: `/api/v1/channel-suppliers/tenants/${tenantId}/channels/${channelType}/suppliers/order`,
    method: 'put',
    data
  })
}

