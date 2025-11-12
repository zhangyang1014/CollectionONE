<template>
  <div class="field-linkage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字段联动配置</span>
          <el-button type="primary" @click="handleAdd">添加联动规则</el-button>
        </div>
      </template>

      <el-table :data="dependencies" border>
        <el-table-column prop="source_field_id" label="源字段ID" />
        <el-table-column prop="target_field_id" label="目标字段ID" />
        <el-table-column prop="dependency_type" label="联动类型" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFieldDependencies } from '@/api/field'

const dependencies = ref([])

const loadDependencies = async () => {
  const res = await getFieldDependencies()
  dependencies.value = res.data || []
}

const handleAdd = () => {
  ElMessage.info('添加功能待实现')
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑功能待实现')
}

const handleDelete = (row: any) => {
  ElMessage.info('删除功能待实现')
}

onMounted(() => {
  loadDependencies()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

