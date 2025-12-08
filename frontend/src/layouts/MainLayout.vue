<template>
  <el-container class="main-layout">
    <el-aside width="250px" class="sidebar">
      <div class="logo">
        <h2>CCO系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ $t('menu.dashboard') }}</span>
        </el-menu-item>

        <el-sub-menu index="dashboard">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </template>
          <el-menu-item index="/performance/my-dashboard">单催员业绩看板</el-menu-item>
          <el-menu-item index="/dashboard/idle-monitor">空闲催员监控</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="case">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.caseManagement') }}</span>
          </template>
          <el-menu-item index="/cases">{{ $t('menu.caseList') }}</el-menu-item>
          <el-menu-item index="/cases/stay">停留案件</el-menu-item>
          <el-menu-item index="/auto-assignment">自动化分案</el-menu-item>
          <el-menu-item index="/cases/reassign-config">案件重新分案配置</el-menu-item>
          <el-menu-item index="/tenants/queue-management">{{ $t('menu.queueManagement') }}</el-menu-item>
        </el-sub-menu>

        <!-- 案件列表字段配置菜单 -->
        <el-sub-menu index="field-list">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>{{ $t('menu.caseListFieldConfig') }}</span>
          </template>
          <el-menu-item index="/field-config/standard">{{ $t('menu.standardFields') }}</el-menu-item>
          <el-menu-item index="/field-config/tenant-fields-view">甲方字段查看</el-menu-item>
          <el-menu-item index="/field-config/custom">{{ $t('menu.customFields') }}</el-menu-item>
          <el-menu-item index="/field-config/groups">{{ $t('menu.fieldGroups') }}</el-menu-item>
          <el-menu-item index="/field-config/list">{{ $t('menu.caseListFieldConfig') }}</el-menu-item>
        </el-sub-menu>

        <!-- 案件详情字段配置菜单（仅保留详情配置，避免与列表菜单重复激活） -->
        <el-sub-menu index="field-detail">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>{{ $t('menu.caseDetailFieldConfig') }}</span>
          </template>
          <el-menu-item index="/field-config/detail/standard">案件详情标准字段管理</el-menu-item>
          <el-menu-item index="/field-config/detail/tenant-fields-view">案件详情甲方字段查看</el-menu-item>
          <el-menu-item index="/field-config/detail/custom">案件详情字段映射配置</el-menu-item>
          <el-menu-item index="/field-config/detail/groups">案件详情字段分组管理</el-menu-item>
          <el-menu-item index="/field-config/detail">{{ $t('menu.caseDetailFieldConfig') }}</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="tenant">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ $t('menu.tenantManagement') }}</span>
          </template>
          <el-menu-item index="/tenants">{{ $t('menu.tenantList') }}</el-menu-item>
          <el-menu-item index="/organization/agencies">{{ $t('menu.agencyManagement') }}</el-menu-item>
          <el-menu-item index="/organization/team-groups">小组群管理</el-menu-item>
          <el-menu-item index="/organization/teams">{{ $t('menu.teamManagement') }}</el-menu-item>
          <el-menu-item index="/organization/admin-accounts">{{ $t('menu.adminAccountManagement') }}</el-menu-item>
          <el-menu-item index="/organization/collectors">{{ $t('menu.collectorManagement') }}</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="channel">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>渠道配置</span>
          </template>
          <el-menu-item index="/channel-config/limits">渠道发送限制配置</el-menu-item>
          <el-menu-item index="/channel-config/suppliers">甲方渠道管理</el-menu-item>
          <el-menu-item index="/channel-config/payment-channels">还款渠道管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="message">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>消息配置</span>
          </template>
          <el-menu-item index="/console/message-templates">消息模板配置管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="isSuperAdmin || isTenantAdmin" index="system">
          <template #title>
            <el-icon><Lock /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/permissions">权限配置</el-menu-item>
          <el-menu-item index="/system/notification-config">通知配置</el-menu-item>
          <el-menu-item v-if="isSuperAdmin" index="/system/i18n">国际化配置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left"></div>
        <div class="header-right">
          <!-- 全局甲方选择器 -->
          <div class="tenant-selector">
            <span class="tenant-label">当前甲方：</span>
            <el-select 
              v-model="currentTenantId" 
              placeholder="请选择甲方" 
              @change="handleTenantChange"
              style="width: 200px"
              clearable
            >
              <el-option
                v-for="tenant in tenants"
                :key="tenant.id"
                :label="tenant.tenant_name"
                :value="tenant.id"
              />
            </el-select>
          </div>
          
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>用户</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, Document, Setting, OfficeBuilding, User, Connection, Lock, DataAnalysis, ChatDotRound } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { getTenants } from '@/api/tenant'

const route = useRoute()
const activeMenu = computed(() => route.path)

// 全局甲方状态管理
const tenantStore = useTenantStore()
const userStore = useUserStore()

// 检查是否为超级管理员
const isSuperAdmin = computed(() => {
  const userRole = userStore.userInfo?.role || ''
  return userRole.toLowerCase() === 'superadmin' || userRole === 'SUPER_ADMIN'
})

// 检查是否为甲方管理员
const isTenantAdmin = computed(() => {
  const userRole = userStore.userInfo?.role || ''
  return userRole.toLowerCase() === 'tenantadmin' || userRole === 'TENANT_ADMIN'
})

const tenants = ref<any[]>([])
const currentTenantId = computed({
  get: () => tenantStore.currentTenantId,
  set: (value) => {
    const tenant = tenants.value.find(t => t.id === value)
    tenantStore.setCurrentTenant(value, tenant)
  }
})

// 加载甲方列表
const loadTenants = async () => {
  try {
    // 设置超时，避免长时间等待
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('请求超时')), 5000)
    })
    
    const res = await Promise.race([getTenants(), timeoutPromise]) as any
    console.log('getTenants 原始响应：', res)
    
    // 处理多种可能的响应格式
    if (Array.isArray(res)) {
      tenants.value = res
    } else if (res.data && Array.isArray(res.data)) {
      tenants.value = res.data
    } else if (res.data && res.data.items && Array.isArray(res.data.items)) {
      tenants.value = res.data.items
    } else {
      console.warn('甲方列表响应格式不符合预期：', res)
      tenants.value = []
    }
    
    console.log('加载到的甲方列表：', tenants.value)
    console.log('甲方数量：', tenants.value.length)
    
    // 从localStorage恢复之前的选择
    tenantStore.restoreFromStorage()
    
    // 如果没有数据，显示友好提示
    if (tenants.value.length === 0) {
      console.warn('当前没有可用的甲方数据')
    }
  } catch (error: any) {
    console.error('加载甲方列表失败：', error)
    console.error('错误详情：', error.response || error.message)
    // 不显示错误提示，避免干扰用户，静默失败
    // ElMessage.error(`加载甲方列表失败: ${error.message || '未知错误'}`)
    tenants.value = []
    // 即使失败也继续，不影响页面正常使用
  }
}

// 甲方切换处理
const handleTenantChange = (value: number | undefined) => {
  console.log('全局甲方切换：', value)
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '退出确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 清除用户信息
      userStore.logout()
      
      // 清除甲方选择
      tenantStore.clearCurrentTenant()
      
      ElMessage.success('已退出登录')
      
      // 跳转到登录页
      window.location.href = '/admin/login'
    } catch (error) {
      // 用户取消
    }
  }
}

onMounted(() => {
  // 异步加载甲方列表，不阻塞页面渲染
  // 使用 nextTick 确保页面先渲染完成
  nextTick(() => {
    loadTenants()
  })
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1f2d3d;
}

.logo h2 {
  color: #fff;
  margin: 0;
}

.sidebar-menu {
  border: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.tenant-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tenant-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>

