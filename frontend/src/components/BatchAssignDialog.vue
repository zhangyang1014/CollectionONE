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

        <el-form-item label="所属系统">
          <el-select
            v-model="filters.limitSystem"
            placeholder="请选择系统"
            clearable
            filterable
            style="width: 150px"
            @change="applyLimitFilters"
          >
            <el-option v-for="item in limitOptions.systems" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="当期天数">
          <el-select
            v-model="filters.limitTermDay"
            placeholder="请选择天数"
            clearable
            filterable
            style="width: 150px"
            @change="applyLimitFilters"
          >
            <el-option v-for="item in limitOptions.termDays" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="产品">
          <el-select
            v-model="filters.limitProduct"
            placeholder="请选择产品"
            clearable
            filterable
            style="width: 150px"
            @change="applyLimitFilters"
          >
            <el-option v-for="item in limitOptions.products" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="APP">
          <el-select
            v-model="filters.limitApp"
            placeholder="请选择APP"
            clearable
            filterable
            style="width: 150px"
            @change="applyLimitFilters"
          >
            <el-option v-for="item in limitOptions.apps" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="商户">
          <el-select
            v-model="filters.limitMerchant"
            placeholder="请选择商户"
            clearable
            filterable
            style="width: 150px"
            @change="applyLimitFilters"
          >
            <el-option v-for="item in limitOptions.merchants" :key="item" :label="item" :value="item" />
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

      <!-- 当前小组限制提示 -->
      <div v-if="currentTeamLimits" class="limit-summary">
        <span class="summary-label">当前小组限制：</span>
        <span>商户：{{ formatLimitDisplay(currentTeamLimits.merchants) }}</span>
        <span>APP：{{ formatLimitDisplay(currentTeamLimits.apps) }}</span>
        <span>产品：{{ formatLimitDisplay(currentTeamLimits.products) }}</span>
        <span>队列：{{ formatLimitDisplay(currentTeamLimits.queues) }}</span>
      </div>

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
        <el-table-column label="限制范围" min-width="260">
          <template #default="{ row }">
            <div class="limit-cell">
              <div>商户：{{ formatLimitDisplay(getCollectorLimit(row, 'merchants')) }}</div>
              <div>APP：{{ formatLimitDisplay(getCollectorLimit(row, 'apps')) }}</div>
              <div>产品：{{ formatLimitDisplay(getCollectorLimit(row, 'products')) }}</div>
              <div>队列：{{ formatLimitDisplay(getCollectorLimit(row, 'queues')) }}</div>
            </div>
          </template>
        </el-table-column>
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

interface AssignCollector {
  id: number
  collectorName: string
  collectorCode: string
  agencyId?: number
  agencyName?: string
  teamId?: number
  teamName?: string
  queueId?: number
  queueName?: string
  currentCaseCount?: number
  status?: string
  allowedMerchants?: (string | number)[]
  allowedApps?: (string | number)[]
  allowedProducts?: (string | number)[]
  allowedQueues?: (string | number)[]
  allowedSystems?: (string | number)[]
  allowedTermDays?: (string | number)[]
}

const props = defineProps<{
  modelValue: boolean
  selectedCaseIds: number[]
  selectedCases?: any[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

const visible = ref(false)
const loading = ref(false)
const collectors = ref<AssignCollector[]>([])
const allCollectors = ref<AssignCollector[]>([])
const selectedCollectors = ref<AssignCollector[]>([])
const showQueueLimitDialog = ref(false)
const unmatchedItems = ref<any[]>([])

// 筛选条件
const filters = reactive({
  agencyId: null as number | null,
  teamId: null as number | null,
  queueId: null as number | null,
  searchKeyword: '',
  limitSystem: '',
  limitTermDay: '',
  limitProduct: '',
  limitApp: '',
  limitMerchant: ''
})

// 机构、小组、队列数据
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const queues = ref<any[]>([])

const normalizeToArray = (value: any): (string | number)[] => {
  if (Array.isArray(value)) return value.filter(v => v !== undefined && v !== null)
  if (value === null || value === undefined || value === '') return []
  return [value]
}

const teamLimitMap = computed(() => {
  const map = new Map<number, { merchants: (string | number)[]; apps: (string | number)[]; products: (string | number)[]; queues: (string | number)[]; systems?: (string | number)[]; termDays?: (string | number)[] }>()
  teams.value.forEach((team: any) => {
    const merchants = normalizeToArray(team.allowed_merchants || team.allowedMerchants)
    const apps = normalizeToArray(team.allowed_apps || team.allowedApps)
    const products = normalizeToArray(team.allowed_products || team.allowedProducts)
    const systems = normalizeToArray(team.allowed_systems || team.allowedSystems)
    const termDays = normalizeToArray(team.allowed_term_days || team.allowedTermDays)
    const queues = normalizeToArray(team.allowed_queues || team.allowedQueues || team.queue_name || team.queueName)
    if (team.id) {
      map.set(team.id, { merchants, apps, products, queues, systems, termDays })
    }
  })
  return map
})

const currentTeamLimits = computed(() => {
  if (!filters.teamId) return null
  return teamLimitMap.value.get(filters.teamId) || null
})

const limitOptions = computed(() => {
  const systems = new Set<string>()
  const termDays = new Set<string>()
  const products = new Set<string>()
  const apps = new Set<string>()
  const merchants = new Set<string>()

  const addValues = (set: Set<string>, values: (string | number)[]) => {
    normalizeToArray(values).forEach(v => set.add(String(v)))
  }

  teamLimitMap.value.forEach((limit) => {
    addValues(systems, limit.systems || [])
    addValues(termDays, limit.termDays || [])
    addValues(products, limit.products || [])
    addValues(apps, limit.apps || [])
    addValues(merchants, limit.merchants || [])
  })

  allCollectors.value.forEach((c) => {
    addValues(systems, (c as any).allowedSystems || [])
    addValues(termDays, (c as any).allowedTermDays || [])
    addValues(products, c.allowedProducts || [])
    addValues(apps, c.allowedApps || [])
    addValues(merchants, c.allowedMerchants || [])
  })

  return {
    systems: Array.from(systems),
    termDays: Array.from(termDays),
    products: Array.from(products),
    apps: Array.from(apps),
    merchants: Array.from(merchants)
  }
})

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
    allCollectors.value = (response || []).map((item: AssignCollector) => {
      const limitsFromTeam = item.teamId ? teamLimitMap.value.get(item.teamId) : undefined
      return {
        ...item,
        allowedMerchants: normalizeToArray(item.allowedMerchants || (limitsFromTeam && limitsFromTeam.merchants)),
        allowedApps: normalizeToArray(item.allowedApps || (limitsFromTeam && limitsFromTeam.apps)),
        allowedProducts: normalizeToArray(item.allowedProducts || (limitsFromTeam && limitsFromTeam.products)),
        allowedQueues: normalizeToArray(item.allowedQueues || (limitsFromTeam && limitsFromTeam.queues) || item.queueName || item.queueId),
        allowedSystems: normalizeToArray((item as any).allowedSystems || (limitsFromTeam && (limitsFromTeam as any).systems)),
        allowedTermDays: normalizeToArray((item as any).allowedTermDays || (limitsFromTeam && (limitsFromTeam as any).termDays))
      }
    })
    applyLimitFilters()
  } catch (error) {
    console.error('获取催员列表失败:', error)
    ElMessage.error('获取催员列表失败')
    allCollectors.value = []
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

  // 校验案件与催员限制是否匹配
  if (!validateCaseCollectorMatch()) {
    ElMessage.warning('您的案件属性和催员属性不匹配，请精准筛选案件和催员后，再批量分配')
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

const getCollectorLimit = (row: AssignCollector, key: 'merchants' | 'apps' | 'products' | 'queues') => {
  const directMap: Record<typeof key, (string | number)[] | undefined> = {
    merchants: row.allowedMerchants,
    apps: row.allowedApps,
    products: row.allowedProducts,
    queues: row.allowedQueues
  }
  const direct = normalizeToArray(directMap[key])
  if (direct.length) return direct
  const fallback = row.teamId ? teamLimitMap.value.get(row.teamId) : undefined
  return fallback ? fallback[key] : []
}

const formatLimitDisplay = (values: (string | number)[]) => {
  const arr = normalizeToArray(values)
  return arr.length ? arr.join('、') : '全部'
}

const matchesLimit = (selected: string | number | null | undefined, list: (string | number)[]) => {
  if (!selected) return true
  const arr = normalizeToArray(list)
  if (!arr.length) return true
  return arr.map(String).includes(String(selected))
}

const applyLimitFilters = () => {
  collectors.value = allCollectors.value.filter((item) => {
    return (
      matchesLimit(filters.limitSystem, (item as any).allowedSystems) &&
      matchesLimit(filters.limitTermDay, (item as any).allowedTermDays) &&
      matchesLimit(filters.limitProduct, item.allowedProducts) &&
      matchesLimit(filters.limitApp, item.allowedApps) &&
      matchesLimit(filters.limitMerchant, item.allowedMerchants)
    )
  })
}

watch(
  () => [filters.limitSystem, filters.limitTermDay, filters.limitProduct, filters.limitApp, filters.limitMerchant],
  () => applyLimitFilters()
)

// 校验案件与催员的限制属性匹配性
const validateCaseCollectorMatch = (): boolean => {
  const cases = Array.isArray(props.selectedCases) ? props.selectedCases : []
  if (!cases.length) return true

  const getCaseValue = (row: any, keys: string[]) => {
    for (const k of keys) {
      const val = row?.[k]
      if (val !== undefined && val !== null && val !== '') return val
    }
    return ''
  }

  const getCollectorLimits = (row: AssignCollector) => {
    const fallback = row.teamId ? teamLimitMap.value.get(row.teamId) : undefined
    return {
      systems: normalizeToArray((row as any).allowedSystems || (fallback as any)?.systems),
      termDays: normalizeToArray((row as any).allowedTermDays || (fallback as any)?.termDays),
      products: normalizeToArray(row.allowedProducts || fallback?.products),
      apps: normalizeToArray(row.allowedApps || fallback?.apps),
      merchants: normalizeToArray(row.allowedMerchants || fallback?.merchants),
      queues: normalizeToArray(row.allowedQueues || fallback?.queues || row.queueId || row.queueName)
    }
  }

  const valueMatches = (value: any, allowed: (string | number)[]) => {
    if (!allowed || allowed.length === 0) return true
    if (value === undefined || value === null || value === '') return true
    return allowed.map(String).includes(String(value))
  }

  for (const c of selectedCollectors.value) {
    const limits = getCollectorLimits(c)
    for (const caseItem of cases) {
      const systemVal = getCaseValue(caseItem, ['system_name', 'systemName'])
      if (!valueMatches(systemVal, limits.systems)) return false

      const termVal = getCaseValue(caseItem, ['term_days', 'termDays'])
      if (!valueMatches(termVal, limits.termDays)) return false

      const productVal = getCaseValue(caseItem, ['product_name', 'productName'])
      if (!valueMatches(productVal, limits.products)) return false

      const appVal = getCaseValue(caseItem, ['app_name', 'appName'])
      if (!valueMatches(appVal, limits.apps)) return false

      const merchantVal = getCaseValue(caseItem, ['merchant_name', 'merchantName'])
      if (!valueMatches(merchantVal, limits.merchants)) return false

      const queueVal = getCaseValue(caseItem, ['queue_id', 'queueId', 'queue_id_fk'])
      if (!valueMatches(queueVal, limits.queues)) return false
    }
  }

  return true
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
  filters.limitSystem = ''
  filters.limitTermDay = ''
  filters.limitProduct = ''
  filters.limitApp = ''
  filters.limitMerchant = ''
}
</script>

<style scoped>
.batch-assign-container {
  padding: 0;
}

.filter-form {
  margin-bottom: 16px;
}

.filter-form :deep(.el-form-item) {
  margin-right: 12px;
  margin-bottom: 12px;
}

.limit-summary {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.summary-label {
  font-weight: 600;
  color: #303133;
}

.limit-cell {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 12px;
  line-height: 1.4;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

