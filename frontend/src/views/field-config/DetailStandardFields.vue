<template>
  <div class="detail-standard-fields">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情标准字段管理（只读）</span>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="字段由CCO统一维护，所有甲方继承，仅支持查看"
        description="不可新增、编辑或删除，展示当前案件详情页的标准字段。"
        class="tip-alert"
      />

      <el-row :gutter="20" style="margin-top: 10px">
        <el-col :span="5">
          <el-card shadow="never">
            <template #header>字段分组</template>
            <el-tree
              :data="groupTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="key"
              :default-expand-all="true"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleGroupClick"
              class="field-group-tree"
            />
          </el-card>
        </el-col>
        <el-col :span="19">
          <el-table :data="filteredFields" border style="width: 100%" v-loading="loading" class="block-table">
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="fieldName" label="字段名称" min-width="160" />
            <el-table-column prop="fieldKey" label="字段标识" min-width="180" />
            <el-table-column prop="fieldDataType" label="数据类型" width="110" />
            <el-table-column label="必填" width="90">
              <template #default="{ row }">
                <el-tag :type="row.required ? 'success' : 'info'">
                  {{ row.required ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="90" />
            <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getCaseDetailStandardFields } from '@/api/field'
import { getDetailFieldGroups } from '@/api/detailFieldGroup'
import { useTenantStore } from '@/stores/tenant'

type CaseDetailField = {
  id: number
  sceneType: string
  sceneName: string
  fieldKey: string
  fieldName: string
  fieldDataType: string
  fieldSource: string
  sortOrder: number
  required?: boolean
  description?: string
  field_group_id?: number
}

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)

const fields = ref<CaseDetailField[]>([])
const allGroups = ref<any[]>([])
const loading = ref(false)
const activeGroup = ref<string | number>('all')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCaseDetailStandardFields()
    const raw = Array.isArray(res) ? res : (res?.data || [])
    fields.value = raw.map((item: any) => normalizeField(item))
  } catch (e: any) {
    ElMessage.error(e?.message || '加载标准字段失败')
  } finally {
    loading.value = false
  }
}

// 加载详情字段分组
const loadGroups = async () => {
  try {
    const data = await getDetailFieldGroups({ tenantId: Number(currentTenantId.value) })
    allGroups.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (e) {
    console.error('加载分组失败：', e)
    allGroups.value = []
  }
}

const normalizeField = (item: any): CaseDetailField => {
  return {
    id: Number(item.id ?? 0),
    sceneType: item.sceneType ?? item.scene_type ?? '',
    sceneName: item.sceneName ?? item.scene_name ?? '',
    fieldKey: item.fieldKey ?? item.field_key ?? '',
    fieldName: item.fieldName ?? item.field_name ?? '',
    fieldDataType: item.fieldDataType ?? item.field_data_type ?? '',
    fieldSource: item.fieldSource ?? item.field_source ?? '',
    sortOrder: Number(item.sortOrder ?? item.sort_order ?? 0),
    required: Boolean(item.required ?? item.is_required ?? false),
    description: item.description ?? '',
    field_group_id: item.field_group_id ?? item.fieldGroupId ?? null,
  }
}

// 构建分组树
const groupTree = computed(() => {
  const roots = allGroups.value.filter(g => !g.parent_id)
  const buildChildren = (parentId: number) => {
    return allGroups.value
      .filter(g => g.parent_id === parentId)
      .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
      .map(g => ({
        key: g.id,
        label: g.group_name,
        groupKey: g.group_key,
        children: buildChildren(g.id)
      }))
  }
  
  const tree = roots
    .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    .map(g => ({
      key: g.id,
      label: g.group_name,
      groupKey: g.group_key,
      children: buildChildren(g.id)
    }))
  
  return [{ key: 'all', label: '全部', children: tree }]
})

// 获取所有选中分组及其子分组的ID
const getGroupAndChildrenIds = (groupId: number): number[] => {
  const ids = [groupId]
  const children = allGroups.value.filter(g => g.parent_id === groupId)
  children.forEach(child => {
    ids.push(...getGroupAndChildrenIds(child.id))
  })
  return ids
}

const filteredFields = computed(() => {
  if (activeGroup.value === 'all') return fields.value
  const groupIds = getGroupAndChildrenIds(Number(activeGroup.value))
  return fields.value.filter(f => f.field_group_id && groupIds.includes(f.field_group_id))
})

const handleGroupClick = (node: any) => {
  activeGroup.value = node.key
}

onMounted(() => {
  loadGroups()
  loadData()
})
</script>

<style scoped>
.card-header {
  font-weight: 600;
  font-size: 16px;
}

.tip-alert {
  margin-bottom: 12px;
}

.block-table :deep(.el-table__row) {
  height: 60px;
}

.block-table :deep(.el-table__cell) {
  padding: 14px 12px;
  font-size: 14px;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 36px;
}
</style>

