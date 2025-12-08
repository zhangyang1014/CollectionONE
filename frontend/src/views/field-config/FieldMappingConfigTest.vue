<template>
  <div class="test-page">
    <el-card>
      <template #header>
        <h2>测试页面 - 字段映射配置</h2>
      </template>
      <p>如果你能看到这个页面，说明路由配置是正确的。</p>
      <el-button type="primary" @click="testClick">测试按钮</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const testClick = () => {
  ElMessage.success('按钮点击成功！')
}
</script>

<style scoped>
.test-page {
  padding: 20px;
}
</style>
