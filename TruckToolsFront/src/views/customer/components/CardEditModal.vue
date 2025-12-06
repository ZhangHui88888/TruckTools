<template>
  <a-modal
    :open="open"
    title="编辑识别结果"
    :width="900"
    :confirm-loading="loading"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
  >
    <div v-if="card" class="card-edit-content">
      <div class="card-preview">
        <img :src="card.imageUrl" alt="名片" />
      </div>
      
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        class="edit-form"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="客户姓名" name="name">
              <a-input v-model:value="formState.name" placeholder="请输入客户姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-radio-group v-model:value="formState.priority" button-style="solid">
                <a-radio-button :value="0">T0</a-radio-button>
                <a-radio-button :value="1">T1</a-radio-button>
                <a-radio-button :value="2">T2</a-radio-button>
                <a-radio-button :value="3">T3</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formState.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号" name="phone">
              <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="公司" name="company">
              <a-input v-model:value="formState.company" placeholder="请输入公司名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职位" name="position">
              <a-input v-model:value="formState.position" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="网站" name="website">
              <a-input v-model:value="formState.website" placeholder="请输入公司官网" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="国家" name="country">
              <a-select
                v-model:value="formState.country"
                placeholder="请选择国家"
                show-search
                allow-clear
              >
                <a-select-option v-for="c in countries" :key="c" :value="c">
                  {{ c }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="地址" name="address">
          <a-input v-model:value="formState.address" placeholder="请输入地址" />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { customerApi } from '@/api/customer'
import type { BusinessCard } from '@/api/customer'

interface Props {
  open: boolean
  card: BusinessCard | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  'success': [customerId: string]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive({
  name: '',
  email: '',
  phone: '',
  company: '',
  position: '',
  website: '',
  address: '',
  country: undefined as string | undefined,
  priority: 1
})

const rules = {
  name: [{ required: true, message: '请输入客户姓名' }],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 全球国家列表（按外贸重要性排序）
const countries = [
  // 中国及港澳台
  '中国', '香港', '台湾', '澳门',
  // 亚洲主要国家
  '日本', '韩国', '印度', '新加坡', '马来西亚', '泰国', '越南', '印度尼西亚', '菲律宾',
  '巴基斯坦', '孟加拉国', '斯里兰卡', '缅甸', '柬埔寨', '老挝', '文莱', '蒙古',
  // 中东地区
  '阿联酋', '沙特阿拉伯', '以色列', '土耳其', '伊朗', '伊拉克', '约旦', '科威特',
  '卡塔尔', '阿曼', '巴林', '黎巴嫩', '叙利亚', '也门',
  // 欧洲主要国家
  '德国', '英国', '法国', '意大利', '西班牙', '荷兰', '比利时', '瑞士', '瑞典',
  '波兰', '奥地利', '丹麦', '芬兰', '挪威', '葡萄牙', '希腊', '捷克', '爱尔兰',
  '匈牙利', '罗马尼亚', '乌克兰', '俄罗斯', '白俄罗斯', '保加利亚', '塞尔维亚',
  '克罗地亚', '斯洛伐克', '斯洛文尼亚', '立陶宛', '拉脱维亚', '爱沙尼亚',
  // 北美洲
  '美国', '加拿大', '墨西哥',
  // 南美洲
  '巴西', '阿根廷', '智利', '秘鲁', '哥伦比亚', '委内瑞拉', '厄瓜多尔', '乌拉圭',
  '巴拉圭', '玻利维亚',
  // 大洋洲
  '澳大利亚', '新西兰', '巴布亚新几内亚', '斐济',
  // 非洲主要国家
  '南非', '埃及', '尼日利亚', '肯尼亚', '埃塞俄比亚', '加纳', '坦桑尼亚', '乌干达',
  '摩洛哥', '阿尔及利亚', '突尼斯', '安哥拉', '赞比亚', '津巴布韦', '博茨瓦纳',
  // 其他
  '其他'
]

// 监听 card 变化，填充表单
watch(
  () => props.card,
  (val) => {
    if (val && val.parsedData) {
      formState.name = val.parsedData.name || ''
      formState.email = val.parsedData.email || ''
      formState.phone = val.parsedData.phone || ''
      formState.company = val.parsedData.company || ''
      formState.position = val.parsedData.position || ''
      formState.website = val.parsedData.website || ''
      formState.address = val.parsedData.address || ''
      formState.country = undefined
      formState.priority = val.priority ?? 1
    }
  },
  { immediate: true }
)

// 提交
const handleSubmit = async () => {
  if (!props.card) return
  
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const res = await customerApi.confirmCard(props.card.id, {
      name: formState.name,
      email: formState.email,
      phone: formState.phone || undefined,
      company: formState.company || undefined,
      position: formState.position || undefined,
      website: formState.website || undefined,
      address: formState.address || undefined,
      country: formState.country,
      priority: formState.priority
    })
    
    emit('success', res.data.customerId)
  } catch {
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="less" scoped>
.card-edit-content {
  display: flex;
  gap: 24px;
}

.card-preview {
  flex-shrink: 0;
  width: 200px;
  
  img {
    width: 100%;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
  }
}

.edit-form {
  flex: 1;
}
</style>

