import { http } from '@/utils/request'
import type { PaginationData } from '@/utils/request'

export interface Product {
  id: string
  brandCode: string
  brandName: string
  xkNo: string
  oeNo: string
  imagePath?: string
  imageUrl?: string
  priceMin?: number
  priceMax?: number
  priceAvg?: number
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface ProductListParams {
  page?: number
  pageSize?: number
  keyword?: string
  brandCode?: string
  oeNo?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

export interface Brand {
  brandCode: string
  brandName: string
  productCount: number
}

export interface BrandSheetInfo {
  sheetName: string
  brandCode: string
  brandName: string
  productCount: number
  imageCount: number
}

export interface ImportResult {
  importId: string
  fileName: string
  brandSheets: BrandSheetInfo[]
  totalProducts: number
  totalImages: number
  previewData: Record<string, any>[]
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
  errorMessage?: string
}

export interface QuoteItem {
  productId: string
  xkNo: string
  oeNo: string
  imageUrl?: string
  brandCode?: string
  priceRmb?: number
  priceUsd?: number
  quantity: number
  profitRate?: number
  includeTax?: boolean
  isFob?: boolean
  recommendedPrice?: number
  finalPrice?: number
  subtotal?: number
  remark?: string
  locked?: boolean  // 是否锁定最终单价，锁定后不随参数变化而更新
}

export interface QuoteRequest {
  items: QuoteItem[]
  priceMode?: 'min' | 'avg' | 'max'
  exchangeRate?: number
  defaultProfitRate?: number
  taxRate?: number
  fobRate?: number
  includeTax?: boolean
  isFob?: boolean
}

export interface QuoteResult {
  items: QuoteItem[]
  totalCount: number
  totalAmount: number
  exchangeRate: number
  priceMode: string
}

// ===================== 报价导入相关 =====================

export interface QuoteImportItem {
  rowIndex: number
  oeNo: string
  customerPriceRaw?: string
  customerPriceUsd?: number
  matched: boolean
  productId?: string
  xkNo?: string
  brandCode?: string
  imageUrl?: string
  ourPriceMin?: number
  ourPriceAvg?: number
  ourPriceMax?: number
  ourPriceRmb?: number
  ourPriceUsd?: number
  quantity: number
  profitRate?: number
  includeTax?: boolean
  isFob?: boolean
  calculatedPrice?: number
  priceDiff?: number
  priceDiffPercent?: number
  remark?: string
}

export interface QuoteImportResult {
  totalRows: number
  matchedCount: number
  unmatchedCount: number
  customerTotalUsd: number
  ourTotalUsd: number
  totalDiffUsd: number
  exchangeRate: number
  priceMode: string
  items: QuoteImportItem[]
}

export interface QuoteImportRequest {
  items: QuoteImportItem[]
  priceMode?: 'min' | 'avg' | 'max'
  exchangeRate?: number
  defaultProfitRate?: number
  taxRate?: number
  fobRate?: number
  includeTax?: boolean
  isFob?: boolean
  customerCurrency?: 'USD' | 'RMB'
}

export const productApi = {
  // 获取产品列表
  getList(params: ProductListParams) {
    return http.get<PaginationData<Product>>('/products', { params })
  },

  // 获取产品详情
  getDetail(id: string) {
    return http.get<Product>(`/products/${id}`)
  },

  // 创建产品
  create(data: Partial<Product>) {
    return http.post<{ id: string }>('/products', data)
  },

  // 更新产品
  update(id: string, data: Partial<Product>) {
    return http.put(`/products/${id}`, data)
  },

  // 删除产品
  delete(id: string) {
    return http.delete(`/products/${id}`)
  },

  // 批量删除产品
  batchDelete(ids: string[]) {
    return http.delete('/products/batch', { data: { ids } })
  },

  // 获取品牌列表
  getBrands() {
    return http.get<Brand[]>('/products/brands')
  },

  // 按OE号搜索产品
  searchByOeNo(oe: string) {
    return http.get<Product[]>('/products/search', { params: { oe } })
  },

  // ===================== Excel导入相关 =====================

  // 下载产品导入模板
  downloadImportTemplate() {
    return http.get('/products/import/template', { responseType: 'blob' })
  },

  // 上传Excel文件
  uploadImportFile(formData: FormData) {
    return http.upload<ImportResult>('/products/import/upload', formData)
  },

  // 执行导入
  executeImport(importId: string) {
    return http.post<ImportStatus>(`/products/import/${importId}/execute`)
  },

  // 查询导入状态
  getImportStatus(importId: string) {
    return http.get<ImportStatus>(`/products/import/${importId}/status`)
  },

  // ===================== 报价相关 =====================

  // 计算报价
  calculateQuote(data: QuoteRequest) {
    return http.post<QuoteResult>('/products/quote/calculate', data)
  },

  // 导出报价单
  exportQuote(data: QuoteRequest) {
    return http.post('/products/quote/export', data, { responseType: 'blob' })
  },

  // 获取推荐利润率
  getProfitRate(quantity: number) {
    return http.get<{ quantity: number; profitRate: number }>('/products/quote/profit-rate', { params: { quantity } })
  },

  // ===================== 报价导入相关 =====================

  // 上传报价Excel并解析匹配
  parseQuoteImport(formData: FormData) {
    return http.upload<QuoteImportResult>('/products/quote/import/parse', formData)
  },

  // 重新计算报价导入结果
  recalculateQuoteImport(data: QuoteImportRequest) {
    return http.post<QuoteImportResult>('/products/quote/import/recalculate', data)
  },

  // 导出报价对比Excel
  exportQuoteCompare(data: QuoteImportRequest) {
    return http.post('/products/quote/import/export', data, { responseType: 'blob' })
  }
}

