<template>
  <div class="language-list">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增语言
      </el-button>
      <el-button @click="handleBatchEnable" :disabled="selectedIds.length === 0">
        <el-icon><Check /></el-icon>
        批量启用
      </el-button>
      <el-button @click="handleBatchDisable" :disabled="selectedIds.length === 0">
        <el-icon><Close /></el-icon>
        批量停用
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-form :inline="true" class="filter-form">
      <el-form-item label="搜索">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索语言名称或Locale"
          clearable
          style="width: 200px"
          @input="handleFilter"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="filters.status"
          placeholder="全部"
          style="width: 120px"
          @change="handleFilter"
        >
          <el-option label="全部" value="all" />
          <el-option label="启用" value="enabled" />
          <el-option label="停用" value="disabled" />
        </el-select>
      </el-form-item>
      <el-form-item label="文本方向">
        <el-select
          v-model="filters.direction"
          placeholder="全部"
          style="width: 120px"
          @change="handleFilter"
        >
          <el-option label="全部" value="all" />
          <el-option label="LTR (左到右)" value="ltr" />
          <el-option label="RTL (右到左)" value="rtl" />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 语言列表表格 -->
    <el-table
      v-loading="loading"
      :data="languageList"
      @selection-change="handleSelectionChange"
      row-key="id"
      stripe
      class="language-table"
    >
      <el-table-column type="selection" width="55" />
      
      <el-table-column label="国旗" width="80" align="center">
        <template #default="{ row }">
          <span class="flag-icon" :title="row.name">{{ row.flagIcon || '🏳️' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="语言名称" prop="name" min-width="150">
        <template #default="{ row }">
          <div class="language-name">
            <span>{{ row.name }}</span>
            <el-tag v-if="row.isDefault" type="warning" size="small" style="margin-left: 8px">
              <el-icon><Star /></el-icon>
              默认
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Locale" prop="locale" width="100" />

      <el-table-column label="文本方向" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.textDirection === 'ltr' ? 'info' : 'warning'" size="small">
            {{ row.textDirection.toUpperCase() }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="翻译进度" width="180">
        <template #default="{ row }">
          <div class="progress-wrapper">
            <el-progress
              :percentage="row.translationProgress || 0"
              :color="getProgressColor(row.translationProgress || 0)"
              :stroke-width="8"
            />
            <span class="progress-text">{{ row.translationProgress || 0 }}%</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="缺失Key" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.missingKeysCount > 0 ? 'danger' : 'success'"
            size="small"
            style="cursor: pointer"
            @click="handleViewMissing(row)"
          >
            {{ row.missingKeysCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isEnabled"
            :disabled="row.isDefault"
            :loading="row.switching"
            @change="handleToggleStatus(row)"
          />
        </template>
      </el-table-column>

      <el-table-column label="版本" prop="version" width="80" />

      <el-table-column label="更新时间" width="160">
        <template #default="{ row }">
          <div class="time-info">
            <div>{{ formatDate(row.updatedAt) }}</div>
            <div class="update-by" v-if="row.updatedBy">
              <el-text type="info" size="small">{{ row.updatedBy }}</el-text>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.isDefault"
            type="warning"
            size="small"
            link
            @click="handleSetDefault(row)"
          >
            设为默认
          </el-button>
          <el-button type="primary" size="small" link @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchLanguageList"
      @current-change="fetchLanguageList"
      class="pagination"
    />

    <!-- 新增/编辑语言对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增语言' : '编辑语言'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
      >
        <el-form-item label="语言名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入语言的本地化名称，如 中文、English"
              maxlength="50"
              show-word-limit
            />
          <el-text type="info" size="small">
            使用该语言的本地化名称，不要翻译
          </el-text>
        </el-form-item>

        <el-form-item label="Locale 代码" prop="locale">
          <el-input
            v-model="formData.locale"
            placeholder="如：zh-CN, en-US, es-MX"
            maxlength="10"
            :disabled="dialogMode === 'edit'"
          >
            <template #append>
              <el-button @click="showLocaleHelper">
                <el-icon><QuestionFilled /></el-icon>
                常用Locale
              </el-button>
            </template>
          </el-input>
          <el-text type="warning" size="small" v-if="dialogMode === 'edit'">
            已有数据的Locale不允许修改
          </el-text>
        </el-form-item>

        <el-form-item label="国旗图标">
          <el-input
            v-model="formData.flagIcon"
            placeholder="可输入Emoji国旗，如🇨🇳"
            style="width: 200px"
          />
          <span class="flag-preview" v-if="formData.flagIcon">
            预览: {{ formData.flagIcon }}
          </span>
        </el-form-item>

        <el-form-item label="文本方向" prop="textDirection">
          <el-radio-group v-model="formData.textDirection">
            <el-radio value="ltr">LTR (左到右)</el-radio>
            <el-radio value="rtl">RTL (右到左)</el-radio>
          </el-radio-group>
          <div v-if="formData.textDirection === 'rtl'">
            <el-alert
              type="warning"
              :closable="false"
              show-icon
              style="margin-top: 8px"
            >
              <template #title>
                RTL语言会影响整体界面布局方向，请确保前端已支持RTL适配
              </template>
            </el-alert>
          </div>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="formData.isEnabled" />
          <el-text type="info" size="small" style="margin-left: 8px">
            启用后前台用户可选择此语言
          </el-text>
        </el-form-item>

        <el-form-item label="翻译负责人">
          <el-input
            v-model="formData.responsibleUserId"
            placeholder="可选"
            type="number"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            :rows="3"
            placeholder="可输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 常用Locale参考对话框 -->
    <el-dialog
      v-model="localeHelperVisible"
      title="常用Locale代码参考"
      width="700px"
    >
      <el-table :data="commonLocales" height="400">
        <el-table-column label="Locale" prop="locale" width="100" />
        <el-table-column label="语言名称" prop="name" width="150" />
        <el-table-column label="地区" prop="region" width="120" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="selectLocale(row.locale, row.name)"
            >
              使用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Check,
  Close,
  Search,
  Star,
  QuestionFilled,
} from '@element-plus/icons-vue'
import {
  getLanguageList,
  createLanguage,
  updateLanguage,
  enableLanguage,
  disableLanguage,
  batchUpdateLanguageStatus,
  setDefaultLanguage,
  deleteLanguage,
  type Language,
  type LanguageFormData,
} from '@/api/i18n'

// ==================== 响应式数据 ====================

const loading = ref(false)
const languageList = ref<Language[]>([])
const selectedIds = ref<number[]>([])

// 筛选条件
const filters = reactive({
  keyword: '',
  status: 'all' as 'all' | 'enabled' | 'disabled',
  direction: 'all' as 'all' | 'ltr' | 'rtl',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 对话框
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const currentEditId = ref<number>()

// 表单数据
const formData = reactive<LanguageFormData>({
  locale: '',
  name: '',
  flagIcon: '',
  textDirection: 'ltr',
  isEnabled: true,
  responsibleUserId: undefined,
  remarks: '',
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入语言名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
  locale: [
    { required: true, message: '请输入Locale代码', trigger: 'blur' },
    {
      pattern: /^[a-z]{2}-[A-Z]{2}$/,
      message: 'Locale格式不正确，示例：zh-CN, en-US',
      trigger: 'blur',
    },
  ],
  textDirection: [
    { required: true, message: '请选择文本方向', trigger: 'change' },
  ],
}

// Locale帮助对话框
const localeHelperVisible = ref(false)
const commonLocales = [
  { locale: 'zh-CN', name: '中文（简体）', region: '中国大陆' },
  { locale: 'zh-TW', name: '中文（繁體）', region: '台湾' },
  { locale: 'en-US', name: 'English', region: '美国' },
  { locale: 'en-GB', name: 'English', region: '英国' },
  { locale: 'es-MX', name: 'Español', region: '墨西哥' },
  { locale: 'es-ES', name: 'Español', region: '西班牙' },
  { locale: 'id-ID', name: 'Indonesia', region: '印度尼西亚' },
  { locale: 'vi-VN', name: 'Tiếng Việt', region: '越南' },
  { locale: 'th-TH', name: 'ไทย', region: '泰国' },
  { locale: 'ja-JP', name: '日本語', region: '日本' },
  { locale: 'ko-KR', name: '한국어', region: '韩国' },
  { locale: 'ar-SA', name: 'العربية', region: '沙特阿拉伯' },
  { locale: 'fr-FR', name: 'Français', region: '法国' },
  { locale: 'de-DE', name: 'Deutsch', region: '德国' },
  { locale: 'pt-BR', name: 'Português', region: '巴西' },
  { locale: 'ru-RU', name: 'Русский', region: '俄罗斯' },
]

// ==================== 生命周期 ====================

onMounted(() => {
  fetchLanguageList()
})

// ==================== 方法 ====================

/**
 * 获取语言列表
 */
async function fetchLanguageList() {
  loading.value = true
  try {
    const params = {
      keyword: filters.keyword || undefined,
      status: filters.status !== 'all' ? filters.status : undefined,
      direction: filters.direction !== 'all' ? filters.direction : undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    }

    const res = await getLanguageList(params)
    const data = Array.isArray(res) ? res : res.data || []
    
    languageList.value = data
    pagination.total = (res as any).total || data.length

    // Mock数据增强（后端未实现时使用）
    if (languageList.value.length === 0) {
      languageList.value = getMockLanguages()
      pagination.total = languageList.value.length
    }
  } catch (error) {
    console.error('获取语言列表失败:', error)
    ElMessage.error('获取语言列表失败')
    // 加载Mock数据
    languageList.value = getMockLanguages()
    pagination.total = languageList.value.length
  } finally {
    loading.value = false
  }
}

/**
 * 处理筛选
 */
function handleFilter() {
  pagination.page = 1
  fetchLanguageList()
}

/**
 * 处理选择变化
 */
function handleSelectionChange(selection: Language[]) {
  selectedIds.value = selection.map((item) => item.id)
}

/**
 * 新增语言
 */
function handleCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑语言
 */
function handleEdit(row: Language) {
  dialogMode.value = 'edit'
  currentEditId.value = row.id
  
  Object.assign(formData, {
    locale: row.locale,
    name: row.name,
    flagIcon: row.flagIcon,
    textDirection: row.textDirection,
    isEnabled: row.isEnabled,
    responsibleUserId: row.responsibleUserId,
    remarks: row.remarks,
  })
  
  dialogVisible.value = true
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (dialogMode.value === 'create') {
        await createLanguage(formData)
        ElMessage.success('新增语言成功')
      } else {
        await updateLanguage(currentEditId.value!, formData)
        ElMessage.success('编辑语言成功')
      }
      
      dialogVisible.value = false
      fetchLanguageList()
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    } finally {
      submitting.value = false
    }
  })
}

/**
 * 切换启用状态
 */
async function handleToggleStatus(row: Language) {
  if (row.isDefault && !row.isEnabled) {
    ElMessage.warning('默认语言不能停用')
    row.isEnabled = true
    return
  }

  ;(row as any).switching = true
  try {
    if (row.isEnabled) {
      await enableLanguage(row.id)
      ElMessage.success('已启用')
    } else {
      await disableLanguage(row.id)
      ElMessage.success('已停用')
    }
  } catch (error) {
    console.error('操作失败:', error)
    row.isEnabled = !row.isEnabled
    ElMessage.error('操作失败')
  } finally {
    ;(row as any).switching = false
  }
}

/**
 * 批量启用
 */
async function handleBatchEnable() {
  try {
    await batchUpdateLanguageStatus(selectedIds.value, true)
    ElMessage.success('批量启用成功')
    fetchLanguageList()
  } catch (error) {
    console.error('批量启用失败:', error)
    ElMessage.error('批量启用失败')
  }
}

/**
 * 批量停用
 */
async function handleBatchDisable() {
  // 检查是否包含默认语言
  const hasDefault = languageList.value.some(
    (lang) => selectedIds.value.includes(lang.id) && lang.isDefault
  )
  
  if (hasDefault) {
    ElMessage.warning('默认语言不能停用')
    return
  }

  try {
    await batchUpdateLanguageStatus(selectedIds.value, false)
    ElMessage.success('批量停用成功')
    fetchLanguageList()
  } catch (error) {
    console.error('批量停用失败:', error)
    ElMessage.error('批量停用失败')
  }
}

/**
 * 设为默认语言
 */
async function handleSetDefault(row: Language) {
  if (!row.isEnabled) {
    ElMessage.warning('请先启用该语言')
    return
  }

  const currentDefault = languageList.value.find((lang) => lang.isDefault)
  
  await ElMessageBox.confirm(
    `将 ${row.name} (${row.locale}) 设为系统默认语言？<br><br>
    <strong>影响范围：</strong><br>
    • 未登录用户首次访问时显示此语言<br>
    • 用户选择的语言不可用时回退到此语言<br>
    • 新注册租户的默认语言<br><br>
    当前默认语言：${currentDefault?.name} (${currentDefault?.locale})`,
    '设置默认语言',
    {
      confirmButtonText: '确认设置',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }
  )

  try {
    await setDefaultLanguage(row.id)
    ElMessage.success('已设置为默认语言')
    fetchLanguageList()
  } catch (error) {
    console.error('设置失败:', error)
    ElMessage.error('设置失败')
  }
}

/**
 * 删除语言
 */
async function handleDelete(row: Language) {
  if (row.isDefault) {
    ElMessage.warning('默认语言不能删除')
    return
  }

  await ElMessageBox.confirm(
    `确定要删除 ${row.name} (${row.locale}) 吗？<br><br>
    <strong style="color: red;">危险操作：</strong><br>
    • 该语言的所有翻译数据将被删除<br>
    • 已选择此语言的用户将回退到默认语言<br>
    • 此操作不可撤销<br><br>
    建议：停用语言而非删除`,
    '删除语言',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'error',
      dangerouslyUseHTMLString: true,
    }
  )

  try {
    await deleteLanguage(row.id)
    ElMessage.success('删除成功')
    fetchLanguageList()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

/**
 * 查看缺失翻译
 */
function handleViewMissing(row: Language) {
  // 切换到翻译统计Tab，并定位到该语言的缺失列表
  // 这里通过emit通知父组件切换Tab
  emit('view-missing', row.locale)
}

/**
 * 显示Locale帮助
 */
function showLocaleHelper() {
  localeHelperVisible.value = true
}

/**
 * 选择Locale
 */
function selectLocale(locale: string, name: string) {
  formData.locale = locale
  formData.name = name
  localeHelperVisible.value = false
}

/**
 * 重置表单
 */
function resetForm() {
  Object.assign(formData, {
    locale: '',
    name: '',
    flagIcon: '',
    textDirection: 'ltr',
    isEnabled: true,
    responsibleUserId: undefined,
    remarks: '',
  })
  formRef.value?.clearValidate()
}

/**
 * 对话框关闭
 */
function handleDialogClose() {
  resetForm()
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
 * 获取Mock数据（后端未实现时使用）
 */
function getMockLanguages(): Language[] {
  return [
    {
      id: 1,
      locale: 'zh-CN',
      name: '中文',
      flagIcon: '🇨🇳',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: true,
      sortOrder: 1,
      translationProgress: 100,
      missingKeysCount: 0,
      version: 'v2.3',
      updatedAt: '2024-12-03 14:30:00',
      updatedBy: 1,
    },
    {
      id: 2,
      locale: 'en-US',
      name: 'English',
      flagIcon: '🇺🇸',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: false,
      sortOrder: 2,
      translationProgress: 85,
      missingKeysCount: 156,
      version: 'v2.1',
      updatedAt: '2024-12-01 10:15:00',
      updatedBy: 2,
    },
    {
      id: 3,
      locale: 'es-MX',
      name: 'Español',
      flagIcon: '🇲🇽',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: false,
      sortOrder: 3,
      translationProgress: 62,
      missingKeysCount: 398,
      version: 'v1.8',
      updatedAt: '2024-11-28 16:45:00',
      updatedBy: 3,
    },
    {
      id: 4,
      locale: 'id-ID',
      name: 'Indonesia',
      flagIcon: '🇮🇩',
      textDirection: 'ltr',
      isEnabled: false,
      isDefault: false,
      sortOrder: 4,
      translationProgress: 35,
      missingKeysCount: 679,
      version: 'v1.2',
      updatedAt: '2024-11-20 09:00:00',
      updatedBy: 4,
    },
  ]
}

// Emit事件
const emit = defineEmits<{
  (e: 'view-missing', locale: string): void
}>()
</script>

<style scoped lang="scss">
.language-list {
  .toolbar {
    margin-bottom: 16px;
  }

  .filter-form {
    background: #f5f7fa;
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 4px;
  }

  .language-table {
    margin-bottom: 16px;

    .flag-icon {
      font-size: 24px;
      cursor: pointer;
    }

    .language-name {
      display: flex;
      align-items: center;
    }

    .progress-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;

      .progress-text {
        min-width: 40px;
        text-align: right;
        font-size: 12px;
        color: #606266;
      }
    }

    .time-info {
      font-size: 12px;
      
      .update-by {
        margin-top: 4px;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
  }

  .flag-preview {
    margin-left: 12px;
    font-size: 20px;
  }
}
</style>

