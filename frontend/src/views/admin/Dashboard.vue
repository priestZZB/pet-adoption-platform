<template>
  <div class="dashboard">
    <h3 class="page-title">控制台</h3>

    <div class="stat-grid">
      <el-card v-for="s in stats" :key="s.label" class="stat-card" shadow="hover">
        <div class="stat-inner">
          <el-icon :size="36" :color="s.color"><component :is="s.icon" /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="welcome-card">
      <h4>欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}</h4>
      <p>宠物领养救助管理平台 — 后台管理系统</p>
    </el-card>
  </div>
</template>

<script setup>
import {
  User, Guide, Select, Warning, Tickets
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 无统计接口时使用静态数据
const stats = [
  { label: '注册用户', value: '—', icon: User, color: '#409EFF' },
  { label: '宠物总数', value: '—', icon: Guide, color: '#67C23A' },
  { label: '领养申请', value: '—', icon: Select, color: '#E6A23C' },
  { label: '待审核',   value: '—', icon: Warning, color: '#F56C6C' },
  { label: '订单总数', value: '—', icon: Tickets, color: '#909399' }
]
</script>

<style scoped>
.dashboard {
  max-width: 1000px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  cursor: default;
}
.stat-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}

.welcome-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}
.welcome-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
</style>
