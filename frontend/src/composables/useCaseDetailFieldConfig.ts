/**
 * 案件详情字段配置Hook
 * 用于两个场景:
 * 1. admin_case_detail - 控台案件详情
 * 2. collector_case_detail - IM端案件详情
 * 
 * @author CCO Team
 * @since 2025-12-07
 */
import { ref, computed, watch } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'
import { getCaseDetailFieldConfigs } from '@/api/caseDetailFieldConfig'

/**
 * 详情场景类型
 */
export type DetailSceneType = 'admin_case_detail' | 'collector_case_detail'

/**
 * 场景配置
 */
export const DETAIL_SCENE_CONFIG = {
  admin_case_detail: {
    name: '控台案件详情',
    description: '管理后台的案件详情页面'
  },
  collector_case_detail: {
    name: 'IM端案件详情',
    description: '催员端的案件详情页面'
  }
}

/**
 * 案件详情字段配置Hook选项
 */
export interface UseCaseDetailFieldConfigOptions {
  /** 甲方ID */
  tenantId: Ref<number | string | null | undefined>
  /** 场景类型 */
  sceneType: DetailSceneType
  /** 是否自动加载 */
  autoLoad?: boolean
}

/**
 * 案件详情字段配置Hook
 */
export function useCaseDetailFieldConfig(options: UseCaseDetailFieldConfigOptions) {
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
      console.warn(`[useCaseDetailFieldConfig] 甲方ID为空,无法加载配置`)
      configs.value = []
      return
    }

    loading.value = true
    try {
      // 使用案件详情专用API
      const data = await getCaseDetailFieldConfigs({
        tenantId: Number(tid),
        sceneType: sceneType
      })
      configs.value = Array.isArray(data) ? data : []
      
      console.log(`[useCaseDetailFieldConfig] 已加载${sceneType}的字段配置,共${configs.value.length}个字段`)
    } catch (error) {
      console.error(`[useCaseDetailFieldConfig] 加载字段配置失败:`, error)
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
   * 获取字段的显示宽度
   */
  const getFieldWidth = (fieldKey: string): number | undefined => {
    const config = configs.value.find(c => c.field_key === fieldKey)
    if (!config) return undefined
    
    // 详情页一般不需要固定宽度
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
    loadConfigs,
    getFieldWidth,
    getFieldColorType,
    formatFieldValue,
    shouldHideField,
    getDetailGroups
  }
}

// 兼容性导出 - 保持旧名称可用
export { useCaseDetailFieldConfig as useFieldDetailConfig }

