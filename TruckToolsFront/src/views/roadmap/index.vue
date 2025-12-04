<template>
  <div class="roadmap-page">
    <div class="page-header">
      <h1 class="page-title">功能路线图</h1>
      <p class="page-subtitle">了解TruckTools的产品规划和未来发展方向</p>
    </div>

    <!-- 时间轴 -->
    <div class="roadmap-timeline">
      <div v-for="(quarter, index) in groupedRoadmap" :key="index" class="timeline-group">
        <div class="timeline-header">
          <div class="timeline-dot" :class="{ active: quarter.hasReleased }"></div>
          <h3>{{ quarter.period }}</h3>
        </div>
        
        <div class="timeline-content">
          <div
            v-for="item in quarter.items"
            :key="item.id"
            class="roadmap-card content-card"
            :class="statusClass(item.status)"
          >
            <div class="card-header">
              <div class="card-icon">
                <component :is="moduleIcons[item.moduleKey]" />
              </div>
              <div class="card-info">
                <h4>{{ item.moduleName }}</h4>
                <a-tag :color="statusColors[item.status]">{{ statusLabels[item.status] }}</a-tag>
              </div>
            </div>
            
            <p class="card-desc">{{ item.description }}</p>
            
            <div v-if="item.features?.length" class="card-features">
              <a-tag v-for="f in item.features" :key="f" size="small">{{ f }}</a-tag>
            </div>
            
            <div class="card-footer">
              <div class="interest-count">
                <HeartOutlined />
                <span>{{ item.interestCount }} 人感兴趣</span>
              </div>
              
              <div class="card-actions">
                <a-button
                  v-if="item.status !== 'released'"
                  type="primary"
                  size="small"
                  :ghost="item.isSubscribed"
                  @click="toggleSubscribe(item)"
                >
                  {{ item.isSubscribed ? '已订阅' : '订阅通知' }}
                </a-button>
                <a-button
                  v-if="item.status !== 'released'"
                  size="small"
                  @click="addInterest(item)"
                >
                  <HeartOutlined />
                  +1
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 反馈入口 -->
    <div class="feedback-section content-card">
      <div class="feedback-content">
        <h3>有新功能建议？</h3>
        <p>我们非常重视您的反馈，欢迎告诉我们您期望的功能</p>
      </div>
      <a-button type="primary">
        <CommentOutlined />
        提交建议
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  TeamOutlined,
  MailOutlined,
  ShoppingOutlined,
  CarOutlined,
  ApartmentOutlined,
  FileTextOutlined,
  LineChartOutlined,
  RocketOutlined,
  MobileOutlined,
  HeartOutlined,
  CommentOutlined
} from '@ant-design/icons-vue'
import { http } from '@/utils/request'

interface RoadmapItem {
  id: number
  moduleName: string
  moduleKey: string
  description: string
  features?: string[]
  plannedQuarter: string
  status: 'released' | 'developing' | 'planned'
  interestCount: number
  isSubscribed: boolean
}

const roadmapData = ref<RoadmapItem[]>([])
const loading = ref(false)

// 模块图标
const moduleIcons: Record<string, any> = {
  'customer': () => h(TeamOutlined),
  'email': () => h(MailOutlined),
  'email-tracking': () => h(MailOutlined),
  'customer-tag': () => h(TeamOutlined),
  'mobile-h5': () => h(MobileOutlined),
  'sales': () => h(ShoppingOutlined),
  'logistics': () => h(CarOutlined),
  'supply-chain': () => h(ApartmentOutlined),
  'document': () => h(FileTextOutlined),
  'analytics': () => h(LineChartOutlined),
  'default': () => h(RocketOutlined)
}

// 状态标签
const statusLabels: Record<string, string> = {
  released: '已发布',
  developing: '开发中',
  planned: '计划中'
}

const statusColors: Record<string, string> = {
  released: 'green',
  developing: 'blue',
  planned: 'default'
}

// 状态样式
const statusClass = (status: string) => ({
  'is-released': status === 'released',
  'is-developing': status === 'developing',
  'is-planned': status === 'planned'
})

// 按时间分组
const groupedRoadmap = computed(() => {
  const groups: Record<string, { period: string; hasReleased: boolean; items: RoadmapItem[] }> = {}
  
  roadmapData.value.forEach(item => {
    const period = item.plannedQuarter
    if (!groups[period]) {
      groups[period] = {
        period,
        hasReleased: false,
        items: []
      }
    }
    groups[period].items.push(item)
    if (item.status === 'released') {
      groups[period].hasReleased = true
    }
  })
  
  return Object.values(groups).sort((a, b) => a.period.localeCompare(b.period))
})

// 获取路线图数据
const fetchRoadmap = async () => {
  loading.value = true
  try {
    const res = await http.get<RoadmapItem[]>('/roadmap')
    roadmapData.value = res.data
  } catch {
    // 使用默认数据
    roadmapData.value = [
      {
        id: 1,
        moduleName: '客户管理',
        moduleKey: 'customer',
        description: '名片识别、客户信息管理、Excel导入导出',
        features: ['名片OCR识别', '客户表格管理', 'Excel批量导入'],
        plannedQuarter: '2025 Q4',
        status: 'released',
        interestCount: 0,
        isSubscribed: false
      },
      {
        id: 2,
        moduleName: '邮件营销',
        moduleKey: 'email',
        description: '邮件模板、批量发送、发送日志追踪',
        features: ['邮件模板编辑', '批量个性化发送', '发送日志导出'],
        plannedQuarter: '2025 Q4',
        status: 'released',
        interestCount: 0,
        isSubscribed: false
      },
      {
        id: 3,
        moduleName: '邮件追踪增强',
        moduleKey: 'email-tracking',
        description: '邮件打开率、点击率追踪',
        features: ['打开率统计', '点击率追踪', '数据分析报表'],
        plannedQuarter: '2026 Q1',
        status: 'developing',
        interestCount: 45,
        isSubscribed: false
      },
      {
        id: 4,
        moduleName: '客户标签系统',
        moduleKey: 'customer-tag',
        description: '自定义标签、智能标签分类',
        features: ['自定义标签', '智能标签推荐', '标签筛选'],
        plannedQuarter: '2026 Q1',
        status: 'planned',
        interestCount: 38,
        isSubscribed: false
      },
      {
        id: 5,
        moduleName: '移动端H5适配',
        moduleKey: 'mobile-h5',
        description: '手机浏览器访问适配',
        plannedQuarter: '2026 Q1',
        status: 'planned',
        interestCount: 62,
        isSubscribed: false
      },
      {
        id: 6,
        moduleName: '销售管理',
        moduleKey: 'sales',
        description: '报价单、合同、订单管理',
        features: ['报价单管理', '合同管理', '订单管理', '销售漏斗'],
        plannedQuarter: '2026 Q2',
        status: 'planned',
        interestCount: 128,
        isSubscribed: false
      },
      {
        id: 7,
        moduleName: '物流跟踪',
        moduleKey: 'logistics',
        description: '货运追踪、清关进度',
        features: ['物流状态追踪', '清关进度', '到货提醒'],
        plannedQuarter: '2026 Q3',
        status: 'planned',
        interestCount: 89,
        isSubscribed: false
      },
      {
        id: 8,
        moduleName: '供应链管理',
        moduleKey: 'supply-chain',
        description: '供应商、采购、库存管理',
        features: ['供应商管理', '采购订单', '库存管理'],
        plannedQuarter: '2026 Q3',
        status: 'planned',
        interestCount: 76,
        isSubscribed: false
      },
      {
        id: 9,
        moduleName: '文档中心',
        moduleKey: 'document',
        description: '合同、发票、证书管理',
        features: ['文档归档', '智能分类', '在线预览'],
        plannedQuarter: '2026 Q4',
        status: 'planned',
        interestCount: 54,
        isSubscribed: false
      },
      {
        id: 10,
        moduleName: '数据分析',
        moduleKey: 'analytics',
        description: '销售报表、客户分析、业务预测',
        features: ['销售报表', '客户分析', 'AI预测'],
        plannedQuarter: '2027',
        status: 'planned',
        interestCount: 112,
        isSubscribed: false
      }
    ]
  } finally {
    loading.value = false
  }
}

// 切换订阅
const toggleSubscribe = async (item: RoadmapItem) => {
  try {
    if (item.isSubscribed) {
      await http.delete(`/roadmap/${item.id}/subscribe`)
      item.isSubscribed = false
      message.success('已取消订阅')
    } else {
      await http.post(`/roadmap/${item.id}/subscribe`)
      item.isSubscribed = true
      message.success('订阅成功，功能上线时将通过邮件通知您')
    }
  } catch {
    message.error('操作失败')
  }
}

// 表达兴趣
const addInterest = async (item: RoadmapItem) => {
  try {
    await http.post(`/roadmap/${item.id}/interest`)
    item.interestCount++
    message.success('感谢您的反馈！')
  } catch {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchRoadmap()
})
</script>

<style lang="less" scoped>
.roadmap-page {
  max-width: 900px;
  margin: 0 auto;
  
  .page-header {
    text-align: center;
    margin-bottom: 48px;
    
    .page-title {
      font-size: 32px;
      font-weight: 700;
      color: #1f2937;
      margin: 0 0 8px;
    }
    
    .page-subtitle {
      font-size: 16px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .roadmap-timeline {
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      left: 20px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #e5e7eb;
    }
  }
  
  .timeline-group {
    position: relative;
    margin-bottom: 40px;
    
    .timeline-header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;
      
      .timeline-dot {
        position: relative;
        z-index: 1;
        width: 42px;
        height: 42px;
        border-radius: 50%;
        background: #f3f4f6;
        border: 3px solid #e5e7eb;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.active {
          background: #52c41a;
          border-color: #52c41a;
          
          &::after {
            content: '✓';
            color: #fff;
            font-size: 18px;
            font-weight: 600;
          }
        }
      }
      
      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #374151;
      }
    }
    
    .timeline-content {
      margin-left: 58px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
  }
  
  .roadmap-card {
    transition: transform 0.2s, box-shadow 0.2s;
    
    &:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    &.is-released {
      border-left: 4px solid #52c41a;
    }
    
    &.is-developing {
      border-left: 4px solid #1677ff;
    }
    
    &.is-planned {
      border-left: 4px solid #d9d9d9;
    }
    
    .card-header {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;
      
      .card-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: linear-gradient(135deg, #1677ff, #4096ff);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 24px;
        flex-shrink: 0;
      }
      
      .card-info {
        flex: 1;
        
        h4 {
          margin: 0 0 4px;
          font-size: 18px;
          font-weight: 600;
          color: #1f2937;
        }
      }
    }
    
    .card-desc {
      color: #6b7280;
      margin: 0 0 12px;
      line-height: 1.6;
    }
    
    .card-features {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 16px;
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid #f3f4f6;
      
      .interest-count {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #6b7280;
        font-size: 13px;
      }
      
      .card-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .feedback-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 48px;
    padding: 32px;
    background: linear-gradient(135deg, #f0f5ff, #e6f4ff);
    
    .feedback-content {
      h3 {
        margin: 0 0 8px;
        font-size: 20px;
        font-weight: 600;
        color: #1f2937;
      }
      
      p {
        margin: 0;
        color: #6b7280;
      }
    }
  }
}
</style>

