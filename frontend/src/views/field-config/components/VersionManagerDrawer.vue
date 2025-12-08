<template>
  <el-drawer
    v-model="drawerVisible"
    title="版本管理 - 案件列表字段配置"
    size="70%"
    :close-on-click-modal="false"
  >
    <div class="version-manager">
      <!-- 当前版本信息 -->
      <div v-if="currentVersionInfo" class="current-version-bar">
        <div class="bar-title">
          <el-icon><Star /></el-icon>
          当前使用版本
        </div>
        <div class="version-card active">
          <div class="version-header">
            <el-tag type="primary" size="large" effect="dark">
              版本 {{ currentVersionInfo.version }}
            </el-tag>
            <el-tag type="success" size="small">当前使用</el-tag>
          </div>
          <div class="version-info">
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              上传时间：{{ formatDateTime(currentVersionInfo.uploaded_at) }}
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              上传人：{{ currentVersionInfo.uploaded_by_name || currentVersionInfo.uploaded_by }}
            </div>
            <div class="info-item">
              <el-icon><Files /></el-icon>
              字段数：{{ currentVersionInfo.fields_count }} 个
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <!-- 筛选和搜索 -->
      <div class="filter-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索版本说明..."
          :prefix-icon="Search"
          clearable
          style="width: 300px;"
        />
      </div>

      <!-- 版本历史列表 -->
      <div v-loading="isLoading" class="version-list">
        <div
          v-for="version in filteredVersions"
          :key="version.version"
          class="version-card"
          :class="{ 'active': version.is_active }"
        >
          <div class="version-header">
            <div class="version-title">
              <el-icon v-if="version.is_active"><CircleCheckFilled /></el-icon>
              <el-icon v-else><Circle /></el-icon>
              <span class="version-number">版本 {{ version.version }}</span>
            </div>
            <el-tag v-if="version.is_active" type="success" size="small">当前使用</el-tag>
          </div>

          <div class="version-info">
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(version.uploaded_at) }}
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              {{ version.uploaded_by_name || version.uploaded_by }}
            </div>
            <div class="info-item">
              <el-icon><Files /></el-icon>
              {{ version.fields_count }} 个字段
            </div>
          </div>

          <div v-if="version.version_note" class="version-note">
            <el-icon><Document /></el-icon>
            {{ version.version_note }}
          </div>

          <div class="version-actions">
            <el-button 
              type="primary" 
              link 
              size="small"
              @click="viewVersionDetail(version)"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button 
              type="primary" 
              link 
              size="small"
              @click="downloadVersion(version)"
            >
              <el-icon><Download /></el-icon>
              下载JSON
            </el-button>
            <el-button 
              v-if="!version.is_active"
              type="warning" 
              link 
              size="small"
              @click="switchToVersion(version)"
            >
              <el-icon><Switch /></el-icon>
              设为当前版本
            </el-button>
            <el-button 
              type="info" 
              link 
              size="small"
              @click="compareVersion(version)"
            >
              <el-icon><Operation /></el-icon>
              版本对比
            </el-button>
          </div>
        </div>

        <el-empty v-if="filteredVersions.length === 0" description="暂无版本记录" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 10" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadVersionHistory"
          @size-change="loadVersionHistory"
        />
      </div>
    </div>

    <!-- 版本详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`版本详情 - 版本${selectedVersion?.version}`"
      width="900px"
    >
      <div v-if="selectedVersion" class="version-detail">
        <div class="detail-info">
          <div class="info-item">
            <span class="label">上传时间：</span>
            <span class="value">{{ formatDateTime(selectedVersion.uploaded_at) }}</span>
          </div>
          <div class="info-item">
            <span class="label">上传人：</span>
            <span class="value">{{ selectedVersion.uploaded_by_name || selectedVersion.uploaded_by }}</span>
          </div>
          <div class="info-item">
            <span class="label">字段数：</span>
            <span class="value">{{ selectedVersion.fields_count }} 个</span>
          </div>
          <div v-if="selectedVersion.version_note" class="info-item">
            <span class="label">说明：</span>
            <span class="value">{{ selectedVersion.version_note }}</span>
          </div>
        </div>

        <el-divider />

        <div class="fields-table">
          <el-table :data="selectedVersionFields" border stripe max-height="400">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="field_name" label="字段名称" width="180" />
            <el-table-column prop="field_key" label="字段标识" width="180" />
            <el-table-column prop="field_type" label="类型" width="100" />
            <el-table-column label="必填" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.is_required ? 'danger' : 'info'" size="small">
                  {{ row.is_required ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadVersion(selectedVersion)">
          <el-icon><Download /></el-icon>
          下载此版本
        </el-button>
      </template>
    </el-dialog>

    <!-- 版本对比对话框 -->
    <el-dialog
      v-model="compareDialogVisible"
      title="版本对比"
      width="90%"
      fullscreen
    >
      <div v-if="compareData" class="version-compare">
        <!-- 对比头部 -->
        <div class="compare-header">
          <div class="compare-version">
            <h3>版本{{ compareData.version1.version }}</h3>
            <p class="version-meta">
              {{ formatDateTime(compareData.version1.uploaded_at) }} · 
              {{ compareData.version1.fields_count }}个字段
            </p>
          </div>
          <div class="compare-vs">
            <el-icon><Operation /></el-icon>
            VS
          </div>
          <div class="compare-version">
            <h3>版本{{ compareData.version2.version }}</h3>
            <p class="version-meta">
              {{ formatDateTime(compareData.version2.uploaded_at) }} · 
              {{ compareData.version2.fields_count }}个字段
            </p>
          </div>
        </div>

        <!-- 变更摘要 -->
        <div class="compare-summary">
          <el-alert
            title="变更摘要"
            type="info"
            :closable="false"
          >
            <div class="summary-stats">
              <div class="stat-item success">
                <el-icon><Plus /></el-icon>
                新增字段：<strong>{{ compareData.summary.added }}</strong> 个
              </div>
              <div class="stat-item danger">
                <el-icon><Minus /></el-icon>
                删除字段：<strong>{{ compareData.summary.removed }}</strong> 个
              </div>
              <div class="stat-item warning">
                <el-icon><Edit /></el-icon>
                修改字段：<strong>{{ compareData.summary.modified }}</strong> 个
              </div>
              <div class="stat-item info">
                <el-icon><Check /></el-icon>
                未变更：<strong>{{ compareData.summary.unchanged }}</strong> 个
              </div>
            </div>
          </el-alert>
        </div>

        <!-- 详细对比 -->
        <div class="compare-details">
          <!-- 新增字段 -->
          <div v-if="compareData.details.added.length > 0" class="change-section">
            <h4 class="section-title success">
              <el-icon><Plus /></el-icon>
              新增字段（{{ compareData.details.added.length }}个）
            </h4>
            <el-table :data="compareData.details.added" border>
              <el-table-column prop="field_name" label="字段名称" width="180" />
              <el-table-column prop="field_key" label="字段标识" width="180" />
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column label="必填" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.is_required ? 'danger' : 'info'" size="small">
                    {{ row.is_required ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="200" />
            </el-table>
          </div>

          <!-- 删除字段 -->
          <div v-if="compareData.details.removed.length > 0" class="change-section">
            <h4 class="section-title danger">
              <el-icon><Minus /></el-icon>
              删除字段（{{ compareData.details.removed.length }}个）
            </h4>
            <el-table :data="compareData.details.removed" border>
              <el-table-column prop="field_name" label="字段名称" width="180" />
              <el-table-column prop="field_key" label="字段标识" width="180" />
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column prop="description" label="说明" min-width="200" />
            </el-table>
          </div>

          <!-- 修改字段 -->
          <div v-if="compareData.details.modified.length > 0" class="change-section">
            <h4 class="section-title warning">
              <el-icon><Edit /></el-icon>
              修改字段（{{ compareData.details.modified.length }}个）
            </h4>
            <div v-for="(item, index) in compareData.details.modified" :key="index" class="modified-item">
              <div class="modified-header">
                <span class="field-name">{{ item.field_name }} ({{ item.field_key }})</span>
              </div>
              <div class="modified-changes">
                <div v-for="(change, idx) in item.changes" :key="idx" class="change-item">
                  <span class="change-label">{{ change.property }}：</span>
                  <span class="change-old">{{ formatValue(change.old_value) }}</span>
                  <el-icon><Right /></el-icon>
                  <span class="change-new">{{ formatValue(change.new_value) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="compareDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { 
  Star, Clock, User, Files, Search, Document, View, Download, 
  Switch, Operation, CircleCheckFilled, Circle, Plus, Minus, 
  Edit, Check, Right 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getFieldsJsonHistory,
  getFieldsJsonVersion,
  compareFieldsJsonVersions,
  activateFieldsJsonVersion,
  downloadFieldsJsonVersion
} from '@/api/field-mapping'

const props = defineProps<{
  modelValue: boolean
  tenantId: number | undefined
  currentVersion: number | undefined
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'version-changed'): void
}>()

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 状态管理
const isLoading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 版本数据
const versionHistory = ref<any[]>([])
const currentVersionInfo = ref<any>(null)

// 对话框状态
const detailDialogVisible = ref(false)
const compareDialogVisible = ref(false)
const selectedVersion = ref<any>(null)
const selectedVersionFields = ref<any[]>([])
const compareData = ref<any>(null)

// 筛选后的版本列表
const filteredVersions = computed(() => {
  if (!searchQuery.value) return versionHistory.value
  
  const query = searchQuery.value.toLowerCase()
  return versionHistory.value.filter(v => 
    v.version_note?.toLowerCase().includes(query) ||
    v.uploaded_by_name?.toLowerCase().includes(query)
  )
})

// 加载版本历史
const loadVersionHistory = async () => {
  if (!props.tenantId) return
  
  isLoading.value = true
  try {
    const response = await getFieldsJsonHistory(props.tenantId, {
      scene: 'list',
      page: currentPage.value,
      page_size: pageSize.value
    })
    
    versionHistory.value = response.records || []
    total.value = response.total || 0
    currentVersionInfo.value = versionHistory.value.find(v => v.is_active)
    
    console.log('版本历史加载成功:', versionHistory.value.length)
  } catch (error) {
    console.error('加载版本历史失败:', error)
    ElMessage.error('加载版本历史失败')
  } finally {
    isLoading.value = false
  }
}

// 查看版本详情
const viewVersionDetail = async (version: any) => {
  try {
    const response = await getFieldsJsonVersion(props.tenantId!, version.version, 'list')
    selectedVersion.value = response
    selectedVersionFields.value = response.fields || []
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载版本详情失败:', error)
    ElMessage.error('加载版本详情失败')
  }
}

// 下载版本
const downloadVersion = async (version: any) => {
  try {
    const blob = await downloadFieldsJsonVersion(props.tenantId!, version.version, 'list')
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `tenant_${props.tenantId}_list_v${version.version}.json`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载版本失败:', error)
    ElMessage.error('下载失败')
  }
}

// 切换版本
const switchToVersion = async (version: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将版本${version.version}设为当前使用版本吗？`,
      '确认切换',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await activateFieldsJsonVersion(
      props.tenantId!,
      version.version,
      {
        operator_id: 'admin', // TODO: 从用户信息获取
        reason: '手动切换版本'
      },
      'list'
    )
    
    ElMessage.success('版本切换成功')
    await loadVersionHistory()
    emit('version-changed')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('切换版本失败:', error)
      ElMessage.error('切换版本失败')
    }
  }
}

// 版本对比
const compareVersion = async (version: any) => {
  if (!currentVersionInfo.value) {
    ElMessage.warning('请先加载当前版本信息')
    return
  }
  
  try {
    const response = await compareFieldsJsonVersions(
      props.tenantId!,
      version.version,
      currentVersionInfo.value.version,
      'list'
    )
    
    compareData.value = response
    compareDialogVisible.value = true
  } catch (error) {
    console.error('版本对比失败:', error)
    ElMessage.error('版本对比失败')
  }
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  try {
    const date = new Date(datetime)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (e) {
    return datetime
  }
}

// 格式化值
const formatValue = (value: any) => {
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return String(value)
}

// 监听抽屉打开
watch(() => props.modelValue, (isOpen) => {
  if (isOpen && props.tenantId) {
    loadVersionHistory()
  }
})
</script>

<style scoped>
.version-manager {
  padding: 0 20px 20px;
}

/* 当前版本栏 */
.current-version-bar {
  margin-bottom: 20px;
}

.bar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

/* 版本卡片 */
.version-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 16px;
  transition: all 0.3s;
  background-color: #fff;
}

.version-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.version-card.active {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.version-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.version-number {
  font-size: 18px;
}

.version-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.version-note {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.version-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

/* 筛选栏 */
.filter-bar {
  margin-bottom: 20px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 版本详情 */
.version-detail {
  padding: 10px 0;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-info .info-item {
  display: flex;
  gap: 8px;
}

.detail-info .label {
  min-width: 80px;
  color: #909399;
  font-weight: 500;
}

.detail-info .value {
  color: #303133;
}

/* 版本对比 */
.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.compare-version h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.version-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.compare-vs {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.compare-summary {
  margin-bottom: 24px;
}

.summary-stats {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.stat-item.success {
  color: #67c23a;
}

.stat-item.danger {
  color: #f56c6c;
}

.stat-item.warning {
  color: #e6a23c;
}

.stat-item.info {
  color: #909399;
}

.stat-item strong {
  font-size: 18px;
  font-weight: 600;
}

/* 变更详情 */
.change-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}

.section-title.success {
  color: #67c23a;
}

.section-title.danger {
  color: #f56c6c;
}

.section-title.warning {
  color: #e6a23c;
}

.modified-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.modified-header {
  font-weight: 600;
  margin-bottom: 8px;
}

.modified-changes {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.change-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.change-label {
  color: #909399;
  min-width: 80px;
}

.change-old {
  color: #f56c6c;
  text-decoration: line-through;
}

.change-new {
  color: #67c23a;
  font-weight: 600;
}
</style>
