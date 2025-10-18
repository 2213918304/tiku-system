<template>
  <div class="page-container admin-import">
    <el-row :gutter="16">
      <!-- 导入面板 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Excel批量导入</span>
              <el-button type="success" @click="downloadTemplate">
                <el-icon><Download /></el-icon>
                下载Excel模板
              </el-button>
            </div>
          </template>
          
          <!-- 导入模式选择 -->
          <el-tabs v-model="importMode" @tab-change="handleModeChange">
            <el-tab-pane label="按章节导入" name="chapter">
              <template #label>
                <span style="font-size: 14px">
                  <el-icon style="vertical-align: middle"><Folder /></el-icon>
                  按章节导入（传统模式）
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="智能批量导入" name="smart">
              <template #label>
                <span style="font-size: 14px">
                  <el-icon style="vertical-align: middle"><MagicStick /></el-icon>
                  智能批量导入（推荐）
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <!-- 步骤条（根据导入模式显示不同步骤） -->
          <el-steps v-if="importMode === 'chapter'" :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
            <el-step title="选择学科章节" />
            <el-step title="上传Excel文件" />
            <el-step title="预览数据" />
            <el-step title="确认导入" />
          </el-steps>
          
          <el-steps v-else :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px">
            <el-step title="选择学科" />
            <el-step title="上传Excel文件" />
            <el-step title="预览数据" />
            <el-step title="确认导入" />
          </el-steps>

          <!-- 步骤1: 选择学科章节（按章节导入） -->
          <div v-show="currentStep === 0 && importMode === 'chapter'" class="step-content">
            <el-alert
              title="导入说明"
              type="info"
              :closable="false"
              style="margin-bottom: 20px"
            >
              <div class="import-tips">
                <p><strong>支持的Excel格式：</strong></p>
                <ul>
                  <li>Excel 2007+ (.xlsx)</li>
                  <li>文件大小不超过10MB</li>
                  <li>建议单次导入不超过500道题目</li>
                </ul>
                <p><strong>Excel列说明：</strong></p>
                <ul>
                  <li><b>题目内容*：</b>题目的完整描述（必填）</li>
                  <li><b>题型*：</b>单选题/多选题/判断题/填空题/简答题（必填）</li>
                  <li><b>选项A-D：</b>选择题的选项（选择题必填）</li>
                  <li><b>正确答案*：</b>单选：A；多选：AB；判断：对/错；填空：答案1;答案2（必填）</li>
                  <li><b>解析：</b>答案解析说明（选填）</li>
                  <li><b>难度：</b>1-5（默认3，选填）</li>
                </ul>
              </div>
            </el-alert>

            <el-form :model="importForm" label-width="100px" style="max-width: 600px">
              <el-form-item label="选择学科" required>
                <el-select 
                  v-model="importForm.subjectId" 
                  placeholder="请选择学科" 
                  style="width: 100%"
                  @change="onSubjectChange"
                >
                  <el-option 
                    v-for="subject in subjectList" 
                    :key="subject.id" 
                    :label="subject.name" 
                    :value="subject.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="选择章节" required>
                <el-select 
                  v-model="importForm.chapterId" 
                  placeholder="请选择章节" 
                  style="width: 100%"
                  :disabled="!importForm.subjectId"
                >
                  <el-option 
                    v-for="chapter in chapterList" 
                    :key="chapter.id" 
                    :label="chapter.name" 
                    :value="chapter.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="nextStep"
                  :disabled="!importForm.subjectId || !importForm.chapterId"
                >
                  下一步
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤1: 选择学科（智能批量导入） -->
          <div v-show="currentStep === 0 && importMode === 'smart'" class="step-content">
            <el-alert
              title="智能导入说明"
              type="success"
              :closable="false"
              style="margin-bottom: 20px"
            >
              <div class="import-tips">
                <p><strong>✨ 智能批量导入优势：</strong></p>
                <ul>
                  <li>📁 <b>自动创建章节</b>：根据Excel中的"章节序号"或"章节名称"自动创建章节</li>
                  <li>🚀 <b>一次导入多章节</b>：无需逐个章节上传，一次性导入整个学科的题目</li>
                  <li>🎯 <b>智能归类</b>：系统自动将题目归类到对应章节</li>
                  <li>💡 <b>灵活设置</b>：支持"章节序号"（如1、2、3）或"章节名称"（如"绪论"）</li>
                </ul>
                <p><strong>Excel格式要求：</strong></p>
                <ul>
                  <li>必填列：<b>章节序号/章节名称、题目内容、题型、正确答案</b></li>
                  <li>选填列：选项A-D、解析、难度、分值</li>
                  <li>章节信息：至少填写"章节序号"或"章节名称"其中一列</li>
                </ul>
              </div>
            </el-alert>

            <el-form :model="importForm" label-width="100px" style="max-width: 600px">
              <el-form-item label="选择学科" required>
                <el-select 
                  v-model="importForm.subjectId" 
                  placeholder="请选择学科" 
                  style="width: 100%"
                >
                  <el-option 
                    v-for="subject in subjectList" 
                    :key="subject.id" 
                    :label="subject.name" 
                    :value="subject.id"
                  />
                </el-select>
                <div style="margin-top: 8px; font-size: 12px; color: #909399">
                  ℹ️ 智能导入模式下，无需选择章节，系统会根据Excel中的章节信息自动创建和归类
                </div>
              </el-form-item>

              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="nextStep"
                  :disabled="!importForm.subjectId"
                >
                  下一步
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 步骤2: 上传文件 -->
          <div v-show="currentStep === 1" class="step-content">
            <div class="upload-area">
              <el-upload
                ref="uploadRef"
                class="upload-demo"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                :before-remove="handleFileRemove"
                :limit="1"
                accept=".xlsx,.xls"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  将Excel文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    仅支持 .xlsx 或 .xls 格式，文件大小不超过10MB
                  </div>
                </template>
              </el-upload>
            </div>

            <div class="step-actions">
              <el-button @click="prevStep">上一步</el-button>
              <el-button 
                type="primary" 
                @click="parseExcel"
                :disabled="!selectedFile"
                :loading="parsing"
              >
                解析Excel
              </el-button>
            </div>
          </div>

          <!-- 步骤3: 预览数据 -->
          <div v-show="currentStep === 2" class="step-content">
            <el-alert
              :title="`共解析到 ${previewData.length} 道题目`"
              type="success"
              :closable="false"
              style="margin-bottom: 20px"
            />

            <el-table 
              :data="previewData.slice(0, 10)" 
              stripe 
              border
              max-height="400"
              style="margin-bottom: 20px"
            >
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
              <el-table-column prop="type" label="题型" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ getTypeName(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="correctAnswer" label="答案" width="100" />
              <el-table-column prop="difficulty" label="难度" width="80" align="center" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.valid ? 'success' : 'danger'" size="small">
                    {{ row.valid ? '有效' : '无效' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <el-alert v-if="previewData.length > 10" type="info" :closable="false">
              仅显示前10条数据，实际将导入 {{ previewData.length }} 条
            </el-alert>

            <div v-if="invalidCount > 0" style="margin-top: 16px">
              <el-alert type="warning" :closable="false">
                <template #title>
                  检测到 {{ invalidCount }} 条无效数据，这些数据将被跳过
                </template>
                <ul style="margin-top: 8px; padding-left: 20px">
                  <li v-for="(error, index) in errorMessages.slice(0, 5)" :key="index">
                    {{ error }}
                  </li>
                  <li v-if="errorMessages.length > 5">
                    还有 {{ errorMessages.length - 5 }} 条错误...
                  </li>
                </ul>
              </el-alert>
            </div>

            <div class="step-actions">
              <el-button @click="prevStep">上一步</el-button>
              <el-button 
                type="primary" 
                @click="nextStep"
                :disabled="validCount === 0"
              >
                确认导入（{{ validCount }}条）
              </el-button>
            </div>
          </div>

          <!-- 步骤4: 导入结果 -->
          <div v-show="currentStep === 3" class="step-content">
            <div v-if="importing" class="importing-status">
              <el-progress 
                :percentage="importProgress" 
                :status="importStatus"
                :stroke-width="20"
              />
              <p style="text-align: center; margin-top: 16px; color: #606266">
                正在导入第 {{ importedCount }} / {{ totalCount }} 题...
              </p>
            </div>

            <div v-else-if="importResult">
              <el-result
                :icon="importResult.success ? 'success' : 'error'"
                :title="importResult.title"
                :sub-title="importResult.message"
              >
                <template #extra>
                  <div class="result-stats" v-if="importResult.success">
                    <el-statistic title="成功导入" :value="importResult.successCount" />
                    <el-statistic title="失败" :value="importResult.failCount" />
                    <el-statistic title="总耗时" :value="importResult.duration" suffix="秒" />
                  </div>
                  <div style="margin-top: 24px">
                    <el-button type="primary" @click="resetImport">继续导入</el-button>
                    <el-button @click="viewQuestions">查看题目</el-button>
                  </div>
                </template>
              </el-result>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快速导入和历史记录 -->
      <el-col :span="8">
        <!-- 快速操作 -->
        <el-card style="margin-bottom: 16px">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <div class="action-section">
              <el-divider content-position="left">下载模板</el-divider>
              <el-button 
                type="success" 
                class="action-btn"
                @click="downloadSmartTemplate"
              >
                <el-icon><MagicStick /></el-icon>
                智能导入模板（推荐）
              </el-button>
              <el-button 
                type="primary" 
                class="action-btn"
                @click="downloadChapterTemplate"
              >
                <el-icon><Folder /></el-icon>
                按章节导入模板
              </el-button>
            </div>
            
            <div class="action-section">
              <el-divider content-position="left">其他操作</el-divider>
              <el-button class="action-btn" @click="viewSample">
                <el-icon><View /></el-icon>
                查看两种模板对比
              </el-button>
              <el-button class="action-btn" @click="resetAll">
                <el-icon><RefreshLeft /></el-icon>
                重置全部
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 导入历史 -->
        <el-card>
          <template #header>
            <span>最近导入</span>
          </template>
          <el-timeline v-if="importHistory.length > 0">
            <el-timeline-item
              v-for="record in importHistory.slice(0, 5)"
              :key="record.id"
              :timestamp="record.createdAt"
              placement="top"
            >
              <div class="history-item">
                <p><strong>{{ record.subjectName }}</strong> / {{ record.chapterName }}</p>
                <p>
                  <el-tag :type="record.status === 'success' ? 'success' : 'danger'" size="small">
                    {{ record.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                  成功：{{ record.successCount }}道
                  <span v-if="record.failCount > 0">/ 失败：{{ record.failCount }}道</span>
                </p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无导入记录" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 示例对话框 - 显示两种模板对比 -->
    <el-dialog v-model="sampleDialogVisible" title="两种导入模式对比" width="95%" top="5vh">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <template #title>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>📊 两种导入模式的Excel表格列差异对比</span>
            <div>
              <el-button type="success" size="small" @click="downloadSmartTemplate">
                <el-icon><Download /></el-icon>
                下载智能导入模板
              </el-button>
              <el-button type="primary" size="small" @click="downloadChapterTemplate" style="margin-left: 8px">
                <el-icon><Download /></el-icon>
                下载按章节导入模板
              </el-button>
            </div>
          </div>
        </template>
      </el-alert>

      <el-tabs v-model="sampleTab">
        <!-- 智能导入模板示例 -->
        <el-tab-pane label="智能批量导入模板（推荐）" name="smart">
          <el-alert type="success" :closable="false" style="margin-bottom: 12px">
            <p><strong>✨ 特点：</strong>包含"章节序号"和"章节名称"列，可一次性导入多个章节的题目</p>
            <p><strong>📁 自动创建：</strong>系统会根据章节信息自动创建章节并归类题目</p>
          </el-alert>
          <el-table :data="smartSampleData" border max-height="400">
            <el-table-column prop="chapterOrder" label="章节序号*" width="100" fixed>
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ row.chapterOrder }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="chapterName" label="章节名称*" width="140" fixed>
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.chapterName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容*" min-width="200" />
            <el-table-column prop="type" label="题型*" width="100" />
            <el-table-column prop="optionA" label="选项A" width="120" />
            <el-table-column prop="optionB" label="选项B" width="120" />
            <el-table-column prop="optionC" label="选项C" width="120" />
            <el-table-column prop="optionD" label="选项D" width="120" />
            <el-table-column prop="correctAnswer" label="正确答案*" width="100" />
            <el-table-column prop="explanation" label="解析" min-width="150" />
            <el-table-column prop="difficulty" label="难度" width="80" />
            <el-table-column prop="score" label="分值" width="80" />
          </el-table>
        </el-tab-pane>

        <!-- 按章节导入模板示例 -->
        <el-tab-pane label="按章节导入模板（传统）" name="chapter">
          <el-alert type="info" :closable="false" style="margin-bottom: 12px">
            <p><strong>📂 特点：</strong>不包含章节信息列，需要先在页面上选择学科和章节</p>
            <p><strong>⚠️ 限制：</strong>每次只能导入一个章节的题目，导入多个章节需要多次操作</p>
          </el-alert>
          <el-table :data="chapterSampleData" border max-height="400">
            <el-table-column prop="content" label="题目内容*" min-width="200" fixed />
            <el-table-column prop="type" label="题型*" width="100" />
            <el-table-column prop="optionA" label="选项A" width="120" />
            <el-table-column prop="optionB" label="选项B" width="120" />
            <el-table-column prop="optionC" label="选项C" width="120" />
            <el-table-column prop="optionD" label="选项D" width="120" />
            <el-table-column prop="correctAnswer" label="正确答案*" width="100" />
            <el-table-column prop="explanation" label="解析" min-width="150" />
            <el-table-column prop="difficulty" label="难度" width="80" />
            <el-table-column prop="score" label="分值" width="80" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <el-divider />
      
      <div class="comparison-tips">
        <h4>📝 列说明对比：</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <h5 style="color: #67c23a">✅ 智能导入特有列：</h5>
            <ul>
              <li><el-tag type="warning" size="small">章节序号</el-tag> 用于排序（如：1、2、3）</li>
              <li><el-tag type="success" size="small">章节名称</el-tag> 章节的完整名称（如："第一章 绪论"）</li>
            </ul>
          </el-col>
          <el-col :span="12">
            <h5 style="color: #409eff">📌 两种模式通用列：</h5>
            <ul>
              <li>题目内容*、题型*、正确答案*（必填）</li>
              <li>选项A-D（选择题必填）</li>
              <li>解析、难度、分值（选填）</li>
            </ul>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled, View, RefreshLeft, Folder, MagicStick } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { subjectApi, chapterApi, dataImportApi } from '@/api'
import type { Subject, Chapter } from '@/types'
import * as XLSX from 'xlsx'

const router = useRouter()

const importMode = ref<'chapter' | 'smart'>('smart') // 默认使用智能导入
const currentStep = ref(0)
const parsing = ref(false)
const importing = ref(false)
const importProgress = ref(0)
const importStatus = ref<'success' | 'exception' | ''>('')
const selectedFile = ref<File | null>(null)
const sampleDialogVisible = ref(false)

const importForm = reactive({
  subjectId: undefined as number | undefined,
  chapterId: undefined as number | undefined
})

const subjectList = ref<Subject[]>([])
const chapterList = ref<Chapter[]>([])
const previewData = ref<any[]>([])
const errorMessages = ref<string[]>([])
const importedCount = ref(0)
const totalCount = ref(0)
const sampleTab = ref('smart') // 示例对话框的标签页

interface ImportResult {
  success: boolean
  title: string
  message: string
  successCount: number
  failCount: number
  duration: number
}

const importResult = ref<ImportResult | null>(null)

interface ImportHistory {
  id: number
  subjectName: string
  chapterName: string
  status: 'success' | 'failure'
  successCount: number
  failCount: number
  createdAt: string
}

const importHistory = ref<ImportHistory[]>([])

// 智能导入示例数据（包含章节信息）
const smartSampleData = [
  {
    chapterOrder: '1',
    chapterName: '第一章 绪论',
    content: '马克思主义哲学的根本特征是什么？',
    type: '单选题',
    optionA: '科学性',
    optionB: '革命性',
    optionC: '实践性',
    optionD: '科学性和革命性的统一',
    correctAnswer: 'D',
    explanation: '马克思主义哲学实现了科学性和革命性的统一，这是其根本特征。',
    difficulty: 3,
    score: 2
  },
  {
    chapterOrder: '1',
    chapterName: '第一章 绪论',
    content: '辩证唯物主义认识论的基本观点包括哪些？',
    type: '多选题',
    optionA: '实践是认识的基础',
    optionB: '认识是主体对客体的能动反映',
    optionC: '认识是在实践基础上不断深化的过程',
    optionD: '真理是绝对的',
    correctAnswer: 'ABC',
    explanation: '辩证唯物主义认识论强调实践的基础性作用，认识的能动性和发展性。',
    difficulty: 4,
    score: 3
  },
  {
    chapterOrder: '2',
    chapterName: '第二章 唯物辩证法',
    content: '唯物辩证法认为，矛盾是事物发展的动力。',
    type: '判断题',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: '对',
    explanation: '矛盾是事物发展的根本动力，这是唯物辩证法的基本观点。',
    difficulty: 2,
    score: 2
  },
  {
    chapterOrder: '2',
    chapterName: '第二章 唯物辩证法',
    content: '请简述质量互变规律的基本内容。',
    type: '简答题',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: '答案要点：1.量变是质变的必要准备；2.质变是量变的必然结果；3.量变和质变相互渗透',
    explanation: '质量互变规律是唯物辩证法的基本规律之一。',
    difficulty: 4,
    score: 10
  }
]

// 按章节导入示例数据（不包含章节信息）
const chapterSampleData = [
  {
    content: '马克思主义哲学的根本特征是什么？',
    type: '单选题',
    optionA: '科学性',
    optionB: '革命性',
    optionC: '实践性',
    optionD: '科学性和革命性的统一',
    correctAnswer: 'D',
    explanation: '马克思主义哲学实现了科学性和革命性的统一，这是其根本特征。',
    difficulty: 3,
    score: 2
  },
  {
    content: '辩证唯物主义认识论的基本观点包括哪些？',
    type: '多选题',
    optionA: '实践是认识的基础',
    optionB: '认识是主体对客体的能动反映',
    optionC: '认识是在实践基础上不断深化的过程',
    optionD: '真理是绝对的',
    correctAnswer: 'ABC',
    explanation: '辩证唯物主义认识论强调实践的基础性作用，认识的能动性和发展性。',
    difficulty: 4,
    score: 3
  },
  {
    content: '唯物辩证法认为，矛盾是事物发展的动力。',
    type: '判断题',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: '对',
    explanation: '矛盾是事物发展的根本动力，这是唯物辩证法的基本观点。',
    difficulty: 2,
    score: 2
  }
]

const validCount = computed(() => previewData.value.filter(item => item.valid).length)
const invalidCount = computed(() => previewData.value.filter(item => !item.valid).length)

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TRUE_FALSE': '判断题',
    'FILL_BLANK': '填空题',
    'SHORT_ANSWER': '简答题',
    '单选题': '单选题',
    '多选题': '多选题',
    '判断题': '判断题',
    '填空题': '填空题',
    '简答题': '简答题'
  }
  return map[type] || type
}

const getTypeCode = (typeName: string) => {
  const map: Record<string, string> = {
    '单选题': 'SINGLE_CHOICE',
    '多选题': 'MULTIPLE_CHOICE',
    '判断题': 'TRUE_FALSE',
    '填空题': 'FILL_BLANK',
    '简答题': 'SHORT_ANSWER'
  }
  return map[typeName] || typeName
}

const loadSubjects = async () => {
  try {
    const res = await subjectApi.list()
    subjectList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科列表失败')
  }
}

const onSubjectChange = async (subjectId: number | undefined) => {
  importForm.chapterId = undefined
  chapterList.value = []
  if (subjectId) {
    try {
      const res = await chapterApi.list(subjectId)
      chapterList.value = res.data || []
    } catch (error: any) {
      ElMessage.error(error.message || '加载章节列表失败')
    }
  }
}

const handleModeChange = () => {
  // 切换模式时重置表单
  resetAll()
}

const nextStep = () => {
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw
}

const handleFileRemove = () => {
  selectedFile.value = null
  return true
}

const parseExcel = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  parsing.value = true
  try {
    const data = await readExcelFile(selectedFile.value)
    previewData.value = data.validData
    errorMessages.value = data.errors
    
    if (data.validData.length === 0) {
      ElMessage.error('Excel文件中没有有效数据')
      return
    }

    ElMessage.success(`成功解析${data.validData.length}条数据`)
    nextStep()
  } catch (error: any) {
    ElMessage.error(error.message || '解析Excel失败')
  } finally {
    parsing.value = false
  }
}

const readExcelFile = (file: File): Promise<{ validData: any[], errors: string[] }> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    
    reader.onload = (e: any) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        const jsonData = XLSX.utils.sheet_to_json(firstSheet)

        const validData: any[] = []
        const errors: string[] = []

        jsonData.forEach((row: any, index: number) => {
          const rowNum = index + 2 // Excel行号从2开始（第1行是表头）
          const item: any = {
            // 智能导入模式特有字段
            chapterOrder: row['章节序号'] || row['chapterOrder'],
            chapterName: row['章节名称'] || row['chapterName'],
            // 通用字段
            content: row['题目内容'] || row['content'],
            type: getTypeCode(row['题型'] || row['type']),
            correctAnswer: row['正确答案'] || row['correctAnswer'],
            explanation: row['解析'] || row['explanation'] || '',
            difficulty: row['难度'] || row['difficulty'] || 3,
            score: row['分值'] || row['score'],
            valid: true,
            errors: []
          }

          // 验证必填字段
          if (!item.content) {
            item.valid = false
            item.errors.push('题目内容不能为空')
            errors.push(`第${rowNum}行：题目内容不能为空`)
          }

          if (!item.type) {
            item.valid = false
            item.errors.push('题型不能为空')
            errors.push(`第${rowNum}行：题型不能为空`)
          }

          if (!item.correctAnswer) {
            item.valid = false
            item.errors.push('正确答案不能为空')
            errors.push(`第${rowNum}行：正确答案不能为空`)
          }

          // 如果是选择题，解析选项
          if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(item.type)) {
            item.options = []
            const optionKeys = ['选项A', '选项B', '选项C', '选项D', 'optionA', 'optionB', 'optionC', 'optionD']
            for (let i = 0; i < 4; i++) {
              const content = row[optionKeys[i]] || row[optionKeys[i + 4]] || ''
              if (content) {
                item.options.push({ content })
              }
            }

            if (item.options.length < 2) {
              item.valid = false
              item.errors.push('选择题至少需要2个选项')
              errors.push(`第${rowNum}行：选择题至少需要2个选项`)
            }
          }

          validData.push(item)
        })

        resolve({ validData, errors })
      } catch (error) {
        reject(error)
      }
    }

    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }

    reader.readAsArrayBuffer(file)
  })
}

const startImport = async () => {
  importing.value = true
  importProgress.value = 0
  importedCount.value = 0
  totalCount.value = validCount.value
  const startTime = Date.now()

  try {
    const validQuestions = previewData.value.filter(item => item.valid)
    let successCount = 0
    let failCount = 0

    if (importMode.value === 'smart') {
      // 智能批量导入
      const importData = {
        subjectId: importForm.subjectId,
        questions: validQuestions.map(q => ({
          chapterOrder: q.chapterOrder,
          chapterName: q.chapterName,
          content: q.content,
          type: q.type,
          options: q.options,
          correctAnswer: q.correctAnswer,
          explanation: q.explanation,
          difficulty: q.difficulty,
          score: q.score
        }))
      }

      try {
        const res = await dataImportApi.smartImport(importData)
        successCount = res.data.successCount || 0
        failCount = res.data.failCount || 0
        importProgress.value = 100
      } catch (error: any) {
        failCount = validQuestions.length
        ElMessage.error(error.message || '智能导入失败')
      }
    } else {
      // 按章节批量导入
      // 需要根据 chapterId 获取 chapterName
      const chapter = chapterList.value.find(c => c.id === importForm.chapterId)
      const chapterName = chapter ? chapter.name : '未分类'
      
      // 为每道题添加章节信息
      const questionsWithChapter = validQuestions.map(q => ({
        chapterName: chapterName,
        chapterOrder: '', // 按章节导入不需要章节序号
        content: q.content,
        type: q.type,
        options: q.options,
        correctAnswer: q.correctAnswer,
        explanation: q.explanation,
        difficulty: q.difficulty,
        score: q.score
      }))
      
      const importData = {
        subjectId: importForm.subjectId,
        questions: questionsWithChapter
      }

      try {
        const res = await dataImportApi.importBatch(importData)
        successCount = res.data.successCount || 0
        failCount = res.data.failCount || 0
        importProgress.value = 100
      } catch (error: any) {
        failCount = validQuestions.length
        ElMessage.error(error.message || '批量导入失败')
      }
    }

    const duration = ((Date.now() - startTime) / 1000).toFixed(1)

    importResult.value = {
      success: successCount > 0,
      title: successCount > 0 ? '导入完成！' : '导入失败',
      message: `成功导入 ${successCount} 道题目${failCount > 0 ? `，失败 ${failCount} 道` : ''}`,
      successCount,
      failCount,
      duration: parseFloat(duration)
    }

    importProgress.value = 100
    importStatus.value = successCount > 0 ? 'success' : 'exception'

    // 添加到历史记录
    const subject = subjectList.value.find(s => s.id === importForm.subjectId)
    const chapter = chapterList.value.find(c => c.id === importForm.chapterId)
    importHistory.value.unshift({
      id: Date.now(),
      subjectName: subject?.name || '',
      chapterName: chapter?.name || '',
      status: successCount > 0 ? 'success' : 'failure',
      successCount,
      failCount,
      createdAt: new Date().toLocaleString('zh-CN')
    })

    if (successCount > 0) {
      ElMessage.success('导入成功')
    }
  } catch (error: any) {
    importResult.value = {
      success: false,
      title: '导入失败',
      message: error.message || '未知错误',
      successCount: 0,
      failCount: totalCount.value,
      duration: 0
    }
    importStatus.value = 'exception'
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

// 下载智能导入模板
const downloadSmartTemplate = () => {
  // 智能导入模板（包含章节信息）
  const templateData = [
      {
        '章节序号': '1',
        '章节名称': '第一章 绪论',
        '题目内容': '这是第一章的单选题示例？',
        '题型': '单选题',
        '选项A': '选项A内容',
        '选项B': '选项B内容',
        '选项C': '选项C内容',
        '选项D': '选项D内容',
        '正确答案': 'A',
        '解析': '这是答案解析',
        '难度': 3,
        '分值': 2
      },
      {
        '章节序号': '1',
        '章节名称': '第一章 绪论',
        '题目内容': '这是第一章的多选题示例？',
        '题型': '多选题',
        '选项A': '选项A内容',
        '选项B': '选项B内容',
        '选项C': '选项C内容',
        '选项D': '选项D内容',
        '正确答案': 'ABC',
        '解析': '这是答案解析',
        '难度': 4,
        '分值': 3
      },
      {
        '章节序号': '2',
        '章节名称': '第二章 概述',
        '题目内容': '这是第二章的判断题示例',
        '题型': '判断题',
        '选项A': '',
        '选项B': '',
        '选项C': '',
        '选项D': '',
        '正确答案': '对',
        '解析': '这是答案解析',
        '难度': 2,
        '分值': 2
      },
      {
        '章节序号': '2',
        '章节名称': '第二章 概述',
        '题目内容': '这是第二章的简答题示例，请简述...？',
        '题型': '简答题',
        '选项A': '',
        '选项B': '',
        '选项C': '',
        '选项D': '',
        '正确答案': '答案要点：1...; 2...; 3...',
        '解析': '详细解析说明',
        '难度': 4,
        '分值': 10
      }
    ]
  
  // 创建工作簿
  const ws = XLSX.utils.json_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '智能导入模板')

  // 下载文件
  XLSX.writeFile(wb, '智能批量导入模板.xlsx')
  ElMessage.success('智能导入模板下载成功！')
}

// 下载按章节导入模板
const downloadChapterTemplate = () => {
  // 按章节导入模板（不需要章节信息）
  const templateData = [
      {
        '题目内容': '这是一道单选题示例？',
        '题型': '单选题',
        '选项A': '选项A内容',
        '选项B': '选项B内容',
        '选项C': '选项C内容',
        '选项D': '选项D内容',
        '正确答案': 'A',
        '解析': '这是答案解析',
        '难度': 3,
        '分值': 2
      },
      {
        '题目内容': '这是一道多选题示例？',
        '题型': '多选题',
        '选项A': '选项A内容',
        '选项B': '选项B内容',
        '选项C': '选项C内容',
        '选项D': '选项D内容',
        '正确答案': 'ABC',
        '解析': '这是答案解析',
        '难度': 4,
        '分值': 3
      },
      {
        '题目内容': '这是一道判断题示例',
        '题型': '判断题',
        '选项A': '',
        '选项B': '',
        '选项C': '',
        '选项D': '',
        '正确答案': '对',
        '解析': '这是答案解析',
        '难度': 2,
        '分值': 2
      }
    ]
  
  // 创建工作簿
  const ws = XLSX.utils.json_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '按章节导入模板')

  // 下载文件
  XLSX.writeFile(wb, '按章节导入模板.xlsx')
  ElMessage.success('按章节导入模板下载成功！')
}

// 根据当前模式下载对应模板
const downloadTemplate = () => {
  if (importMode.value === 'smart') {
    downloadSmartTemplate()
  } else {
    downloadChapterTemplate()
  }
}

const viewSample = () => {
  sampleDialogVisible.value = true
}

const resetAll = () => {
  currentStep.value = 0
  selectedFile.value = null
  previewData.value = []
  errorMessages.value = []
  importResult.value = null
  importProgress.value = 0
}

const resetImport = () => {
  resetAll()
}

const viewQuestions = () => {
  router.push('/admin/questions')
}

// 监听步骤变化，自动触发导入
watch(currentStep, async (newStep) => {
  if (newStep === 3 && validCount.value > 0 && !importing.value && !importResult.value) {
    // 延迟一下，让UI有时间渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    await startImport()
  }
})

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-import {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .import-tips {
    font-size: 13px;

    p {
      margin: 8px 0;
    }

    ul {
      margin: 8px 0;
      padding-left: 20px;

      li {
        margin: 4px 0;
      }
    }
  }

  .step-content {
    min-height: 400px;
    padding: 20px 0;
  }

  .upload-area {
    max-width: 600px;
    margin: 0 auto;
  }

  .step-actions {
    margin-top: 30px;
    text-align: center;
  }

  .importing-status {
    padding: 40px;
  }

  .result-stats {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin: 20px 0;
  }

  .quick-actions {
    display: flex;
    flex-direction: column;
    
    .action-section {
      margin-bottom: $spacing-md;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .el-divider {
        margin: $spacing-md 0;
      }
      
      .action-btn {
        width: 100%;
        margin-bottom: $spacing-sm;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .history-item {
    p {
      margin: 4px 0;
      font-size: 13px;
    }
  }
  
  .comparison-tips {
    padding: 16px;
    background: $bg-gray;
    border-radius: $border-radius-md;
    
    h4 {
      margin-top: 0;
      margin-bottom: 16px;
      font-size: 16px;
      color: $text-primary;
    }
    
    h5 {
      margin-top: 0;
      margin-bottom: 12px;
      font-size: 14px;
    }
    
    ul {
      margin: 8px 0;
      padding-left: 20px;
      
      li {
        margin: 8px 0;
        font-size: 13px;
        line-height: 1.6;
      }
    }
  }
}
</style>
