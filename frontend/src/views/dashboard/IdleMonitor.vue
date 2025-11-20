<template>
  <div class="idle-monitor-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <h2>空闲催员监控看板</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showConfigDialog">
          <el-icon><Setting /></el-icon>
          空闲配置
        </el-button>
        <el-button @click="handleExport" :loading="exportLoading">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 甲方未选择提示 -->
    <el-alert
      v-if="!tenantStore.currentTenant?.id"
      title="请先选择甲方"
      type="warning"
      description="请在页面右上角选择一个甲方后再使用本功能"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    />

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="机构">
              <el-select
                v-model="filterForm.agencyIds"
                multiple
                filterable
                placeholder="请选择机构"
                style="width: 100%"
              >
                <el-option
                  v-for="agency in agencies"
                  :key="agency.id"
                  :label="agency.name"
                  :value="agency.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="小组">
              <el-select
                v-model="filterForm.teamIds"
                multiple
                filterable
                placeholder="请选择小组"
                style="width: 100%"
              >
                <el-option
                  v-for="team in teams"
                  :key="team.id"
                  :label="`${team.name} (${team.memberCount}人)`"
                  :value="team.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="催员">
              <el-select
                v-model="filterForm.collectorIds"
                multiple
                filterable
                placeholder="搜索催员姓名/工号"
                style="width: 100%"
              >
                <el-option
                  v-for="collector in collectors"
                  :key="collector.id"
                  :label="`${collector.name} (${collector.code})`"
                  :value="collector.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="日期">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据总览区域 -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="6">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-content">
            <div class="summary-label">空闲催员总数</div>
            <div class="summary-value danger">{{ summaryData.totalIdleCollectors || 0 }}人</div>
            <div class="summary-change" :class="getChangeClass(summaryData.comparison?.collectorsChange)">
              <el-icon v-if="(summaryData.comparison?.collectorsChange || 0) > 0"><CaretTop /></el-icon>
              <el-icon v-else-if="(summaryData.comparison?.collectorsChange || 0) < 0"><CaretBottom /></el-icon>
              {{ formatPercentage(summaryData.comparison?.collectorsChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-content">
            <div class="summary-label">空闲总次数</div>
            <div class="summary-value warning">{{ summaryData.totalIdleCount || 0 }}次</div>
            <div class="summary-change" :class="getChangeClass(summaryData.comparison?.countChange)">
              <el-icon v-if="(summaryData.comparison?.countChange || 0) > 0"><CaretTop /></el-icon>
              <el-icon v-else-if="(summaryData.comparison?.countChange || 0) < 0"><CaretBottom /></el-icon>
              {{ formatPercentage(summaryData.comparison?.countChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-content">
            <div class="summary-label">空闲总时长</div>
            <div class="summary-value danger">{{ formatHours(summaryData.totalIdleMinutes) }}</div>
            <div class="summary-change" :class="getChangeClass(summaryData.comparison?.minutesChange)">
              <el-icon v-if="(summaryData.comparison?.minutesChange || 0) > 0"><CaretTop /></el-icon>
              <el-icon v-else-if="(summaryData.comparison?.minutesChange || 0) < 0"><CaretBottom /></el-icon>
              {{ formatPercentage(summaryData.comparison?.minutesChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-content">
            <div class="summary-label">平均空闲时长</div>
            <div class="summary-value primary">{{ formatMinutes(summaryData.avgIdleMinutes) }}</div>
            <div class="summary-change" :class="getChangeClass(summaryData.comparison?.avgChange)">
              <el-icon v-if="(summaryData.comparison?.avgChange || 0) > 0"><CaretTop /></el-icon>
              <el-icon v-else-if="(summaryData.comparison?.avgChange || 0) < 0"><CaretBottom /></el-icon>
              {{ formatPercentage(summaryData.comparison?.avgChange) }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表区域 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="chart-header">
          <span>趋势图表</span>
          <el-select v-model="trendMetric" @change="loadTrendData" style="width: 180px">
            <el-option label="空闲催员数" value="collectors" />
            <el-option label="空闲总次数" value="count" />
            <el-option label="空闲总时长(小时)" value="minutes" />
            <el-option label="平均空闲时长(分钟)" value="avg" />
          </el-select>
        </div>
      </template>
      <div ref="trendChartRef" style="width: 100%; height: 300px"></div>
    </el-card>

    <!-- 详情列表区域 -->
    <el-card class="detail-card" shadow="never">
      <template #header>
        <span>空闲催员详情列表</span>
      </template>
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-content">
              <div class="expand-title">空闲时段:</div>
              <div
                v-for="(period, index) in row.idlePeriods"
                :key="index"
                class="period-item"
              >
                • {{ period.start }} - {{ period.end }} ({{ period.duration }}分钟)
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="collectorName" label="催员" width="150" sortable="custom">
          <template #default="{ row }">
            <el-link type="primary" @click="showDetailDialog(row)">
              {{ row.collectorName }} ({{ row.collectorCode }})
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="agencyName" label="所属机构" width="150" sortable="custom" />
        <el-table-column prop="teamName" label="所属小组" width="120" sortable="custom" />
        <el-table-column prop="statDate" label="统计日期" width="120" sortable="custom" />
        <el-table-column label="管理案件数" width="150" sortable="custom">
          <template #default="{ row }">
            <span :class="getCaseRateClass(row.managedCases?.collectionRate)">
              {{ row.managedCases?.collected || 0 }}/{{ row.managedCases?.total || 0 }}
              ({{ formatPercentage(row.managedCases?.collectionRate) }})
            </span>
          </template>
        </el-table-column>
        <el-table-column label="管理案件金额" width="200" sortable="custom">
          <template #default="{ row }">
            <span :class="getCaseRateClass(row.managedAmount?.collectionRate)">
              ¥{{ formatAmount(row.managedAmount?.collected) }}/¥{{ formatAmount(row.managedAmount?.total) }}
              ({{ formatPercentage(row.managedAmount?.collectionRate) }})
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="idleCount"
          label="空闲次数"
          width="100"
          sortable="custom"
          :sort-orders="['descending', 'ascending']"
        >
          <template #default="{ row }">
            <el-tag type="danger">{{ row.idleCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="totalIdleMinutes"
          label="总计空闲时长"
          width="140"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatMinutes(row.totalIdleMinutes) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetailDialog(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 空闲配置弹窗 -->
    <el-dialog
      v-model="configDialogVisible"
      title="空闲定义配置"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        
        <el-form-item label="上班时间段" required>
          <div class="time-slots">
            <div
              v-for="(slot, index) in configForm.workTimeSlots"
              :key="index"
              class="time-slot-item"
            >
              <el-time-select
                v-model="slot.start"
                placeholder="开始时间"
                start="00:00"
                step="00:30"
                end="23:30"
              />
              <span class="time-separator">至</span>
              <el-time-select
                v-model="slot.end"
                placeholder="结束时间"
                start="00:00"
                step="00:30"
                end="23:30"
              />
              <el-button
                v-if="configForm.workTimeSlots.length > 1"
                type="danger"
                link
                @click="removeTimeSlot(index)"
              >
                删除
              </el-button>
            </div>
            <el-button
              v-if="configForm.workTimeSlots.length < 5"
              type="primary"
              link
              @click="addTimeSlot"
            >
              + 添加时间段
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="空闲判定阈值" prop="idleThresholdMinutes">
          <el-input-number
            v-model="configForm.idleThresholdMinutes"
            :min="5"
            :max="120"
            :step="5"
          />
          <span class="form-tip">分钟（范围：5-120分钟）</span>
          <div class="form-description">
            在此时长内，未发生以下任一行为，则判定为空闲
          </div>
        </el-form-item>

        <el-form-item label="监控行为" prop="monitoredActions">
          <el-checkbox-group v-model="configForm.monitoredActions">
            <el-checkbox label="call">打电话</el-checkbox>
            <el-checkbox label="whatsapp">发送WhatsApp</el-checkbox>
            <el-checkbox label="rcs">发送RCS</el-checkbox>
            <el-checkbox label="sms">发送SMS</el-checkbox>
            <el-checkbox label="email">发送邮件</el-checkbox>
            <el-checkbox label="case_update">案件操作</el-checkbox>
            <el-checkbox label="login">系统登录</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">至少选择一项</div>
        </el-form-item>

        <el-form-item label="其他选项">
          <el-checkbox v-model="configForm.excludeHolidays">排除法定节假日</el-checkbox>
        </el-form-item>

        <el-alert
          title="配置说明"
          type="info"
          :closable="false"
          show-icon
        >
          <ul class="config-tips">
            <li>此配置将应用于所有催员的空闲监控</li>
            <li>修改后立即生效，影响后续计算</li>
            <li>历史数据不受影响</li>
          </ul>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showConfigHistory">查看历史配置</el-button>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="configSaving">
          保存并应用
        </el-button>
      </template>
    </el-dialog>

    <!-- 催员详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`催员空闲详情 - ${detailData.collectorInfo?.name} (${detailData.collectorInfo?.code})`"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="detailTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">
              {{ detailData.collectorInfo?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="工号">
              {{ detailData.collectorInfo?.code }}
            </el-descriptions-item>
            <el-descriptions-item label="所属机构">
              {{ detailData.collectorInfo?.agencyName }}
            </el-descriptions-item>
            <el-descriptions-item label="所属小组">
              {{ detailData.collectorInfo?.teamName }}
            </el-descriptions-item>
            <el-descriptions-item label="统计日期">
              {{ detailData.statDate }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-section">
            <h4>空闲统计</h4>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="空闲次数" :value="detailData.idleSummary?.idleCount || 0" suffix="次" />
              </el-col>
              <el-col :span="8">
                <el-statistic
                  title="总计时长"
                  :value="detailData.idleSummary?.totalIdleMinutes || 0"
                  suffix="分钟"
                />
              </el-col>
              <el-col :span="8">
                <el-statistic
                  title="平均时长"
                  :value="detailData.idleSummary?.avgIdleMinutes || 0"
                  suffix="分钟"
                  :precision="1"
                />
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="案件情况" name="cases">
          <div class="detail-section">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总案件">
                {{ detailData.caseSummary?.totalCases || 0 }}件
              </el-descriptions-item>
              <el-descriptions-item label="已还案件">
                {{ detailData.caseSummary?.collectedCases || 0 }}件
              </el-descriptions-item>
              <el-descriptions-item label="案件催回率">
                {{ formatPercentage(detailData.caseSummary?.collectionRate) }}
              </el-descriptions-item>
              <el-descriptions-item label="总金额">
                ¥{{ formatAmount(detailData.caseSummary?.totalAmount) }}
              </el-descriptions-item>
              <el-descriptions-item label="已还金额">
                ¥{{ formatAmount(detailData.caseSummary?.collectedAmount) }}
              </el-descriptions-item>
              <el-descriptions-item label="金额回款率">
                {{ formatPercentage(detailData.caseSummary?.amountCollectionRate) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="空闲明细" name="idle">
          <div class="detail-section">
            <h4>空闲时段时间轴</h4>
            <div ref="timelineChartRef" style="width: 100%; height: 150px"></div>
          </div>

          <div class="detail-section">
            <h4>空闲明细列表</h4>
            <div
              v-for="(detail, index) in detailData.idleDetails"
              :key="index"
              class="idle-detail-item"
            >
              <div class="detail-header">
                {{ index + 1 }}. {{ formatDateTime(detail.startTime) }} - {{ formatTime(detail.endTime) }}
                <el-tag type="danger" size="small">{{ detail.durationMinutes }}分钟</el-tag>
              </div>
              <div class="detail-actions">
                <span class="action-label">之前行为:</span>
                <span class="action-content">
                  {{ getActionLabel(detail.beforeAction?.type) }} @ {{ formatTime(detail.beforeAction?.time) }}
                </span>
              </div>
              <div class="detail-actions">
                <span class="action-label">之后行为:</span>
                <span class="action-content">
                  <template v-if="detail.afterAction">
                    {{ getActionLabel(detail.afterAction?.type) }} @ {{ formatTime(detail.afterAction?.time) }}
                  </template>
                  <template v-else>
                    <el-tag size="small">下班（无后续行为）</el-tag>
                  </template>
                </span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="handleExportDetail">导出报告</el-button>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 配置历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="配置历史记录"
      width="800px"
    >
      <el-table :data="configHistory" stripe>
        <el-table-column prop="configName" label="配置名称" width="200" />
        <el-table-column prop="createdBy" label="创建人" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isActive" type="success">当前使用</el-tag>
            <el-tag v-else type="info">历史版本</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewConfigDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Download, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import {
  getIdleMonitorConfig,
  saveIdleMonitorConfig,
  updateIdleMonitorConfig,
  getIdleConfigHistory,
  getIdleMonitorSummary,
  getIdleMonitorDetails,
  getCollectorIdleDetail,
  getIdleMonitorTrend,
  exportIdleMonitorData
} from '@/api/dashboard'
import { getAgencies, getTeams, getCollectors } from '@/api/organization'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'

const tenantStore = useTenantStore()

// 筛选表单
const filterForm = reactive({
  agencyIds: [],
  teamIds: [],
  collectorIds: [],
  dateRange: [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
})

// 数据
const agencies = ref([])
const teams = ref([])
const collectors = ref([])
const summaryData = ref<any>({})
const tableData = ref([])
const loading = ref(false)
const exportLoading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 排序
const sortBy = ref('idleCount')
const sortOrder = ref('desc')

// 趋势图
const trendMetric = ref('collectors')
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null

// 配置弹窗
const configDialogVisible = ref(false)
const configSaving = ref(false)
const configFormRef = ref()
const configForm = reactive({
  id: null,
  configName: '标准工作日规则',
  workTimeSlots: [
    { start: '09:00', end: '12:00' },
    { start: '14:00', end: '18:00' }
  ],
  idleThresholdMinutes: 30,
  monitoredActions: ['call', 'whatsapp', 'rcs', 'sms', 'email', 'case_update', 'login'],
  excludeHolidays: true
})

const configRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  idleThresholdMinutes: [
    { required: true, message: '请输入空闲阈值', trigger: 'blur' },
    { type: 'number', min: 5, max: 120, message: '范围：5-120分钟', trigger: 'blur' }
  ],
  monitoredActions: [
    {
      type: 'array',
      required: true,
      min: 1,
      message: '至少选择一项监控行为',
      trigger: 'change'
    }
  ]
}

// 详情弹窗
const detailDialogVisible = ref(false)
const detailTab = ref('basic')
const detailData = ref<any>({})
const timelineChartRef = ref<HTMLElement>()
let timelineChart: echarts.ECharts | null = null

// 配置历史弹窗
const historyDialogVisible = ref(false)
const configHistory = ref([])

// 初始化
onMounted(() => {
  // 检查是否有甲方
  if (tenantStore.currentTenant?.id) {
    loadOrganizations()
    loadCurrentConfig()
    handleSearch()
  } else {
    // 没有甲方时，显示友好提示
    console.warn('请先选择甲方')
  }
})

// 监听甲方变化
watch(
  () => tenantStore.currentTenant?.id,
  (newTenantId, oldTenantId) => {
    if (newTenantId && newTenantId !== oldTenantId) {
      // 甲方切换后重新加载数据
      loadOrganizations()
      loadCurrentConfig()
      handleSearch()
    }
  }
)

// 加载机构数据
async function loadOrganizations() {
  try {
    if (!tenantStore.currentTenant?.id) return
    
    // 加载机构列表
    const agenciesRes = await getAgencies({ 
      tenant_id: tenantStore.currentTenant.id,
      is_active: true
    })
    agencies.value = agenciesRes.data || []
    
    // 加载所有小组（从所有机构）
    teams.value = []
    for (const agency of agencies.value) {
      try {
        const teamsRes = await request({
          url: `/api/v1/agencies/${agency.id}/teams`,
          method: 'get'
        })
        if (teamsRes.data) {
          teams.value.push(...teamsRes.data)
        }
      } catch (err) {
        console.warn(`加载机构 ${agency.id} 的小组失败:`, err)
      }
    }
    
    // 加载所有催员（从所有机构）
    collectors.value = []
    for (const agency of agencies.value) {
      try {
        const collectorsRes = await request({
          url: `/api/v1/agencies/${agency.id}/collectors`,
          method: 'get'
        })
        if (collectorsRes.data) {
          collectors.value.push(...collectorsRes.data)
        }
      } catch (err) {
        console.warn(`加载机构 ${agency.id} 的催员失败:`, err)
      }
    }
  } catch (error) {
    console.error('加载组织数据失败:', error)
  }
}

// 加载当前配置
async function loadCurrentConfig() {
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      console.warn('没有选择甲方，使用默认配置')
      return
    }
    
    const res = await getIdleMonitorConfig({ tenant_id: tenantStore.currentTenant.id })
    if (res.data && res.data.id) {
      Object.assign(configForm, res.data)
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    // 使用默认配置，不影响页面显示
  }
}

// 查询
async function handleSearch() {
  loading.value = true
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      ElMessage.warning('请先选择甲方')
      loading.value = false
      return
    }
    
    const params = {
      tenant_id: tenantStore.currentTenant.id,
      agency_ids: filterForm.agencyIds.join(','),
      team_ids: filterForm.teamIds.join(','),
      collector_ids: filterForm.collectorIds.join(','),
      start_date: filterForm.dateRange[0],
      end_date: filterForm.dateRange[1],
      page: pagination.page,
      page_size: pagination.pageSize,
      sort_by: sortBy.value,
      sort_order: sortOrder.value
    }

    // 加载总览数据
    const summaryRes = await getIdleMonitorSummary(params)
    summaryData.value = summaryRes.data || {}

    // 加载详情列表
    const detailRes = await getIdleMonitorDetails(params)
    tableData.value = detailRes.data?.items || []
    pagination.total = detailRes.data?.total || 0

    // 加载趋势数据
    loadTrendData()
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置
function handleReset() {
  filterForm.agencyIds = []
  filterForm.teamIds = []
  filterForm.collectorIds = []
  filterForm.dateRange = [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  pagination.page = 1
  handleSearch()
}

// 排序变化
function handleSortChange({ prop, order }: any) {
  sortBy.value = prop || 'idleCount'
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  handleSearch()
}

// 加载趋势数据
async function loadTrendData() {
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      return
    }
    
    const params = {
      tenant_id: tenantStore.currentTenant.id,
      agency_ids: filterForm.agencyIds.join(','),
      team_ids: filterForm.teamIds.join(','),
      start_date: filterForm.dateRange[0],
      end_date: filterForm.dateRange[1],
      metric: trendMetric.value
    }
    const res = await getIdleMonitorTrend(params)
    renderTrendChart(res.data)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 渲染趋势图
function renderTrendChart(data: any) {
  nextTick(() => {
    if (!trendChartRef.value) return

    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: data?.dates || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: data?.values || [],
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#f56c6c'
          }
        }
      ]
    }

    trendChart.setOption(option)
  })
}

// 显示配置弹窗
function showConfigDialog() {
  configDialogVisible.value = true
}

// 添加时间段
function addTimeSlot() {
  configForm.workTimeSlots.push({ start: '', end: '' })
}

// 删除时间段
function removeTimeSlot(index: number) {
  configForm.workTimeSlots.splice(index, 1)
}

// 保存配置
async function handleSaveConfig() {
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      ElMessage.warning('请先选择甲方')
      return
    }
    
    await configFormRef.value.validate()
    configSaving.value = true

    const data = {
      tenant_id: tenantStore.currentTenant.id,
      config_name: configForm.configName,
      work_time_slots: configForm.workTimeSlots,
      idle_threshold_minutes: configForm.idleThresholdMinutes,
      monitored_actions: configForm.monitoredActions,
      exclude_holidays: configForm.excludeHolidays
    }

    if (configForm.id) {
      await updateIdleMonitorConfig(configForm.id, data)
    } else {
      await saveIdleMonitorConfig(data)
    }

    ElMessage.success('配置保存成功')
    configDialogVisible.value = false
    loadCurrentConfig()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    configSaving.value = false
  }
}

// 显示配置历史
async function showConfigHistory() {
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      ElMessage.warning('请先选择甲方')
      return
    }
    
    const res = await getIdleConfigHistory({
      tenant_id: tenantStore.currentTenant.id,
      page: 1,
      page_size: 20
    })
    configHistory.value = res.data?.items || []
    historyDialogVisible.value = true
  } catch (error) {
    console.error('加载配置历史失败:', error)
    ElMessage.error('加载配置历史失败')
  }
}

// 查看配置详情
function viewConfigDetail(config: any) {
  ElMessageBox.alert(
    `<div style="text-align: left">
      <p><strong>配置名称：</strong>${config.configName}</p>
      <p><strong>空闲阈值：</strong>${config.idleThresholdMinutes}分钟</p>
      <p><strong>创建时间：</strong>${config.createdAt}</p>
    </div>`,
    '配置详情',
    {
      dangerouslyUseHTMLString: true
    }
  )
}

// 显示详情弹窗
async function showDetailDialog(row: any) {
  try {
    const res = await getCollectorIdleDetail(row.collectorId, {
      date: row.statDate
    })
    detailData.value = res.data || {}
    detailDialogVisible.value = true
    detailTab.value = 'basic'

    // 渲染时间轴
    nextTick(() => {
      renderTimelineChart()
    })
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 渲染时间轴图表
function renderTimelineChart() {
  if (!timelineChartRef.value) return

  if (!timelineChart) {
    timelineChart = echarts.init(timelineChartRef.value)
  }

  // 简化的时间轴展示
  const idleDetails = detailData.value.idleDetails || []
  const data = idleDetails.map((item: any) => ({
    name: `${dayjs(item.startTime).format('HH:mm')}-${dayjs(item.endTime).format('HH:mm')}`,
    value: [
      new Date(item.startTime).getTime(),
      new Date(item.endTime).getTime()
    ]
  }))

  const option = {
    tooltip: {
      formatter: (params: any) => {
        return `空闲时段: ${params.name}`
      }
    },
    xAxis: {
      type: 'time',
      min: new Date(detailData.value.statDate + ' 08:00:00').getTime(),
      max: new Date(detailData.value.statDate + ' 19:00:00').getTime()
    },
    yAxis: {
      type: 'category',
      data: ['空闲时段']
    },
    series: [
      {
        type: 'custom',
        renderItem: (params: any, api: any) => {
          const categoryIndex = api.value(0)
          const start = api.coord([api.value(1), categoryIndex])
          const end = api.coord([api.value(2), categoryIndex])
          const height = api.size([0, 1])[1] * 0.6

          return {
            type: 'rect',
            shape: {
              x: start[0],
              y: start[1] - height / 2,
              width: end[0] - start[0],
              height: height
            },
            style: api.style({
              fill: '#f56c6c'
            })
          }
        },
        data: data.map((item: any) => [0, ...item.value])
      }
    ]
  }

  timelineChart.setOption(option)
}

// 导出
async function handleExport() {
  try {
    // 确保有tenant_id
    if (!tenantStore.currentTenant?.id) {
      ElMessage.warning('请先选择甲方')
      return
    }
    
    exportLoading.value = true
    const params = {
      tenant_id: tenantStore.currentTenant.id,
      agency_ids: filterForm.agencyIds.join(','),
      team_ids: filterForm.teamIds.join(','),
      collector_ids: filterForm.collectorIds.join(','),
      start_date: filterForm.dateRange[0],
      end_date: filterForm.dateRange[1]
    }
    const blob = await exportIdleMonitorData(params)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `空闲催员监控_${dayjs().format('YYYYMMDD')}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 导出详情报告
function handleExportDetail() {
  ElMessage.info('导出详情报告功能开发中')
}

// 禁用日期（最大90天）
function disabledDate(date: Date) {
  const start = filterForm.dateRange[0]
  if (!start) return false
  const diff = Math.abs(dayjs(date).diff(dayjs(start), 'day'))
  return diff > 90
}

// 格式化函数
function formatHours(minutes: number): string {
  if (!minutes) return '0小时'
  const hours = (minutes / 60).toFixed(1)
  return `${hours}小时`
}

function formatMinutes(minutes: number): string {
  if (!minutes) return '0分钟'
  return `${minutes.toFixed(1)}分钟`
}

function formatAmount(amount: number): string {
  if (!amount) return '0'
  return (amount / 1000).toFixed(1) + 'k'
}

function formatPercentage(value: number): string {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

function formatDateTime(dateTime: string): string {
  if (!dateTime) return ''
  return dayjs(dateTime).format('HH:mm')
}

function formatTime(time: string): string {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

function getChangeClass(change: number): string {
  if (!change) return ''
  return change > 0 ? 'change-up' : 'change-down'
}

function getCaseRateClass(rate: number): string {
  if (!rate) return ''
  if (rate >= 0.3) return 'rate-success'
  if (rate >= 0.15) return 'rate-warning'
  return 'rate-danger'
}

function getActionLabel(type: string): string {
  const labels: Record<string, string> = {
    call: '打电话',
    whatsapp: '发送WhatsApp',
    rcs: '发送RCS',
    sms: '发送SMS',
    email: '发送邮件',
    case_update: '案件操作',
    login: '登录系统',
    logout: '退出系统'
  }
  return labels[type] || type
}
</script>

<style scoped lang="scss">
.idle-monitor-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  .summary-content {
    text-align: center;

    .summary-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 10px;
    }

    .summary-value {
      font-size: 32px;
      font-weight: bold;
      margin-bottom: 5px;

      &.danger {
        color: #f56c6c;
      }

      &.warning {
        color: #e6a23c;
      }

      &.primary {
        color: #409eff;
      }
    }

    .summary-change {
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;

      &.change-up {
        color: #f56c6c;
      }

      &.change-down {
        color: #67c23a;
      }
    }
  }
}

.chart-card {
  margin-bottom: 20px;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.detail-card {
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.expand-content {
  padding: 10px 20px;
  background-color: #f5f7fa;

  .expand-title {
    font-weight: bold;
    margin-bottom: 10px;
  }

  .period-item {
    padding: 5px 0;
    color: #606266;
  }
}

.rate-success {
  color: #67c23a;
}

.rate-warning {
  color: #e6a23c;
}

.rate-danger {
  color: #f56c6c;
}

.time-slots {
  width: 100%;

  .time-slot-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;

    .time-separator {
      margin: 0 5px;
    }
  }
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.form-description {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}

.config-tips {
  margin: 0;
  padding-left: 20px;

  li {
    margin-bottom: 5px;
  }
}

.detail-section {
  margin-bottom: 20px;

  h4 {
    margin: 15px 0 10px;
    font-size: 16px;
    color: #303133;
  }
}

.idle-detail-item {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;

  .detail-header {
    font-weight: bold;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .detail-actions {
    padding: 5px 0;
    font-size: 14px;

    .action-label {
      color: #909399;
      margin-right: 10px;
    }

    .action-content {
      color: #606266;
    }
  }
}
</style>

