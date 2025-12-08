<template>
  <div class="detail-custom-fields">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情字段映射配置</span>
          <el-button type="primary" @click="handleCreate">新增字段</el-button>
        </div>
      </template>

      <el-table :data="fields" border style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="field_name" label="字段名称" />
        <el-table-column prop="field_key" label="字段标识" />
        <el-table-column prop="field_type" label="类型" width="120" />
        <el-table-column prop="field_group_id" label="分组ID" width="120" />
        <el-table-column prop="sort_order" label="排序" width="80" />
        <el-table-column label="必填" width="80">
          <template #default="{ row }">
            <el-tag :type="row.is_required ? 'danger' : 'info'">{{ row.is_required ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">{{ row.is_active ? '是' : '否' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="字段名称">
          <el-input v-model="form.field_name" />
        </el-form-item>
        <el-form-item label="字段标识">
          <el-input v-model="form.field_key" />
        </el-form-item>
        <el-form-item label="字段类型">
          <el-select v-model="form.field_type" style="width: 100%">
            <el-option label="text" value="text" />
            <el-option label="number" value="number" />
            <el-option label="date" value="date" />
            <el-option label="datetime" value="datetime" />
          </el-select>
        </el-form-item>
        <el-form-item label="分组ID">
          <el-input-number v-model="form.field_group_id" :min="1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="form.is_required" />
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
  getDetailCustomFields,
  createDetailCustomField,
  updateDetailCustomField,
  deleteDetailCustomField
} from '@/api/detailCustomField'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

const loading = ref(false)
const fields = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增字段')
const editingId = ref<number | null>(null)
const form = ref({
  tenant_id: 1,
  field_name: '',
  field_key: '',
  field_type: 'text',
  field_group_id: 1,
  sort_order: 0,
  is_required: false,
  is_active: true
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDetailCustomFields({ tenantId: Number(currentTenantId.value) })
    fields.value = data
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingId.value = null
  dialogTitle.value = '新增字段'
  form.value = {
    tenant_id: Number(currentTenantId.value),
    field_name: '',
    field_key: '',
    field_type: 'text',
    field_group_id: 1,
    sort_order: fields.value.length + 1,
    is_required: false,
    is_active: true
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  editingId.value = row.id
  dialogTitle.value = '编辑字段'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该字段吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteDetailCustomField(row.id, Number(currentTenantId.value))
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!form.value.field_name || !form.value.field_key) {
    ElMessage.warning('请填写必填项')
    return
  }
  form.value.tenant_id = Number(currentTenantId.value)
  if (editingId.value) {
    await updateDetailCustomField(editingId.value, form.value)
    ElMessage.success('更新成功')
  } else {
    await createDetailCustomField(form.value)
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

