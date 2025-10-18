import request from '../request'
import type {
  PracticeRequest,
  PracticeSession,
  SubmitAnswerRequest,
  GradingResult
} from '@/types'

/**
 * 刷题相关API
 */
export const practiceApi = {
  // 获取所有刷题模式
  getPracticeModes() {
    return request.get<any[]>('/practice/modes')
  },

  // 开始刷题
  startPractice(data: PracticeRequest) {
    return request.post<PracticeSession>('/practice/start', data)
  },

  // 提交答案
  submitAnswer(data: SubmitAnswerRequest) {
    return request.post<GradingResult>('/grading/submit', data)
  },

  // 批量提交答案
  submitBatch(data: SubmitAnswerRequest[]) {
    return request.post<GradingResult[]>('/grading/submit/batch', data)
  }
}



