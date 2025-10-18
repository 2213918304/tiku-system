import request from '../request'
import type { Question, PageResponse } from '@/types'

/**
 * 题目相关API
 */
export const questionApi = {
  // 分页查询题目列表
  list(params: any) {
    const { page = 0, size = 20, ...rest } = params
    return request.post<PageResponse<Question>>('/questions/search', rest, {
      params: { page, size }
    })
  },

  // 获取学科的所有题目
  getBySubject(subjectId: number, params?: any) {
    return request.get<any[]>(`/questions/subject/${subjectId}`, { params })
  },

  // 获取章节的题目
  getByChapter(chapterId: number, params?: any) {
    return request.get<any[]>(`/questions/chapter/${chapterId}`, { params })
  },

  // 获取题目详情
  getById(id: number) {
    return request.get<Question>(`/questions/${id}`)
  },

  // 创建题目
  create(data: any) {
    return request.post<Question>('/questions', data)
  },

  // 更新题目
  update(id: number, data: any) {
    return request.put<Question>(`/questions/${id}`, data)
  },

  // 删除题目
  delete(id: number) {
    return request.delete<void>(`/questions/${id}`)
  },

  // 批量删除题目
  batchDelete(ids: number[]) {
    return request.delete<void>('/questions/batch', { data: ids })
  },

  // 搜索题目
  search(params: any) {
    return request.post<PageResponse<Question>>('/questions/search', params)
  }
}


