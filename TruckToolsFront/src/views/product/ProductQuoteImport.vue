<template>
  <div class="quote-import-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">报价导入</h1>
        <p class="page-subtitle">导入客户报价单Excel，自动匹配产品并对比价格</p>
      </div>
    </div>

    <div class="main-content">
      <!-- 上传区域 -->
      <div class="upload-section content-card" v-if="!importResult">
        <a-upload-dragger
          :file-list="fileList"
          :before-upload="beforeUpload"
          :custom-request="handleUpload"
          accept=".xlsx,.xls"
          :multiple="false"
        >
          <p class="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p class="ant-upload-text">点击或拖拽Excel文件到此区域上传</p>
          <p class="ant-upload-hint">
            支持 .xlsx 或 .xls 格式，Excel需包含 OE NO. 和 UNIT PRICE 列
          </p>
        </a-upload-dragger>

        <!-- 上传前设置 -->
        <div class="pre-upload-settings">
          <a-divider>导入参数设置</a-divider>
          <a-form layout="inline" :model="uploadSettings">
            <a-form-item label="客户报价币种">
              <a-radio-group v-model:value="uploadSettings.customerCurrency" button-style="solid">
                <a-radio-button value="USD">美元(USD)</a-radio-button>
                <a-radio-button value="RMB">人民币(RMB)</a-radio-button>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="价格模式">
              <a-radio-group v-model:value="uploadSettings.priceMode" button-style="solid">
                <a-radio-button value="min">最低价</a-radio-button>
                <a-radio-button value="avg">平均价</a-radio-button>
                <a-radio-button value="max">最高价</a-radio-button>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="汇率">
              <a-input-number
                v-model:value="uploadSettings.exchangeRate"
                :min="1"
                :max="20"
                :precision="2"
                style="width: 100px"
              />
              <a-tooltip title="获取最新汇率">
                <a-button type="text" size="small" :loading="exchangeRateLoading" @click="fetchExchangeRate">
                  <SyncOutlined :spin="exchangeRateLoading" />
                </a-button>
              </a-tooltip>
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 结果区域 -->
      <template v-if="importResult">
        <!-- 统计卡片 -->
        <div class="stats-section">
          <a-row :gutter="16">
            <a-col :span="4">
              <div class="stat-card">
                <div class="stat-label">总行数</div>
                <div class="stat-value">{{ importResult.totalRows }}</div>
              </div>
            </a-col>
            <a-col :span="4">
              <div class="stat-card success">
                <div class="stat-label">匹配成功</div>
                <div class="stat-value">{{ importResult.matchedCount }}</div>
              </div>
            </a-col>
            <a-col :span="4">
              <div class="stat-card warning">
                <div class="stat-label">未匹配</div>
                <div class="stat-value">{{ importResult.unmatchedCount }}</div>
              </div>
            </a-col>
            <a-col :span="4">
              <div class="stat-card">
                <div class="stat-label">客户报价合计</div>
                <div class="stat-value">${{ importResult.customerTotalUsd?.toFixed(2) }}</div>
              </div>
            </a-col>
            <a-col :span="4">
              <div class="stat-card">
                <div class="stat-label">我方报价合计</div>
                <div class="stat-value">${{ importResult.ourTotalUsd?.toFixed(2) }}</div>
              </div>
            </a-col>
            <a-col :span="4">
              <div class="stat-card" :class="{ success: (importResult.totalDiffUsd || 0) >= 0, danger: (importResult.totalDiffUsd || 0) < 0 }">
                <div class="stat-label">差异</div>
                <div class="stat-value">${{ importResult.totalDiffUsd?.toFixed(2) }}</div>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 设置区 -->
        <div class="settings-section content-card">
          <div class="settings-row">
            <div class="setting-item">
              <span class="setting-label">价格模式</span>
              <a-radio-group v-model:value="quoteSettings.priceMode" button-style="solid" @change="handleRecalculate">
                <a-radio-button value="min">最低价</a-radio-button>
                <a-radio-button value="avg">平均价</a-radio-button>
                <a-radio-button value="max">最高价</a-radio-button>
              </a-radio-group>
            </div>

            <div class="setting-item">
              <span class="setting-label">汇率</span>
              <a-input-number
                v-model:value="quoteSettings.exchangeRate"
                :min="1"
                :max="20"
                :precision="2"
                style="width: 100px"
                @change="handleRecalculate"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">利润率</span>
              <a-slider
                v-model:value="quoteSettings.defaultProfitRate"
                :min="0"
                :max="50"
                :tipFormatter="(val: number) => `${val}%`"
                style="width: 120px"
                @afterChange="handleGlobalProfitRateChange"
              />
              <a-input-number
                v-model:value="quoteSettings.defaultProfitRate"
                :min="0"
                :max="50"
                :formatter="(value: number) => `${value}%`"
                :parser="(value: string) => Number(value.replace('%', ''))"
                style="width: 70px"
                @change="handleGlobalProfitRateChange"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">含税(10%)</span>
              <a-switch
                v-model:checked="quoteSettings.includeTax"
                :disabled="quoteSettings.isFob"
                @change="handleRecalculate"
              />
            </div>

            <div class="setting-item">
              <span class="setting-label">FOB(15%)</span>
              <a-switch
                v-model:checked="quoteSettings.isFob"
                @change="handleFobChange"
              />
            </div>

            <div class="setting-actions">
              <a-button @click="handleReset">
                <ReloadOutlined />
                重新上传
              </a-button>
              <a-button type="primary" @click="handleExport" :loading="exporting">
                <DownloadOutlined />
                导出报价单（简洁）
              </a-button>
              <a-button @click="handleExportCompare" :loading="exportingCompare">
                <DownloadOutlined />
                导出对比分析
              </a-button>
            </div>
          </div>
        </div>

        <!-- 报价对比表格 -->
        <div class="table-section content-card">
          <a-table
            :data-source="importResult.items"
            :columns="columns"
            :loading="loading"
            :pagination="{ pageSize: 50, showSizeChanger: true, showQuickJumper: true, showTotal: (total: number) => `共 ${total} 条` }"
            row-key="rowIndex"
            :scroll="{ x: 1400 }"
            :row-class-name="getRowClassName"
          >
            <template #bodyCell="{ column, record, index }">
              <!-- 序号 -->
              <template v-if="column.key === 'index'">
                {{ index + 1 }}
              </template>

              <!-- 图片 -->
              <template v-else-if="column.key === 'image'">
                <div class="product-image" @click="showImagePreview(record)">
                  <img v-if="record.imageUrl" :src="record.imageUrl" alt="产品图片" />
                  <div v-else class="no-image">
                    <PictureOutlined />
                  </div>
                </div>
              </template>

              <!-- OE NO. -->
              <template v-else-if="column.key === 'oeNo'">
                <div class="oe-cell">
                  <span>{{ record.oeNo }}</span>
                  <a-tag v-if="!record.matched" color="warning" size="small">未匹配</a-tag>
                </div>
              </template>

              <!-- 客户报价 -->
              <template v-else-if="column.key === 'customerPrice'">
                <span class="price-cell">${{ record.customerPriceUsd?.toFixed(2) || '-' }}</span>
              </template>

              <!-- 我方成本价(RMB) -->
              <template v-else-if="column.key === 'ourPriceRmb'">
                <span class="price-cell">¥{{ record.ourPriceRmb?.toFixed(2) || '-' }}</span>
              </template>

              <!-- 我方报价(USD) -->
              <template v-else-if="column.key === 'calculatedPrice'">
                <span class="price-cell">${{ record.calculatedPrice?.toFixed(2) || '-' }}</span>
              </template>

              <!-- 数量 -->
              <template v-else-if="column.key === 'quantity'">
                <a-input-number
                  v-model:value="record.quantity"
                  :min="1"
                  size="small"
                  style="width: 70px"
                  @change="() => handleItemChange(record)"
                />
              </template>

              <!-- 利润率 -->
              <template v-else-if="column.key === 'profitRate'">
                <a-input-number
                  v-model:value="record.profitRate"
                  :min="0"
                  :max="100"
                  size="small"
                  style="width: 70px"
                  :formatter="(value: number) => `${value}%`"
                  :parser="(value: string) => Number(value.replace('%', ''))"
                  @change="() => handleItemChange(record)"
                />
              </template>

              <!-- 含税/FOB -->
              <template v-else-if="column.key === 'taxFob'">
                <a-space direction="vertical" size="small">
                  <a-checkbox
                    v-model:checked="record.includeTax"
                    :disabled="record.isFob"
                    size="small"
                    @change="() => handleItemChange(record)"
                  >税</a-checkbox>
                  <a-checkbox
                    v-model:checked="record.isFob"
                    size="small"
                    @change="() => handleItemFobChange(record)"
                  >FOB</a-checkbox>
                </a-space>
              </template>

              <!-- 差异 -->
              <template v-else-if="column.key === 'priceDiff'">
                <span 
                  class="price-cell" 
                  :class="{ 'positive': (record.priceDiff || 0) >= 0, 'negative': (record.priceDiff || 0) < 0 }"
                >
                  {{ record.priceDiff != null ? `$${record.priceDiff.toFixed(2)}` : '-' }}
                </span>
              </template>

              <!-- 差异百分比 -->
              <template v-else-if="column.key === 'priceDiffPercent'">
                <span 
                  class="price-cell" 
                  :class="{ 'positive': (record.priceDiffPercent || 0) >= 0, 'negative': (record.priceDiffPercent || 0) < 0 }"
                >
                  {{ record.priceDiffPercent != null ? `${record.priceDiffPercent.toFixed(1)}%` : '-' }}
                </span>
              </template>
            </template>
          </a-table>
        </div>
      </template>
    </div>

    <!-- 图片预览 -->
    <a-modal v-model:open="imagePreviewVisible" :footer="null" :width="600" title="产品图片">
      <img :src="previewImage" alt="产品图片" style="width: 100%" />
    </a-modal>

    <!-- 上传进度 -->
    <a-modal v-model:open="uploadModalVisible" :footer="null" :closable="false" :maskClosable="false" title="正在解析...">
      <div class="upload-progress">
        <a-spin size="large" />
        <p>正在解析Excel并匹配产品，请稍候...</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  InboxOutlined,
  PictureOutlined,
  SyncOutlined,
  ReloadOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { productApi } from '@/api/product'
import type { QuoteImportResult, QuoteImportItem, QuoteImportRequest } from '@/api/product'

const loading = ref(false)
const exporting = ref(false)
const exportingCompare = ref(false)
const exchangeRateLoading = ref(false)
const fileList = ref<any[]>([])
const importResult = ref<QuoteImportResult | null>(null)

// 上传前设置
const uploadSettings = reactive({
  customerCurrency: 'USD' as 'USD' | 'RMB',
  priceMode: 'avg' as 'min' | 'avg' | 'max',
  exchangeRate: 7.2,
  defaultProfitRate: 10,
  includeTax: false,
  isFob: false
})

// 报价设置
const quoteSettings = reactive({
  priceMode: 'avg' as 'min' | 'avg' | 'max',
  exchangeRate: 7.2,
  defaultProfitRate: 10,
  taxRate: 10,
  fobRate: 15,
  includeTax: false,
  isFob: false,
  customerCurrency: 'USD' as 'USD' | 'RMB'
})

// 上传弹窗
const uploadModalVisible = ref(false)

// 图片预览
const imagePreviewVisible = ref(false)
const previewImage = ref('')

// 表格列配置
const columns = [
  { title: '序号', key: 'index', width: 60, fixed: 'left' as const },
  { title: '图片', key: 'image', width: 80 },
  { title: 'XK NO.', dataIndex: 'xkNo', width: 110 },
  { title: 'OE NO.', key: 'oeNo', width: 140 },
  { title: '客户报价', key: 'customerPrice', width: 100 },
  { title: '成本价(¥)', key: 'ourPriceRmb', width: 100 },
  { title: '数量', key: 'quantity', width: 80 },
  { title: '利润率', key: 'profitRate', width: 90 },
  { title: '税/FOB', key: 'taxFob', width: 80 },
  { title: '我方报价', key: 'calculatedPrice', width: 100 },
  { title: '差异', key: 'priceDiff', width: 90 },
  { title: '差异%', key: 'priceDiffPercent', width: 80 },
  { title: '备注', dataIndex: 'remark', ellipsis: true }
]

// 获取实时汇率
const fetchExchangeRate = async () => {
  exchangeRateLoading.value = true
  try {
    const response = await fetch('https://api.exchangerate-api.com/v4/latest/USD')
    const data = await response.json()
    if (data && data.rates && data.rates.CNY) {
      const rate = Number(data.rates.CNY.toFixed(2))
      uploadSettings.exchangeRate = rate
      quoteSettings.exchangeRate = rate
      // 缓存到本地存储
      localStorage.setItem('exchangeRate', JSON.stringify({
        rate: rate,
        date: new Date().toDateString()
      }))
      message.success(`汇率已更新: 1 USD = ${rate} CNY`)
    }
  } catch (error) {
    console.error('获取汇率失败:', error)
    message.error('获取汇率失败，请手动输入')
  } finally {
    exchangeRateLoading.value = false
  }
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel' ||
                  file.name.endsWith('.xlsx') ||
                  file.name.endsWith('.xls')
  if (!isExcel) {
    message.error('请上传Excel文件(.xlsx或.xls)')
    return false
  }
  return true
}

// 处理上传
const handleUpload = async (options: any) => {
  const { file } = options
  
  uploadModalVisible.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('priceMode', uploadSettings.priceMode)
    formData.append('exchangeRate', uploadSettings.exchangeRate.toString())
    formData.append('defaultProfitRate', uploadSettings.defaultProfitRate.toString())
    formData.append('includeTax', uploadSettings.includeTax.toString())
    formData.append('isFob', uploadSettings.isFob.toString())
    formData.append('customerCurrency', uploadSettings.customerCurrency)
    
    const res = await productApi.parseQuoteImport(formData)
    
    if (res && res.data) {
      importResult.value = res.data
      
      // 同步设置
      quoteSettings.priceMode = uploadSettings.priceMode
      quoteSettings.exchangeRate = uploadSettings.exchangeRate
      quoteSettings.defaultProfitRate = uploadSettings.defaultProfitRate
      quoteSettings.includeTax = uploadSettings.includeTax
      quoteSettings.isFob = uploadSettings.isFob
      quoteSettings.customerCurrency = uploadSettings.customerCurrency
      
      message.success(`解析完成：匹配 ${res.data.matchedCount} 个产品，未匹配 ${res.data.unmatchedCount} 个`)
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    message.error(error.message || '上传解析失败')
  } finally {
    uploadModalVisible.value = false
    fileList.value = []
  }
}

// 获取行样式
const getRowClassName = (record: QuoteImportItem) => {
  if (!record.matched) {
    return 'unmatched-row'
  }
  return ''
}

// 显示图片预览
const showImagePreview = (record: QuoteImportItem) => {
  if (record.imageUrl) {
    previewImage.value = record.imageUrl
    imagePreviewVisible.value = true
  }
}

// 处理全局利润率变化，同步到每行
const handleGlobalProfitRateChange = () => {
  if (importResult.value && importResult.value.items) {
    importResult.value.items.forEach(item => {
      item.profitRate = quoteSettings.defaultProfitRate
    })
  }
  handleRecalculate()
}

// 处理FOB开关
const handleFobChange = () => {
  if (quoteSettings.isFob) {
    quoteSettings.includeTax = false
  }
  handleRecalculate()
}

// 处理单项FOB变化
const handleItemFobChange = (item: QuoteImportItem) => {
  if (item.isFob) {
    item.includeTax = false
  }
  handleItemChange(item)
}

// 处理单项变化
const handleItemChange = (item: QuoteImportItem) => {
  // 延迟执行重新计算，避免频繁请求
  handleRecalculate()
}

// 重新计算
const handleRecalculate = async () => {
  if (!importResult.value || !importResult.value.items) return
  
  loading.value = true
  try {
    const request: QuoteImportRequest = {
      items: importResult.value.items,
      priceMode: quoteSettings.priceMode,
      exchangeRate: quoteSettings.exchangeRate,
      defaultProfitRate: quoteSettings.defaultProfitRate,
      taxRate: quoteSettings.taxRate,
      fobRate: quoteSettings.fobRate,
      includeTax: quoteSettings.includeTax,
      isFob: quoteSettings.isFob,
      customerCurrency: quoteSettings.customerCurrency
    }
    
    const res = await productApi.recalculateQuoteImport(request)
    if (res && res.data) {
      importResult.value = res.data
    }
  } catch (error: any) {
    console.error('重新计算失败:', error)
    message.error('重新计算失败')
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  importResult.value = null
  fileList.value = []
}

// 导出
// 导出简洁格式报价单
const handleExport = async () => {
  if (!importResult.value || !importResult.value.items) {
    message.warning('没有可导出的数据')
    return
  }
  
  exporting.value = true
  try {
    // 只导出已匹配的产品，转换为简洁格式
    const matchedItems = importResult.value.items.filter(item => item.matched)
    
    if (matchedItems.length === 0) {
      message.warning('没有已匹配的产品可导出')
      exporting.value = false
      return
    }
    
    // 转换为 QuoteItem 格式
    const quoteItems = matchedItems.map(item => ({
      productId: item.productId || '',
      xkNo: item.xkNo || '',
      oeNo: item.oeNo,
      imageUrl: item.imageUrl,
      brandCode: item.brandCode,
      priceRmb: item.ourPriceRmb,
      quantity: item.quantity || 1,
      profitRate: item.profitRate,
      includeTax: item.includeTax,
      isFob: item.isFob,
      finalPrice: item.calculatedPrice,
      remark: item.remark
    }))
    
    const request = {
      items: quoteItems,
      priceMode: quoteSettings.priceMode,
      exchangeRate: quoteSettings.exchangeRate,
      defaultProfitRate: quoteSettings.defaultProfitRate,
      taxRate: quoteSettings.taxRate,
      fobRate: quoteSettings.fobRate,
      includeTax: quoteSettings.includeTax,
      isFob: quoteSettings.isFob
    }
    
    // 使用简洁格式导出
    const res = await productApi.exportQuote(request) as any
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报价单_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    message.success(`成功导出 ${matchedItems.length} 个产品`)
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 导出对比分析格式
const handleExportCompare = async () => {
  if (!importResult.value || !importResult.value.items) {
    message.warning('没有可导出的数据')
    return
  }
  
  exportingCompare.value = true
  try {
    const request: QuoteImportRequest = {
      items: importResult.value.items,
      priceMode: quoteSettings.priceMode,
      exchangeRate: quoteSettings.exchangeRate,
      defaultProfitRate: quoteSettings.defaultProfitRate,
      taxRate: quoteSettings.taxRate,
      fobRate: quoteSettings.fobRate,
      includeTax: quoteSettings.includeTax,
      isFob: quoteSettings.isFob,
      customerCurrency: quoteSettings.customerCurrency
    }
    
    const res = await productApi.exportQuoteCompare(request) as any
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报价对比_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exportingCompare.value = false
  }
}

// 初始化时自动获取实时汇率
onMounted(() => {
  // 尝试从缓存获取汇率
  const cached = localStorage.getItem('exchangeRate')
  if (cached) {
    try {
      const { rate, date } = JSON.parse(cached)
      // 如果是今天的汇率，使用缓存
      if (date === new Date().toDateString()) {
        uploadSettings.exchangeRate = rate
        quoteSettings.exchangeRate = rate
        return
      }
    } catch {
      // 忽略缓存解析错误
    }
  }
  // 自动获取最新汇率
  fetchExchangeRate()
})
</script>

<style lang="less" scoped>
.quote-import-page {
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

  .upload-section {
    padding: 24px;
    
    :deep(.ant-upload-drag) {
      padding: 40px;
      background: #fafafa;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      
      &:hover {
        border-color: #1677ff;
      }
    }
    
    .pre-upload-settings {
      margin-top: 24px;
      
      :deep(.ant-form-inline) {
        gap: 24px;
      }
    }
  }

  .stats-section {
    margin-bottom: 16px;
    
    .stat-card {
      background: white;
      border-radius: 8px;
      padding: 16px;
      text-align: center;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      
      .stat-label {
        font-size: 12px;
        color: #6b7280;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: #1f2937;
        font-family: 'Roboto Mono', monospace;
      }
      
      &.success {
        border-left: 4px solid #52c41a;
        .stat-value { color: #52c41a; }
      }
      
      &.warning {
        border-left: 4px solid #faad14;
        .stat-value { color: #faad14; }
      }
      
      &.danger {
        border-left: 4px solid #ff4d4f;
        .stat-value { color: #ff4d4f; }
      }
    }
  }

  .settings-section {
    padding: 16px 20px;
    margin-bottom: 16px;

    .settings-row {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      align-items: center;
    }

    .setting-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .setting-label {
        color: #6b7280;
        font-size: 14px;
        white-space: nowrap;
      }
    }
    
    .setting-actions {
      margin-left: auto;
      display: flex;
      gap: 12px;
    }
  }

  .table-section {
    :deep(.ant-table-tbody > tr > td) {
      padding: 12px 8px;
    }

    :deep(.unmatched-row) {
      background: #fafafa;
      
      td {
        color: #9ca3af;
      }
    }

    .product-image {
      width: 60px;
      height: 50px;
      border-radius: 4px;
      overflow: hidden;
      cursor: pointer;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }

      .no-image {
        color: #d9d9d9;
        font-size: 20px;
      }
    }

    .oe-cell {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .price-cell {
      font-family: 'Roboto Mono', monospace;
      font-weight: 500;

      &.positive {
        color: #52c41a;
      }

      &.negative {
        color: #ff4d4f;
      }
    }
  }

  .upload-progress {
    text-align: center;
    padding: 40px 20px;
    
    p {
      margin-top: 16px;
      color: #6b7280;
    }
  }
}
</style>

