<template>
  <div class="field-mapping-config">
    <el-card>
      <!-- 页面头部 -->
      <template #header>
        <div class="card-header">
          <span class="card-title">案件列表字段映射配置</span>
        </div>
      </template>

      <!-- 版本信息栏 -->
      <div v-if="currentTenantId && versionInfo" class="version-info-bar">
        <div class="version-info-left">
          <el-tag type="primary" size="large">
            <el-icon><Document /></el-icon>
            版本 {{ versionInfo.version }}
          </el-tag>
          <div class="version-details">
            <span class="detail-item">
              <el-icon><Clock /></el-icon>
              上传时间：{{ formatDateTime(versionInfo.uploaded_at) }}
            </span>
            <span class="detail-item">
              <el-icon><Files /></el-icon>
              字段数：{{ versionInfo.fields_count }} 个
            </span>
            <span class="detail-item">
              <el-icon><User /></el-icon>
              上传人：{{ versionInfo.uploaded_by_name || versionInfo.uploaded_by }}
            </span>
          </div>
        </div>
        <div class="version-info-right">
          <el-button type="primary" :icon="FolderOpened" @click="openVersionManager">
            版本管理
          </el-button>
          <el-button :icon="Upload" @click="openUploadDialog">
            上传新版本
          </el-button>
        </div>
      </div>

      <!-- 未使用字段警告 -->
      <el-alert
        v-if="unmappedTenantFields.length > 0"
        :title="`⚠️ 警告：发现 ${unmappedTenantFields.length} 个未使用的甲方字段，请尽快处理！`"
        type="error"
        :closable="false"
        show-icon
        class="unmapped-alert"
      >
        <template #default>
          <div>未处理的字段可能导致数据丢失或无法正确展示。</div>
          <el-button link type="primary" @click="activeTab = 'unmapped'">
            查看详情
          </el-button>
        </template>
      </el-alert>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mapping-tabs">
        <!-- Tab 1: 匹配目标字段 -->
        <el-tab-pane label="匹配目标字段" name="matched">
          <div class="tab-content">
            <!-- 操作按钮栏 -->
            <div class="action-bar">
              <el-space wrap>
                <el-button 
                  type="primary" 
                  :icon="MagicStick"
                  @click="handleAutoSuggestMapping"
                  :disabled="!currentTenantId || isLoading"
                >
                  一键建议映射未匹配字段
                </el-button>
                <el-button 
                  type="success" 
                  :icon="Plus"
                  @click="handleAddExtendedField" 
                  :disabled="!currentTenantId"
                >
                  添加拓展字段
                </el-button>
              </el-space>

              <!-- 筛选器 -->
              <div class="filter-controls">
                <el-select 
                  v-model="mappingStatusFilter" 
                  placeholder="筛选匹配状态" 
                  clearable
                  style="width: 180px;"
                  @change="handleFilterChange"
                >
                  <el-option label="全部" value="" />
                  <el-option label="未映射" value="unmapped">
                    <el-tag type="info" size="small">未映射</el-tag>
                  </el-option>
                  <el-option label="已自动映射" value="auto_mapped">
                    <el-tag type="success" size="small">已自动映射</el-tag>
                  </el-option>
                  <el-option label="已手动映射" value="manual_mapped">
                    <el-tag type="warning" size="small">已手动映射</el-tag>
                  </el-option>
                </el-select>
                
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索字段名称或标识"
                  :prefix-icon="Search"
                  clearable
                  style="width: 250px;"
                  @input="handleSearch"
                />
              </div>
            </div>

            <!-- 字段映射表格 -->
            <el-table 
              v-loading="isLoading"
              :data="filteredFields" 
              border 
              stripe
              class="mapping-table"
            >
              <!-- 目标字段 -->
              <el-table-column label="目标字段" width="220" fixed>
                <template #default="{ row }">
                  <div class="target-field-cell">
                    <div class="field-name">
                      {{ row.field_name }}
                      <el-tag v-if="row.is_required" type="danger" size="small" effect="dark">必填</el-tag>
                    </div>
                    <div class="field-key">{{ row.field_key }} · {{ row.field_type }}</div>
                  </div>
                </template>
              </el-table-column>
              
              <!-- 匹配状态 -->
              <el-table-column label="匹配状态" width="130" align="center">
                <template #default="{ row }">
                  <el-tag 
                    :type="getMappingStatusTagType(row.mapping_status)"
                    :effect="row.mapping_status === 'unmapped' && row.is_required ? 'dark' : 'light'"
                    size="default"
                  >
                    {{ getMappingStatusText(row.mapping_status) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <!-- 甲方字段 -->
              <el-table-column label="甲方字段" min-width="280">
                <template #default="{ row }">
                  <el-select
                    v-model="row.tenant_field_key"
                    placeholder="选择甲方字段"
                    clearable
                    filterable
                    style="width: 100%"
                    @change="handleTenantFieldChange(row)"
                    :disabled="isLoading"
                  >
                    <el-option
                      v-for="field in availableTenantFields"
                      :key="field.field_key"
                      :label="`${field.field_name} (${field.field_key})`"
                      :value="field.field_key"
                    >
                      <div class="tenant-field-option">
                        <span>{{ field.field_name }}</span>
                        <span class="field-key-small">{{ field.field_key }}</span>
                      </div>
                    </el-option>
                  </el-select>
                </template>
              </el-table-column>
              
              <!-- 枚举值 -->
              <el-table-column label="枚举值" width="180">
                <template #default="{ row }">
                  <template v-if="row.field_type === 'Enum'">
                    <el-button 
                      v-if="row.tenant_field_key" 
                      link 
                      type="primary" 
                      @click="handleConfigEnumMapping(row)"
                      size="small"
                    >
                      <el-icon><Setting /></el-icon>
                      配置枚举映射
                    </el-button>
                    <span v-else style="color: #909399;">-</span>
                  </template>
                  <span v-else style="color: #909399;">-</span>
                </template>
              </el-table-column>
              
              <!-- 来源 -->
              <el-table-column label="来源" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.field_source === 'standard' ? 'primary' : 'success'" size="small">
                    {{ row.field_source === 'standard' ? '标准' : '自定义' }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <!-- 更新时间 -->
              <el-table-column label="更新时间" width="160" align="center">
                <template #default="{ row }">
                  <span v-if="row.updated_at" class="time-text">
                    {{ formatDateTime(row.updated_at) }}
                  </span>
                  <span v-else style="color: #909399;">-</span>
                </template>
              </el-table-column>
              
              <!-- 操作 -->
              <el-table-column label="操作" width="100" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.tenant_field_key"
                    link 
                    type="danger" 
                    @click="handleClearMapping(row)"
                    size="small"
                  >
                    清除映射
                  </el-button>
                  <span v-else style="color: #909399;">-</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 拓展字段 -->
        <el-tab-pane label="拓展字段" name="extended">
          <div class="tab-content">
            <div class="action-bar">
              <el-button 
                type="primary" 
                :icon="Plus"
                @click="handleAddExtendedField"
                :disabled="!currentTenantId"
              >
                添加拓展字段
              </el-button>
            </div>

            <el-table 
              v-loading="isLoading"
              :data="extendedFields" 
              border 
              stripe
            >
              <el-table-column prop="field_alias" label="扩展字段别名" width="180" />
              <el-table-column label="甲方原始字段" width="220">
                <template #default="{ row }">
                  <div class="target-field-cell">
                    <div class="field-name">{{ row.tenant_field_name }}</div>
                    <div class="field-key">{{ row.tenant_field_key }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column label="隐私标签" width="120" align="center">
                <template #default="{ row }">
                  <el-tag 
                    :type="getPrivacyLabelType(row.privacy_label)"
                    size="default"
                  >
                    {{ row.privacy_label }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="是否必填" width="100" align="center">
                <template #default="{ row }">
                  <el-switch 
                    v-model="row.is_required" 
                    @change="handleExtendedFieldUpdate(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="说明" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.description || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleEditExtended(row)" size="small">
                    编辑
                  </el-button>
                  <el-popconfirm
                    title="确定删除该拓展字段吗？"
                    @confirm="handleDeleteExtended(row)"
                  >
                    <template #reference>
                      <el-button link type="danger" size="small">
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 未使用的甲方字段 -->
        <el-tab-pane label="未使用的甲方字段" name="unmapped">
          <div class="tab-content">
            <el-alert
              title="💡 提示"
              type="warning"
              :closable="false"
              show-icon
              class="unmapped-tip"
            >
              这些字段来自甲方上传的JSON，但尚未映射到标准字段或设为拓展字段。建议尽快处理，避免数据丢失。
            </el-alert>

            <el-table 
              v-loading="isLoading"
              :data="unmappedTenantFields" 
              border 
              stripe
              :empty-text="unmappedTenantFields.length === 0 ? '🎉 太棒了！所有字段都已处理。' : '加载中...'"
            >
              <el-table-column label="字段信息" width="250">
                <template #default="{ row }">
                  <div class="target-field-cell">
                    <div class="field-name">{{ row.field_name }}</div>
                    <div class="field-key">{{ row.field_key }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column label="是否必填" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.is_required ? 'danger' : 'info'" size="small">
                    {{ row.is_required ? '必填' : '非必填' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="说明" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.description || '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260" align="center">
                <template #default="{ row }">
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="handleMatchToTarget(row)"
                  >
                    匹配到目标字段
                  </el-button>
                  <el-button 
                    type="success" 
                    size="small"
                    @click="handleSetAsExtended(row)" 
                  >
                    设为拓展字段
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 自动匹配建议弹窗 -->
    <AutoMatchSuggestDialog
      v-model="autoMatchDialogVisible"
      :suggestions="matchSuggestions"
      @confirm="handleConfirmAutoMatch"
    />

    <!-- 枚举值映射配置弹窗 -->
    <EnumMappingDialog
      v-model="enumMappingDialogVisible"
      :standard-field="currentEnumField"
      :tenant-enums="currentTenantEnums"
      @save="handleSaveEnumMapping"
    />

    <!-- 拓展字段编辑弹窗 -->
    <ExtendedFieldDialog
      v-model="extendedDialogVisible"
      :field-data="extendedForm"
      :is-edit="isEditExtended"
      @save="handleSaveExtended"
    />

    <!-- 匹配到目标字段弹窗 -->
    <MatchToTargetDialog
      v-model="matchDialogVisible"
      :unmapped-field="currentUnmappedField"
      :standard-fields="standardFields"
      @confirm="handleConfirmMatch"
    />

    <!-- 版本管理抽屉 -->
    <VersionManagerDrawer
      v-model="versionManagerVisible"
      :tenant-id="currentTenantId"
      :current-version="versionInfo?.version"
      @version-changed="handleVersionChanged"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, Clock, Files, User, Upload, FolderOpened, 
  MagicStick, Plus, Search, Setting 
} from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { getStandardFields } from '@/api/field'
import {
  getTenantFieldsJson,
  getFieldConfigs,
  saveFieldConfig,
  autoSuggestMapping,
  batchConfirmMapping,
  getExtendedFields,
  getUnmappedFields,
  createExtendedField,
  updateExtendedField,
  deleteExtendedField
} from '@/api/field-mapping'

// 导入子组件
import AutoMatchSuggestDialog from './components/AutoMatchSuggestDialog.vue'
import EnumMappingDialog from './components/EnumMappingDialog.vue'
import ExtendedFieldDialog from './components/ExtendedFieldDialog.vue'
import MatchToTargetDialog from './components/MatchToTargetDialog.vue'
import VersionManagerDrawer from './components/VersionManagerDrawer.vue'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// ==================== 状态管理 ====================
const isLoading = ref(false)
const activeTab = ref('matched')

// 版本信息
const versionInfo = ref<any>(null)

// 字段数据
const standardFields = ref<any[]>([]) // 标准字段列表
const availableTenantFields = ref<any[]>([]) // 甲方字段列表
const mappedFields = ref<any[]>([]) // 映射后的字段列表
const extendedFields = ref<any[]>([]) // 拓展字段列表
const unmappedTenantFields = ref<any[]>([]) // 未使用的甲方字段

// 筛选和搜索
const mappingStatusFilter = ref('')
const searchKeyword = ref('')

// 弹窗状态
const autoMatchDialogVisible = ref(false)
const enumMappingDialogVisible = ref(false)
const extendedDialogVisible = ref(false)
const matchDialogVisible = ref(false)
const versionManagerVisible = ref(false)

// 当前操作数据
const matchSuggestions = ref<any[]>([])
const currentEnumField = ref<any>(null)
const currentTenantEnums = ref<any[]>([])
const extendedForm = ref<any>({})
const isEditExtended = ref(false)
const currentUnmappedField = ref<any>(null)

// ==================== 计算属性 ====================
const filteredFields = computed(() => {
  let result = mappedFields.value

  // 按匹配状态筛选
  if (mappingStatusFilter.value) {
    result = result.filter(field => field.mapping_status === mappingStatusFilter.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(field => 
      field.field_name.toLowerCase().includes(keyword) ||
      field.field_key.toLowerCase().includes(keyword) ||
      (field.tenant_field_key && field.tenant_field_key.toLowerCase().includes(keyword))
    )
  }

  return result
})

// ==================== 数据加载 ====================
const loadAllData = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  isLoading.value = true
  try {
    await Promise.all([
      loadTenantFieldsVersion(),
      loadStandardFields(),
      loadUnmappedFields()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 加载甲方字段版本信息
const loadTenantFieldsVersion = async () => {
  try {
    const response = await getTenantFieldsJson(currentTenantId.value!, 'list')
    
    versionInfo.value = {
      version: response.version || 1,
      uploaded_at: response.uploaded_at || response.fetched_at,
      fields_count: response.fields_count || response.fields?.length || 0,
      uploaded_by: response.uploaded_by,
      uploaded_by_name: response.uploaded_by_name
    }
    
    availableTenantFields.value = response.fields || []
    console.log('甲方字段版本加载成功:', versionInfo.value)
  } catch (error) {
    console.error('加载甲方字段版本失败:', error)
    ElMessage.error('加载甲方字段版本失败')
  }
}

// 加载标准字段
const loadStandardFields = async () => {
  try {
    const response = await getStandardFields()
    standardFields.value = Array.isArray(response) ? response : (response.data || [])
    
    // 合并标准字段和映射关系
    mappedFields.value = standardFields.value.map(field => ({
      ...field,
      field_source: 'standard',
      tenant_field_key: null,
      mapping_status: 'unmapped',
      updated_at: null
    }))
    
    console.log('标准字段加载成功:', standardFields.value.length)
  } catch (error) {
    console.error('加载标准字段失败:', error)
    ElMessage.error('加载标准字段失败')
  }
}

// 加载未映射字段
const loadUnmappedFields = async () => {
  try {
    const response = await getUnmappedFields(currentTenantId.value!)
    unmappedTenantFields.value = Array.isArray(response) ? response : (response.data || [])
    console.log('未映射字段加载成功:', unmappedTenantFields.value.length)
  } catch (error) {
    console.error('加载未映射字段失败:', error)
    // 静默降级
    unmappedTenantFields.value = []
  }
}

// 加载拓展字段
const loadExtendedFields = async () => {
  if (!currentTenantId.value) return
  
  isLoading.value = true
  try {
    const response = await getExtendedFields(currentTenantId.value)
    extendedFields.value = Array.isArray(response) ? response : (response.data || [])
    console.log('拓展字段加载成功:', extendedFields.value.length)
  } catch (error) {
    console.error('加载拓展字段失败:', error)
    extendedFields.value = []
  } finally {
    isLoading.value = false
  }
}

// ==================== 事件处理 ====================

// Tab切换
const handleTabChange = (tabName: string) => {
  if (tabName === 'extended') {
    loadExtendedFields()
  } else if (tabName === 'unmapped') {
    loadUnmappedFields()
  }
}

// 筛选变化
const handleFilterChange = () => {
  // 计算属性会自动更新
}

// 搜索
const handleSearch = () => {
  // 计算属性会自动更新
}

// 甲方字段选择变化
const handleTenantFieldChange = async (row: any) => {
  if (!row.tenant_field_key) {
    // 清除映射
    row.mapping_status = 'unmapped'
    row.updated_at = null
    return
  }

  try {
    await saveFieldConfig(currentTenantId.value!, {
      field_key: row.field_key,
      tenant_field_key: row.tenant_field_key,
      mapping_status: 'manual_mapped'
    })
    
    row.mapping_status = 'manual_mapped'
    row.updated_at = new Date().toISOString()
    
    ElMessage.success('映射保存成功')
    
    // 刷新未映射字段列表
    await loadUnmappedFields()
  } catch (error) {
    console.error('保存映射失败:', error)
    ElMessage.error('保存映射失败')
  }
}

// 清除映射
const handleClearMapping = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定清除该字段的映射关系吗？', '确认操作', {
      type: 'warning'
    })
    
    row.tenant_field_key = null
    row.mapping_status = 'unmapped'
    row.updated_at = null
    
    ElMessage.success('映射已清除')
    
    // 刷新未映射字段列表
    await loadUnmappedFields()
  } catch (error) {
    // 用户取消
  }
}

// 自动建议映射
const handleAutoSuggestMapping = async () => {
  try {
    const response = await autoSuggestMapping(currentTenantId.value!)
    matchSuggestions.value = response.suggestions || []
    autoMatchDialogVisible.value = true
  } catch (error) {
    console.error('自动匹配失败:', error)
    ElMessage.error('自动匹配失败')
  }
}

// 确认自动匹配
const handleConfirmAutoMatch = async (selectedMappings: any[]) => {
  try {
    await batchConfirmMapping(currentTenantId.value!, selectedMappings)
    ElMessage.success(`成功映射 ${selectedMappings.length} 个字段`)
    
    // 刷新数据
    await loadAllData()
  } catch (error) {
    console.error('批量确认映射失败:', error)
    ElMessage.error('批量确认映射失败')
  }
}

// 配置枚举映射
const handleConfigEnumMapping = (row: any) => {
  currentEnumField.value = row
  
  // 获取甲方字段的枚举值
  const tenantField = availableTenantFields.value.find(f => f.field_key === row.tenant_field_key)
  currentTenantEnums.value = tenantField?.enum_values || []
  
  enumMappingDialogVisible.value = true
}

// 保存枚举映射
const handleSaveEnumMapping = async (enumMapping: Record<string, string>) => {
  try {
    await saveFieldConfig(currentTenantId.value!, {
      field_key: currentEnumField.value.field_key,
      tenant_field_key: currentEnumField.value.tenant_field_key,
      enum_mapping: enumMapping
    })
    
    ElMessage.success('枚举映射保存成功')
    enumMappingDialogVisible.value = false
  } catch (error) {
    console.error('保存枚举映射失败:', error)
    ElMessage.error('保存枚举映射失败')
  }
}

// 添加拓展字段
const handleAddExtendedField = () => {
  extendedForm.value = {
    field_alias: '',
    tenant_field_key: '',
    tenant_field_name: '',
    field_type: 'String',
    privacy_label: '公开',
    is_required: false,
    description: ''
  }
  isEditExtended.value = false
  extendedDialogVisible.value = true
}

// 编辑拓展字段
const handleEditExtended = (row: any) => {
  extendedForm.value = { ...row }
  isEditExtended.value = true
  extendedDialogVisible.value = true
}

// 保存拓展字段
const handleSaveExtended = async (formData: any) => {
  try {
    if (isEditExtended.value) {
      await updateExtendedField(currentTenantId.value!, formData.id, formData)
    } else {
      await createExtendedField(currentTenantId.value!, formData)
    }
    
    ElMessage.success('保存成功')
    extendedDialogVisible.value = false
    
    // 刷新列表
    await loadExtendedFields()
    await loadUnmappedFields()
  } catch (error) {
    console.error('保存拓展字段失败:', error)
    ElMessage.error('保存失败')
  }
}

// 拓展字段更新
const handleExtendedFieldUpdate = async (row: any) => {
  try {
    await updateExtendedField(currentTenantId.value!, row.id, row)
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新拓展字段失败:', error)
    ElMessage.error('更新失败')
  }
}

// 删除拓展字段
const handleDeleteExtended = async (row: any) => {
  try {
    await deleteExtendedField(currentTenantId.value!, row.id)
    ElMessage.success('删除成功')
    await loadExtendedFields()
  } catch (error) {
    console.error('删除拓展字段失败:', error)
    ElMessage.error('删除失败')
  }
}

// 匹配到目标字段
const handleMatchToTarget = (row: any) => {
  currentUnmappedField.value = row
  matchDialogVisible.value = true
}

// 确认匹配
const handleConfirmMatch = async (targetFieldKey: string) => {
  try {
    await saveFieldConfig(currentTenantId.value!, {
      field_key: targetFieldKey,
      tenant_field_key: currentUnmappedField.value.field_key,
      mapping_status: 'manual_mapped'
    })
    
    ElMessage.success('匹配成功')
    matchDialogVisible.value = false
    
    // 刷新数据
    await loadAllData()
  } catch (error) {
    console.error('匹配失败:', error)
    ElMessage.error('匹配失败')
  }
}

// 设为拓展字段
const handleSetAsExtended = (row: any) => {
  extendedForm.value = {
    field_alias: '',
    tenant_field_key: row.field_key,
    tenant_field_name: row.field_name,
    field_type: row.field_type,
    privacy_label: '公开',
    is_required: row.is_required || false,
    description: row.description || ''
  }
  isEditExtended.value = false
  extendedDialogVisible.value = true
}

// 打开版本管理
const openVersionManager = () => {
  versionManagerVisible.value = true
}

// 打开上传对话框
const openUploadDialog = () => {
  // TODO: 实现上传功能
  ElMessage.info('上传功能开发中...')
}

// 版本变更处理
const handleVersionChanged = async () => {
  await loadAllData()
}

// ==================== 工具函数 ====================

// 获取匹配状态文本
const getMappingStatusText = (status: string) => {
  const map: Record<string, string> = {
    'unmapped': '未映射',
    'auto_mapped': '已自动映射',
    'manual_mapped': '已手动映射'
  }
  return map[status] || '未映射'
}

// 获取匹配状态标签类型
const getMappingStatusTagType = (status: string) => {
  const map: Record<string, any> = {
    'unmapped': 'info',
    'auto_mapped': 'success',
    'manual_mapped': 'warning'
  }
  return map[status] || 'info'
}

// 获取隐私标签类型
const getPrivacyLabelType = (label: string) => {
  const map: Record<string, any> = {
    'PII': 'danger',
    '敏感': 'warning',
    '公开': 'success'
  }
  return map[label] || 'info'
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

// ==================== 生命周期 ====================
watch(() => currentTenantId.value, (newId) => {
  if (newId) {
    loadAllData()
  }
})

onMounted(() => {
  if (currentTenantId.value) {
    loadAllData()
  }
})
</script>

<style scoped>
.field-mapping-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 版本信息栏 */
.version-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f8 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #d9ecff;
}

.version-info-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.version-details {
  display: flex;
  gap: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.detail-item .el-icon {
  font-size: 16px;
  color: #409eff;
}

.version-info-right {
  display: flex;
  gap: 12px;
}

/* 未映射警告 */
.unmapped-alert {
  margin-bottom: 20px;
}

/* Tab内容区域 */
.tab-content {
  padding-top: 20px;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 表格样式 */
.mapping-table {
  width: 100%;
}

.target-field-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 6px;
}

.field-key {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.tenant-field-option {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.field-key-small {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.time-text {
  font-size: 13px;
  color: #606266;
}

/* 未映射提示 */
.unmapped-tip {
  margin-bottom: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .version-info-bar {
    flex-direction: column;
    gap: 16px;
  }

  .version-details {
    flex-direction: column;
    gap: 8px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls .el-select,
  .filter-controls .el-input {
    width: 100% !important;
  }
}
</style>
