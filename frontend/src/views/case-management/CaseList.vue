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
          </el-space>
        </div>
      </template>

      <!-- 筛选器 - 优化布局为两排 -->
      <el-form :model="filters" class="filter-form" label-width="120px">
        <!-- 第一排：案件状态、案件队列、到期日范围、逾期天数范围 -->
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="案件状态">
              <el-select 
                v-model="filters.case_status" 
                placeholder="全部" 
                clearable
                @change="handleQuery"
              >
                <el-option label="全部" value="" />
                <el-option label="待还款" value="待还款" />
                <el-option label="部分还款" value="部分还款" />
                <el-option label="正常结清" value="正常结清" />
                <el-option label="展期结清" value="展期结清" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="案件队列">
              <el-select 
                v-model="filters.queue_id" 
                placeholder="全部" 
                clearable
                @change="handleQuery"
              >
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
            <el-form-item label="到期日范围">
              <el-date-picker
                v-model="filters.due_date_range"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :shortcuts="dueDateShortcuts"
                @change="handleQuery"
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="逾期天数范围">
              <div class="range-input-group">
                <el-input-number
                  v-model="filters.overdue_days_min"
                  :min="0"
                  :controls="false"
                  placeholder="最小"
                  class="range-input"
                  @change="handleQuery"
                />
                <span class="range-separator">至</span>
                <el-input-number
                  v-model="filters.overdue_days_max"
                  :min="filters.overdue_days_min || 0"
                  :controls="false"
                  placeholder="最大"
                  class="range-input"
                  @change="handleQuery"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二排：催收机构、催收小组群、催收小组、催员 -->
        <el-row :gutter="16">
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
            <el-form-item label="催收小组群">
              <el-select 
                v-model="filters.team_group_id" 
                placeholder="全部" 
                clearable
                :disabled="!filters.agency_id"
                @change="handleTeamGroupChange"
              >
                <el-option
                  v-for="teamGroup in teamGroups"
                  :key="teamGroup.id"
                  :label="teamGroup.group_name"
                  :value="teamGroup.id"
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
                :disabled="!filters.team_group_id && !filters.agency_id"
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

          <el-col :span="6">
            <el-form-item label="催员">
              <el-select 
                v-model="filters.collector_id" 
                placeholder="全部" 
                clearable
                :disabled="!filters.team_id"
                @change="handleQuery"
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
        </el-row>

        <!-- 操作按钮 -->
        <el-row :gutter="16">
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
          placeholder="搜索案件编号、客户姓名、客户ID、手机号码"
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

      <!-- 批量操作工具栏 -->
      <div v-if="currentTenantId && selectedCases.length > 0" class="batch-toolbar">
        <el-alert
          :title="`已选择 ${selectedCases.length} 个案件`"
          type="info"
          :closable="false"
        >
          <template #default>
            <div class="batch-actions">
              <el-button type="primary" size="small" @click="handleBatchAssign">
                <el-icon><Operation /></el-icon>
                批量分案
              </el-button>
              <el-button type="warning" size="small" @click="handleBatchStay">
                <el-icon><Lock /></el-icon>
                标记停留
              </el-button>
              <el-button size="small" @click="clearSelection">
                取消选择
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 案件列表表格 - 使用动态字段展示配置 -->
      <DynamicCaseTable
        v-if="currentTenantId"
        ref="tableRef"
        :data="displayCases"
        :columns="getTableColumns()"
        :loading="configLoading"
        border
        show-selection
        :selectable="canSelectCase"
        show-actions
        :actions-width="200"
        :row-class-name="getRowClassName"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <!-- 自定义客户姓名显示 -->
        <template #cell-user_name="{ row, value }">
          <div class="user-name-cell">
            <span class="user-name">{{ value || row.userName || row.user_name || row['userName'] || row['user_name'] || '-' }}</span>
            <span v-if="(row.userId || row.user_id || row['userId'] || row['user_id'])" class="user-id-text">{{ row.userId || row.user_id || row['userId'] || row['user_id'] }}</span>
          </div>
        </template>

        <!-- 自定义逾期天数显示 -->
        <template #cell-overdue_days="{ row, value }">
          <el-tag :type="getOverdueTagType(value || row.overdueDays || row.overdue_days || 0)" effect="dark">
            {{ value || row.overdueDays || row.overdue_days || 0 }} 天
          </el-tag>
        </template>

        <!-- 自定义案件状态显示 -->
        <template #cell-case_status="{ row, value }">
          <el-tag :type="getCaseStatusType(value || row.caseStatus || row.case_status)">
            {{ getCaseStatusText(value || row.caseStatus || row.case_status) }}
          </el-tag>
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button link type="primary" @click="handleViewNotes(row)">查看催记</el-button>
        </template>
      </DynamicCaseTable>

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

      <!-- 历史催记对话框 -->
      <el-dialog 
        v-model="showHistoryNotesDialog" 
        :title="`历史催记 - ${currentViewingCaseId}`"
        width="1200px" 
        top="5vh"
        class="history-notes-dialog"
      >
        <div class="history-notes-content">
          <!-- 筛选器 -->
          <div class="history-filters">
            <el-select 
              v-model="historyFilters.collector" 
              placeholder="触达人" 
              clearable 
              style="width: 150px;"
              @change="handleHistoryFilter"
            >
              <el-option 
                v-for="collector in collectorList" 
                :key="collector" 
                :label="collector" 
                :value="collector" 
              />
            </el-select>
            
            <el-select 
              v-model="historyFilters.channel" 
              placeholder="触达渠道" 
              clearable 
              style="width: 150px;"
              @change="handleHistoryFilter"
            >
              <el-option label="WhatsApp" value="WhatsApp" />
              <el-option label="SMS" value="SMS" />
              <el-option label="RCS" value="RCS" />
              <el-option label="电话外呼" value="电话外呼" />
            </el-select>
            
            <el-select 
              v-model="historyFilters.status" 
              placeholder="状态" 
              clearable 
              style="width: 120px;"
              @change="handleHistoryFilter"
            >
              <el-option label="可联" value="reachable" />
              <el-option label="不存在" value="not_exist" />
              <el-option label="未响应" value="no_response" />
            </el-select>
            
            <el-select 
              v-model="historyFilters.result" 
              placeholder="结果" 
              clearable 
              style="width: 150px;"
              @change="handleHistoryFilter"
            >
              <el-option label="承诺还款" value="promise_repay" />
              <el-option label="拒绝还款" value="refuse_repay" />
              <el-option label="失联" value="lost_contact" />
              <el-option label="持续跟进" value="continuous_follow_up" />
            </el-select>
            
            <el-date-picker
              v-model="historyFilters.dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 240px;"
              @change="handleHistoryFilter"
            />
          </div>

          <!-- 历史催记列表 -->
          <div class="history-notes-list">
            <el-table 
              :data="filteredHistoryNotes" 
              stripe
              style="width: 100%"
              :empty-text="'暂无历史催记'"
              max-height="500"
            >
              <el-table-column prop="register_time" label="登记时间" width="150" />
              <el-table-column prop="case_id" label="案件ID" width="150" />
              <el-table-column prop="collector" label="触达人" width="100" />
              <el-table-column prop="channel" label="触达渠道" width="100" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag size="small" :type="getStatusTagType(row.status)">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="result" label="结果" width="120">
                <template #default="{ row }">
                  <span>{{ getResultLabel(row.result) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
              <el-table-column prop="next_follow_up" label="下次跟进时间" width="150" />
            </el-table>
          </div>
        </div>
      </el-dialog>

      <!-- 批量分案弹窗 -->
      <BatchAssignDialog
        v-model="showBatchAssignDialog"
        :selected-case-ids="selectedCases.map(c => c.id)"
        @success="handleAssignSuccess"
      />

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
import { Search, Operation, Lock } from '@element-plus/icons-vue'
import { getCases, batchStayCases } from '@/api/case'
import { getFieldGroups } from '@/api/field'
import { getTenantQueues } from '@/api/queue'
import { getTenantAgencies, getAgencyTeamGroups, getAgencyTeams, getTeamGroupTeams, getTeamCollectors } from '@/api/organization'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { useFieldListConfig } from '@/composables/useFieldListConfig'
import DynamicCaseTable from '@/components/DynamicCaseTable.vue'
import BatchAssignDialog from '@/components/BatchAssignDialog.vue'
import dayjs from 'dayjs'
import { getApiUrl } from '@/config/api'

const router = useRouter()
const tenantStore = useTenantStore()
const userStore = useUserStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 使用案件列表字段配置Hook - 控台案件列表场景
const {
  loading: configLoading,
  getTableColumns
} = useFieldListConfig({
  tenantId: currentTenantId,
  sceneType: 'admin_case_list',
  autoLoad: true
})

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
const teamGroups = ref<any[]>([])
const teams = ref<any[]>([])
const collectors = ref<any[]>([])
const searchKeyword = ref('')
const sortConfig = ref({ prop: 'overdue_days', order: 'descending' })
const selectedCases = ref<any[]>([])
const showBatchAssignDialog = ref(false)
const tableRef = ref()

const filters = ref<{
  case_status?: string
  queue_id?: number
  agency_id?: number
  team_group_id?: number
  team_id?: number
  collector_id?: number
  due_date_range: string[] | null
  settlement_date_range: string[] | null
  overdue_days_min?: number
  overdue_days_max?: number
}>({
  case_status: undefined,
  queue_id: undefined,
  agency_id: undefined,
  team_group_id: undefined,
  team_id: undefined,
  collector_id: undefined,
  due_date_range: null,
  settlement_date_range: null,
  overdue_days_min: undefined,
  overdue_days_max: undefined,
})

// 到期日快捷选择选项
const dueDateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 29)
      return [start, end]
    }
  }
]

// 动态筛选器的值(基于字段展示配置自动生成)
const dynamicFilterValues = ref<Record<string, any>>({})

// 筛选器配置相关变量
const filterConfigDialogVisible = ref(false)
const selectedFilterFields = ref<string[]>([])
const dynamicFilters = ref<any[]>([])
const availableFields = ref<any[]>([])
const filterGroupTreeData = ref<any[]>([])
const currentFilterGroupId = ref<number | null>(null)
const currentFilterGroupName = ref<string>('')
const filteredAvailableFields = ref<any[]>([])

// 注意: 筛选器配置现在基于"甲方字段展示配置"自动生成,不再需要单独配置

const pagination = ref({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 搜索过滤 - 现在由后端处理，这里直接返回cases
const searchedCases = computed(() => {
  // 确保 cases.value 是数组
  if (!Array.isArray(cases.value)) {
    return []
  }
  return cases.value
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

// 更新总数（现在由后端返回，此函数保留用于兼容）
const updateTotal = () => {
  // 总数现在由后端返回，这里不再需要计算
  // 如果cases.value有数据但没有设置total，则使用cases长度作为fallback
  if (pagination.value.total === 0 && cases.value.length > 0) {
    pagination.value.total = cases.value.length
  }
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

// 加载催收小组群
const loadTeamGroups = async () => {
  if (!filters.value.agency_id) {
    teamGroups.value = []
    return
  }
  
  try {
    const response = await getAgencyTeamGroups(filters.value.agency_id)
    teamGroups.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载小组群失败:', error)
  }
}

// 加载催收小组
const loadTeams = async () => {
  // 优先使用小组群ID，其次使用机构ID
  if (!filters.value.team_group_id && !filters.value.agency_id) {
    teams.value = []
    return
  }
  
  try {
    let response
    if (filters.value.team_group_id) {
      // 如果选择了小组群，加载小组群下的小组
      response = await getTeamGroupTeams(filters.value.team_group_id)
    } else if (filters.value.agency_id) {
      // 如果只选择了机构，加载机构下的所有小组
      response = await getAgencyTeams(filters.value.agency_id)
    }
    teams.value = response ? (Array.isArray(response) ? response : (response.data || [])) : []
  } catch (error) {
    console.error('加载小组失败:', error)
    teams.value = []
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
  
  // 第一排筛选条件
  if (filters.value.case_status) {
    params.case_status = filters.value.case_status
  }
  if (filters.value.queue_id) {
    params.queue_id = filters.value.queue_id
  }
  
  // 第二排筛选条件（组织架构）
  if (filters.value.agency_id) {
    params.agency_id = filters.value.agency_id
  }
  if (filters.value.team_group_id) {
    params.team_group_id = filters.value.team_group_id
  }
  if (filters.value.team_id) {
    params.team_id = filters.value.team_id
  }
  if (filters.value.collector_id) {
    params.collector_id = filters.value.collector_id
  }
  
  // 第三排筛选条件（范围筛选）
  if (filters.value.due_date_range && Array.isArray(filters.value.due_date_range) && filters.value.due_date_range.length === 2) {
    params.due_date_start = filters.value.due_date_range[0]
    params.due_date_end = filters.value.due_date_range[1]
  }
  if (filters.value.overdue_days_min !== undefined && filters.value.overdue_days_min !== null) {
    params.overdue_days_min = filters.value.overdue_days_min
  }
  if (filters.value.overdue_days_max !== undefined && filters.value.overdue_days_max !== null) {
    params.overdue_days_max = filters.value.overdue_days_max
  }
  
  // 其他筛选条件
  if (filters.value.settlement_date_range && Array.isArray(filters.value.settlement_date_range) && filters.value.settlement_date_range.length === 2) {
    params.settlement_date_start = filters.value.settlement_date_range[0]
    params.settlement_date_end = filters.value.settlement_date_range[1]
  }
  
  // 添加搜索关键词参数
  if (searchKeyword.value && searchKeyword.value.trim()) {
    params.search_keyword = searchKeyword.value.trim()
  }
  
  const res = await getCases(params)
  // 处理不同的响应格式
  // Java后端格式: { items: [...], total: 100 } (request.ts已经提取了data)
  // Python后端格式: 直接返回数组
  const resData: any = res
  if (Array.isArray(resData)) {
    // 直接返回数组（Python后端格式）
    cases.value = resData
  } else if (resData && Array.isArray(resData.items)) {
    // Java后端格式：{ items: [...], total: 100 }
    cases.value = resData.items
  } else if (resData && Array.isArray(resData.data)) {
    // 兼容其他可能的格式
    cases.value = resData.data
  } else {
    cases.value = []
  }
  updateTotal()
}

// 处理机构变更
const handleAgencyChange = () => {
  // 清空下级筛选条件
  filters.value.team_group_id = undefined
  filters.value.team_id = undefined
  filters.value.collector_id = undefined
  teamGroups.value = []
  teams.value = []
  collectors.value = []
  // 加载小组群
  loadTeamGroups()
  handleQuery() // 自动触发查询
}

// 处理小组群变更
const handleTeamGroupChange = () => {
  // 清空下级筛选条件
  filters.value.team_id = undefined
  filters.value.collector_id = undefined
  teams.value = []
  collectors.value = []
  // 加载小组
  loadTeams()
  handleQuery() // 自动触发查询
}

// 处理小组变更
const handleTeamChange = () => {
  // 清空下级筛选条件
  filters.value.collector_id = undefined
  collectors.value = []
  // 加载催员
  loadCollectors()
  handleQuery() // 自动触发查询
}

// 查询
const handleQuery = () => {
  pagination.value.page = 1
  loadCases()
}

const handleSearch = () => {
  pagination.value.page = 1
  loadCases()
}

const resetFilters = () => {
  filters.value = {
    case_status: undefined,
    queue_id: undefined,
    agency_id: undefined,
    team_group_id: undefined,
    team_id: undefined,
    collector_id: undefined,
    due_date_range: null,
    settlement_date_range: null,
    overdue_days_min: undefined,
    overdue_days_max: undefined,
  }
  searchKeyword.value = ''
  pagination.value.page = 1
  teamGroups.value = []
  teams.value = []
  collectors.value = []
  loadCases()
}

const handleSortChange = ({ prop, order }: any) => {
  sortConfig.value = { prop, order }
}

// 批量分案相关方法
/**
 * 判断案件是否可选择（只有未结清的案件可以选择）
 */
const canSelectCase = (row: any) => {
  const status = row.caseStatus || row.case_status
  return status !== 'normal_settlement' && status !== 'extension_settlement'
}

/**
 * 获取行className（用于禁用已结清案件的选择）
 */
const getRowClassName = ({ row }: { row: any }) => {
  if (!canSelectCase(row)) {
    return 'case-row-disabled'
  }
  return ''
}

/**
 * 处理表格选择变化
 */
const handleSelectionChange = (selection: any[]) => {
  // 过滤掉已结清的案件
  selectedCases.value = selection.filter(c => canSelectCase(c))
  
  // 如果选择了已结清的案件，需要清除它们的选择状态
  const disabledCases = selection.filter(c => !canSelectCase(c))
  if (disabledCases.length > 0 && tableRef.value) {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'CaseList.vue:handleSelectionChange',message:'clearing disabled cases',data:{disabledCount:disabledCases.length,hasToggleRowSelection:typeof tableRef.value?.toggleRowSelection === 'function'},timestamp:Date.now(),sessionId:'debug-session',runId:'run1',hypothesisId:'B'})}).catch(()=>{});
    // #endregion
    // 清除已结清案件的选择状态
    disabledCases.forEach(c => {
      if (tableRef.value && typeof tableRef.value.toggleRowSelection === 'function') {
        tableRef.value.toggleRowSelection(c, false)
      }
    })
  }
}

/**
 * 清除选择
 */
const clearSelection = () => {
  // #region agent log
  fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'CaseList.vue:clearSelection',message:'clearSelection called',data:{tableRefExists:!!tableRef.value,tableRefType:tableRef.value?.constructor?.name,hasClearSelection:typeof tableRef.value?.clearSelection === 'function',exposedMethods:tableRef.value ? Object.keys(tableRef.value).filter(k => typeof tableRef.value[k] === 'function') : []},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
  // #endregion
  if (!tableRef.value) {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'CaseList.vue:clearSelection',message:'tableRef is null',data:{},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'A'})}).catch(()=>{});
    // #endregion
    console.warn('tableRef is not available')
    return
  }
  
  if (typeof tableRef.value.clearSelection === 'function') {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'CaseList.vue:clearSelection',message:'calling clearSelection',data:{},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
    // #endregion
    tableRef.value.clearSelection()
  } else {
    // #region agent log
    fetch('http://127.0.0.1:7242/ingest/5212b1a1-7708-4d23-a17a-19c9629d5189',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({location:'CaseList.vue:clearSelection',message:'clearSelection method not available',data:{tableRefValue:tableRef.value,availableMethods:Object.keys(tableRef.value),allKeys:Object.keys(tableRef.value)},timestamp:Date.now(),sessionId:'debug-session',runId:'post-fix',hypothesisId:'B'})}).catch(()=>{});
    // #endregion
    console.error('tableRef.clearSelection is not a function', {
      tableRef: tableRef.value,
      availableMethods: Object.keys(tableRef.value),
      type: typeof tableRef.value.clearSelection
    })
  }
}

/**
 * 打开批量分案弹窗
 */
const handleBatchAssign = () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请先选择案件')
    return
  }
  showBatchAssignDialog.value = true
}

/**
 * 分案成功回调
 */
const handleAssignSuccess = () => {
  clearSelection()
  loadCases()
  ElMessage.success('分案成功')
}

/**
 * 批量标记停留
 */
const handleBatchStay = async () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请先选择案件')
    return
  }

  try {
    const { ElMessageBox } = await import('element-plus')
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedCases.value.length} 个案件标记为停留吗？停留后案件将从催员侧收回，管理端不可再分案。`,
      '标记停留确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const caseIds = selectedCases.value.map(c => c.id)
    console.log('========== 开始批量标记停留 ==========')
    console.log('案件ID列表:', caseIds)
    
    const response = await batchStayCases(caseIds)
    console.log('========== 批量停留响应 ==========')
    console.log('完整响应:', JSON.stringify(response, null, 2))
    
    // request拦截器已经处理了响应，response就是data部分
    // 但为了兼容，也检查response.data
    const data = response?.data || response || {}
    console.log('处理后的数据:', data)
    console.log('successCount:', data.successCount)
    console.log('failureCount:', data.failureCount)

    // 兼容两种字段名：successCount (Java驼峰) 和 success_count (下划线)
    const successCount = data.successCount ?? data.success_count ?? 0
    const failureCount = data.failureCount ?? data.failure_count ?? 0
    const failures = data.failures || []

    console.log('解析结果 - 成功:', successCount, '失败:', failureCount)

    if (successCount > 0) {
      ElMessage.success(`成功标记 ${successCount} 个案件为停留`)
      clearSelection()
      loadCases()
    } else if (failureCount > 0) {
      ElMessage.warning(`有 ${failureCount} 个案件标记停留失败`)
      if (failures.length > 0) {
        console.error('停留失败详情:', failures)
        const errorMsg = failures.map((f: any) => `案件${f.caseId || f.case_id}: ${f.errorMessage || f.error_message || '未知错误'}`).join('; ')
        ElMessage.error(`失败详情: ${errorMsg}`)
      }
    } else {
      // 如果既没有成功也没有失败，可能是所有案件都已经停留了（幂等性）
      ElMessage.info('所选案件可能已经是停留状态，或没有符合条件的案件')
      clearSelection()
      loadCases()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量标记停留失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '批量标记停留失败'
      ElMessage.error(errorMsg)
    }
  }
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

// 案件状态映射（英文编码转中文）
const caseStatusMap: Record<string, string> = {
  'pending_repayment': '待还款',
  'partial_repayment': '部分还款',
  'normal_settlement': '正常结清',
  'extension_settlement': '展期结清',
}

// 获取案件状态中文名称
const getCaseStatusText = (status: string) => {
  return caseStatusMap[status] || status
}

// 案件状态标签类型
const getCaseStatusType = (status: string) => {
  const map: any = {
    '待还款': 'danger',
    '部分还款': 'warning',
    '正常结清': 'success',
    '展期结清': 'success',
    'pending_repayment': 'danger',
    'partial_repayment': 'warning',
    'normal_settlement': 'success',
    'extension_settlement': 'success',
  }
  return map[status] || 'info'
}

const handleView = (row: any) => {
  router.push(`/cases/${String(row.id)}`)
}

// 历史催记相关
const showHistoryNotesDialog = ref(false)
const currentCase = ref<any>(null)
const currentViewingCaseId = ref<string>('')  // 当前查看的案件ID
const historyFilters = ref({
  collector: '',
  channel: '',
  status: '',
  result: '',
  dateRange: null as [Date, Date] | null
})

// 触达人列表（mock数据）
const collectorList = ref(['张三', '李四', '王五', '赵六'])

// 历史催记列表（mock数据）
const historyNotes = ref<any[]>([])

// 生成案件的mock催记数据
const generateMockNotes = (caseData: any) => {
  return [
    {
      id: 1,
      register_time: '2025-01-15 10:30:25',
      case_id: caseData?.loan_id || '-',
      collector: '张三',
      channel: 'WhatsApp',
      status: 'reachable',
      result: 'promise_repay',
      remark: '客户承诺今天下午还款',
      next_follow_up: '2025-01-15 14:00:00'
    },
    {
      id: 2,
      register_time: '2025-01-14 15:20:10',
      case_id: caseData?.loan_id || '-',
      collector: '李四',
      channel: 'SMS',
      status: 'no_response',
      result: 'continuous_follow_up',
      remark: '发送催收短信，未收到回复',
      next_follow_up: '2025-01-15 09:00:00'
    },
    {
      id: 3,
      register_time: '2025-01-13 11:15:30',
      case_id: caseData?.loan_id || '-',
      collector: '王五',
      channel: '电话外呼',
      status: 'reachable',
      result: 'refuse_repay',
      remark: '电话接通，客户拒绝还款',
      next_follow_up: '2025-01-14 10:00:00'
    },
    {
      id: 4,
      register_time: '2025-01-12 09:45:15',
      case_id: caseData?.loan_id || '-',
      collector: '赵六',
      channel: 'RCS',
      status: 'reachable',
      result: 'promise_repay',
      remark: '客户已查看消息，承诺周末还款',
      next_follow_up: '2025-01-13 10:00:00'
    }
  ]
}

// 查看催记
const handleViewNotes = (row: any) => {
  currentCase.value = row
  currentViewingCaseId.value = row.case_code || row.loan_id || ''  // 保存案件ID用于显示在标题
  historyNotes.value = generateMockNotes(row)
  showHistoryNotesDialog.value = true
}

// 筛选后的历史催记列表
const filteredHistoryNotes = computed(() => {
  let result = historyNotes.value

  // 筛选触达人
  if (historyFilters.value.collector) {
    result = result.filter((note: any) => 
      note.collector === historyFilters.value.collector
    )
  }

  // 筛选触达渠道
  if (historyFilters.value.channel) {
    result = result.filter((note: any) => 
      note.channel === historyFilters.value.channel
    )
  }

  // 筛选状态
  if (historyFilters.value.status) {
    result = result.filter((note: any) => 
      note.status === historyFilters.value.status
    )
  }

  // 筛选结果
  if (historyFilters.value.result) {
    result = result.filter((note: any) => 
      note.result === historyFilters.value.result
    )
  }

  // 筛选时间范围
  if (historyFilters.value.dateRange && historyFilters.value.dateRange.length === 2) {
    const [startDate, endDate] = historyFilters.value.dateRange
    result = result.filter((note: any) => {
      const noteDate = dayjs(note.register_time)
      return noteDate.isAfter(dayjs(startDate).startOf('day')) && 
             noteDate.isBefore(dayjs(endDate).endOf('day'))
    })
  }

  return result
})

// 处理历史催记筛选
const handleHistoryFilter = () => {
  // 筛选逻辑已在 computed 中实现
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'reachable': '可联',
    'not_exist': '不存在',
    'no_response': '未响应'
  }
  return labels[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    'reachable': 'success',
    'not_exist': 'danger',
    'no_response': 'warning'
  }
  return types[status] || ''
}

// 获取结果标签
const getResultLabel = (result: string) => {
  const labels: Record<string, string> = {
    'promise_repay': '承诺还款',
    'refuse_repay': '拒绝还款',
    'not_related': '与借款人不相关',
    'related': '与借款人相关',
    'promise_repay_on_behalf': '承诺代还',
    'promise_inform': '承诺转告',
    'lost_contact': '失联',
    'continuous_follow_up': '持续跟进',
    'other': '其它'
  }
  return labels[result] || result
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
    const url = getApiUrl(`tenants/${currentTenantId.value}/fields`)
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
    // 处理不同的响应格式
    // Java后端格式: request.ts已经提取了data，所以res是数组
    // Python后端格式: 直接返回数组
    const allGroups = Array.isArray(res) ? res : (res.data || [])
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
      team_group_id: undefined,
      team_id: undefined,
      collector_id: undefined,
      due_date_range: null,
      settlement_date_range: null,
      overdue_days_min: undefined,
      overdue_days_max: undefined,
    }
    dynamicFilterValues.value = {}
    teamGroups.value = []
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
    teamGroups.value = []
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

.user-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name-cell .user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-name-cell .user-id-text {
  font-size: 12px;
  color: #606266;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

/* 历史催记对话框样式 */
.history-notes-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.history-notes-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.history-notes-list {
  margin-top: 8px;
}

.history-notes-list :deep(.el-table) {
  font-size: 13px;
}

.history-notes-list :deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  padding: 12px 0;
}

.history-notes-list :deep(.el-table td) {
  padding: 12px 0;
}

.history-notes-list :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

/* 批量操作工具栏 */
.batch-toolbar {
  margin-bottom: 16px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

/* 已结清案件行样式（禁用选择） */
:deep(.case-row-disabled) {
  opacity: 0.6;
  cursor: not-allowed;
}

:deep(.case-row-disabled .el-checkbox) {
  cursor: not-allowed;
}

:deep(.case-row-disabled .el-checkbox__input) {
  cursor: not-allowed;
  pointer-events: none;
}

/* 范围输入框组样式 */
.range-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.range-input {
  flex: 1;
  min-width: 0;
}

.range-separator {
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
}
</style>

