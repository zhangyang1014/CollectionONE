<template>
  <div class="detail-mapping-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-info">
            <span class="title">案件详情字段映射配置</span>
            <div class="version-info" v-if="currentMappingVersion">
              <el-tag type="success" size="small">
                基于映射配置：v{{ currentMappingVersion.version_number }}
              </el-tag>
              <span class="info-text">
                来源：{{ currentMappingVersion.source || '上传版本' }}
              </span>
              <span class="info-text">
                拉取时间：{{ formatDate(currentMappingVersion.fetched_at) }}
              </span>
            </div>
          </div>
          <el-space>
            <el-button type="primary" @click="handleSave" :disabled="!canSave">
              <el-icon><Check /></el-icon>
              保存为新版本
            </el-button>
            <el-button @click="handleShowVersions">
              <el-icon><List /></el-icon>
              版本管理
            </el-button>
            <el-button @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧：视图切换和分组树 -->
        <el-col :span="5">
          <el-card shadow="never">
            <template #header>
              <div class="view-mode-selector">
                <div class="mode-title">视图模式</div>
                <el-radio-group v-model="viewMode" size="small">
                  <el-radio-button value="all">全部</el-radio-button>
                  <el-radio-button value="group">分组</el-radio-button>
                </el-radio-group>
              </div>
            </template>

            <!-- 分组树（仅分组视图显示） -->
            <el-tree
              v-if="viewMode === 'group'"
              :data="groupTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="key"
              :default-expand-all="true"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleGroupClick"
              class="field-group-tree"
            />

            <!-- 全部视图提示 -->
            <el-empty 
              v-else 
              description="全部视图模式下显示所有标准字段" 
              :image-size="80"
            />
          </el-card>

          <!-- 映射进度统计 -->
          <el-card shadow="never" style="margin-top: 15px">
            <template #header>映射完成度</template>
            <div class="mapping-progress">
              <div class="progress-summary">
                <el-progress 
                  :percentage="mappingProgress.percentage" 
                  :status="mappingProgress.percentage === 100 ? 'success' : 'warning'"
                />
                <div class="progress-text">
                  {{ mappingProgress.mapped }}/{{ mappingProgress.total }} 
                  ({{ mappingProgress.percentage }}%)
                </div>
              </div>
              
              <div class="group-progress">
                <div 
                  v-for="group in groupMappingProgress" 
                  :key="group.group_key"
                  class="group-item"
                >
                  <div class="group-name">
                    <el-icon v-if="group.percentage === 100"><CircleCheckFilled /></el-icon>
                    <el-icon v-else style="color: #e6a23c"><WarningFilled /></el-icon>
                    {{ group.group_name }}
                  </div>
                  <div class="group-stat">
                    {{ group.mapped }}/{{ group.total }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：Tab内容区 -->
        <el-col :span="19">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- Tab1: 匹配目标字段 -->
            <el-tab-pane label="匹配目标字段" name="mapping">
              <div class="tab-header">
                <div class="tab-info">
                  <span v-if="viewMode === 'group' && activeGroup !== 'all'">
                    当前分组：<el-tag size="small">{{ getGroupName(activeGroup) }}</el-tag>
                  </span>
                </div>
                <el-space>
                  <el-button 
                    type="warning" 
                    size="small" 
                    @click="handleSmartMatch"
                  >
                    <el-icon><MagicStick /></el-icon>
                    智能匹配建议
                  </el-button>
                  <el-input
                    v-model="searchText"
                    placeholder="搜索字段..."
                    style="width: 200px"
                    size="small"
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </el-space>
              </div>

              <!-- 映射表格 -->
              <el-table 
                :data="displayedMappings" 
                border 
                style="width: 100%; margin-top: 10px"
                v-loading="loading"
                max-height="600"
              >
                <el-table-column type="index" label="序号" width="60" />
                <el-table-column 
                  v-if="viewMode === 'all'" 
                  prop="group_name" 
                  label="分组" 
                  width="100" 
                />
                <el-table-column label="标准字段" min-width="200">
                  <template #default="{ row }">
                    <div class="field-cell">
                      <div class="field-name">{{ row.field_name }}</div>
                      <div class="field-key">{{ row.field_key }}</div>
                      <el-tag size="small" type="info">{{ row.field_type || row.field_data_type }}</el-tag>
                      <el-tag v-if="row.is_required" size="small" type="danger">必填</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="映射" width="80" align="center">
                  <template #default="{ row }">
                    <el-icon 
                      v-if="row.mapped_to" 
                      style="color: #67c23a; font-size: 20px"
                    >
                      <CircleCheckFilled />
                    </el-icon>
                    <el-icon 
                      v-else 
                      style="color: #c0c4cc; font-size: 20px"
                    >
                      <CircleCloseFilled />
                    </el-icon>
                  </template>
                </el-table-column>
                <el-table-column label="甲方字段" min-width="200">
                  <template #default="{ row }">
                    <div v-if="row.mapped_to" class="field-cell">
                      <div class="field-name">{{ row.mapped_to.field_name }}</div>
                      <div class="field-key">{{ row.mapped_to.field_key }}</div>
                      <el-tag size="small" type="info">{{ row.mapped_to.field_type }}</el-tag>
                      <el-tag 
                        v-if="row.mapped_to.group_name" 
                        size="small" 
                        type="warning"
                      >
                        来自：{{ row.mapped_to.group_name }}
                      </el-tag>
                    </div>
                    <el-tag v-else type="info">未映射</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag 
                      v-if="row.mapping_status === 'auto_mapped'" 
                      type="success" 
                      size="small"
                    >
                      🟢 自动
                    </el-tag>
                    <el-tag 
                      v-else-if="row.mapping_status === 'manual_mapped'" 
                      type="primary" 
                      size="small"
                    >
                      🔵 手动
                    </el-tag>
                    <el-tag 
                      v-else 
                      type="info" 
                      size="small"
                    >
                      ⚪ 未映射
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row }">
                    <el-button 
                      link 
                      type="primary" 
                      size="small"
                      @click="handleEditMapping(row)"
                    >
                      {{ row.mapped_to ? '重新映射' : '选择映射' }}
                    </el-button>
                    <el-button 
                      v-if="row.mapped_to"
                      link 
                      type="danger" 
                      size="small"
                      @click="handleClearMapping(row)"
                    >
                      清除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- Tab2: 拓展字段 -->
            <el-tab-pane label="拓展字段" name="extended">
              <div class="tab-header">
                <div class="tab-info">
                  拓展字段数量：<el-tag size="small">{{ extendedFields.length }}</el-tag>
                </div>
                <el-button type="primary" size="small" @click="handleAddExtended">
                  <el-icon><Plus /></el-icon>
                  添加拓展字段
                </el-button>
              </div>

              <!-- 拓展字段表格 -->
              <el-table 
                :data="extendedFields" 
                border 
                style="width: 100%; margin-top: 10px"
              >
                <el-table-column type="index" label="序号" width="60" />
                <el-table-column prop="field_alias" label="字段别名" width="150" />
                <el-table-column label="甲方字段" min-width="200">
                  <template #default="{ row }">
                    <div class="field-cell">
                      <div class="field-name">{{ row.tenant_field_name }}</div>
                      <div class="field-key">{{ row.tenant_field_key }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="target_group_name" label="目标分组" width="120" />
                <el-table-column prop="source_group_name" label="来源分组" width="120" />
                <el-table-column label="操作" width="150">
                  <template #default="{ row, $index }">
                    <el-button 
                      link 
                      type="primary" 
                      size="small"
                      @click="handleEditExtended(row, $index)"
                    >
                      编辑
                    </el-button>
                    <el-button 
                      link 
                      type="danger" 
                      size="small"
                      @click="handleDeleteExtended($index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- Tab3: 未使用的甲方字段 -->
            <el-tab-pane label="未使用的甲方字段" name="unused">
              <div class="tab-header">
                <div class="tab-info">
                  未使用字段数量：<el-tag size="small" type="warning">{{ unusedTenantFields.length }}</el-tag>
                </div>
              </div>

              <!-- 未使用字段表格（按分组） -->
              <div v-for="group in unusedFieldsByGroup" :key="group.group_key" style="margin-bottom: 20px">
                <div class="group-section-title">
                  <el-icon><Folder /></el-icon>
                  <span>{{ group.group_name }}</span>
                  <el-tag size="small" type="warning">{{ group.fields.length }}个未使用</el-tag>
                </div>
                
                <el-table :data="group.fields" border>
                  <el-table-column type="index" label="序号" width="60" />
                  <el-table-column label="甲方字段" min-width="200">
                    <template #default="{ row }">
                      <div class="field-cell">
                        <div class="field-name">{{ row.field_name }}</div>
                        <div class="field-key">{{ row.field_key }}</div>
                        <el-tag size="small" type="info">{{ row.field_type }}</el-tag>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="{ row }">
                      <el-button 
                        link 
                        type="primary" 
                        size="small"
                        @click="handleMapUnused(row)"
                      >
                        映射到标准字段
                      </el-button>
                      <el-button 
                        link 
                        type="success" 
                        size="small"
                        @click="handleCreateExtendedFromUnused(row)"
                      >
                        设为拓展字段
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <el-empty 
                v-if="unusedTenantFields.length === 0" 
                description="所有甲方字段都已使用" 
              />
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑映射对话框 -->
    <el-dialog
      v-model="mappingDialogVisible"
      :title="`编辑映射 - ${editingMapping?.field_name || ''}`"
      width="700px"
    >
      <div v-if="editingMapping" class="mapping-dialog">
        <!-- 标准字段信息 -->
        <div class="section">
          <div class="section-title">标准字段信息（目标）</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="所属分组">{{ editingMapping.group_name }}</el-descriptions-item>
            <el-descriptions-item label="字段名称">{{ editingMapping.field_name }}</el-descriptions-item>
            <el-descriptions-item label="字段Key">{{ editingMapping.field_key }}</el-descriptions-item>
            <el-descriptions-item label="字段类型">{{ editingMapping.field_type || editingMapping.field_data_type }}</el-descriptions-item>
            <el-descriptions-item label="是否必填">
              <el-tag :type="editingMapping.is_required ? 'danger' : 'info'" size="small">
                {{ editingMapping.is_required ? '✓ 必填' : '非必填' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 选择甲方字段 -->
        <div class="section">
          <div class="section-title">选择甲方字段（源）</div>
          <el-select
            v-model="selectedTenantField"
            placeholder="搜索或选择甲方字段"
            filterable
            style="width: 100%"
          >
            <el-option-group
              v-for="group in tenantFieldsByGroup"
              :key="group.group_key"
              :label="`📁 ${group.group_name}`"
            >
              <el-option
                v-for="field in group.fields"
                :key="field.field_key"
                :label="`${field.field_name} (${field.field_key})`"
                :value="field.field_key"
              >
                <div style="display: flex; justify-content: space-between;">
                  <span>{{ field.field_name }}</span>
                  <span style="color: #8492a6; font-size: 12px;">
                    {{ field.field_type }}
                  </span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
        </div>

        <!-- 已选择的甲方字段 -->
        <div v-if="selectedTenantFieldInfo" class="section">
          <div class="section-title">已选择的甲方字段</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="所属分组">{{ selectedTenantFieldInfo.group_name }}</el-descriptions-item>
            <el-descriptions-item label="字段名称">{{ selectedTenantFieldInfo.field_name }}</el-descriptions-item>
            <el-descriptions-item label="字段Key">{{ selectedTenantFieldInfo.field_key }}</el-descriptions-item>
            <el-descriptions-item label="字段类型">{{ selectedTenantFieldInfo.field_type }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="mappingDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmMapping"
          :disabled="!selectedTenantField"
        >
          确认映射
        </el-button>
      </template>
    </el-dialog>

    <!-- 拓展字段对话框 -->
    <el-dialog
      v-model="extendedDialogVisible"
      :title="editingExtendedIndex === -1 ? '添加拓展字段' : '编辑拓展字段'"
      width="600px"
    >
      <el-form :model="extendedForm" label-width="120px">
        <el-form-item label="甲方字段">
          <el-select
            v-model="extendedForm.tenant_field_key"
            placeholder="选择甲方字段"
            filterable
            style="width: 100%"
            :disabled="editingExtendedIndex !== -1"
          >
            <el-option-group
              v-for="group in tenantFieldsByGroup"
              :key="group.group_key"
              :label="`📁 ${group.group_name}`"
            >
              <el-option
                v-for="field in group.fields"
                :key="field.field_key"
                :label="`${field.field_name} (${field.field_key})`"
                :value="field.field_key"
              />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="字段别名">
          <el-input
            v-model="extendedForm.field_alias"
            placeholder="只能包含小写字母、数字、下划线"
            maxlength="50"
          />
          <div class="form-tip">系统内部使用，建议与甲方字段保持一致</div>
        </el-form-item>

        <el-form-item label="目标分组">
          <el-select v-model="extendedForm.target_group_key" style="width: 100%">
            <el-option
              v-for="group in allGroups"
              :key="group.group_key"
              :label="group.group_name"
              :value="group.group_key"
            />
          </el-select>
          <div class="form-tip">拓展字段将归属到该分组</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="extendedDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmExtended">确定</el-button>
      </template>
    </el-dialog>

    <!-- 智能匹配对话框 -->
    <el-dialog
      v-model="smartMatchDialogVisible"
      title="智能匹配建议"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="smart-match-dialog">
        <!-- 范围选择 -->
        <div class="match-scope">
          <div class="section-title">选择匹配范围</div>
          <el-radio-group v-model="matchScope" size="small">
            <el-radio value="all">全部字段（{{ standardFields.length }}个）</el-radio>
            <el-radio 
              v-if="viewMode === 'group' && activeGroup !== 'all'" 
              value="group"
            >
              当前分组（{{ getGroupName(activeGroup) }}）
            </el-radio>
            <el-radio value="unmapped">未映射字段（{{ unmappedCount }}个）</el-radio>
          </el-radio-group>
          <el-button 
            type="primary" 
            size="small" 
            @click="handleDoSmartMatch"
            :loading="matching"
            style="margin-left: 20px"
          >
            开始匹配
          </el-button>
        </div>

        <!-- 匹配结果 -->
        <div v-if="matchSuggestions.length > 0" class="match-results">
          <div class="match-summary">
            <el-alert type="success" :closable="false">
              <template #title>
                发现 {{ matchSuggestions.length }} 个匹配建议
              </template>
              <ul>
                <li>✅ 自动映射（相似度≥80%）：{{ highConfidenceCount }}个</li>
                <li>🔍 建议映射（相似度60-80%）：{{ mediumConfidenceCount }}个</li>
              </ul>
            </el-alert>
          </div>

          <!-- 按分组展示建议 -->
          <div v-for="group in suggestionsByGroup" :key="group.group_key" class="suggestions-group">
            <div class="group-section-title">
              <el-icon><Folder /></el-icon>
              <span>{{ group.group_name }}</span>
              <el-tag size="small">{{ group.suggestions.length }}个建议</el-tag>
            </div>
            
            <div class="suggestions-list">
              <div 
                v-for="(suggestion, index) in group.suggestions" 
                :key="index"
                class="suggestion-item"
              >
                <el-checkbox v-model="suggestion.selected">
                  <div class="suggestion-content">
                    <div class="suggestion-main">
                      <span class="standard-field">{{ suggestion.standard_field_name }}</span>
                      <el-icon><Right /></el-icon>
                      <span class="tenant-field">{{ suggestion.tenant_field_name }}</span>
                      <el-tag 
                        :type="suggestion.similarity >= 80 ? 'success' : 'warning'" 
                        size="small"
                      >
                        {{ suggestion.similarity }}%
                      </el-tag>
                    </div>
                    <div class="suggestion-details">
                      <span>类型匹配：{{ suggestion.type_match ? '✓' : '✗' }}</span>
                      <span>匹配方式：{{ suggestion.match_type }}</span>
                    </div>
                  </div>
                </el-checkbox>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="smartMatchDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleApplySelectedSuggestions"
          :disabled="selectedSuggestionsCount === 0"
        >
          确认选中项（{{ selectedSuggestionsCount }}）
        </el-button>
        <el-button 
          type="success" 
          @click="handleApplyAllSuggestions"
          :disabled="matchSuggestions.length === 0"
        >
          确认全部
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, List, Refresh, Search, MagicStick, Plus, Folder, Right,
  CircleCheckFilled, CircleCloseFilled, WarningFilled
} from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { getCaseDetailStandardFields } from '@/api/field'
import { getDetailTenantFieldsJson } from '@/api/detailTenantFields'
import { getDetailFieldGroups } from '@/api/detailFieldGroup'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

// 数据状态
const loading = ref(false)
const standardFields = ref<any[]>([]) // 标准字段（含分组）
const tenantFields = ref<any[]>([]) // 甲方字段（含分组）
const allGroups = ref<any[]>([]) // 所有分组
const currentMappingVersion = ref<any>(null)

// 视图状态
const viewMode = ref<'all' | 'group'>('all')
const activeGroup = ref<string | number>('all')
const activeTab = ref('mapping')
const searchText = ref('')

// 映射数据
const mappings = ref<any[]>([]) // 标准字段到甲方字段的映射关系
const extendedFields = ref<any[]>([]) // 拓展字段列表

// 对话框状态
const mappingDialogVisible = ref(false)
const editingMapping = ref<any>(null)
const selectedTenantField = ref('')

const extendedDialogVisible = ref(false)
const editingExtendedIndex = ref(-1)
const extendedForm = ref({
  tenant_field_key: '',
  field_alias: '',
  target_group_key: '',
  source_group_key: ''
})

// 智能匹配
const smartMatchDialogVisible = ref(false)
const matchScope = ref('all')
const matching = ref(false)
const matchSuggestions = ref<any[]>([])

// 计算属性 - 分组树
const groupTree = computed(() => {
  const roots = allGroups.value.filter(g => !g.parent_id)
  const buildChildren = (parentId: number) => {
    return allGroups.value
      .filter(g => g.parent_id === parentId)
      .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
      .map(g => ({
        key: g.id,
        label: `${g.group_name} (${getGroupMappedCount(g.group_key)}/${getGroupFieldCount(g.group_key)})`,
        groupKey: g.group_key,
        children: buildChildren(g.id)
      }))
  }
  
  return roots
    .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    .map(g => ({
      key: g.id,
      label: `${g.group_name} (${getGroupMappedCount(g.group_key)}/${getGroupFieldCount(g.group_key)})`,
      groupKey: g.group_key,
      children: buildChildren(g.id)
    }))
})

// 计算每个分组的字段数
const getGroupFieldCount = (groupKey: string) => {
  return standardFields.value.filter(f => f.group_key === groupKey).length
}

// 计算每个分组已映射字段数
const getGroupMappedCount = (groupKey: string) => {
  return mappings.value.filter(m => m.group_key === groupKey && m.mapped_to).length
}

// 计算分组名称
const getGroupName = (groupId: string | number) => {
  if (groupId === 'all') return '全部'
  const group = allGroups.value.find(g => g.id === groupId)
  return group?.group_name || ''
}

// 显示的映射列表（根据视图模式和分组筛选）
const displayedMappings = computed(() => {
  let result = mappings.value

  // 分组筛选
  if (viewMode.value === 'group' && activeGroup.value !== 'all') {
    const group = allGroups.value.find(g => g.id === activeGroup.value)
    if (group) {
      result = result.filter(m => m.group_key === group.group_key)
    }
  }

  // 搜索
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(m =>
      m.field_name?.toLowerCase().includes(search) ||
      m.field_key?.toLowerCase().includes(search) ||
      m.mapped_to?.field_name?.toLowerCase().includes(search) ||
      m.mapped_to?.field_key?.toLowerCase().includes(search)
    )
  }

  return result
})

// 映射进度
const mappingProgress = computed(() => {
  const total = standardFields.value.length
  const mapped = mappings.value.filter(m => m.mapped_to).length
  return {
    total,
    mapped,
    percentage: total > 0 ? Math.round((mapped / total) * 100) : 0
  }
})

// 各分组映射进度
const groupMappingProgress = computed(() => {
  const groups = allGroups.value.filter(g => !g.parent_id)
  return groups.map(g => {
    const total = getGroupFieldCount(g.group_key)
    const mapped = getGroupMappedCount(g.group_key)
    return {
      group_key: g.group_key,
      group_name: g.group_name,
      total,
      mapped,
      percentage: total > 0 ? Math.round((mapped / total) * 100) : 0
    }
  })
})

// 是否可以保存
const canSave = computed(() => {
  // 所有标准字段必须映射完成
  return mappingProgress.value.percentage === 100
})

// 未映射字段数量
const unmappedCount = computed(() => {
  return standardFields.value.length - mappings.value.filter(m => m.mapped_to).length
})

// 甲方字段按分组
const tenantFieldsByGroup = computed(() => {
  const groups: Record<string, any> = {}
  tenantFields.value.forEach(field => {
    const groupKey = field.group_key || 'other'
    if (!groups[groupKey]) {
      groups[groupKey] = {
        group_key: groupKey,
        group_name: field.group_name || '其他',
        fields: []
      }
    }
    groups[groupKey].fields.push(field)
  })
  return Object.values(groups)
})

// 未使用的甲方字段
const unusedTenantFields = computed(() => {
  const usedKeys = new Set()
  
  // 收集已映射的甲方字段
  mappings.value.forEach(m => {
    if (m.mapped_to) {
      usedKeys.add(m.mapped_to.field_key)
    }
  })
  
  // 收集拓展字段使用的甲方字段
  extendedFields.value.forEach(e => {
    usedKeys.add(e.tenant_field_key)
  })
  
  return tenantFields.value.filter(f => !usedKeys.has(f.field_key))
})

// 未使用字段按分组
const unusedFieldsByGroup = computed(() => {
  const groups: Record<string, any> = {}
  unusedTenantFields.value.forEach(field => {
    const groupKey = field.group_key || 'other'
    if (!groups[groupKey]) {
      groups[groupKey] = {
        group_key: groupKey,
        group_name: field.group_name || '其他',
        fields: []
      }
    }
    groups[groupKey].fields.push(field)
  })
  return Object.values(groups)
})

// 选中的甲方字段信息
const selectedTenantFieldInfo = computed(() => {
  if (!selectedTenantField.value) return null
  return tenantFields.value.find(f => f.field_key === selectedTenantField.value)
})

// 匹配建议统计
const highConfidenceCount = computed(() => {
  return matchSuggestions.value.filter(s => s.similarity >= 80).length
})

const mediumConfidenceCount = computed(() => {
  return matchSuggestions.value.filter(s => s.similarity >= 60 && s.similarity < 80).length
})

// 匹配建议按分组
const suggestionsByGroup = computed(() => {
  const groups: Record<string, any> = {}
  matchSuggestions.value.forEach(suggestion => {
    const groupKey = suggestion.group_key || 'other'
    if (!groups[groupKey]) {
      groups[groupKey] = {
        group_key: groupKey,
        group_name: suggestion.group_name || '其他',
        suggestions: []
      }
    }
    groups[groupKey].suggestions.push(suggestion)
  })
  return Object.values(groups)
})

// 选中的建议数量
const selectedSuggestionsCount = computed(() => {
  return matchSuggestions.value.filter(s => s.selected).length
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadStandardFields(),
      loadTenantFields(),
      loadGroups()
    ])
    
    // 初始化映射关系
    initializeMappings()
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadStandardFields = async () => {
  const res = await getCaseDetailStandardFields()
  const groups = res?.groups || []
  
  // 展平标准字段
  const fields: any[] = []
  groups.forEach(group => {
    if (group.fields && Array.isArray(group.fields)) {
      group.fields.forEach((field: any) => {
        fields.push({
          ...field,
          // 统一字段类型属性名：标准字段API返回field_data_type，统一转换为field_type
          field_type: field.field_data_type || field.field_type,
          group_key: group.group_key,
          group_name: group.group_name
        })
      })
    }
  })
  standardFields.value = fields
}

const loadTenantFields = async () => {
  try {
    const res = await getDetailTenantFieldsJson(Number(currentTenantId.value))
    const groups = res?.groups || []
    
    // 展平甲方字段
    const fields: any[] = []
    groups.forEach(group => {
      if (group.fields && Array.isArray(group.fields)) {
        group.fields.forEach((field: any) => {
          fields.push({
            ...field,
            group_key: group.group_key,
            group_name: group.group_name
          })
        })
      }
    })
    tenantFields.value = fields
    
    currentMappingVersion.value = {
      version_number: res.version || '1',
      source: '上传版本',
      fetched_at: res.fetched_at || new Date().toISOString()
    }
  } catch (e) {
    console.error('加载甲方字段失败：', e)
    tenantFields.value = []
  }
}

const loadGroups = async () => {
  try {
    const data = await getDetailFieldGroups({ tenantId: Number(currentTenantId.value) })
    allGroups.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (e) {
    console.error('加载分组失败：', e)
  }
}

const initializeMappings = () => {
  // 为每个标准字段创建映射记录
  mappings.value = standardFields.value.map(sf => ({
    ...sf,
    mapped_to: null,
    mapping_status: 'unmapped'
  }))
}

const handleGroupClick = (node: any) => {
  activeGroup.value = node.key
}

// 编辑映射
const handleEditMapping = (mapping: any) => {
  editingMapping.value = mapping
  selectedTenantField.value = mapping.mapped_to?.field_key || ''
  mappingDialogVisible.value = true
}

// 确认映射
const handleConfirmMapping = () => {
  if (!selectedTenantField.value || !editingMapping.value) return
  
  const tenantField = tenantFields.value.find(f => f.field_key === selectedTenantField.value)
  if (!tenantField) return
  
  const index = mappings.value.findIndex(m => m.field_key === editingMapping.value.field_key)
  if (index !== -1) {
    mappings.value[index].mapped_to = {
      field_key: tenantField.field_key,
      field_name: tenantField.field_name,
      field_type: tenantField.field_type,
      group_name: tenantField.group_name
    }
    mappings.value[index].mapping_status = 'manual_mapped'
  }
  
  mappingDialogVisible.value = false
  ElMessage.success('映射成功')
}

// 清除映射
const handleClearMapping = (mapping: any) => {
  const index = mappings.value.findIndex(m => m.field_key === mapping.field_key)
  if (index !== -1) {
    mappings.value[index].mapped_to = null
    mappings.value[index].mapping_status = 'unmapped'
  }
  ElMessage.success('已清除映射')
}

// 智能匹配
const handleSmartMatch = () => {
  smartMatchDialogVisible.value = true
  matchScope.value = 'all'
  matchSuggestions.value = []
}

const handleDoSmartMatch = async () => {
  matching.value = true
  try {
    // TODO: 调用智能匹配API
    // 模拟匹配结果
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 生成模拟建议
    const suggestions: any[] = []
    const unmappedStandards = mappings.value.filter(m => !m.mapped_to)
    
    unmappedStandards.forEach(sm => {
      // 简单的名称匹配逻辑
      const tenantField = tenantFields.value.find(tf => 
        tf.field_name.includes(sm.field_name) || sm.field_name.includes(tf.field_name)
      )
      
      if (tenantField) {
        suggestions.push({
          standard_field_key: sm.field_key,
          standard_field_name: sm.field_name,
          tenant_field_key: tenantField.field_key,
          tenant_field_name: tenantField.field_name,
          group_key: sm.group_key,
          group_name: sm.group_name,
          similarity: Math.floor(Math.random() * 40) + 60,
          type_match: (sm.field_type || sm.field_data_type) === tenantField.field_type,
          match_type: '名称匹配',
          selected: true
        })
      }
    })
    
    matchSuggestions.value = suggestions
    
    if (suggestions.length === 0) {
      ElMessage.info('未找到匹配建议')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '匹配失败')
  } finally {
    matching.value = false
  }
}

const handleApplySelectedSuggestions = () => {
  const selected = matchSuggestions.value.filter(s => s.selected)
  
  selected.forEach(suggestion => {
    const index = mappings.value.findIndex(m => m.field_key === suggestion.standard_field_key)
    if (index !== -1) {
      const tenantField = tenantFields.value.find(f => f.field_key === suggestion.tenant_field_key)
      if (tenantField) {
        mappings.value[index].mapped_to = {
          field_key: tenantField.field_key,
          field_name: tenantField.field_name,
          field_type: tenantField.field_type,
          group_name: tenantField.group_name
        }
        mappings.value[index].mapping_status = 'auto_mapped'
      }
    }
  })
  
  smartMatchDialogVisible.value = false
  ElMessage.success(`已应用 ${selected.length} 个匹配建议`)
}

const handleApplyAllSuggestions = () => {
  matchSuggestions.value.forEach(s => s.selected = true)
  handleApplySelectedSuggestions()
}

// 拓展字段管理
const handleAddExtended = () => {
  editingExtendedIndex.value = -1
  extendedForm.value = {
    tenant_field_key: '',
    field_alias: '',
    target_group_key: '',
    source_group_key: ''
  }
  extendedDialogVisible.value = true
}

const handleEditExtended = (row: any, index: number) => {
  editingExtendedIndex.value = index
  extendedForm.value = { ...row }
  extendedDialogVisible.value = true
}

const handleConfirmExtended = () => {
  if (!extendedForm.value.tenant_field_key || !extendedForm.value.field_alias) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  const tenantField = tenantFields.value.find(f => f.field_key === extendedForm.value.tenant_field_key)
  if (!tenantField) return
  
  const targetGroup = allGroups.value.find(g => g.group_key === extendedForm.value.target_group_key)
  
  const extended = {
    ...extendedForm.value,
    tenant_field_name: tenantField.field_name,
    target_group_name: targetGroup?.group_name || '',
    source_group_name: tenantField.group_name
  }
  
  if (editingExtendedIndex.value === -1) {
    extendedFields.value.push(extended)
    ElMessage.success('添加成功')
  } else {
    extendedFields.value[editingExtendedIndex.value] = extended
    ElMessage.success('更新成功')
  }
  
  extendedDialogVisible.value = false
}

const handleDeleteExtended = (index: number) => {
  extendedFields.value.splice(index, 1)
  ElMessage.success('删除成功')
}

// 未使用字段处理
const handleMapUnused = (field: any) => {
  // 找到第一个未映射的标准字段
  const unmapped = mappings.value.find(m => !m.mapped_to)
  if (unmapped) {
    editingMapping.value = unmapped
    selectedTenantField.value = field.field_key
    mappingDialogVisible.value = true
  } else {
    ElMessage.info('所有标准字段都已映射')
  }
}

const handleCreateExtendedFromUnused = (field: any) => {
  editingExtendedIndex.value = -1
  extendedForm.value = {
    tenant_field_key: field.field_key,
    field_alias: field.field_key.toLowerCase(),
    target_group_key: field.group_key || '',
    source_group_key: field.group_key || ''
  }
  extendedDialogVisible.value = true
}

// 保存
const handleSave = async () => {
  if (!canSave.value) {
    ElMessage.warning('请先完成所有标准字段的映射')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定保存当前映射配置为新版本吗？',
      '提示',
      { type: 'warning' }
    )
    
    // TODO: 调用保存API
    const saveData = {
      tenant_id: Number(currentTenantId.value),
      scene: 'case_detail',
      field_mappings: mappings.value.filter(m => m.mapped_to).map(m => ({
        group_key: m.group_key,
        standard_field_key: m.field_key,
        tenant_field_key: m.mapped_to.field_key,
        tenant_field_group: m.mapped_to.group_name,
        mapping_status: m.mapping_status
      })),
      extended_fields: extendedFields.value,
      total_count: standardFields.value.length,
      mapped_count: mappings.value.filter(m => m.mapped_to).length
    }
    
    console.log('保存数据：', saveData)
    ElMessage.success('保存成功')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '保存失败')
    }
  }
}

const handleShowVersions = () => {
  ElMessage.info('版本管理功能开发中...')
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
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

.version-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.info-text {
  color: #606266;
  font-size: 14px;
}

.view-mode-selector {
  text-align: center;
}

.mode-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 36px;
}

.mapping-progress {
  padding: 10px 0;
}

.progress-summary {
  margin-bottom: 15px;
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.group-progress {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.group-name {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
}

.group-stat {
  font-size: 12px;
  color: #909399;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.field-cell {
  padding: 4px 0;
}

.field-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.field-key {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.field-cell .el-tag {
  margin-right: 5px;
}

.group-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.mapping-dialog .section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.smart-match-dialog {
  max-height: 600px;
  overflow-y: auto;
}

.match-scope {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.match-summary {
  margin-bottom: 20px;
}

.suggestions-group {
  margin-bottom: 20px;
}

.suggestions-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.suggestion-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-content {
  margin-left: 24px;
}

.suggestion-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.standard-field {
  font-weight: 600;
  color: #303133;
}

.tenant-field {
  font-weight: 600;
  color: #409eff;
}

.suggestion-details {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 15px;
}
</style>
