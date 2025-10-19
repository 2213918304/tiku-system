import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  // 管理后台路由
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'subjects',
        name: 'AdminSubjects',
        component: () => import('@/views/admin/Subjects.vue'),
        meta: { title: '学科管理' }
      },
      {
        path: 'questions',
        name: 'AdminQuestions',
        component: () => import('@/views/admin/Questions.vue'),
        meta: { title: '题目管理' }
      },
      {
        path: 'import',
        name: 'AdminImport',
        component: () => import('@/views/admin/Import.vue'),
        meta: { title: '数据导入' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'chapters',
        name: 'AdminChapters',
        component: () => import('@/views/admin/Chapters.vue'),
        meta: { title: '章节管理' }
      },
      {
        path: 'answer-records',
        name: 'AdminAnswerRecords',
        component: () => import('@/views/admin/AnswerRecords.vue'),
        meta: { title: '答题记录' }
      },
      {
        path: 'ai-grading',
        name: 'AdminAIGrading',
        component: () => import('@/views/admin/AIGradingReview.vue'),
        meta: { title: 'AI判题审核' }
      }
    ]
  },
  // 学生前台路由
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/login',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', requiresAuth: true }
      },
      {
        path: 'practice',
        name: 'Practice',
        component: () => import('@/views/practice/Index.vue'),
        meta: { title: '开始刷题', icon: 'EditPen', requiresAuth: true }
      },
      {
        path: 'practice/:sessionId',
        name: 'PracticeSession',
        component: () => import('@/views/practice/Session.vue'),
        meta: { title: '答题中', hidden: true, requiresAuth: true }
      },
      {
        path: 'subjects',
        name: 'Subjects',
        component: () => import('@/views/subjects/Index.vue'),
        meta: { title: '题库', icon: 'Reading', requiresAuth: true }
      },
      {
        path: 'my-collection',
        name: 'MyCollection',
        component: () => import('@/views/collection/Index.vue'),
        meta: { title: '我的收藏', icon: 'Star', requiresAuth: true }
      },
      {
        path: 'my-notes',
        name: 'MyNotes',
        component: () => import('@/views/notes/Index.vue'),
        meta: { title: '我的笔记', icon: 'Notebook', requiresAuth: true }
      },
      {
        path: 'wrong-questions',
        name: 'WrongQuestions',
        component: () => import('@/views/wrong/Index.vue'),
        meta: { title: '错题本', icon: 'WarningFilled', requiresAuth: true }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Index.vue'),
        meta: { title: '学习统计', icon: 'DataAnalysis', requiresAuth: true }
      },
      {
        path: 'ranking',
        name: 'Ranking',
        component: () => import('@/views/ranking/Index.vue'),
        meta: { title: '排行榜', icon: 'TrophyBase', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true, requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 题库系统` : '题库系统'

  // 检查路由是否需要登录（包括父路由）
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // 检查是否是访客页面（登录后不能访问）
  const isGuestPage = to.matched.some(record => record.meta.guest)

  // 需要登录的页面
  if (requiresAuth) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    
    // 检查是否需要管理员权限
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
    if (requiresAdmin && !userStore.isAdmin && !userStore.isTeacher) {
      ElMessage.error('无权限访问')
      next('/dashboard')
      return
    }
  }
  
  // 登录后不能访问的页面（如登录、注册）
  if (isGuestPage && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router

