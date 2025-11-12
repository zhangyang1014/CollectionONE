<template>
  <div class="queue-dashboard">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>队列总览</span>
          <el-space>
            <el-select v-model="selectedQueue" placeholder="选择队列" style="width: 150px" @change="loadDashboard">
              <el-option
                v-for="queue in queues"
                :key="queue.id"
                :label="queue.queue_name"
                :value="queue.id"
              />
            </el-select>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              @change="loadDashboard"
              style="width: 150px"
            />
            <el-button type="primary" :icon="Refresh" @click="loadDashboard">刷新</el-button>
            <el-button :icon="Download" @click="exportData">导出</el-button>
          </el-space>
        </div>
      </template>

      <!-- 指标卡片 -->
      <el-row :gutter="20" class="metrics-row">
        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-icon" style="background: #409eff20;">
              <el-icon :size="32" color="#409eff"><Clock /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">自动分案时间</div>
              <div class="metric-value">{{ dashboardData.schedule_time || '--' }}</div>
              <div class="metric-sub">下次执行：{{ dashboardData.next_run || '--' }}</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-icon" style="background: #67c23a20;">
              <el-icon :size="32" color="#67c23a"><DocumentCopy /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">今日计划入催</div>
              <div class="metric-value">{{ dashboardData.planned_cases || 0 }}</div>
              <div class="metric-sub">不含 IVR 预催</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-icon" style="background: #e6a23c20;">
              <el-icon :size="32" color="#e6a23c"><Check /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">已分配案件数</div>
              <div class="metric-value">{{ dashboardData.assigned_cases || 0 }}</div>
              <div class="metric-sub">
                分配率：{{ dashboardData.assignment_rate || 0 }}%
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="metric-card">
            <div class="metric-icon" style="background: #f56c6c20;">
              <el-icon :size="32" color="#f56c6c"><User /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">平均持案数</div>
              <div class="metric-value">{{ dashboardData.avg_cases_per_collector || 0 }}</div>
              <div class="metric-sub">
                参与催员：{{ dashboardData.active_collectors || 0 }} 人
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 剩余待分配 -->
      <el-alert
        v-if="dashboardData.remaining_cases > 0"
        :title="`剩余待分配案件：${dashboardData.remaining_cases} 件`"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top: 20px"
      />

      <!-- 口径说明 -->
      <el-collapse style="margin-top: 20px">
        <el-collapse-item title="指标口径说明" name="1">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="今日计划入催">
              统计当日进入该队列的案件总数，C 队列已剔除 C-2（IVR 预催）部分
            </el-descriptions-item>
            <el-descriptions-item label="已分配案件数">
              已成功分配至催员的案件数量
            </el-descriptions-item>
            <el-descriptions-item label="平均持案数">
              已分配案件数 ÷ 参与分配的催员数（仅统计已启用且绑定该队列的催员）
            </el-descriptions-item>
            <el-descriptions-item label="剩余待分配">
              计划入催 - 已分配案件数
            </el-descriptions-item>
            <el-descriptions-item label="数据刷新频率">
              每 5 分钟自动刷新一次
            </el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
      </el-collapse>

      <!-- 历史趋势图 -->
      <el-card style="margin-top: 20px" shadow="never">
        <template #header>
          <span>近 7 日分配趋势</span>
        </template>
        <div ref="chartRef" style="height: 300px"></div>
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download, Clock, DocumentCopy, Check, User } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'

const tenantStore = useTenantStore()

// 队列列表
const queues = ref([
  { id: 1, queue_code: 'C', queue_name: 'C队列' },
  { id: 2, queue_code: 'S0', queue_name: 'S0队列' },
  { id: 3, queue_code: 'S1', queue_name: 'S1队列' },
  { id: 4, queue_code: 'L1', queue_name: 'L1队列' },
  { id: 5, queue_code: 'M1', queue_name: 'M1队列' },
])

const selectedQueue = ref(1)
const selectedDate = ref(new Date())
const chartRef = ref()

// Mock 数据
const dashboardData = ref({
  schedule_time: '每日 09:00',
  next_run: '2025-11-13 09:00:00',
  planned_cases: 1250,
  assigned_cases: 1180,
  assignment_rate: 94.4,
  remaining_cases: 70,
  active_collectors: 25,
  avg_cases_per_collector: 47.2,
})

// 加载仪表盘数据
const loadDashboard = async () => {
  if (!tenantStore.currentTenantId) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    // TODO: 调用实际 API
    // const res = await getQueueDashboard(selectedQueue.value, selectedDate.value)
    
    // Mock 数据
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('加载仪表盘失败：', error)
    ElMessage.error('加载仪表盘失败')
  }
}

// 导出数据
const exportData = () => {
  ElMessage.info('导出功能开发中...')
}

// Mock 趋势数据
const trendData = ref({
  dates: ['11-06', '11-07', '11-08', '11-09', '11-10', '11-11', '11-12'],
  planned: [1180, 1220, 1200, 1280, 1150, 1300, 1250],
  assigned: [1150, 1200, 1180, 1250, 1120, 1270, 1180],
  avgCases: [45.2, 47.1, 46.3, 49.0, 44.0, 49.8, 47.2],
})

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 简单的 CSS 绘制图表（不依赖 ECharts）
  const chartHtml = `
    <div style="padding: 20px;">
      <div style="display: flex; justify-content: space-around; margin-bottom: 20px;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <div style="width: 12px; height: 12px; background: #409eff; border-radius: 2px;"></div>
          <span style="font-size: 14px; color: #606266;">计划入催</span>
        </div>
        <div style="display: flex; align-items: center; gap: 8px;">
          <div style="width: 12px; height: 12px; background: #67c23a; border-radius: 2px;"></div>
          <span style="font-size: 14px; color: #606266;">已分配</span>
        </div>
        <div style="display: flex; align-items: center; gap: 8px;">
          <div style="width: 12px; height: 12px; background: #e6a23c; border-radius: 2px;"></div>
          <span style="font-size: 14px; color: #606266;">平均持案数</span>
        </div>
      </div>
      <div style="display: flex; align-items: flex-end; justify-content: space-around; height: 200px; border-bottom: 1px solid #dcdfe6; border-left: 1px solid #dcdfe6; padding: 10px;">
        ${trendData.value.dates.map((date, index) => `
          <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 5px;">
            <div style="display: flex; align-items: flex-end; gap: 3px; height: 180px;">
              <div style="width: 20px; background: #409eff; opacity: 0.8; height: ${(trendData.value.planned[index] / 1300) * 100}%; border-radius: 4px 4px 0 0;" title="计划入催: ${trendData.value.planned[index]}"></div>
              <div style="width: 20px; background: #67c23a; opacity: 0.8; height: ${(trendData.value.assigned[index] / 1300) * 100}%; border-radius: 4px 4px 0 0;" title="已分配: ${trendData.value.assigned[index]}"></div>
              <div style="width: 20px; background: #e6a23c; opacity: 0.8; height: ${(trendData.value.avgCases[index] / 50) * 100}%; border-radius: 4px 4px 0 0;" title="平均持案: ${trendData.value.avgCases[index]}"></div>
            </div>
            <span style="font-size: 12px; color: #909399;">${date}</span>
          </div>
        `).join('')}
      </div>
      <div style="text-align: center; margin-top: 10px; color: #909399; font-size: 12px;">
        提示：鼠标悬停在柱状图上可查看具体数值
      </div>
    </div>
  `
  
  chartRef.value.innerHTML = chartHtml
}

onMounted(() => {
  loadDashboard()
  setTimeout(() => {
    initChart()
  }, 100)
})
</script>

<style scoped>
.queue-dashboard {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.metric-card:hover {
  transform: translateY(-5px);
}

.metric-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.metric-sub {
  font-size: 12px;
  color: #909399;
}
</style>

