<template>
  <div class="message-template-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>消息模板配置管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filters" inline>
        <el-form-item label="案件阶段">
          <el-select v-model="filters.caseStage" placeholder="全部阶段" clearable style="width: 140px;">
            <el-option label="全部阶段" value="" />
            <el-option label="C（催收前）" value="C" />
            <el-option label="S0（首次联系）" value="S0" />
            <el-option label="S1-3（初期）" value="S1-3" />
            <el-option label="S3+（后期）" value="S3+" />
          </el-select>
        </el-form-item>

        <el-form-item label="模板类型">
          <el-select v-model="filters.templateType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="组织模板" value="organization" />
            <el-option label="个人模板" value="personal" />
          </el-select>
        </el-form-item>

        <el-form-item label="场景">
          <el-select v-model="filters.scene" placeholder="全部场景" clearable style="width: 120px;">
            <el-option label="全部场景" value="" />
            <el-option label="问候" value="greeting" />
            <el-option label="提醒" value="reminder" />
            <el-option label="强度" value="strong" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间点">
          <el-select v-model="filters.timeSlot" placeholder="全部时间" clearable style="width: 120px;">
            <el-option label="全部时间" value="" />
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
            <el-option label="晚上" value="evening" />
          </el-select>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-select v-model="filters.isEnabled" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" :value="null" />
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input 
            v-model="filters.keyword" 
            placeholder="模板名称/内容" 
            clearable 
            style="width: 200px;"
            @keyup.enter="loadTemplates"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="templateList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="templateName" label="模板名称" width="200" />
        
        <el-table-column prop="templateType" label="模板类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.templateType === 'organization' ? 'success' : 'warning'" size="small">
              {{ row.templateType === 'organization' ? '组织模板' : '个人模板' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="agencyNames" label="适用机构" width="120">
          <template #default="{ row }">
            <el-tooltip v-if="row.agencyIds && row.agencyIds.length > 0" :content="getAgencyTooltip(row.agencyIds)" placement="top">
              <span>{{ row.agencyNames }}</span>
            </el-tooltip>
            <span v-else>{{ row.agencyNames }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="caseStage" label="案件阶段" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getCaseStageLabel(row.caseStage) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="scene" label="场景" width="80">
          <template #default="{ row }">
            {{ getSceneLabel(row.scene) }}
          </template>
        </el-table-column>

        <el-table-column prop="timeSlot" label="时间点" width="80">
          <template #default="{ row }">
            {{ getTimeSlotLabel(row.timeSlot) }}
          </template>
        </el-table-column>

        <el-table-column prop="content" label="模板内容预览" width="300">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top">
              <span class="content-preview">{{ row.content.substring(0, 50) }}{{ row.content.length > 50 ? '...' : '' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="usageCount" label="使用次数" width="80" align="center" />

        <el-table-column prop="isEnabled" label="启用状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.isEnabled" 
              @change="handleToggle(row)"
              :loading="row.toggleLoading"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTemplates"
          @current-change="loadTemplates"
        />
      </div>
    </el-card>

    <!-- 模板表单抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑模板' : '新增模板'"
      size="600px"
      :before-close="handleDrawerClose"
    >
      <el-form 
        ref="templateFormRef"
        :model="templateForm" 
        :rules="formRules"
        label-width="100px"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <div class="section-title">基础信息</div>
          
          <el-form-item label="模板名称" prop="templateName">
            <el-input 
              v-model="templateForm.templateName" 
              placeholder="请输入模板名称"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="模板类型" prop="templateType">
            <el-radio-group v-model="templateForm.templateType">
              <el-radio label="organization">组织模板</el-radio>
              <el-radio label="personal">个人模板</el-radio>
            </el-radio-group>
            <div class="form-tip">
              组织模板：选定机构的催员可见；个人模板：仅创建人可见
            </div>
          </el-form-item>

          <el-form-item 
            label="适用机构" 
            prop="agencyIds"
            v-if="templateForm.templateType === 'organization'"
          >
            <el-select 
              v-model="templateForm.agencyIds" 
              multiple 
              placeholder="请选择适用机构"
              style="width: 100%"
            >
              <el-option 
                v-for="agency in agencyOptions" 
                :key="agency.id" 
                :label="agency.name" 
                :value="agency.id" 
              />
            </el-select>
            <div class="form-actions">
              <el-button link type="primary" @click="selectAllAgencies">全选</el-button>
              <el-button link @click="clearAgencies">清空</el-button>
            </div>
          </el-form-item>
        </div>

        <!-- 分类维度 -->
        <div class="form-section">
          <div class="section-title">分类维度</div>

          <el-form-item label="案件阶段" prop="caseStage">
            <el-select v-model="templateForm.caseStage" placeholder="请选择案件阶段" style="width: 100%">
              <el-option label="C（催收前）" value="C" />
              <el-option label="S0（首次联系）" value="S0" />
              <el-option label="S1-3（初期）" value="S1-3" />
              <el-option label="S3+（后期）" value="S3+" />
            </el-select>
          </el-form-item>

          <el-form-item label="场景" prop="scene">
            <el-select v-model="templateForm.scene" placeholder="请选择场景" style="width: 100%">
              <el-option label="问候" value="greeting" />
              <el-option label="提醒" value="reminder" />
              <el-option label="强度" value="strong" />
            </el-select>
          </el-form-item>

          <el-form-item label="时间点" prop="timeSlot">
            <el-select v-model="templateForm.timeSlot" placeholder="请选择时间点" style="width: 100%">
              <el-option label="上午（8:00-12:00）" value="morning" />
              <el-option label="下午（12:00-18:00）" value="afternoon" />
              <el-option label="晚上（18:00-22:00）" value="evening" />
            </el-select>
          </el-form-item>
        </div>

        <!-- 模板内容 -->
        <div class="form-section">
          <div class="section-title">模板内容</div>

          <el-form-item label="可用变量">
            <div class="variable-tags">
              <el-tag 
                v-for="variable in availableVariables" 
                :key="variable.key"
                class="variable-tag"
                @click="insertVariable(variable.key)"
                style="cursor: pointer; margin: 4px;"
              >
                {{ variable.key }}
              </el-tag>
            </div>
            <div class="form-tip">点击变量标签插入到模板内容</div>
          </el-form-item>

          <el-form-item label="模板内容" prop="content">
            <el-input 
              ref="contentInputRef"
              v-model="templateForm.content" 
              type="textarea"
              :rows="6"
              placeholder="请输入模板内容，可使用变量如：{客户名}、{贷款编号}、{逾期天数}、{应还金额}、{产品名称} 等"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="内容预览">
            <div class="content-preview-box">
              {{ renderPreview(templateForm.content) }}
            </div>
          </el-form-item>
        </div>

        <!-- 状态配置 -->
        <div class="form-section">
          <div class="section-title">状态配置</div>

          <el-form-item label="启用状态">
            <el-switch v-model="templateForm.isEnabled" />
            <span style="margin-left: 10px; color: #909399;">
              {{ templateForm.isEnabled ? '启用' : '禁用' }}
            </span>
          </el-form-item>

          <el-form-item label="排序权重" prop="sortOrder">
            <el-input-number 
              v-model="templateForm.sortOrder" 
              :min="0" 
              :max="9999"
              placeholder="数值越小越靠前"
            />
            <div class="form-tip">数值越小，模板在列表中排序越靠前</div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="handleDrawerClose">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// ==================== 数据定义 ====================

interface MessageTemplate {
  id?: number
  tenantId: number
  templateName: string
  templateType: string
  agencyIds: number[] | null
  agencyNames?: string
  caseStage: string
  scene: string
  timeSlot: string
  content: string
  variables: string[]
  isEnabled: boolean
  sortOrder: number
  usageCount?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: number
  createdByName?: string
  toggleLoading?: boolean
}

interface Variable {
  name: string
  key: string
  type: string
  example: string
  description: string
}

interface Agency {
  id: number
  name: string
}

// 列表数据
const templateList = ref<MessageTemplate[]>([])
const loading = ref(false)

// 筛选条件
const filters = reactive({
  caseStage: '',
  templateType: '',
  scene: '',
  timeSlot: '',
  isEnabled: null as boolean | null,
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 表单
const drawerVisible = ref(false)
const isEdit = ref(false)
const templateFormRef = ref<FormInstance>()
const contentInputRef = ref()
const saveLoading = ref(false)

const templateForm = reactive<MessageTemplate>({
  tenantId: 1, // 从登录信息获取
  templateName: '',
  templateType: 'organization',
  agencyIds: null,
  caseStage: '',
  scene: '',
  timeSlot: '',
  content: '',
  variables: [],
  isEnabled: true,
  sortOrder: 0
})

// 可用变量列表
const availableVariables = ref<Variable[]>([])

// 机构列表（Mock数据）
const agencyOptions = ref<Agency[]>([
  { id: 1, name: '催收一部' },
  { id: 2, name: '催收二部' },
  { id: 3, name: '催收三部' },
  { id: 4, name: '催收四部' }
])

// 表单校验规则
const formRules: FormRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  agencyIds: [
    { 
      required: true, 
      message: '请选择适用机构', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (templateForm.templateType === 'organization' && (!value || value.length === 0)) {
          callback(new Error('请至少选择一个机构'))
        } else {
          callback()
        }
      }
    }
  ],
  caseStage: [
    { required: true, message: '请选择案件阶段', trigger: 'change' }
  ],
  scene: [
    { required: true, message: '请选择场景', trigger: 'change' }
  ],
  timeSlot: [
    { required: true, message: '请选择时间点', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入模板内容', trigger: 'blur' },
    { max: 1000, message: '内容不能超过1000个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', message: '排序权重必须是数字', trigger: 'blur' }
  ]
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadTemplates()
  loadVariables()
})

// ==================== 方法 ====================

/**
 * 加载模板列表
 */
const loadTemplates = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      tenantId: 1 // 从登录信息获取
    }

    if (filters.caseStage) params.caseStage = filters.caseStage
    if (filters.templateType) params.templateType = filters.templateType
    if (filters.scene) params.scene = filters.scene
    if (filters.timeSlot) params.timeSlot = filters.timeSlot
    if (filters.isEnabled !== null) params.isEnabled = filters.isEnabled
    if (filters.keyword) params.keyword = filters.keyword

    const response = await request({
      url: '/api/v1/console/message-templates',
      method: 'get',
      params
    })

    const result = response.data || response
    templateList.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载可用变量
 */
const loadVariables = async () => {
  try {
    const response = await request({
      url: '/api/v1/message-templates/variables',
      method: 'get'
    })

    availableVariables.value = response.data || response || []
  } catch (error) {
    console.error('加载变量列表失败:', error)
  }
}

/**
 * 查询
 */
const handleSearch = () => {
  pagination.page = 1
  loadTemplates()
}

/**
 * 重置筛选条件
 */
const handleReset = () => {
  filters.caseStage = ''
  filters.templateType = ''
  filters.scene = ''
  filters.timeSlot = ''
  filters.isEnabled = null
  filters.keyword = ''
  pagination.page = 1
  loadTemplates()
}

/**
 * 新增模板
 */
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  drawerVisible.value = true
}

/**
 * 编辑模板
 */
const handleEdit = (row: MessageTemplate) => {
  isEdit.value = true
  Object.assign(templateForm, {
    id: row.id,
    tenantId: row.tenantId,
    templateName: row.templateName,
    templateType: row.templateType,
    agencyIds: row.agencyIds ? [...row.agencyIds] : null,
    caseStage: row.caseStage,
    scene: row.scene,
    timeSlot: row.timeSlot,
    content: row.content,
    variables: row.variables ? [...row.variables] : [],
    isEnabled: row.isEnabled,
    sortOrder: row.sortOrder
  })
  drawerVisible.value = true
}

/**
 * 删除模板
 */
const handleDelete = (row: MessageTemplate) => {
  ElMessageBox.confirm(
    `确定删除模板【${row.templateName}】吗？删除后催员将无法继续使用此模板。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/api/v1/console/message-templates/${row.id}`,
        method: 'delete',
        params: { tenantId: row.tenantId }
      })

      ElMessage.success('删除成功')
      loadTemplates()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 切换启用状态
 */
const handleToggle = async (row: MessageTemplate) => {
  row.toggleLoading = true
  try {
    await request({
      url: `/api/v1/console/message-templates/${row.id}/toggle`,
      method: 'patch',
      data: {
        tenantId: row.tenantId,
        isEnabled: row.isEnabled
      }
    })

    ElMessage.success(row.isEnabled ? '已启用' : '已禁用')
  } catch (error) {
    console.error('切换状态失败:', error)
    row.isEnabled = !row.isEnabled // 回滚状态
    ElMessage.error('切换状态失败')
  } finally {
    row.toggleLoading = false
  }
}

/**
 * 保存模板
 */
const handleSave = async () => {
  if (!templateFormRef.value) return

  await templateFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }

    saveLoading.value = true
    try {
      // 提取模板中使用的变量
      const usedVariables = extractVariables(templateForm.content)
      templateForm.variables = usedVariables

      const url = isEdit.value 
        ? `/api/v1/console/message-templates/${templateForm.id}`
        : '/api/v1/console/message-templates'
      
      const method = isEdit.value ? 'put' : 'post'

      const requestData: any = {
        tenantId: templateForm.tenantId,
        templateName: templateForm.templateName,
        templateType: templateForm.templateType,
        agencyIds: templateForm.templateType === 'organization' ? templateForm.agencyIds : null,
        caseStage: templateForm.caseStage,
        scene: templateForm.scene,
        timeSlot: templateForm.timeSlot,
        content: templateForm.content,
        variables: templateForm.variables,
        isEnabled: templateForm.isEnabled,
        sortOrder: templateForm.sortOrder,
        createdBy: 1, // 从登录信息获取
        updatedBy: 1
      }

      await request({
        url,
        method,
        data: requestData
      })

      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      drawerVisible.value = false
      loadTemplates()
    } catch (error: any) {
      console.error('保存失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '保存失败'
      ElMessage.error(errorMsg)
    } finally {
      saveLoading.value = false
    }
  })
}

/**
 * 关闭抽屉
 */
const handleDrawerClose = (done?: () => void) => {
  ElMessageBox.confirm('当前有未保存的修改，确定要离开吗？')
    .then(() => {
      resetForm()
      if (done) done()
      else drawerVisible.value = false
    })
    .catch(() => {
      // 取消关闭
    })
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(templateForm, {
    id: undefined,
    tenantId: 1,
    templateName: '',
    templateType: 'organization',
    agencyIds: null,
    caseStage: '',
    scene: '',
    timeSlot: '',
    content: '',
    variables: [],
    isEnabled: true,
    sortOrder: 0
  })
  templateFormRef.value?.resetFields()
}

/**
 * 全选机构
 */
const selectAllAgencies = () => {
  templateForm.agencyIds = agencyOptions.value.map(a => a.id)
}

/**
 * 清空机构
 */
const clearAgencies = () => {
  templateForm.agencyIds = []
}

/**
 * 插入变量到光标位置
 */
const insertVariable = (variableKey: string) => {
  const textarea = contentInputRef.value?.$el?.querySelector('textarea')
  if (!textarea) {
    templateForm.content += variableKey
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = templateForm.content

  templateForm.content = text.substring(0, start) + variableKey + text.substring(end)

  nextTick(() => {
    textarea.focus()
    const newPos = start + variableKey.length
    textarea.setSelectionRange(newPos, newPos)
  })
}

/**
 * 渲染内容预览
 */
const renderPreview = (content: string) => {
  if (!content) return ''

  const exampleData: { [key: string]: string } = {
    '{客户名}': '张三',
    '{贷款编号}': 'BTSK-200100',
    '{逾期天数}': '23',
    '{到期日期}': '2025-01-15',
    '{贷款金额}': '50,000',
    '{应还金额}': '10,529',
    '{本金}': '50,000',
    '{罚息}': '529',
    '{产品名称}': '快速贷',
    '{App名称}': 'MegaPeso'
  }

  let preview = content
  Object.keys(exampleData).forEach(key => {
    preview = preview.replace(new RegExp(key.replace(/[{}]/g, '\\$&'), 'g'), exampleData[key])
  })

  return preview
}

/**
 * 从内容中提取使用的变量
 */
const extractVariables = (content: string): string[] => {
  const regex = /\{([^}]+)\}/g
  const variables: string[] = []
  let match

  while ((match = regex.exec(content)) !== null) {
    if (!variables.includes(match[1])) {
      variables.push(match[1])
    }
  }

  return variables
}

/**
 * 获取案件阶段标签
 */
const getCaseStageLabel = (stage: string): string => {
  const map: { [key: string]: string } = {
    'C': 'C（催收前）',
    'S0': 'S0（首次联系）',
    'S1-3': 'S1-3（初期）',
    'S3+': 'S3+（后期）'
  }
  return map[stage] || stage
}

/**
 * 获取场景标签
 */
const getSceneLabel = (scene: string): string => {
  const map: { [key: string]: string } = {
    'greeting': '问候',
    'reminder': '提醒',
    'strong': '强度'
  }
  return map[scene] || scene
}

/**
 * 获取时间点标签
 */
const getTimeSlotLabel = (timeSlot: string): string => {
  const map: { [key: string]: string } = {
    'morning': '上午',
    'afternoon': '下午',
    'evening': '晚上'
  }
  return map[timeSlot] || timeSlot
}

/**
 * 获取机构提示信息
 */
const getAgencyTooltip = (agencyIds: number[]): string => {
  const names = agencyIds
    .map(id => agencyOptions.value.find(a => a.id === id)?.name)
    .filter(Boolean)
  return names.join('、')
}
</script>

<style scoped lang="scss">
.message-template-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .filter-card {
    margin-bottom: 16px;
  }
  
  .table-card {
    .content-preview {
      cursor: pointer;
      
      &:hover {
        color: #409EFF;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
  
  .form-section {
    margin-bottom: 24px;
    
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #EBEEF5;
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
  
  .form-actions {
    margin-top: 4px;
  }
  
  .variable-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .variable-tag {
    cursor: pointer;
    user-select: none;
    
    &:hover {
      opacity: 0.8;
      transform: scale(1.05);
    }
  }
  
  .content-preview-box {
    background-color: #F5F7FA;
    border: 1px solid #DCDFE6;
    border-radius: 4px;
    padding: 12px;
    min-height: 100px;
    color: #606266;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
  }
  
  .drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>

