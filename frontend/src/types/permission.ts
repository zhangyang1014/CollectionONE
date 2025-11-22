/**
 * 权限系统类型定义
 * 支持新的三级权限体系：不可见、仅可见、可编辑
 */

// 权限级别枚举
export enum PermissionLevel {
  NONE = 'none',           // 不可见
  READONLY = 'readonly',   // 仅可见（可见不可改）
  EDITABLE = 'editable'    // 可编辑（可见可编辑）
}

// 角色代码枚举
export enum RoleCode {
  SUPER_ADMIN = 'SUPER_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  AGENCY_ADMIN = 'AGENCY_ADMIN',
  TEAM_LEADER = 'TEAM_LEADER',
  QUALITY_INSPECTOR = 'QUALITY_INSPECTOR',
  DATA_SOURCE = 'DATA_SOURCE',
  COLLECTOR = 'COLLECTOR'
}

// 权限模块
export interface PermissionModule {
  id: number
  module_key: string
  module_name: string
  sort_order: number
  is_active: boolean
  created_at: string
  updated_at: string
}

// 权限项
export interface PermissionItem {
  id: number
  module_id: number
  module_key?: string
  item_key: string
  item_name: string
  description?: string
  sort_order: number
  is_active: boolean
  created_at: string
  updated_at: string
}

// 角色权限配置
export interface RolePermissionConfig {
  id: number
  tenant_id: number | null  // null 表示系统默认配置
  role_code: RoleCode | string
  permission_item_id: number
  permission_level: PermissionLevel
  created_at: string
  updated_at: string
  created_by?: number
  updated_by?: number
}

// 权限矩阵数据（用于前端展示）
export interface PermissionMatrixData {
  modules: PermissionModule[]
  items: PermissionItem[]
  configs: RolePermissionConfig[]
}

// 权限矩阵单元格数据
export interface PermissionCell {
  item_id: number
  item_key: string
  item_name: string
  role_code: RoleCode | string
  permission_level: PermissionLevel
  config_id?: number  // 如果已有配置，存储配置ID
}

// 批量更新权限配置请求
export interface BatchUpdatePermissionRequest {
  tenant_id?: number | null
  updates: Array<{
    role_code: RoleCode | string
    permission_item_id: number
    permission_level: PermissionLevel
  }>
}

// 可配置角色列表响应
export interface ConfigurableRolesResponse {
  current_role: RoleCode | string
  configurable_roles: Array<{
    code: RoleCode | string
    name: string
  }>
}

// 角色信息
export interface RoleInfo {
  code: RoleCode | string
  name: string
  description?: string
}

// 权限级别显示信息
export interface PermissionLevelDisplay {
  level: PermissionLevel
  label: string
  icon: string
  color: string
  tagType: 'success' | 'primary' | 'info' | 'warning' | 'danger'
}

// 权限级别显示配置
export const PERMISSION_LEVEL_DISPLAY: Record<PermissionLevel, PermissionLevelDisplay> = {
  [PermissionLevel.NONE]: {
    level: PermissionLevel.NONE,
    label: '不可见',
    icon: '❌',
    color: '#909399',
    tagType: 'info'
  },
  [PermissionLevel.READONLY]: {
    level: PermissionLevel.READONLY,
    label: '仅可见',
    icon: '👁️',
    color: '#409EFF',
    tagType: 'primary'
  },
  [PermissionLevel.EDITABLE]: {
    level: PermissionLevel.EDITABLE,
    label: '可编辑',
    icon: '✏️',
    color: '#67C23A',
    tagType: 'success'
  }
}

// 角色显示配置
export const ROLE_DISPLAY: Record<string, RoleInfo> = {
  [RoleCode.SUPER_ADMIN]: {
    code: RoleCode.SUPER_ADMIN,
    name: '超级管理员',
    description: '系统最高权限角色'
  },
  [RoleCode.TENANT_ADMIN]: {
    code: RoleCode.TENANT_ADMIN,
    name: '甲方管理员',
    description: '管理单个甲方的配置和运营'
  },
  [RoleCode.AGENCY_ADMIN]: {
    code: RoleCode.AGENCY_ADMIN,
    name: '机构管理员',
    description: '管理单个机构的日常运营'
  },
  [RoleCode.TEAM_LEADER]: {
    code: RoleCode.TEAM_LEADER,
    name: '小组长',
    description: '管理小组的日常催收工作'
  },
  [RoleCode.QUALITY_INSPECTOR]: {
    code: RoleCode.QUALITY_INSPECTOR,
    name: '质检员',
    description: '质量检查和监督'
  },
  [RoleCode.DATA_SOURCE]: {
    code: RoleCode.DATA_SOURCE,
    name: '数据源',
    description: '数据查看和分析'
  },
  [RoleCode.COLLECTOR]: {
    code: RoleCode.COLLECTOR,
    name: '催员',
    description: '一线催收人员'
  }
}

// 获取下一个权限级别（用于点击循环切换）
export function getNextPermissionLevel(current: PermissionLevel): PermissionLevel {
  const levels = [PermissionLevel.NONE, PermissionLevel.READONLY, PermissionLevel.EDITABLE]
  const currentIndex = levels.indexOf(current)
  const nextIndex = (currentIndex + 1) % levels.length
  return levels[nextIndex]
}

// 判断权限级别是否有访问权限
export function hasAccess(level: PermissionLevel): boolean {
  return level === PermissionLevel.READONLY || level === PermissionLevel.EDITABLE
}

// 判断权限级别是否可编辑
export function canEdit(level: PermissionLevel): boolean {
  return level === PermissionLevel.EDITABLE
}

// 从旧格式转换到新格式
export function convertFromOldPermissionFormat(oldPermission: 'yes' | 'no' | 'limited'): PermissionLevel {
  const mapping: Record<string, PermissionLevel> = {
    'yes': PermissionLevel.EDITABLE,
    'no': PermissionLevel.NONE,
    'limited': PermissionLevel.READONLY
  }
  return mapping[oldPermission] || PermissionLevel.NONE
}

