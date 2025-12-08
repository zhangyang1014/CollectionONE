/**
 * 案件列表字段配置Hook
 * 用于两个场景:
 * 1. admin_case_list - 控台案件列表
 * 2. collector_case_list - IM端案件列表
 * 
 * @author CCO Team
 * @since 2025-12-07
 */
import { ref, computed, watch } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'
import { getCaseListFieldConfigs } from '@/api/caseListFieldConfig'

/**
 * 列表场景类型
 */
export type ListSceneType = 'admin_case_list' | 'collector_case_list'

/**
 * 场景配置
 */
export const LIST_SCENE_CONFIG = {
  admin_case_list: {
    name: '控台案件列表',
    description: '管理后台的案件列表页面'
  },
  collector_case_list: {
    name: 'IM端案件列表',
    description: '催员端的案件列表页面'
  }
}

/**
 * 控台案件列表必须展示的字段(不可配置隐藏)
 */
export const ADMIN_CASE_LIST_REQUIRED_FIELDS = [
  'case_code',        // 案件编号
  'user_name',        // 客户
  'loan_amount',      // 贷款金额
  'outstanding_amount', // 未还金额
  'overdue_days',     // 逾期天数
  'case_status',      // 案件状态
  'due_date'          // 到期日期
]

/**
 * 不在列表中展示但支持搜索的字段
 * 说明：原先将手机号标记为仅搜索字段；但控台已要求可点击展示手机号，故这里留空。
 */
export const SEARCH_ONLY_FIELDS: string[] = []

/**
 * 案件列表字段配置Hook选项
 */
export interface UseCaseListFieldConfigOptions {
  /** 甲方ID */
  tenantId: Ref<number | string | null | undefined>
  /** 场景类型 */
  sceneType: ListSceneType
  /** 是否自动加载 */
  autoLoad?: boolean
}

/**
 * 案件列表字段配置Hook
 */
export function useCaseListFieldConfig(options: UseCaseListFieldConfigOptions) {
  const { tenantId, sceneType, autoLoad = true } = options

  // 兼容：后端当前只接受详情场景(admin_case_detail / collector_case_detail)
  const normalizeSceneType = (scene: string): ListSceneType => {
    if (scene === 'admin_case_list') return 'admin_case_detail'
    if (scene === 'collector_case_list') return 'collector_case_detail'
    return scene as ListSceneType
  }
  const normalizedSceneType = normalizeSceneType(sceneType)

  // 状态
  const loading = ref(false)
  const configs = ref<FieldDisplayConfig[]>([])
  
  /**
   * 加载字段展示配置
   */
  const loadConfigs = async () => {
    const tid = tenantId.value
    if (!tid) {
      console.warn(`[useCaseListFieldConfig] 甲方ID为空,无法加载配置`)
      configs.value = []
      return
    }

    loading.value = true
    try {
      // 使用案件列表专用API
      const data = await getCaseListFieldConfigs({
        tenantId: Number(tid),
        sceneType: normalizedSceneType
      })
      configs.value = Array.isArray(data) ? data : []
      
      console.log(`[useCaseListFieldConfig] 已加载${normalizedSceneType}的字段配置,共${configs.value.length}个字段`)
    } catch (error) {
      console.error(`[useCaseListFieldConfig] 加载字段配置失败:`, error)
      ElMessage.error('加载字段配置失败')
      configs.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 有效的字段配置(已启用且可见)
   */
  const visibleConfigs = computed(() => {
    return configs.value.filter(c => {
      // 注意: 新版模型已移除is_enabled和is_visible字段
      // 现在只要配置存在就表示需要显示
      return true
    }).sort((a, b) => a.sort_order - b.sort_order)
  })

  /**
   * 可筛选的字段(枚举类型)
   */
  const filterableFields = computed(() => {
    return visibleConfigs.value.filter(c => c.is_filterable)
  })

  /**
   * 可范围检索的字段(数字和时间类型)
   */
  const rangeSearchableFields = computed(() => {
    return visibleConfigs.value.filter(c => c.is_range_searchable)
  })

  /**
   * 获取字段的显示宽度
   */
  const getFieldWidth = (fieldKey: string): number | undefined => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    if (!config) return undefined
    
    // 0表示自动宽度
    return config.display_width === 0 ? undefined : config.display_width
  }

  /**
   * 获取字段的颜色类型
   */
  const getFieldColorType = (fieldKey: string): string => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    return config?.color_type || 'normal'
  }

  /**
   * 获取字段的对齐方式(从format_rule中获取,或默认左对齐)
   */
  const getFieldAlign = (fieldKey: string): 'left' | 'center' | 'right' => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    // 数字和金额类型默认右对齐
    if (config?.field_data_type === 'Integer' || 
        config?.field_data_type === 'Decimal' ||
        config?.format_rule?.format_type === 'currency') {
      return 'right'
    }
    return 'left'
  }

  /**
   * 格式化字段值
   */
  const formatFieldValue = (fieldKey: string, value: any): string => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    if (!config || value === null || value === undefined) {
      return '-'
    }

    const { format_rule } = config
    
    // 货币格式化
    if (format_rule?.format_type === 'currency') {
      const num = parseFloat(value) || 0
      const formatted = num.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
      return `${format_rule.prefix || ''}${formatted}${format_rule.suffix || ''}`
    }

    // 百分比格式化
    if (format_rule?.format_type === 'percentage') {
      const num = parseFloat(value) || 0
      return `${num}${format_rule.suffix || '%'}`
    }

    // 默认返回字符串
    return String(value)
  }

  /**
   * 判断字段是否应该隐藏(仅催员端)
   * @param fieldKey 字段key
   * @param context 上下文信息(队列ID、机构ID、小组ID)
   */
  const shouldHideField = (
    fieldKey: string, 
    context: { queueId?: number; agencyId?: number; teamId?: number }
  ): boolean => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    if (!config) return false

    // 检查队列隐藏规则
    if (context.queueId && config.hide_for_queues?.includes(String(context.queueId))) {
      return true
    }

    // 检查机构隐藏规则
    if (context.agencyId && config.hide_for_agencies?.includes(String(context.agencyId))) {
      return true
    }

    // 检查小组隐藏规则
    if (context.teamId && config.hide_for_teams?.includes(String(context.teamId))) {
      return true
    }

    return false
  }

  /**
   * 获取表格列配置(用于el-table)
   */
  const getTableColumns = (
    hideContext?: { queueId?: number; agencyId?: number; teamId?: number }
  ) => {
    return visibleConfigs.value
      .filter(config => {
        // 应用隐藏规则
        if (hideContext) {
          return !shouldHideField(config.field_key, hideContext)
        }
        return true
      })
      .map(config => ({
        prop: config.field_key,
        label: config.field_name,
        width: config.display_width === 0 ? undefined : config.display_width,
        minWidth: config.display_width === 0 ? 100 : undefined,
        align: getFieldAlign(config.field_key),
        sortable: config.is_range_searchable, // 数字和时间字段支持排序
        showOverflowTooltip: true,
        fieldDataType: config.field_data_type,
        colorType: config.color_type,
        formatRule: config.format_rule,
        isRequired: sceneType === 'admin_case_list' 
          ? ADMIN_CASE_LIST_REQUIRED_FIELDS.includes(config.field_key)
          : false
      }))
  }

  // 监听tenantId变化自动重新加载
  if (autoLoad) {
    watch(
      () => tenantId.value,
      (newVal, oldVal) => {
        if (newVal && newVal !== oldVal) {
          loadConfigs()
        }
      },
      { immediate: true }
    )
  }

  return {
    loading,
    configs,
    visibleConfigs,
    filterableFields,
    rangeSearchableFields,
    loadConfigs,
    getFieldWidth,
    getFieldColorType,
    getFieldAlign,
    formatFieldValue,
    shouldHideField,
    getTableColumns
  }
}

// 兼容性导出 - 保持旧名称可用
export { useCaseListFieldConfig as useFieldListConfig }

