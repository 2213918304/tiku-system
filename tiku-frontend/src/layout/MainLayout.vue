<template>
  <el-container class="main-layout">
    <!-- 侧边栏 - 仅登录后显示 -->
    <el-aside v-if="userStore.isLoggedIn" :width="sidebarWidth" class="main-sidebar">
      <div class="sidebar-header">
        <transition name="fade">
          <div v-if="!appStore.sidebarCollapsed" class="logo-full">
            <el-icon :size="32" color="#165DFF"><Reading /></el-icon>
            <span class="logo-text">题库系统</span>
          </div>
          <div v-else class="logo-mini">
            <el-icon :size="28" color="#165DFF"><Reading /></el-icon>
          </div>
        </transition>
      </div>

      <el-scrollbar class="sidebar-scrollbar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="appStore.sidebarCollapsed"
          :unique-opened="false"
          router
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <el-menu-item
              v-if="!route.meta?.hidden"
              :index="route.path"
            >
              <el-icon v-if="route.meta?.iconComponent">
                <component :is="route.meta.iconComponent" />
              </el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="main-header">
        <div class="header-left">
          <el-button
            v-if="userStore.isLoggedIn"
            text
            :icon="appStore.sidebarCollapsed ? Expand : Fold"
            @click="appStore.toggleSidebar"
          />
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 未登录状态 -->
          <template v-if="!userStore.isLoggedIn">
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>

          <!-- 已登录状态 -->
          <template v-else>
            <!-- 切换到后台（管理员和教师） -->
            <el-button v-if="userStore.isAdmin || userStore.isTeacher" text @click="router.push('/admin/dashboard')">
              <el-icon><Setting /></el-icon>
              管理后台
            </el-button>

            <!-- 消息通知 -->
            <el-badge :value="3" class="header-badge">
              <el-button text :icon="Bell" />
            </el-badge>

            <!-- 用户信息 -->
            <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :style="{ background: '#165DFF' }">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ userStore.userInfo?.realName || '用户' }}</div>
                <div class="user-role">{{ getRoleName(userStore.userInfo?.role) }}</div>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  <span>设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </template>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Reading,
  Fold,
  Expand,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Bell,
  HomeFilled,
  EditPen,
  Star,
  Notebook,
  WarningFilled,
  DataAnalysis,
  TrophyBase
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 侧边栏宽度
const sidebarWidth = computed(() => {
  return appStore.sidebarCollapsed ? '64px' : '240px'
})

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前路由名称
const currentRoute = computed(() => route.meta.title as string)

// 图标映射
const iconMap: Record<string, any> = {
  HomeFilled,
  EditPen,
  Reading,
  Star,
  Notebook,
  WarningFilled,
  DataAnalysis,
  TrophyBase,
  User
}

// 菜单路由
const menuRoutes = computed(() => {
  // 找到有 children 的主布局路由
  const mainRoute = router.options.routes.find(r => r.path === '/' && r.children && r.children.length > 0)
  const routes = mainRoute?.children || []
  
  console.log('menuRoutes - found routes:', routes.length)
  
  return routes.filter(r => !r.meta?.hidden).map(route => ({
    ...route,
    // 确保路径始终是绝对路径（以 / 开头）
    path: route.path.startsWith('/') ? route.path : `/${route.path}`,
    meta: {
      ...route.meta,
      iconComponent: route.meta?.icon ? iconMap[route.meta.icon as string] : null
    }
  }))
})

// 获取角色名称
const getRoleName = (role?: string) => {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return role ? roleMap[role] || '用户' : '用户'
}

// 初始化
onMounted(() => {
  // 移动端自动收起侧边栏
  if (window.innerWidth < 768) {
    appStore.sidebarCollapsed = true
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (window.innerWidth < 768) {
      appStore.sidebarCollapsed = true
    }
  })
})

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch {
        // 取消退出
      }
      break
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.main-layout {
  height: 100vh;
  background: $bg-gray;
}

// 侧边栏
.main-sidebar {
  background: $bg-white;
  border-right: 1px solid $border-color;
  transition: width $transition-base;
  overflow: hidden;

  .sidebar-header {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid $border-color;
    padding: 0 $spacing-lg;

    .logo-full {
      display: flex;
      align-items: center;
      gap: 12px;

      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
      }
    }

    .logo-mini {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .sidebar-scrollbar {
    height: calc(100vh - #{$header-height});
  }

  .sidebar-menu {
    border-right: none;
    
    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
      margin: 4px 12px;
      border-radius: $border-radius-md;
      color: $text-regular;

      &:hover {
        background-color: $bg-hover;
        color: $primary-color;
      }

      &.is-active {
        background-color: $primary-lightest;
        color: $primary-color;
        font-weight: 500;
      }
    }
  }
}

// 主容器
.main-container {
  display: flex;
  flex-direction: column;
}

// 顶部导航
.main-header {
  height: $header-height;
  background: $bg-white;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    :deep(.el-breadcrumb) {
      font-size: 14px;

      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          color: $text-secondary;
          font-weight: 400;

          &:hover {
            color: $primary-color;
          }
        }

        &:last-child .el-breadcrumb__inner {
          color: $text-primary;
          font-weight: 500;
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .header-badge {
      :deep(.el-badge__content) {
        background-color: $danger-color;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: $border-radius-md;
      transition: all $transition-fast;

      &:hover {
        background-color: $bg-hover;
      }

      .user-detail {
        .user-name {
          font-size: 14px;
          color: $text-primary;
          font-weight: 500;
          line-height: 1.4;
        }

        .user-role {
          font-size: 12px;
          color: $text-secondary;
          line-height: 1.4;
        }
      }

      .arrow-icon {
        color: $text-secondary;
        font-size: 12px;
      }
    }
  }
}

// 主内容区
.main-content {
  padding: $spacing-lg;
  background: $bg-gray;
  overflow-y: auto;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity $transition-base;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all $transition-base;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
