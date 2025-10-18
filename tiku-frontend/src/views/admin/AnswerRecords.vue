<template>
  <div class="page-container admin-answer-records">
    <div class="page-header">
      <h2>答题记录管理</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-loading="statsLoading">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">总答题数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">正确数</div>
          <div class="stat-value success">{{ stats.correctCount }}</div>
          <div class="stat-sub">正确率 {{ stats.correctRate?.toFixed(1) }}%</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">今日答题</div>
          <div class="stat-value primary">{{ stats.todayCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">本周答题</div>
          <div class="stat-value warning">{{ stats.weekCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card>
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-input
          v-model="searchUserId"
          placeholder="输入用户ID"
          style="width: 200px"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        
        <el-select 
          v-model="searchSubjectId" 
          placeholder="选择学科" 
          clearable
          style="width: 200px"
        >
          <el-option 
            v-for="subject in subjectList" 
            :key="subject.id" 
            :label="subject.name" 
            :value="subject.id"
          />
        </el-select>

        <el-select 
          v-model="searchIsCorrect" 
          placeholder="答题结果" 
          clearable
          style="width: 150px"
        >
          <el-option label="正确" :value="true" />
          <el-option label="错误" :value="false" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <!-- 答题记录表格 -->
      <el-table
        :data="records"
        v-loading="loading"
        stripe
        style="margin-top: 16px"
      >
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="用户" prop="userId" width="80" />
        <el-table-column label="题目" prop="questionId" min-width="100">
          <template #default="{ row }">
            <div class="question-info">
              <div>ID: {{ row.questionId }}</div>
              <div class="question-content">{{ row.question?.content?.substring(0, 30) }}...</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="学科/章节" min-width="150">
          <template #default="{ row }">
            <div>{{ row.question?.subject?.name }}</div>
            <div class="text-secondary">{{ row.question?.chapter?.name }}</div>
          </template>
        </el-table-column>
        <el-table-column label="用户答案" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatAnswer(row.userAnswer) }}
          </template>
        </el-table-column>
        <el-table-column label="分数" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect ? 'success' : 'danger'">
              {{ row.score }} / {{ row.question?.score }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否正确" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isCorrect ? 'success' : 'danger'" size="small">
              {{ row.isCorrect ? '正确' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="答题时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.answeredAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="danger" size="small" @click="deleteRecord(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRecords"
        @current-change="loadRecords"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="答题详情" width="700px">
      <div v-if="currentRecord" class="record-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">
            {{ currentRecord.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="题目ID">
            {{ currentRecord.questionId }}
          </el-descriptions-item>
          <el-descriptions-item label="学科/章节">
            {{ currentRecord.question?.subject?.name }} / {{ currentRecord.question?.chapter?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="题目类型">
            <el-tag size="small">{{ formatQuestionType(currentRecord.question?.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="题目内容">
            <div class="text-content">{{ currentRecord.question?.title || currentRecord.question?.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="用户答案">
            <div class="answer-content">
              <el-tag>{{ formatAnswer(currentRecord.userAnswer) }}</el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="正确答案">
            <div class="answer-content">
              <el-tag type="success">{{ formatAnswer(currentRecord.question?.answer) }}</el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="得分">
            <el-tag :type="currentRecord.isCorrect ? 'success' : 'danger'">
              {{ currentRecord.score }} / {{ currentRecord.question?.score }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="判题类型">
            <el-tag :type="currentRecord.gradingType === 'AI' ? 'primary' : 'info'" size="small">
              {{ currentRecord.gradingType === 'AI' ? 'AI判题' : '自动判题' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="答题时间">
            {{ formatDate(currentRecord.answeredAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.question?.answerAnalysis" label="题目解析">
            <div class="text-content">{{ currentRecord.question.answerAnalysis }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Delete } from '@element-plus/icons-vue'
import request from '@/api/request'
import { subjectApi } from '@/api'
import type { Subject, AnswerRecord } from '@/types'
import dayjs from 'dayjs'

const loading = ref(false)
const statsLoading = ref(false)
const detailVisible = ref(false)
const currentRecord = ref<AnswerRecord | null>(null)

const subjectList = ref<Subject[]>([])
const records = ref<AnswerRecord[]>([])

const searchUserId = ref('')
const searchSubjectId = ref<number>()
const searchIsCorrect = ref<boolean>()

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const stats = reactive({
  totalCount: 0,
  correctCount: 0,
  correctRate: 0,
  todayCount: 0,
  weekCount: 0
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatQuestionType = (type: string | undefined) => {
  if (!type) return '-'
  
  const typeMap: Record<string, string> = {
    'SINGLE': '单选题',
    'MULTIPLE': '多选题',
    'JUDGE': '判断题',
    'FILL': '填空题',
    'SHORT_ANSWER': '简答题',
    'ESSAY': '论述题',
    'CASE_ANALYSIS': '案例分析',
    'MATERIAL_ANALYSIS': '材料分析',
    'CALCULATION': '计算题',
    'ORDERING': '排序题',
    'MATCHING': '匹配题',
    'COMPOSITE': '组合题'
  }
  return typeMap[type] || type
}

const formatAnswer = (answer: any) => {
  if (answer === null || answer === undefined) return '-'
  
  // 如果answer是字符串，尝试解析为JSON
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
        answer = parsed.answer
      } else {
        // 如果解析成功但没有answer字段，直接使用原字符串
        return answer
      }
    } catch {
      // 如果不是JSON，直接返回原字符串
      return answer
    }
  }
  
  // 如果answer是对象，提取answer字段
  if (answer && typeof answer === 'object' && 'answer' in answer) {
    answer = answer.answer
  }
  
  // 格式化最终答案
  if (Array.isArray(answer)) {
    return answer.join('、')
  }
  if (answer === true || answer === 'true') return '正确'
  if (answer === false || answer === 'false') return '错误'
  if (typeof answer === 'object') return JSON.stringify(answer)
  
  return String(answer)
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.list()
    subjectList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科列表失败')
  }
}

const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/admin/answer-records/statistics')
    Object.assign(stats, res.data)
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchUserId.value) params.userId = searchUserId.value
    if (searchSubjectId.value) params.subjectId = searchSubjectId.value
    if (searchIsCorrect.value !== undefined) params.isCorrect = searchIsCorrect.value

    const res = await request.get('/admin/answer-records', { params })
    records.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载答题记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadRecords()
}

const handleReset = () => {
  searchUserId.value = ''
  searchSubjectId.value = undefined
  searchIsCorrect.value = undefined
  pagination.page = 1
  loadRecords()
}

const viewDetail = (record: AnswerRecord) => {
  currentRecord.value = record
  detailVisible.value = true
}

const deleteRecord = async (record: AnswerRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条答题记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.delete(`/admin/answer-records/${record.id}`)
    ElMessage.success('删除成功')
    await loadRecords()
    await loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadSubjects()
  loadStatistics()
  loadRecords()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-answer-records {
  .page-header {
    margin-bottom: $spacing-lg;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .stats-row {
    margin-bottom: $spacing-lg;

    .stat-card {
      background: $bg-white;
      border-radius: $border-radius-lg;
      padding: $spacing-lg;
      border: 1px solid $border-color;
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: $text-primary;

        &.primary {
          color: $primary-color;
        }

        &.success {
          color: $success-color;
        }

        &.warning {
          color: $warning-color;
        }
      }

      .stat-sub {
        font-size: 12px;
        color: $text-secondary;
        margin-top: 4px;
      }
    }
  }

  .filter-bar {
    display: flex;
    gap: $spacing-md;
    flex-wrap: wrap;
  }

  .question-info {
    .question-content {
      font-size: 12px;
      color: $text-secondary;
      margin-top: 4px;
    }
  }

  .text-secondary {
    font-size: 12px;
    color: $text-secondary;
  }

  .record-detail {
    max-height: 600px;
    overflow-y: auto;

    .text-content {
      max-height: 200px;
      overflow-y: auto;
      line-height: 1.6;
      padding: 8px;
      background-color: $bg-gray;
      border-radius: $border-radius-sm;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .answer-content {
      .el-tag {
        max-width: 100%;
        white-space: normal;
        height: auto;
        line-height: 1.6;
        padding: 8px 12px;
      }
    }
  }
}
</style>

