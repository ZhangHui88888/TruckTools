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

      <!-- 名片上传（仅编辑时显示） -->
      <a-form-item v-if="customer" label="名片" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <div class="business-card-upload">
          <div class="card-item">
            <div class="card-label">正面</div>
            <div class="card-upload-area" @click="triggerCardUpload('front')">
              <img v-if="formState.businessCardFront" :src="formState.businessCardFront" alt="名片正面" />
              <div v-else class="upload-placeholder">
                <PlusOutlined style="font-size: 20px; color: #999" />
                <div style="margin-top: 4px; color: #999; font-size: 12px">上传正面</div>
              </div>
              <div v-if="formState.businessCardFront" class="card-overlay">
                <span>更换</span>
              </div>
            </div>
          </div>
          <div class="card-item">
            <div class="card-label">背面</div>
            <div class="card-upload-area" @click="triggerCardUpload('back')">
              <img v-if="formState.businessCardBack" :src="formState.businessCardBack" alt="名片背面" />
              <div v-else class="upload-placeholder">
                <PlusOutlined style="font-size: 20px; color: #999" />
                <div style="margin-top: 4px; color: #999; font-size: 12px">上传背面</div>
              </div>
              <div v-if="formState.businessCardBack" class="card-overlay">
                <span>更换</span>
              </div>
            </div>
          </div>
          <input
            ref="cardInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleCardSelect"
          />
        </div>
        <div class="upload-tip">支持 jpg/png/gif/webp 格式</div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
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

// 名片上传
const cardInputRef = ref<HTMLInputElement | null>(null)
const currentCardSide = ref<'front' | 'back'>('front')

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
  businessCardFront: string
  businessCardBack: string
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
  priority: 1,
  businessCardFront: '',
  businessCardBack: ''
})

const rules: Record<string, any[]> = {
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
  '阿联酋', '迪拜', '沙特阿拉伯', '以色列', '土耳其', '伊朗', '伊拉克', '约旦', '科威特',
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
      formState.priority = val.priority ?? 1
      formState.businessCardFront = val.businessCardFront || ''
      formState.businessCardBack = val.businessCardBack || ''
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
  formState.priority = 1
  formState.businessCardFront = ''
  formState.businessCardBack = ''
}

// 触发名片上传
const triggerCardUpload = (side: 'front' | 'back') => {
  currentCardSide.value = side
  cardInputRef.value?.click()
}

// 处理名片选择
const handleCardSelect = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !props.customer) return

  // 验证文件大小（最大 10MB）
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片大小不能超过 10MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await customerApi.uploadBusinessCard(props.customer.id, currentCardSide.value, formData)
    if (res && res.data) {
      if (currentCardSide.value === 'front') {
        formState.businessCardFront = res.data.imageUrl
      } else {
        formState.businessCardBack = res.data.imageUrl
      }
      message.success('名片上传成功')
    }
  } catch {
    message.error('名片上传失败')
  } finally {
    // 清空 input 以便可以再次选择同一文件
    input.value = ''
  }
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

<style lang="less" scoped>
.business-card-upload {
  display: flex;
  gap: 16px;

  .card-item {
    .card-label {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
      text-align: center;
    }

    .card-upload-area {
      width: 160px;
      height: 100px;
      border: 1px dashed #d9d9d9;
      border-radius: 8px;
      overflow: hidden;
      position: relative;
      cursor: pointer;
      transition: border-color 0.3s;

      &:hover {
        border-color: #1677ff;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .upload-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #fafafa;
      }

      .card-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        color: #fff;
        font-size: 14px;
      }

      &:hover .card-overlay {
        opacity: 1;
      }
    }
  }
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>

