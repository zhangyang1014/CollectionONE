<template>
  <div class="tenant-field-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方字段配置</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-table :data="configs" border>
        <el-table-column prop="field_id" label="字段ID" />
        <el-table-column prop="field_type" label="字段类型" />
        <el-table-column label="是否启用">
          <template #default="{ row }">
            <el-switch v-model="row.is_enabled" @change="updateConfig(row)" />
          </template>
        </el-table-column>
        <el-table-column label="是否必填">
          <template #default="{ row }">
            <el-switch v-model="row.is_required" @change="updateConfig(row)" />
          </template>
        </el-table-column>
        <el-table-column label="是否只读">
          <template #default="{ row }">
            <el-switch v-model="row.is_readonly" @change="updateConfig(row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTenantFieldConfigs, updateTenantFieldConfig } from '@/api/tenant'

const route = useRoute()
const tenantId = Number(route.params.id)
const configs = ref([])

const loadConfigs = async () => {
  const res = await getTenantFieldConfigs(tenantId)
  configs.value = res.data || []
}

const updateConfig = async (row: any) => {
  try {
    await updateTenantFieldConfig(tenantId, row.id, row)
    ElMessage.success('更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
    loadConfigs()
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

