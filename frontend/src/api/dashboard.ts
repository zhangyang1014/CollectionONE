/**
 * 数据看板API客户端
 */
import request from '@/utils/request'

// ============ 通信记录API ============

/**
 * 创建通信记录
 */
export function createCommunicationRecord(data: any) {
  return request({
    url: '/api/v1/communications/',
    method: 'post',
    data
  })
}

/**
 * 获取通信记录列表
 */
export function getCommunicationRecords(params: any) {
  return request({
    url: '/api/v1/communications/',
    method: 'get',
    params
  })
}

/**
 * 获取案件的所有通信记录
 */
export function getCaseCommunications(caseId: number, params?: any) {
  return request({
    url: `/api/v1/communications/case/${caseId}`,
    method: 'get',
    params
  })
}

/**
 * 获取TTFC统计数据
 */
export function getTTFCStats(params: any) {
  return request({
    url: '/api/v1/communications/stats/ttfc',
    method: 'get',
    params
  })
}

// ============ PTP管理API ============

/**
 * 创建PTP承诺
 */
export function createPTPRecord(data: any) {
  return request({
    url: '/api/v1/ptp/',
    method: 'post',
    data
  })
}

/**
 * 获取PTP列表
 */
export function getPTPRecords(params: any) {
  return request({
    url: '/api/v1/ptp/',
    method: 'get',
    params
  })
}

/**
 * 更新PTP状态
 */
export function updatePTPStatus(id: number, data: any) {
  return request({
    url: `/api/v1/ptp/${id}/status`,
    method: 'put',
    data
  })
}

/**
 * 获取PTP统计数据
 */
export function getPTPStats(params: any) {
  return request({
    url: '/api/v1/ptp/stats/summary',
    method: 'get',
    params
  })
}

// ============ 质检API ============

/**
 * 创建质检记录
 */
export function createQualityInspection(data: any) {
  return request({
    url: '/api/v1/quality-inspections/',
    method: 'post',
    data
  })
}

/**
 * 获取质检记录列表
 */
export function getQualityInspections(params: any) {
  return request({
    url: '/api/v1/quality-inspections/',
    method: 'get',
    params
  })
}

/**
 * 获取催员的质检历史
 */
export function getCollectorInspections(collectorId: number, params?: any) {
  return request({
    url: `/api/v1/quality-inspections/collector/${collectorId}`,
    method: 'get',
    params
  })
}

/**
 * 获取质检统计数据
 */
export function getQualityStats(params: any) {
  return request({
    url: '/api/v1/quality-inspections/stats/summary',
    method: 'get',
    params
  })
}

// ============ 催员绩效API ============

/**
 * 获取催员个人绩效看板数据
 */
export function getCollectorPerformance(collectorId: number, params: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}`,
    method: 'get',
    params
  })
}

/**
 * 获取催员趋势数据
 */
export function getCollectorTrend(collectorId: number, params?: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}/trend`,
    method: 'get',
    params
  })
}

/**
 * 获取同比/环比数据
 */
export function getCollectorComparison(collectorId: number, params: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}/comparison`,
    method: 'get',
    params
  })
}

/**
 * 获取小组排名
 */
export function getCollectorRanking(collectorId: number, params: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}/ranking`,
    method: 'get',
    params
  })
}

/**
 * 获取案件明细
 */
export function getCollectorCases(collectorId: number, params: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}/cases`,
    method: 'get',
    params
  })
}

/**
 * 获取互动明细
 */
export function getCollectorCommunications(collectorId: number, params: any) {
  return request({
    url: `/api/v1/performance/collector/${collectorId}/communications`,
    method: 'get',
    params
  })
}

// ============ 自定义维度分析API ============

/**
 * 获取可分析的自定义字段列表
 */
export function getAnalyzableFields(params: any) {
  return request({
    url: '/api/v1/analytics/custom-dimensions/fields',
    method: 'get',
    params
  })
}

/**
 * 获取自定义维度统计数据
 */
export function getCustomDimensionStats(params: any) {
  return request({
    url: '/api/v1/analytics/custom-dimensions/stats',
    method: 'get',
    params
  })
}

/**
 * 获取自定义维度图表数据
 */
export function getCustomDimensionChart(params: any) {
  return request({
    url: '/api/v1/analytics/custom-dimensions/chart',
    method: 'get',
    params
  })
}

// ============ 预警API ============

/**
 * 获取催员的预警信息
 */
export function getCollectorAlerts(collectorId: number, params?: any) {
  return request({
    url: `/api/v1/alerts/collector/${collectorId}`,
    method: 'get',
    params
  })
}

/**
 * 获取小组预警信息
 */
export function getTeamAlerts(teamId: number, params?: any) {
  return request({
    url: `/api/v1/alerts/team/${teamId}`,
    method: 'get',
    params
  })
}

/**
 * 获取机构预警信息
 */
export function getAgencyAlerts(agencyId: number, params?: any) {
  return request({
    url: `/api/v1/alerts/agency/${agencyId}`,
    method: 'get',
    params
  })
}

