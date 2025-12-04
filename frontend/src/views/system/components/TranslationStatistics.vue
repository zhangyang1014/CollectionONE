<template>
  <div class="translation-statistics">
    <!-- 总览卡片 -->
    <el-row :gutter="16" class="overview-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="支持语言" :value="statistics.totalLanguages">
            <template #prefix>
              <el-icon color="#409eff"><DataAnalysis /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总翻译Key" :value="statistics.totalKeys">
            <template #prefix>
              <el-icon color="#67c23a"><Key /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="平均完成度" :value="statistics.averageProgress" suffix="%">
            <template #prefix>
              <el-icon color="#e6a23c"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="待翻译项" :value="statistics.totalMissing">
            <template #prefix>
              <el-icon color="#f56c6c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各语言翻译进度 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><DataAnalysis /></el-icon> 各语言翻译进度</span>
          <el-button @click="refreshStatistics" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <div class="language-progress-list">
        <div
          v-for="item in languageProgress"
          :key="item.locale"
          class="progress-item"
        >
          <div class="language-info">
            <span class="flag-icon">{{ item.flagIcon || '🏳️' }}</span>
            <span class="language-name">{{ item.languageName }}</span>
            <span class="locale-code">{{ item.locale }}</span>
          </div>
          <div class="progress-bar">
            <el-progress
              :percentage="item.translationProgress"
              :color="getProgressColor(item.translationProgress)"
              :stroke-width="20"
            >
              <span class="progress-label">
                {{ item.translationProgress }}% ({{ item.translatedKeys }}/{{ item.totalKeys }})
              </span>
            </el-progress>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 命名空间翻译完成度 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Files /></el-icon> 命名空间翻译完成度</span>
        </div>
      </template>

      <div class="namespace-chart">
        <div
          v-for="ns in namespaceProgress"
          :key="ns.namespace"
          class="namespace-item"
        >
          <div class="namespace-header">
            <span class="namespace-name">{{ ns.namespace }}</span>
            <el-tag :type="getProgressType(ns.averageProgress)" size="small">
              {{ ns.averageProgress }}%
            </el-tag>
          </div>
          <el-progress
            :percentage="ns.averageProgress"
            :color="getProgressColor(ns.averageProgress)"
            :stroke-width="12"
          />
        </div>
      </div>
    </el-card>

    <!-- 缺失翻译详情 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><DocumentRemove /></el-icon> 缺失翻译详情</span>
          <el-button
            type="success"
            @click="exportMissing"
            :disabled="!selectedLocaleForMissing || currentMissingList.length === 0"
          >
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="选择语言">
          <el-select
            v-model="selectedLocaleForMissing"
            placeholder="请选择语言"
            style="width: 200px"
            @change="fetchMissingTranslations"
          >
            <el-option
              v-for="lang in languageProgress"
              :key="lang.locale"
              :label="`${lang.flagIcon || ''} ${lang.languageName} (${lang.locale})`"
              :value="lang.locale"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="命名空间">
          <el-select
            v-model="missingFilters.namespace"
            placeholder="全部"
            clearable
            style="width: 150px"
            @change="fetchMissingTranslations"
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="ns in namespaceProgress"
              :key="ns.namespace"
              :label="ns.namespace"
              :value="ns.namespace"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select
            v-model="missingFilters.priority"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="fetchMissingTranslations"
          >
            <el-option label="全部" value="all" />
            <el-option label="P0 (核心)" value="P0" />
            <el-option label="P1 (重要)" value="P1" />
            <el-option label="P2 (次要)" value="P2" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="missingFilters.keyword"
            placeholder="搜索翻译Key"
            clearable
            style="width: 200px"
            @input="fetchMissingTranslations"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>

      <!-- 缺失列表表格 -->
      <el-table
        v-loading="loadingMissing"
        :data="currentMissingList"
        stripe
        :empty-text="selectedLocaleForMissing ? '该语言没有缺失翻译' : '请先选择语言'"
      >
        <el-table-column label="翻译Key" prop="keyPath" min-width="250" />
        
        <el-table-column label="基准值(zh-CN)" prop="baseValue" min-width="200">
          <template #default="{ row }">
            <el-text type="info">{{ row.baseValue }}</el-text>
          </template>
        </el-table-column>

        <el-table-column label="命名空间" prop="namespace" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.namespace }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="优先级" prop="priority" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.priority === 'P0' ? 'danger' : row.priority === 'P1' ? 'warning' : 'info'"
              size="small"
            >
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="selectedLocaleForMissing"
        v-model:current-page="missingPagination.page"
        v-model:page-size="missingPagination.pageSize"
        :total="missingPagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchMissingTranslations"
        @current-change="fetchMissingTranslations"
        class="pagination"
      />
    </el-card>

    <!-- 翻译质量问题 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><WarnTriangleFilled /></el-icon> 翻译质量问题</span>
          <el-button
            @click="recheckQualityIssues"
            :loading="loadingQuality"
            :disabled="!selectedLocaleForQuality"
          >
            <el-icon><Refresh /></el-icon>
            重新检测
          </el-button>
        </div>
      </template>

      <!-- 语言选择 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="选择语言">
          <el-select
            v-model="selectedLocaleForQuality"
            placeholder="请选择语言"
            style="width: 200px"
            @change="fetchQualityIssues"
          >
            <el-option
              v-for="lang in languageProgress"
              :key="lang.locale"
              :label="`${lang.flagIcon || ''} ${lang.languageName} (${lang.locale})`"
              :value="lang.locale"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 质量问题汇总 -->
      <div v-if="selectedLocaleForQuality && qualityIssueSummary.total > 0" class="quality-summary">
        <el-alert
          type="warning"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <template #title>
            <strong>发现 {{ qualityIssueSummary.total }} 个潜在问题</strong>
          </template>
        </el-alert>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="缺少变量占位符">
            <el-tag type="warning">{{ qualityIssueSummary.missing_variable || 0 }}</el-tag>
            <el-button
              type="primary"
              size="small"
              link
              @click="filterQualityIssues('missing_variable')"
            >
              查看详情
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="长度超出建议">
            <el-tag type="info">{{ qualityIssueSummary.length_exceeded || 0 }}</el-tag>
            <el-button
              type="primary"
              size="small"
              link
              @click="filterQualityIssues('length_exceeded')"
            >
              查看详情
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="包含HTML标签">
            <el-tag type="warning">{{ qualityIssueSummary.html_tag || 0 }}</el-tag>
            <el-button
              type="primary"
              size="small"
              link
              @click="filterQualityIssues('html_tag')"
            >
              查看详情
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="复数形式缺失">
            <el-tag type="warning">{{ qualityIssueSummary.plural_missing || 0 }}</el-tag>
            <el-button
              type="primary"
              size="small"
              link
              @click="filterQualityIssues('plural_missing')"
            >
              查看详情
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="疑似未翻译">
            <el-tag type="danger">{{ qualityIssueSummary.untranslated || 0 }}</el-tag>
            <el-button
              type="primary"
              size="small"
              link
              @click="filterQualityIssues('untranslated')"
            >
              查看详情
            </el-button>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-empty
        v-else-if="selectedLocaleForQuality"
        description="未发现质量问题"
        :image-size="100"
      />

      <el-empty
        v-else
        description="请先选择语言"
        :image-size="100"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Key,
  TrendCharts,
  Warning,
  DataAnalysis,
  Refresh,
  Files,
  DocumentRemove,
  Download,
  Search,
  WarnTriangleFilled,
} from '@element-plus/icons-vue'
import {
  getTranslationStatistics,
  getNamespaceStatistics,
  getMissingTranslations,
  exportMissingTranslations,
  getQualityIssues,
  recheckQuality,
  type MissingTranslation,
  type QualityIssue,
} from '@/api/i18n'

// ==================== Props ====================

interface Props {
  initialLocale?: string // 从其他Tab跳转过来时指定的locale
}

const props = defineProps<Props>()

// ==================== 响应式数据 ====================

const loading = ref(false)

// 总览统计
const statistics = reactive({
  totalLanguages: 0,
  totalKeys: 0,
  averageProgress: 0,
  totalMissing: 0,
})

// 各语言进度
interface LanguageProgress {
  locale: string
  languageName: string
  flagIcon?: string
  totalKeys: number
  translatedKeys: number
  missingKeys: number
  translationProgress: number
  lastUpdatedAt?: string
}

const languageProgress = ref<LanguageProgress[]>([])

// 命名空间进度
interface NamespaceProgress {
  namespace: string
  averageProgress: number
  totalKeys: number
  translatedKeys: number
}

const namespaceProgress = ref<NamespaceProgress[]>([])

// 缺失翻译
const selectedLocaleForMissing = ref<string>(props.initialLocale || '')
const loadingMissing = ref(false)
const currentMissingList = ref<MissingTranslation[]>([])
const missingFilters = reactive({
  namespace: '',
  priority: 'all' as 'all' | 'P0' | 'P1' | 'P2',
  keyword: '',
})
const missingPagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 质量问题
const selectedLocaleForQuality = ref<string>('')
const loadingQuality = ref(false)
const qualityIssues = ref<QualityIssue[]>([])
const qualityIssueSummary = computed(() => {
  const summary: Record<string, number> = {
    missing_variable: 0,
    length_exceeded: 0,
    html_tag: 0,
    plural_missing: 0,
    untranslated: 0,
    total: 0,
  }

  qualityIssues.value.forEach((issue) => {
    summary[issue.type] = (summary[issue.type] || 0) + 1
    summary.total++
  })

  return summary
})

// ==================== 生命周期 ====================

onMounted(async () => {
  await fetchStatistics()
  
  // 如果有初始locale，自动加载缺失翻译
  if (props.initialLocale) {
    await fetchMissingTranslations()
  }
})

// ==================== 方法 ====================

/**
 * 获取统计数据
 */
async function fetchStatistics() {
  loading.value = true
  try {
    // 获取总览统计
    const statsRes = await getTranslationStatistics()
    const statsData = statsRes.data || statsRes
    
    Object.assign(statistics, statsData)

    // 获取各语言进度
    const progressData = Array.isArray(statsData.languageProgress)
      ? statsData.languageProgress
      : []
    languageProgress.value = progressData

    // 获取命名空间统计
    const nsRes = await getNamespaceStatistics()
    const nsData = Array.isArray(nsRes.data || nsRes) ? (nsRes.data || nsRes) : []
    namespaceProgress.value = nsData

    // Mock数据（后端未实现时）
    if (languageProgress.value.length === 0) {
      statistics.totalLanguages = 4
      statistics.totalKeys = 1200
      statistics.averageProgress = 78
      statistics.totalMissing = 1840

      languageProgress.value = getMockLanguageProgress()
      namespaceProgress.value = getMockNamespaceProgress()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用Mock数据
    statistics.totalLanguages = 4
    statistics.totalKeys = 1200
    statistics.averageProgress = 78
    statistics.totalMissing = 1840

    languageProgress.value = getMockLanguageProgress()
    namespaceProgress.value = getMockNamespaceProgress()
  } finally {
    loading.value = false
  }
}

/**
 * 刷新统计
 */
async function refreshStatistics() {
  await fetchStatistics()
  ElMessage.success('已刷新统计数据')
}

/**
 * 获取缺失翻译列表
 */
async function fetchMissingTranslations() {
  if (!selectedLocaleForMissing.value) return

  loadingMissing.value = true
  try {
    const params = {
      namespace: missingFilters.namespace || undefined,
      priority: missingFilters.priority !== 'all' ? missingFilters.priority : undefined,
      keyword: missingFilters.keyword || undefined,
      page: missingPagination.page,
      pageSize: missingPagination.pageSize,
    }

    const res = await getMissingTranslations(selectedLocaleForMissing.value, params)
    const data = Array.isArray(res) ? res : res.data || []
    
    currentMissingList.value = data
    missingPagination.total = (res as any).total || data.length

    // Mock数据
    if (currentMissingList.value.length === 0 && selectedLocaleForMissing.value === 'en-US') {
      currentMissingList.value = getMockMissingTranslations()
      missingPagination.total = currentMissingList.value.length
    }
  } catch (error) {
    console.error('获取缺失翻译列表失败:', error)
    currentMissingList.value = getMockMissingTranslations()
    missingPagination.total = currentMissingList.value.length
  } finally {
    loadingMissing.value = false
  }
}

/**
 * 导出缺失翻译
 */
async function exportMissing() {
  if (!selectedLocaleForMissing.value) return

  try {
    const response = await exportMissingTranslations(selectedLocaleForMissing.value, 'excel')
    
    const blob = response.data || response
    const url = window.URL.createObjectURL(blob as Blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${selectedLocaleForMissing.value}-missing-translations.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

/**
 * 获取质量问题
 */
async function fetchQualityIssues() {
  if (!selectedLocaleForQuality.value) return

  loadingQuality.value = true
  try {
    const res = await getQualityIssues(selectedLocaleForQuality.value)
    const data = Array.isArray(res) ? res : res.data || []
    
    qualityIssues.value = data

    // Mock数据
    if (qualityIssues.value.length === 0 && selectedLocaleForQuality.value === 'en-US') {
      qualityIssues.value = getMockQualityIssues()
    }
  } catch (error) {
    console.error('获取质量问题失败:', error)
    qualityIssues.value = getMockQualityIssues()
  } finally {
    loadingQuality.value = false
  }
}

/**
 * 重新检测质量问题
 */
async function recheckQualityIssues() {
  if (!selectedLocaleForQuality.value) return

  loadingQuality.value = true
  try {
    await recheckQuality(selectedLocaleForQuality.value)
    await fetchQualityIssues()
    ElMessage.success('质量检测完成')
  } catch (error) {
    console.error('检测失败:', error)
    ElMessage.error('检测失败')
  } finally {
    loadingQuality.value = false
  }
}

/**
 * 筛选质量问题
 */
function filterQualityIssues(type: string) {
  // 这里可以展开详细列表或跳转到详情页
  ElMessage.info(`查看 ${type} 类型的问题`)
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
 * 获取进度类型
 */
function getProgressType(percentage: number): 'danger' | 'warning' | 'success' | 'info' {
  if (percentage < 50) return 'danger'
  if (percentage < 90) return 'warning'
  return 'success'
}

/**
 * 获取Mock语言进度
 */
function getMockLanguageProgress(): LanguageProgress[] {
  return [
    {
      locale: 'zh-CN',
      languageName: '中文',
      flagIcon: '🇨🇳',
      totalKeys: 1200,
      translatedKeys: 1200,
      missingKeys: 0,
      translationProgress: 100,
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
      lastUpdatedAt: '2024-11-20 09:00:00',
    },
  ]
}

/**
 * 获取Mock命名空间进度
 */
function getMockNamespaceProgress(): NamespaceProgress[] {
  return [
    { namespace: 'common', averageProgress: 90, totalKeys: 200, translatedKeys: 180 },
    { namespace: 'auth', averageProgress: 95, totalKeys: 80, translatedKeys: 76 },
    { namespace: 'dashboard', averageProgress: 78, totalKeys: 150, translatedKeys: 117 },
    { namespace: 'case', averageProgress: 68, totalKeys: 300, translatedKeys: 204 },
    { namespace: 'payment', averageProgress: 45, totalKeys: 120, translatedKeys: 54 },
    { namespace: 'field', averageProgress: 72, totalKeys: 100, translatedKeys: 72 },
    { namespace: 'tenant', averageProgress: 80, totalKeys: 150, translatedKeys: 120 },
    { namespace: 'organization', averageProgress: 65, totalKeys: 100, translatedKeys: 65 },
  ]
}

/**
 * 获取Mock缺失翻译
 */
function getMockMissingTranslations(): MissingTranslation[] {
  return [
    {
      keyPath: 'payment.method.alipay',
      baseValue: '支付宝',
      namespace: 'payment',
      priority: 'P1',
    },
    {
      keyPath: 'payment.method.wechat',
      baseValue: '微信支付',
      namespace: 'payment',
      priority: 'P1',
    },
    {
      keyPath: 'case.status.overdue',
      baseValue: '逾期',
      namespace: 'case',
      priority: 'P0',
    },
    {
      keyPath: 'dashboard.chart.title',
      baseValue: '数据趋势',
      namespace: 'dashboard',
      priority: 'P2',
    },
    {
      keyPath: 'common.action.confirm',
      baseValue: '确认',
      namespace: 'common',
      priority: 'P0',
    },
  ]
}

/**
 * 获取Mock质量问题
 */
function getMockQualityIssues(): QualityIssue[] {
  return [
    {
      type: 'missing_variable',
      keyPath: 'common.message.welcome',
      baseValue: '欢迎, {username}!',
      translatedValue: 'Welcome!',
      suggestion: 'Welcome, {username}!',
    },
    {
      type: 'length_exceeded',
      keyPath: 'dashboard.description.revenue',
      baseValue: '收入趋势',
      translatedValue: 'The trend of revenue in recent months',
    },
    {
      type: 'html_tag',
      keyPath: 'auth.message.error',
      baseValue: '登录失败',
      translatedValue: '<b>Login failed',
    },
    {
      type: 'plural_missing',
      keyPath: 'case.count',
      baseValue: '{count} 个案件',
      translatedValue: '{count} cases',
      suggestion: 'no cases | one case | {count} cases',
    },
    {
      type: 'untranslated',
      keyPath: 'payment.button.submit',
      baseValue: '提交',
      translatedValue: '提交',
    },
  ]
}
</script>

<style scoped lang="scss">
.translation-statistics {
  .overview-cards {
    margin-bottom: 24px;
  }

  .section-card {
    margin-bottom: 24px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: bold;
      }
    }
  }

  .language-progress-list {
    .progress-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .language-info {
        display: flex;
        align-items: center;
        gap: 12px;
        min-width: 250px;

        .flag-icon {
          font-size: 24px;
        }

        .language-name {
          font-weight: 500;
          font-size: 14px;
        }

        .locale-code {
          font-size: 12px;
          color: #909399;
        }
      }

      .progress-bar {
        flex: 1;
        margin-left: 24px;

        .progress-label {
          font-size: 12px;
        }
      }
    }
  }

  .namespace-chart {
    .namespace-item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .namespace-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .namespace-name {
          font-weight: 500;
          font-size: 14px;
        }
      }
    }
  }

  .filter-form {
    background: #f5f7fa;
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 4px;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .quality-summary {
    margin-top: 16px;
  }
}
</style>

