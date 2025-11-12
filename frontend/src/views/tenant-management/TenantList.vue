<template>
  <div class="tenant-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方管理</span>
          <el-button type="primary" @click="handleAdd">添加甲方</el-button>
        </div>
      </template>

      <el-table :data="tenants" border>
        <el-table-column prop="tenant_name" label="甲方名称" />
        <el-table-column prop="tenant_code" label="甲方编码" />
        <el-table-column prop="country_code" label="国家代码" />
        <el-table-column prop="timezone" label="时区">
          <template #default="{ row }">
            {{ row.timezone !== null && row.timezone !== undefined ? row.timezone : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currency_code" label="货币" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑甲方对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="140px" :rules="rules" ref="formRef">
        <el-divider content-position="left">甲方基础信息</el-divider>
        
        <el-form-item label="甲方名称" prop="tenant_name">
          <el-input v-model="form.tenant_name" placeholder="请输入甲方名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="甲方编码" prop="tenant_code">
          <el-input 
            v-model="form.tenant_code" 
            placeholder="如：TENANT001" 
            maxlength="50"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="国家代码" prop="country_code">
          <el-input v-model="form.country_code" placeholder="如：CN" maxlength="10" />
        </el-form-item>

        <el-form-item label="时区" prop="timezone">
          <el-input-number 
            v-model="form.timezone" 
            :min="-12" 
            :max="12" 
            :precision="0"
            placeholder="请输入UTC偏移量（如：8表示UTC+8）"
            style="width: 100%"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            范围：-12 到 +12（UTC偏移量，如：8表示UTC+8，-5表示UTC-5）
          </div>
        </el-form-item>

        <el-form-item label="货币" prop="currency_code">
          <el-input v-model="form.currency_code" placeholder="如：CNY" maxlength="10" />
        </el-form-item>

        <el-divider content-position="left">甲方管理员账号</el-divider>

        <el-form-item label="管理员账号名" prop="admin_name">
          <el-input v-model="form.admin_name" placeholder="请输入管理员账号名" maxlength="50" />
        </el-form-item>

        <el-form-item label="管理员登录ID" prop="admin_login_id">
          <el-input v-model="form.admin_login_id" placeholder="请输入登录ID" maxlength="50" />
        </el-form-item>

        <el-form-item label="管理员邮箱" prop="admin_email">
          <el-input v-model="form.admin_email" placeholder="请输入邮箱地址" maxlength="100" />
        </el-form-item>

        <el-form-item label="管理员密码" prop="admin_password">
          <el-input 
            v-model="form.admin_password" 
            type="password" 
            placeholder="请输入初始密码" 
            maxlength="50"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="admin_password_confirm">
          <el-input 
            v-model="form.admin_password_confirm" 
            type="password" 
            placeholder="请再次输入密码" 
            maxlength="50"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { getTenants } from '@/api/tenant'

const tenants = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const dialogTitle = computed(() => isEdit.value ? '编辑甲方' : '创建甲方')

// 表单数据
const form = ref({
  id: undefined,
  tenant_name: '',
  tenant_code: '',
  country_code: '',
  timezone: undefined as number | undefined,
  currency_code: '',
  admin_name: '',
  admin_login_id: '',
  admin_email: '',
  admin_password: '',
  admin_password_confirm: ''
})

// 密码确认验证器
const validatePasswordConfirm = (rule: any, value: any, callback: any) => {
  if (value !== form.value.admin_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  tenant_name: [
    { required: true, message: '请输入甲方名称', trigger: 'blur' }
  ],
  tenant_code: [
    { required: true, message: '请输入甲方编码', trigger: 'blur' }
  ],
  country_code: [
    { required: true, message: '请输入国家代码', trigger: 'blur' }
  ],
  timezone: [
    { required: true, message: '请输入时区偏移量', trigger: 'blur' },
    { 
      type: 'number', 
      min: -12, 
      max: 12, 
      message: '时区偏移量必须在-12到+12之间', 
      trigger: 'blur' 
    }
  ],
  currency_code: [
    { required: true, message: '请输入货币代码', trigger: 'blur' }
  ],
  admin_name: [
    { required: true, message: '请输入管理员账号名', trigger: 'blur' }
  ],
  admin_login_id: [
    { required: true, message: '请输入管理员登录ID', trigger: 'blur' }
  ],
  admin_email: [
    { required: true, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  admin_password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  admin_password_confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePasswordConfirm, trigger: 'blur' }
  ]
}

// 加载甲方列表
const loadTenants = async () => {
  const res = await getTenants()
  // API直接返回数组，不是{data: [...]}格式
  tenants.value = Array.isArray(res) ? res : (res.data || [])
}

// 添加甲方
const handleAdd = () => {
  isEdit.value = false
  form.value = {
    id: undefined,
    tenant_name: '',
    tenant_code: '',
    country_code: '',
    timezone: undefined,
    currency_code: '',
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: ''
  }
  dialogVisible.value = true
}

// 编辑甲方
const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    tenant_name: row.tenant_name,
    tenant_code: row.tenant_code,
    country_code: row.country_code,
    timezone: row.timezone !== null && row.timezone !== undefined ? Number(row.timezone) : undefined,
    currency_code: row.currency_code,
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: ''
  }
  dialogVisible.value = true
}

// 删除甲方
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除甲方"${row.tenant_name}"吗？删除后相关数据将无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    // TODO: 调用API删除
    ElMessage.success('删除成功')
    await loadTenants()
  } catch {
    // 用户取消删除
  }
}

// 保存甲方
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // TODO: 调用API保存
    // 创建时会同时创建甲方管理员账号
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadTenants()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  loadTenants()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

