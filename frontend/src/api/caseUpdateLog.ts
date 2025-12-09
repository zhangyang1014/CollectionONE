import request from '@/utils/request'

/**
 * 案件更新日志查询参数
 */
export interface CaseUpdateLogQuery {
  keyword?: string
  caseId?: string | number
  caseName?: string
  changeTypes?: string[]
  actorSources?: string[]
  statusAfter?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface CaseUpdateLogDiffItem {
  field: string
  oldValue: string | number | null
  newValue: string | number | null
}

export interface CaseUpdateLogRecord {
  id: string | number
  caseId: string | number
  caseName?: string
  batchId?: string
  updatedAt: string
  actor: string
  changeType: 'status' | 'field' | 'batch'
  summary?: string
  diffItems?: CaseUpdateLogDiffItem[]
}

/**
 * 获取案件更新日志列表
 * - 后端接口预期路径：/api/v1/case-update-logs
 * - 变更类型、来源等多选项用逗号分隔传递
 */
export function getCaseUpdateLogs(params: CaseUpdateLogQuery) {
  const query: Record<string, any> = {}

  if (params.keyword) query.keyword = params.keyword
  if (params.caseId) query.caseId = params.caseId
  if (params.caseName) query.caseName = params.caseName
  if (params.changeTypes?.length) query.changeTypes = params.changeTypes.join(',')
  if (params.actorSources?.length) query.actorSources = params.actorSources.join(',')
  if (params.statusAfter) query.statusAfter = params.statusAfter
  if (params.startTime) query.startTime = params.startTime
  if (params.endTime) query.endTime = params.endTime
  query.page = params.page ?? 1
  query.pageSize = params.pageSize ?? 20

  return request({
    url: '/api/v1/case-update-logs',
    method: 'get',
    params: query,
    meta: { silentNotFound: true }, // 404 时不弹全局错误，用前端mock兜底
  })
}
