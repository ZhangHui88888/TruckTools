<template>
  <div class="product-list-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">产品目录</h1>
        <p class="page-subtitle">管理您的产品信息和报价</p>
      </div>
      <div class="header-actions">
        <div class="exchange-rate-setting">
          <span class="setting-label">汇率</span>
          <a-tooltip :title="exchangeRateUpdatedAt ? `更新时间: ${exchangeRateUpdatedAt}` : '点击刷新获取最新汇率'">
            <a-input-number
              v-model:value="exchangeRate"
              :min="1"
              :max="20"
              :precision="2"
              :step="0.1"
              style="width: 80px"
            />
          </a-tooltip>
          <a-tooltip title="获取最新汇率">
            <a-button 
              type="text" 
              size="small" 
              :loading="exchangeRateLoading"
              @click="fetchExchangeRate"
            >
              <SyncOutlined :spin="exchangeRateLoading" />
            </a-button>
          </a-tooltip>
        </div>
        <a-radio-group v-model:value="priceMode" button-style="solid" @change="handlePriceModeChange">
          <a-radio-button value="min">最低价</a-radio-button>
          <a-radio-button value="avg">平均价</a-radio-button>
          <a-radio-button value="max">最高价</a-radio-button>
        </a-radio-group>
        <a-button @click="$router.push('/product/import')">
          <UploadOutlined />
          导入产品
        </a-button>
        <a-button type="primary" @click="$router.push('/product/quote-import')">
          <FileExcelOutlined />
          报价导入
        </a-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧品牌导航 -->
      <div class="brand-sidebar content-card">
        <div class="sidebar-header">
          <span class="sidebar-title">品牌分类</span>
        </div>
        <div class="brand-list">
          <div 
            class="brand-item" 
            :class="{ active: !selectedBrand }"
            @click="selectBrand(null)"
          >
            <span class="brand-name">全部品牌</span>
            <span class="brand-count">{{ totalProductCount }}</span>
          </div>
          <div 
            v-for="brand in brands" 
            :key="brand.brandCode"
            class="brand-item"
            :class="{ active: selectedBrand === brand.brandCode }"
            @click="selectBrand(brand.brandCode)"
          >
            <span class="brand-name">{{ brand.brandName || brand.brandCode }}</span>
            <span class="brand-count">{{ brand.productCount }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧产品表格 -->
      <div class="product-content">
        <!-- 搜索栏 -->
        <div class="search-bar content-card">
          <a-input-search
            v-model:value="filterState.keyword"
            placeholder="搜索OE号或XK号"
            style="width: 300px"
            @search="handleSearch"
            allow-clear
          />
        </div>

        <!-- 表格 -->
        <div class="table-container content-card">
          <a-table
            :data-source="products"
            :columns="columns"
            :loading="loading"
            :pagination="pagination"
            :row-selection="rowSelection"
            row-key="id"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <!-- 图片 -->
              <template v-if="column.key === 'image'">
                <div class="product-image" @click="showImagePreview(record)">
                  <img v-if="record.imageUrl" :src="record.imageUrl" alt="产品图片" />
                  <div v-else class="no-image">
                    <PictureOutlined />
                  </div>
                </div>
              </template>
              
              <!-- OE编号 -->
              <template v-else-if="column.key === 'oeNo'">
                <div class="oe-cell">
                  <span class="oe-text">{{ record.oeNo }}</span>
                  <a-tooltip title="复制">
                    <CopyOutlined class="copy-icon" @click="copyToClipboard(record.oeNo)" />
                  </a-tooltip>
                </div>
              </template>
              
              <!-- 出厂价(RMB) -->
              <template v-else-if="column.key === 'priceRmb'">
                <span class="price-cell">{{ formatPrice(getDisplayPrice(record), '¥') }}</span>
              </template>
              
              <!-- 出厂价(USD) -->
              <template v-else-if="column.key === 'priceUsd'">
                <span class="price-cell">{{ formatPrice(getDisplayPrice(record) / exchangeRate, '$') }}</span>
              </template>
              
              <!-- 操作 -->
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-tooltip title="添加到报价单">
                    <a-button type="link" size="small" @click="addToQuote(record)">
                      <ShoppingCartOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="编辑">
                    <a-button type="link" size="small" @click="handleEdit(record)">
                      <EditOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-popconfirm
                    title="确定删除此产品吗？"
                    @confirm="handleDelete(record.id)"
                  >
                    <a-tooltip title="删除">
                      <a-button type="link" size="small" danger>
                        <DeleteOutlined />
                      </a-button>
                    </a-tooltip>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>

          <!-- 批量操作 -->
          <div v-if="selectedRowKeys.length > 0" class="batch-actions">
            <span class="selected-info">已选择 {{ selectedRowKeys.length }} 项</span>
            <a-button type="primary" @click="batchAddToQuote">
              <ShoppingCartOutlined />
              批量添加到报价单
            </a-button>
            <a-popconfirm
              title="确定删除选中的产品吗？"
              @confirm="handleBatchDelete"
            >
              <a-button danger>
                <DeleteOutlined />
                批量删除
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <a-modal v-model:open="imagePreviewVisible" :footer="null" width="auto" title="产品图片" centered>
      <div class="image-preview-container">
        <img :src="previewImage" alt="产品图片" />
      </div>
    </a-modal>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑产品"
      @ok="handleEditSubmit"
      :confirmLoading="editLoading"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="品牌缩写">
          <a-input v-model:value="editForm.brandCode" />
        </a-form-item>
        <a-form-item label="品牌全称">
          <a-input v-model:value="editForm.brandName" />
        </a-form-item>
        <a-form-item label="XK编号">
          <a-input v-model:value="editForm.xkNo" />
        </a-form-item>
        <a-form-item label="OE编号">
          <a-input v-model:value="editForm.oeNo" />
        </a-form-item>
        <a-form-item label="最低价(RMB)">
          <a-input-number v-model:value="editForm.priceMin" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="最高价(RMB)">
          <a-input-number v-model:value="editForm.priceMax" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="平均价(RMB)">
          <a-input-number v-model:value="editForm.priceAvg" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="editForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'
import {
  UploadOutlined,
  PictureOutlined,
  CopyOutlined,
  ShoppingCartOutlined,
  EditOutlined,
  DeleteOutlined,
  SyncOutlined,
  FileExcelOutlined
} from '@ant-design/icons-vue'
import { productApi } from '@/api/product'
import type { Product, Brand, ProductListParams } from '@/api/product'

const router = useRouter()

const loading = ref(false)
const products = ref<Product[]>([])
const brands = ref<Brand[]>([])
const selectedBrand = ref<string | null>(null)
const priceMode = ref<'min' | 'avg' | 'max'>('avg')
const exchangeRate = ref(7.2)
const exchangeRateLoading = ref(false)
const exchangeRateUpdatedAt = ref<string>('')
const selectedRowKeys = ref<string[]>([])

// 获取实时汇率
const fetchExchangeRate = async () => {
  exchangeRateLoading.value = true
  try {
    // 使用免费的汇率API
    const response = await fetch('https://api.exchangerate-api.com/v4/latest/USD')
    const data = await response.json()
    if (data && data.rates && data.rates.CNY) {
      exchangeRate.value = Number(data.rates.CNY.toFixed(2))
      exchangeRateUpdatedAt.value = new Date().toLocaleString()
      // 缓存到本地存储
      localStorage.setItem('exchangeRate', JSON.stringify({
        rate: exchangeRate.value,
        updatedAt: exchangeRateUpdatedAt.value,
        date: new Date().toDateString()
      }))
      message.success(`汇率已更新: 1 USD = ${exchangeRate.value} CNY`)
    }
  } catch (error) {
    console.error('获取汇率失败:', error)
    message.error('获取汇率失败，请手动输入')
  } finally {
    exchangeRateLoading.value = false
  }
}

// 初始化汇率（从缓存或自动获取）
const initExchangeRate = () => {
  const cached = localStorage.getItem('exchangeRate')
  if (cached) {
    try {
      const { rate, updatedAt, date } = JSON.parse(cached)
      exchangeRate.value = rate
      exchangeRateUpdatedAt.value = updatedAt
      // 如果不是今天的汇率，自动更新
      if (date !== new Date().toDateString()) {
        fetchExchangeRate()
      }
    } catch {
      fetchExchangeRate()
    }
  } else {
    fetchExchangeRate()
  }
}

// 图片预览
const imagePreviewVisible = ref(false)
const previewImage = ref('')

// 编辑弹窗
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive<Partial<Product>>({})
const currentEditId = ref<string>('')

// 筛选条件
const filterState = reactive<ProductListParams>({
  page: 1,
  pageSize: 20,
  keyword: '',
  brandCode: undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '图片', key: 'image', width: 80 },
  { title: 'XK NO.', key: 'xkNo', dataIndex: 'xkNo', width: 120 },
  { title: 'OE NO.', key: 'oeNo', dataIndex: 'oeNo', width: 150 },
  { title: '品牌', key: 'brandCode', dataIndex: 'brandCode', width: 80 },
  { title: '出厂价(RMB)', key: 'priceRmb', width: 120, sorter: true },
  { title: '出厂价(USD)', key: 'priceUsd', width: 120 },
  { title: '备注', key: 'remark', dataIndex: 'remark', ellipsis: true },
  { title: '操作', key: 'action', width: 130, fixed: 'right' as const }
]

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}))

// 计算总产品数
const totalProductCount = computed(() => {
  return brands.value.reduce((sum, b) => sum + b.productCount, 0)
})

// 获取显示价格
const getDisplayPrice = (product: Product) => {
  switch (priceMode.value) {
    case 'min': return product.priceMin || 0
    case 'max': return product.priceMax || 0
    default: return product.priceAvg || 0
  }
}

// 格式化价格
const formatPrice = (price: number, symbol: string) => {
  if (!price) return '-'
  return `${symbol}${price.toFixed(2)}`
}

// 获取品牌列表
const fetchBrands = async () => {
  try {
    const res = await productApi.getBrands()
    if (res && res.data) {
      brands.value = res.data
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error)
  }
}

// 获取产品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    filterState.brandCode = selectedBrand.value || undefined
    const res = await productApi.getList(filterState)
    if (res && res.data) {
      products.value = res.data.list || []
      pagination.total = res.data.pagination?.total || 0
      pagination.current = res.data.pagination?.page || 1
    }
  } catch (error) {
    console.error('获取产品列表失败:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

// 选择品牌
const selectBrand = (brandCode: string | null) => {
  selectedBrand.value = brandCode
  filterState.page = 1
  fetchProducts()
}

// 价格模式切换
const handlePriceModeChange = () => {
  // 价格模式切换不需要重新请求数据，只需要重新渲染
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag, _filters, sorter: any) => {
  filterState.page = pag.current
  filterState.pageSize = pag.pageSize
  if (sorter.field) {
    filterState.sortField = sorter.field
    filterState.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }
  fetchProducts()
}

// 搜索
const handleSearch = () => {
  filterState.page = 1
  fetchProducts()
}

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
  message.success('已复制到剪贴板')
}

// 显示图片预览
const showImagePreview = (record: Product) => {
  if (record.imageUrl) {
    previewImage.value = record.imageUrl
    imagePreviewVisible.value = true
  }
}

// 添加到报价单
const addToQuote = (record: Product) => {
  // 存储到 sessionStorage
  const quoteItems = JSON.parse(sessionStorage.getItem('quoteItems') || '[]')
  const existingIndex = quoteItems.findIndex((item: any) => item.productId === record.id)
  
  if (existingIndex >= 0) {
    message.warning('该产品已在报价单中')
    return
  }

  quoteItems.push({
    productId: record.id,
    xkNo: record.xkNo,
    oeNo: record.oeNo,
    imageUrl: record.imageUrl,
    brandCode: record.brandCode,
    priceRmb: getDisplayPrice(record),
    quantity: 1
  })
  
  sessionStorage.setItem('quoteItems', JSON.stringify(quoteItems))
  message.success('已添加到报价单')
}

// 批量添加到报价单
const batchAddToQuote = () => {
  const quoteItems = JSON.parse(sessionStorage.getItem('quoteItems') || '[]')
  let addedCount = 0

  selectedRowKeys.value.forEach(id => {
    const product = products.value.find(p => p.id === id)
    if (product) {
      const existingIndex = quoteItems.findIndex((item: any) => item.productId === id)
      if (existingIndex < 0) {
        quoteItems.push({
          productId: product.id,
          xkNo: product.xkNo,
          oeNo: product.oeNo,
          imageUrl: product.imageUrl,
          brandCode: product.brandCode,
          priceRmb: getDisplayPrice(product),
          quantity: 1
        })
        addedCount++
      }
    }
  })

  sessionStorage.setItem('quoteItems', JSON.stringify(quoteItems))
  message.success(`已添加 ${addedCount} 个产品到报价单`)
  selectedRowKeys.value = []
}

// 编辑
const handleEdit = (record: Product) => {
  currentEditId.value = record.id
  Object.assign(editForm, {
    brandCode: record.brandCode,
    brandName: record.brandName,
    xkNo: record.xkNo,
    oeNo: record.oeNo,
    priceMin: record.priceMin,
    priceMax: record.priceMax,
    priceAvg: record.priceAvg,
    remark: record.remark
  })
  editModalVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  editLoading.value = true
  try {
    await productApi.update(currentEditId.value, editForm)
    message.success('更新成功')
    editModalVisible.value = false
    fetchProducts()
    fetchBrands()
  } catch {
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
}

// 删除
const handleDelete = async (id: string) => {
  try {
    await productApi.delete(id)
    message.success('删除成功')
    fetchProducts()
    fetchBrands()
  } catch {
    message.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await productApi.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    fetchProducts()
    fetchBrands()
  } catch {
    message.error('批量删除失败')
  }
}

onMounted(() => {
  initExchangeRate()
  fetchBrands()
  fetchProducts()
})
</script>

<style lang="less" scoped>
.product-list-page {
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
    
    .header-actions {
      display: flex;
      gap: 16px;
      align-items: center;

      .exchange-rate-setting {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 12px;
        background: #f5f5f5;
        border-radius: 6px;
        
        .setting-label {
          color: #6b7280;
          font-size: 14px;
        }
      }
    }
  }

  .main-content {
    display: flex;
    gap: 24px;
  }

  .brand-sidebar {
    width: 220px;
    flex-shrink: 0;

    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      
      .sidebar-title {
        font-weight: 600;
        color: #1f2937;
      }
    }

    .brand-list {
      .brand-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        cursor: pointer;
        transition: all 0.2s;
        border-left: 3px solid transparent;

        &:hover {
          background: #f5f5f5;
        }

        &.active {
          background: #e6f7ff;
          border-left-color: #1677ff;
          
          .brand-name {
            color: #1677ff;
            font-weight: 500;
          }
        }

        .brand-name {
          color: #374151;
        }

        .brand-count {
          color: #9ca3af;
          font-size: 12px;
          background: #f3f4f6;
          padding: 2px 8px;
          border-radius: 10px;
        }
      }
    }
  }

  .product-content {
    flex: 1;
    min-width: 0;
  }

  .search-bar {
    margin-bottom: 16px;
    padding: 16px;
  }

  .table-container {
    position: relative;

    // 增加表格行高以容纳图片
    :deep(.product-row) {
      td {
        padding: 12px 8px !important;
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
      gap: 8px;

      .copy-icon {
        color: #1677ff;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s;
      }

      &:hover .copy-icon {
        opacity: 1;
      }
    }

    .price-cell {
      font-family: 'Roboto Mono', monospace;
      font-weight: 500;
    }

    .batch-actions {
      position: sticky;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 16px 24px;
      background: #f0f5ff;
      border-top: 1px solid #d6e4ff;
      display: flex;
      align-items: center;
      gap: 16px;
      margin: -24px;
      margin-top: 16px;
      
      .selected-info {
        color: #1677ff;
        font-weight: 500;
      }
    }
  }
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 300px;
  max-width: 90vw;
  max-height: 80vh;
  
  img {
    max-width: 100%;
    max-height: 80vh;
    object-fit: contain;
  }
}
</style>

