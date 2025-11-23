<template>
  <div class="custom-fields">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字段映射配置</span>
        </div>
      </template>

      <!-- 未使用字段警告 -->
      <el-alert
        v-if="unmappedTenantFields.length > 0"
        :title="`警告：发现 ${unmappedTenantFields.length} 个未使用的甲方字段，请尽快处理！`"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <div>未使用的字段可能导致数据丢失。请前往"未使用的甲方字段"tab进行处理。</div>
        </template>
      </el-alert>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- Tab 1: 匹配目标字段 -->
        <el-tab-pane label="匹配目标字段" name="matched">
          <el-row :gutter="20">
            <!-- 左侧：字段分组树 -->
            <el-col :span="5">
              <el-card shadow="never">
                <template #header>字段分组</template>
                <el-tree
                  :data="treeData"
                  :props="{ label: 'group_name', children: 'children' }"
                  node-key="id"
                  :default-expand-all="true"
                  :expand-on-click-node="false"
                  @node-click="handleGroupClick"
                  highlight-current
                  class="field-group-tree"
                />
              </el-card>
            </el-col>

            <!-- 右侧：字段列表 -->
            <el-col :span="19">
              <div class="table-header">
                <el-space>
                  <el-button 
                    type="primary" 
                    @click="handleAutoSuggestMapping"
                    :disabled="!currentTenantId || unmappedTenantFields.length === 0"
                  >
                    一键建议映射未匹配字段
                  </el-button>
                  <el-button 
                    type="success" 
                    @click="handleAddCustom" 
                    :disabled="!currentTenantId"
                  >
                    添加自定义字段
                  </el-button>
                </el-space>
              </div>
              
              <!-- 筛选器 -->
              <div class="filter-bar">
                <el-select 
                  v-model="mappingStatusFilter" 
                  placeholder="匹配状态" 
                  clearable
                  style="width: 150px; margin-right: 10px;"
                  @change="handleFilterChange"
                >
                  <el-option label="全部" value="" />
                  <el-option label="未映射" value="unmapped" />
                  <el-option label="已自动映射" value="auto_mapped" />
                  <el-option label="已手动映射" value="manual_mapped" />
                </el-select>
              </div>
          
          <el-table 
            ref="tableRef"
            :data="filteredFields" 
            row-key="id"
            border 
            style="width: 100%"
            class="sortable-table"
          >
            <!-- 拖拽手柄 -->
            <el-table-column width="50" align="center" fixed>
              <template #default>
                <el-icon class="drag-handle" style="cursor: move;">
                  <Rank />
                </el-icon>
              </template>
            </el-table-column>

            <!-- 目标字段（字段名称 + 字段标识） -->
            <el-table-column label="目标字段" width="200">
              <template #default="{ row }">
                <div class="target-field-cell">
                  <div class="field-name">{{ row.field_name }}</div>
                  <div class="field-key">{{ row.field_key }} · {{ row.field_type }}</div>
                </div>
              </template>
            </el-table-column>
            
            <!-- 匹配状态 -->
            <el-table-column prop="mapping_status" label="匹配状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="getMappingStatusType(row.mapping_status)"
                  size="small"
                >
                  {{ getMappingStatusText(row.mapping_status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <!-- 甲方字段（下拉选择） -->
            <el-table-column label="甲方字段" width="250">
              <template #default="{ row }">
                <el-select
                  v-model="row.tenant_field_key"
                  placeholder="选择甲方字段"
                  clearable
                  filterable
                  style="width: 100%"
                  @change="handleTenantFieldChange(row)"
                >
                  <el-option
                    v-for="field in tenantFields"
                    :key="field.key"
                    :label="`${field.key} (${field.id})`"
                    :value="field.key"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <span>{{ field.key }} ({{ field.id }})</span>
                      <span v-if="field.isAutoMatch" style="font-size: 12px; color: #67c23a;">系统建议</span>
                    </div>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            
            <!-- 枚举值 -->
            <el-table-column prop="enum_values" label="枚举值" width="200">
              <template #default="{ row }">
                <span v-if="row.field_type === 'Enum' && row.enum_values && row.enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in row.enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px"
                  >
                    {{ item.standard_name }}
                  </el-tag>
                  <el-tag v-if="row.enum_values.length > 2" size="small" type="info">
                    +{{ row.enum_values.length - 2 }}
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            
            <!-- 来源 -->
            <el-table-column prop="field_source" label="来源" width="100">
              <template #default="{ row }">
                <el-tag :type="row.field_source === 'standard' ? 'primary' : 'success'">
                  {{ row.field_source === 'standard' ? '标准' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <!-- 甲方更新时间 -->
            <el-table-column label="甲方更新时间" width="180" align="center">
              <template #default="{ row }">
                <span v-if="row.tenant_updated_at" style="color: #606266;">
                  {{ formatDateTime(row.tenant_updated_at) }}
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            
            <!-- 是否必填 -->
            <el-table-column prop="is_required" label="是否必填" width="100">
              <template #default="{ row }">
                <el-switch 
                  v-model="row.is_required" 
                  :disabled="row.field_source === 'standard'"
                  @change="handleFieldUpdate(row)"
                />
              </template>
            </el-table-column>
            
            <!-- 队列隐藏配置 -->
            <el-table-column label="队列可见性" width="120">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="primary" 
                  @click="handleQueueConfig(row)"
                  size="small"
                >
                  配置队列
                </el-button>
              </template>
            </el-table-column>
            
            <!-- 排序 -->
            <el-table-column prop="sort_order" label="排序" width="80" />
            
            <!-- 操作 -->
            <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
                <el-button 
                  link 
                  type="primary" 
                  @click="handleEdit(row)"
                  size="small"
                >
                  编辑
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  @click="handleDelete(row)"
                  size="small"
                  :disabled="row.field_source === 'standard'"
                >
                  删除
                </el-button>
          </template>
        </el-table-column>
      </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- Tab 2: 拓展字段 -->
        <el-tab-pane label="拓展字段" name="extended">
          <el-row :gutter="20">
            <!-- 左侧：字段分组树 -->
            <el-col :span="5">
              <el-card shadow="never">
                <template #header>字段分组</template>
                <el-tree
                  :data="treeData"
                  :props="{ label: 'group_name', children: 'children' }"
                  node-key="id"
                  :default-expand-all="true"
                  :expand-on-click-node="false"
                  @node-click="handleExtendedGroupClick"
                  highlight-current
                  class="field-group-tree"
                />
              </el-card>
            </el-col>

            <!-- 右侧：字段列表 -->
            <el-col :span="19">
              <div class="table-header">
                <el-button 
                  type="primary" 
                  @click="handleAddExtendedField"
                  :disabled="!currentTenantId"
                >
                  添加拓展字段
                </el-button>
              </div>

              <el-table 
                :data="filteredExtendedFields" 
                border 
                style="width: 100%"
              >
                <el-table-column prop="field_alias" label="扩展字段别名" width="180" />
                <el-table-column prop="tenant_field_name" label="甲方原始字段" width="180">
                  <template #default="{ row }">
                    <div>{{ row.tenant_field_name }}</div>
                    <div style="font-size: 12px; color: #909399;">{{ row.tenant_field_key }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="field_type" label="类型" width="100" />
                <el-table-column prop="privacy_label" label="隐私标签" width="100">
                  <template #default="{ row }">
                    <el-tag 
                      :type="row.privacy_label === 'PII' ? 'danger' : row.privacy_label === '敏感' ? 'warning' : 'success'"
                      size="small"
                    >
                      {{ row.privacy_label }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="is_required" label="是否必填" width="100">
                  <template #default="{ row }">
                    <el-switch 
                      v-model="row.is_required" 
                      @change="handleExtendedFieldUpdate(row)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleEditExtended(row)" size="small">
                      编辑
                    </el-button>
                    <el-button link type="danger" @click="handleDeleteExtended(row)" size="small">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- Tab 3: 未使用的甲方字段 -->
        <el-tab-pane label="未使用的甲方字段" name="unmapped">
          <div class="unmapped-fields-container">
            <el-alert
              title="提示：这些是甲方推送但尚未映射到标准字段或设为扩展字段的字段"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 20px"
            />

            <el-table 
              :data="unmappedTenantFields" 
              border 
              style="width: 100%"
            >
              <el-table-column label="字段名" width="250">
                <template #default="{ row }">
                  <div class="target-field-cell">
                    <div class="field-name">{{ row.tenant_field_name }}</div>
                    <div class="field-key">{{ row.tenant_field_key }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column label="甲方更新时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.tenant_updated_at) }}
                </template>
              </el-table-column>
              <el-table-column prop="is_required" label="是否必填" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.is_required ? 'success' : 'info'" size="small">
                    {{ row.is_required ? '必填' : '非必填' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button 
                    link 
                    type="primary" 
                    @click="handleMatchToTarget(row)" 
                    size="small"
                  >
                    匹配到目标字段
                  </el-button>
                  <el-button 
                    link 
                    type="success" 
                    @click="handleSetAsExtended(row)" 
                    size="small"
                  >
                    设为扩展字段
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑/添加对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" label-width="140px">
        <el-form-item label="字段来源" v-if="!form.id">
          <el-radio-group v-model="form.field_source" disabled>
            <el-radio value="standard">标准字段</el-radio>
            <el-radio value="custom">自定义字段</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="字段名称" v-if="form.field_source === 'custom'">
          <el-input v-model="form.field_name" placeholder="请输入字段名称" />
        </el-form-item>
        <el-form-item label="字段名称" v-else>
          <el-input v-model="form.field_name" disabled />
        </el-form-item>

        <el-form-item label="字段标识" v-if="form.field_source === 'custom'">
          <el-input v-model="form.field_key" placeholder="请输入字段标识" />
        </el-form-item>
        <el-form-item label="字段标识" v-else>
          <el-input v-model="form.field_key" disabled />
        </el-form-item>

        <el-form-item label="甲方字段标识">
          <el-input v-model="form.tenant_field_key" placeholder="甲方系统中的字段标识" />
        </el-form-item>

        <el-form-item label="甲方字段ID">
          <el-input v-model="form.tenant_field_id" placeholder="甲方系统中的字段ID" />
        </el-form-item>

        <el-form-item label="字段类型" v-if="form.field_source === 'custom'">
          <el-select v-model="form.field_type" @change="handleFieldTypeChange">
            <el-option label="文本" value="String" />
            <el-option label="整数" value="Integer" />
            <el-option label="小数" value="Decimal" />
            <el-option label="日期" value="Date" />
            <el-option label="日期时间" value="Datetime" />
            <el-option label="布尔" value="Boolean" />
            <el-option label="枚举" value="Enum" />
          </el-select>
        </el-form-item>
        <el-form-item label="字段类型" v-else>
          <el-input v-model="form.field_type" disabled />
        </el-form-item>

        <!-- 枚举值配置 -->
        <el-form-item v-if="form.field_type === 'Enum'" label="枚举值配置">
          <div class="enum-config">
            <div class="enum-header">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAddEnumValue"
                :disabled="form.field_source === 'standard' && !editingEnum"
              >
                添加枚举项
              </el-button>
              <el-button 
                v-if="form.field_source === 'standard' && !editingEnum"
                type="warning" 
                size="small" 
                @click="editingEnum = true"
                style="margin-left: 10px"
              >
                编辑枚举值
              </el-button>
            </div>
            <el-table :data="form.enum_values" border style="width: 100%; margin-top: 10px">
              <el-table-column label="标准名称" width="140">
                <template #default="{ row }">
                  <el-input 
                    v-model="row.standard_name" 
                    size="small" 
                    placeholder="如：待还款"
                    :disabled="form.field_source === 'standard' && !editingEnum"
                  />
                </template>
              </el-table-column>
              <el-table-column label="标准ID" width="140">
                <template #default="{ row }">
                  <el-input 
                    v-model="row.standard_id" 
                    size="small" 
                    placeholder="如：pending"
                    :disabled="form.field_source === 'standard' && !editingEnum"
                  />
                </template>
              </el-table-column>
              <el-table-column label="甲方名称" width="140">
                <template #default="{ row }">
                  <el-input 
                    v-model="row.tenant_name" 
                    size="small" 
                    placeholder="如：未还款"
                  />
                </template>
              </el-table-column>
              <el-table-column label="甲方ID" width="140">
                <template #default="{ row }">
                  <el-input 
                    v-model="row.tenant_id" 
                    size="small" 
                    placeholder="如：unpaid"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button 
                    link 
                    type="danger" 
                    size="small" 
                    @click="handleRemoveEnumValue($index)"
                    :disabled="form.field_source === 'standard' && !editingEnum"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>

        <el-form-item label="所属分组" v-if="form.field_source === 'custom'">
          <el-cascader
            v-model="form.field_group_path"
            :options="treeData"
            :props="{
              value: 'id',
              label: 'group_name',
              children: 'children',
              checkStrictly: true,
              emitPath: false
            }"
            placeholder="请选择分组"
            clearable
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="是否必填" v-if="form.field_source === 'custom'">
          <el-switch v-model="form.is_required" />
        </el-form-item>

        <!-- 筛选器配置 -->
        <el-form-item 
          v-if="form.field_type === 'String'" 
          label="控台搜索框"
        >
          <el-switch v-model="form.enable_search" />
          <span style="margin-left: 10px; color: #909399; font-size: 13px;">
            开启后该字段可作为搜索关键词
          </span>
        </el-form-item>

        <el-form-item 
          v-if="form.field_type === 'Enum'" 
          label="控台筛选器"
        >
          <el-switch v-model="form.enable_filter" />
          <span style="margin-left: 10px; color: #909399; font-size: 13px;">
            开启后该字段可作为下拉筛选器
          </span>
        </el-form-item>

        <el-form-item 
          v-if="form.field_type === 'Integer' || form.field_type === 'Decimal'" 
          label="范围筛选器"
        >
          <el-switch v-model="form.enable_range_filter" />
          <span style="margin-left: 10px; color: #909399; font-size: 13px;">
            开启后该字段可作为范围筛选器（如：金额范围）
          </span>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 队列可见性配置对话框 -->
    <el-dialog v-model="queueDialogVisible" title="队列可见性配置" width="500px">
      <el-form label-width="120px">
        <el-form-item label="选择隐藏队列">
          <el-checkbox-group v-model="selectedHiddenQueues">
            <el-checkbox 
              v-for="queue in queues" 
              :key="queue.id" 
              :label="queue.id"
            >
              {{ queue.queue_name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="queueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQueueConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 扩展字段编辑对话框 -->
    <el-dialog v-model="extendedDialogVisible" :title="extendedDialogTitle" width="700px">
      <el-form :model="extendedForm" label-width="150px">
        <el-form-item label="扩展字段别名" required>
          <el-input v-model="extendedForm.field_alias" placeholder="如：company_name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="extendedForm.field_type" style="width: 100%">
            <el-option label="文本" value="String" />
            <el-option label="整数" value="Integer" />
            <el-option label="小数" value="Decimal" />
            <el-option label="日期" value="Date" />
            <el-option label="日期时间" value="Datetime" />
            <el-option label="布尔" value="Boolean" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属分组">
          <el-cascader
            v-model="extendedForm.field_group_path"
            :options="treeData"
            :props="{
              value: 'id',
              label: 'group_name',
              children: 'children',
              checkStrictly: true,
              emitPath: false
            }"
            placeholder="请选择分组"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="隐私标签" required>
          <el-select v-model="extendedForm.privacy_label" style="width: 100%">
            <el-option label="PII（个人身份信息）" value="PII" />
            <el-option label="敏感" value="敏感" />
            <el-option label="公开" value="公开" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="extendedForm.is_required" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendedDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveExtended">保存</el-button>
      </template>
    </el-dialog>

    <!-- 匹配到目标字段对话框 -->
    <el-dialog v-model="matchDialogVisible" title="匹配到目标字段" width="600px">
      <el-form :model="matchForm" label-width="120px">
        <el-form-item label="甲方字段">
          <div class="target-field-cell">
            <div class="field-name">{{ matchForm.tenant_field_name }}</div>
            <div class="field-key">{{ matchForm.tenant_field_key }} · {{ matchForm.field_type }}</div>
          </div>
        </el-form-item>
        <el-form-item label="匹配到" required>
          <el-select 
            v-model="matchForm.target_field_id" 
            filterable
            placeholder="请选择目标字段"
            style="width: 100%"
          >
            <el-option 
              v-for="field in fields" 
              :key="field.id" 
              :label="`${field.field_name} (${field.field_key})`"
              :value="field.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="matchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmMatch">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getFieldGroups, getStandardFields } from '@/api/field'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'

const tenantStore = useTenantStore()
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)

// Tab状态
const activeTab = ref('matched')

// 基础数据
const fields = ref<any[]>([])
const filteredFields = ref<any[]>([])
const allGroups = ref<any[]>([])
const treeData = ref<any[]>([])
const currentGroupId = ref<number>()
const tableRef = ref()
const mappingStatusFilter = ref<string>('')
let sortableInstance: any = null

// 扩展字段数据
const extendedFields = ref<any[]>([])
const filteredExtendedFields = ref<any[]>([])
const currentExtendedGroupId = ref<number>()

// 未使用的甲方字段
const unmappedTenantFields = ref<any[]>([])

// Mock甲方字段数据（后续从API获取）
const tenantFields = ref<Array<{
  key: string
  id: string
  isAutoMatch?: boolean
}>>([
  { key: 'LOAN_ID', id: 'loan_id_035', isAutoMatch: true },
  { key: 'USER_ID', id: 'uid', isAutoMatch: true },
  { key: 'CASE_STATUS', id: 'case_status_036', isAutoMatch: true },
  { key: 'TOTAL_DUE_AMOUNT', id: 'total_due_amount', isAutoMatch: true },
  { key: 'USER_NAME', id: 'user_name_002' },
  { key: 'MOBILE', id: 'mobile_003' },
  { key: 'GENDER', id: 'gender_003' },
  { key: 'BIRTH_DATE', id: 'birth_date_004' },
  { key: 'MARITAL_STATUS', id: 'marital_status_005' },
  { key: 'ID_TYPE', id: 'id_type_006' },
  { key: 'ID_NUMBER', id: 'id_number_007' },
  { key: 'ADDRESS', id: 'address_008' },
  { key: 'YEARS_AT_ADDRESS', id: 'years_at_address_009' },
  { key: 'HOUSING_TYPE', id: 'housing_type_010' },
  { key: 'EDUCATION_LEVEL', id: 'education_level_011' },
  { key: 'OCCUPATION', id: 'occupation_012' },
  { key: 'MONTHLY_INCOME', id: 'monthly_income_013' },
  { key: 'LOAN_AMOUNT', id: 'loan_amount_014' },
  { key: 'REPAID_AMOUNT', id: 'repaid_amount_015' },
  { key: 'OUTSTANDING_AMOUNT', id: 'outstanding_amount_016' },
])

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  (newTenantId) => {
    currentTenantId.value = newTenantId
    if (newTenantId) {
      loadGroups()
    } else {
      fields.value = []
      filteredFields.value = []
      treeData.value = []
    }
  }
)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingEnum = ref(false) // 标准字段的枚举值编辑状态
const form = ref<any>({
  field_name: '',
  field_key: '',
  tenant_field_key: '',
  tenant_field_id: '',
  field_type: 'String',
  field_group_path: 0,
  field_source: 'custom',
  is_required: false,
  sort_order: 0,
  enable_search: false,        // String类型：控台搜索框
  enable_filter: false,        // Enum类型：控台筛选器
  enable_range_filter: false,  // 数字类型：范围筛选器
  enum_values: [] as Array<{
    standard_name: string
    standard_id: string
    tenant_name: string
    tenant_id: string
  }>
})

const queueDialogVisible = ref(false)
const currentField = ref<any>(null)
const queues = ref<any[]>([
  { id: 1, queue_name: 'M1队列' },
  { id: 2, queue_name: 'M2队列' },
  { id: 3, queue_name: 'M3+队列' },
])
const selectedHiddenQueues = ref<number[]>([])

// 扩展字段对话框
const extendedDialogVisible = ref(false)
const extendedDialogTitle = ref('')
const extendedForm = ref<any>({
  field_alias: '',
  tenant_field_key: '',
  tenant_field_name: '',
  field_type: 'String',
  field_group_path: 0,
  privacy_label: '公开',
  is_required: false
})

// 匹配到目标字段对话框
const matchDialogVisible = ref(false)
const matchForm = ref<any>({
  tenant_field_key: '',
  tenant_field_name: '',
  field_type: '',
  target_field_id: null
})

// 加载字段分组
const loadGroups = async () => {
  try {
    const res = await getFieldGroups()
    // API直接返回数组，不是{data: [...]}格式
    allGroups.value = Array.isArray(res) ? res : (res.data || [])
    treeData.value = buildTree(allGroups.value)
    
    // 自动选中第一个分组（但只在已选择甲方的情况下加载字段）
    if (treeData.value.length > 0) {
      currentGroupId.value = treeData.value[0].id
      if (currentTenantId.value) {
        loadFields(treeData.value[0].id)
      }
    }
  } catch (error) {
    console.error('加载分组失败：', error)
    ElMessage.error('加载分组失败')
  }
}

// 构建树形数据
const buildTree = (groups: any[]) => {
  const map = new Map()
  const roots: any[] = []

  groups.forEach((group) => {
    map.set(group.id, { ...group, children: [] })
  })

  groups.forEach((group) => {
    const node = map.get(group.id)
    if (group.parent_id) {
      const parent = map.get(group.parent_id)
      if (parent) {
        parent.children.push(node)
      }
    } else {
      roots.push(node)
    }
  })

  return roots
}

// 获取匹配状态文本
const getMappingStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'unmapped': '未映射',
    'auto_mapped': '已自动映射',
    'manual_mapped': '已手动映射'
  }
  return statusMap[status] || '未映射'
}

// 获取匹配状态标签类型
const getMappingStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'unmapped': 'info',
    'auto_mapped': 'success',
    'manual_mapped': 'warning'
  }
  return typeMap[status] || 'info'
}

// 计算匹配状态
const calculateMappingStatus = (field: any): string => {
  // 如果字段有tenant_field_key或tenant_field_id，说明已映射
  if (field.tenant_field_key || field.tenant_field_id) {
    // 判断是自动映射还是手动映射（这里可以根据业务逻辑判断）
    // 假设：如果tenant_field_key和field_key相同，则是自动映射；否则是手动映射
    if (field.tenant_field_key === field.field_key || field.auto_mapped) {
      return 'auto_mapped'
    } else {
      return 'manual_mapped'
    }
  }
  return 'unmapped'
}

// 筛选字段
const filterFields = () => {
  if (!mappingStatusFilter.value) {
    filteredFields.value = fields.value
  } else {
    filteredFields.value = fields.value.filter(field => {
      const status = calculateMappingStatus(field)
      return status === mappingStatusFilter.value
    })
  }
}

// 筛选器变化处理
const handleFilterChange = () => {
  filterFields()
}

// 加载字段列表（标准字段全列，用于匹配甲方字段）
const loadFields = async (groupId?: number) => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    console.log('加载标准字段，分组ID：', groupId, '甲方ID：', currentTenantId.value)
    
    // 读取标准字段全列（用于匹配甲方字段）
    const params = groupId ? { field_group_id: groupId } : {}
    const res = await getStandardFields(params)
    // API直接返回数组，不是{data: [...]}格式
    const standardFields = Array.isArray(res) ? res : (res.data || [])
    
    // 同时加载甲方字段JSON数据，用于匹配
    let tenantFieldsData: any[] = []
    try {
      const tenantFieldsRes = await request({
        url: `/api/v1/tenants/${currentTenantId.value}/fields-json`,
        method: 'get',
      })
      const tenantData = tenantFieldsRes || {}
      tenantFieldsData = Array.isArray(tenantData.fields) ? tenantData.fields : (Array.isArray(tenantData) ? tenantData : [])
    } catch (error) {
      console.warn('加载甲方字段失败，将只显示标准字段：', error)
    }
    
    // 将标准字段与甲方字段进行匹配
    fields.value = standardFields.map((field: any) => {
      // 查找匹配的甲方字段（通过field_key匹配）
      const matchedTenantField = tenantFieldsData.find((tf: any) => 
        tf.field_key?.toLowerCase() === field.field_key?.toLowerCase() ||
        tf.field_key === field.field_key
      )
      
      return {
        ...field,
        field_source: 'standard',
        tenant_field_key: matchedTenantField?.field_key || null,
        tenant_field_name: matchedTenantField?.field_name || null,
        tenant_field_id: matchedTenantField?.id || null,
        mapping_status: calculateMappingStatus({
          tenant_field_key: matchedTenantField?.field_key,
          tenant_field_id: matchedTenantField?.id,
          field_key: field.field_key,
          auto_mapped: !!matchedTenantField
        }),
        tenant_updated_at: matchedTenantField?.updated_at || null
      }
    })
    
    console.log(`已加载 ${fields.value.length} 个标准字段`)
    console.log(`  - 已匹配：${fields.value.filter(f => f.mapping_status !== 'unmapped').length} 个`)
    console.log(`  - 未匹配：${fields.value.filter(f => f.mapping_status === 'unmapped').length} 个`)

    // 应用筛选
    filterFields()

    // 加载完成后初始化拖拽
    await nextTick()
    initSortable()
  } catch (error) {
    console.error('加载字段失败：', error)
    ElMessage.error('加载字段失败')
  }
}

// 初始化拖拽排序
const initSortable = () => {
  // 销毁旧的实例
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }

  const table = tableRef.value?.$el.querySelector('.el-table__body-wrapper tbody')
  if (!table) return

  sortableInstance = Sortable.create(table, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'sortable-ghost',
    chosenClass: 'sortable-chosen',
    onEnd: (evt: any) => {
      const { oldIndex, newIndex } = evt
      if (oldIndex === newIndex) return

      // 更新数组顺序
      const movedItem = filteredFields.value.splice(oldIndex, 1)[0]
      filteredFields.value.splice(newIndex, 0, movedItem)

      // 同步更新fields数组
      const fieldIndex = fields.value.findIndex(f => f.id === movedItem.id)
      if (fieldIndex !== -1) {
        const movedField = fields.value.splice(fieldIndex, 1)[0]
        const newFieldIndex = filteredFields.value.findIndex(f => f.id === movedItem.id)
        fields.value.splice(newFieldIndex, 0, movedField)
      }

      // 更新所有字段的 sort_order
      fields.value.forEach((field, index) => {
        field.sort_order = index + 1
      })

      ElMessage.success('排序已更新')
      // TODO: 调用API保存排序
    },
  })
}

// 点击分组
const handleGroupClick = (data: any) => {
  currentGroupId.value = data.id
  loadFields(data.id)
}

// 添加自定义字段
const handleAddCustom = () => {
  dialogTitle.value = '添加自定义字段'
  editingEnum.value = false
  form.value = {
    field_name: '',
    field_key: '',
    tenant_field_key: '',
    tenant_field_id: '',
    field_type: 'String',
    field_group_path: currentGroupId.value || 0,
    field_source: 'custom',
    is_required: false,
    sort_order: fields.value.length + 1,
    enum_values: []
  }
  dialogVisible.value = true
}

// 编辑字段
const handleEdit = (row: any) => {
  dialogTitle.value = row.field_source === 'standard' ? '编辑标准字段映射' : '编辑自定义字段'
  editingEnum.value = false
  form.value = { 
    ...row,
    field_group_path: row.field_group_id || row.field_group_path, // 编辑时将 field_group_id 赋值给 field_group_path
    enum_values: row.enum_values || [] // 确保枚举值正确加载
  }
  dialogVisible.value = true
}

// 字段类型变更处理
const handleFieldTypeChange = (value: string) => {
  if (value === 'Enum' && form.value.enum_values.length === 0) {
    // 如果切换到枚举类型且没有枚举值，添加一个默认项
    form.value.enum_values = [{
      standard_name: '',
      standard_id: '',
      tenant_name: '',
      tenant_id: ''
    }]
  }
}

// 添加枚举项
const handleAddEnumValue = () => {
  form.value.enum_values.push({
    standard_name: '',
    standard_id: '',
    tenant_name: '',
    tenant_id: ''
  })
}

// 删除枚举项
const handleRemoveEnumValue = (index: number) => {
  form.value.enum_values.splice(index, 1)
}

// 保存字段
const handleSave = () => {
  if (form.value.field_source === 'custom' && (!form.value.field_name || !form.value.field_key)) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 准备提交数据：将 field_group_path 转换为 field_group_id
  const submitData = {
    ...form.value,
    field_group_id: form.value.field_group_path
  }
  delete submitData.field_group_path
  
  // 如果有甲方字段映射，更新甲方更新时间
  if (submitData.tenant_field_key || submitData.tenant_field_id) {
    submitData.tenant_updated_at = new Date().toISOString()
  }

  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadFields(currentGroupId.value)
  // TODO: 调用API保存 submitData
}

// 甲方字段选择变化处理
const handleTenantFieldChange = (row: any) => {
  // 根据选择的key找到对应的id
  const selectedField = tenantFields.value.find(f => f.key === row.tenant_field_key)
  if (selectedField) {
    row.tenant_field_id = selectedField.id
    // 更新匹配状态
    row.mapping_status = calculateMappingStatus(row)
    // 更新甲方更新时间
    row.tenant_updated_at = new Date().toISOString()
    // 如果当前有筛选，重新筛选
    filterFields()
    handleFieldUpdate(row)
  } else {
    // 清空选择
    row.tenant_field_key = ''
    row.tenant_field_id = ''
    row.mapping_status = 'unmapped'
    filterFields()
  }
}

// 字段更新（inline编辑）
const handleFieldUpdate = (row: any) => {
  // 更新匹配状态
  row.mapping_status = calculateMappingStatus(row)
  // 更新甲方更新时间（如果字段有映射关系）
  if (row.tenant_field_key || row.tenant_field_id) {
    row.tenant_updated_at = new Date().toISOString()
  }
  // 如果当前有筛选，重新筛选
  filterFields()
  ElMessage.success('已更新')
  // TODO: 调用API保存
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

// 删除字段
const handleDelete = (row: any) => {
  if (row.field_source === 'standard') {
    ElMessage.warning('标准字段不能删除')
    return
  }

  ElMessageBox.confirm('确定要删除该自定义字段吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadFields(currentGroupId.value)
    // TODO: 调用API删除
  }).catch(() => {})
}

// 配置队列可见性
const handleQueueConfig = (row: any) => {
  currentField.value = row
  selectedHiddenQueues.value = row.hidden_queues || []
  queueDialogVisible.value = true
}

// 保存队列配置
const handleSaveQueueConfig = () => {
  if (currentField.value) {
    currentField.value.hidden_queues = selectedHiddenQueues.value
    ElMessage.success('队列配置已保存')
    queueDialogVisible.value = false
    // TODO: 调用API保存
  }
}

// Tab切换处理
const handleTabChange = (tabName: string) => {
  console.log('切换到tab:', tabName)
  if (tabName === 'extended') {
    // 如果还没有选中分组，自动选中第一个分组
    if (!currentExtendedGroupId.value && treeData.value.length > 0) {
      currentExtendedGroupId.value = treeData.value[0].id
    }
    loadExtendedFields(currentExtendedGroupId.value)
  } else if (tabName === 'unmapped') {
    loadUnmappedTenantFields()
  }
}

// 点击扩展字段分组
const handleExtendedGroupClick = (data: any) => {
  currentExtendedGroupId.value = data.id
  loadExtendedFields(data.id)
}

// 加载扩展字段
const loadExtendedFields = async (groupId?: number) => {
  if (!currentTenantId.value) return
  
  try {
    const params = groupId ? { field_group_id: groupId } : {}
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/extended-fields`,
      method: 'get',
      params,
    })
    // API直接返回数组，不是{data: [...]}格式
    extendedFields.value = Array.isArray(response) ? response : (response.data || [])
    // 根据分组筛选
    filterExtendedFields()
  } catch (error: any) {
    console.log('加载扩展字段失败，使用Mock数据:', error.message)
    // API失败时使用Mock数据（静默降级）
    extendedFields.value = [
      {
        id: 1,
        field_alias: 'company_name',
        tenant_field_key: 'COMP_NAME',
        tenant_field_name: '公司名称',
        field_type: 'String',
        field_group_id: groupId || null,
        privacy_label: 'PII',
        is_required: false
      }
    ]
    filterExtendedFields()
  }
}

// 筛选扩展字段
const filterExtendedFields = () => {
  if (!currentExtendedGroupId.value) {
    filteredExtendedFields.value = extendedFields.value
  } else {
    filteredExtendedFields.value = extendedFields.value.filter(field => 
      field.field_group_id === currentExtendedGroupId.value
    )
  }
}

// 加载未使用的甲方字段
const loadUnmappedTenantFields = async () => {
  if (!currentTenantId.value) return
  
  try {
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/unmapped-fields`,
      method: 'get',
    })
    // API直接返回数组，不是{data: [...]}格式
    unmappedTenantFields.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error: any) {
    console.log('加载未映射字段失败，使用Mock数据:', error.message)
    // API失败时使用Mock数据（静默降级）
    unmappedTenantFields.value = [
      {
        tenant_field_key: 'EXTRA_FIELD_1',
        tenant_field_name: '额外字段1',
        field_type: 'String',
        is_required: false,
        tenant_updated_at: '2024-01-15T10:30:00Z'
      },
      {
        tenant_field_key: 'EXTRA_FIELD_2',
        tenant_field_name: '额外字段2',
        field_type: 'Integer',
        is_required: true,
        tenant_updated_at: '2024-01-16T14:20:00Z'
      }
    ]
  }
}

// 一键建议映射
const handleAutoSuggestMapping = async () => {
  ElMessageBox.confirm(
    '系统将基于字段名称和同义词的相似度，自动为未匹配的甲方字段建议映射关系。是否继续？',
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(async () => {
    try {
      const response = await request({
        url: `/api/v1/tenants/${currentTenantId.value}/fields/auto-suggest-mapping`,
        method: 'post',
      })
      ElMessage.success(`成功建议 ${response.data.count || 0} 个字段的映射关系`)
      loadFields(currentGroupId.value)
      loadUnmappedTenantFields()
    } catch (error) {
      console.error('一键建议映射失败:', error)
      ElMessage.error('一键建议映射失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 添加扩展字段
const handleAddExtendedField = () => {
  extendedDialogTitle.value = '添加扩展字段'
  extendedForm.value = {
    field_alias: '',
    tenant_field_key: '',
    tenant_field_name: '',
    field_type: 'String',
    field_group_path: currentExtendedGroupId.value || 0,
    privacy_label: '公开',
    is_required: false
  }
  extendedDialogVisible.value = true
}

// 编辑扩展字段
const handleEditExtended = (row: any) => {
  extendedDialogTitle.value = '编辑扩展字段'
  extendedForm.value = { 
    ...row,
    field_group_path: row.field_group_id || row.field_group_path || 0 // 编辑时将 field_group_id 赋值给 field_group_path
  }
  extendedDialogVisible.value = true
}

// 保存扩展字段
const handleSaveExtended = async () => {
  if (!extendedForm.value.field_alias) {
    ElMessage.warning('请填写扩展字段别名')
    return
  }
  
  try {
    // 准备提交数据：将 field_group_path 转换为 field_group_id
    const submitData = {
      ...extendedForm.value,
      field_group_id: extendedForm.value.field_group_path
    }
    delete submitData.field_group_path
    
    const url = extendedForm.value.id
      ? `/api/v1/tenants/${currentTenantId.value}/extended-fields/${extendedForm.value.id}`
      : `/api/v1/tenants/${currentTenantId.value}/extended-fields`
    
    await request({
      url,
      method: extendedForm.value.id ? 'put' : 'post',
      data: submitData,
    })
    
    ElMessage.success('保存成功')
    extendedDialogVisible.value = false
    loadExtendedFields(currentExtendedGroupId.value)
    // 更新未映射字段列表
    loadUnmappedTenantFields()
  } catch (error) {
    console.error('保存扩展字段失败:', error)
    ElMessage.error('保存失败')
  }
}

// 扩展字段更新（inline编辑）
const handleExtendedFieldUpdate = (row: any) => {
  ElMessage.success('已更新')
  // TODO: 调用API保存
}

// 删除扩展字段
const handleDeleteExtended = async (row: any) => {
  ElMessageBox.confirm('确定删除该扩展字段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request({
        url: `/api/v1/tenants/${currentTenantId.value}/extended-fields/${row.id}`,
        method: 'delete',
      })
      ElMessage.success('删除成功')
      loadExtendedFields()
    } catch (error) {
      console.error('删除扩展字段失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 匹配到目标字段
const handleMatchToTarget = (row: any) => {
  matchForm.value = {
    tenant_field_key: row.tenant_field_key,
    tenant_field_name: row.tenant_field_name,
    field_type: row.field_type,
    target_field_id: null
  }
  matchDialogVisible.value = true
}

// 确认匹配
const handleConfirmMatch = async () => {
  if (!matchForm.value.target_field_id) {
    ElMessage.warning('请选择目标字段')
    return
  }
  
  try {
    await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields/match`,
      method: 'post',
      data: {
        tenant_field_key: matchForm.value.tenant_field_key,
        target_field_id: matchForm.value.target_field_id
      },
    })
    
    ElMessage.success('匹配成功')
    matchDialogVisible.value = false
    loadFields(currentGroupId.value)
    loadUnmappedTenantFields()
  } catch (error) {
    console.error('匹配失败:', error)
    ElMessage.error('匹配失败')
  }
}

// 设为扩展字段
const handleSetAsExtended = (row: any) => {
  extendedDialogTitle.value = '设为扩展字段'
  extendedForm.value = {
    field_alias: '',
    tenant_field_key: row.tenant_field_key,
    tenant_field_name: row.tenant_field_name,
    field_type: row.field_type,
    field_group_path: currentExtendedGroupId.value || 0,
    privacy_label: '公开',
    is_required: false
  }
  extendedDialogVisible.value = true
}

onMounted(() => {
  if (currentTenantId.value) {
    loadGroups().then(() => {
      // 如果当前在扩展字段tab，初始化分组选择
      if (activeTab.value === 'extended' && treeData.value.length > 0) {
        currentExtendedGroupId.value = treeData.value[0].id
        loadExtendedFields(currentExtendedGroupId.value)
      }
    })
    // 初始加载未映射字段（用于显示警告）
    loadUnmappedTenantFields()
  }
})

onBeforeUnmount(() => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header {
  margin-bottom: 15px;
}

.filter-bar {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

/* 目标字段单元格样式 */
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
}

.field-key {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.field-group-tree {
  cursor: pointer;
  user-select: none;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 40px;
  cursor: pointer;
  padding: 0 10px;
  transition: all 0.3s;
}

.field-group-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.field-group-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.field-group-tree :deep(.el-tree-node__label) {
  cursor: pointer;
  flex: 1;
}

/* 拖拽排序样式 */
.sortable-table :deep(.el-table__row) {
  cursor: move;
  transition: background-color 0.3s;
}

.sortable-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.drag-handle {
  color: #909399;
  font-size: 18px;
  transition: color 0.3s;
}

.drag-handle:hover {
  color: #409eff;
}

.sortable-ghost {
  opacity: 0.5;
  background: #ecf5ff !important;
}

.sortable-table :deep(.sortable-chosen) {
  background-color: #f0f9ff !important;
}

.sortable-table :deep(tr.sortable-chosen td) {
  background-color: #f0f9ff !important;
}

/* 枚举配置样式 */
.enum-config {
  width: 100%;
}

.enum-header {
  margin-bottom: 10px;
}

.enum-config :deep(.el-table) {
  font-size: 13px;
}

.enum-config :deep(.el-input__inner) {
  font-size: 12px;
}
</style>
