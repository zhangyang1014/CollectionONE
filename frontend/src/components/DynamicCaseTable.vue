<template>
  <el-table
    :data="data"
    v-bind="$attrs"
    :row-class-name="rowClassName"
    v-loading="loading"
  >
    <!-- 选择框(可选) -->
    <el-table-column 
      v-if="showSelection" 
      type="selection" 
      width="55" 
      fixed="left"
    />

    <!-- 前置自定义列 -->
    <slot name="prepend-columns" />

    <!-- 动态列 -->
    <el-table-column
      v-for="column in effectiveColumns"
      :key="column.prop"
      :prop="column.prop"
      :label="column.label"
      :width="column.width"
      :min-width="column.minWidth"
      :align="column.align"
      :sortable="column.sortable"
      :show-overflow-tooltip="column.showOverflowTooltip"
      :fixed="column.fixed"
    >
      <template #default="{ row }">
        <div :class="getCellClass(column, row)">
          <!-- 自定义插槽优先 -->
          <slot 
            :name="`cell-${column.prop}`" 
            :row="row" 
            :column="column"
            :value="row[column.prop]"
            :formatted-value="formatValue(column, row[column.prop])"
          >
            <!-- 默认渲染 -->
            <component 
              :is="getCellComponent(column)" 
              :column="column" 
              :value="row[column.prop]" 
              :row="row"
            />
          </slot>
        </div>
      </template>
    </el-table-column>

    <!-- 操作列(可选) -->
    <el-table-column
      v-if="showActions"
      label="操作"
      :width="actionsWidth"
      fixed="right"
    >
      <template #default="{ row }">
        <slot name="actions" :row="row" />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { ElTag } from 'element-plus'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'

/**
 * Props
 */
interface Props {
  /** 表格数据 */
  data: any[]
  /** 字段展示配置 */
  columns: Array<{
    prop: string
    label: string
    width?: number
    minWidth?: number
    align?: 'left' | 'center' | 'right'
    sortable?: boolean
    showOverflowTooltip?: boolean
    fixed?: boolean | 'left' | 'right'
    fieldDataType?: string
    colorType?: string
    formatRule?: any
  }>
  /** 是否显示选择框 */
  showSelection?: boolean
  /** 是否显示操作列 */
  showActions?: boolean
  /** 操作列宽度 */
  actionsWidth?: number
  /** 加载状态 */
  loading?: boolean
  /** 行className函数 */
  rowClassName?: (row: any) => string
  /** 隐藏的字段key */
  hiddenFields?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  showSelection: false,
  showActions: false,
  actionsWidth: 150,
  loading: false,
  hiddenFields: () => []
})

/**
 * 有效的列(排除隐藏的)
 */
const effectiveColumns = computed(() => {
  return props.columns.filter(col => !props.hiddenFields.includes(col.prop))
})

/**
 * 格式化值
 */
const formatValue = (column: any, value: any): string => {
  if (value === null || value === undefined) {
    return '-'
  }

  const { formatRule } = column
  
  // 货币格式化
  if (formatRule?.format_type === 'currency') {
    const num = parseFloat(value) || 0
    const formatted = num.toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
    return `${formatRule.prefix || ''}${formatted}${formatRule.suffix || ''}`
  }

  // 百分比格式化
  if (formatRule?.format_type === 'percentage') {
    const num = parseFloat(value) || 0
    return `${num}${formatRule.suffix || '%'}`
  }

  // 日期格式化
  if (column.fieldDataType === 'Date' && value) {
    return String(value).replace('T', ' ').substring(0, 19)
  }

  return String(value)
}

/**
 * 获取单元格CSS类
 */
const getCellClass = (column: any, row: any) => {
  const classes = []
  
  // 颜色类型
  if (column.colorType) {
    classes.push(`cell-color-${column.colorType}`)
  }
  
  // 数字右对齐
  if (column.align === 'right') {
    classes.push('cell-align-right')
  }
  
  return classes.join(' ')
}

/**
 * 获取单元格组件
 */
const getCellComponent = (column: any) => {
  return (props: any) => {
    const { value, column: col } = props
    
    // Tag类型(枚举)
    if (col.fieldDataType === 'Enum') {
      let type: any = 'info'
      if (col.colorType === 'red') type = 'danger'
      else if (col.colorType === 'yellow') type = 'warning'
      else if (col.colorType === 'green') type = 'success'
      
      return h(ElTag, { type, size: 'small' }, { default: () => formatValue(col, value) })
    }
    
    // 默认文本
    return h('span', formatValue(col, value))
  }
}
</script>

<style scoped>
/* 颜色类型 */
.cell-color-red {
  color: #f56c6c;
}

.cell-color-yellow {
  color: #e6a23c;
}

.cell-color-green {
  color: #67c23a;
}

.cell-align-right {
  text-align: right;
}

/* 金额样式 */
:deep(.amount) {
  font-weight: 500;
  color: #f56c6c;
}
</style>



