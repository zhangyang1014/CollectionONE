<template>
  <el-table
    ref="elTableRef"
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
      :selectable="selectable"
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
            :value="getFieldValue(row, column.prop)"
            :formatted-value="formatValue(column, getFieldValue(row, column.prop))"
          >
            <!-- 默认渲染 -->
            <component 
              :is="getCellComponent(column)" 
              :column="column" 
              :value="getFieldValue(row, column.prop)" 
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
import { computed, h, ref, nextTick } from 'vue'
import { ElTag } from 'element-plus'
import type { FieldDisplayConfig } from '@/types/fieldDisplay'

// #region agent log
// 内部el-table的ref
const elTableRef = ref<InstanceType<typeof import('element-plus').ElTable>>()
// #endregion

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
  /** 行是否可选择函数 */
  selectable?: (row: any, index: number) => boolean
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
 * 获取字段值（支持snake_case和camelCase映射）
 * 前端配置使用snake_case（case_code），后端返回camelCase（caseCode）
 */
const getFieldValue = (row: any, fieldKey: string): any => {
  if (!row || !fieldKey) {
    return undefined
  }
  
  // 先尝试直接获取
  if (fieldKey in row) {
    const val = row[fieldKey]
    if (val !== undefined && val !== null && val !== '') {
      return val
    }
  }
  
  // 如果fieldKey是snake_case，尝试转换为camelCase
  if (fieldKey.includes('_')) {
    const camelCase = snakeToCamel(fieldKey)
    if (camelCase in row) {
      const val = row[camelCase]
      if (val !== undefined && val !== null && val !== '') {
        return val
      }
    }
  }
  
  // 如果fieldKey是camelCase，尝试转换为snake_case
  const snakeCase = camelToSnake(fieldKey)
  if (snakeCase in row) {
    const val = row[snakeCase]
    if (val !== undefined && val !== null && val !== '') {
      return val
    }
  }
  
  return undefined
}

/**
 * snake_case转camelCase
 */
const snakeToCamel = (str: string): string => {
  return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
}

/**
 * camelCase转snake_case
 */
const camelToSnake = (str: string): string => {
  return str.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)
}

/**
 * 格式化值
 */
const formatValue = (column: any, value: any): string => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  const { formatRule, fieldDataType } = column
  
  // 枚举类型特殊处理
  if (fieldDataType === 'Enum') {
    // 案件状态映射
    if (column.prop === 'case_status' || column.prop === 'caseStatus') {
      const statusMap: Record<string, string> = {
        'pending_repayment': '待还款',
        'partial_repayment': '部分还款',
        'normal_settlement': '正常结清',
        'extension_settlement': '展期结清'
      }
      return statusMap[String(value)] || String(value)
    }
    return String(value)
  }
  
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
  if (fieldDataType === 'Date' && value) {
    return String(value).replace('T', ' ').substring(0, 19)
  }

  // 整数格式化（逾期天数）
  if (fieldDataType === 'Integer' && value !== null && value !== undefined) {
    return String(value)
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

// #region agent log
// 暴露方法给父组件使用
defineExpose({
  clearSelection() {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'DynamicCaseTable.vue:clearSelection',message:'clearSelection called',data:{elTableRefExists:!!elTableRef.value,elTableRefType:elTableRef.value?.constructor?.name},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
    // #endregion
    if (elTableRef.value && typeof elTableRef.value.clearSelection === 'function') {
      // #region agent log
      fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'DynamicCaseTable.vue:clearSelection',message:'calling elTableRef.clearSelection',data:{},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
      // #endregion
      elTableRef.value.clearSelection()
    } else {
      // #region agent log
      fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'DynamicCaseTable.vue:clearSelection',message:'elTableRef invalid',data:{elTableRefExists:!!elTableRef.value,hasMethod:typeof elTableRef.value?.clearSelection},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'A'})}).catch(()=>{});
      // #endregion
      console.warn('elTableRef.clearSelection is not available', elTableRef.value)
    }
  },
  toggleRowSelection(row: any, selected?: boolean) {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'DynamicCaseTable.vue:toggleRowSelection',message:'toggleRowSelection called',data:{elTableRefExists:!!elTableRef.value},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
    // #endregion
    if (elTableRef.value && typeof elTableRef.value.toggleRowSelection === 'function') {
      elTableRef.value.toggleRowSelection(row, selected)
    }
  },
  // 暴露el-table的其他常用方法
  getSelectionRows() {
    return elTableRef.value?.getSelectionRows() || []
  }
})
// #endregion
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



