import { http } from '@/utils/request'
import type { PaginationData } from '@/utils/request'

export interface EmailTemplate {
  id: string
  name: string
  subject: string
  content: string
  contentText?: string
  variables?: string[]
  category?: string
  isDefault: boolean
  useCount: number
  lastUsedAt?: string
  createdAt: string
  updatedAt: string
}

export interface EmailTask {
  id: string
  taskName?: string
  templateId: string
  templateName?: string
  smtpConfigId: string
  subject: string
  content: string
  filterConditions?: Record<string, any>
  totalCount: number
  sentCount: number
  successCount: number
  failedCount: number
  status: number // 0=待发送, 1=发送中, 2=已暂停, 3=已完成, 4=已取消
  statusText?: string
  progress?: number
  scheduledAt?: string
  startedAt?: string
  completedAt?: string
  estimatedEndTime?: string
  createdAt: string
}

export interface EmailLog {
  id: string
  taskId: string
  customerId: string
  customerName?: string
  customerEmail: string
  customerCompany?: string
  customerCountry?: string
  customerPriority?: number
  subject: string
  content?: string
  variablesData?: Record<string, string>
  status: number // 0=待发送, 1=发送中, 2=发送成功, 3=发送失败
  statusText?: string
  errorCode?: string
  errorMessage?: string
  retryCount: number
  sentAt?: string
  createdAt: string
}

export interface SMTPConfig {
  id: string
  configName: string
  smtpHost: string
  smtpPort: number
  smtpUsername: string
  smtpPassword?: string
  senderName?: string
  senderEmail: string
  useSsl: boolean
  useTls: boolean
  isDefault: boolean
  status: number
  testStatus?: number
  testTime?: string
  dailyLimit: number
  hourlyLimit: number
  createdAt: string
}

export interface TemplateVariable {
  key: string
  label: string
  example?: string
  fieldId?: string
}

export interface EmailAttachment {
  id: string
  fileName: string
  fileUrl: string
  fileSize: number
  fileType?: string
  fileExtension?: string
  createdAt: string
}

export const emailApi = {
  // ===================== 模板相关 =====================
  
  // 获取模板列表
  getTemplates(params?: { page?: number; pageSize?: number; category?: string }) {
    return http.get<PaginationData<EmailTemplate>>('/email/templates', { params })
  },

  // 获取模板详情
  getTemplateDetail(id: string) {
    return http.get<EmailTemplate>(`/email/templates/${id}`)
  },

  // 创建模板
  createTemplate(data: Partial<EmailTemplate>) {
    return http.post<{ id: string }>('/email/templates', data)
  },

  // 更新模板
  updateTemplate(id: string, data: Partial<EmailTemplate>) {
    return http.put(`/email/templates/${id}`, data)
  },

  // 删除模板
  deleteTemplate(id: string) {
    return http.delete(`/email/templates/${id}`)
  },

  // 获取可用变量列表
  getVariables() {
    return http.get<{
      systemVariables: TemplateVariable[]
      customVariables: TemplateVariable[]
    }>('/email/templates/variables')
  },

  // 预览邮件
  previewEmail(data: { subject: string; content: string; customerId: string }) {
    return http.post<{
      subject: string
      content: string
      customer: { name: string; email: string; company?: string }
    }>('/email/templates/preview', data)
  },

  // ===================== 发送任务相关 =====================

  // 创建发送任务
  createTask(data: {
    taskName?: string
    templateId: string
    smtpConfigId: string
    customerIds?: string[]
    filter?: Record<string, any>
    scheduledAt?: string
  }) {
    return http.post<{ taskId: string; totalCount: number; status: number }>('/email/tasks', data)
  },

  // 获取任务列表
  getTasks(params?: { page?: number; pageSize?: number; status?: number }) {
    return http.get<PaginationData<EmailTask>>('/email/tasks', { params })
  },

  // 获取任务详情
  getTaskDetail(taskId: string) {
    return http.get<EmailTask>(`/email/tasks/${taskId}`)
  },

  // 开始发送
  startTask(taskId: string) {
    return http.post(`/email/tasks/${taskId}/start`)
  },

  // 暂停发送
  pauseTask(taskId: string) {
    return http.post(`/email/tasks/${taskId}/pause`)
  },

  // 继续发送
  resumeTask(taskId: string) {
    return http.post(`/email/tasks/${taskId}/resume`)
  },

  // 取消任务
  cancelTask(taskId: string) {
    return http.post(`/email/tasks/${taskId}/cancel`)
  },

  // 获取发送日志
  getTaskLogs(taskId: string, params?: { page?: number; pageSize?: number; status?: number }) {
    return http.get<PaginationData<EmailLog>>(`/email/tasks/${taskId}/logs`, { params })
  },

  // 重试失败的邮件
  retryFailed(taskId: string, logIds?: string[]) {
    return http.post(`/email/tasks/${taskId}/retry`, { logIds })
  },

  // 导出发送日志
  exportLogs(taskId: string) {
    return http.post<{ downloadUrl: string; expiresAt: string }>(`/email/tasks/${taskId}/export`)
  },

  // ===================== SMTP配置相关 =====================

  // 获取SMTP配置列表
  getSMTPConfigs() {
    return http.get<SMTPConfig[]>('/settings/smtp')
  },

  // 创建SMTP配置
  createSMTPConfig(data: Partial<SMTPConfig>) {
    return http.post<{ id: string }>('/settings/smtp', data)
  },

  // 测试SMTP连接
  testSMTPConfig(id: string) {
    return http.post<{ success: boolean; message: string }>(`/settings/smtp/${id}/test`)
  },

  // 更新SMTP配置
  updateSMTPConfig(id: string, data: Partial<SMTPConfig>) {
    return http.put(`/settings/smtp/${id}`, data)
  },

  // 删除SMTP配置
  deleteSMTPConfig(id: string) {
    return http.delete(`/settings/smtp/${id}`)
  },

  // ===================== 附件相关 =====================

  // 上传附件
  uploadAttachment(templateId: string | undefined, file: File) {
    const formData = new FormData()
    formData.append('file', file)
    if (templateId) {
      formData.append('templateId', templateId)
    }
    return http.post<EmailAttachment>('/email/attachments/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除附件
  deleteAttachment(id: string) {
    return http.delete(`/email/attachments/${id}`)
  },

  // 获取模板的附件列表
  getTemplateAttachments(templateId: string) {
    return http.get<EmailAttachment[]>(`/email/attachments/template/${templateId}`)
  }
}

