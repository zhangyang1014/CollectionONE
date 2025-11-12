<template>
  <div class="workspace-container">
    <h1>催员工作台</h1>
    <p>欢迎，{{ user?.collectorName }}！</p>
    <el-card>
      <template #header>
        <span>用户信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="催员ID">{{ user?.collectorId }}</el-descriptions-item>
        <el-descriptions-item label="催员姓名">{{ user?.collectorName }}</el-descriptions-item>
        <el-descriptions-item label="机构">{{ user?.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ user?.role }}</el-descriptions-item>
        <el-descriptions-item label="团队">{{ user?.team }}</el-descriptions-item>
        <el-descriptions-item label="WhatsApp">
          <el-tag :type="user?.whatsappConnected ? 'success' : 'info'">
            {{ user?.whatsappConnected ? '已连接' : '未连接' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useImUserStore } from '@/stores/imUser'

const imUserStore = useImUserStore()
const user = computed(() => imUserStore.user)
</script>

<style scoped>
.workspace-container {
  padding: 24px;
}

h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

p {
  color: #909399;
  margin-bottom: 24px;
}
</style>

