<template>
  <div class="page-container admin-subjects">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学科管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增学科
          </el-button>
        </div>
      </template>

      <!-- 学科列表 -->
      <el-table 
        v-loading="loading" 
        :data="subjectList" 
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="学科名称" min-width="150" />
        <el-table-column prop="code" label="学科代码" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="questionCount" label="题目数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.questionCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewChapters(row)">
              章节
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
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="学科名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入学科名称" />
        </el-form-item>
        <el-form-item label="学科代码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入学科代码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入学科描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 章节管理对话框 -->
    <el-dialog
      v-model="chapterDialogVisible"
      title="章节管理"
      width="800px"
    >
      <div class="chapter-header">
        <h4>{{ currentSubject?.name }} - 章节列表</h4>
        <el-button type="primary" size="small" @click="handleAddChapter">
          <el-icon><Plus /></el-icon>
          新增章节
        </el-button>
      </div>
      
      <el-table :data="chapterList" stripe style="margin-top: 16px">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="章节名称" min-width="200" />
        <el-table-column prop="questionCount" label="题目数量" width="100" align="center">
          <template #default="{ row }">
            {{ row.questionCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditChapter(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteChapter(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 章节编辑对话框 -->
    <el-dialog
      v-model="chapterFormVisible"
      :title="chapterFormTitle"
      width="500px"
      @close="handleChapterDialogClose"
    >
      <el-form
        ref="chapterFormRef"
        :model="chapterFormData"
        :rules="chapterFormRules"
        label-width="100px"
      >
        <el-form-item label="章节名称" prop="name">
          <el-input v-model="chapterFormData.name" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="chapterFormData.description" 
            type="textarea" 
            :rows="2"
            placeholder="请输入章节描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="chapterFormData.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChapterSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { subjectApi, chapterApi } from '@/api'
import type { Subject, Chapter } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const chapterDialogVisible = ref(false)
const chapterFormVisible = ref(false)
const isAdd = ref(true)
const isChapterAdd = ref(true)
const formRef = ref<FormInstance>()
const chapterFormRef = ref<FormInstance>()

const subjectList = ref<Subject[]>([])
const chapterList = ref<Chapter[]>([])
const currentSubject = ref<Subject>()

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const chapterFormData = reactive({
  id: undefined as number | undefined,
  subjectId: undefined as number | undefined,
  name: '',
  description: '',
  sortOrder: 0
})

const formRules = {
  name: [{ required: true, message: '请输入学科名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入学科代码', trigger: 'blur' }]
}

const chapterFormRules = {
  name: [{ required: true, message: '请输入章节名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增学科' : '编辑学科')
const chapterFormTitle = computed(() => isChapterAdd.value ? '新增章节' : '编辑章节')

const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await subjectApi.list()
    subjectList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科列表失败')
  } finally {
    loading.value = false
  }
}

const loadChapters = async (subjectId: number) => {
  try {
    const res = await chapterApi.list(subjectId)
    chapterList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载章节列表失败')
  }
}

const handleAdd = () => {
  isAdd.value = true
  dialogVisible.value = true
}

const handleEdit = (row: Subject) => {
  isAdd.value = false
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description || '',
    sortOrder: row.sortOrder || 0,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row: Subject) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学科 ${row.name} 吗？此操作将同时删除该学科下的所有章节和题目`,
      '删除学科',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await subjectApi.delete(row.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleStatusChange = async (row: Subject) => {
  try {
    await subjectApi.update(row.id, { ...row, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
    row.status = row.status === 1 ? 0 : 1 // 回滚
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isAdd.value) {
        await subjectApi.create(formData as any)
        ElMessage.success('创建成功')
      } else {
        await subjectApi.update(formData.id!, formData as any)
        ElMessage.success('更新成功')
      }
      
      dialogVisible.value = false
      loadSubjects()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: undefined,
    name: '',
    code: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
}

const handleViewChapters = async (row: Subject) => {
  currentSubject.value = row
  await loadChapters(row.id)
  chapterDialogVisible.value = true
}

const handleAddChapter = () => {
  isChapterAdd.value = true
  chapterFormData.subjectId = currentSubject.value?.id
  chapterFormVisible.value = true
}

const handleEditChapter = (row: Chapter) => {
  isChapterAdd.value = false
  Object.assign(chapterFormData, {
    id: row.id,
    subjectId: row.subjectId,
    name: row.name,
    description: row.description || '',
    sortOrder: row.sortOrder || 0
  })
  chapterFormVisible.value = true
}

const handleDeleteChapter = async (row: Chapter) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除章节 ${row.name} 吗？`,
      '删除章节',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await chapterApi.delete(row.id)
    ElMessage.success('删除成功')
    loadChapters(currentSubject.value!.id)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleChapterSubmit = async () => {
  if (!chapterFormRef.value) return
  
  await chapterFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isChapterAdd.value) {
        await chapterApi.create(chapterFormData as any)
        ElMessage.success('创建成功')
      } else {
        await chapterApi.update(chapterFormData.id!, chapterFormData as any)
        ElMessage.success('更新成功')
      }
      
      chapterFormVisible.value = false
      loadChapters(currentSubject.value!.id)
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleChapterDialogClose = () => {
  chapterFormRef.value?.resetFields()
  Object.assign(chapterFormData, {
    id: undefined,
    subjectId: undefined,
    name: '',
    description: '',
    sortOrder: 0
  })
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-subjects {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .chapter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    h4 {
      margin: 0;
      font-size: 16px;
      color: $text-primary;
    }
  }
}
</style>
