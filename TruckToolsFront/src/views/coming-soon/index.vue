<template>
  <div class="coming-soon-page">
    <div class="coming-soon-container">
      <div class="coming-soon-icon">
        <RocketOutlined />
      </div>
      
      <h1 class="coming-soon-title">{{ title }}</h1>
      
      <p class="coming-soon-desc">
        我们正在努力开发此功能，预计在 <strong>{{ plannedQuarter }}</strong> 上线，敬请期待！
      </p>
      
      <div v-if="features?.length" class="feature-list">
        <h4>即将推出的功能：</h4>
        <ul>
          <li v-for="feature in features" :key="feature">{{ feature }}</li>
        </ul>
      </div>
      
      <div class="coming-soon-actions">
        <a-button type="primary" @click="$router.push('/roadmap')">
          <FlagOutlined />
          查看功能路线图
        </a-button>
        <a-button @click="$router.back()">
          <ArrowLeftOutlined />
          返回上一页
        </a-button>
      </div>
      
      <div class="subscribe-section">
        <p>想在功能上线时收到通知？</p>
        <a-button type="link" @click="handleSubscribe">
          <BellOutlined />
          订阅上线通知
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  RocketOutlined,
  FlagOutlined,
  ArrowLeftOutlined,
  BellOutlined
} from '@ant-design/icons-vue'

const route = useRoute()

const title = computed(() => (route.meta.title as string) || '此功能即将推出')
const plannedQuarter = computed(() => (route.meta.plannedQuarter as string) || '2026年')
const features = computed(() => route.meta.features as string[] | undefined)

const handleSubscribe = () => {
  message.success('订阅成功！功能上线时将通过邮件通知您')
}
</script>

<style lang="less" scoped>
.coming-soon-page {
  min-height: calc(100vh - 64px - 48px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
}

.coming-soon-container {
  text-align: center;
  max-width: 600px;
  padding: 48px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.coming-soon-icon {
  font-size: 80px;
  color: #1677ff;
  margin-bottom: 24px;
  
  :deep(svg) {
    animation: rocket-float 3s ease-in-out infinite;
  }
}

@keyframes rocket-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.coming-soon-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 16px;
}

.coming-soon-desc {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.8;
  margin: 0 0 32px;
  
  strong {
    color: #1677ff;
    font-weight: 600;
  }
}

.feature-list {
  text-align: left;
  margin-bottom: 32px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  
  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    color: #6b7280;
    
    li {
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.coming-soon-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 32px;
}

.subscribe-section {
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
  
  p {
    margin: 0 0 8px;
    color: #9ca3af;
    font-size: 14px;
  }
}
</style>

