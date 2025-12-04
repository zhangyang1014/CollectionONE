<template>
  <div class="admin-account-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小组管理员管理</span>
          <el-space>
            <el-select 
              v-model="currentAgencyId" 
              placeholder="全部机构" 
              @change="handleAgencyChange"
              style="width: 160px"
              clearable
            >
              <el-option label="全部机构" :value="undefined" />
              <el-option
                v-for="agency in agencies"
                :key="agency.id"
                :label="agency.agency_name"
                :value="agency.id"
              />
            </el-select>
            <el-select 
              v-model="currentTeamId" 
              placeholder="全部小组" 
              @change="loadAccounts"
              style="width: 160px"
              clearable
              :disabled="!currentAgencyId"
            >
              <el-option label="全部小组" :value="undefined" />
              <el-option
                v-for="team in teams"
                :key="team.id"
                :label="team.team_name"
                :value="team.id"
              />
            </el-select>
            <el-select 
              v-model="filterRole" 
              placeholder="全部角色" 
              @change="applyFilters"
              style="width: 120px"
              clearable
            >
              <el-option label="全部角色" :value="undefined" />
              <el-option label="小组长" value="team_leader" />
              <el-option label="质检员" value="quality_inspector" />
              <el-option label="统计员" value="statistician" />
            </el-select>
            <el-select 
              v-model="filterStatus" 
              placeholder="全部状态" 
              @change="applyFilters"
              style="width: 120px"
              clearable
            >
              <el-option label="全部状态" :value="undefined" />
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
            <el-button 
              type="primary" 
              @click="handleAdd" 
              :disabled="!currentTenantId"
            >
              创建账号
            </el-button>
          </el-space>
        </div>
      </template>

      <el-table :data="accounts" border style="width: 100%">
        <el-table-column prop="login_id" label="登录ID" width="120" />
        <el-table-column prop="account_name" label="账号名" width="120" />
        <el-table-column prop="tenant_name" label="所属甲方" width="130" />
        <el-table-column prop="agency_name" label="所属机构" width="130" />
        <el-table-column prop="team_name" label="所属小组" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collector_count" label="催员数" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewCollectors(row)"
              :disabled="!row.collector_count || row.collector_count === 0 || !row.team_id"
            >
              {{ row.collector_count || 0 }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="160" />
        <el-table-column prop="updated_at" label="最近修改时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button link type="warning" @click="handleResetPassword(row)" size="small">
              修改密码
            </el-button>
            <el-button 
              link 
              :type="row.is_active ? 'warning' : 'success'" 
              @click="handleToggleStatus(row)" 
              size="small"
            >
              {{ row.is_active ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" size="small">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="登录ID" prop="login_id">
          <el-input 
            v-model="form.login_id" 
            placeholder="请输入自定义部分（如：teamadmin01）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整登录ID：{{ tenantPrefix || '甲方编码' }}-{{ form.login_id || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            登录ID不可修改
          </div>
        </el-form-item>

        <el-form-item label="账号名" prop="account_name">
          <el-input v-model="form.account_name" placeholder="请输入账号名" maxlength="50" />
        </el-form-item>

        <el-form-item label="登录密码" prop="password" v-if="!isEdit">
          <el-input 
            v-model="form.password" 
            type="password"
            placeholder="请输入登录密码" 
            maxlength="50"
            show-password
          />
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select v-model="form.agency_id" placeholder="选择机构" style="width: 100%" @change="handleFormAgencyChange">
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属小组" prop="team_id">
          <el-select v-model="form.team_id" placeholder="选择小组" style="width: 100%">
            <el-option
              v-for="team in formTeams"
              :key="team.id"
              :label="team.team_name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="选择角色" style="width: 100%">
            <el-option label="小组长" value="team_leader" />
            <el-option label="质检员" value="quality_inspector" />
            <el-option label="统计员" value="statistician" />
          </el-select>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
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

        <el-form-item label="是否启用">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="新密码" prop="new_password">
          <el-input 
            v-model="passwordForm.new_password" 
            type="password"
            placeholder="请输入新密码" 
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirm_password">
          <el-input 
            v-model="passwordForm.confirm_password" 
            type="password"
            placeholder="请再次输入新密码" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePassword" :loading="savingPassword">确定</el-button>
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
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const formTeams = ref<any[]>([]) // 表单中的小组选项
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentTenant = computed(() => tenantStore.currentTenant)
const tenantPrefix = computed(() => currentTenant.value?.tenant_code || '')
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const currentTeamId = ref<number | undefined>(undefined) // 默认全选
const allAccounts = ref<any[]>([]) // 存储所有账号（用于筛选）
const accounts = ref<any[]>([]) // 显示在表格中的账号（根据筛选条件）
const filterRole = ref<string | undefined>(undefined) // 角色筛选
const filterStatus = ref<boolean | undefined>(undefined) // 状态筛选
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const savingPassword = ref(false)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const isEdit = ref(false)
const currentAccount = ref<any>(null)

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  async (newTenantId, oldTenantId) => {
    currentTenantId.value = newTenantId
    currentAgencyId.value = undefined // 重置为全选
    currentTeamId.value = undefined // 重置为全选
    filterRole.value = undefined // 重置角色筛选
    filterStatus.value = undefined // 重置状态筛选
    accounts.value = []
    allAccounts.value = []
    agencies.value = []
    teams.value = []
    
    if (newTenantId) {
      await loadAgencies()
      await loadAccounts() // 默认加载所有账号
    }
  }
)

// 初始加载
onMounted(async () => {
  if (currentTenantId.value) {
    await loadAgencies()
    await loadAccounts() // 默认加载所有账号
  }
})

const form = ref({
  id: undefined as number | undefined,
  account_name: '',
  login_id: '',
  password: '',
  agency_id: undefined as number | undefined,
  team_id: undefined as number | undefined,
  role: '',
  email: '',
  remark: '',
  is_active: true
})

const passwordForm = ref({
  new_password: '',
  confirm_password: ''
})

const rules = reactive({
  login_id: [
    { required: true, message: '请输入登录ID', trigger: 'blur' }
  ],
  account_name: [
    { required: true, message: '请输入账号名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  agency_id: [
    { required: true, message: '请选择所属机构', trigger: 'change' }
  ],
  team_id: [
    { required: true, message: '请选择所属小组', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
})

const passwordRules = reactive({
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirm_password: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        if (value !== passwordForm.value.new_password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 角色名称映射
const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    'team_leader': '小组长',
    'quality_inspector': '质检员',
    'statistician': '统计员'
  }
  return roleMap[role] || role
}

// 角色标签类型
const getRoleType = (role: string) => {
  const typeMap: Record<string, string> = {
    'team_leader': 'warning',
    'quality_inspector': 'success',
    'statistician': 'info'
  }
  return typeMap[role] || ''
}

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
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 加载小组列表
const loadTeams = async () => {
  if (!currentAgencyId.value) {
    teams.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`agencies/${currentAgencyId.value}/teams`)
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    teams.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载小组失败：', error)
    ElMessage.error('加载小组失败')
  }
}

// 机构切换时加载小组
const handleAgencyChange = async () => {
  currentTeamId.value = undefined
  accounts.value = []
  
  if (currentAgencyId.value) {
    // 选择了机构，加载该机构的小组
    await loadTeams()
    // 加载该机构的所有账号（小组为全选）
    await loadAccounts()
  } else {
    // 选择"全部机构"，清空小组列表，加载所有账号
    teams.value = []
    await loadAccounts()
  }
}

// 加载账号列表（支持筛选）
const loadAccounts = async () => {
  if (!currentTenantId.value) {
    allAccounts.value = []
    accounts.value = []
    return
  }

  try {
    let loadedAccounts: any[] = []
    
    if (!currentAgencyId.value && !currentTeamId.value) {
      // 全选：加载所有机构的账号
      await loadAgencies()
      
      for (const agency of agencies.value) {
        try {
          // 加载该机构的小组
          const { getApiUrl } = await import('@/config/api')
          const teamsUrl = getApiUrl(`agencies/${agency.id}/teams`)
          const teamsResponse = await fetch(teamsUrl)
          const teamsResult = await teamsResponse.json()
          // API直接返回数组，不是{data: [...]}格式
          const agencyTeams = Array.isArray(teamsResult) ? teamsResult : (teamsResult.data || [])
          
          // 遍历该机构的所有小组，加载账号
          for (const team of agencyTeams) {
            try {
              const accountsUrl = getApiUrl(`teams/${team.id}/admin-accounts`)
              const accountsResponse = await fetch(accountsUrl)
              const accountsResult = await accountsResponse.json()
              // API直接返回数组，不是{data: [...]}格式
              const teamAccounts = Array.isArray(accountsResult) ? accountsResult : (accountsResult.data || [])
              
              // 为每个账号添加机构、小组信息和催员数量
              teamAccounts.forEach((account: any) => {
                account.agency_name = agency.agency_name
                account.team_name = team.team_name
                account.collector_count = team.collector_count || 0
              })
              
              loadedAccounts.push(...teamAccounts)
            } catch (error) {
              console.error(`加载小组 ${team.id} 的账号失败：`, error)
            }
          }
        } catch (error) {
          console.error(`加载机构 ${agency.id} 的小组失败：`, error)
        }
      }
    } else if (currentAgencyId.value && !currentTeamId.value) {
      // 只选择了机构：加载该机构所有小组的账号
      await loadTeams()
      
      for (const team of teams.value) {
        try {
          const accountsUrl = `http://getApiUrl/api/v1/teams/${team.id}/admin-accounts`
          const accountsResponse = await fetch(accountsUrl)
          const accountsResult = await accountsResponse.json()
          // API直接返回数组，不是{data: [...]}格式
          const teamAccounts = Array.isArray(accountsResult) ? accountsResult : (accountsResult.data || [])
          
          // 为每个账号添加机构、小组信息和催员数量
          const agency = agencies.value.find(a => a.id === currentAgencyId.value)
          teamAccounts.forEach((account: any) => {
            account.agency_name = agency?.agency_name || ''
            account.team_name = team.team_name
            account.collector_count = team.collector_count || 0
          })
          
          loadedAccounts.push(...teamAccounts)
        } catch (error) {
          console.error(`加载小组 ${team.id} 的账号失败：`, error)
        }
      }
    } else if (currentAgencyId.value && currentTeamId.value) {
      // 选择了机构和小组：加载该小组的账号
      const { getApiUrl } = await import('@/config/api')
      const accountsUrl = getApiUrl(`teams/${currentTeamId.value}/admin-accounts`)
      const accountsResponse = await fetch(accountsUrl)
      const accountsResult = await accountsResponse.json()
      // API直接返回数组，不是{data: [...]}格式
      const teamAccounts = Array.isArray(accountsResult) ? accountsResult : (accountsResult.data || [])
      
      // 为每个账号添加机构、小组信息和催员数量
      const agency = agencies.value.find(a => a.id === currentAgencyId.value)
      const team = teams.value.find(t => t.id === currentTeamId.value)
      teamAccounts.forEach((account: any) => {
        account.agency_name = agency?.agency_name || ''
        account.team_name = team?.team_name || ''
        account.collector_count = team?.collector_count || 0
      })
      
      loadedAccounts = teamAccounts
    }
    
    // 存储到 allAccounts
    allAccounts.value = loadedAccounts
    console.log(`已加载 ${allAccounts.value.length} 个小组管理员`)
    
    // 应用筛选
    applyFilters()
  } catch (error) {
    console.error('加载账号失败：', error)
    ElMessage.error('加载账号失败')
  }
}

// 应用筛选条件
const applyFilters = () => {
  let filtered = [...allAccounts.value]
  
  // 角色筛选
  if (filterRole.value !== undefined) {
    filtered = filtered.filter(account => account.role === filterRole.value)
  }
  
  // 状态筛选
  if (filterStatus.value !== undefined) {
    filtered = filtered.filter(account => account.is_active === filterStatus.value)
  }
  
  accounts.value = filtered
  console.log(`筛选后显示 ${accounts.value.length} 个账号`)
}

// 加载表单中的小组列表
const loadFormTeams = async (agencyId: number) => {
  if (!agencyId) {
    formTeams.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`agencies/${agencyId}/teams`)
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    formTeams.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载小组失败：', error)
  }
}

// 表单中机构切换时加载小组
const handleFormAgencyChange = async () => {
  form.value.team_id = undefined
  await loadFormTeams(form.value.agency_id!)
}

// 创建账号
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建账号'
  form.value = {
    id: undefined,
    account_name: '',
    login_id: '',
    password: '',
    agency_id: undefined,
    team_id: undefined,
    role: '',
    email: '',
    remark: '',
    is_active: true
  }
  dialogVisible.value = true
}

// 查看催员（跳转到催员管理页面，筛选该管理员所属小组的催员）
const handleViewCollectors = (row: any) => {
  if (!row.team_id) {
    ElMessage.warning('该账号未关联小组')
    return
  }
  
  router.push({
    path: '/organization/collectors',
    query: {
      agencyId: row.agency_id,
      teamId: row.team_id
    }
  })
}

// 编辑账号
const handleEdit = async (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑账号'
  form.value = {
    id: row.id,
    account_name: row.account_name,
    login_id: row.login_id,
    password: '',
    agency_id: row.agency_id,
    team_id: row.team_id,
    role: row.role,
    email: row.email || '',
    remark: row.remark || '',
    is_active: row.is_active
  }
  await loadFormTeams(row.agency_id)
  dialogVisible.value = true
}

// 保存账号
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    // 在创建模式下，需要拼接前缀和用户输入的部分
    const submitData = { ...form.value }
    if (!isEdit.value && tenantPrefix.value) {
      submitData.login_id = tenantPrefix.value + '-' + form.value.login_id
    }

    console.log('保存账号：', submitData)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadAccounts() // 重新加载并应用筛选
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleResetPassword = (row: any) => {
  currentAccount.value = row
  passwordForm.value = {
    new_password: '',
    confirm_password: ''
  }
  passwordDialogVisible.value = true
}

// 保存密码
const handleSavePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    savingPassword.value = true

    console.log('修改密码：', {
      account_id: currentAccount.value.id,
      new_password: passwordForm.value.new_password
    })
    
    // TODO: 调用API保存密码
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    console.error('修改密码失败：', error)
  } finally {
    savingPassword.value = false
  }
}

// 启用/禁用账号
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}账号"${row.account_name}"吗？${row.is_active ? '禁用后该账号将无法登录系统。' : ''}`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API更新状态
    row.is_active = !row.is_active
    // 同步更新 allAccounts 中的数据
    const accountInAll = allAccounts.value.find(a => a.id === row.id)
    if (accountInAll) {
      accountInAll.is_active = row.is_active
    }
    // 重新应用筛选
    applyFilters()
    ElMessage.success(`${action}成功`)
  } catch (error) {
    // 用户取消
  }
}

// 删除账号
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除账号"${row.account_name}"吗？删除后将无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API删除
    ElMessage.success('删除成功')
    await loadAccounts() // 重新加载并应用筛选
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

