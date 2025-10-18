<template>
  <div class="page-container collection-page">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p>收藏重点题目，随时复习</p>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">收藏总数</div>
            <div class="stat-value">{{ stats.totalCount }}</div>
          </div>
          <div class="stat-icon warning">
            <el-icon><Star /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">本周新增</div>
            <div class="stat-value">{{ stats.weekCount }}</div>
          </div>
          <div class="stat-icon primary">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="8">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">已练习</div>
            <div class="stat-value">{{ stats.practicedCount }}</div>
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

        <el-form-item label="排序">
          <el-select
            v-model="filters.sortBy"
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option label="收藏时间" value="createTime" />
            <el-option label="题目难度" value="difficulty" />
            <el-option label="练习次数" value="practiceCount" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="startPractice" :disabled="favorites.length === 0">
            <el-icon><CaretRight /></el-icon>
            开始练习
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 收藏列表 -->
    <el-card class="favorites-card" v-loading="loading">
      <template #header>
        <div class="card-header-content">
          <span>收藏列表（共 {{ filteredFavorites.length }} 题）</span>
          <el-button
            v-if="selectedFavorites.length > 0"
            type="danger"
            plain
            @click="batchRemove"
          >
            <el-icon><Delete /></el-icon>
            批量取消（{{ selectedFavorites.length }}）
          </el-button>
        </div>
      </template>

      <el-table
        :data="paginatedFavorites"
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
                <el-tag v-if="row.difficulty" type="info" size="small">
                  {{ getDifficultyName(row.difficulty) }}
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

        <el-table-column label="练习次数" width="100" align="center">
          <template #default="{ row }">
            {{ row.practiceCount || 0 }} 次
          </template>
        </el-table-column>

        <el-table-column label="收藏时间" width="150">
          <template #default="{ row }">
            {{ formatTime(row.favoriteAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button text size="small" @click="practiceOne(row)">
              <el-icon><Edit /></el-icon>
              练习
            </el-button>
            <el-button text size="small" type="danger" @click="removeFavorite(row)">
              <el-icon><StarFilled /></el-icon>
              取消收藏
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="filteredFavorites.length === 0"
        description="暂无收藏题目"
      />

      <!-- 分页 -->
      <div v-if="filteredFavorites.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredFavorites.length"
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
      title="题目详情"
      width="800px"
      top="5vh"
    >
      <div v-if="selectedQuestion" class="question-detail">
        <div class="detail-tags">
          <el-tag :type="getQuestionTypeColor(selectedQuestion.type)">
            {{ getQuestionTypeName(selectedQuestion.type) }}
          </el-tag>
          <el-tag v-if="selectedQuestion.difficulty" type="info">
            {{ getDifficultyName(selectedQuestion.difficulty) }}
          </el-tag>
          <el-tag type="warning">{{ selectedQuestion.subjectName }} - {{ selectedQuestion.chapterName }}</el-tag>
        </div>

        <div class="detail-section">
          <div class="section-title">题目</div>
          <div class="section-content" v-html="selectedQuestion.content"></div>
        </div>

        <div v-if="selectedQuestion.options && selectedQuestion.options.length > 0" class="detail-section">
          <div class="section-title">选项</div>
          <div class="options-list">
            <div
              v-for="(option, index) in selectedQuestion.options"
              :key="index"
              class="option-item"
            >
              <span class="option-key">{{ option.key || String.fromCharCode(65 + index) }}.</span>
              <span v-html="option.value || option.content || option"></span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">正确答案</div>
          <div class="section-content correct-answer">
            {{ formatAnswer(selectedQuestion.answer) }}
          </div>
        </div>

        <div v-if="selectedQuestion.explanation" class="detail-section">
          <div class="section-title">答案解析</div>
          <div class="section-content" v-html="selectedQuestion.explanation"></div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="danger" plain @click="removeFavorite(selectedQuestion)">
          <el-icon><StarFilled /></el-icon>
          取消收藏
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Star,
  StarFilled,
  TrendCharts,
  CircleCheck,
  CaretRight,
  Delete,
  View,
  Edit
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { favoriteApi } from '@/api/modules/favorite'
import { subjectApi } from '@/api/modules/subject'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const loading = ref(false)

const stats = ref({
  totalCount: 0,
  weekCount: 0,
  practicedCount: 0
})

const subjects = ref<any[]>([])
const favorites = ref<any[]>([])
const selectedFavorites = ref<any[]>([])
const selectedQuestion = ref<any>()
const showDetailDialog = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)

const filters = ref({
  subjectId: null,
  type: '',
  sortBy: 'createTime'
})

// 过滤后的收藏
const filteredFavorites = computed(() => {
  let result = [...favorites.value]

  if (filters.value.subjectId) {
    result = result.filter(f => f.subjectId === filters.value.subjectId)
  }

  if (filters.value.type) {
    result = result.filter(f => f.type === filters.value.type)
  }

  // 排序
  result.sort((a, b) => {
    if (filters.value.sortBy === 'createTime') {
      return new Date(b.favoriteAt).getTime() - new Date(a.favoriteAt).getTime()
    } else if (filters.value.sortBy === 'difficulty') {
      const diffMap: Record<string, number> = { EASY: 1, MEDIUM: 2, HARD: 3 }
      return (diffMap[b.difficulty] || 0) - (diffMap[a.difficulty] || 0)
    } else if (filters.value.sortBy === 'practiceCount') {
      return (b.practiceCount || 0) - (a.practiceCount || 0)
    }
    return 0
  })

  return result
})

// 分页后的收藏
const paginatedFavorites = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFavorites.value.slice(start, end)
})

onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载统计信息
    const statsRes = await favoriteApi.getStats()
    stats.value = statsRes.data
    
    // 加载学科列表
    const subjectsRes = await subjectApi.list()
    subjects.value = subjectsRes.data || []
    
    // 加载收藏列表
    const favoritesRes = await favoriteApi.getAllMyFavorites()
    
    // 解析并处理收藏数据
    favorites.value = (favoritesRes.data || []).map((fav: any) => {
      // 解析选项
      let options = null
      if (fav.options) {
        if (typeof fav.options === 'string') {
          try {
            options = JSON.parse(fav.options)
          } catch (e) {
            options = null
          }
        } else {
          options = fav.options
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
        id: fav.favoriteId,
        questionId: fav.questionId,
        type: fav.type,
        difficulty: fav.difficulty,
        content: fav.content,
        subjectId: fav.subjectId,
        subjectName: fav.subjectName,
        chapterId: fav.chapterId,
        chapterName: fav.chapterName,
        options,
        answer: fav.answer,
        explanation: fav.explanation,
        practiceCount: fav.practiceCount || 0,
        favoriteAt: fav.favoriteAt
      }
    })
  } catch (error: any) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
}

// 选择变化
const handleSelectionChange = (val: any[]) => {
  selectedFavorites.value = val
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
  if (filteredFavorites.value.length === 0) {
    ElMessage.warning('没有可练习的收藏题目')
    return
  }

  // 创建收藏题练习会话
  const sessionId = `collection_${Date.now()}`
  const sessionData = {
    sessionId,
    mode: '收藏题练习',
    subjectName: filters.value.subjectId ? subjects.value.find(s => s.id === filters.value.subjectId)?.name : '全部学科',
    questions: filteredFavorites.value.map(f => ({
      id: f.questionId,
      type: f.type,
      difficulty: f.difficulty,
      title: f.content,
      content: f.content,
      chapterId: f.chapterId,
      chapterName: f.chapterName,
      subjectName: f.subjectName,
      options: f.options,
      answer: f.answer,
      answerAnalysis: f.explanation
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
      answer: question.answer,
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

// 取消收藏
const removeFavorite = async (question: any) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这道题吗？', '确认取消', {
      type: 'warning'
    })

    // 调用API
    await favoriteApi.remove(question.questionId)
    
    // 从列表中移除
    const index = favorites.value.findIndex(f => f.id === question.id)
    if (index > -1) {
      favorites.value.splice(index, 1)
    }
    stats.value.totalCount--
    ElMessage.success('已取消收藏')
    showDetailDialog.value = false
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error(error.response?.data?.message || '取消收藏失败')
    }
  }
}

// 批量取消收藏
const batchRemove = async () => {
  if (selectedFavorites.value.length === 0) {
    ElMessage.warning('请先选择要取消收藏的题目')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要取消选中的 ${selectedFavorites.value.length} 道题的收藏吗？`,
      '批量取消',
      { type: 'warning' }
    )

    // 批量调用API取消收藏
    const promises = selectedFavorites.value.map(f => favoriteApi.remove(f.questionId))
    await Promise.all(promises)

    // 从列表中移除
    selectedFavorites.value.forEach(f => {
      const index = favorites.value.findIndex(fav => fav.id === f.id)
      if (index > -1) {
        favorites.value.splice(index, 1)
      }
    })

    stats.value.totalCount -= selectedFavorites.value.length
    selectedFavorites.value = []
    ElMessage.success('已批量取消收藏')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量取消失败:', error)
      ElMessage.error('批量取消失败')
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

const getDifficultyName = (difficulty: string) => {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return map[difficulty] || difficulty
}

const formatAnswer = (answer: any) => {
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

const formatTime = (date: Date) => {
  return dayjs(date).fromNow()
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.collection-page {
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

      &.primary {
        background: $primary-lightest;
        color: $primary-color;
      }

      &.success {
        background: rgba(0, 180, 42, 0.1);
        color: $success-color;
      }

      &.warning {
        background: rgba(255, 193, 7, 0.1);
        color: $warning-color;
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

// 收藏列表卡片
.favorites-card {
  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

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
}
</style>
