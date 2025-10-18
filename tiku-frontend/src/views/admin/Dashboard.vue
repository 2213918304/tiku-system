<template>
  <div class="page-container admin-dashboard">
    <div class="page-header">
      <h1>数据统计</h1>
      <p>系统整体数据概览</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-loading="loading">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="$router.push('/admin/users')">
          <div class="stat-left">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-growth">
              <el-icon color="#00b42a"><CaretTop /></el-icon>
              <span>较昨日 +{{ stats.newUsersToday }}</span>
            </div>
          </div>
          <div class="stat-icon primary">
            <el-icon><User /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="$router.push('/admin/questions')">
          <div class="stat-left">
            <div class="stat-label">题目总数</div>
            <div class="stat-value">{{ stats.totalQuestions }}</div>
            <div class="stat-growth">
              <el-icon color="#00b42a"><CaretTop /></el-icon>
              <span>较昨日 +{{ stats.newQuestionsToday }}</span>
            </div>
          </div>
          <div class="stat-icon success">
            <el-icon><Edit /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">今日答题</div>
            <div class="stat-value">{{ stats.todayAnswers }}</div>
            <div class="stat-growth">
              <span>总答题 {{ stats.totalAnswers }}</span>
            </div>
          </div>
          <div class="stat-icon warning">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-left">
            <div class="stat-label">活跃用户</div>
            <div class="stat-value">{{ stats.activeUsers }}</div>
            <div class="stat-growth">
              <span>在线 {{ stats.onlineUsers }}</span>
            </div>
          </div>
          <div class="stat-icon danger">
            <el-icon><UserFilled /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 数据趋势图表 -->
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>答题趋势</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" ref="trendChartRef" />
        </el-card>
      </el-col>

      <!-- 学科分布 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>学科题目分布</span>
          </template>
          <div class="chart-container" ref="subjectChartRef" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 最近活动 -->
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="activity in recentActivities"
              :key="activity.id"
              :timestamp="activity.time"
              placement="top"
            >
              <div class="activity-item">
                <el-avatar :size="32" :style="{ background: activity.color }">
                  {{ activity.user.charAt(0) }}
                </el-avatar>
                <div class="activity-content">
                  <p><strong>{{ activity.user }}</strong> {{ activity.action }}</p>
                  <p class="activity-detail">{{ activity.detail }}</p>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 系统状态 -->
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>
            <span>系统状态</span>
          </template>
          <div class="system-status">
            <div class="status-item">
              <div class="status-label">
                <span>CPU使用率</span>
                <span class="status-value">{{ systemStatus.cpu }}%</span>
              </div>
              <el-progress 
                :percentage="systemStatus.cpu" 
                :color="getProgressColor(systemStatus.cpu)"
                :stroke-width="10"
              />
            </div>

            <div class="status-item">
              <div class="status-label">
                <span>内存使用率</span>
                <span class="status-value">{{ systemStatus.memory }}%</span>
              </div>
              <el-progress 
                :percentage="systemStatus.memory" 
                :color="getProgressColor(systemStatus.memory)"
                :stroke-width="10"
              />
            </div>

            <div class="status-item">
              <div class="status-label">
                <span>磁盘使用率</span>
                <span class="status-value">{{ systemStatus.disk }}%</span>
              </div>
              <el-progress 
                :percentage="systemStatus.disk" 
                :color="getProgressColor(systemStatus.disk)"
                :stroke-width="10"
              />
            </div>

            <div class="status-item">
              <div class="status-label">
                <span>数据库连接</span>
                <span class="status-value">{{ systemStatus.dbConnections }}/100</span>
              </div>
              <el-progress 
                :percentage="(systemStatus.dbConnections / 100) * 100" 
                :color="getProgressColor((systemStatus.dbConnections / 100) * 100)"
                :stroke-width="10"
              />
            </div>
          </div>
        </el-card>

        <!-- 快速操作 -->
        <el-card style="margin-top: 16px">
          <template #header>
            <span>快速操作</span>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/questions')">
              <el-icon><Edit /></el-icon>
              题目管理
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/import')">
              <el-icon><Upload /></el-icon>
              数据导入
            </el-button>
            <el-button @click="$router.push('/dashboard')">
              <el-icon><Switch /></el-icon>
              切换到前台
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { User, Edit, TrendCharts, UserFilled, Upload, Switch, CaretTop } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import * as echarts from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  LineChart,
  PieChart,
  CanvasRenderer
])

const loading = ref(false)
const trendType = ref('week')
const trendChartRef = ref<HTMLElement>()
const subjectChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let subjectChart: echarts.ECharts | null = null

const stats = reactive({
  totalUsers: 0,
  newUsersToday: 0,
  totalQuestions: 0,
  newQuestionsToday: 0,
  todayAnswers: 0,
  totalAnswers: 0,
  activeUsers: 0,
  onlineUsers: 0
})

const systemStatus = reactive({
  cpu: 0,
  memory: 0,
  disk: 0,
  dbConnections: 0
})

const recentActivities = ref<any[]>([])

const getProgressColor = (percentage: number) => {
  if (percentage < 60) return '#00b42a'
  if (percentage < 80) return '#ff7d00'
  return '#f53f3f'
}

const loadStats = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await request.get('/admin/dashboard/statistics')
    Object.assign(stats, statsRes.data)

    // 加载系统状态
    const statusRes = await request.get('/admin/dashboard/system-status')
    Object.assign(systemStatus, statusRes.data)

    // 加载最近活动
    const activitiesRes = await request.get('/admin/dashboard/recent-activities')
    recentActivities.value = activitiesRes.data || []
    
    // 如果没有真实活动数据，使用模拟数据
    if (recentActivities.value.length === 0) {
      recentActivities.value = [
        {
          id: 1,
          user: '用户',
          action: '完成了一次练习',
          detail: '正在收集真实活动数据...',
          time: '刚刚',
          color: '#165DFF'
        }
      ]
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const initTrendChart = async () => {
  if (!trendChartRef.value) return

  try {
    // 加载趋势数据
    const res = await request.get('/admin/dashboard/trends', {
      params: { type: trendType.value }
    })
    const trendData = res.data

    trendChart = echarts.init(trendChartRef.value)
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['答题数', '用户数']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendData.labels || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '答题数',
          type: 'line',
          data: trendData.answerCounts || [],
          smooth: true,
          itemStyle: { color: '#165DFF' }
        },
        {
          name: '用户数',
          type: 'line',
          data: trendData.userCounts || [],
          smooth: true,
          itemStyle: { color: '#00B42A' }
        }
      ]
    }

    trendChart.setOption(option)
  } catch (error: any) {
    ElMessage.error(error.message || '加载趋势数据失败')
  }
}

const initSubjectChart = async () => {
  if (!subjectChartRef.value) return

  try {
    // 加载学科分布数据
    const res = await request.get('/admin/dashboard/subject-distribution')
    const subjectData = res.data || []

    subjectChart = echarts.init(subjectChartRef.value)
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '题目数量',
          type: 'pie',
          radius: '50%',
          data: subjectData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }

    subjectChart.setOption(option)
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科分布失败')
  }
}

const handleResize = () => {
  trendChart?.resize()
  subjectChart?.resize()
}

watch(trendType, () => {
  nextTick(async () => {
    await initTrendChart()
  })
})

onMounted(() => {
  loadStats()
  nextTick(() => {
    initTrendChart()
    initSubjectChart()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  subjectChart?.dispose()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

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
    cursor: pointer;
    transition: all $transition-base;

    &:hover {
      box-shadow: $box-shadow-md;
      transform: translateY(-2px);
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
        margin-bottom: 4px;
      }

      .stat-growth {
        font-size: 12px;
        color: $text-secondary;
        display: flex;
        align-items: center;
        gap: 4px;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.activity-item {
  display: flex;
  gap: 12px;

  .activity-content {
    flex: 1;

    p {
      margin: 0;
      font-size: 13px;
      line-height: 1.6;

      &.activity-detail {
        color: $text-secondary;
        margin-top: 4px;
      }
    }
  }
}

.system-status {
  .status-item {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .status-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;

      .status-value {
        color: $primary-color;
        font-weight: 500;
      }
    }
  }
}
</style>
