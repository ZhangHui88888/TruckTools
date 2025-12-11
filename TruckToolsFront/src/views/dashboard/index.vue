<template>
  <div class="dashboard-page">
    <!-- 统计卡片区域 -->
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-pending" :loading="statsLoading">
          <a-statistic
            title="待处理"
            :value="stats.pendingCount"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <ExclamationCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-waiting" :loading="statsLoading">
          <a-statistic
            title="等待反馈"
            :value="stats.waitingCount"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-today" :loading="statsLoading">
          <a-statistic
            title="今日处理"
            :value="stats.todayProcessedCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-overdue" :loading="statsLoading">
          <a-statistic
            title="超时未跟进"
            :value="stats.overdueCount"
            :value-style="{ color: '#ff4d4f' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-weekly" :loading="statsLoading">
          <a-statistic
            title="本周新增"
            :value="stats.weeklyNewCustomerCount"
            :value-style="{ color: '#1677ff' }"
          >
            <template #prefix>
              <UserAddOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-active" :loading="statsLoading">
          <a-statistic
            title="活跃客户"
            :value="stats.activeCustomerCount"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-total" :loading="statsLoading">
          <a-statistic
            title="总客户数"
            :value="stats.totalCustomerCount"
            :value-style="{ color: '#13c2c2' }"
          >
            <template #prefix>
              <ContactsOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3">
        <a-card class="stat-card stat-stopped" :loading="statsLoading">
          <a-statistic
            title="已停止跟进"
            :value="stats.stoppedFollowUpCount"
            :value-style="{ color: '#8c8c8c' }"
          >
            <template #prefix>
              <StopOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 快捷操作区域 -->
    <a-card class="quick-actions-card" title="快捷操作">
      <a-space size="middle">
        <a-button type="primary" @click="$router.push('/customer/create')">
          <UserAddOutlined />
          新增客户
        </a-button>
        <a-button @click="$router.push('/email/send')">
          <MailOutlined />
          发送邮件
        </a-button>
        <a-button @click="$router.push('/product/quote')">
          <DollarOutlined />
          产品报价
        </a-button>
        <a-button @click="$router.push('/customer/list')">
          <UnorderedListOutlined />
          查看所有客户
        </a-button>
      </a-space>
    </a-card>

    <!-- 事件列表区域 -->
    <a-card class="events-card">
      <template #title>
        <div class="events-header">
          <span>事件跟进</span>
          <a-space>
            <a-select
              v-model:value="queryParams.eventStatus"
              style="width: 140px"
              @change="fetchEventList"
            >
              <a-select-option value="all">全部状态</a-select-option>
              <a-select-option value="pending_us">等待我方处理</a-select-option>
              <a-select-option value="pending_customer">等待客户反馈</a-select-option>
            </a-select>
            <a-input-search
              v-model:value="queryParams.customerKeyword"
              placeholder="搜索客户名称"
              style="width: 200px"
              @search="fetchEventList"
            />
            <a-checkbox v-model:checked="queryParams.overdueOnly" @change="fetchEventList">
              仅显示超时
            </a-checkbox>
            <a-button type="primary" ghost @click="exportEventList" :loading="exportLoading">
              <DownloadOutlined /> 导出
            </a-button>
          </a-space>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="eventList"
        :loading="eventsLoading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'customer'">
            <div class="customer-info">
              <a-tag :color="getPriorityColor(record.customerPriority)" class="priority-tag">
                {{ getPriorityText(record.customerPriority) }}
              </a-tag>
              <a @click="goToCustomerDetail(record.customerId)" class="customer-name">
                {{ record.customerName }}
              </a>
              <span v-if="record.customerCompany" class="customer-company">
                {{ record.customerCompany }}
              </span>
            </div>
          </template>

          <template v-else-if="column.key === 'eventContent'">
            <div class="event-content">
              <a-tag v-if="record.isSystemGenerated" color="orange" size="small">
                系统提醒
              </a-tag>
              <span class="content-text">{{ truncateText(record.eventContent, 80) }}</span>
            </div>
          </template>

          <template v-else-if="column.key === 'eventStatus'">
            <a-tag :color="record.eventStatus === 'pending_us' ? 'red' : 'green'">
              {{ record.eventStatus === 'pending_us' ? '等待我方处理' : '等待客户反馈' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'waitingDays'">
            <span :class="{ 'overdue-text': record.isOverdue }">
              {{ record.waitingDays }} 天
              <WarningOutlined v-if="record.isOverdue" class="overdue-icon" />
            </span>
          </template>

          <template v-else-if="column.key === 'eventTime'">
            {{ formatDateTime(record.eventTime) }}
          </template>

          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button
                v-if="record.eventStatus === 'pending_us'"
                type="primary"
                size="small"
                @click="showProcessModal(record)"
              >
                <CheckOutlined />
                处理
              </a-button>
              <a-button size="small" @click="goToCustomerDetail(record.customerId)">
                <EyeOutlined />
                详情
              </a-button>
              <a-tooltip v-if="record.isSystemGenerated" title="延迟提醒">
                <a-button size="small" class="snooze-btn" @click="showSnoozeModal(record)">
                  <FieldTimeOutlined />
                </a-button>
              </a-tooltip>
              <a-popconfirm
                title="确定不再跟进该客户吗？停止后将不再收到超时提醒。"
                ok-text="确定停止"
                cancel-text="取消"
                ok-type="danger"
                @confirm="handleStopFollowUp(record.customerId)"
              >
                <a-tooltip title="停止跟进后不再提醒">
                  <a-button size="small" class="stop-btn">
                    <StopOutlined />
                  </a-button>
                </a-tooltip>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 延迟提醒弹窗 -->
    <a-modal
      v-model:open="snoozeModalVisible"
      title="延迟提醒"
      :confirm-loading="snoozeLoading"
      @ok="handleSnoozeReminder"
      @cancel="closeSnoozeModal"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="客户">
          <span>{{ snoozeEvent?.customerName }}</span>
        </a-form-item>
        <a-form-item label="当前提醒">
          <a-typography-paragraph
            :ellipsis="{ rows: 2, expandable: true }"
            :content="snoozeEvent?.eventContent"
          />
        </a-form-item>
        <a-form-item label="延迟时间" required>
          <a-select v-model:value="snoozeForm.snoozeDays" style="width: 100%">
            <a-select-option :value="1">1 天</a-select-option>
            <a-select-option :value="3">3 天</a-select-option>
            <a-select-option :value="7">1 周</a-select-option>
            <a-select-option :value="14">2 周</a-select-option>
            <a-select-option :value="30">1 个月</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-typography-text type="secondary">
            延迟后，系统将在 {{ snoozeForm.snoozeDays }} 天后重新提醒您跟进此客户
          </a-typography-text>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 处理事件弹窗 -->
    <a-modal
      v-model:open="processModalVisible"
      title="处理事件"
      :confirm-loading="processLoading"
      @ok="handleProcessEvent"
      @cancel="closeProcessModal"
    >
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="客户">
          <span>{{ currentEvent?.customerName }}</span>
        </a-form-item>
        <a-form-item label="原事件">
          <a-typography-paragraph
            :ellipsis="{ rows: 3, expandable: true }"
            :content="currentEvent?.eventContent"
          />
        </a-form-item>
        <a-form-item label="处理内容" required>
          <a-textarea
            v-model:value="processForm.processContent"
            :rows="4"
            placeholder="请输入处理内容..."
          />
        </a-form-item>
        <a-form-item label="处理时间">
          <a-date-picker
            v-model:value="processForm.processTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  UserAddOutlined,
  TeamOutlined,
  ContactsOutlined,
  StopOutlined,
  MailOutlined,
  DollarOutlined,
  UnorderedListOutlined,
  CheckOutlined,
  EyeOutlined,
  DownloadOutlined,
  FieldTimeOutlined
} from '@ant-design/icons-vue'
import { workbenchApi } from '@/api/workbench'
import type { WorkbenchStats, WorkbenchEvent, WorkbenchEventQueryParams, SnoozeReminderRequest } from '@/api/workbench'

const router = useRouter()

// 统计数据
const stats = ref<WorkbenchStats>({
  pendingCount: 0,
  waitingCount: 0,
  todayProcessedCount: 0,
  overdueCount: 0,
  weeklyNewCustomerCount: 0,
  activeCustomerCount: 0,
  totalCustomerCount: 0,
  stoppedFollowUpCount: 0
})
const statsLoading = ref(false)

// 事件列表
const eventList = ref<WorkbenchEvent[]>([])
const eventsLoading = ref(false)
const queryParams = reactive<WorkbenchEventQueryParams>({
  page: 1,
  pageSize: 10,
  eventStatus: 'pending_us',
  customerKeyword: '',
  overdueOnly: false,
  excludeStoppedFollowUp: true
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 处理事件弹窗
const processModalVisible = ref(false)
const processLoading = ref(false)
const currentEvent = ref<WorkbenchEvent | null>(null)
const processForm = reactive({
  processContent: '',
  processTime: dayjs()
})

// 延迟提醒弹窗
const snoozeModalVisible = ref(false)
const snoozeLoading = ref(false)
const snoozeEvent = ref<WorkbenchEvent | null>(null)
const snoozeForm = reactive({
  snoozeDays: 7  // 默认延迟7天
})

// 表格列定义
const columns = [
  {
    title: '客户',
    key: 'customer',
    width: 200
  },
  {
    title: '事件内容',
    key: 'eventContent',
    ellipsis: true
  },
  {
    title: '状态',
    key: 'eventStatus',
    width: 120
  },
  {
    title: '等待天数',
    key: 'waitingDays',
    width: 100
  },
  {
    title: '事件时间',
    key: 'eventTime',
    width: 160
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 获取统计数据
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await workbenchApi.getStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取事件列表
const fetchEventList = async () => {
  eventsLoading.value = true
  try {
    const params = {
      ...queryParams,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await workbenchApi.getEventList(params)
    eventList.value = res.data.list
    pagination.total = res.data.pagination.total
  } catch (error) {
    console.error('获取事件列表失败:', error)
  } finally {
    eventsLoading.value = false
  }
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchEventList()
}

// 显示处理弹窗
const showProcessModal = (event: WorkbenchEvent) => {
  currentEvent.value = event
  processForm.processContent = ''
  processForm.processTime = dayjs()
  processModalVisible.value = true
}

// 关闭处理弹窗
const closeProcessModal = () => {
  processModalVisible.value = false
  currentEvent.value = null
}

// 处理事件
const handleProcessEvent = async () => {
  if (!processForm.processContent.trim()) {
    message.warning('请输入处理内容')
    return
  }
  if (!currentEvent.value) return

  processLoading.value = true
  try {
    await workbenchApi.processEvent({
      eventId: currentEvent.value.id,
      processContent: processForm.processContent,
      processTime: processForm.processTime?.format('YYYY-MM-DD HH:mm:ss')
    })
    message.success('处理成功')
    closeProcessModal()
    fetchStats()
    fetchEventList()
  } catch (error) {
    console.error('处理事件失败:', error)
    message.error('处理失败')
  } finally {
    processLoading.value = false
  }
}

// 停止跟进
const handleStopFollowUp = async (customerId: string) => {
  try {
    await workbenchApi.stopFollowUp({ customerId })
    message.success('已停止跟进')
    fetchStats()
    fetchEventList()
  } catch (error) {
    console.error('停止跟进失败:', error)
    message.error('操作失败')
  }
}

// 显示延迟提醒弹窗
const showSnoozeModal = (event: WorkbenchEvent) => {
  snoozeEvent.value = event
  snoozeForm.snoozeDays = 7  // 重置为默认7天
  snoozeModalVisible.value = true
}

// 关闭延迟提醒弹窗
const closeSnoozeModal = () => {
  snoozeModalVisible.value = false
  snoozeEvent.value = null
}

// 处理延迟提醒
const handleSnoozeReminder = async () => {
  if (!snoozeEvent.value) return

  snoozeLoading.value = true
  try {
    await workbenchApi.snoozeReminder({
      eventId: snoozeEvent.value.id,
      snoozeDays: snoozeForm.snoozeDays
    })
    message.success(`已延迟 ${snoozeForm.snoozeDays} 天提醒`)
    closeSnoozeModal()
    fetchStats()
    fetchEventList()
  } catch (error) {
    console.error('延迟提醒失败:', error)
    message.error('延迟提醒失败')
  } finally {
    snoozeLoading.value = false
  }
}

// 跳转到客户详情
const goToCustomerDetail = (customerId: string) => {
  router.push(`/customer/${customerId}`)
}

// 工具函数
const getPriorityColor = (priority: number) => {
  const colors: Record<number, string> = {
    0: 'red',
    1: 'orange',
    2: 'blue',
    3: 'default'
  }
  return colors[priority] ?? 'default'
}

const getPriorityText = (priority: number) => {
  const texts: Record<number, string> = {
    0: 'T0',
    1: 'T1',
    2: 'T2',
    3: 'T3'
  }
  return texts[priority] ?? `T${priority}`
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

// 导出相关
const exportLoading = ref(false)

// 导出事件列表
const exportEventList = async () => {
  exportLoading.value = true
  try {
    const params = {
      ...queryParams,
      page: 1,
      pageSize: 9999
    }
    const res = await workbenchApi.exportEvents(params) as any
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const statusText = queryParams.eventStatus === 'pending_us' ? '等待我方处理' : 
                       queryParams.eventStatus === 'pending_customer' ? '等待客户反馈' : '全部'
    link.download = `待处理事件_${statusText}_${dayjs().format('YYYY-MM-DD')}.xlsx`
    
    link.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchStats()
  fetchEventList()
})
</script>

<style lang="less" scoped>
.dashboard-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 8px;
  
  :deep(.ant-statistic-title) {
    font-size: 13px;
    color: #666;
  }
  
  :deep(.ant-statistic-content) {
    font-size: 24px;
  }
  
  :deep(.ant-statistic-content-prefix) {
    margin-right: 8px;
  }
}

.quick-actions-card {
  margin-bottom: 24px;
  border-radius: 8px;
}

.events-card {
  border-radius: 8px;
  
  .events-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .priority-tag {
    width: fit-content;
  }
  
  .customer-name {
    font-weight: 500;
    color: #1677ff;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
  
  .customer-company {
    font-size: 12px;
    color: #999;
  }
}

.event-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  
  .content-text {
    flex: 1;
    line-height: 1.5;
  }
}

.overdue-text {
  color: #ff4d4f;
  font-weight: 500;
  
  .overdue-icon {
    margin-left: 4px;
  }
}

.stop-btn {
  color: #8c8c8c;
  border-color: #d9d9d9;
  
  &:hover {
    color: #ff4d4f;
    border-color: #ff4d4f;
    background: #fff1f0;
  }
}

.snooze-btn {
  color: #1677ff;
  border-color: #1677ff;
  
  &:hover {
    color: #4096ff;
    border-color: #4096ff;
    background: #e6f4ff;
  }
}
</style>

