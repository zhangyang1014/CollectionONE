<template>
  <div class="notification-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知配置</span>
        </div>
      </template>

      <!-- 通知类型标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="config-tabs">
        <!-- 公共通知 - 官方通知，最重要 -->
        <el-tab-pane label="公共通知" name="public">
          <PublicNotificationConfig />
        </el-tab-pane>

        <!-- 案件有待回复信息 - 实时性最强 -->
        <el-tab-pane label="案件有待回复信息" name="unreplied">
          <UnrepliedConfig 
            :config="configs.unreplied" 
            @save="handleSave('unreplied', $event)"
          />
        </el-tab-pane>

        <!-- 长时间未响应 - 紧急性高 -->
        <el-tab-pane label="长时间未响应" name="timeout">
          <TimeoutConfig 
            :config="configs.timeout" 
            @save="handleSave('timeout', $event)"
          />
        </el-tab-pane>

        <!-- 催办机制 - 日常提醒 -->
        <el-tab-pane label="催办机制" name="nudge">
          <NudgeConfig 
            :config="configs.nudge" 
            @save="handleSave('nudge', $event)"
          />
        </el-tab-pane>

        <!-- 案件信息更新 - 信息通知 -->
        <el-tab-pane label="案件信息更新" name="case_update">
          <CaseUpdateConfig 
            :config="configs.case_update" 
            @save="handleSave('case_update', $event)"
          />
        </el-tab-pane>

        <!-- 组织绩效通知 - 激励性质 -->
        <el-tab-pane label="组织绩效通知" name="performance">
          <PerformanceConfig 
            :config="configs.performance" 
            @save="handleSave('performance', $event)"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import { getNotificationConfigs, updateNotificationConfig } from '@/api/notification'
import type { NotificationConfig } from '@/types/notification'
import PublicNotificationConfig from './components/PublicNotificationConfig.vue'
import UnrepliedConfig from './components/UnrepliedConfig.vue'
import NudgeConfig from './components/NudgeConfig.vue'
import CaseUpdateConfig from './components/CaseUpdateConfig.vue'
import PerformanceConfig from './components/PerformanceConfig.vue'
import TimeoutConfig from './components/TimeoutConfig.vue'

const tenantStore = useTenantStore()
const activeTab = ref('public')
const loading = ref(false)

const configs = ref<Record<string, NotificationConfig | null>>({
  unreplied: null,
  nudge: null,
  case_update: null,
  performance: null,
  timeout: null
})

// 加载配置
const loadConfigs = async () => {
  try {
    loading.value = true
    const tenantId = tenantStore.currentTenantId || undefined
    const response: any = await getNotificationConfigs({ tenant_id: tenantId })
    const configsList = Array.isArray(response) ? response : (response.data || [])
    
    // 按类型分组
    configs.value = {
      unreplied: null,
      nudge: null,
      case_update: null,
      performance: null,
      timeout: null
    }
    
    configsList.forEach((config: NotificationConfig) => {
      if (config.notification_type in configs.value) {
        configs.value[config.notification_type] = config
      }
    })
  } catch (error: any) {
    console.error('加载通知配置失败：', error)
    ElMessage.error('加载通知配置失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 保存配置
const handleSave = async (type: string, configData: any) => {
  try {
    const config = configs.value[type]
    if (!config) {
      ElMessage.error('配置不存在')
      return
    }
    
    await updateNotificationConfig(config.id, {
      is_enabled: configData.is_enabled,
      config_data: configData.config_data
    })
    
    ElMessage.success('保存成功')
    await loadConfigs()
  } catch (error: any) {
    console.error('保存通知配置失败：', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.notification-config {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-tabs {
  margin-top: 20px;
}
</style>
