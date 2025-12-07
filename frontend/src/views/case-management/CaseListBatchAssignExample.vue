<!--
  案件列表批量分案功能集成示例
  
  这是一个简化的示例，展示如何在案件列表中集成批量分案功能。
  实际使用时，请将相关代码复制到 CaseList.vue 中。
-->
<template>
  <div class="case-list-with-batch-assign">
    <!-- 批量操作工具栏 -->
    <div v-if="selectedCases.length > 0" class="batch-toolbar">
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
            <el-button size="small" @click="clearSelection">
              取消选择
            </el-button>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- 案件列表 -->
    <el-table
      ref="tableRef"
      :data="cases"
      @selection-change="handleSelectionChange"
      v-loading="loading"
      style="width: 100%"
    >
      <!-- 选择列 -->
      <el-table-column 
        type="selection" 
        width="55"
        :selectable="row => canSelectCase(row)"
      />
      
      <!-- 其他列（示例） -->
      <el-table-column prop="caseCode" label="案件编号" width="120" />
      <el-table-column prop="userName" label="客户姓名" width="100" />
      <el-table-column prop="mobile" label="手机号码" width="120" />
      <el-table-column prop="outstandingAmount" label="逾期金额" width="120" align="right">
        <template #default="{ row }">
          ¥{{ row.outstandingAmount?.toLocaleString() || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="overdueDays" label="逾期天数" width="100" align="right" />
      <el-table-column prop="caseStatus" label="案件状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getCaseStatusType(row.caseStatus)">
            {{ getCaseStatusText(row.caseStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small">
            查看详情
          </el-button>
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

    <!-- 批量分案弹窗 -->
    <BatchAssignDialog
      v-model="showBatchAssignDialog"
      :selected-case-ids="selectedCases.map(c => c.id)"
      @success="handleAssignSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Operation } from '@element-plus/icons-vue'
import request from '@/utils/request'
import BatchAssignDialog from '@/components/BatchAssignDialog.vue'

// ==================== 状态管理 ====================
const loading = ref(false)
const cases = ref<any[]>([])
const selectedCases = ref<any[]>([])
const showBatchAssignDialog = ref(false)
const tableRef = ref()

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadCases()
})

// ==================== 数据加载 ====================
/**
 * 加载案件列表
 */
const loadCases = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/cases',
      method: 'get',
      params: {
        skip: (pagination.page - 1) * pagination.pageSize,
        limit: pagination.pageSize
      }
    })
    
    cases.value = response.data?.items || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    console.error('加载案件列表失败:', error)
    ElMessage.error('加载案件列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 选择处理 ====================
/**
 * 判断案件是否可选择
 * 只有未结清的案件可以选择
 */
const canSelectCase = (row: any) => {
  return row.caseStatus !== 'normal_settlement' && row.caseStatus !== 'extension_settlement'
}

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
}

// ==================== 批量分案 ====================
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

// ==================== 辅助函数 ====================
/**
 * 获取案件状态类型（用于标签颜色）
 */
const getCaseStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'pending_repayment': 'danger',
    'partial_repayment': 'warning',
    'normal_settlement': 'success',
    'extension_settlement': 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取案件状态文本
 */
const getCaseStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'pending_repayment': '待还款',
    'partial_repayment': '部分还款',
    'normal_settlement': '正常结清',
    'extension_settlement': '展期结清'
  }
  return textMap[status] || status
}
</script>

<style scoped>
.case-list-with-batch-assign {
  padding: 20px;
}

.batch-toolbar {
  margin-bottom: 16px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

:deep(.el-table__header-wrapper) {
  .el-checkbox {
    /* 自定义全选框样式 */
  }
}
</style>



















