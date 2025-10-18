<template>
  <div class="page-container statistics-page">
    <div class="page-header">
      <h1>学习统计</h1>
      <p>数据驱动学习，科学分析进步</p>
    </div>

    <!-- 总体统计 -->
    <el-row v-loading="loading" :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalDays }}</div>
          <div class="stat-label">学习天数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalQuestions }}</div>
          <div class="stat-label">答题总数</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.accuracy }}%</div>
          <div class="stat-label">平均正确率</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.studyMinutes }}</div>
          <div class="stat-label">学习时长（分）</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>学习趋势（最近30天）</span>
          </template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>题型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>学科正确率对比</span>
          </template>
          <div ref="subjectChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <span>章节掌握情况</span>
          </template>
          <div ref="chapterChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/modules/statistics'

const loading = ref(false)
const stats = ref({
  totalDays: 0,
  totalQuestions: 0,
  accuracy: 0,
  studyMinutes: 0
})

const trendChartRef = ref()
const typeChartRef = ref()
const subjectChartRef = ref()
const chapterChartRef = ref()

let trendChart: any = null
let typeChart: any = null
let subjectChart: any = null
let chapterChart: any = null

onMounted(async () => {
  await loadData()
  await nextTick()
  initCharts()
})

const loadData = async () => {
  loading.value = true
  try {
    // 加载用户统计数据
    const userStats = await statisticsApi.getMyStatistics()
    stats.value = {
      totalDays: userStats.data.totalCheckInDays || 0,
      totalQuestions: userStats.data.totalAnswered || 0,
      accuracy: Math.round(userStats.data.accuracy || 0),
      studyMinutes: userStats.data.totalStudyMinutes || 0
    }
  } catch (error: any) {
    console.error('加载统计数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const initCharts = async () => {
  // 初始化ECharts实例
  trendChart = echarts.init(trendChartRef.value)
  typeChart = echarts.init(typeChartRef.value)
  subjectChart = echarts.init(subjectChartRef.value)
  chapterChart = echarts.init(chapterChartRef.value)

  // 加载学习趋势图
  await loadTrendChart()
  
  // 加载题型分布图
  await loadTypeChart()
  
  // 加载学科正确率图
  await loadSubjectChart()
  
  // 加载章节掌握图
  await loadChapterChart()

  // 响应式
  window.addEventListener('resize', () => {
    trendChart?.resize()
    typeChart?.resize()
    subjectChart?.resize()
    chapterChart?.resize()
  })
}

const loadTrendChart = async () => {
  try {
    const res = await statisticsApi.getStudyTrend(30)
    const dates = res.data.dates || []
    const answerCounts = res.data.answerCounts || []
    
    // 格式化日期为 MM-DD
    const xAxisData = dates.map((date: string) => {
      const d = new Date(date)
      return `${d.getMonth() + 1}-${d.getDate()}`
    })
    
    trendChart.setOption({
      tooltip: { 
        trigger: 'axis',
        axisPointer: { type: 'cross' }
      },
      xAxis: { 
        type: 'category', 
        data: xAxisData,
        boundaryGap: false
      },
      yAxis: { 
        type: 'value',
        name: '答题数'
      },
      series: [{
        name: '答题数',
        data: answerCounts,
        type: 'line',
        smooth: true,
        areaStyle: { color: 'rgba(22, 119, 255, 0.1)' },
        itemStyle: { color: '#1677ff' }
      }]
    })
  } catch (error: any) {
    console.error('加载趋势图失败:', error)
  }
}

const loadTypeChart = async () => {
  try {
    const res = await statisticsApi.getQuestionTypeStatistics()
    const typeData = (res.data || []).map((item: any) => ({
      value: item.count,
      name: item.typeName
    }))
    
    typeChart.setOption({
      tooltip: { 
        trigger: 'item',
        formatter: '{b}: {c} 题 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center'
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: typeData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  } catch (error: any) {
    console.error('加载题型图失败:', error)
  }
}

const loadSubjectChart = async () => {
  try {
    const res = await statisticsApi.getSubjectStatistics()
    const subjects = res.data || []
    
    const xAxisData = subjects.map((s: any) => s.subjectName)
    const accuracyData = subjects.map((s: any) => Math.round(s.accuracy || 0))
    
    subjectChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>正确率: {c}%'
      },
      xAxis: { 
        type: 'category', 
        data: xAxisData,
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: { 
        type: 'value', 
        max: 100,
        name: '正确率(%)'
      },
      series: [{
        name: '正确率',
        data: accuracyData,
        type: 'bar',
        itemStyle: { 
          color: '#1677ff',
          barBorderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      }]
    })
  } catch (error: any) {
    console.error('加载学科图失败:', error)
  }
}

const loadChapterChart = async () => {
  try {
    // 获取第一个学科的章节统计（作为示例）
    const subjectsRes = await statisticsApi.getSubjectStatistics()
    const subjects = subjectsRes.data || []
    
    if (subjects.length === 0) {
      // 没有学科数据，显示空图表
      chapterChart.setOption({
        title: {
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 14
          }
        }
      })
      return
    }
    
    const firstSubject = subjects[0]
    const chaptersRes = await statisticsApi.getChapterStatistics(firstSubject.subjectId)
    const chapters = (chaptersRes.data || []).slice(0, 8) // 最多显示8个章节
    
    if (chapters.length === 0) {
      chapterChart.setOption({
        title: {
          text: '暂无章节数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#999',
            fontSize: 14
          }
        }
      })
      return
    }
    
    const indicators = chapters.map((c: any) => ({
      name: c.chapterName,
      max: 100
    }))
    
    const masteryData = chapters.map((c: any) => c.masteryLevel || 0)
    
    chapterChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      radar: {
        indicator: indicators,
        shape: 'circle',
        radius: '60%'
      },
      series: [{
        type: 'radar',
        data: [{
          value: masteryData,
          name: '掌握度',
          areaStyle: {
            color: 'rgba(22, 119, 255, 0.3)'
          },
          lineStyle: {
            color: '#1677ff'
          },
          itemStyle: {
            color: '#1677ff'
          }
        }]
      }]
    })
  } catch (error: any) {
    console.error('加载章节图失败:', error)
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.statistics-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: $spacing-lg;

  .stat-card {
    background: $bg-white;
    border-radius: $border-radius-lg;
    padding: $spacing-xl;
    border: 1px solid $border-color;
    text-align: center;

    .stat-value {
      font-size: 32px;
      font-weight: 700;
      color: $primary-color;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: $text-secondary;
    }
  }
}

.chart-card {
  margin-bottom: $spacing-lg;
}
</style>
