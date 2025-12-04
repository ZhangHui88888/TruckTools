import { http } from '@/utils/request'
import type { UserInfo } from '@/stores/user'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface RegisterParams {
  username: string
  email: string
  password: string
  confirmPassword: string
}

export const authApi = {
  // 登录
  login(data: LoginParams) {
    return http.post<LoginResult>('/auth/login', data)
  },

  // 注册
  register(data: RegisterParams) {
    return http.post<{ userId: string }>('/auth/register', data)
  },

  // 刷新Token
  refreshToken(data: { refreshToken: string }) {
    return http.post<{ accessToken: string; refreshToken: string }>('/auth/refresh', data)
  },

  // 退出登录
  logout() {
    return http.post('/auth/logout')
  },

  // 发送密码重置邮件
  forgotPassword(data: { email: string }) {
    return http.post('/auth/forgot-password', data)
  },

  // 重置密码
  resetPassword(data: { token: string; newPassword: string; confirmPassword: string }) {
    return http.post('/auth/reset-password', data)
  },

  // 获取个人资料
  getProfile() {
    return http.get<UserInfo>('/user/profile')
  },

  // 更新个人资料
  updateProfile(data: Partial<UserInfo>) {
    return http.put('/user/profile', data)
  },

  // 上传头像
  uploadAvatar(formData: FormData) {
    return http.upload<{ avatarUrl: string }>('/user/avatar', formData)
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
    return http.put('/user/password', data)
  }
}

