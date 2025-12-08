<template>
  <div class="field-mapping-config">
    <div class="page-header">
      <div>
        <h2>案件列表字段映射配置</h2>
        <div class="mapping-status">
          <el-tag :type="mappingStatusType" size="large">
            {{ mappingStatusText }}
          </el-tag>
          <span class="status-text">
            已映射 {{ mappedCount }}/{{ totalStandardFields }} 个标准字段
          </span>
          <span v-if="hasUnsavedChanges" class="unsaved-indicator">
            <el-icon><Warning /></el-icon>
            有未保存的修改
          </span>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="handleManageVersion">
          <el-icon><FolderOpened /></el-icon>
          版本管理
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSaveConfig"
          :disabled="!canSaveConfig"
          :loading="isSaving"
        >
          <el-icon><Select /></el-icon>
          {{ currentConfigVersion ? '保存新版本' : '保存' }}
        </el-button>
      </div>
    </div>

    <!-- 映射配置版本信息 -->
    <el-card v-if="currentConfigVersion" class="config-version-card" v-loading="isLoading">
      <template #header>
        <div class="card-header">
          <span>当前生效的映射配置版本</span>
          <el-tag type="success" size="small">已生效</el-tag>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="配置版本">v{{ currentConfigVersion.version }}</el-descriptions-item>
        <el-descriptions-item label="保存时间">{{ formatDateTime(currentConfigVersion.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="映射完成度">{{ currentConfigVersion.mapped_count }}/{{ currentConfigVersion.total_count }}</el-descriptions-item>
        <el-descriptions-item label="保存人">{{ currentConfigVersion.created_by_name || '管理员' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 甲方字段版本信息 -->
    <el-card v-if="versionInfo" class="version-info-card" v-loading="isLoading">
      <template #header>
        <div class="card-header">
          <span>甲方字段数据版本</span>
          <el-tag size="small">数据源</el-tag>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="字段版本">v{{ versionInfo.version || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ versionInfo.upload_time || '-' }}</el-descriptions-item>
        <el-descriptions-item label="字段数量">{{ versionInfo.field_count || 0 }}</el-descriptions-item>
        <el-descriptions-item label="上传人">{{ versionInfo.uploader || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 无版本提示 -->
    <el-alert
      v-if="!versionInfo && !isLoading"
      title="⚠️ 尚未上传甲方字段"
      type="warning"
      show-icon
      :closable="false"
      class="version-info-card"
    >
      <template #default>
        <p>请先上传甲方字段JSON文件，才能进行字段映射配置。</p>
      </template>
    </el-alert>

    <!-- 未使用字段警告 -->
    <el-alert
      v-if="unmappedCount > 0"
      :title="`⚠️ 发现 ${unmappedCount} 个未使用的甲方字段`"
      type="warning"
      show-icon
      :closable="false"
      class="unmapped-alert"
    >
      <template #default>
        <p>这些字段来自甲方上传的JSON，但尚未映射到标准字段或设为拓展字段。</p>
        <el-button type="warning" size="small">立即处理</el-button>
      </template>
    </el-alert>

    <!-- 主要内容标签页 -->
    <el-tabs v-model="activeTab" class="config-tabs">
      <!-- Tab 1: 匹配目标字段 -->
      <el-tab-pane label="匹配目标字段" name="matched">
        <div class="tab-content">
          <div class="tab-actions">
            <el-button type="primary" @click="handleAutoMatch">
              <el-icon><MagicStick /></el-icon>
              智能匹配建议
            </el-button>
          </div>

          <el-table :data="mappedFields" stripe border v-loading="isLoading">
            <el-table-column type="index" label="序号" width="60" />
            
            <!-- 标准字段信息 -->
            <el-table-column prop="field_name" label="标准字段名称" width="140" show-overflow-tooltip />
            <el-table-column prop="field_key" label="标准字段Key" width="160" show-overflow-tooltip>
              <template #default="scope">
                <code class="field-key-text">{{ scope.row.field_key }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="field_type" label="标准字段类型" width="120" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.field_type" size="small" type="info">
                  {{ scope.row.field_type }}
                </el-tag>
              </template>
            </el-table-column>
            
            <!-- 映射关系图标 -->
            <el-table-column label="映射" width="70" align="center">
              <template #default="scope">
                <el-icon v-if="scope.row.tenant_field_key" color="#67c23a" :size="20">
                  <Right />
                </el-icon>
                <el-icon v-else color="#909399" :size="20">
                  <Close />
                </el-icon>
              </template>
            </el-table-column>
            
            <!-- 甲方字段信息 -->
            <el-table-column label="甲方字段Key" width="160" show-overflow-tooltip>
              <template #default="scope">
                <code v-if="scope.row.tenant_field_key" class="field-key-text">
                  {{ scope.row.tenant_field_key }}
                </code>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="甲方字段名称" width="140" show-overflow-tooltip>
              <template #default="scope">
                <span v-if="scope.row.tenant_field_name">{{ scope.row.tenant_field_name }}</span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="甲方字段类型" width="120" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.tenant_field_type" size="small" type="success">
                  {{ scope.row.tenant_field_type }}
                </el-tag>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            
            <!-- 枚举值（只有甲方字段是枚举类型时才显示） -->
            <el-table-column label="枚举值" width="200">
              <template #default="scope">
                <span v-if="scope.row.tenant_field_type === 'Enum' && scope.row.tenant_enum_values && scope.row.tenant_enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in scope.row.tenant_enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ typeof item === 'string' ? item : item.value || item.label || item.name }}
                  </el-tag>
                  <el-tag v-if="scope.row.tenant_enum_values.length > 2" size="small" type="info">
                    等{{ scope.row.tenant_enum_values.length }}个
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            
            <!-- 映射状态 -->
            <el-table-column label="映射状态" width="120">
              <template #default="scope">
                <el-tag v-if="scope.row.mapping_status === 'auto_mapped'" type="success" effect="dark" size="small">
                  <el-icon><MagicStick /></el-icon>
                  自动匹配
                </el-tag>
                <el-tag v-else-if="scope.row.mapping_status === 'manual_mapped'" type="primary" size="small">
                  <el-icon><Edit /></el-icon>
                  手动映射
                </el-tag>
                <el-tag v-else type="info" effect="plain" size="small">未映射</el-tag>
              </template>
            </el-table-column>
            
            <!-- 操作按钮 -->
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditMapping(scope.row)">
                  {{ scope.row.tenant_field_key ? '重新映射' : '选择映射' }}
                </el-button>
                <el-button v-if="scope.row.tenant_field_key" type="danger" size="small" @click="handleClearMapping(scope.row)">清除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空数据提示 -->
          <el-empty v-if="mappedFields.length === 0 && !isLoading" description="暂无标准字段数据">
            <el-button type="primary" @click="loadAllData">刷新数据</el-button>
          </el-empty>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 拓展字段 -->
      <el-tab-pane label="拓展字段" name="extended">
        <div class="tab-content">
          <div class="tab-actions">
            <el-button type="primary" @click="handleAddExtendedField">
              <el-icon><Plus /></el-icon>
              添加拓展字段
            </el-button>
          </div>

          <el-table :data="extendedFields" stripe border v-loading="isLoading">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="field_alias" label="字段别名" width="150" />
            <el-table-column prop="tenant_field_key" label="甲方字段Key" width="180" />
            <el-table-column prop="tenant_field_name" label="甲方字段名称" width="150" />
            <el-table-column prop="field_type" label="字段类型" width="100" />
            <el-table-column label="枚举值" width="180">
              <template #default="scope">
                <span v-if="scope.row.field_type === 'Enum' && scope.row.enum_values && scope.row.enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in scope.row.enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ typeof item === 'string' ? item : item.value || item.label || item.name }}
                  </el-tag>
                  <el-tag v-if="scope.row.enum_values.length > 2" size="small" type="info">
                    等{{ scope.row.enum_values.length }}个
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditExtendedField(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDeleteExtendedField(scope.row)">删除</el-button>
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
          >
            这些字段来自甲方上传的JSON，但尚未映射到标准字段或设为拓展字段。建议尽快处理，避免数据丢失。
          </el-alert>

          <el-table :data="unmappedFields" stripe border style="margin-top: 16px" v-loading="isLoading">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="field_key" label="字段Key" width="180" />
            <el-table-column prop="field_name" label="字段名称" width="150" />
            <el-table-column prop="field_type" label="字段类型" width="100" />
            <el-table-column label="枚举值" width="180">
              <template #default="scope">
                <span v-if="scope.row.field_type === 'Enum' && scope.row.enum_values && scope.row.enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in scope.row.enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ typeof item === 'string' ? item : item.value || item.label || item.name }}
                  </el-tag>
                  <el-tag v-if="scope.row.enum_values.length > 2" size="small" type="info">
                    等{{ scope.row.enum_values.length }}个
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleMatchToTarget(scope.row)">匹配到目标字段</el-button>
                <el-button type="success" size="small" @click="handleSetAsExtended(scope.row)">设为拓展字段</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 甲方字段版本管理抽屉 -->
    <VersionManagerDrawerSimple
      v-model="versionManagerVisible"
      :tenant-id="currentTenantId"
      :current-version="versionInfo?.version"
      @version-changed="loadAllData"
    />

    <!-- 映射配置版本管理对话框 -->
    <ConfigVersionManagerDialog
      v-model="configVersionDialogVisible"
      :tenant-id="currentTenantId"
      :current-version="currentConfigVersion"
      @restore="handleRestoreVersion"
    />

    <!-- 自动匹配建议对话框 -->
    <AutoMatchSuggestDialogSimple
      ref="autoMatchDialogRef"
      v-model="autoMatchDialogVisible"
      :tenant-id="currentTenantId"
      @confirm="loadAllData"
    />

    <!-- 拓展字段对话框 -->
    <ExtendedFieldDialogSimple
      v-model="extendedFieldDialogVisible"
      :tenant-id="currentTenantId"
      :edit-data="currentExtendedField"
      @confirm="loadAllData"
    />

    <!-- 匹配到目标字段对话框 -->
    <MatchToTargetDialogSimple
      v-model="matchDialogVisible"
      :tenant-id="currentTenantId"
      :unmapped-field="currentUnmappedField"
      :standard-fields="standardFields"
      @confirm="loadAllData"
    />

    <!-- 编辑映射对话框 -->
    <EditMappingDialog
      v-model="editMappingDialogVisible"
      :tenant-id="currentTenantId"
      :standard-field="currentStandardField"
      :current-mapping="currentStandardField"
      @confirm="loadAllData"
      @enum-mapping-needed="handleEnumMappingNeeded"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  MagicStick, 
  Plus, 
  Right, 
  Close, 
  Edit, 
  Select, 
  FolderOpened, 
  Warning 
} from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { getCaseListStandardFields } from '@/api/field'
import {
  getTenantFieldsJson,
  getFieldConfigs,
  getExtendedFields,
  getUnmappedFields
} from '@/api/field-mapping'

// 导入组件
import VersionManagerDrawerSimple from './components/VersionManagerDrawerSimple.vue'
import ConfigVersionManagerDialog from './components/ConfigVersionManagerDialog.vue'
import AutoMatchSuggestDialogSimple from './components/AutoMatchSuggestDialogSimple.vue'
import ExtendedFieldDialogSimple from './components/ExtendedFieldDialogSimple.vue'
import MatchToTargetDialogSimple from './components/MatchToTargetDialogSimple.vue'
import EditMappingDialog from './components/EditMappingDialog.vue'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 状态管理
const isLoading = ref(false)
const isSaving = ref(false)
const activeTab = ref('matched')

// 数据状态
const versionInfo = ref<any>(null)
const standardFields = ref<any[]>([])
const mappedFields = ref<any[]>([])
const extendedFields = ref<any[]>([])
const unmappedFields = ref<any[]>([])

// 配置版本状态
const currentConfigVersion = ref<any>(null)
const hasUnsavedChanges = ref(false)

// 统计信息
const unmappedCount = computed(() => unmappedFields.value.length)
const totalStandardFields = computed(() => standardFields.value.length)
const mappedCount = computed(() => {
  return mappedFields.value.filter(f => f.tenant_field_key).length
})

// 映射状态
const mappingStatusType = computed(() => {
  if (mappedCount.value === 0) return 'info'
  if (mappedCount.value === totalStandardFields.value) return 'success'
  return 'warning'
})

const mappingStatusText = computed(() => {
  if (mappedCount.value === 0) return '未开始映射'
  if (mappedCount.value === totalStandardFields.value) return '✓ 映射完成'
  return '映射进行中'
})

// 是否可以保存
const canSaveConfig = computed(() => {
  // 必须所有标准字段都已映射
  return mappedCount.value === totalStandardFields.value && 
         totalStandardFields.value > 0 &&
         !isSaving.value
})

// 加载所有数据
const loadAllData = async () => {
  console.log('[字段映射] 开始加载数据, tenantId:', currentTenantId.value)
  
  if (!currentTenantId.value) {
    console.warn('[字段映射] 未选择甲方，跳过数据加载')
    // 重置所有数据
    versionInfo.value = null
    standardFields.value = []
    mappedFields.value = []
    extendedFields.value = []
    unmappedFields.value = []
    return
  }

  isLoading.value = true
  try {
    console.log('[字段映射] 并行加载5个API...')
    
  // 工具方法：将不同结构的响应统一转换为数组
  const toList = (res: any) => {
    if (!res) return []
    if (Array.isArray(res)) return res
    if (Array.isArray(res?.data)) return res.data
    if (Array.isArray(res?.data?.list)) return res.data.list
    if (Array.isArray(res?.list)) return res.list
    return []
  }

  // 并行加载所有数据
    const [versionRes, standardRes, configsRes, extendedRes, unmappedRes] = await Promise.all([
      getTenantFieldsJson(currentTenantId.value).catch(err => {
        console.error('[字段映射] 获取甲方字段版本失败:', err)
        return null
      }),
      getCaseListStandardFields().catch(err => {
        console.error('[字段映射] 获取案件列表标准字段失败:', err)
        return { data: [] }
      }),
      getFieldConfigs(currentTenantId.value).catch(err => {
        console.error('[字段映射] 获取字段配置失败:', err)
        return { data: [] }
      }),
      getExtendedFields(currentTenantId.value).catch(err => {
        console.error('[字段映射] 获取拓展字段失败:', err)
        return { data: [] }
      }),
      getUnmappedFields(currentTenantId.value).catch(err => {
        console.error('[字段映射] 获取未使用字段失败:', err)
        return { data: [] }
      })
    ])

    console.log('[字段映射] API返回结果:', {
      versionRes,
      standardRes,
      configsRes,
      extendedRes,
      unmappedRes
    })

    // 更新版本信息（兼容多种返回结构）
    const vr = versionRes?.data ?? versionRes ?? null
    if (vr && (vr.version || vr.scene || vr.fields || vr.field_count || vr.upload_time)) {
      versionInfo.value = vr
      console.log('[字段映射] 版本信息:', versionInfo.value)
    } else {
      versionInfo.value = null
      console.warn('[字段映射] 未获取到版本信息')
    }

    // 获取甲方字段列表（从版本信息中）
    const tenantFieldsList = versionInfo.value?.fields || []
    console.log('[字段映射] 甲方字段列表数量:', tenantFieldsList.length)

    // 更新标准字段
    standardFields.value = toList(standardRes)
    console.log('[字段映射] 标准字段数量:', standardFields.value.length)
    if (standardFields.value.length === 0) {
      console.warn('[字段映射] 未获取到标准字段')
    }

    // 合并标准字段和映射配置
    const configs = toList(configsRes)
    console.log('[字段映射] 字段配置数量:', configs.length)
    
    mappedFields.value = standardFields.value.map(field => {
      const config = configs.find(c => c.field_key === field.field_key)
      
      // 如果有映射配置，从甲方字段列表中查找对应字段的详细信息
      let tenantFieldInfo = null
      if (config?.tenant_field_key && tenantFieldsList.length > 0) {
        tenantFieldInfo = tenantFieldsList.find(
          tf => tf.field_key === config.tenant_field_key
        )
      }
      
      return {
        ...field,
        tenant_field_key: config?.tenant_field_key || null,
        tenant_field_name: config?.tenant_field_name || tenantFieldInfo?.field_name || null,
        tenant_field_type: tenantFieldInfo?.field_type || null,
        tenant_enum_values: tenantFieldInfo?.enum_values || null,
        mapping_status: config?.mapping_status || 'unmapped'
      }
    })
    console.log('[字段映射] 映射字段数量:', mappedFields.value.length)

    // 更新拓展字段
    extendedFields.value = toList(extendedRes)
    console.log('[字段映射] 拓展字段数量:', extendedFields.value.length)

    // 更新未使用字段
    unmappedFields.value = toList(unmappedRes)
    console.log('[字段映射] 未使用字段数量:', unmappedFields.value.length)

    console.log('[字段映射] 数据加载完成✅:', {
      version: versionInfo.value,
      standardFields: standardFields.value.length,
      mappedFields: mappedFields.value.length,
      extendedFields: extendedFields.value.length,
      unmappedFields: unmappedFields.value.length
    })
  } catch (error) {
    console.error('[字段映射] 加载数据失败❌:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    isLoading.value = false
  }
}

// 保存配置
const handleSaveConfig = async () => {
  if (!canSaveConfig.value) {
    ElMessage.warning('请先完成所有标准字段的映射')
    return
  }

  // 确认保存
  try {
    await ElMessageBox.confirm(
      `即将保存当前映射配置，共映射 ${mappedCount.value} 个标准字段。保存后将生成新版本并在线上生效。`,
      '确认保存',
      {
        confirmButtonText: '确认保存',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
  } catch {
    return // 用户取消
  }

  isSaving.value = true
  try {
    // 准备保存数据
    const saveData = {
      tenant_id: currentTenantId.value,
      scene: 'case_list',
      field_mappings: mappedFields.value
        .filter(f => f.tenant_field_key)
        .map(f => ({
          standard_field_key: f.field_key,
          standard_field_name: f.field_name,
          tenant_field_key: f.tenant_field_key,
          tenant_field_name: f.tenant_field_name,
          tenant_field_type: f.tenant_field_type,
          mapping_status: f.mapping_status || 'manual_mapped'
        })),
      extended_fields: extendedFields.value.map(f => ({
        field_alias: f.field_alias,
        tenant_field_key: f.tenant_field_key,
        tenant_field_name: f.tenant_field_name,
        field_type: f.field_type,
        enum_values: f.enum_values
      })),
      total_count: totalStandardFields.value,
      mapped_count: mappedCount.value
    }

    console.log('[字段映射] 保存配置数据:', saveData)

    // 调用保存API（这里先用Mock）
    // const result = await saveFieldMappingConfig(saveData)
    
    // Mock保存成功
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新当前配置版本
    const newVersion = (currentConfigVersion.value?.version || 0) + 1
    currentConfigVersion.value = {
      version: newVersion,
      created_at: new Date().toISOString(),
      created_by_name: '当前用户',
      mapped_count: mappedCount.value,
      total_count: totalStandardFields.value,
      is_active: true
    }

    hasUnsavedChanges.value = false
    
    ElMessage.success({
      message: `配置保存成功！已生成版本 v${newVersion}`,
      duration: 3000
    })

    console.log('[字段映射] 配置保存成功，版本:', newVersion)
  } catch (error) {
    console.error('[字段映射] 保存配置失败:', error)
    ElMessage.error('保存配置失败，请重试')
  } finally {
    isSaving.value = false
  }
}

// 甲方字段版本管理
const versionManagerVisible = ref(false)

// 映射配置版本管理
const configVersionDialogVisible = ref(false)
const handleManageVersion = () => {
  configVersionDialogVisible.value = true
}

// 恢复历史版本
const handleRestoreVersion = async (version: any) => {
  try {
    // Mock恢复操作
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新当前配置版本
    currentConfigVersion.value = {
      ...version,
      is_active: true
    }
    
    // 重新加载数据
    await loadAllData()
    
    ElMessage.success(`已恢复到版本 v${version.version}`)
  } catch (error) {
    console.error('恢复版本失败:', error)
    ElMessage.error('恢复版本失败')
  }
}

// 智能匹配建议
const autoMatchDialogRef = ref()
const autoMatchDialogVisible = ref(false)
const handleAutoMatch = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }
  autoMatchDialogVisible.value = true
  // 打开对话框后加载建议
  setTimeout(() => {
    autoMatchDialogRef.value?.loadSuggestions()
  }, 100)
}

// 添加映射
// 编辑映射
const editMappingDialogVisible = ref(false)
const currentStandardField = ref<any>(null)

const handleEditMapping = (row: any) => {
  currentStandardField.value = row
  editMappingDialogVisible.value = true
}

// 清除映射
const handleClearMapping = async (row: any) => {
  if (!currentTenantId.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清除字段「${row.field_name}」的映射吗？`,
      '提示',
      {
        type: 'warning'
      }
    )

    // TODO: 调用清除映射API
    ElMessage.success('清除成功')
    await loadAllData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清除失败:', error)
      ElMessage.error('清除失败')
    }
  }
}

// 枚举映射需要
const handleEnumMappingNeeded = (data: any) => {
  ElMessage.info('枚举类型字段映射成功，请继续配置枚举值映射')
  // TODO: 打开枚举值映射对话框
}

// 添加拓展字段
const extendedFieldDialogVisible = ref(false)
const currentExtendedField = ref<any>(null)

const handleAddExtendedField = () => {
  currentExtendedField.value = null
  extendedFieldDialogVisible.value = true
}

// 编辑拓展字段
const handleEditExtendedField = (row: any) => {
  currentExtendedField.value = row
  extendedFieldDialogVisible.value = true
}

// 删除拓展字段
const handleDeleteExtendedField = async (row: any) => {
  if (!currentTenantId.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除拓展字段「${row.field_alias}」吗？`,
      '提示',
      {
        type: 'warning'
      }
    )

    // TODO: 调用删除API
    ElMessage.success('删除成功')
    await loadAllData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 匹配到目标字段
const matchDialogVisible = ref(false)
const currentUnmappedField = ref<any>(null)

const handleMatchToTarget = (row: any) => {
  currentUnmappedField.value = row
  matchDialogVisible.value = true
}

// 设为拓展字段
const handleSetAsExtended = (row: any) => {
  // 设置甲方字段信息后打开拓展字段对话框
  currentExtendedField.value = {
    tenant_field_key: row.field_key,
    tenant_field_name: row.field_name,
    field_type: row.field_type
  }
  extendedFieldDialogVisible.value = true
}

// 工具函数：格式化日期时间
const formatDateTime = (dateStr: string | null | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch {
    return dateStr
  }
}

// 监听字段变化，标记为未保存
watch([mappedFields, extendedFields], () => {
  if (currentConfigVersion.value) {
    hasUnsavedChanges.value = true
  }
}, { deep: true })

// 监听甲方ID变化
watch(currentTenantId, (newId, oldId) => {
  console.log('[字段映射] 甲方ID变化:', { oldId, newId })
  if (newId) {
    loadAllData()
  }
}, { immediate: false })

// 页面加载时获取数据
onMounted(() => {
  console.log('[字段映射] 页面挂载, 当前甲方ID:', currentTenantId.value)
  // 如果已经有甲方ID，立即加载
  if (currentTenantId.value) {
    loadAllData()
  }
})
</script>

<style scoped>
.field-mapping-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.version-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unmapped-alert {
  margin-bottom: 20px;
}

.config-tabs {
  background: white;
  padding: 20px;
  border-radius: 4px;
}

.tab-content {
  padding: 16px 0;
}

.tab-actions {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.field-key-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
}

code.field-key-text {
  font-size: 12px;
  color: #606266;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
}

.tenant-field-info {
  padding: 4px 0;
}

.tenant-field-info strong {
  color: #303133;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table .el-table__header th) {
  background-color: #f5f7fa;
  color: #303133;
  font-weight: 600;
}

/* 映射状态样式 */
.mapping-status {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.status-text {
  color: #606266;
  font-size: 14px;
}

.unsaved-indicator {
  color: #e6a23c;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.config-version-card {
  margin-bottom: 16px;
}

.config-version-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
