<template>
  <div class="detail-tenant-fields-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情甲方字段查看</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-table :data="fields" border style="width: 100%">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="field_name" label="字段名称" />
          <el-table-column prop="field_key" label="字段标识" />
          <el-table-column prop="field_type" label="类型" width="120" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import { getDetailTenantFieldsJson } from '@/api/detailTenantFields'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

const loading = ref(false)
const fields = ref<any[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDetailTenantFieldsJson(Number(currentTenantId.value))
    fields.value = res?.fields || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

