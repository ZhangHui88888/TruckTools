<template>
  <div class="customer-import-page">
    <div class="page-header">
      <h1 class="page-title">Excel导入客户</h1>
      <p class="page-subtitle">批量导入客户信息，快速建立客户档案</p>
    </div>

    <!-- 步骤条 -->
    <a-steps :current="currentStep" class="import-steps">
      <a-step title="上传文件" />
      <a-step title="字段映射" />
      <a-step title="数据预检" />
      <a-step title="导入完成" />
    </a-steps>

    <div class="step-content content-card">
      <!-- 步骤1: 上传文件 -->
      <div v-if="currentStep === 0" class="step-upload">
        <div class="upload-tips">
          <h4>导入说明</h4>
          <ul>
            <li>支持 .xlsx 格式的 Excel 文件</li>
            <li>单次最多导入 10,000 条客户数据</li>
            <li>必填字段：客户姓名 + 至少一种联系方式或公司名称</li>
            <li>联系方式包括：邮箱、手机号、微信、WhatsApp</li>
            <li>建议先下载模板，按模板格式填写数据</li>
          </ul>
          <a-button type="link" @click="downloadTemplate">
            <DownloadOutlined />
            下载导入模板
          </a-button>
        </div>

        <a-upload-dragger
          name="file"
          :accept="'.xlsx'"
          :max-count="1"
          :before-upload="handleFileSelect"
          :show-upload-list="false"
          class="upload-area"
        >
          <p class="ant-upload-drag-icon">
            <FileExcelOutlined />
          </p>
          <p class="ant-upload-text">点击或拖拽Excel文件到此处</p>
          <p class="ant-upload-hint">仅支持 .xlsx 格式</p>
        </a-upload-dragger>

        <div v-if="selectedFile" class="selected-file">
          <FileExcelOutlined class="file-icon" />
          <span class="file-name">{{ selectedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
          <a-button type="link" danger @click="selectedFile = null">移除</a-button>
        </div>

        <div class="step-actions">
          <a-button
            type="primary"
            :disabled="!selectedFile"
            :loading="uploading"
            @click="uploadFile"
          >
            下一步
          </a-button>
        </div>
      </div>

      <!-- 步骤2: 字段映射 -->
      <div v-else-if="currentStep === 1" class="step-mapping">
        <a-alert
          type="info"
          message="系统已自动识别字段映射，请检查并调整"
          show-icon
          style="margin-bottom: 24px"
        />

        <a-table
          :data-source="mappingData"
          :columns="mappingColumns"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'systemField'">
              <a-select
                v-model:value="record.systemField"
                style="width: 160px"
                allow-clear
                placeholder="选择映射字段"
              >
                <a-select-option v-for="f in systemFields" :key="f.key" :value="f.key">
                  {{ f.label }}
                  <a-tag v-if="f.required" color="red" size="small">必填</a-tag>
                </a-select-option>
              </a-select>
            </template>
            <template v-else-if="column.key === 'preview'">
              <span class="preview-text">{{ record.previewValue || '-' }}</span>
            </template>
          </template>
        </a-table>

        <div class="import-options">
          <a-form layout="inline">
            <a-form-item label="导入模式">
              <a-radio-group v-model:value="importMode">
                <a-radio value="append">追加（不影响现有数据）</a-radio>
                <a-radio value="overwrite">覆盖（根据邮箱匹配更新）</a-radio>
                <a-radio value="merge">合并（仅更新非空字段）</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-form>
        </div>

        <div class="step-actions">
          <a-button @click="goBackToUpload">上一步</a-button>
          <a-button type="primary" :loading="validating" @click="validateMapping">
            下一步：数据预检
          </a-button>
        </div>
      </div>

      <!-- 步骤3: 数据预检 -->
      <div v-else-if="currentStep === 2" class="step-validation">
        <div class="validation-summary">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic title="总数据行" :value="validationResult?.totalRows || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="有效数据"
                :value="validationResult?.validRows || 0"
                :value-style="{ color: '#52c41a' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="无效数据"
                :value="validationResult?.invalidRows || 0"
                :value-style="{ color: '#ff4d4f' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="重复数据"
                :value="validationResult?.duplicateRows || 0"
                :value-style="{ color: '#faad14' }"
              >
                <template #suffix>
                  <a-tooltip v-if="(validationResult?.duplicateInFile || 0) > 0 || (validationResult?.duplicateInDb || 0) > 0">
                    <template #title>
                      <div style="text-align: left">
                        <div v-if="(validationResult?.duplicateInFile || 0) > 0">
                          文件内重复: {{ validationResult?.duplicateInFile }} 条（将跳过）
                        </div>
                        <div v-if="(validationResult?.duplicateInDb || 0) > 0">
                          数据库重复: {{ validationResult?.duplicateInDb }} 条（将{{ importMode === 'overwrite' ? '覆盖' : importMode === 'merge' ? '合并' : '追加' }}）
                        </div>
                      </div>
                    </template>
                    <QuestionCircleOutlined style="font-size: 14px; margin-left: 4px; cursor: help" />
                  </a-tooltip>
                </template>
              </a-statistic>
            </a-col>
          </a-row>
          
          <!-- 重复数据处理选项 -->
          <div v-if="(validationResult?.duplicateInDb || 0) > 0" class="duplicate-options" style="margin-top: 16px">
            <a-alert type="info" show-icon>
              <template #message>
                <div>
                  <strong>重复数据说明：</strong>
                  <ul style="margin: 8px 0 0 0; padding-left: 20px">
                    <li v-if="(validationResult?.duplicateInFile || 0) > 0">
                      文件内重复 {{ validationResult?.duplicateInFile }} 条：同一文件中姓名+邮箱+国家相同，将<strong>跳过</strong>后面重复的行
                    </li>
                    <li>
                      数据库重复 {{ validationResult?.duplicateInDb }} 条：与数据库中已有客户相同
                    </li>
                  </ul>
                  <div style="margin-top: 12px">
                    <strong>重复数据处理方式：</strong>
                    <a-radio-group v-model:value="duplicateAction" style="margin-left: 8px">
                      <a-radio value="skip">跳过（仅导入新数据）</a-radio>
                      <a-radio value="update">更新（覆盖已有客户信息）</a-radio>
                    </a-radio-group>
                  </div>
                </div>
              </template>
            </a-alert>
          </div>
          
          <!-- 无重复数据时的说明 -->
          <a-alert 
            v-else-if="(validationResult?.duplicateInFile || 0) > 0" 
            type="info" 
            show-icon
            style="margin-top: 16px"
          >
            <template #message>
              <div>
                <strong>重复数据说明：</strong>
                <ul style="margin: 8px 0 0 0; padding-left: 20px">
                  <li>
                    文件内重复 {{ validationResult?.duplicateInFile }} 条：同一文件中姓名+邮箱+国家相同，将<strong>跳过</strong>后面重复的行
                  </li>
                </ul>
              </div>
            </template>
          </a-alert>
        </div>

        <!-- 可导入数据预览 -->
        <div v-if="previewData.length > 0" class="preview-list" style="margin-top: 16px">
          <h4>
            {{ duplicateAction === 'skip' ? '新数据预览' : '可导入数据预览' }}
            <span style="font-weight: normal; color: #999; font-size: 12px">
              （显示前 {{ previewData.length }} 条）
            </span>
          </h4>
          <a-table
            :data-source="previewData"
            :columns="previewColumns"
            :pagination="false"
            size="small"
            :scroll="{ x: 800 }"
          />
        </div>

        <div v-if="validationResult?.errors?.length" class="error-list" style="margin-top: 16px">
          <h4>
            错误详情
            <a-button type="link" size="small" @click="downloadErrors">下载错误报告</a-button>
          </h4>
          <a-table
            :data-source="validationResult.errors.slice(0, 10)"
            :columns="errorColumns"
            :pagination="false"
            size="small"
          />
          <p v-if="validationResult.errors.length > 10" class="more-errors">
            还有 {{ validationResult.errors.length - 10 }} 条错误...
          </p>
        </div>

        <div class="step-actions">
          <a-button @click="currentStep = 1">上一步</a-button>
          <a-button
            type="primary"
            :disabled="importCount === 0"
            :loading="importing"
            @click="executeImport"
          >
            开始导入 ({{ importCount }} 条)
          </a-button>
        </div>
      </div>

      <!-- 步骤4: 导入完成 -->
      <div v-else class="step-result">
        <a-result
          :status="importStatus?.status === 'completed' ? 'success' : 'error'"
          :title="importStatus?.status === 'completed' ? '导入完成' : '导入失败'"
        >
          <template #extra>
            <div v-if="importStatus?.status === 'completed'" class="result-stats">
              <a-row :gutter="24">
                <a-col :span="8">
                  <a-statistic
                    title="成功导入"
                    :value="importStatus.successCount"
                    :value-style="{ color: '#52c41a' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="跳过（重复）"
                    :value="importStatus.skippedCount"
                    :value-style="{ color: '#faad14' }"
                  />
                </a-col>
                <a-col :span="8">
                  <a-statistic
                    title="失败"
                    :value="importStatus.failedCount"
                    :value-style="{ color: '#ff4d4f' }"
                  />
                </a-col>
              </a-row>
            </div>
            <div class="result-actions">
              <a-button type="primary" @click="$router.push('/customer/list')">
                查看客户列表
              </a-button>
              <a-button @click="resetImport">继续导入</a-button>
              <a-button v-if="importStatus?.logFileUrl" @click="downloadLog">
                下载导入日志
              </a-button>
            </div>
          </template>
        </a-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { DownloadOutlined, FileExcelOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue'
import { customerApi } from '@/api/customer'
import type { ImportResult, ImportValidation, ImportStatus } from '@/api/customer'
import * as XLSX from 'xlsx'

const currentStep = ref(0)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const validating = ref(false)
const importing = ref(false)

const importId = ref('')
const importResult = ref<ImportResult | null>(null)
const validationResult = ref<ImportValidation | null>(null)
const importStatus = ref<ImportStatus | null>(null)
const importMode = ref('overwrite')
const duplicateAction = ref<'update' | 'skip'>('skip') // 重复数据处理：update=更新, skip=跳过

// 字段映射数据
const mappingData = ref<Array<{
  excelColumn: string
  systemField: string
  previewValue: string
}>>([])

// 系统字段
const systemFields = [
  { key: 'name', label: '客户姓名', required: true },
  { key: 'email', label: '邮箱', required: false },
  { key: 'phone', label: '手机号', required: false },
  { key: 'company', label: '所属公司', required: false },
  { key: 'position', label: '职位', required: false },
  { key: 'country', label: '国家', required: false },
  { key: 'website', label: '公司官网', required: false },
  { key: 'address', label: '地址', required: false },
  { key: 'wechatName', label: '微信名称/ID', required: false },
  { key: 'whatsappName', label: 'WhatsApp名称/号码', required: false },
  { key: 'meetingTime', label: '会面时间', required: false },
  { key: 'meetingLocation', label: '会面地点', required: false },
  { key: 'followUpStatus', label: '跟进状态', required: false },
  { key: 'priority', label: '优先级', required: false },
  { key: 'remark', label: '备注', required: false }
]

// 映射表格列
const mappingColumns = [
  { title: 'Excel列名', dataIndex: 'excelColumn', key: 'excelColumn', width: 200 },
  { title: '系统字段', key: 'systemField', width: 200 },
  { title: '数据预览', key: 'preview' }
]

// 错误表格列
const errorColumns = [
  { title: '行号', dataIndex: 'row', key: 'row', width: 80 },
  { title: '字段', dataIndex: 'field', key: 'field', width: 100 },
  { title: '错误值', dataIndex: 'value', key: 'value', width: 150 },
  { title: '错误信息', dataIndex: 'message', key: 'message' }
]

// 预览表格列
const previewColumns = [
  { title: '行号', dataIndex: 'rowNum', key: 'rowNum', width: 70 },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 120 },
  { title: '公司', dataIndex: 'company', key: 'company', width: 150 },
  { title: '国家', dataIndex: 'country', key: 'country', width: 80 }
]

// 预览数据（根据选择显示新数据或全部数据）
const previewData = computed(() => {
  if (!validationResult.value) return []
  if (duplicateAction.value === 'skip') {
    // 跳过模式：只显示新数据
    return validationResult.value.newDataPreview || []
  } else {
    // 更新模式：显示新数据 + 重复数据
    const newData = validationResult.value.newDataPreview || []
    const dupData = validationResult.value.duplicateDataPreview || []
    return [...newData, ...dupData].slice(0, 20)
  }
})

// 计算实际导入数量
const importCount = computed(() => {
  if (!validationResult.value) return 0
  const validRows = validationResult.value.validRows || 0
  const duplicateRows = validationResult.value.duplicateRows || 0
  // 如果选择跳过重复数据，则减去重复数量（duplicateRows是有效数据中的重复数）
  if (duplicateAction.value === 'skip') {
    return Math.max(0, validRows - duplicateRows)
  }
  return validRows
})

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 下载模板
const downloadTemplate = () => {
  // 模板数据：表头和示例行
  const templateData = [
    {
      '客户姓名': '张三（必填）',
      '邮箱': 'zhangsan@example.com',
      '手机号': '+86 13800138000',
      '所属公司': 'ABC贸易有限公司',
      '职位': '采购经理',
      '国家': '美国',
      '公司官网': 'https://www.example.com',
      '地址': '123 Main Street, New York',
      '微信名称/ID': 'zhang_san_wechat',
      'WhatsApp名称/号码': '+86 13800138000',
      '会面时间': '2025-01-15 14:00:00',
      '会面地点': '广交会A馆3楼',
      '跟进状态': 'pending_us',
      '优先级': '1',
      '备注': '对我司产品非常感兴趣'
    },
    {
      '客户姓名': 'John Smith',
      '邮箱': 'john@company.com',
      '手机号': '+1 555-1234',
      '所属公司': 'Smith Industries',
      '职位': 'CEO',
      '国家': '英国',
      '公司官网': 'https://smith.com',
      '地址': '456 Oxford Street, London',
      '微信名称/ID': '',
      'WhatsApp名称/号码': '+1 555-1234',
      '会面时间': '',
      '会面地点': '',
      '跟进状态': 'pending_customer',
      '优先级': '2',
      '备注': ''
    }
  ]
  
  // 创建工作簿
  const wb = XLSX.utils.book_new()
  const ws = XLSX.utils.json_to_sheet(templateData)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 15 }, // 客户姓名
    { wch: 30 }, // 邮箱
    { wch: 18 }, // 手机号
    { wch: 25 }, // 所属公司
    { wch: 12 }, // 职位
    { wch: 10 }, // 国家
    { wch: 30 }, // 公司官网
    { wch: 30 }, // 地址
    { wch: 20 }, // 微信名称/ID
    { wch: 20 }, // WhatsApp名称/号码
    { wch: 20 }, // 会面时间
    { wch: 18 }, // 会面地点
    { wch: 15 }, // 跟进状态
    { wch: 10 }, // 优先级
    { wch: 30 }  // 备注
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '客户数据')
  
  // 添加说明页
  const instructionData = [
    { '字段说明': '【重要说明】', '是否必填': '', '格式要求': '标注"至少一项"的字段，需要至少填写其中一项（邮箱、手机号、公司名称、微信、WhatsApp任选一项）' },
    { '字段说明': '客户姓名', '是否必填': '是', '格式要求': '文本，最大50字符' },
    { '字段说明': '邮箱', '是否必填': '至少一项', '格式要求': '有效的邮箱格式' },
    { '字段说明': '手机号', '是否必填': '至少一项', '格式要求': '文本，可包含国家代码' },
    { '字段说明': '所属公司', '是否必填': '至少一项', '格式要求': '文本，最大100字符' },
    { '字段说明': '职位', '是否必填': '否', '格式要求': '文本，最大50字符' },
    { '字段说明': '国家', '是否必填': '否', '格式要求': '文本，如：美国、德国、英国' },
    { '字段说明': '公司官网', '是否必填': '否', '格式要求': 'URL格式，以http://或https://开头' },
    { '字段说明': '地址', '是否必填': '否', '格式要求': '文本，最大200字符' },
    { '字段说明': '微信名称/ID', '是否必填': '至少一项', '格式要求': '文本，微信号或微信昵称' },
    { '字段说明': 'WhatsApp名称/号码', '是否必填': '至少一项', '格式要求': '文本，WhatsApp号码' },
    { '字段说明': '会面时间', '是否必填': '否', '格式要求': '日期时间格式，如：2025-01-15 14:00:00' },
    { '字段说明': '会面地点', '是否必填': '否', '格式要求': '文本，如：广交会A馆' },
    { '字段说明': '跟进状态', '是否必填': '否', '格式要求': 'pending_us=等待我们回复, pending_customer=等待客户回复, completed=已完成' },
    { '字段说明': '优先级', '是否必填': '否', '格式要求': '数字1-3，1最高3最低，默认2' },
    { '字段说明': '备注', '是否必填': '否', '格式要求': '文本，最大500字符' }
  ]
  const wsInstruction = XLSX.utils.json_to_sheet(instructionData)
  wsInstruction['!cols'] = [{ wch: 15 }, { wch: 10 }, { wch: 50 }]
  XLSX.utils.book_append_sheet(wb, wsInstruction, '填写说明')
  
  // 下载文件
  XLSX.writeFile(wb, '客户导入模板.xlsx')
  message.success('模板下载成功')
}

// 选择文件
const handleFileSelect = (file: File) => {
  console.log('🔥 新代码已加载 - 选择文件:', file.name)
  if (file.size > 500 * 1024 * 1024) {
    message.error('文件大小不能超过 500MB')
    return false
  }
  // 清理之前的导入数据
  console.log('🧹 清理旧数据...')
  importId.value = ''
  importResult.value = null
  validationResult.value = null
  importStatus.value = null
  mappingData.value = []
  
  selectedFile.value = file
  message.success('文件已选择，请点击"下一步"')
  return false
}

// 上传文件
const uploadFile = async () => {
  if (!selectedFile.value) return
  
  console.log('📤 开始上传文件:', selectedFile.value.name)
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    
    const res = await customerApi.uploadImportFile(formData)
    console.log('✅ 上传成功，后端返回数据:', res.data)
    console.log('📋 文件名:', res.data.fileName)
    console.log('📊 表头:', res.data.headers)
    
    importId.value = res.data.importId
    importResult.value = res.data
    
    // 构建映射数据
    mappingData.value = res.data.headers.map((header) => ({
      excelColumn: header,
      systemField: res.data.suggestedMapping[header] || '',
      previewValue: res.data.previewData[0]?.[header] || ''
    }))
    
    console.log('🗺️ 映射数据已构建:', mappingData.value)
    currentStep.value = 1
    message.success('文件上传成功！')
  } catch (error) {
    console.error('❌ 上传失败:', error)
    message.error('上传失败')
  } finally {
    uploading.value = false
  }
}

// 验证映射
const validateMapping = async () => {
  // 检查必填字段
  const mappedFields = mappingData.value.map(m => m.systemField).filter(Boolean)
  const requiredFields = systemFields.filter(f => f.required).map(f => f.key)
  const missingFields = requiredFields.filter(f => !mappedFields.includes(f))
  
  if (missingFields.length > 0) {
    const labels = missingFields.map(f => systemFields.find(sf => sf.key === f)?.label).join('、')
    message.error(`请映射必填字段：${labels}`)
    return
  }
  
  validating.value = true
  try {
    const fieldMapping: Record<string, string> = {}
    mappingData.value.forEach(m => {
      if (m.systemField) {
        fieldMapping[m.excelColumn] = m.systemField
      }
    })
    
    const res = await customerApi.validateImport(importId.value, {
      fieldMapping,
      importMode: importMode.value
    })
    
    validationResult.value = res.data
    currentStep.value = 2
  } catch {
    message.error('数据预检失败')
  } finally {
    validating.value = false
  }
}

// 执行导入
const executeImport = async () => {
  importing.value = true
  try {
    const res = await customerApi.executeImport(importId.value, {
      importMode: importMode.value,
      duplicateAction: duplicateAction.value
    })
    
    // 同步执行完成，直接使用返回的状态
    importStatus.value = res.data
    importing.value = false
    currentStep.value = 3
    
    if (res.data.status === 'completed') {
      message.success(`导入完成！成功 ${res.data.successCount} 条`)
    }
  } catch {
    message.error('导入失败')
    importing.value = false
  }
}

// 轮询导入状态
const pollImportStatus = async () => {
  const poll = async () => {
    try {
      const res = await customerApi.getImportStatus(importId.value)
      importStatus.value = res.data
      
      if (res.data.status === 'processing') {
        setTimeout(poll, 2000)
      } else {
        importing.value = false
        currentStep.value = 3
      }
    } catch {
      importing.value = false
    }
  }
  
  poll()
}

// 下载错误报告
const downloadErrors = () => {
  if (!validationResult.value || !validationResult.value.errors || validationResult.value.errors.length === 0) {
    message.warning('没有错误数据可以下载')
    return
  }
  
  // 如果后端提供了错误文件URL，直接下载
  if (validationResult.value.errorFileUrl) {
    window.open(validationResult.value.errorFileUrl, '_blank')
    return
  }
  
  // 否则前端生成错误报告Excel
  try {
    const errorData = validationResult.value.errors.map(error => ({
      '行号': error.row,
      '字段': error.field,
      '错误值': error.value || '',
      '错误信息': error.message
    }))
    
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(errorData)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 10 }, // 行号
      { wch: 25 }, // 字段
      { wch: 35 }, // 错误值
      { wch: 50 }  // 错误信息
    ]
    
    XLSX.utils.book_append_sheet(wb, ws, '错误详情')
    
    // 下载文件
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[:-]/g, '').replace('T', '_')
    XLSX.writeFile(wb, `导入错误报告_${timestamp}.xlsx`)
    message.success('错误报告下载成功')
  } catch (error) {
    console.error('生成错误报告失败:', error)
    message.error('生成错误报告失败')
  }
}

// 下载导入日志
const downloadLog = () => {
  if (importStatus.value?.logFileUrl) {
    window.open(importStatus.value.logFileUrl, '_blank')
  }
}

// 返回上传步骤（清理数据）
const goBackToUpload = () => {
  currentStep.value = 0
  // 清理导入相关数据，但保留已选择的文件
  importId.value = ''
  importResult.value = null
  validationResult.value = null
  importStatus.value = null
  mappingData.value = []
}

// 重置导入
const resetImport = () => {
  currentStep.value = 0
  selectedFile.value = null
  importId.value = ''
  importResult.value = null
  validationResult.value = null
  importStatus.value = null
  mappingData.value = []
}
</script>

<style lang="less" scoped>
.customer-import-page {
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
  
  .import-steps {
    margin-bottom: 32px;
    padding: 24px;
    background: #fff;
    border-radius: 8px;
  }
  
  .step-content {
    min-height: 400px;
  }
  
  .step-actions {
    margin-top: 32px;
    display: flex;
    justify-content: center;
    gap: 16px;
  }
  
  .step-upload {
    .upload-tips {
      margin-bottom: 24px;
      padding: 16px;
      background: #f6f8fa;
      border-radius: 8px;
      
      h4 {
        margin: 0 0 12px;
        font-weight: 600;
      }
      
      ul {
        margin: 0 0 12px;
        padding-left: 20px;
        color: #6b7280;
        
        li {
          margin-bottom: 4px;
        }
      }
    }
    
    .upload-area {
      max-width: 500px;
      margin: 0 auto;
    }
    
    .selected-file {
      max-width: 500px;
      margin: 16px auto 0;
      padding: 12px 16px;
      background: #f0f5ff;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
      
      .file-icon {
        font-size: 24px;
        color: #52c41a;
      }
      
      .file-name {
        flex: 1;
        font-weight: 500;
      }
      
      .file-size {
        color: #6b7280;
      }
    }
  }
  
  .step-mapping {
    .import-options {
      margin-top: 24px;
      padding: 16px;
      background: #f6f8fa;
      border-radius: 8px;
    }
  }
  
  .step-validation {
    .validation-summary {
      margin-bottom: 24px;
      padding: 24px;
      background: #f6f8fa;
      border-radius: 8px;
    }
    
    .error-list {
      h4 {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        font-weight: 600;
      }
      
      .more-errors {
        margin-top: 8px;
        color: #6b7280;
        font-size: 13px;
      }
    }
  }
  
  .step-result {
    .result-stats {
      margin-bottom: 24px;
    }
    
    .result-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
    }
  }
}
</style>

