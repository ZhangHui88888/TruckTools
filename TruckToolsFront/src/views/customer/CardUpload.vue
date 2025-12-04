<template>
  <div class="card-upload-page">
    <div class="page-header">
      <h1 class="page-title">名片识别</h1>
      <p class="page-subtitle">上传名片图片，AI自动识别并提取客户信息</p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section content-card">
      <a-upload-dragger
        v-model:file-list="fileList"
        name="files"
        :multiple="true"
        :max-count="50"
        :accept="'.jpg,.jpeg,.png,.heic'"
        :before-upload="beforeUpload"
        :custom-request="customUpload"
        :show-upload-list="false"
        class="upload-dragger"
      >
        <p class="ant-upload-drag-icon">
          <CameraOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽名片图片到此处上传</p>
        <p class="ant-upload-hint">
          支持 JPG、PNG、HEIC 格式，单次最多上传 50 张名片
        </p>
      </a-upload-dragger>

      <!-- 待上传文件列表 -->
      <div v-if="pendingFiles.length > 0" class="pending-files">
        <div class="pending-header">
          <span>待上传 {{ pendingFiles.length }} 张名片</span>
          <a-button type="link" danger @click="pendingFiles = []">清空</a-button>
        </div>
        <div class="file-grid">
          <div
            v-for="(file, index) in pendingFiles"
            :key="file.uid"
            class="file-item"
          >
            <img :src="file.preview" alt="名片预览" class="file-preview" />
            <div class="file-overlay">
              <a-radio-group v-model:value="file.priority" size="small" button-style="solid">
                <a-radio-button :value="1">高</a-radio-button>
                <a-radio-button :value="2">中</a-radio-button>
                <a-radio-button :value="3">低</a-radio-button>
              </a-radio-group>
            </div>
            <a-button
              type="text"
              size="small"
              class="remove-btn"
              @click="removePendingFile(index)"
            >
              <CloseOutlined />
            </a-button>
          </div>
        </div>
        <div class="pending-actions">
          <a-button type="primary" :loading="uploading" @click="startUpload">
            <UploadOutlined />
            开始识别
          </a-button>
        </div>
      </div>
    </div>

    <!-- 识别结果 -->
    <div v-if="recognitionResults.length > 0" class="results-section content-card">
      <div class="results-header">
        <h3>识别结果</h3>
        <div class="results-stats">
          <a-badge status="processing" text="识别中" v-if="processingCount > 0" />
          <a-badge status="success" :text="`成功 ${successCount}`" />
          <a-badge status="error" :text="`失败 ${failedCount}`" />
        </div>
      </div>

      <a-list :data-source="recognitionResults" class="results-list">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <div class="card-thumbnail">
                  <img :src="item.thumbnailUrl || item.imageUrl" alt="名片" />
                  <div class="status-badge" :class="statusClass(item.ocrStatus)">
                    <LoadingOutlined v-if="item.ocrStatus === 1" spin />
                    <CheckOutlined v-else-if="item.ocrStatus === 2" />
                    <CloseOutlined v-else-if="item.ocrStatus === 3" />
                  </div>
                </div>
              </template>
              <template #title>
                <span v-if="item.ocrStatus === 2">{{ item.parsedData?.name || '未识别姓名' }}</span>
                <span v-else-if="item.ocrStatus === 1">识别中...</span>
                <span v-else class="text-error">识别失败</span>
              </template>
              <template #description>
                <template v-if="item.ocrStatus === 2">
                  <div class="parsed-info">
                    <span v-if="item.parsedData?.company">
                      <HomeOutlined /> {{ item.parsedData.company }}
                    </span>
                    <span v-if="item.parsedData?.email">
                      <MailOutlined /> {{ item.parsedData.email }}
                    </span>
                    <span v-if="item.parsedData?.phone">
                      <PhoneOutlined /> {{ item.parsedData.phone }}
                    </span>
                  </div>
                  <div v-if="item.ocrConfidence" class="confidence">
                    置信度: {{ item.ocrConfidence.toFixed(1) }}%
                  </div>
                </template>
              </template>
            </a-list-item-meta>
            
            <template #actions>
              <template v-if="item.ocrStatus === 2 && !item.isProcessed">
                <a-button type="primary" size="small" @click="confirmCard(item)">
                  确认保存
                </a-button>
                <a-button size="small" @click="editCard(item)">
                  编辑
                </a-button>
              </template>
              <template v-else-if="item.isProcessed">
                <a-tag color="success">已保存</a-tag>
              </template>
            </template>
          </a-list-item>
        </template>
      </a-list>

      <!-- 批量操作 -->
      <div class="batch-actions">
        <a-button
          type="primary"
          :disabled="successCount === 0 || allProcessed"
          @click="batchConfirm"
        >
          批量保存全部 ({{ unprocessedSuccessCount }})
        </a-button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <CardEditModal
      v-model:open="showEditModal"
      :card="currentCard"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadFile } from 'ant-design-vue'
import {
  CameraOutlined,
  CloseOutlined,
  UploadOutlined,
  LoadingOutlined,
  CheckOutlined,
  HomeOutlined,
  MailOutlined,
  PhoneOutlined
} from '@ant-design/icons-vue'
import { customerApi } from '@/api/customer'
import type { BusinessCard } from '@/api/customer'
import CardEditModal from './components/CardEditModal.vue'

interface PendingFile {
  uid: string
  file: File
  preview: string
  priority: number
}

const fileList = ref<UploadFile[]>([])
const pendingFiles = ref<PendingFile[]>([])
const recognitionResults = ref<BusinessCard[]>([])
const uploading = ref(false)
const showEditModal = ref(false)
const currentCard = ref<BusinessCard | null>(null)

// 统计
const processingCount = computed(() => 
  recognitionResults.value.filter(r => r.ocrStatus === 0 || r.ocrStatus === 1).length
)
const successCount = computed(() => 
  recognitionResults.value.filter(r => r.ocrStatus === 2).length
)
const failedCount = computed(() => 
  recognitionResults.value.filter(r => r.ocrStatus === 3).length
)
const unprocessedSuccessCount = computed(() => 
  recognitionResults.value.filter(r => r.ocrStatus === 2 && !r.isProcessed).length
)
const allProcessed = computed(() => 
  recognitionResults.value.every(r => r.ocrStatus !== 2 || r.isProcessed)
)

// 状态样式
const statusClass = (status: number) => {
  switch (status) {
    case 1: return 'processing'
    case 2: return 'success'
    case 3: return 'error'
    default: return 'pending'
  }
}

// 上传前处理
const beforeUpload = async (file: File) => {
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/heic']
  if (!validTypes.includes(file.type)) {
    message.error('只支持 JPG、PNG、HEIC 格式的图片')
    return false
  }
  
  // 验证文件大小
  if (file.size > 500 * 1024 * 1024) {
    message.error('图片大小不能超过 500MB')
    return false
  }
  
  // 创建预览
  const preview = await createPreview(file)
  pendingFiles.value.push({
    uid: `${Date.now()}-${Math.random()}`,
    file,
    preview,
    priority: 2
  })
  
  return false // 阻止自动上传
}

// 自定义上传（不使用）
const customUpload = () => {
  // 使用自定义上传逻辑
}

// 创建预览
const createPreview = (file: File): Promise<string> => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      resolve(e.target?.result as string)
    }
    reader.readAsDataURL(file)
  })
}

// 移除待上传文件
const removePendingFile = (index: number) => {
  pendingFiles.value.splice(index, 1)
}

// 开始上传
const startUpload = async () => {
  if (pendingFiles.value.length === 0) {
    message.warning('请先选择名片图片')
    return
  }
  
  uploading.value = true
  
  try {
    const formData = new FormData()
    const priorities: number[] = []
    
    pendingFiles.value.forEach((pf) => {
      formData.append('files', pf.file)
      priorities.push(pf.priority) // 1-3（1最高）
    })
    formData.append('priorities', JSON.stringify(priorities))
    
    const res = await customerApi.uploadCards(formData)
    
    // 添加到识别结果
    recognitionResults.value = [...res.data.cards, ...recognitionResults.value]
    
    // 清空待上传
    pendingFiles.value = []
    
    message.success(`已上传 ${res.data.totalCount} 张名片，正在识别...`)
    
    // 开始轮询状态
    pollBatchStatus(res.data.batchId)
  } catch {
    message.error('上传失败')
  } finally {
    uploading.value = false
  }
}

// 轮询批次状态
const pollBatchStatus = async (batchId: string) => {
  const poll = async () => {
    try {
      const res = await customerApi.getBatchStatus(batchId)
      
      // 更新识别结果
      res.data.cards.forEach((card) => {
        const index = recognitionResults.value.findIndex(r => r.id === card.id)
        if (index !== -1) {
          recognitionResults.value[index] = { ...recognitionResults.value[index], ...card }
        }
      })
      
      // 如果还有未完成的，继续轮询
      if (res.data.pendingCount > 0 || res.data.processingCount > 0) {
        setTimeout(poll, 2000)
      }
    } catch {
      // 轮询失败，静默处理
    }
  }
  
  poll()
}

// 确认保存单张
const confirmCard = async (card: BusinessCard) => {
  try {
    await customerApi.confirmCard(card.id, {
      name: card.parsedData?.name,
      email: card.parsedData?.email,
      phone: card.parsedData?.phone,
      company: card.parsedData?.company,
      position: card.parsedData?.position,
      address: card.parsedData?.address,
      website: card.parsedData?.website,
      priority: card.priority
    })
    
    // 更新状态
    const index = recognitionResults.value.findIndex(r => r.id === card.id)
    if (index !== -1) {
      recognitionResults.value[index].isProcessed = true
    }
    
    message.success('保存成功')
  } catch {
    message.error('保存失败')
  }
}

// 编辑
const editCard = (card: BusinessCard) => {
  currentCard.value = card
  showEditModal.value = true
}

// 编辑成功
const handleEditSuccess = (customerId: string) => {
  if (currentCard.value) {
    const index = recognitionResults.value.findIndex(r => r.id === currentCard.value!.id)
    if (index !== -1) {
      recognitionResults.value[index].isProcessed = true
    }
  }
  showEditModal.value = false
  currentCard.value = null
  message.success('保存成功')
  console.log('Created customer:', customerId)
}

// 批量保存
const batchConfirm = async () => {
  const cardIds = recognitionResults.value
    .filter(r => r.ocrStatus === 2 && !r.isProcessed)
    .map(r => r.id)
  
  if (cardIds.length === 0) return
  
  try {
    const res = await customerApi.batchConfirmCards(cardIds)
    
    // 更新状态
    cardIds.forEach((id) => {
      const index = recognitionResults.value.findIndex(r => r.id === id)
      if (index !== -1) {
        recognitionResults.value[index].isProcessed = true
      }
    })
    
    message.success(`成功保存 ${res.data.successCount} 个客户`)
  } catch {
    message.error('批量保存失败')
  }
}
</script>

<style lang="less" scoped>
.card-upload-page {
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
    margin-bottom: 24px;
    
    .upload-dragger {
      :deep(.ant-upload-drag-icon) {
        font-size: 48px;
        color: #1677ff;
        margin-bottom: 16px;
      }
    }
  }
  
  .pending-files {
    margin-top: 24px;
    
    .pending-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      font-weight: 500;
    }
    
    .file-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 16px;
    }
    
    .file-item {
      position: relative;
      aspect-ratio: 3/2;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #e5e7eb;
      
      .file-preview {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .file-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 8px;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
        display: flex;
        justify-content: center;
      }
      
      .remove-btn {
        position: absolute;
        top: 4px;
        right: 4px;
        background: rgba(0, 0, 0, 0.5);
        color: #fff;
        border-radius: 50%;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &:hover {
          background: rgba(255, 0, 0, 0.7);
        }
      }
    }
    
    .pending-actions {
      margin-top: 24px;
      text-align: center;
    }
  }
  
  .results-section {
    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
      
      .results-stats {
        display: flex;
        gap: 16px;
      }
    }
    
    .card-thumbnail {
      position: relative;
      width: 100px;
      height: 60px;
      border-radius: 4px;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .status-badge {
        position: absolute;
        bottom: 4px;
        right: 4px;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: #fff;
        
        &.pending { background: #8c8c8c; }
        &.processing { background: #1677ff; }
        &.success { background: #52c41a; }
        &.error { background: #ff4d4f; }
      }
    }
    
    .parsed-info {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-top: 4px;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #6b7280;
        font-size: 13px;
      }
    }
    
    .confidence {
      margin-top: 4px;
      font-size: 12px;
      color: #9ca3af;
    }
    
    .text-error {
      color: #ff4d4f;
    }
    
    .batch-actions {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #e5e7eb;
      text-align: center;
    }
  }
}
</style>

