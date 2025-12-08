/**
 * 兼容导出：保留 useFieldListConfig 名称
 * 实际实现复用新的 useCaseListFieldConfig
 */
import { useCaseListFieldConfig } from './useCaseListFieldConfig'

export { useCaseListFieldConfig }

export function useFieldListConfig(options: Parameters<typeof useCaseListFieldConfig>[0]) {
  return useCaseListFieldConfig(options)
}

export default useCaseListFieldConfig

