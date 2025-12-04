<template>
  <a-modal
    :open="open"
    :title="customer ? '编辑客户' : '添加客户'"
    :width="900"
    :confirm-loading="loading"
    @update:open="$emit('update:open', $event)"
    @ok="handleSubmit"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="客户姓名" name="name">
            <a-input v-model:value="formState.name" placeholder="请输入客户姓名" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" name="priority">
            <a-radio-group v-model:value="formState.priority" button-style="solid">
              <a-radio-button :value="1">高</a-radio-button>
              <a-radio-button :value="2">中</a-radio-button>
              <a-radio-button :value="3">低</a-radio-button>
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
          <a-form-item label="国家" name="country">
            <a-select
              v-model:value="formState.country"
              placeholder="请选择国家"
              show-search
              allow-clear
            >
              <a-select-option v-for="c in countries" :key="c" :value="c">{{ c }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="公司官网" name="website">
            <a-input v-model:value="formState.website" placeholder="https://" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="会面时间" name="meetingTime">
            <a-date-picker
              v-model:value="formState.meetingTime"
              style="width: 100%"
              placeholder="选择会面日期"
              format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="会面地点" name="meetingLocation">
            <a-input v-model:value="formState.meetingLocation" placeholder="如：广交会A馆" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="微信名称" name="wechatName">
            <a-input v-model:value="formState.wechatName" placeholder="微信昵称或ID" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="WhatsApp" name="whatsappName">
            <a-input v-model:value="formState.whatsappName" placeholder="WhatsApp号码" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="地址" name="address" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <a-input v-model:value="formState.address" placeholder="请输入详细地址" />
      </a-form-item>

      <a-form-item label="备注" name="remark" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <a-textarea
          v-model:value="formState.remark"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'
import { customerApi } from '@/api/customer'
import type { Customer } from '@/api/customer'

interface Props {
  open: boolean
  customer?: Customer | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:open': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive<{
  name: string
  email: string
  phone: string
  company: string
  position: string
  country: string | undefined
  website: string
  address: string
  meetingTime: ReturnType<typeof dayjs> | null
  meetingLocation: string
  wechatName: string
  whatsappName: string
  remark: string
  priority: number
}>({
  name: '',
  email: '',
  phone: '',
  company: '',
  position: '',
  country: undefined,
  website: '',
  address: '',
  meetingTime: null,
  meetingLocation: '',
  wechatName: '',
  whatsappName: '',
  remark: '',
  priority: 2
})

const rules: Record<string, any[]> = {
  name: [{ required: true, message: '请输入客户姓名' }],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 全球国家列表（按外贸重要性排序）
const countries = [
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

// 监听 customer 变化，填充表单
watch(
  () => props.customer,
  (val) => {
    if (val) {
      formState.name = val.name || ''
      formState.email = val.email || ''
      formState.phone = val.phone || ''
      formState.company = val.company || ''
      formState.position = val.position || ''
      formState.country = val.country || undefined
      formState.website = val.website || ''
      formState.address = val.address || ''
      formState.meetingTime = val.meetingTime ? dayjs(val.meetingTime) : null
      formState.meetingLocation = val.meetingLocation || ''
      formState.wechatName = val.wechatName || ''
      formState.whatsappName = val.whatsappName || ''
      formState.remark = val.remark || ''
      formState.priority = val.priority || 2
    } else {
      resetForm()
    }
  }
)

// 重置表单
const resetForm = () => {
  formState.name = ''
  formState.email = ''
  formState.phone = ''
  formState.company = ''
  formState.position = ''
  formState.country = undefined
  formState.website = ''
  formState.address = ''
  formState.meetingTime = null
  formState.meetingLocation = ''
  formState.wechatName = ''
  formState.whatsappName = ''
  formState.remark = ''
  formState.priority = 2
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const data = {
      name: formState.name,
      email: formState.email,
      phone: formState.phone || undefined,
      company: formState.company || undefined,
      position: formState.position || undefined,
      country: formState.country,
      website: formState.website || undefined,
      address: formState.address || undefined,
      meetingTime: formState.meetingTime?.format('YYYY-MM-DD') || undefined,
      meetingLocation: formState.meetingLocation || undefined,
      wechatName: formState.wechatName || undefined,
      whatsappName: formState.whatsappName || undefined,
      remark: formState.remark || undefined,
      priority: formState.priority
    }

    if (props.customer) {
      await customerApi.update(props.customer.id, data)
      message.success('更新成功')
    } else {
      await customerApi.create(data)
      message.success('添加成功')
    }
    
    emit('success')
  } catch {
    // 表单验证失败或API错误
  } finally {
    loading.value = false
  }
}
</script>

