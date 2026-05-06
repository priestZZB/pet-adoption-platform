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
          <span class="admin-nickname">{{ userStore.userInfo?.nickname || '管理员' }}</span>
          <el-dropdown>
            <el-avatar :size="32" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.nickname?.[0] || 'U' }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import Sidebar from '@/components/Sidebar.vue'

const router = useRouter()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
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
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-nickname {
  font-size: 14px;
  color: #606266;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
