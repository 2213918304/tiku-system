<template>
  <div class="page-container profile-page">
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理个人信息和学习设置</p>
    </div>

    <el-row :gutter="16">
      <!-- 个人信息 -->
      <el-col :xs="24" :lg="8">
        <el-card class="profile-card" v-loading="loading">
          <div class="avatar-section">
            <el-avatar :size="100" :style="{ background: '#165DFF', fontSize: '40px' }">
              {{ userInfo.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <h2>{{ userInfo.realName }}</h2>
            <p class="username">@{{ userInfo.username }}</p>
            <p class="email" v-if="userInfo.email">{{ userInfo.email }}</p>
          </div>
          
          <el-divider />
          
          <div class="info-section">
            <div class="info-item">
              <span class="label">角色</span>
              <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : userInfo.role === 'TEACHER' ? 'warning' : 'success'">
                {{ getRoleName(userInfo.role) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span>{{ formatDate(userInfo.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">学习天数</span>
              <span class="value">{{ studyDays }} 天</span>
            </div>
          </div>
          
          <el-button type="primary" style="width: 100%; margin-top: 16px" @click="openEditDialog">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </el-card>

        <!-- 成就系统 -->
        <el-card class="achievement-card">
          <template #header>
            <span>我的成就</span>
          </template>
          <div class="achievements-grid">
            <div v-for="ach in achievements" :key="ach.id" :class="['achievement-badge', { unlocked: ach.unlocked }]">
              <div class="ach-icon">{{ ach.icon || '🏆' }}</div>
              <div class="ach-name">{{ ach.name }}</div>
            </div>
          </div>
          <el-empty v-if="achievements.length === 0" description="暂无成就" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 学习计划 -->
      <el-col :xs="24" :lg="16">
        <el-card class="plan-card">
          <template #header>
            <div class="card-header-content">
              <span>学习计划</span>
              <el-button type="primary" @click="showPlanDialog = true">
                <el-icon><Plus /></el-icon>
                新建计划
              </el-button>
            </div>
          </template>

          <div v-for="plan in plans" :key="plan.id" class="plan-item">
            <div class="plan-header">
              <h3>{{ plan.name || plan.description }}</h3>
              <el-tag :type="plan.status === 'ACTIVE' ? 'success' : 'info'">
                {{ plan.status === 'ACTIVE' ? '进行中' : '已完成' }}
              </el-tag>
            </div>
            <div class="plan-progress">
              <el-progress :percentage="plan.progress || 0" />
            </div>
            <div class="plan-footer">
              <span>{{ plan.completedDays || 0 }} / {{ plan.totalDays || 0 }} 天</span>
              <span>目标：{{ plan.targetCount || 0 }} 题</span>
            </div>
          </div>

          <el-empty v-if="plans.length === 0" description="暂无学习计划" />
        </el-card>

        <!-- 学习记录 -->
        <el-card class="record-card">
          <template #header>
            <span>最近学习</span>
          </template>
          <el-timeline>
            <el-timeline-item v-for="record in recentRecords" :key="record.id" :timestamp="formatTime(record.time)">
              完成了 <strong>{{ record.subjectName }}</strong> 的练习，答对 {{ record.correctCount }}/{{ record.totalCount }} 题
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile" :loading="loading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新建计划对话框 -->
    <el-dialog v-model="showPlanDialog" title="新建学习计划" width="500px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="planForm.description" placeholder="例如：马原冲刺计划" />
        </el-form-item>
        <el-form-item label="学科">
          <el-select v-model="planForm.subjectId" placeholder="请选择学科" style="width: 100%">
            <el-option label="马克思主义基本原理" :value="1" />
            <el-option label="毛泽东思想" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标题数">
          <el-input-number v-model="planForm.targetCount" :min="10" :max="10000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划天数">
          <el-input-number v-model="planForm.totalDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPlanDialog = false">取消</el-button>
        <el-button type="primary" @click="createPlan" :loading="loading">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Edit, Trophy, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { userApi, statisticsApi, type StudyPlan, type CreateStudyPlanRequest } from '@/api'

const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {
  realName: '用户',
  username: 'user',
  email: '',
  role: 'STUDENT',
  createdAt: new Date().toISOString()
})

const studyDays = ref(0)
const achievements = ref<any[]>([])
const plans = ref<StudyPlan[]>([])
const recentRecords = ref<any[]>([])
const loading = ref(false)

const showEditDialog = ref(false)
const showPlanDialog = ref(false)
const editForm = ref({ realName: '', email: '' })
const planForm = ref<CreateStudyPlanRequest>({
  subjectId: 1,
  targetCount: 100,
  totalDays: 7,
  description: ''
})

const getRoleName = (role: string) => {
  const map: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[role] || role
}

const formatDate = (date: string | Date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatTime = (date: string | Date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 加载用户数据
const loadUserData = async () => {
  loading.value = true
  try {
    // 获取用户统计信息
    const statsRes = await statisticsApi.getMyStatistics()
    studyDays.value = statsRes.data.totalCheckInDays || 0

    // 获取成就（使用统计数据生成）
    achievements.value = [
      { id: 1, name: '初学者', unlocked: true, icon: '🎓' },
      { id: 2, name: '勤奋者', unlocked: studyDays.value >= 7, icon: '💪' },
      { id: 3, name: '学霸', unlocked: statsRes.data.accuracy >= 90, icon: '👑' },
      { id: 4, name: '坚持者', unlocked: studyDays.value >= 30, icon: '🔥' },
      { id: 5, name: '百题斩', unlocked: statsRes.data.totalAnswered >= 100, icon: '⚔️' },
      { id: 6, name: '千题王', unlocked: statsRes.data.totalAnswered >= 1000, icon: '🏆' }
    ]

    // 获取学习计划
    const plansRes = await userApi.getStudyPlans()
    plans.value = plansRes.data || []

    // 获取最近学习记录（从统计接口获取）
    const subjectsRes = await statisticsApi.getSubjectStatistics()
    recentRecords.value = subjectsRes.data
      .filter((s: any) => s.answeredCount > 0)
      .slice(0, 5)
      .map((s: any) => ({
        id: s.subjectId,
        subjectName: s.subjectName,
        correctCount: s.correctCount,
        totalCount: s.answeredCount,
        time: new Date()
      }))
  } catch (error: any) {
    console.error('加载用户数据失败：', error)
    ElMessage.error('加载数据失败')
    
    // 失败时使用默认值
    studyDays.value = 0
    achievements.value = [
      { id: 1, name: '初学者', unlocked: true, icon: '🎓' },
      { id: 2, name: '勤奋者', unlocked: false, icon: '💪' },
      { id: 3, name: '学霸', unlocked: false, icon: '👑' },
      { id: 4, name: '坚持者', unlocked: false, icon: '🔥' },
      { id: 5, name: '百题斩', unlocked: false, icon: '⚔️' },
      { id: 6, name: '千题王', unlocked: false, icon: '🏆' }
    ]
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = () => {
  editForm.value = {
    realName: userInfo.value.realName,
    email: userInfo.value.email || ''
  }
  showEditDialog.value = true
}

// 保存个人信息
const saveProfile = async () => {
  try {
    await userApi.updateProfile(editForm.value)
    // 更新本地用户信息
    await userStore.getUserInfo()
    ElMessage.success('保存成功')
    showEditDialog.value = false
  } catch (error: any) {
    console.error('保存失败：', error)
  }
}

// 创建学习计划
const createPlan = async () => {
  if (!planForm.value.description) {
    ElMessage.warning('请输入计划名称')
    return
  }

  try {
    await userApi.createStudyPlan(planForm.value)
    ElMessage.success('创建成功')
    showPlanDialog.value = false
    // 重新加载计划列表
    loadUserData()
  } catch (error: any) {
    console.error('创建失败：', error)
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.profile-page {
  max-width: 1400px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: $spacing-lg;

  .avatar-section {
    text-align: center;
    padding: $spacing-lg 0;

    h2 {
      margin: $spacing-md 0 4px;
      font-size: 20px;
      font-weight: 600;
    }

    .username {
      color: $text-secondary;
      font-size: 14px;
      margin: 4px 0;
    }

    .email {
      color: $text-placeholder;
      font-size: 12px;
      margin: 4px 0;
    }
  }

  .info-section {
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: $spacing-sm 0;

      .label {
        color: $text-secondary;
      }

      .value {
        font-weight: 600;
        color: $primary-color;
      }
    }
  }
}

.achievement-card {
  .achievements-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: $spacing-md;
  }

  .achievement-badge {
    text-align: center;
    padding: $spacing-md;
    border-radius: $border-radius-md;
    background: $bg-gray;
    opacity: 0.4;
    transition: all $transition-fast;
    cursor: pointer;

    &.unlocked {
      opacity: 1;
      background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
      box-shadow: 0 4px 12px rgba(253, 203, 110, 0.3);

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 16px rgba(253, 203, 110, 0.4);
      }
    }

    .ach-icon {
      font-size: 32px;
      margin-bottom: 4px;
    }

    .ach-name {
      font-size: 12px;
      font-weight: 500;
      color: $text-primary;
    }
  }
}

.plan-card, .record-card {
  margin-bottom: $spacing-lg;

  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .plan-item {
    padding: $spacing-md;
    border: 1px solid $border-color;
    border-radius: $border-radius-md;
    margin-bottom: $spacing-md;

    .plan-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-sm;

      h3 {
        font-size: 16px;
      }
    }

    .plan-progress {
      margin-bottom: $spacing-sm;
    }

    .plan-footer {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      color: $text-secondary;
    }
  }
}
</style>
