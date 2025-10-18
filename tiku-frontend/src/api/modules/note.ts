import request from '../request'

/**
 * 笔记相关API
 */
export const noteApi = {
  // 获取笔记列表（分页）
  getMyNotes(params?: { page?: number; size?: number; subjectId?: number; keyword?: string }) {
    return request.get<any>('/notes', { params })
  },

  // 获取所有笔记（不分页）
  getAllMyNotes() {
    return request.get<any[]>('/notes/all')
  },

  // 添加笔记
  add(data: { questionId?: number; subjectId?: number; title: string; content: string }) {
    return request.post<any>('/notes', data)
  },

  // 更新笔记
  update(id: number, data: { subjectId?: number; title: string; content: string }) {
    return request.put<any>(`/notes/${id}`, data)
  },

  // 删除笔记
  delete(id: number) {
    return request.delete(`/notes/${id}`)
  },

  // 获取某题的笔记
  getByQuestion(questionId: number) {
    return request.get<any>(`/notes/question/${questionId}`)
  }
}
