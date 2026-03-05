<template>
  <el-dialog
    v-model="visible"
    title="分配记录"
    width="760px"
    @close="handleClose"
  >
    <!-- 号码信息 -->
    <div v-if="phone" class="phone-info">
      <el-tag type="info">{{ phone.instantId }}</el-tag>
      <span v-if="phone.phone" class="phone-number">{{ phone.phone }}</span>
    </div>

    <el-table :data="logs" v-loading="loading" empty-text="暂无分配记录">
      <el-table-column prop="assignedAt" label="分配时间" width="170" />
      <el-table-column label="分配催员" width="120">
        <template #default="{ row }">
          <span class="collector-name">{{ row.collectorName }}</span>
          <span class="team-name">{{ row.teamName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="使用状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="!row.reclaimedAt" type="success" size="small">使用中</el-tag>
          <el-tag v-else type="info" size="small">已回收</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回收时间" width="170">
        <template #default="{ row }">
          <span v-if="row.reclaimedAt">{{ row.reclaimedAt }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="本次使用时长" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.reclaimedAt && row.usageHours > 0">{{ formatHours(row.usageHours) }}</span>
          <span v-else-if="!row.reclaimedAt" class="in-use">进行中</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="90" />
      <el-table-column prop="remark" label="备注" min-width="120">
        <template #default="{ row }">
          <span v-if="row.remark">{{ row.remark }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { WaPhone, PhoneAssignLog } from '@/types/wa-management'
import { getPhoneAssignLogs } from '@/api/wa-management'

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
const logs = ref<PhoneAssignLog[]>([])

const handleClose = () => {
  visible.value = false
}

const formatHours = (hours: number): string => {
  if (hours < 24) return `${hours}小时`
  const days = Math.floor(hours / 24)
  const h = hours % 24
  return h > 0 ? `${days}天${h}小时` : `${days}天`
}

const loadLogs = async () => {
  if (!props.phone) return
  loading.value = true
  try {
    logs.value = await getPhoneAssignLogs(props.phone.id)
  } finally {
    loading.value = false
  }
}

watch(visible, (val) => {
  if (val) loadLogs()
  else logs.value = []
})
</script>

<style scoped>
.phone-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.phone-number {
  color: #606266;
  font-size: 14px;
}

.collector-name {
  display: block;
  font-weight: 500;
  color: #303133;
}

.team-name {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.text-muted {
  color: #c0c4cc;
}

.in-use {
  color: #67c23a;
  font-size: 12px;
}
</style>
