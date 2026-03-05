/**
 * WhatsApp 官方号管理模块 - API + 前端 Mock 数据
 */
import type {
  WaPhone,
  WaPhoneStats,
  WaIp,
  WaConfig,
  WaPurchaseChannel,
  WaChannelStats,
  IpChangeLog,
  IpDemand,
  PhoneListParams,
  PhoneRegisterRequest,
  IpCreateRequest,
  PhoneAssignLog,
  PhoneLifecycleEvent,
  AssignStatus,
  AppealResult,
} from '@/types/wa-management'

const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// ==================== Mock 数据 ====================

let nextPhoneId = 100
let nextIpId = 100
let nextLogId = 100
let nextChannelId = 100

const mockPurchaseChannels: WaPurchaseChannel[] = [
  { id: 1, channelName: '云盾BD', description: '云盾BD海外渠道', isEnabled: true, createdAt: '2025-11-01 10:00:00', updatedAt: '2025-11-01 10:00:00' },
  { id: 2, channelName: 'Geelark官方', description: 'Geelark官方合作渠道', isEnabled: true, createdAt: '2025-11-05 14:00:00', updatedAt: '2025-11-05 14:00:00' },
  { id: 3, channelName: '第三方A', description: '第三方供应商A', isEnabled: false, createdAt: '2025-12-01 09:00:00', updatedAt: '2025-12-01 09:00:00' },
]

const mockIps: WaIp[] = [
  { id: 1, ipAddress: '103.45.67.12', port: 8443, accountName: 'proxy_user_01', password: '***', onlineAt: '2025-10-15 08:00:00', cumulativeServiceHours: 2160, healthScore: 95, linkedPhoneCount: 3, status: 'ACTIVE', createdAt: '2025-10-15 08:00:00', updatedAt: '2026-03-01 12:00:00' },
  { id: 2, ipAddress: '103.45.67.13', port: 8443, accountName: 'proxy_user_02', password: '***', onlineAt: '2025-10-20 09:00:00', cumulativeServiceHours: 2040, healthScore: 88, linkedPhoneCount: 4, status: 'ACTIVE', createdAt: '2025-10-20 09:00:00', updatedAt: '2026-03-01 12:00:00' },
  { id: 3, ipAddress: '103.45.67.14', port: 8443, accountName: 'proxy_user_03', password: '***', onlineAt: '2025-11-01 10:00:00', cumulativeServiceHours: 1800, healthScore: 72, linkedPhoneCount: 5, status: 'ACTIVE', createdAt: '2025-11-01 10:00:00', updatedAt: '2026-03-01 12:00:00' },
  { id: 4, ipAddress: '103.45.67.15', port: 8443, accountName: 'proxy_user_04', password: '***', onlineAt: '2025-12-01 11:00:00', cumulativeServiceHours: 1080, healthScore: 60, linkedPhoneCount: 2, status: 'ACTIVE', createdAt: '2025-12-01 11:00:00', updatedAt: '2026-03-01 12:00:00' },
  { id: 5, ipAddress: '103.45.67.16', port: 8443, accountName: 'proxy_user_05', password: '***', onlineAt: '2026-01-10 08:00:00', cumulativeServiceHours: 480, healthScore: 40, linkedPhoneCount: 0, status: 'INACTIVE', createdAt: '2026-01-10 08:00:00', updatedAt: '2026-03-01 12:00:00' },
]

const mockPhones: WaPhone[] = [
  // 待绑定IP
  { id: 1, instantId: 'INST-20260301-001', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '', ipId: null, ipAddress: '', phoneStatus: 'PENDING_IP', nurtureStatus: 'PENDING', waStatus: 'NORMAL', activatedAt: '', nurtureStartedAt: '', nurtureDays: 0, acquisitionCount: 0, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-03-01 10:00:00', updatedAt: '2026-03-01 10:00:00' },
  { id: 2, instantId: 'INST-20260301-002', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '', ipId: null, ipAddress: '', phoneStatus: 'PENDING_IP', nurtureStatus: 'PENDING', waStatus: 'NORMAL', activatedAt: '', nurtureStartedAt: '', nurtureDays: 0, acquisitionCount: 0, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-03-01 10:05:00', updatedAt: '2026-03-01 10:05:00' },
  // 待激活
  { id: 3, instantId: 'INST-20260225-010', purchaseChannelId: 2, purchaseChannelName: 'Geelark官方', phone: '', ipId: 1, ipAddress: '103.45.67.12', phoneStatus: 'PENDING_ACTIVATION', nurtureStatus: 'PENDING', waStatus: 'NORMAL', activatedAt: '', nurtureStartedAt: '', nurtureDays: 0, acquisitionCount: 0, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-02-25 09:00:00', updatedAt: '2026-02-26 14:00:00' },
  { id: 4, instantId: 'INST-20260225-011', purchaseChannelId: 2, purchaseChannelName: 'Geelark官方', phone: '', ipId: 2, ipAddress: '103.45.67.13', phoneStatus: 'PENDING_ACTIVATION', nurtureStatus: 'PENDING', waStatus: 'NORMAL', activatedAt: '', nurtureStartedAt: '', nurtureDays: 0, acquisitionCount: 0, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-02-25 09:05:00', updatedAt: '2026-02-26 14:05:00' },
  // 已激活 - 待投养
  { id: 5, instantId: 'INST-20260220-005', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5001', ipId: 1, ipAddress: '103.45.67.12', phoneStatus: 'ACTIVATED', nurtureStatus: 'PENDING', waStatus: 'NORMAL', activatedAt: '2026-02-22 16:00:00', nurtureStartedAt: '', nurtureDays: 0, acquisitionCount: 0, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-02-20 10:00:00', updatedAt: '2026-02-22 16:00:00' },
  // 已激活 - 投养中
  { id: 6, instantId: 'INST-20260210-003', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5002', ipId: 2, ipAddress: '103.45.67.13', phoneStatus: 'ACTIVATED', nurtureStatus: 'NURTURING', waStatus: 'NORMAL', activatedAt: '2026-02-12 10:00:00', nurtureStartedAt: '2026-02-13 08:00:00', nurtureDays: 20, acquisitionCount: 47, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-02-10 09:00:00', updatedAt: '2026-03-05 08:00:00' },
  { id: 7, instantId: 'INST-20260210-004', purchaseChannelId: 2, purchaseChannelName: 'Geelark官方', phone: '+52 55 1234 5003', ipId: 3, ipAddress: '103.45.67.14', phoneStatus: 'ACTIVATED', nurtureStatus: 'NURTURING', waStatus: 'NORMAL', activatedAt: '2026-02-12 11:00:00', nurtureStartedAt: '2026-02-13 09:00:00', nurtureDays: 20, acquisitionCount: 53, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-02-10 09:30:00', updatedAt: '2026-03-05 08:00:00' },
  // 投养完成 - 待分配（核心场景）
  { id: 8, instantId: 'INST-20260115-001', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5004', ipId: 1, ipAddress: '103.45.67.12', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', activatedAt: '2026-01-17 14:00:00', nurtureStartedAt: '2026-01-18 08:00:00', nurtureDays: 30, acquisitionCount: 128, assignStatus: 'UNASSIGNED', assignedAt: '', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 0, offlineAt: '', appealedAt: '', createdAt: '2026-01-15 10:00:00', updatedAt: '2026-02-17 08:00:00' },
  // 已分配 - 正常使用
  { id: 9, instantId: 'INST-20251201-008', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5005', ipId: 2, ipAddress: '103.45.67.13', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', activatedAt: '2025-12-05 10:00:00', nurtureStartedAt: '2025-12-06 08:00:00', nurtureDays: 30, acquisitionCount: 142, assignStatus: 'ASSIGNED', assignedAt: '2026-01-10 09:00:00', assignedCollectorId: 101, assignedCollectorName: '张三', cumulativeUsageHours: 1320, offlineAt: '', appealedAt: '', createdAt: '2025-12-01 10:00:00', updatedAt: '2026-03-05 08:00:00' },
  { id: 10, instantId: 'INST-20251201-009', purchaseChannelId: 2, purchaseChannelName: 'Geelark官方', phone: '+52 55 1234 5006', ipId: 3, ipAddress: '103.45.67.14', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', activatedAt: '2025-12-06 11:00:00', nurtureStartedAt: '2025-12-07 09:00:00', nurtureDays: 28, acquisitionCount: 119, assignStatus: 'ASSIGNED', assignedAt: '2026-01-12 10:00:00', assignedCollectorId: 102, assignedCollectorName: '李四', cumulativeUsageHours: 1260, offlineAt: '', appealedAt: '', createdAt: '2025-12-01 11:00:00', updatedAt: '2026-03-05 08:00:00' },
  { id: 11, instantId: 'INST-20251115-002', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5007', ipId: 3, ipAddress: '103.45.67.14', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', activatedAt: '2025-11-18 14:00:00', nurtureStartedAt: '2025-11-19 08:00:00', nurtureDays: 30, acquisitionCount: 156, assignStatus: 'ASSIGNED', assignedAt: '2025-12-20 09:00:00', assignedCollectorId: 103, assignedCollectorName: '王五', cumulativeUsageHours: 1800, offlineAt: '', appealedAt: '', createdAt: '2025-11-15 10:00:00', updatedAt: '2026-03-05 08:00:00' },
  // 申诉中（Mock：封号后已提交申诉，等待结果）
  { id: 12, instantId: 'INST-20251110-006', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5008', ipId: 4, ipAddress: '103.45.67.15', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'APPEALING', activatedAt: '2025-11-12 10:00:00', nurtureStartedAt: '2025-11-13 08:00:00', nurtureDays: 30, acquisitionCount: 98, assignStatus: 'ASSIGNED', assignedAt: '2025-12-15 09:00:00', assignedCollectorId: 104, assignedCollectorName: '赵六', cumulativeUsageHours: 960, offlineAt: '2026-02-28 15:30:00', appealedAt: '2026-03-01 09:00:00', createdAt: '2025-11-10 10:00:00', updatedAt: '2026-03-01 09:00:00' },
  // 已停用
  { id: 13, instantId: 'INST-20251105-007', purchaseChannelId: 2, purchaseChannelName: 'Geelark官方', phone: '+52 55 1234 5009', ipId: 4, ipAddress: '103.45.67.15', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'DISABLED', activatedAt: '2025-11-07 10:00:00', nurtureStartedAt: '2025-11-08 08:00:00', nurtureDays: 25, acquisitionCount: 87, assignStatus: 'ASSIGNED', assignedAt: '2025-12-05 10:00:00', assignedCollectorId: null, assignedCollectorName: '', cumulativeUsageHours: 720, offlineAt: '2026-02-20 11:00:00', appealedAt: '', createdAt: '2025-11-05 10:00:00', updatedAt: '2026-02-20 11:00:00' },
  // 掉线
  { id: 14, instantId: 'INST-20251201-010', purchaseChannelId: 1, purchaseChannelName: '云盾BD', phone: '+52 55 1234 5010', ipId: 3, ipAddress: '103.45.67.14', phoneStatus: 'ACTIVATED', nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', activatedAt: '2025-12-03 10:00:00', nurtureStartedAt: '2025-12-04 08:00:00', nurtureDays: 30, acquisitionCount: 134, assignStatus: 'ASSIGNED', assignedAt: '2026-01-05 09:00:00', assignedCollectorId: 105, assignedCollectorName: '孙七', cumulativeUsageHours: 1440, offlineAt: '2026-03-04 22:15:00', appealedAt: '', createdAt: '2025-12-01 10:00:00', updatedAt: '2026-03-04 22:15:00' },
]

const mockIpChangeLogs: IpChangeLog[] = [
  { id: 1, phoneId: 9, instantId: 'INST-20251201-008', oldIpAddress: '103.45.67.14', newIpAddress: '103.45.67.13', changeReason: '原IP负载过高，自动迁移', operator: '系统', createdAt: '2026-01-20 14:00:00' },
  { id: 2, phoneId: 10, instantId: 'INST-20251201-009', oldIpAddress: '103.45.67.12', newIpAddress: '103.45.67.14', changeReason: '手动换绑', operator: '运营小王', createdAt: '2026-02-01 10:30:00' },
  { id: 3, phoneId: 14, instantId: 'INST-20251201-010', oldIpAddress: '103.45.67.15', newIpAddress: '103.45.67.14', changeReason: 'IP掉线，紧急切换', operator: '运维小李', createdAt: '2026-02-15 08:00:00' },
]

/** 分配记录 Mock 数据 */
const mockAssignLogs: PhoneAssignLog[] = [
  // INST-20251201-008 (id=9) - 张三当前使用中
  { id: 1, phoneId: 9, instantId: 'INST-20251201-008', collectorId: 101, collectorName: '张三', teamName: '催收一组', assignedAt: '2026-01-10 09:00:00', reclaimedAt: '', usageHours: 0, operator: '运营主管', remark: '正式分配' },
  // INST-20251201-009 (id=10) - 李四当前使用中
  { id: 2, phoneId: 10, instantId: 'INST-20251201-009', collectorId: 102, collectorName: '李四', teamName: '催收一组', assignedAt: '2026-01-12 10:00:00', reclaimedAt: '', usageHours: 0, operator: '运营主管', remark: '' },
  // INST-20251115-002 (id=11) - 王五当前使用中（曾经分配过赵六）
  { id: 3, phoneId: 11, instantId: 'INST-20251115-002', collectorId: 104, collectorName: '赵六', teamName: '催收二组', assignedAt: '2025-12-10 09:00:00', reclaimedAt: '2025-12-20 08:00:00', usageHours: 239, operator: '运营主管', remark: '赵六离职，提前回收' },
  { id: 4, phoneId: 11, instantId: 'INST-20251115-002', collectorId: 103, collectorName: '王五', teamName: '催收二组', assignedAt: '2025-12-20 09:00:00', reclaimedAt: '', usageHours: 0, operator: '运营主管', remark: '接替赵六' },
  // INST-20251110-006 (id=12) - 赵六（封号）
  { id: 5, phoneId: 12, instantId: 'INST-20251110-006', collectorId: 104, collectorName: '赵六', teamName: '催收二组', assignedAt: '2025-12-15 09:00:00', reclaimedAt: '2026-02-28 16:00:00', usageHours: 960, operator: '运营主管', remark: '封号后回收' },
  // INST-20251201-010 (id=14) - 孙七（掉线中）
  { id: 6, phoneId: 14, instantId: 'INST-20251201-010', collectorId: 105, collectorName: '孙七', teamName: '催收三组', assignedAt: '2026-01-05 09:00:00', reclaimedAt: '', usageHours: 0, operator: '运营主管', remark: '' },
]

/** 生命周期事件 Mock 数据 */
const mockLifecycleEvents: PhoneLifecycleEvent[] = [
  // INST-20251201-008 (id=9) - 完整生命周期
  { id: 1,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'REGISTERED',       eventLabel: '号码登记',     detail: '通过云盾BD渠道购买并登记',          operator: '运营小王', createdAt: '2025-12-01 10:00:00' },
  { id: 2,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'IP_BOUND',          eventLabel: 'IP绑定',       detail: '绑定IP: 103.45.67.14',              operator: '运营小王', createdAt: '2025-12-03 14:00:00' },
  { id: 3,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'ACTIVATED',         eventLabel: '激活',         detail: '激活手机号: +52 55 1234 5005',       operator: '运营小王', createdAt: '2025-12-05 10:00:00' },
  { id: 4,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'NURTURE_STARTED',   eventLabel: '开始投养',     detail: '开始养号，预计30天',                 operator: '系统',     createdAt: '2025-12-06 08:00:00' },
  { id: 5,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'IP_CHANGED',        eventLabel: 'IP变更',       detail: '原IP: 103.45.67.14 → 新IP: 103.45.67.13，原因：负载过高', operator: '系统', createdAt: '2026-01-20 14:00:00' },
  { id: 6,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'NURTURE_COMPLETED', eventLabel: '投养完成',     detail: '累计投养30天，质量评分: 92',          operator: '系统',     createdAt: '2026-01-05 08:00:00' },
  { id: 7,  phoneId: 9,  instantId: 'INST-20251201-008', eventType: 'ASSIGNED',          eventLabel: '分配催员',     detail: '分配给张三（催收一组）',             operator: '运营主管', createdAt: '2026-01-10 09:00:00' },
  // INST-20251110-006 (id=12) - 封号流程
  { id: 8,  phoneId: 12, instantId: 'INST-20251110-006', eventType: 'REGISTERED',        eventLabel: '号码登记',     detail: '通过云盾BD渠道购买并登记',           operator: '运营小王', createdAt: '2025-11-10 10:00:00' },
  { id: 9,  phoneId: 12, instantId: 'INST-20251110-006', eventType: 'IP_BOUND',          eventLabel: 'IP绑定',       detail: '绑定IP: 103.45.67.15',               operator: '运营小王', createdAt: '2025-11-11 09:00:00' },
  { id: 10, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'ACTIVATED',         eventLabel: '激活',         detail: '激活手机号: +52 55 1234 5008',        operator: '运营小王', createdAt: '2025-11-12 10:00:00' },
  { id: 11, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'NURTURE_STARTED',   eventLabel: '开始投养',     detail: '开始养号，预计30天',                  operator: '系统',     createdAt: '2025-11-13 08:00:00' },
  { id: 12, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'NURTURE_COMPLETED', eventLabel: '投养完成',     detail: '累计投养30天，质量评分: 88',           operator: '系统',     createdAt: '2025-12-13 08:00:00' },
  { id: 13, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'ASSIGNED',          eventLabel: '分配催员',     detail: '分配给赵六（催收二组）',              operator: '运营主管', createdAt: '2025-12-15 09:00:00' },
  { id: 14, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'BANNED',            eventLabel: 'WA封号',       detail: 'WhatsApp账号被封禁，原因：频繁群发',  operator: '系统',     createdAt: '2026-02-28 15:30:00' },
  { id: 15, phoneId: 12, instantId: 'INST-20251110-006', eventType: 'RECLAIMED',         eventLabel: '回收号码',     detail: '封号后从赵六处回收',                  operator: '运营主管', createdAt: '2026-02-28 16:00:00' },
  // INST-20251105-007 (id=13) - 停用流程
  { id: 16, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'REGISTERED',        eventLabel: '号码登记',     detail: '通过Geelark官方渠道购买并登记',       operator: '运营小王', createdAt: '2025-11-05 10:00:00' },
  { id: 17, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'IP_BOUND',          eventLabel: 'IP绑定',       detail: '绑定IP: 103.45.67.15',               operator: '运营小王', createdAt: '2025-11-06 09:00:00' },
  { id: 18, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'ACTIVATED',         eventLabel: '激活',         detail: '激活手机号: +52 55 1234 5009',        operator: '运营小王', createdAt: '2025-11-07 10:00:00' },
  { id: 19, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'NURTURE_STARTED',   eventLabel: '开始投养',     detail: '开始养号，预计30天',                  operator: '系统',     createdAt: '2025-11-08 08:00:00' },
  { id: 20, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'NURTURE_COMPLETED', eventLabel: '投养完成',     detail: '累计投养25天，质量评分: 80',           operator: '系统',     createdAt: '2025-12-03 08:00:00' },
  { id: 21, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'ASSIGNED',          eventLabel: '分配催员',     detail: '分配给周八（催收三组）',              operator: '运营主管', createdAt: '2025-12-05 10:00:00' },
  { id: 22, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'OFFLINE',           eventLabel: '号码掉线',     detail: '检测到号码掉线，IP连接中断',          operator: '系统',     createdAt: '2026-02-20 11:00:00' },
  { id: 23, phoneId: 13, instantId: 'INST-20251105-007', eventType: 'DISABLED',          eventLabel: '停用',         detail: '手动停用，原因：长期掉线无法恢复',    operator: '运营主管', createdAt: '2026-02-20 11:05:00' },
  // INST-20251201-010 (id=14) - 掉线中
  { id: 24, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'REGISTERED',        eventLabel: '号码登记',     detail: '通过云盾BD渠道购买并登记',           operator: '运营小王', createdAt: '2025-12-01 10:00:00' },
  { id: 25, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'IP_BOUND',          eventLabel: 'IP绑定',       detail: '绑定IP: 103.45.67.15',               operator: '运营小王', createdAt: '2025-12-02 14:00:00' },
  { id: 26, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'ACTIVATED',         eventLabel: '激活',         detail: '激活手机号: +52 55 1234 5010',        operator: '运营小王', createdAt: '2025-12-03 10:00:00' },
  { id: 27, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'NURTURE_STARTED',   eventLabel: '开始投养',     detail: '开始养号，预计30天',                  operator: '系统',     createdAt: '2025-12-04 08:00:00' },
  { id: 28, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'IP_CHANGED',        eventLabel: 'IP变更',       detail: '原IP: 103.45.67.15 → 新IP: 103.45.67.14，原因：IP掉线，紧急切换', operator: '运维小李', createdAt: '2026-02-15 08:00:00' },
  { id: 29, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'NURTURE_COMPLETED', eventLabel: '投养完成',     detail: '累计投养30天，质量评分: 91',           operator: '系统',     createdAt: '2026-01-03 08:00:00' },
  { id: 30, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'ASSIGNED',          eventLabel: '分配催员',     detail: '分配给孙七（催收三组）',              operator: '运营主管', createdAt: '2026-01-05 09:00:00' },
  { id: 31, phoneId: 14, instantId: 'INST-20251201-010', eventType: 'OFFLINE',           eventLabel: '号码掉线',     detail: '检测到号码掉线，IP连接中断',          operator: '系统',     createdAt: '2026-03-04 22:15:00' },
]

const mockConfig: WaConfig = {
  phonesPerIp: 5,
  autoIpAssign: true,
  autoActivate: false,
  autoNurture: true,
  autoAssign: false,
  autoAssignRule: 'high_performance_first',
}

// ==================== Phone API ====================

export async function getPhoneList(params: PhoneListParams): Promise<{ list: WaPhone[]; total: number }> {
  await delay()
  let filtered = [...mockPhones]

  if (params.phoneStatus) {
    filtered = filtered.filter(p => p.phoneStatus === params.phoneStatus)
  }
  if (params.nurtureStatus) {
    filtered = filtered.filter(p => p.nurtureStatus === params.nurtureStatus)
  }
  if (params.waStatus) {
    filtered = filtered.filter(p => p.waStatus === params.waStatus)
  }
  if (params.assignStatus) {
    filtered = filtered.filter(p => p.assignStatus === params.assignStatus)
  }
  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    filtered = filtered.filter(p =>
      p.instantId.toLowerCase().includes(kw) ||
      p.phone.toLowerCase().includes(kw) ||
      p.assignedCollectorName.toLowerCase().includes(kw)
    )
  }
  if (params.purchaseChannelId) {
    filtered = filtered.filter(p => p.purchaseChannelId === params.purchaseChannelId)
  }

  const page = params.page || 1
  const pageSize = params.pageSize || 20
  const start = (page - 1) * pageSize
  return { list: filtered.slice(start, start + pageSize), total: filtered.length }
}

export async function getPhoneStats(): Promise<WaPhoneStats> {
  await delay(200)
  const phonesPerIp = mockConfig.phonesPerIp
  const availableIps = mockIps.filter(ip => ip.status === 'ACTIVE' && ip.linkedPhoneCount < phonesPerIp).length

  // 第一行：生命周期分布（互斥分桶，waStatus 异常优先级最高）
  const problematic = mockPhones.filter(p =>
    p.waStatus === 'BANNED' || p.waStatus === 'APPEALING' || p.waStatus === 'DISABLED'
  ).length

  const configuring = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    (p.phoneStatus === 'PENDING_IP' || p.phoneStatus === 'PENDING_ACTIVATION')
  ).length

  const pendingNurture = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    p.phoneStatus === 'ACTIVATED' &&
    p.nurtureStatus === 'PENDING'
  ).length

  const nurturing = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    p.nurtureStatus === 'NURTURING'
  ).length

  const pendingAssign = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    p.nurtureStatus === 'COMPLETED' &&
    p.assignStatus === 'UNASSIGNED'
  ).length

  const inUse = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    p.assignStatus === 'ASSIGNED'
  ).length

  // 第二行：健康状态指标（独立统计）
  const nurtureCompleted = mockPhones.filter(p => p.nurtureStatus === 'COMPLETED').length

  const activeInUse = mockPhones.filter(p =>
    p.waStatus === 'NORMAL' &&
    p.assignStatus === 'ASSIGNED' &&
    !p.offlineAt
  ).length

  const offline = mockPhones.filter(p => p.offlineAt && p.waStatus === 'NORMAL').length

  const banned = mockPhones.filter(p => p.waStatus === 'BANNED').length
  const appealing = mockPhones.filter(p => p.waStatus === 'APPEALING').length
  const disabled = mockPhones.filter(p => p.waStatus === 'DISABLED').length

  // 保留供 Tab badge 使用
  const pendingIp = mockPhones.filter(p => p.phoneStatus === 'PENDING_IP').length

  return {
    totalInstants: mockPhones.length,
    // Row 1
    configuring,
    pendingNurture,
    nurturing,
    pendingAssign,
    inUse,
    problematic,
    // Row 2
    nurtureCompleted,
    activeInUse,
    offline,
    banned,
    appealing,
    disabled,
    // IP 资源
    availableIps,
    pendingIp,
  }
}

export async function registerPhones(req: PhoneRegisterRequest): Promise<{ count: number }> {
  await delay(500)
  const channel = mockPurchaseChannels.find(c => c.id === req.purchaseChannelId)
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  for (const instId of req.instantIds) {
    mockPhones.push({
      id: nextPhoneId++,
      instantId: instId,
      purchaseChannelId: req.purchaseChannelId,
      purchaseChannelName: channel?.channelName || '',
      phone: '',
      ipId: null,
      ipAddress: '',
      phoneStatus: 'PENDING_IP',
      nurtureStatus: 'PENDING',
      waStatus: 'NORMAL',
      activatedAt: '',
      nurtureStartedAt: '',
      nurtureDays: 0,
      acquisitionCount: 0,
      assignStatus: 'UNASSIGNED' as AssignStatus,
      assignedAt: '',
      assignedCollectorId: null,
      assignedCollectorName: '',
      cumulativeUsageHours: 0,
      offlineAt: '',
      appealedAt: '',
      createdAt: now,
      updatedAt: now,
    })
  }
  return { count: req.instantIds.length }
}

export async function bindPhoneIp(phoneId: number, ipId: number): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  const ip = mockIps.find(i => i.id === ipId)
  if (phone && ip) {
    const oldIp = phone.ipAddress
    phone.ipId = ip.id
    phone.ipAddress = ip.ipAddress
    if (phone.phoneStatus === 'PENDING_IP') {
      phone.phoneStatus = 'PENDING_ACTIVATION'
    }
    if (oldIp && oldIp !== ip.ipAddress) {
      mockIpChangeLogs.push({
        id: nextLogId++,
        phoneId: phone.id,
        instantId: phone.instantId,
        oldIpAddress: oldIp,
        newIpAddress: ip.ipAddress,
        changeReason: '手动换绑',
        operator: '当前用户',
        createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      })
    }
  }
}

export async function activatePhone(phoneId: number, phoneNumber: string): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  if (phone) {
    phone.phone = phoneNumber
    phone.phoneStatus = 'ACTIVATED'
    phone.nurtureStatus = 'PENDING'
    phone.activatedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
  }
}

export async function assignPhone(phoneId: number, collectorId: number, collectorName: string): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  if (phone) {
    phone.assignedCollectorId = collectorId
    phone.assignedCollectorName = collectorName
    phone.assignedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
  }
}

export async function appealPhone(phoneId: number): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  if (phone) {
    phone.waStatus = 'APPEALING'
    phone.appealedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
    phone.updatedAt = phone.appealedAt
  }
}

export async function submitAppealResult(phoneId: number, result: AppealResult): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  if (phone) {
    phone.waStatus = result === 'SUCCESS' ? 'NORMAL' : 'DISABLED'
    phone.appealedAt = ''
    phone.updatedAt = new Date().toISOString().replace('T', ' ').substring(0, 19)
  }
}

export async function disablePhone(phoneId: number): Promise<void> {
  await delay(400)
  const phone = mockPhones.find(p => p.id === phoneId)
  if (phone) {
    phone.waStatus = 'DISABLED'
  }
}

export async function batchBindIp(phoneIds: number[], ipId: number): Promise<{ count: number }> {
  await delay(500)
  let count = 0
  for (const pid of phoneIds) {
    const phone = mockPhones.find(p => p.id === pid)
    const ip = mockIps.find(i => i.id === ipId)
    if (phone && ip) {
      phone.ipId = ip.id
      phone.ipAddress = ip.ipAddress
      if (phone.phoneStatus === 'PENDING_IP') phone.phoneStatus = 'PENDING_ACTIVATION'
      count++
    }
  }
  return { count }
}

export async function batchAssignPhones(phoneIds: number[], collectorId: number, collectorName: string): Promise<{ count: number }> {
  await delay(500)
  let count = 0
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  for (const pid of phoneIds) {
    const phone = mockPhones.find(p => p.id === pid)
    if (phone) {
      phone.assignedCollectorId = collectorId
      phone.assignedCollectorName = collectorName
      phone.assignedAt = now
      count++
    }
  }
  return { count }
}

export async function batchAppealPhones(phoneIds: number[]): Promise<{ count: number }> {
  await delay(500)
  let count = 0
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  for (const pid of phoneIds) {
    const phone = mockPhones.find(p => p.id === pid && p.waStatus === 'BANNED')
    if (phone) {
      phone.waStatus = 'APPEALING'
      phone.appealedAt = now
      phone.updatedAt = now
      count++
    }
  }
  return { count }
}

export async function batchResolveAppeal(phoneIds: number[], result: AppealResult): Promise<{ count: number }> {
  await delay(500)
  let count = 0
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  for (const pid of phoneIds) {
    const phone = mockPhones.find(p => p.id === pid && p.waStatus === 'APPEALING')
    if (phone) {
      phone.waStatus = result === 'SUCCESS' ? 'NORMAL' : 'DISABLED'
      phone.appealedAt = ''
      phone.updatedAt = now
      count++
    }
  }
  return { count }
}

export async function getIpChangeLogs(phoneId: number): Promise<IpChangeLog[]> {
  await delay(200)
  return mockIpChangeLogs.filter(log => log.phoneId === phoneId)
}

export async function getPhoneAssignLogs(phoneId: number): Promise<PhoneAssignLog[]> {
  await delay(200)
  return mockAssignLogs.filter(log => log.phoneId === phoneId)
}

export async function getPhoneLifecycle(phoneId: number): Promise<PhoneLifecycleEvent[]> {
  await delay(200)
  return mockLifecycleEvents.filter(e => e.phoneId === phoneId)
}

// ==================== IP 健康度计算 ====================

/**
 * 动态计算IP健康度评分（0~100）
 *
 * 计分规则：
 *   基础分 100
 *   ① INACTIVE 状态        → -40
 *   ② 封号率               → -(banRate × 40)，最多扣40
 *   ③ 负载率 > 80%         → -10；100%满载 → -20
 *   ④ 累计服务时长奖励      → >720h +5；>2160h +10（上限+10）
 *   ⑤ 掉线Phone数          → 每个 -3，最多扣15
 */
function calcHealthScore(params: {
  status: IpStatus
  banRate: number
  loadRate: number
  cumulativeServiceHours: number
  offlinePhoneCount: number
}): number {
  let score = 100

  // ① IP状态
  if (params.status === 'INACTIVE') score -= 40

  // ② 封号率扣分（最多40分）
  score -= Math.round(params.banRate * 40)

  // ③ 负载率扣分
  if (params.loadRate >= 1) {
    score -= 20
  } else if (params.loadRate > 0.8) {
    score -= 10
  }

  // ④ 在线稳定性奖励（服务时间越长越健康）
  if (params.cumulativeServiceHours > 2160) {
    score += 10
  } else if (params.cumulativeServiceHours > 720) {
    score += 5
  }

  // ⑤ 掉线Phone扣分（每个-3，最多扣15）
  score -= Math.min(params.offlinePhoneCount * 3, 15)

  return Math.max(0, Math.min(100, score))
}

// ==================== IP API ====================

/** 将日期字符串转换为毫秒时间戳，不合法时返回 NaN */
function parseTs(dateStr: string): number {
  if (!dateStr) return NaN
  return new Date(dateStr.replace(' ', 'T')).getTime()
}

export async function getIpList(): Promise<WaIp[]> {
  await delay()
  const phonesPerIp = mockConfig.phonesPerIp
  const now = Date.now()

  return mockIps.map(ip => {
    // 找出绑定在该IP下的所有Phone
    const phones = mockPhones.filter(p => p.ipId === ip.id)
    const bannedCount = phones.filter(p => p.waStatus === 'BANNED').length
    const offlineCount = phones.filter(p => !!p.offlineAt && p.waStatus === 'NORMAL').length
    const banRate = phones.length > 0 ? bannedCount / phones.length : 0
    const loadRate = phonesPerIp > 0 ? ip.linkedPhoneCount / phonesPerIp : 0

    const healthScore = calcHealthScore({
      status: ip.status,
      banRate,
      loadRate,
      cumulativeServiceHours: ip.cumulativeServiceHours,
      offlinePhoneCount: offlineCount,
    })

    // 筛选用于WA统计的Phone：已封号 或 存活超过3天（72小时）
    const THREE_DAYS_MS = 72 * 60 * 60 * 1000
    const statPhones = phones.filter(p => {
      if (p.waStatus === 'BANNED') return true
      if (!p.activatedAt) return false
      const activatedTs = parseTs(p.activatedAt)
      if (isNaN(activatedTs)) return false
      const endTs = p.offlineAt ? parseTs(p.offlineAt) : now
      return (endTs - activatedTs) >= THREE_DAYS_MS
    })

    // 计算平均存活时间（激活到下线/现在）
    let avgSurvivalHours = 0
    if (statPhones.length > 0) {
      const totalSurvivalMs = statPhones.reduce((sum, p) => {
        const activatedTs = parseTs(p.activatedAt)
        if (isNaN(activatedTs)) return sum
        const endTs = p.offlineAt ? parseTs(p.offlineAt) : now
        return sum + Math.max(0, endTs - activatedTs)
      }, 0)
      avgSurvivalHours = Math.round(totalSurvivalMs / statPhones.length / (1000 * 60 * 60))
    }

    // 计算平均使用时间（cumulativeUsageHours 均值）
    let avgUsageHours = 0
    if (statPhones.length > 0) {
      const totalUsage = statPhones.reduce((sum, p) => sum + (p.cumulativeUsageHours || 0), 0)
      avgUsageHours = Math.round(totalUsage / statPhones.length)
    }

    return {
      ...ip,
      banRate,
      loadRate,
      offlinePhoneCount: offlineCount,
      healthScore,
      avgSurvivalHours,
      avgUsageHours,
    }
  })
}

export async function createIp(req: IpCreateRequest): Promise<WaIp> {
  await delay(400)
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  const newIp: WaIp = {
    id: nextIpId++,
    ipAddress: req.ipAddress,
    port: req.port,
    accountName: req.accountName,
    password: req.password,
    onlineAt: now,
    cumulativeServiceHours: 0,
    healthScore: 100,
    linkedPhoneCount: 0,
    status: 'ACTIVE',
    createdAt: now,
    updatedAt: now,
    banRate: 0,
    loadRate: 0,
    offlinePhoneCount: 0,
    avgSurvivalHours: 0,
    avgUsageHours: 0,
  }
  mockIps.push(newIp)
  return newIp
}

export async function updateIp(id: number, data: Partial<IpCreateRequest>): Promise<void> {
  await delay(300)
  const ip = mockIps.find(i => i.id === id)
  if (ip) {
    if (data.ipAddress) ip.ipAddress = data.ipAddress
    if (data.port) ip.port = data.port
    if (data.accountName) ip.accountName = data.accountName
    if (data.password) ip.password = data.password
  }
}

export async function deleteIp(id: number): Promise<void> {
  await delay(300)
  const idx = mockIps.findIndex(i => i.id === id)
  if (idx >= 0) mockIps.splice(idx, 1)
}

export async function getIpDemand(): Promise<IpDemand> {
  await delay(200)
  const totalInstants = mockPhones.length
  const phonesPerIp = mockConfig.phonesPerIp
  const requiredIps = Math.ceil(totalInstants / phonesPerIp)
  const currentIps = mockIps.filter(i => i.status === 'ACTIVE').length
  return {
    totalInstants,
    phonesPerIp,
    requiredIps,
    currentIps,
    newIpsNeeded: Math.max(0, requiredIps - currentIps),
  }
}

// ==================== 配置 API ====================

export async function getWaConfig(): Promise<WaConfig> {
  await delay(200)
  return { ...mockConfig }
}

export async function updateWaConfig(data: Partial<WaConfig>): Promise<void> {
  await delay(300)
  Object.assign(mockConfig, data)
}

export async function getPurchaseChannels(): Promise<WaPurchaseChannel[]> {
  await delay(200)
  return [...mockPurchaseChannels]
}

export async function createPurchaseChannel(data: { channelName: string; description: string }): Promise<WaPurchaseChannel> {
  await delay(300)
  const now = new Date().toISOString().replace('T', ' ').substring(0, 19)
  const ch: WaPurchaseChannel = {
    id: nextChannelId++,
    channelName: data.channelName,
    description: data.description,
    isEnabled: true,
    createdAt: now,
    updatedAt: now,
  }
  mockPurchaseChannels.push(ch)
  return ch
}

export async function updatePurchaseChannel(id: number, data: Partial<WaPurchaseChannel>): Promise<void> {
  await delay(300)
  const ch = mockPurchaseChannels.find(c => c.id === id)
  if (ch) {
    if (data.channelName !== undefined) ch.channelName = data.channelName
    if (data.description !== undefined) ch.description = data.description
    if (data.isEnabled !== undefined) ch.isEnabled = data.isEnabled
  }
}

export async function deletePurchaseChannel(id: number): Promise<void> {
  await delay(300)
  const idx = mockPurchaseChannels.findIndex(c => c.id === id)
  if (idx >= 0) mockPurchaseChannels.splice(idx, 1)
}

/**
 * 获取各渠道质量统计数据
 *
 * 统计规则：
 *   - instantCount：该渠道下注册的 Phone 总数
 *   - banRate：已封号 / 总数
 *   - nurtureCompletionRate：nurtureStatus=COMPLETED / 总数
 *   - availableRate：waStatus=NORMAL / 总数
 *   - avgSurvivalHours / avgUsageHours：仅统计已封号或存活>3天的号码，取均值
 */
export async function getChannelStats(): Promise<WaChannelStats[]> {
  await delay(200)
  const THREE_DAYS_MS = 72 * 60 * 60 * 1000
  const now = Date.now()

  return mockPurchaseChannels.map(ch => {
    const phones = mockPhones.filter(p => p.purchaseChannelId === ch.id)
    const total = phones.length

    const banCount = phones.filter(p => p.waStatus === 'BANNED').length
    const nurtureCompletedCount = phones.filter(p => p.nurtureStatus === 'COMPLETED').length
    const availableCount = phones.filter(p => p.waStatus === 'NORMAL').length

    // 筛选用于WA统计的Phone：已封号 或 存活超过3天（72小时）
    const statPhones = phones.filter(p => {
      if (p.waStatus === 'BANNED') return true
      if (!p.activatedAt) return false
      const activatedTs = parseTs(p.activatedAt)
      if (isNaN(activatedTs)) return false
      const endTs = p.offlineAt ? parseTs(p.offlineAt) : now
      return (endTs - activatedTs) >= THREE_DAYS_MS
    })

    let avgSurvivalHours = 0
    if (statPhones.length > 0) {
      const totalSurvivalMs = statPhones.reduce((sum, p) => {
        const activatedTs = parseTs(p.activatedAt)
        if (isNaN(activatedTs)) return sum
        const endTs = p.offlineAt ? parseTs(p.offlineAt) : now
        return sum + Math.max(0, endTs - activatedTs)
      }, 0)
      avgSurvivalHours = Math.round(totalSurvivalMs / statPhones.length / (1000 * 60 * 60))
    }

    let avgUsageHours = 0
    if (statPhones.length > 0) {
      const totalUsage = statPhones.reduce((sum, p) => sum + (p.cumulativeUsageHours || 0), 0)
      avgUsageHours = Math.round(totalUsage / statPhones.length)
    }

    return {
      channelId: ch.id,
      instantCount: total,
      banRate: total > 0 ? banCount / total : 0,
      nurtureCompletionRate: total > 0 ? nurtureCompletedCount / total : 0,
      availableRate: total > 0 ? availableCount / total : 0,
      avgSurvivalHours,
      avgUsageHours,
    }
  })
}

// ==================== 辅助：催员列表（供分配弹窗使用） ====================

export interface SimpleCollector {
  id: number
  name: string
  team: string
  performanceScore: number
}

export async function getCollectorsForAssign(): Promise<SimpleCollector[]> {
  await delay(200)
  return [
    { id: 101, name: '张三', team: '催收一组', performanceScore: 95 },
    { id: 102, name: '李四', team: '催收一组', performanceScore: 88 },
    { id: 103, name: '王五', team: '催收二组', performanceScore: 92 },
    { id: 104, name: '赵六', team: '催收二组', performanceScore: 78 },
    { id: 105, name: '孙七', team: '催收三组', performanceScore: 85 },
    { id: 106, name: '周八', team: '催收三组', performanceScore: 90 },
  ]
}
