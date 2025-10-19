import request from '../request'

/**
 * 排行榜相关API
 */
export const rankingApi = {
  // 答题数排行榜
  getAnswerCountRanking(limit: number = 100) {
    return request.get<any[]>('/ranking/answer-count', { params: { limit } })
  },

  // 正确率排行榜
  getAccuracyRanking(limit: number = 100) {
    return request.get<any[]>('/ranking/accuracy', { params: { limit } })
  },

  // 积分排行榜
  getPointsRanking(limit: number = 100) {
    return request.get<any[]>('/ranking/points', { params: { limit } })
  },

  // 学科排行榜
  getSubjectRanking(subjectId: number, limit: number = 100) {
    return request.get<any[]>(`/ranking/subject/${subjectId}`, { params: { limit } })
  }
}






