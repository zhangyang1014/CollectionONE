<template>
  <el-dialog
    v-model="visible"
    title="IP变更记录"
    width="700px"
    @close="handleClose"
  >
    <div v-if="phone" style="margin-bottom: 16px">
      <el-tag>{{ phone.instantId }}</el-tag>
      <span v-if="phone.phone" style="margin-left: 8px; color: #606266">{{ phone.phone }}</span>
    </div>

    <el-table :data="logs" v-loading="loading" empty-text="暂无变更记录">
      <el-table-column prop="createdAt" label="变更时间" width="170" />
      <el-table-column prop="oldIpAddress" label="原IP" width="140" />
      <el-table-column label="" width="40">
        <template #default>
          <el-icon><Right /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="newIpAddress" label="新IP" width="140" />
      <el-table-column prop="changeReason" label="变更原因" min-width="150" />
      <el-table-column prop="operator" label="操作人" width="100" />
    </el-table>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Right } from '@element-plus/icons-vue'
import type { WaPhone, IpChangeLog } from '@/types/wa-management'
import { getIpChangeLogs } from '@/api/wa-management'

const props = defineProps<{
  modelValue: boolean
  phone: WaPhone | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const logs = ref<IpChangeLog[]>([])

const handleClose = () => {
  visible.value = false
}

const loadLogs = async () => {
  if (!props.phone) return
  loading.value = true
  try {
    logs.value = await getIpChangeLogs(props.phone.id)
  } finally {
    loading.value = false
  }
}

watch(visible, (val) => {
  if (val) loadLogs()
})
</script>
