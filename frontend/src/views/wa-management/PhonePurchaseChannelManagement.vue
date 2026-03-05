<template>
  <div class="phone-purchase-channel">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>PHONE购买渠道管理</span>
          <el-button type="primary" size="small" @click="handleAddChannel">
            <el-icon><Plus /></el-icon>
            新增渠道
          </el-button>
        </div>
      </template>

      <el-table v-loading="channelLoading" :data="channels" style="width: 100%">
        <el-table-column prop="channelName" label="渠道名称" min-width="130" />
        <el-table-column prop="description" label="说明" min-width="180" />

        <!-- Instant 数量列 -->
        <el-table-column label="Instant数量" width="120" align="center">
          <template #header>
            <span>Instant数量</span>
            <el-tooltip content="该渠道下注册的 Phone 总数量，点击可跳转查看该渠道下所有号码" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <el-link
              type="primary"
              :underline="false"
              class="instant-count-link"
              @click="handleGoToPhones(row)"
            >
              {{ getStats(row.id)?.instantCount ?? '-' }}
            </el-link>
          </template>
        </el-table-column>

        <!-- 封号率列 -->
        <el-table-column label="封号率" width="90" align="center">
          <template #header>
            <span>封号率</span>
            <el-tooltip content="已封号 Phone 数 / 总 Phone 数" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <el-tag v-if="getStats(row.id)" :type="getBanRateType(getStats(row.id)!.banRate)" size="small">
              {{ formatPercent(getStats(row.id)!.banRate) }}
            </el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>

        <!-- 投养完成率列 -->
        <el-table-column label="投养完成率" width="160" align="center">
          <template #header>
            <span>投养完成率</span>
            <el-tooltip content="投养状态为「投养完成」的号码比例" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <div v-if="getStats(row.id)" class="progress-cell">
              <el-progress
                :percentage="Math.round(getStats(row.id)!.nurtureCompletionRate * 100)"
                :color="getNurtureColor(getStats(row.id)!.nurtureCompletionRate)"
                :stroke-width="7"
                style="flex: 1; min-width: 80px"
              />
              <span class="progress-text">{{ formatPercent(getStats(row.id)!.nurtureCompletionRate) }}</span>
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>

        <!-- 可用率列 -->
        <el-table-column label="可用率" width="90" align="center">
          <template #header>
            <span>可用率</span>
            <el-tooltip content="WA 状态为「正常」的号码比例" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <el-tag v-if="getStats(row.id)" :type="getAvailableRateType(getStats(row.id)!.availableRate)" size="small">
              {{ formatPercent(getStats(row.id)!.availableRate) }}
            </el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>

        <!-- WA存活时间列 -->
        <el-table-column label="均存活时长" width="110" align="center">
          <template #header>
            <span>均存活时长</span>
            <el-tooltip content="号码从激活到封号/下线的平均时长，仅统计存活>3天或已封号的号码" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span v-if="getStats(row.id)" class="quality-value">
              {{ getStats(row.id)!.avgSurvivalHours > 0 ? formatHours(getStats(row.id)!.avgSurvivalHours) : '-' }}
            </span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>

        <!-- WA使用时间列 -->
        <el-table-column label="均使用时长" width="110" align="center">
          <template #header>
            <span>均使用时长</span>
            <el-tooltip content="号码被催员实际使用的平均累计时长，仅统计存活>3天或已封号的号码" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <span v-if="getStats(row.id)" class="quality-value">
              {{ getStats(row.id)!.avgUsageHours > 0 ? formatHours(getStats(row.id)!.avgUsageHours) : '-' }}
            </span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              @change="handleToggleChannel(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditChannel(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteChannel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑购买渠道弹窗 -->
    <el-dialog
      v-model="showChannelDialog"
      :title="editingChannel ? '编辑购买渠道' : '新增购买渠道'"
      width="500px"
      :close-on-click-modal="false"
      @close="resetChannelForm"
    >
      <el-form ref="channelFormRef" :model="channelForm" :rules="channelRules" label-width="80px">
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="channelForm.channelName" placeholder="例如：云盾BD" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="channelForm.description"
            type="textarea"
            :rows="3"
            placeholder="渠道描述信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChannelDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingChannel" @click="handleSubmitChannel">
          {{ editingChannel ? '保存' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, QuestionFilled } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { WaPurchaseChannel, WaChannelStats } from '@/types/wa-management'
import {
  getPurchaseChannels,
  createPurchaseChannel,
  updatePurchaseChannel,
  deletePurchaseChannel,
  getChannelStats,
} from '@/api/wa-management'

const router = useRouter()

const channels = ref<WaPurchaseChannel[]>([])
const channelLoading = ref(false)
const showChannelDialog = ref(false)
const editingChannel = ref<WaPurchaseChannel | null>(null)
const savingChannel = ref(false)
const channelFormRef = ref<FormInstance>()

/** 渠道统计 Map，key=channelId */
const channelStatsMap = ref<Map<number, WaChannelStats>>(new Map())

const channelForm = reactive({
  channelName: '',
  description: '',
})

const channelRules = {
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
}

/** 根据渠道ID获取统计数据 */
const getStats = (channelId: number): WaChannelStats | undefined => {
  return channelStatsMap.value.get(channelId)
}

const loadChannels = async () => {
  channelLoading.value = true
  try {
    const [list, stats] = await Promise.all([
      getPurchaseChannels(),
      getChannelStats(),
    ])
    channels.value = list
    const map = new Map<number, WaChannelStats>()
    stats.forEach(s => map.set(s.channelId, s))
    channelStatsMap.value = map
  } finally {
    channelLoading.value = false
  }
}

/** 跳转到Phone管理，并以该渠道ID筛选 */
const handleGoToPhones = (row: WaPurchaseChannel) => {
  router.push({ name: 'WaPhoneManagement', query: { purchaseChannelId: String(row.id) } })
}

/** 封号率 tag 类型：<10% success，<30% warning，>=30% danger */
const getBanRateType = (rate: number): string => {
  if (rate < 0.1) return 'success'
  if (rate < 0.3) return 'warning'
  return 'danger'
}

/** 投养完成率进度条颜色：>=70% 绿，>=40% 橙，<40% 红 */
const getNurtureColor = (rate: number): string => {
  if (rate >= 0.7) return '#67c23a'
  if (rate >= 0.4) return '#e6a23c'
  return '#f56c6c'
}

/** 可用率 tag 类型：>=80% success，>=50% warning，<50% danger */
const getAvailableRateType = (rate: number): string => {
  if (rate >= 0.8) return 'success'
  if (rate >= 0.5) return 'warning'
  return 'danger'
}

const formatPercent = (rate: number): string => {
  return `${(rate * 100).toFixed(1)}%`
}

const formatHours = (hours: number): string => {
  if (hours < 24) return `${hours}小时`
  const days = Math.floor(hours / 24)
  const h = hours % 24
  return h > 0 ? `${days}天${h}小时` : `${days}天`
}

const handleAddChannel = () => {
  editingChannel.value = null
  channelForm.channelName = ''
  channelForm.description = ''
  showChannelDialog.value = true
}

const handleEditChannel = (row: WaPurchaseChannel) => {
  editingChannel.value = row
  channelForm.channelName = row.channelName
  channelForm.description = row.description
  showChannelDialog.value = true
}

const handleToggleChannel = async (row: WaPurchaseChannel) => {
  await updatePurchaseChannel(row.id, { isEnabled: row.isEnabled })
  ElMessage.success(row.isEnabled ? '已启用' : '已禁用')
}

const handleDeleteChannel = async (row: WaPurchaseChannel) => {
  await ElMessageBox.confirm(`确定删除渠道"${row.channelName}"？`, '删除确认', { type: 'warning' })
  await deletePurchaseChannel(row.id)
  ElMessage.success('删除成功')
  loadChannels()
}

const handleSubmitChannel = async () => {
  if (!channelFormRef.value) return
  await channelFormRef.value.validate()

  savingChannel.value = true
  try {
    if (editingChannel.value) {
      await updatePurchaseChannel(editingChannel.value.id, {
        channelName: channelForm.channelName,
        description: channelForm.description,
      })
      ElMessage.success('更新成功')
    } else {
      await createPurchaseChannel({
        channelName: channelForm.channelName,
        description: channelForm.description,
      })
      ElMessage.success('新增成功')
    }
    showChannelDialog.value = false
    loadChannels()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    savingChannel.value = false
  }
}

const resetChannelForm = () => {
  editingChannel.value = null
  channelForm.channelName = ''
  channelForm.description = ''
}

onMounted(() => {
  loadChannels()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Instant 数量可点击样式 */
.instant-count-link {
  font-size: 15px;
  font-weight: 600;
}

/* 投养完成率进度条单元格 */
.progress-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.progress-text {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
  min-width: 36px;
}

.quality-value {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
}

.no-data {
  font-size: 12px;
  color: #c0c4cc;
}

/* ? 提示图标 */
.col-tip {
  margin-left: 2px;
  font-size: 13px;
  color: #909399;
  vertical-align: middle;
  cursor: pointer;
}

.col-tip:hover {
  color: #409eff;
}
</style>
