<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑拓展字段' : '添加拓展字段'"
    width="650px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
    >
      <el-form-item label="扩展字段别名" prop="field_alias" required>
        <el-input
          v-model="formData.field_alias"
          placeholder="如：customer_level"
          maxlength="100"
          show-word-limit
        >
          <template #append>
            <el-tooltip content="系统内部使用，只能包含小写字母、数字、下划线" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="甲方原始字段" prop="tenant_field_key" required>
        <el-input
          v-model="formData.tenant_field_key"
          placeholder="如：CUSTOMER_LEVEL"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="字段名称" prop="tenant_field_name" required>
        <el-input
          v-model="formData.tenant_field_name"
          placeholder="如：客户等级"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="字段类型" prop="field_type" required>
        <el-select v-model="formData.field_type" style="width: 100%">
          <el-option label="文本" value="String" />
          <el-option label="整数" value="Integer" />
          <el-option label="小数" value="Decimal" />
          <el-option label="日期" value="Date" />
          <el-option label="日期时间" value="Datetime" />
          <el-option label="布尔" value="Boolean" />
        </el-select>
      </el-form-item>

      <el-form-item label="所属分组" prop="field_group_id">
        <el-select
          v-model="formData.field_group_id"
          placeholder="选择字段分组（可选）"
          clearable
          style="width: 100%"
        >
          <el-option label="基本信息" :value="1" />
          <el-option label="金额信息" :value="2" />
          <el-option label="状态信息" :value="3" />
          <el-option label="产品信息" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="隐私标签" prop="privacy_label" required>
        <el-radio-group v-model="formData.privacy_label">
          <el-radio-button value="PII">
            <el-icon><Lock /></el-icon>
            PII（个人身份信息）
          </el-radio-button>
          <el-radio-button value="敏感">
            <el-icon><Warning /></el-icon>
            敏感
          </el-radio-button>
          <el-radio-button value="公开">
            <el-icon><Unlock /></el-icon>
            公开
          </el-radio-button>
        </el-radio-group>
        <div class="privacy-tip">
          <el-alert
            :title="getPrivacyTip(formData.privacy_label)"
            :type="getPrivacyAlertType(formData.privacy_label)"
            :closable="false"
            show-icon
          />
        </div>
      </el-form-item>

      <el-form-item label="是否必填">
        <el-switch v-model="formData.is_required" />
        <span class="form-tip">必填字段在案件列表中不能为空</span>
      </el-form-item>

      <el-form-item label="字段说明" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="说明该字段的用途和含义"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { QuestionFilled, Lock, Warning, Unlock } from '@element-plus/icons-vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  fieldData: any
  isEdit: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', data: any): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref<FormInstance>()
const formData = ref({
  field_alias: '',
  tenant_field_key: '',
  tenant_field_name: '',
  field_type: 'String',
  field_group_id: null,
  privacy_label: '公开',
  is_required: false,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  field_alias: [
    { required: true, message: '请输入扩展字段别名', trigger: 'blur' },
    { 
      pattern: /^[a-z][a-z0-9_]*$/,
      message: '只能包含小写字母、数字和下划线，且必须以字母开头',
      trigger: 'blur'
    },
    { min: 1, max: 100, message: '长度在1到100个字符', trigger: 'blur' }
  ],
  tenant_field_key: [
    { required: true, message: '请输入甲方原始字段', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在1到100个字符', trigger: 'blur' }
  ],
  tenant_field_name: [
    { required: true, message: '请输入字段名称', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在1到200个字符', trigger: 'blur' }
  ],
  field_type: [
    { required: true, message: '请选择字段类型', trigger: 'change' }
  ],
  privacy_label: [
    { required: true, message: '请选择隐私标签', trigger: 'change' }
  ]
}

// 监听props变化，更新表单数据
watch(() => props.fieldData, (newData) => {
  if (newData) {
    formData.value = { ...newData }
  }
}, { immediate: true, deep: true })

// 获取隐私提示
const getPrivacyTip = (label: string) => {
  const tips: Record<string, string> = {
    'PII': 'PII字段将被加密存储，并在展示时进行脱敏处理（如手机号、身份证号）',
    '敏感': '敏感字段需要特定权限才能查看（如收入、地址）',
    '公开': '公开字段可以正常展示，无需特殊处理'
  }
  return tips[label] || ''
}

// 获取隐私提示类型
const getPrivacyAlertType = (label: string): any => {
  const types: Record<string, any> = {
    'PII': 'error',
    '敏感': 'warning',
    '公开': 'success'
  }
  return types[label] || 'info'
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    emit('save', formData.value)
  } catch (error) {
    ElMessage.warning('请填写完整信息')
  }
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.privacy-tip {
  margin-top: 8px;
}

.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

:deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
