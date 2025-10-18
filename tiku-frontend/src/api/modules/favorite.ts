import request from '../request'
import type { Favorite } from '@/types'

/**
 * 收藏相关API
 */
export const favoriteApi = {
  // 获取我的收藏列表（分页）
  getMyFavorites(params?: { page?: number; size?: number }) {
    return request.get<any>('/favorites', { params })
  },

  // 获取我的所有收藏（不分页）
  getAllMyFavorites() {
    return request.get<any[]>('/favorites/all')
  },

  // 获取收藏统计
  getStats() {
    return request.get<{totalCount: number; weekCount: number; practicedCount: number}>('/favorites/stats')
  },

  // 添加收藏
  add(data: { questionId: number; category?: string; remark?: string }) {
    return request.post<Favorite>('/favorites', data)
  },

  // 添加收藏（兼容旧方法名）
  addFavorite(questionId: number) {
    return request.post<Favorite>('/favorites', { questionId })
  },

  // 取消收藏
  remove(questionId: number) {
    return request.delete(`/favorites/${questionId}`)
  },

  // 取消收藏（兼容旧方法名）
  removeFavorite(questionId: number) {
    return request.delete(`/favorites/${questionId}`)
  },

  // 检查是否已收藏
  checkFavorite(questionId: number) {
    return request.get<boolean>(`/favorites/check/${questionId}`)
  },

  // 批量检查收藏状态
  checkBatch(questionIds: number[]) {
    return request.post<Record<number, any>>('/favorites/check/batch', { questionIds })
  }
}



