import request from '../request'
import type { WrongQuestion, PageResponse } from '@/types'

/**
 * 错题相关API
 */
export const wrongApi = {
  // 获取我的错题列表（分页）
  getMyWrongQuestions(params?: { page?: number; size?: number }) {
    return request.get<PageResponse<WrongQuestion>>('/wrong-questions', { params })
  },

  // 获取最近错题（不分页，用于Dashboard）
  getRecentWrongQuestions(limit = 5) {
    return request.get<PageResponse<WrongQuestion>>('/wrong-questions', { 
      params: { page: 0, size: limit } 
    })
  },

  // 标记为已掌握
  markAsMastered(questionId: number) {
    return request.put(`/wrong-questions/${questionId}/master`)
  },

  // 从错题本删除
  remove(questionId: number) {
    return request.delete(`/wrong-questions/${questionId}`)
  },

  // 获取错题统计
  getStats() {
    return request.get<any>('/wrong-questions/stats')
  },

  // 获取错题数量
  getCount() {
    return request.get<number>('/wrong-questions/count')
  }
}



