<template>
  <el-dialog
    v-model="visible"
    title="WA生命周期记录"
    width="680px"
    @close="handleClose"
  >
    <!-- 号码信息 -->
    <div v-if="phone" class="phone-info">
      <el-tag type="info">{{ phone.instantId }}</el-tag>
      <span v-if="phone.phone" class="phone-number">{{ phone.phone }}</span>
      <el-tag :type="currentStatusType" size="small" style="margin-left: auto">
        {{ currentStatusLabel }}
      </el-tag>
    </div>

    <div v-loading="loading" class="timeline-wrap">
      <el-empty v-if="!loading && events.length === 0" description="暂无生命周期记录" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="event in events"
          :key="event.id"
          :timestamp="event.createdAt"
          placement="top"
          :type="getEventColor(event.eventType)"
          :hollow="isMinorEvent(event.eventType)"
          size="normal"
        >
          <div class="event-card">
            <div class="event-header">
              <span class="event-label">{{ event.eventLabel }}</span>
              <el-tag :type="getEventTagType(event.eventType)" size="small" class="event-type-tag">
                {{ EVENT_TYPE_CATEGORY[event.eventType] }}
              </el-tag>
            </div>
            <div class="event-detail">{{ event.detail }}</div>
            <div class="event-operator">
              <el-icon><User /></el-icon>
              {{ event.operator }}
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { User } from '@element-plus/icons-vue'
import type { WaPhone, PhoneLifecycleEvent, LifecycleEventType } from '@/types/wa-management'
import { WA_STATUS_MAP, PHONE_STATUS_MAP } from '@/types/wa-management'
import { getPhoneLifecycle } from '@/api/wa-management'

const props = defineProps<{
  modelValue: boolean
  phone: WaPhone | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const loading = ref(false)
const events = ref<PhoneLifecycleEvent[]>([])

/** 事件类型 → 所属分类标签文字 */
const EVENT_TYPE_CATEGORY: Record<LifecycleEventType, string> = {
  REGISTERED:        '登记',
  IP_BOUND:          'IP管理',
  IP_CHANGED:        'IP管理',
  ACTIVATED:         '激活',
  NURTURE_STARTED:   '投养',
  NURTURE_COMPLETED: '投养',
  ASSIGNED:          '分配',
  RECLAIMED:         '分配',
  OFFLINE:           '异常',
  BACK_ONLINE:       '恢复',
  BANNED:            '封号',
  APPEALED:          '申诉',
  DISABLED:          '停用',
}

/** 事件类型 → 时间线节点颜色（el-timeline-item type） */
const getEventColor = (type: LifecycleEventType): string => {
  const colorMap: Partial<Record<LifecycleEventType, string>> = {
    REGISTERED:        'primary',
    ACTIVATED:         'success',
    NURTURE_COMPLETED: 'success',
    ASSIGNED:          'primary',
    BANNED:            'danger',
    DISABLED:          'info',
    OFFLINE:           'warning',
    APPEALED:          'warning',
  }
  return colorMap[type] || 'primary'
}

/** 次要事件节点用空心样式 */
const isMinorEvent = (type: LifecycleEventType): boolean => {
  return ['IP_BOUND', 'IP_CHANGED', 'NURTURE_STARTED', 'BACK_ONLINE'].includes(type)
}

/** 事件类型 → 分类 Tag 的颜色 */
const getEventTagType = (type: LifecycleEventType): string => {
  const map: Partial<Record<LifecycleEventType, string>> = {
    REGISTERED:        'info',
    IP_BOUND:          '',
    IP_CHANGED:        '',
    ACTIVATED:         'success',
    NURTURE_STARTED:   'warning',
    NURTURE_COMPLETED: 'success',
    ASSIGNED:          'primary',
    RECLAIMED:         'info',
    OFFLINE:           'warning',
    BACK_ONLINE:       'success',
    BANNED:            'danger',
    APPEALED:          'warning',
    DISABLED:          'info',
  }
  return map[type] ?? ''
}

/** 当前号码状态标签 */
const currentStatusLabel = computed(() => {
  if (!props.phone) return ''
  if (props.phone.waStatus === 'BANNED') return WA_STATUS_MAP.BANNED.label
  if (props.phone.waStatus === 'DISABLED') return WA_STATUS_MAP.DISABLED.label
  return PHONE_STATUS_MAP[props.phone.phoneStatus].label
})

const currentStatusType = computed(() => {
  if (!props.phone) return ''
  if (props.phone.waStatus === 'BANNED') return WA_STATUS_MAP.BANNED.type
  if (props.phone.waStatus === 'DISABLED') return WA_STATUS_MAP.DISABLED.type
  return PHONE_STATUS_MAP[props.phone.phoneStatus].type
})

const handleClose = () => {
  visible.value = false
}

const loadEvents = async () => {
  if (!props.phone) return
  loading.value = true
  try {
    const list = await getPhoneLifecycle(props.phone.id)
    // 按时间升序排列，最新的在最下方
    events.value = list.sort((a, b) => a.createdAt.localeCompare(b.createdAt))
  } finally {
    loading.value = false
  }
}

watch(visible, (val) => {
  if (val) loadEvents()
  else events.value = []
})
</script>

<style scoped>
.phone-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.phone-number {
  color: #606266;
  font-size: 14px;
}

.timeline-wrap {
  min-height: 120px;
  max-height: 480px;
  overflow-y: auto;
  padding-right: 8px;
}

.event-card {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 2px;
}

.event-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.event-label {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.event-type-tag {
  margin-left: auto;
  flex-shrink: 0;
}

.event-detail {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 6px;
}

.event-operator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
