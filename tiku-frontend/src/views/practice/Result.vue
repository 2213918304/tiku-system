<template>
  <div class="practice-result-page">
    <div class="result-container" v-loading="loading">
      <!-- 结果卡片 -->
      <el-card class="result-card">
        <div class="result-header">
          <div class="result-icon" :class="getResultLevel()">
            <el-icon :size="80">
              <Trophy v-if="resultData.accuracy >= 80" />
              <Medal v-else-if="resultData.accuracy >= 60" />
              <CircleClose v-else />
            </el-icon>
          </div>
          
          <h1 class="result-title">{{ getResultTitle() }}</h1>
          <p class="result-subtitle">{{ resultData.subjectName }} - {{ getModeName() }}</p>
        </div>

        <!-- 统计数据 -->
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-label">答题数</div>
            <div class="stat-value">{{ resultData.answeredCount }}/{{ resultData.totalQuestions }}</div>
          </div>
          
          <div class="stat-item">
            <div class="stat-label">正确数</div>
            <div class="stat-value correct">{{ resultData.correctCount }}</div>
          </div>
          
          <div class="stat-item">
            <div class="stat-label">错误数</div>
            <div class="stat-value wrong">{{ resultData.answeredCount - resultData.correctCount }}</div>
          </div>
          
          <div class="stat-item">
            <div class="stat-label">正确率</div>
            <div class="stat-value" :class="getAccuracyClass()">{{ resultData.accuracy }}%</div>
          </div>
          
          <div class="stat-item" v-if="resultData.totalScore">
            <div class="stat-label">总得分</div>
            <div class="stat-value">{{ Math.round(resultData.totalScore) }}</div>
          </div>
          
          <div class="stat-item" v-if="resultData.timeUsed">
            <div class="stat-label">用时</div>
            <div class="stat-value">{{ formatTime(resultData.timeUsed) }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="result-actions">
          <el-button size="large" @click="viewDetails">
            <el-icon><Document /></el-icon>
            查看详情
          </el-button>
          <el-button size="large" @click="reviewWrongQuestions" v-if="hasWrongQuestions">
            <el-icon><TrendCharts /></el-icon>
            查看错题
          </el-button>
          <el-button type="primary" size="large" @click="continueParctice">
            <el-icon><Refresh /></el-icon>
            继续练习
          </el-button>
        </div>
      </el-card>

      <!-- 题目详情（可展开） -->
      <el-card class="details-card" v-if="showDetails">
        <template #header>
          <span>答题详情</span>
        </template>
        
        <div class="question-list">
          <div
            v-for="(question, index) in resultData.questions"
            :key="question.id"
            class="question-result-item"
          >
            <div class="question-result-header">
              <div class="question-number">
                <span>第 {{ index + 1 }} 题</span>
                <el-tag :type="getQuestionTypeColor(question.type)" size="small">
                  {{ getQuestionTypeName(question.type) }}
                </el-tag>
              </div>
              <div class="question-status">
                <el-icon v-if="getAnswerStatus(index)?.correct" class="icon-correct">
                  <CircleCheck />
                </el-icon>
                <el-icon v-else-if="getAnswerStatus(index)?.correct === false" class="icon-wrong">
                  <CircleClose />
                </el-icon>
                <el-tag v-else type="info" size="small">未答</el-tag>
              </div>
            </div>
            
            <div class="question-content">
              <div class="question-title" v-html="question.content || question.title"></div>
              
              <!-- 选项 -->
              <div v-if="question.options" class="question-options">
                <div
                  v-for="(option, idx) in question.options"
                  :key="idx"
                  :class="[
                    'option-item',
                    {
                      correct: isCorrectOption(question, option.key),
                      wrong: isWrongOption(question, index, option.key),
                      selected: isSelectedOption(question, index, option.key)
                    }
                  ]"
                >
                  <span class="option-key">{{ option.key }}.</span>
                  <span class="option-value">{{ option.value }}</span>
                </div>
              </div>
              
              <!-- 用户答案 -->
              <div class="answer-section">
                <span class="answer-label">你的答案：</span>
                <span class="answer-content">
                  {{ formatUserAnswer(index) || '未作答' }}
                </span>
              </div>
              
              <!-- 正确答案 -->
              <div class="answer-section correct-answer">
                <span class="answer-label">正确答案：</span>
                <span class="answer-content">
                  {{ formatAnswer(question.answer) }}
                </span>
              </div>
              
              <!-- 解析 -->
              <div v-if="question.answerAnalysis || question.explanation" class="answer-analysis">
                <div class="analysis-title">
                  <el-icon><Reading /></el-icon>
                  答案解析
                </div>
                <div class="analysis-content" v-html="question.answerAnalysis || question.explanation"></div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy,
  Medal,
  CircleClose,
  CircleCheck,
  Document,
  TrendCharts,
  Refresh,
  Reading
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const showDetails = ref(false)
const resultData = ref<any>({
  sessionId: '',
  mode: '',
  subjectName: '',
  totalQuestions: 0,
  answeredCount: 0,
  correctCount: 0,
  accuracy: 0,
  totalScore: 0,
  timeUsed: 0,
  answerStatus: {},
  questions: [],
  userAnswers: []
})

const userAnswersMap = ref<Map<number, any>>(new Map())

const hasWrongQuestions = computed(() => {
  return resultData.value.answeredCount - resultData.value.correctCount > 0
})

onMounted(() => {
  loadResult()
})

// 加载结果
const loadResult = () => {
  const sessionId = route.query.sessionId as string
  
  if (!sessionId) {
    ElMessage.error('缺少会话ID')
    router.push('/practice')
    return
  }

  try {
    const resultDataStr = sessionStorage.getItem(`practice_result_${sessionId}`)
    
    if (resultDataStr) {
      const data = JSON.parse(resultDataStr)
      resultData.value = data
      
      // 转换用户答案为Map
      if (data.userAnswers) {
        userAnswersMap.value = new Map(data.userAnswers)
      }
    } else {
      throw new Error('未找到结果数据')
    }
  } catch (error: any) {
    console.error('加载结果失败:', error)
    ElMessage.error('加载结果失败')
    router.push('/practice')
  }
}

// 获取结果等级
const getResultLevel = () => {
  const accuracy = resultData.value.accuracy
  if (accuracy >= 80) return 'excellent'
  if (accuracy >= 60) return 'good'
  return 'poor'
}

// 获取结果标题
const getResultTitle = () => {
  const accuracy = resultData.value.accuracy
  if (accuracy >= 90) return '太棒了！'
  if (accuracy >= 80) return '表现优秀！'
  if (accuracy >= 70) return '继续努力！'
  if (accuracy >= 60) return '还需加油！'
  return '需要多多练习哦'
}

// 获取模式名称
const getModeName = () => {
  const modeMap: Record<string, string> = {
    SEQUENTIAL: '顺序练习',
    RANDOM: '随机练习',
    CHAPTER: '章节练习',
    EXAM: '模拟考试',
    WRONG_QUESTION: '错题练习',
    FAVORITE: '收藏专练',
    CHALLENGE: '闯关模式',
    TIMED: '限时挑战',
    SMART_RECOMMEND: '智能推荐'
  }
  return modeMap[resultData.value.mode] || resultData.value.mode
}

// 获取正确率样式
const getAccuracyClass = () => {
  const accuracy = resultData.value.accuracy
  if (accuracy >= 80) return 'excellent'
  if (accuracy >= 60) return 'good'
  return 'poor'
}

// 格式化时间
const formatTime = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

// 题型颜色
const getQuestionTypeColor = (type: string) => {
  const map: Record<string, any> = {
    SINGLE: 'primary',
    MULTIPLE: 'success',
    JUDGE: 'warning',
    FILL: 'info',
    SHORT_ANSWER: 'danger',
    ESSAY: 'danger'
  }
  return map[type] || 'info'
}

// 题型名称
const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    FILL: '填空题',
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题'
  }
  return map[type] || type
}

// 获取答题状态
const getAnswerStatus = (index: number) => {
  return resultData.value.answerStatus[index]
}

// 格式化答案
const formatAnswer = (answer: any) => {
  // 如果是字符串，尝试解析JSON
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      // 如果解析后是对象且有answer字段，使用该字段
      if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
        answer = parsed.answer
      }
    } catch {
      // 不是JSON，继续使用原值
    }
  }
  
  // 如果是对象且有answer字段
  if (answer && typeof answer === 'object' && 'answer' in answer) {
    answer = answer.answer
  }
  
  // 格式化最终答案
  if (Array.isArray(answer)) {
    return answer.join('、')
  }
  if (answer === 'true' || answer === true) return '正确'
  if (answer === 'false' || answer === false) return '错误'
  return answer
}

// 格式化用户答案
const formatUserAnswer = (index: number) => {
  const question = resultData.value.questions[index]
  const userAnswer = userAnswersMap.value.get(question.id)
  return formatAnswer(userAnswer)
}

// 是否是正确选项
const isCorrectOption = (question: any, optionKey: string) => {
  let answer = question.answer
  
  // 如果是字符串，尝试解析JSON
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
        answer = parsed.answer
      }
    } catch {
      // 不是JSON，继续使用原值
    }
  }
  
  // 如果是对象且有answer字段
  if (answer && typeof answer === 'object' && 'answer' in answer) {
    answer = answer.answer
  }
  
  // 判断是否匹配
  if (Array.isArray(answer)) {
    return answer.includes(optionKey)
  }
  return answer === optionKey
}

// 是否是错误选项
const isWrongOption = (question: any, index: number, optionKey: string) => {
  const userAnswer = userAnswersMap.value.get(question.id)
  if (!userAnswer) return false
  
  const isSelected = Array.isArray(userAnswer)
    ? userAnswer.includes(optionKey)
    : userAnswer === optionKey
    
  return isSelected && !isCorrectOption(question, optionKey)
}

// 是否是用户选择的选项
const isSelectedOption = (question: any, index: number, optionKey: string) => {
  const userAnswer = userAnswersMap.value.get(question.id)
  if (!userAnswer) return false
  
  if (Array.isArray(userAnswer)) {
    return userAnswer.includes(optionKey)
  }
  return userAnswer === optionKey
}

// 查看详情
const viewDetails = () => {
  showDetails.value = !showDetails.value
}

// 查看错题
const reviewWrongQuestions = () => {
  router.push('/wrong')
}

// 继续练习
const continueParctice = () => {
  router.push('/practice')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.practice-result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.05) 0%, rgba(138, 43, 226, 0.05) 100%);
  padding: $spacing-xl;
}

.result-container {
  max-width: 900px;
  margin: 0 auto;
}

// 结果卡片
.result-card {
  margin-bottom: $spacing-lg;
  text-align: center;

  .result-header {
    padding: $spacing-xl 0;

    .result-icon {
      margin: 0 auto $spacing-lg;
      width: 120px;
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      
      &.excellent {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
        color: $bg-white;
        box-shadow: 0 4px 20px rgba(255, 215, 0, 0.4);
      }
      
      &.good {
        background: linear-gradient(135deg, $success-color 0%, #52c41a 100%);
        color: $bg-white;
        box-shadow: 0 4px 20px rgba(0, 180, 42, 0.4);
      }
      
      &.poor {
        background: linear-gradient(135deg, $danger-color 0%, #ff7875 100%);
        color: $bg-white;
        box-shadow: 0 4px 20px rgba(245, 63, 63, 0.4);
      }
    }

    .result-title {
      font-size: 32px;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: $spacing-sm;
    }

    .result-subtitle {
      font-size: 16px;
      color: $text-secondary;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: $spacing-lg;
    padding: $spacing-xl;
    background: $bg-gray;
    border-radius: $border-radius-lg;
    margin-bottom: $spacing-lg;

    .stat-item {
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

        &.correct {
          color: $success-color;
        }

        &.wrong {
          color: $danger-color;
        }

        &.excellent {
          color: #FFD700;
        }

        &.good {
          color: $success-color;
        }

        &.poor {
          color: $danger-color;
        }
      }
    }
  }

  .result-actions {
    display: flex;
    gap: $spacing-md;
    justify-content: center;
    flex-wrap: wrap;
    padding-top: $spacing-lg;

    .el-button {
      min-width: 140px;
    }
  }
}

// 详情卡片
.details-card {
  .question-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-lg;

    .question-result-item {
      padding: $spacing-lg;
      background: $bg-gray;
      border-radius: $border-radius-lg;
      border: 2px solid $border-light;

      .question-result-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-md;
        padding-bottom: $spacing-sm;
        border-bottom: 1px solid $border-light;

        .question-number {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          font-weight: 600;
        }

        .question-status {
          .icon-correct {
            color: $success-color;
            font-size: 24px;
          }

          .icon-wrong {
            color: $danger-color;
            font-size: 24px;
          }
        }
      }

      .question-content {
        .question-title {
          font-size: 15px;
          line-height: 1.8;
          color: $text-primary;
          margin-bottom: $spacing-md;
        }

        .question-options {
          margin: $spacing-md 0;

          .option-item {
            padding: $spacing-sm $spacing-md;
            margin-bottom: 8px;
            border: 1px solid $border-color;
            border-radius: $border-radius-md;
            display: flex;
            gap: 8px;

            &.correct {
              background: rgba(0, 180, 42, 0.1);
              border-color: $success-color;
              color: $success-color;
              font-weight: 600;
            }

            &.wrong {
              background: rgba(245, 63, 63, 0.1);
              border-color: $danger-color;
              color: $danger-color;
            }

            &.selected:not(.correct):not(.wrong) {
              background: $primary-lightest;
              border-color: $primary-lighter;
            }

            .option-key {
              font-weight: 600;
              flex-shrink: 0;
            }

            .option-value {
              flex: 1;
            }
          }
        }

        .answer-section {
          padding: $spacing-sm $spacing-md;
          margin: $spacing-sm 0;
          background: $bg-white;
          border-radius: $border-radius-md;
          font-size: 14px;

          .answer-label {
            font-weight: 600;
            color: $text-secondary;
            margin-right: 8px;
          }

          .answer-content {
            color: $text-primary;
          }

          &.correct-answer {
            background: rgba(0, 180, 42, 0.1);
            
            .answer-label {
              color: $success-color;
            }

            .answer-content {
              color: $success-color;
              font-weight: 600;
            }
          }
        }

        .answer-analysis {
          margin-top: $spacing-md;
          padding: $spacing-md;
          background: $bg-white;
          border-left: 3px solid $primary-color;
          border-radius: $border-radius-md;

          .analysis-title {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 600;
            color: $primary-color;
            margin-bottom: $spacing-sm;
          }

          .analysis-content {
            font-size: 14px;
            line-height: 1.8;
            color: $text-secondary;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .practice-result-page {
    padding: $spacing-md;
  }

  .result-card {
    .result-header .result-title {
      font-size: 24px;
    }

    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: $spacing-md;

      .stat-item .stat-value {
        font-size: 22px;
      }
    }

    .result-actions {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>





