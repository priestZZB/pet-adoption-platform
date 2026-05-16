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
  background: var(--yc-admin-sidebar);
  color: var(--yc-admin-sidebar-text);
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
  border-bottom: 2px solid #409EFF;
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
  background: var(--yc-admin-bg);
}

/* 侧边栏菜单激活 - 科技蓝左发光条 */
.sidebar-wrap :deep(.el-menu-item.is-active) {
  background: rgba(64,158,255,0.08) !important;
  position: relative;
  color: #fff !important;
}
.sidebar-wrap :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: #409EFF;
  border-radius: 0 2px 2px 0;
  box-shadow: 0 0 8px rgba(64,158,255,0.4);
}
.sidebar-wrap :deep(.el-menu-item:hover) {
  background: rgba(64,158,255,0.05);
}

/* 侧边栏深色主题适配 */
.sidebar-wrap :deep(.el-menu) {
  background: transparent;
  border-right: none;
}
.sidebar-wrap :deep(.el-menu-item) {
  color: var(--yc-admin-sidebar-text);
}
.sidebar-wrap :deep(.el-menu-item:hover) {
  color: #fff;
}
.sidebar-wrap :deep(.el-tooltip__trigger) {
  outline: none;
}
</style>

<!-- 全局后台页面通用样式 -->
<style>
/* 页面容器 */
.admin-page,
.dashboard {
  padding: 8px;
}

/* 页面标题：深海军蓝 + 左侧科技蓝条 */
.admin-page .page-title,
.dashboard .page-title {
  font-size: 18px;
  color: #1a2332;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 3px solid #409EFF;
}

/* 后台卡片 */
.admin-page .el-card,
.dashboard .el-card {
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

/* 后台表格圆角 */
.admin-page .el-table {
  border-radius: 10px;
  overflow: hidden;
}
.admin-page .el-table th.el-table__cell {
  background: #f5f7fa;
  color: #1a2332;
  font-weight: 600;
}

/* 后台表格内按钮（排除 link 类型，避免蓝底蓝字看不清） */
.admin-page .el-button--primary:not(.el-button--link) {
  background: #409EFF;
  border-color: #409EFF;
}
.admin-page .el-button--primary:not(.el-button--link):hover {
  background: #66b1ff;
  border-color: #66b1ff;
}

/* 后台分页 */
.admin-page .el-pagination.is-background .el-pager li.is-active {
  background: #409EFF;
}

/* 后台弹窗 */
.admin-page .el-dialog {
  border-radius: 12px;
}
.admin-page .el-dialog__header {
  margin: 0;
  padding: 18px 20px 14px;
}

/* 后台搜索按钮暖色改为科技蓝 */
.admin-page .toolbar .el-button--primary {
  background: #409EFF;
  border-color: #409EFF;
}
</style>
