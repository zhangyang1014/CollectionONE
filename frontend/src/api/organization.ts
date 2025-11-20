/**
 * 组织架构管理 API
 */
import request from '@/utils/request'
import type {
  CollectionAgency,
  CollectionAgencyCreate,
  CollectionAgencyUpdate,
  TeamGroup,
  TeamGroupCreate,
  TeamGroupUpdate,
  CollectionTeam,
  CollectionTeamCreate,
  CollectionTeamUpdate,
  Collector,
  CollectorCreate,
  CollectorUpdate,
  CollectorDetail,
  AssignToAgency,
  AssignToTeam,
  AssignToCollector,
  AutoAssignRequest,
  TransferCaseRequest,
  ReclaimCaseRequest,
  AssignmentResponse,
  CaseAssignmentHistory,
  AgencyWorkingHours,
  AgencyWorkingHoursUpdate,
  AgencyWorkingHoursBatchUpdate
} from '@/types/organization'

// ===== 催收机构 API =====

/**
 * 获取催收机构列表
 */
export function getAgencies(params: {
  tenant_id: number
  is_active?: boolean
  skip?: number
  limit?: number
}) {
  return request<CollectionAgency[]>({
    url: '/api/v1/agencies',
    method: 'get',
    params
  })
}

/**
 * 获取指定甲方的催收机构列表
 */
export function getTenantAgencies(tenantId: number) {
  return request<CollectionAgency[]>({
    url: `/api/v1/tenants/${tenantId}/agencies`,
    method: 'get'
  })
}

/**
 * 创建催收机构
 */
export function createAgency(data: CollectionAgencyCreate) {
  return request<CollectionAgency>({
    url: '/api/v1/agencies',
    method: 'post',
    data
  })
}

/**
 * 获取催收机构详情
 */
export function getAgency(id: number) {
  return request<CollectionAgency>({
    url: `/api/v1/agencies/${id}`,
    method: 'get'
  })
}

/**
 * 更新催收机构
 */
export function updateAgency(id: number, data: CollectionAgencyUpdate) {
  return request<CollectionAgency>({
    url: `/api/v1/agencies/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除催收机构
 */
export function deleteAgency(id: number) {
  return request({
    url: `/api/v1/agencies/${id}`,
    method: 'delete'
  })
}

// ===== 小组群 API =====

/**
 * 获取小组群列表
 */
export function getTeamGroups(params: {
  tenant_id?: number
  agency_id?: number
  is_active?: boolean
  skip?: number
  limit?: number
}) {
  return request<TeamGroup[]>({
    url: '/api/v1/team-groups',
    method: 'get',
    params
  })
}

/**
 * 获取指定机构的小组群列表
 */
export function getAgencyTeamGroups(agencyId: number) {
  return request<TeamGroup[]>({
    url: '/api/v1/team-groups',
    method: 'get',
    params: { agency_id: agencyId }
  })
}

/**
 * 获取小组群详情
 */
export function getTeamGroup(id: number) {
  return request<TeamGroup>({
    url: `/api/v1/team-groups/${id}`,
    method: 'get'
  })
}

/**
 * 创建小组群
 */
export function createTeamGroup(data: TeamGroupCreate) {
  return request<TeamGroup>({
    url: '/api/v1/team-groups',
    method: 'post',
    data
  })
}

/**
 * 更新小组群
 */
export function updateTeamGroup(id: number, data: TeamGroupUpdate) {
  return request<TeamGroup>({
    url: `/api/v1/team-groups/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除小组群
 */
export function deleteTeamGroup(id: number) {
  return request({
    url: `/api/v1/team-groups/${id}`,
    method: 'delete'
  })
}

/**
 * 获取小组群下的小组列表
 */
export function getTeamGroupTeams(teamGroupId: number) {
  return request<CollectionTeam[]>({
    url: `/api/v1/team-groups/${teamGroupId}/teams`,
    method: 'get'
  })
}

// ===== 催收小组 API =====

/**
 * 获取催收小组列表
 */
export function getTeams(params: {
  agency_id?: number
  is_active?: boolean
  skip?: number
  limit?: number
}) {
  return request<CollectionTeam[]>({
    url: '/api/v1/teams',
    method: 'get',
    params
  })
}

/**
 * 获取指定机构的小组列表
 */
export function getAgencyTeams(agencyId: number) {
  return request<CollectionTeam[]>({
    url: `/api/v1/agencies/${agencyId}/teams`,
    method: 'get'
  })
}

/**
 * 创建催收小组
 */
export function createTeam(data: CollectionTeamCreate) {
  return request<CollectionTeam>({
    url: '/api/v1/teams',
    method: 'post',
    data
  })
}

/**
 * 更新催收小组
 */
export function updateTeam(id: number, data: CollectionTeamUpdate) {
  return request<CollectionTeam>({
    url: `/api/v1/teams/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除催收小组
 */
export function deleteTeam(id: number) {
  return request({
    url: `/api/v1/teams/${id}`,
    method: 'delete'
  })
}

// ===== 催员 API =====

/**
 * 获取催员列表
 */
export function getCollectors(params: {
  team_id?: number
  agency_id?: number
  status?: string
  is_active?: boolean
  skip?: number
  limit?: number
}) {
  return request<Collector[]>({
    url: '/api/v1/collectors',
    method: 'get',
    params
  })
}

/**
 * 获取指定小组的催员列表
 */
export function getTeamCollectors(teamId: number) {
  return request<Collector[]>({
    url: `/api/v1/teams/${teamId}/collectors`,
    method: 'get'
  })
}

/**
 * 创建催员
 */
export function createCollector(data: CollectorCreate) {
  return request<Collector>({
    url: '/api/v1/collectors',
    method: 'post',
    data
  })
}

/**
 * 更新催员
 */
export function updateCollector(id: number, data: CollectorUpdate) {
  return request<Collector>({
    url: `/api/v1/collectors/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除催员
 */
export function deleteCollector(id: number) {
  return request({
    url: `/api/v1/collectors/${id}`,
    method: 'delete'
  })
}

/**
 * 获取催员详情（包含统计）
 */
export function getCollectorDetail(id: number) {
  return request<CollectorDetail>({
    url: `/api/v1/collectors/${id}/detail`,
    method: 'get'
  })
}

// ===== 案件分配 API =====

/**
 * 分配案件到机构
 */
export function assignToAgency(data: AssignToAgency) {
  return request<AssignmentResponse>({
    url: '/api/v1/cases/assign-to-agency',
    method: 'post',
    data
  })
}

/**
 * 分配案件到小组
 */
export function assignToTeam(data: AssignToTeam) {
  return request<AssignmentResponse>({
    url: '/api/v1/cases/assign-to-team',
    method: 'post',
    data
  })
}

/**
 * 分配案件到催员
 */
export function assignToCollector(data: AssignToCollector) {
  return request<AssignmentResponse>({
    url: '/api/v1/cases/assign-to-collector',
    method: 'post',
    data
  })
}

/**
 * 自动分配案件
 */
export function autoAssignCases(data: AutoAssignRequest) {
  return request<AssignmentResponse>({
    url: '/api/v1/cases/auto-assign',
    method: 'post',
    data
  })
}

/**
 * 转移案件
 */
export function transferCases(data: TransferCaseRequest) {
  return request({
    url: '/api/v1/cases/transfer',
    method: 'post',
    data
  })
}

/**
 * 批量回收案件
 */
export function reclaimCases(data: ReclaimCaseRequest) {
  return request({
    url: '/api/v1/cases/reclaim',
    method: 'post',
    data
  })
}

/**
 * 获取案件分配历史
 */
export function getCaseAssignmentHistory(caseId: number) {
  return request<{ case_id: number; history: CaseAssignmentHistory[] }>({
    url: `/api/v1/cases/${caseId}/assignment-history`,
    method: 'get'
  })
}

// ===== 机构作息时间 API =====

/**
 * 获取机构作息时间
 */
export function getAgencyWorkingHours(agencyId: number) {
  return request<AgencyWorkingHours[]>({
    url: `/api/v1/agencies/${agencyId}/working-hours`,
    method: 'get'
  })
}

/**
 * 批量更新机构作息时间
 */
export function updateAgencyWorkingHours(agencyId: number, data: AgencyWorkingHoursBatchUpdate) {
  return request<AgencyWorkingHours[]>({
    url: `/api/v1/agencies/${agencyId}/working-hours`,
    method: 'put',
    data
  })
}

/**
 * 更新单天作息时间
 */
export function updateSingleDayWorkingHours(agencyId: number, dayOfWeek: number, data: AgencyWorkingHoursUpdate) {
  return request<AgencyWorkingHours>({
    url: `/api/v1/agencies/${agencyId}/working-hours/${dayOfWeek}`,
    method: 'put',
    data
  })
}

// ===== 催员登录人脸记录 API =====

/**
 * 获取催员登录人脸记录
 */
export function getCollectorLoginFaceRecords(collectorId: number) {
  return request<Array<{
    id: number
    collector_id: number
    login_time: string
    face_image: string // base64 图片或图片URL
    face_id: string
    created_at: string
  }>>({
    url: `/api/v1/collectors/${collectorId}/login-face-records`,
    method: 'get'
  })
}

