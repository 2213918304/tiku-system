<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="64" color="#165DFF"><Reading /></el-icon>
        </div>
        <h1 class="brand-title">题库学习系统</h1>
        <p class="brand-subtitle">智能刷题 · 高效学习 · 轻松备考</p>
        
        <div class="brand-features">
          <div class="feature-item" v-for="(feature, index) in features" :key="index">
            <el-icon :size="20" color="#165DFF"><Check /></el-icon>
            <span>{{ feature }}</span>
          </div>
        </div>

        <div class="brand-stats">
          <div class="stat-item">
            <div class="stat-number">10,000+</div>
            <div class="stat-label">题目总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">5,000+</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">95%</div>
            <div class="stat-label">通过率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-wrapper">
      <div class="login-form-container">
        <div class="form-header">
          <h2>登录账号</h2>
          <p>欢迎回来，请登录你的账号</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <el-link type="primary" :underline="false" @click="goToRegister">
              立即注册
            </el-link>
          </div>
        </el-form>

        <!-- 底部信息 -->
        <div class="form-info">
          <el-icon><InfoFilled /></el-icon>
          <span>登录即表示同意《用户协议》和《隐私政策》</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, Reading, Check, UserFilled, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const features = [
  '9种刷题模式，满足不同学习需求',
  'AI智能判题，实时反馈学习效果',
  '完整学习统计，掌握学习进度',
  '错题本管理，针对性提升'
]

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      
      // 根据角色跳转到不同页面
      const redirect = route.query.redirect as string
      if (redirect) {
        router.push(redirect)
      } else {
        // 管理员和教师跳转到后台，学生跳转到前台
        if (userStore.isAdmin || userStore.isTeacher) {
          router.push('/admin/dashboard')
        } else {
          router.push('/dashboard')
        }
      }
    } catch (error: any) {
      // 错误消息已经在axios拦截器中显示了，这里不需要再显示
      console.error('登录失败：', error)
    } finally {
      loading.value = false
    }
  })
}

// 快速登录
const quickLogin = (username: string, password: string) => {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}

// 跳转注册
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.login-container {
  display: flex;
  height: 100vh;
  background: $bg-white;
}

// 左侧品牌区
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #E8F3FF 0%, #F0F7FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  // 装饰性背景图案
  &::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(22, 93, 255, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    top: -200px;
    left: -200px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(22, 93, 255, 0.06) 0%, transparent 70%);
    border-radius: 50%;
    bottom: -150px;
    right: -150px;
  }

  .brand-content {
    max-width: 520px;
    position: relative;
    z-index: 1;

    .brand-logo {
      width: 96px;
      height: 96px;
      background: $bg-white;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $box-shadow-lg;
      margin-bottom: 32px;
    }

    .brand-title {
      font-size: 36px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
    }

    .brand-subtitle {
      font-size: 16px;
      color: $text-secondary;
      margin-bottom: 48px;
    }

    .brand-features {
      margin-bottom: 48px;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 15px;
        color: $text-regular;
        margin-bottom: 16px;

        .el-icon {
          flex-shrink: 0;
        }
      }
    }

    .brand-stats {
      display: flex;
      gap: 48px;

      .stat-item {
        .stat-number {
          font-size: 32px;
          font-weight: 600;
          color: $primary-color;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          color: $text-secondary;
        }
      }
    }
  }
}

// 右侧表单区
.login-form-wrapper {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: $bg-white;
}

.login-form-container {
  width: 100%;
  max-width: 400px;

  .form-header {
    margin-bottom: 40px;

    h2 {
      font-size: 28px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: $text-secondary;
    }
  }

  .login-form {
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }

    :deep(.el-input__wrapper) {
      padding: 12px 16px;
      box-shadow: none;
      border: 1px solid $border-color;
      transition: all $transition-fast;

      &:hover {
        border-color: $primary-light;
      }

      &.is-focus {
        border-color: $primary-color;
        box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
      }
    }

    .form-options {
      :deep(.el-form-item__content) {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }

    .login-button {
      width: 100%;
      height: 44px;
      font-size: 16px;
      font-weight: 500;
      border-radius: $border-radius-md;
    }

    .form-footer {
      text-align: center;
      color: $text-secondary;
      font-size: 14px;

      .footer-text {
        margin-right: 8px;
      }
    }
  }

  .divider-text {
    font-size: 12px;
    color: $text-placeholder;
  }

  .quick-login {
    display: flex;
    gap: 12px;
    margin-top: 20px;

    .quick-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 10px 20px;
      border: 1px solid $border-color;
      color: $text-regular;
      background: $bg-white;

      &:hover {
        color: $primary-color;
        border-color: $primary-color;
        background: $primary-lightest;
      }
    }
  }

  .form-info {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    margin-top: 24px;
    font-size: 12px;
    color: $text-placeholder;

    .el-icon {
      font-size: 14px;
    }
  }
}

// 响应式
@media (max-width: 1024px) {
  .login-brand {
    display: none;
  }

  .login-form-wrapper {
    width: 100%;
  }
}
</style>
