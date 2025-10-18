import request from '../request'

/**
 * 数据导入相关API
 */
export const dataImportApi = {
  // 批量导入题目
  importBatch(data: any) {
    return request.post<any>('/admin/data-import/batch', data)
  },

  // 导入马原题库
  importMayuan() {
    return request.post<any>('/admin/data-import/mayuan')
  },
  
  // 智能批量导入（根据Excel自动归类章节）
  smartImport(data: any) {
    return request.post<any>('/admin/data-import/smart-import', data)
  }
}

