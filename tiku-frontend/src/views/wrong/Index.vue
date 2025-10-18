<template>
  <div class="page-container wrong-questions-page">
    <div class="page-header">
      <h1>错题本</h1>
      <p>复习错题，查漏补缺，提高正确率</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">错题总数</div>
            <div class="stat-value">{{ stats.totalWrong }}</div>
          </div>
          <div class="stat-icon danger">
            <el-icon><WarningFilled /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">待复习</div>
            <div class="stat-value">{{ stats.needReview }}</div>
          </div>
          <div class="stat-icon warning">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">已掌握</div>
            <div class="stat-value">{{ stats.mastered }}</div>
          </div>
          <div class="stat-icon success">
            <el-icon><CircleCheck /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选和操作 -->
    <el-card class="filter-card">
      <el-form :model="filters" :inline="true" class="filter-form">
        <el-form-item label="学科">
          <el-select
            v-model="filters.subjectId"
            placeholder="全部学科"
            clearable
            style="width: 180px"
            @change="handleFilterChange"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="章节">
          <el-select
            v-model="filters.chapterId"
            placeholder="全部章节"
            clearable
            style="width: 200px"
            :disabled="!filters.subjectId"
            @change="handleFilterChange"
          >
            <el-option
              v-for="chapter in chapters"
              :key="chapter.id"
              :label="chapter.name"
              :value="chapter.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="题型">
          <el-select
            v-model="filters.type"
            placeholder="全部题型"
            clearable
            style="width: 120px"
            @change="handleFilterChange"
          >
            <el-option label="单选题" value="SINGLE" />
            <el-option label="多选题" value="MULTIPLE" />
            <el-option label="判断题" value="JUDGE" />
            <el-option label="填空题" value="FILL" />
            <el-option label="简答题" value="SHORT_ANSWER" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            clearable
            style="width: 120px"
            @change="handleFilterChange"
          >
            <el-option label="错误" value="WRONG" />
            <el-option label="多次错误" value="REPEATED_WRONG" />
            <el-option label="已掌握" value="MASTERED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="startPractice" :disabled="wrongQuestions.length === 0">
            <el-icon><CaretRight /></el-icon>
            开始练习
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 错题列表 -->
    <el-card class="question-list-card">
      <template #header>
        <div class="card-header-content">
          <span>错题列表（共 {{ filteredQuestions.length }} 题）</span>
          <div class="header-actions">
            <el-button-group>
              <el-button
                :type="viewMode === 'list' ? 'primary' : ''"
                @click="viewMode = 'list'"
              >
                <el-icon><List /></el-icon>
                列表
              </el-button>
              <el-button
                :type="viewMode === 'card' ? 'primary' : ''"
                @click="viewMode = 'card'"
              >
                <el-icon><Grid /></el-icon>
                卡片
              </el-button>
            </el-button-group>
            
            <el-button
              v-if="selectedQuestions.length > 0"
              type="danger"
              plain
              @click="batchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除（{{ selectedQuestions.length }}）
            </el-button>
          </div>
        </div>
      </template>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="list-view">
        <el-table
          v-loading="loading"
          :data="paginatedQuestions"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="题目" min-width="300">
            <template #default="{ row }">
              <div class="question-cell">
                <div class="question-tags">
                  <el-tag :type="getQuestionTypeColor(row.type)" size="small">
                    {{ getQuestionTypeName(row.type) }}
                  </el-tag>
                  <el-tag :type="getStatusColor(row.status)" size="small">
                    {{ getStatusName(row.status) }}
                  </el-tag>
                </div>
                <div class="question-content" v-html="row.content"></div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="学科/章节" width="200">
            <template #default="{ row }">
              <div class="subject-info">
                <div>{{ row.subjectName }}</div>
                <div class="chapter-name">{{ row.chapterName }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="错误次数" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.wrongCount >= 3 ? 'danger' : 'warning'">
                {{ row.wrongCount }} 次
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="最后错误" width="150">
            <template #default="{ row }">
              {{ formatTime(row.lastWrongAt) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" @click="viewDetail(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button text size="small" @click="practiceOne(row)">
                <el-icon><Edit /></el-icon>
                练习
              </el-button>
              <el-button
                text
                size="small"
                type="success"
                :disabled="row.status === 'MASTERED'"
                @click="markAsMastered(row)"
              >
                <el-icon><Select /></el-icon>
                掌握
              </el-button>
              <el-button text size="small" type="danger" @click="removeWrong(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="card-view" v-loading="loading">
        <el-row :gutter="16">
          <el-col
            v-for="question in paginatedQuestions"
            :key="question.id"
            :xs="24"
            :sm="12"
            :lg="8"
          >
            <div class="wrong-question-card">
              <el-checkbox
                v-model="question.selected"
                class="card-checkbox"
                @change="handleCardSelection(question)"
              />
              
              <div class="card-header">
                <div class="header-tags">
                  <el-tag :type="getQuestionTypeColor(question.type)" size="small">
                    {{ getQuestionTypeName(question.type) }}
                  </el-tag>
                  <el-tag :type="getStatusColor(question.status)" size="small">
                    {{ getStatusName(question.status) }}
                  </el-tag>
                </div>
                <el-tag :type="question.wrongCount >= 3 ? 'danger' : 'warning'">
                  错 {{ question.wrongCount }} 次
                </el-tag>
              </div>

              <div class="card-content" @click="viewDetail(question)">
                <div class="question-text" v-html="question.content"></div>
                <div class="question-info">
                  <span class="subject-name">{{ question.subjectName }}</span>
                  <span class="chapter-name">{{ question.chapterName }}</span>
                </div>
                <div class="question-time">{{ formatTime(question.lastWrongAt) }}</div>
              </div>

              <div class="card-actions">
                <el-button text @click="viewDetail(question)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button text @click="practiceOne(question)">
                  <el-icon><Edit /></el-icon>
                  练习
                </el-button>
                <el-button
                  text
                  type="success"
                  :disabled="question.status === 'MASTERED'"
                  @click="markAsMastered(question)"
                >
                  <el-icon><Select /></el-icon>
                  掌握
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-empty
          v-if="filteredQuestions.length === 0"
          description="暂无错题数据"
        />
      </div>

      <!-- 分页 -->
      <div v-if="filteredQuestions.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredQuestions.length"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="错题详情"
      width="800px"
      top="5vh"
    >
      <div v-if="selectedQuestion" class="question-detail">
        <div class="detail-tags">
          <el-tag :type="getQuestionTypeColor(selectedQuestion.type)">
            {{ getQuestionTypeName(selectedQuestion.type) }}
          </el-tag>
          <el-tag :type="getStatusColor(selectedQuestion.status)">
            {{ getStatusName(selectedQuestion.status) }}
          </el-tag>
          <el-tag type="warning">{{ selectedQuestion.subjectName }} - {{ selectedQuestion.chapterName }}</el-tag>
          <el-tag :type="selectedQuestion.wrongCount >= 3 ? 'danger' : 'warning'">
            错误 {{ selectedQuestion.wrongCount }} 次
          </el-tag>
        </div>

        <div class="detail-section">
          <div class="section-title">题目</div>
          <div class="section-content" v-html="selectedQuestion.content"></div>
        </div>

        <div v-if="selectedQuestion.options?.length" class="detail-section">
          <div class="section-title">选项</div>
          <div class="options-list">
            <div
              v-for="option in selectedQuestion.options"
              :key="option.key"
              class="option-item"
            >
              <span class="option-key">{{ option.key }}.</span>
              <span v-html="option.value"></span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">我的答案</div>
          <div class="section-content wrong-answer">
            {{ formatAnswer(selectedQuestion.userAnswer) }}
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">正确答案</div>
          <div class="section-content correct-answer">
            {{ formatAnswer(selectedQuestion.correctAnswer) }}
          </div>
        </div>

        <div v-if="selectedQuestion.explanation" class="detail-section">
          <div class="section-title">答案解析</div>
          <div class="section-content" v-html="selectedQuestion.explanation"></div>
        </div>

        <div class="detail-section">
          <div class="section-title">错误记录</div>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in selectedQuestion.wrongRecords"
              :key="index"
              :timestamp="formatTime(record.wrongAt)"
              placement="top"
            >
              第 {{ index + 1 }} 次错误
              <span v-if="record.userAnswer">
                ，回答：{{ formatAnswer(record.userAnswer) }}
              </span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button
          type="success"
          :disabled="selectedQuestion?.status === 'MASTERED'"
          @click="markAsMastered(selectedQuestion)"
        >
          <el-icon><Select /></el-icon>
          标记为已掌握
        </el-button>
        <el-button type="primary" @click="practiceOne(selectedQuestion)">
          <el-icon><Edit /></el-icon>
          立即练习
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  WarningFilled,
  Clock,
  CircleCheck,
  CaretRight,
  List,
  Grid,
  Delete,
  View,
  Edit,
  Select
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { wrongQuestionApi } from '@/api/modules/wrongQuestion'
import { subjectApi } from '@/api/modules/subject'
import { chapterApi } from '@/api/modules/chapter'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const loading = ref(false)

const stats = ref({
  totalWrong: 0,
  needReview: 0,
  mastered: 0
})

const subjects = ref<any[]>([])
const chapters = ref<any[]>([])
const wrongQuestions = ref<any[]>([])
const selectedQuestions = ref<any[]>([])
const selectedQuestion = ref<any>()
const showDetailDialog = ref(false)
const viewMode = ref('list')
const currentPage = ref(1)
const pageSize = ref(20)

const filters = ref({
  subjectId: null,
  chapterId: null,
  type: '',
  status: ''
})

// 过滤后的题目
const filteredQuestions = computed(() => {
  let result = wrongQuestions.value

  if (filters.value.subjectId) {
    result = result.filter(q => q.subjectId === filters.value.subjectId)
  }

  if (filters.value.chapterId) {
    result = result.filter(q => q.chapterId === filters.value.chapterId)
  }

  if (filters.value.type) {
    result = result.filter(q => q.type === filters.value.type)
  }

  if (filters.value.status) {
    result = result.filter(q => q.status === filters.value.status)
  }

  return result
})

// 分页后的题目
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredQuestions.value.slice(start, end)
})

onMounted(() => {
  loadData()
})

// 监听学科变化，加载章节
watch(() => filters.value.subjectId, (newVal) => {
  filters.value.chapterId = null
  if (newVal) {
    loadChapters(newVal)
  } else {
    chapters.value = []
  }
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await wrongQuestionApi.getStats()
    stats.value = statsRes.data
    
    // 加载学科列表
    const subjectsRes = await subjectApi.list()
    subjects.value = subjectsRes.data || []
    
    // 加载错题列表
    const wrongRes = await wrongQuestionApi.getAllMyWrongQuestions()
    
    // 解析并处理错题数据
    wrongQuestions.value = (wrongRes.data || []).map((wq: any) => {
      // 解析选项
      let options = null
      if (wq.options) {
        if (typeof wq.options === 'string') {
          try {
            options = JSON.parse(wq.options)
          } catch (e) {
            options = null
          }
        } else {
          options = wq.options
        }
        
        // 格式化选项
        if (Array.isArray(options) && options.length > 0) {
          options = options.map((opt: any, index: number) => {
            if (typeof opt === 'object' && opt !== null) {
              return {
                key: opt.key || String.fromCharCode(65 + index),
                value: opt.value || opt.content || JSON.stringify(opt)
              }
            }
            if (typeof opt === 'string') {
              const match = opt.match(/^([A-Z])\.\s*(.*)$/)
              if (match) {
                return { key: match[1], value: match[2] }
              }
              return { key: String.fromCharCode(65 + index), value: opt }
            }
            return { key: String.fromCharCode(65 + index), value: String(opt) }
          })
        }
      }
      
      return {
        id: wq.wrongQuestionId,
        questionId: wq.questionId,
        type: wq.type,
        difficulty: wq.difficulty,
        content: wq.content,
        subjectId: wq.subjectId,
        subjectName: wq.subjectName,
        chapterId: wq.chapterId,
        chapterName: wq.chapterName,
        options,
        userAnswer: wq.userAnswer,
        correctAnswer: wq.correctAnswer,
        explanation: wq.explanation,
        wrongCount: wq.wrongCount,
        lastWrongAt: wq.lastWrongAt,
        status: wq.status,
        selected: false,
        wrongRecords: wq.wrongRecords || []
      }
    })
  } catch (error: any) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载章节
const loadChapters = async (subjectId: number) => {
  try {
    const res = await chapterApi.getTree(subjectId)
    chapters.value = res.data || []
  } catch (error: any) {
    console.error('加载章节失败:', error)
    ElMessage.error(error.response?.data?.message || '加载章节失败')
  }
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
}

// 选择变化
const handleSelectionChange = (val: any[]) => {
  selectedQuestions.value = val
}

const handleCardSelection = (question: any) => {
  if (question.selected) {
    selectedQuestions.value.push(question)
  } else {
    const index = selectedQuestions.value.findIndex(q => q.id === question.id)
    if (index > -1) {
      selectedQuestions.value.splice(index, 1)
    }
  }
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 查看详情
const viewDetail = (question: any) => {
  selectedQuestion.value = question
  showDetailDialog.value = true
}

// 开始练习
const startPractice = () => {
  if (filteredQuestions.value.length === 0) {
    ElMessage.warning('没有可练习的错题')
    return
  }

  // 创建错题练习会话
  const sessionId = `wrong_${Date.now()}`
  const sessionData = {
    sessionId,
    mode: '错题强化',
    subjectName: filters.value.subjectId ? subjects.value.find(s => s.id === filters.value.subjectId)?.name : '全部错题',
    questions: filteredQuestions.value.map(q => ({
      id: q.questionId,
      type: q.type,
      difficulty: q.difficulty,
      title: q.content,
      content: q.content,
      chapterId: q.chapterId,
      chapterName: q.chapterName,
      subjectName: q.subjectName,
      options: q.options,
      answer: q.correctAnswer,
      answerAnalysis: q.explanation
    }))
  }
  
  sessionStorage.setItem(`practice_session_${sessionId}`, JSON.stringify(sessionData))
  
  router.push({
    path: '/practice/session',
    query: { sessionId }
  })
}

// 练习单题
const practiceOne = (question: any) => {
  // 创建单题练习会话
  const sessionId = `single_${Date.now()}`
  const sessionData = {
    sessionId,
    mode: '单题练习',
    subjectName: question.subjectName,
    questions: [{
      id: question.questionId,
      type: question.type,
      difficulty: question.difficulty,
      title: question.content,
      content: question.content,
      chapterId: question.chapterId,
      chapterName: question.chapterName,
      subjectName: question.subjectName,
      options: question.options,
      answer: question.correctAnswer,
      answerAnalysis: question.explanation
    }]
  }
  
  sessionStorage.setItem(`practice_session_${sessionId}`, JSON.stringify(sessionData))
  showDetailDialog.value = false
  
  router.push({
    path: '/practice/session',
    query: { sessionId }
  })
}

// 标记为已掌握
const markAsMastered = async (question: any) => {
  if (!question) return

  try {
    await wrongQuestionApi.markAsMastered(question.questionId)
    question.status = 'MASTERED'
    stats.value.mastered++
    stats.value.needReview--
    ElMessage.success('已标记为掌握')
    showDetailDialog.value = false
  } catch (error: any) {
    console.error('标记失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除错题
const removeWrong = async (question: any) => {
  try {
    await ElMessageBox.confirm('确定要从错题本中删除这道题吗？', '确认删除', {
      type: 'warning'
    })

    await wrongQuestionApi.remove(question.questionId)
    const index = wrongQuestions.value.findIndex(q => q.id === question.id)
    if (index > -1) {
      wrongQuestions.value.splice(index, 1)
    }
    stats.value.totalWrong--
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请先选择要删除的错题')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedQuestions.value.length} 道错题吗？`,
      '批量删除',
      { type: 'warning' }
    )

    const questionIds = selectedQuestions.value.map(q => q.questionId)
    await wrongQuestionApi.batchRemove(questionIds)

    selectedQuestions.value.forEach(q => {
      const index = wrongQuestions.value.findIndex(wq => wq.id === q.id)
      if (index > -1) {
        wrongQuestions.value.splice(index, 1)
      }
    })

    stats.value.totalWrong -= selectedQuestions.value.length
    selectedQuestions.value = []
    ElMessage.success('批量删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }
}

// 工具函数
const getQuestionTypeColor = (type: string) => {
  const map: Record<string, any> = {
    SINGLE: 'primary',
    MULTIPLE: 'success',
    JUDGE: 'warning',
    FILL: 'info',
    SHORT_ANSWER: 'danger'
  }
  return map[type] || 'info'
}

const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    FILL: '填空题',
    SHORT_ANSWER: '简答题'
  }
  return map[type] || type
}

const getStatusColor = (status: string) => {
  const map: Record<string, any> = {
    WRONG: 'danger',
    REPEATED_WRONG: 'warning',
    MASTERED: 'success'
  }
  return map[status] || 'info'
}

const getStatusName = (status: string) => {
  const map: Record<string, string> = {
    WRONG: '错误',
    REPEATED_WRONG: '多次错误',
    MASTERED: '已掌握'
  }
  return map[status] || status
}

const formatAnswer = (answer: any) => {
  if (!answer) return '-'
  
  // 如果是JSON字符串，先解析
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
        answer = parsed.answer
      }
    } catch {
      // 不是JSON，继续处理
    }
  }
  
  // 如果是对象，提取answer字段
  if (typeof answer === 'object' && answer !== null && !Array.isArray(answer)) {
    if ('answer' in answer) {
      answer = answer.answer
    }
  }
  
  // 处理不同类型的答案
  if (Array.isArray(answer)) {
    return answer.join('、')
  }
  if (answer === 'true' || answer === true) return '正确'
  if (answer === 'false' || answer === false) return '错误'
  if (answer === '对') return '正确'
  if (answer === '错') return '错误'
  
  return answer
}

const formatTime = (date: Date | string) => {
  if (!date) return '-'
  return dayjs(date).fromNow()
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.wrong-questions-page {
  max-width: 1600px;
  margin: 0 auto;
}

// 统计卡片
.stats-row {
  margin-bottom: $spacing-lg;

  .stat-card {
    background: $bg-white;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    border: 1px solid $border-color;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    .stat-left {
      .stat-label {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: $text-primary;
      }
    }

    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: $border-radius-lg;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;

      &.success {
        background: rgba(0, 180, 42, 0.1);
        color: $success-color;
      }

      &.warning {
        background: rgba(255, 125, 0, 0.1);
        color: $warning-color;
      }

      &.danger {
        background: rgba(245, 63, 63, 0.1);
        color: $danger-color;
      }
    }
  }
}

// 筛选卡片
.filter-card {
  margin-bottom: $spacing-lg;

  .filter-form {
    margin-bottom: 0;
  }
}

// 题目列表卡片
.question-list-card {
  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: $spacing-md;

    .header-actions {
      display: flex;
      gap: $spacing-sm;
    }
  }

  // 列表视图
  .list-view {
    .question-cell {
      .question-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 8px;
        flex-wrap: wrap;
      }

      .question-content {
        font-size: 14px;
        line-height: 1.6;
        color: $text-primary;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }

    .subject-info {
      font-size: 14px;

      .chapter-name {
        font-size: 12px;
        color: $text-secondary;
        margin-top: 4px;
      }
    }
  }

  // 卡片视图
  .card-view {
    .wrong-question-card {
      position: relative;
      border: 1px solid $border-color;
      border-radius: $border-radius-lg;
      padding: $spacing-md;
      margin-bottom: $spacing-md;
      transition: all $transition-fast;

      &:hover {
        border-color: $primary-color;
        box-shadow: $box-shadow-md;
        transform: translateY(-2px);
      }

      .card-checkbox {
        position: absolute;
        top: $spacing-md;
        left: $spacing-md;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: $spacing-md;
        padding-left: 32px;

        .header-tags {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
        }
      }

      .card-content {
        cursor: pointer;
        margin-bottom: $spacing-md;

        .question-text {
          font-size: 14px;
          line-height: 1.8;
          color: $text-primary;
          margin-bottom: $spacing-sm;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
        }

        .question-info {
          display: flex;
          gap: $spacing-sm;
          font-size: 12px;
          margin-bottom: 4px;

          .subject-name {
            color: $text-primary;
            font-weight: 500;
          }

          .chapter-name {
            color: $text-secondary;
          }
        }

        .question-time {
          font-size: 12px;
          color: $text-secondary;
        }
      }

      .card-actions {
        display: flex;
        justify-content: space-between;
        gap: 4px;
        padding-top: $spacing-sm;
        border-top: 1px solid $border-light;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: $spacing-lg;
  }
}

// 详情对话框
.question-detail {
  .detail-tags {
    display: flex;
    gap: 8px;
    margin-bottom: $spacing-lg;
    flex-wrap: wrap;
  }

  .detail-section {
    margin-bottom: $spacing-lg;

    .section-title {
      font-weight: 600;
      color: $text-primary;
      margin-bottom: $spacing-sm;
      display: flex;
      align-items: center;
      gap: 6px;

      &::before {
        content: '';
        width: 3px;
        height: 16px;
        background: $primary-color;
        border-radius: 2px;
      }
    }

    .section-content {
      line-height: 1.8;
      color: $text-secondary;
      padding: $spacing-md;
      background: $bg-gray;
      border-radius: $border-radius-md;

      &.wrong-answer {
        background: rgba(245, 63, 63, 0.1);
        color: $danger-color;
        font-weight: 600;
      }

      &.correct-answer {
        background: rgba(0, 180, 42, 0.1);
        color: $success-color;
        font-weight: 600;
      }
    }

    .options-list {
      .option-item {
        display: flex;
        gap: 8px;
        padding: $spacing-sm $spacing-md;
        margin-bottom: 6px;
        background: $bg-gray;
        border-radius: $border-radius-sm;
        line-height: 1.6;

        .option-key {
          font-weight: 600;
          color: $primary-color;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .filter-form {
    .el-form-item {
      width: 100%;

      :deep(.el-select) {
        width: 100% !important;
      }
    }
  }

  .card-header-content {
    .header-actions {
      width: 100%;
      
      .el-button-group {
        flex: 1;
      }
    }
  }
}
</style>
