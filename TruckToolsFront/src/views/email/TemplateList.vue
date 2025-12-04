<template>
  <div class="template-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">邮件模板</h1>
        <p class="page-subtitle">管理您的邮件模板，支持个性化变量</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="$router.push('/email/template/edit')">
          <PlusOutlined />
          新建模板
        </a-button>
      </div>
    </div>

    <!-- 模板列表 -->
    <div class="template-grid">
      <a-spin :spinning="loading">
        <a-empty v-if="templates.length === 0" description="暂无模板">
          <a-button type="primary" @click="$router.push('/email/template/edit')">
            创建第一个模板
          </a-button>
        </a-empty>
        
        <a-row v-else :gutter="[24, 24]">
          <a-col v-for="template in templates" :key="template.id" :xs="24" :sm="12" :lg="8" :xl="6">
            <div class="template-card content-card">
              <div class="template-header">
                <div class="template-icon">
                  <MailOutlined />
                </div>
                <a-dropdown>
                  <a-button type="text" size="small">
                    <MoreOutlined />
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item key="edit" @click="$router.push(`/email/template/edit/${template.id}`)">
                        <EditOutlined /> 编辑
                      </a-menu-item>
                      <a-menu-item key="copy" @click="handleCopy(template)">
                        <CopyOutlined /> 复制
                      </a-menu-item>
                      <a-menu-item key="use" @click="handleUse(template)">
                        <SendOutlined /> 使用此模板
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item key="delete" danger @click="handleDelete(template)">
                        <DeleteOutlined /> 删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
              
              <h4 class="template-name">{{ template.name }}</h4>
              <p class="template-subject">{{ template.subject }}</p>
              
              <div class="template-meta">
                <div class="meta-item">
                  <ClockCircleOutlined />
                  {{ dayjs(template.createdAt).format('YYYY-MM-DD') }}
                </div>
                <div class="meta-item">
                  <BarChartOutlined />
                  使用 {{ template.useCount }} 次
                </div>
              </div>
              
              <div v-if="template.variables?.length" class="template-variables">
                <a-tag v-for="v in template.variables.slice(0, 3)" :key="v" size="small" v-text="'{{' + v + '}}'" />
                <span v-if="template.variables.length > 3" class="more-vars">
                  +{{ template.variables.length - 3 }}
                </span>
              </div>
            </div>
          </a-col>
        </a-row>
        
        <!-- 分页 -->
        <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
          <a-pagination
            v-model:current="pagination.current"
            :total="pagination.total"
            :page-size="pagination.pageSize"
            show-less-items
            @change="handlePageChange"
          />
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PlusOutlined,
  MailOutlined,
  MoreOutlined,
  EditOutlined,
  CopyOutlined,
  SendOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { emailApi } from '@/api/email'
import type { EmailTemplate } from '@/api/email'

const router = useRouter()
const loading = ref(false)
const templates = ref<EmailTemplate[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0
})

// 获取模板列表
const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await emailApi.getTemplates({
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    templates.value = res.data.list
    pagination.total = res.data.pagination.total
  } catch {
    message.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  fetchTemplates()
}

// 复制模板
const handleCopy = async (template: EmailTemplate) => {
  try {
    await emailApi.createTemplate({
      name: `${template.name} - 副本`,
      subject: template.subject,
      content: template.content,
      category: template.category
    })
    message.success('复制成功')
    fetchTemplates()
  } catch {
    message.error('复制失败')
  }
}

// 使用模板
const handleUse = (template: EmailTemplate) => {
  router.push({
    path: '/email/send',
    query: { templateId: template.id }
  })
}

// 删除模板
const handleDelete = (template: EmailTemplate) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除模板"${template.name}"吗？`,
    okText: '删除',
    okType: 'danger',
    async onOk() {
      try {
        await emailApi.deleteTemplate(template.id)
        message.success('删除成功')
        fetchTemplates()
      } catch {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style lang="less" scoped>
.template-list-page {
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
  
  .template-card {
    height: 100%;
    transition: box-shadow 0.2s, transform 0.2s;
    cursor: pointer;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    .template-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }
    
    .template-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: linear-gradient(135deg, #1677ff, #4096ff);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 20px;
    }
    
    .template-name {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .template-subject {
      font-size: 13px;
      color: #6b7280;
      margin: 0 0 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .template-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #9ca3af;
      margin-bottom: 12px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .template-variables {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      align-items: center;
      
      .more-vars {
        font-size: 12px;
        color: #6b7280;
      }
    }
  }
  
  .pagination-wrapper {
    margin-top: 24px;
    text-align: center;
  }
}
</style>

