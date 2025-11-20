/**
 * 字段展示配置相关类型定义
 */

export interface ColorRule {
  condition: string // 条件表达式
  color: string // 颜色：red/yellow/green/normal
}

export interface HideRule {
  rule_type: string // 规则类型：queue/agency/team
  target_ids: string[] // 目标ID列表
}

export interface FormatRule {
  format_type: string // 格式类型：date/number/currency/percent/custom
  format_pattern?: string // 格式模式
  prefix?: string // 前缀
  suffix?: string // 后缀
}

export interface FieldDisplayConfig {
  id: number
  tenant_id: string
  scene_type: string // 场景类型：admin_case_list/collector_case_list/collector_case_detail
  scene_name: string // 场景名称
  field_key: string // 字段标识
  field_name: string // 字段名称
  field_data_type?: string // 字段数据类型：String/Integer/Boolean/Enum等
  field_source?: string // 字段来源：standard/extended/custom
  sort_order: number // 排序顺序
  display_width: number // 显示宽度（像素），0表示自动
  color_type: string // 颜色类型：normal/red/yellow/green
  color_rule?: ColorRule[] // 颜色规则
  hide_rule?: HideRule[] // 隐藏规则
  hide_for_queues?: string[] // 对哪些队列隐藏
  hide_for_agencies?: string[] // 对哪些机构隐藏
  hide_for_teams?: string[] // 对哪些小组隐藏
  format_rule?: FormatRule // 格式化规则
  is_searchable: boolean // 是否可搜索（针对文本字段）
  is_filterable: boolean // 是否可筛选（针对枚举字段）
  is_range_searchable: boolean // 是否支持范围检索（针对数字和时间字段）
  created_at: string
  updated_at: string
  created_by?: string
  updated_by?: string
}

export interface FieldDisplayConfigCreate {
  tenant_id: string
  scene_type: string
  scene_name: string
  field_key: string
  field_name: string
  field_data_type?: string
  field_source?: string
  sort_order?: number
  display_width?: number
  color_type?: string
  color_rule?: ColorRule[]
  hide_rule?: HideRule[]
  hide_for_queues?: string[]
  hide_for_agencies?: string[]
  hide_for_teams?: string[]
  format_rule?: FormatRule
  is_searchable?: boolean
  is_filterable?: boolean
  is_range_searchable?: boolean
  created_by?: string
}

export interface FieldDisplayConfigUpdate {
  scene_name?: string
  field_name?: string
  field_data_type?: string
  field_source?: string
  sort_order?: number
  display_width?: number
  color_type?: string
  color_rule?: ColorRule[]
  hide_rule?: HideRule[]
  hide_for_queues?: string[]
  hide_for_agencies?: string[]
  hide_for_teams?: string[]
  format_rule?: FormatRule
  is_searchable?: boolean
  is_filterable?: boolean
  is_range_searchable?: boolean
  updated_by?: string
}

export interface SceneType {
  key: string // 场景键值
  name: string // 场景名称
  description: string // 场景描述
}

export interface FieldDisplayConfigQuery {
  tenant_id?: string
  scene_type?: string
  field_key?: string
  is_enabled?: boolean
}

export interface AvailableFieldOption {
  field_key: string // 字段标识
  field_name: string // 字段名称
  field_type: string // 字段数据类型
  field_source: string // 字段来源：standard/extended/custom
  field_group_name?: string // 字段分组名称
  is_extended: boolean // 是否为扩展字段
  is_required: boolean // 是否必填
  enum_options?: any[] // 枚举选项
  description?: string // 字段描述
}

