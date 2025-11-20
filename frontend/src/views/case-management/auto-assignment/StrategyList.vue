<template>
  <div class="strategy-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分案策略列表</span>
          <el-space>
            <el-select v-model="selectedQueue" placeholder="选择队列" style="width: 150px" @change="loadStrategies">
              <el-option
                v-for="queue in queues"
                :key="queue.id"
                :label="queue.queue_name"
                :value="queue.id"
              />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="handleCreate">新建策略</el-button>
          </el-space>
        </div>
      </template>

      <el-alert
        title="提示：同队列内策略按从上到下顺序执行，可拖拽调整顺序。仅「开启」且达到「启动时间」的策略会参与执行。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 策略列表 -->
      <el-table
        :data="strategies"
        border
        style="width: 100%"
        row-key="id"
        @row-click="handleRowClick"
      >
        <el-table-column label="执行顺序" width="100" align="center">
          <template #default="{ row }">
            <div class="drag-handle">
              <el-icon><Rank /></el-icon>
              <span>{{ row.sort_order }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="strategy_name" label="策略名称" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" underline="hover" @click.stop="handleViewDetail(row)">
              {{ row.strategy_name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.is_enabled ? 'success' : 'info'">
              {{ row.is_enabled ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="target_level" label="分配目标" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.target_level }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="target_names" label="目标机构/小组" min-width="180">
          <template #default="{ row }">
            <el-text truncated>{{ row.target_names }}</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="start_time" label="启动时间" width="180" />

        <el-table-column label="最近执行" width="150" align="center">
          <template #default="{ row }">
            <div v-if="row.last_execution">
              <el-tag :type="getExecutionStatusType(row.last_execution.status)" size="small">
                {{ getExecutionStatusText(row.last_execution.status) }}
              </el-tag>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                {{ row.last_execution.time }}
              </div>
            </div>
            <el-text v-else type="info">未执行</el-text>
          </template>
        </el-table-column>

        <el-table-column prop="created_at" label="创建时间" width="180" />

        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-space>
              <el-button link type="primary" size="small" @click.stop="handleEdit(row)">
                编辑
              </el-button>
              <el-button link type="success" size="small" @click.stop="handleCopy(row)">
                复制
              </el-button>
              <el-button
                link
                :type="row.is_enabled ? 'warning' : 'success'"
                size="small"
                @click.stop="handleToggleStatus(row)"
              >
                {{ row.is_enabled ? '关闭' : '开启' }}
              </el-button>
              <el-button link type="info" size="small" @click.stop="handleSimulate(row)">
                模拟
              </el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(row)">
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 策略详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="策略详情"
      size="60%"
      destroy-on-close
    >
      <strategy-detail v-if="currentStrategy" :strategy="currentStrategy" />
    </el-drawer>

    <!-- 策略向导对话框 -->
    <el-dialog
      v-model="wizardDialogVisible"
      :title="wizardTitle"
      width="80%"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <strategy-wizard
        v-if="wizardDialogVisible"
        :queue-id="selectedQueue"
        :strategy="editingStrategy"
        @success="handleWizardSuccess"
        @cancel="wizardDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import StrategyDetail from './StrategyDetail.vue'
import StrategyWizard from './StrategyWizard.vue'

const tenantStore = useTenantStore()

// 队列列表
const queues = ref([
  { id: 1, queue_code: 'C', queue_name: 'C队列' },
  { id: 2, queue_code: 'S0', queue_name: 'S0队列' },
  { id: 3, queue_code: 'S1', queue_name: 'S1队列' },
  { id: 4, queue_code: 'L1', queue_name: 'L1队列' },
  { id: 5, queue_code: 'M1', queue_name: 'M1队列' },
])

const selectedQueue = ref(1)
const detailDrawerVisible = ref(false)
const wizardDialogVisible = ref(false)
const wizardTitle = ref('新建策略')
const currentStrategy = ref<any>(null)
const editingStrategy = ref<any>(null)

// Mock 策略数据
const strategies = ref([
  {
    id: 1,
    sort_order: 1,
    strategy_name: 'C队列-机构A优先分配',
    is_enabled: true,
    target_level: '催员',
    target_names: '机构A - 电催组1, 电催组2',
    start_time: '2025-11-10 09:00:00',
    created_at: '2025-11-10 08:30:00',
    last_execution: {
      status: 'success',
      time: '2025-11-12 09:05:00'
    }
  },
  {
    id: 2,
    sort_order: 2,
    strategy_name: 'C队列-机构B平均分配',
    is_enabled: true,
    target_level: '催员',
    target_names: '机构B - 催收组1, 催收组2, 催收组3',
    start_time: '2025-11-10 09:00:00',
    created_at: '2025-11-10 08:45:00',
    last_execution: {
      status: 'success',
      time: '2025-11-12 09:10:00'
    }
  },
  {
    id: 3,
    sort_order: 3,
    strategy_name: 'C队列-高额案件专项分配',
    is_enabled: false,
    target_level: '小组',
    target_names: '机构A - 高额案件组',
    start_time: '2025-11-15 09:00:00',
    created_at: '2025-11-11 10:00:00',
    last_execution: null
  },
  {
    id: 4,
    sort_order: 4,
    strategy_name: 'C队列-机构C补充分配',
    is_enabled: true,
    target_level: '催员',
    target_names: '机构C - 综合组',
    start_time: '2025-11-10 09:00:00',
    created_at: '2025-11-10 09:00:00',
    last_execution: {
      status: 'partial',
      time: '2025-11-12 09:15:00'
    }
  },
  {
    id: 5,
    sort_order: 5,
    strategy_name: 'C队列-新手催员培训分配',
    is_enabled: true,
    target_level: '催员',
    target_names: '机构A - 新手组',
    start_time: '2025-11-11 09:00:00',
    created_at: '2025-11-11 08:00:00',
    last_execution: {
      status: 'success',
      time: '2025-11-12 09:20:00'
    }
  },
])

// 加载策略列表
const loadStrategies = async () => {
  if (!tenantStore.currentTenantId) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    // TODO: 调用实际 API
    // const res = await getStrategies(selectedQueue.value)
    
    ElMessage.success('策略列表已加载')
  } catch (error) {
    console.error('加载策略列表失败：', error)
    ElMessage.error('加载策略列表失败')
  }
}

// 获取执行状态类型
const getExecutionStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    success: 'success',
    failed: 'danger',
    partial: 'warning',
    running: 'info',
  }
  return typeMap[status] || 'info'
}

// 获取执行状态文本
const getExecutionStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    success: '成功',
    failed: '失败',
    partial: '部分成功',
    running: '执行中',
  }
  return textMap[status] || '未知'
}

// 新建策略
const handleCreate = () => {
  editingStrategy.value = null
  wizardTitle.value = '新建策略'
  wizardDialogVisible.value = true
}

// 编辑策略
const handleEdit = (row: any) => {
  editingStrategy.value = { ...row }
  wizardTitle.value = '编辑策略'
  wizardDialogVisible.value = true
}

// 复制策略
const handleCopy = (row: any) => {
  editingStrategy.value = { ...row, id: undefined, strategy_name: `${row.strategy_name} (副本)` }
  wizardTitle.value = '复制策略'
  wizardDialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row: any) => {
  currentStrategy.value = row
  detailDrawerVisible.value = true
}

// 行点击
const handleRowClick = (row: any) => {
  handleViewDetail(row)
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.is_enabled ? '关闭' : '开启'}策略"${row.strategy_name}"吗？`,
      '状态切换确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // TODO: 调用实际 API
    row.is_enabled = !row.is_enabled
    ElMessage.success(`策略已${row.is_enabled ? '开启' : '关闭'}`)
  } catch (error) {
    // 用户取消
  }
}

// 模拟执行
const handleSimulate = (row: any) => {
  ElMessage.info('模拟功能开发中...')
}

// 删除策略
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除策略"${row.strategy_name}"吗？删除后无法恢复，但会保留操作日志。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // TODO: 调用实际 API
    const index = strategies.value.findIndex(s => s.id === row.id)
    if (index > -1) {
      strategies.value.splice(index, 1)
    }
    ElMessage.success('策略已删除')
  } catch (error) {
    // 用户取消
  }
}

// 向导成功回调
const handleWizardSuccess = () => {
  wizardDialogVisible.value = false
  loadStrategies()
}

onMounted(() => {
  loadStrategies()
})
</script>

<style scoped>
.strategy-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drag-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  cursor: move;
}

.drag-handle:hover {
  color: #409eff;
}
</style>

