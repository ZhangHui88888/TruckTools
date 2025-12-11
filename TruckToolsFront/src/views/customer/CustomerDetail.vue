<template>
  <div class="customer-detail-page">
    <a-spin :spinning="loading">
      <template v-if="customer">
        <!-- 头部信息 -->
        <div class="detail-header content-card">
          <div class="header-main">
            <div class="customer-avatar">
              {{ customer.name?.charAt(0) || 'C' }}
            </div>
            <div class="customer-info">
              <h1 class="customer-name">
                {{ customer.name }}
                <a-tag :color="priorityColors[customer.priority]" style="margin-left: 12px">{{ priorityLabels[customer.priority] }}</a-tag>
              </h1>
              <div class="customer-meta">
                <span v-if="customer.position">{{ customer.position }}</span>
                <span v-if="customer.company">
                  <HomeOutlined /> {{ customer.company }}
                </span>
                <span v-if="customer.country">
                  <GlobalOutlined /> {{ customer.country }}
                </span>
              </div>
            </div>
          </div>
          <div class="header-actions">
            <a-button type="primary" @click="handleSendEmail">
              <MailOutlined /> 发送邮件
            </a-button>
            <a-button @click="handleEdit">
              <EditOutlined /> 编辑
            </a-button>
            <a-popconfirm title="确定删除此客户吗？" @confirm="handleDelete">
              <a-button danger>
                <DeleteOutlined /> 删除
              </a-button>
            </a-popconfirm>
          </div>
        </div>

        <!-- 详细信息 -->
        <a-row :gutter="24" class="detail-content">
          <a-col :span="16">
            <!-- 基本信息 -->
            <div class="info-section content-card">
              <h3>基本信息</h3>
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item label="公司">
                  {{ customer.company || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="职位">
                  {{ customer.position || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="国家">
                  {{ customer.country || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="邮箱">
                  <template v-if="customer.email">
                    <a :href="`mailto:${customer.email}`">{{ customer.email }}</a>
                    <a-badge
                      :status="emailStatusMap[customer.emailStatus]?.status"
                      :text="emailStatusMap[customer.emailStatus]?.text"
                      style="margin-left: 8px"
                    />
                  </template>
                  <span v-else>-</span>
                </a-descriptions-item>
                <a-descriptions-item label="手机号">
                  {{ customer.phone || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="公司官网">
                  <a v-if="customer.website" :href="formatWebsiteUrl(customer.website)" target="_blank">
                    {{ customer.website }} <LinkOutlined />
                  </a>
                  <span v-else>-</span>
                </a-descriptions-item>
                <a-descriptions-item label="地址" :span="2">
                  {{ customer.address || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="上次会面时间">
                  {{ customer.meetingTime ? dayjs(customer.meetingTime).format('YYYY-MM-DD HH:mm') : '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="上次会面地点">
                  {{ customer.meetingLocation || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="微信名称">
                  {{ customer.wechatName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="WhatsApp">
                  {{ customer.whatsappName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="跟进状态">
                  <a-tag :color="followUpStatusColors[customer.followUpStatus]?.color">
                    {{ followUpStatusLabels[customer.followUpStatus] || '-' }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="来源">
                  <a-tag :color="sourceColors[customer.source]">{{ sourceLabels[customer.source] }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ dayjs(customer.createdAt).format('YYYY-MM-DD HH:mm') }}
                </a-descriptions-item>
                <a-descriptions-item label="更新时间">
                  {{ dayjs(customer.updatedAt).format('YYYY-MM-DD HH:mm') }}
                </a-descriptions-item>
              </a-descriptions>
              
              <!-- 微信/WhatsApp 二维码 -->
              <div v-if="customer.wechatQrcode || customer.whatsappQrcode" class="qrcode-section">
                <h4>联系二维码</h4>
                <div class="qrcode-grid">
                  <div v-if="customer.wechatQrcode" class="qrcode-item">
                    <img :src="customer.wechatQrcode" alt="微信二维码" />
                    <span>微信二维码</span>
                  </div>
                  <div v-if="customer.whatsappQrcode" class="qrcode-item">
                    <img :src="customer.whatsappQrcode" alt="WhatsApp二维码" />
                    <span>WhatsApp二维码</span>
                  </div>
                </div>
              </div>

              <!-- 名片 -->
              <div v-if="customer.businessCardFront || customer.businessCardBack" class="business-card-section">
                <h4>名片</h4>
                <div class="business-card-grid">
                  <div v-if="customer.businessCardFront" class="card-item" @click="previewImage(customer.businessCardFront)">
                    <img :src="customer.businessCardFront" alt="名片正面" />
                    <span>正面</span>
                  </div>
                  <div v-if="customer.businessCardBack" class="card-item" @click="previewImage(customer.businessCardBack)">
                    <img :src="customer.businessCardBack" alt="名片背面" />
                    <span>背面</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 备注信息 -->
            <div class="info-section content-card">
              <h3>备注信息</h3>
              <div class="remark-content">
                {{ customer.remark || '暂无备注' }}
              </div>
            </div>

            <!-- 自定义字段 -->
            <div v-if="customer.customFields && Object.keys(customer.customFields).length > 0" class="info-section content-card">
              <h3>自定义字段</h3>
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item
                  v-for="(value, key) in customer.customFields"
                  :key="key"
                  :label="key"
                >
                  {{ value || '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </div>

            <!-- 事件列表 -->
            <div class="info-section content-card">
              <div class="section-header">
                <h3>事件记录</h3>
                <a-button type="primary" @click="handleAddEvent">
                  <PlusOutlined /> 添加事件
                </a-button>
              </div>
              <a-spin :spinning="eventsLoading">
                <a-timeline v-if="events.length > 0" class="events-timeline">
                  <a-timeline-item
                    v-for="event in events"
                    :key="event.id"
                    :color="getEventTimelineColor(event)"
                  >
                    <template #dot v-if="event.eventType === 'reminder'">
                      <BellOutlined style="font-size: 16px" />
                    </template>
                    <div class="event-item" :class="{ 'reminder-event': event.eventType === 'reminder', 'system-event': event.isSystemGenerated }">
                      <div class="event-header">
                        <div class="event-time">
                          <ClockCircleOutlined />
                          {{ dayjs(event.eventTime).format('YYYY-MM-DD') }}
                          <a-tag v-if="event.eventType === 'reminder'" color="purple" size="small" style="margin-left: 8px">
                            <BellOutlined /> 提醒
                          </a-tag>
                          <a-tag v-if="event.isSystemGenerated" color="cyan" size="small" style="margin-left: 4px">
                            系统生成
                          </a-tag>
                        </div>
                        <div class="event-actions">
                          <a-tag :color="eventStatusTagColors[event.eventStatus]">
                            {{ eventStatusLabels[event.eventStatus] }}
                          </a-tag>
                          <a-button 
                            v-if="event.eventStatus !== 'completed'" 
                            type="link" 
                            size="small" 
                            @click="handleCompleteEvent(event)"
                          >
                            完结
                          </a-button>
                          <a-button 
                            v-if="event.eventStatus !== 'completed'" 
                            type="link" 
                            size="small"
                            style="color: #1677ff"
                            @click="showSnoozeModal(event)"
                          >
                            <FieldTimeOutlined /> 延迟
                          </a-button>
                          <a-button type="link" size="small" @click="handleEditEvent(event)">
                            编辑
                          </a-button>
                          <a-popconfirm title="确定删除此事件吗？" @confirm="handleDeleteEvent(event.id)">
                            <a-button type="link" size="small" danger>
                              删除
                            </a-button>
                          </a-popconfirm>
                        </div>
                      </div>
                      <div v-if="event.eventLocation" class="event-location">
                        <EnvironmentOutlined />
                        {{ event.eventLocation }}
                      </div>
                      <div class="event-content">{{ event.eventContent }}</div>
                      <!-- 提醒事件显示提醒时间 -->
                      <div v-if="event.eventType === 'reminder' && event.reminderTime" class="event-reminder-info">
                        <BellOutlined />
                        提醒时间: {{ dayjs(event.reminderTime).format('YYYY-MM-DD') }}
                        <a-tag v-if="event.reminderTriggered" color="green" size="small">已触发</a-tag>
                        <a-tag v-else color="blue" size="small">待触发</a-tag>
                      </div>
                    </div>
                  </a-timeline-item>
                </a-timeline>
                <a-empty v-else description="暂无事件记录" />
              </a-spin>
            </div>
          </a-col>

          <a-col :span="8">
            <!-- 邮件统计 -->
            <div class="info-section content-card">
              <h3>邮件统计</h3>
              <div class="stat-items">
                <div class="stat-item">
                  <div class="stat-value">{{ customer.emailCount }}</div>
                  <div class="stat-label">已发送邮件</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">
                    {{ customer.lastEmailTime ? dayjs(customer.lastEmailTime).format('MM-DD HH:mm') : '-' }}
                  </div>
                  <div class="stat-label">最后发送时间</div>
                </div>
              </div>
            </div>

            <!-- 话术营销 -->
            <div class="info-section content-card">
              <div class="section-header">
                <h3>话术营销</h3>
                <a-button type="link" size="small" @click="$router.push('/customer/script-template')">
                  管理模板
                </a-button>
              </div>
              <a-spin :spinning="scriptsLoading">
                <a-empty v-if="generatedScripts.length === 0" description="暂无话术模板">
                  <template #extra>
                    <a-button type="primary" @click="$router.push('/customer/script-template')">
                      去创建模板
                    </a-button>
                  </template>
                </a-empty>
                <div v-else class="scripts-list">
                  <div 
                    v-for="script in generatedScripts" 
                    :key="script.templateId"
                    class="script-item"
                  >
                    <div class="script-header">
                      <span class="script-name">{{ script.templateName }}</span>
                      <a-button 
                        type="primary" 
                        size="small" 
                        @click="copyScript(script.generatedContent)"
                      >
                        <CopyOutlined /> 复制
                      </a-button>
                    </div>
                    <div class="script-content">{{ script.generatedContent }}</div>
                  </div>
                </div>
              </a-spin>
            </div>

            <!-- 名片原图 -->
            <div v-if="customer.sourceFile && customer.source === 'ocr'" class="info-section content-card">
              <h3>名片原图</h3>
              <div class="card-image">
                <img :src="customer.sourceFile" alt="名片" />
                <div v-if="customer.ocrConfidence" class="confidence-badge">
                  识别置信度: {{ customer.ocrConfidence?.toFixed(1) }}%
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </template>
    </a-spin>

    <!-- 编辑弹窗 -->
    <CustomerFormModal
      v-model:open="showEditModal"
      :customer="customer"
      @success="handleEditSuccess"
    />

    <!-- 事件编辑弹窗 -->
    <a-modal
      v-model:open="showEventModal"
      :title="eventFormTitle"
      width="600px"
      @ok="handleEventSubmit"
      @cancel="handleEventCancel"
    >
      <a-form
        ref="eventFormRef"
        :model="eventForm"
        :rules="eventFormRules"
        layout="vertical"
      >
        <a-form-item label="事件时间" name="eventTime">
          <a-date-picker
            v-model:value="eventForm.eventTime"
            format="YYYY-MM-DD"
            placeholder="选择事件日期"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="事件地点" name="eventLocation">
          <a-input v-model:value="eventForm.eventLocation" placeholder="请输入事件地点（可选）" />
        </a-form-item>
        <a-form-item label="事件内容" name="eventContent">
          <a-textarea
            v-model:value="eventForm.eventContent"
            placeholder="请输入事件内容"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="事件进度状态" name="eventStatus">
          <a-radio-group v-model:value="eventForm.eventStatus">
            <a-radio value="pending_customer">等待客户回复</a-radio>
            <a-radio value="pending_us">等待我们回复</a-radio>
            <a-radio value="completed">已完结</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="事件类型" name="eventType">
          <a-radio-group v-model:value="eventForm.eventType">
            <a-radio value="normal">普通事件</a-radio>
            <a-radio value="reminder">提醒事件</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item 
          v-if="eventForm.eventType === 'reminder'" 
          label="提醒时间" 
          name="reminderTime"
          :rules="[{ required: eventForm.eventType === 'reminder', message: '请选择提醒时间' }]"
        >
          <a-date-picker
            v-model:value="eventForm.reminderTime"
            format="YYYY-MM-DD"
            placeholder="选择提醒日期（到期后会生成新事件提醒您）"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 图片预览弹窗 -->
    <a-modal
      v-model:open="imagePreviewVisible"
      :footer="null"
      width="auto"
      title="名片预览"
      centered
    >
      <div class="image-preview-container">
        <img :src="previewImageUrl" alt="名片预览" />
      </div>
    </a-modal>

    <!-- 延迟提醒弹窗 -->
    <a-modal
      v-model:open="snoozeModalVisible"
      title="延迟提醒"
      :confirm-loading="snoozeLoading"
      @ok="handleSnoozeReminder"
      @cancel="closeSnoozeModal"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  HomeOutlined,
  GlobalOutlined,
  MailOutlined,
  EditOutlined,
  DeleteOutlined,
  LinkOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  BellOutlined,
  CopyOutlined,
  FieldTimeOutlined
} from '@ant-design/icons-vue'
import { customerApi } from '@/api/customer'
import type { Customer, CustomerEvent, CustomerEventRequest } from '@/api/customer'
import { workbenchApi } from '@/api/workbench'
import { scriptTemplateApi } from '@/api/scriptTemplate'
import type { GeneratedScript } from '@/api/scriptTemplate'
import CustomerFormModal from './components/CustomerFormModal.vue'
import type { Dayjs } from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const customer = ref<Customer | null>(null)
const showEditModal = ref(false)

// 图片预览
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const previewImage = (url: string) => {
  previewImageUrl.value = url
  imagePreviewVisible.value = true
}

// 话术相关
const generatedScripts = ref<GeneratedScript[]>([])
const scriptsLoading = ref(false)

// 事件相关
const events = ref<CustomerEvent[]>([])
const eventsLoading = ref(false)
const showEventModal = ref(false)
const eventFormRef = ref()
const currentEventId = ref<string | null>(null)
const eventForm = ref<{
  eventTime: Dayjs | null
  eventLocation: string
  eventContent: string
  eventStatus: 'pending_customer' | 'pending_us' | 'completed'
  eventType: 'normal' | 'reminder'
  reminderTime: Dayjs | null
}>({
  eventTime: null,
  eventLocation: '',
  eventContent: '',
  eventStatus: 'pending_us',
  eventType: 'normal',
  reminderTime: null
})

const eventFormRules = {
  eventTime: [{ required: true, message: '请选择事件时间' }],
  eventContent: [{ required: true, message: '请输入事件内容' }],
  eventStatus: [{ required: true, message: '请选择事件状态' }]
}

// 延迟提醒弹窗
const snoozeModalVisible = ref(false)
const snoozeLoading = ref(false)
const snoozeEvent = ref<CustomerEvent | null>(null)
const snoozeForm = ref({
  snoozeDays: 7  // 默认延迟7天
})

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

const emailStatusMap: Record<number, { status: string; text: string }> = {
  0: { status: 'error', text: '无效' },
  1: { status: 'success', text: '有效' },
  2: { status: 'warning', text: '退信' }
}

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

// 跟进状态
const followUpStatusLabels: Record<string, string> = {
  pending_customer: '等待客户回复',
  pending_us: '等待我们回复',
  completed: '已完成'
}

const followUpStatusColors: Record<string, { color: string }> = {
  pending_customer: { color: 'green' },
  pending_us: { color: 'orange' },
  completed: { color: 'blue' }
}

// 事件状态
const eventStatusLabels: Record<string, string> = {
  pending_customer: '等待客户回复',
  pending_us: '等待我们回复',
  completed: '已完结'
}

const eventStatusColors: Record<string, string> = {
  pending_customer: 'green',
  pending_us: 'orange',
  completed: 'gray'
}

const eventStatusTagColors: Record<string, string> = {
  pending_customer: 'success',
  pending_us: 'warning',
  completed: 'default'
}

const eventFormTitle = computed(() => currentEventId.value ? '编辑事件' : '添加事件')

// 获取事件时间线颜色
const getEventTimelineColor = (event: CustomerEvent) => {
  if (event.eventType === 'reminder') {
    return event.reminderTriggered ? 'gray' : 'purple'
  }
  if (event.isSystemGenerated) {
    return 'cyan'
  }
  return eventStatusColors[event.eventStatus] || 'blue'
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

// 获取客户详情
const fetchCustomer = async () => {
  loading.value = true
  try {
    const id = route.params.id as string
    
    // 验证ID格式
    if (!id || !/^\d+$/.test(id)) {
      message.error('客户ID格式无效')
      router.push('/customer/list')
      return
    }
    
    const res = await customerApi.getDetail(id)
    customer.value = res.data
    
    // 获取事件列表和话术
    fetchEvents()
    fetchScripts()
  } catch {
    message.error('获取客户信息失败')
    router.push('/customer/list')
  } finally {
    loading.value = false
  }
}

// 获取事件列表
const fetchEvents = async () => {
  if (!customer.value) return
  eventsLoading.value = true
  try {
    const res = await customerApi.getEventsByCustomerId(customer.value.id)
    events.value = res.data
  } catch {
    message.error('获取事件列表失败')
  } finally {
    eventsLoading.value = false
  }
}

// 获取话术列表
const fetchScripts = async () => {
  if (!customer.value) return
  scriptsLoading.value = true
  try {
    const res = await scriptTemplateApi.generateForCustomer(customer.value.id)
    generatedScripts.value = res.data
  } catch {
    console.error('获取话术失败')
  } finally {
    scriptsLoading.value = false
  }
}

// 复制话术
const copyScript = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    message.success('已复制到剪贴板')
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = content
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    message.success('已复制到剪贴板')
  }
}

// 发送邮件
const handleSendEmail = () => {
  if (customer.value) {
    router.push({
      path: '/email/send',
      query: { customerIds: customer.value.id }
    })
  }
}

// 编辑
const handleEdit = () => {
  showEditModal.value = true
}

// 编辑成功
const handleEditSuccess = () => {
  showEditModal.value = false
  fetchCustomer()
}

// 删除
const handleDelete = async () => {
  if (!customer.value) return
  try {
    await customerApi.delete(customer.value.id)
    message.success('删除成功')
    router.push('/customer/list')
  } catch {
    message.error('删除失败')
  }
}

// 添加事件
const handleAddEvent = () => {
  currentEventId.value = null
  eventForm.value = {
    eventTime: null,
    eventLocation: '',
    eventContent: '',
    eventStatus: 'pending_us',
    eventType: 'normal',
    reminderTime: null
  }
  showEventModal.value = true
}

// 编辑事件
const handleEditEvent = (event: CustomerEvent) => {
  currentEventId.value = event.id
  eventForm.value = {
    eventTime: dayjs(event.eventTime),
    eventLocation: event.eventLocation || '',
    eventContent: event.eventContent,
    eventStatus: event.eventStatus,
    eventType: event.eventType || 'normal',
    reminderTime: event.reminderTime ? dayjs(event.reminderTime) : null
  }
  showEventModal.value = true
}

// 提交事件
const handleEventSubmit = async () => {
  if (!customer.value) return
  
  try {
    await eventFormRef.value?.validate()
    
    const data: CustomerEventRequest = {
      customerId: customer.value.id,
      eventTime: eventForm.value.eventTime!.format('YYYY-MM-DD'),
      eventLocation: eventForm.value.eventLocation,
      eventContent: eventForm.value.eventContent,
      eventStatus: eventForm.value.eventStatus,
      eventType: eventForm.value.eventType,
      reminderTime: eventForm.value.eventType === 'reminder' && eventForm.value.reminderTime 
        ? eventForm.value.reminderTime.format('YYYY-MM-DD') 
        : undefined
    }
    
    if (currentEventId.value) {
      await customerApi.updateEvent(currentEventId.value, data)
      message.success('事件更新成功')
    } else {
      await customerApi.createEvent(data)
      message.success('事件添加成功')
    }
    
    showEventModal.value = false
    fetchEvents()
    fetchCustomer() // 刷新客户信息（跟进状态会更新）
  } catch (error) {
    console.error('提交事件失败', error)
  }
}

// 取消事件编辑
const handleEventCancel = () => {
  showEventModal.value = false
  eventFormRef.value?.resetFields()
}

// 删除事件
const handleDeleteEvent = async (eventId: string) => {
  try {
    await customerApi.deleteEvent(eventId)
    message.success('事件删除成功')
    fetchEvents()
    fetchCustomer() // 刷新客户信息（跟进状态会更新）
  } catch {
    message.error('删除事件失败')
  }
}

// 完结事件
const handleCompleteEvent = async (event: CustomerEvent) => {
  if (!customer.value) return
  
  try {
    const data: CustomerEventRequest = {
      customerId: customer.value.id,
      eventTime: dayjs(event.eventTime).format('YYYY-MM-DD'),
      eventLocation: event.eventLocation || '',
      eventContent: event.eventContent,
      eventStatus: 'completed',
      eventType: event.eventType || 'normal',
      reminderTime: event.reminderTime ? dayjs(event.reminderTime).format('YYYY-MM-DD') : undefined
    }
    
    await customerApi.updateEvent(event.id, data)
    message.success('事件已完结')
    fetchEvents()
    fetchCustomer()
  } catch {
    message.error('完结事件失败')
  }
}

// 显示延迟提醒弹窗
const showSnoozeModal = (event: CustomerEvent) => {
  snoozeEvent.value = event
  snoozeForm.value.snoozeDays = 7  // 重置为默认7天
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
      snoozeDays: snoozeForm.value.snoozeDays
    })
    message.success(`已延迟 ${snoozeForm.value.snoozeDays} 天提醒`)
    closeSnoozeModal()
    fetchEvents()
    fetchCustomer()
  } catch (error) {
    console.error('延迟提醒失败:', error)
    message.error('延迟提醒失败')
  } finally {
    snoozeLoading.value = false
  }
}

onMounted(() => {
  fetchCustomer()
})
</script>

<style lang="less" scoped>
.customer-detail-page {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-main {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    
    .customer-avatar {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, #1677ff, #4096ff);
      color: #fff;
      font-size: 24px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .customer-name {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 8px;
      display: flex;
      align-items: center;
    }
    
    .customer-meta {
      display: flex;
      gap: 16px;
      color: #6b7280;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .detail-content {
    .info-section {
      margin-bottom: 24px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #374151;
        margin: 0 0 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #e5e7eb;
      }
    }
    
    .remark-content {
      color: #4b5563;
      line-height: 1.8;
      white-space: pre-wrap;
    }
    
    .stat-items {
      display: flex;
      gap: 32px;
      
      .stat-item {
        text-align: center;
        
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #1677ff;
        }
        
        .stat-label {
          font-size: 13px;
          color: #6b7280;
          margin-top: 4px;
        }
      }
    }
    
    .qrcode-section {
      margin-top: 24px;
      
      h4 {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        margin-bottom: 12px;
      }
      
      .qrcode-grid {
        display: flex;
        gap: 24px;
        
        .qrcode-item {
          text-align: center;
          
          img {
            width: 100px;
            height: 100px;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
          }
          
          span {
            display: block;
            margin-top: 8px;
            font-size: 13px;
            color: #6b7280;
          }
        }
      }
    }
    
    .business-card-section {
      margin-top: 24px;
      
      h4 {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        margin-bottom: 12px;
      }
      
      .business-card-grid {
        display: flex;
        gap: 24px;
        
        .card-item {
          text-align: center;
          cursor: pointer;
          transition: transform 0.2s;
          
          &:hover {
            transform: scale(1.02);
          }
          
          img {
            width: 200px;
            height: 120px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
          }
          
          span {
            display: block;
            margin-top: 8px;
            font-size: 13px;
            color: #6b7280;
          }
        }
      }
    }
    
    .card-image {
      position: relative;
      
      img {
        width: 100%;
        border-radius: 8px;
        border: 1px solid #e5e7eb;
      }
      
      .confidence-badge {
        position: absolute;
        bottom: 8px;
        right: 8px;
        padding: 4px 8px;
        background: rgba(0, 0, 0, 0.6);
        color: #fff;
        font-size: 12px;
        border-radius: 4px;
      }
    }
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      margin: 0;
    }
  }
  
  .events-timeline {
    margin-top: 16px;
    
    .event-item {
      .event-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .event-time {
          font-size: 14px;
          font-weight: 500;
          color: #374151;
          display: flex;
          align-items: center;
          gap: 6px;
        }
        
        .event-actions {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
      
      .event-location {
        font-size: 13px;
        color: #6b7280;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        gap: 6px;
      }
      
      .event-content {
        font-size: 14px;
        color: #1f2937;
        line-height: 1.6;
        white-space: pre-wrap;
      }
      
      .event-reminder-info {
        margin-top: 8px;
        padding: 8px 12px;
        background: #f5f3ff;
        border-radius: 6px;
        font-size: 13px;
        color: #7c3aed;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      &.reminder-event {
        border-left: 3px solid #7c3aed;
        padding-left: 12px;
        margin-left: -15px;
      }
      
      &.system-event {
        background: #f0fdfa;
        padding: 8px 12px;
        border-radius: 6px;
        margin-left: -12px;
      }
    }
  }
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 90vw;
  max-height: 80vh;
  
  img {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
  }
}

.scripts-list {
  .script-item {
    background: #f9fafb;
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .script-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .script-name {
        font-weight: 500;
        color: #374151;
      }
    }
    
    .script-content {
      font-size: 14px;
      color: #4b5563;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}
</style>

