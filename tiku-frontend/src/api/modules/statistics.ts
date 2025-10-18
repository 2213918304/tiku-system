import request from '../request'

/**
 * 统计分析相关API
 */
export const statisticsApi = {
  // 获取我的学习统计
  getMyStatistics() {
    return request.get<any>('/statistics/my')
  },

  // 获取学科统计
  getSubjectStatistics() {
    return request.get<any[]>('/statistics/subjects')
  },

  // 获取章节统计
  getChapterStatistics(subjectId: number) {
    return request.get<any[]>(`/statistics/chapters/${subjectId}`)
  },

  // 获取学习趋势
  getStudyTrend(days: number = 30) {
    return request.get<any>('/statistics/trend', { params: { days } })
  },

  // 获取题型统计
  getQuestionTypeStatistics() {
    return request.get<any[]>('/statistics/question-types')
  },

  // 获取学习日历
  getStudyCalendar(year?: number, month?: number) {
    return request.get<any>('/statistics/calendar', { params: { year, month } })
  },

  // 每日打卡
  checkIn() {
    return request.post<string>('/statistics/check-in')
  }
}
