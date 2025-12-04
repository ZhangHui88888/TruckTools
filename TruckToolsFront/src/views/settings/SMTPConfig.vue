<template>
  <div class="smtp-config-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">邮箱配置</h1>
        <p class="page-subtitle">配置SMTP服务器用于发送邮件</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showAddModal">
          <PlusOutlined />
          添加配置
        </a-button>
      </div>
    </div>

    <!-- 配置列表 -->
    <a-spin :spinning="loading">
      <div v-if="configs.length === 0" class="empty-state content-card">
        <a-empty description="还没有配置邮箱">
          <a-button type="primary" @click="showAddModal">
            添加第一个邮箱配置
          </a-button>
        </a-empty>
      </div>
      
      <div v-else class="config-list">
        <div v-for="config in configs" :key="config.id" class="config-card content-card">
          <div class="card-header">
            <div class="config-info">
              <h4>{{ config.configName }}</h4>
              <p>{{ config.senderEmail }}</p>
            </div>
            <div class="config-badges">
              <a-tag v-if="config.isDefault" color="blue">默认</a-tag>
              <a-tag v-if="config.testStatus === 1" color="green">已验证</a-tag>
              <a-tag v-else-if="config.testStatus === 0" color="red">验证失败</a-tag>
              <a-tag v-else color="default">未验证</a-tag>
            </div>
          </div>
          
          <div class="card-body">
            <a-descriptions :column="2" size="small">
              <a-descriptions-item label="SMTP服务器">{{ config.smtpHost }}</a-descriptions-item>
              <a-descriptions-item label="端口">{{ config.smtpPort }}</a-descriptions-item>
              <a-descriptions-item label="加密方式">
                {{ config.useSsl ? 'SSL' : config.useTls ? 'TLS' : '无' }}
              </a-descriptions-item>
              <a-descriptions-item label="发件人名称">{{ config.senderName || '-' }}</a-descriptions-item>
              <a-descriptions-item label="每日限制">{{ config.dailyLimit }} 封</a-descriptions-item>
              <a-descriptions-item label="每小时限制">{{ config.hourlyLimit }} 封</a-descriptions-item>
            </a-descriptions>
          </div>
          
          <div class="card-footer">
            <a-space>
              <a-button size="small" :loading="testingId === config.id" @click="handleTest(config)">
                <ApiOutlined />
                测试连接
              </a-button>
              <a-button size="small" @click="handleEdit(config)">
                <EditOutlined />
                编辑
              </a-button>
              <a-button v-if="!config.isDefault" size="small" @click="handleSetDefault(config)">
                设为默认
              </a-button>
              <a-popconfirm title="确定删除此配置吗？" @confirm="handleDelete(config)">
                <a-button size="small" danger>
                  <DeleteOutlined />
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </div>
        </div>
      </div>
    </a-spin>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="currentConfig ? '编辑配置' : '添加配置'"
      :width="600"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="配置名称" name="configName">
          <a-input v-model:value="formState.configName" placeholder="如：公司邮箱" />
        </a-form-item>
        <a-form-item label="SMTP服务器" name="smtpHost">
          <a-input v-model:value="formState.smtpHost" placeholder="如：smtp.example.com" />
        </a-form-item>
        <a-form-item label="端口" name="smtpPort">
          <a-input-number v-model:value="formState.smtpPort" :min="1" :max="65535" style="width: 120px" />
        </a-form-item>
        <a-form-item label="用户名" name="smtpUsername">
          <a-input v-model:value="formState.smtpUsername" placeholder="SMTP登录用户名" />
        </a-form-item>
        <a-form-item label="密码" name="smtpPassword">
          <a-input-password v-model:value="formState.smtpPassword" placeholder="SMTP登录密码" />
        </a-form-item>
        <a-form-item label="发件人名称" name="senderName">
          <a-input v-model:value="formState.senderName" placeholder="如：ABC公司销售部" />
        </a-form-item>
        <a-form-item label="发件人邮箱" name="senderEmail">
          <a-input v-model:value="formState.senderEmail" placeholder="发件人显示的邮箱地址" />
        </a-form-item>
        <a-form-item label="加密方式">
          <a-radio-group v-model:value="formState.encryption">
            <a-radio value="ssl">SSL</a-radio>
            <a-radio value="tls">TLS</a-radio>
            <a-radio value="none">无</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="发送限制">
          <a-space>
            <span>每日</span>
            <a-input-number v-model:value="formState.dailyLimit" :min="1" style="width: 100px" />
            <span>封，每小时</span>
            <a-input-number v-model:value="formState.hourlyLimit" :min="1" style="width: 100px" />
            <span>封</span>
          </a-space>
        </a-form-item>
        <a-form-item label="设为默认" name="isDefault">
          <a-switch v-model:checked="formState.isDefault" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  PlusOutlined,
  ApiOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { emailApi } from '@/api/email'
import type { SMTPConfig } from '@/api/email'

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const testingId = ref<string>()
const showModal = ref(false)
const configs = ref<SMTPConfig[]>([])
const currentConfig = ref<SMTPConfig | null>(null)

const formState = reactive({
  configName: '',
  smtpHost: '',
  smtpPort: 465,
  smtpUsername: '',
  smtpPassword: '',
  senderName: '',
  senderEmail: '',
  encryption: 'ssl' as 'ssl' | 'tls' | 'none',
  dailyLimit: 500,
  hourlyLimit: 100,
  isDefault: false
})

const rules = {
  configName: [{ required: true, message: '请输入配置名称' }],
  smtpHost: [{ required: true, message: '请输入SMTP服务器地址' }],
  smtpPort: [{ required: true, message: '请输入端口' }],
  smtpUsername: [{ required: true, message: '请输入用户名' }],
  smtpPassword: [{ required: true, message: '请输入密码' }],
  senderEmail: [
    { required: true, message: '请输入发件人邮箱' },
    { type: 'email', message: '请输入有效的邮箱地址' }
  ]
}

// 获取配置列表
const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await emailApi.getSMTPConfigs()
    configs.value = res.data
  } catch {
    message.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加弹窗
const showAddModal = () => {
  currentConfig.value = null
  resetForm()
  showModal.value = true
}

// 编辑
const handleEdit = (config: SMTPConfig) => {
  currentConfig.value = config
  formState.configName = config.configName
  formState.smtpHost = config.smtpHost
  formState.smtpPort = config.smtpPort
  formState.smtpUsername = config.smtpUsername
  formState.smtpPassword = ''
  formState.senderName = config.senderName || ''
  formState.senderEmail = config.senderEmail
  formState.encryption = config.useSsl ? 'ssl' : config.useTls ? 'tls' : 'none'
  formState.dailyLimit = config.dailyLimit
  formState.hourlyLimit = config.hourlyLimit
  formState.isDefault = config.isDefault
  showModal.value = true
}

// 重置表单
const resetForm = () => {
  formState.configName = ''
  formState.smtpHost = ''
  formState.smtpPort = 465
  formState.smtpUsername = ''
  formState.smtpPassword = ''
  formState.senderName = ''
  formState.senderEmail = ''
  formState.encryption = 'ssl'
  formState.dailyLimit = 500
  formState.hourlyLimit = 100
  formState.isDefault = false
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    const data = {
      configName: formState.configName,
      smtpHost: formState.smtpHost,
      smtpPort: formState.smtpPort,
      smtpUsername: formState.smtpUsername,
      smtpPassword: formState.smtpPassword || undefined,
      senderName: formState.senderName || undefined,
      senderEmail: formState.senderEmail,
      useSsl: formState.encryption === 'ssl',
      useTls: formState.encryption === 'tls',
      dailyLimit: formState.dailyLimit,
      hourlyLimit: formState.hourlyLimit,
      isDefault: formState.isDefault
    }
    
    if (currentConfig.value) {
      await emailApi.updateSMTPConfig(currentConfig.value.id, data)
      message.success('更新成功')
    } else {
      await emailApi.createSMTPConfig(data)
      message.success('添加成功')
    }
    
    showModal.value = false
    fetchConfigs()
  } catch {
    // 验证失败
  } finally {
    saving.value = false
  }
}

// 测试连接
const handleTest = async (config: SMTPConfig) => {
  testingId.value = config.id
  try {
    const res = await emailApi.testSMTPConfig(config.id)
    if (res.data.success) {
      message.success('连接成功，测试邮件已发送')
      fetchConfigs()
    } else {
      message.error(res.data.message || '连接失败')
    }
  } catch {
    message.error('测试失败')
  } finally {
    testingId.value = undefined
  }
}

// 设为默认
const handleSetDefault = async (config: SMTPConfig) => {
  try {
    await emailApi.updateSMTPConfig(config.id, { isDefault: true })
    message.success('已设为默认')
    fetchConfigs()
  } catch {
    message.error('操作失败')
  }
}

// 删除
const handleDelete = async (config: SMTPConfig) => {
  try {
    await emailApi.deleteSMTPConfig(config.id)
    message.success('删除成功')
    fetchConfigs()
  } catch {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style lang="less" scoped>
.smtp-config-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
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
  
  .empty-state {
    text-align: center;
    padding: 48px;
  }
  
  .config-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .config-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      
      h4 {
        margin: 0 0 4px;
        font-size: 16px;
        font-weight: 600;
        color: #374151;
      }
      
      p {
        margin: 0;
        font-size: 14px;
        color: #6b7280;
      }
      
      .config-badges {
        display: flex;
        gap: 8px;
      }
    }
    
    .card-body {
      padding: 16px;
      background: #f9fafb;
      border-radius: 8px;
      margin-bottom: 16px;
    }
    
    .card-footer {
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>

