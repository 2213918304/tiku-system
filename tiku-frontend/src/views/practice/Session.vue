<template>
  <div class="practice-session" :class="`mode-${practiceMode}`">
    <!-- 模式特色顶部栏 -->
    <div class="session-header" :class="`header-${practiceMode}`">
      <div class="header-left">
        <el-button text @click="showExitDialog = true">
          <el-icon><ArrowLeft /></el-icon>
          退出练习
        </el-button>
      </div>
      
      <div class="header-center">
        <!-- 模式标识 -->
        <div class="mode-badge">
          <el-icon><component :is="modeIcon" /></el-icon>
          {{ modeName }}
        </div>
        
        <div class="progress-info">
          <span class="current-num">{{ currentIndex + 1 }}</span>
          <span class="divider">/</span>
          <span class="total-num">{{ questions.length }}</span>
        </div>
        <el-progress
          :percentage="progressPercentage"
          :show-text="false"
          :stroke-width="6"
          :color="progressColor"
          class="progress-bar"
        />
      </div>
      
      <div class="header-right">
        <!-- 限时挑战：每题倒计时 -->
        <div v-if="practiceMode === 'TIMED'" class="timer timer-per-question">
          <el-icon><Timer /></el-icon>
          <span :class="{ warning: questionTimeRemaining < 10, danger: questionTimeRemaining < 5 }">
            {{ questionTimeRemaining }}s
          </span>
          <el-progress 
            :percentage="(questionTimeRemaining / timePerQuestion) * 100" 
            :show-text="false"
            :color="getTimerColor()"
            :stroke-width="4"
            style="width: 60px"
          />
        </div>
        
        <!-- 模拟考试：总时间倒计时 -->
        <div v-else-if="showTimer" class="timer">
          <el-icon><Timer /></el-icon>
          <span :class="{ warning: remainingSeconds < 300 }">
            {{ formatTime(remainingSeconds) }}
          </span>
        </div>
        
        <!-- 顺序练习：知识点进度 -->
        <div v-else-if="practiceMode === 'SEQUENTIAL'" class="knowledge-progress">
          <el-icon><TrendCharts /></el-icon>
          <span>已学 {{ answeredCount }}/{{ questions.length }}</span>
        </div>
        
        <!-- 错题练习：错误率 -->
        <div v-else-if="practiceMode === 'WRONG_QUESTION'" class="error-rate">
          <el-icon><Warning /></el-icon>
          <span>掌握率 {{ masteryRate }}%</span>
        </div>
        
        <!-- 随机练习：连击数 -->
        <div v-else-if="practiceMode === 'RANDOM'" class="combo-counter">
          <el-icon><Star /></el-icon>
          <span class="combo-text">{{ combo }}连对</span>
        </div>
      </div>
    </div>

    <!-- 答题区域 -->
    <div class="session-content">
      <div v-if="currentQuestion" class="question-container">
        <!-- 限时挑战：题目卡片带进度环 -->
        <el-card class="question-card" :class="{ 'time-pressure': practiceMode === 'TIMED' && questionTimeRemaining < 10 }">
          <!-- 限时挑战：时间进度环 -->
          <div v-if="practiceMode === 'TIMED'" class="time-ring-overlay">
            <svg class="time-ring" viewBox="0 0 100 100">
              <circle
                class="time-ring-bg"
                cx="50"
                cy="50"
                r="45"
                fill="none"
                stroke="#e5e7eb"
                stroke-width="8"
              />
              <circle
                class="time-ring-progress"
                cx="50"
                cy="50"
                r="45"
                fill="none"
                stroke-width="8"
                :stroke-dasharray="`${(questionTimeRemaining / timePerQuestion) * 282.7} 282.7`"
                :stroke="getTimerColor()"
                transform="rotate(-90 50 50)"
              />
            </svg>
          </div>

          <div class="question-header">
            <el-tag :type="getQuestionTypeColor(currentQuestion.type as string)">
              {{ getQuestionTypeName(currentQuestion.type as string) }}
            </el-tag>
            <el-tag v-if="currentQuestion.difficulty" :type="getDifficultyTagType(currentQuestion.difficulty as string)">
              {{ getDifficultyName(currentQuestion.difficulty as string) }}
            </el-tag>
            
            <!-- 顺序练习：显示知识点 -->
            <el-tag v-if="practiceMode === 'SEQUENTIAL' && (currentQuestion as any).knowledgePoint" type="info">
              {{ (currentQuestion as any).knowledgePoint }}
            </el-tag>
            
            <!-- 错题练习：显示错误次数 -->
            <el-tag v-if="practiceMode === 'WRONG_QUESTION'" type="danger">
              已错 {{ (currentQuestion as any).wrongCount || 1 }} 次
            </el-tag>
            
            <div class="question-actions">
              <el-button
                text
                :class="{ active: isFavorite }"
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>

          <div class="question-content">
            <div class="question-title">
              <span class="question-number">{{ currentIndex + 1 }}. </span>
              <span v-html="currentQuestion.title || currentQuestion.content"></span>
            </div>

            <!-- 题目材料（如果有） -->
            <div v-if="(currentQuestion as any).material" class="question-material">
              <div class="material-label">
                <el-icon><Document /></el-icon>
                阅读材料
              </div>
              <div class="material-content" v-html="(currentQuestion as any).material"></div>
            </div>
          </div>

          <!-- 答题区域 -->
          <div class="answer-area">
            <!-- 单选题 -->
            <el-radio-group
              v-if="currentQuestion.type === 'SINGLE'"
              v-model="userAnswer"
              class="answer-options"
              :disabled="practiceMode === 'EXAM' && showAnalysis"
            >
              <el-radio
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                :label="option.key"
                class="answer-option"
              >
                <span class="option-key">{{ option.key }}.</span>
                <span class="option-content" v-html="option.value"></span>
              </el-radio>
            </el-radio-group>

            <!-- 多选题 -->
            <el-checkbox-group
              v-else-if="currentQuestion.type === 'MULTIPLE'"
              v-model="userAnswer"
              class="answer-options"
              :disabled="practiceMode === 'EXAM' && showAnalysis"
            >
              <el-checkbox
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                :label="option.key"
                class="answer-option"
              >
                <span class="option-key">{{ option.key }}.</span>
                <span class="option-content" v-html="option.value"></span>
              </el-checkbox>
            </el-checkbox-group>

            <!-- 判断题 -->
            <el-radio-group
              v-else-if="currentQuestion.type === 'JUDGE'"
              v-model="userAnswer"
              class="answer-options judge-options"
              :disabled="practiceMode === 'EXAM' && showAnalysis"
            >
              <el-radio label="true" class="judge-option">
                <el-icon><Select /></el-icon>
                正确
              </el-radio>
              <el-radio label="false" class="judge-option">
                <el-icon><CloseBold /></el-icon>
                错误
              </el-radio>
            </el-radio-group>

            <!-- 填空题 -->
            <div v-else-if="currentQuestion.type === 'FILL'" class="fill-answer">
              <el-input
                v-for="(_blank, index) in (currentQuestion as any).blankCount || 1"
                :key="index"
                v-model="userAnswer[index]"
                :placeholder="`第 ${index + 1} 空`"
                :disabled="practiceMode === 'EXAM' && showAnalysis"
                class="fill-input"
              />
            </div>

            <!-- 简答题/问答题 -->
            <el-input
              v-else-if="['SHORT_ANSWER', 'ESSAY'].includes(currentQuestion.type)"
              v-model="userAnswer"
              type="textarea"
              :rows="8"
              :placeholder="`请输入你的答案...${currentQuestion.type === 'ESSAY' ? '（将使用AI进行评分）' : ''}`"
              :disabled="practiceMode === 'EXAM' && showAnalysis"
              class="text-answer"
            />
          </div>

          <!-- 笔记区域 -->
          <div v-if="showNoteArea" class="note-area">
            <div class="note-header">
              <el-icon><Edit /></el-icon>
              <span>我的笔记</span>
            </div>
            <el-input
              v-model="currentNote"
              type="textarea"
              :rows="3"
              placeholder="记录你的想法、解题思路..."
              class="note-input"
            />
          </div>
        </el-card>

        <!-- 答题解析（根据模式决定是否显示） -->
        <el-card v-if="canShowAnalysis && currentQuestionResult" class="analysis-card">
          <div class="analysis-header">
            <div class="result-tag">
              <el-icon v-if="currentQuestionResult.isCorrect" class="icon-correct">
                <CircleCheck />
              </el-icon>
              <el-icon v-else class="icon-wrong">
                <CircleClose />
              </el-icon>
              <span :class="currentQuestionResult.isCorrect ? 'text-correct' : 'text-wrong'">
                {{ currentQuestionResult.isCorrect ? '回答正确' : '回答错误' }}
              </span>
            </div>
            
            <!-- 随机练习：显示连击 -->
            <el-tag v-if="practiceMode === 'RANDOM' && combo > 1" type="warning" effect="dark">
              🔥 {{ combo }}连击！
            </el-tag>
          </div>

          <div class="analysis-content">
            <div class="answer-section">
              <div class="section-title">正确答案</div>
              <div class="section-content correct-answer">
                {{ formatAnswer(currentQuestion.answer) }}
              </div>
            </div>

            <div v-if="currentQuestion.answerAnalysis || (currentQuestion as any).explanation" class="answer-section">
              <div class="section-title">答案解析</div>
              <div class="section-content" v-html="currentQuestion.answerAnalysis || (currentQuestion as any).explanation"></div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 题目导航侧边栏（根据模式调整） -->
      <div class="question-nav" :class="`nav-${practiceMode}`">
        <div class="nav-header">
          <span>题目导航</span>
          <el-button text @click="toggleNoteArea">
            <el-icon><Edit /></el-icon>
            笔记
          </el-button>
        </div>
        
        <!-- 顺序练习：显示章节分组 -->
        <div v-if="practiceMode === 'SEQUENTIAL' && chapterGroups.length > 0" class="chapter-groups">
          <div v-for="group in chapterGroups" :key="group.chapterId" class="chapter-group">
            <div class="chapter-name">{{ group.chapterName }}</div>
            <div class="nav-grid">
              <div
                v-for="(q, index) in group.questions"
                :key="q.id"
                :class="[
                  'nav-item',
                  {
                    active: index === currentIndex,
                    answered: answerStatus[index]?.answered,
                    correct: answerStatus[index]?.correct,
                    wrong: answerStatus[index]?.correct === false
                  }
                ]"
                @click="jumpToQuestion(index)"
              >
                {{ index + 1 }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 其他模式：普通导航 -->
        <div v-else class="nav-grid">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            :class="[
              'nav-item',
              {
                active: index === currentIndex,
                answered: answerStatus[index]?.answered,
                correct: answerStatus[index]?.correct,
                wrong: answerStatus[index]?.correct === false
              }
            ]"
            @click="jumpToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>

        <div class="nav-legend">
          <div class="legend-item">
            <span class="legend-dot answered"></span>
            <span>已答</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot correct"></span>
            <span>正确</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot wrong"></span>
            <span>错误</span>
          </div>
        </div>
        
        <!-- 错题练习：显示掌握度统计 -->
        <div v-if="practiceMode === 'WRONG_QUESTION'" class="mastery-stats">
          <el-divider>掌握度分析</el-divider>
          <div class="stat-item">
            <span>已掌握</span>
            <el-tag type="success">{{ masteredCount }}</el-tag>
          </div>
          <div class="stat-item">
            <span>待巩固</span>
            <el-tag type="warning">{{ questions.length - masteredCount }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="session-footer">
      <el-button
        :disabled="currentIndex === 0"
        @click="previousQuestion"
      >
        <el-icon><ArrowLeft /></el-icon>
        上一题
      </el-button>

      <div class="footer-center">
        <!-- 模拟考试模式：不显示立即提交按钮 -->
        <el-button
          v-if="practiceMode !== 'EXAM' && !showAnalysis"
          type="primary"
          :disabled="!hasAnswer"
          @click="submitCurrentAnswer"
        >
          提交答案
        </el-button>
        <el-button
          v-else-if="showAnalysis"
          type="success"
          @click="nextQuestion"
        >
          下一题
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        
        <!-- 限时挑战：自动跳过按钮 -->
        <el-button
          v-if="practiceMode === 'TIMED' && !showAnalysis"
          type="info"
          @click="skipQuestion"
        >
          跳过（{{ questionTimeRemaining }}s）
        </el-button>
      </div>

      <el-button
        v-if="currentIndex < questions.length - 1"
        @click="nextQuestion"
      >
        {{ showAnalysis || practiceMode === 'EXAM' ? '下一题' : '跳过' }}
        <el-icon><ArrowRight /></el-icon>
      </el-button>
      <el-button
        v-else
        type="primary"
        @click="finishPractice"
      >
        完成练习
        <el-icon><Check /></el-icon>
      </el-button>
    </div>

    <!-- 退出确认对话框 -->
    <el-dialog
      v-model="showExitDialog"
      title="确认退出"
      width="400px"
      align-center
    >
      <p>你确定要退出练习吗？当前进度将会保存。</p>
      <template #footer>
        <el-button @click="showExitDialog = false">取消</el-button>
        <el-button type="primary" @click="exitPractice">确认退出</el-button>
      </template>
    </el-dialog>
    
    <!-- 限时挑战：时间到提示 -->
    <el-dialog
      v-model="showTimeUpDialog"
      title="时间到！"
      width="400px"
      align-center
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="time-up-content">
        <el-icon class="time-up-icon"><Timer /></el-icon>
        <p>本题时间已用完，自动跳到下一题</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleTimeUp">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Timer,
  Star,
  Document,
  Edit,
  Select,
  CloseBold,
  CircleCheck,
  CircleClose,
  Check,
  TrendCharts,
  Warning,
  MagicStick,
  Trophy,
  Reading,
  Compass
} from '@element-plus/icons-vue'
import { practiceApi } from '@/api/modules/practice'
import { favoriteApi } from '@/api/modules/favorite'
import { noteApi } from '@/api/modules/note'
import type { Question, PracticeSession as PracticeSessionType, GradingResult } from '@/types'

const router = useRouter()
const route = useRoute()

// 会话信息
const sessionInfo = ref<PracticeSessionType | null>(null)
const questions = ref<Question[]>([])
const currentIndex = ref(0)
const userAnswer = ref<any>(null)
const userAnswers = ref<Map<number, any>>(new Map())
const currentNote = ref('')
const answerStatus = ref<Record<number, { answered: boolean; correct: boolean | null; result?: GradingResult }>>({})
const currentQuestionResult = ref<GradingResult | null>(null)
const showAnalysis = ref(false)
const showNoteArea = ref(false)
const isFavorite = ref(false)
const showExitDialog = ref(false)
const showTimeUpDialog = ref(false)
const loading = ref(false)
const hasUnsavedChanges = ref(false)
const isExiting = ref(false)

// 练习模式
const practiceMode = ref<string>('SEQUENTIAL')

// 计时器
const showTimer = ref(false)
const remainingSeconds = ref(3600)
let timerInterval: any = null
let autoSaveInterval: any = null

// 限时挑战专用
const timePerQuestion = ref(30) // 每题时间（秒）
const questionTimeRemaining = ref(30)
let questionTimerInterval: any = null

// 随机练习专用：连击数
const combo = ref(0)
const maxCombo = ref(0)

// 错题练习专用
const masteredCount = ref(0)

// 章节分组（顺序练习）
const chapterGroups = ref<Array<{ chapterId: number; chapterName: string; questions: Question[] }>>([])

// 当前题目
const currentQuestion = computed(() => questions.value[currentIndex.value])

// 进度百分比
const progressPercentage = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round(((currentIndex.value + 1) / questions.value.length) * 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const percentage = progressPercentage.value
  if (practiceMode.value === 'TIMED') {
    return percentage < 30 ? '#f56c6c' : percentage < 70 ? '#e6a23c' : '#67c23a'
  }
  return '#409eff'
})

// 是否已作答
const hasAnswer = computed(() => {
  if (!userAnswer.value) return false
  if (Array.isArray(userAnswer.value)) {
    return userAnswer.value.length > 0 && userAnswer.value.every(a => a !== null && a !== undefined && a !== '')
  }
  return !!userAnswer.value
})

// 已答题数
const answeredCount = computed(() => {
  return Object.values(answerStatus.value).filter((s: any) => s.answered).length
})

// 掌握率（错题练习）
const masteryRate = computed(() => {
  if (answeredCount.value === 0) return 0
  const correct = Object.values(answerStatus.value).filter((s: any) => s.correct === true).length
  return Math.round((correct / answeredCount.value) * 100)
})

// 是否可以显示解析
const canShowAnalysis = computed(() => {
  // 模拟考试模式：完成后才显示解析
  if (practiceMode.value === 'EXAM') {
    return false
  }
  return showAnalysis.value
})

// 模式名称
const modeName = computed(() => {
  const names: Record<string, string> = {
    'SEQUENTIAL': '顺序练习',
    'RANDOM': '随机练习',
    'TIMED': '限时挑战',
    'EXAM': '模拟考试',
    'WRONG_QUESTION': '错题练习',
    'CHAPTER': '章节练习',
    'FAVORITE': '收藏专练'
  }
  return names[practiceMode.value] || '练习模式'
})

// 模式图标
const modeIcon = computed(() => {
  const icons: Record<string, any> = {
    'SEQUENTIAL': Reading,
    'RANDOM': MagicStick,
    'TIMED': Timer,
    'EXAM': Trophy,
    'WRONG_QUESTION': TrendCharts,
    'CHAPTER': Document,
    'FAVORITE': Star
  }
  return icons[practiceMode.value] || Compass
})

// 监听题目切换
watch(currentIndex, () => {
  loadQuestionState()
  
  // 限时挑战：重置题目计时器
  if (practiceMode.value === 'TIMED') {
    resetQuestionTimer()
  }
})

// 监听用户答案变化
watch(userAnswer, () => {
  hasUnsavedChanges.value = true
}, { deep: true })

onMounted(() => {
  loadPracticeSession()
  startAutoSave()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  stopTimer()
  stopQuestionTimer()
  stopAutoSave()
  saveProgress()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 路由守卫
onBeforeRouteLeave((_to, _from, next) => {
  if (isExiting.value) {
    next()
    return
  }
  
  ElMessageBox.confirm(
    '你确定要离开答题页面吗？当前进度已自动保存，下次可以继续作答。',
    '确认离开',
    {
      confirmButtonText: '离开',
      cancelButtonText: '继续答题',
      type: 'warning'
    }
  ).then(() => {
    saveProgress()
    isExiting.value = true
    next()
  }).catch(() => {
    next(false)
  })
})

// 处理页面刷新/关闭
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (hasUnsavedChanges.value) {
    saveProgress()
    e.preventDefault()
    e.returnValue = '你确定要离开吗？当前进度已自动保存。'
    return e.returnValue
  }
}

// 加载练习会话
const loadPracticeSession = async () => {
  const sessionId = route.query.sessionId as string
  
  if (!sessionId) {
    ElMessage.error('缺少会话ID')
    router.push('/practice')
    return
  }

  loading.value = true
  
  try {
    const sessionData = sessionStorage.getItem(`practice_session_${sessionId}`)
    
    if (sessionData) {
      const session = JSON.parse(sessionData) as PracticeSessionType
      sessionInfo.value = session
      questions.value = session.questions || []
      practiceMode.value = session.mode || 'SEQUENTIAL'
      
      // 转换题目选项格式
      questions.value = questions.value.map(q => {
        if (q.options) {
          if (typeof q.options === 'string') {
            try {
              q.options = JSON.parse(q.options)
            } catch (e) {
              q.options = []
            }
          }
          
          if (Array.isArray(q.options)) {
            q.options = q.options.map((opt: any, index: number) => {
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
        return q
      })
      
      // 设置计时器
      if (practiceMode.value === 'TIMED') {
        // 限时挑战：每题计时
        timePerQuestion.value = session.timePerQuestion || 30
        questionTimeRemaining.value = timePerQuestion.value
        showTimer.value = false
        startQuestionTimer()
      } else if (session.examDuration) {
        // 模拟考试：总时间计时
        showTimer.value = true
        remainingSeconds.value = session.examDuration * 60
        startTimer()
      }
      
      // 初始化答题状态
      questions.value.forEach((_, index) => {
        answerStatus.value[index] = { answered: false, correct: null }
      })
      
      // 顺序练习：组织章节分组
      if (practiceMode.value === 'SEQUENTIAL') {
        organizeChapterGroups()
      }
      
      // 尝试恢复进度
      const savedProgress = sessionStorage.getItem(`practice_progress_${sessionId}`)
      if (savedProgress) {
        try {
          const progress = JSON.parse(savedProgress)
          
          if (progress.currentIndex !== undefined) {
            currentIndex.value = progress.currentIndex
          }
          
          if (progress.userAnswers && Array.isArray(progress.userAnswers)) {
            userAnswers.value = new Map(progress.userAnswers)
          }
          
          if (progress.answerStatus) {
            answerStatus.value = progress.answerStatus
          }
          
          if (progress.remainingSeconds !== undefined && showTimer.value) {
            remainingSeconds.value = progress.remainingSeconds
          }
          
          if (progress.combo !== undefined) {
            combo.value = progress.combo
            maxCombo.value = progress.maxCombo || 0
          }
          
          ElMessage.success('已恢复上次答题进度')
        } catch (error) {
          console.error('恢复进度失败:', error)
        }
      }
      
      loadQuestionState()
      
    } else {
      throw new Error('会话数据不存在')
    }
  } catch (error: any) {
    console.error('加载练习会话失败:', error)
    ElMessage.error(error.message || '加载练习失败')
    router.push('/practice')
  } finally {
    loading.value = false
  }
}

// 组织章节分组（顺序练习）
const organizeChapterGroups = () => {
  const groups: Map<number, { chapterId: number; chapterName: string; questions: Question[] }> = new Map()
  
  questions.value.forEach(q => {
    const chapterId = (q as any).chapterId || 0
    const chapterName = (q as any).chapterName || '未分类'
    
    if (!groups.has(chapterId)) {
      groups.set(chapterId, { chapterId, chapterName, questions: [] })
    }
    
    groups.get(chapterId)!.questions.push(q)
  })
  
  chapterGroups.value = Array.from(groups.values())
}

// 开始题目计时（限时挑战）
const startQuestionTimer = () => {
  stopQuestionTimer()
  questionTimeRemaining.value = timePerQuestion.value
  
  questionTimerInterval = setInterval(() => {
    if (questionTimeRemaining.value > 0) {
      questionTimeRemaining.value--
    } else {
      // 时间到，自动跳过
      stopQuestionTimer()
      showTimeUpDialog.value = true
    }
  }, 1000)
}

// 停止题目计时
const stopQuestionTimer = () => {
  if (questionTimerInterval) {
    clearInterval(questionTimerInterval)
    questionTimerInterval = null
  }
}

// 重置题目计时
const resetQuestionTimer = () => {
  if (practiceMode.value === 'TIMED') {
    startQuestionTimer()
  }
}

// 处理时间到
const handleTimeUp = () => {
  showTimeUpDialog.value = false
  skipQuestion()
}

// 获取计时器颜色
const getTimerColor = () => {
  const percentage = (questionTimeRemaining.value / timePerQuestion.value) * 100
  if (percentage < 20) return '#f56c6c'
  if (percentage < 50) return '#e6a23c'
  return '#67c23a'
}

// 开始总计时
const startTimer = () => {
  timerInterval = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      stopTimer()
      ElMessageBox.alert('时间到！练习已自动提交。', '提示', {
        confirmButtonText: '确定',
        callback: () => {
          finishPractice()
        }
      })
    }
  }, 1000)
}

// 停止计时
const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// 开始自动保存
const startAutoSave = () => {
  autoSaveInterval = setInterval(() => {
    if (hasUnsavedChanges.value) {
      saveProgress()
    }
  }, 30000)
}

// 停止自动保存
const stopAutoSave = () => {
  if (autoSaveInterval) {
    clearInterval(autoSaveInterval)
    autoSaveInterval = null
  }
}

// 格式化时间
const formatTime = (seconds: number) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 题型颜色
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

// 题型名称
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

// 难度名称
const getDifficultyName = (difficulty: string) => {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return map[difficulty] || difficulty
}

// 难度标签类型
const getDifficultyTagType = (difficulty: string) => {
  const map: Record<string, any> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return map[difficulty] || 'info'
}

// 格式化答案
const formatAnswer = (answer: any) => {
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
        answer = parsed.answer
      }
    } catch {
      // 不是JSON，直接使用
    }
  }
  
  if (answer && typeof answer === 'object' && 'answer' in answer) {
    answer = answer.answer
  }
  
  if (Array.isArray(answer)) {
    return answer.join('、')
  }
  if (answer === true || answer === 'true') return '正确'
  if (answer === false || answer === 'false') return '错误'
  return answer
}

// 切换收藏
const toggleFavorite = async () => {
  if (!currentQuestion.value) return
  
  try {
    if (isFavorite.value) {
      await favoriteApi.remove(currentQuestion.value.id)
      isFavorite.value = false
      ElMessage.success('取消收藏')
    } else {
      await favoriteApi.add({ questionId: currentQuestion.value.id })
      isFavorite.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error: any) {
    console.error('切换收藏失败:', error)
  }
}

// 切换笔记区域
const toggleNoteArea = () => {
  showNoteArea.value = !showNoteArea.value
}

// 提交当前答案
const submitCurrentAnswer = async () => {
  if (!hasAnswer.value) {
    ElMessage.warning('请先作答')
    return
  }

  if (!currentQuestion.value) return

  loading.value = true
  
  try {
    const res = await practiceApi.submitAnswer({
      questionId: currentQuestion.value.id,
      userAnswer: userAnswer.value,
      timeSpent: practiceMode.value === 'TIMED' ? (timePerQuestion.value - questionTimeRemaining.value) : 0
    })

    if (res.data) {
      currentQuestionResult.value = res.data
      
      // 更新答题状态
      answerStatus.value[currentIndex.value] = {
        answered: true,
        correct: res.data.isCorrect,
        result: res.data
      }

      userAnswers.value.set(currentQuestion.value.id, userAnswer.value)
      showAnalysis.value = true
      
      // 随机练习：更新连击数
      if (practiceMode.value === 'RANDOM') {
        if (res.data.isCorrect) {
          combo.value++
          maxCombo.value = Math.max(maxCombo.value, combo.value)
          
          if (combo.value >= 5) {
            ElMessage.success(`🔥 ${combo.value}连击！太棒了！`)
          }
        } else {
          combo.value = 0
        }
      }
      
      // 错题练习：更新掌握度
      if (practiceMode.value === 'WRONG_QUESTION' && res.data.isCorrect) {
        masteredCount.value++
      }
      
      // 限时挑战：停止计时
      if (practiceMode.value === 'TIMED') {
        stopQuestionTimer()
      }

      // 保存笔记
      if (currentNote.value.trim()) {
        try {
          await noteApi.add({
            questionId: currentQuestion.value.id,
            title: `${currentQuestion.value.title?.substring(0, 50) || '笔记'}`,
            content: currentNote.value
          })
        } catch (error) {
          console.error('保存笔记失败:', error)
        }
      }
      
      saveProgress()
    }
  } catch (error: any) {
    console.error('提交答案失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

// 跳过题目
const skipQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    saveCurrentState()
    currentIndex.value++
    loadQuestionState()
  }
}

// 上一题
const previousQuestion = () => {
  if (currentIndex.value > 0) {
    saveCurrentState()
    currentIndex.value--
    loadQuestionState()
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    saveCurrentState()
    currentIndex.value++
    loadQuestionState()
  }
}

// 跳转到指定题目
const jumpToQuestion = (index: number) => {
  if (index !== currentIndex.value) {
    saveCurrentState()
    currentIndex.value = index
    loadQuestionState()
  }
}

// 保存当前状态
const saveCurrentState = () => {
  if (currentQuestion.value && userAnswer.value) {
    userAnswers.value.set(currentQuestion.value.id, userAnswer.value)
    
    // 模拟考试模式：自动标记为已答题（不需要手动提交）
    if (practiceMode.value === 'EXAM' && hasAnswer.value) {
      answerStatus.value[currentIndex.value] = {
        answered: true,
        correct: null, // 考试模式结束后才判分
        result: undefined
      }
    }
  }
}

// 加载题目状态
const loadQuestionState = () => {
  if (!currentQuestion.value) return
  
  const savedAnswer = userAnswers.value.get(currentQuestion.value.id)
  if (savedAnswer !== undefined) {
    userAnswer.value = savedAnswer
  } else {
    if (currentQuestion.value.type === 'MULTIPLE') {
      userAnswer.value = []
    } else if (currentQuestion.value.type === 'FILL') {
      const blankCount = (currentQuestion.value as any).blankCount || 1
      userAnswer.value = Array(blankCount).fill('')
    } else {
      userAnswer.value = null
    }
  }
  
  currentNote.value = ''
  currentQuestionResult.value = answerStatus.value[currentIndex.value]?.result || null
  showAnalysis.value = answerStatus.value[currentIndex.value]?.answered || false
  
  checkFavoriteStatus()
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!currentQuestion.value) return
  
  try {
    isFavorite.value = false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 保存进度
const saveProgress = () => {
  const sessionId = route.query.sessionId as string
  if (sessionId) {
    const progressData = {
      currentIndex: currentIndex.value,
      userAnswers: Array.from(userAnswers.value.entries()),
      answerStatus: answerStatus.value,
      remainingSeconds: remainingSeconds.value,
      combo: combo.value,
      maxCombo: maxCombo.value,
      masteredCount: masteredCount.value,
      savedAt: new Date().toISOString()
    }
    sessionStorage.setItem(`practice_progress_${sessionId}`, JSON.stringify(progressData))
    hasUnsavedChanges.value = false
  }
}

// 完成练习
const finishPractice = async () => {
  try {
    // 先保存当前题目状态（特别是模拟考试模式）
    saveCurrentState()
    
    const answered = Object.values(answerStatus.value).filter((s: any) => s.answered).length
    const correct = Object.values(answerStatus.value).filter((s: any) => s.correct === true).length
    
    const result = await ElMessageBox.confirm(
      `你已完成 ${answered}/${questions.value.length} 题，确认提交吗？`,
      '确认提交',
      {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    if (result) {
      stopTimer()
      stopQuestionTimer()
      
      const totalScore = Object.values(answerStatus.value).reduce((sum, s: any) => {
        return sum + (s.result?.score || 0)
      }, 0)
      
      const accuracy = answered > 0 ? Math.round((correct / answered) * 100) : 0
      const timeUsed = sessionInfo.value?.examDuration 
        ? (sessionInfo.value.examDuration * 60 - remainingSeconds.value)
        : 0
      
      const sessionId = route.query.sessionId as string
      const resultData = {
        sessionId,
        mode: practiceMode.value,
        subjectName: sessionInfo.value?.subjectName,
        totalQuestions: questions.value.length,
        answeredCount: answered,
        correctCount: correct,
        accuracy,
        totalScore,
        timeUsed,
        maxCombo: maxCombo.value,
        masteryRate: masteryRate.value,
        answerStatus: answerStatus.value,
        questions: questions.value,
        userAnswers: Array.from(userAnswers.value.entries())
      }
      sessionStorage.setItem(`practice_result_${sessionId}`, JSON.stringify(resultData))
      
      sessionStorage.removeItem(`practice_progress_${sessionId}`)
      hasUnsavedChanges.value = false
      isExiting.value = true
      
      ElMessage.success('练习已提交！')
      
      router.push({
        path: '/practice/result',
        query: { sessionId }
      })
    }
  } catch {
    // 用户取消
  }
}

// 退出练习
const exitPractice = () => {
  showExitDialog.value = false
  stopTimer()
  stopQuestionTimer()
  saveProgress()
  hasUnsavedChanges.value = false
  isExiting.value = true
  router.push('/practice')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.practice-session {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $bg-gray;
  
  // 模式特定样式
  &.mode-TIMED {
    .session-header {
      background: linear-gradient(135deg, #5fc3e4 0%, #4a90e2 100%);
      color: white;
      
      .mode-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }
  
  &.mode-EXAM {
    .session-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      
      .mode-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }
  
  &.mode-RANDOM {
    .session-header {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;
      
      .mode-badge {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }
}

// 顶部栏
.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md $spacing-lg;
  background: $bg-white;
  border-bottom: 1px solid $border-color;
  box-shadow: $box-shadow-sm;
  z-index: 100;
  transition: all 0.3s ease;

  .header-left,
  .header-right {
    flex: 0 0 200px;
  }

  .header-center {
    flex: 1;
    max-width: 600px;
    
    .mode-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      background: $primary-lightest;
      color: $primary-color;
      border-radius: 12px;
      font-size: 13px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    .progress-info {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      margin-bottom: 8px;
      font-weight: 600;

      .current-num {
        color: $primary-color;
        font-size: 20px;
      }

      .divider {
        color: $text-secondary;
      }

      .total-num {
        color: $text-secondary;
        font-size: 16px;
      }
    }

    .progress-bar {
      width: 100%;
    }
  }

  .header-right {
    display: flex;
    justify-content: flex-end;

    .timer {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: $border-radius-md;
      font-size: 18px;
      font-weight: 600;

      &.warning,
      span.warning {
        color: $warning-color;
        animation: pulse 1s infinite;
      }
      
      span.danger {
        color: $danger-color;
        animation: pulse 0.5s infinite;
      }
    }
    
    .timer-per-question {
      display: flex;
      align-items: center;
      gap: 8px;
      
      span {
        font-size: 24px;
        min-width: 50px;
        text-align: center;
      }
    }
    
    .knowledge-progress,
    .error-rate,
    .combo-counter {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: $border-radius-md;
      font-weight: 600;
    }
    
    .combo-counter {
      .combo-text {
        font-size: 18px;
        color: $warning-color;
      }
    }
  }
}

// 内容区域
.session-content {
  flex: 1;
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-lg;
  overflow: hidden;

  .question-container {
    flex: 1;
    overflow-y: auto;
    padding-right: $spacing-sm;

    &::-webkit-scrollbar {
      width: 6px;
    }
  }

  // 题目卡片
  .question-card {
    margin-bottom: $spacing-md;
    position: relative;
    transition: all 0.3s ease;
    
    &.time-pressure {
      box-shadow: 0 0 20px rgba(245, 63, 63, 0.3);
      animation: shake 0.5s infinite;
    }
    
    .time-ring-overlay {
      position: absolute;
      top: 10px;
      right: 10px;
      width: 60px;
      height: 60px;
      z-index: 10;
      
      .time-ring {
        width: 100%;
        height: 100%;
        transform: scaleY(-1);
        
        .time-ring-progress {
          transition: stroke-dasharray 0.3s ease, stroke 0.3s ease;
        }
      }
    }

    .question-header {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      margin-bottom: $spacing-lg;

      .question-actions {
        margin-left: auto;

        .el-button.active {
          color: $warning-color;
        }
      }
    }

    .question-content {
      margin-bottom: $spacing-xl;

      .question-title {
        font-size: 16px;
        line-height: 1.8;
        color: $text-primary;
        font-weight: 500;

        .question-number {
          color: $primary-color;
          font-weight: 600;
        }
      }

      .question-material {
        margin-top: $spacing-lg;
        padding: $spacing-md;
        background: $bg-gray;
        border-left: 3px solid $primary-color;
        border-radius: $border-radius-md;

        .material-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-weight: 600;
          color: $primary-color;
          margin-bottom: $spacing-sm;
        }

        .material-content {
          font-size: 14px;
          line-height: 1.8;
          color: $text-secondary;
        }
      }
    }

    .answer-area {
      .answer-options {
        display: flex;
        flex-direction: column;
        gap: $spacing-md;
        width: 100%;

        .answer-option {
          padding: $spacing-md;
          border: 2px solid $border-color;
          border-radius: $border-radius-md;
          transition: all $transition-fast;
          margin-right: 0;
          width: 100%;

          &:hover {
            border-color: $primary-lighter;
            background: $primary-lightest;
          }

          :deep(.el-radio__input.is-checked + .el-radio__label),
          :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
            color: $primary-color;
          }

          :deep(.el-radio__label),
          :deep(.el-checkbox__label) {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            white-space: normal;
            line-height: 1.6;
            padding-left: 8px;
          }

          .option-key {
            font-weight: 600;
            color: $primary-color;
            flex-shrink: 0;
          }

          .option-content {
            flex: 1;
          }
        }

        &.judge-options {
          flex-direction: row;
          gap: $spacing-lg;

          .judge-option {
            flex: 1;
            padding: $spacing-lg;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            font-weight: 600;

            :deep(.el-radio__label) {
              display: flex;
              align-items: center;
              gap: 8px;
            }

            .el-icon {
              font-size: 24px;
            }
          }
        }
      }

      .fill-answer {
        display: flex;
        flex-direction: column;
        gap: $spacing-md;

        .fill-input {
          :deep(.el-input__inner) {
            height: 48px;
            font-size: 15px;
          }
        }
      }

      .text-answer {
        :deep(.el-textarea__inner) {
          font-size: 15px;
          line-height: 1.8;
        }
      }
    }

    .note-area {
      margin-top: $spacing-xl;
      padding-top: $spacing-lg;
      border-top: 1px solid $border-light;

      .note-header {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: $spacing-sm;
      }
    }
  }

  // 解析卡片
  .analysis-card {
    .analysis-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: $spacing-lg;

      .result-tag {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;

        .icon-correct {
          color: $success-color;
          font-size: 24px;
        }

        .icon-wrong {
          color: $danger-color;
          font-size: 24px;
        }

        .text-correct {
          color: $success-color;
        }

        .text-wrong {
          color: $danger-color;
        }
      }
    }

    .analysis-content {
      .answer-section {
        margin-bottom: $spacing-lg;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          display: flex;
          align-items: center;
          gap: 6px;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: $spacing-sm;
        }

        .section-content {
          padding: $spacing-md;
          background: $bg-gray;
          border-radius: $border-radius-md;
          line-height: 1.8;
          color: $text-secondary;

          &.correct-answer {
            background: rgba(0, 180, 42, 0.1);
            color: $success-color;
            font-weight: 600;
          }
        }
      }
    }
  }

  // 题目导航
  .question-nav {
    flex: 0 0 280px;
    background: $bg-white;
    border: 1px solid $border-color;
    border-radius: $border-radius-lg;
    padding: $spacing-md;
    overflow-y: auto;
    max-height: calc(100vh - 200px);

    .nav-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      margin-bottom: $spacing-md;
      padding-bottom: $spacing-sm;
      border-bottom: 1px solid $border-light;
    }
    
    .chapter-groups {
      .chapter-group {
        margin-bottom: $spacing-lg;
        
        .chapter-name {
          font-size: 13px;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: $spacing-sm;
          padding-left: 4px;
        }
      }
    }

    .nav-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 8px;
      margin-bottom: $spacing-md;

      .nav-item {
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid $border-color;
        border-radius: $border-radius-sm;
        cursor: pointer;
        font-weight: 600;
        transition: all $transition-fast;

        &:hover {
          border-color: $primary-color;
          transform: scale(1.1);
        }

        &.active {
          background: $primary-color;
          color: $bg-white;
          border-color: $primary-color;
        }

        &.answered {
          background: $primary-lightest;
          border-color: $primary-lighter;
          color: $primary-color;
        }

        &.correct {
          background: rgba(0, 180, 42, 0.1);
          border-color: $success-color;
          color: $success-color;
        }

        &.wrong {
          background: rgba(245, 63, 63, 0.1);
          border-color: $danger-color;
          color: $danger-color;
        }
      }
    }

    .nav-legend {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-top: $spacing-sm;
      border-top: 1px solid $border-light;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: $text-secondary;

        .legend-dot {
          width: 20px;
          height: 20px;
          border: 2px solid $border-color;
          border-radius: $border-radius-sm;

          &.answered {
            background: $primary-lightest;
            border-color: $primary-lighter;
          }
          
          &.correct {
            background: rgba(0, 180, 42, 0.1);
            border-color: $success-color;
          }
          
          &.wrong {
            background: rgba(245, 63, 63, 0.1);
            border-color: $danger-color;
          }
        }
      }
    }
    
    .mastery-stats {
      margin-top: $spacing-md;
      padding-top: $spacing-md;
      
      .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: $spacing-sm 0;
        font-size: 13px;
      }
    }
  }
}

// 底部操作栏
.session-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background: $bg-white;
  border-top: 1px solid $border-color;
  box-shadow: $box-shadow-sm;

  .footer-center {
    flex: 1;
    display: flex;
    justify-content: center;
    gap: $spacing-md;

    .el-button {
      min-width: 160px;
      height: 44px;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

// 时间到提示
.time-up-content {
  text-align: center;
  padding: $spacing-lg 0;
  
  .time-up-icon {
    font-size: 64px;
    color: $danger-color;
    margin-bottom: $spacing-md;
  }
  
  p {
    font-size: 16px;
    color: $text-primary;
  }
}

// 动画
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-2px);
  }
  75% {
    transform: translateX(2px);
  }
}

// 响应式
@media (max-width: 1200px) {
  .session-content {
    .question-nav {
      flex: 0 0 240px;

      .nav-grid {
        grid-template-columns: repeat(4, 1fr);
      }
    }
  }
}

@media (max-width: 992px) {
  .session-content {
    flex-direction: column;

    .question-nav {
      flex: none;
      max-height: 200px;
    }
  }

  .session-header {
    .header-left,
    .header-right {
      flex: 0 0 auto;
    }
  }
}

@media (max-width: 768px) {
  .session-header {
    flex-wrap: wrap;

    .header-center {
      order: 3;
      flex: 1 1 100%;
    }
  }

  .session-content .answer-area .answer-options.judge-options {
    flex-direction: column;
  }

  .session-footer {
    flex-wrap: wrap;

    .footer-center {
      order: -1;
      flex: 1 1 100%;
      margin-bottom: $spacing-sm;
    }
  }
}
</style>

