import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export interface UserInfo {
  id: string
  username: string
  email: string
  nickname?: string
  avatar?: string
  phone?: string
}

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string>('')
    const refreshToken = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username || '')
    const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
    const avatar = computed(() => userInfo.value?.avatar || '')

    // 登录
    const login = async (username: string, password: string) => {
      try {
        const res = await authApi.login({ username, password })
        if (res.code === 200) {
          token.value = res.data.accessToken
          refreshToken.value = res.data.refreshToken
          userInfo.value = res.data.user
          localStorage.setItem('token', res.data.accessToken)
          return true
        }
        return false
      } catch {
        return false
      }
    }

    // 注册
    const register = async (data: {
      username: string
      email: string
      password: string
      confirmPassword: string
    }) => {
      try {
        const res = await authApi.register(data)
        return res.code === 200
      } catch {
        return false
      }
    }

    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        const res = await authApi.getProfile()
        if (res.code === 200) {
          userInfo.value = res.data
        }
      } catch {
        // 获取失败时清除登录状态
        logout()
      }
    }

    // 更新用户信息
    const updateProfile = async (data: Partial<UserInfo>) => {
      try {
        const res = await authApi.updateProfile(data)
        if (res.code === 200) {
          userInfo.value = { ...userInfo.value, ...data } as UserInfo
          return true
        }
        return false
      } catch {
        return false
      }
    }

    // 退出登录
    const logout = () => {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
    }

    // 刷新token
    const refreshAccessToken = async () => {
      if (!refreshToken.value) return false
      try {
        const res = await authApi.refreshToken({ refreshToken: refreshToken.value })
        if (res.code === 200) {
          token.value = res.data.accessToken
          refreshToken.value = res.data.refreshToken
          localStorage.setItem('token', res.data.accessToken)
          return true
        }
        return false
      } catch {
        logout()
        return false
      }
    }

    return {
      token,
      refreshToken,
      userInfo,
      isLoggedIn,
      username,
      nickname,
      avatar,
      login,
      register,
      fetchUserInfo,
      updateProfile,
      logout,
      refreshAccessToken
    }
  },
  {
    persist: {
      paths: ['token', 'refreshToken', 'userInfo']
    }
  }
)

