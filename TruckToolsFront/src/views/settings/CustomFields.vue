<template>
  <div class="custom-fields-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">自定义字段</h1>
        <p class="page-subtitle">为客户添加自定义字段，可用于邮件模板变量</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showAddModal">
          <PlusOutlined />
          添加字段
        </a-button>
      </div>
    </div>

    <!-- 字段列表 -->
    <div class="fields-section content-card">
      <a-table
        :data-source="fields"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fieldType'">
            <a-tag>{{ fieldTypeLabels[record.fieldType] }}</a-tag>
          </template>
          <template v-else-if="column.key === 'isRequired'">
            <a-tag v-if="record.isRequired" color="red">必填</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'variable'">
            <code v-text="'{{' + record.fieldKey + '}}'" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm title="确定删除此字段吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="currentField ? '编辑字段' : '添加字段'"
      :width="500"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="字段键名" name="fieldKey">
          <a-input
            v-model:value="formState.fieldKey"
            placeholder="英文标识，如 productInterest"
            :disabled="!!currentField"
          />
        </a-form-item>
        <a-form-item label="显示名称" name="fieldName">
          <a-input v-model:value="formState.fieldName" placeholder="如：感兴趣产品" />
        </a-form-item>
        <a-form-item label="字段类型" name="fieldType">
          <a-select v-model:value="formState.fieldType">
            <a-select-option value="text">文本</a-select-option>
            <a-select-option value="number">数字</a-select-option>
            <a-select-option value="date">日期</a-select-option>
            <a-select-option value="select">下拉选择</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="formState.fieldType === 'select'" label="选项值" name="fieldOptions">
          <a-textarea
            v-model:value="formState.optionsText"
            placeholder="每行一个选项"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="默认值" name="defaultValue">
          <a-input v-model:value="formState.defaultValue" placeholder="可选" />
        </a-form-item>
        <a-form-item label="是否必填" name="isRequired">
          <a-switch v-model:checked="formState.isRequired" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { http } from '@/utils/request'

interface CustomField {
  id: string
  fieldKey: string
  fieldName: string
  fieldType: 'text' | 'number' | 'date' | 'select'
  fieldOptions?: string[]
  isRequired: boolean
  defaultValue?: string
  sortOrder: number
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const fields = ref<CustomField[]>([])
const currentField = ref<CustomField | null>(null)

const formState = reactive({
  fieldKey: '',
  fieldName: '',
  fieldType: 'text' as 'text' | 'number' | 'date' | 'select',
  optionsText: '',
  defaultValue: '',
  isRequired: false
})

const rules = {
  fieldKey: [
    { required: true, message: '请输入字段键名' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '只能包含字母、数字和下划线，且以字母开头' }
  ],
  fieldName: [{ required: true, message: '请输入显示名称' }],
  fieldType: [{ required: true, message: '请选择字段类型' }]
}

const fieldTypeLabels: Record<string, string> = {
  text: '文本',
  number: '数字',
  date: '日期',
  select: '下拉选择'
}

const columns = [
  { title: '字段键名', dataIndex: 'fieldKey', key: 'fieldKey' },
  { title: '显示名称', dataIndex: 'fieldName', key: 'fieldName' },
  { title: '类型', key: 'fieldType' },
  { title: '是否必填', key: 'isRequired' },
  { title: '邮件变量', key: 'variable' },
  { title: '操作', key: 'action', width: 150 }
]

// 获取字段列表
const fetchFields = async () => {
  loading.value = true
  try {
    const res = await http.get<CustomField[]>('/custom-fields')
    fields.value = res.data
  } catch {
    message.error('获取字段列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加弹窗
const showAddModal = () => {
  currentField.value = null
  resetForm()
  showModal.value = true
}

// 编辑
const handleEdit = (field: CustomField) => {
  currentField.value = field
  formState.fieldKey = field.fieldKey
  formState.fieldName = field.fieldName
  formState.fieldType = field.fieldType
  formState.optionsText = field.fieldOptions?.join('\n') || ''
  formState.defaultValue = field.defaultValue || ''
  formState.isRequired = field.isRequired
  showModal.value = true
}

// 重置表单
const resetForm = () => {
  formState.fieldKey = ''
  formState.fieldName = ''
  formState.fieldType = 'text'
  formState.optionsText = ''
  formState.defaultValue = ''
  formState.isRequired = false
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    const data = {
      fieldKey: formState.fieldKey,
      fieldName: formState.fieldName,
      fieldType: formState.fieldType,
      fieldOptions: formState.fieldType === 'select' 
        ? formState.optionsText.split('\n').filter(Boolean)
        : undefined,
      defaultValue: formState.defaultValue || undefined,
      isRequired: formState.isRequired
    }
    
    if (currentField.value) {
      await http.put(`/custom-fields/${currentField.value.id}`, data)
      message.success('更新成功')
    } else {
      await http.post('/custom-fields', data)
      message.success('添加成功')
    }
    
    showModal.value = false
    fetchFields()
  } catch {
    // 验证失败
  } finally {
    saving.value = false
  }
}

// 删除
const handleDelete = async (field: CustomField) => {
  try {
    await http.delete(`/custom-fields/${field.id}`)
    message.success('删除成功')
    fetchFields()
  } catch {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchFields()
})
</script>

<style lang="less" scoped>
.custom-fields-page {
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
  
  code {
    padding: 2px 8px;
    background: #f3f4f6;
    border-radius: 4px;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 13px;
  }
}
</style>

