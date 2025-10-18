<template>
  <div class="page-container subjects-page">
    <div class="page-header">
      <h1>题库浏览</h1>
      <p>浏览所有学科和章节，查看题目详情</p>
    </div>

    <!-- 学科列表 -->
    <div v-loading="loading">
      <el-empty v-if="!loading && subjects.length === 0" description="暂无学科数据" />
      
      <el-row v-else :gutter="16">
        <el-col
          v-for="subject in subjects"
          :key="subject.id"
          :xs="24"
          :sm="12"
          :lg="8"
        >
        <el-card
          :class="['subject-card', { active: selectedSubject?.id === subject.id }]"
          @click="selectSubject(subject)"
        >
          <div class="subject-icon">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <div class="subject-content">
            <h3 class="subject-name">{{ subject.name }}</h3>
            <p class="subject-desc">{{ subject.description || '暂无描述' }}</p>
            <div class="subject-stats">
              <div class="stat-item">
                <span class="stat-label">章节</span>
                <span class="stat-value">{{ subject.chapterCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">题目</span>
                <span class="stat-value">{{ subject.questionCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已答</span>
                <span class="stat-value">{{ subject.answeredCount || 0 }}</span>
              </div>
            </div>
          </div>
          <el-icon v-if="selectedSubject?.id === subject.id" class="check-icon">
            <CircleCheck />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>
    </div>

    <!-- 章节和题目详情 -->
    <el-card v-if="selectedSubject" class="detail-card">
      <template #header>
        <div class="card-header-content">
          <div class="header-left">
            <el-icon><List /></el-icon>
            <span>{{ selectedSubject.name }} - 章节列表</span>
          </div>
          <div class="header-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目..."
              clearable
              style="width: 300px"
              @input="currentPage = 1"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <div class="detail-content">
        <!-- 章节树 -->
        <div v-loading="loadingChapters" class="chapter-tree">
          <div class="tree-header">
            <span class="tree-title">章节导航</span>
            <el-button
              text
              size="small"
              @click="expandAll = !expandAll"
            >
              {{ expandAll ? '收起全部' : '展开全部' }}
            </el-button>
          </div>
          
          <!-- 全部章节选项 -->
          <div 
            :class="['all-chapters-item', { active: !currentChapter }]"
            @click="handleNodeClick(null)"
          >
            <el-icon><Reading /></el-icon>
            <span class="all-chapters-text">全部章节</span>
            <span class="all-chapters-count">{{ questions.length }}题</span>
          </div>
          
          <el-empty 
            v-if="chapterTree.length === 0 && !loadingChapters" 
            description="暂无章节" 
            :image-size="80"
          />
          
          <el-tree
            v-else
            ref="treeRef"
            :data="chapterTree"
            :props="{ label: 'name', children: 'children' }"
            :default-expand-all="expandAll"
            :highlight-current="true"
            node-key="id"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span class="node-label">{{ data.name }}</span>
                <span class="node-count">{{ data.questionCount || 0 }}题</span>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 题目列表 -->
        <div v-loading="loadingQuestions" class="question-list">
          <div class="list-header">
            <div class="header-info">
              <span class="current-chapter">
                {{ currentChapter?.name || '全部题目' }}
              </span>
              <span class="question-count">
                共 {{ filteredQuestions.length }} 道题
              </span>
            </div>
            
            <div class="header-filters">
              <el-select
                v-model="filterType"
                placeholder="题型"
                clearable
                style="width: 120px"
                @change="currentPage = 1"
              >
                <el-option label="单选题" value="SINGLE" />
                <el-option label="多选题" value="MULTIPLE" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="填空题" value="FILL" />
                <el-option label="简答题" value="SHORT_ANSWER" />
              </el-select>

              <el-select
                v-model="filterDifficulty"
                placeholder="难度"
                clearable
                style="width: 100px"
                @change="currentPage = 1"
              >
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>

              <el-button
                type="primary"
                :disabled="filteredQuestions.length === 0"
                @click="startPractice"
              >
                <el-icon><CaretRight /></el-icon>
                开始练习
              </el-button>
            </div>
          </div>

          <div class="list-content">
            <el-empty
              v-if="filteredQuestions.length === 0 && !loadingQuestions"
              description="暂无题目"
            />
            
            <div
              v-for="question in paginatedQuestions"
              :key="question.id"
              class="question-item"
              @click="viewQuestion(question)"
            >
              <div class="question-header">
                <div class="header-left">
                  <el-tag :type="getQuestionTypeColor(question.type)" size="small">
                    {{ getQuestionTypeName(question.type) }}
                  </el-tag>
                  <el-tag v-if="question.difficulty" type="info" size="small">
                    {{ getDifficultyName(question.difficulty) }}
                  </el-tag>
                  <el-tag v-if="question.answered" type="success" size="small">
                    已答
                  </el-tag>
                </div>
                <div class="header-right">
                  <el-button
                    text
                    :class="{ active: question.isFavorite }"
                    @click.stop="toggleFavorite(question)"
                  >
                    <el-icon><Star /></el-icon>
                  </el-button>
                </div>
              </div>

              <div class="question-content">
                <span class="question-number">{{ question.serialNumber }}. </span>
                <span v-html="question.content || question.title"></span>
              </div>

              <div class="question-footer">
                <span class="chapter-tag">{{ question.chapterName }}</span>
                <div class="question-actions">
                  <el-button text size="small">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                  <el-button text size="small" @click.stop="quickPractice(question)">
                    <el-icon><Edit /></el-icon>
                    快速练习
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="filteredQuestions.length > 0" class="list-pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="filteredQuestions.length"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="showQuestionDialog"
      :title="`题目详情 - ${getQuestionTypeName(selectedQuestion?.type || '')}`"
      width="800px"
      top="5vh"
    >
      <div v-if="selectedQuestion" class="question-dialog">
        <div class="dialog-tags">
          <el-tag :type="getQuestionTypeColor(selectedQuestion.type)">
            {{ getQuestionTypeName(selectedQuestion.type) }}
          </el-tag>
          <el-tag v-if="selectedQuestion.difficulty" type="info">
            {{ getDifficultyName(selectedQuestion.difficulty) }}
          </el-tag>
          <el-tag type="warning">{{ selectedQuestion.chapterName }}</el-tag>
        </div>

        <div class="dialog-content">
          <div class="content-title">题目</div>
          <div class="content-text" v-html="selectedQuestion.content"></div>
        </div>

        <div v-if="selectedQuestion.options && selectedQuestion.options.length > 0" class="dialog-options">
          <div class="content-title">选项</div>
          <div
            v-for="(option, index) in selectedQuestion.options"
            :key="index"
            class="option-item"
          >
            <span class="option-key">{{ String.fromCharCode(65 + index) }}.</span>
            <span v-html="option.content || option.value || option"></span>
          </div>
        </div>

        <div class="dialog-answer">
          <div class="content-title">正确答案</div>
          <div class="content-text answer-text">
            {{ formatAnswer(selectedQuestion.answer) }}
          </div>
        </div>

        <div v-if="selectedQuestion.explanation" class="dialog-explanation">
          <div class="content-title">答案解析</div>
          <div class="content-text" v-html="selectedQuestion.explanation"></div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showQuestionDialog = false">关闭</el-button>
        <el-button type="primary" @click="quickPractice(selectedQuestion)">
          <el-icon><Edit /></el-icon>
          快速练习
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading,
  CircleCheck,
  List,
  Search,
  CaretRight,
  Star,
  View,
  Edit
} from '@element-plus/icons-vue'
import type { Subject, Chapter, Question } from '@/types'
import { subjectApi } from '@/api/modules/subject'
import { chapterApi } from '@/api/modules/chapter'
import { questionApi } from '@/api/modules/question'
import { favoriteApi } from '@/api/modules/favorite'

const router = useRouter()

const subjects = ref<Subject[]>([])
const selectedSubject = ref<Subject>()
const chapterTree = ref<any[]>([])
const questions = ref<any[]>([])
const currentChapter = ref<any>()
const searchKeyword = ref('')
const expandAll = ref(false)
const filterType = ref('')
const filterDifficulty = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const showQuestionDialog = ref(false)
const selectedQuestion = ref<any>()
const loading = ref(false)
const loadingChapters = ref(false)
const loadingQuestions = ref(false)

// 过滤后的题目
const filteredQuestions = computed(() => {
  let result = questions.value

  // 章节筛选
  if (currentChapter.value) {
    // 如果选择了子章节，只显示该章节的题目
    // 如果选择了父章节，显示该章节及其所有子章节的题目
    const chapterIds = getAllChapterIds(currentChapter.value)
    result = result.filter(q => chapterIds.includes(q.chapterId))
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(q =>
      (q.content && q.content.toLowerCase().includes(keyword)) ||
      (q.title && q.title.toLowerCase().includes(keyword)) ||
      (q.chapterName && q.chapterName.toLowerCase().includes(keyword))
    )
  }

  // 题型筛选
  if (filterType.value) {
    result = result.filter(q => q.type === filterType.value)
  }

  // 难度筛选
  if (filterDifficulty.value) {
    result = result.filter(q => q.difficulty === filterDifficulty.value)
  }

  return result
})

// 获取章节及其所有子章节的ID
const getAllChapterIds = (chapter: any): number[] => {
  const ids = [chapter.id]
  if (chapter.children && chapter.children.length > 0) {
    chapter.children.forEach((child: any) => {
      ids.push(...getAllChapterIds(child))
    })
  }
  return ids
}

// 分页后的题目
const paginatedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredQuestions.value.slice(start, end)
})

onMounted(() => {
  loadSubjects()
})

// 监听学科变化
watch(selectedSubject, (newSubject) => {
  if (newSubject) {
    loadChapters(newSubject.id)
    loadQuestions(newSubject.id)
  }
})

// 加载学科列表
const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await subjectApi.getEnabledSubjects()
    subjects.value = res.data

    // 默认选择第一个学科
    if (subjects.value.length > 0) {
      selectedSubject.value = subjects.value[0]
    }
  } catch (error) {
    console.error('加载学科失败:', error)
    ElMessage.error('加载学科失败')
  } finally {
    loading.value = false
  }
}

// 加载章节
const loadChapters = async (subjectId: number) => {
  loadingChapters.value = true
  try {
    const res = await chapterApi.getTree(subjectId)
    chapterTree.value = res.data
  } catch (error) {
    console.error('加载章节失败:', error)
    ElMessage.error('加载章节失败')
    chapterTree.value = []
  } finally {
    loadingChapters.value = false
  }
}

// 加载题目
const loadQuestions = async (subjectId: number) => {
  loadingQuestions.value = true
  try {
    const res = await questionApi.list({
      subjectId,
      page: 0,
      size: 10000, // 获取所有题目
      sortBy: 'serialNumber',
      direction: 'asc' // 按序号升序排序，小号在前
    })
    
    // 转换题目数据格式 - 按章节分组后重新编号
    const questionsByChapter = new Map<number, any[]>()
    
    res.data.content.forEach((q: Question) => {
      const chapterId = q.chapterId || 0
      if (!questionsByChapter.has(chapterId)) {
        questionsByChapter.set(chapterId, [])
      }
      questionsByChapter.get(chapterId)!.push(q)
    })
    
    // 为每个章节的题目重新编号
    questions.value = []
    questionsByChapter.forEach((chapterQuestions) => {
      chapterQuestions.forEach((q: Question, index: number) => {
        // 解析选项（如果是JSON字符串）
        let options = q.options
        if (typeof options === 'string') {
          try {
            options = JSON.parse(options)
          } catch (e) {
            options = []
          }
        }
        
        questions.value.push({
          id: q.id,
          serialNumber: index + 1, // 章节内从1开始连续编号
          type: q.type,
          difficulty: q.difficulty,
          content: q.content || q.title,
          title: q.title,
          chapterId: q.chapterId,
          chapterName: q.chapterName,
          subjectName: q.subjectName,
          options: options,
          answer: q.answer,
          explanation: q.answerAnalysis,
          answered: false,
          isFavorite: false
        })
      })
    })
    
    // 批量检查收藏状态
    if (questions.value.length > 0) {
      await loadFavoriteStatus()
    }
  } catch (error) {
    console.error('加载题目失败:', error)
    ElMessage.error('加载题目失败')
    questions.value = []
  } finally {
    loadingQuestions.value = false
  }
}

// 加载收藏状态
const loadFavoriteStatus = async () => {
  try {
    const questionIds = questions.value.map(q => q.id)
    const res = await favoriteApi.checkBatch(questionIds)
    const favoriteMap = res.data
    
    // 更新题目的收藏状态
    questions.value.forEach(q => {
      if (favoriteMap[q.id]) {
        q.isFavorite = true
        q.favoriteId = favoriteMap[q.id].id
      }
    })
  } catch (error) {
    console.debug('加载收藏状态失败:', error)
    // 不显示错误提示，收藏状态可选
  }
}

// 选择学科
const selectSubject = (subject: Subject) => {
  if (selectedSubject.value?.id === subject.id) {
    return // 如果点击的是当前学科，不做任何操作
  }
  selectedSubject.value = subject
  currentChapter.value = null
  currentPage.value = 1
  // 重置筛选条件
  filterType.value = ''
  filterDifficulty.value = ''
  searchKeyword.value = ''
}

// 点击树节点
const handleNodeClick = (data: any) => {
  currentChapter.value = data
  currentPage.value = 1
}

// 分页事件
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 查看题目详情
const viewQuestion = (question: any) => {
  selectedQuestion.value = question
  showQuestionDialog.value = true
}

// 收藏/取消收藏
const toggleFavorite = async (question: any) => {
  try {
    if (question.isFavorite) {
      await favoriteApi.remove(question.favoriteId)
      question.isFavorite = false
      question.favoriteId = null
      ElMessage.success('取消收藏')
    } else {
      const res = await favoriteApi.add({
        questionId: question.id
      })
      question.isFavorite = true
      question.favoriteId = res.data.id
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 开始练习
const startPractice = () => {
  const questionIds = filteredQuestions.value.map(q => q.id)
  // TODO: 创建练习会话并跳转
  router.push({
    path: '/practice',
    query: {
      subjectId: selectedSubject.value?.id,
      chapterId: currentChapter.value?.id,
      questionIds: questionIds.join(',')
    }
  })
}

// 快速练习单题
const quickPractice = (question: any) => {
  showQuestionDialog.value = false
  router.push({
    path: '/practice/session',
    query: {
      mode: 'SEQUENTIAL',
      subjectId: selectedSubject.value?.id,
      questionIds: question.id
    }
  })
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
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题'
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

// 格式化答案
const formatAnswer = (answer: any) => {
  // 如果是JSON字符串，先解析
  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (parsed.answer) {
        answer = parsed.answer
      }
    } catch (e) {
      // 不是JSON，继续处理
    }
  }
  
  // 如果是对象，提取answer字段
  if (typeof answer === 'object' && answer !== null && !Array.isArray(answer)) {
    if (answer.answer) {
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
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.subjects-page {
  max-width: 1600px;
  margin: 0 auto;
}

// 学科卡片
.subject-card {
  position: relative;
  cursor: pointer;
  transition: all $transition-fast;
  margin-bottom: $spacing-md;
  height: 100%;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $box-shadow-lg;
    border-color: $primary-color;
  }

  &.active {
    border-color: $primary-color;
    box-shadow: $box-shadow-md;

    .check-icon {
      position: absolute;
      top: $spacing-md;
      right: $spacing-md;
      color: $primary-color;
      font-size: 24px;
    }
  }

  .subject-icon {
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, $primary-color 0%, $primary-lighter 100%);
    border-radius: $border-radius-lg;
    color: $bg-white;
    margin-bottom: $spacing-md;
  }

  .subject-content {
    .subject-name {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 8px;
    }

    .subject-desc {
      font-size: 13px;
      color: $text-secondary;
      margin-bottom: $spacing-md;
      line-height: 1.6;
      min-height: 40px;
    }

    .subject-stats {
      display: flex;
      gap: $spacing-lg;
      padding-top: $spacing-md;
      border-top: 1px solid $border-light;

      .stat-item {
        flex: 1;
        text-align: center;

        .stat-label {
          display: block;
          font-size: 12px;
          color: $text-secondary;
          margin-bottom: 4px;
        }

        .stat-value {
          display: block;
          font-size: 20px;
          font-weight: 600;
          color: $primary-color;
        }
      }
    }
  }
}

// 详情卡片
.detail-card {
  margin-top: $spacing-lg;

  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
    }
  }

  .detail-content {
    display: flex;
    gap: $spacing-lg;

    // 章节树
    .chapter-tree {
      flex: 0 0 300px;
      border-right: 1px solid $border-light;
      padding-right: $spacing-md;

      .tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-md;
        padding-bottom: $spacing-sm;
        border-bottom: 1px solid $border-light;

        .tree-title {
          font-weight: 600;
          color: $text-primary;
        }
      }

      :deep(.el-tree) {
        background: transparent;

        .el-tree-node__content {
          padding: 8px 0;
          border-radius: $border-radius-sm;

          &:hover {
            background: $primary-lightest;
          }
        }

        .el-tree-node.is-current > .el-tree-node__content {
          background: $primary-lightest;
          color: $primary-color;
          font-weight: 600;
        }
      }

      .tree-node {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 14px;

        .node-count {
          font-size: 12px;
          color: $text-secondary;
          margin-left: $spacing-sm;
        }
      }
    }

    // 题目列表
    .question-list {
      flex: 1;
      min-width: 0;

      .list-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-md;
        padding-bottom: $spacing-md;
        border-bottom: 1px solid $border-light;
        flex-wrap: wrap;
        gap: $spacing-md;

        .header-info {
          display: flex;
          align-items: center;
          gap: $spacing-md;

          .current-chapter {
            font-weight: 600;
            color: $text-primary;
          }

          .question-count {
            font-size: 13px;
            color: $text-secondary;
          }
        }

        .header-filters {
          display: flex;
          gap: $spacing-sm;
          align-items: center;
        }
      }

      .list-content {
        .question-item {
          padding: $spacing-md;
          border: 1px solid $border-color;
          border-radius: $border-radius-md;
          margin-bottom: $spacing-sm;
          cursor: pointer;
          transition: all $transition-fast;

          &:hover {
            border-color: $primary-color;
            background: $primary-lightest;
            transform: translateX(4px);
          }

          .question-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: $spacing-sm;

            .header-left {
              display: flex;
              gap: 6px;
              flex-wrap: wrap;
            }

            .header-right {
              .el-button.active {
                color: $warning-color;
              }
            }
          }

          .question-content {
            font-size: 15px;
            line-height: 1.8;
            color: $text-primary;
            margin-bottom: $spacing-sm;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;

            .question-number {
              font-weight: 600;
              color: $primary-color;
            }
          }

          .question-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .chapter-tag {
              font-size: 12px;
              color: $text-secondary;
              padding: 2px 8px;
              background: $bg-gray;
              border-radius: $border-radius-sm;
            }

            .question-actions {
              display: flex;
              gap: 4px;
            }
          }
        }
      }

      .list-pagination {
        display: flex;
        justify-content: center;
        margin-top: $spacing-lg;
      }
    }
  }
}

// 题目详情对话框
.question-dialog {
  .dialog-tags {
    display: flex;
    gap: 8px;
    margin-bottom: $spacing-lg;
    flex-wrap: wrap;
  }

  .dialog-content,
  .dialog-options,
  .dialog-answer,
  .dialog-explanation {
    margin-bottom: $spacing-lg;

    .content-title {
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

    .content-text {
      line-height: 1.8;
      color: $text-secondary;
      padding: $spacing-md;
      background: $bg-gray;
      border-radius: $border-radius-md;

      &.answer-text {
        background: rgba(0, 180, 42, 0.1);
        color: $success-color;
        font-weight: 600;
      }
    }
  }

  .dialog-options {
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

// 全部章节选项样式
.all-chapters-item {
  display: flex;
  align-items: center;
  padding: 10px $spacing-md;
  margin-bottom: $spacing-sm;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: all $transition-fast;
  background: $bg-gray;
  
  &:hover {
    background: $primary-lightest;
  }
  
  &.active {
    background: $primary-lightest;
    color: $primary-color;
    font-weight: 600;
  }
  
  .el-icon {
    margin-right: 8px;
    font-size: 16px;
  }
  
  .all-chapters-text {
    flex: 1;
  }
  
  .all-chapters-count {
    font-size: 12px;
    color: $text-secondary;
    margin-left: $spacing-sm;
  }
}

// 响应式
@media (max-width: 1200px) {
  .detail-content {
    flex-direction: column;

    .chapter-tree {
      flex: none;
      border-right: none;
      border-bottom: 1px solid $border-light;
      padding-right: 0;
      padding-bottom: $spacing-md;
    }
  }
}

@media (max-width: 768px) {
  .list-header {
    .header-filters {
      width: 100%;

      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
