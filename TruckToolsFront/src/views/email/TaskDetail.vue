<template>
  <div class="task-detail-page">
    <a-spin :spinning="loading">
      <template v-if="task">
        <!-- 头部 -->
        <div class="task-header content-card">
          <div class="header-left">
            <a-button type="text" @click="$router.back()">
              <ArrowLeftOutlined />
            </a-button>
            <div class="task-info">
              <h1>{{ task.taskName || '发送任务' }}</h1>
              <div class="task-meta">
                <a-tag :color="statusColors[task.status]">{{ statusLabels[task.status] }}</a-tag>
                <span>创建于 {{ dayjs(task.createdAt).format('YYYY-MM-DD HH:mm') }}</span>
              </div>
            </div>
          </div>
          <div class="header-actions">
            <template v-if="task.status === 1">
              <a-button @click="handlePause">
                <PauseOutlined /> 暂停
              </a-button>
            </template>
            <template v-else-if="task.status === 2">
              <a-button type="primary" @click="handleResume">
                <PlayCircleOutlined /> 继续
              </a-button>
              <a-button @click="handleCancel">取消</a-button>
            </template>
            <a-button @click="handleExport">
              <DownloadOutlined /> 导出日志
            </a-button>
          </div>
        </div>

        <!-- 统计 -->
        <div class="task-stats content-card">
          <a-row :gutter="24">
            <a-col :span="4">
              <a-statistic title="总发送" :value="task.totalCount" />
            </a-col>
            <a-col :span="4">
              <a-statistic title="已发送" :value="task.sentCount" />
            </a-col>
            <a-col :span="4">
              <a-statistic
                title="发送成功"
                :value="task.successCount"
                :value-style="{ color: '#52c41a' }"
              />
            </a-col>
            <a-col :span="4">
              <a-statistic
                title="发送失败"
                :value="task.failedCount"
                :value-style="{ color: '#ff4d4f' }"
              />
            </a-col>
            <a-col :span="4">
              <a-statistic
                title="成功率"
                :value="successRate"
                suffix="%"
                :value-style="{ color: successRate >= 90 ? '#52c41a' : '#faad14' }"
              />
            </a-col>
            <a-col :span="4">
              <a-statistic
                title="完成时间"
                :value="task.completedAt ? dayjs(task.completedAt).format('HH:mm:ss') : '-'"
              />
            </a-col>
          </a-row>
          
          <div class="progress-bar">
            <a-progress
              :percent="progress"
              :status="task.status === 3 ? 'success' : 'active'"
            />
          </div>
        </div>

        <!-- 发送日志 -->
        <div class="task-logs content-card">
          <div class="logs-header">
            <h3>发送详情</h3>
            <div class="logs-filter">
              <a-radio-group v-model:value="logFilter" button-style="solid" size="small">
                <a-radio-button value="all">全部</a-radio-button>
                <a-radio-button value="success">成功</a-radio-button>
                <a-radio-button value="failed">失败</a-radio-button>
              </a-radio-group>
              
              <a-button
                v-if="hasFailedLogs"
                type="primary"
                size="small"
                @click="handleRetryAll"
              >
                重试失败
              </a-button>
            </div>
          </div>

          <a-table
            :data-source="filteredLogs"
            :columns="logColumns"
            :loading="loadingLogs"
            :pagination="logPagination"
            row-key="id"
            size="small"
            @change="handleLogTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- 优先级 -->
              <template v-if="column.key === 'priority'">
                <a-rate :value="6 - (record.customerPriority || 3)" :count="5" disabled style="font-size: 10px" />
              </template>
              
              <!-- 状态 -->
              <template v-else-if="column.key === 'status'">
                <a-tag :color="logStatusColors[record.status]">
                  {{ logStatusLabels[record.status] }}
                </a-tag>
              </template>
              
              <!-- 错误信息 -->
              <template v-else-if="column.key === 'error'">
                <template v-if="record.status === 3">
                  <a-tooltip :title="record.errorMessage">
                    <span class="error-text">{{ record.errorCode || record.errorMessage }}</span>
                  </a-tooltip>
                </template>
                <span v-else>-</span>
              </template>
              
              <!-- 操作 -->
              <template v-else-if="column.key === 'action'">
                <a-button
                  v-if="record.status === 3"
                  type="link"
                  size="small"
                  @click="handleRetry(record)"
                >
                  重试
                </a-button>
              </template>
            </template>
          </a-table>
        </div>
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  ArrowLeftOutlined,
  PauseOutlined,
  PlayCircleOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { emailApi } from '@/api/email'
import type { EmailTask, EmailLog } from '@/api/email'

const route = useRoute()
const taskId = computed(() => route.params.id as string)

const loading = ref(false)
const loadingLogs = ref(false)
const task = ref<EmailTask | null>(null)
const logs = ref<EmailLog[]>([])
const logFilter = ref('all')

let pollTimer: ReturnType<typeof setInterval> | null = null

// 状态标签
const statusLabels: Record<number, string> = {
  0: '待发送',
  1: '发送中',
  2: '已暂停',
  3: '已完成',
  4: '已取消'
}

const statusColors: Record<number, string> = {
  0: 'default',
  1: 'processing',
  2: 'warning',
  3: 'success',
  4: 'error'
}

const logStatusLabels: Record<number, string> = {
  0: '待发送',
  1: '发送中',
  2: '成功',
  3: '失败'
}

const logStatusColors: Record<number, string> = {
  0: 'default',
  1: 'processing',
  2: 'success',
  3: 'error'
}

// 日志分页
const logPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 日志表格列
const logColumns = [
  { title: '优先级', key: 'priority', width: 100 },
  { title: '客户姓名', key: 'customerName', dataIndex: 'customerName', width: 120 },
  { title: '邮箱', key: 'customerEmail', dataIndex: 'customerEmail', ellipsis: true },
  { title: '公司', key: 'customerCompany', dataIndex: 'customerCompany', ellipsis: true, width: 150 },
  { title: '状态', key: 'status', width: 100 },
  { title: '错误信息', key: 'error', ellipsis: true, width: 150 },
  { 
    title: '发送时间', 
    key: 'sentAt', 
    dataIndex: 'sentAt', 
    width: 160,
    customRender: ({ text }: { text: string }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '-'
  },
  { title: '操作', key: 'action', width: 80 }
]

// 计算属性
const progress = computed(() => {
  if (!task.value || task.value.totalCount === 0) return 0
  return Math.round((task.value.sentCount / task.value.totalCount) * 100)
})

const successRate = computed(() => {
  if (!task.value || task.value.sentCount === 0) return 0
  return Math.round((task.value.successCount / task.value.sentCount) * 100)
})

const filteredLogs = computed(() => {
  if (logFilter.value === 'all') return logs.value
  if (logFilter.value === 'success') return logs.value.filter(l => l.status === 2)
  if (logFilter.value === 'failed') return logs.value.filter(l => l.status === 3)
  return logs.value
})

const hasFailedLogs = computed(() => logs.value.some(l => l.status === 3))

// 获取任务详情
const fetchTask = async () => {
  loading.value = true
  try {
    const res = await emailApi.getTaskDetail(taskId.value)
    task.value = res.data
  } catch {
    message.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 获取日志
const fetchLogs = async () => {
  loadingLogs.value = true
  try {
    const statusFilter = logFilter.value === 'success' ? 2 : logFilter.value === 'failed' ? 3 : undefined
    const res = await emailApi.getTaskLogs(taskId.value, {
      page: logPagination.current,
      pageSize: logPagination.pageSize,
      status: statusFilter
    })
    logs.value = res.data.list
    logPagination.total = res.data.pagination.total
  } catch {
    message.error('获取日志失败')
  } finally {
    loadingLogs.value = false
  }
}

// 日志表格变化
const handleLogTableChange: TableProps['onChange'] = (pag) => {
  logPagination.current = pag.current || 1
  logPagination.pageSize = pag.pageSize || 20
  fetchLogs()
}

// 暂停
const handlePause = async () => {
  try {
    await emailApi.pauseTask(taskId.value)
    task.value = { ...task.value!, status: 2 }
    message.success('已暂停')
  } catch {
    message.error('暂停失败')
  }
}

// 继续
const handleResume = async () => {
  try {
    await emailApi.resumeTask(taskId.value)
    task.value = { ...task.value!, status: 1 }
    startPolling()
    message.success('已继续发送')
  } catch {
    message.error('继续发送失败')
  }
}

// 取消
const handleCancel = async () => {
  try {
    await emailApi.cancelTask(taskId.value)
    task.value = { ...task.value!, status: 4 }
    message.success('已取消')
  } catch {
    message.error('取消失败')
  }
}

// 导出
const handleExport = async () => {
  try {
    const res = await emailApi.exportLogs(taskId.value)
    window.open(res.data.downloadUrl, '_blank')
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  }
}

// 重试单个
const handleRetry = async (log: EmailLog) => {
  try {
    await emailApi.retryFailed(taskId.value, [log.id])
    message.success('已重新发送')
    fetchLogs()
    fetchTask()
  } catch {
    message.error('重试失败')
  }
}

// 重试全部失败
const handleRetryAll = async () => {
  try {
    await emailApi.retryFailed(taskId.value)
    message.success('已重新发送所有失败邮件')
    fetchLogs()
    fetchTask()
  } catch {
    message.error('重试失败')
  }
}

// 开始轮询
const startPolling = () => {
  if (pollTimer) return
  pollTimer = setInterval(() => {
    if (task.value?.status === 1) {
      fetchTask()
      fetchLogs()
    } else {
      stopPolling()
    }
  }, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  fetchTask()
  fetchLogs()
  // 如果正在发送中，开始轮询
  if (task.value?.status === 1) {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style lang="less" scoped>
.task-detail-page {
  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h1 {
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 4px;
      }
      
      .task-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 13px;
        color: #6b7280;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .task-stats {
    margin-bottom: 24px;
    
    .progress-bar {
      margin-top: 24px;
    }
  }
  
  .task-logs {
    .logs-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
      
      .logs-filter {
        display: flex;
        gap: 12px;
        align-items: center;
      }
    }
    
    .error-text {
      color: #ff4d4f;
      cursor: help;
    }
  }
}
</style>

