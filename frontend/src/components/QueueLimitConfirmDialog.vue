<template>
  <el-dialog
    v-model="visible"
    title="队列限制确认"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="queue-limit-confirm">
      <el-alert
        title="检测到队列不匹配"
        type="warning"
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #default>
          <div>案件队列不属于催员所在小组的范围队列，是否忽略队列限制继续分配?</div>
        </template>
      </el-alert>

      <!-- 不匹配详情 -->
      <div v-if="unmatchedItems && unmatchedItems.length > 0" class="unmatched-details">
        <div class="details-title">不匹配详情（前5条）：</div>
        <el-table
          :data="unmatchedItems.slice(0, 5)"
          max-height="300"
          style="width: 100%"
        >
          <el-table-column prop="caseCode" label="案件编号" width="120" />
          <el-table-column prop="caseQueueName" label="案件队列" width="100" />
          <el-table-column prop="collectorName" label="催员" width="100" />
          <el-table-column prop="collectorTeamQueueName" label="小组队列" width="100" />
        </el-table>
        <div v-if="unmatchedItems.length > 5" class="more-info">
          ...还有 {{ unmatchedItems.length - 5 }} 条不匹配记录
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">否，返回修改</el-button>
        <el-button type="warning" @click="handleConfirm">是，忽略限制继续分配</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  unmatchedItems: any[]
}>()

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

const handleConfirm = () => {
  emit('confirm')
  visible.value = false
}

const handleCancel = () => {
  emit('cancel')
  visible.value = false
}
</script>

<style scoped>
.queue-limit-confirm {
  padding: 0;
}

.unmatched-details {
  margin-top: 16px;
}

.details-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #606266;
}

.more-info {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>


