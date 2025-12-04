import imService from '@/utils/imRequest'

/**
 * WhatsApp账号管理相关API
 * 个人WA账号绑定、解绑、状态管理
 */

// ========== 类型定义 ==========

/**
 * WA账号状态
 */
export type WAAccountStatus = 'pending' | 'paired' | 'unpaired' | 'binding' | 'failed'

/**
 * WA账号信息
 */
export interface WAAccount {
  deviceId: string
  phoneNumber?: string
  accountName?: string
  status: WAAccountStatus
  qrCode?: string
  qrCodeExpiresAt?: string
  pairedAt?: string
  unpairedAt?: string
  lastSeen?: string
  createdAt: string
  updatedAt: string
}

/**
 * 创建云设备请求参数
 */
export interface CreateDeviceRequest {
  collectorId: string
  deviceType: 'personal_wa'
}

/**
 * 创建云设备响应
 */
export interface CreateDeviceResponse {
  deviceId: string
  qrCode: string
  expiresAt: string
}

/**
 * 设备状态响应
 */
export interface DeviceStatusResponse {
  deviceId: string
  status: WAAccountStatus
  phoneNumber?: string
  pairedAt?: string
  errorMessage?: string
}

/**
 * 个人WA账号列表响应
 */
export interface PersonalWAAccountsResponse {
  accounts: WAAccount[]
  totalCount: number
  maxCount: number
}

// ========== API函数 ==========

/**
 * 创建WA云设备
 * @param data 创建请求参数
 * @returns 云设备信息和二维码
 */
export function createWADevice(data: CreateDeviceRequest): Promise<CreateDeviceResponse> {
  return imService({
    url: '/api/v1/wa/devices/create',
    method: 'post',
    data
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 查询云设备绑定状态
 * @param deviceId 设备ID
 * @returns 设备状态信息
 */
export function getDeviceStatus(deviceId: string): Promise<DeviceStatusResponse> {
  return imService({
    url: `/api/v1/wa/devices/${deviceId}/status`,
    method: 'get'
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 重新绑定云设备
 * @param deviceId 设备ID
 * @returns 新的二维码信息
 */
export function rebindWADevice(deviceId: string): Promise<CreateDeviceResponse> {
  return imService({
    url: `/api/v1/wa/devices/${deviceId}/rebind`,
    method: 'post',
    data: { deviceId }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 查询个人WA账号列表
 * @param collectorId 催员ID
 * @returns 个人WA账号列表
 */
export function getPersonalWAAccounts(collectorId: string): Promise<PersonalWAAccountsResponse> {
  return imService({
    url: '/api/v1/wa/accounts/personal',
    method: 'get',
    params: { collectorId }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 解绑WA云设备
 * @param deviceId 设备ID
 * @returns 解绑结果
 */
export function unbindWADevice(deviceId: string): Promise<any> {
  return imService({
    url: `/api/v1/wa/devices/${deviceId}/unbind`,
    method: 'post',
    data: { deviceId }
  }).then((res: any) => {
    return res.data || res
  })
}

