<template>
  <div class="agency-list">
    <el-card>
      <!-- 头部操作栏 -->
      <template #header>
        <div class="card-header">
          <span>催收机构管理</span>
          <el-button type="primary" @click="handleCreate">添加机构</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="甲方">
          <el-select v-model="queryForm.tenant_id" placeholder="请选择甲方" style="width: 200px">
            <el-option
              v-for="tenant in tenants"
              :key="tenant.id"
              :label="tenant.tenant_name"
              :value="tenant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.is_active" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 机构列表 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="agency_code" label="机构编码" width="120" />
        <el-table-column prop="agency_name" label="机构名称" width="180" />
        <el-table-column prop="contact_person" label="联系人" width="100" />
        <el-table-column prop="contact_phone" label="联系电话" width="130" />
        <el-table-column label="小组数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.team_count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="催员数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.collector_count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="案件数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.case_count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.is_active"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort_order" width="80" align="center" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleManageTeams(row)">
              管理小组
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="甲方" prop="tenant_id">
          <el-select v-model="formData.tenant_id" placeholder="请选择甲方" style="width: 100%">
            <el-option
              v-for="tenant in tenants"
              :key="tenant.id"
              :label="tenant.tenant_name"
              :value="tenant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机构编码" prop="agency_code">
          <el-input v-model="formData.agency_code" placeholder="请输入机构编码" />
        </el-form-item>
        <el-form-item label="机构名称" prop="agency_name">
          <el-input v-model="formData.agency_name" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构名称（英文）">
          <el-input v-model="formData.agency_name_en" placeholder="请输入英文名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact_person">
          <el-input v-model="formData.contact_person" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contact_phone">
          <el-input v-model="formData.contact_phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="formData.contact_email" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="机构地址">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入机构地址"
          />
        </el-form-item>
        <el-form-item label="机构描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入机构描述"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort_order" :min="0" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="formData.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getAgencies,
  createAgency,
  updateAgency,
  deleteAgency
} from '@/api/organization'
import { getTenants } from '@/api/tenant'
import type {
  CollectionAgency,
  CollectionAgencyCreate,
  CollectionAgencyUpdate
} from '@/types/organization'

const router = useRouter()

// 状态
const loading = ref(false)
const tableData = ref<CollectionAgency[]>([])
const tenants = ref<any[]>([])

// 查询表单
const queryForm = reactive({
  tenant_id: undefined as number | undefined,
  is_active: undefined as boolean | undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const editId = ref<number>()

// 表单数据
const formData = reactive<CollectionAgencyCreate & { id?: number }>({
  tenant_id: 0,
  agency_code: '',
  agency_name: '',
  agency_name_en: '',
  contact_person: '',
  contact_phone: '',
  contact_email: '',
  address: '',
  description: '',
  sort_order: 0,
  is_active: true
})

// 表单验证规则
const formRules: FormRules = {
  tenant_id: [{ required: true, message: '请选择甲方', trigger: 'change' }],
  agency_code: [
    { required: true, message: '请输入机构编码', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  agency_name: [
    { required: true, message: '请输入机构名称', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  contact_person: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contact_phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

// 生命周期
onMounted(() => {
  loadTenants()
})

// 加载甲方列表
async function loadTenants() {
  try {
    const res = await getTenants()
    tenants.value = res.data || []
    if (tenants.value.length > 0) {
      queryForm.tenant_id = tenants.value[0].id
      loadData()
    }
  } catch (error) {
    console.error('加载甲方列表失败:', error)
  }
}

// 加载数据
async function loadData() {
  if (!queryForm.tenant_id) {
    ElMessage.warning('请先选择甲方')
    return
  }

  loading.value = true
  try {
    const res = await getAgencies({
      tenant_id: queryForm.tenant_id,
      is_active: queryForm.is_active
    })
    tableData.value = res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 重置查询
function handleReset() {
  queryForm.is_active = undefined
  loadData()
}

// 创建
function handleCreate() {
  if (!queryForm.tenant_id) {
    ElMessage.warning('请先选择甲方')
    return
  }
  
  dialogTitle.value = '添加机构'
  isEdit.value = false
  Object.assign(formData, {
    tenant_id: queryForm.tenant_id,
    agency_code: '',
    agency_name: '',
    agency_name_en: '',
    contact_person: '',
    contact_phone: '',
    contact_email: '',
    address: '',
    description: '',
    sort_order: 0,
    is_active: true
  })
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: CollectionAgency) {
  dialogTitle.value = '编辑机构'
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 查看详情
function handleView(row: CollectionAgency) {
  router.push({
    name: 'AgencyDetail',
    params: { id: row.id }
  })
}

// 管理小组
function handleManageTeams(row: CollectionAgency) {
  router.push({
    name: 'TeamList',
    query: { agency_id: row.id }
  })
}

// 删除
function handleDelete(row: CollectionAgency) {
  ElMessageBox.confirm(
    `确定要删除机构"${row.agency_name}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAgency(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.detail || '删除失败')
    }
  })
}

// 状态切换
async function handleStatusChange(row: CollectionAgency) {
  try {
    await updateAgency(row.id, { is_active: row.is_active })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.is_active = !row.is_active
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value && editId.value) {
        const { tenant_id, agency_code, ...updateData } = formData
        await updateAgency(editId.value, updateData as CollectionAgencyUpdate)
        ElMessage.success('更新成功')
      } else {
        await createAgency(formData as CollectionAgencyCreate)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.detail || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 关闭对话框
function handleDialogClose() {
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.agency-list {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 0;
  }
}
</style>

