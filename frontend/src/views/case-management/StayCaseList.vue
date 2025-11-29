<template>
  <div class="stay-case-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>停留案件</span>
        </div>
      </template>

      <!-- 筛选器 -->
      <el-form :model="filters" class="filter-form" label-width="100px">
        <el-row :gutter="20">
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
            <el-form-item label="停留时间">
              <el-date-picker
                v-model="filters.stay_date_range"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="handleQuery"
              />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="停留操作人">
              <el-select 
                v-model="filters.stay_by" 
                placeholder="全部" 
                clearable
                @change="handleQuery"
              >
                <!-- TODO: 从用户列表获取 -->
                <el-option label="管理员" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 搜索区域 -->
      <div class="search-area">
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
      <div v-if="selectedCases.length > 0" class="batch-toolbar">
        <el-alert
          :title="`已选择 ${selectedCases.length} 个案件`"
          type="info"
          :closable="false"
        >
          <template #default>
            <div class="batch-actions">
              <el-button type="primary" size="small" @click="handleBatchReleaseStay">
                <el-icon><Unlock /></el-icon>
                批量解放停留
              </el-button>
              <el-button size="small" @click="clearSelection">
                取消选择
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 案件列表表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="displayCases"
        border
        @selection-change="handleSelectionChange"
        style="width: 100%; margin-top: 16px"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="caseCode" label="案件编号" width="150" />
        <el-table-column prop="userName" label="客户姓名" width="120" />
        <el-table-column prop="mobile" label="手机号码" width="130" />
        <el-table-column prop="caseStatus" label="案件状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getCaseStatusType(row.caseStatus)">
              {{ getCaseStatusText(row.caseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="outstandingAmount" label="逾期金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatAmount(row.outstandingAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="stayAt" label="停留时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.stayAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="stayBy" label="停留操作人" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看详情</el-button>
            <el-button link type="primary" @click="handleReleaseStay(row)">解放停留</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Unlock } from '@element-plus/icons-vue'
import { getStayCases, batchReleaseStayCases } from '@/api/case'
import { getQueues } from '@/api/queue'
import { useRouter } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'

const router = useRouter()
const tenantStore = useTenantStore()

// ==================== 状态管理 ====================
const loading = ref(false)
const cases = ref<any[]>([])
const selectedCases = ref<any[]>([])
const queues = ref<any[]>([])
const searchKeyword = ref('')
const tableRef = ref()

const filters = reactive({
  queue_id: null as number | null,
  stay_date_range: null as string[] | null,
  stay_by: null as number | null,
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

// ==================== 计算属性 ====================
const displayCases = computed(() => {
  return cases.value
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadQueues()
  loadCases()
})

// ==================== 数据加载 ====================
/**
 * 加载队列列表
 */
const loadQueues = async () => {
  const currentTenantId = tenantStore.currentTenantId
  if (!currentTenantId) {
    queues.value = []
    return
  }

  try {
    const response = await getQueues({ tenant_id: currentTenantId })
    queues.value = Array.isArray(response) ? response : (response.data?.items || [])
  } catch (error) {
    console.error('加载队列失败:', error)
  }
}

/**
 * 加载停留案件列表
 */
const loadCases = async () => {
  loading.value = true
  try {
    const params: any = {
      skip: (pagination.page - 1) * pagination.pageSize,
      limit: pagination.pageSize,
    }

    if (filters.queue_id) {
      params.queue_id = filters.queue_id
    }

    if (filters.stay_date_range && filters.stay_date_range.length === 2) {
      params.stay_date_start = filters.stay_date_range[0]
      params.stay_date_end = filters.stay_date_range[1]
    }

    if (filters.stay_by) {
      params.stay_by = filters.stay_by
    }

    if (searchKeyword.value) {
      params.search_keyword = searchKeyword.value
    }

    const response = await getStayCases(params)
    const data = response.data || response
    
    cases.value = data.items || []
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载停留案件列表失败:', error)
    ElMessage.error('加载停留案件列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 查询处理 ====================
/**
 * 查询
 */
const handleQuery = () => {
  pagination.page = 1
  loadCases()
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadCases()
}

/**
 * 重置筛选器
 */
const resetFilters = () => {
  filters.queue_id = null
  filters.stay_date_range = null
  filters.stay_by = null
  searchKeyword.value = ''
  handleQuery()
}

// ==================== 选择处理 ====================
/**
 * 处理表格选择变化
 */
const handleSelectionChange = (selection: any[]) => {
  selectedCases.value = selection
}

/**
 * 清除选择
 */
const clearSelection = () => {
  tableRef.value?.clearSelection()
  selectedCases.value = []
}

// ==================== 批量操作 ====================
/**
 * 批量解放停留
 */
const handleBatchReleaseStay = async () => {
  if (selectedCases.value.length === 0) {
    ElMessage.warning('请先选择案件')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedCases.value.length} 个案件解放停留吗？解放后案件将恢复到正常状态，回到正常案件列表。`,
      '解放停留确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const caseIds = selectedCases.value.map(c => c.id)
    const response = await batchReleaseStayCases(caseIds)
    const data = response.data || response

    if (data.success_count > 0) {
      ElMessage.success(`成功解放停留 ${data.success_count} 个案件`)
      clearSelection()
      loadCases()
    }

    if (data.failure_count > 0) {
      ElMessage.warning(`有 ${data.failure_count} 个案件解放停留失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量解放停留失败:', error)
      ElMessage.error('批量解放停留失败')
    }
  }
}

// ==================== 单个操作 ====================
/**
 * 查看详情
 */
const handleView = (row: any) => {
  router.push(`/cases/${row.id}`)
}

/**
 * 单个解放停留
 */
const handleReleaseStay = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要将此案件解放停留吗？解放后案件将恢复到正常状态，回到正常案件列表。',
      '解放停留确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await batchReleaseStayCases([row.id])
    const data = response.data || response

    if (data.success_count > 0) {
      ElMessage.success('解放停留成功')
      loadCases()
    } else {
      ElMessage.error('解放停留失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('解放停留失败:', error)
      ElMessage.error('解放停留失败')
    }
  }
}

// ==================== 分页处理 ====================
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  loadCases()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadCases()
}

// ==================== 工具函数 ====================
/**
 * 格式化金额
 */
const formatAmount = (amount: number | string | null | undefined) => {
  if (!amount) return '0.00'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime: string | null | undefined) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

/**
 * 案件状态映射
 */
const caseStatusMap: Record<string, string> = {
  'pending_repayment': '待还款',
  'partial_repayment': '部分还款',
  'normal_settlement': '正常结清',
  'extension_settlement': '展期结清',
}

/**
 * 获取案件状态中文名称
 */
const getCaseStatusText = (status: string) => {
  return caseStatusMap[status] || status
}

/**
 * 案件状态标签类型
 */
const getCaseStatusType = (status: string) => {
  if (status === 'pending_repayment') return 'warning'
  if (status === 'partial_repayment') return 'info'
  if (status === 'normal_settlement' || status === 'extension_settlement') return 'success'
  return ''
}
</script>

<style scoped lang="scss">
.stay-case-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .filter-form {
    margin-bottom: 16px;
  }

  .search-area {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .search-input {
      flex: 1;
      max-width: 400px;
    }
  }

  .batch-toolbar {
    margin-bottom: 16px;

    .batch-actions {
      display: flex;
      gap: 8px;
      margin-left: 16px;
    }
  }
}
</style>

