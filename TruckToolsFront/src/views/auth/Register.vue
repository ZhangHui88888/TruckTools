<template>
  <div class="register-container">
    <div class="register-bg">
      <div class="bg-pattern"></div>
    </div>
    
    <div class="register-content">
      <div class="register-card">
        <div class="register-header">
          <img src="@/assets/images/logo.svg" alt="TruckTools" class="logo" />
          <h1 class="title">创建账号</h1>
          <p class="subtitle">加入TruckTools，开启高效外贸之旅</p>
        </div>

        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          layout="vertical"
          class="register-form"
          @finish="handleSubmit"
        >
          <a-form-item name="username" label="用户名">
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="请输入用户名"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="email" label="邮箱">
            <a-input
              v-model:value="formState.email"
              size="large"
              placeholder="请输入邮箱"
            >
              <template #prefix>
                <MailOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" label="密码">
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="请输入密码（至少8位，包含大小写字母和数字）"
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="confirmPassword" label="确认密码">
            <a-input-password
              v-model:value="formState.confirmPassword"
              size="large"
              placeholder="请再次输入密码"
            >
              <template #prefix>
                <LockOutlined />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="agreement">
            <a-checkbox v-model:checked="formState.agreement">
              我已阅读并同意 <a href="#" @click.prevent>《用户协议》</a> 和 <a href="#" @click.prevent>《隐私政策》</a>
            </a-checkbox>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              注册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { UserOutlined, MailOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validatePassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请输入密码')
  }
  if (value.length < 8) {
    return Promise.reject('密码长度至少8位')
  }
  if (!/[a-z]/.test(value) || !/[A-Z]/.test(value) || !/\d/.test(value)) {
    return Promise.reject('密码需包含大小写字母和数字')
  }
  return Promise.resolve()
}

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请确认密码')
  }
  if (value !== formState.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const validateAgreement = async (_rule: Rule, value: boolean) => {
  if (!value) {
    return Promise.reject('请阅读并同意用户协议')
  }
  return Promise.resolve()
}

const rules: Record<string, Rule[]> = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 4, max: 20, message: '用户名长度为4-20位' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_.]*$/, message: '用户名必须以字母开头，只能包含字母、数字、下划线和点号' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '请输入有效的邮箱地址' }
  ],
  password: [{ validator: validatePassword, trigger: 'change' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'change' }],
  agreement: [{ validator: validateAgreement, trigger: 'change' }]
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const success = await userStore.register({
      username: formState.username,
      email: formState.email,
      password: formState.password,
      confirmPassword: formState.confirmPassword
    })
    if (success) {
      message.success('注册成功，请查收验证邮件')
      router.push('/login')
    } else {
      message.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="less" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
}

.register-bg {
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
  }
}

.register-content {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-card {
  width: 480px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  .register-header {
    text-align: center;
    margin-bottom: 32px;
    
    .logo {
      width: 56px;
      height: 56px;
      margin-bottom: 16px;
    }
    
    .title {
      font-size: 24px;
      font-weight: 700;
      color: #1e3a5f;
      margin: 0 0 8px;
    }
    
    .subtitle {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }
  
  .register-form {
    a {
      color: #1677ff;
    }
  }
  
  .register-footer {
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

@media (max-width: 480px) {
  .register-content {
    padding: 20px;
  }
  
  .register-card {
    width: 100%;
    padding: 32px 24px;
  }
}
</style>

