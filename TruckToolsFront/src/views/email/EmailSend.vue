<template>
  <div class="email-send-page">
    <div class="page-header">
      <h1 class="page-title">批量发送邮件</h1>
      <p class="page-subtitle">选择模板和客户，批量发送个性化邮件</p>
    </div>

    <a-row :gutter="24">
      <!-- 左侧：配置区域 -->
      <a-col :span="16">
        <div class="config-section content-card">
          <!-- 步骤1：选择模板 -->
          <div class="config-step">
            <div class="step-header">
              <span class="step-number">1</span>
              <h3>选择邮件模板</h3>
            </div>
            <div class="step-content">
              <a-select
                v-model:value="selectedTemplateId"
                placeholder="请选择邮件模板"
                style="width: 100%"
                size="large"
                :loading="loadingTemplates"
                @change="handleTemplateChange"
              >
                <a-select-option v-for="t in templates" :key="t.id" :value="t.id">
                  <div class="template-option">
                    <span class="template-name">{{ t.name }}</span>
                    <span class="template-subject">{{ t.subject }}</span>
                  </div>
                </a-select-option>
              </a-select>
              <a-button type="link" @click="$router.push('/email/template/edit')">
                + 新建模板
              </a-button>
            </div>
          </div>

          <!-- 步骤2：选择SMTP -->
          <div class="config-step">
            <div class="step-header">
              <span class="step-number">2</span>
              <h3>选择发送邮箱</h3>
            </div>
            <div class="step-content">
              <a-select
                v-model:value="selectedSmtpId"
                placeholder="请选择发送邮箱"
                style="width: 100%"
                size="large"
                :loading="loadingSmtp"
              >
                <a-select-option v-for="s in smtpConfigs" :key="s.id" :value="s.id">
                  {{ s.configName }} ({{ s.senderEmail }})
                </a-select-option>
              </a-select>
              <a-button type="link" @click="$router.push('/settings/smtp')">
                + 配置邮箱
              </a-button>
            </div>
          </div>

          <!-- 步骤3：筛选客户 -->
          <div class="config-step">
            <div class="step-header">
              <span class="step-number">3</span>
              <h3>筛选收件客户</h3>
            </div>
            <div class="step-content">
              <a-form layout="inline" :model="filterState">
                <a-form-item label="优先级">
                  <a-select
                    v-model:value="filterState.priority"
                    placeholder="全部"
                    style="width: 120px"
                    allow-clear
                    mode="multiple"
                  >
                    <a-select-option :value="1">⭐⭐⭐⭐⭐</a-select-option>
                    <a-select-option :value="2">⭐⭐⭐⭐</a-select-option>
                    <a-select-option :value="3">⭐⭐⭐</a-select-option>
                    <a-select-option :value="4">⭐⭐</a-select-option>
                    <a-select-option :value="5">⭐</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="国家">
                  <a-select
                    v-model:value="filterState.country"
                    placeholder="全部"
                    style="width: 150px"
                    allow-clear
                    mode="multiple"
                  >
                    <a-select-option v-for="c in countries" :key="c" :value="c">{{ c }}</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="fetchCustomers">筛选</a-button>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </div>

        <!-- 客户列表 -->
        <div class="customer-section content-card">
          <div class="section-header">
            <h3>收件人列表</h3>
            <span class="customer-count">
              已选 {{ selectedCustomerIds.length }} / {{ customers.length }}
            </span>
          </div>
          
          <a-table
            :data-source="customers"
            :columns="customerColumns"
            :loading="loadingCustomers"
            :pagination="false"
            :row-selection="customerSelection"
            row-key="id"
            size="small"
            :scroll="{ y: 300 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'priority'">
                <a-rate :value="6 - record.priority" :count="5" disabled style="font-size: 10px" />
              </template>
            </template>
          </a-table>
        </div>
      </a-col>

      <!-- 右侧：预览和发送 -->
      <a-col :span="8">
        <!-- 发送预览 -->
        <div class="preview-section content-card">
          <h3>发送预览</h3>
          
          <div v-if="selectedTemplate" class="preview-info">
            <div class="info-item">
              <label>模板：</label>
              <span>{{ selectedTemplate.name }}</span>
            </div>
            <div class="info-item">
              <label>主题：</label>
              <span>{{ selectedTemplate.subject }}</span>
            </div>
            <div class="info-item">
              <label>收件人数：</label>
              <span class="highlight">{{ selectedCustomerIds.length }}</span>
            </div>
            <div v-if="templateAttachments.length" class="info-item attachments-info">
              <label>附件：</label>
              <div class="attachment-list">
                <div v-for="att in templateAttachments" :key="att.id" class="attachment-item">
                  <PaperClipOutlined />
                  <span class="attachment-name">{{ att.fileName }}</span>
                  <span class="attachment-size">({{ formatFileSize(att.fileSize) }})</span>
                </div>
              </div>
            </div>
          </div>
          
          <a-empty v-else description="请先选择模板" />
        </div>

        <!-- 发送按钮 -->
        <div class="action-section content-card">
          <a-button
            type="primary"
            size="large"
            block
            :disabled="!canSend"
            :loading="sending"
            @click="handleSend"
          >
            <SendOutlined />
            立即发送 ({{ selectedCustomerIds.length }} 封)
          </a-button>
          
          <div class="action-tips">
            <p>发送前请确认：</p>
            <ul>
              <li>已选择正确的邮件模板</li>
              <li>已选择正确的发送邮箱</li>
              <li>收件人列表无误</li>
            </ul>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 发送进度弹窗 -->
    <a-modal
      v-model:open="showProgress"
      title="发送进度"
      :closable="!isSending"
      :mask-closable="false"
      :keyboard="false"
      :footer="null"
      width="600"
    >
      <div class="progress-content">
        <a-progress
          :percent="sendProgress"
          :status="sendStatus"
          :stroke-color="{
            '0%': '#1677ff',
            '100%': '#52c41a',
          }"
        />
        
        <div class="progress-stats">
          <a-row :gutter="24">
            <a-col :span="6">
              <a-statistic title="总数" :value="taskInfo?.totalCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="已发送" :value="taskInfo?.sentCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="成功"
                :value="taskInfo?.successCount || 0"
                :value-style="{ color: '#52c41a' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="失败"
                :value="taskInfo?.failedCount || 0"
                :value-style="{ color: '#ff4d4f' }"
              />
            </a-col>
          </a-row>
        </div>
        
        <div class="progress-actions">
          <template v-if="isSending">
            <a-button @click="handlePause">
              <PauseOutlined /> 暂停
            </a-button>
          </template>
          <template v-else-if="taskInfo?.status === 2">
            <a-button type="primary" @click="handleResume">
              <PlayCircleOutlined /> 继续
            </a-button>
            <a-button @click="handleCancel">取消发送</a-button>
          </template>
          <template v-else>
            <a-button type="primary" @click="viewTaskDetail">
              查看详情
            </a-button>
            <a-button @click="showProgress = false">关闭</a-button>
          </template>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SendOutlined,
  PauseOutlined,
  PlayCircleOutlined,
  PaperClipOutlined
} from '@ant-design/icons-vue'
import { emailApi } from '@/api/email'
import { customerApi } from '@/api/customer'
import type { EmailTemplate, EmailTask, SMTPConfig, EmailAttachment } from '@/api/email'
import type { Customer } from '@/api/customer'

const route = useRoute()
const router = useRouter()

// 加载状态
const loadingTemplates = ref(false)
const loadingSmtp = ref(false)
const loadingCustomers = ref(false)
const sending = ref(false)

// 数据
const templates = ref<EmailTemplate[]>([])
const smtpConfigs = ref<SMTPConfig[]>([])
const customers = ref<Customer[]>([])

// 选中状态
const selectedTemplateId = ref<string>()
const selectedSmtpId = ref<string>()
const selectedCustomerIds = ref<string[]>([])

// 附件
const templateAttachments = ref<EmailAttachment[]>([])

// 筛选条件
const filterState = reactive({
  priority: [] as number[],
  country: [] as string[]
})

// 发送进度
const showProgress = ref(false)
const taskId = ref<string>()
const taskInfo = ref<EmailTask | null>(null)

// 全球国家列表（按外贸重要性排序）
const countries = [
  // 中国及港澳台
  '中国', '香港', '台湾', '澳门',
  // 亚洲主要国家
  '日本', '韩国', '印度', '新加坡', '马来西亚', '泰国', '越南', '印度尼西亚', '菲律宾',
  '巴基斯坦', '孟加拉国', '斯里兰卡', '缅甸', '柬埔寨', '老挝', '文莱', '蒙古',
  // 中东地区
  '阿联酋', '沙特阿拉伯', '以色列', '土耳其', '伊朗', '伊拉克', '约旦', '科威特',
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

// 客户表格列
const customerColumns = [
  { title: '优先级', key: 'priority', dataIndex: 'priority', width: 120 },
  { title: '姓名', key: 'name', dataIndex: 'name', width: 120 },
  { title: '邮箱', key: 'email', dataIndex: 'email', ellipsis: true },
  { title: '公司', key: 'company', dataIndex: 'company', ellipsis: true },
  { title: '国家', key: 'country', dataIndex: 'country', width: 80 }
]

// 客户选择配置
const customerSelection = computed(() => ({
  selectedRowKeys: selectedCustomerIds.value,
  onChange: (keys: string[]) => {
    selectedCustomerIds.value = keys
  }
}))

// 选中的模板
const selectedTemplate = computed(() => 
  templates.value.find(t => t.id === selectedTemplateId.value)
)

// 是否可以发送
const canSend = computed(() => 
  selectedTemplateId.value && 
  selectedSmtpId.value && 
  selectedCustomerIds.value.length > 0
)

// 发送状态
const isSending = computed(() => 
  taskInfo.value?.status === 1
)

// 发送进度
const sendProgress = computed(() => {
  if (!taskInfo.value || taskInfo.value.totalCount === 0) return 0
  return Math.round((taskInfo.value.sentCount / taskInfo.value.totalCount) * 100)
})

// 发送状态
const sendStatus = computed(() => {
  if (!taskInfo.value) return 'active'
  if (taskInfo.value.status === 3) return 'success'
  if (taskInfo.value.status === 4) return 'exception'
  return 'active'
})

// 获取模板列表
const fetchTemplates = async () => {
  loadingTemplates.value = true
  try {
    const res = await emailApi.getTemplates({ pageSize: 100 })
    templates.value = res.data.list
    
    // 如果URL有templateId参数，自动选中
    if (route.query.templateId) {
      selectedTemplateId.value = route.query.templateId as string
    }
  } catch {
    message.error('获取模板列表失败')
  } finally {
    loadingTemplates.value = false
  }
}

// 获取SMTP配置
const fetchSmtpConfigs = async () => {
  loadingSmtp.value = true
  try {
    const res = await emailApi.getSMTPConfigs()
    smtpConfigs.value = res.data
    
    // 自动选中默认配置
    const defaultConfig = res.data.find(s => s.isDefault)
    if (defaultConfig) {
      selectedSmtpId.value = defaultConfig.id
    }
  } catch {
    message.error('获取邮箱配置失败')
  } finally {
    loadingSmtp.value = false
  }
}

// 获取客户列表
const fetchCustomers = async () => {
  loadingCustomers.value = true
  try {
    const params: any = {
      pageSize: 1000 // 获取足够多的客户
    }
    
    // 如果URL有customerIds参数
    if (route.query.customerIds) {
      const ids = (route.query.customerIds as string).split(',')
      // 这里简化处理，实际需要根据IDs获取客户
      params.ids = ids
    }
    
    // 应用筛选条件
    if (filterState.priority && filterState.priority.length > 0) {
      params.priorities = filterState.priority.join(',')
    }
    if (filterState.country && filterState.country.length > 0) {
      params.countries = filterState.country.join(',')
    }
    
    const res = await customerApi.getList(params)
    customers.value = res.data.list
    
    // 筛选后自动全选
    if (filterState.priority.length > 0 || filterState.country.length > 0) {
      selectedCustomerIds.value = customers.value.map(c => c.id)
    } else if (route.query.customerIds) {
      // 如果有指定的客户ID，自动选中
      selectedCustomerIds.value = (route.query.customerIds as string).split(',')
    }
  } catch {
    message.error('获取客户列表失败')
  } finally {
    loadingCustomers.value = false
  }
}

// 模板变化
const handleTemplateChange = async () => {
  // 加载模板附件
  if (selectedTemplateId.value) {
    try {
      const res = await emailApi.getTemplateAttachments(selectedTemplateId.value)
      templateAttachments.value = res.data
    } catch {
      templateAttachments.value = []
    }
  } else {
    templateAttachments.value = []
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 发送邮件
const handleSend = () => {
  Modal.confirm({
    title: '确认发送',
    content: `确定向 ${selectedCustomerIds.value.length} 位客户发送邮件吗？`,
    okText: '发送',
    async onOk() {
      sending.value = true
      try {
        const res = await emailApi.createTask({
          templateId: selectedTemplateId.value!,
          smtpConfigId: selectedSmtpId.value!,
          customerIds: selectedCustomerIds.value
        })
        
        taskId.value = res.data.taskId
        taskInfo.value = {
          id: res.data.taskId,
          totalCount: res.data.totalCount,
          sentCount: 0,
          successCount: 0,
          failedCount: 0,
          status: res.data.status
        } as EmailTask
        
        showProgress.value = true
        
        // 开始发送
        await emailApi.startTask(res.data.taskId)
        
        // 开始轮询状态
        pollTaskStatus()
      } catch {
        message.error('创建任务失败')
      } finally {
        sending.value = false
      }
    }
  })
}

// 轮询任务状态
const pollTaskStatus = async () => {
  if (!taskId.value) return
  
  const poll = async () => {
    try {
      const res = await emailApi.getTaskDetail(taskId.value!)
      taskInfo.value = res.data
      
      // 如果还在发送中，继续轮询
      if (res.data.status === 1) {
        setTimeout(poll, 2000)
      }
    } catch {
      // 轮询失败，静默处理
    }
  }
  
  poll()
}

// 暂停
const handlePause = async () => {
  if (!taskId.value) return
  try {
    await emailApi.pauseTask(taskId.value)
    taskInfo.value = { ...taskInfo.value!, status: 2 }
    message.success('已暂停')
  } catch {
    message.error('暂停失败')
  }
}

// 继续
const handleResume = async () => {
  if (!taskId.value) return
  try {
    await emailApi.resumeTask(taskId.value)
    taskInfo.value = { ...taskInfo.value!, status: 1 }
    pollTaskStatus()
    message.success('已继续发送')
  } catch {
    message.error('继续发送失败')
  }
}

// 取消
const handleCancel = async () => {
  if (!taskId.value) return
  try {
    await emailApi.cancelTask(taskId.value)
    taskInfo.value = { ...taskInfo.value!, status: 4 }
    message.success('已取消')
  } catch {
    message.error('取消失败')
  }
}

// 查看任务详情
const viewTaskDetail = () => {
  showProgress.value = false
  router.push(`/email/task/${taskId.value}`)
}

onMounted(() => {
  fetchTemplates()
  fetchSmtpConfigs()
  fetchCustomers()
})
</script>

<style lang="less" scoped>
.email-send-page {
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
  
  .config-section {
    margin-bottom: 24px;
  }
  
  .config-step {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .step-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      
      .step-number {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: #1677ff;
        color: #fff;
        font-weight: 600;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #374151;
      }
    }
    
    .step-content {
      padding-left: 40px;
      
      .template-option {
        display: flex;
        flex-direction: column;
        
        .template-name {
          font-weight: 500;
        }
        
        .template-subject {
          font-size: 12px;
          color: #6b7280;
        }
      }
    }
  }
  
  .customer-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
      
      .customer-count {
        color: #1677ff;
        font-weight: 500;
      }
    }
  }
  
  .preview-section,
  .action-section {
    margin-bottom: 24px;
    
    h3 {
      margin: 0 0 16px;
      font-size: 16px;
      font-weight: 600;
      color: #374151;
    }
  }
  
  .preview-info {
    .info-item {
      display: flex;
      margin-bottom: 12px;
      
      label {
        width: 80px;
        color: #6b7280;
        flex-shrink: 0;
      }
      
      span {
        color: #374151;
        
        &.highlight {
          color: #1677ff;
          font-weight: 600;
          font-size: 18px;
        }
      }
      
      &.attachments-info {
        flex-direction: column;
        
        label {
          margin-bottom: 8px;
        }
        
        .attachment-list {
          .attachment-item {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 0;
            font-size: 13px;
            color: #374151;
            
            .attachment-name {
              max-width: 150px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .attachment-size {
              color: #9ca3af;
              font-size: 12px;
            }
          }
        }
      }
    }
  }
  
  .action-tips {
    margin-top: 16px;
    padding: 12px;
    background: #f6f8fa;
    border-radius: 8px;
    font-size: 13px;
    color: #6b7280;
    
    p {
      margin: 0 0 8px;
      font-weight: 500;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
    }
  }
  
  .progress-content {
    .progress-stats {
      margin: 24px 0;
      padding: 16px;
      background: #f9fafb;
      border-radius: 8px;
    }
    
    .progress-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
    }
  }
}
</style>

