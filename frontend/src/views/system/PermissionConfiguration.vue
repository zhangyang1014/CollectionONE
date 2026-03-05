<template>
  <div class="permission-configuration">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Setting /></el-icon>
            权限配置管理
          </span>
          <el-space>
            <el-button
              type="primary"
              plain
              @click="showViewMode"
            >
              <el-icon><View /></el-icon>
              查看模式
            </el-button>
            <el-button
              type="info"
              plain
              @click="handleExport"
            >
              <el-icon><Download /></el-icon>
              导出配置
            </el-button>
          </el-space>
        </div>
      </template>

      <!-- 配置选择区 -->
      <div class="config-selector">
        <el-alert
          :title="configLevelText"
          :type="configLevel === 'system' ? 'warning' : 'info'"
          :closable="false"
          show-icon
        >
          <template v-if="configLevel === 'system'">
            <p>您正在配置<strong>系统默认权限</strong>。</p>
            <p>这些配置将作为所有甲方的默认权限，除非甲方有自定义配置。</p>
          </template>
          <template v-else>
            <p>您正在配置<strong>{{ currentTenantName }}</strong>的权限。</p>
            <p>这些配置将覆盖系统默认配置，仅对当前甲方生效。</p>
          </template>
        </el-alert>

        <el-form :inline="true" style="margin-top: 16px">
          <el-form-item label="配置级别">
            <el-radio-group v-model="configLevel" @change="handleConfigLevelChange">
              <el-radio value="system">系统默认配置</el-radio>
              <el-radio value="tenant" :disabled="!currentTenantId">甲方自定义配置</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="当前角色">
            <el-tag type="primary" size="large">{{ currentRoleName }}</el-tag>
          </el-form-item>
          <el-form-item label="可配置角色">
            <el-text type="info">
              共 {{ configurableRoles.length }} 个角色可配置
            </el-text>
          </el-form-item>
        </el-form>
      </div>

      <!-- 权限树 + 矩阵 -->
      <div v-loading="loading" class="matrix-wrapper">
        <div class="tree-matrix-layout">
          <div class="tree-panel">
            <div class="tree-panel__header">
              <span>权限树</span>
              <el-text type="info" size="small">点击节点筛选矩阵</el-text>
            </div>
            <el-tree
              v-if="permissionTree.length > 0"
              :data="permissionTree"
              node-key="id"
              :props="treeProps"
              default-expand-all
              highlight-current
              :current-node-key="selectedTreeNodeId"
              @node-click="handleTreeNodeClick"
              class="permission-tree"
            >
              <template #default="{ node, data }">
                <span class="tree-node-label">
                  {{ node.label }}
                  <el-tag 
                    v-if="getNodeDisplayCount(data) > 0" 
                    size="small" 
                    type="info" 
                    style="margin-left: 8px;"
                  >
                    {{ getNodeDisplayCount(data) }}
                  </el-tag>
                </span>
              </template>
            </el-tree>
            <el-empty v-else description="暂无树数据" />
          </div>

          <div class="matrix-panel">
            <PermissionMatrix
              v-if="!loading && (filteredMatrix.modules.length > 0 || filteredMatrix.items.length > 0)"
              :modules="filteredMatrix.modules"
              :items="filteredMatrix.items"
              :configs="filteredMatrix.configs"
              :display-roles="displayRoles"
              :loading="loading"
              :filter-item-ids="selectedItemIds.length > 0 ? selectedItemIds : undefined"
              @save="handleSaveConfigs"
              @reset="handleResetConfigs"
            />
            <el-empty
              v-else-if="!loading"
              :description="selectedTreeNodeId ? '当前节点暂无权限数据' : '请选择树节点查看权限配置'"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, View, Download } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTenantStore } from '@/stores/tenant'
import PermissionMatrix from '@/components/PermissionMatrix.vue'
import {
  getPermissionMatrix,
  getConfigurableRoles,
  batchUpdatePermissionConfigs
} from '@/api/permission'
import type {
  PermissionModule,
  PermissionItem,
  RolePermissionConfig,
  RoleInfo
} from '@/types/permission'
import { ROLE_DISPLAY } from '@/types/permission'

// ==================== Stores ====================
const router = useRouter()
const userStore = useUserStore()
const tenantStore = useTenantStore()

// ==================== State ====================
const loading = ref(false)
const configLevel = ref<'system' | 'tenant'>('system')
const matrixData = ref<{
  modules: PermissionModule[]
  items: PermissionItem[]
  configs: RolePermissionConfig[]
}>({
  modules: [],
  items: [],
  configs: []
})
const configurableRoles = ref<RoleInfo[]>([])

// 权限树
type PermissionTreeNode = {
  id: string
  label: string
  level: number
  moduleKeys?: string[]
  keywords?: string[]
  itemIds: number[]
  children?: PermissionTreeNode[]
}

const permissionTree = ref<PermissionTreeNode[]>([])
const treeProps = {
  children: 'children',
  label: 'label'
}
const selectedTreeNodeId = ref('')
const nodeItemIdsMap = ref<Record<string, number[]>>({})

// ==================== Computed ====================
const currentRoleCode = computed(() => userStore.role || 'SUPER_ADMIN')
const currentRoleName = computed(() => ROLE_DISPLAY[currentRoleCode.value]?.name || currentRoleCode.value)
const currentTenantId = computed(() => tenantStore.currentTenantId)
const currentTenantName = computed(() => tenantStore.currentTenant?.name || '未知甲方')

const configLevelText = computed(() => {
  return configLevel.value === 'system' ? '系统默认配置' : '甲方自定义配置'
})

// 显示的角色列表（根据当前用户角色筛选）
const displayRoles = computed(() => configurableRoles.value)

// 当前树节点下的权限项id
const selectedItemIds = computed(() => {
  return nodeItemIdsMap.value[selectedTreeNodeId.value] || []
})

// 获取节点显示的数量
const getNodeDisplayCount = (nodeData: PermissionTreeNode): number => {
  // 如果是第3级节点（二级菜单，如"待分配案件"、"催收中案件"），
  // 计算其所有子节点（第4级权限类型）的权限项数量总和（去重）
  if (nodeData.level === 3 && nodeData.children && nodeData.children.length > 0) {
    const allItemIds = new Set<number>()
    // 只计算子节点（第4级）的权限项，不包含父节点自己的
    nodeData.children.forEach(child => {
      // 第4级节点是叶子节点，它们的 itemIds 是直接分配的
      const childItemIds = nodeItemIdsMap.value[child.id] || []
      childItemIds.forEach(id => allItemIds.add(id))
    })
    return allItemIds.size
  }
  // 其他节点（第1、2、4级）显示自己的权限项数量
  const itemIds = nodeItemIdsMap.value[nodeData.id] || []
  return itemIds.length
}

// 矩阵过滤数据（按树节点）
const filteredMatrix = computed(() => {
  if (!selectedTreeNodeId.value) {
    return matrixData.value
  }

  if (!selectedItemIds.value.length) {
    return {
      modules: [],
      items: [],
      configs: []
    }
  }

  const itemIdSet = new Set(selectedItemIds.value)
  const filteredItems = matrixData.value.items.filter(item => itemIdSet.has(item.id))
  const moduleIds = new Set(filteredItems.map(item => item.module_id))

  return {
    modules: matrixData.value.modules.filter(m => moduleIds.has(m.id)),
    items: filteredItems,
    configs: matrixData.value.configs.filter(c => itemIdSet.has(c.permission_item_id))
  }
})

// ==================== Methods ====================

// 加载可配置角色列表
const loadConfigurableRoles = async () => {
  try {
    const response = await getConfigurableRoles(currentRoleCode.value)
    // axios拦截器已经解包了response.data，所以直接使用response
    const configurable_roles = response.configurable_roles || []
    configurableRoles.value = configurable_roles.map(role => ({
      code: role.code,
      name: role.name,
      description: ROLE_DISPLAY[role.code]?.description
    }))
    console.log('可配置角色：', configurableRoles.value)
  } catch (error) {
    console.error('加载可配置角色失败:', error)
    ElMessage.error('加载可配置角色失败')
  }
}

// 加载权限矩阵数据
const loadMatrixData = async () => {
  loading.value = true
  try {
    const tenantId = configLevel.value === 'system' ? null : currentTenantId.value
    const response = await getPermissionMatrix(tenantId)
    
    // axios拦截器已经解包了response.data，所以直接使用response
    matrixData.value = {
      modules: response.modules || [],
      items: response.items || [],
      configs: response.configs || []
    }

    buildPermissionTree()
    
    console.log('权限矩阵数据：', {
      模块数: matrixData.value.modules.length,
      权限项数: matrixData.value.items.length,
      配置数: matrixData.value.configs.length
    })
  } catch (error) {
    console.error('加载权限矩阵失败:', error)
    ElMessage.error('加载权限矩阵失败')
  } finally {
    loading.value = false
  }
}

// 配置级别切换
const handleConfigLevelChange = () => {
  loadMatrixData()
}

// 保存配置
const handleSaveConfigs = async (configs: RolePermissionConfig[]) => {
  try {
    loading.value = true
    
    const tenantId = configLevel.value === 'system' ? null : currentTenantId.value
    
    // 构造批量更新请求
    const updates = configs.map(config => ({
      role_code: config.role_code,
      permission_item_id: config.permission_item_id,
      permission_level: config.permission_level
    }))
    
    const response = await batchUpdatePermissionConfigs({
      tenant_id: tenantId,
      updates
    })
    
    // axios拦截器已经解包了response.data
    if (response.success) {
      ElMessage.success(response.message || '权限配置保存成功')
      // 重新加载数据
      await loadMatrixData()
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error: any) {
    console.error('保存权限配置失败:', error)
    ElMessage.error(error.message || '保存权限配置失败')
  } finally {
    loading.value = false
  }
}

// 重置配置
const handleResetConfigs = () => {
  loadMatrixData()
}

// 树节点点击
const handleTreeNodeClick = (node: PermissionTreeNode) => {
  selectedTreeNodeId.value = node.id
}

// 根据图片规则构建权限树（控台部分），IM预留
const buildPermissionTree = () => {
  const modules = matrixData.value.modules || []
  const items = matrixData.value.items || []
  nodeItemIdsMap.value = {}

  // 树模板
  const consoleSchema = {
    key: 'console',
    label: '控台',
    modules: [
      {
        key: 'case_mgmt',
        label: '案件管理',
        moduleKeys: ['case'],
        keywords: ['案'],
        children: [
          {
            key: 'case_pending',
            label: '待分配案件',
            keywords: ['待分配'],
            types: [
              { key: 'case_pending_operate', label: '操作权限', keywords: ['操作'] },
              { key: 'case_pending_tab', label: '详情TAB页权限', keywords: ['tab', '详情'] }
            ]
          },
          {
            key: 'case_collecting',
            label: '催收中案件',
            keywords: ['催收中', '催收'],
            types: [
              { key: 'case_collecting_data', label: '数据权限', keywords: ['数据'] },
              { key: 'case_collecting_operate', label: '操作权限', keywords: ['操作'] },
              { key: 'case_collecting_tab', label: '详情TAB页权限', keywords: ['tab', '详情'] }
            ]
          },
          {
            key: 'case_settled',
            label: '已结清案件',
            keywords: ['结清'],
            types: [
              { key: 'case_settled_operate', label: '操作权限', keywords: ['操作'] },
              { key: 'case_settled_tab', label: '详情TAB页权限', keywords: ['tab', '详情'] }
            ]
          },
          {
            key: 'case_stop',
            label: '停催案件',
            keywords: ['停催'],
            types: [
              { key: 'case_stop_operate', label: '操作权限', keywords: ['操作'] },
              { key: 'case_stop_tab', label: '详情TAB页权限', keywords: ['tab', '详情'] }
            ]
          }
        ]
      },
      {
        key: 'field_mgmt',
        label: '字段管理',
        moduleKeys: ['field'],
        keywords: ['字段'],
        children: [
          {
            key: 'field_list',
            label: '案件列表字段管理',
            keywords: ['列表', '字段'],
            types: [{ key: 'field_list_operate', label: '操作权限', keywords: ['操作'] }]
          },
          {
            key: 'field_detail',
            label: '案件详情字段管理',
            keywords: ['详情', '字段'],
            types: [{ key: 'field_detail_operate', label: '操作权限', keywords: ['操作'] }]
          }
        ]
      },
      {
        key: 'org_mgmt',
        label: '人员与机构管理',
        moduleKeys: ['tenant', 'agency', 'team', 'collector'],
        keywords: ['人', '机构', '小组', '催员', '甲方'],
        children: [
          { key: 'tenant_mgmt', label: '甲方管理', keywords: ['甲方'], types: [{ key: 'tenant_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'agency_mgmt', label: '机构管理', keywords: ['机构'], types: [{ key: 'agency_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'team_group_mgmt', label: '小组群管理', keywords: ['小组群'], types: [{ key: 'team_group_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'team_mgmt', label: '小组管理', keywords: ['小组'], types: [{ key: 'team_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'team_admin_mgmt', label: '小组管理员管理', keywords: ['管理员'], types: [{ key: 'team_admin_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'collector_mgmt', label: '催员管理', keywords: ['催员'], types: [{ key: 'collector_operate', label: '操作权限', keywords: ['操作'] }] }
        ]
      },
      {
        key: 'system_mgmt',
        label: '系统管理',
        moduleKeys: ['system', 'channel', 'performance', 'chat', 'dashboard'],
        keywords: ['系统', '角色', '队列', '通知', '分案'],
        children: [
          { key: 'console_role', label: '控台角色管理', keywords: ['控台', '角色'], types: [{ key: 'console_role_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'collector_role', label: '催员角色管理', keywords: ['催员', '角色'], types: [{ key: 'collector_role_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'case_queue', label: '案件队列配置', keywords: ['队列'], types: [{ key: 'case_queue_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'reassign', label: '重新分案配置', keywords: ['分案'], types: [{ key: 'reassign_operate', label: '操作权限', keywords: ['操作'] }] },
          { key: 'notify', label: '通知配置', keywords: ['通知'], types: [{ key: 'notify_operate', label: '操作权限', keywords: ['操作'] }] }
        ]
      }
    ]
  }

  const imSchema = {
    key: 'im',
    label: 'IM',
    modules: [] as any[]
  }

  const normalize = (str?: string) => (str || '').toLowerCase().trim()
  const matchByKeywords = (text: string, keywords?: string[]) => {
    if (!keywords || keywords.length === 0) return false
    const normalizedText = normalize(text)
    // 至少匹配一个关键词即可（OR逻辑）
    return keywords.some(kw => normalizedText.includes(normalize(kw)))
  }

  const createNode = (key: string, label: string, level: number, moduleKeys?: string[], keywords?: string[]): PermissionTreeNode => ({
    id: key,
    label,
    level,
    moduleKeys,
    keywords,
    itemIds: [],
    children: []
  })

  const rootNodes: PermissionTreeNode[] = []
  const leafMap: Record<string, PermissionTreeNode> = {}

  const buildFromSchema = (schema: any): PermissionTreeNode | null => {
    if (!schema.modules || schema.modules.length === 0) {
      // IM部分暂时为空，不创建节点
      return null
    }
    const root = createNode(schema.key, schema.label, 1)
    schema.modules.forEach((module: any) => {
      const moduleNode = createNode(`module_${module.key}`, module.label, 2, module.moduleKeys, module.keywords)
      if (module.children && module.children.length > 0) {
        module.children.forEach((group: any) => {
          const groupNode = createNode(`group_${group.key}`, group.label, 3, undefined, group.keywords)
          if (group.types && group.types.length > 0) {
            group.types.forEach((type: any) => {
              const typeNode = createNode(`type_${type.key}`, type.label, 4, undefined, type.keywords)
              leafMap[typeNode.id] = typeNode
              if (!groupNode.children) groupNode.children = []
              groupNode.children.push(typeNode)
            })
          }
          if (!moduleNode.children) moduleNode.children = []
          moduleNode.children.push(groupNode)
        })
      }
      if (!root.children) root.children = []
      root.children.push(moduleNode)
    })
    return root
  }

  const consoleRoot = buildFromSchema(consoleSchema)
  if (consoleRoot) rootNodes.push(consoleRoot)
  
  // IM部分暂时为空，不创建节点
  // const imRoot = buildFromSchema(imSchema)
  // if (imRoot) rootNodes.push(imRoot)

  const moduleMap = new Map<number, { key?: string; name?: string }>()
  modules.forEach(m => {
    moduleMap.set(m.id, { key: m.module_key, name: m.module_name })
  })

  // 为每个权限项分配到最合适的叶子节点
  items.forEach(item => {
    const moduleInfo = moduleMap.get(item.module_id) || {}
    const moduleKey = normalize(moduleInfo.key)
    const moduleName = normalize(moduleInfo.name)
    const itemName = normalize(item.item_name)

    // 找到匹配的顶级schema（目前只有控台）
    const schemaKey = 'console'

    // 匹配模块（优先匹配moduleKey，再匹配关键词）
    const targetModule = consoleSchema.modules.find((mod: any) => {
      if (mod.moduleKeys && mod.moduleKeys.some((k: string) => k === moduleInfo.key)) return true
      if (mod.keywords && matchByKeywords(moduleName, mod.keywords)) return true
      return false
    })

    if (!targetModule) {
      // 找不到模块，跳过该权限项
      return
    }

    // 匹配二级菜单（优先精确匹配，再模糊匹配）
    let targetGroup = targetModule.children?.find((group: any) => {
      if (!group.keywords) return false
      // 精确匹配：权限项名称包含二级菜单的所有关键词
      return group.keywords.every((kw: string) => itemName.includes(normalize(kw)))
    })

    // 如果精确匹配失败，尝试模糊匹配
    if (!targetGroup) {
      targetGroup = targetModule.children?.find((group: any) => 
        matchByKeywords(itemName, group.keywords)
      )
    }

    // 如果还是找不到，使用第一个二级菜单作为默认
    if (!targetGroup && targetModule.children && targetModule.children.length > 0) {
      targetGroup = targetModule.children[0]
    }

    if (!targetGroup || !targetGroup.types || targetGroup.types.length === 0) {
      // 找不到二级菜单或没有权限类型，跳过该权限项
      return
    }

    // 匹配权限类型（操作权限、数据权限、详情TAB页权限）
    let targetType = targetGroup.types.find((t: any) => {
      if (!t.keywords) return false
      // 精确匹配权限类型关键词
      return t.keywords.some((kw: string) => {
        const normalizedKw = normalize(kw)
        // 特殊处理：详情TAB页权限可能包含"详情"、"tab"等关键词
        if (normalizedKw === 'tab' || normalizedKw === '详情') {
          return itemName.includes('详情') || itemName.includes('tab') || itemName.includes('页面')
        }
        // 操作权限匹配"操作"、"编辑"、"删除"等
        if (normalizedKw === '操作') {
          return itemName.includes('操作') || itemName.includes('编辑') || 
                 itemName.includes('删除') || itemName.includes('创建') ||
                 itemName.includes('修改') || itemName.includes('管理')
        }
        // 数据权限匹配"数据"、"查看"等
        if (normalizedKw === '数据') {
          return itemName.includes('数据') || itemName.includes('查看')
        }
        return itemName.includes(normalizedKw)
      })
    })

    // 如果找不到，使用第一个权限类型作为默认
    if (!targetType && targetGroup.types.length > 0) {
      targetType = targetGroup.types[0]
    }

    const leafId = targetType ? `type_${targetType.key}` : null
    const leafNode = leafId ? leafMap[leafId] : null

    if (leafNode) {
      leafNode.itemIds.push(item.id)
    }
    // 如果找不到匹配的节点，跳过该权限项（不分配到未分组）
  })

  // 汇总子节点itemIds到父节点
  const aggregate = (node: PermissionTreeNode): number[] => {
    let ids: number[] = [...node.itemIds]
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => {
        ids = ids.concat(aggregate(child))
      })
    }
    nodeItemIdsMap.value[node.id] = ids
    return ids
  }

  rootNodes.forEach(root => aggregate(root))

  // 默认选中：第一个有数据的叶子，否则第一个叶子，如果都没有则选中根节点
  const allLeaves = Object.values(leafMap)
  const firstWithData = allLeaves.find(l => {
    const ids = nodeItemIdsMap.value[l.id]
    return ids && ids.length > 0
  })
  
  if (firstWithData) {
    selectedTreeNodeId.value = firstWithData.id
  } else if (allLeaves.length > 0) {
    selectedTreeNodeId.value = allLeaves[0].id
  } else if (rootNodes.length > 0 && rootNodes[0].id) {
    // 如果连叶子都没有，选中根节点（会显示所有数据）
    selectedTreeNodeId.value = rootNodes[0].id
  }

  permissionTree.value = rootNodes
}

// 查看模式（跳转到只读查看页面）
const showViewMode = () => {
  router.push('/system/permission-management')
}

// 导出配置
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
  // TODO: 实现导出功能
}

// ==================== Lifecycle ====================
onMounted(async () => {
  console.log('=== 权限配置页面初始化 ===')
  console.log('用户信息:', userStore.userInfo)
  console.log('当前角色代码:', currentRoleCode.value)
  console.log('当前角色名称:', currentRoleName.value)
  console.log('当前甲方ID:', currentTenantId.value)
  console.log('配置级别:', configLevel.value)
  
  await loadConfigurableRoles()
  await loadMatrixData()
  
  console.log('可配置角色数量:', configurableRoles.value.length)
  console.log('显示角色数量:', displayRoles.value.length)
  console.log('矩阵数据模块数:', matrixData.value.modules.length)
  console.log('矩阵数据权限项数:', matrixData.value.items.length)
  console.log('矩阵数据配置数:', matrixData.value.configs.length)
})
</script>

<style scoped>
.permission-configuration {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.config-selector {
  margin-bottom: 24px;
}

.config-selector :deep(.el-alert) {
  margin-bottom: 16px;
}

.config-selector :deep(.el-alert__description) {
  margin-top: 8px;
}

.config-selector :deep(.el-alert__description) p {
  margin: 4px 0;
}

.config-selector :deep(.el-alert__description) strong {
  color: #409eff;
}

.matrix-wrapper {
  min-height: 400px;
}

.tree-matrix-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

.tree-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  background: #f9fafb;
  height: 100%;
  overflow: auto;
}

.tree-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.permission-tree {
  --el-tree-node-hover-bg-color: #f5f7fa;
  --el-tree-node-current-bg-color: #ecf5ff;
}

.tree-node-label {
  display: flex;
  align-items: center;
  flex: 1;
}

.matrix-panel {
  min-height: 400px;
}
</style>

