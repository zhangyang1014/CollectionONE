<template>
  <div class="field-display-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方字段展示配置</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">添加字段配置</el-button>
            <el-button @click="handleCopyScene">复制场景配置</el-button>
            <el-button @click="handleBatchSave">批量保存</el-button>
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
        
        <el-table-column label="可搜索" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-if="isSearchableType(row.field_data_type)"
              v-model="row.is_searchable" 
              size="small" 
            />
            <span v-else style="color: #c0c4cc;">-</span>
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
        
        <el-table-column label="隐藏规则" width="150">
          <template #default="{ row }">
            <el-tag
              v-if="row.hide_for_queues && row.hide_for_queues.length > 0"
              type="warning"
              size="small"
            >
              队列: {{ row.hide_for_queues.length }}
            </el-tag>
            <el-tag
              v-if="row.hide_for_agencies && row.hide_for_agencies.length > 0"
              type="warning"
              size="small"
              style="margin-left: 5px;"
            >
              机构: {{ row.hide_for_agencies.length }}
            </el-tag>
            <el-tag
              v-if="row.hide_for_teams && row.hide_for_teams.length > 0"
              type="warning"
              size="small"
              style="margin-left: 5px;"
            >
              小组: {{ row.hide_for_teams.length }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
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
              <el-input v-model="form.field_key" :disabled="isEditMode" placeholder="如：case_number" />
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
              label="是否可搜索"
              v-if="isSearchableType(form.field_data_type)"
            >
              <el-switch v-model="form.is_searchable" />
              <span style="margin-left: 10px; color: #909399;">针对文本字段，启用后可在列表中搜索</span>
            </el-form-item>
            
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
              v-if="!isSearchableType(form.field_data_type) && 
                    !isFilterableType(form.field_data_type) && 
                    !isRangeSearchableType(form.field_data_type)"
              title="当前字段类型不支持搜索、筛选或范围检索功能"
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import Sortable from 'sortablejs'
import {
  getSceneTypes,
  getFieldDisplayConfigs,
  createFieldDisplayConfig,
  updateFieldDisplayConfig,
  deleteFieldDisplayConfig,
  copySceneConfig,
  batchCreateOrUpdateConfigs,
  getAvailableFields
} from '@/api/fieldDisplay'
import type {
  FieldDisplayConfig,
  FieldDisplayConfigCreate,
  SceneType,
  AvailableFieldOption
} from '@/types/fieldDisplay'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)
const tableRef = ref()

const sceneTypes = ref<SceneType[]>([])
const currentScene = ref('admin_case_list')
const configs = ref<FieldDisplayConfig[]>([])
const loading = ref(false)
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
  is_searchable: false,
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
    'extended': '扩展字段',
    'custom': '自定义字段'
  }
  return labels[source || ''] || '-'
}

// 字段来源类型
const getFieldSourceType = (source?: string) => {
  const types: Record<string, any> = {
    'standard': 'success',
    'extended': 'warning',
    'custom': 'info'
  }
  return types[source || ''] || ''
}

// 判断是否是可搜索的类型（文本类型）
const isSearchableType = (fieldType?: string) => {
  if (!fieldType) return false
  const searchableTypes = ['String', 'Text']
  return searchableTypes.includes(fieldType)
}

// 判断是否是可筛选的类型（枚举类型）
const isFilterableType = (fieldType?: string) => {
  if (!fieldType) return false
  return fieldType === 'Enum'
}

// 判断是否是可范围检索的类型（数字和时间类型）
const isRangeSearchableType = (fieldType?: string) => {
  if (!fieldType) return false
  const rangeTypes = ['Integer', 'Decimal', 'Date', 'Datetime']
  return rangeTypes.includes(fieldType)
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
    const data = await getAvailableFields(currentTenantId.value)
    availableFields.value = data
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
    
    // 根据字段类型自动设置搜索、筛选和范围检索选项
    if (field.field_type === 'String' || field.field_type === 'Text') {
      form.value.is_searchable = true
      form.value.is_filterable = false
      form.value.is_range_searchable = false
    } else if (field.field_type === 'Enum') {
      form.value.is_searchable = false
      form.value.is_filterable = true
      form.value.is_range_searchable = false
    } else if (['Integer', 'Decimal', 'Date', 'Datetime'].includes(field.field_type)) {
      form.value.is_searchable = false
      form.value.is_filterable = false
      form.value.is_range_searchable = true // 自动开启范围检索
    } else {
      form.value.is_searchable = false
      form.value.is_filterable = false
      form.value.is_range_searchable = false
    }
  }
}

// 加载场景类型
const loadSceneTypes = async () => {
  try {
    const data = await getSceneTypes()
    sceneTypes.value = data
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
    const data = await getFieldDisplayConfigs({
      tenant_id: currentTenantId.value,
      scene_type: currentScene.value
    })
    configs.value = data
    
    // 初始化拖拽排序
    initDragSort()
  } catch (error: any) {
    ElMessage.error('加载配置失败：' + error.message)
  } finally {
    loading.value = false
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
    is_searchable: false,
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
    
    await deleteFieldDisplayConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
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
      // 更新
      await updateFieldDisplayConfig((form.value as any).id, form.value)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createFieldDisplayConfig(form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadConfigs()
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
    await copySceneConfig(
      copyForm.value.fromScene,
      copyForm.value.toScene,
      currentTenantId.value || ''
    )
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
    await batchCreateOrUpdateConfigs(
      currentTenantId.value || '',
      currentScene.value,
      configs.value as any
    )
    ElMessage.success('批量保存成功')
    loadConfigs()
  } catch (error: any) {
    ElMessage.error('批量保存失败：' + error.message)
  }
}

onMounted(() => {
  loadSceneTypes()
  if (currentTenantId.value) {
    loadConfigs()
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
</style>
