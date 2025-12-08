<template>
  <el-dialog
    v-model="dialogVisible"
    title="枚举值映射配置"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="enum-mapping-dialog">
      <!-- 字段信息 -->
      <div v-if="standardField" class="field-info-bar">
        <div class="field-info-item">
          <span class="label">标准字段：</span>
          <el-tag type="primary">{{ standardField.field_name }} ({{ standardField.field_key }})</el-tag>
        </div>
        <div class="field-info-item">
          <span class="label">甲方字段：</span>
          <el-tag type="success">{{ standardField.tenant_field_name || standardField.tenant_field_key }}</el-tag>
        </div>
      </div>

      <el-divider />

      <!-- 枚举值映射表格 -->
      <div class="mapping-table-container">
        <div class="table-header">
          <div class="header-left">
            <h4>枚举值映射关系</h4>
            <p class="tip-text">💡 提示：所有标准枚举值必须映射到甲方枚举值</p>
          </div>
          <div class="header-right">
            <el-button 
              type="primary" 
              size="small"
              @click="autoMatchEnums"
            >
              智能匹配
            </el-button>
          </div>
        </div>

        <div class="mapping-grid">
          <div class="grid-header">
            <div class="col-standard">标准枚举值</div>
            <div class="col-arrow"></div>
            <div class="col-tenant">甲方枚举值</div>
            <div class="col-status">状态</div>
          </div>

          <div 
            v-for="(mapping, index) in enumMappings" 
            :key="index"
            class="grid-row"
            :class="{ 'matched': mapping.matched }"
          >
            <!-- 标准枚举值 -->
            <div class="col-standard">
              <div class="enum-box standard">
                <div class="enum-value">{{ mapping.standardValue }}</div>
                <div class="enum-label">{{ mapping.standardLabel }}</div>
              </div>
            </div>

            <!-- 箭头 -->
            <div class="col-arrow">
              <el-icon class="arrow-icon"><Right /></el-icon>
            </div>

            <!-- 甲方枚举值选择 -->
            <div class="col-tenant">
              <el-select
                v-model="mapping.tenantValue"
                placeholder="选择甲方枚举值"
                clearable
                filterable
                @change="handleMappingChange(index)"
                style="width: 100%;"
              >
                <el-option
                  v-for="tenantEnum in availableTenantEnums"
                  :key="tenantEnum.value"
                  :label="`${tenantEnum.label} (${tenantEnum.value})`"
                  :value="tenantEnum.value"
                  :disabled="isEnumValueUsed(tenantEnum.value, index)"
                >
                  <div class="enum-option">
                    <span class="enum-option-label">{{ tenantEnum.label }}</span>
                    <span class="enum-option-value">{{ tenantEnum.value }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>

            <!-- 状态 -->
            <div class="col-status">
              <el-tag 
                v-if="mapping.matched" 
                type="success" 
                size="small"
                effect="dark"
              >
                已匹配
              </el-tag>
              <el-tag 
                v-else 
                type="warning" 
                size="small"
                effect="dark"
              >
                未匹配
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 未使用的甲方枚举值 -->
        <div v-if="unusedTenantEnums.length > 0" class="unused-enums">
          <el-alert
            title="未映射的甲方枚举值"
            type="info"
            :closable="false"
            show-icon
          >
            <div class="unused-list">
              <el-tag 
                v-for="enumVal in unusedTenantEnums" 
                :key="enumVal.value"
                size="small"
                type="info"
              >
                {{ enumVal.label }} ({{ enumVal.value }})
              </el-tag>
            </div>
            <p class="unused-tip">这些枚举值未被使用，将不会参与映射。</p>
          </el-alert>
        </div>
      </div>

      <!-- 验证提示 -->
      <div v-if="!allMapped" class="validation-alert">
        <el-alert
          title="⚠️ 警告：所有标准枚举值必须映射到甲方枚举值"
          type="warning"
          :closable="false"
          show-icon
        >
          <p>未完成映射的标准枚举值：{{ unmappedCount }} 个</p>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSave"
          :disabled="!allMapped"
        >
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Right } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface EnumValue {
  value: string
  label: string
}

interface EnumMapping {
  standardValue: string
  standardLabel: string
  tenantValue: string | null
  matched: boolean
}

const props = defineProps<{
  modelValue: boolean
  standardField: any
  tenantEnums: EnumValue[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', mapping: Record<string, string>): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 枚举值映射数据
const enumMappings = ref<EnumMapping[]>([])

// 初始化映射数据
watch(() => [props.standardField, props.tenantEnums], () => {
  if (props.standardField && props.standardField.enum_values) {
    enumMappings.value = props.standardField.enum_values.map((enumVal: any) => ({
      standardValue: enumVal.standard_id || enumVal.value,
      standardLabel: enumVal.standard_name || enumVal.label,
      tenantValue: null,
      matched: false
    }))
    
    // 自动匹配
    autoMatchEnums()
  }
}, { immediate: true, deep: true })

// 可用的甲方枚举值
const availableTenantEnums = computed(() => {
  return props.tenantEnums || []
})

// 未使用的甲方枚举值
const unusedTenantEnums = computed(() => {
  const usedValues = enumMappings.value
    .filter(m => m.tenantValue)
    .map(m => m.tenantValue)
  
  return availableTenantEnums.value.filter(e => !usedValues.includes(e.value))
})

// 检查枚举值是否已被使用
const isEnumValueUsed = (value: string, currentIndex: number) => {
  return enumMappings.value.some((m, idx) => 
    idx !== currentIndex && m.tenantValue === value
  )
}

// 所有枚举值是否已映射
const allMapped = computed(() => {
  return enumMappings.value.every(m => m.matched)
})

// 未映射数量
const unmappedCount = computed(() => {
  return enumMappings.value.filter(m => !m.matched).length
})

// 映射变化处理
const handleMappingChange = (index: number) => {
  const mapping = enumMappings.value[index]
  mapping.matched = !!mapping.tenantValue
}

// 智能匹配枚举值
const autoMatchEnums = () => {
  enumMappings.value.forEach(mapping => {
    // 尝试按value匹配
    let matched = availableTenantEnums.value.find(
      te => te.value.toLowerCase() === mapping.standardValue.toLowerCase()
    )
    
    // 如果按value没匹配到，尝试按label匹配
    if (!matched) {
      matched = availableTenantEnums.value.find(
        te => te.label === mapping.standardLabel
      )
    }
    
    // 如果匹配到且未被使用，则自动填充
    if (matched && !isEnumValueUsed(matched.value, enumMappings.value.indexOf(mapping))) {
      mapping.tenantValue = matched.value
      mapping.matched = true
    }
  })
  
  const matchedCount = enumMappings.value.filter(m => m.matched).length
  if (matchedCount > 0) {
    ElMessage.success(`智能匹配成功 ${matchedCount} 个枚举值`)
  } else {
    ElMessage.info('未找到可自动匹配的枚举值，请手动配置')
  }
}

// 保存
const handleSave = () => {
  if (!allMapped.value) {
    ElMessage.warning('请完成所有枚举值的映射')
    return
  }
  
  // 构造映射对象
  const mapping: Record<string, string> = {}
  enumMappings.value.forEach(m => {
    if (m.tenantValue) {
      mapping[m.standardValue] = m.tenantValue
    }
  })
  
  emit('save', mapping)
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.enum-mapping-dialog {
  padding: 10px 0;
}

/* 字段信息栏 */
.field-info-bar {
  display: flex;
  gap: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.field-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-info-item .label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 表格容器 */
.mapping-table-container {
  margin-top: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.tip-text {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* 映射网格 */
.mapping-grid {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.grid-header {
  display: grid;
  grid-template-columns: 2fr 60px 2fr 100px;
  gap: 12px;
  padding: 12px 16px;
  background-color: #f5f7fa;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #e4e7ed;
}

.grid-row {
  display: grid;
  grid-template-columns: 2fr 60px 2fr 100px;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  align-items: center;
  transition: all 0.3s;
}

.grid-row:last-child {
  border-bottom: none;
}

.grid-row:hover {
  background-color: #f5f7fa;
}

.grid-row.matched {
  background-color: #f0f9ff;
}

.col-standard,
.col-tenant {
  display: flex;
  align-items: center;
}

.col-arrow {
  display: flex;
  justify-content: center;
  align-items: center;
}

.arrow-icon {
  font-size: 20px;
  color: #409eff;
}

.col-status {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 枚举值框 */
.enum-box {
  padding: 10px 12px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.enum-box.standard {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.enum-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
  font-family: 'Courier New', monospace;
}

.enum-label {
  font-size: 13px;
  color: #606266;
}

/* 枚举选项 */
.enum-option {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.enum-option-label {
  color: #303133;
}

.enum-option-value {
  color: #909399;
  font-size: 12px;
  font-family: 'Courier New', monospace;
}

/* 未使用的枚举值 */
.unused-enums {
  margin-top: 20px;
}

.unused-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 8px 0;
}

.unused-tip {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #909399;
}

/* 验证提示 */
.validation-alert {
  margin-top: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .grid-header,
  .grid-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .col-arrow {
    transform: rotate(90deg);
  }

  .field-info-bar {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
