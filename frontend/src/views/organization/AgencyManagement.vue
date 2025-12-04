<template>
  <div class="agency-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>机构管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd" 
            :disabled="!currentTenantId"
          >
            创建机构
          </el-button>
        </div>
      </template>

      <el-table :data="agencies" border style="width: 100%">
        <el-table-column prop="agency_code" label="机构ID" width="120" />
        <el-table-column prop="agency_name" label="机构名称" width="200" />
        <el-table-column prop="agency_type" label="机构类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.agency_type === 'real' ? 'success' : 'info'">
              {{ row.agency_type === 'real' ? '真实机构' : '虚拟机构' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tenant_name" label="所属甲方" width="150" />
        <el-table-column prop="admin_name" label="机构管理员" width="120" />
        <el-table-column prop="team_group_count" label="小组群数量" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewTeamGroups(row)"
              :disabled="!row.team_group_count || row.team_group_count === 0"
            >
              {{ row.team_group_count || 0 }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="team_count" label="小组数量" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewTeams(row)"
              :disabled="!row.team_count || row.team_count === 0"
            >
              {{ row.team_count || 0 }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="collector_count" label="催员数" width="100" align="center">
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
        <el-table-column prop="created_at" label="创建时间" width="180" />
        <el-table-column prop="updated_at" label="最近修改时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button link type="success" @click="handleWorkingHours(row)" size="small">
              作息时间管理
            </el-button>
            <el-button 
              link 
              :type="row.is_active ? 'warning' : 'success'" 
              @click="handleToggleStatus(row)" 
              size="small"
            >
              {{ row.is_active ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="140px" :rules="rules" ref="formRef">
        <el-divider content-position="left">机构基础信息</el-divider>
        
        <el-form-item label="机构编码" prop="agency_code">
          <el-input 
            v-model="form.agency_code" 
            placeholder="请输入自定义部分（如：AG001）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整编码：{{ tenantPrefix || '甲方编码' }}-{{ form.agency_code || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            机构编码不可修改
          </div>
        </el-form-item>

        <el-form-item label="机构名称" prop="agency_name">
          <el-input v-model="form.agency_name" placeholder="请输入机构名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="机构类型" prop="agency_type">
          <el-radio-group v-model="form.agency_type">
            <el-radio value="real">真实机构</el-radio>
            <el-radio value="virtual">虚拟机构</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="时区" prop="timezone">
          <el-input-number 
            v-model="form.timezone" 
            :min="-12" 
            :max="14" 
            :precision="0"
            placeholder="请输入UTC偏移量（如：8表示UTC+8）"
            style="width: 100%"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            范围：-12 到 +14（UTC偏移量，如：8表示UTC+8，-5表示UTC-5）
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
          />
        </el-form-item>

        <el-divider content-position="left">机构管理员账号</el-divider>

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
            placeholder="请输入自定义部分（如：agadmin01）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整登录ID：{{ tenantPrefix || '甲方编码' }}-{{ form.admin_login_id || '自定义部分' }}
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

        <el-form-item label="是否启用" v-if="isEdit">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'

const router = useRouter()

const tenantStore = useTenantStore()
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentTenant = computed(() => tenantStore.currentTenant)
const tenantPrefix = computed(() => currentTenant.value?.tenant_code || '')
const agencies = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  (newTenantId) => {
    currentTenantId.value = newTenantId
    loadAgencies()
  }
)

// 初始加载
onMounted(() => {
  loadAgencies()
})

const form = ref({
  id: undefined as number | undefined,
  agency_code: '',
  agency_name: '',
  agency_type: 'real',
  timezone: undefined as number | undefined,
  remark: '',
  admin_name: '',
  admin_login_id: '',
  admin_email: '',
  admin_password: '',
  admin_password_confirm: '',
  is_active: true
})


// 密码确认验证器
const validatePasswordConfirm = (_rule: any, value: any, callback: any) => {
  if (value !== form.value.admin_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  agency_code: [
    { required: true, message: '请输入机构编码', trigger: 'blur' }
  ],
  agency_name: [
    { required: true, message: '请输入机构名称', trigger: 'blur' }
  ],
  agency_type: [
    { required: true, message: '请选择机构类型', trigger: 'change' }
  ],
  timezone: [
    { required: true, message: '请输入时区', trigger: 'blur' },
    { type: 'number', message: '时区必须是数字', trigger: 'blur' }
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
})

// 加载机构列表
const loadAgencies = async () => {
  if (!currentTenantId.value) {
    agencies.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`tenants/${currentTenantId.value}/agencies`)
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    agencies.value = Array.isArray(result) ? result : (result.data || [])
    console.log(`已加载 ${agencies.value.length} 个机构`)
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 创建机构
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建机构'
  form.value = {
    id: undefined,
    agency_code: '',
    agency_name: '',
    agency_type: 'real',
    timezone: undefined,
    remark: '',
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: '',
    is_active: true
  }
  dialogVisible.value = true
}

// 编辑机构
const handleEdit = async (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑机构'
  
  // 先设置基础信息
  form.value = {
    id: row.id,
    agency_code: row.agency_code,
    agency_name: row.agency_name,
    agency_type: row.agency_type || 'real',
    timezone: row.timezone !== null && row.timezone !== undefined ? Number(row.timezone) : undefined,
    remark: row.remark || '',
    admin_name: '',
    admin_login_id: '',
    admin_email: '',
    admin_password: '',
    admin_password_confirm: '',
    is_active: row.is_active
  }
  
  // 获取该机构的管理员账号信息并回显
  try {
    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl(`agencies/${row.id}/admin-accounts`))
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

// 作息时间管理
const handleWorkingHours = (row: any) => {
  router.push(`/organization/agencies/${row.id}/working-hours`)
}

// 查看小组群
const handleViewTeamGroups = (row: any) => {
  router.push({
    path: '/organization/team-groups',
    query: {
      agencyId: row.id
    }
  })
}

// 查看小组
const handleViewTeams = (row: any) => {
  router.push({
    path: '/organization/teams',
    query: {
      agencyId: row.id
    }
  })
}

// 查看催员
const handleViewCollectors = (row: any) => {
  router.push({
    path: '/organization/collectors',
    query: {
      agencyId: row.id
    }
  })
}

// 保存机构
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    // 在创建模式下，需要拼接前缀和用户输入的部分
    const submitData = { ...form.value }
    if (!isEdit.value && tenantPrefix.value) {
      submitData.agency_code = tenantPrefix.value + '-' + form.value.agency_code
      submitData.admin_login_id = tenantPrefix.value + '-' + form.value.admin_login_id
    }

    console.log('保存机构：', submitData)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadAgencies()
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 启用/禁用机构
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}机构"${row.agency_name}"吗？${row.is_active ? '禁用后该机构下的所有小组和催员将无法工作。' : ''}`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API更新状态
    row.is_active = !row.is_active
    ElMessage.success(`${action}成功`)
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

