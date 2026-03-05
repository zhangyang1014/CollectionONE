<template>
  <div class="ip-management">
    <!-- IP需求推算卡片 -->
    <el-card class="demand-card" shadow="hover" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <span>IP管理</span>
          <el-button type="primary" size="small" @click="loadDemand">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-row :gutter="24">
        <el-col :span="5">
          <el-statistic title="总Instant数" :value="demand.totalInstants" />
        </el-col>
        <el-col :span="5">
          <el-statistic title="每IP可用Phone数" :value="demand.phonesPerIp" />
        </el-col>
        <el-col :span="5">
          <el-statistic title="需要IP总数" :value="demand.requiredIps" />
        </el-col>
        <el-col :span="5">
          <el-statistic title="当前活跃IP" :value="demand.currentIps" />
        </el-col>
        <el-col :span="4">
          <div class="demand-highlight">
            <div class="demand-value" :class="{ urgent: demand.newIpsNeeded > 0 }">
              {{ demand.newIpsNeeded }}
            </div>
            <div class="demand-label">需新增IP数</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- IP列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <span>IP地址列表</span>
            <span v-if="selectedIps.length > 0" class="selection-tip">
              已选择 {{ selectedIps.length }} 项
            </span>
          </div>
          <div class="card-header-right">
            <el-button
              v-if="selectedIps.length > 0"
              type="danger"
              plain
              @click="handleBatchDelete"
            >
              批量删除 ({{ selectedIps.length }})
            </el-button>
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增IP
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="ipList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <!-- 复选框：仅允许勾选无关联Phone的行 -->
        <el-table-column type="selection" width="45" :selectable="isRowSelectable" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="port" label="端口" width="80" align="center" />
        <el-table-column prop="accountName" label="账号名" width="140" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="IP_STATUS_MAP[row.status].type" size="small">
              {{ IP_STATUS_MAP[row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="onlineAt" label="上线时间" width="170" />

        <!-- 累计服务时间 -->
        <el-table-column label="累计服务时间" width="140" align="center">
          <template #header>
            <span>累计服务时间</span>
            <el-tooltip content="该IP自上线以来累计提供代理服务的总时长" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            {{ formatHours(row.cumulativeServiceHours) }}
          </template>
        </el-table-column>

        <!-- 封号率 -->
        <el-table-column label="封号率" width="110" align="center">
          <template #header>
            <span>封号率</span>
            <el-tooltip content="该IP下被封号的号码数 / 总绑定号码数；有封号即为红色" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <el-tag :type="row.banRate > 0 ? 'danger' : 'success'" size="small">
              {{ formatPercent(row.banRate) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 负载率 -->
        <el-table-column label="负载率" width="150" align="center">
          <template #header>
            <span>负载率</span>
            <el-tooltip content="已绑定Phone数 / 每IP上限数；超过80%变橙色，满载变红" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <div class="load-cell">
              <el-progress
                :percentage="Math.round(row.loadRate * 100)"
                :color="getLoadColor(row.loadRate)"
                :stroke-width="8"
                style="flex: 1"
              />
              <span class="load-text">{{ row.linkedPhoneCount }}/{{ phonesPerIpLimit }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 健康度 -->
        <el-table-column label="健康度" width="170">
          <template #header>
            <span>健康度</span>
            <el-tooltip content="综合封号率、负载率、IP状态、在线稳定性的综合评分（0~100分）" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <div class="health-cell">
              <el-progress
                :percentage="row.healthScore"
                :color="getHealthColor(row.healthScore)"
                :stroke-width="14"
                :text-inside="true"
                style="flex: 1"
              />
            </div>
          </template>
        </el-table-column>

        <!-- WA存活时间 -->
        <el-table-column label="WA存活时间" width="130" align="center">
          <template #header>
            <span>WA存活时间</span>
            <el-tooltip content="该IP下号码从激活到封号/下线的平均时长（仅统计存活>3天或已封号的号码）" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            {{ row.avgSurvivalHours > 0 ? formatHours(row.avgSurvivalHours) : '-' }}
          </template>
        </el-table-column>

        <!-- WA使用时间 -->
        <el-table-column label="WA使用时间" width="130" align="center">
          <template #header>
            <span>WA使用时间</span>
            <el-tooltip content="该IP下号码被催员实际使用的平均累计时长（仅统计存活>3天或已封号的号码）" placement="top">
              <el-icon class="col-tip"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="{ row }">
            {{ row.avgUsageHours > 0 ? formatHours(row.avgUsageHours) : '-' }}
          </template>
        </el-table-column>

        <el-table-column label="关联Phone" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.linkedPhoneCount > 0 ? 'primary' : 'info'">
              {{ row.linkedPhoneCount }} 个
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑IP弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingIp ? '编辑IP' : '新增IP'"
      width="500px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="ipForm" :rules="ipRules" label-width="80px">
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="ipForm.ipAddress" placeholder="例如 103.45.67.12" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="ipForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="账号" prop="accountName">
          <el-input v-model="ipForm.accountName" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="ipForm.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingIp ? '保存' : '新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, QuestionFilled } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { WaIp, IpDemand } from '@/types/wa-management'
import { IP_STATUS_MAP } from '@/types/wa-management'
import { getIpList, createIp, updateIp, deleteIp, getIpDemand } from '@/api/wa-management'

const loading = ref(false)
const submitting = ref(false)
const ipList = ref<WaIp[]>([])
const demand = ref<IpDemand>({ totalInstants: 0, phonesPerIp: 5, requiredIps: 0, currentIps: 0, newIpsNeeded: 0 })
const selectedIps = ref<WaIp[]>([])

/** 每IP上限Phone数，从需求数据中同步 */
const phonesPerIpLimit = computed(() => demand.value.phonesPerIp || 5)

const showAddDialog = ref(false)
const editingIp = ref<WaIp | null>(null)
const formRef = ref<FormInstance>()

const ipForm = reactive({
  ipAddress: '',
  port: 8443,
  accountName: '',
  password: '',
})

const ipRules = {
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: 'IP格式不正确', trigger: 'blur' },
  ],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const loadList = async () => {
  loading.value = true
  try {
    ipList.value = await getIpList()
  } finally {
    loading.value = false
  }
}

const loadDemand = async () => {
  demand.value = await getIpDemand()
}

const formatHours = (hours: number): string => {
  if (hours < 24) return `${hours}小时`
  const days = Math.floor(hours / 24)
  const h = hours % 24
  return h > 0 ? `${days}天${h}小时` : `${days}天`
}

/** 封号率格式化，保留一位小数百分比 */
const formatPercent = (rate: number): string => {
  return `${(rate * 100).toFixed(1)}%`
}

/** 负载率进度条颜色：>80% 橙色，满载 红色，否则绿色 */
const getLoadColor = (rate: number): string => {
  if (rate >= 1) return '#f56c6c'
  if (rate > 0.8) return '#e6a23c'
  return '#67c23a'
}

const getHealthColor = (score: number): string => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const handleEdit = (row: WaIp) => {
  editingIp.value = row
  ipForm.ipAddress = row.ipAddress
  ipForm.port = row.port
  ipForm.accountName = row.accountName
  ipForm.password = row.password
  showAddDialog.value = true
}

const handleDelete = async (row: WaIp) => {
  if (row.linkedPhoneCount > 0) {
    ElMessage.warning(`该IP下还有 ${row.linkedPhoneCount} 个Phone关联，请先解绑后再删除`)
    return
  }
  await ElMessageBox.confirm(`确定删除IP ${row.ipAddress}:${row.port}？`, '删除确认', { type: 'warning' })
  await deleteIp(row.id)
  ElMessage.success('删除成功')
  loadList()
  loadDemand()
}

/** 控制复选框是否可选：仅允许无关联Phone的行 */
const isRowSelectable = (row: WaIp): boolean => row.linkedPhoneCount === 0

const handleSelectionChange = (rows: WaIp[]) => {
  selectedIps.value = rows
}

const handleBatchDelete = async () => {
  const targets = selectedIps.value
  if (targets.length === 0) return
  await ElMessageBox.confirm(
    `确定删除选中的 ${targets.length} 个IP地址？此操作不可撤销。`,
    '批量删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  let successCount = 0
  for (const ip of targets) {
    try {
      await deleteIp(ip.id)
      successCount++
    } catch {
      // 单条失败不中断整体
    }
  }
  ElMessage.success(`已成功删除 ${successCount} 个IP地址`)
  selectedIps.value = []
  loadList()
  loadDemand()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    if (editingIp.value) {
      await updateIp(editingIp.value.id, { ...ipForm })
      ElMessage.success('更新成功')
    } else {
      await createIp({ ...ipForm })
      ElMessage.success('新增成功')
    }
    showAddDialog.value = false
    loadList()
    loadDemand()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingIp.value = null
  ipForm.ipAddress = ''
  ipForm.port = 8443
  ipForm.accountName = ''
  ipForm.password = ''
}

onMounted(() => {
  loadList()
  loadDemand()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selection-tip {
  font-size: 13px;
  color: #409eff;
}

.health-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.load-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.load-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  min-width: 28px;
}

.col-tip {
  margin-left: 3px;
  font-size: 13px;
  color: #909399;
  vertical-align: middle;
  cursor: pointer;
}

.col-tip:hover {
  color: #409eff;
}

.demand-highlight {
  text-align: center;
}

.demand-value {
  font-size: 28px;
  font-weight: 700;
  color: #67c23a;
  line-height: 1.4;
}

.demand-value.urgent {
  color: #f56c6c;
}

.demand-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
