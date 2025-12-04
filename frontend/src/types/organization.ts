/**
 * 组织架构相关类型定义
 */

// ===== 催收机构 =====
export interface CollectionAgency {
  id: number
  tenant_id: number
  agency_code: string
  agency_name: string
  agency_name_en?: string
  contact_person?: string
  contact_phone?: string
  contact_email?: string
  address?: string
  description?: string
  timezone?: number
  agency_type: string  // 'real' | 'virtual'
  sort_order: number
  is_active: boolean
  team_count: number
  collector_count: number
  case_count: number
  created_at: string
  updated_at: string
}

export interface CollectionAgencyCreate {
  tenant_id: number
  agency_code: string
  agency_name: string
  agency_name_en?: string
  contact_person?: string
  contact_phone?: string
  contact_email?: string
  address?: string
  description?: string
  agency_type?: string  // 'real' | 'virtual'
  sort_order?: number
  is_active?: boolean
}

export interface CollectionAgencyUpdate {
  agency_name?: string
  agency_name_en?: string
  contact_person?: string
  contact_phone?: string
  contact_email?: string
  address?: string
  description?: string
  timezone?: number
  agency_type?: string  // 'real' | 'virtual'
  sort_order?: number
  is_active?: boolean
}

// ===== 小组群 =====
export interface TeamGroup {
  id: number
  tenant_id: number
  agency_id: number
  group_code: string
  group_name: string
  group_name_en?: string
  description?: string
  sort_order: number
  is_active: boolean
  agency_name?: string
  spv_account_name?: string
  spv_login_id?: string
  team_count: number
  collector_count: number
  created_at: string
  updated_at: string
}

export interface TeamGroupCreate {
  tenant_id: number
  agency_id: number
  group_code: string
  group_name: string
  group_name_en?: string
  description?: string
  sort_order?: number
  is_active?: boolean
  // SPV管理员账号信息
  spv_account_name: string
  spv_login_id: string
  spv_email: string
  spv_password: string
  spv_remark?: string
}

export interface TeamGroupUpdate {
  group_name?: string
  group_name_en?: string
  description?: string
  sort_order?: number
  is_active?: boolean
}

// ===== 催收小组 =====
export interface CollectionTeam {
  id: number
  agency_id: number
  team_group_id?: number
  queue_id: number
  team_code: string
  team_name: string
  team_name_en?: string
  team_leader_id?: number
  team_type?: string
  description?: string
  max_case_count: number
  sort_order: number
  is_active: boolean
  agency_name?: string
  team_group_name?: string
  queue_name?: string
  team_leader_name?: string
  collector_count: number
  case_count: number
  created_at: string
  updated_at: string
}

export interface CollectionTeamCreate {
  agency_id: number
  team_group_id?: number
  queue_id: number
  team_code: string
  team_name: string
  team_name_en?: string
  team_leader_id?: number
  team_type?: string
  description?: string
  max_case_count?: number
  sort_order?: number
  is_active?: boolean
}

export interface CollectionTeamUpdate {
  team_group_id?: number
  queue_id?: number
  team_name?: string
  team_name_en?: string
  team_leader_id?: number
  team_type?: string
  description?: string
  max_case_count?: number
  sort_order?: number
  is_active?: boolean
}

// ===== 催员 =====
export interface Collector {
  id: number
  team_id: number
  user_id: number
  collector_code: string
  collector_name: string
  mobile_number?: string
  email?: string
  employee_no?: string
  collector_level?: string
  max_case_count: number
  current_case_count: number
  specialties?: string[]
  performance_score?: number
  status: string
  hire_date?: string
  is_active: boolean
  agency_name?: string
  team_name?: string
  created_at: string
  updated_at: string
}

export interface CollectorCreate {
  team_id: number
  user_id: number
  collector_code: string
  collector_name: string
  mobile_number?: string
  email?: string
  employee_no?: string
  collector_level?: string
  max_case_count?: number
  specialties?: string[]
  hire_date?: string
  is_active?: boolean
}

export interface CollectorUpdate {
  team_id?: number
  collector_name?: string
  mobile_number?: string
  email?: string
  employee_no?: string
  collector_level?: string
  max_case_count?: number
  specialties?: string[]
  status?: string
  is_active?: boolean
}

export interface CollectorStatistics {
  today_assigned: number
  week_assigned: number
  month_settled: number
  month_amount_collected: number
  month_contact_count: number
  case_status_distribution: Record<string, number>
}

export interface CollectorDetail extends Collector {
  statistics?: CollectorStatistics
}

// ===== 案件分配 =====
export interface AssignToAgency {
  case_ids: number[]
  agency_id: number
  reason?: string
  remarks?: string
}

export interface AssignToTeam {
  case_ids: number[]
  team_id: number
  reason?: string
  remarks?: string
}

export interface AssignToCollector {
  case_ids: number[]
  collector_id: number
  reason?: string
  remarks?: string
}

export interface AutoAssignRequest {
  case_ids: number[]
  assignment_rule: 'balanced' | 'random' | 'skill_match'
  target_level: 'agency' | 'team' | 'collector'
  agency_id?: number
  team_id?: number
  reason?: string
  remarks?: string
}

export interface TransferCaseRequest {
  case_ids: number[]
  from_collector_id: number
  to_collector_id: number
  reason: string
  remarks?: string
}

export interface ReclaimCaseRequest {
  case_ids: number[]
  reason: string
  target_level: 'team' | 'agency'
}

export interface AssignmentResult {
  case_id: number
  assigned_to_collector_id?: number
  assigned_to_collector_name?: string
  assigned_to_team_id?: number
  assigned_to_team_name?: string
  assigned_to_agency_id?: number
  assigned_to_agency_name?: string
}

export interface AssignmentResponse {
  assigned_count: number
  assignments: AssignmentResult[]
}

// ===== 案件分配历史 =====
export interface CaseAssignmentHistory {
  id: number
  case_id: number
  assignment_type: string
  from_agency_name?: string
  to_agency_name?: string
  from_team_name?: string
  to_team_name?: string
  from_collector_name?: string
  to_collector_name?: string
  reason?: string
  assigned_by_name?: string
  assigned_at: string
  remarks?: string
}

// ===== 机构作息时间 =====
export interface TimeSlot {
  start: string  // 格式：HH:MM（24小时制）
  end: string    // 格式：HH:MM（24小时制）
}

export interface AgencyWorkingHours {
  id: number
  agency_id: number
  day_of_week: number  // 0=周一，6=周日
  time_slots: TimeSlot[]
  created_at: string
  updated_at: string
}

export interface AgencyWorkingHoursUpdate {
  time_slots: TimeSlot[]
}

export interface AgencyWorkingHoursBatchUpdate {
  working_hours: Array<{
    agency_id: number
    day_of_week: number
    time_slots: TimeSlot[]
  }>
}

