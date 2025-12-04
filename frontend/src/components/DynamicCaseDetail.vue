<template>
  <div class="dynamic-case-detail">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 动态生成Tab页 -->
      <el-tab-pane
        v-for="group in groupedFields"
        :key="group.name"
        :label="group.label"
        :name="group.name"
      >
        <el-descriptions
          :column="group.column || 2"
          border
          size="small"
        >
          <el-descriptions-item
            v-for="field in group.fields"
            :key="field.field_key"
            :label="field.field_name"
            :span="field.span || 1"
          >
            <!-- 自定义插槽优先 -->
            <slot
              :name="`field-${field.field_key}`"
              :field="field"
              :value="getValue(field.field_key)"
              :formatted-value="formatValue(field)"
            >
              <!-- 默认渲染 -->
              {{ formatValue(field) }}
            </slot>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'

/**
 * Props
 */
interface Props {
  /** 案件数据 */
  caseData: any
  /** 字段展示配置 */
  fields: FieldDisplayConfig[]
  /** 字段分组配置 */
  groupConfig?: Array<{
    name: string
    label: string
    fieldKeys: string[]
    column?: number
  }>
}

const props = withDefaults(defineProps<Props>(), {
  groupConfig: () => [
    {
      name: 'basic',
      label: '基本信息',
      fieldKeys: [],
      column: 2
    }
  ]
})

// 当前激活的Tab
const activeTab = ref(props.groupConfig[0]?.name || 'basic')

/**
 * 获取字段值
 */
const getValue = (fieldKey: string): any => {
  return props.caseData?.[fieldKey]
}

/**
 * 格式化字段值
 */
const formatValue = (field: FieldDisplayConfig): string => {
  const value = getValue(field.field_key)
  
  if (value === null || value === undefined) {
    return '-'
  }

  const { format_rule, field_data_type } = field
  
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

  // 日期格式化
  if (field_data_type === 'Date' && value) {
    return String(value).replace('T', ' ').substring(0, 19)
  }

  // 布尔值
  if (field_data_type === 'Boolean') {
    return value ? '是' : '否'
  }

  return String(value)
}

/**
 * 字段分组
 */
const groupedFields = computed(() => {
  return props.groupConfig.map(group => {
    // 如果指定了fieldKeys,使用指定的;否则使用所有fields
    const fields = group.fieldKeys.length > 0
      ? props.fields.filter(f => group.fieldKeys.includes(f.field_key))
      : props.fields
    
    return {
      ...group,
      fields: fields.sort((a, b) => a.sort_order - b.sort_order)
    }
  })
})
</script>

<style scoped>
.dynamic-case-detail {
  height: 100%;
}

:deep(.el-tabs) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

:deep(.el-descriptions) {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  background-color: #f5f7fa;
}
</style>






























