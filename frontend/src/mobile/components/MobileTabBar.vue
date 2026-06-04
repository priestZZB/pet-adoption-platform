<template>
  <nav class="mobile-tab-bar">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="tab-item"
      :class="{ active: isTabActive(tab) }"
      @click="navigateTab(tab)"
    >
      <i :class="tab.icon"></i>
      <span class="tab-label">{{ tab.label }}</span>
    </div>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const tabs = [
  { path: '/',              label: '首页',   icon: 'fas fa-home'          },
  { path: '/mall',         label: '商城',   icon: 'fas fa-shopping-bag'  },
  { path: '/ai',           label: 'AI助手', icon: 'fas fa-magic'          },
  { path: '/user/profile', label: '我的',   icon: 'fas fa-user'          },
]

function isTabActive(tab) {
  if (tab.path === '/') return route.path === '/'
  return route.path.startsWith(tab.path)
}

function navigateTab(tab) {
  if ((tab.path === '/ai' || tab.path === '/user/profile') && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(tab.path)
}
</script>

<style scoped>
.mobile-tab-bar {
  position: sticky;
  bottom: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom, 0);
  background: rgba(254, 250, 245, 0.97);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-top: 1px solid rgba(209, 231, 221, 0.5);
  flex-shrink: 0;
}
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: #a09080;
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}
.tab-item.active {
  color: #8ab8a0;
}
.tab-item i {
  font-size: 20px;
}
.tab-label {
  font-size: 11px;
  font-weight: 500;
}
</style>
