<template>
  <div class="page-container admin-chapters">
    <el-card class="header-card">
      <div class="page-header">
        <div class="header-left">
          <h2>章节管理</h2>
          <p class="header-desc">组织和管理学科章节结构</p>
        </div>
        <el-button type="primary" size="large" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加章节
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card">
      <!-- 学科选择 -->
      <div class="filter-bar">
        <div class="filter-item">
          <label class="filter-label">筛选学科：</label>
          <el-select 
            v-model="selectedSubjectId" 
            placeholder="全部学科（可筛选）" 
            size="large"
            clearable
            style="width: 320px"
            @change="loadChapters"
          >
            <el-option 
              v-for="subject in subjectList" 
              :key="subject.id" 
              :label="subject.name" 
              :value="subject.id"
            >
              <span style="float: left">{{ subject.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ subject.questionCount || 0 }} 题
              </span>
            </el-option>
          </el-select>
        </div>
        <el-button @click="loadChapters" :loading="loading" size="large">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 章节树 -->
      <el-tree
        v-if="chapters.length > 0"
        :data="chapters"
        node-key="id"
        default-expand-all
        :props="{ label: 'name', children: 'children' }"
        class="chapter-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="{ 'is-subject-node': data.level === 0 }">
            <div class="node-info">
              <span class="node-label">{{ data.name }}</span>
              <el-tag size="small" style="margin-left: 8px">
                {{ data.questionCount || 0 }} 题
              </el-tag>
              <el-tag v-if="data.level === 0" type="primary" size="small" style="margin-left: 4px">
                学科
              </el-tag>
              <el-tag v-else-if="!data.parentId || data.parentId === 0" type="success" size="small" style="margin-left: 4px">
                父章节
              </el-tag>
            </div>
            <div class="node-actions" v-if="data.level !== 0">
              <el-button link type="primary" size="small" @click="showAddDialog(data)">
                <el-icon><Plus /></el-icon>
                添加子章节
              </el-button>
              <el-button link type="primary" size="small" @click="showEditDialog(data)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="deleteChapter(data)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
            <div class="node-actions" v-else>
              <el-button link type="primary" size="small" @click="showAddDialog(data)">
                <el-icon><Plus /></el-icon>
                添加章节
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>

      <el-empty 
        v-else-if="!loading"
        description="暂无章节数据"
        :image-size="160"
      >
        <template #description>
          <p style="margin: 16px 0; color: var(--el-text-color-secondary); font-size: 14px;">
            当前还没有创建任何章节，点击上方"添加章节"按钮开始创建
          </p>
        </template>
      </el-empty>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '添加章节' : '编辑章节'"
      width="500px"
    >
      <el-form :model="chapterForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学科" prop="subjectId">
          <el-select 
            v-model="chapterForm.subjectId" 
            placeholder="选择学科" 
            style="width: 100%"
            :disabled="dialogMode === 'edit'"
          >
            <el-option 
              v-for="subject in subjectList" 
              :key="subject.id" 
              :label="subject.name" 
              :value="subject.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="父章节" prop="parentId">
          <el-select 
            v-model="chapterForm.parentId" 
            placeholder="无（根章节）" 
            clearable
            style="width: 100%"
          >
            <el-option 
              v-for="chapter in parentChapterOptions" 
              :key="chapter.id" 
              :label="chapter.name" 
              :value="chapter.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="章节名称" prop="name">
          <el-input v-model="chapterForm.name" placeholder="请输入章节名称" />
        </el-form-item>

        <el-form-item label="章节序号" prop="sortOrder">
          <el-input-number 
            v-model="chapterForm.sortOrder" 
            :min="0" 
            :max="999"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="章节描述">
          <el-input 
            v-model="chapterForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入章节描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChapter" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import { subjectApi, chapterApi } from '@/api'
import type { Subject, Chapter } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

const selectedSubjectId = ref<number>()
const subjectList = ref<Subject[]>([])
const chapters = ref<Chapter[]>([])
const editingChapter = ref<Chapter | null>(null)

const chapterForm = reactive({
  subjectId: undefined as number | undefined,
  parentId: undefined as number | undefined,
  name: '',
  sortOrder: 0,
  description: ''
})

const rules: FormRules = {
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  name: [{ required: true, message: '请输入章节名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入章节序号', trigger: 'blur' }]
}

// 可选的父章节列表（只显示当前学科的一级章节）
const parentChapterOptions = computed(() => {
  if (!chapterForm.subjectId) return []
  
  // 如果选择了学科，只显示该学科的章节
  if (selectedSubjectId.value) {
    return chapters.value.filter(c => (!c.parentId || c.parentId === 0) && c.subjectId === chapterForm.subjectId)
  }
  
  // 如果显示所有学科，需要找到对应学科下的章节
  const subjectNode = chapters.value.find(c => c.id === chapterForm.subjectId * 10000)
  if (subjectNode && subjectNode.children) {
    return subjectNode.children.filter(c => !c.parentId || c.parentId === 0)
  }
  
  return []
})

const loadSubjects = async () => {
  try {
    const res = await subjectApi.list()
    subjectList.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科列表失败')
  }
}

const loadChapters = async () => {
  loading.value = true
  try {
    if (!selectedSubjectId.value) {
      // 未选择学科，显示所有学科的章节
      const allChapters: Chapter[] = []
      for (const subject of subjectList.value) {
        const res = await chapterApi.getTree(subject.id)
        if (res.data && res.data.length > 0) {
          // 为每个学科添加一个虚拟的根节点
          allChapters.push({
            id: subject.id * 10000, // 使用一个不会冲突的ID
            name: subject.name,
            subjectId: subject.id,
            parentId: 0,
            level: 0,
            sortOrder: subject.sortOrder || 0,
            questionCount: subject.questionCount || 0,
            children: res.data
          } as Chapter)
        }
      }
      chapters.value = allChapters
    } else {
      // 选择了学科，只显示该学科的章节
      const res = await chapterApi.getTree(selectedSubjectId.value)
      chapters.value = res.data || []
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载章节列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = (parent?: Chapter) => {
  // 如果是从顶部按钮添加章节，需要先选择学科
  if (!parent && !selectedSubjectId.value) {
    ElMessage.warning('请先从下拉框选择要添加章节的学科，或者从章节树中点击"添加子章节"')
    return
  }
  
  dialogMode.value = 'add'
  chapterForm.subjectId = parent ? parent.subjectId : selectedSubjectId.value
  chapterForm.parentId = parent?.id
  chapterForm.name = ''
  chapterForm.sortOrder = 0
  chapterForm.description = ''
  editingChapter.value = null
  dialogVisible.value = true
}

const showEditDialog = (chapter: Chapter) => {
  dialogMode.value = 'edit'
  chapterForm.subjectId = chapter.subjectId
  // 后端 0 表示顶级章节，前端表单使用 undefined 表示未选择父章节
  chapterForm.parentId = (chapter.parentId && chapter.parentId !== 0) ? chapter.parentId : undefined
  chapterForm.name = chapter.name
  chapterForm.sortOrder = chapter.sortOrder || 0
  chapterForm.description = chapter.description || ''
  editingChapter.value = chapter
  dialogVisible.value = true
}

const submitChapter = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    submitting.value = true
    const data = {
      subjectId: chapterForm.subjectId!,
      parentId: chapterForm.parentId || 0,  // 后端使用 0 表示顶级章节
      name: chapterForm.name,
      sortOrder: chapterForm.sortOrder,
      description: chapterForm.description || null
    }

    if (dialogMode.value === 'add') {
      await chapterApi.create(data)
      ElMessage.success('添加成功')
    } else {
      await chapterApi.update(editingChapter.value!.id, data)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
    
    // 确保选择的学科ID与表单中的学科ID一致
    selectedSubjectId.value = chapterForm.subjectId
    await loadChapters()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const deleteChapter = async (chapter: Chapter) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除章节"${chapter.name}"吗？${chapter.children?.length ? '子章节也会被删除！' : ''}`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await chapterApi.delete(chapter.id)
    ElMessage.success('删除成功')
    
    // 确保使用正确的学科ID刷新
    selectedSubjectId.value = chapter.subjectId
    await loadChapters()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await loadSubjects()
  // 加载完学科后自动加载所有章节
  await loadChapters()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-chapters {
  padding: $spacing-lg;

  .header-card {
    margin-bottom: $spacing-lg;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: $text-primary;
      }

      .header-desc {
        margin: 0;
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }

  .content-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    min-height: 500px;
  }

  .filter-bar {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    margin-bottom: $spacing-xl;
    padding-bottom: $spacing-lg;
    border-bottom: 1px solid #f0f0f0;

    .filter-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      flex: 1;

      .filter-label {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
        white-space: nowrap;
      }
    }
  }

  .chapter-tree {
    margin-top: $spacing-lg;
    background: #fafafa;
    padding: $spacing-lg;
    border-radius: 8px;

    :deep(.el-tree-node__content) {
      height: 48px;
      margin-bottom: 8px;
      background: white;
      border-radius: 8px;
      padding: 0 $spacing-md;
      transition: all 0.3s;

      &:hover {
        background: #f0f7ff;
        transform: translateX(4px);
      }
    }

    :deep(.el-tree-node__children) {
      .el-tree-node__content {
        background: #f8f8f8;
      }
    }

    // 学科节点特殊样式
    :deep(.el-tree-node) {
      > .el-tree-node__content {
        .is-subject-node {
          width: 100%;
        }
      }

      &:has(.is-subject-node) > .el-tree-node__content {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        font-weight: 600;
        margin-bottom: 12px;

        &:hover {
          background: linear-gradient(135deg, #5568d3 0%, #63408a 100%);
          transform: translateX(0);
        }

        .el-tag {
          background: rgba(255, 255, 255, 0.2);
          border: none;
          color: white;
        }

        .node-label {
          color: white !important;
        }
      }
    }

    .tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-right: $spacing-md;

      .node-info {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .node-label {
          font-size: 15px;
          font-weight: 500;
          color: $text-primary;
        }
      }

      .node-actions {
        display: flex;
        gap: $spacing-sm;
        opacity: 0;
        transition: opacity 0.3s;

        .el-button {
          border-radius: 6px;
        }
      }

      &:hover .node-actions {
        opacity: 1;
      }

      &.is-subject-node {
        .node-label {
          font-size: 16px;
          font-weight: 600;
          color: $primary-color;
        }

        .node-actions {
          opacity: 1; // 学科节点始终显示操作按钮
        }
      }
    }
  }

  :deep(.el-empty) {
    padding: 48px 0;
  }
}
</style>

