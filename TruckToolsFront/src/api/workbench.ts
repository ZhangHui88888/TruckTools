import { http } from '@/utils/request'
import type { PaginationData } from '@/utils/request'

// 工作台统计数据
export interface WorkbenchStats {
  pendingCount: number          // 待处理事件数量
  waitingCount: number          // 等待反馈数量
  todayProcessedCount: number   // 今日处理数量
  overdueCount: number          // 超时未跟进客户数量
  weeklyNewCustomerCount: number // 本周新增客户数量
  activeCustomerCount: number   // 活跃客户数量
  totalCustomerCount: number    // 总客户数量
  stoppedFollowUpCount: number  // 已停止跟进客户数量
}

// 工作台事件
export interface WorkbenchEvent {
  id: string
  customerId: string
  customerName: string
  customerCompany?: string
  customerPriority: number
  customerRemark?: string
  customerEmail?: string
  customerPhone?: string
  eventTime: string
  eventLocation?: string
  eventContent: string
  eventStatus: 'pending_customer' | 'pending_us'
  isSystemGenerated: boolean
  waitingDays: number
  isOverdue: boolean
  attachmentUrls?: string[]
  createdAt: string
  updatedAt: string
}

// 事件查询参数
export interface WorkbenchEventQueryParams {
  page?: number
  pageSize?: number
  customerKeyword?: string
  eventStatus?: 'pending_customer' | 'pending_us' | 'all'
  startTime?: string
  endTime?: string
  overdueOnly?: boolean
  excludeStoppedFollowUp?: boolean
}

// 处理事件请求
export interface ProcessEventRequest {
  eventId: string
  processContent: string
  processTime?: string
  attachmentUrls?: string[]
}

// 停止跟进请求
export interface StopFollowUpRequest {
  customerId: string
  reason?: string
}

export const workbenchApi = {
  // 获取工作台统计数据
  getStats() {
    return http.get<WorkbenchStats>('/workbench/stats')
  },

  // 获取工作台事件列表
  getEventList(params: WorkbenchEventQueryParams) {
    return http.get<PaginationData<WorkbenchEvent>>('/workbench/events', { params })
  },

  // 处理事件
  processEvent(data: ProcessEventRequest) {
    return http.post<WorkbenchEvent>('/workbench/events/process', data)
  },

  // 停止跟进客户
  stopFollowUp(data: StopFollowUpRequest) {
    return http.post<void>('/workbench/customers/stop-follow-up', data)
  },

  // 恢复跟进客户
  resumeFollowUp(customerId: string) {
    return http.post<void>(`/workbench/customers/${customerId}/resume-follow-up`)
  },

  // 手动触发超时提醒检查（仅管理员）
  checkOverdue() {
    return http.post<number>('/workbench/check-overdue')
  },

  // 导出事件列表
  exportEvents(params: WorkbenchEventQueryParams) {
    return http.get('/workbench/events/export', { 
      params,
      responseType: 'blob'
    })
  }
}
