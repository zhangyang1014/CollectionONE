/**
 * 甲方字段展示配置统一Hook
 * 用于三个场景:
 * 1. admin_case_list - 控台案件列表
 * 2. collector_case_list - 催员案件列表
 * 3. collector_case_detail - 催员案件详情
 */
import { ref, computed, watch } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'
import { getFieldDisplayConfigs } from '@/api/fieldDisplay'

/**
 * 场景类型
 */
export type SceneType = 'admin_case_list' | 'collector_case_list' | 'collector_case_detail'

/**
 * 场景配置
 */
export const SCENE_CONFIG = {
  admin_case_list: {
    name: '控台案件管理列表',
    description: '管理后台的案件列表页面'
  },
  collector_case_list: {
    name: '催员案件列表',
    description: '催员端的案件列表页面'
  },
  collector_case_detail: {
    name: '催员案件详情',
    description: '催员端的案件详情页面'
  }
}

/**
 * 字段展示配置Hook选项
 */
export interface UseFieldDisplayConfigOptions {
  /** 甲方ID */
  tenantId: Ref<number | string | null | undefined>
  /** 场景类型 */
  sceneType: SceneType
  /** 是否自动加载 */
  autoLoad?: boolean
}

/**
 * 字段展示配置Hook
 */
export function useFieldDisplayConfig(options: UseFieldDisplayConfigOptions) {
  const { tenantId, sceneType, autoLoad = true } = options

  // 状态
  const loading = ref(false)
  const configs = ref<FieldDisplayConfig[]>([])
  
  /**
   * 加载字段展示配置
   */
  const loadConfigs = async () => {
    const tid = tenantId.value
    if (!tid) {
      console.warn(`[useFieldDisplayConfig] 甲方ID为空,无法加载配置`)
      configs.value = []
      return
    }

    loading.value = true
    try {
      // 使用统一的API工具，自动处理认证和错误
      const data = await getFieldDisplayConfigs({
        tenant_id: tid,
        scene_type: sceneType
      })
      configs.value = Array.isArray(data) ? data : []
      
      console.log(`[useFieldDisplayConfig] 已加载${sceneType}的字段配置,共${configs.value.length}个字段`)
    } catch (error) {
      console.error(`[useFieldDisplayConfig] 加载字段配置失败:`, error)
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
        formatRule: config.format_rule
      }))
  }

  /**
   * 获取详情页分组字段
   */
  const getDetailGroups = () => {
    // 将字段按分组归类
    const groups = new Map<string, FieldDisplayConfig[]>()
    
    visibleConfigs.value.forEach(config => {
      const groupName = config.field_source || 'other'
      if (!groups.has(groupName)) {
        groups.set(groupName, [])
      }
      groups.get(groupName)!.push(config)
    })

    return Array.from(groups.entries()).map(([groupName, fields]) => ({
      groupName,
      fields
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
    getTableColumns,
    getDetailGroups
  }
}

