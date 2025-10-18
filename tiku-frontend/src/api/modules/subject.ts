import request from '../request'
import type { Subject } from '@/types'

/**
 * 学科相关API
 */
export const subjectApi = {
  // 获取所有学科列表（管理员用）
  list() {
    return request.get<Subject[]>('/subjects/enabled')
  },

  // 获取所有启用的学科
  getEnabledSubjects() {
    return request.get<Subject[]>('/subjects/enabled')
  },

  // 获取学科详情
  getSubjectById(id: number) {
    return request.get<Subject>(`/subjects/${id}`)
  },

  // 创建学科（管理员）
  create(data: Partial<Subject>) {
    return request.post<Subject>('/subjects', data)
  },

  // 更新学科（管理员）
  update(id: number, data: Partial<Subject>) {
    return request.put<Subject>(`/subjects/${id}`, data)
  },

  // 删除学科（管理员）
  delete(id: number) {
    return request.delete(`/subjects/${id}`)
  },

  // 兼容旧方法名
  createSubject(data: Partial<Subject>) {
    return this.create(data)
  },

  updateSubject(id: number, data: Partial<Subject>) {
    return this.update(id, data)
  },

  deleteSubject(id: number) {
    return this.delete(id)
  }
}


