<template>
  <div class="case-list-field-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="title-block">
            <span class="title">案件列表字段配置</span>
            <el-tag size="small" type="info" class="version-badge">
              基于映射配置：v{{ versionInfo.version || 0 }}
            </el-tag>
            <el-tag size="small" class="version-badge" :type="versionInfo.source === 'upload' ? 'success' : 'warning'">
              {{ versionInfo.source === 'upload' ? '来源：上传版本' : '来源：内置Mock' }}
            </el-tag>
            <span v-if="versionInfo.fetched_at" class="version-time">拉取时间：{{ versionInfo.fetched_at }}</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="handleSaveVersion" :loading="saveVersionLoading">
              保存为新版本
            </el-button>
            <el-button @click="handleShowHistory">
              版本历史
            </el-button>
          </div>
        </div>
      </template>

      <!-- 场景选择 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-select
            v-model="currentScene"
            placeholder="请选择场景"
            style="width: 100%"
            @change="handleSceneChange"
          >
            <el-option
              v-for="scene in sceneTypes"
              :key="scene.key"
              :label="scene.name"
              :value="scene.key"
            >
              <span>{{ scene.name }}</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">
                {{ scene.description }}
              </span>
            </el-option>
          </el-select>
        </el-col>
      </el-row>

      <!-- 配置表格 -->
      <el-table
        ref="tableRef"
        :data="configs"
        border
        style="width: 100%"
        v-loading="loading"
        row-key="id"
      >
        <el-table-column label="拖拽" width="60" align="center">
          <template #default>
            <el-icon class="drag-handle" style="cursor: move;">
              <Rank />
            </el-icon>
          </template>
        </el-table-column>
        
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="field_name" label="字段名称" width="150">
          <template #default="{ row }">
            <el-tag>{{ row.field_name }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="field_key" label="字段标识" width="150" />
        
        <el-table-column label="字段类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.field_data_type || '-' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="枚举值" width="200">
          <template #default="{ row }">
            <span>{{ getEnumOptionsLabel(row) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="映射类型" width="100">
          <template #default="{ row }">
            <el-tag 
              size="small" 
              :type="getFieldSourceType(row.field_source)"
            >
              {{ getFieldSourceLabel(row.field_source) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="display_width" label="显示宽度（0=自动）" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.display_width"
              :min="0"
              :max="500"
              size="small"
              placeholder="0"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="color_type" label="颜色" width="120">
          <template #default="{ row }">
            <el-select v-model="row.color_type" size="small" style="width: 100%">
              <el-option label="普通" value="normal">
                <span style="color: #606266;">普通</span>
              </el-option>
              <el-option label="红色" value="red">
                <span style="color: #f56c6c;">红色</span>
              </el-option>
              <el-option label="黄色" value="yellow">
                <span style="color: #e6a23c;">黄色</span>
              </el-option>
              <el-option label="绿色" value="green">
                <span style="color: #67c23a;">绿色</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="可筛选" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-if="isFilterableType(row.field_data_type)"
              v-model="row.is_filterable" 
              size="small" 
            />
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="范围检索" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-if="isRangeSearchableType(row.field_data_type)"
              v-model="row.is_range_searchable" 
              size="small"
            />
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form :model="form" label-width="140px" ref="formRef">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form-item label="场景类型" required>
              <el-select v-model="form.scene_type" disabled style="width: 100%">
                <el-option
                  v-for="scene in sceneTypes"
                  :key="scene.key"
                  :label="scene.name"
                  :value="scene.key"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="选择字段" required v-if="!isEditMode">
              <el-select 
                v-model="selectedFieldKey" 
                placeholder="请选择字段"
                style="width: 100%"
                filterable
                @change="handleFieldSelect"
              >
                <el-option-group
                  v-for="group in groupedFields"
                  :key="group.label"
                  :label="group.label"
                >
                  <el-option
                    v-for="field in group.options"
                    :key="field.field_key"
                    :label="`${field.field_name} (${field.field_key})`"
                    :value="field.field_key"
                  >
                    <div style="display: flex; justify-content: space-between;">
                      <span>{{ field.field_name }}</span>
                      <span style="color: #8492a6; font-size: 12px;">
                        {{ getFieldSourceLabel(field.field_source) }} | {{ field.field_type }}
                      </span>
                    </div>
                  </el-option>
                </el-option-group>
              </el-select>
            </el-form-item>
            
            <el-form-item label="字段标识" required>
              <el-input v-model="form.field_key" :disabled="isEditMode" placeholder="如：case_code" />
            </el-form-item>
            
            <el-form-item label="字段名称" required>
              <el-input v-model="form.field_name" placeholder="如：案件编号" />
            </el-form-item>
            
            <el-form-item label="字段类型">
              <el-input v-model="form.field_data_type" disabled />
            </el-form-item>
            
            <el-form-item label="字段来源">
              <el-tag :type="getFieldSourceType(form.field_source)">
                {{ getFieldSourceLabel(form.field_source) }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="排序顺序">
              <el-input-number v-model="form.sort_order" :min="0" />
            </el-form-item>
            
            <el-form-item label="显示宽度">
              <el-input-number
                v-model="form.display_width"
                :min="0"
                :max="500"
                placeholder="0表示自动"
              />
              <span style="margin-left: 10px; color: #909399;">像素（0表示自动）</span>
            </el-form-item>
          </el-tab-pane>

          <!-- 样式配置 -->
          <el-tab-pane label="样式配置" name="style">
            <el-form-item label="颜色类型">
              <el-radio-group v-model="form.color_type">
                <el-radio value="normal">
                  <span style="color: #606266;">普通</span>
                </el-radio>
                <el-radio value="red">
                  <span style="color: #f56c6c;">红色</span>
                </el-radio>
                <el-radio value="yellow">
                  <span style="color: #e6a23c;">黄色</span>
                </el-radio>
                <el-radio value="green">
                  <span style="color: #67c23a;">绿色</span>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-tab-pane>

          <!-- 搜索筛选配置 -->
          <el-tab-pane label="搜索筛选" name="search">
            <el-form-item 
              label="是否可筛选"
              v-if="isFilterableType(form.field_data_type)"
            >
              <el-switch v-model="form.is_filterable" />
              <span style="margin-left: 10px; color: #909399;">针对枚举字段，启用后可在列表中筛选</span>
            </el-form-item>
            
            <el-form-item 
              label="是否支持范围检索"
              v-if="isRangeSearchableType(form.field_data_type)"
            >
              <el-switch v-model="form.is_range_searchable" />
              <span style="margin-left: 10px; color: #909399;">
                针对数字和时间字段，支持最小-最大值或开始-结束时间范围筛选
              </span>
            </el-form-item>
            
            <!-- 如果没有可配置的搜索筛选项，显示提示 -->
            <el-alert
              v-if="!isFilterableType(form.field_data_type) && 
                    !isRangeSearchableType(form.field_data_type)"
              title="当前字段类型不支持筛选或范围检索功能"
              type="info"
              :closable="false"
            />
          </el-tab-pane>

          <!-- 隐藏规则（仅催员端） -->
          <el-tab-pane
            label="隐藏规则"
            name="hide"
            v-if="isCollectorScene"
          >
            <el-alert
              title="提示：隐藏规则仅对催员端生效，可以根据案件所属队列、机构、小组来控制字段显示"
              type="info"
              :closable="false"
              style="margin-bottom: 20px;"
            />
            
            <el-form-item label="对队列隐藏">
              <el-select
                v-model="form.hide_for_queues"
                multiple
                placeholder="选择要隐藏的队列"
                style="width: 100%"
              >
                <el-option
                  v-for="queue in queues"
                  :key="queue.id"
                  :label="queue.queue_name"
                  :value="queue.id.toString()"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="对机构隐藏">
              <el-select
                v-model="form.hide_for_agencies"
                multiple
                placeholder="选择要隐藏的机构"
                style="width: 100%"
              >
                <el-option
                  v-for="agency in agencies"
                  :key="agency.id"
                  :label="agency.agency_name"
                  :value="agency.id.toString()"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="对小组隐藏">
              <el-select
                v-model="form.hide_for_teams"
                multiple
                placeholder="选择要隐藏的小组"
                style="width: 100%"
              >
                <el-option
                  v-for="team in teams"
                  :key="team.id"
                  :label="team.team_name"
                  :value="team.id.toString()"
                />
              </el-select>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 复制场景对话框 -->
    <el-dialog v-model="copyDialogVisible" title="复制场景配置" width="500px">
      <el-form label-width="100px">
        <el-form-item label="源场景">
          <el-select v-model="copyForm.fromScene" style="width: 100%">
            <el-option
              v-for="scene in sceneTypes"
              :key="scene.key"
              :label="scene.name"
              :value="scene.key"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="目标场景">
          <el-select v-model="copyForm.toScene" style="width: 100%">
            <el-option
              v-for="scene in sceneTypes"
              :key="scene.key"
              :label="scene.name"
              :value="scene.key"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCopyConfirm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 版本历史抽屉 -->
    <el-drawer
      v-model="historyVisible"
      title="展示配置版本历史"
      direction="rtl"
      size="800px"
    >
      <div class="history-content">
        <el-table :data="versionHistory" border style="width: 100%">
          <el-table-column prop="version" label="版本号" width="80" align="center">
            <template #default="{ row }">
              <el-tag type="primary">v{{ row.version }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="saved_at" label="保存时间" width="180" />
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="note" label="备注说明" min-width="200">
            <template #default="{ row }">
              {{ row.note || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="字段数" width="80" align="center">
            <template #default="{ row }">
              {{ row.configs?.length || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                link 
                size="small" 
                @click="handleRestoreVersion(row)"
              >
                恢复此版本
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="versionHistory.length === 0" description="暂无版本历史" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import Sortable from 'sortablejs'
import {
  getCaseListSceneTypes,
  getCaseListFieldConfigs,
  batchSaveCaseListFieldConfigs,
  copyCaseListScene,
  getAvailableFieldsForList,
  getCaseListFieldConfigVersion,
  saveCaseListFieldConfigVersion,
  getCaseListFieldConfigVersionHistory,
  activateCaseListFieldConfigVersion
} from '@/api/caseListFieldConfig'
import type {
  FieldDisplayConfig,
  FieldDisplayConfigCreate,
  AvailableFieldOption
} from '@/types/fieldDisplay'

type CaseListSceneType = 'admin_case_list' | 'collector_case_list'
interface SceneOption {
  key: CaseListSceneType
  name: string
  description?: string
}

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)
const tableRef = ref()

const sceneTypes = ref<SceneOption[]>([])
const currentScene = ref<CaseListSceneType>('admin_case_list')
const configs = ref<FieldDisplayConfig[]>([])
const loading = ref(false)
const versionInfo = ref<{ version: number | string; fetched_at: string; source: string }>({
  version: 0,
  fetched_at: '',
  source: 'mock'
})
const saveVersionLoading = ref(false)
const historyVisible = ref(false)
const versionHistory = ref<any[]>([])
const versionNote = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const activeTab = ref('basic')
const formRef = ref()
const isEditMode = ref(false)

// 可用字段列表
const availableFields = ref<AvailableFieldOption[]>([])
const selectedFieldKey = ref('')

// 队列、机构、小组数据（用于隐藏规则）
const queues = ref<any[]>([])
const agencies = ref<any[]>([])
const teams = ref<any[]>([])

// 表单数据
const form = ref<FieldDisplayConfigCreate>({
  tenant_id: '',
  scene_type: 'admin_case_list',
  scene_name: '',
  field_key: '',
  field_name: '',
  field_data_type: '',
  field_source: '',
  sort_order: 0,
  display_width: 0,
  color_type: 'normal',
  is_filterable: false,
  hide_for_queues: [],
  hide_for_agencies: [],
  hide_for_teams: []
})

// 复制场景表单
const copyDialogVisible = ref(false)
const copyForm = ref({
  fromScene: '',
  toScene: ''
})

// 是否是催员端场景
const isCollectorScene = computed(() => {
  return form.value.scene_type?.includes('collector')
})

// 字段来源标签
const getFieldSourceLabel = (source?: string) => {
  const labels: Record<string, string> = {
    'standard': '标准字段',
    'custom': '自定义字段',
    'system': '系统字段'
  }
  return labels[source || ''] || '-'
}

// 字段来源类型
const getFieldSourceType = (source?: string) => {
  const types: Record<string, any> = {
    'standard': 'success',
    'custom': 'info',
    'system': 'danger'
  }
  return types[source || ''] || ''
}

// 判断是否是可筛选的类型（枚举类型）
const isFilterableType = (fieldType?: string) => {
  if (!fieldType) return false
  return fieldType === 'Enum'
}

// 判断是否是可范围检索的类型（数字和时间类型）
const isRangeSearchableType = (fieldType?: string) => {
  if (!fieldType) return false
  const rangeTypes = ['Integer', 'Number', 'Decimal', 'Date', 'Datetime']
  return rangeTypes.includes(fieldType)
}

// 展示枚举值文本
const getEnumOptionsLabel = (row: any) => {
  if (row?.field_data_type !== 'Enum') return '-'
  const options = row.enum_options || row.enum_values || row.enumValues
  if (Array.isArray(options) && options.length) {
    return options.join('、')
  }
  return '-'
}

// 分组的字段列表
const groupedFields = computed(() => {
  const groups: Record<string, AvailableFieldOption[]> = {}
  
  availableFields.value.forEach(field => {
    const groupName = field.field_group_name || '其他'
    if (!groups[groupName]) {
      groups[groupName] = []
    }
    groups[groupName].push(field)
  })
  
  return Object.keys(groups).map(label => ({
    label,
    options: groups[label]
  }))
})

// 加载可用字段
const loadAvailableFields = async () => {
  try {
    const data = await getAvailableFieldsForList({
      tenantId: currentTenantId.value ? Number(currentTenantId.value) : undefined
    })
    availableFields.value = Array.isArray(data) ? data : (data?.data ?? [])
  } catch (error: any) {
    ElMessage.error('加载可用字段失败：' + error.message)
  }
}

// 选择字段时
const handleFieldSelect = (fieldKey: string) => {
  const field = availableFields.value.find(f => f.field_key === fieldKey)
  if (field) {
    form.value.field_key = field.field_key
    form.value.field_name = field.field_name
    form.value.field_data_type = field.field_type
    form.value.field_source = field.field_source
    
    // 根据字段类型自动设置筛选和范围检索选项
    if (field.field_type === 'Enum') {
      form.value.is_filterable = true
      form.value.is_range_searchable = false
    } else if (['Integer', 'Decimal', 'Date', 'Datetime'].includes(field.field_type)) {
      form.value.is_filterable = false
      form.value.is_range_searchable = true
    } else {
      form.value.is_filterable = false
      form.value.is_range_searchable = false
    }
  }
}

// 加载场景类型
const loadSceneTypes = async () => {
  try {
    const data = await getCaseListSceneTypes()
    sceneTypes.value = Array.isArray(data) ? data : (data?.data ?? [])
  } catch (error: any) {
    ElMessage.error('加载场景类型失败：' + error.message)
  }
}

// 初始化拖拽排序
const initDragSort = () => {
  nextTick(() => {
    const table = tableRef.value?.$el
    if (!table) return
    
    const tbody = table.querySelector('.el-table__body-wrapper tbody')
    if (!tbody) return
    
    Sortable.create(tbody, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: ({ oldIndex, newIndex }) => {
        if (oldIndex === newIndex) return
        
        // 重新排列数组
        const movedItem = configs.value.splice(oldIndex!, 1)[0]
        configs.value.splice(newIndex!, 0, movedItem)
        
        // 更新所有项的sort_order
        configs.value.forEach((config, index) => {
          config.sort_order = index + 1
        })
        
        ElMessage.success('拖拽成功，请点击"批量保存"保存更改')
      }
    })
  })
}

// 加载配置列表
const loadConfigs = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  loading.value = true
  try {
    const data = await getCaseListFieldConfigs({
      tenantId: Number(currentTenantId.value),
      sceneType: currentScene.value
    })
    configs.value = Array.isArray(data) ? data : (data?.data ?? [])
    await loadVersionInfo()
    
    // 初始化拖拽排序
    initDragSort()
  } catch (error: any) {
    ElMessage.error('加载配置失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取映射配置版本信息
const loadVersionInfo = async () => {
  try {
    const data = await getCaseListFieldConfigVersion({
      tenantId: Number(currentTenantId.value),
      sceneType: currentScene.value
    })
    const payload = Array.isArray(data) ? data : (data?.data ?? {})
    versionInfo.value = {
      version: payload.version ?? 0,
      fetched_at: payload.fetched_at ?? '',
      source: payload.source ?? 'mock'
    }
  } catch (error: any) {
    console.error(error)
  }
}

// 场景切换
const handleSceneChange = () => {
  loadConfigs()
  // 重新初始化拖拽
  initDragSort()
}

// 添加配置
const handleAdd = () => {
  isEditMode.value = false
  dialogTitle.value = '添加字段配置'
  selectedFieldKey.value = ''
  form.value = {
    tenant_id: currentTenantId.value || '',
    scene_type: currentScene.value,
    scene_name: sceneTypes.value.find(s => s.key === currentScene.value)?.name || '',
    field_key: '',
    field_name: '',
    field_data_type: '',
    field_source: '',
    sort_order: configs.value.length,
    display_width: 0,
    color_type: 'normal',
    is_filterable: false,
    is_range_searchable: false,
    hide_for_queues: [],
    hide_for_agencies: [],
    hide_for_teams: []
  }
  dialogVisible.value = true
  
  // 加载可用字段
  loadAvailableFields()
}

// 编辑配置
const handleEdit = (row: FieldDisplayConfig) => {
  isEditMode.value = true
  dialogTitle.value = '编辑字段配置'
  form.value = {
    ...row,
    hide_for_queues: row.hide_for_queues || [],
    hide_for_agencies: row.hide_for_agencies || [],
    hide_for_teams: row.hide_for_teams || []
  }
  dialogVisible.value = true
}

// 删除配置
const handleDelete = async (row: FieldDisplayConfig) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
      type: 'warning'
    })
    
    // 从列表中移除
    const index = configs.value.findIndex(c => c.id === row.id)
    if (index > -1) {
      configs.value.splice(index, 1)
      ElMessage.success('删除成功，请点击"批量保存"保存更改')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (!form.value.field_key || !form.value.field_name) {
      ElMessage.warning('请填写必填字段')
      return
    }
    
    if ((form.value as any).id) {
      // 更新 - 在configs中找到并更新
      const index = configs.value.findIndex(c => c.id === (form.value as any).id)
      if (index > -1) {
        configs.value[index] = { ...form.value } as any
      }
      ElMessage.success('更新成功，请点击"批量保存"保存更改')
    } else {
      // 创建
      configs.value.push({
        ...form.value,
        id: Date.now()
      } as any)
      ElMessage.success('创建成功，请点击"批量保存"保存更改')
    }
    
    dialogVisible.value = false
  } catch (error: any) {
    ElMessage.error('操作失败：' + error.message)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  activeTab.value = 'basic'
  selectedFieldKey.value = ''
}

// 复制场景
const handleCopyScene = () => {
  copyForm.value = {
    fromScene: currentScene.value,
    toScene: ''
  }
  copyDialogVisible.value = true
}

// 确认复制
const handleCopyConfirm = async () => {
  if (!copyForm.value.fromScene || !copyForm.value.toScene) {
    ElMessage.warning('请选择源场景和目标场景')
    return
  }
  
  if (copyForm.value.fromScene === copyForm.value.toScene) {
    ElMessage.warning('源场景和目标场景不能相同')
    return
  }
  
  try {
    await copyCaseListScene({
      tenant_id: Number(currentTenantId.value || 0),
      from_scene: copyForm.value.fromScene as CaseListSceneType,
      to_scene: copyForm.value.toScene as CaseListSceneType
    })
    ElMessage.success('复制成功')
    copyDialogVisible.value = false
    
    // 如果当前场景是目标场景，重新加载
    if (currentScene.value === copyForm.value.toScene) {
      loadConfigs()
    }
  } catch (error: any) {
    ElMessage.error('复制失败：' + error.message)
  }
}

// 批量保存
const handleBatchSave = async () => {
  try {
    await batchSaveCaseListFieldConfigs({
      tenant_id: Number(currentTenantId.value || 0),
      scene_type: currentScene.value,
      configs: configs.value as any
    })
    ElMessage.success('批量保存成功')
    loadConfigs()
  } catch (error: any) {
    ElMessage.error('批量保存失败：' + error.message)
  }
}

// 保存为新版本
const handleSaveVersion = async () => {
  if (!configs.value || configs.value.length === 0) {
    ElMessage.warning('当前没有可保存的配置')
    return
  }

  try {
    const { value } = await ElMessageBox.prompt(
      `当前配置包含 ${configs.value.length} 个字段，请填写版本备注（可选）`,
      '保存为新版本',
      {
        confirmButtonText: '确定保存',
        cancelButtonText: '取消',
        inputPlaceholder: '描述本次修改内容，如：调整字段宽度、开启范围检索等'
      }
    )

    saveVersionLoading.value = true

    const res = await saveCaseListFieldConfigVersion({
      tenant_id: Number(currentTenantId.value),
      scene_type: currentScene.value,
      configs: configs.value.map(c => ({
        field_key: c.field_key,
        field_name: c.field_name,
        field_data_type: c.field_data_type,
        field_source: c.field_source,
        enum_options: c.enum_options,
        sort_order: c.sort_order,
        display_width: c.display_width,
        color_type: c.color_type,
        is_filterable: c.is_filterable,
        is_range_searchable: c.is_range_searchable
      })),
      operator: 'admin',
      note: value || ''
    })

    const versionNum = res?.data?.version || '未知'
    ElMessage.success(`保存成功，新版本号：v${versionNum}`)
    
    // 刷新配置和版本信息
    await loadConfigs()
    await loadVersionInfo()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败：' + (error.message || error))
    }
  } finally {
    saveVersionLoading.value = false
  }
}

// 显示版本历史
const handleShowHistory = async () => {
  historyVisible.value = true

  try {
    const data = await getCaseListFieldConfigVersionHistory({
      tenantId: currentTenantId.value,
      sceneType: currentScene.value
    })
    versionHistory.value = Array.isArray(data) ? data : (data?.data || [])
    
    // 按版本号倒序排列
    versionHistory.value.sort((a, b) => b.version - a.version)
  } catch (error: any) {
    ElMessage.error('加载版本历史失败：' + error.message)
  }
}

// 恢复历史版本
const handleRestoreVersion = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `您确定要恢复到版本 v${row.version} 吗？\n保存时间：${row.saved_at}\n备注：${row.note || '无'}\n\n恢复后当前配置将被替换，建议先保存当前配置。`,
      '确认恢复版本',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await activateCaseListFieldConfigVersion({
      tenant_id: Number(currentTenantId.value),
      scene_type: currentScene.value,
      version: row.version
    })

    ElMessage.success('版本恢复成功')
    historyVisible.value = false
    
    // 刷新配置和版本信息
    await loadConfigs()
    await loadVersionInfo()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('恢复失败：' + (error.message || error))
    }
  }
}

onMounted(() => {
  loadSceneTypes()
  if (currentTenantId.value) {
    loadConfigs()
    loadVersionInfo()
  }
  // 初始化拖拽
  initDragSort()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-block {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.title-block .title {
  font-weight: 600;
  font-size: 16px;
}

.version-badge {
  padding: 0 6px;
}

.version-time {
  color: #909399;
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.drag-handle {
  font-size: 16px;
  color: #909399;
  transition: color 0.3s;
}

.drag-handle:hover {
  color: #409eff;
}

:deep(.sortable-ghost) {
  opacity: 0.4;
  background: #f5f7fa;
}

:deep(.sortable-drag) {
  opacity: 0.8;
  background: #ecf5ff;
}

.history-content {
  padding: 0;
}
</style>

