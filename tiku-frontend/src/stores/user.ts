import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { User, LoginRequest, RegisterRequest } from '@/types'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isStudent = computed(() => userInfo.value?.role === 'STUDENT')

  // Actions
  async function login(loginData: LoginRequest) {
    const res = await authApi.login(loginData)
    console.log('🔍 登录响应原始数据:', res.data)
    
    token.value = res.data.token
    
    // 构造用户信息对象
    const user: User = {
      id: res.data.userId,
      username: res.data.username,
      email: '',
      realName: res.data.realName,
      role: res.data.role as any,
      avatar: res.data.avatar,
      status: 1,
      createdAt: new Date().toISOString()
    }
    
    console.log('✅ 构造的用户对象:', user)
    console.log('📌 realName 字段值:', res.data.realName)
    
    userInfo.value = user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(user))
    
    console.log('💾 已保存到 localStorage')
  }

  async function register(registerData: RegisterRequest) {
    const res = await authApi.register(registerData)
    return res.data
  }

  async function getUserInfo() {
    const res = await authApi.getCurrentUser()
    
    // 构造用户信息对象
    const user: User = {
      id: res.data.userId,
      username: res.data.username,
      email: '',
      realName: res.data.realName,
      role: res.data.role as any,
      avatar: res.data.avatar,
      status: 1,
      createdAt: new Date().toISOString()
    }
    
    userInfo.value = user
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function initFromStorage() {
    const storedToken = localStorage.getItem('token')
    const storedUserInfo = localStorage.getItem('userInfo')
    
    if (storedToken && storedToken !== 'undefined' && storedToken !== 'null') {
      token.value = storedToken
    }
    
    if (storedUserInfo && storedUserInfo !== 'undefined' && storedUserInfo !== 'null') {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('Failed to parse user info:', e)
        // 清理无效数据
        localStorage.removeItem('userInfo')
      }
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    register,
    getUserInfo,
    logout,
    initFromStorage
  }
})

