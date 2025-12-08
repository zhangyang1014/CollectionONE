<template>
  <div class="tenant-fields-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <h2 style="margin: 0">案件列表甲方字段查看</h2>
            <div v-if="currentVersionInfo" class="version-tag">
              当前版本：v{{ currentVersionInfo.version }} | 
              上传时间：{{ formatDateTime(currentVersionInfo.uploadedAt) }} | 
              字段数：{{ currentVersionInfo.fieldsCount }}个
            </div>
          </div>
          <div class="header-buttons">
            <el-button @click="handleDownloadTemplate">
              <el-icon><Download /></el-icon>
              下载JSON模板
            </el-button>
            <el-button 
              type="primary" 
              @click="handleUploadClick"
              :disabled="!currentTenantId"
            >
              <el-icon><Upload /></el-icon>
              上传JSON文件
            </el-button>
            <el-button 
              @click="showVersionManagement"
              :disabled="!currentTenantId"
            >
              <el-icon><Operation /></el-icon>
              版本管理
            </el-button>
          </div>
        </div>
      </template>

      <!-- 提示信息 -->
      <el-alert
        v-if="!currentTenantId"
        title="请先选择甲方"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-alert
        v-else-if="fields.length === 0"
        title="当前甲方未上传字段配置，显示标准字段作为参考"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 字段表格 -->
      <el-table 
        :data="filteredFields" 
        row-key="field_key"
        border 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="field_name" label="字段名称" min-width="120" />
        <el-table-column prop="field_key" label="字段标识" min-width="140" />
        <el-table-column prop="field_type" label="字段类型" width="100" />
        <el-table-column prop="enum_values" label="枚举值" width="200">
          <template #default="{ row }">
            <span v-if="row.field_type === 'Enum' && row.enum_values && row.enum_values.length > 0">
              <el-tag 
                v-for="(item, index) in row.enum_values.slice(0, 2)" 
                :key="index"
                size="small"
                style="margin-right: 4px"
              >
                {{ typeof item === 'string' ? item : item.standard_name || item.name || item.value }}
              </el-tag>
              <el-tag v-if="row.enum_values.length > 2" size="small" type="info">
                等{{ row.enum_values.length }}个
              </el-tag>
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="is_required" label="是否必填" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.is_required">✓</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 上传JSON文件弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传案件列表甲方字段配置"
      width="900px"
      :close-on-click-modal="false"
      @close="handleCancelUpload"
    >
      <div v-loading="uploadLoading">
        <!-- 当前甲方信息 -->
        <div class="section">
          <h3>当前甲方信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="甲方名称">{{ tenantInfo.name || '甲方' + currentTenantId }}</el-descriptions-item>
            <el-descriptions-item label="甲方ID">{{ currentTenantId }}</el-descriptions-item>
            <el-descriptions-item label="配置场景">案件列表</el-descriptions-item>
            <el-descriptions-item label="当前生效版本">
              {{ currentVersionInfo ? 'v' + currentVersionInfo.version : '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="当前字段数">{{ currentVersionInfo?.fieldsCount || 0 }}个</el-descriptions-item>
            <el-descriptions-item label="最后更新">
              {{ currentVersionInfo ? formatDateTime(currentVersionInfo.uploadedAt) : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 上传历史记录 -->
        <div class="section">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h3>上传历史记录（最近5次）</h3>
            <el-link type="primary" @click="showVersionManagement">查看全部历史 ></el-link>
          </div>
          <div v-if="uploadHistory.length === 0" style="text-align: center; color: #909399; padding: 20px;">
            暂无上传记录
          </div>
          <div v-else class="history-list">
            <div 
              v-for="item in uploadHistory.slice(0, 5)" 
              :key="item.version"
              class="history-item"
              :class="{ active: item.isActive }"
            >
              <div class="history-header">
                <span class="version-badge" :class="{ current: item.isActive }">
                  {{ item.isActive ? '●' : '○' }} 版本{{ item.version }}
                </span>
                <el-tag v-if="item.isActive" type="success" size="small">当前使用</el-tag>
              </div>
              <div class="history-info">
                <span>上传时间：{{ formatDateTime(item.uploadedAt) }}</span>
                <span>上传人：{{ item.uploadedByName }}</span>
                <span>字段数：{{ item.fieldsCount }}个</span>
              </div>
              <div class="history-actions">
                <el-button size="small" text @click="viewVersionDetail(item.version)">查看</el-button>
                <el-button size="small" text @click="downloadVersion(item.version)">下载</el-button>
                <el-button 
                  v-if="!item.isActive && uploadHistory.length > 1" 
                  size="small" 
                  text 
                  @click="compareWithVersion(item.version)"
                >
                  对比
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 上传新文件 -->
        <div class="section">
          <h3>上传新文件</h3>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".json"
            :limit="1"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              点击或拖拽上传JSON文件
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持.json格式，文件大小不超过2MB<br>
                上传后将创建新版本并自动设为当前使用版本
              </div>
            </template>
          </el-upload>

          <!-- 上传选项 -->
          <div style="margin-top: 20px;">
            <el-checkbox v-model="uploadOptions.validate" disabled>上传前验证JSON格式</el-checkbox>
            <el-checkbox v-model="uploadOptions.setActive">上传成功后自动设为当前使用版本</el-checkbox>
            <el-checkbox v-model="uploadOptions.showCompare">上传后显示与上一版本的对比</el-checkbox>
          </div>

          <!-- 验证结果 -->
          <div v-if="validateResult && !validateResult.valid" class="validation-error">
            <el-alert
              title="JSON格式校验失败"
              type="error"
              :closable="false"
              show-icon
            >
              <div v-for="(error, index) in validateResult.errors" :key="index" style="margin-bottom: 5px;">
                ❌ {{ error }}
              </div>
            </el-alert>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleCancelUpload">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmUpload"
          :disabled="!selectedFile || (validateResult && !validateResult.valid) || uploadLoading"
          :loading="uploadLoading"
        >
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 版本管理抽屉 -->
    <el-drawer
      v-model="versionDrawerVisible"
      title="版本管理 - 案件列表字段配置"
      size="50%"
      direction="rtl"
    >
      <div v-loading="versionLoading">
        <div class="drawer-header">
          <el-alert 
            :title="`当前使用版本：版本${currentVersionInfo?.version || '-'}`"
            type="success"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          />
          <el-input
            v-model="versionSearch"
            placeholder="搜索版本..."
            clearable
            style="margin-bottom: 15px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="version-list">
          <div 
            v-for="item in filteredVersionHistory" 
            :key="item.version"
            class="version-card"
            :class="{ active: item.isActive }"
          >
            <div class="version-header">
              <span class="version-title">
                {{ item.isActive ? '●' : '○' }} 版本{{ item.version }}
              </span>
              <el-tag v-if="item.isActive" type="success" size="small">当前使用</el-tag>
            </div>
            <div class="version-info">
              <div><strong>上传时间：</strong>{{ formatDateTime(item.uploadedAt) }}</div>
              <div><strong>上传人：</strong>{{ item.uploadedByName }}</div>
              <div><strong>字段数：</strong>{{ item.fieldsCount }}个</div>
              <div v-if="item.versionNote"><strong>说明：</strong>{{ item.versionNote }}</div>
            </div>
            <div class="version-actions">
              <el-button size="small" @click="viewVersionDetail(item.version)">查看详情</el-button>
              <el-button size="small" @click="downloadVersion(item.version)">下载JSON</el-button>
              <el-button 
                v-if="!item.isActive" 
                size="small" 
                type="primary"
                @click="confirmActivateVersion(item.version)"
              >
                设为当前版本
              </el-button>
              <el-button 
                v-if="allVersionHistory.length > 1"
                size="small" 
                @click="showCompareDialog(item.version)"
              >
                对比
              </el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="versionTotal > versionPageSize"
          v-model:current-page="versionPage"
          v-model:page-size="versionPageSize"
          :total="versionTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadVersionHistory"
          @size-change="loadVersionHistory"
          style="margin-top: 20px; justify-content: center;"
        />
      </div>
    </el-drawer>

    <!-- 版本详情对话框 -->
    <el-dialog
      v-model="versionDetailDialogVisible"
      :title="`版本详情 - 版本${selectedVersion?.version}`"
      width="900px"
    >
      <div v-if="selectedVersion">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="上传时间">{{ formatDateTime(selectedVersion.uploadedAt) }}</el-descriptions-item>
          <el-descriptions-item label="上传人">{{ selectedVersion.uploadedByName }}</el-descriptions-item>
          <el-descriptions-item label="字段数">{{ selectedVersion.fieldsCount }}个</el-descriptions-item>
          <el-descriptions-item label="是否当前使用">
            <el-tag :type="selectedVersion.isActive ? 'success' : 'info'">
              {{ selectedVersion.isActive ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本说明" :span="2">{{ selectedVersion.versionNote || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4>字段列表</h4>
        <el-table :data="selectedVersion.fields" border size="small">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="field_name" label="字段名称" />
          <el-table-column prop="field_key" label="字段标识" />
          <el-table-column prop="field_type" label="类型" width="100" />
          <el-table-column prop="is_required" label="必填" width="70" align="center">
            <template #default="{ row }">
              {{ row.is_required ? '✓' : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="downloadVersion(selectedVersion?.version)">下载此版本</el-button>
        <el-button type="primary" @click="versionDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 版本对比对话框 -->
    <el-dialog
      v-model="compareDialogVisible"
      :title="compareDialogTitle"
      width="90%"
      top="5vh"
    >
      <div v-if="compareResult" class="compare-content">
        <!-- 自动对比提示 -->
        <el-alert
          v-if="isAutoCompare"
          title="💡 这是您刚刚上传的新版本与上一版本的对比结果"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <!-- 版本信息 -->
        <el-row :gutter="20" style="margin-bottom: 20px;">
          <el-col :span="12">
            <el-card shadow="never">
              <h4>版本{{ compareResult.version1.version }}（上一版本）</h4>
              <p>上传时间：{{ formatDateTime(compareResult.version1.uploadedAt) }}</p>
              <p>字段数：{{ compareResult.version1.fieldsCount }}个</p>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" :class="{ 'highlight-version': isAutoCompare }">
              <h4>
                版本{{ compareResult.version2.version }}
                <span v-if="isAutoCompare">（新上传，当前使用）</span>
                <span v-else>（对比版本）</span>
              </h4>
              <p>上传时间：{{ formatDateTime(compareResult.version2.uploadedAt) }}</p>
              <p>字段数：{{ compareResult.version2.fieldsCount }}个</p>
            </el-card>
          </el-col>
        </el-row>

        <!-- 变更摘要 -->
        <el-alert
          title="变更摘要"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <ul style="margin: 10px 0; padding-left: 20px;">
            <li>新增字段：{{ compareResult.summary.added }}个</li>
            <li>删除字段：{{ compareResult.summary.removed }}个</li>
            <li>修改字段：{{ compareResult.summary.modified }}个</li>
            <li>未变更：{{ compareResult.summary.unchanged }}个</li>
          </ul>
        </el-alert>

        <el-divider />

        <!-- 详细对比 -->
        <h3 style="margin-bottom: 15px;">📊 详细对比</h3>

        <!-- 新增字段 -->
        <div v-if="compareResult.details.added?.length > 0" class="compare-section">
          <h4 style="color: #67c23a;">🟢 新增字段（{{ compareResult.details.added.length }}个）</h4>
          <el-table :data="compareResult.details.added" border size="small" style="margin-bottom: 20px;">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="field_name" label="字段名称" />
            <el-table-column prop="field_key" label="字段标识" />
            <el-table-column prop="field_type" label="类型" width="100" />
            <el-table-column prop="is_required" label="必填" width="70" align="center">
              <template #default="{ row }">{{ row.is_required ? '是' : '否' }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 删除字段 -->
        <div v-if="compareResult.details.removed?.length > 0" class="compare-section">
          <h4 style="color: #f56c6c;">🔴 删除字段（{{ compareResult.details.removed.length }}个）</h4>
          <el-table :data="compareResult.details.removed" border size="small" style="margin-bottom: 20px;">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="field_name" label="字段名称" />
            <el-table-column prop="field_key" label="字段标识" />
            <el-table-column prop="field_type" label="类型" width="100" />
          </el-table>
        </div>

        <!-- 修改字段 -->
        <div v-if="compareResult.details.modified?.length > 0" class="compare-section">
          <h4 style="color: #e6a23c;">🟡 修改字段（{{ compareResult.details.modified.length }}个）</h4>
          <div v-for="(item, index) in compareResult.details.modified" :key="index" class="modified-field">
            <h5>≈ {{ item.fieldName }} ({{ item.fieldKey }})</h5>
            <el-table :data="item.changes" border size="small" style="margin-bottom: 15px;">
              <el-table-column prop="property" label="属性" width="150" />
              <el-table-column label="版本{{ compareResult.version1.version }}（旧值）">
                <template #default="{ row }">
                  <span style="color: #f56c6c;">- {{ formatValue(row.oldValue) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="版本{{ compareResult.version2.version }}（新值）">
                <template #default="{ row }">
                  <span style="color: #67c23a;">+ {{ formatValue(row.newValue) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 未变更字段（可折叠） -->
        <el-collapse v-if="compareResult.details.unchanged?.length > 0" style="margin-top: 20px;">
          <el-collapse-item>
            <template #title>
              <h4 style="color: #909399;">⚪ 未变更字段（{{ compareResult.details.unchanged.length }}个）</h4>
            </template>
            <el-table :data="compareResult.details.unchanged" border size="small">
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="field_name" label="字段名称" />
              <el-table-column prop="field_key" label="字段标识" />
              <el-table-column prop="field_type" label="类型" width="100" />
            </el-table>
          </el-collapse-item>
        </el-collapse>

        <el-divider />

        <!-- 版本选择 -->
        <div style="margin-top: 20px;">
          <h4>选择要使用的版本：</h4>
          <el-radio-group v-model="selectedCompareVersion" style="margin-top: 10px;">
            <el-radio :label="compareResult.version1.version">
              版本{{ compareResult.version1.version }}（{{ compareResult.version1.fieldsCount }}个字段）
            </el-radio>
            <el-radio :label="compareResult.version2.version">
              版本{{ compareResult.version2.version }}（{{ compareResult.version2.fieldsCount }}个字段）
              <el-tag v-if="currentVersionInfo?.version === compareResult.version2.version" type="success" size="small" style="margin-left: 10px;">
                当前使用
              </el-tag>
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <template #footer>
        <el-button @click="exportCompareReport">导出对比报告</el-button>
        <el-button @click="downloadVersion(compareResult?.version1?.version)">下载版本{{ compareResult?.version1?.version }}</el-button>
        <el-button @click="downloadVersion(compareResult?.version2?.version)">下载版本{{ compareResult?.version2?.version }}</el-button>
        <el-button 
          v-if="selectedCompareVersion && selectedCompareVersion !== currentVersionInfo?.version"
          type="primary" 
          @click="activateComparedVersion"
        >
          切换到版本{{ selectedCompareVersion }}
        </el-button>
        <el-button type="primary" @click="handleCloseCompareDialog">
          {{ isAutoCompare ? '确认并关闭' : '关闭' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Upload, Operation, UploadFilled, Search } from '@element-plus/icons-vue'
import { getCaseListStandardFields } from '@/api/field'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || '1')

// 数据
const fields = ref<any[]>([])
const loading = ref(false)
const currentVersionInfo = ref<any>(null)
const tenantInfo = ref<any>({})

// 搜索过滤
const searchText = ref('')
const filteredFields = computed(() => {
  if (!searchText.value) return fields.value
  const text = searchText.value.toLowerCase()
  return fields.value.filter(f => 
    f.field_name?.toLowerCase().includes(text) || 
    f.field_key?.toLowerCase().includes(text)
  )
})

// 对比对话框标题
const compareDialogTitle = computed(() => {
  if (!compareResult.value) return '版本对比'
  
  const v1 = compareResult.value.version1.version
  const v2 = compareResult.value.version2.version
  
  if (isAutoCompare.value) {
    return `自动版本对比：版本${v1} vs 版本${v2}（新上传）`
  } else {
    return `版本对比：版本${v1} vs 版本${v2}`
  }
})

// 上传相关
const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)
const selectedFile = ref<File | null>(null)
const uploadHistory = ref<any[]>([])
const validateResult = ref<any>(null)
const uploadOptions = ref({
  validate: true,
  setActive: true,
  showCompare: false
})
const uploadRef = ref()

// 版本管理
const versionDrawerVisible = ref(false)
const versionLoading = ref(false)
const allVersionHistory = ref<any[]>([])
const versionSearch = ref('')
const versionPage = ref(1)
const versionPageSize = ref(10)
const versionTotal = ref(0)

const filteredVersionHistory = computed(() => {
  if (!versionSearch.value) return allVersionHistory.value
  const text = versionSearch.value.toLowerCase()
  return allVersionHistory.value.filter(v => 
    v.version.toString().includes(text) ||
    v.uploadedByName?.toLowerCase().includes(text) ||
    v.versionNote?.toLowerCase().includes(text)
  )
})

// 版本详情
const versionDetailDialogVisible = ref(false)
const selectedVersion = ref<any>(null)

// 版本对比
const compareDialogVisible = ref(false)
const compareResult = ref<any>(null)
const selectedCompareVersion = ref<number | null>(null)
const isAutoCompare = ref(false)  // 标识是否为上传后自动对比

// 加载甲方字段数据
const loadTenantFields = async () => {
  if (!currentTenantId.value) {
    return
  }

  loading.value = true
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json`,
      method: 'get',
      params: { scene: 'list' }
    })
    
    if (response && response.fields && response.fields.length > 0) {
      fields.value = response.fields
      currentVersionInfo.value = {
        version: response.version,
        uploadedAt: response.fetched_at,
        fieldsCount: response.fields_count,
        uploadedBy: response.uploaded_by,
        uploadedByName: response.uploaded_by_name
      }
      tenantInfo.value = {
        id: response.tenant_id,
        name: response.tenant_name
      }
    } else {
      // 兜底：使用标准字段
      fields.value = await loadFallbackFromStandard()
      currentVersionInfo.value = null
    }
    
  } catch (error: any) {
    console.error('加载甲方字段失败：', error)
    fields.value = await loadFallbackFromStandard()
    currentVersionInfo.value = null
  } finally {
    loading.value = false
  }
}

// 加载标准字段作为兜底
const loadFallbackFromStandard = async () => {
  try {
    const res = await getCaseListStandardFields()
    const raw = Array.isArray(res) ? res : (res?.data || [])
    return raw.map((item: any, idx: number) => ({
      id: item.id ?? idx + 1,
      field_name: item.field_name ?? item.fieldName,
      field_key: item.field_key ?? item.fieldKey,
      field_type: item.field_data_type ?? item.fieldDataType ?? 'String',
      enum_values: [],
      is_required: item.is_required ?? item.required ?? false,
      sort_order: item.sort_order ?? item.sortOrder ?? idx + 1,
      description: item.description ?? ''
    }))
  } catch (e) {
    console.warn('加载标准字段兜底失败', e)
    return []
  }
}

// 下载JSON模板
const handleDownloadTemplate = async () => {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/v1/tenants/fields-json/template?scene=list`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    )
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'tenant_fields_list_template.json'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败', error)
    ElMessage.error('下载模板失败')
  }
}

// 打开上传弹窗
const handleUploadClick = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }
  
  uploadDialogVisible.value = true
  selectedFile.value = null
  validateResult.value = null
  
  // 加载上传历史
  await loadUploadHistory()
}

// 加载上传历史
const loadUploadHistory = async () => {
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/history`,
      method: 'get',
      params: { scene: 'list', page: 1, page_size: 10 }
    })
    
    uploadHistory.value = response.records || []
  } catch (error) {
    console.error('加载上传历史失败', error)
    uploadHistory.value = []
  }
}

// 文件选择
const handleFileChange = async (file: any) => {
  const rawFile = file.raw || file
  if (!rawFile) return
  
  selectedFile.value = rawFile
  
  // 验证文件
  await validateFile(rawFile)
}

// 文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  validateResult.value = null
}

// 验证文件
const validateFile = async (file: File) => {
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/validate`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    validateResult.value = response
    
    if (response.valid) {
      ElMessage.success('文件格式验证通过')
    } else {
      ElMessage.warning('文件格式验证失败，请查看错误信息')
    }
  } catch (error: any) {
    console.error('验证文件失败', error)
    ElMessage.error('验证文件失败')
  } finally {
    uploadLoading.value = false
  }
}

// 确认上传
const handleConfirmUpload = async () => {
  if (!selectedFile.value || !validateResult.value?.valid) {
    ElMessage.warning('请选择有效的JSON文件')
    return
  }
  
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('scene', 'list')
    formData.append('uploadedBy', 'admin')  // TODO: 从用户信息获取
    
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/upload`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const newVersion = response.version
    const fieldsCount = response.fields_count
    
    ElMessage.success(`成功上传版本${newVersion}，共 ${fieldsCount} 个字段`)
    uploadDialogVisible.value = false
    
    // 重新加载字段数据
    await loadTenantFields()
    
    // 如果勾选了显示对比选项，且不是首次上传
    if (uploadOptions.value.showCompare && newVersion > 1) {
      // 延迟200ms让用户看到成功提示
      await new Promise(resolve => setTimeout(resolve, 200))
      
      // 自动触发版本对比
      await showAutoCompareAfterUpload(newVersion)
    } else if (newVersion === 1 && uploadOptions.value.showCompare) {
      ElMessage.info('这是首次上传，无历史版本可对比')
    }
    
  } catch (error: any) {
    console.error('上传失败', error)
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 上传后自动对比
const showAutoCompareAfterUpload = async (newVersion: number) => {
  try {
    const previousVersion = newVersion - 1
    
    // 设置5秒超时
    const timeoutPromise = new Promise((_, reject) => 
      setTimeout(() => reject(new Error('对比超时')), 5000)
    )
    
    const comparePromise = compareVersions(currentVersionInfo.value?.version || previousVersion, newVersion)
    
    // 调用对比接口（带超时保护）
    const result = await Promise.race([comparePromise, timeoutPromise])
    
    // 设置对比数据
    compareResult.value = result
    isAutoCompare.value = true  // 标记为自动对比
    selectedCompareVersion.value = newVersion  // 默认选中新版本
    compareDialogVisible.value = true
    
  } catch (error: any) {
    console.error('自动对比失败：', error)
    
    if (error.message === '对比超时') {
      ElMessage.warning('对比计算中，请稍后在版本管理中手动查看')
    } else if (error.response?.status === 404) {
      ElMessage.warning('上一版本数据不存在，无法对比')
    } else {
      ElMessage.warning('自动对比失败，您可在版本管理中手动查看')
    }
  }
}

// 取消上传
const handleCancelUpload = () => {
  uploadDialogVisible.value = false
  selectedFile.value = null
  validateResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 显示版本管理
const showVersionManagement = async () => {
  versionDrawerVisible.value = true
  await loadVersionHistory()
}

// 加载版本历史
const loadVersionHistory = async () => {
  versionLoading.value = true
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/history`,
      method: 'get',
      params: { 
        scene: 'list',
        page: versionPage.value,
        page_size: versionPageSize.value
      }
    })
    
    allVersionHistory.value = response.records || []
    versionTotal.value = response.total || 0
  } catch (error) {
    console.error('加载版本历史失败', error)
    allVersionHistory.value = []
  } finally {
    versionLoading.value = false
  }
}

// 查看版本详情
const viewVersionDetail = async (version: number) => {
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/version/${version}`,
      method: 'get',
      params: { scene: 'list' }
    })
    
    selectedVersion.value = response
    versionDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取版本详情失败', error)
    ElMessage.error('获取版本详情失败')
  }
}

// 下载版本
const downloadVersion = async (version: number) => {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/v1/tenants/${currentTenantId.value}/fields-json/download/${version}?scene=list`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    )
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `tenant_${currentTenantId.value}_list_v${version}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败', error)
    ElMessage.error('下载失败')
  }
}

// 确认激活版本
const confirmActivateVersion = async (version: number) => {
  try {
    await ElMessageBox.confirm(
      `您确定要将版本${version}设为当前使用版本吗？`,
      '确认切换版本',
      {
        confirmButtonText: '确认切换',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await activateVersion(version)
  } catch {
    // 用户取消
  }
}

// 激活版本
const activateVersion = async (version: number) => {
  try {
    await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/activate/${version}`,
      method: 'put',
      params: { scene: 'list' },
      data: {
        operator_id: 'admin',
        reason: '手动切换版本'
      }
    })
    
    ElMessage.success(`已切换到版本${version}`)
    
    // 重新加载数据
    await loadTenantFields()
    await loadVersionHistory()
    
  } catch (error: any) {
    console.error('切换版本失败', error)
    ElMessage.error(error.message || '切换版本失败')
  }
}

// 显示对比对话框
const showCompareDialog = async (version: number) => {
  if (!currentVersionInfo.value) {
    ElMessage.warning('当前没有生效版本，无法对比')
    return
  }
  
  await compareVersions(currentVersionInfo.value.version, version)
}

// 与当前版本对比
const compareWithVersion = async (version: number) => {
  if (!currentVersionInfo.value) {
    ElMessage.warning('当前没有生效版本，无法对比')
    return
  }
  
  await compareVersions(currentVersionInfo.value.version, version)
}

// 对比版本
const compareVersions = async (version1: number, version2: number) => {
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/compare`,
      method: 'get',
      params: {
        scene: 'list',
        version1,
        version2
      }
    })
    
    compareResult.value = response
    selectedCompareVersion.value = currentVersionInfo.value?.version || version2
    compareDialogVisible.value = true
  } catch (error) {
    console.error('版本对比失败', error)
    ElMessage.error('版本对比失败')
  }
}

// 激活对比选中的版本
const activateComparedVersion = async () => {
  if (!selectedCompareVersion.value) return
  
  try {
    await ElMessageBox.confirm(
      `您确定要切换到版本${selectedCompareVersion.value}吗？`,
      '确认切换版本',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await activateVersion(selectedCompareVersion.value)
    compareDialogVisible.value = false
  } catch {
    // 用户取消
  }
}

// 导出对比报告
const exportCompareReport = () => {
  if (!compareResult.value) return
  
  const { version1, version2, summary, details } = compareResult.value
  
  let report = `# 版本对比报告${isAutoCompare.value ? '（新上传）' : ''}\n\n`
  report += `## 基本信息\n`
  report += `- 对比版本：版本${version1.version} vs 版本${version2.version}${isAutoCompare.value ? '（新上传）' : ''}\n`
  report += `- 对比时间：${formatDateTime(new Date().toISOString())}\n`
  if (isAutoCompare.value) {
    report += `- 对比类型：自动对比（上传后触发）\n`
  }
  report += `\n`
  
  report += `## 版本信息\n`
  report += `| 项目 | 版本${version1.version} | 版本${version2.version} |\n`
  report += `|------|---------|----------|\n`
  report += `| 上传时间 | ${formatDateTime(version1.uploadedAt)} | ${formatDateTime(version2.uploadedAt)} |\n`
  report += `| 字段数 | ${version1.fieldsCount}个 | ${version2.fieldsCount}个 |\n\n`
  
  report += `## 变更摘要\n`
  report += `- 新增字段：${summary.added}个\n`
  report += `- 删除字段：${summary.removed}个\n`
  report += `- 修改字段：${summary.modified}个\n`
  report += `- 未变更：${summary.unchanged}个\n\n`
  
  if (details.added?.length > 0) {
    report += `## 新增字段\n`
    details.added.forEach((f: any, i: number) => {
      report += `${i + 1}. ${f.field_name} (${f.field_key})\n`
      report += `   - 类型：${f.field_type}\n`
      report += `   - 必填：${f.is_required ? '是' : '否'}\n`
      report += `   - 排序：${f.sort_order}\n\n`
    })
  }
  
  if (details.removed?.length > 0) {
    report += `## 删除字段\n`
    details.removed.forEach((f: any, i: number) => {
      report += `${i + 1}. ${f.field_name} (${f.field_key})\n`
    })
    report += `\n`
  }
  
  if (details.modified?.length > 0) {
    report += `## 修改字段\n`
    details.modified.forEach((f: any, i: number) => {
      report += `${i + 1}. ${f.fieldName} (${f.fieldKey})\n`
      f.changes.forEach((c: any) => {
        report += `   - ${c.property}：${formatValue(c.oldValue)} → ${formatValue(c.newValue)}\n`
      })
      report += `\n`
    })
  }
  
  report += `---\n生成时间：${formatDateTime(new Date().toISOString())}\n`
  
  // 下载
  const blob = new Blob([report], { type: 'text/markdown;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const autoCompareTag = isAutoCompare.value ? '_新上传' : ''
  a.download = `version_compare_v${version1.version}_vs_v${version2.version}${autoCompareTag}_${Date.now()}.md`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
  
  ElMessage.success('对比报告已导出')
}

// 关闭对比对话框
const handleCloseCompareDialog = () => {
  compareDialogVisible.value = false
  isAutoCompare.value = false  // 重置自动对比标识
}

// 格式化值
const formatValue = (value: any) => {
  if (value === null || value === undefined) return '-'
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
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
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return datetime
  }
}

// 生命周期
onMounted(() => {
  if (currentTenantId.value) {
    loadTenantFields()
  }
})

// 监听甲方变化
watch(() => currentTenantId.value, (newId) => {
  if (newId) {
    loadTenantFields()
  } else {
    fields.value = []
    currentVersionInfo.value = null
  }
})
</script>

<style scoped>
.tenant-fields-list-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.version-tag {
  font-size: 13px;
  color: #606266;
  margin-top: 8px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.history-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.history-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-item.active {
  background-color: #ecf5ff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.version-badge {
  font-weight: 600;
  font-size: 15px;
}

.version-badge.current {
  color: #67c23a;
}

.history-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}

.history-actions {
  display: flex;
  gap: 10px;
}

.validation-error {
  margin-top: 15px;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.version-card {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  transition: all 0.3s;
}

.version-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.version-card.active {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.version-title {
  font-size: 16px;
  font-weight: 600;
}

.version-info {
  font-size: 13px;
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.8;
}

.version-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.compare-content {
  max-height: 70vh;
  overflow-y: auto;
}

.compare-section {
  margin-bottom: 30px;
}

.compare-section h4 {
  margin-bottom: 15px;
}

.modified-field {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.modified-field h5 {
  margin: 0 0 10px 0;
  color: #e6a23c;
}

.drawer-header {
  margin-bottom: 20px;
}

/* 新上传版本高亮样式 */
.highlight-version {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border: 2px solid #2196f3;
}

.highlight-version h4 {
  color: #1976d2;
  font-weight: 600;
}
</style>
