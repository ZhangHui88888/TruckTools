<template>
  <div class="dashboard-page">
    <div class="coming-soon-container">
      <div class="coming-soon-icon">
        <DashboardOutlined />
      </div>
      <h2 class="coming-soon-title">工作台即将推出</h2>
      <p class="coming-soon-desc">
        我们正在努力开发工作台功能，包括数据概览、待办事项、快捷入口等。<br />
        预计在 <strong>2026 Q1</strong> 上线，敬请期待！
      </p>
      <div class="coming-soon-actions">
        <a-button type="primary" @click="$router.push('/customer/list')">
          <TeamOutlined />
          前往客户管理
        </a-button>
        <a-button @click="$router.push('/roadmap')">
          <FlagOutlined />
          查看功能路线图
        </a-button>
      </div>
      
      <div class="feature-preview">
        <h4>即将推出的功能</h4>
        <a-row :gutter="[24, 24]">
          <a-col :xs="24" :sm="8">
            <div class="feature-card">
              <PieChartOutlined class="feature-icon" />
              <h5>数据概览</h5>
              <p>客户数量、邮件发送统计、业务数据一目了然</p>
            </div>
          </a-col>
          <a-col :xs="24" :sm="8">
            <div class="feature-card">
              <CheckCircleOutlined class="feature-icon" />
              <h5>待办事项</h5>
              <p>客户跟进提醒、任务管理、工作日程</p>
            </div>
          </a-col>
          <a-col :xs="24" :sm="8">
            <div class="feature-card">
              <ThunderboltOutlined class="feature-icon" />
              <h5>快捷操作</h5>
              <p>一键快速访问常用功能，提升工作效率</p>
            </div>
          </a-col>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  DashboardOutlined,
  TeamOutlined,
  FlagOutlined,
  PieChartOutlined,
  CheckCircleOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue'
</script>

<style lang="less" scoped>
.dashboard-page {
  min-height: calc(100vh - 64px - 48px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.coming-soon-container {
  text-align: center;
  max-width: 800px;
  padding: 48px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.coming-soon-icon {
  font-size: 72px;
  color: #1677ff;
  margin-bottom: 24px;
  opacity: 0.8;
}

.coming-soon-title {
  font-size: 28px;
  font-weight: 600;
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
  }
}

.coming-soon-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 48px;
}

.feature-preview {
  padding-top: 32px;
  border-top: 1px solid #e5e7eb;
  
  h4 {
    font-size: 18px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 24px;
  }
}

.feature-card {
  padding: 24px;
  background: #f9fafb;
  border-radius: 12px;
  text-align: center;
  height: 100%;
  
  .feature-icon {
    font-size: 32px;
    color: #1677ff;
    margin-bottom: 16px;
  }
  
  h5 {
    font-size: 16px;
    font-weight: 600;
    color: #374151;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
    line-height: 1.6;
  }
}
</style>

