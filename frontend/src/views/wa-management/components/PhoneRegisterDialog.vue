<template>
  <el-dialog
    v-model="visible"
    title="云号码登记"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      <template #title>
        将云盾BD同学购买的海外云手机号兑换码（instant_id）登记到系统中，登记后状态为"待绑定IP"。
      </template>
    </el-alert>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="购买渠道" prop="purchaseChannelId">
        <el-select v-model="form.purchaseChannelId" placeholder="请选择购买渠道" style="width: 100%">
          <el-option
            v-for="ch in channels"
            :key="ch.id"
            :label="ch.channelName"
            :value="ch.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="导入方式">
        <el-radio-group v-model="importMode">
          <el-radio-button value="file">文件上传</el-radio-button>
          <el-radio-button value="text">手动输入</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="importMode === 'file'" label="上传文件" prop="file">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".txt,.xlsx,.xls,.csv"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
        >
          <template #trigger>
            <el-button type="primary">选择文件</el-button>
          </template>
          <template #tip>
            <div class="el-upload__tip">支持 .txt / .xlsx / .csv 文件，每行一个 instant_id</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item v-if="importMode === 'text'" label="Instant IDs" prop="textInput">
        <el-input
          v-model="form.textInput"
          type="textarea"
          :rows="6"
          placeholder="每行输入一个 instant_id，例如：&#10;INST-20260301-001&#10;INST-20260301-002&#10;INST-20260301-003"
        />
      </el-form-item>

      <el-form-item v-if="parsedIds.length > 0" label="解析结果">
        <el-tag type="success" style="margin-right: 8px">共 {{ parsedIds.length }} 个</el-tag>
        <div class="parsed-ids-preview">
          <el-tag
            v-for="(id, idx) in parsedIds.slice(0, 10)"
            :key="idx"
            size="small"
            style="margin: 2px 4px"
          >
            {{ id }}
          </el-tag>
          <el-tag v-if="parsedIds.length > 10" size="small" type="info">
            ...还有 {{ parsedIds.length - 10 }} 个
          </el-tag>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="parsedIds.length === 0" @click="handleSubmit">
        登记 ({{ parsedIds.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { WaPurchaseChannel } from '@/types/wa-management'
import { registerPhones, getPurchaseChannels } from '@/api/wa-management'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const importMode = ref<'file' | 'text'>('text')
const channels = ref<WaPurchaseChannel[]>([])

const form = ref({
  purchaseChannelId: null as number | null,
  textInput: '',
  file: null as File | null,
})

const rules = {
  purchaseChannelId: [{ required: true, message: '请选择购买渠道', trigger: 'change' }],
}

const parsedIds = computed(() => {
  if (importMode.value === 'text' && form.value.textInput) {
    return form.value.textInput
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0)
  }
  if (importMode.value === 'file' && form.value.file) {
    return fileContent.value
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0)
  }
  return []
})

const fileContent = ref('')

const handleFileChange = (uploadFile: any) => {
  const file = uploadFile.raw as File
  form.value.file = file
  const reader = new FileReader()
  reader.onload = (e) => {
    fileContent.value = (e.target?.result as string) || ''
  }
  reader.readAsText(file)
}

const handleFileRemove = () => {
  form.value.file = null
  fileContent.value = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  if (parsedIds.value.length === 0) {
    ElMessage.warning('请输入或上传至少一个 instant_id')
    return
  }

  submitting.value = true
  try {
    const res = await registerPhones({
      instantIds: parsedIds.value,
      purchaseChannelId: form.value.purchaseChannelId!,
    })
    ElMessage.success(`成功登记 ${res.count} 个云号码`)
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('登记失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  form.value = { purchaseChannelId: null, textInput: '', file: null }
  fileContent.value = ''
}

const loadChannels = async () => {
  channels.value = await getPurchaseChannels()
}

watch(visible, (val) => {
  if (val) loadChannels()
})
</script>

<style scoped>
.parsed-ids-preview {
  display: flex;
  flex-wrap: wrap;
  max-height: 120px;
  overflow-y: auto;
}
</style>
