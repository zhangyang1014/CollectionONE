<template>
  <div class="case-detail-field-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-info">
            <span class="title">案件详情字段展示配置</span>
            <div class="data-source-info">
              <el-tag type="info" size="small">数据来源：{{ dataSourceInfo.source }}</el-tag>
              <el-tag type="success" size="small">版本：{{ dataSourceInfo.version }}</el-tag>
              <span class="info-text">拉取时间：{{ dataSourceInfo.fetchTime }}</span>
            </div>
          </div>
          <div class="header-actions">
            <el-button @click="handleManageGroups">
              <el-icon><Setting /></el-icon>
              分组管理
            </el-button>
            <el-button type="primary" @click="handleAdd">添加字段配置</el-button>
            <el-button type="success" @click="handleBatchSave">保存到服务器</el-button>
            <el-button @click="handleSaveVersion">保存为本地版本</el-button>
            <el-button @click="handleShowVersions">版本管理</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧分组树 -->
        <el-col :span="5">
          <el-card shadow="never">
            <template #header>字段分组</template>
            <el-tree
              :data="groupTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="key"
              :default-expand-all="true"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleGroupClick"
              class="field-group-tree"
            />
          </el-card>
        </el-col>

        <!-- 右侧配置表格 -->
        <el-col :span="19">
      <!-- 配置表格 -->
      <el-table
        ref="tableRef"
        :data="filteredConfigs"
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
        </el-col>
      </el-row>
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
              <el-input v-model="form.scene_name" disabled />
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

    <!-- 分组管理对话框 -->
    <el-dialog
      v-model="groupManageDialogVisible"
      title="分组管理"
      width="700px"
    >
      <el-alert
        title="提示：配置分组的显示顺序和默认折叠状态，应用于案件详情页面的分组卡片展示"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <el-table :data="groupConfigs" border row-key="group_key">
        <el-table-column label="拖拽" width="60" align="center">
          <template #default>
            <el-icon class="drag-handle" style="cursor: move;">
              <Rank />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="group_name" label="分组名称" width="150" />
        <el-table-column prop="group_key" label="分组标识" width="150" />
        <el-table-column label="排序" width="100" align="center">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.sort_order" 
              :min="1" 
              size="small"
              style="width: 80px"
            />
          </template>
        </el-table-column>
        <el-table-column label="默认折叠" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.is_collapsed_default" />
          </template>
        </el-table-column>
        <el-table-column label="字段数" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ getGroupFieldCount(row.group_key) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="groupManageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveGroupConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 版本管理抽屉 -->
    <el-drawer
      v-model="versionDrawerVisible"
      title="版本管理 - 案件详情字段展示配置"
      size="500px"
    >
      <div class="version-list">
        <el-alert
          title="提示：版本文件保存在本地 case-detail-display-versions 目录"
          type="info"
          :closable="false"
          style="margin-bottom: 15px"
        />
        
        <div v-if="localVersions.length === 0">
          <el-empty description="暂无保存的版本" />
        </div>
        
        <div v-else>
          <div 
            v-for="version in localVersions" 
            :key="version.version"
            class="version-item"
            :class="{ active: version.is_active }"
          >
            <div class="version-header">
              <el-tag :type="version.is_active ? 'success' : 'info'">
                版本{{ version.version }}
              </el-tag>
              <el-tag v-if="version.is_active" type="success" size="small">当前使用</el-tag>
            </div>
            <div class="version-body">
              <div>保存时间：{{ formatDate(version.created_at) }}</div>
              <div>操作人：{{ version.operator }}</div>
              <div>备注：{{ version.note || '-' }}</div>
            </div>
            <div class="version-actions">
              <el-button 
                v-if="!version.is_active"
                link 
                type="primary" 
                size="small"
                @click="handleActivateLocalVersion(version)"
              >
                激活
              </el-button>
              <el-button link type="danger" size="small" @click="handleDeleteLocalVersion(version)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank, Setting } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import Sortable from 'sortablejs'
import {
  getCaseDetailFieldConfigs,
  batchSaveCaseDetailFieldConfigs,
  getAvailableFieldsForDetail
} from '@/api/caseDetailFieldConfig'
import { getDetailFieldGroups } from '@/api/detailFieldGroup'
import type {
  FieldDisplayConfig,
  FieldDisplayConfigCreate,
  AvailableFieldOption
} from '@/types/fieldDisplay'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)
const tableRef = ref()

// 常量：统一场景
const SCENE_TYPE = 'admin_case_detail'
const SCENE_NAME = '控台案件详情'

// 分组相关
const allGroups = ref<any[]>([])
const activeGroupId = ref<string | number>('all')
const groupConfigs = ref<any[]>([]) // 分组配置（排序、折叠状态）

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

// 分组管理
const groupManageDialogVisible = ref(false)

// 版本管理
const versionDrawerVisible = ref(false)
const localVersions = ref<any[]>([])

// 数据来源信息
const dataSourceInfo = ref({
  source: '映射配置',
  version: 'v1',
  fetchTime: new Date().toLocaleString('zh-CN')
})

// 表单数据
const form = ref<FieldDisplayConfigCreate>({
  tenant_id: '',
  scene_type: SCENE_TYPE,
  scene_name: SCENE_NAME,
  field_key: '',
  field_name: '',
  field_data_type: '',
  field_source: '',
  sort_order: 0,
  display_width: 0,
  color_type: 'normal',
  field_group_id: undefined as any,
  hide_for_queues: [],
  hide_for_agencies: [],
  hide_for_teams: []
})

// 是否是催员端场景（统一场景后始终为 false）
const isCollectorScene = computed(() => false)

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

// 加载分组
const loadGroups = async () => {
  try {
    const data = await getDetailFieldGroups({
      tenantId: currentTenantId.value ? Number(currentTenantId.value) : undefined
    })
    allGroups.value = Array.isArray(data) ? data : (data?.data ?? [])
  } catch (error) {
    console.error('加载分组失败：', error)
    allGroups.value = []
  }
}

// 分组树
const groupTree = computed(() => {
  const roots = allGroups.value.filter(g => !g.parent_id)
  const buildChildren = (parentId: number) => {
    return allGroups.value
      .filter(g => g.parent_id === parentId)
      .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
      .map(g => ({
        key: g.id,
        label: g.group_name,
        groupKey: g.group_key,
        children: buildChildren(g.id)
      }))
  }

  const tree = roots
    .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    .map(g => ({
      key: g.id,
      label: g.group_name,
      groupKey: g.group_key,
      children: buildChildren(g.id)
    }))

  return [{ key: 'all', label: '全部', children: tree }]
})

// 获取选中分组及子分组ID
const getGroupAndChildrenIds = (groupId: number): number[] => {
  const ids = [groupId]
  const children = allGroups.value.filter(g => g.parent_id === groupId)
  children.forEach(child => ids.push(...getGroupAndChildrenIds(child.id)))
  return ids
}

// 分组过滤后的配置
const filteredConfigs = computed(() => {
  if (activeGroupId.value === 'all') return configs.value
  const ids = getGroupAndChildrenIds(Number(activeGroupId.value))
  return configs.value.filter(c => !c.field_group_id || ids.includes(Number(c.field_group_id)))
})

// 加载可用字段
const loadAvailableFields = async () => {
  try {
    const data = await getAvailableFieldsForDetail({
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
    form.value.field_group_id = field.field_group_id ?? null
  }
}

// 分组点击
const handleGroupClick = (node: any) => {
  activeGroupId.value = node.key
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
    const data = await getCaseDetailFieldConfigs({
      tenantId: Number(currentTenantId.value),
      sceneType: SCENE_TYPE
    })
    configs.value = Array.isArray(data) ? data : (data?.data ?? [])
    
    // 初始化拖拽排序
    initDragSort()
  } catch (error: any) {
    ElMessage.error('加载配置失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 添加配置
const handleAdd = () => {
  isEditMode.value = false
  dialogTitle.value = '添加字段配置'
  selectedFieldKey.value = ''
  form.value = {
    tenant_id: currentTenantId.value || '',
    scene_type: SCENE_TYPE,
    scene_name: SCENE_NAME,
    field_key: '',
    field_name: '',
    field_data_type: '',
    field_source: '',
    sort_order: configs.value.length,
    display_width: 0,
    color_type: 'normal',
    field_group_id: activeGroupId.value === 'all' ? null as any : Number(activeGroupId.value),
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

// 批量保存
const handleBatchSave = async () => {
  try {
    await batchSaveCaseDetailFieldConfigs({
      tenant_id: Number(currentTenantId.value || 0),
      scene_type: SCENE_TYPE,
      configs: configs.value as any
    })
    ElMessage.success('批量保存成功')
    loadConfigs()
  } catch (error: any) {
    ElMessage.error('批量保存失败：' + error.message)
  }
}

// 分组管理
const handleManageGroups = () => {
  // 初始化分组配置
  groupConfigs.value = allGroups.value
    .filter(g => !g.parent_id)
    .map(g => ({
      group_key: g.group_key,
      group_name: g.group_name,
      sort_order: g.sort_order || 0,
      is_collapsed_default: g.is_collapsed_default || false
    }))
  
  groupManageDialogVisible.value = true
}

const getGroupFieldCount = (groupKey: string) => {
  return configs.value.filter(c => {
    const group = allGroups.value.find(g => g.id === c.field_group_id)
    return group?.group_key === groupKey
  }).length
}

const handleSaveGroupConfig = () => {
  // 更新allGroups中的分组配置
  groupConfigs.value.forEach(gc => {
    const group = allGroups.value.find(g => g.group_key === gc.group_key)
    if (group) {
      group.sort_order = gc.sort_order
      group.is_collapsed_default = gc.is_collapsed_default
    }
  })
  
  groupManageDialogVisible.value = false
  ElMessage.success('分组配置已更新，请保存为新版本')
}

// 版本管理
const handleSaveVersion = async () => {
  try {
    const note = await ElMessageBox.prompt('请输入版本说明', '保存为新版本', {
      inputPlaceholder: '例如：调整分组排序和字段宽度',
      inputType: 'textarea'
    })
    
    // TODO: 保存到本地JSON文件
    const versionData = {
      version: localVersions.value.length + 1,
      tenant_id: Number(currentTenantId.value),
      scene_type: SCENE_TYPE,
      groups: buildGroupsWithFields(),
      created_at: new Date().toISOString(),
      operator: 'admin',
      note: note.value,
      is_active: true
    }
    
    console.log('保存版本：', versionData)
    ElMessage.success('版本保存成功')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('保存失败')
    }
  }
}

const buildGroupsWithFields = () => {
  return groupConfigs.value.map(gc => {
    const groupFields = configs.value.filter(c => {
      const group = allGroups.value.find(g => g.id === c.field_group_id)
      return group?.group_key === gc.group_key
    })
    
    return {
      group_key: gc.group_key,
      group_name: gc.group_name,
      sort_order: gc.sort_order,
      is_collapsed_default: gc.is_collapsed_default,
      fields: groupFields.map(f => ({
        field_key: f.field_key,
        field_name: f.field_name,
        sort_order: f.sort_order,
        display_width: f.display_width,
        color_type: f.color_type
      }))
    }
  })
}

const handleShowVersions = () => {
  // TODO: 加载本地版本列表
  localVersions.value = []
  versionDrawerVisible.value = true
}

const handleActivateLocalVersion = (version: any) => {
  ElMessage.info('激活版本功能开发中...')
}

const handleDeleteLocalVersion = (version: any) => {
  ElMessage.info('删除版本功能开发中...')
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadGroups()
  loadAvailableFields()
  loadConfigs()
  // 初始化拖拽
  initDragSort()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-info {
  flex: 1;
}

.header-info .title {
  font-size: 18px;
  font-weight: 600;
  display: block;
  margin-bottom: 10px;
}

.data-source-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.info-text {
  color: #606266;
  font-size: 14px;
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

.version-list {
  padding: 0 10px;
}

.version-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.version-item.active {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.version-body {
  font-size: 14px;
  color: #606266;
  margin: 10px 0;
}

.version-body > div {
  margin: 5px 0;
}

.version-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.field-cell {
  padding: 4px 0;
}

.field-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.field-key {
  font-size: 12px;
  color: #909399;
}
</style>

