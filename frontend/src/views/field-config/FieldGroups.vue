<template>
  <div class="field-groups">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字段分组管理</span>
          <el-space>
            <el-button type="primary" @click="handleAdd">添加一级分组</el-button>
            <el-button type="success" @click="handleAddChild" :disabled="!currentGroup">添加子分组</el-button>
          </el-space>
        </div>
        <el-alert
          title="提示：可以通过拖拽节点来调整分组排序"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
      </template>

      <div class="groups-container">
        <el-tree
          :data="treeData"
          node-key="id"
          :props="{ label: 'group_name', children: 'children' }"
          :expand-on-click-node="false"
          default-expand-all
          draggable
          :allow-drop="allowDrop"
          @node-drop="handleDrop"
          @node-click="handleNodeClick"
          class="group-tree"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <div class="node-content">
                <el-icon class="drag-handle" style="cursor: move; margin-right: 8px; color: #909399;">
                  <Rank />
                </el-icon>
                <el-tag :type="data.parent_id ? 'info' : 'primary'" size="small" style="margin-right: 8px">
                  {{ data.parent_id ? '子分组' : '一级分组' }}
                </el-tag>
                <span class="node-label">{{ data.group_name }}</span>
                <span class="node-key">{{ data.group_key }}</span>
              </div>
              <div class="node-actions">
                <el-button size="small" link type="primary" @click.stop="handleEdit(data)">
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  link 
                  type="success" 
                  @click.stop="handleAddChild(data)"
                  v-if="!data.parent_id"
                >
                  添加子分组
                </el-button>
                <el-button size="small" link type="danger" @click.stop="handleDelete(data)">
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分组名称">
          <el-input v-model="form.group_name" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组标识">
          <el-input v-model="form.group_key" placeholder="请输入分组标识（英文）" />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input v-model="form.group_name_en" placeholder="请输入英文名称" />
        </el-form-item>
        <el-form-item label="父分组" v-if="form.parent_id !== undefined">
          <el-select v-model="form.parent_id" placeholder="选择父分组" clearable>
            <el-option
              v-for="group in rootGroups"
              :key="group.id"
              :label="group.group_name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import { getFieldGroups, updateFieldGroupsSort } from '@/api/field'
import type { FieldGroup } from '@/types'

interface TreeNode {
  id: number
  group_name: string
  group_key: string
  group_name_en?: string
  parent_id: number | null
  sort_order: number
  children?: TreeNode[]
}

const groups = ref<FieldGroup[]>([])
const treeData = ref<TreeNode[]>([])
const currentGroup = ref<FieldGroup | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  group_name: '',
  group_key: '',
  group_name_en: '',
  parent_id: null as number | null,
  sort_order: 0
})

// 获取所有一级分组（用于选择父分组）
const rootGroups = computed(() => {
  return groups.value.filter(g => !g.parent_id)
})

// 构建树形数据
const buildTree = (data: FieldGroup[]): TreeNode[] => {
  const map: { [key: number]: TreeNode } = {}
  const roots: TreeNode[] = []

  // 先创建所有节点
  data.forEach(item => {
    map[item.id] = {
      id: item.id,
      group_name: item.group_name,
      group_key: item.group_key,
      group_name_en: item.group_name_en,
      parent_id: item.parent_id,
      sort_order: item.sort_order,
      children: []
    }
  })

  // 构建树形结构
  data.forEach(item => {
    if (item.parent_id && map[item.parent_id]) {
      map[item.parent_id].children!.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })

  // 排序
  const sortNodes = (nodes: TreeNode[]) => {
    nodes.sort((a, b) => a.sort_order - b.sort_order)
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        sortNodes(node.children)
      }
    })
  }
  sortNodes(roots)

  return roots
}

const loadGroups = async () => {
  try {
    const res = await getFieldGroups()
    // API直接返回数组，不是{data: [...]}格式
    groups.value = Array.isArray(res) ? res : (res.data || [])
    treeData.value = buildTree(groups.value)
  } catch (error) {
    ElMessage.error('加载分组失败')
  }
}

const handleNodeClick = (data: TreeNode) => {
  currentGroup.value = groups.value.find(g => g.id === data.id) || null
}

// 添加一级分组
const handleAdd = () => {
  dialogTitle.value = '添加一级分组'
  form.value = {
    group_name: '',
    group_key: '',
    group_name_en: '',
    parent_id: null,
    sort_order: rootGroups.value.length
  }
  dialogVisible.value = true
}

// 添加子分组
const handleAddChild = (parent?: TreeNode) => {
  const parentGroup = parent || currentGroup.value
  if (!parentGroup) {
    ElMessage.warning('请先选择一个一级分组')
    return
  }
  
  dialogTitle.value = `添加子分组（父分组：${parentGroup.group_name}）`
  const childCount = groups.value.filter(g => g.parent_id === parentGroup.id).length
  form.value = {
    group_name: '',
    group_key: '',
    group_name_en: '',
    parent_id: parentGroup.id,
    sort_order: childCount
  }
  dialogVisible.value = true
}

const handleEdit = (data: TreeNode) => {
  const group = groups.value.find(g => g.id === data.id)
  if (!group) return
  
  dialogTitle.value = '编辑分组'
  form.value = {
    group_name: group.group_name,
    group_key: group.group_key,
    group_name_en: group.group_name_en || '',
    parent_id: group.parent_id,
    sort_order: group.sort_order
  }
  dialogVisible.value = true
}

const handleSave = () => {
  if (!form.value.group_name || !form.value.group_key) {
    ElMessage.warning('请填写完整信息')
    return
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  // TODO: 调用API保存数据
}

const handleDelete = (data: TreeNode) => {
  ElMessageBox.confirm(
    `确定要删除分组"${data.group_name}"吗？${data.children && data.children.length > 0 ? '子分组也会被删除。' : ''}`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
    // TODO: 调用API删除数据
  }).catch(() => {
    // 取消删除
  })
}

// 拖拽控制：只允许同级拖动
const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 只允许在同级节点之间拖动（before/after）
  if (type === 'inner') {
    return false // 不允许拖入成为子节点
  }
  // 检查是否同级
  const draggingParentId = draggingNode.data.parent_id || null
  const dropParentId = dropNode.data.parent_id || null
  return draggingParentId === dropParentId
}

// 拖拽完成后更新排序
const handleDrop = async (draggingNode: any, dropNode: any, dropType: string) => {
  try {
    // dropType: 'before' | 'after' | 'inner'
    // 只处理同级排序（before/after），不允许拖入成为子节点
    if (dropType === 'inner') {
      ElMessage.warning('不允许将分组拖入其他分组')
      await loadGroups() // 恢复原状态
      return
    }

    const draggingData = draggingNode.data
    const dropData = dropNode.data
    
    // 确保是同级节点
    const draggingParentId = draggingData.parent_id || null
    const dropParentId = dropData.parent_id || null
    
    if (draggingParentId !== dropParentId) {
      ElMessage.warning('只能在同一层级内排序')
      await loadGroups() // 恢复原状态
      return
    }

    // 获取同级所有节点
    const siblings = draggingParentId 
      ? groups.value.filter(g => g.parent_id === draggingParentId)
      : groups.value.filter(g => !g.parent_id)
    
    // 找到拖拽节点和目标节点在同级中的位置
    const draggingIndex = siblings.findIndex(g => g.id === draggingData.id)
    const dropIndex = siblings.findIndex(g => g.id === dropData.id)
    
    // 计算新位置
    let newIndex: number
    if (dropType === 'before') {
      newIndex = dropIndex
    } else { // 'after'
      newIndex = dropIndex + 1
    }
    
    // 如果向后移动，需要减1（因为元素被移除后索引会变化）
    if (draggingIndex < newIndex) {
      newIndex -= 1
    }
    
    // 更新数组顺序
    const movedItem = siblings.splice(draggingIndex, 1)[0]
    siblings.splice(newIndex, 0, movedItem)
    
    // 更新所有同级节点的 sort_order
    const sortUpdates: { id: number; sort_order: number }[] = []
    siblings.forEach((group, index) => {
      group.sort_order = index + 1
      sortUpdates.push({
        id: group.id,
        sort_order: index + 1
      })
    })
    
    // 调用API保存排序
    try {
      await updateFieldGroupsSort(sortUpdates)
      ElMessage.success('排序已保存')
      
      // 重新加载数据以刷新显示
      await loadGroups()
    } catch (error) {
      console.error('保存排序失败：', error)
      ElMessage.error('保存排序失败，请重试')
      // 恢复原状态
      await loadGroups()
    }
  } catch (error) {
    console.error('拖拽处理失败：', error)
    ElMessage.error('排序更新失败')
    await loadGroups() // 恢复原状态
  }
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.groups-container {
  margin-top: 20px;
}

.group-tree {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}

.group-tree :deep(.el-tree-node__content) {
  height: auto;
  min-height: 50px;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

.group-tree :deep(.el-tree-node__content:hover) {
  background-color: #ecf5ff;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 20px;
}

.node-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.node-label {
  font-size: 15px;
  font-weight: 500;
  margin-right: 12px;
}

.node-key {
  font-size: 13px;
  color: #909399;
  font-family: 'Courier New', monospace;
}

.node-actions {
  display: flex;
  gap: 8px;
}

/* 一级分组样式 */
.group-tree :deep(.el-tree-node__content) > .el-tree-node__expand-icon {
  color: #409eff;
  font-size: 16px;
}

/* 子分组缩进效果 */
.group-tree :deep(.el-tree-node__children) {
  background: #ffffff;
  padding-left: 20px;
  margin-top: 5px;
  border-left: 2px solid #409eff;
}

/* 拖拽样式 */
.group-tree :deep(.el-tree-node.is-drop-inner) {
  background-color: #f0f9ff;
  border: 2px dashed #409eff;
}

.drag-handle {
  cursor: move;
  transition: color 0.3s;
}

.drag-handle:hover {
  color: #409eff !important;
}

/* 拖拽时的节点样式 */
.group-tree :deep(.el-tree-node.is-dragging) {
  opacity: 0.5;
  background-color: #ecf5ff;
}

.group-tree :deep(.el-tree-node.is-drop-inner > .el-tree-node__content) {
  background-color: #f0f9ff;
  border: 2px dashed #409eff;
}
</style>

