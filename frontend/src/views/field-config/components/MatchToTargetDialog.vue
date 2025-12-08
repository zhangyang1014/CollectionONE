<template>
  <el-dialog
    v-model="dialogVisible"
    title="匹配到目标字段"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="match-dialog">
      <!-- 甲方字段信息 -->
      <div v-if="unmappedField" class="field-info-section">
        <h4>未使用的甲方字段</h4>
        <div class="field-info-box">
          <div class="info-row">
            <span class="label">字段名称：</span>
            <span class="value">{{ unmappedField.field_name }}</span>
          </div>
          <div class="info-row">
            <span class="label">字段标识：</span>
            <span class="value code">{{ unmappedField.field_key }}</span>
          </div>
          <div class="info-row">
            <span class="label">字段类型：</span>
            <el-tag size="small">{{ unmappedField.field_type }}</el-tag>
          </div>
          <div class="info-row">
            <span class="label">是否必填：</span>
            <el-tag :type="unmappedField.is_required ? 'danger' : 'info'" size="small">
              {{ unmappedField.is_required ? '必填' : '非必填' }}
            </el-tag>
          </div>
          <div v-if="unmappedField.description" class="info-row">
            <span class="label">说明：</span>
            <span class="value">{{ unmappedField.description }}</span>
          </div>
        </div>
      </div>

      <el-divider>
        <el-icon><Bottom /></el-icon>
        匹配到
      </el-divider>

      <!-- 选择目标字段 -->
      <div class="target-selection-section">
        <h4>选择目标字段 <span class="required">*</span></h4>
        <el-select
          v-model="selectedFieldKey"
          placeholder="请选择要映射的标准字段"
          filterable
          style="width: 100%"
          size="large"
        >
          <el-option
            v-for="field in availableFields"
            :key="field.field_key"
            :label="`${field.field_name} (${field.field_key})`"
            :value="field.field_key"
          >
            <div class="field-option">
              <div class="field-option-main">
                <span class="field-name">{{ field.field_name }}</span>
                <el-tag v-if="field.is_required" type="danger" size="small" effect="dark">必填</el-tag>
              </div>
              <div class="field-option-sub">
                <span class="field-key">{{ field.field_key }}</span>
                <span class="field-type">{{ field.field_type }}</span>
              </div>
            </div>
          </el-option>
        </el-select>

        <!-- 类型匹配提示 -->
        <div v-if="selectedFieldKey && selectedFieldInfo" class="match-tip">
          <el-alert
            v-if="isTypeCompatible"
            title="✓ 字段类型匹配"
            type="success"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="type-info">
                <span>甲方字段类型：<code>{{ unmappedField?.field_type }}</code></span>
                <el-icon><Right /></el-icon>
                <span>标准字段类型：<code>{{ selectedFieldInfo.field_type }}</code></span>
              </div>
            </template>
          </el-alert>
          <el-alert
            v-else
            title="⚠️ 字段类型不匹配"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="type-info">
                <span>甲方字段类型：<code>{{ unmappedField?.field_type }}</code></span>
                <el-icon><Close /></el-icon>
                <span>标准字段类型：<code>{{ selectedFieldInfo.field_type }}</code></span>
              </div>
              <p class="warning-text">类型不匹配可能导致数据转换错误，建议选择类型兼容的字段。</p>
            </template>
          </el-alert>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm"
        :disabled="!selectedFieldKey"
      >
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Bottom, Right, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  unmappedField: any
  standardFields: any[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', fieldKey: string): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const selectedFieldKey = ref('')

// 可用的标准字段（未映射的）
const availableFields = computed(() => {
  return props.standardFields.filter(f => f.mapping_status === 'unmapped')
})

// 选中字段的信息
const selectedFieldInfo = computed(() => {
  return props.standardFields.find(f => f.field_key === selectedFieldKey.value)
})

// 类型兼容性检查
const isTypeCompatible = computed(() => {
  if (!props.unmappedField || !selectedFieldInfo.value) return false
  
  const tenantType = props.unmappedField.field_type
  const standardType = selectedFieldInfo.value.field_type
  
  // 类型兼容规则
  const compatibilityMap: Record<string, string[]> = {
    'String': ['String', 'Text'],
    'Integer': ['Integer', 'Number'],
    'Decimal': ['Decimal', 'Float', 'Double', 'Number'],
    'Date': ['Date'],
    'Datetime': ['Datetime', 'Timestamp'],
    'Boolean': ['Boolean', 'Bool'],
    'Enum': ['Enum']
  }
  
  const compatibleTypes = compatibilityMap[tenantType] || []
  return compatibleTypes.includes(standardType)
})

// 确认
const handleConfirm = () => {
  if (!selectedFieldKey.value) {
    ElMessage.warning('请选择目标字段')
    return
  }
  
  if (!isTypeCompatible.value) {
    ElMessage.warning('字段类型不匹配，建议选择类型兼容的字段')
    return
  }
  
  emit('confirm', selectedFieldKey.value)
}

// 取消
const handleCancel = () => {
  selectedFieldKey.value = ''
  dialogVisible.value = false
}
</script>

<style scoped>
.match-dialog {
  padding: 10px 0;
}

/* 字段信息区域 */
.field-info-section h4,
.target-selection-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #303133;
  font-weight: 600;
}

.required {
  color: #f56c6c;
}

.field-info-box {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  min-width: 80px;
  color: #606266;
  font-weight: 500;
}

.info-row .value {
  color: #303133;
}

.info-row .value.code {
  font-family: 'Courier New', monospace;
  background-color: #fff;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #dcdfe6;
}

/* 目标选择区域 */
.target-selection-section {
  margin-top: 20px;
}

.field-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-option-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-option-main .field-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.field-option-sub {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.field-option-sub .field-key {
  font-family: 'Courier New', monospace;
}

/* 匹配提示 */
.match-tip {
  margin-top: 16px;
}

.type-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.type-info code {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #409eff;
}

.warning-text {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #e6a23c;
}
</style>
