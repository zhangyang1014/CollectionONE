<template>
  <div class="detail-field-groups">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情字段分组管理</span>
          <el-button type="primary" @click="handleCreate">新增分组</el-button>
        </div>
      </template>

      <el-table :data="groups" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="group_name" label="分组名称" />
        <el-table-column prop="group_key" label="分组标识" />
        <el-table-column prop="sort_order" label="排序" width="100" />
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="分组名称">
          <el-input v-model="form.group_name" />
        </el-form-item>
        <el-form-item label="分组标识">
          <el-input v-model="form.group_key" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  getDetailFieldGroups,
  createDetailFieldGroup,
  updateDetailFieldGroup,
  deleteDetailFieldGroup
} from '@/api/detailFieldGroup'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

const loading = ref(false)
const groups = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分组')
const editingId = ref<number | null>(null)
const form = ref({
  tenant_id: 1,
  group_name: '',
  group_key: '',
  sort_order: 0,
  is_active: true
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDetailFieldGroups({ tenantId: Number(currentTenantId.value) })
    groups.value = data
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingId.value = null
  dialogTitle.value = '新增分组'
  form.value = {
    tenant_id: Number(currentTenantId.value),
    group_name: '',
    group_key: '',
    sort_order: groups.value.length + 1,
    is_active: true
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  editingId.value = row.id
  dialogTitle.value = '编辑分组'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该分组吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteDetailFieldGroup(row.id, Number(currentTenantId.value))
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!form.value.group_name || !form.value.group_key) {
    ElMessage.warning('请填写必填项')
    return
  }
  form.value.tenant_id = Number(currentTenantId.value)
  if (editingId.value) {
    await updateDetailFieldGroup(editingId.value, form.value)
    ElMessage.success('更新成功')
  } else {
    await createDetailFieldGroup(form.value)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  loadData()
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

