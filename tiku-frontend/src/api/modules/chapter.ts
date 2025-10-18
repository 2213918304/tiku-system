import request from '../request'
import type { Chapter } from '@/types'

/**
 * 章节相关API
 */
export const chapterApi = {
  // 获取学科的所有章节列表
  list(subjectId: number) {
    return request.get<Chapter[]>(`/chapters/subject/${subjectId}`)
  },

  // 获取学科的所有章节
  getBySubject(subjectId: number) {
    return request.get<Chapter[]>(`/chapters/subject/${subjectId}`)
  },

  // 获取章节树形结构
  getTree(subjectId: number) {
    return request.get<Chapter[]>(`/chapters/subject/${subjectId}/tree`)
  },

  // 获取章节详情
  getById(id: number) {
    return request.get<Chapter>(`/chapters/${id}`)
  },

  // 创建章节
  create(data: {
    subjectId: number
    parentId?: number | null
    name: string
    sortOrder?: number
    level?: number
    description?: string | null
  }) {
    return request.post<Chapter>('/chapters', data)
  },

  // 更新章节
  update(id: number, data: {
    subjectId: number
    parentId?: number | null
    name: string
    sortOrder?: number
    level?: number
    description?: string | null
  }) {
    return request.put<Chapter>(`/chapters/${id}`, data)
  },

  // 删除章节
  delete(id: number) {
    return request.delete<void>(`/chapters/${id}`)
  }
}


