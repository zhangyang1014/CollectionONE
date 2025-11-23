<template>
  <div class="permission-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出权限文档
          </el-button>
        </div>
      </template>

      <!-- 角色选择器 -->
      <div class="role-selector">
        <el-radio-group v-model="selectedRole" @change="handleRoleChange">
          <el-radio-button value="all">全部角色</el-radio-button>
          <el-radio-button value="SUPER_ADMIN">超级管理员</el-radio-button>
          <el-radio-button value="TENANT_ADMIN">甲方管理员</el-radio-button>
          <el-radio-button value="AGENCY_ADMIN">机构管理员</el-radio-button>
          <el-radio-button value="TEAM_LEADER">小组长</el-radio-button>
          <el-radio-button value="QUALITY_INSPECTOR">质检员</el-radio-button>
          <el-radio-button value="DATA_SOURCE">数据源</el-radio-button>
          <el-radio-button value="COLLECTOR">催员</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 权限模块标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="permission-tabs">
        <el-tab-pane
          v-for="module in permissionModules"
          :key="module.key"
          :label="module.label"
          :name="module.key"
        >
          <div class="permission-table-container">
            <el-table
              :data="getFilteredPermissions(module.key)"
              border
              stripe
              style="width: 100%"
              :max-height="600"
            >
              <el-table-column prop="name" label="权限项目" width="250" fixed="left">
                <template #default="{ row }">
                  <div class="permission-name">
                    <span>{{ row.name }}</span>
                    <el-tooltip v-if="row.description" :content="row.description" placement="top">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column
                v-for="role in roles"
                :key="role.code"
                :label="role.name"
                width="120"
                align="center"
              >
                <template #default="{ row }">
                  <el-tag
                    :type="getPermissionType(row.permissions[role.code])"
                    effect="plain"
                  >
                    {{ getPermissionText(row.permissions[role.code]) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 权限说明 -->
      <el-card class="permission-legend" style="margin-top: 20px">
        <template #header>
          <span>权限说明</span>
        </template>
        <div class="legend-content">
          <div class="legend-item">
            <el-tag type="success" effect="plain">✅</el-tag>
            <span>拥有该权限</span>
          </div>
          <div class="legend-item">
            <el-tag type="info" effect="plain">❌</el-tag>
            <span>不拥有该权限</span>
          </div>
          <div class="legend-item">
            <el-tag type="warning" effect="plain">🔒</el-tag>
            <span>受限权限（只能操作权限范围内的数据）</span>
          </div>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import permissionData from './permission-data'

const userStore = useUserStore()

const selectedRole = ref('all')
const activeTab = ref('system')

// 角色列表
const roles = [
  { code: 'SUPER_ADMIN', name: '超级管理员' },
  { code: 'TENANT_ADMIN', name: '甲方管理员' },
  { code: 'AGENCY_ADMIN', name: '机构管理员' },
  { code: 'TEAM_LEADER', name: '小组长' },
  { code: 'QUALITY_INSPECTOR', name: '质检员' },
  { code: 'DATA_SOURCE', name: '数据源' },
  { code: 'COLLECTOR', name: '催员' }
]

// 权限模块列表
const permissionModules = [
  { key: 'system', label: '系统管理' },
  { key: 'tenant', label: '甲方管理' },
  { key: 'agency', label: '机构管理' },
  { key: 'team', label: '小组管理' },
  { key: 'collector', label: '催员管理' },
  { key: 'case', label: '案件管理' },
  { key: 'field', label: '字段配置' },
  { key: 'channel', label: '渠道配置' },
  { key: 'performance', label: '业绩查看' },
  { key: 'chat', label: '聊天内容查看' },
  { key: 'dashboard', label: '工作台' }
]

// 获取过滤后的权限列表
const getFilteredPermissions = (moduleKey: string) => {
  const permissions = permissionData[moduleKey] || []
  if (selectedRole.value === 'all') {
    return permissions
  }
  // 如果选择了特定角色，只显示该角色有权限的项目
  return permissions.filter(p => {
    const perm = p.permissions[selectedRole.value]
    return perm === 'yes' || perm === 'limited'
  })
}

// 获取权限类型
const getPermissionType = (permission: string) => {
  if (permission === 'yes') return 'success'
  if (permission === 'limited') return 'warning'
  return 'info'
}

// 获取权限文本
const getPermissionText = (permission: string) => {
  if (permission === 'yes') return '✅'
  if (permission === 'limited') return '🔒'
  return '❌'
}

// 角色切换
const handleRoleChange = () => {
  // 可以在这里添加筛选逻辑
}

// 导出权限文档
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
  // TODO: 实现导出功能
}
</script>

<style scoped>
.permission-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-selector {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.permission-tabs {
  margin-top: 20px;
}

.permission-table-container {
  max-height: 600px;
  overflow-y: auto;
}

.permission-name {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 14px;
}

.permission-legend {
  background: #f9fafb;
}

.legend-content {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-item span {
  font-size: 14px;
  color: #606266;
}
</style>

