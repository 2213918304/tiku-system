<template>
  <div class="page-container notes-page">
    <div class="page-header">
      <h1>学习笔记</h1>
      <p>记录学习心得，整理知识要点</p>
    </div>

    <!-- 快速操作 -->
    <el-row :gutter="16" class="action-row">
      <el-col :span="24">
        <el-button type="primary" size="large" @click="showNoteDialog = true">
          <el-icon><Plus /></el-icon>
          新建笔记
        </el-button>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :model="filters" :inline="true">
        <el-form-item label="学科">
          <el-select v-model="filters.subjectId" placeholder="全部学科" clearable style="width: 180px">
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="filters.keyword" placeholder="搜索笔记标题或内容..." clearable style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleFilter">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 笔记列表 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="note in paginatedNotes" :key="note.id" :xs="24" :sm="12" :lg="8">
        <el-card class="note-card" @click="viewNote(note)">
          <div class="note-header">
            <h3 class="note-title">{{ note.title }}</h3>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, note)">
              <el-button text circle>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="note-content">{{ note.content }}</div>
          
          <div class="note-footer">
            <el-tag v-if="note.questionTitle" size="small" type="info">
              <el-icon><Link /></el-icon>
              关联题目
            </el-tag>
            <span class="note-time">{{ formatTime(note.createTime) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="filteredNotes.length === 0" description="暂无笔记" />

    <!-- 分页 -->
    <div v-if="filteredNotes.length > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredNotes.length"
        layout="total, prev, pager, next"
      />
    </div>

    <!-- 新建/编辑笔记对话框 -->
    <el-dialog v-model="showNoteDialog" :title="editingNote ? '编辑笔记' : '新建笔记'" width="800px">
      <el-form :model="noteForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="noteForm.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="学科">
          <el-select v-model="noteForm.subjectId" placeholder="选择学科" style="width: 100%">
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="noteForm.content" type="textarea" :rows="10" placeholder="请输入笔记内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNoteDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, MoreFilled, Link } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { noteApi } from '@/api/modules/note'
import { subjectApi } from '@/api/modules/subject'

const subjects = ref<any[]>([])
const notes = ref<any[]>([])
const showNoteDialog = ref(false)
const editingNote = ref<any>(null)
const currentPage = ref(1)
const pageSize = ref(9)
const loading = ref(false)

const filters = ref({
  subjectId: null,
  keyword: ''
})

const noteForm = ref({
  title: '',
  subjectId: null,
  content: '',
  questionId: null
})

const filteredNotes = computed(() => {
  let result = notes.value
  if (filters.value.subjectId) {
    result = result.filter(n => n.subjectId === filters.value.subjectId)
  }
  if (filters.value.keyword) {
    const kw = filters.value.keyword.toLowerCase()
    result = result.filter(n => 
      n.title.toLowerCase().includes(kw) || 
      n.content.toLowerCase().includes(kw)
    )
  }
  return result
})

const paginatedNotes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredNotes.value.slice(start, start + pageSize.value)
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    // 加载学科列表
    const subjectsRes = await subjectApi.list()
    subjects.value = subjectsRes.data || []
    
    // 加载笔记列表
    const notesRes = await noteApi.getAllMyNotes()
    notes.value = (notesRes.data || []).map((note: any) => ({
      id: note.id,
      title: note.title,
      content: note.content,
      subjectId: note.subjectId,
      subjectName: note.subjectName,
      questionId: note.questionId,
      questionTitle: note.questionTitle,
      chapterId: note.chapterId,
      chapterName: note.chapterName,
      createTime: note.createTime,
      updateTime: note.updateTime
    }))
  } catch (error: any) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  currentPage.value = 1
}

const viewNote = (note: any) => {
  ElMessageBox.alert(note.content, note.title, {
    confirmButtonText: '关闭'
  })
}

const handleCommand = async (cmd: string, note: any) => {
  if (cmd === 'edit') {
    editingNote.value = note
    noteForm.value = {
      title: note.title,
      subjectId: note.subjectId,
      content: note.content,
      questionId: note.questionId
    }
    showNoteDialog.value = true
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这条笔记吗？', '确认删除', {
        type: 'warning'
      })
      
      await noteApi.delete(note.id)
      const idx = notes.value.findIndex(n => n.id === note.id)
      if (idx > -1) notes.value.splice(idx, 1)
      ElMessage.success('删除成功')
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error(error.response?.data?.message || '删除失败')
      }
    }
  }
}

const saveNote = async () => {
  if (!noteForm.value.title || !noteForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  
  loading.value = true
  try {
    if (editingNote.value) {
      // 更新笔记
      await noteApi.update(editingNote.value.id, {
        title: noteForm.value.title,
        subjectId: noteForm.value.subjectId,
        content: noteForm.value.content
      })
      
      // 重新加载数据
      await loadData()
      ElMessage.success('更新成功')
    } else {
      // 创建笔记
      await noteApi.add({
        questionId: noteForm.value.questionId,
        subjectId: noteForm.value.subjectId,
        title: noteForm.value.title,
        content: noteForm.value.content
      })
      
      // 重新加载数据
      await loadData()
      ElMessage.success('创建成功')
    }
    
    showNoteDialog.value = false
    editingNote.value = null
    noteForm.value = { title: '', subjectId: null, content: '', questionId: null }
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (date: Date | string) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.notes-page {
  max-width: 1400px;
  margin: 0 auto;
}

.action-row {
  margin-bottom: $spacing-lg;
}

.filter-card {
  margin-bottom: $spacing-lg;
}

.note-card {
  margin-bottom: $spacing-md;
  cursor: pointer;
  transition: all $transition-fast;
  height: 220px;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $box-shadow-lg;
    border-color: $primary-color;
  }

  .note-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-sm;

    .note-title {
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .note-content {
    flex: 1;
    font-size: 14px;
    line-height: 1.8;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 4;
    -webkit-box-orient: vertical;
    margin-bottom: $spacing-sm;
  }

  .note-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-light;

    .note-time {
      font-size: 12px;
      color: $text-secondary;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
}
</style>
