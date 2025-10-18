<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="admin-sidebar">
      <div class="sidebar-header">
        <transition name="fade">
          <div v-if="!appStore.sidebarCollapsed" class="logo-full">
            <el-icon :size="32" color="#165DFF"><Setting /></el-icon>
            <span class="logo-text">管理后台</span>
          </div>
          <div v-else class="logo-mini">
            <el-icon :size="28" color="#165DFF"><Setting /></el-icon>
          </div>
        </transition>
      </div>

      <el-scrollbar class="sidebar-scrollbar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="appStore.sidebarCollapsed"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>数据统计</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/subjects">
            <el-icon><Reading /></el-icon>
            <template #title>学科管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/questions">
            <el-icon><Edit /></el-icon>
            <template #title>题目管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/chapters">
            <el-icon><FolderOpened /></el-icon>
            <template #title>章节管理</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/answer-records">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>答题记录</template>
          </el-menu-item>

          <el-menu-item index="/admin/ai-grading">
            <el-icon><MagicStick /></el-icon>
            <template #title>AI判题审核</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/import">
            <el-icon><Upload /></el-icon>
            <template #title>数据导入</template>
          </el-menu-item>
          
          <el-menu-item index="/admin/settings">
            <el-icon><Tools /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="admin-container">
      <!-- 顶部导航 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-button
            text
            :icon="appStore.sidebarCollapsed ? Expand : Fold"
            @click="appStore.toggleSidebar"
          />
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 切换到前台 -->
          <el-button text @click="router.push('/dashboard')">
            <el-icon><Switch /></el-icon>
            切换到前台
          </el-button>

          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :style="{ background: '#165DFF' }">
                {{ userStore.userInfo?.realName?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ userStore.userInfo?.realName }}</div>
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
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="admin-content">
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
  Setting,
  DataAnalysis,
  User,
  Reading,
  Edit,
  Upload,
  Tools,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton,
  Switch,
  FolderOpened,
  DocumentChecked,
  MagicStick
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarWidth = computed(() => {
  return appStore.sidebarCollapsed ? '64px' : '240px'
})

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route.meta.title as string)

const getRoleName = (role?: string) => {
  const roleMap: Record<string, string> = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return role ? roleMap[role] || '用户' : '用户'
}

onMounted(() => {
  // 检查权限
  if (!userStore.isAdmin && !userStore.isTeacher) {
    ElMessage.error('无权限访问管理后台')
    router.push('/dashboard')
  }
  
  // 移动端自动收起侧边栏
  if (window.innerWidth < 768) {
    appStore.sidebarCollapsed = true
  }
})

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
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

.admin-layout {
  height: 100vh;
  background: $bg-gray;
}

.admin-sidebar {
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
  }

  .sidebar-scrollbar {
    height: calc(100vh - #{$header-height});
  }

  .sidebar-menu {
    border: none;
  }
}

.admin-container {
  .admin-header {
    background: $bg-white;
    border-bottom: 1px solid $border-color;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 $spacing-lg;
    box-shadow: $box-shadow-sm;

    .header-left {
      display: flex;
      align-items: center;
      gap: $spacing-md;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        padding: 8px 12px;
        border-radius: $border-radius-md;
        transition: background $transition-fast;

        &:hover {
          background: $bg-gray;
        }

        .user-detail {
          .user-name {
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
          }

          .user-role {
            font-size: 12px;
            color: $text-secondary;
          }
        }
      }
    }
  }

  .admin-content {
    background: $bg-gray;
    overflow-y: auto;
  }
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


