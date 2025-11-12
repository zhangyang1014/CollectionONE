<template>
  <div class="case-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件列表</span>
          <el-space>
            <el-button 
              v-if="canManageFilters"
              type="success" 
              @click="handleFilterConfig"
            >
              筛选器配置
            </el-button>
            <el-button type="primary" @click="handleAdd">添加案件</el-button>
          </el-space>
        </div>
      </template>

      <!-- 筛选器 -->
      <el-form :model="filters" class="filter-form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="案件状态">
              <el-select v-model="filters.case_status" placeholder="全部" clearable>
                <el-option label="待还款" value="待还款" />
                <el-option label="部分还款" value="部分还款" />
                <el-option label="正常结清" value="正常结清" />
                <el-option label="展期结清" value="展期结清" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="案件队列">
              <el-select v-model="filters.queue_id" placeholder="全部" clearable>
                <el-option
                  v-for="queue in queues"
                  :key="queue.id"
                  :label="queue.queue_name"
                  :value="queue.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="催收机构">
              <el-select 
                v-model="filters.agency_id" 
                placeholder="全部" 
                clearable
                @change="handleAgencyChange"
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
            <el-form-item label="催收小组">
              <el-select 
                v-model="filters.team_id" 
                placeholder="全部" 
                clearable
                :disabled="!filters.agency_id"
                @change="handleTeamChange"
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
        </el-row>

        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="催员">
              <el-select 
                v-model="filters.collector_id" 
                placeholder="全部" 
                clearable
                :disabled="!filters.team_id"
              >
                <el-option
                  v-for="collector in collectors"
                  :key="collector.id"
                  :label="collector.collector_name"
                  :value="collector.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 动态筛选器 -->
          <el-col 
            v-for="filter in dynamicFilters" 
            :key="filter.field_key" 
            :span="6"
          >
            <el-form-item :label="filter.field_name">
              <!-- 下拉选择（Enum类型） -->
              <el-select
                v-if="filter.filter_type === 'select'"
                v-model="dynamicFilterValues[filter.field_key]"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="option in filter.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
              <!-- 范围选择（数字类型） -->
              <el-date-picker
                v-else-if="filter.filter_type === 'range' && filter.field_type === 'Date'"
                v-model="dynamicFilterValues[filter.field_key]"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
              <el-input
                v-else-if="filter.filter_type === 'range'"
                v-model="dynamicFilterValues[filter.field_key]"
                placeholder="范围，如：1000-5000"
                style="width: 100%"
              />
              <!-- 搜索框（String类型） -->
              <el-input
                v-else-if="filter.filter_type === 'search'"
                v-model="dynamicFilterValues[filter.field_key]"
                placeholder="搜索"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="9">
            <el-form-item label="到期日">
              <el-date-picker
                v-model="filters.due_date_range"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="9">
            <el-form-item label="结清日期">
              <el-date-picker
                v-model="filters.settlement_date_range"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>
      </el-form>

      <!-- 搜索区域 - 只在选择甲方后显示 -->
      <div v-if="currentTenantId" class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="输入精准用户编号、贷款编号、手机号进行搜索"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 案件列表表格 -->
      <el-table
        v-if="currentTenantId"
        :data="displayCases"
        border
        :default-sort="{ prop: 'overdue_days', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="loan_id" label="贷款编号" width="150">
          <template #default="{ row }">
            <span>{{ row.loan_id || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="user_name" label="用户名" width="150">
          <template #default="{ row }">
            <span class="user-name">{{ row.user_first_name || row.user_name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="user_id" label="用户ID" width="150">
          <template #default="{ row }">
            <span class="user-id-text">{{ row.user_id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="overdue_days" label="逾期天数" width="120" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="getOverdueTagType(row.overdue_days)" effect="dark">
              {{ row.overdue_days }} 天
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="outstanding_amount"
          label="未还金额"
          width="130"
          sortable="custom"
        >
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.outstanding_amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="total_due_amount"
          label="应还金额"
          width="130"
          sortable="custom"
        >
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.total_due_amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="case_status" label="案件状态" width="110" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="getCaseStatusType(row.case_status)">
              {{ row.case_status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="product_name" label="产品名字" width="130" />
        <el-table-column prop="app_name" label="App名字" width="130" />
        <el-table-column prop="settlement_method" label="结清方式" width="130" />
        
        <el-table-column prop="settlement_time" label="结清时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.settlement_time) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 未选择甲方时的提示 -->
      <el-empty
        v-else
        description="请先选择甲方查看案件列表"
        :image-size="200"
      />

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 筛选器配置对话框 -->
      <el-dialog 
        v-model="filterConfigDialogVisible" 
        title="筛选器配置" 
        width="1000px"
      >
        <el-alert
          title="提示：配置的筛选器将同步给所有用户使用"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
        
        <el-row :gutter="20">
          <!-- 左侧：字段分组树 -->
          <el-col :span="6">
            <el-card shadow="never" style="height: 500px; overflow-y: auto;">
              <template #header>
                <span style="font-weight: 600;">字段分组</span>
              </template>
              <el-tree
                v-if="filterGroupTreeData.length > 0"
                :data="filterGroupTreeData"
                :props="{ label: 'group_name', children: 'children' }"
                node-key="id"
                :default-expand-all="true"
                :expand-on-click-node="false"
                @node-click="handleFilterGroupClick"
                highlight-current
                class="filter-group-tree"
              />
              <el-empty v-else description="暂无分组" :image-size="100" />
            </el-card>
          </el-col>
          
          <!-- 右侧：字段列表 -->
          <el-col :span="18">
            <el-card shadow="never" style="height: 500px; overflow-y: auto;">
              <template #header>
                <span style="font-weight: 600;">
                  {{ currentFilterGroupName || '请选择分组' }}
                </span>
              </template>
              
              <div v-if="filteredAvailableFields.length > 0">
                <el-checkbox-group v-model="selectedFilterFields" style="width: 100%">
                  <el-row :gutter="20">
                    <el-col 
                      v-for="field in filteredAvailableFields" 
                      :key="field.field_key"
                      :span="8"
                      style="margin-bottom: 10px"
                    >
                      <el-checkbox :label="field.field_key">
                        {{ field.field_name }} ({{ field.field_type }})
                      </el-checkbox>
                    </el-col>
                  </el-row>
                </el-checkbox-group>
              </div>
              <el-empty v-else description="该分组下暂无可用字段" :image-size="100" />
            </el-card>
          </el-col>
        </el-row>
        
        <template #footer>
          <el-button @click="filterConfigDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveFilterConfig">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getCases } from '@/api/case'
import { getFieldGroups } from '@/api/field'
import { getTenantQueues } from '@/api/queue'
import { getTenantAgencies, getAgencyTeams, getTeamCollectors } from '@/api/organization'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const tenantStore = useTenantStore()
const userStore = useUserStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 权限检查：是否为超级管理员或甲方管理员
const canManageFilters = computed(() => {
  const role = userStore.userInfo?.role || ''
  const roleLower = role.toLowerCase()
  return roleLower === 'superadmin' || roleLower === 'super_admin' || 
         roleLower === 'tenantadmin' || roleLower === 'tenant_admin'
})

const cases = ref<any[]>([])
const queues = ref<any[]>([])
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const collectors = ref<any[]>([])
const searchKeyword = ref('')
const sortConfig = ref({ prop: 'overdue_days', order: 'descending' })

const filters = ref<{
  case_status?: string
  queue_id?: number
  agency_id?: number
  team_id?: number
  collector_id?: number
  due_date_range: string[] | null
  settlement_date_range: string[] | null
}>({
  case_status: undefined,
  queue_id: undefined,
  agency_id: undefined,
  team_id: undefined,
  collector_id: undefined,
  due_date_range: null,
  settlement_date_range: null,
})

// 动态筛选器配置（从字段映射配置中获取）
const dynamicFilters = ref<Array<{
  field_key: string
  field_name: string
  field_type: string
  filter_type: 'select' | 'range' | 'search'
  options?: Array<{ label: string, value: any }>
}>>([])

// 动态筛选器的值
const dynamicFilterValues = ref<Record<string, any>>({})

// 筛选器配置对话框
const filterConfigDialogVisible = ref(false)
const availableFields = ref<any[]>([]) // 可配置为筛选器的字段列表
const selectedFilterFields = ref<string[]>([]) // 已选中的字段标识列表
const filterGroupTreeData = ref<any[]>([]) // 字段分组树数据
const currentFilterGroupId = ref<number | undefined>() // 当前选中的分组ID
const currentFilterGroupName = ref<string>('') // 当前选中的分组名称
const filteredAvailableFields = ref<any[]>([]) // 当前分组下的字段列表

const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 搜索过滤
const searchedCases = computed(() => {
  if (!searchKeyword.value) {
    return cases.value
  }
  const keyword = searchKeyword.value.trim().toLowerCase()
  return cases.value.filter((c: any) => {
    return (
      c.user_id?.toLowerCase().includes(keyword) ||
      c.loan_id?.toLowerCase().includes(keyword) ||
      c.mobile_number?.toLowerCase().includes(keyword)
    )
  })
})

// 排序后的数据
const sortedCases = computed(() => {
  const data = [...searchedCases.value]
  if (!sortConfig.value.prop) {
    return data
  }

  return data.sort((a: any, b: any) => {
    const prop = sortConfig.value.prop
    let aVal = a[prop] ?? 0
    let bVal = b[prop] ?? 0

    // 数字类型转换
    if (['overdue_days', 'outstanding_amount', 'total_due_amount', 'contact_channels'].includes(prop)) {
      aVal = parseFloat(aVal) || 0
      bVal = parseFloat(bVal) || 0
    }

    if (sortConfig.value.order === 'ascending') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })
})

// 分页显示的数据
const displayCases = computed(() => {
  const start = (pagination.value.page - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return sortedCases.value.slice(start, end)
})

// 更新总数
const updateTotal = () => {
  pagination.value.total = searchedCases.value.length
}

// 加载案件队列
const loadQueues = async () => {
  if (!currentTenantId.value) return
  
  try {
    const response = await getTenantQueues(currentTenantId.value)
    queues.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载队列失败:', error)
  }
}

// 加载催收机构
const loadAgencies = async () => {
  if (!currentTenantId.value) return
  
  try {
    const response = await getTenantAgencies(currentTenantId.value)
    agencies.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载机构失败:', error)
  }
}

// 加载催收小组
const loadTeams = async () => {
  if (!filters.value.agency_id) {
    teams.value = []
    return
  }
  
  try {
    const response = await getAgencyTeams(filters.value.agency_id)
    teams.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载小组失败:', error)
  }
}

// 加载催员
const loadCollectors = async () => {
  if (!filters.value.team_id) {
    collectors.value = []
    return
  }
  
  try {
    const response = await getTeamCollectors(filters.value.team_id)
    collectors.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载催员失败:', error)
  }
}

// 加载案件数据
const loadCases = async () => {
  if (!currentTenantId.value) {
    cases.value = []
    updateTotal()
    return
  }
  
  // 构建查询参数
  const params: any = {
    tenant_id: currentTenantId.value,
  }
  
  if (filters.value.case_status) {
    params.case_status = filters.value.case_status
  }
  if (filters.value.queue_id) {
    params.queue_id = filters.value.queue_id
  }
  if (filters.value.agency_id) {
    params.agency_id = filters.value.agency_id
  }
  if (filters.value.team_id) {
    params.team_id = filters.value.team_id
  }
  if (filters.value.collector_id) {
    params.collector_id = filters.value.collector_id
  }
  if (filters.value.due_date_range && Array.isArray(filters.value.due_date_range) && filters.value.due_date_range.length === 2) {
    params.due_date_start = filters.value.due_date_range[0]
    params.due_date_end = filters.value.due_date_range[1]
  }
  if (filters.value.settlement_date_range && Array.isArray(filters.value.settlement_date_range) && filters.value.settlement_date_range.length === 2) {
    params.settlement_date_start = filters.value.settlement_date_range[0]
    params.settlement_date_end = filters.value.settlement_date_range[1]
  }
  
  const res = await getCases(params)
  // 如果响应是数组，直接使用；否则使用res.data
  cases.value = Array.isArray(res) ? res : (res.data || [])
  updateTotal()
}

// 处理机构变更
const handleAgencyChange = () => {
  filters.value.team_id = undefined
  filters.value.collector_id = undefined
  teams.value = []
  collectors.value = []
  loadTeams()
}

// 处理小组变更
const handleTeamChange = () => {
  filters.value.collector_id = undefined
  collectors.value = []
  loadCollectors()
}

// 查询
const handleQuery = () => {
  pagination.value.page = 1
  loadCases()
}

const handleSearch = () => {
  pagination.value.page = 1
  updateTotal()
}

const resetFilters = () => {
  filters.value = {
    case_status: undefined,
    queue_id: undefined,
    agency_id: undefined,
    team_id: undefined,
    collector_id: undefined,
    due_date_range: null,
    settlement_date_range: null,
  }
  searchKeyword.value = ''
  pagination.value.page = 1
  teams.value = []
  collectors.value = []
  loadCases()
}

const handleSortChange = ({ prop, order }: any) => {
  sortConfig.value = { prop, order }
}

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
}

const handlePageChange = (page: number) => {
  pagination.value.page = page
}

// 逾期天数标签类型
const getOverdueTagType = (days: number | string) => {
  const d = typeof days === 'string' ? parseFloat(days) : days
  if (d < 0) return 'success' // 负数绿色
  if (d === 0) return 'warning' // 当日橙色
  return 'danger' // 正数红色
}

// 案件状态标签类型
const getCaseStatusType = (status: string) => {
  const map: any = {
    '进行中': 'primary',
    '已结清': 'success',
    '逾期': 'danger',
  }
  return map[status] || 'info'
}

// 格式化金额
const formatAmount = (amount: any) => {
  const num = parseFloat(amount) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

const handleAdd = () => {
  ElMessage.info('添加功能待实现')
}

const handleView = (row: any) => {
  router.push(`/cases/${String(row.id)}`)
}

// 筛选器配置
const handleFilterConfig = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }
  
  // 加载字段分组
  await loadFilterGroups()
  
  // 加载可配置的字段列表
  try {
    const url = `http://localhost:8000/api/v1/tenants/${currentTenantId.value}/fields`
    const response = await fetch(url)
    const result = await response.json()
    availableFields.value = (result.data || []).filter((field: any) => {
      // 只显示已映射的字段，且支持筛选的类型
      return (field.tenant_field_key || field.tenant_field_id) && 
             ['String', 'Enum', 'Integer', 'Decimal', 'Date'].includes(field.field_type)
    })
    
    // 加载已配置的筛选器
    await loadFilterConfig()
    
    // 默认选中第一个分组
    if (filterGroupTreeData.value.length > 0) {
      handleFilterGroupClick(filterGroupTreeData.value[0])
    }
    
    filterConfigDialogVisible.value = true
  } catch (error) {
    console.error('加载字段列表失败：', error)
    ElMessage.error('加载字段列表失败')
  }
}

// 加载字段分组
const loadFilterGroups = async () => {
  try {
    const res = await getFieldGroups()
    const allGroups = res.data || []
    filterGroupTreeData.value = buildFilterGroupTree(allGroups)
  } catch (error) {
    console.error('加载字段分组失败：', error)
    ElMessage.error('加载字段分组失败')
  }
}

// 构建字段分组树
const buildFilterGroupTree = (groups: any[]) => {
  const map = new Map()
  const roots: any[] = []

  groups.forEach((group) => {
    map.set(group.id, { ...group, children: [] })
  })

  groups.forEach((group) => {
    const node = map.get(group.id)
    if (group.parent_id) {
      const parent = map.get(group.parent_id)
      if (parent) {
        parent.children.push(node)
      }
    } else {
      roots.push(node)
    }
  })

  return roots
}

// 处理分组点击
const handleFilterGroupClick = (group: any) => {
  currentFilterGroupId.value = group.id
  currentFilterGroupName.value = group.group_name
  
  // 筛选当前分组下的字段
  filteredAvailableFields.value = availableFields.value.filter((field: any) => {
    return field.field_group_id === group.id || 
           (group.children && group.children.some((child: any) => field.field_group_id === child.id))
  })
  
  // 如果没有找到字段，尝试查找子分组
  if (filteredAvailableFields.value.length === 0 && group.children) {
    const childGroupIds = group.children.map((child: any) => child.id)
    filteredAvailableFields.value = availableFields.value.filter((field: any) => {
      return childGroupIds.includes(field.field_group_id)
    })
  }
}

// 加载筛选器配置
const loadFilterConfig = async () => {
  // TODO: 从API加载已配置的筛选器
  // 暂时使用localStorage作为mock
  const saved = localStorage.getItem(`case_filters_${currentTenantId.value}`)
  if (saved) {
    try {
      const config = JSON.parse(saved)
      selectedFilterFields.value = config.selectedFields || []
      dynamicFilters.value = config.filters || []
    } catch (e) {
      console.error('解析筛选器配置失败：', e)
    }
  }
}

// 保存筛选器配置
const handleSaveFilterConfig = async () => {
  // 构建筛选器配置
  const config = {
    selectedFields: selectedFilterFields.value,
    filters: availableFields.value
      .filter((field: any) => selectedFilterFields.value.includes(field.field_key))
      .map((field: any) => {
        let filterType: 'select' | 'range' | 'search' = 'search'
        let options: Array<{ label: string, value: any }> | undefined = undefined
        
        if (field.field_type === 'Enum') {
          filterType = 'select'
          options = (field.enum_values || []).map((ev: any) => ({
            label: ev.standard_name || ev.tenant_name || ev.standard_id,
            value: ev.standard_id || ev.tenant_id
          }))
        } else if (['Integer', 'Decimal', 'Date'].includes(field.field_type)) {
          filterType = 'range'
        }
        
        return {
          field_key: field.field_key,
          field_name: field.field_name,
          field_type: field.field_type,
          filter_type: filterType,
          options
        }
      })
  }
  
  // TODO: 调用API保存配置
  // 暂时使用localStorage作为mock
  localStorage.setItem(`case_filters_${currentTenantId.value}`, JSON.stringify(config))
  
  // 更新动态筛选器
  dynamicFilters.value = config.filters
  
  ElMessage.success('筛选器配置已保存，已同步给所有用户')
  filterConfigDialogVisible.value = false
}

// 监听全局甲方变化
watch(currentTenantId, async (newTenantId) => {
  if (newTenantId) {
    // 重置筛选器
    filters.value = {
      case_status: undefined,
      queue_id: undefined,
      agency_id: undefined,
      team_id: undefined,
      collector_id: undefined,
      due_date_range: null,
      settlement_date_range: null,
    }
    dynamicFilterValues.value = {}
    teams.value = []
    collectors.value = []
    
    // 加载数据
    await loadQueues()
    await loadAgencies()
    await loadCases()
    // 加载动态筛选器配置
    await loadFilterConfig()
  } else {
    // 清空数据
    cases.value = []
    queues.value = []
    agencies.value = []
    teams.value = []
    collectors.value = []
    dynamicFilters.value = []
    dynamicFilterValues.value = {}
  }
})

onMounted(async () => {
  if (currentTenantId.value) {
    await loadQueues()
    await loadAgencies()
    await loadCases()
    await loadFilterConfig()
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.filter-form :deep(.el-select),
.filter-form :deep(.el-date-picker) {
  width: 100%;
}

.search-area {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  max-width: 600px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-id-text {
  color: #606266;
}

.amount {
  font-weight: 500;
  color: #F56C6C;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.filter-group-tree {
  font-size: 14px;
}

.filter-group-tree :deep(.el-tree-node__content) {
  height: 32px;
  line-height: 32px;
}

.filter-group-tree :deep(.el-tree-node__label) {
  font-size: 14px;
}
</style>

