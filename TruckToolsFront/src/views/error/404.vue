<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-code">404</div>
      <h1 class="error-title">页面不存在</h1>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      
      <div class="error-actions">
        <a-button type="primary" @click="$router.push('/')">
          <HomeOutlined />
          返回首页
        </a-button>
        <a-button @click="$router.back()">
          <ArrowLeftOutlined />
          返回上一页
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
</script>

<style lang="less" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.error-container {
  text-align: center;
  padding: 48px;
}

.error-code {
  font-size: 160px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 24px;
}

.error-title {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px;
}

.error-desc {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>

