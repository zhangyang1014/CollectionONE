<template>
  <el-dialog
    v-model="visible"
    title="号码分配"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="phones.length > 0" style="margin-bottom: 16px">
      <el-tag v-for="p in phones.slice(0, 5)" :key="p.id" style="margin: 2px 4px">
        {{ p.instantId }}
      </el-tag>
      <el-tag v-if="phones.length > 5" type="info">...共 {{ phones.length }} 个</el-tag>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="选择催员" prop="collectorId">
        <el-select
          v-model="form.collectorId"
          filterable
          placeholder="搜索催员姓名"
          style="width: 100%"
          @change="handleCollectorChange"
        >
          <el-option
            v-for="c in collectors"
            :key="c.id"
            :label="`${c.name} (${c.team}) - 业绩: ${c.performanceScore}`"
            :value="c.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定分配
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { WaPhone } from '@/types/wa-management'
import { assignPhone, batchAssignPhones, getCollectorsForAssign } from '@/api/wa-management'
import type { SimpleCollector } from '@/api/wa-management'

const props = defineProps<{
  modelValue: boolean
  phones: WaPhone[]
}>()

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
const collectors = ref<SimpleCollector[]>([])
const selectedCollector = ref<SimpleCollector | null>(null)

const form = ref({ collectorId: null as number | null })
const rules = {
  collectorId: [{ required: true, message: '请选择催员', trigger: 'change' }],
}

const handleCollectorChange = (id: number) => {
  selectedCollector.value = collectors.value.find(c => c.id === id) || null
}

const handleSubmit = async () => {
  if (!formRef.value || !selectedCollector.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    if (props.phones.length === 1) {
      await assignPhone(props.phones[0].id, selectedCollector.value.id, selectedCollector.value.name)
    } else {
      await batchAssignPhones(
        props.phones.map(p => p.id),
        selectedCollector.value.id,
        selectedCollector.value.name
      )
    }
    ElMessage.success(`成功分配 ${props.phones.length} 个号码`)
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('分配失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  form.value = { collectorId: null }
  selectedCollector.value = null
}

watch(visible, async (val) => {
  if (val) {
    collectors.value = await getCollectorsForAssign()
  }
})
</script>
