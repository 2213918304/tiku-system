<template>
  <div class="page-container admin-ai-grading">
    <div class="page-header">
      <h2>AI判题审核</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-loading="statsLoading">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">总判题数</div>
          <div class="stat-value">{{ stats.totalCount }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">待审核</div>
          <div class="stat-value warning">{{ stats.pendingCount }}</div>
          <div class="stat-sub">低置信度 &lt;70%</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">高置信度</div>
          <div class="stat-value success">{{ stats.highConfidenceCount }}</div>
          <div class="stat-sub">置信度 ≥90%</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-label">平均置信度</div>
          <div class="stat-value primary">{{ (stats.avgConfidence * 100).toFixed(1) }}%</div>
        </div>
      </el-col>
    </el-row>

    <el-card>
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-select 
          v-model="filterStatus" 
          placeholder="筛选状态" 
          style="width: 200px"
          @change="loadRecords"
        >
          <el-option label="全部" value="" />
          <el-option label="待审核（低置信度）" value="pending" />
        </el-select>

        <el-button type="primary" @click="loadRecords">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- AI判题记录表格 -->
      <el-table
        :data="records"
        v-loading="loading"
        stripe
        style="margin-top: 16px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="用户" prop="userId" width="80" />
        <el-table-column label="题目" min-width="200">
          <template #default="{ row }">
            <div class="question-preview">
              {{ row.questionContent?.substring(0, 50) }}...
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户答案" min-width="150">
          <template #default="{ row }">
            <div class="answer-preview">
              {{ row.userAnswer?.substring(0, 40) }}...
            </div>
          </template>
        </el-table-column>
        <el-table-column label="AI评分" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.score }} 分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="120" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.confidence * 100" 
              :color="getConfidenceColor(row.confidence)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.confidence >= 0.7 ? 'success' : 'warning'" size="small">
              {{ row.confidence >= 0.7 ? '正常' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="判题时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.gradedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button link type="warning" size="small" @click="reviewRecord(row)">
              <el-icon><Edit /></el-icon>
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedRecords.length > 0" class="batch-actions">
        <span>已选择 {{ selectedRecords.length }} 条记录</span>
        <el-button type="success" size="small" @click="batchApprove">
          批量通过
        </el-button>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRecords"
        @current-change="loadRecords"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="AI判题详情" width="800px">
      <div v-if="currentRecord" class="grading-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="题目内容">
            <div class="content-box">{{ currentRecord.questionContent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="用户答案">
            <div class="content-box">{{ currentRecord.userAnswer }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="AI评分">
            <el-tag size="large">{{ currentRecord.score }} 分</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">
            <el-progress 
              :percentage="currentRecord.confidence * 100" 
              :color="getConfidenceColor(currentRecord.confidence)"
              :stroke-width="10"
              style="width: 300px"
            />
            <span style="margin-left: 12px">{{ (currentRecord.confidence * 100).toFixed(1) }}%</span>
          </el-descriptions-item>
          <el-descriptions-item label="AI评语">
            <div class="content-box">{{ currentRecord.feedback }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="判题时间">
            {{ formatDate(currentRecord.gradedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewVisible" title="审核判题结果" width="600px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="原AI评分">
          <el-tag>{{ currentRecord?.score }} 分</el-tag>
        </el-form-item>
        <el-form-item label="调整分数">
          <el-input-number 
            v-model="reviewForm.score" 
            :min="0" 
            :max="100"
            style="width: 200px"
          />
          <span style="margin-left: 12px; color: #909399">（可调整分数）</span>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input 
            v-model="reviewForm.comment" 
            type="textarea" 
            :rows="4"
            placeholder="请输入审核备注（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewing">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, View, Edit } from '@element-plus/icons-vue'
import request from '@/api/request'
import type { AIGradingRecord } from '@/types'
import dayjs from 'dayjs'

const loading = ref(false)
const statsLoading = ref(false)
const detailVisible = ref(false)
const reviewVisible = ref(false)
const reviewing = ref(false)

const currentRecord = ref<AIGradingRecord | null>(null)
const records = ref<AIGradingRecord[]>([])
const selectedRecords = ref<AIGradingRecord[]>([])

const filterStatus = ref('')

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const stats = reactive({
  totalCount: 0,
  pendingCount: 0,
  highConfidenceCount: 0,
  avgConfidence: 0
})

const reviewForm = reactive({
  score: 0,
  comment: ''
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getConfidenceColor = (confidence: number) => {
  if (confidence >= 0.9) return '#00b42a'
  if (confidence >= 0.7) return '#165dff'
  return '#ff7d00'
}

const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/admin/ai-grading/statistics')
    Object.assign(stats, res.data)
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (filterStatus.value) {
      params.status = filterStatus.value
    }

    const res = await request.get('/admin/ai-grading', { params })
    records.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载AI判题记录失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: AIGradingRecord[]) => {
  selectedRecords.value = selection
}

const viewDetail = (record: AIGradingRecord) => {
  currentRecord.value = record
  detailVisible.value = true
}

const reviewRecord = (record: AIGradingRecord) => {
  currentRecord.value = record
  reviewForm.score = record.score
  reviewForm.comment = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!currentRecord.value) return

  reviewing.value = true
  try {
    await request.put(`/admin/ai-grading/${currentRecord.value.id}/review`, {
      score: reviewForm.score,
      comment: reviewForm.comment
    })
    ElMessage.success('审核成功')
    reviewVisible.value = false
    await loadRecords()
    await loadStatistics()
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    reviewing.value = false
  }
}

const batchApprove = async () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定批量通过选中的 ${selectedRecords.value.length} 条记录吗？`,
      '批量审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    const ids = selectedRecords.value.map(r => r.id)
    await request.post('/admin/ai-grading/batch-approve', ids)
    ElMessage.success('批量审核成功')
    await loadRecords()
    await loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量审核失败')
    }
  }
}

onMounted(() => {
  loadStatistics()
  loadRecords()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.admin-ai-grading {
  .page-header {
    margin-bottom: $spacing-lg;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .stats-row {
    margin-bottom: $spacing-lg;

    .stat-card {
      background: $bg-white;
      border-radius: $border-radius-lg;
      padding: $spacing-lg;
      border: 1px solid $border-color;
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: $text-primary;

        &.primary {
          color: $primary-color;
        }

        &.success {
          color: $success-color;
        }

        &.warning {
          color: $warning-color;
        }
      }

      .stat-sub {
        font-size: 12px;
        color: $text-secondary;
        margin-top: 4px;
      }
    }
  }

  .filter-bar {
    display: flex;
    gap: $spacing-md;
    align-items: center;
  }

  .question-preview,
  .answer-preview {
    font-size: 13px;
    line-height: 1.5;
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    background: $primary-lightest;
    border-radius: $border-radius-md;
    margin-top: $spacing-md;
  }

  .grading-detail {
    .content-box {
      padding: $spacing-md;
      background: $bg-gray;
      border-radius: $border-radius-md;
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }
}
</style>

