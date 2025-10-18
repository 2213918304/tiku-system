import request from '../request'

/**
 * 错题本相关API
 */
export const wrongQuestionApi = {
  // 获取错题列表（分页）
  getMyWrongQuestions(params?: { 
    page?: number
    size?: number
    subjectId?: number
    chapterId?: number
    type?: string
    status?: string
  }) {
    return request.get<any>('/wrong-questions', { params })
  },

  // 获取所有错题（不分页）
  getAllMyWrongQuestions() {
    return request.get<any[]>('/wrong-questions/all')
  },

  // 获取错题统计
  getStats() {
    return request.get<{totalWrong: number; needReview: number; mastered: number}>('/wrong-questions/stats')
  },

  // 标记为已掌握
  markAsMastered(questionId: number) {
    return request.put(`/wrong-questions/${questionId}/mastered`)
  },

  // 从错题本移除
  remove(questionId: number) {
    return request.delete(`/wrong-questions/${questionId}`)
  },

  // 批量移除
  batchRemove(questionIds: number[]) {
    return request.delete('/wrong-questions/batch', { data: questionIds })
  }
}




