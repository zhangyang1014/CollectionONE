<template>
  <div class="detail-field-groups">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情字段分组管理</span>
          <el-space>
            <el-button type="primary" @click="handleAddRoot">添加一级分组</el-button>
            <el-button type="success" @click="handleAddChild" :disabled="!currentGroup || currentGroup.is_standard">添加子分组</el-button>
            <el-button @click="handleBatchSort">批量排序</el-button>
          </el-space>
        </div>
        <el-alert
          title="提示：分组支持两级结构（一级/子级），用于案件详情字段归类。标准分组不可删除，标识不可修改。"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        />
      </template>

      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="id"
        :props="{ label: 'group_name', children: 'children' }"
        :expand-on-click-node="false"
        default-expand-all
        @node-click="handleNodeClick"
        class="group-tree"
        draggable
        @node-drop="handleNodeDrop"
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
      >
        <template #default="{ data }">
          <div class="custom-tree-node">
            <div class="node-content">
              <el-tag :type="data.parent_id ? 'info' : 'primary'" size="small" style="margin-right: 6px">
                {{ data.parent_id ? '子分组' : '一级分组' }}
              </el-tag>
              <el-tag v-if="data.is_standard" type="warning" size="small" style="margin-right: 6px">
                标准分组
              </el-tag>
              <span class="node-label">{{ data.group_name }}</span>
              <span class="node-key">{{ data.group_key }}</span>
              <span class="node-sort">排序: {{ data.sort_order }}</span>
              <el-tag :type="data.is_active ? 'success' : 'info'" size="small" style="margin-left: 6px">
                {{ data.is_active ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="node-actions">
              <el-button size="small" link type="primary" @click.stop="handleEdit(data)">
                编辑
              </el-button>
              <el-button 
                size="small" 
                link 
                type="success" 
                v-if="!data.parent_id && !data.is_standard"
                @click.stop="handleAddChild(data)"
              >
                添加子分组
              </el-button>
              <el-button 
                size="small" 
                link 
                type="danger" 
                @click.stop="handleDelete(data)"
                :disabled="data.is_standard"
              >
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="分组名称">
          <el-input v-model="form.group_name" />
        </el-form-item>
        <el-form-item label="分组标识">
          <el-input 
            v-model="form.group_key" 
            :disabled="isEditingStandardGroup"
            :placeholder="isEditingStandardGroup ? '标准分组标识不可修改' : '请输入分组标识'"
          />
        </el-form-item>
        <el-form-item label="父分组">
          <el-select 
            v-model="form.parent_id" 
            placeholder="请选择父分组" 
            clearable
            :disabled="isEditingStandardGroup"
          >
            <el-option
              v-for="g in availableParentGroups"
              :key="g.id"
              :label="g.group_name"
              :value="g.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.is_active" />
        </el-form-item>
        <el-form-item v-if="isEditingStandardGroup">
          <el-alert
            title="标准分组的标识和层级关系不可修改"
            type="warning"
            :closable="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  getDetailFieldGroups,
  createDetailFieldGroup,
  updateDetailFieldGroup,
  deleteDetailFieldGroup,
  batchUpdateGroupSort
} from '@/api/detailFieldGroup'
import type { FieldGroup } from '@/types'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

const loading = ref(false)
const groups = ref<FieldGroup[]>([])
const treeData = ref<FieldGroup[]>([])
const treeRef = ref()
const currentGroup = ref<FieldGroup | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分组')
const editingId = ref<number | null>(null)
const isEditingStandardGroup = ref(false)
const form = ref({
  tenant_id: 1,
  group_name: '',
  group_key: '',
  parent_id: null as number | null,
  sort_order: 0,
  is_active: true,
  is_standard: false
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDetailFieldGroups({ tenantId: Number(currentTenantId.value) })
    groups.value = data
    treeData.value = buildTree(groups.value)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleAddRoot = () => {
  editingId.value = null
  isEditingStandardGroup.value = false
  dialogTitle.value = '添加一级分组'
  form.value = {
    tenant_id: Number(currentTenantId.value),
    group_name: '',
    group_key: '',
    parent_id: null,
    sort_order: (groups.value.filter(g => !g.parent_id).length || 0) + 1,
    is_active: true,
    is_standard: false
  }
  dialogVisible.value = true
}

const handleAddChild = (parent?: FieldGroup) => {
  const p = parent || currentGroup.value
  if (!p) {
    ElMessage.warning('请先选择父分组')
    return
  }
  editingId.value = null
  isEditingStandardGroup.value = false
  dialogTitle.value = '添加子分组'
  form.value = {
    tenant_id: Number(currentTenantId.value),
    group_name: '',
    group_key: '',
    parent_id: p.id,
    sort_order: (groups.value.filter(g => g.parent_id === p.id).length || 0) + 1,
    is_active: true,
    is_standard: false
  }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  editingId.value = row.id
  dialogTitle.value = row.is_standard ? '编辑标准分组' : '编辑分组'
  isEditingStandardGroup.value = row.is_standard || false
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  // 标准分组不能删除
  if (row.is_standard) {
    ElMessage.warning('标准分组不可删除')
    return
  }

  // 检查是否有子分组
  const hasChildren = groups.value.some(g => g.parent_id === row.id)
  if (hasChildren) {
    ElMessage.warning('该分组下存在子分组，请先删除子分组')
    return
  }

  // TODO: 检查是否有关联字段（需要调用API）
  // 这里可以添加检查逻辑，例如：
  // const hasFields = await checkGroupHasFields(row.id)
  // if (hasFields) {
  //   ElMessage.warning('该分组下存在关联字段，无法删除')
  //   return
  // }

  try {
    await ElMessageBox.confirm(
      '确定删除该分组吗？删除后不可恢复。',
      '提示',
      { type: 'warning' }
    )
    await deleteDetailFieldGroup(row.id, Number(currentTenantId.value))
    ElMessage.success('删除成功')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.value.group_name || !form.value.group_key) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 标准分组的group_key不能修改
  if (isEditingStandardGroup.value && editingId.value) {
    const originalGroup = groups.value.find(g => g.id === editingId.value)
    if (originalGroup && form.value.group_key !== originalGroup.group_key) {
      ElMessage.warning('标准分组的标识不可修改')
      return
    }
  }

  try {
    form.value.tenant_id = Number(currentTenantId.value)
    if (editingId.value) {
      await updateDetailFieldGroup(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createDetailFieldGroup(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const buildTree = (data: FieldGroup[]) => {
  const map: Record<number, any> = {}
  const roots: any[] = []
  data.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  data.forEach(item => {
    if (item.parent_id && map[item.parent_id]) {
      map[item.parent_id].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  const sortNodes = (nodes: any[]) => {
    nodes.sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    nodes.forEach(n => n.children && n.children.length && sortNodes(n.children))
  }
  sortNodes(roots)
  return roots
}

const handleNodeClick = (data: FieldGroup) => {
  currentGroup.value = data
}

const rootGroups = computed(() => groups.value.filter(g => !g.parent_id))

// 可用的父分组（非标准分组才能被选为父分组）
const availableParentGroups = computed(() => {
  return rootGroups.value.filter(g => !g.is_standard)
})

// 拖拽相关
const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 标准分组不允许拖拽
  if (draggingNode.data.is_standard) {
    return false
  }
  // 只允许同级拖拽
  if (type === 'inner') {
    return false
  }
  return true
}

const allowDrag = (node: any) => {
  // 标准分组不允许拖拽
  return !node.data.is_standard
}

const handleNodeDrop = async (draggingNode: any, dropNode: any, dropType: string) => {
  // 重新计算排序
  const parentId = draggingNode.data.parent_id
  const siblings = groups.value.filter(g => g.parent_id === parentId)
  
  // 更新排序
  const updates = siblings.map((g, index) => ({
    id: g.id,
    sort_order: index + 1
  }))

  try {
    await batchUpdateGroupSort({
      tenantId: Number(currentTenantId.value),
      updates
    })
    ElMessage.success('排序已更新')
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '排序更新失败')
    loadData() // 重新加载恢复原状
  }
}

const handleBatchSort = () => {
  ElMessage.info('请直接拖拽分组节点进行排序（标准分组除外）')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-tree {
  margin-top: 10px;
}

/* 增加树节点的行高 */
.group-tree :deep(.el-tree-node__content) {
  min-height: 50px;
  padding: 8px 0;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 4px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.node-content .el-tag {
  min-width: 65px;
  text-align: center;
}

.node-label {
  font-weight: 600;
  min-width: 120px;
  font-size: 14px;
}

.node-key {
  color: #909399;
  font-size: 12px;
  min-width: 180px;
}

.node-sort {
  color: #606266;
  font-size: 12px;
  min-width: 80px;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 20px;
}
</style>

