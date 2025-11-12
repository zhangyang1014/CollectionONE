<template>
  <div class="agency-working-hours">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>机构作息时间管理</h2>
        <span v-if="agencyInfo" class="agency-name">{{ agencyInfo.agency_name }}</span>
      </div>
    </div>

    <div class="content-section">
      <el-table :data="workingHours" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="day_of_week" label="星期" width="120" align="center">
          <template #default="{ row }">
            {{ getDayName(row.day_of_week) }}
          </template>
        </el-table-column>

        <el-table-column label="作息时间" min-width="400">
          <template #default="{ row }">
            <div class="time-slots-container">
              <el-tag
                v-for="(slot, index) in row.time_slots"
                :key="index"
                closable
                @close="removeTimeSlot(row, index)"
                style="margin-right: 8px; margin-bottom: 4px;"
              >
                {{ slot.start }} - {{ slot.end }}
              </el-tag>
              <el-button
                v-if="row.time_slots.length < 5"
                link
                type="primary"
                size="small"
                @click="addTimeSlot(row)"
                style="margin-left: 8px;"
              >
                + 添加时间段
              </el-button>
              <el-text v-if="row.time_slots.length === 0" type="info" size="small">
                暂无时间段
              </el-text>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleSaveRow(row)"
              :disabled="!hasRowChanges(row)"
            >
              保存
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加时间段对话框 -->
      <el-dialog
        v-model="timeSlotDialogVisible"
        title="添加时间段"
        width="400px"
        @close="handleTimeSlotDialogClose"
      >
        <el-form :model="timeSlotForm" label-width="100px">
          <el-form-item label="开始时间" required>
            <el-time-picker
              v-model="timeSlotForm.start"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间" required>
            <el-time-picker
              v-model="timeSlotForm.end"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="timeSlotDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddTimeSlot">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAgencyWorkingHours, updateSingleDayWorkingHours, getAgency } from '@/api/organization'
import type { AgencyWorkingHours, TimeSlot } from '@/types/organization'

const route = useRoute()
const router = useRouter()

const agencyId = computed(() => {
  const id = Number(route.params.id)
  console.log('Route params:', route.params, 'Parsed agencyId:', id)
  return id
})
const loading = ref(false)
const agencyInfo = ref<any>(null)
const workingHours = ref<AgencyWorkingHours[]>([])
const originalWorkingHours = ref<AgencyWorkingHours[]>([])

// 时间段对话框
const timeSlotDialogVisible = ref(false)
const currentEditingRow = ref<AgencyWorkingHours | null>(null)
const timeSlotForm = ref({
  start: '',
  end: ''
})

// 星期名称映射
const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const getDayName = (dayOfWeek: number) => {
  return dayNames[dayOfWeek] || `星期${dayOfWeek + 1}`
}

// 检查单行是否有修改
const hasRowChanges = (row: AgencyWorkingHours) => {
  const original = originalWorkingHours.value.find(orig => orig.day_of_week === row.day_of_week)
  if (!original) return false
  
  if (row.time_slots.length !== original.time_slots.length) return true
  
  for (let i = 0; i < row.time_slots.length; i++) {
    if (
      row.time_slots[i].start !== original.time_slots[i].start ||
      row.time_slots[i].end !== original.time_slots[i].end
    ) {
      return true
    }
  }
  
  return false
}

// 添加时间段
const addTimeSlot = (row: AgencyWorkingHours) => {
  if (row.time_slots.length >= 5) {
    ElMessage.warning('每天最多只能配置5个时间段')
    return
  }
  currentEditingRow.value = row
  timeSlotForm.value = { start: '', end: '' }
  timeSlotDialogVisible.value = true
}

// 确认添加时间段
const confirmAddTimeSlot = () => {
  if (!timeSlotForm.value.start || !timeSlotForm.value.end) {
    ElMessage.warning('请选择开始时间和结束时间')
    return
  }
  
  if (!currentEditingRow.value) return
  
  // 验证时间逻辑
  const startTime = timeSlotForm.value.start
  const endTime = timeSlotForm.value.end
  
  const startTotal = parseInt(startTime.split(':')[0]) * 60 + parseInt(startTime.split(':')[1])
  const endTotal = parseInt(endTime.split(':')[0]) * 60 + parseInt(endTime.split(':')[1])
  
  if (startTotal >= endTotal) {
    ElMessage.error('开始时间必须小于结束时间')
    return
  }
  
  // 检查是否与现有时间段重叠
  const hasOverlap = currentEditingRow.value.time_slots.some(slot => {
    const slotStart = parseInt(slot.start.split(':')[0]) * 60 + parseInt(slot.start.split(':')[1])
    const slotEnd = parseInt(slot.end.split(':')[0]) * 60 + parseInt(slot.end.split(':')[1])
    
    return (startTotal < slotEnd && endTotal > slotStart)
  })
  
  if (hasOverlap) {
    ElMessage.error('时间段不能重叠')
    return
  }
  
  // 添加时间段
  currentEditingRow.value.time_slots.push({
    start: timeSlotForm.value.start,
    end: timeSlotForm.value.end
  })
  
  // 按开始时间排序
  currentEditingRow.value.time_slots.sort((a, b) => {
    const aStart = parseInt(a.start.split(':')[0]) * 60 + parseInt(a.start.split(':')[1])
    const bStart = parseInt(b.start.split(':')[0]) * 60 + parseInt(b.start.split(':')[1])
    return aStart - bStart
  })
  
  timeSlotDialogVisible.value = false
}

// 删除时间段
const removeTimeSlot = (row: AgencyWorkingHours, index: number) => {
  row.time_slots.splice(index, 1)
}

// 关闭时间段对话框
const handleTimeSlotDialogClose = () => {
  currentEditingRow.value = null
  timeSlotForm.value = { start: '', end: '' }
}

// 保存单行
const handleSaveRow = async (row: AgencyWorkingHours) => {
  if (!hasRowChanges(row)) {
    ElMessage.info('该行没有需要保存的修改')
    return
  }

  try {
    loading.value = true
    
    await updateSingleDayWorkingHours(agencyId.value, row.day_of_week, {
      time_slots: row.time_slots
    })
    
    // 更新原始数据
    const originalIndex = originalWorkingHours.value.findIndex(orig => orig.day_of_week === row.day_of_week)
    if (originalIndex !== -1) {
      originalWorkingHours.value[originalIndex] = JSON.parse(JSON.stringify(row))
    }
    
    ElMessage.success('保存成功')
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.push('/organization/agencies')
}

// 加载机构信息
const loadAgencyInfo = async () => {
  try {
    const response: any = await getAgency(agencyId.value)
    agencyInfo.value = response.data || response
  } catch (error) {
    console.error('加载机构信息失败：', error)
  }
}

// 加载作息时间
const loadWorkingHours = async () => {
  try {
    loading.value = true
    console.log('Loading working hours for agency:', agencyId.value)
    const response: any = await getAgencyWorkingHours(agencyId.value)
    console.log('Working hours response:', response)
    
    // 确保有7天的数据
    const data = Array.isArray(response) ? response : (response.data || [])
    console.log('Processed data:', data)
    const daysMap = new Map(data.map((wh: AgencyWorkingHours) => [wh.day_of_week, wh]))
    
    workingHours.value = []
    for (let day = 0; day < 7; day++) {
      if (daysMap.has(day)) {
        workingHours.value.push(daysMap.get(day)!)
      } else {
        workingHours.value.push({
          id: 0,
          agency_id: agencyId.value,
          day_of_week: day,
          time_slots: [],
          created_at: '',
          updated_at: ''
        })
      }
    }
    
    // 保存原始数据用于比较
    originalWorkingHours.value = JSON.parse(JSON.stringify(workingHours.value))
  } catch (error: any) {
    console.error('加载作息时间失败：', error)
    ElMessage.error('加载作息时间失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAgencyInfo()
  loadWorkingHours()
})
</script>

<style scoped>
.agency-working-hours {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.agency-name {
  color: #909399;
  font-size: 14px;
}

.content-section {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.time-slots-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  min-height: 32px;
}
</style>

