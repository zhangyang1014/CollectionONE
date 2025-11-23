<template>
  <div class="permission-matrix">
    <!-- 工具栏 -->
    <div class="matrix-toolbar">
      <el-space wrap>
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
        <el-button type="info" plain @click="handleBatchSet">
          <el-icon><Operation /></el-icon>
          批量设置
        </el-button>
      </el-space>
      
      <div class="legend">
        <span class="legend-title">权限说明：</span>
        <el-tag type="info" size="small">❌ 不可见</el-tag>
        <el-tag type="primary" size="small">👁️ 仅可见</el-tag>
        <el-tag type="success" size="small">✏️ 可编辑</el-tag>
        <el-text type="info" size="small">（点击单元格切换权限）</el-text>
      </div>
    </div>

    <!-- 权限矩阵表格 -->
    <div class="matrix-container">
      <el-tabs v-model="activeModule" type="card">
        <el-tab-pane
          v-for="module in modules"
          :key="module.module_key"
          :label="`${module.module_name} (${getModuleItemCount(module.id)})`"
          :name="module.module_key"
        >
          <el-table
            :data="getModuleItems(module.id)"
            border
            stripe
            :max-height="600"
            style="width: 100%"
          >
            <!-- 权限项列 -->
            <el-table-column
              prop="item_name"
              label="权限项"
              width="250"
              fixed="left"
            >
              <template #default="{ row }">
                <div class="permission-item-cell">
                  <span class="item-name">{{ row.item_name }}</span>
                  <el-tooltip 
                    v-if="row.description" 
                    placement="top"
                    popper-class="permission-tooltip"
                  >
                    <template #content>
                      <div style="white-space: pre-line;">{{ row.description }}</div>
                    </template>
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>

            <!-- 角色列 - 动态生成 -->
            <el-table-column
              v-for="role in displayRoles"
              :key="role.code"
              :label="role.name"
              width="120"
              align="center"
            >
              <template #default="{ row }">
                <div
                  class="permission-cell"
                  :class="getCellClass(row.id, role.code)"
                  @click="handleCellClick(row.id, role.code)"
                >
                  <el-tag
                    :type="getPermissionTagType(row.id, role.code)"
                    effect="plain"
                    size="small"
                    class="permission-tag"
                  >
                    {{ getPermissionIcon(row.id, role.code) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 批量设置对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量设置权限" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="目标角色">
          <el-select v-model="batchForm.targetRole" placeholder="选择角色">
            <el-option
              v-for="role in displayRoles"
              :key="role.code"
              :label="role.name"
              :value="role.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权限级别">
          <el-radio-group v-model="batchForm.permissionLevel">
            <el-radio value="none">❌ 不可见</el-radio>
            <el-radio value="readonly">👁️ 仅可见</el-radio>
            <el-radio value="editable">✏️ 可编辑</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应用范围">
          <el-radio-group v-model="batchForm.scope">
            <el-radio value="current">当前模块</el-radio>
            <el-radio value="all">所有模块</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchApply">应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Operation, InfoFilled } from '@element-plus/icons-vue'
import type {
  PermissionModule,
  PermissionItem,
  RolePermissionConfig,
  PermissionLevel,
  RoleInfo
} from '@/types/permission'
import {
  getNextPermissionLevel,
  PERMISSION_LEVEL_DISPLAY,
  PermissionLevel as PermissionLevelEnum
} from '@/types/permission'

// ==================== Props ====================
const props = defineProps<{
  modules: PermissionModule[]
  items: PermissionItem[]
  configs: RolePermissionConfig[]
  displayRoles: RoleInfo[]
  loading?: boolean
}>()

// ==================== Emits ====================
const emit = defineEmits<{
  save: [configs: RolePermissionConfig[]]
  reset: []
}>()

// ==================== State ====================
const activeModule = ref('')
const saving = ref(false)
const batchDialogVisible = ref(false)
const batchForm = ref({
  targetRole: '',
  permissionLevel: 'none' as PermissionLevel,
  scope: 'current'
})

// 本地配置副本（用于编辑）
const localConfigs = ref<Map<string, PermissionLevel>>(new Map())

// ==================== Computed ====================

// 初始化时设置默认激活模块
watch(() => props.modules, (newModules) => {
  if (newModules.length > 0 && !activeModule.value) {
    activeModule.value = newModules[0].module_key
  }
}, { immediate: true })

// 初始化本地配置
watch(() => props.configs, (newConfigs) => {
  const map = new Map<string, PermissionLevel>()
  newConfigs.forEach(config => {
    const key = `${config.role_code}_${config.permission_item_id}`
    map.set(key, config.permission_level as PermissionLevel)
  })
  localConfigs.value = map
}, { immediate: true })

// 获取模块的权限项数量
const getModuleItemCount = (moduleId: number) => {
  return props.items.filter(item => item.module_id === moduleId).length
}

// 获取模块的权限项列表
const getModuleItems = (moduleId: number) => {
  return props.items.filter(item => item.module_id === moduleId)
}

// ==================== Methods ====================

// 获取权限级别
const getPermissionLevel = (itemId: number, roleCode: string): PermissionLevel => {
  const key = `${roleCode}_${itemId}`
  return localConfigs.value.get(key) || PermissionLevelEnum.NONE
}

// 获取单元格样式类
const getCellClass = (itemId: number, roleCode: string) => {
  const level = getPermissionLevel(itemId, roleCode)
  return `permission-${level}`
}

// 获取权限标签类型
const getPermissionTagType = (itemId: number, roleCode: string) => {
  const level = getPermissionLevel(itemId, roleCode)
  return PERMISSION_LEVEL_DISPLAY[level].tagType
}

// 获取权限图标
const getPermissionIcon = (itemId: number, roleCode: string) => {
  const level = getPermissionLevel(itemId, roleCode)
  return PERMISSION_LEVEL_DISPLAY[level].icon
}

// 单元格点击 - 循环切换权限级别
const handleCellClick = (itemId: number, roleCode: string) => {
  const key = `${roleCode}_${itemId}`
  const currentLevel = localConfigs.value.get(key) || PermissionLevelEnum.NONE
  const nextLevel = getNextPermissionLevel(currentLevel)
  localConfigs.value.set(key, nextLevel)
}

// 保存配置
const handleSave = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要保存当前的权限配置吗？',
      '确认保存',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    saving.value = true
    
    // 将本地配置转换为 RolePermissionConfig 数组
    const updatedConfigs: RolePermissionConfig[] = []
    localConfigs.value.forEach((level, key) => {
      const [roleCode, itemIdStr] = key.split('_')
      const itemId = parseInt(itemIdStr)
      
      // 查找原始配置
      const originalConfig = props.configs.find(
        c => c.role_code === roleCode && c.permission_item_id === itemId
      )
      
      updatedConfigs.push({
        id: originalConfig?.id || 0,
        tenant_id: originalConfig?.tenant_id || null,
        role_code: roleCode,
        permission_item_id: itemId,
        permission_level: level,
        created_at: originalConfig?.created_at || '',
        updated_at: originalConfig?.updated_at || ''
      })
    })
    
    // 发送保存事件，等待父组件处理完成
    emit('save', updatedConfigs)
    // 注意：成功消息由父组件的 handleSaveConfigs 方法显示
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 重置配置
const handleReset = () => {
  ElMessageBox.confirm(
    '确定要重置所有修改吗？这将恢复到上次保存的状态。',
    '确认重置',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    emit('reset')
    ElMessage.success('已重置')
  }).catch(() => {
    // 用户取消
  })
}

// 打开批量设置对话框
const handleBatchSet = () => {
  batchForm.value = {
    targetRole: props.displayRoles[0]?.code || '',
    permissionLevel: PermissionLevelEnum.NONE,
    scope: 'current'
  }
  batchDialogVisible.value = true
}

// 应用批量设置
const handleBatchApply = () => {
  const { targetRole, permissionLevel, scope } = batchForm.value
  
  if (!targetRole) {
    ElMessage.warning('请选择目标角色')
    return
  }
  
  let affectedItems: PermissionItem[] = []
  
  if (scope === 'current') {
    // 当前模块
    const currentModule = props.modules.find(m => m.module_key === activeModule.value)
    if (currentModule) {
      affectedItems = getModuleItems(currentModule.id)
    }
  } else {
    // 所有模块
    affectedItems = props.items
  }
  
  // 批量更新
  affectedItems.forEach(item => {
    const key = `${targetRole}_${item.id}`
    localConfigs.value.set(key, permissionLevel)
  })
  
  batchDialogVisible.value = false
  ElMessage.success(`已为角色 "${props.displayRoles.find(r => r.code === targetRole)?.name}" 批量设置权限`)
}
</script>

<style scoped>
.permission-matrix {
  width: 100%;
}

.matrix-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.legend {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-title {
  font-weight: 600;
  color: #606266;
}

.matrix-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.permission-item-cell {
  display: flex;
  align-items: center;
  gap: 5px;
}

.item-name {
  flex: 1;
}

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 14px;
}

.permission-cell {
  cursor: pointer;
  padding: 4px;
  transition: all 0.2s;
  border-radius: 4px;
}

.permission-cell:hover {
  background: #f5f7fa;
  transform: scale(1.05);
}

.permission-tag {
  font-size: 16px;
  border: none;
  padding: 2px 8px;
}

/* 权限级别背景色 */
.permission-none {
  background: #f4f4f5;
}

.permission-readonly {
  background: #ecf5ff;
}

.permission-editable {
  background: #f0f9ff;
}
</style>

<style>
/* 权限说明tooltip样式 - 全局样式，不使用scoped */
.permission-tooltip {
  white-space: pre-line;
  max-width: 400px;
  line-height: 1.6;
}
</style>

