<template>
  <div class="permission-configuration">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Setting /></el-icon>
            权限配置管理
          </span>
          <el-space>
            <el-button
              type="primary"
              plain
              @click="showViewMode"
            >
              <el-icon><View /></el-icon>
              查看模式
            </el-button>
            <el-button
              type="info"
              plain
              @click="handleExport"
            >
              <el-icon><Download /></el-icon>
              导出配置
            </el-button>
          </el-space>
        </div>
      </template>

      <!-- 配置选择区 -->
      <div class="config-selector">
        <el-alert
          :title="configLevelText"
          :type="configLevel === 'system' ? 'warning' : 'info'"
          :closable="false"
          show-icon
        >
          <template v-if="configLevel === 'system'">
            <p>您正在配置<strong>系统默认权限</strong>。</p>
            <p>这些配置将作为所有甲方的默认权限，除非甲方有自定义配置。</p>
          </template>
          <template v-else>
            <p>您正在配置<strong>{{ currentTenantName }}</strong>的权限。</p>
            <p>这些配置将覆盖系统默认配置，仅对当前甲方生效。</p>
          </template>
        </el-alert>

        <el-form :inline="true" style="margin-top: 16px">
          <el-form-item label="配置级别">
            <el-radio-group v-model="configLevel" @change="handleConfigLevelChange">
              <el-radio value="system">系统默认配置</el-radio>
              <el-radio value="tenant" :disabled="!currentTenantId">甲方自定义配置</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="当前角色">
            <el-tag type="primary" size="large">{{ currentRoleName }}</el-tag>
          </el-form-item>
          <el-form-item label="可配置角色">
            <el-text type="info">
              共 {{ configurableRoles.length }} 个角色可配置
            </el-text>
          </el-form-item>
        </el-form>
      </div>

      <!-- 权限矩阵 -->
      <div v-loading="loading" class="matrix-wrapper">
        <PermissionMatrix
          v-if="!loading && matrixData.modules.length > 0"
          :modules="matrixData.modules"
          :items="matrixData.items"
          :configs="matrixData.configs"
          :display-roles="displayRoles"
          :loading="loading"
          @save="handleSaveConfigs"
          @reset="handleResetConfigs"
        />
        <el-empty
          v-else-if="!loading"
          description="暂无权限数据"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, View, Download } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'
import PermissionMatrix from '@/components/PermissionMatrix.vue'
import {
  getPermissionMatrix,
  getConfigurableRoles,
  batchUpdatePermissionConfigs
} from '@/api/permission'
import type {
  PermissionModule,
  PermissionItem,
  RolePermissionConfig,
  RoleInfo
} from '@/types/permission'
import { ROLE_DISPLAY } from '@/types/permission'

// ==================== Stores ====================
const router = useRouter()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// ==================== State ====================
const loading = ref(false)
const configLevel = ref<'system' | 'tenant'>('system')
const matrixData = ref<{
  modules: PermissionModule[]
  items: PermissionItem[]
  configs: RolePermissionConfig[]
}>({
  modules: [],
  items: [],
  configs: []
})
const configurableRoles = ref<RoleInfo[]>([])

// ==================== Computed ====================
const currentRoleCode = computed(() => userStore.role || 'SUPER_ADMIN')
const currentRoleName = computed(() => ROLE_DISPLAY[currentRoleCode.value]?.name || currentRoleCode.value)
const currentTenantId = computed(() => tenantStore.currentTenantId)
const currentTenantName = computed(() => tenantStore.currentTenant?.name || '未知甲方')

const configLevelText = computed(() => {
  return configLevel.value === 'system' ? '系统默认配置' : '甲方自定义配置'
})

// 显示的角色列表（根据当前用户角色筛选）
const displayRoles = computed(() => configurableRoles.value)

// ==================== Methods ====================

// 加载可配置角色列表
const loadConfigurableRoles = async () => {
  try {
    const response = await getConfigurableRoles(currentRoleCode.value)
    // axios拦截器已经解包了response.data，所以直接使用response
    const configurable_roles = response.configurable_roles || []
    configurableRoles.value = configurable_roles.map(role => ({
      code: role.code,
      name: role.name,
      description: ROLE_DISPLAY[role.code]?.description
    }))
    console.log('可配置角色：', configurableRoles.value)
  } catch (error) {
    console.error('加载可配置角色失败:', error)
    ElMessage.error('加载可配置角色失败')
  }
}

// 加载权限矩阵数据
const loadMatrixData = async () => {
  loading.value = true
  try {
    const tenantId = configLevel.value === 'system' ? null : currentTenantId.value
    const response = await getPermissionMatrix(tenantId)
    
    // axios拦截器已经解包了response.data，所以直接使用response
    matrixData.value = {
      modules: response.modules || [],
      items: response.items || [],
      configs: response.configs || []
    }
    
    console.log('权限矩阵数据：', {
      模块数: matrixData.value.modules.length,
      权限项数: matrixData.value.items.length,
      配置数: matrixData.value.configs.length
    })
  } catch (error) {
    console.error('加载权限矩阵失败:', error)
    ElMessage.error('加载权限矩阵失败')
  } finally {
    loading.value = false
  }
}

// 配置级别切换
const handleConfigLevelChange = () => {
  loadMatrixData()
}

// 保存配置
const handleSaveConfigs = async (configs: RolePermissionConfig[]) => {
  try {
    loading.value = true
    
    const tenantId = configLevel.value === 'system' ? null : currentTenantId.value
    
    // 构造批量更新请求
    const updates = configs.map(config => ({
      role_code: config.role_code,
      permission_item_id: config.permission_item_id,
      permission_level: config.permission_level
    }))
    
    const response = await batchUpdatePermissionConfigs({
      tenant_id: tenantId,
      updates
    })
    
    // axios拦截器已经解包了response.data
    if (response.success) {
      ElMessage.success(response.message || '权限配置保存成功')
      // 重新加载数据
      await loadMatrixData()
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error: any) {
    console.error('保存权限配置失败:', error)
    ElMessage.error(error.message || '保存权限配置失败')
  } finally {
    loading.value = false
  }
}

// 重置配置
const handleResetConfigs = () => {
  loadMatrixData()
}

// 查看模式（跳转到只读查看页面）
const showViewMode = () => {
  router.push('/system/permission-management')
}

// 导出配置
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
  // TODO: 实现导出功能
}

// ==================== Lifecycle ====================
onMounted(async () => {
  console.log('=== 权限配置页面初始化 ===')
  console.log('用户信息:', userStore.userInfo)
  console.log('当前角色代码:', currentRoleCode.value)
  console.log('当前角色名称:', currentRoleName.value)
  console.log('当前甲方ID:', currentTenantId.value)
  console.log('配置级别:', configLevel.value)
  
  await loadConfigurableRoles()
  await loadMatrixData()
  
  console.log('可配置角色数量:', configurableRoles.value.length)
  console.log('显示角色数量:', displayRoles.value.length)
  console.log('矩阵数据模块数:', matrixData.value.modules.length)
  console.log('矩阵数据权限项数:', matrixData.value.items.length)
  console.log('矩阵数据配置数:', matrixData.value.configs.length)
})
</script>

<style scoped>
.permission-configuration {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.config-selector {
  margin-bottom: 24px;
}

.config-selector :deep(.el-alert) {
  margin-bottom: 16px;
}

.config-selector :deep(.el-alert__description) {
  margin-top: 8px;
}

.config-selector :deep(.el-alert__description) p {
  margin: 4px 0;
}

.config-selector :deep(.el-alert__description) strong {
  color: #409eff;
}

.matrix-wrapper {
  min-height: 400px;
}
</style>

