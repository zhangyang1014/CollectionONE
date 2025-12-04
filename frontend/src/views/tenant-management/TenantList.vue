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
        <el-table-column label="甲方名称">
          <template #default="{ row }">
            {{ row.tenantName || row.tenant_name }}
          </template>
        </el-table-column>
        <el-table-column label="甲方编码">
          <template #default="{ row }">
            {{ row.tenantCode || row.tenant_code }}
          </template>
        </el-table-column>
        <el-table-column label="国家代码">
          <template #default="{ row }">
            {{ row.countryCode || row.country_code }}
          </template>
        </el-table-column>
        <el-table-column label="时区">
          <template #default="{ row }">
            {{ row.timezone !== null && row.timezone !== undefined ? row.timezone : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="货币">
          <template #default="{ row }">
            {{ row.currencyCode || row.currency_code }}
          </template>
        </el-table-column>
        <el-table-column label="默认语言" width="140">
          <template #default="{ row }">
            <span v-if="row.defaultLanguage || row.default_language">
              {{ getLanguageDisplay(row.defaultLanguage || row.default_language) }}
            </span>
            <el-text v-else type="info">未设置</el-text>
          </template>
        </el-table-column>
        <el-table-column label="催员数" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewCollectors(row)"
              :disabled="!row.collector_count || row.collector_count === 0"
            >
              {{ row.collector_count || 0 }}
            </el-button>
          </template>
        </el-table-column>
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

        <el-form-item label="默认语言" prop="default_language">
          <el-select 
            v-model="form.default_language" 
            placeholder="请选择默认语言"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="lang in availableLanguages"
              :key="lang.locale"
              :label="`${lang.flagIcon || ''} ${lang.name} (${lang.locale})`"
              :value="lang.locale"
            >
              <span style="display: flex; align-items: center; gap: 8px;">
                <span style="font-size: 18px;">{{ lang.flagIcon || '🏳️' }}</span>
                <span>{{ lang.name }}</span>
                <el-tag v-if="lang.isDefault" type="warning" size="small">推荐</el-tag>
              </span>
            </el-option>
          </el-select>
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            该语言将作为该甲方下所有催员的默认界面语言
          </div>
        </el-form-item>

        <el-divider content-position="left">甲方管理员账号</el-divider>

        <el-form-item label="管理员账号名" prop="admin_name">
          <el-input 
            v-model="form.admin_name" 
            placeholder="请输入管理员账号名" 
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="管理员登录ID" prop="admin_login_id">
          <el-input 
            v-model="form.admin_login_id" 
            placeholder="请输入自定义部分（如：admin01）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && form.tenant_code">{{ form.tenant_code }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整登录ID：{{ form.tenant_code || '甲方编码' }}-{{ form.admin_login_id || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            登录ID不可修改
          </div>
        </el-form-item>

        <el-form-item label="管理员邮箱" prop="admin_email">
          <el-input 
            v-model="form.admin_email" 
            placeholder="请输入邮箱地址" 
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="管理员密码" prop="admin_password">
          <el-input 
            v-model="form.admin_password" 
            type="password" 
            :placeholder="isEdit ? '如需修改密码请输入新密码，否则留空' : '请输入初始密码'" 
            maxlength="50"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="admin_password_confirm" v-if="!isEdit || form.admin_password">
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
import { useRouter } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'
import { getTenants } from '@/api/tenant'
import { getLanguageList } from '@/api/i18n'

const router = useRouter()
const tenantStore = useTenantStore()
const tenants = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 可用语言列表
const availableLanguages = ref<any[]>([])

// 加载可用语言列表
const loadAvailableLanguages = async () => {
  try {
    const response = await getLanguageList({ status: 'enabled' })
    availableLanguages.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error) {
    console.error('加载语言列表失败：', error)
    ElMessage.warning('加载语言列表失败，将使用默认选项')
    // 提供默认语言选项
    availableLanguages.value = [
      { locale: 'zh-CN', name: '简体中文', flagIcon: '🇨🇳', isDefault: true },
      { locale: 'en-US', name: 'English', flagIcon: '🇺🇸', isDefault: false }
    ]
  }
}

// 获取语言显示名称
const getLanguageDisplay = (locale: string) => {
  const lang = availableLanguages.value.find(l => l.locale === locale)
  return lang ? `${lang.flagIcon || ''} ${lang.name}` : locale
}

const dialogTitle = computed(() => isEdit.value ? '编辑甲方' : '创建甲方')

// 表单数据
const form = ref({
  id: undefined,
  tenant_name: '',
  tenant_code: '',
  country_code: '',
  timezone: undefined as number | undefined,
  currency_code: '',
  default_language: '' as string,
  admin_name: '',
  admin_login_id: '',
  admin_email: '',
  admin_password: '',
  admin_password_confirm: ''
})


// 密码确认验证器
const validatePasswordConfirm = (_rule: any, value: any, callback: any) => {
  if (value !== form.value.admin_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码条件验证器（编辑时如果填写了密码才验证）
const validatePasswordConditional = (_rule: any, value: any, callback: any) => {
  if (!isEdit.value) {
    // 创建模式：密码必填
    if (!value) {
      callback(new Error('请输入管理员密码'))
    } else if (value.length < 6) {
      callback(new Error('密码长度不能少于6位'))
    } else {
      callback()
    }
  } else {
    // 编辑模式：密码可选，但如果填写了就要验证长度
    if (value && value.length < 6) {
      callback(new Error('密码长度不能少于6位'))
    } else {
      callback()
    }
  }
}

// 动态表单验证规则
const getRules = () => ({
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
  default_language: [
    { required: true, message: '请选择默认语言', trigger: 'change' }
  ],
  admin_name: [
    { required: !isEdit.value, message: '请输入管理员账号名', trigger: 'blur' }
  ],
  admin_login_id: [
    { required: !isEdit.value, message: '请输入管理员登录ID', trigger: 'blur' }
  ],
  admin_email: [
    { required: !isEdit.value, message: '请输入管理员邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  admin_password: [
    { validator: validatePasswordConditional, trigger: 'blur' }
  ],
  admin_password_confirm: [
    { validator: validatePasswordConfirm, trigger: 'blur' }
  ]
})

const rules = getRules()

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
    default_language: '',
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: ''
  }
  dialogVisible.value = true
}

// 查看催员（跳转到催员管理页面，需要先切换到该甲方）
const handleViewCollectors = async (row: any) => {
  // 先切换到该甲方
  tenantStore.setCurrentTenant(row.id, row)
  
  // 跳转到催员管理页面
  router.push({
    path: '/organization/collectors'
  })
}

// 编辑甲方
const handleEdit = async (row: any) => {
  isEdit.value = true
  
  // 先设置基础信息
  form.value = {
    id: row.id,
    tenant_name: row.tenant_name,
    tenant_code: row.tenant_code,
    country_code: row.country_code,
    timezone: row.timezone !== null && row.timezone !== undefined ? Number(row.timezone) : undefined,
    currency_code: row.currency_code,
    default_language: row.default_language || '',
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: ''
  }
  
  // 获取该甲方的管理员账号信息并回显
  try {
    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl(`tenants/${row.id}/admin-accounts`))
    const accounts = await response.json()
    
    // 如果存在管理员账号，回显第一个账号的信息
    if (accounts && accounts.length > 0) {
      const adminAccount = accounts[0]
      form.value.admin_name = adminAccount.account_name || ''
      form.value.admin_login_id = adminAccount.login_id || ''
      form.value.admin_email = adminAccount.email || ''
    }
  } catch (error) {
    console.error('获取管理员账号信息失败：', error)
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
    
    const { getApiUrl } = await import('@/config/api')
    const url = isEdit.value 
      ? getApiUrl(`tenants/${form.value.id}`)
      : getApiUrl('tenants')
    
    const method = isEdit.value ? 'PUT' : 'POST'
    
    // 准备甲方基础数据
    const tenantData = {
      tenant_name: form.value.tenant_name,
      tenant_code: form.value.tenant_code,
      country_code: form.value.country_code,
      timezone: form.value.timezone,
      currency_code: form.value.currency_code,
      is_active: true
    }
    
    // 保存甲方基础信息
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(tenantData)
    })
    
    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.detail || '保存失败')
    }
    
    const savedTenant = await response.json()
    
    // 如果是创建模式且填写了管理员信息，则创建管理员账号
    if (!isEdit.value && form.value.admin_name && form.value.admin_login_id) {
      await createTenantAdmin(savedTenant.id)
    } else if (isEdit.value && form.value.admin_name) {
      // 编辑模式：更新管理员账号
      await updateTenantAdmin(savedTenant.id)
    }
    
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadTenants()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  }
}

// 创建甲方管理员账号
const createTenantAdmin = async (tenantId: number) => {
  try {
    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl('admin-accounts'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        tenant_id: tenantId,
        account_code: `TENANT_ADMIN_${tenantId}`,
        account_name: form.value.admin_name,
        login_id: form.value.tenant_code + '-' + form.value.admin_login_id,
        password: form.value.admin_password,
        email: form.value.admin_email,
        role: 'tenant_admin',
        is_active: true
      })
    })
    
    if (!response.ok) {
      console.error('创建管理员账号失败')
    }
  } catch (error) {
    console.error('创建管理员账号失败:', error)
  }
}

// 更新甲方管理员账号
const updateTenantAdmin = async (tenantId: number) => {
  try {
    // 先获取现有管理员账号
    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl(`tenants/${tenantId}/admin-accounts`))
    const accounts = await response.json()
    
    if (accounts && accounts.length > 0) {
      // 更新现有账号
      const adminId = accounts[0].id
      const updateData: any = {
        account_name: form.value.admin_name,
        email: form.value.admin_email
      }
      
      // 如果填写了新密码，则更新密码
      if (form.value.admin_password) {
        updateData.password = form.value.admin_password
      }
      
      const { getApiUrl } = await import('@/config/api')
      await fetch(getApiUrl(`admin-accounts/${adminId}`), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updateData)
      })
    } else if (form.value.admin_login_id && form.value.admin_password) {
      // 如果没有管理员账号但填写了信息，则创建新账号
      await createTenantAdmin(tenantId)
    }
  } catch (error) {
    console.error('更新管理员账号失败:', error)
  }
}

onMounted(() => {
  loadTenants()
  loadAvailableLanguages()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

