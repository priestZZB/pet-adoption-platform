<template>
  <div v-if="isMobile" class="mobile-tab-bar">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="tab-item"
      :class="{ active: isActive(tab) }"
      @click="navigate(tab)"
    >
      <el-icon :size="20"><component :is="tab.icon" /></el-icon>
      <span class="tab-label">{{ tab.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMobile } from '@/composables/useMobile'
import { HomeFilled, ShoppingBag, MagicStick, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isMobile } = useMobile()

const tabs = [
  { path: '/',              label: '首页',  icon: HomeFilled   },
  { path: '/mall',          label: '商城',  icon: ShoppingBag  },
  { path: '/ai',            label: 'AI助手', icon: MagicStick  },
  { path: '/user/profile',  label: '我的',   icon: User         },
]

function isActive(tab) {
  if (tab.path === '/') return route.path === '/'
  if (tab.path === '/mall') return route.path.startsWith('/mall')
  if (tab.path === '/ai') return route.path === '/ai'
  if (tab.path === '/user/profile') return route.path.startsWith('/user/profile')
  return false
}

function navigate(tab) {
  // AI 和 我的 需要登录
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
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--yc-bg-card);
  border-top: 1px solid var(--yc-border);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom, 0);
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
  color: var(--yc-text-secondary);
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.tab-item.active {
  color: var(--yc-accent);
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
}
</style>
