<template>
  <div class="collector-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>催员管理</span>
        </div>
      </template>

      <el-container class="org-container">
        <!-- 左侧：树状图 -->
        <el-aside :width="asideWidth" class="tree-aside">
          <div class="tree-header">
            <span class="tree-title">组织架构</span>
            <el-space>
              <el-button 
                size="small" 
                text 
                @click="showTreeDiagnostic"
              >
                诊断
              </el-button>
              <el-button 
                size="small" 
                text 
                @click="refreshTree"
                :loading="treeLoading"
              >
                刷新
              </el-button>
            </el-space>
          </div>
          <div class="tree-container">
            <!-- 提示信息 -->
            <el-alert
              v-if="treeData.length === 0 && !treeLoading"
              title="暂无组织架构数据"
              type="info"
              :closable="false"
              style="margin-bottom: 12px;"
            >
              <template #default>
                <div style="font-size: 13px;">
                  请先创建机构、小组群、小组和催员。<br />
                  如果已创建但未显示，请按F12查看控制台日志。
                </div>
              </template>
            </el-alert>

            <el-tree
              v-if="treeData.length > 0"
              ref="treeRef"
              :data="treeData"
              :props="treeProps"
              node-key="id"
              :expand-on-click-node="false"
              default-expand-all
              :highlight-current="true"
              @node-click="handleTreeNodeClick"
              class="org-tree"
            >
              <template #default="{ node, data }">
                <div class="custom-tree-node">
                  <span class="node-label">
                    <el-icon v-if="data.type === 'agency'"><OfficeBuilding /></el-icon>
                    <el-icon v-if="data.type === 'team_group'"><Grid /></el-icon>
                    <el-icon v-if="data.type === 'team'"><User /></el-icon>
                    <el-icon v-if="data.type === 'collector'"><Avatar /></el-icon>
                    {{ node.label }}
                    <el-tag v-if="data.type === 'team' && data.data.collector_count !== undefined" size="small" type="info" style="margin-left: 8px;">
                      {{ data.data.collector_count }}人
                    </el-tag>
                  </span>
                  <span class="node-actions" v-if="data.type === 'team'">
                    <el-button 
                      size="small" 
                      text 
                      type="primary"
                      @click.stop="handleCreateCollectorFromTree(data)"
                    >
                      创建催员
                    </el-button>
                  </span>
                </div>
              </template>
            </el-tree>
          </div>
        </el-aside>

        <!-- 拖动分隔条 -->
        <div 
          class="resize-handle"
          @mousedown="startResize"
        ></div>

        <!-- 右侧：筛选器和表格 -->
        <el-main class="table-main">
          <div class="filter-bar">
            <el-space>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索催员登录id或催员名"
              style="width: 240px"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select 
              v-model="statusFilter" 
              placeholder="全部状态" 
              style="width: 130px"
            >
              <el-option label="全部" :value="undefined" />
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
            <el-select 
              v-model="currentAgencyId" 
              placeholder="全部机构" 
              @change="handleAgencyChange"
              style="width: 160px"
              clearable
              v-if="showAgencySelector"
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
              @change="handleTeamChange"
              style="width: 160px"
              clearable
              :disabled="!currentAgencyId"
              v-if="showTeamSelector"
            >
              <el-option label="全部小组" :value="undefined" />
              <el-option
                v-for="team in teams"
                :key="team.id"
                :label="team.team_name"
                :value="team.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              @click="handleAdd" 
              :disabled="!currentTenantId"
            >
              创建催员
            </el-button>
            <el-button 
              type="success" 
              @click="handleExportAccounts"
              :disabled="!currentTenantId"
            >
              导出催员账号与密码
            </el-button>
            </el-space>
          </div>

          <!-- 催员统计信息 -->
          <div class="collector-stats">
            <el-text type="info">
              当前筛选条件下，共有 <el-text type="primary" style="font-weight: 600;">{{ filteredCollectors.length }}</el-text> 位催员
            </el-text>
          </div>

          <el-table :data="filteredCollectors" border style="width: 100%">
        <el-table-column prop="collector_code" label="催员登录id" width="120" fixed="left" />
        <el-table-column prop="collector_name" label="催员名" width="120" />
        <el-table-column prop="last_login_at" label="最近登录时间" width="140">
          <template #default="{ row }">
            <div v-if="row.last_login_at" class="login-time-cell">
              <div class="date-line">{{ formatDate(row.last_login_at) }}</div>
              <div class="time-line">{{ formatTime(row.last_login_at) }}</div>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="tenant_name" label="所属甲方" width="130" />
        <el-table-column prop="agency_name" label="所属机构" width="130" />
        <el-table-column prop="team_name" label="所属小组" width="130" />
        <el-table-column prop="created_at" label="创建时间" width="160" />
        <el-table-column prop="updated_at" label="最近修改时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="460">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button link type="warning" @click="handleResetPassword(row)" size="small">
              修改密码
            </el-button>
            <el-button link type="info" @click="handleViewLoginFaces(row)" size="small">
              登录人脸查询
            </el-button>
            <el-button link type="success" @click="handleViewIm(row)" size="small">
              查看IM端
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
        </el-main>
      </el-container>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-divider content-position="left">基础信息</el-divider>
        
        <el-form-item label="催员登录id" prop="collector_code">
          <el-input 
            v-model="form.collector_code" 
            placeholder="请输入自定义部分（如：col001）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整登录ID：{{ tenantPrefix || '甲方编码' }}-{{ form.collector_code || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            催员登录ID不可修改
          </div>
        </el-form-item>

        <el-form-item label="催员姓名" prop="collector_name">
          <el-input v-model="form.collector_name" placeholder="请输入催员姓名" maxlength="50" />
        </el-form-item>

        <el-form-item label="登录密码" prop="password" v-if="!isEdit">
          <div style="display: flex; gap: 8px;">
          <el-input 
            v-model="form.password" 
            type="password"
              placeholder="请输入登录密码或点击生成" 
            maxlength="50"
            show-password
              style="flex: 1;"
          />
            <el-button @click="generatePassword" type="info" plain>生成密码</el-button>
          </div>
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select 
            v-model="form.agency_id" 
            placeholder="选择机构" 
            style="width: 100%" 
            @change="handleFormAgencyChange"
            :disabled="fromTree"
          >
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
          <div v-if="fromTree" style="margin-top: 5px; color: #909399; font-size: 12px;">
            从树状图创建，机构已自动选择
          </div>
        </el-form-item>

        <el-form-item label="所属小组" prop="team_id">
          <el-select 
            v-model="form.team_id" 
            placeholder="选择小组" 
            style="width: 100%"
            :disabled="fromTree"
          >
            <el-option
              v-for="team in formTeams"
              :key="team.id"
              :label="team.team_name"
              :value="team.id"
            />
          </el-select>
          <div v-if="fromTree" style="margin-top: 5px; color: #909399; font-size: 12px;">
            从树状图创建，小组已自动选择
          </div>
        </el-form-item>

        <el-divider content-position="left">账号信息</el-divider>

        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱，用于接收系统通知" 
            maxlength="100"
            type="email"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            邮箱用于接收密码重置、账号状态变更等系统通知
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息，如工作职责、特长、注意事项等"
            maxlength="500"
            show-word-limit
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            可记录催员的工作职责、擅长领域、特殊注意事项等信息
          </div>
        </el-form-item>

        <el-divider content-position="left">状态配置</el-divider>

        <el-form-item label="是否启用">
          <el-switch v-model="form.is_active" />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            {{ form.is_active ? '启用状态：催员可正常登录系统' : '禁用状态：催员无法登录系统' }}
          </div>
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
import { Search, OfficeBuilding, Grid, User, Avatar } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import type { OrgTreeNode } from '@/types/organization'
// import { getCollectorLoginFaceRecords } from '@/api/organization' // TODO: 后续替换Mock数据时使用

const route = useRoute()
const router = useRouter()

const tenantStore = useTenantStore()
const userStore = useUserStore()
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const formTeams = ref<any[]>([]) // 表单中的小组选项
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentTenant = computed(() => tenantStore.currentTenant)
const tenantPrefix = computed(() => currentTenant.value?.tenant_code || '')
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const currentTeamId = ref<number | undefined>(undefined) // 默认全选
const collectors = ref<any[]>([])
const searchKeyword = ref('') // 搜索关键词
const statusFilter = ref<boolean | undefined>(true) // 状态筛选：true=启用, false=禁用, undefined=全部
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const savingPassword = ref(false)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const isEdit = ref(false)
const currentCollector = ref<any>(null)

// 树状图相关
const treeRef = ref()
const treeData = ref<OrgTreeNode[]>([])
const treeLoading = ref(false)
const treeProps = {
  children: 'children',
  label: 'label'
}
const fromTree = ref(false) // 标记是否从树创建

// 可调整宽度
const asideWidth = ref('22%')
const isResizing = ref(false)
const startX = ref(0)
const startWidth = ref(0)

// 获取当前用户信息
const currentUser = computed(() => userStore.userInfo)

// 检查用户角色
const userRole = computed(() => {
  return currentUser.value?.role || ''
})

// 获取用户所属机构ID
const userAgencyId = computed(() => {
  return currentUser.value?.agency_id || currentUser.value?.agencyId || null
})

// 获取用户所属小组ID
const userTeamId = computed(() => {
  return currentUser.value?.team_id || currentUser.value?.teamId || null
})

// 是否显示机构选择器（机构管理员不显示）
const showAgencySelector = computed(() => {
  // 如果是机构管理员，不显示机构选择器
  if (userRole.value === 'agency_admin' || userRole.value === 'AgencyAdmin') {
    return false
  }
  return true
})

// 是否显示小组选择器（小组管理员不显示）
const showTeamSelector = computed(() => {
  // 如果是小组管理员，不显示小组选择器
  if (userRole.value === 'team_admin' || userRole.value === 'TeamAdmin' || 
      userRole.value === 'team_leader' || userRole.value === 'TeamLeader') {
    return false
  }
  return true
})

// 过滤后的催员列表（根据搜索关键词和状态筛选）
const filteredCollectors = computed(() => {
  let result = collectors.value
  
  // 状态筛选
  if (statusFilter.value !== undefined) {
    result = result.filter((collector) => collector.is_active === statusFilter.value)
  }
  
  // 关键词搜索
  if (searchKeyword.value && searchKeyword.value.trim() !== '') {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter((collector) => {
      // 搜索催员登录id和催员名
      const collectorCode = (collector.collector_code || '').toLowerCase()
      const collectorName = (collector.collector_name || '').toLowerCase()
      
      return collectorCode.includes(keyword) || collectorName.includes(keyword)
    })
  }
  
  return result
})

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  async (newTenantId, _oldTenantId) => {
    currentTenantId.value = newTenantId
    currentAgencyId.value = undefined // 重置为全选
    currentTeamId.value = undefined // 重置为全选
    searchKeyword.value = '' // 清空搜索关键词
    statusFilter.value = true // 重置为启用状态
    collectors.value = []
    agencies.value = []
    teams.value = []
    treeData.value = []
    
    if (newTenantId) {
      await loadAgencies()
      await buildTreeData() // 构建树状图
      await loadCollectors() // 默认加载所有催员
    }
  }
)

// 构建树状图数据
const buildTreeData = async () => {
  if (!currentTenantId.value) {
    treeData.value = []
    return
  }

  treeLoading.value = true
  try {
    const treeNodes: OrgTreeNode[] = []
    const { getApiUrl } = await import('@/config/api')

    console.log('开始构建树状图，机构数量：', agencies.value.length)

    // 第一层：机构
    for (const agency of agencies.value) {
      const agencyNode: OrgTreeNode = {
        id: `agency-${agency.id}`,
        label: agency.agency_name,
        type: 'agency',
        data: agency,
        children: []
      }

      try {
        // 第二层：小组群（使用正确的API格式）
        const teamGroupsUrl = `${getApiUrl('team-groups')}?tenant_id=${currentTenantId.value}&agency_id=${agency.id}`
        console.log('加载小组群：', teamGroupsUrl)
        const teamGroupsResponse = await fetch(teamGroupsUrl)
        const teamGroupsResult = await teamGroupsResponse.json()
        const teamGroups = Array.isArray(teamGroupsResult) ? teamGroupsResult : (teamGroupsResult.data || [])
        
        console.log(`机构 ${agency.agency_name} 的小组群数量：`, teamGroups.length)

        if (teamGroups.length > 0) {
          // 如果有小组群，按小组群分组
          for (const teamGroup of teamGroups) {
            const teamGroupNode: OrgTreeNode = {
              id: `team-group-${teamGroup.id}`,
              label: teamGroup.group_name,
              type: 'team_group',
              data: teamGroup,
              children: []
            }

            // 第三层：小组（属于该小组群）
            const teamsUrl = getApiUrl(`agencies/${agency.id}/teams`)
            console.log('加载小组：', teamsUrl)
            const teamsResponse = await fetch(teamsUrl)
            const teamsResult = await teamsResponse.json()
            const allTeams = Array.isArray(teamsResult) ? teamsResult : (teamsResult.data || [])
            const teams = allTeams.filter((t: any) => t.team_group_id === teamGroup.id)
            
            console.log(`小组群 ${teamGroup.group_name} 的小组数量：`, teams.length)

            for (const team of teams) {
              const teamNode: OrgTreeNode = {
                id: `team-${team.id}`,
                label: team.team_name,
                type: 'team',
                data: team,
                children: []
              }

              // 第四层：催员
              try {
                const collectorsUrl = getApiUrl(`teams/${team.id}/collectors`)
                const collectorsResponse = await fetch(collectorsUrl)
                const collectorsResult = await collectorsResponse.json()
                const collectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])

                console.log(`小组 ${team.team_name} 的催员数量：`, collectors.length)

                for (const collector of collectors) {
                  teamNode.children!.push({
                    id: `collector-${collector.id}`,
                    label: collector.collector_name,
                    type: 'collector',
                    data: collector
                  })
                }
              } catch (error) {
                console.error(`加载小组 ${team.id} 的催员失败：`, error)
              }

              teamGroupNode.children!.push(teamNode)
            }

            agencyNode.children!.push(teamGroupNode)
          }
        } else {
          console.log(`机构 ${agency.agency_name} 没有小组群，直接加载小组`)
        }

        // 处理没有小组群的小组（team_group_id 为空或null）
        const teamsUrl = getApiUrl(`agencies/${agency.id}/teams`)
        console.log('加载无小组群的小组：', teamsUrl)
        const teamsResponse = await fetch(teamsUrl)
        const teamsResult = await teamsResponse.json()
        const allTeams = Array.isArray(teamsResult) ? teamsResult : (teamsResult.data || [])
        const teamsWithoutGroup = allTeams.filter((t: any) => !t.team_group_id)

        console.log(`机构 ${agency.agency_name} 无小组群的小组数量：`, teamsWithoutGroup.length)

        for (const team of teamsWithoutGroup) {
          const teamNode: OrgTreeNode = {
            id: `team-${team.id}`,
            label: team.team_name,
            type: 'team',
            data: team,
            children: []
          }

          // 第四层：催员
          try {
            const collectorsUrl = getApiUrl(`teams/${team.id}/collectors`)
            const collectorsResponse = await fetch(collectorsUrl)
            const collectorsResult = await collectorsResponse.json()
            const collectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])

            console.log(`小组 ${team.team_name} 的催员数量：`, collectors.length)

            for (const collector of collectors) {
              teamNode.children!.push({
                id: `collector-${collector.id}`,
                label: collector.collector_name,
                type: 'collector',
                data: collector
              })
            }
          } catch (error) {
            console.error(`加载小组 ${team.id} 的催员失败：`, error)
          }

          agencyNode.children!.push(teamNode)
        }
      } catch (error) {
        console.error(`加载机构 ${agency.id} 的小组群/小组失败：`, error)
        ElMessage.error(`加载机构"${agency.agency_name}"的数据失败`)
      }

      treeNodes.push(agencyNode)
      console.log(`机构 ${agency.agency_name} 的子节点数量：`, agencyNode.children?.length)
    }

    treeData.value = treeNodes
    console.log('树状图构建完成，根节点数量：', treeNodes.length)
    console.log('树状图数据：', JSON.stringify(treeData.value, null, 2))
  } catch (error) {
    console.error('构建树状图失败：', error)
    ElMessage.error('加载组织架构失败')
  } finally {
    treeLoading.value = false
  }
}

// 刷新树状图
const refreshTree = async () => {
  await loadAgencies()
  await buildTreeData()
}

// 树状图诊断
const showTreeDiagnostic = () => {
  const diagnostic = {
    '机构数量': agencies.value.length,
    '树根节点数': treeData.value.length,
    '机构详情': agencies.value.map(a => ({
      机构名称: a.agency_name,
      机构ID: a.id
    })),
    '树数据详情': treeData.value.map(node => ({
      节点: node.label,
      类型: node.type,
      子节点数: node.children?.length || 0,
      第一个子节点类型: node.children?.[0]?.type || '无',
      第一个子节点名称: node.children?.[0]?.label || '无'
    }))
  }
  
  console.log('========== 树状图诊断信息 ==========')
  console.log(JSON.stringify(diagnostic, null, 2))
  console.log('完整树数据：', treeData.value)
  console.log('====================================')
  
  ElMessage.info({
    message: '诊断信息已输出到控制台，请按F12查看',
    duration: 3000
  })
}

// 拖动调整宽度
const startResize = (e: MouseEvent) => {
  isResizing.value = true
  startX.value = e.clientX
  
  // 获取当前宽度（百分比转像素）
  const containerWidth = (e.target as HTMLElement).parentElement?.offsetWidth || 1000
  const currentPercent = parseFloat(asideWidth.value)
  startWidth.value = (containerWidth * currentPercent) / 100
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  
  // 防止选中文本
  e.preventDefault()
}

const handleResize = (e: MouseEvent) => {
  if (!isResizing.value) return
  
  const deltaX = e.clientX - startX.value
  const containerWidth = document.querySelector('.org-container')?.clientWidth || 1000
  const newWidth = startWidth.value + deltaX
  
  // 限制宽度范围：15% - 40%
  const minWidth = containerWidth * 0.15
  const maxWidth = containerWidth * 0.40
  
  if (newWidth >= minWidth && newWidth <= maxWidth) {
    const newPercent = (newWidth / containerWidth) * 100
    asideWidth.value = `${newPercent.toFixed(2)}%`
  }
}

const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  
  // 保存到localStorage
  localStorage.setItem('collectorManagement_asideWidth', asideWidth.value)
}

// 树节点点击处理
const handleTreeNodeClick = (data: OrgTreeNode) => {
  console.log('点击树节点：', data)
  
  if (data.type === 'agency') {
    // 点击机构：筛选该机构的所有催员
    currentAgencyId.value = (data.data as any).id
    currentTeamId.value = undefined
    loadCollectors()
  } else if (data.type === 'team_group') {
    // 点击小组群：筛选该小组群下所有小组的催员
    const teamGroup = data.data as any
    currentAgencyId.value = teamGroup.agency_id
    currentTeamId.value = undefined
    // TODO: 需要根据小组群筛选，这里暂时显示该机构的所有催员
    loadCollectors()
  } else if (data.type === 'team') {
    // 点击小组：筛选该小组的催员
    const team = data.data as any
    currentAgencyId.value = team.agency_id
    currentTeamId.value = team.id
    loadTeams()
    loadCollectors()
  } else if (data.type === 'collector') {
    // 点击催员：在表格中高亮（滚动到该催员）
    const collector = data.data as any
    // 使用搜索关键词来筛选
    searchKeyword.value = collector.collector_code || collector.collector_name
  }
}

// 从树创建催员
const handleCreateCollectorFromTree = (treeNodeData: OrgTreeNode) => {
  const team = treeNodeData.data as any
  
  isEdit.value = false
  fromTree.value = true
  dialogTitle.value = `为小组"${team.team_name}"创建催员`
  
  form.value = {
    id: undefined,
    collector_code: '',
    collector_name: '',
    password: '',
    agency_id: team.agency_id,
    team_id: team.id,
    role: 'collector',
    email: '',
    remark: '',
    is_active: true
  }
  
  // 加载表单中的小组列表
  loadFormTeams(team.agency_id)
  
  // 自动生成密码
  generatePassword()
  
  dialogVisible.value = true
}

// 初始加载
onMounted(async () => {
  // 恢复保存的宽度
  const savedWidth = localStorage.getItem('collectorManagement_asideWidth')
  if (savedWidth) {
    asideWidth.value = savedWidth
  }
  
  if (currentTenantId.value) {
    await loadAgencies()
    
    // 构建树状图
    await buildTreeData()
    
    // 检查URL参数中是否有筛选条件
    const urlAgencyId = route.query.agencyId ? Number(route.query.agencyId) : undefined
    const urlTeamId = route.query.teamId ? Number(route.query.teamId) : undefined
    
    // 优先使用URL参数，否则根据用户角色设置默认值
    if (urlAgencyId) {
      currentAgencyId.value = urlAgencyId
      await loadTeams()
      
      if (urlTeamId) {
        currentTeamId.value = urlTeamId
      }
    } else if (userAgencyId.value && showAgencySelector.value === false) {
      // 机构管理员：自动设置为自己的机构
      currentAgencyId.value = userAgencyId.value
      await loadTeams()
    }
    
    if (userTeamId.value && showTeamSelector.value === false && !urlTeamId) {
      // 小组管理员：自动设置为自己的小组（如果URL没有指定）
      currentTeamId.value = userTeamId.value
    }
    
    await loadCollectors() // 默认加载所有催员
  }
})

const form = ref({
  id: undefined as number | undefined,
  collector_code: '',
  collector_name: '',
  password: '',
  agency_id: undefined as number | undefined,
  team_id: undefined as number | undefined,
  role: 'collector',
  email: '',
  remark: '',
  is_active: true
})

const passwordForm = ref({
  new_password: '',
  confirm_password: ''
})

const rules = reactive({
  collector_code: [
    { required: true, message: '请输入催员登录id', trigger: 'blur' }
  ],
  collector_name: [
    { required: true, message: '请输入催员姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '催员姓名长度为2-50个字符', trigger: 'blur' }
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
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    { max: 100, message: '邮箱长度不能超过100个字符', trigger: 'blur' }
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
      validator: (_rule: any, value: any, callback: any) => {
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

// 机构切换时加载小组
const handleAgencyChange = async () => {
  currentTeamId.value = undefined
  collectors.value = []
  searchKeyword.value = '' // 清空搜索关键词
  
  if (currentAgencyId.value) {
    // 选择了机构，加载该机构的小组
    await loadTeams()
    // 加载该机构的所有催员（小组为全选）
    await loadCollectors()
    
    // 同步树状图：高亮对应机构节点
    syncTreeSelection()
  } else {
    // 选择"全部机构"，清空小组列表，加载所有催员
    teams.value = []
    await loadCollectors()
    
    // 清除树选择
    if (treeRef.value) {
      treeRef.value.setCurrentKey(null)
    }
  }
}

// 小组切换时加载催员
const handleTeamChange = async () => {
  await loadCollectors()
  // 同步树状图：高亮对应小组节点
  syncTreeSelection()
}

// 同步树状图选择
const syncTreeSelection = () => {
  if (!treeRef.value) return
  
  let nodeKey: string | null = null
  
  if (currentTeamId.value) {
    // 选择了小组：高亮小组节点
    nodeKey = `team-${currentTeamId.value}`
  } else if (currentAgencyId.value) {
    // 只选择了机构：高亮机构节点
    nodeKey = `agency-${currentAgencyId.value}`
  }
  
  if (nodeKey) {
    treeRef.value.setCurrentKey(nodeKey)
    // 确保节点展开可见
    const node = treeRef.value.getNode(nodeKey)
    if (node && node.parent) {
      node.parent.expanded = true
    }
  }
}

// 表单中机构切换时加载小组
const handleFormAgencyChange = async () => {
  form.value.team_id = undefined
  if (form.value.agency_id) {
    await loadFormTeams(form.value.agency_id)
  } else {
    formTeams.value = []
  }
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
    let allAgencies = Array.isArray(result) ? result : (result.data || [])
    
    // 如果是机构管理员，只显示自己所属的机构
    if (userAgencyId.value && showAgencySelector.value === false) {
      allAgencies = allAgencies.filter((agency: any) => agency.id === userAgencyId.value)
    }
    
    agencies.value = allAgencies
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
    let allTeams = Array.isArray(result) ? result : (result.data || [])
    
    // 如果是小组管理员，只显示自己所属的小组
    if (userTeamId.value && showTeamSelector.value === false) {
      allTeams = allTeams.filter((team: any) => team.id === userTeamId.value)
    }
    
    teams.value = allTeams
  } catch (error) {
    console.error('加载小组失败：', error)
    ElMessage.error('加载小组失败')
  }
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

// 搜索处理（计算属性会自动响应搜索关键词变化）
const handleSearch = () => {
  // 搜索功能由 filteredCollectors 计算属性自动处理
  // 这里可以添加额外的搜索逻辑，如日志记录
  console.log('搜索关键词：', searchKeyword.value)
}

// 加载催员列表（支持全选）
const loadCollectors = async () => {
  if (!currentTenantId.value) {
    collectors.value = []
    return
  }

  try {
    let allCollectors: any[] = []
    
    // 获取当前甲方名称
    const tenantName = tenantStore.currentTenant?.tenant_name || 
                       tenantStore.currentTenant?.name || 
                       tenantStore.currentTenant?.tenantName || ''
    
    if (!currentAgencyId.value && !currentTeamId.value) {
      // 全选：加载所有机构的催员
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
          
          // 遍历该机构的所有小组，加载催员
          for (const team of agencyTeams) {
            try {
              const collectorsUrl = getApiUrl(`teams/${team.id}/collectors`)
              const collectorsResponse = await fetch(collectorsUrl)
              const collectorsResult = await collectorsResponse.json()
              // API直接返回数组，不是{data: [...]}格式
              const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
              
              // 为每个催员添加甲方、机构和小组信息
              teamCollectors.forEach((collector: any) => {
                collector.tenant_name = tenantName
                collector.agency_name = agency.agency_name
                collector.team_name = team.team_name
              })
              
              allCollectors.push(...teamCollectors)
            } catch (error) {
              console.error(`加载小组 ${team.id} 的催员失败：`, error)
            }
          }
        } catch (error) {
          console.error(`加载机构 ${agency.id} 的小组失败：`, error)
        }
      }
    } else if (currentAgencyId.value && !currentTeamId.value) {
      // 只选择了机构：加载该机构所有小组的催员
      await loadTeams()
      
      for (const team of teams.value) {
        try {
          const collectorsUrl = `http://getApiUrl/api/v1/teams/${team.id}/collectors`
          const collectorsResponse = await fetch(collectorsUrl)
          const collectorsResult = await collectorsResponse.json()
          // API直接返回数组，不是{data: [...]}格式
          const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
          
          // 为每个催员添加甲方、机构和小组信息
          const agency = agencies.value.find(a => a.id === currentAgencyId.value)
          teamCollectors.forEach((collector: any) => {
            collector.tenant_name = tenantName
            collector.agency_name = agency?.agency_name || ''
            collector.team_name = team.team_name
          })
          
          allCollectors.push(...teamCollectors)
        } catch (error) {
          console.error(`加载小组 ${team.id} 的催员失败：`, error)
        }
      }
    } else if (currentAgencyId.value && currentTeamId.value) {
      // 选择了机构和小组：加载该小组的催员
      const { getApiUrl } = await import('@/config/api')
      const collectorsUrl = getApiUrl(`teams/${currentTeamId.value}/collectors`)
      const collectorsResponse = await fetch(collectorsUrl)
      const collectorsResult = await collectorsResponse.json()
      // API直接返回数组，不是{data: [...]}格式
      const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
      
      // 为每个催员添加甲方、机构和小组信息
      const agency = agencies.value.find(a => a.id === currentAgencyId.value)
      const team = teams.value.find(t => t.id === currentTeamId.value)
      teamCollectors.forEach((collector: any) => {
        collector.tenant_name = tenantName
        collector.agency_name = agency?.agency_name || ''
        collector.team_name = team?.team_name || ''
      })
      
      allCollectors = teamCollectors
    }
    
    collectors.value = allCollectors
    console.log(`已加载 ${collectors.value.length} 个催员`)
  } catch (error) {
    console.error('加载催员失败：', error)
    ElMessage.error('加载催员失败')
  }
}

// 生成随机密码（包含数字、大小写字母，8位以内）
const generatePassword = () => {
  const lowercase = 'abcdefghijklmnopqrstuvwxyz'
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const numbers = '0123456789'
  const allChars = lowercase + uppercase + numbers
  
  // 确保至少包含一个大写字母、一个小写字母和一个数字
  let password = ''
  password += uppercase[Math.floor(Math.random() * uppercase.length)]
  password += lowercase[Math.floor(Math.random() * lowercase.length)]
  password += numbers[Math.floor(Math.random() * numbers.length)]
  
  // 随机生成剩余字符，总长度在6-8位之间
  const length = Math.floor(Math.random() * 3) + 6 // 6-8位
  for (let i = password.length; i < length; i++) {
    password += allChars[Math.floor(Math.random() * allChars.length)]
  }
  
  // 打乱字符顺序
  password = password.split('').sort(() => Math.random() - 0.5).join('')
  
  form.value.password = password
}

// 创建催员
const handleAdd = () => {
  isEdit.value = false
  fromTree.value = false // 不是从树创建
  dialogTitle.value = '创建催员'
  form.value = {
    id: undefined,
    collector_code: '',
    collector_name: '',
    password: '',
    agency_id: undefined,
    team_id: undefined,
    role: 'collector',
    email: '',
    remark: '',
    is_active: true
  }
  // 自动生成密码
  generatePassword()
  dialogVisible.value = true
}

// 编辑催员
const handleEdit = async (row: any) => {
  isEdit.value = true
  fromTree.value = false // 编辑时不限制
  dialogTitle.value = '编辑催员'
  form.value = {
    id: row.id,
    collector_code: row.collector_code,
    collector_name: row.collector_name,
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

// 保存催员
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    // 在创建模式下，需要拼接前缀和用户输入的部分
    const submitData = { ...form.value }
    if (!isEdit.value && tenantPrefix.value) {
      submitData.collector_code = tenantPrefix.value + '-' + form.value.collector_code
    }

    console.log('保存催员：', submitData)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    
    // 刷新表格和树状图
    await loadCollectors()
    if (fromTree.value) {
      await buildTreeData()
    }
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleResetPassword = (row: any) => {
  currentCollector.value = row
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
      collector_id: currentCollector.value.id,
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

// 导出催员账号与密码
const handleExportAccounts = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能待实现')
}

// 查看IM端
const handleViewIm = (row: any) => {
  // 构建URL参数
  const params = new URLSearchParams({
    collectorId: row.collector_code || row.collector_id || '',
    tenantId: String(row.tenant_id || currentTenantId.value || ''),
    simulate: 'true' // 标记为模拟登录
  })
  
  // 打开新标签页
  const imLoginUrl = `${window.location.origin}/im/login?${params.toString()}`
  window.open(imLoginUrl, '_blank')
}

// 查看登录人脸记录
const handleViewLoginFaces = (row: any) => {
  const query: Record<string, string | number> = {}

  const collectorId = row.id ?? row.collector_id
  if (collectorId) {
    query.collectorId = collectorId
  }
  if (row.agency_id) {
    query.agencyId = row.agency_id
  }
  if (row.team_group_id) {
    query.teamGroupId = row.team_group_id
  }
  if (row.team_id) {
    query.teamId = row.team_id
  }

  router.push({
    path: '/ai-quality/face-compare',
    query
  })
}

// 格式化日期（年-月-日）
const formatDate = (dateTime: string) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化时间（时:分:秒）
const formatTime = (dateTime: string) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 启用/禁用催员
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}催员"${row.collector_name}"吗？${row.is_active ? '禁用后该催员将无法登录系统。' : ''}`,
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

/* 左右布局 */
.org-container {
  height: calc(100vh - 280px);
  min-height: 600px;
}

.tree-aside {
  padding: 16px;
  overflow-y: auto;
  background-color: #fafafa;
  position: relative;
}

/* 拖动分隔条 */
.resize-handle {
  width: 8px;
  cursor: col-resize;
  background-color: #e4e7ed;
  position: relative;
  flex-shrink: 0;
  transition: background-color 0.3s;
}

.resize-handle:hover {
  background-color: #409eff;
}

.resize-handle::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 2px;
  height: 40px;
  background-color: #909399;
  border-radius: 1px;
  transition: background-color 0.3s;
}

.resize-handle:hover::before {
  background-color: #fff;
}

.table-main {
  padding: 16px;
  overflow-y: auto;
}

/* 树状图头部 */
.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.tree-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

/* 树容器 */
.tree-container {
  height: calc(100% - 50px);
  overflow-y: visible;
}

/* 树状图样式 */
.org-tree {
  background-color: transparent;
}

.org-tree :deep(.el-tree-node__content) {
  height: 40px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.org-tree :deep(.el-tree-node__content:hover) {
  background-color: #f0f2f5;
}

.org-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #e6f7ff;
  font-weight: 500;
}

/* 自定义树节点 */
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-label .el-icon {
  font-size: 16px;
  color: #606266;
}

.node-actions {
  display: none;
}

.custom-tree-node:hover .node-actions {
  display: block;
}

/* 筛选栏 */
.filter-bar {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 催员统计信息 */
.collector-stats {
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

/* 登录时间单元格样式 */
.login-time-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.4;
}

.login-time-cell .date-line {
  font-weight: 500;
  color: #303133;
}

.login-time-cell .time-line {
  font-size: 12px;
  color: #909399;
}

/* 登录人脸记录样式 */
.face-record-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.face-image-container {
  flex-shrink: 0;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e4e7ed;
  background: #f5f7fa;
}

.face-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-row .label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.info-row .value {
  color: #303133;
}

/* 时间轴样式优化 */
:deep(.el-timeline-item__timestamp) {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

:deep(.el-timeline-item__wrapper) {
  padding-left: 20px;
}

:deep(.el-timeline-item__tail) {
  border-left: 2px solid #e4e7ed;
}

:deep(.el-timeline-item__node) {
  background-color: #409eff;
  border-color: #409eff;
}
</style>

