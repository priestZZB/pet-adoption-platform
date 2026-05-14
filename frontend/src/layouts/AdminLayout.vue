<template>
  <div class="admin-layout">
    <!-- 左侧边栏 -->
    <div class="sidebar-wrap" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo-area" @click="$router.push('/admin')">
        <span class="logo-text">{{ sidebarCollapsed ? '管' : '管理后台' }}</span>
      </div>
      <el-scrollbar>
        <Sidebar />
      </el-scrollbar>
    </div>

    <!-- 右侧主区域 -->
    <div class="main-area" :class="{ collapsed: sidebarCollapsed }">
      <!-- 顶栏 -->
      <div class="topbar">
        <el-button
          :icon="sidebarCollapsed ? 'Expand' : 'Fold'"
          text
          @click="sidebarCollapsed = !sidebarCollapsed"
        />

        <div class="topbar-right">
          <div class="admin-dropdown" ref="dropdownRef">
            <span class="admin-trigger" @click="toggleDropdown">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar" style="margin-right:6px">
                {{ userStore.userInfo?.nickname?.[0] || 'U' }}
              </el-avatar>
              {{ userStore.userInfo?.nickname || '管理员' }}
            </span>
            <transition name="dropdown-fade">
              <div v-if="dropdownOpen" class="admin-dropdown-menu">
                <div class="dropdown-item" @click.stop="goPage('/')">返回首页</div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-item dropdown-item-danger" @click.stop="handleLogout">退出登录</div>
              </div>
            </transition>
          </div>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="content-area">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function goPage(path) {
  dropdownOpen.value = false
  router.push(path)
}

function handleLogout() {
  dropdownOpen.value = false
  userStore.logout()
  router.push('/login')
}

function handleClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar-wrap {
  width: 220px;
  background: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  flex-shrink: 0;
}
.sidebar-wrap.collapsed {
  width: 64px;
}

.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-text {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.topbar-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.admin-dropdown {
  position: relative;
}
.admin-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #303133;
  outline: none;
  font-size: 14px;
}
.admin-trigger:hover {
  color: #409EFF;
}
.admin-dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 120px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  padding: 6px 0;
  z-index: 9999;
}
.dropdown-item {
  padding: 10px 18px;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}
.dropdown-item:hover {
  background: #f5f7fa;
  color: #409EFF;
}
.dropdown-item-danger {
  color: #f56c6c;
}
.dropdown-item-danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}
.dropdown-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 4px 0;
}
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}
.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
