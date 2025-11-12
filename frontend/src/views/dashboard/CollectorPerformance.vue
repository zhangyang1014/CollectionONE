<template>
  <div class="collector-performance">
    <!-- 头部筛选器 -->
    <el-card class="header-card">
      <el-form :model="filterForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="催员ID搜索">
              <el-input
                v-model="filterForm.collectorIdSearch"
                placeholder="请输入催员ID"
                style="width: 200px"
                clearable
                @clear="handleCollectorIdClear"
                @keyup.enter="handleCollectorIdSearch"
              >
                <template #append>
                  <el-button @click="handleCollectorIdSearch">搜索</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="机构">
              <el-select
                v-model="filterForm.agencyIds"
                multiple
                placeholder="全部机构"
                style="width: 100%"
                @change="handleAgencyChange"
                clearable
              >
                <el-option
                  v-for="agency in agencies"
                  :key="agency.id"
                  :label="agency.agency_name"
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
                placeholder="全部小组"
                style="width: 100%"
                @change="handleTeamChange"
                clearable
              >
                <el-option
                  v-for="team in teams"
                  :key="team.id"
                  :label="team.team_name"
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
                placeholder="全部催员"
                style="width: 100%"
                @change="handleCollectorChange"
                clearable
                filterable
              >
                <el-option
                  v-for="collector in collectors"
                  :key="collector.id"
                  :label="`${collector.collector_name} (${collector.collector_code})`"
                  :value="collector.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="时间周期">
              <el-select v-model="filterForm.period" @change="loadPerformanceData" style="width: 100%">
                <el-option label="日" value="daily" />
                <el-option label="周" value="weekly" />
                <el-option label="月" value="monthly" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="loadPerformanceData"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="队列">
              <el-select v-model="filterForm.queueIds" multiple placeholder="全部队列" style="width: 100%">
                <el-option label="队列1" value="1" />
                <el-option label="队列2" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div v-if="collectorInfo.name" class="collector-info">
        <h3>{{ collectorInfo.name || '催员姓名' }}</h3>
        <p>编号: {{ collectorInfo.code || '-' }}</p>
      </div>
    </el-card>

    <!-- 预警区域 -->
    <el-card v-if="alerts.length > 0" class="alert-card">
      <template #header>
        <div class="card-header">
          <span>⚠️ 预警提示</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col v-for="alert in alerts" :key="alert.alert_type" :span="8">
          <el-alert
            :title="alert.title"
            :type="alert.severity === 'high' ? 'error' : 'warning'"
            :description="alert.description"
            show-icon
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 业绩总览KPI卡片 -->
    <el-card class="kpi-card">
      <template #header>
        <div class="card-header">
          <span>业绩总览</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">应催案件</div>
            <div class="kpi-value">{{ performanceStats.assigned_cases || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">收回案件</div>
            <div class="kpi-value">{{ performanceStats.collected_cases || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">案件催回率</div>
            <div class="kpi-value">{{ performanceStats.case_collection_rate || 0 }}%</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">应催金额</div>
            <div class="kpi-value">{{ formatCurrency(performanceStats.assigned_amount) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">收回金额</div>
            <div class="kpi-value">{{ formatCurrency(performanceStats.collected_amount) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="kpi-item">
            <div class="kpi-label">金额催回率</div>
            <div class="kpi-value">{{ performanceStats.amount_collection_rate || 0 }}%</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- PTP指标 -->
    <el-card class="kpi-card">
      <template #header>
        <div class="card-header">
          <span>PTP指标</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="kpi-item">
            <div class="kpi-label">PTP数量</div>
            <div class="kpi-value">{{ performanceStats.ptp_count || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-item">
            <div class="kpi-label">PTP金额</div>
            <div class="kpi-value">{{ formatCurrency(performanceStats.ptp_amount) }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-item">
            <div class="kpi-label">PTP履约数</div>
            <div class="kpi-value">{{ performanceStats.ptp_fulfilled_count || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="kpi-item">
            <div class="kpi-label">PTP履约率</div>
            <div class="kpi-value">{{ performanceStats.ptp_fulfillment_rate || 0 }}%</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 沟通覆盖与效率 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>沟通覆盖与效率</span>
        </div>
      </template>
      <div ref="communicationChart" style="height: 300px;"></div>
    </el-card>

    <!-- 案件明细表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>案件明细</span>
        </div>
      </template>
      <el-table :data="cases" border>
        <el-table-column prop="case_code" label="案件编号" width="140" />
        <el-table-column prop="user_name" label="客户姓名" width="120" />
        <el-table-column prop="dpd" label="DPD" width="80" />
        <el-table-column prop="outstanding_amount" label="逾期金额" width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.outstanding_amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="communication_count" label="触达次数" width="100" />
        <el-table-column prop="last_contact_result" label="最近触达结果" width="130" />
        <el-table-column prop="ptp_status" label="PTP状态" width="100" />
        <el-table-column prop="case_status" label="案件状态" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'
import {
  getCollectorPerformance,
  getCollectorAlerts,
  getCollectorCases,
  getCollectorTrend,
} from '@/api/dashboard'
import {
  getTenantAgencies,
  getAgencyTeams,
  getTeamCollectors,
  getCollectors,
} from '@/api/organization'

const route = useRoute()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// 获取催员ID的逻辑
const getCollectorId = () => {
  // 1. 如果路由中有ID参数，优先使用
  if (route.params.id) {
    return Number(route.params.id)
  }
  
  // 2. 从用户信息中获取collector_id
  const userInfo = userStore.userInfo
  if (userInfo) {
    // 尝试多种可能的字段名
    return userInfo.collector_id || userInfo.collectorId || userInfo.id || null
  }
  
  // 3. 默认返回null，等待用户选择
  return null
}

const collectorId = ref(getCollectorId())

// 筛选表单
// 默认时间范围：过去7天
const getDefaultDateRange = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 7)
  return [startDate, endDate]
}

const filterForm = reactive({
  collectorIdSearch: '',
  agencyIds: [] as number[],
  teamIds: [] as number[],
  collectorIds: [] as number[],
  period: 'daily',
  dateRange: getDefaultDateRange(),
  queueIds: []
})

// 组织架构数据
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const collectors = ref<any[]>([])
const trendData = ref<any>(null)

// 数据
const collectorInfo = ref<any>({})
const performanceStats = ref<any>({})
const alerts = ref<any[]>([])
const cases = ref<any[]>([])
const communicationChart = ref<any>(null)

// 加载机构列表
const loadAgencies = async () => {
  const currentTenantId = tenantStore.currentTenantId
  if (!currentTenantId) {
    agencies.value = []
    return
  }

  try {
    const result = await getTenantAgencies(currentTenantId)
    agencies.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 加载小组列表
const loadTeams = async () => {
  if (filterForm.agencyIds.length === 0) {
    teams.value = []
    return
  }

  try {
    const allTeams: any[] = []
    for (const agencyId of filterForm.agencyIds) {
      const result = await getAgencyTeams(agencyId)
      const agencyTeams = Array.isArray(result) ? result : (result.data || [])
      allTeams.push(...agencyTeams)
    }
    teams.value = allTeams
  } catch (error) {
    console.error('加载小组失败：', error)
    ElMessage.error('加载小组失败')
  }
}

// 加载催员列表
const loadCollectors = async () => {
  try {
    let allCollectors: any[] = []
    
    if (filterForm.teamIds.length > 0) {
      // 如果选择了小组，加载这些小组的催员
      for (const teamId of filterForm.teamIds) {
        const result = await getTeamCollectors(teamId)
        const teamCollectors = Array.isArray(result) ? result : (result.data || [])
        allCollectors.push(...teamCollectors)
      }
    } else if (filterForm.agencyIds.length > 0) {
      // 如果只选择了机构，加载这些机构下的所有催员
      const result = await getCollectors({
        agency_id: filterForm.agencyIds[0],
        is_active: true
      })
      allCollectors = Array.isArray(result) ? result : (result.data || [])
    } else {
      // 如果没有选择，加载当前甲方下的所有催员
      const currentTenantId = tenantStore.currentTenantId
      if (currentTenantId) {
        const result = await getCollectors({
          is_active: true
        })
        allCollectors = Array.isArray(result) ? result : (result.data || [])
      }
    }
    
    // 去重
    collectors.value = allCollectors.filter((collector, index, self) =>
      index === self.findIndex(c => c.id === collector.id)
    )
    
    // 如果加载了催员列表且当前没有选择催员，默认选择第一个催员
    if (collectors.value.length > 0 && !collectorId.value && filterForm.collectorIds.length === 0) {
      const firstCollector = collectors.value[0]
      collectorId.value = firstCollector.id
      filterForm.collectorIds = [firstCollector.id]
      // 自动加载该催员的数据
      await loadPerformanceData()
    }
  } catch (error) {
    console.error('加载催员失败：', error)
    ElMessage.error('加载催员失败')
  }
}

// 机构变化处理
const handleAgencyChange = async () => {
  filterForm.teamIds = []
  filterForm.collectorIds = []
  await loadTeams()
  await loadCollectors()
}

// 小组变化处理
const handleTeamChange = async () => {
  filterForm.collectorIds = []
  await loadCollectors()
}

// 催员变化处理
const handleCollectorChange = () => {
  // 如果选择了催员，使用第一个催员ID
  if (filterForm.collectorIds.length > 0) {
    collectorId.value = filterForm.collectorIds[0]
    loadPerformanceData()
  }
}

// 催员ID搜索处理
const handleCollectorIdSearch = () => {
  const searchId = parseInt(filterForm.collectorIdSearch)
  if (isNaN(searchId) || searchId <= 0) {
    ElMessage.warning('请输入有效的催员ID')
    return
  }
  collectorId.value = searchId
  filterForm.collectorIds = [searchId]
  loadPerformanceData()
}

// 催员ID清除处理
const handleCollectorIdClear = () => {
  collectorId.value = getCollectorId()
  if (collectorId.value) {
    loadPerformanceData()
  }
}

// 加载绩效数据
const loadPerformanceData = async () => {
  // 检查collectorId是否有效
  if (!collectorId.value || collectorId.value <= 0) {
    // 如果没有选择催员，不显示错误，只是不加载数据
    return
  }

  try {
    const [startDate, endDate] = filterForm.dateRange
    
    // 确保日期格式正确
    if (!startDate || !endDate) {
      ElMessage.error('请选择日期范围')
      return
    }

    const params = {
      start_date: startDate.toISOString().split('T')[0],
      end_date: endDate.toISOString().split('T')[0],
      period: filterForm.period,
      queue_ids: filterForm.queueIds.length > 0 ? filterForm.queueIds.join(',') : undefined
    }

    console.log('加载绩效数据，催员ID:', collectorId.value, '参数:', params)
    
    const response = await getCollectorPerformance(collectorId.value, params)
    
    console.log('绩效数据响应:', response)
    
    if (response && response.data) {
      collectorInfo.value = response.data.collector_info || {}
      performanceStats.value = response.data.performance_stats || {}
      alerts.value = response.data.alerts || []
    } else if (response) {
      // 如果响应直接是数据对象
      collectorInfo.value = response.collector_info || {}
      performanceStats.value = response.performance_stats || {}
      alerts.value = response.alerts || []
    }

    // 加载趋势数据用于图表
    await loadTrendData()

    // 加载案件明细
    await loadCases()
    
    // 初始化图表
    await nextTick()
    initCharts()
  } catch (error: any) {
    console.error('加载绩效数据失败：', error)
    const errorMsg = error?.response?.data?.detail || error?.message || '加载绩效数据失败'
    ElMessage.error(errorMsg)
    
    // 如果是404错误，提示更友好的信息
    if (error?.response?.status === 404) {
      ElMessage.warning('API接口不存在，请确认后端服务已启动并注册了数据看板API路由')
    }
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!collectorId.value) return

  try {
    const [startDate, endDate] = filterForm.dateRange
    if (!startDate || !endDate) return
    
    const params: any = {
      start_date: startDate.toISOString().split('T')[0],
      end_date: endDate.toISOString().split('T')[0]
    }
    
    const response = await getCollectorTrend(collectorId.value, params)
    trendData.value = response.data || response
  } catch (error) {
    console.error('加载趋势数据失败：', error)
    trendData.value = null
  }
}

// 加载案件明细
const loadCases = async () => {
  try {
    const [startDate, endDate] = filterForm.dateRange
    const params = {
      start_date: startDate.toISOString().split('T')[0],
      end_date: endDate.toISOString().split('T')[0],
      skip: 0,
      limit: 100
    }

    const response = await getCollectorCases(collectorId.value, params)
    cases.value = response.data?.cases || []
  } catch (error) {
    console.error('加载案件明细失败：', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (!communicationChart.value) return

  const chart = echarts.init(communicationChart.value)
  
  // 如果有趋势数据，使用趋势数据；否则使用当前统计数据
  if (trendData.value && trendData.value.dates && trendData.value.dates.length > 0) {
    // 使用趋势数据，横轴为日期
    const dates = trendData.value.dates.map((date: string) => {
      // 格式化日期显示
      const d = new Date(date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })
    
    const option = {
      title: {
        text: '沟通覆盖与效率趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['本人电话沟通率', '本人WhatsApp沟通率', '本人总沟通率', '总联系人电话沟通率', '总联系人WhatsApp沟通率', '总联系人总沟通率'],
        bottom: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '本人电话沟通率',
          type: 'line',
          data: trendData.value.self_call_rate || [],
          smooth: true
        },
        {
          name: '本人WhatsApp沟通率',
          type: 'line',
          data: trendData.value.self_wa_contact_rate || [],
          smooth: true
        },
        {
          name: '本人总沟通率',
          type: 'line',
          data: trendData.value.self_contact_rate || [],
          smooth: true
        },
        {
          name: '总联系人电话沟通率',
          type: 'line',
          data: trendData.value.total_call_rate || [],
          smooth: true
        },
        {
          name: '总联系人WhatsApp沟通率',
          type: 'line',
          data: trendData.value.total_wa_contact_rate || [],
          smooth: true
        },
        {
          name: '总联系人总沟通率',
          type: 'line',
          data: trendData.value.total_contact_rate || [],
          smooth: true
        }
      ]
    }
    
    chart.setOption(option)
  } else {
    // 如果没有趋势数据，使用当前统计数据（降级方案）
    const option = {
      title: {
        text: '沟通覆盖率对比',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['本人沟通率', '总联系人沟通率'],
        bottom: 10
      },
      xAxis: {
        type: 'category',
        data: ['电话', 'WhatsApp', 'SMS']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '本人沟通率',
          type: 'bar',
          data: [
            performanceStats.value.self_call_rate || 0,
            performanceStats.value.self_wa_contact_rate || 0,
            performanceStats.value.self_contact_rate || 0
          ]
        },
        {
          name: '总联系人沟通率',
          type: 'bar',
          data: [
            performanceStats.value.total_call_rate || 0,
            performanceStats.value.total_wa_contact_rate || 0,
            performanceStats.value.total_contact_rate || 0
          ]
        }
      ]
    }
    
    chart.setOption(option)
  }
}

// 格式化货币
const formatCurrency = (amount: number | undefined) => {
  if (!amount) return '0.00'
  return Number(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 监听甲方变化
watch(() => tenantStore.currentTenantId, async (newTenantId) => {
  if (newTenantId) {
    await loadAgencies()
    // 重新加载催员列表，会自动选择第一个催员
    await loadCollectors()
  }
}, { immediate: true })

onMounted(async () => {
  // 加载组织架构数据
  await loadAgencies()
  await loadCollectors()
  
  // 如果有默认的催员ID（从路由或用户信息获取），加载数据
  // 否则loadCollectors会自动选择第一个催员并加载数据
  if (collectorId.value && filterForm.collectorIds.length === 0) {
    filterForm.collectorIds = [collectorId.value]
    await loadPerformanceData()
  }
})
</script>

<style scoped lang="scss">
.collector-performance {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;
  }

  .collector-info {
    h3 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: bold;
    }
    p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }

  .alert-card {
    margin-bottom: 20px;
  }

  .kpi-card {
    margin-bottom: 20px;
  }

  .kpi-item {
    text-align: center;
    padding: 20px 0;

    .kpi-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .kpi-value {
      font-size: 24px;
      font-weight: bold;
      color: #409EFF;
    }
  }

  .chart-card {
    margin-bottom: 20px;
  }

  .table-card {
    margin-bottom: 20px;
  }

  .card-header {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>

