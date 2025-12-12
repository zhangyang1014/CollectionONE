<template>
  <div class="standard-fields-from-csv">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-info">
            <span class="title">案件详情标准字段查看（基于CSV）</span>
            <el-tag type="success" size="small" style="margin-left: 10px;">
              标准字段参考
            </el-tag>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧分组标签 -->
        <el-col :span="24">
          <el-card shadow="never" class="group-tabs-card">
            <div class="group-tabs-wrapper">
              <el-space wrap>
                <el-button
                  v-for="group in allGroupsFlat"
                  :key="group.group_key"
                  :type="activeGroup === group.group_key ? 'primary' : 'default'"
                  :plain="activeGroup !== group.group_key"
                  @click="handleGroupClick(group.group_key)"
                  size="default"
                >
                  {{ group.group_name }}
                  <el-tag
                    v-if="group.field_count > 0"
                    :type="activeGroup === group.group_key ? 'success' : 'info'"
                    size="small"
                    style="margin-left: 8px;"
                  >
                    {{ group.field_count }}
                  </el-tag>
                </el-button>
              </el-space>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧字段表格 -->
        <el-col :span="24" style="margin-top: 20px;">
          <div v-loading="loading">
            <!-- 当前分组信息 -->
            <el-alert
              :title="currentGroupInfo"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 15px"
            />

            <!-- 搜索栏 -->
            <el-row :gutter="10" style="margin-bottom: 15px">
              <el-col :span="8">
                <el-input
                  v-model="searchText"
                  placeholder="搜索字段名称或标识"
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-select v-model="filterType" placeholder="筛选字段类型" clearable>
                  <el-option label="全部类型" value="" />
                  <el-option label="String" value="String" />
                  <el-option label="Integer" value="Integer" />
                  <el-option label="Decimal" value="Decimal" />
                  <el-option label="Date" value="Date" />
                  <el-option label="Datetime" value="Datetime" />
                  <el-option label="Enum" value="Enum" />
                  <el-option label="Boolean" value="Boolean" />
                  <el-option label="Button" value="Button" />
                  <el-option label="FileList" value="FileList" />
                </el-select>
              </el-col>
              <el-col :span="6">
                <el-select v-model="filterExtension" placeholder="筛选拓展字段" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="标准字段" value="false" />
                  <el-option label="拓展字段" value="true" />
                </el-select>
              </el-col>
            </el-row>

            <!-- 字段表格 -->
            <el-table :data="paginatedFields" border style="width: 100%" class="block-table">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column 
                v-if="activeGroup === 'all'" 
                prop="group_name" 
                label="分组" 
                width="140" 
              />
              <el-table-column prop="field_name" label="字段名称" min-width="150" />
              <el-table-column prop="field_key" label="字段标识" min-width="180" />
              <el-table-column prop="field_type" label="字段类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getFieldTypeTag(row.field_type)" size="small">
                    {{ row.field_type }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="枚举值" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  <span v-if="row.field_type === 'Enum' && row.enum_values">
                    {{ formatEnumValues(row.enum_values) }}
                  </span>
                  <span v-else style="color: #c0c4cc;">-</span>
                </template>
              </el-table-column>
              <el-table-column label="示例" min-width="160" show-overflow-tooltip>
                <template #default="{ row }">
                  <span v-if="row.example" style="color: #909399; font-size: 12px;">
                    {{ row.example }}
                  </span>
                  <span v-else style="color: #c0c4cc;">-</span>
                </template>
              </el-table-column>
              <el-table-column label="拓展字段" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.is_extension ? 'warning' : 'success'" size="small">
                    {{ row.is_extension ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="filteredFields.length"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              style="margin-top: 15px; justify-content: flex-end"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { fieldGroups, getAllStandardFields, getFieldsByGroupKey, type StandardField } from '@/data/standardFieldsFromCSV'

// 状态
const loading = ref(false)
const activeGroup = ref('all')
const searchText = ref('')
const filterType = ref('')
const filterExtension = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// 所有字段
const allFields = ref<StandardField[]>([])

// 所有分组（扁平化，用于标签按钮）
const allGroupsFlat = computed(() => {
  const groups: any[] = []
  
  // 添加"全部"选项
  groups.push({
    group_key: 'all',
    group_name: '全部',
    field_count: allFields.value.length,
    parent_key: null
  })
  
  // 添加所有分组
  fieldGroups.forEach(group => {
    const fieldCount = group.fields.length
    
    // 如果有子分组，也要计算子分组的字段数
    const children = fieldGroups.filter(g => g.parent_key === group.group_key)
    const totalCount = fieldCount + children.reduce((sum, child) => sum + child.fields.length, 0)
    
    groups.push({
      group_key: group.group_key,
      group_name: group.group_name,
      field_count: fieldCount || totalCount,
      parent_key: group.parent_key
    })
  })
  
  return groups
})

// 当前分组信息
const currentGroupInfo = computed(() => {
  const group = allGroupsFlat.value.find(g => g.group_key === activeGroup.value)
  if (!group) return '未知分组'
  
  const fieldCount = filteredFields.value.length
  if (activeGroup.value === 'all') {
    return `显示全部字段，共 ${fieldCount} 个标准字段`
  }
  
  return `当前分组：${group.group_name}，共 ${fieldCount} 个字段`
})

// 筛选后的字段
const filteredFields = computed(() => {
  let result = [...allFields.value]
  
  // 分组筛选
  if (activeGroup.value !== 'all') {
    // 检查是否是父分组（客户基础信息）
    const isParentGroup = fieldGroups.find(g => 
      g.group_key === activeGroup.value && 
      !g.parent_key
    )
    
    if (isParentGroup) {
      // 如果是父分组，显示所有子分组的字段
      const childGroups = fieldGroups.filter(g => g.parent_key === activeGroup.value)
      const childGroupKeys = childGroups.map(g => g.group_key)
      result = result.filter(f => childGroupKeys.includes(f.group_key))
    } else {
      // 如果是子分组，只显示该分组的字段
      result = result.filter(f => f.group_key === activeGroup.value)
    }
  }
  
  // 搜索
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(f => 
      f.field_name?.toLowerCase().includes(search) || 
      f.field_key?.toLowerCase().includes(search)
    )
  }
  
  // 类型筛选
  if (filterType.value) {
    result = result.filter(f => f.field_type === filterType.value)
  }
  
  // 拓展字段筛选
  if (filterExtension.value !== '') {
    const isExtension = filterExtension.value === 'true'
    result = result.filter(f => f.is_extension === isExtension)
  }
  
  return result
})

// 分页后的字段
const paginatedFields = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFields.value.slice(start, end)
})

// 方法
const loadData = () => {
  loading.value = true
  try {
    allFields.value = getAllStandardFields()
    console.log('加载标准字段成功，共', allFields.value.length, '个')
  } catch (e) {
    console.error('加载标准字段失败：', e)
  } finally {
    loading.value = false
  }
}

const handleGroupClick = (groupKey: string) => {
  activeGroup.value = groupKey
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const formatEnumValues = (values: string[]) => {
  if (!values || values.length === 0) return '-'
  if (values.length <= 3) return values.join(' / ')
  return `${values.slice(0, 3).join(' / ')} 等${values.length}个`
}

const getFieldTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    'String': 'info',
    'Integer': 'success',
    'Decimal': 'warning',
    'Date': 'primary',
    'Datetime': 'primary',
    'Enum': 'danger',
    'Boolean': 'info',
    'Button': 'warning',
    'FileList': 'success'
  }
  return typeMap[type] || 'info'
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.standard-fields-from-csv {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.group-tabs-card {
  :deep(.el-card__body) {
    padding: 15px;
  }
}

.group-tabs-wrapper {
  .el-space {
    width: 100%;
  }

  .el-button {
    margin-bottom: 8px;
  }
}

.block-table {
  :deep(.el-table__header) {
    th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }
  }

  :deep(.el-table__body) {
    td {
      padding: 12px 0;
    }
  }
}

:deep(.el-pagination) {
  display: flex;
}
</style>


