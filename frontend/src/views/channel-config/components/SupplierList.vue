<template>
  <div class="supplier-list">
    <div class="list-header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加供应商
      </el-button>
    </div>

    <el-table
      ref="tableRef"
      :data="suppliers"
      border
      stripe
      style="width: 100%"
      class="sortable-table"
      v-loading="loading"
    >
      <el-table-column width="50" align="center">
        <template #default>
          <el-icon class="drag-handle" style="cursor: move; color: #909399;">
            <Rank />
          </el-icon>
        </template>
      </el-table-column>

      <el-table-column prop="supplier_name" label="供应商名字" width="150" />
      
      <el-table-column prop="api_url" label="接口地址" min-width="200" show-overflow-tooltip />
      
      <el-table-column prop="api_key" label="API Key" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ maskApiKey(row.api_key) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="secret_key" label="SECRET_KEY" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ maskSecretKey(row.secret_key) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="sort_order" label="排序号" width="100" align="center" />
      
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.is_active ? 'success' : 'info'">
            {{ row.is_active ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)" size="small">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)" size="small">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="120px"
      >
        <el-form-item label="供应商名字" prop="supplier_name">
          <el-input
            v-model="form.supplier_name"
            placeholder="请输入供应商名字"
            maxlength="200"
          />
        </el-form-item>

        <el-form-item label="接口地址" prop="api_url">
          <el-input
            v-model="form.api_url"
            placeholder="请输入供应商接口地址"
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="API Key" prop="api_key">
          <el-input
            v-model="form.api_key"
            type="password"
            placeholder="请输入API Key"
            show-password
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="SECRET_KEY" prop="secret_key">
          <el-input
            v-model="form.secret_key"
            type="password"
            placeholder="请输入SECRET_KEY"
            show-password
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="form.is_active" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="1000"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Rank, Plus } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import {
  getChannelSuppliers,
  createChannelSupplier,
  updateChannelSupplier,
  deleteChannelSupplier,
  updateSupplierOrder
} from '@/api/channel'
import type { ChannelSupplier, ChannelSupplierCreate, ChannelType } from '@/types/channel'

const props = defineProps<{
  tenantId: number
  channelType: ChannelType
}>()

const emit = defineEmits<{
  refresh: []
}>()

const tableRef = ref()
const formRef = ref<FormInstance>()
const suppliers = ref<ChannelSupplier[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加供应商')
const saving = ref(false)
let sortableInstance: any = null

const form = ref<ChannelSupplierCreate & { id?: number }>({
  tenant_id: props.tenantId,
  channel_type: props.channelType,
  supplier_name: '',
  api_url: '',
  api_key: '',
  secret_key: '',
  is_active: true,
  remark: ''
})

const rules = reactive({
  supplier_name: [
    { required: true, message: '请输入供应商名字', trigger: 'blur' }
  ],
  api_url: [
    { required: true, message: '请输入接口地址', trigger: 'blur' }
  ],
  api_key: [
    { required: true, message: '请输入API Key', trigger: 'blur' }
  ],
  secret_key: [
    { required: true, message: '请输入SECRET_KEY', trigger: 'blur' }
  ]
})

// 加载供应商列表
const loadSuppliers = async () => {
  loading.value = true
  try {
    const response = await getChannelSuppliers(props.tenantId, props.channelType)
    // API直接返回数组，不是{data: [...]}格式
    suppliers.value = Array.isArray(response) ? response : (response.data || [])
    
    // 初始化拖拽排序
    await nextTick()
    initSortable()
  } catch (error) {
    console.error('加载供应商列表失败：', error)
    ElMessage.error('加载供应商列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化拖拽排序
const initSortable = () => {
  // 销毁旧的实例
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }

  const table = tableRef.value?.$el.querySelector('.el-table__body-wrapper tbody')
  if (!table) {
    console.warn('未找到表格tbody元素')
    return
  }

  sortableInstance = Sortable.create(table, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'sortable-ghost',
    chosenClass: 'sortable-chosen',
    dragClass: 'sortable-drag',
    forceFallback: true,
    fallbackTolerance: 3,
    onEnd: async (evt: any) => {
      const { oldIndex, newIndex } = evt
      if (oldIndex === newIndex || oldIndex === undefined || newIndex === undefined) return

      // 更新数组顺序
      const movedItem = suppliers.value.splice(oldIndex, 1)[0]
      suppliers.value.splice(newIndex, 0, movedItem)

      // 更新所有供应商的排序号
      const orders = suppliers.value.map((supplier, index) => ({
        supplier_id: supplier.id!,
        sort_order: index
      }))

      // 调用API保存排序
      try {
        await updateSupplierOrder(props.tenantId, props.channelType, { orders })
        ElMessage.success('排序已保存')
        // 重新加载数据以刷新显示
        await loadSuppliers()
      } catch (error) {
        console.error('保存排序失败：', error)
        ElMessage.error('保存排序失败，请重试')
        // 恢复原状态
        await loadSuppliers()
      }
    }
  })
}

// 掩码显示API Key
const maskApiKey = (key: string) => {
  if (!key) return ''
  if (key.length <= 8) return key
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

// 掩码显示SECRET_KEY
const maskSecretKey = (key: string) => {
  if (!key) return ''
  if (key.length <= 8) return key
  return key.substring(0, 4) + '****' + key.substring(key.length - 4)
}

// 添加供应商
const handleAdd = () => {
  dialogTitle.value = '添加供应商'
  form.value = {
    tenant_id: props.tenantId,
    channel_type: props.channelType,
    supplier_name: '',
    api_url: '',
    api_key: '',
    secret_key: '',
    is_active: true,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑供应商
const handleEdit = (row: ChannelSupplier) => {
  dialogTitle.value = '编辑供应商'
  form.value = {
    id: row.id,
    tenant_id: row.tenant_id,
    channel_type: row.channel_type,
    supplier_name: row.supplier_name,
    api_url: row.api_url,
    api_key: row.api_key,
    secret_key: row.secret_key,
    is_active: row.is_active,
    remark: row.remark || ''
  }
  dialogVisible.value = true
}

// 删除供应商
const handleDelete = async (row: ChannelSupplier) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除供应商"${row.supplier_name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteChannelSupplier(row.id!)
    ElMessage.success('删除成功')
    await loadSuppliers()
    emit('refresh')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除供应商失败：', error)
      ElMessage.error('删除供应商失败')
    }
  }
}

// 保存供应商
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    if (form.value.id) {
      // 更新
      const { id, tenant_id, channel_type, ...updateData } = form.value
      await updateChannelSupplier(id!, updateData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createChannelSupplier(props.tenantId, props.channelType, form.value)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await loadSuppliers()
    emit('refresh')
  } catch (error: any) {
    if (error !== false) {
      console.error('保存供应商失败：', error)
      ElMessage.error('保存供应商失败')
    }
  } finally {
    saving.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadSuppliers()
})

onBeforeUnmount(() => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
})
</script>

<style scoped>
.list-header {
  margin-bottom: 20px;
}

.sortable-table :deep(.sortable-ghost) {
  opacity: 0.5;
  background-color: #f0f9ff;
}

.sortable-table :deep(.sortable-chosen) {
  background-color: #f0f9ff;
}

.sortable-table :deep(tr.sortable-chosen td) {
  background-color: #f0f9ff !important;
}

.sortable-table :deep(.sortable-drag) {
  opacity: 0.8;
}

.drag-handle {
  cursor: move;
  font-size: 18px;
}

.drag-handle:hover {
  color: #409eff !important;
}
</style>

