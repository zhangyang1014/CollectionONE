<template>
  <el-dialog
    v-model="visible"
    title="匹配到目标字段"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="match-dialog">
      <!-- 未使用字段信息 -->
      <el-card v-if="unmappedField" class="field-info-card">
        <template #header>
          <span>未使用的甲方字段</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="字段Key">{{ unmappedField.field_key }}</el-descriptions-item>
          <el-descriptions-item label="字段名称">{{ unmappedField.field_name }}</el-descriptions-item>
          <el-descriptions-item label="字段类型">{{ unmappedField.field_type }}</el-descriptions-item>
          <el-descriptions-item label="数据示例" v-if="unmappedField.sample_value">
            {{ unmappedField.sample_value }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-divider>
        <el-icon><Bottom /></el-icon>
        匹配到
      </el-divider>

      <!-- 选择标准字段 -->
      <el-form :model="formData" label-width="120px">
        <el-form-item label="选择标准字段" required>
          <el-select 
            v-model="formData.targetFieldKey"
            placeholder="请选择要匹配的标准字段"
            filterable
            style="width: 100%"
            @change="handleFieldChange"
          >
            <el-option
              v-for="field in standardFields"
              :key="field.field_key"
              :label="`${field.field_name} (${field.field_key})`"
              :value="field.field_key"
            >
              <div class="field-option">
                <span class="field-name">{{ field.field_name }}</span>
                <span class="field-key">{{ field.field_key }}</span>
                <el-tag v-if="field.field_type" size="small" type="info">
                  {{ field.field_type }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 类型不兼容警告 -->
        <el-alert
          v-if="showTypeWarning"
          title="⚠️ 字段类型不匹配"
          type="warning"
          :closable="false"
          show-icon
        >
          <p>甲方字段类型 ({{ unmappedField?.field_type }}) 与目标字段类型 ({{ selectedFieldType }}) 不一致，</p>
          <p>可能导致数据转换错误。是否继续？</p>
        </el-alert>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm"
        :disabled="!formData.targetFieldKey"
        :loading="isSubmitting"
      >
        确认匹配
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Bottom } from '@element-plus/icons-vue'
import { saveFieldConfig } from '@/api/field-mapping'

// Props
const props = defineProps<{
  modelValue: boolean
  tenantId?: number
  unmappedField?: any
  standardFields?: any[]
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
}>()

// 响应式状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isSubmitting = ref(false)
const formData = ref({
  targetFieldKey: ''
})

const selectedFieldType = ref('')
const showTypeWarning = computed(() => {
  if (!formData.value.targetFieldKey || !props.unmappedField) {
    return false
  }
  return selectedFieldType.value && selectedFieldType.value !== props.unmappedField.field_type
})

// 处理字段选择变化
const handleFieldChange = (fieldKey: string) => {
  const selectedField = props.standardFields?.find(f => f.field_key === fieldKey)
  selectedFieldType.value = selectedField?.field_type || ''
}

// 确认匹配
const handleConfirm = async () => {
  if (!props.tenantId || !props.unmappedField || !formData.value.targetFieldKey) {
    return
  }

  isSubmitting.value = true
  try {
    await saveFieldConfig(props.tenantId, {
      field_key: formData.value.targetFieldKey,
      tenant_field_key: props.unmappedField.field_key,
      mapping_status: 'manual_mapped'
    })
    
    ElMessage.success('映射成功')
    visible.value = false
    emit('confirm')
    resetForm()
  } catch (error) {
    console.error('映射失败:', error)
    ElMessage.error('映射失败')
  } finally {
    isSubmitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value.targetFieldKey = ''
  selectedFieldType.value = ''
}

// 监听对话框关闭
watch(visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>

<style scoped>
.match-dialog {
  padding: 0 4px;
}

.field-info-card {
  margin-bottom: 20px;
}

.field-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-name {
  font-weight: bold;
}

.field-key {
  color: #909399;
  font-size: 12px;
}
</style>
