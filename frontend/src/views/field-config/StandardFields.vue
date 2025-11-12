<template>
  <div class="standard-fields">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标准字段管理</span>
          <el-button type="primary" @click="handleAdd">添加字段</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="never">
            <template #header>字段分组</template>
            <el-tree
              :data="treeData"
              :props="{ label: 'group_name', children: 'children' }"
              node-key="id"
              :default-expand-all="true"
              :expand-on-click-node="false"
              @node-click="handleGroupClick"
              highlight-current
              class="field-group-tree"
            />
          </el-card>
        </el-col>

        <el-col :span="18">
          <div class="table-header">
            <el-alert
              title="提示：可以通过拖拽表格行来调整字段排序"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
          <el-table 
            ref="tableRef"
            :data="fields" 
            row-key="id"
            border 
            style="width: 100%"
            class="sortable-table"
          >
            <el-table-column width="50" align="center">
              <template #default>
                <el-icon class="drag-handle" style="cursor: move;">
                  <Rank />
                </el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="field_name" label="字段名称" />
            <el-table-column prop="field_key" label="字段标识" />
            <el-table-column prop="field_type" label="字段类型" width="100" />
            <el-table-column prop="enum_values" label="枚举值" width="200">
              <template #default="{ row }">
                <span v-if="row.field_type === 'Enum' && row.enum_values && row.enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in row.enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px"
                  >
                    {{ item.standard_name }}
                  </el-tag>
                  <el-tag v-if="row.enum_values.length > 2" size="small" type="info">
                    +{{ row.enum_values.length - 2 }}
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="is_required" label="是否必填" width="100">
              <template #default="{ row }">
                <el-tag :type="row.is_required ? 'success' : 'info'">
                  {{ row.is_required ? '必填' : '非必填' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort_order" label="排序" width="80" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="字段名称">
          <el-input v-model="form.field_name" />
        </el-form-item>
        <el-form-item label="字段标识">
          <el-input v-model="form.field_key" />
        </el-form-item>
        <el-form-item label="字段类型">
          <el-select v-model="form.field_type" @change="handleFieldTypeChange">
            <el-option label="文本" value="String" />
            <el-option label="整数" value="Integer" />
            <el-option label="小数" value="Decimal" />
            <el-option label="日期" value="Date" />
            <el-option label="日期时间" value="Datetime" />
            <el-option label="布尔" value="Boolean" />
            <el-option label="枚举" value="Enum" />
          </el-select>
        </el-form-item>

        <!-- 枚举值配置 -->
        <el-form-item v-if="form.field_type === 'Enum'" label="枚举值配置">
          <div class="enum-config">
            <div class="enum-header">
              <el-button type="primary" size="small" @click="handleAddEnumValue">
                添加枚举项
              </el-button>
            </div>
            <el-table :data="form.enum_values" border style="width: 100%; margin-top: 10px">
              <el-table-column label="标准名称" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.standard_name" size="small" placeholder="如：待还款" />
                </template>
              </el-table-column>
              <el-table-column label="标准ID" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.standard_id" size="small" placeholder="如：pending" />
                </template>
              </el-table-column>
              <el-table-column label="甲方名称" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.tenant_name" size="small" placeholder="如：未还款" />
                </template>
              </el-table-column>
              <el-table-column label="甲方ID" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.tenant_id" size="small" placeholder="如：unpaid" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button 
                    link 
                    type="danger" 
                    size="small" 
                    @click="handleRemoveEnumValue($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>

        <el-form-item label="所属分组">
          <el-cascader
            v-model="form.field_group_path"
            :options="treeData"
            :props="{
              value: 'id',
              label: 'group_name',
              children: 'children',
              checkStrictly: true,
              emitPath: false
            }"
            placeholder="请选择分组"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="form.is_required" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort_order" :min="0" />
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import {
  getFieldGroups,
  getStandardFields,
  createStandardField,
  updateStandardField,
  deleteStandardField,
} from '@/api/field'
import type { StandardField, FieldGroup } from '@/types'

const fields = ref<StandardField[]>([])
const allGroups = ref<FieldGroup[]>([])
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const currentGroupId = ref<number>()
const tableRef = ref()
let sortableInstance: any = null

const form = ref({
  field_name: '',
  field_key: '',
  field_type: 'String',
  field_group_path: 0,
  is_required: false,
  is_extended: false,
  sort_order: 0,
  enum_values: [] as Array<{
    standard_name: string
    standard_id: string
    tenant_name: string
    tenant_id: string
  }>
})

const loadGroups = async () => {
  try {
    const res = await getFieldGroups()
    // API直接返回数组，不是{data: [...]}格式
    allGroups.value = Array.isArray(res) ? res : (res.data || [])
    treeData.value = buildTree(allGroups.value)
    
    // 自动选中第一个分组
    if (treeData.value.length > 0) {
      currentGroupId.value = treeData.value[0].id
      loadFields(treeData.value[0].id)
    }
  } catch (error) {
    ElMessage.error('加载分组失败')
  }
}

const buildTree = (groups: FieldGroup[]) => {
  const map = new Map()
  const roots: any[] = []

  groups.forEach((group) => {
    map.set(group.id, { ...group, children: [] })
  })

  groups.forEach((group) => {
    const node = map.get(group.id)
    if (group.parent_id) {
      const parent = map.get(group.parent_id)
      if (parent) {
        parent.children.push(node)
      }
    } else {
      roots.push(node)
    }
  })

  return roots
}

const loadFields = async (groupId?: number) => {
  try {
    const params = groupId ? { field_group_id: groupId } : {}
    const res = await getStandardFields(params)
    // API直接返回数组，不是{data: [...]}格式
    fields.value = Array.isArray(res) ? res : (res.data || [])
    // 加载完成后初始化拖拽
    await nextTick()
    initSortable()
  } catch (error) {
    console.error('加载字段失败：', error)
    ElMessage.error('加载字段失败')
  }
}

// 初始化拖拽排序
const initSortable = () => {
  // 销毁旧的实例
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }

  const table = tableRef.value?.$el.querySelector('.el-table__body-wrapper tbody')
  if (!table) {
    console.warn('未找到表格tbody元素')
    return
  }

  console.log('初始化Sortable，表格元素：', table)

  sortableInstance = Sortable.create(table, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'sortable-ghost',
    chosenClass: 'sortable-chosen',
    dragClass: 'sortable-drag',
    forceFallback: true,
    fallbackTolerance: 3,
    onStart: (evt: any) => {
      console.log('开始拖拽，索引：', evt.oldIndex)
    },
    onEnd: (evt: any) => {
      const { oldIndex, newIndex } = evt
      console.log('拖拽结束，从', oldIndex, '到', newIndex)
      
      if (oldIndex === newIndex) return

      // 更新数组顺序
      const movedItem = fields.value.splice(oldIndex, 1)[0]
      fields.value.splice(newIndex, 0, movedItem)

      // 更新所有字段的 sort_order
      fields.value.forEach((field, index) => {
        field.sort_order = index + 1
      })

      ElMessage.success('排序已更新')
      console.log('新的排序：', fields.value.map(f => f.field_name))
      // TODO: 调用API保存新的排序
    },
  })
  
  console.log('Sortable初始化完成')
}

const handleGroupClick = (data: any) => {
  currentGroupId.value = data.id
  loadFields(data.id)
}

const handleAdd = () => {
  dialogTitle.value = '添加标准字段'
  form.value = {
    field_name: '',
    field_key: '',
    field_type: 'String',
    field_group_path: currentGroupId.value || 0,
    is_required: false,
    is_extended: false,
    sort_order: 0,
    enum_values: []
  }
  dialogVisible.value = true
}

const handleEdit = (row: StandardField) => {
  dialogTitle.value = '编辑标准字段'
  form.value = { 
    ...row,
    field_group_path: row.field_group_id, // 编辑时将 field_group_id 赋值给 field_group_path
    enum_values: row.enum_values || [] // 确保枚举值正确加载
  }
  dialogVisible.value = true
}

// 字段类型变更处理
const handleFieldTypeChange = (value: string) => {
  if (value === 'Enum' && form.value.enum_values.length === 0) {
    // 如果切换到枚举类型且没有枚举值，添加一个默认项
    form.value.enum_values = [{
      standard_name: '',
      standard_id: '',
      tenant_name: '',
      tenant_id: ''
    }]
  }
}

// 添加枚举项
const handleAddEnumValue = () => {
  form.value.enum_values.push({
    standard_name: '',
    standard_id: '',
    tenant_name: '',
    tenant_id: ''
  })
}

// 删除枚举项
const handleRemoveEnumValue = (index: number) => {
  form.value.enum_values.splice(index, 1)
}

const handleDelete = async (row: StandardField) => {
  try {
    await ElMessageBox.confirm('确定删除该字段吗？', '提示', {
      type: 'warning',
    })
    await deleteStandardField(row.id)
    ElMessage.success('删除成功')
    loadFields(currentGroupId.value)
  } catch (error) {
    // 用户取消
  }
}

const handleSubmit = async () => {
  try {
    // 准备提交数据：将 field_group_path 转换为 field_group_id
    const submitData = {
      ...form.value,
      field_group_id: form.value.field_group_path
    }
    delete submitData.field_group_path
    
    if (form.value.id) {
      await updateStandardField(form.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createStandardField(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadFields(currentGroupId.value)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadGroups()
  loadFields()
})

// 组件卸载时销毁Sortable实例
onBeforeUnmount(() => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header {
  margin-bottom: 15px;
}

.field-group-tree {
  cursor: pointer;
  user-select: none;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 40px;
  cursor: pointer;
  padding: 0 10px;
  transition: all 0.3s;
}

.field-group-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.field-group-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.field-group-tree :deep(.el-tree-node__label) {
  cursor: pointer;
  flex: 1;
}

/* 拖拽排序样式 */
.sortable-table :deep(.el-table__row) {
  cursor: move;
  transition: background-color 0.3s;
}

.sortable-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.drag-handle {
  color: #909399;
  font-size: 18px;
  transition: color 0.3s;
}

.drag-handle:hover {
  color: #409eff;
}

/* 拖拽时的幽灵元素样式 */
.sortable-ghost {
  opacity: 0.5;
  background: #ecf5ff !important;
}

/* 拖拽时选中的行样式 */
.sortable-table :deep(.sortable-chosen) {
  background-color: #f0f9ff !important;
}

/* 拖拽时正在移动的行 */
.sortable-table :deep(.sortable-drag) {
  background-color: #ecf5ff !important;
  opacity: 1 !important;
  border: 2px solid #409eff !important;
}

/* 确保拖拽时表格行可见 */
.sortable-table :deep(tr) {
  transition: all 0.3s;
}

.sortable-table :deep(tr.sortable-chosen td) {
  background-color: #f0f9ff !important;
}

/* 枚举配置样式 */
.enum-config {
  width: 100%;
}

.enum-header {
  margin-bottom: 10px;
}

.enum-config :deep(.el-table) {
  font-size: 13px;
}

.enum-config :deep(.el-input__inner) {
  font-size: 12px;
}
</style>

