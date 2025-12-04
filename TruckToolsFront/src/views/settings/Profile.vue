<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-subtitle">管理您的账号信息</p>
    </div>

    <a-row :gutter="24">
      <!-- 基本信息 -->
      <a-col :span="16">
        <div class="profile-section content-card">
          <h3>基本信息</h3>
          <a-form
            :model="formState"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
          >
            <a-form-item label="用户名">
              <a-input :value="userStore.userInfo?.username" disabled />
            </a-form-item>
            <a-form-item label="邮箱">
              <a-input :value="userStore.userInfo?.email" disabled />
              <template #extra>
                <span v-if="userStore.userInfo?.email">
                  邮箱已验证 ✓
                </span>
              </template>
            </a-form-item>
            <a-form-item label="昵称">
              <a-input v-model:value="formState.nickname" placeholder="请输入昵称" />
            </a-form-item>
            <a-form-item label="手机号">
              <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
            </a-form-item>
            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" :loading="saving" @click="handleSave">
                保存修改
              </a-button>
            </a-form-item>
          </a-form>
        </div>

        <!-- 修改密码 -->
        <div class="profile-section content-card">
          <h3>修改密码</h3>
          <a-form
            ref="passwordFormRef"
            :model="passwordState"
            :rules="passwordRules"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
          >
            <a-form-item label="当前密码" name="oldPassword">
              <a-input-password v-model:value="passwordState.oldPassword" placeholder="请输入当前密码" />
            </a-form-item>
            <a-form-item label="新密码" name="newPassword">
              <a-input-password v-model:value="passwordState.newPassword" placeholder="请输入新密码" />
            </a-form-item>
            <a-form-item label="确认密码" name="confirmPassword">
              <a-input-password v-model:value="passwordState.confirmPassword" placeholder="请再次输入新密码" />
            </a-form-item>
            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-col>

      <!-- 头像 -->
      <a-col :span="8">
        <div class="avatar-section content-card">
          <h3>头像</h3>
          <div class="avatar-wrapper">
            <a-avatar :size="120" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'U' }}
            </a-avatar>
            <a-upload
              name="file"
              :show-upload-list="false"
              :before-upload="handleAvatarUpload"
            >
              <a-button style="margin-top: 16px">
                <UploadOutlined />
                更换头像
              </a-button>
            </a-upload>
          </div>
        </div>

        <!-- 账号信息 -->
        <div class="account-section content-card">
          <h3>账号信息</h3>
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">2025-12-01</span>
          </div>
          <div class="info-item">
            <span class="label">最后登录</span>
            <span class="value">2025-12-02 10:30</span>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { UploadOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const userStore = useUserStore()
const passwordFormRef = ref<FormInstance>()
const saving = ref(false)
const changingPassword = ref(false)

const formState = reactive({
  nickname: '',
  phone: ''
})

const passwordState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (value !== passwordState.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const passwordRules: Record<string, Rule[]> = {
  oldPassword: [{ required: true, message: '请输入当前密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 8, message: '密码长度至少8位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    { validator: validateConfirmPassword, trigger: 'change' }
  ]
}

// 保存基本信息
const handleSave = async () => {
  saving.value = true
  try {
    const success = await userStore.updateProfile({
      nickname: formState.nickname,
      phone: formState.phone
    })
    if (success) {
      message.success('保存成功')
    } else {
      message.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    changingPassword.value = true
    
    await authApi.changePassword({
      oldPassword: passwordState.oldPassword,
      newPassword: passwordState.newPassword,
      confirmPassword: passwordState.confirmPassword
    })
    
    message.success('密码修改成功')
    passwordState.oldPassword = ''
    passwordState.newPassword = ''
    passwordState.confirmPassword = ''
  } catch {
    // 验证失败或API错误
  } finally {
    changingPassword.value = false
  }
}

// 上传头像
const handleAvatarUpload = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await authApi.uploadAvatar(formData)
    if (res.code === 200) {
      await userStore.fetchUserInfo()
      message.success('头像更新成功')
    }
  } catch {
    message.error('上传失败')
  }
  
  return false
}

onMounted(() => {
  if (userStore.userInfo) {
    formState.nickname = userStore.userInfo.nickname || ''
    formState.phone = userStore.userInfo.phone || ''
  }
})
</script>

<style lang="less" scoped>
.profile-page {
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
  
  .profile-section,
  .avatar-section,
  .account-section {
    margin-bottom: 24px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #374151;
      margin: 0 0 24px;
      padding-bottom: 12px;
      border-bottom: 1px solid #e5e7eb;
    }
  }
  
  .avatar-wrapper {
    text-align: center;
    padding: 24px 0;
  }
  
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f3f4f6;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      color: #6b7280;
    }
    
    .value {
      color: #374151;
      font-weight: 500;
    }
  }
}
</style>

