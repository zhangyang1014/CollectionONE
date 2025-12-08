<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑拓展字段' : '添加拓展字段'"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <el-form-item label="字段别名" prop="field_alias">
        <el-input 
          v-model="formData.field_alias" 
          placeholder="请输入字段别名，用于系统显示"
        />
      </el-form-item>

      <el-form-item label="甲方字段Key" prop="tenant_field_key">
        <el-input 
          v-model="formData.tenant_field_key" 
          placeholder="请输入甲方字段Key"
        />
      </el-form-item>

      <el-form-item label="甲方字段名称" prop="tenant_field_name">
        <el-input 
          v-model="formData.tenant_field_name" 
          placeholder="请输入甲方字段名称"
        />
      </el-form-item>

      <el-form-item label="字段类型" prop="field_type">
        <el-select v-model="formData.field_type" placeholder="请选择字段类型">
          <el-option label="字符串" value="string" />
          <el-option label="数字" value="number" />
          <el-option label="日期" value="date" />
          <el-option label="布尔值" value="boolean" />
          <el-option label="枚举" value="enum" />
        </el-select>
      </el-form-item>

      <el-form-item label="字段分组" prop="field_group_id">
        <el-select v-model="formData.field_group_id" placeholder="请选择字段分组" clearable>
          <el-option label="基本信息" :value="1" />
          <el-option label="联系信息" :value="2" />
          <el-option label="账务信息" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="是否必填" prop="is_required">
        <el-switch v-model="formData.is_required" />
      </el-form-item>

      <el-form-item label="隐私标签" prop="privacy_label">
        <el-select v-model="formData.privacy_label" placeholder="请选择隐私标签" clearable>
          <el-option label="普通" value="normal" />
          <el-option label="敏感" value="sensitive" />
          <el-option label="高度敏感" value="highly_sensitive" />
        </el-select>
        <div class="form-tip">高度敏感信息将被加密存储和脱敏显示</div>
      </el-form-item>

      <el-form-item label="字段说明" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入字段说明（选填）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="isSubmitting">
        确认
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createExtendedField, updateExtendedField } from '@/api/field-mapping'

// Props
const props = defineProps<{
  modelValue: boolean
  tenantId?: number
  editData?: any
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

const isEdit = computed(() => !!props.editData?.id)
const formRef = ref<FormInstance>()
const isSubmitting = ref(false)

// 表单数据
const formData = ref({
  field_alias: '',
  tenant_field_key: '',
  tenant_field_name: '',
  field_type: 'string',
  field_group_id: undefined as number | undefined,
  is_required: false,
  privacy_label: 'normal',
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  field_alias: [
    { required: true, message: '请输入字段别名', trigger: 'blur' }
  ],
  tenant_field_key: [
    { required: true, message: '请输入甲方字段Key', trigger: 'blur' }
  ],
  tenant_field_name: [
    { required: true, message: '请输入甲方字段名称', trigger: 'blur' }
  ],
  field_type: [
    { required: true, message: '请选择字段类型', trigger: 'change' }
  ]
}

// 监听编辑数据变化
watch(() => props.editData, (newData) => {
  if (newData) {
    formData.value = {
      field_alias: newData.field_alias || '',
      tenant_field_key: newData.tenant_field_key || '',
      tenant_field_name: newData.tenant_field_name || '',
      field_type: newData.field_type || 'string',
      field_group_id: newData.field_group_id,
      is_required: newData.is_required || false,
      privacy_label: newData.privacy_label || 'normal',
      description: newData.description || ''
    }
  }
}, { immediate: true, deep: true })

// 重置表单
const resetForm = () => {
  formData.value = {
    field_alias: '',
    tenant_field_key: '',
    tenant_field_name: '',
    field_type: 'string',
    field_group_id: undefined,
    is_required: false,
    privacy_label: 'normal',
    description: ''
  }
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  if (!props.tenantId) {
    return
  }

  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  isSubmitting.value = true
  try {
    if (isEdit.value) {
      await updateExtendedField(props.tenantId, props.editData.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createExtendedField(props.tenantId, formData.value)
      ElMessage.success('添加成功')
    }
    
    visible.value = false
    emit('confirm')
    resetForm()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    isSubmitting.value = false
  }
}

// 监听对话框关闭
watch(visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
