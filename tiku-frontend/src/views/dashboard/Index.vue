<template>
  <div class="page-container dashboard-page" v-loading="loading">
    <div class="page-header">
      <h1>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username || '同学' }}！</h1>
      <p>今天也要加油学习哦~ 💪</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">累计学习</div>
            <div class="stat-value">{{ stats.totalStudyDays }}</div>
            <div class="stat-unit">天</div>
          </div>
          <div class="stat-icon primary">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">答题总数</div>
            <div class="stat-value">{{ stats.totalAnswers }}</div>
            <div class="stat-unit">题</div>
          </div>
          <div class="stat-icon success">
            <el-icon><Edit /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">正确率</div>
            <div class="stat-value">{{ stats.accuracy }}%</div>
            <div class="stat-unit">平均</div>
          </div>
          <div class="stat-icon warning">
            <el-icon><Trophy /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">连续打卡</div>
            <div class="stat-value">{{ stats.streakDays }}</div>
            <div class="stat-unit">天</div>
          </div>
          <div class="stat-icon danger">
            <el-icon><Medal /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快速入口 -->
    <el-card class="quick-actions-card">
      <template #header>
        <span>快速开始</span>
      </template>
      
      <el-row :gutter="16">
        <el-col
          v-for="action in quickActions"
          :key="action.path"
          :xs="12"
          :sm="8"
          :lg="6"
        >
          <div class="action-item" @click="navigateTo(action.path)">
            <div class="action-icon" :style="{ backgroundColor: action.color }">
              <el-icon :size="28">
                <component :is="action.icon" />
              </el-icon>
            </div>
            <div class="action-title">{{ action.title }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16">
      <!-- 学习进度 -->
      <el-col :xs="24" :lg="12">
        <el-card class="progress-card">
          <template #header>
            <div class="card-header-content">
              <span>学习进度</span>
              <el-button text @click="navigateTo('/statistics')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div
            v-for="subject in subjectProgress"
            :key="subject.subjectId"
            class="progress-item"
          >
            <div class="progress-header">
              <span class="progress-name">{{ subject.subjectName }}</span>
              <span class="progress-percent">{{ subject.progress }}%</span>
            </div>
            <el-progress
              :percentage="subject.progress"
              :color="getProgressColor(subject.progress)"
              :show-text="false"
            />
            <div class="progress-info">
              <span>已答：{{ subject.answeredCount }}/{{ subject.totalQuestions }}</span>
              <span>正确率：{{ Math.round(subject.accuracy) }}%</span>
            </div>
          </div>

          <el-empty v-if="subjectProgress.length === 0" description="暂无学习数据，快去开始学习吧！" />
        </el-card>
      </el-col>

      <!-- 最近错题 -->
      <el-col :xs="24" :lg="12">
        <el-card class="wrong-questions-card">
          <template #header>
            <div class="card-header-content">
              <span>最近错题</span>
              <el-button text @click="navigateTo('/wrong')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div
            v-for="wrongItem in recentWrong"
            :key="wrongItem.id"
            class="wrong-item"
            @click="viewQuestion(wrongItem.questionId)"
          >
            <div class="wrong-header">
              <el-tag :type="getQuestionTypeColor(wrongItem.question?.type || 'SINGLE')" size="small">
                {{ getQuestionTypeName(wrongItem.question?.type || 'SINGLE') }}
              </el-tag>
              <span class="wrong-time">{{ formatTime(wrongItem.lastWrongAt) }}</span>
            </div>
            <div class="wrong-title">{{ wrongItem.question?.title || '题目加载中...' }}</div>
            <div class="wrong-count" v-if="wrongItem.wrongCount > 1">
              <el-tag type="danger" size="small">错误 {{ wrongItem.wrongCount }} 次</el-tag>
            </div>
          </div>

          <el-empty v-if="recentWrong.length === 0" description="暂无错题，继续加油！" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 学习日历 -->
    <el-card class="calendar-card">
      <template #header>
        <span>学习日历</span>
      </template>
      
      <div class="calendar-container">
        <div class="calendar-legend">
          <span>最近学习记录：</span>
          <div class="legend-items">
            <span class="legend-item">
              <span class="legend-dot" style="background: #ebedf0"></span>
              未学习
            </span>
            <span class="legend-item">
              <span class="legend-dot" style="background: #c6e48b"></span>
              少量
            </span>
            <span class="legend-item">
              <span class="legend-dot" style="background: #7bc96f"></span>
              中等
            </span>
            <span class="legend-item">
              <span class="legend-dot" style="background: #239a3b"></span>
              较多
            </span>
            <span class="legend-item">
              <span class="legend-dot" style="background: #196127"></span>
              大量
            </span>
          </div>
        </div>
        
        <div class="calendar-grid">
          <div
            v-for="day in calendarDays"
            :key="day.date"
            :class="['calendar-day', `level-${day.level}`]"
            :title="`${day.date}: ${day.count}道题`"
          ></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  Edit,
  Trophy,
  Medal,
  ArrowRight
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { statisticsApi } from '@/api/modules/statistics'
import { wrongApi } from '@/api/modules/wrong'
import type { SubjectStatistics, StudyCalendar, WrongQuestion, Question } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const stats = ref({
  totalStudyDays: 0,
  totalAnswers: 0,
  accuracy: 0,
  streakDays: 0
})

const subjectProgress = ref<SubjectStatistics[]>([])
const recentWrong = ref<Array<WrongQuestion & { question?: Question }>>([])
const calendarDays = ref<any[]>([])

// 快速入口
const quickActions = [
  {
    title: '随机练习',
    path: '/practice?mode=random',
    icon: 'MagicStick',
    color: 'rgba(22, 119, 255, 0.1)'
  },
  {
    title: '模拟考试',
    path: '/practice?mode=exam',
    icon: 'Trophy',
    color: 'rgba(245, 63, 63, 0.1)'
  },
  {
    title: '错题本',
    path: '/wrong',
    icon: 'TrendCharts',
    color: 'rgba(255, 125, 0, 0.1)'
  },
  {
    title: '我的收藏',
    path: '/collection',
    icon: 'Star',
    color: 'rgba(255, 193, 7, 0.1)'
  },
  {
    title: '学习笔记',
    path: '/notes',
    icon: 'DocumentCopy',
    color: 'rgba(0, 180, 42, 0.1)'
  },
  {
    title: '学科浏览',
    path: '/subjects',
    icon: 'Reading',
    color: 'rgba(138, 43, 226, 0.1)'
  }
]

onMounted(() => {
  loadDashboardData()
})

// 加载仪表盘数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 并行加载所有数据
    const [userStatsRes, subjectStatsRes, calendarRes, wrongQuestionsRes] = await Promise.all([
      statisticsApi.getMyStatistics(),
      statisticsApi.getSubjectStatistics(),
      statisticsApi.getStudyCalendar(),
      wrongApi.getRecentWrongQuestions(5)
    ])

    // 用户统计数据
    if (userStatsRes.data) {
      const data = userStatsRes.data
      stats.value = {
        totalStudyDays: data.totalCheckInDays || 0,
        totalAnswers: data.totalAnswered || 0,
        accuracy: data.accuracy ? Math.round(data.accuracy) : 0,
        streakDays: data.continuousDays || 0
      }
    }

    // 学科进度
    if (subjectStatsRes.data) {
      subjectProgress.value = subjectStatsRes.data.map(item => ({
        ...item,
        // 计算进度百分比
        progress: item.totalQuestions > 0 
          ? Math.round((item.answeredCount / item.totalQuestions) * 100)
          : 0
      }))
    }

    // 学习日历
    if (calendarRes.data) {
      generateCalendarDays(calendarRes.data)
    }

    // 最近错题
    if (wrongQuestionsRes.data && wrongQuestionsRes.data.content) {
      recentWrong.value = wrongQuestionsRes.data.content
    }

  } catch (error: any) {
    console.error('加载Dashboard数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 生成日历数据
const generateCalendarDays = (calendar: StudyCalendar) => {
  // 生成最近90天的日历
  calendarDays.value = Array.from({ length: 90 }, (_, i) => {
    const date = dayjs().subtract(89 - i, 'day').format('YYYY-MM-DD')
    const dayData = calendar.studyData?.[date]
    const count = dayData?.answeredCount || 0
    
    // 根据答题数量设置等级
    let level = 0
    if (count > 0) level = 1
    if (count > 10) level = 2
    if (count > 20) level = 3
    if (count > 30) level = 4
    
    return { 
      date, 
      count,
      level,
      checked: dayData?.checked || false,
      accuracy: dayData?.accuracy || 0
    }
  })
}

// 导航
const navigateTo = (path: string) => {
  router.push(path)
}

// 查看题目
const viewQuestion = (questionId: number) => {
  // 跳转到练习模式，显示该题目
  router.push(`/practice?questionId=${questionId}`)
}

// 进度颜色
const getProgressColor = (progress: number) => {
  if (progress < 30) return '#F56C6C'
  if (progress < 70) return '#E6A23C'
  return '#67C23A'
}

// 题型颜色
const getQuestionTypeColor = (type: string) => {
  const map: Record<string, any> = {
    SINGLE: 'primary',
    MULTIPLE: 'success',
    JUDGE: 'warning',
    FILL: 'info',
    SHORT_ANSWER: 'danger',
    ESSAY: 'danger',
    CASE_ANALYSIS: 'danger',
    MATERIAL_ANALYSIS: 'danger'
  }
  return map[type] || 'info'
}

// 题型名称
const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单选',
    MULTIPLE: '多选',
    JUDGE: '判断',
    FILL: '填空',
    SHORT_ANSWER: '简答',
    ESSAY: '论述',
    CASE_ANALYSIS: '案例分析',
    MATERIAL_ANALYSIS: '材料分析',
    CALCULATION: '计算',
    ORDERING: '排序',
    MATCHING: '匹配',
    COMPOSITE: '综合',
    PROGRAMMING: '编程'
  }
  return map[type] || type
}

// 格式化时间
const formatTime = (dateStr: string) => {
  return dayjs(dateStr).fromNow()
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard-page {
  max-width: 1400px;
  margin: 0 auto;
}

// 统计卡片行
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
    transition: all $transition-fast;

    &:hover {
      transform: translateY(-4px);
      box-shadow: $box-shadow-lg;
    }

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
        line-height: 1;
        margin-bottom: 4px;
      }

      .stat-unit {
        font-size: 12px;
        color: $text-secondary;
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

// 快速入口
.quick-actions-card {
  margin-bottom: $spacing-lg;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-lg;
    border: 1px solid $border-color;
    border-radius: $border-radius-lg;
    cursor: pointer;
    transition: all $transition-fast;
    margin-bottom: $spacing-md;

    &:hover {
      border-color: $primary-color;
      transform: translateY(-4px);
      box-shadow: $box-shadow-md;
    }

    .action-icon {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: $border-radius-lg;
      color: $primary-color;
    }

    .action-title {
      font-size: 14px;
      font-weight: 500;
      color: $text-primary;
    }
  }
}

// 进度卡片
.progress-card,
.wrong-questions-card,
.calendar-card {
  margin-bottom: $spacing-lg;

  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .progress-item {
    padding: $spacing-md 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }

    .progress-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      .progress-name {
        font-weight: 500;
        color: $text-primary;
      }

      .progress-percent {
        color: $primary-color;
        font-weight: 600;
      }
    }

    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-top: 6px;
      font-size: 13px;
      color: $text-secondary;
    }
  }
}

// 错题卡片
.wrong-questions-card {
  .wrong-item {
    padding: $spacing-md;
    border: 1px solid $border-color;
    border-radius: $border-radius-md;
    margin-bottom: $spacing-sm;
    cursor: pointer;
    transition: all $transition-fast;

    &:hover {
      border-color: $primary-color;
      background: $primary-lightest;
    }

    .wrong-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .wrong-time {
        font-size: 12px;
        color: $text-secondary;
      }
    }

    .wrong-title {
      font-size: 14px;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      line-clamp: 2;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      margin-bottom: 8px;
    }

    .wrong-count {
      margin-top: 4px;
    }
  }
}

// 日历卡片
.calendar-card {
  .calendar-container {
    .calendar-legend {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      margin-bottom: $spacing-md;
      font-size: 13px;
      color: $text-secondary;
      flex-wrap: wrap;

      .legend-items {
        display: flex;
        gap: $spacing-sm;
        flex-wrap: wrap;
      }

      .legend-item {
        display: flex;
        align-items: center;
        gap: 4px;

        .legend-dot {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }
      }
    }

    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(12px, 1fr));
      gap: 4px;

      .calendar-day {
        width: 12px;
        height: 12px;
        border-radius: 2px;
        cursor: pointer;
        transition: transform $transition-fast;

        &:hover {
          transform: scale(1.5);
        }

        &.level-0 {
          background: #ebedf0;
        }

        &.level-1 {
          background: #c6e48b;
        }

        &.level-2 {
          background: #7bc96f;
        }

        &.level-3 {
          background: #239a3b;
        }

        &.level-4 {
          background: #196127;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .stats-row .stat-card .stat-value {
    font-size: 24px;
  }

  .calendar-container .calendar-grid {
    grid-template-columns: repeat(auto-fill, minmax(10px, 1fr));
    gap: 3px;

    .calendar-day {
      width: 10px;
      height: 10px;
    }
  }
}
</style>
