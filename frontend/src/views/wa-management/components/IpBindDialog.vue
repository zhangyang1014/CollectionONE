<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 单号码模式：显示当前号码详情 -->
    <div v-if="isSingleMode && currentPhone" class="phone-info">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="Instant ID">{{ currentPhone.instantId }}</el-descriptions-item>
        <el-descriptions-item label="当前IP">{{ currentPhone.ipAddress || '未绑定' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 批量模式：显示选中号码列表 -->
    <div v-if="!isSingleMode && phones.length > 0" class="phone-info">
      <div style="margin-bottom: 8px; font-size: 13px; color: #606266">
        已选择 <strong>{{ phones.length }}</strong> 个号码，将统一绑定到以下IP：
      </div>
      <div class="batch-phones-tags">
        <el-tag v-for="p in phones.slice(0, 5)" :key="p.id" size="small" style="margin: 2px 4px">
          {{ p.instantId }}
        </el-tag>
        <el-tag v-if="phones.length > 5" type="info" size="small">...共 {{ phones.length }} 个</el-tag>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="margin-top: 16px">
      <el-form-item label="选择IP" prop="ipId">
        <el-select v-model="form.ipId" placeholder="请选择可用的IP地址" style="width: 100%">
          <el-option
            v-for="ip in availableIps"
            :key="ip.id"
            :label="`${ip.ipAddress}:${ip.port} (已用 ${ip.linkedPhoneCount} 个)`"
            :value="ip.id"
          >
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ ip.ipAddress }}:{{ ip.port }}</span>
              <span style="font-size: 12px; color: #909399">
                已绑 {{ ip.linkedPhoneCount }} | 健康 {{ ip.healthScore }}%
              </span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定绑定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { WaPhone, WaIp } from '@/types/wa-management'
import { bindPhoneIp, batchBindIp, getIpList } from '@/api/wa-management'

const props = defineProps<{
  modelValue: boolean
  phone: WaPhone | null
  phones?: WaPhone[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

/** 是否为单号码模式（phones 为空或仅1个时降级为单号码模式） */
const isSingleMode = computed(() => !props.phones || props.phones.length <= 1)
const currentPhone = computed(() => props.phone)
const phones = computed(() => props.phones || [])

const dialogTitle = computed(() => {
  if (!isSingleMode.value) return `批量绑定IP（${phones.value.length} 个号码）`
  return currentPhone.value?.ipId ? '换绑IP' : '绑定IP'
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const availableIps = ref<WaIp[]>([])

const form = ref({ ipId: null as number | null })
const rules = {
  ipId: [{ required: true, message: '请选择IP地址', trigger: 'change' }],
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    if (isSingleMode.value && currentPhone.value) {
      await bindPhoneIp(currentPhone.value.id, form.value.ipId!)
      ElMessage.success('IP绑定成功')
    } else {
      const res = await batchBindIp(phones.value.map(p => p.id), form.value.ipId!)
      ElMessage.success(`已成功绑定 ${res.count} 个号码`)
    }
    emit('success')
    handleClose()
  } catch {
    ElMessage.error('绑定失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  form.value = { ipId: null }
}

const loadIps = async () => {
  const list = await getIpList()
  availableIps.value = list.filter(ip => ip.status === 'ACTIVE')
}

watch(visible, (val) => {
  if (val) loadIps()
})
</script>

<style scoped>
.phone-info {
  margin-bottom: 10px;
}

.batch-phones-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}
</style>
