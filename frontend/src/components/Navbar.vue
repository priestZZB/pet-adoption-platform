<template>
  <div class="navbar-wrapper">
    <el-menu mode="horizontal" :default-active="activeMenu" class="navbar">
      <div class="navbar-brand">
        <el-image src="/images/logo.jpg" fit="contain" style="width:32px;height:32px;vertical-align:middle;margin-right:8px;border-radius:6px" />
        <span style="font-size:18px;font-weight:bold;color:#303133">有宠</span>
      </div>
      <el-menu-item index="/" @click="router.push('/')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/mall" @click="router.push('/mall')">
        <el-icon><ShoppingBag /></el-icon>
        <span>商城</span>
      </el-menu-item>
      <el-menu-item index="/notices" @click="router.push('/notices')">
        <el-icon><Bell /></el-icon>
        <span>公告</span>
      </el-menu-item>
      <el-menu-item index="/ai" @click="handleAIClick">
        <el-icon><MagicStick /></el-icon>
        <span>AI助手</span>
      </el-menu-item>
      <el-menu-item index="/user/orders" @click="handleOrderClick">
        <el-icon><Tickets /></el-icon>
        <span>我的订单</span>
      </el-menu-item>
      <el-menu-item index="/mall/cart" @click="handleCartClick">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
      </el-menu-item>
    </el-menu>

    <div class="user-section">
      <!-- 通知铃铛 -->
      <div v-if="userStore.isLogin" class="notification-bell" ref="notifRef">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
          <el-icon :size="22" class="bell-icon" @click="toggleNotif"><Bell /></el-icon>
        </el-badge>
        <transition name="dropdown-fade">
          <div v-if="notifOpen" class="notif-dropdown">
            <div class="notif-header">
              <span class="notif-title">消息通知</span>
              <span v-if="unreadCount > 0" class="notif-mark-all" @click.stop="handleMarkAll">全部已读</span>
            </div>
            <div v-if="notifList.length === 0" class="notif-empty">暂无通知</div>
            <div
              v-for="n in notifList"
              :key="n.id"
              class="notif-item"
              :class="{ 'notif-unread': n.isRead === 0 }"
              @click.stop="handleNotifClick(n)"
            >
              <div class="notif-dot" v-if="n.isRead === 0"></div>
              <div class="notif-body">
                <div class="notif-title-text">{{ n.title }}</div>
                <div class="notif-content-text">{{ n.content }}</div>
                <div class="notif-time">{{ formatTime(n.createdAt) }}</div>
              </div>
            </div>
          </div>
        </transition>
      </div>
      <template v-if="userStore.isLogin">
        <div class="user-dropdown" ref="dropdownRef">
          <span class="user-trigger" @click="toggleDropdown">
            <el-avatar :size="28" :src="userStore.userInfo?.avatar" style="margin-right:6px">
              {{ userStore.userInfo?.nickname?.[0] || "U" }}
            </el-avatar>
            {{ userStore.userInfo?.nickname || "用户" }}
          </span>
          <transition name="dropdown-fade">
            <div v-if="dropdownOpen" class="dropdown-menu">
              <div class="dropdown-item" @click.stop="goPage('/user/profile')">个人中心</div>
              <div class="dropdown-item" @click.stop="goPage('/adopt/exam')">领养考试</div>
              <div class="dropdown-item" v-if="userStore.isDonor" @click.stop="goPage('/donate/pets')">我的发布</div>
              <div class="dropdown-item" v-if="userStore.isVolunteer" @click.stop="goPage('/volunteer/pending')">待审核宠物</div>
              <div class="dropdown-item" v-if="userStore.isVolunteer" @click.stop="goPage('/volunteer/reviewed')">审核历史</div>
              <div class="dropdown-item" v-if="userStore.isVolunteer" @click.stop="goPage('/volunteer/visits/add')">去走访</div>
              <div class="dropdown-item" v-if="userStore.isVolunteer" @click.stop="goPage('/volunteer/visits')">走访记录</div>
              <div class="dropdown-item" v-if="userStore.isAdmin" @click.stop="goPage('/admin')">后台管理</div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item dropdown-item-danger" @click.stop="handleLogout">退出登录</div>
            </div>
          </transition>
        </div>
      </template>
      <template v-else>
        <el-button size="small" class="nav-login-btn" @click="router.push('/login')">登录</el-button>
        <el-button size="small" class="nav-reg-btn" @click="router.push('/register')" style="margin-left:8px">注册</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useUserStore } from "@/stores/user"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from "@/api/notification"

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

// ---- 通知 ----
const notifOpen = ref(false)
const notifRef = ref(null)
const notifList = ref([])
const unreadCount = ref(0)

// 当前路由对应的高亮菜单
const activeMenu = computed(() => route.path)

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
}

function handleOrderClick() {
  if (!userStore.isLogin) {
    ElMessage.warning("请先登录")
    router.push("/login")
  } else {
    router.push("/user/orders")
  }
}

function handleCartClick() {
  if (!userStore.isLogin) {
    ElMessage.warning("请先登录")
    router.push("/login")
  } else {
    router.push("/mall/cart")
  }
}

function handleAIClick() {
  if (!userStore.isLogin) {
    ElMessage.warning("请先登录")
    router.push("/login")
  } else {
    router.push("/ai")
  }
}

// ---- 通知逻辑 ----
async function loadNotifications() {
  if (!userStore.isLogin) return
  try {
    const [notifs, countData] = await Promise.all([
      getNotifications(),
      getUnreadCount()
    ])
    notifList.value = (notifs || []).slice(0, 20)
    unreadCount.value = countData?.count ?? 0
  } catch {
    // ignore
  }
}

function toggleNotif() {
  notifOpen.value = !notifOpen.value
  if (notifOpen.value && notifList.value.length === 0) {
    loadNotifications()
  }
}

async function handleNotifClick(n) {
  // 标记已读
  if (n.isRead === 0) {
    try {
      await markAsRead(n.id)
      n.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch { /* ignore */ }
  }
  notifOpen.value = false
}

async function handleMarkAll() {
  try {
    await markAllAsRead()
    notifList.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    ElMessage.success("已全部标记为已读")
  } catch { /* ignore */ }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}

function handleClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
  if (notifRef.value && !notifRef.value.contains(e.target)) {
    notifOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside)
  // 登录后初始加载通知数
  if (userStore.isLogin) {
    getUnreadCount().then(d => { unreadCount.value = d?.count ?? 0 }).catch(() => {})
  }
})
onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside)
})
</script>

<style scoped>
.navbar-wrapper {
  display: flex;
  align-items: center;
  background: var(--yc-bg-card);
  border-bottom: 1px solid var(--yc-border);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
.navbar {
  flex: 1;
  border-bottom: none !important;
}
.navbar-brand {
  display: inline-flex;
  align-items: center;
  margin-right: 10px;
  padding: 0 16px;
  cursor: default;
  height: 60px;
  flex-shrink: 0;
}
.user-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding-right: 20px;
}
.user-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--yc-text-primary);
  outline: none;
  font-size: 14px;
}
.user-trigger:hover {
  color: var(--yc-accent);
}
.user-dropdown {
  position: relative;
}
.dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 140px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-input);
  box-shadow: var(--yc-shadow-dropdown);
  padding: 6px 0;
  z-index: 9999;
}
.dropdown-item {
  padding: 10px 18px;
  font-size: 14px;
  color: var(--yc-text-primary);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;
}
.dropdown-item:hover {
  background: var(--yc-bg-page);
  color: var(--yc-accent);
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
  background: var(--yc-border);
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

/* ====== 通知铃铛 ====== */
.notification-bell {
  position: relative;
  margin-right: 16px;
  display: flex;
  align-items: center;
}
.bell-icon {
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}
.bell-icon:hover {
  color: #409EFF;
}
.notif-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: -10px;
  width: 340px;
  max-height: 420px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  z-index: 9999;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
  position: sticky;
  top: 0;
  background: #fff;
  border-radius: 10px 10px 0 0;
}
.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.notif-mark-all {
  font-size: 12px;
  color: #409EFF;
  cursor: pointer;
}
.notif-mark-all:hover {
  color: #66b1ff;
}
.notif-empty {
  text-align: center;
  padding: 30px 0;
  font-size: 13px;
  color: #c0c4cc;
}
.notif-item {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
  transition: background 0.15s;
}
.notif-item:last-child {
  border-bottom: none;
}
.notif-item:hover {
  background: #f5f7fa;
}
.notif-item.notif-unread {
  background: #ecf5ff;
}
.notif-item.notif-unread:hover {
  background: #d9ecff;
}
.notif-dot {
  width: 8px;
  height: 8px;
  background: #409EFF;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}
.notif-body {
  flex: 1;
  min-width: 0;
}
.notif-title-text {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}
.notif-unread .notif-title-text {
  color: #409EFF;
}
.notif-content-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notif-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* 暖色登录/注册按钮 */
:deep(.nav-login-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.nav-login-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.nav-reg-btn) {
  border-radius: var(--yc-radius-btn);
  border: 1px solid var(--yc-border);
  color: var(--yc-text-primary);
  font-weight: 500;
}
:deep(.nav-reg-btn:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
</style>
