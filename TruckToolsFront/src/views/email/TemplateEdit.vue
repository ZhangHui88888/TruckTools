<template>
  <div class="template-edit-page">
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" @click="$router.back()">
          <ArrowLeftOutlined />
        </a-button>
        <h1 class="page-title">{{ isEdit ? '编辑模板' : '新建模板' }}</h1>
      </div>
      <div class="header-actions">
        <a-button @click="handlePreview">
          <EyeOutlined />
          预览
        </a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <SaveOutlined />
          保存
        </a-button>
      </div>
    </div>

    <div class="edit-content">
      <a-row :gutter="24">
        <!-- 编辑区域 -->
        <a-col :span="16">
          <div class="editor-section content-card">
            <a-form :model="formState" layout="vertical">
              <a-form-item label="模板名称" required>
                <a-input
                  v-model:value="formState.name"
                  placeholder="请输入模板名称，如：展会感谢信"
                  :maxlength="100"
                />
              </a-form-item>
              
              <a-form-item label="邮件主题" required>
                <a-input
                  v-model:value="formState.subject"
                  placeholder="请输入邮件主题，支持变量如 {{客户姓名}}"
                  :maxlength="500"
                >
                  <template #suffix>
                    <a-dropdown :trigger="['click']">
                      <a-button type="link" size="small">
                        插入变量
                      </a-button>
                      <template #overlay>
                        <a-menu @click="insertSubjectVariable">
                          <a-menu-item v-for="v in variables" :key="v.key">
                            {{ v.label }}
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </template>
                </a-input>
              </a-form-item>
              
              <a-form-item label="邮件正文" required>
                <div class="editor-toolbar">
                  <a-dropdown :trigger="['click']">
                    <a-button size="small">
                      <TagOutlined />
                      插入变量
                    </a-button>
                    <template #overlay>
                      <a-menu @click="insertContentVariable">
                        <template v-if="systemVariables.length">
                          <a-menu-item-group title="系统变量">
                            <a-menu-item v-for="v in systemVariables" :key="v.key">
                              {{ v.label }} ({{ v.example }})
                            </a-menu-item>
                          </a-menu-item-group>
                        </template>
                        <template v-if="customVariables.length">
                          <a-menu-item-group title="自定义字段">
                            <a-menu-item v-for="v in customVariables" :key="v.key">
                              {{ v.label }}
                            </a-menu-item>
                          </a-menu-item-group>
                        </template>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </div>
                <div class="editor-wrapper">
                  <!-- 使用简单的 textarea，实际项目可替换为 TinyMCE -->
                  <a-textarea
                    ref="editorRef"
                    v-model:value="formState.content"
                    :rows="16"
                    placeholder="请输入邮件正文内容...

支持使用变量，如：
尊敬的{{客户姓名}}先生/女士，

感谢您在{{会面地点}}与我们会面..."
                  />
                </div>
              </a-form-item>
              
              <a-form-item label="模板分类">
                <a-select
                  v-model:value="formState.category"
                  placeholder="选择或输入分类"
                  allow-clear
                  mode="tags"
                  style="width: 200px"
                >
                  <a-select-option value="展会跟进">展会跟进</a-select-option>
                  <a-select-option value="产品推广">产品推广</a-select-option>
                  <a-select-option value="节日问候">节日问候</a-select-option>
                  <a-select-option value="促销活动">促销活动</a-select-option>
                </a-select>
              </a-form-item>
              
              <a-form-item label="邮件附件">
                <div class="attachment-section">
                  <a-upload
                    :before-upload="handleUploadAttachment"
                    :show-upload-list="false"
                    :multiple="true"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.jpg,.jpeg,.png,.gif,.zip,.rar"
                  >
                    <a-button :loading="uploadingAttachment">
                      <UploadOutlined />
                      上传附件
                    </a-button>
                  </a-upload>
                  <span class="attachment-tips">支持 PDF、Office 文档、图片、压缩包等，单个文件不超过 10MB</span>
                  
                  <div v-if="attachments.length" class="attachment-list">
                    <div v-for="att in attachments" :key="att.id" class="attachment-item">
                      <PaperClipOutlined />
                      <span class="attachment-name">{{ att.fileName }}</span>
                      <span class="attachment-size">{{ formatFileSize(att.fileSize) }}</span>
                      <a-button type="text" danger size="small" @click="handleDeleteAttachment(att)">
                        <DeleteOutlined />
                      </a-button>
                    </div>
                  </div>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </a-col>
        
        <!-- 右侧面板 -->
        <a-col :span="8">
          <!-- 可用变量 -->
          <div class="variables-section content-card">
            <h4>可用变量</h4>
            <p class="section-desc">点击变量可复制到剪贴板</p>
            
            <div class="variable-group">
              <h5>系统变量</h5>
              <div class="variable-list">
                <a-tag
                  v-for="v in systemVariables"
                  :key="v.key"
                  class="variable-tag"
                  @click="copyVariable(v.key)"
                  v-text="formatVariable(v.key)"
                />
              </div>
            </div>
            
            <div v-if="customVariables.length" class="variable-group">
              <h5>自定义字段</h5>
              <div class="variable-list">
                <a-tag
                  v-for="v in customVariables"
                  :key="v.key"
                  class="variable-tag"
                  color="orange"
                  @click="copyVariable(v.key)"
                  v-text="formatVariable(v.key)"
                />
              </div>
            </div>
          </div>
          
          <!-- 使用提示 -->
          <div class="tips-section content-card">
            <h4>使用提示</h4>
            <ul class="tips-list">
              <li>变量格式为 <code>{'{{变量名}}'}</code></li>
              <li>发送时会自动替换为实际客户信息</li>
              <li>如果客户数据为空，变量将显示为空</li>
              <li>建议在预览中检查变量替换效果</li>
            </ul>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 预览弹窗 -->
    <a-modal
      v-model:open="showPreview"
      title="邮件预览"
      width="700"
      :footer="null"
    >
      <div class="preview-content">
        <div class="preview-header">
          <label>收件人：</label>
          <a-select
            v-model:value="previewCustomerId"
            placeholder="选择客户预览效果"
            style="width: 300px"
            show-search
            :filter-option="false"
            @search="searchCustomers"
            @change="handlePreviewCustomerChange"
          >
            <a-select-option v-for="c in previewCustomers" :key="c.id" :value="c.id">
              {{ c.name }} ({{ c.email }})
            </a-select-option>
          </a-select>
        </div>
        
        <div v-if="previewData" class="preview-email">
          <div class="email-subject">
            <label>主题：</label>
            <span>{{ previewData.subject }}</span>
          </div>
          <div class="email-body" v-html="previewData.content"></div>
        </div>
        
        <a-empty v-else description="请选择客户预览效果" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  EyeOutlined,
  SaveOutlined,
  TagOutlined,
  PaperClipOutlined,
  DeleteOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import { emailApi } from '@/api/email'
import { customerApi } from '@/api/customer'
import type { TemplateVariable, EmailAttachment } from '@/api/email'
import type { Customer } from '@/api/customer'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const editorRef = ref()

const formState = reactive({
  name: '',
  subject: '',
  content: '',
  category: undefined as string | undefined
})

// 变量
const systemVariables = ref<TemplateVariable[]>([])
const customVariables = ref<TemplateVariable[]>([])
const variables = computed(() => [...systemVariables.value, ...customVariables.value])

// 预览
const showPreview = ref(false)
const previewCustomerId = ref<string>()
const previewCustomers = ref<Customer[]>([])
const previewData = ref<{ subject: string; content: string } | null>(null)

// 附件
const attachments = ref<EmailAttachment[]>([])
const uploadingAttachment = ref(false)

// 获取模板详情
const fetchTemplate = async () => {
  if (!route.params.id) return
  
  try {
    const res = await emailApi.getTemplateDetail(route.params.id as string)
    formState.name = res.data.name
    formState.subject = res.data.subject
    formState.content = res.data.content
    formState.category = res.data.category
    
    // 获取模板附件
    fetchAttachments()
  } catch {
    message.error('获取模板失败')
    router.push('/email/template')
  }
}

// 获取附件列表
const fetchAttachments = async () => {
  if (!route.params.id) return
  
  try {
    const res = await emailApi.getTemplateAttachments(route.params.id as string)
    attachments.value = res.data
  } catch {
    // ignore
  }
}

// 上传附件
const handleUploadAttachment = async (file: File) => {
  uploadingAttachment.value = true
  try {
    const templateId = route.params.id as string | undefined
    const res = await emailApi.uploadAttachment(templateId, file)
    attachments.value.push(res.data)
    message.success('附件上传成功')
  } catch {
    message.error('附件上传失败')
  } finally {
    uploadingAttachment.value = false
  }
  return false
}

// 删除附件
const handleDeleteAttachment = async (attachment: EmailAttachment) => {
  try {
    await emailApi.deleteAttachment(attachment.id)
    attachments.value = attachments.value.filter(a => a.id !== attachment.id)
    message.success('附件已删除')
  } catch {
    message.error('删除失败')
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 获取可用变量
const fetchVariables = async () => {
  try {
    const res = await emailApi.getVariables()
    systemVariables.value = res.data.systemVariables
    customVariables.value = res.data.customVariables
  } catch {
    // 使用默认变量
    systemVariables.value = [
      { key: 'name', label: '客户姓名', example: 'John Smith' },
      { key: 'email', label: '邮箱', example: 'john@example.com' },
      { key: 'company', label: '公司名称', example: 'ABC Trading' },
      { key: 'position', label: '职位', example: 'Sales Manager' },
      { key: 'country', label: '国家', example: '美国' },
      { key: 'meetingTime', label: '会面时间', example: '2025-11-28' },
      { key: 'meetingLocation', label: '会面地点', example: '广交会' }
    ]
  }
}

// 插入主题变量
const insertSubjectVariable = (e: { key: string }) => {
  const variable = `{{${e.key}}}`
  formState.subject += variable
}

// 插入正文变量
const insertContentVariable = (e: { key: string }) => {
  const variable = `{{${e.key}}}`
  const textarea = editorRef.value?.resizableTextArea?.textArea
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = formState.content
    formState.content = text.substring(0, start) + variable + text.substring(end)
    // 恢复光标位置
    setTimeout(() => {
      textarea.selectionStart = textarea.selectionEnd = start + variable.length
      textarea.focus()
    }, 0)
  } else {
    formState.content += variable
  }
}

// 格式化变量显示
const formatVariable = (key: string) => {
  return `{{${key}}}`
}

// 复制变量
const copyVariable = (key: string) => {
  const variable = formatVariable(key)
  navigator.clipboard.writeText(variable)
  message.success(`已复制: ${variable}`)
}

// 搜索客户
const searchCustomers = async (keyword: string) => {
  if (!keyword) return
  try {
    const res = await customerApi.getList({ keyword, pageSize: 10 })
    previewCustomers.value = res.data.list
  } catch {
    // ignore
  }
}

// 预览
const handlePreview = () => {
  showPreview.value = true
}

// 切换预览客户
const handlePreviewCustomerChange = async (customerId: string) => {
  if (!customerId) {
    previewData.value = null
    return
  }
  
  try {
    const res = await emailApi.previewEmail({
      subject: formState.subject,
      content: formState.content,
      customerId
    })
    previewData.value = res.data
  } catch {
    message.error('预览失败')
  }
}

// 保存
const handleSave = async () => {
  if (!formState.name) {
    message.error('请输入模板名称')
    return
  }
  if (!formState.subject) {
    message.error('请输入邮件主题')
    return
  }
  if (!formState.content) {
    message.error('请输入邮件正文')
    return
  }
  
  saving.value = true
  try {
    const data = {
      name: formState.name,
      subject: formState.subject,
      content: formState.content,
      category: formState.category
    }
    
    if (isEdit.value) {
      await emailApi.updateTemplate(route.params.id as string, data)
      message.success('更新成功')
    } else {
      await emailApi.createTemplate(data)
      message.success('创建成功')
    }
    
    router.push('/email/template')
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchVariables()
  if (isEdit.value) {
    fetchTemplate()
  }
})
</script>

<style lang="less" scoped>
.template-edit-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .editor-section {
    .editor-toolbar {
      margin-bottom: 8px;
    }
    
    .editor-wrapper {
      :deep(.ant-input) {
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 14px;
        line-height: 1.6;
      }
    }
  }
  
  .variables-section,
  .tips-section {
    margin-bottom: 24px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #374151;
      margin: 0 0 8px;
    }
    
    .section-desc {
      font-size: 13px;
      color: #6b7280;
      margin: 0 0 16px;
    }
  }
  
  .variable-group {
    margin-bottom: 16px;
    
    h5 {
      font-size: 13px;
      font-weight: 500;
      color: #6b7280;
      margin: 0 0 8px;
    }
  }
  
  .variable-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .variable-tag {
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: scale(1.05);
      }
    }
  }
  
  .tips-list {
    margin: 0;
    padding-left: 20px;
    color: #6b7280;
    font-size: 13px;
    line-height: 2;
    
    code {
      background: #f3f4f6;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
    }
  }
  
  .preview-content {
    .preview-header {
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      
      label {
        font-weight: 500;
      }
    }
    
    .preview-email {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      overflow: hidden;
      
      .email-subject {
        padding: 12px 16px;
        background: #f9fafb;
        border-bottom: 1px solid #e5e7eb;
        
        label {
          font-weight: 500;
          margin-right: 8px;
        }
      }
      
      .email-body {
        padding: 16px;
        min-height: 200px;
        line-height: 1.8;
        white-space: pre-wrap;
      }
    }
  }
  
  .attachment-section {
    .attachment-tips {
      display: block;
      margin-top: 8px;
      font-size: 12px;
      color: #9ca3af;
    }
    
    .attachment-list {
      margin-top: 12px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      overflow: hidden;
      
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        border-bottom: 1px solid #f3f4f6;
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background: #f9fafb;
        }
        
        .attachment-name {
          flex: 1;
          font-size: 14px;
          color: #374151;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .attachment-size {
          font-size: 12px;
          color: #9ca3af;
        }
      }
    }
  }
}
</style>

