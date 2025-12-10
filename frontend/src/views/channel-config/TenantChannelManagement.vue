<template>
  <div class="tenant-channel-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方触达渠道管理</span>
          <el-space v-if="isSuperAdmin">
            <span>当前甲方:</span>
            <el-select 
              v-model="currentTenantId" 
              placeholder="选择甲方" 
              @change="handleTenantChange"
              style="width: 200px"
            >
              <el-option
                v-for="tenant in tenants"
                :key="tenant.id"
                :label="tenant.tenant_name"
                :value="tenant.id"
              />
            </el-select>
          </el-space>
          <span v-else-if="currentTenantName">当前甲方: {{ currentTenantName }}</span>
        </div>
      </template>

      <div v-if="!currentTenantId" class="no-tenant-tip">
        <el-empty description="请选择甲方" />
      </div>

      <div v-else>
        <el-tabs v-model="activeChannel" @tab-change="handleChannelChange">
          <el-tab-pane label="短信" name="sms">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="sms"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
          <el-tab-pane label="电话外呼" name="call">
            <!-- 电话外呼子渠道 -->
            <el-tabs v-model="activeCallSubChannel">
              <el-tab-pane label="Infinity外呼配置" name="infinity">
                <infinity-call-config-content />
              </el-tab-pane>
            </el-tabs>
          </el-tab-pane>
          <el-tab-pane label="RCS" name="rcs">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="rcs"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
          <el-tab-pane label="WABA" name="waba">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="waba"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
          <el-tab-pane label="WhatsApp" name="whatsapp">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="whatsapp"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
          <el-tab-pane label="邮件" name="email">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="email"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
          <el-tab-pane label="手机日历" name="mobile_calendar">
            <supplier-list 
              :tenant-id="currentTenantId"
              channel-type="mobile_calendar"
              @refresh="loadSuppliers"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import SupplierList from './components/SupplierList.vue'
import InfinityCallConfigContent from './InfinityCallConfigContent.vue'
import { getTenants } from '@/api/tenant'
import type { ChannelType } from '@/types/channel'

const tenantStore = useTenantStore()
const userStore = useUserStore()

const activeChannel = ref<ChannelType>('sms')
const activeCallSubChannel = ref<ChannelType>('infinity')
const tenants = ref<any[]>([])
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)

// 检查用户角色
const userRole = computed(() => {
  return userStore.userInfo?.role || ''
})

// 是否为超级管理员（支持多种命名格式）
const isSuperAdmin = computed(() => {
  const role = userRole.value.toLowerCase()
  return role === 'superadmin' || role === 'super_admin' || role === 'admin'
})

// 是否为甲方管理员（支持多种命名格式）
const isTenantAdmin = computed(() => {
  const role = userRole.value.toLowerCase()
  return role === 'tenantadmin' || role === 'tenant_admin' || role === 'tenant admin'
})

// 当前甲方名称
const currentTenantName = computed(() => {
  if (isSuperAdmin.value) {
    const tenant = tenants.value.find(t => t.id === currentTenantId.value)
    return tenant?.tenant_name || ''
  }
  return tenantStore.currentTenant?.tenant_name || ''
})

// 加载甲方列表（仅超级管理员）
const loadTenants = async () => {
  if (!isSuperAdmin.value) {
    // 甲方管理员使用当前甲方
    currentTenantId.value = tenantStore.currentTenantId
    return
  }

  try {
    const response = await getTenants()
    // API直接返回数组，不是{data: [...]}格式
    tenants.value = Array.isArray(response) ? response : (response.data || [])
    
    // 如果有保存的甲方ID，使用它
    if (!currentTenantId.value && tenants.value.length > 0) {
      currentTenantId.value = tenants.value[0].id
    }
  } catch (error) {
    console.error('加载甲方列表失败：', error)
    ElMessage.error('加载甲方列表失败')
  }
}

// 甲方切换
const handleTenantChange = (tenantId: number) => {
  currentTenantId.value = tenantId
  tenantStore.setCurrentTenant(tenantId, tenants.value.find(t => t.id === tenantId))
}

// 渠道切换
const handleChannelChange = (channel: string) => {
  activeChannel.value = channel as ChannelType
}

// 加载供应商（由子组件触发）
const loadSuppliers = () => {
  // 子组件会自己加载数据
}

// 权限检查（延迟到mounted后检查，确保用户信息已加载）
onMounted(() => {
  // 尝试从localStorage恢复用户信息
  userStore.initFromStorage()
  
  // 延迟检查权限，确保用户信息已加载
  setTimeout(() => {
    if (!isSuperAdmin.value && !isTenantAdmin.value) {
      ElMessage.error('您没有权限访问此页面')
      // 可以重定向到其他页面
    } else {
      loadTenants()
    }
  }, 100)
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-tenant-tip {
  padding: 40px 0;
}
</style>

