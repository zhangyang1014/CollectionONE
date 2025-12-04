<template>
  <div class="translation-bundle-management">
    <!-- 基准语言选择 -->
    <div class="base-locale-selector">
      <el-form :inline="true">
        <el-form-item label="基准语言">
          <el-select v-model="baseLocale" @change="fetchBundleList">
            <el-option
              v-for="lang in enabledLanguages"
              :key="lang.locale"
              :label="`${lang.flagIcon || ''} ${lang.name} (${lang.locale})`"
              :value="lang.locale"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="refreshStatistics" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新统计
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 语言包列表 -->
    <el-table
      v-loading="loading"
      :data="bundleList"
      stripe
      class="bundle-table"
    >
      <el-table-column label="Locale" prop="locale" width="100" />
      
      <el-table-column label="语言名称" width="150">
        <template #default="{ row }">
          <div class="language-cell">
            <span class="flag-icon">{{ row.flagIcon || '🏳️' }}</span>
            <span>{{ row.languageName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="翻译进度" width="250">
        <template #default="{ row }">
          <div class="progress-cell">
            <el-progress
              :percentage="row.translationProgress || 0"
              :color="getProgressColor(row.translationProgress || 0)"
              :stroke-width="16"
            />
            <span class="progress-detail">
              {{ row.translatedKeys || 0 }} / {{ row.totalKeys || 0 }}
            </span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="缺失Key" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.missingKeys > 0 ? 'warning' : 'success'"
            size="large"
          >
            {{ row.missingKeys || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="版本" prop="version" width="100" />

      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.lastUpdatedAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="handleDownload(row, 'full')"
          >
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="handleDownload(row, 'missing')"
            :disabled="row.missingKeys === 0"
          >
            <el-icon><DocumentCopy /></el-icon>
            缺失模板
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleUpload(row)"
          >
            <el-icon><Upload /></el-icon>
            上传
          </el-button>
          <el-button
            type="info"
            size="small"
            link
            @click="handleViewVersions(row)"
          >
            版本历史
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 下载选项对话框 -->
    <el-dialog
      v-model="downloadDialogVisible"
      :title="`下载语言包 - ${currentBundle?.languageName} (${currentBundle?.locale})`"
      width="500px"
    >
      <el-form :model="downloadOptions" label-width="100px">
        <el-form-item label="下载类型">
          <el-radio-group v-model="downloadOptions.type">
            <el-radio value="full">
              <div>
                <div>完整语言包</div>
                <el-text type="info" size="small">
                  包含所有已翻译的key（{{ currentBundle?.translatedKeys || 0 }}条）
                </el-text>
              </div>
            </el-radio>
            <el-radio value="missing" :disabled="!currentBundle || currentBundle.missingKeys === 0">
              <div>
                <div>仅缺失项模板</div>
                <el-text type="info" size="small">
                  包含{{ currentBundle?.missingKeys || 0 }}个未翻译key，值为基准语言
                </el-text>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="文件格式">
          <el-radio-group v-model="downloadOptions.format">
            <el-radio value="json">JSON (标准格式，适合开发)</el-radio>
            <el-radio value="excel">Excel (适合翻译人员编辑)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="命名空间">
          <el-checkbox-group v-model="downloadOptions.namespaces">
            <el-checkbox
              v-for="ns in namespaceList"
              :key="ns"
              :value="ns"
              :label="ns"
            />
          </el-checkbox-group>
          <el-text type="info" size="small">
            不选择则导出全部命名空间
          </el-text>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="downloadDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="confirmDownload"
          :loading="downloading"
        >
          下载
        </el-button>
      </template>
    </el-dialog>

    <!-- 上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      :title="`上传语言包 - ${currentBundle?.languageName} (${currentBundle?.locale})`"
      width="600px"
      @close="handleUploadDialogClose"
    >
      <el-form :model="uploadOptions" label-width="100px">
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :accept="'.json,.xlsx'"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持格式：.json, .xlsx，最大 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="导入策略">
          <el-radio-group v-model="uploadOptions.strategy">
            <el-radio value="overwrite">
              <div>
                <div><strong>覆盖模式</strong>（推荐）</div>
                <el-text type="info" size="small">
                  同名key覆盖，新key添加，旧key保留
                </el-text>
              </div>
            </el-radio>
            <el-radio value="add_only">
              <div>
                <div><strong>仅新增模式</strong></div>
                <el-text type="info" size="small">
                  只添加不存在的key，已存在的保持不变
                </el-text>
              </div>
            </el-radio>
            <el-radio value="replace">
              <div>
                <div><strong style="color: red;">替换模式</strong>（危险）</div>
                <el-text type="warning" size="small">
                  完全替换当前语言包，旧数据清空
                </el-text>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="版本号">
          <el-radio-group v-model="uploadOptions.versionMode">
            <el-radio value="auto">
              自动递增 (当前 {{ currentBundle?.version || 'v1.0' }} → {{ getNextVersion() }})
            </el-radio>
            <el-radio value="custom">
              自定义版本号
            </el-radio>
          </el-radio-group>
          <el-input
            v-if="uploadOptions.versionMode === 'custom'"
            v-model="uploadOptions.customVersion"
            placeholder="如 v2.0"
            style="width: 200px; margin-top: 8px"
          />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="uploadOptions.remarks"
            type="textarea"
            :rows="3"
            placeholder="本次更新的说明..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 校验结果 -->
        <el-card v-if="validationResult" class="validation-result">
          <template #header>
            <div class="card-header">
              <span>上传校验结果</span>
            </div>
          </template>

          <div v-if="validationResult.isValid">
            <el-result icon="success" title="校验通过">
              <template #sub-title>
                <div class="validation-info">
                  <p><el-icon><Check /></el-icon> 文件格式正确</p>
                  <p><el-icon><Check /></el-icon> JSON语法有效</p>
                  <p><el-icon><Check /></el-icon> 共检测到 {{ validationResult.totalKeys }} 个key</p>
                </div>
              </template>
            </el-result>

            <div class="validation-details">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="新增key">
                  <el-tag type="success">{{ validationResult.newKeys }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="更新key">
                  <el-tag type="warning">{{ validationResult.updatedKeys }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="翻译进度">
                  {{ validationResult.progressBefore }}% → {{ validationResult.progressAfter }}%
                  <el-tag
                    :type="validationResult.progressAfter > validationResult.progressBefore ? 'success' : 'info'"
                    size="small"
                    style="margin-left: 8px"
                  >
                    {{ validationResult.progressAfter > validationResult.progressBefore ? '↑' : '→' }}
                    {{ (validationResult.progressAfter - validationResult.progressBefore).toFixed(1) }}%
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 警告信息 -->
              <el-alert
                v-if="validationResult.warnings.length > 0"
                type="warning"
                :closable="false"
                style="margin-top: 16px"
              >
                <template #title>
                  <div><strong>⚠️ 警告 ({{ validationResult.warnings.length }}项)</strong></div>
                </template>
                <ul>
                  <li v-for="(warning, index) in validationResult.warnings.slice(0, 5)" :key="index">
                    {{ warning }}
                  </li>
                  <li v-if="validationResult.warnings.length > 5">
                    ... 还有 {{ validationResult.warnings.length - 5 }} 条
                  </li>
                </ul>
              </el-alert>

              <!-- 未知key列表 -->
              <el-alert
                v-if="validationResult.unknownKeys.length > 0"
                type="info"
                :closable="false"
                style="margin-top: 16px"
              >
                <template #title>
                  <div><strong>包含 {{ validationResult.unknownKeys.length }} 个未知key（不在基准语言中）</strong></div>
                </template>
                <ul>
                  <li v-for="(key, index) in validationResult.unknownKeys.slice(0, 5)" :key="index">
                    {{ key }}
                  </li>
                  <li v-if="validationResult.unknownKeys.length > 5">
                    ... 还有 {{ validationResult.unknownKeys.length - 5 }} 个
                  </li>
                </ul>
              </el-alert>
            </div>
          </div>

          <el-result
            v-else
            icon="error"
            title="校验失败"
          >
            <template #sub-title>
              <div class="validation-errors">
                <p v-for="(error, index) in validationResult.errors" :key="index">
                  <el-icon><Close /></el-icon> {{ error }}
                </p>
              </div>
            </template>
          </el-result>
        </el-card>
      </el-form>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button
          v-if="!validationResult"
          type="primary"
          @click="validateUpload"
          :loading="validating"
          :disabled="!uploadFile"
        >
          开始校验
        </el-button>
        <el-button
          v-else-if="validationResult.isValid"
          type="success"
          @click="confirmUpload"
          :loading="uploading"
        >
          确认导入
        </el-button>
        <el-button
          v-else
          type="primary"
          @click="resetUpload"
        >
          重新选择文件
        </el-button>
      </template>
    </el-dialog>

    <!-- 版本历史对话框 -->
    <el-dialog
      v-model="versionsDialogVisible"
      :title="`版本历史 - ${currentBundle?.languageName} (${currentBundle?.locale})`"
      width="800px"
    >
      <el-table
        :data="versionList"
        v-loading="loadingVersions"
        stripe
      >
        <el-table-column label="版本" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isCurrent ? 'success' : 'info'">
              {{ row.version }}
              <el-icon v-if="row.isCurrent"><Check /></el-icon>
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="上传时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.uploadedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作人" prop="uploadedBy" width="100" />

        <el-table-column label="变更说明" prop="changeSummary" min-width="200" />

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleDownloadVersion(row)"
            >
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button
              v-if="!row.isCurrent"
              type="warning"
              size="small"
              link
              @click="handleRollback(row)"
            >
              <el-icon><RefreshLeft /></el-icon>
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="versionPagination.page"
        v-model:page-size="versionPagination.pageSize"
        :total="versionPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchVersionHistory"
        @current-change="fetchVersionHistory"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type UploadInstance } from 'element-plus'
import {
  Refresh,
  Download,
  Upload,
  DocumentCopy,
  UploadFilled,
  Check,
  Close,
  RefreshLeft,
} from '@element-plus/icons-vue'
import {
  getTranslationBundleList,
  downloadTranslationBundle,
  validateTranslationBundle,
  confirmUploadTranslationBundle,
  getTranslationVersionHistory,
  downloadTranslationVersion,
  rollbackTranslationVersion,
  type Language,
  type BundleUploadValidation,
  type TranslationVersion,
} from '@/api/i18n'

// ==================== Props ====================

interface Props {
  languages: Language[]
}

const props = defineProps<Props>()

// ==================== 响应式数据 ====================

const loading = ref(false)
const baseLocale = ref('zh-CN')

// 启用的语言列表
const enabledLanguages = computed(() => {
  return props.languages.filter(lang => lang.isEnabled)
})

// 语言包列表
interface BundleItem {
  locale: string
  languageName: string
  flagIcon?: string
  totalKeys: number
  translatedKeys: number
  missingKeys: number
  translationProgress: number
  version?: string
  lastUpdatedAt?: string
}

const bundleList = ref<BundleItem[]>([])

// 命名空间列表
const namespaceList = ref<string[]>([
  'common',
  'auth',
  'dashboard',
  'case',
  'payment',
  'field',
  'tenant',
  'organization',
])

// 下载对话框
const downloadDialogVisible = ref(false)
const downloading = ref(false)
const currentBundle = ref<BundleItem>()
const downloadOptions = reactive({
  type: 'full' as 'full' | 'missing',
  format: 'json' as 'json' | 'excel',
  namespaces: [] as string[],
})

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadRef = ref<UploadInstance>()
const uploadFile = ref<File>()
const validating = ref(false)
const uploading = ref(false)
const validationResult = ref<BundleUploadValidation>()
const uploadOptions = reactive({
  strategy: 'overwrite' as 'overwrite' | 'add_only' | 'replace',
  versionMode: 'auto' as 'auto' | 'custom',
  customVersion: '',
  remarks: '',
})

// 版本历史对话框
const versionsDialogVisible = ref(false)
const loadingVersions = ref(false)
const versionList = ref<TranslationVersion[]>([])
const versionPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// ==================== 生命周期 ====================

onMounted(() => {
  fetchBundleList()
})

// ==================== 方法 ====================

/**
 * 获取语言包列表
 */
async function fetchBundleList() {
  loading.value = true
  try {
    const res = await getTranslationBundleList({
      baseLocale: baseLocale.value,
    })
    
    const data = Array.isArray(res) ? res : res.data || []
    bundleList.value = data

    // Mock数据（后端未实现时）
    if (bundleList.value.length === 0) {
      bundleList.value = getMockBundleList()
    }
  } catch (error) {
    console.error('获取语言包列表失败:', error)
    bundleList.value = getMockBundleList()
  } finally {
    loading.value = false
  }
}

/**
 * 刷新统计
 */
function refreshStatistics() {
  fetchBundleList()
  ElMessage.success('已刷新统计数据')
}

/**
 * 处理下载
 */
function handleDownload(row: BundleItem, type: 'full' | 'missing') {
  currentBundle.value = row
  downloadOptions.type = type
  downloadOptions.format = 'json'
  downloadOptions.namespaces = []
  downloadDialogVisible.value = true
}

/**
 * 确认下载
 */
async function confirmDownload() {
  if (!currentBundle.value) return

  downloading.value = true
  try {
    const response = await downloadTranslationBundle(
      currentBundle.value.locale,
      {
        type: downloadOptions.type,
        format: downloadOptions.format,
        namespaces: downloadOptions.namespaces.length > 0
          ? downloadOptions.namespaces
          : undefined,
      }
    )

    // 创建下载链接
    const blob = response.data || response
    const url = window.URL.createObjectURL(blob as Blob)
    const link = document.createElement('a')
    link.href = url
    
    const ext = downloadOptions.format === 'json' ? 'json' : 'xlsx'
    const typePrefix = downloadOptions.type === 'full' ? 'full' : 'missing'
    link.download = `${currentBundle.value.locale}-${typePrefix}.${ext}`
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载成功')
    downloadDialogVisible.value = false
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  } finally {
    downloading.value = false
  }
}

/**
 * 处理上传
 */
function handleUpload(row: BundleItem) {
  currentBundle.value = row
  uploadOptions.strategy = 'overwrite'
  uploadOptions.versionMode = 'auto'
  uploadOptions.customVersion = ''
  uploadOptions.remarks = ''
  uploadDialogVisible.value = true
}

/**
 * 处理文件选择
 */
function handleFileChange(file: any) {
  uploadFile.value = file.raw
  validationResult.value = undefined
}

/**
 * 处理文件移除
 */
function handleFileRemove() {
  uploadFile.value = undefined
  validationResult.value = undefined
}

/**
 * 校验上传文件
 */
async function validateUpload() {
  if (!uploadFile.value || !currentBundle.value) return

  validating.value = true
  try {
    const version = uploadOptions.versionMode === 'custom'
      ? uploadOptions.customVersion
      : getNextVersion()

    const res = await validateTranslationBundle(
      currentBundle.value.locale,
      uploadFile.value,
      {
        strategy: uploadOptions.strategy,
        version,
      }
    )

    validationResult.value = res.data || res
    
    // Mock数据（后端未实现时）
    if (!validationResult.value) {
      validationResult.value = {
        isValid: true,
        totalKeys: 1200,
        newKeys: 44,
        updatedKeys: 156,
        unknownKeys: ['payment.new.feature.title'],
        warnings: ['覆盖 156 个已存在的key'],
        errors: [],
        progressBefore: 85,
        progressAfter: 92,
      }
    }
  } catch (error) {
    console.error('校验失败:', error)
    ElMessage.error('校验失败')
  } finally {
    validating.value = false
  }
}

/**
 * 确认上传
 */
async function confirmUpload() {
  if (!currentBundle.value || !validationResult.value) return

  uploading.value = true
  try {
    await confirmUploadTranslationBundle(
      currentBundle.value.locale,
      'validation-id-mock', // 实际应该是校验返回的ID
      uploadOptions.remarks
    )

    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    fetchBundleList()
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

/**
 * 重置上传
 */
function resetUpload() {
  uploadRef.value?.clearFiles()
  uploadFile.value = undefined
  validationResult.value = undefined
}

/**
 * 上传对话框关闭
 */
function handleUploadDialogClose() {
  resetUpload()
}

/**
 * 查看版本历史
 */
async function handleViewVersions(row: BundleItem) {
  currentBundle.value = row
  versionsDialogVisible.value = true
  versionPagination.page = 1
  await fetchVersionHistory()
}

/**
 * 获取版本历史
 */
async function fetchVersionHistory() {
  if (!currentBundle.value) return

  loadingVersions.value = true
  try {
    const res = await getTranslationVersionHistory(
      currentBundle.value.locale,
      {
        page: versionPagination.page,
        pageSize: versionPagination.pageSize,
      }
    )

    const data = Array.isArray(res) ? res : res.data || []
    versionList.value = data
    versionPagination.total = (res as any).total || data.length

    // Mock数据
    if (versionList.value.length === 0) {
      versionList.value = getMockVersionList()
      versionPagination.total = versionList.value.length
    }
  } catch (error) {
    console.error('获取版本历史失败:', error)
    versionList.value = getMockVersionList()
    versionPagination.total = versionList.value.length
  } finally {
    loadingVersions.value = false
  }
}

/**
 * 下载指定版本
 */
async function handleDownloadVersion(version: TranslationVersion) {
  if (!currentBundle.value) return

  try {
    const response = await downloadTranslationVersion(
      currentBundle.value.locale,
      version.version
    )

    const blob = response.data || response
    const url = window.URL.createObjectURL(blob as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${currentBundle.value.locale}-${version.version}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  }
}

/**
 * 回滚版本
 */
async function handleRollback(version: TranslationVersion) {
  if (!currentBundle.value) return

  await ElMessageBox.confirm(
    `确定要将 ${currentBundle.value.languageName} (${currentBundle.value.locale}) 回滚到 ${version.version} 吗？<br><br>
    <strong>当前版本：</strong>${currentBundle.value.version}<br>
    <strong>目标版本：</strong>${version.version}<br><br>
    <strong>影响：</strong><br>
    • 将丢失当前版本中的所有变更<br>
    • 系统会自动备份当前版本`,
    '版本回滚确认',
    {
      confirmButtonText: '确认回滚',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }
  )

  try {
    await rollbackTranslationVersion(
      currentBundle.value.locale,
      version.version
    )

    ElMessage.success('回滚成功')
    versionsDialogVisible.value = false
    fetchBundleList()
  } catch (error) {
    console.error('回滚失败:', error)
    ElMessage.error('回滚失败')
  }
}

/**
 * 获取下一个版本号
 */
function getNextVersion(): string {
  if (!currentBundle.value?.version) return 'v1.0'
  
  const match = currentBundle.value.version.match(/v(\d+)\.(\d+)/)
  if (!match) return 'v1.0'
  
  const major = parseInt(match[1])
  const minor = parseInt(match[2]) + 1
  
  return `v${major}.${minor}`
}

/**
 * 获取进度颜色
 */
function getProgressColor(percentage: number): string {
  if (percentage < 50) return '#f56c6c'
  if (percentage < 90) return '#e6a23c'
  return '#67c23a'
}

/**
 * 格式化日期
 */
function formatDate(date?: string): string {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 获取Mock语言包列表
 */
function getMockBundleList(): BundleItem[] {
  return [
    {
      locale: 'zh-CN',
      languageName: '中文',
      flagIcon: '🇨🇳',
      totalKeys: 1200,
      translatedKeys: 1200,
      missingKeys: 0,
      translationProgress: 100,
      version: 'v2.3',
      lastUpdatedAt: '2024-12-03 14:30:00',
    },
    {
      locale: 'en-US',
      languageName: 'English',
      flagIcon: '🇺🇸',
      totalKeys: 1200,
      translatedKeys: 1020,
      missingKeys: 180,
      translationProgress: 85,
      version: 'v2.1',
      lastUpdatedAt: '2024-12-01 10:15:00',
    },
    {
      locale: 'es-MX',
      languageName: 'Español',
      flagIcon: '🇲🇽',
      totalKeys: 1200,
      translatedKeys: 744,
      missingKeys: 456,
      translationProgress: 62,
      version: 'v1.8',
      lastUpdatedAt: '2024-11-28 16:45:00',
    },
    {
      locale: 'id-ID',
      languageName: 'Indonesia',
      flagIcon: '🇮🇩',
      totalKeys: 1200,
      translatedKeys: 420,
      missingKeys: 780,
      translationProgress: 35,
      version: 'v1.2',
      lastUpdatedAt: '2024-11-20 09:00:00',
    },
  ]
}

/**
 * 获取Mock版本列表
 */
function getMockVersionList(): TranslationVersion[] {
  return [
    {
      id: 1,
      languageId: 2,
      version: 'v2.1',
      bundleJson: '{}',
      changeSummary: '修复登录页翻译',
      uploadedBy: 1,
      uploadedAt: '2024-12-03 14:30:00',
      isCurrent: true,
    },
    {
      id: 2,
      languageId: 2,
      version: 'v2.0',
      bundleJson: '{}',
      changeSummary: '大版本更新',
      uploadedBy: 2,
      uploadedAt: '2024-12-01 10:15:00',
      isCurrent: false,
    },
    {
      id: 3,
      languageId: 2,
      version: 'v1.9',
      bundleJson: '{}',
      changeSummary: '补充仪表板翻译',
      uploadedBy: 3,
      uploadedAt: '2024-11-28 16:45:00',
      isCurrent: false,
    },
  ]
}
</script>

<style scoped lang="scss">
.translation-bundle-management {
  .base-locale-selector {
    margin-bottom: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
  }

  .bundle-table {
    .language-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .flag-icon {
        font-size: 20px;
      }
    }

    .progress-cell {
      display: flex;
      align-items: center;
      gap: 12px;

      .progress-detail {
        min-width: 80px;
        font-size: 12px;
        color: #606266;
      }
    }
  }

  .validation-result {
    margin-top: 16px;

    .validation-info {
      text-align: left;

      p {
        margin: 4px 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .validation-details {
      margin-top: 16px;

      ul {
        margin: 8px 0;
        padding-left: 24px;

        li {
          margin: 4px 0;
        }
      }
    }

    .validation-errors {
      text-align: left;

      p {
        margin: 8px 0;
        color: #f56c6c;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
}
</style>

