import request from '../request'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User
} from '@/types'

/**
 * 认证相关API
 */
export const authApi = {
  // 登录
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/auth/login', data)
  },

  // 注册
  register(data: RegisterRequest) {
    return request.post<User>('/auth/register', data)
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get<LoginResponse>('/auth/current')
  }
}

