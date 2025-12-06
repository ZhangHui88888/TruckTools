import { http } from '@/utils/request'
import type { PaginationData } from '@/utils/request'

export interface Customer {
  id: string
  name: string
  email: string
  phone?: string
  company?: string
  position?: string
  country?: string
  countryCode?: string
  address?: string
  website?: string
  priority: number
  meetingTime?: string
  meetingLocation?: string
  wechatName?: string
  wechatQrcode?: string
  whatsappName?: string
  whatsappQrcode?: string
  businessCardFront?: string
  businessCardBack?: string
  followUpStatus?: string
  remark?: string
  source: 'manual' | 'ocr' | 'import'
  sourceFile?: string
  ocrConfidence?: number
  emailStatus: number
  lastEmailTime?: string
  emailCount: number
  customFields?: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface CustomerListParams {
  page?: number
  pageSize?: number
  keyword?: string
  priority?: number
  country?: string
  source?: string
  startDate?: string
  endDate?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

export interface BusinessCard {
  id: string
  imageUrl: string
  thumbnailUrl?: string
  priority: number
  ocrStatus: number // 0=待识别, 1=识别中, 2=识别成功, 3=识别失败
  ocrConfidence?: number
  parsedData?: {
    name?: string
    email?: string
    phone?: string
    company?: string
    position?: string
    address?: string
    website?: string
  }
  hasWechatQr: boolean
  hasWhatsappQr: boolean
  isProcessed: boolean
  createdAt: string
}

export interface ImportResult {
  importId: string
  fileName: string
  totalRows: number
  headers: string[]
  suggestedMapping: Record<string, string>
  previewData: Record<string, string>[]
}

export interface ImportValidation {
  totalRows: number
  validRows: number
  invalidRows: number
  duplicateRows: number
  duplicateInFile?: number
  duplicateInDb?: number
  errors: { row: number; field: string; value: string; message: string }[]
  errorFileUrl?: string
  newDataPreview?: { rowNum: string; name: string; email: string; phone: string; company: string; country: string }[]
  duplicateDataPreview?: { rowNum: string; name: string; email: string; phone: string; company: string; country: string }[]
}

export interface ImportStatus {
  importId: string
  status: 'pending' | 'processing' | 'completed' | 'failed'
  totalRows: number
  successCount: number
  failedCount: number
  skippedCount: number
  progress: number
  startedAt?: string
  completedAt?: string
  logFileUrl?: string
}

export interface CustomerEvent {
  id: string
  customerId: string
  eventTime: string
  eventLocation?: string
  eventContent: string
  eventStatus: 'pending_customer' | 'pending_us'
  createdAt: string
  updatedAt: string
}

export interface CustomerEventRequest {
  customerId: string
  eventTime: string
  eventLocation?: string
  eventContent: string
  eventStatus: 'pending_customer' | 'pending_us'
}

export const customerApi = {
  // 获取客户列表
  getList(params: CustomerListParams) {
    return http.get<PaginationData<Customer>>('/customers', { params })
  },

  // 获取客户详情
  getDetail(id: string) {
    return http.get<Customer>(`/customers/${id}`)
  },

  // 创建客户
  create(data: Partial<Customer>) {
    return http.post<{ id: string }>('/customers', data)
  },

  // 更新客户
  update(id: string, data: Partial<Customer>) {
    return http.put(`/customers/${id}`, data)
  },

  // 上传名片图片
  uploadBusinessCard(id: string, side: 'front' | 'back', formData: FormData) {
    return http.upload<{ imageUrl: string }>(`/customers/${id}/business-card/${side}`, formData)
  },

  // 删除客户
  delete(id: string) {
    return http.delete(`/customers/${id}`)
  },

  // 批量删除客户
  batchDelete(ids: string[]) {
    return http.delete('/customers/batch', { data: { ids } })
  },

  // 导出客户
  export(data: { ids?: string[]; filter?: Partial<CustomerListParams>; fields?: string[]; format?: string }) {
    return http.post<{ downloadUrl: string; expiresAt: string }>('/customers/export', data)
  },

  // ===================== 名片识别相关 =====================
  
  // 上传名片
  uploadCards(formData: FormData) {
    return http.upload<{
      batchId: string
      totalCount: number
      cards: BusinessCard[]
    }>('/business-cards/upload', formData)
  },

  // 获取识别结果
  getCardResult(id: string) {
    return http.get<BusinessCard>(`/business-cards/${id}`)
  },

  // 查询批次识别状态
  getBatchStatus(batchId: string) {
    return http.get<{
      batchId: string
      totalCount: number
      pendingCount: number
      processingCount: number
      successCount: number
      failedCount: number
      cards: BusinessCard[]
    }>(`/business-cards/batch/${batchId}/status`)
  },

  // 确认识别结果转为客户
  confirmCard(id: string, data: Partial<Customer>) {
    return http.post<{ customerId: string }>(`/business-cards/${id}/confirm`, data)
  },

  // 批量确认转为客户
  batchConfirmCards(cardIds: string[]) {
    return http.post<{ successCount: number; failedCount: number }>('/business-cards/batch-confirm', { cardIds })
  },

  // ===================== Excel导入相关 =====================

  // 下载导入模板
  downloadTemplate() {
    return http.get('/customers/import/template', { responseType: 'blob' })
  },

  // 上传Excel文件
  uploadImportFile(formData: FormData) {
    return http.upload<ImportResult>('/customers/import/upload', formData)
  },

  // 确认字段映射并预检
  validateImport(importId: string, data: { fieldMapping: Record<string, string>; importMode?: string }) {
    return http.post<ImportValidation>(`/customers/import/${importId}/validate`, data)
  },

  // 执行导入
  executeImport(importId: string, data: { importMode?: string; duplicateAction?: string }) {
    return http.post<{ importId: string; status: string }>(`/customers/import/${importId}/execute`, data)
  },

  // 查询导入状态
  getImportStatus(importId: string) {
    return http.get<ImportStatus>(`/customers/import/${importId}/status`)
  },

  // ===================== 客户事件相关 =====================

  // 创建事件
  createEvent(data: CustomerEventRequest) {
    return http.post<CustomerEvent>('/customer-events', data)
  },

  // 更新事件
  updateEvent(id: string, data: CustomerEventRequest) {
    return http.put<CustomerEvent>(`/customer-events/${id}`, data)
  },

  // 删除事件
  deleteEvent(id: string) {
    return http.delete(`/customer-events/${id}`)
  },

  // 获取客户的所有事件
  getEventsByCustomerId(customerId: string) {
    return http.get<CustomerEvent[]>(`/customer-events/customer/${customerId}`)
  },

  // 获取事件详情
  getEventDetail(id: string) {
    return http.get<CustomerEvent>(`/customer-events/${id}`)
  }
}

