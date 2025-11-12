/**
 * 渠道相关类型定义
 */

export type ChannelType = 'sms' | 'rcs' | 'whatsapp' | 'call'

export interface ChannelSupplier {
  id?: number
  tenant_id: number
  channel_type: ChannelType
  supplier_name: string
  api_url: string
  api_key: string
  secret_key: string
  sort_order: number
  is_active: boolean
  remark?: string
  created_at?: string
  updated_at?: string
}

export interface ChannelSupplierCreate {
  tenant_id: number
  channel_type: ChannelType
  supplier_name: string
  api_url: string
  api_key: string
  secret_key: string
  sort_order?: number
  is_active?: boolean
  remark?: string
}

export interface ChannelSupplierUpdate {
  supplier_name?: string
  api_url?: string
  api_key?: string
  secret_key?: string
  sort_order?: number
  is_active?: boolean
  remark?: string
}

export interface SupplierOrderUpdate {
  supplier_id: number
  sort_order: number
}

export interface SupplierOrderBatchUpdate {
  orders: SupplierOrderUpdate[]
}

