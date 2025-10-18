import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 主题模式
  const isDark = ref(false)
  
  // 加载状态
  const loading = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  function setLoading(state: boolean) {
    loading.value = state
  }

  return {
    sidebarCollapsed,
    isDark,
    loading,
    toggleSidebar,
    toggleTheme,
    setLoading
  }
})



