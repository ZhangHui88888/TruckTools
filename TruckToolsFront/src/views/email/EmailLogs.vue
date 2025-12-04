<template>
  <div class="email-logs-page">
    <div class="page-header">
      <h1 class="page-title">发送日志</h1>
      <p class="page-subtitle">查看所有邮件发送任务和详细日志</p>
    </div>

    <!-- 任务列表 -->
    <div class="task-list content-card">
      <a-table
        :data-source="tasks"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 任务名称 -->
          <template v-if="column.key === 'taskName'">
            <a @click="$router.push(`/email/task/${record.id}`)">
              {{ record.taskName || record.templateName || '未命名任务' }}
            </a>
          </template>
          
          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColors[record.status]">
              {{ statusLabels[record.status] }}
            </a-tag>
          </template>
          
          <!-- 进度 -->
          <template v-else-if="column.key === 'progress'">
            <div class="progress-cell">
              <a-progress
                :percent="getProgress(record)"
                :size="[100, 6]"
                :stroke-color="progressColors[record.status]"
                :show-info="false"
              />
              <span class="progress-text">
                {{ record.successCount }}/{{ record.totalCount }}
              </span>
            </div>
          </template>
          
          <!-- 统计 -->
          <template v-else-if="column.key === 'stats'">
            <div class="stats-cell">
              <span class="stat success">✓ {{ record.successCount }}</span>
              <span class="stat failed">✗ {{ record.failedCount }}</span>
            </div>
          </template>
          
          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="$router.push(`/email/task/${record.id}`)">
                查看详情
              </a-button>
              <a-button type="link" size="small" @click="handleExport(record)">
                导出日志
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { emailApi } from '@/api/email'
import type { EmailTask } from '@/api/email'

const loading = ref(false)
const tasks = ref<EmailTask[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

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

const progressColors: Record<number, string> = {
  0: '#d9d9d9',
  1: '#1677ff',
  2: '#faad14',
  3: '#52c41a',
  4: '#ff4d4f'
}

// 表格列
const columns = [
  { title: '任务名称', key: 'taskName', dataIndex: 'taskName', width: 200 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 100 },
  { title: '发送进度', key: 'progress', width: 180 },
  { title: '成功/失败', key: 'stats', width: 120 },
  { 
    title: '创建时间', 
    key: 'createdAt', 
    dataIndex: 'createdAt', 
    width: 180,
    customRender: ({ text }: { text: string }) => dayjs(text).format('YYYY-MM-DD HH:mm')
  },
  { 
    title: '完成时间', 
    key: 'completedAt', 
    dataIndex: 'completedAt', 
    width: 180,
    customRender: ({ text }: { text: string }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-'
  },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

// 计算进度
const getProgress = (task: EmailTask) => {
  if (task.totalCount === 0) return 0
  return Math.round((task.sentCount / task.totalCount) * 100)
}

// 获取任务列表
const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await emailApi.getTasks({
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    tasks.value = res.data.list
    pagination.total = res.data.pagination.total
  } catch {
    message.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 20
  fetchTasks()
}

// 导出日志
const handleExport = async (task: EmailTask) => {
  try {
    const res = await emailApi.exportLogs(task.id)
    window.open(res.data.downloadUrl, '_blank')
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  }
}

onMounted(() => {
  fetchTasks()
})
</script>

<style lang="less" scoped>
.email-logs-page {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .progress-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .progress-text {
      font-size: 12px;
      color: #6b7280;
      white-space: nowrap;
    }
  }
  
  .stats-cell {
    display: flex;
    gap: 12px;
    
    .stat {
      font-size: 13px;
      
      &.success {
        color: #52c41a;
      }
      
      &.failed {
        color: #ff4d4f;
      }
    }
  }
}
</style>

