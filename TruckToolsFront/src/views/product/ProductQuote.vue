<template>
  <div class="product-quote-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">搜索报价</h1>
        <p class="page-subtitle">按OE号搜索产品并生成报价单</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="$router.push('/product/quote-import')">
          <FileExcelOutlined />
          报价导入
        </a-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 搜索区 -->
      <div class="search-section content-card">
        <a-form layout="inline">
          <a-form-item label="OE号搜索">
            <a-textarea
              v-model:value="searchOeNos"
              placeholder="输入OE号，多个用逗号或换行分隔"
              :rows="2"
              style="width: 400px"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch" :loading="searching">
              <SearchOutlined />
              搜索
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 全局设置区 -->
      <div class="settings-section content-card">
        <div class="settings-row">
          <div class="setting-item">
            <span class="setting-label">价格模式</span>
            <a-radio-group v-model:value="quoteSettings.priceMode" button-style="solid" @change="recalculate">
              <a-radio-button value="min">最低价</a-radio-button>
              <a-radio-button value="avg">平均价</a-radio-button>
              <a-radio-button value="max">最高价</a-radio-button>
            </a-radio-group>
          </div>

          <div class="setting-item">
            <span class="setting-label">汇率(RMB/USD)</span>
            <a-input-number
              v-model:value="quoteSettings.exchangeRate"
              :min="1"
              :max="20"
              :precision="2"
              style="width: 100px"
              @change="recalculate"
            />
            <a-tooltip title="获取最新汇率">
              <a-button type="text" size="small" :loading="exchangeRateLoading" @click="fetchExchangeRate">
                <SyncOutlined :spin="exchangeRateLoading" />
              </a-button>
            </a-tooltip>
          </div>

          <div class="setting-item">
            <span class="setting-label">利润率</span>
            <a-slider
              v-model:value="quoteSettings.defaultProfitRate"
              :min="0"
              :max="50"
              :tipFormatter="(val: number) => `${val}%`"
              style="width: 150px"
              @change="recalculate"
            />
            <a-input-number
              v-model:value="quoteSettings.defaultProfitRate"
              :min="0"
              :max="50"
              :formatter="(value: number) => `${value}%`"
              :parser="(value: string) => Number(value.replace('%', ''))"
              style="width: 80px"
              @change="recalculate"
            />
          </div>

          <div class="setting-item">
            <span class="setting-label">含税(10%)</span>
            <a-switch
              v-model:checked="quoteSettings.includeTax"
              :disabled="quoteSettings.isFob"
              @change="recalculate"
            />
          </div>

          <div class="setting-item">
            <span class="setting-label">FOB(15%)</span>
            <a-switch
              v-model:checked="quoteSettings.isFob"
              @change="handleFobChange"
            />
          </div>
        </div>
      </div>

      <!-- 报价列表 -->
      <div class="quote-section content-card">
        <div class="section-header">
          <h3>报价列表</h3>
          <span class="item-count">{{ quoteItems.length }} 个产品</span>
        </div>

        <a-table
          :data-source="quoteItems"
          :columns="quoteColumns"
          :pagination="false"
          row-key="productId"
          :scroll="{ x: 1200 }"
        >
          <template #bodyCell="{ column, record, index }">
            <!-- 序号 -->
            <template v-if="column.key === 'index'">
              {{ index + 1 }}
            </template>

            <!-- 图片 -->
            <template v-else-if="column.key === 'image'">
              <div class="product-image">
                <img v-if="record.imageUrl" :src="record.imageUrl" alt="产品图片" />
                <div v-else class="no-image">
                  <PictureOutlined />
                </div>
              </div>
            </template>

            <!-- 出厂价(USD) -->
            <template v-else-if="column.key === 'priceUsd'">
              <span class="price-cell">${{ record.priceUsd?.toFixed(2) || '-' }}</span>
            </template>

            <!-- 数量 -->
            <template v-else-if="column.key === 'quantity'">
              <a-input-number
                v-model:value="record.quantity"
                :min="1"
                size="small"
                style="width: 70px"
                @change="(val) => handleQuantityChange(record, val)"
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
                @change="() => recalculateItem(record)"
              />
            </template>

            <!-- 含税/FOB -->
            <template v-else-if="column.key === 'taxFob'">
              <a-space direction="vertical" size="small">
                <a-checkbox
                  v-model:checked="record.includeTax"
                  :disabled="record.isFob"
                  size="small"
                  @change="() => recalculateItem(record)"
                >税</a-checkbox>
                <a-checkbox
                  v-model:checked="record.isFob"
                  size="small"
                  @change="() => handleItemFobChange(record)"
                >FOB</a-checkbox>
              </a-space>
            </template>

            <!-- 推荐单价 -->
            <template v-else-if="column.key === 'recommendedPrice'">
              <span class="price-cell">${{ record.recommendedPrice?.toFixed(2) || '-' }}</span>
            </template>

            <!-- 最终单价 -->
            <template v-else-if="column.key === 'finalPrice'">
              <a-space>
                <a-input-number
                  v-model:value="record.finalPrice"
                  :min="0"
                  :precision="2"
                  size="small"
                  style="width: 90px"
                  prefix="$"
                  @change="() => updateSubtotal(record)"
                />
                <a-tooltip :title="record.locked ? '点击解锁，自动更新价格' : '点击锁定，固定当前价格'">
                  <a-button
                    type="text"
                    size="small"
                    @click="() => toggleLock(record)"
                    :style="{ color: record.locked ? '#faad14' : '#d9d9d9' }"
                  >
                    <LockOutlined v-if="record.locked" />
                    <UnlockOutlined v-else />
                  </a-button>
                </a-tooltip>
              </a-space>
            </template>

            <!-- 小计 -->
            <template v-else-if="column.key === 'subtotal'">
              <span class="price-cell subtotal">${{ record.subtotal?.toFixed(2) || '-' }}</span>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <a-popconfirm
                title="确定移除此产品吗？"
                @confirm="removeItem(index)"
              >
                <a-button type="link" danger size="small">
                  <DeleteOutlined />
                </a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>

        <!-- 汇总区 -->
        <div class="quote-summary">
          <div class="summary-item">
            <span class="summary-label">产品总数</span>
            <span class="summary-value">{{ quoteItems.length }}</span>
          </div>
          <div class="summary-item total">
            <span class="summary-label">总金额(USD)</span>
            <span class="summary-value">${{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="summary-actions">
            <a-button @click="clearAll" :disabled="quoteItems.length === 0">清空</a-button>
            <a-button type="primary" @click="exportQuote" :loading="exporting" :disabled="quoteItems.length === 0">
              <DownloadOutlined />
              导出报价单
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  PictureOutlined,
  DeleteOutlined,
  DownloadOutlined,
  SyncOutlined,
  LockOutlined,
  UnlockOutlined,
  FileExcelOutlined
} from '@ant-design/icons-vue'
import { productApi } from '@/api/product'
import type { QuoteItem, QuoteRequest, Product } from '@/api/product'

const searchOeNos = ref('')
const searching = ref(false)
const exporting = ref(false)
const exchangeRateLoading = ref(false)
const quoteItems = ref<QuoteItem[]>([])

// 报价设置
const quoteSettings = reactive({
  priceMode: 'avg' as 'min' | 'avg' | 'max',
  exchangeRate: 7.2,
  defaultProfitRate: 10,
  taxRate: 10,
  fobRate: 15,
  includeTax: false,
  isFob: false
})

// 表格列配置
const quoteColumns = [
  { title: '序号', key: 'index', width: 60 },
  { title: '图片', key: 'image', width: 80 },
  { title: 'XK NO.', dataIndex: 'xkNo', width: 110 },
  { title: 'OE NO.', dataIndex: 'oeNo', width: 130 },
  { title: '出厂价(USD)', key: 'priceUsd', width: 100 },
  { title: '数量', key: 'quantity', width: 80 },
  { title: '利润率', key: 'profitRate', width: 90 },
  { title: '含税/FOB', key: 'taxFob', width: 80 },
  { title: '推荐单价', key: 'recommendedPrice', width: 100 },
  { title: '最终单价', key: 'finalPrice', width: 140 },
  { title: '小计', key: 'subtotal', width: 100 },
  { title: '操作', key: 'action', width: 60, fixed: 'right' as const }
]

// 计算总金额
const totalAmount = computed(() => {
  return quoteItems.value.reduce((sum, item) => sum + (item.subtotal || 0), 0)
})

// 搜索产品
const handleSearch = async () => {
  if (!searchOeNos.value.trim()) {
    message.warning('请输入OE号')
    return
  }

  searching.value = true
  try {
    const res = await productApi.searchByOeNo(searchOeNos.value)
    if (res && res.data && res.data.length > 0) {
      // 添加到报价列表
      res.data.forEach((product: Product) => {
        const existingIndex = quoteItems.value.findIndex(item => item.productId === product.id)
        if (existingIndex < 0) {
          const priceRmb = getProductPrice(product)
          const profitRate = getRecommendedProfitRate(1)
          
          const newItem: QuoteItem = {
            productId: product.id,
            xkNo: product.xkNo,
            oeNo: product.oeNo,
            imageUrl: product.imageUrl,
            brandCode: product.brandCode,
            priceRmb: priceRmb,
            priceUsd: priceRmb / quoteSettings.exchangeRate,
            quantity: 1,
            profitRate: profitRate,
            includeTax: quoteSettings.includeTax,
            isFob: quoteSettings.isFob,
            recommendedPrice: 0,
            finalPrice: 0,
            subtotal: 0,
            locked: false
          }
          
          recalculateItem(newItem)
          quoteItems.value.push(newItem)
        }
      })
      message.success(`找到 ${res.data.length} 个产品`)
    } else {
      message.info('未找到匹配的产品')
    }
  } catch (error) {
    message.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 获取产品价格
const getProductPrice = (product: Product) => {
  switch (quoteSettings.priceMode) {
    case 'min': return product.priceMin || 0
    case 'max': return product.priceMax || 0
    default: return product.priceAvg || 0
  }
}

// 获取推荐利润率
const getRecommendedProfitRate = (quantity: number) => {
  if (quantity >= 500) return 0
  if (quantity >= 200) return 3
  if (quantity >= 100) return 6
  return 10
}

// 处理数量变化
const handleQuantityChange = (item: QuoteItem, val: number) => {
  item.quantity = val
  item.profitRate = getRecommendedProfitRate(val)
  recalculateItem(item)
}

// 处理FOB开关
const handleFobChange = () => {
  if (quoteSettings.isFob) {
    quoteSettings.includeTax = false
  }
  recalculate()
}

// 处理单项FOB变化
const handleItemFobChange = (item: QuoteItem) => {
  if (item.isFob) {
    item.includeTax = false
  }
  recalculateItem(item)
}

// 重新计算所有项
const recalculate = () => {
  quoteItems.value.forEach(item => {
    item.includeTax = quoteSettings.includeTax
    item.isFob = quoteSettings.isFob
    item.profitRate = quoteSettings.defaultProfitRate
    recalculateItem(item)
  })
}

// 重新计算单项
const recalculateItem = (item: QuoteItem) => {
  if (!item.priceRmb || item.priceRmb <= 0) {
    item.priceUsd = 0
    item.recommendedPrice = 0
    if (!item.locked) {
      item.finalPrice = 0
    }
    item.subtotal = 0
    return
  }

  // 汇率换算
  const priceUsd = item.priceRmb / quoteSettings.exchangeRate
  item.priceUsd = Number(priceUsd.toFixed(2))

  // 利润加成
  const profitRate = item.profitRate || quoteSettings.defaultProfitRate
  const afterProfit = priceUsd * (1 + profitRate / 100)

  // 计算推荐价格
  let recommendedPrice = afterProfit
  if (item.isFob) {
    recommendedPrice = afterProfit * (1 + quoteSettings.fobRate / 100)
  } else if (item.includeTax) {
    recommendedPrice = afterProfit * (1 + quoteSettings.taxRate / 100)
  }

  item.recommendedPrice = Number(recommendedPrice.toFixed(2))
  
  // 只有未锁定时才自动更新最终价格
  if (!item.locked) {
    item.finalPrice = item.recommendedPrice
  }

  updateSubtotal(item)
}

// 更新小计
const updateSubtotal = (item: QuoteItem) => {
  item.subtotal = Number(((item.finalPrice || 0) * (item.quantity || 1)).toFixed(2))
}

// 切换锁定状态
const toggleLock = (item: QuoteItem) => {
  item.locked = !item.locked
  if (!item.locked) {
    // 解锁时，立即更新为推荐价格
    item.finalPrice = item.recommendedPrice
    updateSubtotal(item)
    message.success('已解锁，价格将随参数自动更新')
  } else {
    message.success('已锁定当前价格')
  }
}

// 移除项
const removeItem = (index: number) => {
  quoteItems.value.splice(index, 1)
}

// 清空
const clearAll = () => {
  quoteItems.value = []
}

// 导出报价单
const exportQuote = async () => {
  if (quoteItems.value.length === 0) {
    message.warning('报价列表为空')
    return
  }

  exporting.value = true
  try {
    const request: QuoteRequest = {
      items: quoteItems.value,
      priceMode: quoteSettings.priceMode,
      exchangeRate: quoteSettings.exchangeRate,
      defaultProfitRate: quoteSettings.defaultProfitRate,
      taxRate: quoteSettings.taxRate,
      fobRate: quoteSettings.fobRate,
      includeTax: quoteSettings.includeTax,
      isFob: quoteSettings.isFob
    }

    const res = await productApi.exportQuote(request) as any
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报价单_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 获取实时汇率
const fetchExchangeRate = async () => {
  exchangeRateLoading.value = true
  try {
    const response = await fetch('https://api.exchangerate-api.com/v4/latest/USD')
    const data = await response.json()
    if (data && data.rates && data.rates.CNY) {
      const rate = Number(data.rates.CNY.toFixed(2))
      quoteSettings.exchangeRate = rate
      // 缓存到本地存储
      localStorage.setItem('exchangeRate', JSON.stringify({
        rate,
        updatedAt: new Date().toLocaleString(),
        date: new Date().toDateString()
      }))
      message.success(`汇率已更新: ${rate}`)
      recalculate()
    }
  } catch (error) {
    message.error('获取汇率失败')
  } finally {
    exchangeRateLoading.value = false
  }
}

// 初始化时从localStorage加载汇率
const loadExchangeRate = () => {
  const cached = localStorage.getItem('exchangeRate')
  if (cached) {
    try {
      const { rate, date } = JSON.parse(cached)
      quoteSettings.exchangeRate = rate
      // 如果不是今天的汇率，提示用户更新
      if (date !== new Date().toDateString()) {
        message.info('汇率数据不是最新的，建议点击刷新按钮更新')
      }
    } catch {
      // 忽略缓存解析错误
    }
  }
}

// 初始化时从sessionStorage加载
onMounted(() => {
  // 加载汇率
  loadExchangeRate()
  
  const savedItems = sessionStorage.getItem('quoteItems')
  if (savedItems) {
    try {
      const items = JSON.parse(savedItems)
      items.forEach((item: any) => {
        const newItem: QuoteItem = {
          productId: item.productId,
          xkNo: item.xkNo,
          oeNo: item.oeNo,
          imageUrl: item.imageUrl,
          brandCode: item.brandCode,
          priceRmb: item.priceRmb,
          priceUsd: item.priceRmb / quoteSettings.exchangeRate,
          quantity: item.quantity || 1,
          profitRate: quoteSettings.defaultProfitRate,
          includeTax: quoteSettings.includeTax,
          isFob: quoteSettings.isFob,
          recommendedPrice: 0,
          finalPrice: 0,
          subtotal: 0,
          locked: item.locked || false
        }
        recalculateItem(newItem)
        quoteItems.value.push(newItem)
      })
      // 清除已加载的数据
      sessionStorage.removeItem('quoteItems')
    } catch (e) {
      console.error('加载报价数据失败:', e)
    }
  }
})

// 监听报价列表变化，保存到sessionStorage
watch(quoteItems, (items) => {
  if (items.length > 0) {
    sessionStorage.setItem('quoteItems', JSON.stringify(items))
  }
}, { deep: true })
</script>

<style lang="less" scoped>
.product-quote-page {
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

  .search-section {
    padding: 20px;
    margin-bottom: 16px;
  }

  .settings-section {
    padding: 20px;
    margin-bottom: 16px;

    .settings-row {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;
      align-items: center;
    }

    .setting-item {
      display: flex;
      align-items: center;
      gap: 12px;

      .setting-label {
        color: #6b7280;
        font-size: 14px;
        white-space: nowrap;
      }
    }
  }

  .quote-section {
    padding: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }

      .item-count {
        color: #6b7280;
        font-size: 14px;
      }
    }

    :deep(.ant-table-tbody > tr > td) {
      padding: 12px 8px;
    }

    .product-image {
      width: 60px;
      height: 50px;
      border-radius: 4px;
      overflow: hidden;
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

    .price-cell {
      font-family: 'Roboto Mono', monospace;
      font-weight: 500;

      &.subtotal {
        color: #1677ff;
        font-weight: 600;
      }
    }

    .quote-summary {
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid #f0f0f0;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 32px;

      .summary-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .summary-label {
          color: #6b7280;
        }

        .summary-value {
          font-weight: 600;
          font-size: 16px;
        }

        &.total {
          .summary-value {
            color: #1677ff;
            font-size: 20px;
          }
        }
      }

      .summary-actions {
        display: flex;
        gap: 12px;
        margin-left: 24px;
      }
    }
  }
}
</style>

