<template>
  <div class="register-container">
    <!-- 左侧品牌区域 -->
    <div class="register-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="64" color="#165DFF"><Reading /></el-icon>
        </div>
        <h1 class="brand-title">加入我们</h1>
        <p class="brand-subtitle">开启你的学习之旅</p>
        
        <div class="brand-benefits">
          <div class="benefit-item" v-for="(benefit, index) in benefits" :key="index">
            <div class="benefit-icon">
              <el-icon :size="24" color="#165DFF"><component :is="benefit.icon" /></el-icon>
            </div>
            <div class="benefit-content">
              <h3>{{ benefit.title }}</h3>
              <p>{{ benefit.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="register-form-wrapper">
      <div class="register-form-container">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名（3-20个字符）"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="真实姓名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><Avatar /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱地址"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码（6-20个字符）"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              clearable
            >
              <template #prefix>
                <el-icon color="#86909C"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="agreeTerms">
              我已阅读并同意
              <el-link type="primary" :underline="false">《用户协议》</el-link>
              和
              <el-link type="primary" :underline="false">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              立即注册
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <el-link type="primary" :underline="false" @click="goToLogin">
              立即登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { 
  User, 
  Lock, 
  Reading, 
  Message, 
  Avatar,
  Edit,
  TrendCharts,
  Medal
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { RegisterRequest } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const agreeTerms = ref(false)

const benefits = [
  {
    icon: Edit,
    title: '海量题库',
    desc: '涵盖各学科精选题目，持续更新'
  },
  {
    icon: TrendCharts,
    title: '智能分析',
    desc: '实时统计学习数据，精准定位薄弱点'
  },
  {
    icon: Medal,
    title: '个性化学习',
    desc: 'AI推荐题目，制定专属学习计划'
  }
]

const registerForm = reactive<RegisterRequest & { confirmPassword: string }>({
  username: '',
  realName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!agreeTerms.value) {
    ElMessage.warning('请先阅读并同意用户协议和隐私政策')
    return
  }

  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const { confirmPassword, ...data } = registerForm
      await userStore.register(data)
      ElMessage.success('注册成功！请登录')
      router.push('/login')
    } catch (error: any) {
      // 错误消息已经在axios拦截器中显示了
      console.error('注册失败：', error)
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.register-container {
  display: flex;
  height: 100vh;
  background: $bg-white;
}

// 左侧品牌区
.register-brand {
  flex: 1;
  background: linear-gradient(135deg, #E8F3FF 0%, #F0F7FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

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

    .brand-benefits {
      .benefit-item {
        display: flex;
        gap: 20px;
        margin-bottom: 32px;

        .benefit-icon {
          width: 56px;
          height: 56px;
          background: $bg-white;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          box-shadow: $box-shadow-sm;
        }

        .benefit-content {
          h3 {
            font-size: 18px;
            font-weight: 600;
            color: $text-primary;
            margin-bottom: 8px;
          }

          p {
            font-size: 14px;
            color: $text-secondary;
            line-height: 1.6;
          }
        }
      }
    }
  }
}

// 右侧表单区
.register-form-wrapper {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: $bg-white;
}

.register-form-container {
  width: 100%;
  max-width: 400px;

  .form-header {
    margin-bottom: 32px;

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

  .register-form {
    :deep(.el-form-item) {
      margin-bottom: 20px;
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

    .register-button {
      width: 100%;
      height: 44px;
      font-size: 16px;
      font-weight: 500;
      border-radius: $border-radius-md;
      margin-top: 8px;
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
}

@media (max-width: 1024px) {
  .register-brand {
    display: none;
  }

  .register-form-wrapper {
    width: 100%;
  }
}
</style>
