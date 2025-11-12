<template>
  <div class="im-layout">
    <!-- 直接显示内容，不需要额外的导航栏 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
// 空布局，仅用于路由嵌套
</script>

<style scoped>
.im-layout {
  height: 100vh;
  overflow: hidden;
}
</style>

