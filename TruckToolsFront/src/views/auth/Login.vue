<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-pattern"></div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <img src="@/assets/images/logo.svg" alt="TruckTools" class="logo" />
          <h1 class="title">TruckTools</h1>
          <p class="subtitle">卡车外贸综合工具平台</p>
        </div>

        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          layout="vertical"
          class="login-form"
          @finish="handleSubmit"
        >
          <a-form-item name="username">
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="用户名或邮箱"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="密码"
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <div class="form-actions">
              <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
              <a class="forgot-link" @click="$router.push('/forgot-password')">忘记密码?</a>
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>

      <div class="login-features">
        <div class="feature-item">
          <div class="feature-icon">
            <CameraOutlined />
          </div>
          <div class="feature-text">
            <h4>智能名片识别</h4>
            <p>AI自动识别名片信息，快速建立客户档案</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">
            <MailOutlined />
          </div>
          <div class="feature-text">
            <h4>批量邮件营销</h4>
            <p>个性化邮件模板，一键群发营销邮件</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">
            <TeamOutlined />
          </div>
          <div class="feature-text">
            <h4>客户管理</h4>
            <p>完整的客户信息管理，高效跟进客户</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  CameraOutlined,
  MailOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  remember: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名或邮箱' }],
  password: [{ required: true, message: '请输入密码' }]
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const success = await userStore.login(formState.username, formState.password)
    if (success) {
      message.success('登录成功')
      const redirect = route.query.redirect as string || '/dashboard'
      router.push(redirect)
    } else {
      message.error('用户名或密码错误')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="less" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1e3a5f 0%, #0d1b2a 50%, #1b263b 100%);
  
  .bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.02) 0%, transparent 50%),
      radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.02) 0%, transparent 50%);
    
    &::before {
      content: '';
      position: absolute;
      top: 10%;
      right: 10%;
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, rgba(22, 119, 255, 0.15) 0%, transparent 70%);
      border-radius: 50%;
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: 10%;
      left: 5%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(82, 196, 26, 0.1) 0%, transparent 70%);
      border-radius: 50%;
    }
  }
}

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px;
  align-items: center;
  justify-content: space-between;
  gap: 80px;
}

.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  .login-header {
    text-align: center;
    margin-bottom: 40px;
    
    .logo {
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }
    
    .title {
      font-size: 28px;
      font-weight: 700;
      color: #1e3a5f;
      margin: 0 0 8px;
      letter-spacing: -0.5px;
    }
    
    .subtitle {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .login-form {
    .form-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .forgot-link {
        color: #1677ff;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
  
  .login-footer {
    text-align: center;
    margin-top: 24px;
    color: #6b7280;
    
    a {
      color: #1677ff;
      margin-left: 4px;
      font-weight: 500;
    }
  }
}

.login-features {
  flex: 1;
  max-width: 500px;
  
  .feature-item {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    padding: 24px;
    margin-bottom: 20px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: transform 0.3s, background 0.3s;
    
    &:hover {
      transform: translateX(8px);
      background: rgba(255, 255, 255, 0.08);
    }
    
    .feature-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1677ff, #4096ff);
      border-radius: 12px;
      font-size: 24px;
      color: #fff;
      flex-shrink: 0;
    }
    
    .feature-text {
      h4 {
        color: #fff;
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 8px;
      }
      
      p {
        color: rgba(255, 255, 255, 0.7);
        font-size: 14px;
        margin: 0;
        line-height: 1.6;
      }
    }
  }
}

@media (max-width: 1024px) {
  .login-content {
    flex-direction: column;
    justify-content: center;
    gap: 40px;
  }
  
  .login-features {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-content {
    padding: 20px;
  }
  
  .login-card {
    width: 100%;
    padding: 32px 24px;
  }
}
</style>

