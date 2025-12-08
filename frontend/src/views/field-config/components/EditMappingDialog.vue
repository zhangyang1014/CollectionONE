<template>
  <el-dialog
    v-model="visible"
    :title="`编辑字段映射 - ${standardField?.field_name}`"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="edit-mapping-dialog">
      <!-- 标准字段信息 -->
      <el-card class="field-info-card">
        <template #header>
          <span>标准字段信息</span>
        </template>
        <el-descriptions :column="2" border v-if="standardField">
          <el-descriptions-item label="字段名称">{{ standardField.field_name }}</el-descriptions-item>
          <el-descriptions-item label="字段Key">{{ standardField.field_key }}</el-descriptions-item>
          <el-descriptions-item label="字段类型">{{ standardField.field_type }}</el-descriptions-item>
          <el-descriptions-item label="是否必填">
            <el-tag :type="standardField.is_required ? 'danger' : 'info'">
              {{ standardField.is_required ? '必填' : '选填' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-divider>
        <el-icon><Connection /></el-icon>
        映射到
      </el-divider>

      <!-- 选择甲方字段 -->
      <el-form :model="formData" label-width="120px">
        <el-form-item label="选择甲方字段" required>
          <el-select 
            v-model="formData.tenantFieldKey"
            placeholder="请选择甲方字段进行映射"
            filterable
            clearable
            style="width: 100%"
            @change="handleFieldChange"
          >
            <el-option
              v-for="field in availableTenantFields"
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
          <div class="form-tip">从甲方上传的字段JSON中选择要映射的字段</div>
        </el-form-item>

        <!-- 已选择的甲方字段信息 -->
        <el-card v-if="selectedTenantField" class="selected-field-card">
          <template #header>
            <span>已选择的甲方字段</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="字段名称">{{ selectedTenantField.field_name }}</el-descriptions-item>
            <el-descriptions-item label="字段Key">{{ selectedTenantField.field_key }}</el-descriptions-item>
            <el-descriptions-item label="字段类型">{{ selectedTenantField.field_type }}</el-descriptions-item>
            <el-descriptions-item label="数据示例" v-if="selectedTenantField.sample_value">
              {{ selectedTenantField.sample_value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 类型不兼容警告 -->
        <el-alert
          v-if="showTypeWarning"
          title="⚠️ 字段类型不匹配"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 16px;"
        >
          <p>标准字段类型 ({{ standardField?.field_type }}) 与甲方字段类型 ({{ selectedTenantField?.field_type }}) 不一致。</p>
          <p>这可能导致数据转换错误，请确认是否继续映射。</p>
        </el-alert>

        <!-- 枚举类型提示 -->
        <el-alert
          v-if="isEnumType"
          title="📋 枚举类型字段"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 16px;"
        >
          <p>此字段为枚举类型，映射后需要进一步配置枚举值映射关系。</p>
        </el-alert>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm"
        :loading="isSubmitting"
      >
        确认映射
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection } from '@element-plus/icons-vue'
import { saveFieldConfig } from '@/api/field-mapping'
import { getTenantFieldsJson } from '@/api/field-mapping'

// Props
const props = defineProps<{
  modelValue: boolean
  tenantId?: number
  standardField?: any
  currentMapping?: any
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'enum-mapping-needed', data: any): void
}>()

// 响应式状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isSubmitting = ref(false)
const availableTenantFields = ref<any[]>([])
const formData = ref({
  tenantFieldKey: ''
})

const selectedTenantField = computed(() => {
  if (!formData.value.tenantFieldKey) return null
  return availableTenantFields.value.find(f => f.field_key === formData.value.tenantFieldKey)
})

const showTypeWarning = computed(() => {
  if (!selectedTenantField.value || !props.standardField) return false
  return selectedTenantField.value.field_type !== props.standardField.field_type
})

const isEnumType = computed(() => {
  return props.standardField?.field_type === 'enum' || selectedTenantField.value?.field_type === 'enum'
})

// 加载可用的甲方字段
const loadTenantFields = async () => {
  if (!props.tenantId) return

  try {
    const res = await getTenantFieldsJson(props.tenantId)
    const data = res?.data
    if (data && data.fields) {
      availableTenantFields.value = data.fields
    }
  } catch (error) {
    console.error('加载甲方字段失败:', error)
  }
}

// 处理字段选择变化
const handleFieldChange = (fieldKey: string) => {
  console.log('选择字段:', fieldKey)
}

// 确认映射
const handleConfirm = async () => {
  if (!props.tenantId || !props.standardField || !formData.value.tenantFieldKey) {
    ElMessage.warning('请选择要映射的甲方字段')
    return
  }

  isSubmitting.value = true
  try {
    await saveFieldConfig(props.tenantId, {
      field_key: props.standardField.field_key,
      tenant_field_key: formData.value.tenantFieldKey,
      mapping_status: 'manual_mapped'
    })
    
    ElMessage.success('映射成功')
    
    // 如果是枚举类型，提示需要配置枚举值映射
    if (isEnumType.value) {
      emit('enum-mapping-needed', {
        standardField: props.standardField,
        tenantField: selectedTenantField.value
      })
    }
    
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
  formData.value.tenantFieldKey = ''
}

// 监听对话框打开
watch(visible, (newVal) => {
  if (newVal) {
    loadTenantFields()
    // 如果有当前映射，设置默认值
    if (props.currentMapping?.tenant_field_key) {
      formData.value.tenantFieldKey = props.currentMapping.tenant_field_key
    }
  } else {
    resetForm()
  }
})
</script>

<style scoped>
.edit-mapping-dialog {
  padding: 0 4px;
}

.field-info-card {
  margin-bottom: 20px;
}

.selected-field-card {
  margin-top: 16px;
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
