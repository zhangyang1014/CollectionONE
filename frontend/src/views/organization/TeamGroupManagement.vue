<template>
  <div class="team-group-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小组群管理</span>
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
            <el-button 
              type="primary" 
              @click="handleAdd" 
              :disabled="!currentTenantId"
            >
              创建小组群
            </el-button>
          </el-space>
        </div>
      </template>

      <el-table :data="teamGroups" border style="width: 100%">
        <el-table-column prop="group_code" label="小组群ID" width="120" />
        <el-table-column prop="group_name" label="小组群名称" width="180" />
        <el-table-column prop="agency_name" label="所属机构" width="150" />
        <el-table-column prop="spv_account_name" label="小组群长SPV" width="150" />
        <el-table-column prop="spv_login_id" label="SPV登录ID" width="150" />
        <el-table-column prop="team_count" label="小组数量" width="100" align="center" />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
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
        <el-divider content-position="left">小组群基础信息</el-divider>
        
        <el-form-item label="小组群编码" prop="group_code">
          <el-input 
            v-model="form.group_code" 
            placeholder="请输入自定义部分（如：GP001）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整编码：{{ tenantPrefix || '甲方编码' }}-{{ form.group_code || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            小组群编码不可修改
          </div>
        </el-form-item>

        <el-form-item label="小组群名称" prop="group_name">
          <el-input v-model="form.group_name" placeholder="请输入小组群名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select v-model="form.agency_id" placeholder="选择机构" style="width: 100%" :disabled="isEdit">
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
          />
        </el-form-item>

        <el-divider content-position="left">小组群长SPV账号</el-divider>

        <el-form-item label="SPV账号名称" prop="spv_account_name">
          <el-input 
            v-model="form.spv_account_name" 
            placeholder="请输入SPV账号名称" 
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="SPV登录ID" prop="spv_login_id">
          <el-input 
            v-model="form.spv_login_id" 
            placeholder="请输入自定义部分（如：spv01）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整登录ID：{{ tenantPrefix || '甲方编码' }}-{{ form.spv_login_id || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            登录ID不可修改
          </div>
        </el-form-item>

        <el-form-item label="SPV邮箱" prop="spv_email">
          <el-input v-model="form.spv_email" placeholder="请输入邮箱地址" maxlength="100" />
        </el-form-item>

        <el-form-item label="SPV密码" prop="spv_password">
          <el-input 
            v-model="form.spv_password" 
            type="password" 
            :placeholder="isEdit ? '如需修改密码请输入新密码，否则留空' : '请输入初始密码'" 
            maxlength="50"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="spv_password_confirm" v-if="!isEdit || form.spv_password">
          <el-input 
            v-model="form.spv_password_confirm" 
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
import { useRouter, useRoute } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'

const router = useRouter()
const route = useRoute()
const tenantStore = useTenantStore()
const agencies = ref<any[]>([])
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentTenant = computed(() => tenantStore.currentTenant)
const tenantPrefix = computed(() => currentTenant.value?.tenant_code || '')
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const teamGroups = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  async (newTenantId) => {
    currentTenantId.value = newTenantId
    currentAgencyId.value = undefined // 重置为全选
    teamGroups.value = []
    agencies.value = []
    
    if (newTenantId) {
      await loadAgencies()
      await loadTeamGroups()
    }
  }
)


// 初始加载
onMounted(async () => {
  if (currentTenantId.value) {
    await loadAgencies()
    
    // 从URL参数中获取agencyId并设置筛选
    const agencyIdParam = route.query.agencyId
    if (agencyIdParam) {
      currentAgencyId.value = Number(agencyIdParam)
    }
    
    await loadTeamGroups()
  }
})

const form = ref({
  id: undefined as number | undefined,
  group_code: '',
  group_name: '',
  agency_id: undefined as number | undefined,
  description: '',
  is_active: true,
  // SPV账号信息
  spv_account_name: '',
  spv_login_id: '',
  spv_email: '',
  spv_password: '',
  spv_password_confirm: ''
})

// 密码确认验证器
const validatePasswordConfirm = (_rule: any, value: any, callback: any) => {
  if (value !== form.value.spv_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  group_code: [
    { required: true, message: '请输入小组群编码', trigger: 'blur' }
  ],
  group_name: [
    { required: true, message: '请输入小组群名称', trigger: 'blur' }
  ],
  agency_id: [
    { required: true, message: '请选择所属机构', trigger: 'change' }
  ],
  spv_account_name: [
    { required: true, message: '请输入SPV账号名称', trigger: 'blur' }
  ],
  spv_login_id: [
    { required: true, message: '请输入SPV登录ID', trigger: 'blur' }
  ],
  spv_email: [
    { required: true, message: '请输入SPV邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  spv_password: [
    { required: true, message: '请输入SPV密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  spv_password_confirm: [
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
    const url = `${getApiUrl('agencies')}?tenant_id=${currentTenantId.value}`
    const response = await fetch(url)
    const result = await response.json()
    
    agencies.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 已移除队列和催员加载逻辑，SPV现在直接创建账号

// 加载小组群列表
const loadTeamGroups = async () => {
  if (!currentTenantId.value) {
    teamGroups.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    let url = `${getApiUrl('team-groups')}?tenant_id=${currentTenantId.value}`
    
    if (currentAgencyId.value) {
      url += `&agency_id=${currentAgencyId.value}`
    }
    
    const response = await fetch(url)
    const result = await response.json()
    
    teamGroups.value = Array.isArray(result) ? result : (result.data || [])
    console.log(`已加载 ${teamGroups.value.length} 个小组群`)
  } catch (error) {
    console.error('加载小组群失败：', error)
    ElMessage.error('加载小组群失败')
  }
}

// 机构切换时
const handleAgencyChange = async () => {
  await loadTeamGroups()
}

// 创建小组群
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建小组群'
  form.value = {
    id: undefined,
    group_code: '',
    group_name: '',
    agency_id: currentAgencyId.value,
    description: '',
    is_active: true,
    spv_account_name: '',
    spv_login_id: '',
    spv_email: '',
    spv_password: '',
    spv_password_confirm: ''
  }
  dialogVisible.value = true
}

// 查看催员（跳转到催员管理页面，筛选该小组群所属机构的催员）
const handleViewCollectors = (row: any) => {
  router.push({
    path: '/organization/collectors',
    query: {
      agencyId: row.agency_id
    }
  })
}

// 编辑小组群
const handleEdit = async (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑小组群'
  
  // 先设置基础信息
  form.value = {
    id: row.id,
    group_code: row.group_code,
    group_name: row.group_name,
    agency_id: row.agency_id,
    description: row.description || '',
    is_active: row.is_active,
    spv_account_name: '',
    spv_login_id: '',
    spv_email: '',
    spv_password: '',
    spv_password_confirm: ''
  }
  
  // 获取小组群详情（包含SPV账号信息）并回显
  try {
    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl(`team-groups/${row.id}`))
    const teamGroupDetail = await response.json()
    
    // 如果存在SPV账号信息，回显
    if (teamGroupDetail) {
      form.value.spv_account_name = teamGroupDetail.spv_account_name || ''
      form.value.spv_login_id = teamGroupDetail.spv_login_id || ''
      form.value.spv_email = teamGroupDetail.spv_email || ''
    }
  } catch (error) {
    console.error('获取小组群详情失败：', error)
  }
  
  dialogVisible.value = true
}

// 保存小组群
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    const { getApiUrl } = await import('@/config/api')
    const url = isEdit.value 
      ? getApiUrl(`team-groups/${form.value.id}`)
      : getApiUrl('team-groups')
    
    const method = isEdit.value ? 'PUT' : 'POST'
    
    const payload = isEdit.value ? {
      group_name: form.value.group_name,
      description: form.value.description,
      is_active: form.value.is_active
    } : {
      tenant_id: currentTenantId.value,
      agency_id: form.value.agency_id,
      group_code: tenantPrefix.value ? tenantPrefix.value + '-' + form.value.group_code : form.value.group_code,
      group_name: form.value.group_name,
      description: form.value.description,
      is_active: form.value.is_active,
      // SPV管理员账号信息
      spv_account_name: form.value.spv_account_name,
      spv_login_id: tenantPrefix.value ? tenantPrefix.value + '-' + form.value.spv_login_id : form.value.spv_login_id,
      spv_email: form.value.spv_email,
      spv_password: form.value.spv_password,
      spv_remark: ''
    }

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.detail || '保存失败')
    }

    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadTeamGroups()
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 启用/禁用小组群
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}小组群"${row.group_name}"吗？${row.is_active ? '禁用后该小组群下的所有小组和催员将无法工作。' : ''}`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const { getApiUrl } = await import('@/config/api')
    const response = await fetch(getApiUrl(`team-groups/${row.id}`), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        is_active: !row.is_active
      })
    })

    if (!response.ok) {
      throw new Error('更新状态失败')
    }

    ElMessage.success(`${action}成功`)
    await loadTeamGroups()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('更新状态失败：', error)
      ElMessage.error('更新状态失败')
    }
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

