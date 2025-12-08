import { http } from '@/utils/request'

export interface ScriptTemplate {
  id: string
  name: string
  content: string
  description?: string
  sortOrder: number
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface ScriptTemplateRequest {
  name: string
  content: string
  description?: string
  sortOrder?: number
  enabled?: boolean
}

export interface GeneratedScript {
  templateId: string
  templateName: string
  generatedContent: string
}

export const scriptTemplateApi = {
  // 获取话术模板列表
  getList() {
    return http.get<ScriptTemplate[]>('/script-templates')
  },

  // 获取话术模板详情
  getDetail(id: string) {
    return http.get<ScriptTemplate>(`/script-templates/${id}`)
  },

  // 创建话术模板
  create(data: ScriptTemplateRequest) {
    return http.post<ScriptTemplate>('/script-templates', data)
  },

  // 更新话术模板
  update(id: string, data: ScriptTemplateRequest) {
    return http.put<ScriptTemplate>(`/script-templates/${id}`, data)
  },

  // 删除话术模板
  delete(id: string) {
    return http.delete(`/script-templates/${id}`)
  },

  // 根据客户生成话术
  generateForCustomer(customerId: string) {
    return http.get<GeneratedScript[]>(`/script-templates/generate/${customerId}`)
  },

  // 获取支持的变量列表
  getSupportedVariables() {
    return http.get<string[]>('/script-templates/variables')
  }
}
