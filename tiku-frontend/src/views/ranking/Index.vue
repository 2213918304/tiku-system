<template>
  <div class="page-container ranking-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <div class="page-header">
      <h1>
        <span class="trophy-icon">🏆</span>
        排行榜
        <span class="trophy-icon">🏆</span>
      </h1>
      <p>与 <span class="highlight">{{ totalUsers }}</span> 位学习者一起进步，追求卓越</p>
    </div>

    <!-- 我的排名卡片 -->
    <transition name="slide-down" appear>
      <el-card v-if="myRanking" class="my-ranking-card" shadow="never">
        <div class="my-ranking-content">
          <div class="rank-section">
            <div class="rank-badge-large" :class="`rank-${myRanking.rank}`">
              <span v-if="myRanking.rank === 1" class="medal">👑</span>
              <span v-else-if="myRanking.rank === 2" class="medal">🥈</span>
              <span v-else-if="myRanking.rank === 3" class="medal">🥉</span>
              <span v-else class="rank-num">{{ myRanking.rank }}</span>
            </div>
            <div class="rank-label">
              <span class="label-text">我的排名</span>
              <span class="label-desc">/ {{ totalUsers }} 人</span>
            </div>
          </div>
          
          <div class="stats-section">
            <div class="stat-box">
              <div class="stat-icon">📝</div>
              <div class="stat-info">
                <div class="stat-value">{{ myRanking.value }}</div>
                <div class="stat-label">答题数</div>
              </div>
            </div>
            
            <div class="stat-box" v-if="myRanking.accuracy !== undefined">
              <div class="stat-icon">🎯</div>
              <div class="stat-info">
                <div class="stat-value">{{ myRanking.accuracy }}%</div>
                <div class="stat-label">正确率</div>
              </div>
            </div>
            
            <div class="stat-box" v-if="myRanking.points !== undefined">
              <div class="stat-icon">⭐</div>
              <div class="stat-info">
                <div class="stat-value">{{ myRanking.points }}</div>
                <div class="stat-label">积分</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </transition>

    <!-- 榜单切换 -->
    <el-card class="ranking-tabs-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="ranking-tabs">
        <el-tab-pane name="answerCount">
          <template #label>
            <div class="tab-label">
              <span class="tab-icon">📊</span>
              <span class="tab-text">答题榜</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane name="accuracy">
          <template #label>
            <div class="tab-label">
              <span class="tab-icon">🎯</span>
              <span class="tab-text">正确率榜</span>
            </div>
          </template>
        </el-tab-pane>
        <el-tab-pane name="points">
          <template #label>
            <div class="tab-label">
              <span class="tab-icon">⭐</span>
              <span class="tab-text">积分榜</span>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 榜单说明 -->
      <div class="tab-description">
        <el-icon><InfoFilled /></el-icon>
        <span v-if="activeTab === 'answerCount'">根据答题总数排名，量变产生质变</span>
        <span v-else-if="activeTab === 'accuracy'">至少答题10道才计入排名，正确率越高排名越前</span>
        <span v-else>根据学习积分排名，每答对一题得10分</span>
      </div>

      <!-- 排行榜列表 -->
      <div v-loading="loading" class="ranking-list">
        <el-empty v-if="rankingData.length === 0" description="暂无排名数据" />
        
        <transition-group name="list" appear>
          <div
            v-for="(item, index) in displayedRankings"
            :key="item.userId"
            class="ranking-item"
            :class="{ 
              'is-current-user': item.isCurrentUser, 
              'top-one': item.rank === 1,
              'top-two': item.rank === 2,
              'top-three': item.rank === 3
            }"
            :style="{ transitionDelay: `${index * 30}ms` }"
          >
            <!-- 排名徽章 -->
            <div class="rank-badge" :class="`rank-${item.rank}`">
              <span v-if="item.rank === 1" class="medal-icon">👑</span>
              <span v-else-if="item.rank === 2" class="medal-icon">🥈</span>
              <span v-else-if="item.rank === 3" class="medal-icon">🥉</span>
              <span v-else class="rank-number">{{ item.rank }}</span>
            </div>

            <!-- 用户信息 -->
            <div class="user-info">
              <div class="user-avatar-wrapper">
                <el-avatar :size="50" class="user-avatar">
                  {{ item.username ? item.username[0].toUpperCase() : '?' }}
                </el-avatar>
                <div v-if="item.rank <= 3" class="avatar-glow"></div>
              </div>
              <div class="user-details">
                <div class="username">
                  {{ item.username }}
                  <el-tag v-if="item.isCurrentUser" type="success" size="small" effect="dark">
                    <el-icon><User /></el-icon> 我
                  </el-tag>
                </div>
                <div class="real-name">{{ item.realName || '未设置' }}</div>
              </div>
            </div>

            <!-- 统计数据 -->
            <div class="stats-display">
              <div v-if="activeTab === 'answerCount'" class="stat-main">
                <div class="stat-number">{{ item.value }}</div>
                <div class="stat-unit">题</div>
              </div>
              <div v-else-if="activeTab === 'accuracy'" class="stat-main">
                <div class="stat-number">{{ item.accuracy || 0 }}%</div>
                <div class="stat-unit">{{ item.value }} 题</div>
              </div>
              <div v-else-if="activeTab === 'points'" class="stat-main">
                <div class="stat-number">{{ item.points || 0 }}</div>
                <div class="stat-unit">分</div>
              </div>
            </div>
          </div>
        </transition-group>

        <!-- 加载更多按钮 -->
        <div v-if="canLoadMore" class="load-more-btn">
          <el-button @click="loadMore" :loading="loading" round>
            <el-icon><ArrowDown /></el-icon>
            加载更多
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, User, ArrowDown } from '@element-plus/icons-vue'
import { rankingApi } from '@/api/modules/ranking'

const loading = ref(false)
const activeTab = ref('answerCount')
const rankingData = ref<any[]>([])
const displayLimit = ref(20) // 初始显示20条

// 计算总用户数
const totalUsers = computed(() => rankingData.value.length)

// 计算我的排名
const myRanking = computed(() => {
  return rankingData.value.find(item => item.isCurrentUser)
})

// 显示的排名列表（分页显示）
const displayedRankings = computed(() => {
  return rankingData.value.slice(0, displayLimit.value)
})

// 是否可以加载更多
const canLoadMore = computed(() => {
  return displayLimit.value < rankingData.value.length
})

onMounted(() => {
  loadRanking()
})

const handleTabChange = () => {
  displayLimit.value = 20 // 切换榜单时重置显示数量
  loadRanking()
}

const loadRanking = async () => {
  loading.value = true
  try {
    let res
    switch (activeTab.value) {
      case 'answerCount':
        res = await rankingApi.getAnswerCountRanking(1000) // 获取所有用户
        break
      case 'accuracy':
        res = await rankingApi.getAccuracyRanking(1000)
        break
      case 'points':
        res = await rankingApi.getPointsRanking(1000)
        break
      default:
        res = await rankingApi.getAnswerCountRanking(1000)
    }
    rankingData.value = res.data || []
  } catch (error: any) {
    console.error('加载排行榜失败:', error)
    ElMessage.error(error.response?.data?.message || '加载排行榜失败')
    rankingData.value = []
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  displayLimit.value += 20
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.ranking-page {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  padding-bottom: 40px;
}

// 背景装饰
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  
  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
    animation: float 20s infinite ease-in-out;
    
    &.circle-1 {
      width: 300px;
      height: 300px;
      top: -150px;
      right: -100px;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 200px;
      height: 200px;
      bottom: 100px;
      left: -50px;
      animation-delay: 5s;
    }
    
    &.circle-3 {
      width: 150px;
      height: 150px;
      top: 50%;
      right: 10%;
      animation-delay: 10s;
    }
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -30px) rotate(120deg); }
  66% { transform: translate(-20px, 20px) rotate(240deg); }
}

.page-header {
  position: relative;
  z-index: 1;
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 42px;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 12px;
    
    .trophy-icon {
      font-size: 48px;
      display: inline-block;
      animation: bounce 2s infinite;
    }
  }
  
  p {
    font-size: 16px;
    color: $text-secondary;
    
    .highlight {
      font-weight: 700;
      color: $primary-color;
      font-size: 20px;
    }
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

// 动画
.slide-down-enter-active {
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 我的排名卡片
.my-ranking-card {
  position: relative;
  z-index: 1;
  margin-bottom: 24px;
  border-radius: 20px;
  overflow: hidden;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
  
  :deep(.el-card__body) {
    padding: 32px;
  }
  
  .my-ranking-content {
    display: flex;
    align-items: center;
    gap: 48px;
    color: white;
    
    .rank-section {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .rank-badge-large {
        width: 100px;
        height: 100px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);
        font-size: 48px;
        font-weight: 800;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          inset: -4px;
          border-radius: 50%;
          padding: 4px;
          background: linear-gradient(135deg, rgba(255, 255, 255, 0.5), rgba(255, 255, 255, 0.1));
          -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
          -webkit-mask-composite: xor;
          mask-composite: exclude;
        }
        
        .medal {
          font-size: 56px;
          animation: rotate 3s linear infinite;
        }
        
        .rank-num {
          color: white;
        }
      }
      
      .rank-label {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .label-text {
          font-size: 18px;
          font-weight: 600;
          opacity: 0.9;
        }
        
        .label-desc {
          font-size: 24px;
          font-weight: 300;
          opacity: 0.8;
        }
      }
    }
    
    .stats-section {
      flex: 1;
      display: flex;
      gap: 24px;
      justify-content: flex-end;
      
      .stat-box {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px 28px;
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(10px);
        border-radius: 16px;
        min-width: 150px;
        
        .stat-icon {
          font-size: 36px;
        }
        
        .stat-info {
          .stat-value {
            font-size: 32px;
            font-weight: 700;
            line-height: 1;
            margin-bottom: 4px;
          }
          
          .stat-label {
            font-size: 13px;
            opacity: 0.9;
          }
        }
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 排行榜卡片
.ranking-tabs-card {
  position: relative;
  z-index: 1;
  border-radius: 20px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  
  :deep(.el-card__body) {
    padding: 0;
  }
  
  .ranking-tabs {
    :deep(.el-tabs__header) {
      margin: 0;
      padding: 24px 24px 0;
      background: linear-gradient(to bottom, #fafbfc 0%, #ffffff 100%);
    }
    
    :deep(.el-tabs__nav-wrap) {
      &::after {
        display: none;
      }
    }
    
    :deep(.el-tabs__item) {
      font-size: 16px;
      font-weight: 600;
      color: $text-secondary;
      padding: 0 32px;
      height: 56px;
      line-height: 56px;
      
      &.is-active {
        color: $primary-color;
      }
      
      &:hover {
        color: $primary-color;
      }
    }
    
    :deep(.el-tabs__active-bar) {
      height: 3px;
      border-radius: 3px 3px 0 0;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    }
    
    .tab-label {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .tab-icon {
        font-size: 20px;
      }
      
      .tab-text {
        font-size: 16px;
      }
    }
  }
  
  .tab-description {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px 24px;
    background: linear-gradient(90deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
    color: $text-secondary;
    font-size: 14px;
    border-bottom: 1px solid $border-light;
  }
  
  .ranking-list {
    padding: 24px;
    min-height: 600px;
    
    .list-enter-active {
      transition: all 0.4s ease;
    }
    
    .list-enter-from {
      opacity: 0;
      transform: translateY(20px);
    }
    
    .ranking-item {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 20px 24px;
      margin-bottom: 12px;
      border-radius: 16px;
      background: white;
      border: 2px solid transparent;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: $bg-gray;
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        border-color: rgba(102, 126, 234, 0.3);
        
        &::before {
          opacity: 1;
        }
      }
      
      &.is-current-user {
        background: linear-gradient(135deg, rgba(22, 119, 255, 0.08), rgba(102, 126, 234, 0.08));
        border-color: $primary-color;
        box-shadow: 0 4px 16px rgba(22, 119, 255, 0.2);
      }
      
      &.top-one {
        background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 165, 0, 0.1));
        border-color: rgba(255, 215, 0, 0.5);
      }
      
      &.top-two {
        background: linear-gradient(135deg, rgba(192, 192, 192, 0.1), rgba(169, 169, 169, 0.1));
        border-color: rgba(192, 192, 192, 0.5);
      }
      
      &.top-three {
        background: linear-gradient(135deg, rgba(205, 127, 50, 0.1), rgba(139, 69, 19, 0.1));
        border-color: rgba(205, 127, 50, 0.5);
      }
      
      > * {
        position: relative;
        z-index: 1;
      }
      
      .rank-badge {
        min-width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-weight: 700;
        font-size: 20px;
        background: $bg-gray;
        flex-shrink: 0;
        
        &.rank-1, &.rank-2, &.rank-3 {
          .medal-icon {
            font-size: 36px;
            animation: pulse 2s infinite;
          }
        }
        
        &.rank-1 {
          background: linear-gradient(135deg, #FFD700, #FFA500);
          color: white;
          box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
        }
        
        &.rank-2 {
          background: linear-gradient(135deg, #E8E8E8, #B0B0B0);
          color: white;
          box-shadow: 0 4px 12px rgba(192, 192, 192, 0.4);
        }
        
        &.rank-3 {
          background: linear-gradient(135deg, #CD7F32, #8B4513);
          color: white;
          box-shadow: 0 4px 12px rgba(205, 127, 50, 0.4);
        }
        
        .rank-number {
          color: $text-secondary;
          font-weight: 600;
        }
      }
      
      .user-info {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 16px;
        
        .user-avatar-wrapper {
          position: relative;
          
          .user-avatar {
            background: linear-gradient(135deg, $primary-color, #764ba2);
            color: white;
            font-weight: 700;
            font-size: 20px;
            border: 3px solid white;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
          }
          
          .avatar-glow {
            position: absolute;
            inset: -4px;
            border-radius: 50%;
            background: linear-gradient(135deg, rgba(255, 215, 0, 0.5), rgba(255, 165, 0, 0.5));
            filter: blur(8px);
            z-index: -1;
            animation: glow 2s infinite;
          }
        }
        
        .user-details {
          .username {
            font-weight: 600;
            font-size: 17px;
            margin-bottom: 6px;
            display: flex;
            align-items: center;
            gap: 10px;
            color: $text-primary;
          }
          
          .real-name {
            font-size: 13px;
            color: $text-secondary;
          }
        }
      }
      
      .stats-display {
        text-align: right;
        
        .stat-main {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 4px;
          
          .stat-number {
            font-size: 28px;
            font-weight: 700;
            background: linear-gradient(135deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            line-height: 1;
          }
          
          .stat-unit {
            font-size: 13px;
            color: $text-secondary;
            font-weight: 500;
          }
        }
      }
    }
    
    .load-more-btn {
      text-align: center;
      padding: 24px 0;
      
      .el-button {
        padding: 12px 32px;
        font-size: 15px;
        font-weight: 600;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        color: white;
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes glow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

@media (max-width: 768px) {
  .my-ranking-content {
    flex-direction: column;
    gap: 24px;
    
    .rank-section {
      width: 100%;
      justify-content: center;
    }
    
    .stats-section {
      width: 100%;
      flex-wrap: wrap;
      justify-content: center;
      
      .stat-box {
        flex: 1;
        min-width: 140px;
      }
    }
  }
  
  .ranking-item {
    padding: 16px !important;
    
    .rank-badge {
      min-width: 44px !important;
      height: 44px !important;
      font-size: 16px !important;
      
      .medal-icon {
        font-size: 28px !important;
      }
    }
    
    .user-avatar {
      width: 44px !important;
      height: 44px !important;
      font-size: 18px !important;
    }
    
    .stats-display {
      .stat-main {
        .stat-number {
          font-size: 22px !important;
        }
      }
    }
  }
}
</style>
