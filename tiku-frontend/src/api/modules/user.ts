import request from '../request'
import type { User } from '@/types'

export interface StudyPlan {
  id: number
  userId: number
  subjectId: number
  subjectName?: string
  name: string
  description?: string
  dailyTarget: number
  startDate: string
  endDate: string
  completedDays: number
  totalDays: number
  targetCount: number
  completedCount: number
  progress: number
  status: string
}

export interface CreateStudyPlanRequest {
  subjectId: number
  description: string
  targetCount: number
  totalDays: number
}

/**
 * 用户相关API
 */
export const userApi = {
  // 获取用户信息
  getProfile() {
    return request.get<User>('/user/profile')
  },

  // 更新用户信息
  updateProfile(data: { realName?: string; email?: string; phone?: string; bio?: string }) {
    return request.put<User>('/user/profile', data)
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.put('/user/password', data)
  },

  // 获取用户成就
  getAchievements() {
    return request.get<any[]>('/user/achievements')
  },

  // 获取学习计划
  getStudyPlans() {
    return request.get<StudyPlan[]>('/user/study-plans')
  },

  // 创建学习计划
  createStudyPlan(data: CreateStudyPlanRequest) {
    return request.post<StudyPlan>('/user/study-plans', data)
  },

  // 更新学习计划
  updateStudyPlan(id: number, data: CreateStudyPlanRequest) {
    return request.put<StudyPlan>(`/user/study-plans/${id}`, data)
  },

  // 打卡
  checkIn() {
    return request.post('/statistics/check-in')
  }
}

