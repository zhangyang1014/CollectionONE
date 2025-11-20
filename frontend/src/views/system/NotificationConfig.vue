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

        <!-- 消息维度提醒 - 系统自动触发的各类提醒 -->
        <el-tab-pane label="消息维度提醒" name="dimension">
          <NotificationDimensionConfig />
        </el-tab-pane>

        <!-- 通知模板 - 甲方系统推送通知 -->
        <el-tab-pane label="通知模板" name="template">
          <NotificationTemplateConfig />
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PublicNotificationConfig from './components/PublicNotificationConfig.vue'
import NotificationDimensionConfig from './components/NotificationDimensionConfig.vue'
import NotificationTemplateConfig from './components/NotificationTemplateConfig.vue'

const activeTab = ref('public')
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
