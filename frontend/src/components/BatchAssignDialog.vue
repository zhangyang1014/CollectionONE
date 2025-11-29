<template>
  <el-dialog
    v-model="visible"
    title="批量分案"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="batch-assign-container">
      <!-- 已选案件数量提示 -->
      <el-alert
        :title="`已选择 ${selectedCaseIds.length} 个案件`"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />

      <!-- 筛选器 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="机构">
          <el-select
            v-model="filters.agencyId"
            placeholder="请选择机构"
            clearable
            @change="handleAgencyChange"
            style="width: 180px"
          >
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name || agency.agencyName"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="小组">
          <el-select
            v-model="filters.teamId"
            placeholder="请选择小组"
            clearable
            @change="handleTeamChange"
            style="width: 180px"
          >
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.team_name || team.teamName"
              :value="team.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="队列属性">
          <el-select
            v-model="filters.queueId"
            placeholder="请选择队列"
            clearable
            @change="loadCollectors"
            style="width: 180px"
          >
            <el-option
              v-for="queue in queues"
              :key="queue.id"
              :label="queue.queue_name || queue.queueName"
              :value="queue.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input
            v-model="filters.searchKeyword"
            placeholder="催员名/催员ID"
            clearable
            @input="loadCollectors"
            style="width: 200px"
          />
        </el-form-item>
      </el-form>

      <!-- 催员列表 -->
      <el-table
        :data="collectors"
        @selection-change="handleSelectionChange"
        v-loading="loading"
        max-height="400"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="collectorName" label="催员名称" width="120" />
        <el-table-column prop="collectorCode" label="催员ID" width="100" />
        <el-table-column prop="agencyName" label="所属机构" width="120" />
        <el-table-column prop="teamName" label="所属小组" width="120" />
        <el-table-column prop="queueName" label="队列属性" width="100" />
        <el-table-column prop="currentCaseCount" label="今日持案量" width="100" align="right" />
      </el-table>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleAssign" :disabled="selectedCollectors.length === 0">
          平均分案
        </el-button>
      </span>
    </template>

    <!-- 队列限制确认弹窗 -->
    <QueueLimitConfirmDialog
      v-model="showQueueLimitDialog"
      :unmatched-items="unmatchedItems"
      @confirm="handleConfirmAssign"
      @cancel="handleCancelAssign"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getTenantAgencies, getAgencyTeams } from '@/api/organization'
import { getTenantQueues } from '@/api/queue'
import { useTenantStore } from '@/stores/tenant'
import QueueLimitConfirmDialog from './QueueLimitConfirmDialog.vue'

const props = defineProps<{
  modelValue: boolean
  selectedCaseIds: number[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

const visible = ref(false)
const loading = ref(false)
const collectors = ref<any[]>([])
const selectedCollectors = ref<any[]>([])
const showQueueLimitDialog = ref(false)
const unmatchedItems = ref<any[]>([])

// 筛选条件
const filters = reactive({
  agencyId: null as number | null,
  teamId: null as number | null,
  queueId: null as number | null,
  searchKeyword: ''
})

// 机构、小组、队列数据
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const queues = ref<any[]>([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadAgencies()
    loadQueues()
    loadCollectors()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

// 加载机构列表
const loadAgencies = async () => {
  if (!currentTenantId.value) return
  
  try {
    const response = await getTenantAgencies(currentTenantId.value)
    agencies.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载机构列表失败:', error)
  }
}

// 加载队列列表
const loadQueues = async () => {
  if (!currentTenantId.value) return
  
  try {
    const response = await getTenantQueues(currentTenantId.value)
    queues.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载队列列表失败:', error)
  }
}

// 机构变化时，重新加载小组
const handleAgencyChange = async () => {
  filters.teamId = null
  filters.queueId = null
  teams.value = []
  
  if (filters.agencyId) {
    try {
      const response = await getAgencyTeams(filters.agencyId)
      teams.value = Array.isArray(response) ? response : (response.data || [])
    } catch (error) {
      console.error('加载小组列表失败:', error)
    }
  }
  
  loadCollectors()
}

// 小组变化时，重新加载队列（从小组信息中获取队列）
const handleTeamChange = () => {
  filters.queueId = null
  loadCollectors()
}

// 加载催员列表
const loadCollectors = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/api/v1/cases/collectors-for-assign',
      method: 'get',
      params: {
        agency_id: filters.agencyId,
        team_id: filters.teamId,
        queue_id: filters.queueId,
        search_keyword: filters.searchKeyword
      }
    })
    
    collectors.value = response || []
  } catch (error) {
    console.error('获取催员列表失败:', error)
    ElMessage.error('获取催员列表失败')
    collectors.value = []
  } finally {
    loading.value = false
  }
}

// 催员选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedCollectors.value = selection
}

// 点击平均分案按钮
const handleAssign = async () => {
  if (selectedCollectors.value.length === 0) {
    ElMessage.warning('请至少选择一个催员')
    return
  }

  // 先检查队列限制
  try {
    const response = await request({
      url: '/api/v1/cases/check-queue-limit',
      method: 'post',
      data: {
        caseIds: props.selectedCaseIds,
        collectorIds: selectedCollectors.value.map(c => c.id)
      }
    })

    // request.ts已经提取了data字段，所以response就是data
    if (response && response.hasLimit) {
      // 存在队列限制，显示确认弹窗
      unmatchedItems.value = response.unmatchedItems || []
      showQueueLimitDialog.value = true
    } else {
      // 没有队列限制，直接执行分配
      await executeAssign(false)
    }
  } catch (error) {
    console.error('检查队列限制失败:', error)
    ElMessage.error('检查队列限制失败')
  }
}

// 确认忽略队列限制
const handleConfirmAssign = async () => {
  showQueueLimitDialog.value = false
  await executeAssign(true)
}

// 取消分配
const handleCancelAssign = () => {
  showQueueLimitDialog.value = false
}

// 执行分配
const executeAssign = async (ignoreQueueLimit: boolean) => {
  loading.value = true
  try {
    const response = await request({
      url: '/api/v1/cases/batch-assign',
      method: 'post',
      data: {
        caseIds: props.selectedCaseIds,
        collectorIds: selectedCollectors.value.map(c => c.id),
        ignoreQueueLimit
      }
    })

    // request.ts已经提取了data字段，所以response就是data
    const result = response
    if (result && result.successCount > 0) {
      ElMessage.success(`成功分配 ${result.successCount} 个案件给 ${selectedCollectors.value.length} 个催员`)
      emit('success')
      handleClose()
    } else {
      ElMessage.error('分配失败')
    }
  } catch (error) {
    console.error('批量分配失败:', error)
    ElMessage.error('批量分配失败')
  } finally {
    loading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  selectedCollectors.value = []
  filters.agencyId = null
  filters.teamId = null
  filters.queueId = null
  filters.searchKeyword = ''
}
</script>

<style scoped>
.batch-assign-container {
  padding: 0;
}

.filter-form {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

