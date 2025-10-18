<template>
  <div class="page-container admin-questions">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增题目
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="学科">
            <el-select v-model="searchForm.subjectId" placeholder="选择学科" clearable style="width: 150px" @change="onSubjectChange">
              <el-option 
                v-for="subject in subjectList" 
                :key="subject.id" 
                :label="subject.name" 
                :value="subject.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="章节">
            <el-select v-model="searchForm.chapterId" placeholder="选择章节" clearable style="width: 150px">
              <el-option 
                v-for="chapter in chapterList" 
                :key="chapter.id" 
                :label="chapter.name" 
                :value="chapter.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="searchForm.type" placeholder="选择题型" clearable style="width: 120px">
              <el-option label="单选题" value="SINGLE" />
              <el-option label="多选题" value="MULTIPLE" />
              <el-option label="判断题" value="JUDGE" />
              <el-option label="填空题" value="FILL" />
              <el-option label="简答题" value="SHORT_ANSWER" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="题目内容关键词" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadQuestions">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 题目列表 -->
      <el-table 
        v-loading="loading" 
        :data="questionList" 
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="title" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyColor(row.difficulty)">{{ getDifficultyName(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subjectName" label="学科" width="100" />
        <el-table-column prop="chapterName" label="章节" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadQuestions"
          @current-change="loadQuestions"
        />
      </div>
    </el-card>

    <!-- 查看/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :destroy-on-close="true"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学科" prop="subjectId">
              <el-select 
                v-model="formData.subjectId" 
                placeholder="选择学科" 
                style="width: 100%"
                :disabled="viewMode"
                @change="onFormSubjectChange"
              >
                <el-option 
                  v-for="subject in subjectList" 
                  :key="subject.id" 
                  :label="subject.name" 
                  :value="subject.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="章节" prop="chapterId">
              <el-select v-model="formData.chapterId" placeholder="选择章节" style="width: 100%" :disabled="viewMode">
                <el-option 
                  v-for="chapter in formChapterList" 
                  :key="chapter.id" 
                  :label="chapter.name" 
                  :value="chapter.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题型" prop="type">
              <el-select v-model="formData.type" placeholder="选择题型" style="width: 100%" :disabled="viewMode">
                <el-option label="单选题" value="SINGLE" />
                <el-option label="多选题" value="MULTIPLE" />
                <el-option label="判断题" value="JUDGE" />
                <el-option label="填空题" value="FILL" />
                <el-option label="简答题" value="SHORT_ANSWER" />
                <el-option label="论述题" value="ESSAY" />
                <el-option label="案例分析" value="CASE_ANALYSIS" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="formData.difficulty" placeholder="选择难度" style="width: 100%" :disabled="viewMode">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="题目内容" prop="title">
          <el-input 
            v-model="formData.title" 
            type="textarea" 
            :rows="4"
            placeholder="请输入题目内容"
            :disabled="viewMode"
          />
        </el-form-item>

        <el-form-item v-if="showOptions" label="选项">
          <div class="options-container">
            <div v-for="(option, index) in formData.options" :key="index" class="option-item">
              <el-input 
                v-model="option.content" 
                :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                :disabled="viewMode"
              >
                <template #prepend>{{ String.fromCharCode(65 + index) }}</template>
              </el-input>
              <el-button 
                v-if="!viewMode && formData.options.length > 2" 
                link 
                type="danger" 
                @click="removeOption(index)"
              >
                删除
              </el-button>
            </div>
            <el-button v-if="!viewMode" type="primary" link @click="addOption">
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-input 
            v-model="formData.answer" 
            placeholder="请输入正确答案"
            :disabled="viewMode"
          />
          <div class="form-tip">
            {{ getAnswerTip() }}
          </div>
        </el-form-item>

        <el-form-item label="解析" prop="answerAnalysis">
          <el-input 
            v-model="formData.answerAnalysis" 
            type="textarea" 
            :rows="3"
            placeholder="请输入答案解析"
            :disabled="viewMode"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ viewMode ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!viewMode" type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { questionApi, subjectApi, chapterApi } from '@/api'
import type { Question, Subject, Chapter } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const viewMode = ref(false)
const isAdd = ref(true)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  subjectId: undefined as number | undefined,
  chapterId: undefined as number | undefined,
  type: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const questionList = ref<Question[]>([])
const subjectList = ref<Subject[]>([])
const chapterList = ref<Chapter[]>([])
const formChapterList = ref<Chapter[]>([])

const formData = reactive({
  id: undefined as number | undefined,
  subjectId: undefined as number | undefined,
  chapterId: undefined as number | undefined,
  type: 'SINGLE',
  difficulty: 'MEDIUM',
  title: '',
  content: '',
  options: [
    { content: '' },
    { content: '' },
    { content: '' },
    { content: '' }
  ],
  answer: '',
  answerAnalysis: ''
})

const formRules = {
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  chapterId: [{ required: true, message: '请选择章节', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  if (viewMode.value) return '查看题目'
  return isAdd.value ? '新增题目' : '编辑题目'
})

const showOptions = computed(() => {
  return ['SINGLE', 'MULTIPLE'].includes(formData.type)
})

const getTypeColor = (type: string) => {
  const map: Record<string, any> = {
    SINGLE: 'primary',
    MULTIPLE: 'success',
    JUDGE: 'warning',
    FILL: 'info',
    SHORT_ANSWER: 'danger',
    ESSAY: '',
    CASE_ANALYSIS: 'warning'
  }
  return map[type] || ''
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    FILL: '填空题',
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题',
    CASE_ANALYSIS: '案例分析',
    MATERIAL_ANALYSIS: '材料分析',
    CALCULATION: '计算题',
    ORDERING: '排序题',
    MATCHING: '匹配题',
    COMPOSITE: '组合题',
    PROGRAMMING: '编程题'
  }
  return map[type] || type
}

const getDifficultyColor = (difficulty: string) => {
  const map: Record<string, any> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return map[difficulty] || 'info'
}

const getDifficultyName = (difficulty: string) => {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return map[difficulty] || difficulty
}

const getAnswerTip = () => {
  const tips: Record<string, string> = {
    SINGLE: '单选题答案格式：A 或 B 或 C 或 D',
    MULTIPLE: '多选题答案格式：AB 或 ACD 或 ABCD',
    JUDGE: '判断题答案格式：true 或 false',
    FILL: '填空题答案格式：用分号分隔多个空，如：答案1;答案2',
    SHORT_ANSWER: '简答题答案格式：参考答案要点'
  }
  return tips[formData.type] || ''
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.list()
    subjectList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科列表失败')
  }
}

const loadChapters = async (subjectId: number) => {
  try {
    const res = await chapterApi.list(subjectId)
    return res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载章节列表失败')
    return []
  }
}

const onSubjectChange = async (subjectId: number | undefined) => {
  searchForm.chapterId = undefined
  if (subjectId) {
    chapterList.value = await loadChapters(subjectId)
  } else {
    chapterList.value = []
  }
}

const onFormSubjectChange = async (subjectId: number | undefined) => {
  formData.chapterId = undefined
  if (subjectId) {
    formChapterList.value = await loadChapters(subjectId)
  } else {
    formChapterList.value = []
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    // 过滤掉空字符串，避免后端枚举转换错误
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (searchForm.subjectId) {
      params.subjectId = searchForm.subjectId
    }
    if (searchForm.chapterId) {
      params.chapterId = searchForm.chapterId
    }
    if (searchForm.type && searchForm.type.trim() !== '') {
      params.type = searchForm.type
    }
    if (searchForm.keyword && searchForm.keyword.trim() !== '') {
      params.keyword = searchForm.keyword
    }
    
    const res = await questionApi.list(params)
    // 后端返回的是 PageResponse 格式
    if (res.data) {
      questionList.value = res.data.content || []
      pagination.total = res.data.totalElements || 0
    } else {
      questionList.value = []
      pagination.total = 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载题目列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.subjectId = undefined
  searchForm.chapterId = undefined
  searchForm.type = ''
  searchForm.keyword = ''
  chapterList.value = []
  pagination.page = 1
  loadQuestions()
}

const handleAdd = () => {
  isAdd.value = true
  viewMode.value = false
  dialogVisible.value = true
}

const handleView = async (row: Question) => {
  viewMode.value = true
  isAdd.value = false
  if (row.subjectId) {
    formChapterList.value = await loadChapters(row.subjectId)
  }
  
  // 处理选项 - 支持多种格式
  let options = [{ content: '' }, { content: '' }]
  if (row.options) {
    // 如果是JSON字符串，先解析
    let optionsArray = row.options
    if (typeof optionsArray === 'string') {
      try {
        optionsArray = JSON.parse(optionsArray)
      } catch (e) {
        optionsArray = []
      }
    }
    
    if (Array.isArray(optionsArray) && optionsArray.length > 0) {
      options = optionsArray.map((opt: any) => {
        // 如果是对象格式 {key: 'A', value: '内容'} 或 {content: '内容'}
        if (typeof opt === 'object' && opt !== null) {
          return { content: opt.value || opt.content || JSON.stringify(opt) }
        }
        // 如果是字符串，去掉 "A. " 这样的前缀
        if (typeof opt === 'string') {
          return { content: opt.replace(/^[A-Z]\.\s*/, '') }
        }
        return { content: String(opt) }
      })
    }
  }
  
  // 处理答案 - 支持多种格式
  let answer = ''
  if (row.answer) {
    // 如果是JSON字符串，先解析
    if (typeof row.answer === 'string') {
      try {
        const parsed = JSON.parse(row.answer)
        if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
          const answerValue = parsed.answer
          answer = Array.isArray(answerValue) ? answerValue.join('') : String(answerValue)
        } else {
          answer = row.answer
        }
      } catch {
        // 不是JSON字符串，直接使用
        answer = row.answer
      }
    } 
    // 如果是对象
    else if (typeof row.answer === 'object') {
      const answerObj = row.answer as any
      if ('answer' in answerObj) {
        const answerValue = answerObj.answer
        answer = Array.isArray(answerValue) ? answerValue.join('') : String(answerValue)
      } else {
        answer = JSON.stringify(answerObj)
      }
    } 
    // 其他类型
    else {
      answer = String(row.answer)
    }
  }
  
  Object.assign(formData, {
    id: row.id,
    subjectId: row.subjectId,
    chapterId: row.chapterId,
    type: row.type,
    difficulty: row.difficulty,
    title: row.title,
    content: row.content || '',
    options,
    answer,
    answerAnalysis: row.answerAnalysis || ''
  })
  dialogVisible.value = true
}

const handleEdit = async (row: Question) => {
  viewMode.value = false
  isAdd.value = false
  if (row.subjectId) {
    formChapterList.value = await loadChapters(row.subjectId)
  }
  
  // 处理选项 - 支持多种格式
  let options = [{ content: '' }, { content: '' }]
  if (row.options) {
    // 如果是JSON字符串，先解析
    let optionsArray = row.options
    if (typeof optionsArray === 'string') {
      try {
        optionsArray = JSON.parse(optionsArray)
      } catch (e) {
        optionsArray = []
      }
    }
    
    if (Array.isArray(optionsArray) && optionsArray.length > 0) {
      options = optionsArray.map((opt: any) => {
        // 如果是对象格式 {key: 'A', value: '内容'} 或 {content: '内容'}
        if (typeof opt === 'object' && opt !== null) {
          return { content: opt.value || opt.content || JSON.stringify(opt) }
        }
        // 如果是字符串，去掉 "A. " 这样的前缀
        if (typeof opt === 'string') {
          return { content: opt.replace(/^[A-Z]\.\s*/, '') }
        }
        return { content: String(opt) }
      })
    }
  }
  
  // 处理答案 - 支持多种格式
  let answer = ''
  if (row.answer) {
    // 如果是JSON字符串，先解析
    if (typeof row.answer === 'string') {
      try {
        const parsed = JSON.parse(row.answer)
        if (parsed && typeof parsed === 'object' && 'answer' in parsed) {
          const answerValue = parsed.answer
          answer = Array.isArray(answerValue) ? answerValue.join('') : String(answerValue)
        } else {
          answer = row.answer
        }
      } catch {
        // 不是JSON字符串，直接使用
        answer = row.answer
      }
    } 
    // 如果是对象
    else if (typeof row.answer === 'object') {
      const answerObj = row.answer as any
      if ('answer' in answerObj) {
        const answerValue = answerObj.answer
        answer = Array.isArray(answerValue) ? answerValue.join('') : String(answerValue)
      } else {
        answer = JSON.stringify(answerObj)
      }
    } 
    // 其他类型
    else {
      answer = String(row.answer)
    }
  }
  
  Object.assign(formData, {
    id: row.id,
    subjectId: row.subjectId,
    chapterId: row.chapterId,
    type: row.type,
    difficulty: row.difficulty,
    title: row.title,
    content: row.content || '',
    options,
    answer,
    answerAnalysis: row.answerAnalysis || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row: Question) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这道题目吗？',
      '删除题目',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await questionApi.delete(row.id)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const addOption = () => {
  formData.options.push({ content: '' })
}

const removeOption = (index: number) => {
  formData.options.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data: any = {
        subjectId: formData.subjectId,
        chapterId: formData.chapterId,
        type: formData.type,
        difficulty: formData.difficulty,
        title: formData.title,
        content: formData.content || '',
        answerAnalysis: formData.answerAnalysis || ''
      }
      
      // 处理选项 - 后端期望格式: ["A. 选项1", "B. 选项2", ...]
      if (showOptions.value) {
        data.options = formData.options
          .map((opt, index) => {
            const content = opt.content.trim()
            if (!content) return ''
            // 添加字母前缀
            const prefix = String.fromCharCode(65 + index) // A, B, C, D...
            return `${prefix}. ${content}`
          })
          .filter(opt => opt !== '')
      }
      
      // 处理答案 - 后端需要JSON对象格式
      if (formData.type === 'SINGLE' || formData.type === 'FILL' || formData.type === 'SHORT_ANSWER') {
        data.answer = { answer: formData.answer }
      } else if (formData.type === 'MULTIPLE') {
        // 将 "ABC" 转换为 ["A", "B", "C"]
        const answerArray = formData.answer.split('').filter(c => c.trim())
        data.answer = { answer: answerArray }
      } else if (formData.type === 'JUDGE') {
        data.answer = { answer: formData.answer === 'true' || formData.answer === 'TRUE' }
      } else {
        data.answer = { answer: formData.answer }
      }
      
      if (isAdd.value) {
        await questionApi.create(data)
        ElMessage.success('创建成功')
      } else {
        data.id = formData.id
        await questionApi.update(formData.id!, data)
        ElMessage.success('更新成功')
      }
      
      dialogVisible.value = false
      loadQuestions()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadSubjects()
  loadQuestions()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-questions {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-area {
    margin-bottom: $spacing-lg;
  }

  .pagination-container {
    margin-top: $spacing-lg;
    display: flex;
    justify-content: flex-end;
  }

  .options-container {
    width: 100%;

    .option-item {
      display: flex;
      gap: 8px;
      margin-bottom: 8px;
    }
  }

  .form-tip {
    font-size: 12px;
    color: $text-secondary;
    margin-top: 4px;
  }
}
</style>
