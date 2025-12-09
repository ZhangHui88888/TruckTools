<template>
  <div class="customer-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户列表</h1>
        <p class="page-subtitle">管理您的所有客户信息</p>
      </div>
      <div class="header-actions">
        <a-button @click="handleExport">
          <DownloadOutlined />
          导出
        </a-button>
        <a-button type="primary" @click="showAddModal = true">
          <PlusOutlined />
          添加客户
        </a-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar content-card">
      <a-form layout="inline" :model="filterState">
        <a-form-item label="关键词">
          <a-input
            v-model:value="filterState.keyword"
            placeholder="搜索姓名、公司、邮箱"
            style="width: 200px"
            allow-clear
            @pressEnter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="优先级">
          <a-select
            v-model:value="filterState.priority"
            placeholder="全部"
            style="width: 120px"
            allow-clear
          >
            <a-select-option :value="0">T0</a-select-option>
            <a-select-option :value="1">T1</a-select-option>
            <a-select-option :value="2">T2</a-select-option>
            <a-select-option :value="3">T3</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="国家">
          <a-select
            v-model:value="filterState.country"
            placeholder="全部"
            style="width: 150px"
            allow-clear
            show-search
          >
            <a-select-option v-for="c in countries" :key="c" :value="c">{{ c }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="来源">
          <a-select
            v-model:value="filterState.source"
            placeholder="全部"
            style="width: 120px"
            allow-clear
          >
            <a-select-option value="manual">手动录入</a-select-option>
            <a-select-option value="ocr">名片识别</a-select-option>
            <a-select-option value="import">Excel导入</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            <SearchOutlined />
            搜索
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格 -->
    <div class="table-container content-card">
      <a-table
        :data-source="customers"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 优先级 -->
          <template v-if="column.key === 'priority'">
            <a-tag :color="priorityColors[record.priority]">{{ priorityLabels[record.priority] }}</a-tag>
          </template>
          
          <!-- 客户姓名 -->
          <template v-else-if="column.key === 'name'">
            <a @click="handleViewDetail(record)">{{ record.name }}</a>
          </template>
          
          <!-- 公司 -->
          <template v-else-if="column.key === 'company'">
            <div class="company-cell">
              <span class="company-name text-ellipsis">{{ record.company || '-' }}</span>
              <a v-if="record.website" :href="formatWebsiteUrl(record.website)" target="_blank" class="website-link" @click.stop>
                <LinkOutlined />
              </a>
            </div>
          </template>
          
          <!-- 跟进状态 -->
          <template v-else-if="column.key === 'followUpStatus'">
            <div class="status-indicator">
              <span :class="['status-dot', followUpStatusColors[record.followUpStatus]]"></span>
            </div>
          </template>
          
          <!-- 最新事件 -->
          <template v-else-if="column.key === 'latestEvent'">
            <div v-if="record.latestEventContent" class="latest-event">
              <span class="event-content text-ellipsis">{{ record.latestEventContent }}</span>
              <span v-if="record.latestEventTime" class="event-time">{{ formatEventTime(record.latestEventTime) }}</span>
            </div>
            <span v-else class="text-muted">-</span>
          </template>
          
          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-tooltip title="发送邮件">
                <a-button type="link" size="small" @click="handleSendEmail(record)">
                  <MailOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="编辑">
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <EditOutlined />
                </a-button>
              </a-tooltip>
              <a-popconfirm
                title="确定删除此客户吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-tooltip title="删除">
                  <a-button type="link" size="small" danger>
                    <DeleteOutlined />
                  </a-button>
                </a-tooltip>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 批量操作 -->
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <span class="selected-info">已选择 {{ selectedRowKeys.length }} 项</span>
        <a-button @click="handleBatchEmail">
          <MailOutlined />
          批量发送邮件
        </a-button>
        <a-popconfirm
          title="确定删除选中的客户吗？"
          @confirm="handleBatchDelete"
        >
          <a-button danger>
            <DeleteOutlined />
            批量删除
          </a-button>
        </a-popconfirm>
      </div>
    </div>

    <!-- 添加/编辑客户弹窗 -->
    <CustomerFormModal
      v-model:open="showAddModal"
      :customer="currentCustomer"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  DownloadOutlined,
  MailOutlined,
  EditOutlined,
  DeleteOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { customerApi } from '@/api/customer'
import type { Customer, CustomerListParams } from '@/api/customer'
import CustomerFormModal from './components/CustomerFormModal.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const customers = ref<Customer[]>([])
const showAddModal = ref(false)
const currentCustomer = ref<Customer | null>(null)
const selectedRowKeys = ref<string[]>([])

// 筛选条件
const filterState = reactive<CustomerListParams>({
  page: 1,
  pageSize: 20,
  keyword: (route.query.keyword as string) || '',
  priority: undefined,
  country: undefined,
  source: undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '优先级', key: 'priority', dataIndex: 'priority', width: 80, sorter: true },
  { title: '客户姓名', key: 'name', dataIndex: 'name', width: 130 },
  { title: '公司', key: 'company', dataIndex: 'company', width: 150, ellipsis: true },
  { title: '国家', key: 'country', dataIndex: 'country', width: 80 },
  { title: '地址', key: 'address', dataIndex: 'address', width: 150, ellipsis: true },
  { title: '跟进状态', key: 'followUpStatus', dataIndex: 'followUpStatus', width: 100 },
  { title: '最新事件', key: 'latestEvent', dataIndex: 'latestEventContent', width: 250, ellipsis: true },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
]

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}))

// 来源标签
const sourceLabels: Record<string, string> = {
  manual: '手动录入',
  ocr: '名片识别',
  import: 'Excel导入'
}

const sourceColors: Record<string, string> = {
  manual: 'blue',
  ocr: 'green',
  import: 'orange'
}

// 邮件状态
const emailStatusMap: Record<number, { status: string; text: string }> = {
  0: { status: 'error', text: '无效' },
  1: { status: 'success', text: '有效' },
  2: { status: 'warning', text: '退信' }
}

// 优先级
const priorityLabels: Record<number, string> = {
  0: 'T0',
  1: 'T1',
  2: 'T2',
  3: 'T3'
}

const priorityColors: Record<number, string> = {
  0: 'red',
  1: 'orange',
  2: 'blue',
  3: 'default'
}

// 跟进状态颜色
const followUpStatusColors: Record<string, string> = {
  pending_customer: 'status-green',  // 等待客户回复 - 绿色
  completed: 'status-green',         // 已完成 - 绿色
  pending_us: 'status-yellow'        // 等待我们回复 - 黄色
}

// 全球国家列表（按外贸重要性排序）
const countries = [
  // 中国及港澳台
  '中国', '香港', '台湾', '澳门',
  // 亚洲主要国家
  '日本', '韩国', '印度', '新加坡', '马来西亚', '泰国', '越南', '印度尼西亚', '菲律宾',
  '巴基斯坦', '孟加拉国', '斯里兰卡', '缅甸', '柬埔寨', '老挝', '文莱', '蒙古',
  // 中东地区
  '阿联酋', '迪拜', '沙特阿拉伯', '以色列', '土耳其', '伊朗', '伊拉克', '约旦', '科威特',
  '卡塔尔', '阿曼', '巴林', '黎巴嫩', '叙利亚', '也门',
  // 欧洲主要国家
  '德国', '英国', '法国', '意大利', '西班牙', '荷兰', '比利时', '瑞士', '瑞典',
  '波兰', '奥地利', '丹麦', '芬兰', '挪威', '葡萄牙', '希腊', '捷克', '爱尔兰',
  '匈牙利', '罗马尼亚', '乌克兰', '俄罗斯', '白俄罗斯', '保加利亚', '塞尔维亚',
  '克罗地亚', '斯洛伐克', '斯洛文尼亚', '立陶宛', '拉脱维亚', '爱沙尼亚',
  // 北美洲
  '美国', '加拿大', '墨西哥',
  // 南美洲
  '巴西', '阿根廷', '智利', '秘鲁', '哥伦比亚', '委内瑞拉', '厄瓜多尔', '乌拉圭',
  '巴拉圭', '玻利维亚',
  // 大洋洲
  '澳大利亚', '新西兰', '巴布亚新几内亚', '斐济',
  // 非洲主要国家
  '南非', '埃及', '尼日利亚', '肯尼亚', '埃塞俄比亚', '加纳', '坦桑尼亚', '乌干达',
  '摩洛哥', '阿尔及利亚', '突尼斯', '安哥拉', '赞比亚', '津巴布韦', '博茨瓦纳',
  // 其他
  '其他'
]

// 获取客户列表
const fetchCustomers = async () => {
  loading.value = true
  try {
    const res = await customerApi.getList(filterState)
    if (res && res.data) {
      customers.value = res.data.list || []
      pagination.total = res.data.pagination?.total || 0
      pagination.current = res.data.pagination?.page || 1
    }
  } catch (error) {
    console.error('获取客户列表失败:', error)
    customers.value = []
    // 如果是网络错误，不重复显示消息（request.ts 已经处理了）
  } finally {
    loading.value = false
  }
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag, _filters, sorter: any) => {
  filterState.page = pag.current
  filterState.pageSize = pag.pageSize
  if (sorter.field) {
    filterState.sortField = sorter.field
    filterState.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }
  fetchCustomers()
}

// 搜索
const handleSearch = () => {
  filterState.page = 1
  fetchCustomers()
}

// 重置
const handleReset = () => {
  filterState.keyword = ''
  filterState.priority = undefined
  filterState.country = undefined
  filterState.source = undefined
  filterState.page = 1
  fetchCustomers()
}

// 编辑
const handleEdit = (record: Customer) => {
  currentCustomer.value = record
  showAddModal.value = true
}

// 删除
const handleDelete = async (id: string) => {
  try {
    await customerApi.delete(id)
    message.success('删除成功')
    fetchCustomers()
  } catch {
    message.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await customerApi.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    fetchCustomers()
  } catch {
    message.error('批量删除失败')
  }
}

// 发送邮件
const handleSendEmail = (record: Customer) => {
  router.push({
    path: '/email/send',
    query: { customerIds: record.id }
  })
}

// 批量发送邮件
const handleBatchEmail = () => {
  router.push({
    path: '/email/send',
    query: { customerIds: selectedRowKeys.value.join(',') }
  })
}

// 导出
const handleExport = async () => {
  try {
    const res = await customerApi.export({
      ids: selectedRowKeys.value.length > 0 ? selectedRowKeys.value : undefined,
      filter: filterState
    })
    window.open(res.data.downloadUrl, '_blank')
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  showAddModal.value = false
  currentCustomer.value = null
  fetchCustomers()
}

// 格式化事件时间
const formatEventTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 查看客户详情（带ID验证）
const handleViewDetail = (record: Customer) => {
  if (!record.id || !/^\d+$/.test(record.id)) {
    message.error('客户ID无效，无法查看详情')
    console.error('无效的客户ID:', record.id, '客户数据:', record)
    return
  }
  router.push(`/customer/${record.id}`)
}

// 格式化网站URL
const formatWebsiteUrl = (url: string) => {
  if (!url) return '#'
  // 如果URL不包含协议，自动添加https://
  if (!/^https?:\/\//i.test(url)) {
    return `https://${url}`
  }
  return url
}

onMounted(() => {
  fetchCustomers()
})
</script>

<style lang="less" scoped>
.customer-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
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
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .filter-bar {
    margin-bottom: 24px;
    
    :deep(.ant-form-item) {
      margin-bottom: 0;
    }
  }
  
  .table-container {
    position: relative;
    
    .company-cell {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .company-name {
        flex: 1;
      }
      
      .website-link {
        color: #1677ff;
        flex-shrink: 0;
      }
    }
    
    .batch-actions {
      position: sticky;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 16px 24px;
      background: #f0f5ff;
      border-top: 1px solid #d6e4ff;
      display: flex;
      align-items: center;
      gap: 16px;
      margin: -24px;
      margin-top: 16px;
      
      .selected-info {
        color: #1677ff;
        font-weight: 500;
      }
    }
  }
  
  .status-indicator {
    display: flex;
    justify-content: center;
    align-items: center;
    
    .status-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      
      &.status-green {
        background-color: #52c41a;
      }
      
      &.status-yellow {
        background-color: #faad14;
      }
    }
  }
  
  .latest-event {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .event-content {
      color: #333;
      font-size: 13px;
    }
    
    .event-time {
      color: #999;
      font-size: 11px;
    }
  }
  
  .text-muted {
    color: #999;
  }
}
</style>

