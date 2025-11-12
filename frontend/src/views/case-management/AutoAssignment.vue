<template>
  <div class="auto-assignment">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 队列总览 -->
      <el-tab-pane label="队列总览" name="dashboard">
        <queue-dashboard />
      </el-tab-pane>

      <!-- 策略管理 -->
      <el-tab-pane label="策略管理" name="strategies">
        <strategy-list />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QueueDashboard from './auto-assignment/QueueDashboard.vue'
import StrategyList from './auto-assignment/StrategyList.vue'

const activeTab = ref('dashboard')
</script>

<style scoped>
.auto-assignment {
  height: 100%;
}
</style>

