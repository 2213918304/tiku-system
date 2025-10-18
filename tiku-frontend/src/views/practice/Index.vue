<template>
  <div class="page-container practice-page">
    <div class="page-header">
      <h1>开始练习</h1>
      <p>选择练习模式，开启学习之旅</p>
    </div>

    <!-- 学科选择 -->
    <el-card class="subject-card">
      <template #header>
        <div class="card-header-content">
          <span>选择学科</span>
          <el-button text @click="loadSubjects">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="16">
        <el-col
          v-for="subject in subjects"
          :key="subject.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <div
            :class="['subject-item', { active: selectedSubject?.id === subject.id }]"
            @click="selectSubject(subject)"
          >
            <div class="subject-icon">
              <el-icon :size="32"><Reading /></el-icon>
            </div>
            <div class="subject-info">
              <div class="subject-name">{{ subject.name }}</div>
              <div class="subject-count">{{ subject.questionCount || 0 }} 道题目</div>
            </div>
            <el-icon v-if="selectedSubject?.id === subject.id" class="check-icon">
              <CircleCheck />
            </el-icon>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 章节选择（可选） -->
    <el-card v-if="selectedSubject && showChapterSelect" class="chapter-card">
      <template #header>
        <div class="card-header-content">
          <span>选择章节（可选）</span>
          <el-button text @click="clearChapters">清除选择</el-button>
        </div>
      </template>
      
      <el-checkbox-group v-model="selectedChapters" class="chapter-group">
        <el-checkbox
          v-for="chapter in chapters"
          :key="chapter.id"
          :label="chapter.id"
          class="chapter-checkbox"
        >
          <span>{{ chapter.name }}</span>
          <span class="chapter-count">（{{ chapter.questionCount || 0 }}题）</span>
        </el-checkbox>
      </el-checkbox-group>
    </el-card>

    <!-- 练习模式选择 -->
    <div class="mode-section">
      <h2 class="section-title">选择练习模式</h2>
      
      <el-row :gutter="16">
        <el-col
          v-for="mode in practiceModes"
          :key="mode.value"
          :xs="24"
          :sm="12"
          :lg="8"
        >
          <div
            :class="['mode-card', { active: selectedMode === mode.value }]"
            @click="selectMode(mode.value)"
          >
            <div class="mode-icon" :style="{ backgroundColor: mode.color }">
              <el-icon :size="36">
                <component :is="mode.icon" />
              </el-icon>
            </div>
            <div class="mode-content">
              <h3>{{ mode.title }}</h3>
              <p>{{ mode.description }}</p>
            </div>
            <el-tag v-if="mode.recommended" type="danger" size="small">推荐</el-tag>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 配置选项 -->
    <el-card v-if="selectedMode" class="config-card">
      <template #header>
        <span>练习配置</span>
      </template>
      
      <el-form :model="practiceConfig" label-width="100px" label-position="left">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12">
            <el-form-item label="题目数量">
              <el-input-number
                v-model="practiceConfig.count"
                :min="5"
                :max="100"
                :step="5"
              />
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :sm="12">
            <el-form-item label="题目类型">
              <el-select
                v-model="practiceConfig.questionTypes"
                multiple
                collapse-tags
                placeholder="全部类型"
                style="width: 100%"
              >
                <el-option label="单选题" value="SINGLE" />
                <el-option label="多选题" value="MULTIPLE" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="填空题" value="FILL" />
                <el-option label="简答题" value="SHORT_ANSWER" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :sm="12">
            <el-form-item label="难度">
              <el-select
                v-model="practiceConfig.difficulty"
                placeholder="全部难度"
                style="width: 100%"
              >
                <el-option label="全部" value="" />
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <!-- 模拟考试：总时间限制 -->
          <el-col v-if="selectedMode === 'EXAM'" :xs="24" :sm="12">
            <el-form-item label="考试时长">
              <el-input-number
                v-model="practiceConfig.timeLimit"
                :min="10"
                :max="180"
                :step="5"
              />
              <span style="margin-left: 8px">分钟</span>
            </el-form-item>
          </el-col>
          
          <!-- 限时挑战：每题时间限制 -->
          <el-col v-if="selectedMode === 'TIMED'" :xs="24" :sm="12">
            <el-form-item label="每题限时">
              <el-input-number
                v-model="practiceConfig.timePerQuestion"
                :min="0.5"
                :max="5"
                :step="0.5"
              />
              <span style="margin-left: 8px">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 开始按钮 -->
    <div class="action-bar">
      <el-button
        type="primary"
        size="large"
        :disabled="!canStart"
        :loading="loading"
        @click="startPractice"
      >
        <el-icon><CaretRight /></el-icon>
        开始练习
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading,
  Refresh,
  CircleCheck,
  CaretRight,
  TrendCharts,
  Compass,
  Timer,
  Trophy,
  Document,
  MagicStick
} from '@element-plus/icons-vue'
import { practiceApi, subjectApi, chapterApi } from '@/api'
import type { Subject, Chapter } from '@/types'

const router = useRouter()
const route = useRoute()

const subjects = ref<Subject[]>([])
const selectedSubject = ref<Subject>()
const chapters = ref<Chapter[]>([])
const selectedChapters = ref<number[]>([])
const showChapterSelect = ref(false)
const selectedMode = ref('')
const loading = ref(false)

const practiceConfig = ref({
  count: 20,
  questionTypes: [] as string[],
  difficulty: '',
  timeLimit: 60, // 模拟考试总时长（分钟）
  timePerQuestion: 1 // 限时挑战每题时间（分钟）
})

// 练习模式
const practiceModes = [
  {
    value: 'SEQUENTIAL',
    title: '顺序练习',
    description: '按章节顺序系统学习，实时追踪进度，适合初学者',
    icon: 'Document',
    color: 'rgba(22, 119, 255, 0.1)',
    recommended: false
  },
  {
    value: 'RANDOM',
    title: '随机练习',
    description: '随机抽题，连击奖励，避免记忆答案位置',
    icon: 'MagicStick',
    color: 'rgba(0, 180, 42, 0.1)',
    recommended: true
  },
  {
    value: 'CHAPTER',
    title: '章节练习',
    description: '针对特定章节深度练习，突破重难点',
    icon: 'Reading',
    color: 'rgba(255, 125, 0, 0.1)',
    recommended: false
  },
  {
    value: 'EXAM',
    title: '模拟考试',
    description: '真实考试环境，总时间限制，考后统一评分',
    icon: 'Trophy',
    color: 'rgba(245, 63, 63, 0.1)',
    recommended: true
  },
  {
    value: 'WRONG_QUESTION',
    title: '错题练习',
    description: '智能筛选错题，实时掌握度分析，查漏补缺',
    icon: 'TrendCharts',
    color: 'rgba(138, 43, 226, 0.1)',
    recommended: false
  },
  {
    value: 'TIMED',
    title: '限时挑战',
    description: '每题倒计时，时间管理训练，提升答题效率',
    icon: 'Timer',
    color: 'rgba(95, 195, 228, 0.1)',
    recommended: true
  }
]

// 是否可以开始
const canStart = computed(() => {
  return selectedSubject.value && selectedMode.value
})

onMounted(() => {
  loadSubjects()
  
  // 从URL参数中获取预设模式
  const mode = route.query.mode as string
  if (mode) {
    selectedMode.value = mode.toUpperCase()
  }
})

// 加载学科列表
const loadSubjects = async () => {
  try {
    const res = await subjectApi.getEnabledSubjects()
    subjects.value = res.data || []
    
    // 如果只有一个学科，自动选择
    if (subjects.value.length === 1) {
      await selectSubject(subjects.value[0])
    }
  } catch (error: any) {
    console.error('加载学科失败:', error)
    ElMessage.error(error.response?.data?.message || '加载学科失败')
  }
}

// 选择学科
const selectSubject = async (subject: Subject) => {
  selectedSubject.value = subject
  
  // 如果是章节练习，加载章节列表
  if (selectedMode.value === 'CHAPTER') {
    await loadChapters(subject.id)
  }
}

// 加载章节列表
const loadChapters = async (subjectId: number) => {
  try {
    showChapterSelect.value = true
    const res = await chapterApi.list(subjectId)
    chapters.value = res.data || []
  } catch (error: any) {
    console.error('加载章节失败:', error)
    ElMessage.error(error.response?.data?.message || '加载章节失败')
  }
}

// 清除章节选择
const clearChapters = () => {
  selectedChapters.value = []
}

// 选择模式
const selectMode = async (mode: string) => {
  selectedMode.value = mode
  
  // 如果是章节练习且已选择学科，加载章节
  if (mode === 'CHAPTER' && selectedSubject.value) {
    await loadChapters(selectedSubject.value.id)
  } else {
    showChapterSelect.value = false
  }
}

// 开始练习
const startPractice = async () => {
  if (!selectedSubject.value || !selectedMode.value) {
    ElMessage.warning('请选择学科和练习模式')
    return
  }

  loading.value = true
  
  try {
    // 构建练习请求
    const requestData: any = {
      mode: selectedMode.value,
      subjectId: selectedSubject.value.id,
      count: practiceConfig.value.count
    }

    // 添加章节ID（如果有选择）
    if (selectedChapters.value.length > 0) {
      requestData.chapterId = selectedChapters.value[0] // 后端只支持单个章节
    }

    // 添加题型过滤
    if (practiceConfig.value.questionTypes.length > 0) {
      requestData.questionType = practiceConfig.value.questionTypes[0] // 后端只支持单个题型
    }

    // 添加难度过滤
    if (practiceConfig.value.difficulty) {
      requestData.difficulty = practiceConfig.value.difficulty
    }

    // 添加限时配置（考试和限时模式）
    if (selectedMode.value === 'EXAM') {
      // 模拟考试：总时长（分钟）
      requestData.examDuration = practiceConfig.value.timeLimit
    } else if (selectedMode.value === 'TIMED') {
      // 限时挑战：每题时间（分钟转为秒）
      requestData.timePerQuestion = Math.floor(practiceConfig.value.timePerQuestion * 60)
    }

    // 调用API创建练习会话
    const res = await practiceApi.startPractice(requestData)
    
    if (res.data && res.data.sessionId) {
      // 保存会话数据到sessionStorage
      sessionStorage.setItem(
        `practice_session_${res.data.sessionId}`,
        JSON.stringify(res.data)
      )
      
      ElMessage.success('开始练习！')
      
      // 跳转到答题页面，传递会话数据
      router.push({
        path: '/practice/session',
        query: { 
          sessionId: res.data.sessionId
        }
      })
    } else {
      throw new Error('未返回会话ID')
    }
  } catch (error: any) {
    console.error('创建练习会话失败:', error)
    ElMessage.error(error.response?.data?.message || '创建练习会话失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.practice-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

// 学科卡片
.subject-card {
  margin-bottom: $spacing-lg;

  .subject-item {
    position: relative;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    border: 2px solid $border-color;
    border-radius: $border-radius-lg;
    cursor: pointer;
    transition: all $transition-fast;
    margin-bottom: $spacing-md;

    &:hover {
      border-color: $primary-color;
      background: $primary-lightest;
      transform: translateY(-2px);
    }

    &.active {
      border-color: $primary-color;
      background: $primary-lightest;
      box-shadow: $box-shadow-md;
    }

    .subject-icon {
      width: 56px;
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, $primary-color 0%, $primary-lighter 100%);
      border-radius: $border-radius-lg;
      color: $bg-white;
    }

    .subject-info {
      flex: 1;

      .subject-name {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .subject-count {
        font-size: 13px;
        color: $text-secondary;
      }
    }

    .check-icon {
      color: $primary-color;
      font-size: 24px;
    }
  }
}

// 章节卡片
.chapter-card {
  margin-bottom: $spacing-lg;

  .chapter-group {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: $spacing-md;

    .chapter-checkbox {
      padding: $spacing-sm $spacing-md;
      border: 1px solid $border-color;
      border-radius: $border-radius-md;
      transition: all $transition-fast;

      &:hover {
        border-color: $primary-color;
        background: $primary-lightest;
      }

      .chapter-count {
        font-size: 12px;
        color: $text-secondary;
        margin-left: 4px;
      }
    }
  }
}

// 模式区域
.mode-section {
  margin-bottom: $spacing-lg;

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: $spacing-lg;
  }

  .mode-card {
    position: relative;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-lg;
    background: $bg-white;
    border: 2px solid $border-color;
    border-radius: $border-radius-lg;
    cursor: pointer;
    transition: all $transition-fast;
    height: 100%;
    margin-bottom: $spacing-md;

    &:hover {
      border-color: $primary-color;
      transform: translateY(-4px);
      box-shadow: $box-shadow-lg;
    }

    &.active {
      border-color: $primary-color;
      background: $primary-lightest;
      box-shadow: $box-shadow-md;
    }

    .mode-icon {
      width: 64px;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: $border-radius-lg;
      color: $primary-color;
      flex-shrink: 0;
    }

    .mode-content {
      flex: 1;

      h3 {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 6px;
      }

      p {
        font-size: 13px;
        color: $text-secondary;
        line-height: 1.5;
      }
    }

    .el-tag {
      position: absolute;
      top: $spacing-sm;
      right: $spacing-sm;
    }
  }
}

// 配置卡片
.config-card {
  margin-bottom: $spacing-lg;
}

// 操作栏
.action-bar {
  display: flex;
  justify-content: center;
  padding: $spacing-xl 0;

  .el-button {
    min-width: 200px;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .mode-section .mode-card {
    flex-direction: column;
    text-align: center;

    .mode-content {
      h3 {
        margin-top: $spacing-sm;
      }
    }
  }
}
</style>
