<template>
  <div class="page-container admin-settings">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>系统设置</span>
          </template>

          <el-tabs v-model="activeTab" class="settings-tabs">
            <!-- 基本设置 -->
            <el-tab-pane label="基本设置" name="basic">
              <el-form :model="basicSettings" label-width="120px" style="max-width: 600px">
                <el-form-item label="系统名称">
                  <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
                </el-form-item>
                
                <el-form-item label="系统描述">
                  <el-input 
                    v-model="basicSettings.systemDesc" 
                    type="textarea" 
                    :rows="3"
                    placeholder="请输入系统描述"
                  />
                </el-form-item>

                <el-form-item label="是否开放注册">
                  <el-switch v-model="basicSettings.allowRegister" />
                  <span class="form-tip">关闭后新用户无法自主注册</span>
                </el-form-item>

                <el-form-item label="默认用户角色">
                  <el-select v-model="basicSettings.defaultRole" placeholder="选择默认角色">
                    <el-option label="学生" value="STUDENT" />
                    <el-option label="教师" value="TEACHER" />
                  </el-select>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="saveBasicSettings">
                    保存设置
                  </el-button>
                  <el-button @click="resetBasicSettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 答题设置 -->
            <el-tab-pane label="答题设置" name="practice">
              <el-form :model="practiceSettings" label-width="140px" style="max-width: 600px">
                <el-form-item label="每次练习题目数">
                  <el-input-number 
                    v-model="practiceSettings.defaultQuestionCount" 
                    :min="5" 
                    :max="100"
                  />
                  <span class="form-tip">默认的练习题目数量</span>
                </el-form-item>

                <el-form-item label="考试时长（分钟）">
                  <el-input-number 
                    v-model="practiceSettings.examDuration" 
                    :min="30" 
                    :max="300"
                  />
                </el-form-item>

                <el-form-item label="限时挑战时间（秒）">
                  <el-input-number 
                    v-model="practiceSettings.timedChallengeDuration" 
                    :min="10" 
                    :max="120"
                  />
                  <span class="form-tip">每题的答题时间</span>
                </el-form-item>

                <el-form-item label="自动判题">
                  <el-switch v-model="practiceSettings.autoGrading" />
                  <span class="form-tip">客观题自动判分</span>
                </el-form-item>

                <el-form-item label="显示解析">
                  <el-switch v-model="practiceSettings.showExplanation" />
                  <span class="form-tip">答题后立即显示解析</span>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="savePracticeSettings">
                    保存设置
                  </el-button>
                  <el-button @click="resetPracticeSettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- AI设置 -->
            <el-tab-pane label="AI设置" name="ai">
              <el-form :model="aiSettings" label-width="120px" style="max-width: 600px">
                <el-form-item label="启用AI判题">
                  <el-switch v-model="aiSettings.enableAI" />
                  <span class="form-tip">使用AI对主观题进行智能判分</span>
                </el-form-item>

                <el-form-item label="API密钥" v-if="aiSettings.enableAI">
                  <el-input 
                    v-model="aiSettings.apiKey" 
                    type="password" 
                    show-password
                    placeholder="请输入API密钥"
                  />
                </el-form-item>

                <el-form-item label="API地址" v-if="aiSettings.enableAI">
                  <el-input 
                    v-model="aiSettings.apiUrl" 
                    placeholder="请输入API地址"
                  />
                </el-form-item>

                <el-form-item label="模型名称" v-if="aiSettings.enableAI">
                  <el-input 
                    v-model="aiSettings.modelName" 
                    placeholder="请输入模型名称"
                  />
                </el-form-item>

                <el-form-item v-if="aiSettings.enableAI">
                  <el-button type="primary" @click="saveAISettings">
                    保存设置
                  </el-button>
                  <el-button @click="testAI" :loading="testing">
                    测试连接
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <el-form :model="securitySettings" label-width="140px" style="max-width: 600px">
                <el-form-item label="密码最小长度">
                  <el-input-number 
                    v-model="securitySettings.minPasswordLength" 
                    :min="6" 
                    :max="20"
                  />
                </el-form-item>

                <el-form-item label="密码复杂度">
                  <el-checkbox-group v-model="securitySettings.passwordComplexity">
                    <el-checkbox label="数字" />
                    <el-checkbox label="小写字母" />
                    <el-checkbox label="大写字母" />
                    <el-checkbox label="特殊字符" />
                  </el-checkbox-group>
                </el-form-item>

                <el-form-item label="会话超时（分钟）">
                  <el-input-number 
                    v-model="securitySettings.sessionTimeout" 
                    :min="30" 
                    :max="1440"
                  />
                </el-form-item>

                <el-form-item label="登录失败限制">
                  <el-input-number 
                    v-model="securitySettings.maxLoginAttempts" 
                    :min="3" 
                    :max="10"
                  />
                  <span class="form-tip">失败次数后锁定账户</span>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="saveSecuritySettings">
                    保存设置
                  </el-button>
                  <el-button @click="resetSecuritySettings">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 数据管理 -->
            <el-tab-pane label="数据管理" name="data">
              <div class="data-management">
                <el-alert
                  title="数据管理操作"
                  type="warning"
                  :closable="false"
                  style="margin-bottom: 20px"
                >
                  以下操作可能影响系统数据，请谨慎操作！
                </el-alert>

                <div class="data-action-item">
                  <div class="action-info">
                    <h4>清空答题记录</h4>
                    <p>删除所有用户的答题记录，但保留题目和用户数据</p>
                  </div>
                  <el-button type="warning" @click="clearAnswerRecords">
                    清空记录
                  </el-button>
                </div>

                <el-divider />

                <div class="data-action-item">
                  <div class="action-info">
                    <h4>备份数据库</h4>
                    <p>创建当前数据库的完整备份</p>
                  </div>
                  <el-button type="primary" @click="backupDatabase">
                    立即备份
                  </el-button>
                </div>

                <el-divider />

                <div class="data-action-item">
                  <div class="action-info">
                    <h4>重建索引</h4>
                    <p>重建数据库索引，提升查询性能</p>
                  </div>
                  <el-button type="info" @click="rebuildIndex">
                    重建索引
                  </el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 系统信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="label">系统版本</span>
              <span class="value">{{ systemInfo.version || 'v1.0.0' }}</span>
            </div>
            <div class="info-item">
              <span class="label">数据库类型</span>
              <span class="value">{{ systemInfo.databaseType || 'MySQL 8.0' }}</span>
            </div>
            <div class="info-item">
              <span class="label">Java版本</span>
              <span class="value">{{ systemInfo.javaVersion || 'JDK 17' }}</span>
            </div>
            <div class="info-item">
              <span class="label">运行时间</span>
              <span class="value">{{ systemRuntime }}</span>
            </div>
            <el-divider style="margin: 12px 0" />
            <div class="info-item">
              <span class="label">用户总数</span>
              <span class="value">{{ systemInfo.userCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="label">题目总数</span>
              <span class="value">{{ systemInfo.questionCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="label">答题记录</span>
              <span class="value">{{ systemInfo.answerRecordCount || 0 }}</span>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 16px">
          <template #header>
            <span>存储信息</span>
          </template>
          <div class="storage-info">
            <div class="storage-item">
              <div class="storage-label">数据库大小</div>
              <el-progress :percentage="databaseUsage" :stroke-width="10" />
              <div class="storage-text">
                {{ systemInfo.databaseSize || 0 }} MB / {{ systemInfo.databaseTotal || 1024 }} MB
              </div>
            </div>
            <div class="storage-item">
              <div class="storage-label">文件存储</div>
              <el-progress :percentage="fileStorageUsage" :stroke-width="10" />
              <div class="storage-text">
                {{ systemInfo.fileStorageSize || 0 }} MB / {{ systemInfo.fileStorageTotal || 10240 }} MB
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api'

const activeTab = ref('basic')
const testing = ref(false)
const loading = ref(false)

const basicSettings = reactive({
  systemName: '题库系统',
  systemDesc: '一个功能完善的在线刷题系统',
  allowRegister: true,
  defaultRole: 'STUDENT'
})

const practiceSettings = reactive({
  defaultQuestionCount: 20,
  examDuration: 120,
  timedChallengeDuration: 30,
  autoGrading: true,
  showExplanation: true
})

const aiSettings = reactive({
  enableAI: false,
  apiKey: '',
  apiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
  modelName: 'qwen/qwen-plus'
})

const securitySettings = reactive({
  minPasswordLength: 6,
  passwordComplexity: ['数字', '小写字母'],
  sessionTimeout: 120,
  maxLoginAttempts: 5
})

const systemInfo = reactive({
  version: '',
  databaseType: '',
  javaVersion: '',
  runtime: '',
  databaseSize: 0,
  databaseTotal: 1024,
  fileStorageSize: 0,
  fileStorageTotal: 10240,
  userCount: 0,
  questionCount: 0,
  answerRecordCount: 0
})

const systemRuntime = computed(() => systemInfo.runtime || '加载中...')
const databaseUsage = computed(() => {
  if (systemInfo.databaseTotal === 0) return 0
  return Math.round((systemInfo.databaseSize / systemInfo.databaseTotal) * 100)
})
const fileStorageUsage = computed(() => {
  if (systemInfo.fileStorageTotal === 0) return 0
  return Math.round((systemInfo.fileStorageSize / systemInfo.fileStorageTotal) * 100)
})

// 加载所有设置
const loadSettings = async () => {
  try {
    const res = await systemApi.getSettings()
    Object.assign(basicSettings, res.data.basic)
    Object.assign(practiceSettings, res.data.practice)
    Object.assign(aiSettings, res.data.ai)
    Object.assign(securitySettings, res.data.security)
  } catch (error: any) {
    ElMessage.error(error.message || '加载设置失败')
  }
}

// 加载系统信息
const loadSystemInfo = async () => {
  try {
    const res = await systemApi.getSystemInfo()
    Object.assign(systemInfo, res.data)
  } catch (error: any) {
    console.error('加载系统信息失败', error)
  }
}

const saveBasicSettings = async () => {
  try {
    await systemApi.saveBasicSettings(basicSettings)
    ElMessage.success('基本设置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const resetBasicSettings = async () => {
  basicSettings.systemName = '题库系统'
  basicSettings.systemDesc = '一个功能完善的在线刷题系统'
  basicSettings.allowRegister = true
  basicSettings.defaultRole = 'STUDENT'
  ElMessage.info('已重置为默认设置')
}

const savePracticeSettings = async () => {
  try {
    await systemApi.savePracticeSettings(practiceSettings)
    ElMessage.success('答题设置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const resetPracticeSettings = () => {
  practiceSettings.defaultQuestionCount = 20
  practiceSettings.examDuration = 120
  practiceSettings.timedChallengeDuration = 30
  practiceSettings.autoGrading = true
  practiceSettings.showExplanation = true
  ElMessage.info('已重置为默认设置')
}

const saveAISettings = async () => {
  if (aiSettings.enableAI && (!aiSettings.apiKey || !aiSettings.apiUrl)) {
    ElMessage.warning('请填写完整的AI配置信息')
    return
  }
  try {
    await systemApi.saveAISettings(aiSettings)
    ElMessage.success('AI设置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const testAI = async () => {
  testing.value = true
  try {
    await systemApi.testAI()
    ElMessage.success('AI服务连接成功')
  } catch (error: any) {
    ElMessage.error(error.message || 'AI服务连接失败')
  } finally {
    testing.value = false
  }
}

const saveSecuritySettings = async () => {
  try {
    await systemApi.saveSecuritySettings(securitySettings)
    ElMessage.success('安全设置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const resetSecuritySettings = () => {
  securitySettings.minPasswordLength = 6
  securitySettings.passwordComplexity = ['数字', '小写字母']
  securitySettings.sessionTimeout = 120
  securitySettings.maxLoginAttempts = 5
  ElMessage.info('已重置为默认设置')
}

const clearAnswerRecords = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有答题记录吗？此操作不可恢复！',
      '清空答题记录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const loadingMsg = ElMessage.loading('正在清空...')
    try {
      const res = await systemApi.clearAnswerRecords()
      loadingMsg.close()
      ElMessage.success(`已清空 ${res.data.deletedCount} 条答题记录`)
      await loadSystemInfo() // 重新加载系统信息
    } catch (error: any) {
      loadingMsg.close()
      ElMessage.error(error.message || '清空失败')
    }
  } catch {
    // 用户取消
  }
}

const backupDatabase = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要备份数据库吗？',
      '备份数据库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const loadingMsg = ElMessage.loading('正在备份...')
    try {
      const res = await systemApi.backupDatabase()
      loadingMsg.close()
      ElMessage.success(`备份文件：${res.data.backupFile}`)
    } catch (error: any) {
      loadingMsg.close()
      ElMessage.error(error.message || '备份失败')
    }
  } catch {
    // 用户取消
  }
}

const rebuildIndex = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重建索引吗？此操作可能需要较长时间',
      '重建索引',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const loadingMsg = ElMessage.loading('正在重建索引...')
    try {
      await systemApi.rebuildIndex()
      loadingMsg.close()
      ElMessage.success('索引重建完成')
    } catch (error: any) {
      loadingMsg.close()
      ElMessage.error(error.message || '重建失败')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadSettings()
  loadSystemInfo()
  
  // 每30秒更新一次系统信息
  setInterval(() => {
    loadSystemInfo()
  }, 30000)
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-settings {
  .settings-tabs {
    min-height: 500px;
  }

  .form-tip {
    margin-left: 12px;
    font-size: 12px;
    color: $text-secondary;
  }

  .data-management {
    .data-action-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;

      .action-info {
        h4 {
          margin: 0 0 8px 0;
          font-size: 16px;
          color: $text-primary;
        }

        p {
          margin: 0;
          font-size: 13px;
          color: $text-secondary;
        }
      }
    }
  }

  .system-info {
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid $border-color;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: $text-secondary;
        font-size: 14px;
      }

      .value {
        color: $text-primary;
        font-weight: 500;
      }
    }
  }

  .storage-info {
    .storage-item {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }

      .storage-label {
        margin-bottom: 8px;
        font-size: 14px;
        color: $text-primary;
      }

      .storage-text {
        margin-top: 4px;
        font-size: 12px;
        color: $text-secondary;
        text-align: right;
      }
    }
  }
}
</style>
