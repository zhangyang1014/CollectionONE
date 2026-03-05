/**
 * WhatsApp 官方号管理模块 - 类型定义
 */

// ==================== 枚举 / 常量 ====================

/** PHONE状态 */
export type PhoneStatus = 'PENDING_IP' | 'PENDING_ACTIVATION' | 'ACTIVATED'

/** 投养状态 */
export type NurtureStatus = 'PENDING' | 'NURTURING' | 'COMPLETED'

/** WA号码状态 */
export type WaStatus = 'NORMAL' | 'BANNED' | 'APPEALING' | 'DISABLED'

/** 分配状态 */
export type AssignStatus = 'UNASSIGNED' | 'ASSIGNED'

export const ASSIGN_STATUS_MAP: Record<AssignStatus, { label: string; type: string }> = {
  UNASSIGNED: { label: '待分配', type: 'warning' },
  ASSIGNED:   { label: '已分配', type: 'success' },
}

/** IP状态 */
export type IpStatus = 'ACTIVE' | 'INACTIVE'

export const PHONE_STATUS_MAP: Record<PhoneStatus, { label: string; type: string }> = {
  PENDING_IP: { label: '待绑定IP', type: 'info' },
  PENDING_ACTIVATION: { label: '待激活', type: 'warning' },
  ACTIVATED: { label: '已激活', type: 'success' },
}

export const NURTURE_STATUS_MAP: Record<NurtureStatus, { label: string; type: string }> = {
  PENDING: { label: '待投养', type: 'info' },
  NURTURING: { label: '投养中', type: 'warning' },
  COMPLETED: { label: '投养完成', type: 'success' },
}

export const WA_STATUS_MAP: Record<WaStatus, { label: string; type: string }> = {
  NORMAL:    { label: '正常',   type: 'success' },
  BANNED:    { label: '封号待申诉', type: 'danger'  },
  APPEALING: { label: '申诉中', type: 'warning' },
  DISABLED:  { label: '已停用', type: 'info'    },
}

export const IP_STATUS_MAP: Record<IpStatus, { label: string; type: string }> = {
  ACTIVE: { label: '在线', type: 'success' },
  INACTIVE: { label: '离线', type: 'info' },
}

// ==================== Phone 相关 ====================

export interface WaPhone {
  id: number
  instantId: string
  purchaseChannelId: number
  purchaseChannelName: string
  phone: string
  ipId: number | null
  ipAddress: string
  phoneStatus: PhoneStatus
  nurtureStatus: NurtureStatus
  waStatus: WaStatus
  activatedAt: string
  nurtureStartedAt: string
  nurtureDays: number
  acquisitionCount: number
  assignStatus: AssignStatus
  assignedAt: string
  assignedCollectorId: number | null
  assignedCollectorName: string
  cumulativeUsageHours: number
  offlineAt: string
  appealedAt: string
  createdAt: string
  updatedAt: string
}

export interface WaPhoneStats {
  /** 总 Instant 数量 */
  totalInstants: number

  // ── 第一行：生命周期分布（互斥分桶，合计 = totalInstants） ──
  /** 云机配置中：phoneStatus = PENDING_IP 或 PENDING_ACTIVATION */
  configuring: number
  /** 待投养：ACTIVATED + nurtureStatus=PENDING + waStatus=NORMAL */
  pendingNurture: number
  /** 投养中：nurtureStatus=NURTURING + waStatus=NORMAL */
  nurturing: number
  /** 待分配：COMPLETED + UNASSIGNED + waStatus=NORMAL */
  pendingAssign: number
  /** 使用中：ASSIGNED + waStatus=NORMAL（含掉线） */
  inUse: number
  /** 问题号：waStatus IN (BANNED, APPEALING, DISABLED)，优先级最高 */
  problematic: number

  // ── 第二行：健康状态指标（独立统计，可重叠） ──
  /** 已养成：nurtureStatus=COMPLETED 的号码总量 */
  nurtureCompleted: number
  /** 正常使用中：ASSIGNED + waStatus=NORMAL 且未掉线 */
  activeInUse: number
  /** 掉线中：offlineAt 非空 + waStatus=NORMAL */
  offline: number
  /** 封号中：waStatus=BANNED */
  banned: number
  /** 解封中：waStatus=APPEALING */
  appealing: number
  /** 彻底停用：waStatus=DISABLED */
  disabled: number

  // ── IP 资源（保留，用于 Tab badge 等） ──
  /** 可用 IP 数（ACTIVE 且未满载） */
  availableIps: number
  /** 待绑定 IP 的号码数（原 pendingIp，保留供 Tab badge 使用） */
  pendingIp: number
}

// ==================== IP 相关 ====================

export interface WaIp {
  id: number
  ipAddress: string
  port: number
  accountName: string
  password: string
  onlineAt: string
  cumulativeServiceHours: number
  healthScore: number
  linkedPhoneCount: number
  status: IpStatus
  createdAt: string
  updatedAt: string
  /** 封号率 0~1，例如 0.25 表示该IP下25%的号码已封号 */
  banRate: number
  /** 负载率 0~1，例如 0.6 表示已绑定3个/上限5个 */
  loadRate: number
  /** 该IP下掉线（offlineAt非空且waStatus正常）的Phone数量 */
  offlinePhoneCount: number
  /** WA平均存活时间（小时），仅统计存活>3天或已封号的号码 */
  avgSurvivalHours: number
  /** WA平均使用时间（小时），仅统计存活>3天或已封号的号码，取 cumulativeUsageHours 均值 */
  avgUsageHours: number
}

export interface IpDemand {
  totalInstants: number
  phonesPerIp: number
  requiredIps: number
  currentIps: number
  newIpsNeeded: number
}

export interface IpChangeLog {
  id: number
  phoneId: number
  instantId: string
  oldIpAddress: string
  newIpAddress: string
  changeReason: string
  operator: string
  createdAt: string
}

/** 分配记录 */
export interface PhoneAssignLog {
  id: number
  phoneId: number
  instantId: string
  collectorId: number
  collectorName: string
  teamName: string
  assignedAt: string
  reclaimedAt: string
  usageHours: number
  operator: string
  remark: string
}

/** 生命周期事件类型 */
export type LifecycleEventType =
  | 'REGISTERED'
  | 'IP_BOUND'
  | 'IP_CHANGED'
  | 'ACTIVATED'
  | 'NURTURE_STARTED'
  | 'NURTURE_COMPLETED'
  | 'ASSIGNED'
  | 'RECLAIMED'
  | 'OFFLINE'
  | 'BACK_ONLINE'
  | 'BANNED'
  | 'APPEALED'
  | 'DISABLED'

/** 生命周期事件 */
export interface PhoneLifecycleEvent {
  id: number
  phoneId: number
  instantId: string
  eventType: LifecycleEventType
  eventLabel: string
  detail: string
  operator: string
  createdAt: string
}

// ==================== 配置 相关 ====================

export interface WaConfig {
  phonesPerIp: number
  autoIpAssign: boolean
  autoActivate: boolean
  autoNurture: boolean
  autoAssign: boolean
  autoAssignRule: string
}

export interface WaPurchaseChannel {
  id: number
  channelName: string
  description: string
  isEnabled: boolean
  createdAt: string
  updatedAt: string
}

/** 渠道质量统计 */
export interface WaChannelStats {
  /** 渠道ID */
  channelId: number
  /** 该渠道下的总 Phone 数 */
  instantCount: number
  /** 封号率 0~1，已封号Phone数 / 总Phone数 */
  banRate: number
  /** 投养完成率 0~1，nurtureStatus=COMPLETED 的比例 */
  nurtureCompletionRate: number
  /** 可用率 0~1，waStatus=NORMAL 的比例 */
  availableRate: number
  /** WA平均存活时长（小时），仅统计存活>3天或已封号的号码 */
  avgSurvivalHours: number
  /** WA平均使用时长（小时），仅统计存活>3天或已封号的号码，取 cumulativeUsageHours 均值 */
  avgUsageHours: number
}

// ==================== 请求参数 ====================

export interface PhoneListParams {
  page?: number
  pageSize?: number
  phoneStatus?: PhoneStatus | ''
  nurtureStatus?: NurtureStatus | ''
  waStatus?: WaStatus | ''
  assignStatus?: AssignStatus | ''
  keyword?: string
  purchaseChannelId?: number | ''
}

/** 申诉结果 */
export type AppealResult = 'SUCCESS' | 'FAILURE'

export interface PhoneRegisterRequest {
  instantIds: string[]
  purchaseChannelId: number
}

export interface IpCreateRequest {
  ipAddress: string
  port: number
  accountName: string
  password: string
}
