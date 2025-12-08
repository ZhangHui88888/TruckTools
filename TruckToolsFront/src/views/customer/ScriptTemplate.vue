<template>
  <div class="script-template-page">
    <a-card title="话术模板管理">
      <template #extra>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新建模板
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-empty v-if="templates.length === 0" description="暂无话术模板" />
        
        <div v-else class="template-list">
          <a-card 
            v-for="template in templates" 
            :key="template.id"
            class="template-card"
            :class="{ disabled: !template.enabled }"
          >
            <template #title>
              <div class="template-header">
                <span class="template-name">{{ template.name }}</span>
                <a-tag :color="template.enabled ? 'green' : 'default'">
                  {{ template.enabled ? '启用' : '禁用' }}
                </a-tag>
              </div>
            </template>
            <template #extra>
              <a-space>
                <a-button type="link" size="small" @click="handleEdit(template)">
                  <EditOutlined /> 编辑
                </a-button>
                <a-popconfirm 
                  title="确定删除此模板吗？" 
                  @confirm="handleDelete(template.id)"
                >
                  <a-button type="link" size="small" danger>
                    <DeleteOutlined /> 删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
            
            <div class="template-description" v-if="template.description">
              {{ template.description }}
            </div>
            <div class="template-content">
              <pre>{{ template.content }}</pre>
            </div>
          </a-card>
        </div>
      </a-spin>
    </a-card>

    <!-- 变量说明 -->
    <a-card title="支持的变量" class="variables-card">
      <a-table 
        :columns="variableColumns" 
        :data-source="variableData" 
        :pagination="false"
        size="small"
      />
    </a-card>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="isEdit ? '编辑话术模板' : '新建话术模板'"
      width="700px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="模板名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="模板描述" name="description">
          <a-input v-model:value="form.description" placeholder="请输入模板描述（可选）" />
        </a-form-item>
        <a-form-item label="模板内容" name="content">
          <a-textarea
            v-model:value="form.content"
            placeholder="请输入话术内容，支持变量如 {{name}}, {{company}} 等"
            :rows="6"
          />
          <div class="form-tip">
            支持变量：{{name}} 客户姓名、{{company}} 公司、{{position}} 职位、{{country}} 国家、
            {{email}} 邮箱、{{phone}} 手机、{{wechatName}} 微信、{{whatsappName}} WhatsApp、
            {{meetingTime}} 会面时间、{{meetingLocation}} 会面地点
          </div>
        </a-form-item>
        <a-form-item label="排序号" name="sortOrder">
          <a-input-number v-model:value="form.sortOrder" :min="0" placeholder="数字越小越靠前" />
        </a-form-item>
        <a-form-item label="状态" name="enabled">
          <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { scriptTemplateApi } from '@/api/scriptTemplate'
import type { ScriptTemplate, ScriptTemplateRequest } from '@/api/scriptTemplate'

const loading = ref(false)
const templates = ref<ScriptTemplate[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const currentId = ref<string | null>(null)
const formRef = ref()

const form = reactive<ScriptTemplateRequest & { enabled: boolean }>({
  name: '',
  content: '',
  description: '',
  sortOrder: 0,
  enabled: true
})

const formRules = {
  name: [{ required: true, message: '请输入模板名称' }],
  content: [{ required: true, message: '请输入模板内容' }]
}

const variableColumns = [
  { title: '变量', dataIndex: 'variable', key: 'variable' },
  { title: '说明', dataIndex: 'description', key: 'description' },
  { title: '示例', dataIndex: 'example', key: 'example' }
]

const variableData = [
  { key: '1', variable: '{{name}}', description: '客户姓名', example: '张三' },
  { key: '2', variable: '{{company}}', description: '公司名称', example: 'ABC贸易公司' },
  { key: '3', variable: '{{position}}', description: '职位', example: '采购经理' },
  { key: '4', variable: '{{country}}', description: '国家', example: '印度' },
  { key: '5', variable: '{{email}}', description: '邮箱', example: 'example@email.com' },
  { key: '6', variable: '{{phone}}', description: '手机号', example: '+86 138xxxx' },
  { key: '7', variable: '{{wechatName}}', description: '微信名称', example: 'wechat_id' },
  { key: '8', variable: '{{whatsappName}}', description: 'WhatsApp', example: '+91 xxxxx' },
  { key: '9', variable: '{{meetingTime}}', description: '会面时间', example: '2025-11-26' },
  { key: '10', variable: '{{meetingLocation}}', description: '会面地点', example: '上海展博会' }
]

const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await scriptTemplateApi.getList()
    templates.value = res.data
  } catch (error) {
    console.error('获取模板列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.name = ''
  form.content = ''
  form.description = ''
  form.sortOrder = 0
  form.enabled = true
  showModal.value = true
}

const handleEdit = (template: ScriptTemplate) => {
  isEdit.value = true
  currentId.value = template.id
  form.name = template.name
  form.content = template.content
  form.description = template.description || ''
  form.sortOrder = template.sortOrder
  form.enabled = template.enabled
  showModal.value = true
}

const handleDelete = async (id: string) => {
  try {
    await scriptTemplateApi.delete(id)
    message.success('删除成功')
    fetchTemplates()
  } catch {
    message.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    const data: ScriptTemplateRequest = {
      name: form.name,
      content: form.content,
      description: form.description,
      sortOrder: form.sortOrder,
      enabled: form.enabled
    }
    
    if (isEdit.value && currentId.value) {
      await scriptTemplateApi.update(currentId.value, data)
      message.success('更新成功')
    } else {
      await scriptTemplateApi.create(data)
      message.success('创建成功')
    }
    
    showModal.value = false
    fetchTemplates()
  } catch (error) {
    console.error('提交失败', error)
  }
}

const handleCancel = () => {
  showModal.value = false
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style lang="less" scoped>
.script-template-page {
  padding: 24px;
  
  .template-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 16px;
  }
  
  .template-card {
    &.disabled {
      opacity: 0.6;
    }
    
    .template-header {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .template-name {
        font-weight: 500;
      }
    }
    
    .template-description {
      color: #666;
      font-size: 13px;
      margin-bottom: 12px;
    }
    
    .template-content {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 6px;
      
      pre {
        margin: 0;
        white-space: pre-wrap;
        word-break: break-word;
        font-family: inherit;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }
  
  .variables-card {
    margin-top: 24px;
  }
  
  .form-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
    line-height: 1.6;
  }
}
</style>
