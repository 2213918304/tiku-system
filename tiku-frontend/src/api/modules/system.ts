import request from '../request'

/**
 * 系统设置相关API
 */
export const systemApi = {
  // 获取所有设置
  getSettings() {
    return request.get<any>('/admin/system/settings')
  },

  // 保存基本设置
  saveBasicSettings(data: any) {
    return request.post<any>('/admin/system/settings/basic', data)
  },

  // 保存答题设置
  savePracticeSettings(data: any) {
    return request.post<any>('/admin/system/settings/practice', data)
  },

  // 保存AI设置
  saveAISettings(data: any) {
    return request.post<any>('/admin/system/settings/ai', data)
  },

  // 保存安全设置
  saveSecuritySettings(data: any) {
    return request.post<any>('/admin/system/settings/security', data)
  },

  // 获取系统信息
  getSystemInfo() {
    return request.get<any>('/admin/system/info')
  },

  // 清空答题记录
  clearAnswerRecords() {
    return request.post<any>('/admin/system/data/clear-answer-records')
  },

  // 备份数据库
  backupDatabase() {
    return request.post<any>('/admin/system/data/backup')
  },

  // 重建索引
  rebuildIndex() {
    return request.post<any>('/admin/system/data/rebuild-index')
  },

  // 测试AI连接
  testAI() {
    return request.post<any>('/admin/system/test-ai')
  }
}

