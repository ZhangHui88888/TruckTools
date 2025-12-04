<template>
  <div class="product-import-page">
    <div class="page-header">
      <div class="header-left">
        <a-button type="link" @click="$router.push('/product/list')">
          <ArrowLeftOutlined />
          返回产品目录
        </a-button>
        <h1 class="page-title">产品导入</h1>
        <p class="page-subtitle">从Excel文件批量导入产品数据</p>
      </div>
    </div>

    <div class="import-steps content-card">
      <a-steps :current="currentStep" :items="steps" />
    </div>

    <!-- 步骤1: 上传文件 -->
    <div v-if="currentStep === 0" class="step-content content-card">
      <div class="template-download">
        <a-button type="primary" size="large" @click="handleDownloadTemplate" :loading="downloadingTemplate">
          <DownloadOutlined />
          下载产品导入模板
        </a-button>
        <p class="template-hint">模板包含3个品牌示例数据，可直接参考填写</p>
      </div>

      <a-divider>或上传已填写的Excel文件</a-divider>

      <div class="upload-area">
        <a-upload-dragger
          v-model:file-list="fileList"
          name="file"
          accept=".xlsx"
          :max-count="1"
          :before-upload="handleBeforeUpload"
          :custom-request="handleUpload"
        >
          <p class="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p class="ant-upload-hint">
            仅支持 .xlsx 格式的Excel文件，每个Sheet代表一个品牌
          </p>
        </a-upload-dragger>
      </div>

      <div class="format-info">
        <h3>Excel格式要求</h3>
        <ul>
          <li>每个 <strong>Sheet</strong> = 一个品牌，Sheet名 = 品牌全称</li>
          <li><strong>第1行</strong> = 品牌缩写（如 MB、VL、SC）</li>
          <li><strong>第2行</strong> = 表头：NO. | XK NO. | OE NO. | PICTURE | 售价 | 备注</li>
          <li><strong>数据从第3行开始</strong></li>
          <li>图片支持 DISPIMG 公式格式，系统会自动提取</li>
        </ul>
      </div>
    </div>

    <!-- 步骤2: 预览确认 -->
    <div v-if="currentStep === 1" class="step-content content-card">
      <div class="preview-summary">
        <a-descriptions title="文件信息" :column="3" bordered>
          <a-descriptions-item label="文件名">{{ importResult?.fileName }}</a-descriptions-item>
          <a-descriptions-item label="总产品数">{{ importResult?.totalProducts }}</a-descriptions-item>
          <a-descriptions-item label="总图片数">{{ importResult?.totalImages }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <div class="brand-sheets">
        <h3>品牌Sheet信息</h3>
        <a-table
          :data-source="importResult?.brandSheets || []"
          :columns="brandColumns"
          :pagination="false"
          row-key="sheetName"
          size="small"
        />
      </div>

      <div class="preview-data">
        <h3>数据预览（前5条）</h3>
        <a-table
          :data-source="importResult?.previewData || []"
          :columns="previewColumns"
          :pagination="false"
          row-key="oeNo"
          size="small"
        />
      </div>

      <div class="step-actions">
        <a-button @click="currentStep = 0">上一步</a-button>
        <a-button type="primary" @click="handleExecuteImport" :loading="importing">
          确认导入
        </a-button>
      </div>
    </div>

    <!-- 步骤3: 导入结果 -->
    <div v-if="currentStep === 2" class="step-content content-card">
      <div class="import-result">
        <a-result
          v-if="importStatus?.status === 'completed'"
          status="success"
          title="导入完成"
          :sub-title="`成功导入 ${importStatus.successCount} 条产品`"
        >
          <template #extra>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="总行数">{{ importStatus.totalRows }}</a-descriptions-item>
              <a-descriptions-item label="成功数">{{ importStatus.successCount }}</a-descriptions-item>
              <a-descriptions-item label="失败数">{{ importStatus.failedCount }}</a-descriptions-item>
              <a-descriptions-item label="跳过数(重复)">{{ importStatus.skippedCount }}</a-descriptions-item>
            </a-descriptions>
            <div class="result-actions">
              <a-button type="primary" @click="$router.push('/product/list')">
                查看产品列表
              </a-button>
              <a-button @click="resetImport">继续导入</a-button>
            </div>
          </template>
        </a-result>

        <a-result
          v-else-if="importStatus?.status === 'failed'"
          status="error"
          title="导入失败"
          :sub-title="importStatus.errorMessage"
        >
          <template #extra>
            <a-button type="primary" @click="resetImport">重新导入</a-button>
          </template>
        </a-result>

        <div v-else class="importing-progress">
          <a-spin size="large" />
          <p>正在导入中，请稍候...</p>
          <a-progress :percent="importStatus?.progress || 0" status="active" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  InboxOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'
import { productApi } from '@/api/product'
import type { ImportResult, ImportStatus } from '@/api/product'

const router = useRouter()

const currentStep = ref(0)
const fileList = ref<any[]>([])
const uploading = ref(false)
const importing = ref(false)
const downloadingTemplate = ref(false)
const importResult = ref<ImportResult | null>(null)
const importStatus = ref<ImportStatus | null>(null)

const steps = [
  { title: '上传文件', description: '选择Excel文件' },
  { title: '预览确认', description: '确认导入数据' },
  { title: '导入结果', description: '查看导入结果' }
]

const brandColumns = [
  { title: 'Sheet名称', dataIndex: 'sheetName' },
  { title: '品牌缩写', dataIndex: 'brandCode' },
  { title: '品牌全称', dataIndex: 'brandName' },
  { title: '产品数量', dataIndex: 'productCount' },
  { title: '图片数量', dataIndex: 'imageCount' }
]

const previewColumns = [
  { title: '品牌', dataIndex: 'brandCode' },
  { title: 'XK NO.', dataIndex: 'xkNo' },
  { title: 'OE NO.', dataIndex: 'oeNo' },
  { title: '售价', dataIndex: 'price' },
  { title: '备注', dataIndex: 'remark' }
]

const handleBeforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isXlsx = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                 file.name.toLowerCase().endsWith('.xlsx')
  if (!isXlsx) {
    message.error('仅支持 .xlsx 格式的文件')
    return false
  }
  return true
}

const handleUpload: UploadProps['customRequest'] = async (options) => {
  const { file, onSuccess, onError } = options
  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', file as File)

    const res = await productApi.uploadImportFile(formData)
    if (res && res.data) {
      importResult.value = res.data
      currentStep.value = 1
      onSuccess?.(res.data)
      message.success('文件解析成功')
    }
  } catch (error: any) {
    message.error('文件上传失败: ' + (error.message || '未知错误'))
    onError?.(error)
  } finally {
    uploading.value = false
  }
}

const handleExecuteImport = async () => {
  if (!importResult.value?.importId) {
    message.error('导入ID不存在')
    return
  }

  importing.value = true
  try {
    const res = await productApi.executeImport(importResult.value.importId)
    if (res && res.data) {
      importStatus.value = res.data
      currentStep.value = 2

      // 轮询导入状态
      if (res.data.status === 'processing') {
        pollImportStatus()
      }
    }
  } catch (error) {
    message.error('导入失败')
  } finally {
    importing.value = false
  }
}

const pollImportStatus = async () => {
  if (!importResult.value?.importId) return

  const poll = async () => {
    try {
      const res = await productApi.getImportStatus(importResult.value!.importId)
      if (res && res.data) {
        importStatus.value = res.data

        if (res.data.status === 'processing') {
          setTimeout(poll, 1000)
        } else if (res.data.status === 'completed') {
          message.success('导入完成')
        } else if (res.data.status === 'failed') {
          message.error('导入失败')
        }
      }
    } catch (error) {
      console.error('获取导入状态失败:', error)
    }
  }

  poll()
}

const resetImport = () => {
  currentStep.value = 0
  fileList.value = []
  importResult.value = null
  importStatus.value = null
}

const handleDownloadTemplate = async () => {
  downloadingTemplate.value = true
  try {
    const response = await productApi.downloadImportTemplate()
    if (response && response.data) {
      // 创建下载链接
      const blob = new Blob([response.data], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // 从响应头获取文件名，或使用默认文件名
      const contentDisposition = response.headers?.['content-disposition']
      let fileName = '产品导入模板.xlsx'
      if (contentDisposition) {
        const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition)
        if (matches && matches[1]) {
          fileName = decodeURIComponent(matches[1].replace(/['"]/g, ''))
        }
      }
      
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      message.success('模板下载成功')
    }
  } catch (error: any) {
    message.error('模板下载失败: ' + (error.message || '未知错误'))
  } finally {
    downloadingTemplate.value = false
  }
}
</script>

<style lang="less" scoped>
.product-import-page {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 8px 0 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #6b7280;
      margin: 0;
    }
  }

  .import-steps {
    margin-bottom: 24px;
    padding: 24px;
  }

  .step-content {
    padding: 24px;
  }

  .template-download {
    text-align: center;
    padding: 32px 0 24px;
    
    .ant-btn {
      height: 48px;
      font-size: 16px;
      padding: 0 32px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
        transition: all 0.3s ease;
      }
    }
    
    .template-hint {
      margin-top: 12px;
      color: #6b7280;
      font-size: 14px;
    }
  }

  :deep(.ant-divider) {
    margin: 32px 0;
    font-size: 14px;
    color: #9ca3af;
  }

  .upload-area {
    max-width: 600px;
    margin: 0 auto 32px;
  }

  .format-info {
    background: #f9fafb;
    border-radius: 8px;
    padding: 20px;
    
    h3 {
      margin: 0 0 12px;
      font-size: 16px;
      color: #1f2937;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 8px;
        color: #6b7280;
        line-height: 1.6;
      }
    }
  }

  .preview-summary {
    margin-bottom: 24px;
  }

  .brand-sheets,
  .preview-data {
    margin-bottom: 24px;

    h3 {
      margin: 0 0 12px;
      font-size: 16px;
      color: #1f2937;
    }
  }

  .step-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
  }

  .import-result {
    text-align: center;
    padding: 40px 0;

    .result-actions {
      margin-top: 24px;
      display: flex;
      justify-content: center;
      gap: 12px;
    }
  }

  .importing-progress {
    text-align: center;
    padding: 60px 0;

    p {
      margin: 24px 0;
      color: #6b7280;
    }

    :deep(.ant-progress) {
      max-width: 400px;
      margin: 0 auto;
    }
  }
}
</style>

