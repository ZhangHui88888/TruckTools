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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  HomeOutlined,
  GlobalOutlined,
  MailOutlined,
  EditOutlined,
  DeleteOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { customerApi } from '@/api/customer'
import type { Customer } from '@/api/customer'
import CustomerFormModal from './components/CustomerFormModal.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const customer = ref<Customer | null>(null)
const showEditModal = ref(false)

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
  1: '高优先级',
  2: '中优先级',
  3: '低优先级'
}

const priorityColors: Record<number, string> = {
  1: 'red',
  2: 'orange',
  3: 'blue'
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
  } catch {
    message.error('获取客户信息失败')
    router.push('/customer/list')
  } finally {
    loading.value = false
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
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid #e5e7eb;
      
      h4 {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        margin: 0 0 12px;
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
}
</style>

