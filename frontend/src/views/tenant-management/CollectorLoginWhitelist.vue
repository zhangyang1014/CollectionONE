<template>
  <div class="collector-login-whitelist">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>催员登录白名单IP管理</span>
          <div>
            <el-switch
              v-model="whitelistEnabled"
              active-text="启用白名单"
              inactive-text="禁用白名单"
              @change="handleToggleWhitelist"
              :loading="toggleLoading"
              style="margin-right: 10px;"
            />
            <el-button 
              type="primary" 
              @click="handleAdd" 
              :disabled="!currentTenantId"
            >
              添加IP地址
            </el-button>
          </div>
        </div>
      </template>

      <!-- 提示信息 -->
      <el-alert
        v-if="whitelistEnabled"
        title="白名单IP登录管理已启用"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <div style="font-size: 12px;">
            只有白名单中的IP地址可以登录，其他IP地址将被拒绝访问
          </div>
        </template>
      </el-alert>

      <el-alert
        v-else
        title="白名单IP登录管理已禁用"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <div style="font-size: 12px;">
            所有IP地址都可以登录，不受限制
          </div>
        </template>
      </el-alert>

      <!-- 筛选器 -->
      <el-form :model="filters" class="filter-form" label-width="100px" inline>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">刷新</el-button>
        </el-form-item>
      </el-form>

      <!-- IP列表 -->
      <el-table :data="whitelists" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ipAddress" label="IP地址" width="200">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.ipAddress || row.ip_address }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述/备注" min-width="200">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.isActive !== undefined ? row.isActive : row.is_active) ? 'success' : 'info'">
              {{ (row.isActive !== undefined ? row.isActive : row.is_active) ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt || row.created_at || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)" 
              size="small"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="140px" :rules="rules" ref="formRef">
        <el-form-item label="IP地址" prop="ip_address">
          <el-input 
            v-model="form.ip_address" 
            placeholder="请输入IP地址（支持IPv4和CIDR格式，如：192.168.1.1 或 192.168.1.0/24）"
            style="width: 100%"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            支持格式：单个IP（如：192.168.1.1）或CIDR网段（如：192.168.1.0/24）
          </div>
        </el-form-item>

        <el-form-item label="描述/备注" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入IP地址的描述或备注信息（可选）"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="是否启用" prop="is_active">
          <el-switch v-model="form.is_active" />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            禁用后，该IP地址将不会生效
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  getCollectorLoginWhitelistList,
  createCollectorLoginWhitelist,
  updateCollectorLoginWhitelist,
  deleteCollectorLoginWhitelist,
  setWhitelistEnabled,
  checkWhitelistEnabled
} from '@/api/collectorLoginWhitelist'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 数据
const loading = ref(false)
const whitelists = ref<any[]>([])
const whitelistEnabled = ref(false)
const toggleLoading = ref(false)
const filters = reactive({})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加IP地址')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  id: null as number | null,
  tenant_id: null as number | null,
  ip_address: '',
  description: '',
  is_active: true
})

// 表单验证规则
const rules = {
  ip_address: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { 
      pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\/([0-9]|[12][0-9]|3[0-2]))?$/,
      message: 'IP地址格式不正确（支持IPv4和CIDR格式）',
      trigger: 'blur'
    }
  ]
}

// 查询列表
const handleQuery = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  loading.value = true
  try {
    const response: any = await getCollectorLoginWhitelistList(currentTenantId.value)
    whitelists.value = Array.isArray(response) ? response : (response.data || [])
    
    // 检查白名单是否启用
    const enabledResponse: any = await checkWhitelistEnabled(currentTenantId.value)
    whitelistEnabled.value = enabledResponse?.enabled || false
  } catch (error: any) {
    console.error('查询白名单IP列表失败:', error)
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 切换白名单启用状态
const handleToggleWhitelist = async (enabled: boolean) => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    whitelistEnabled.value = false
    return
  }

  toggleLoading.value = true
  try {
    await setWhitelistEnabled(currentTenantId.value, enabled)
    ElMessage.success(enabled ? '已启用白名单IP登录管理' : '已禁用白名单IP登录管理')
    whitelistEnabled.value = enabled
  } catch (error: any) {
    console.error('切换白名单状态失败:', error)
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
    // 恢复原状态
    whitelistEnabled.value = !enabled
  } finally {
    toggleLoading.value = false
  }
}

// 添加
const handleAdd = () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  isEdit.value = false
  dialogTitle.value = '添加IP地址'
  form.id = null
  form.tenant_id = currentTenantId.value
  form.ip_address = ''
  form.description = ''
  form.is_active = true
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑IP地址'
  form.id = row.id
  form.tenant_id = row.tenantId || row.tenant_id
  form.ip_address = row.ipAddress || row.ip_address
  form.description = row.description || ''
  form.is_active = row.isActive !== undefined ? row.isActive : row.is_active
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除IP地址 "${row.ipAddress || row.ip_address}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCollectorLoginWhitelist(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (isEdit.value) {
      // 更新
      await updateCollectorLoginWhitelist(form.id!, {
        ip_address: form.ip_address,
        description: form.description,
        is_active: form.is_active
      })
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createCollectorLoginWhitelist({
        tenant_id: form.tenant_id!,
        ip_address: form.ip_address,
        description: form.description,
        is_active: form.is_active
      })
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    handleQuery()
  } catch (error: any) {
    if (error !== false) { // 表单验证失败会返回false
      console.error('提交失败:', error)
      ElMessage.error('操作失败：' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  if (currentTenantId.value) {
    handleQuery()
  }
})

// 监听甲方变化
import { watch } from 'vue'
watch(currentTenantId, (newVal) => {
  if (newVal) {
    handleQuery()
  } else {
    whitelists.value = []
    whitelistEnabled.value = false
  }
})
</script>

<style scoped lang="scss">
.collector-login-whitelist {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .filter-form {
    margin-bottom: 20px;
  }
}
</style>



