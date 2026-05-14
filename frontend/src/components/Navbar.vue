<template>
  <div class="navbar-wrapper">
    <el-menu mode="horizontal" :default-active="activeMenu" class="navbar">
      <div class="navbar-brand">
        <el-image src="/logo.jpg" fit="contain" style="width:32px;height:32px;vertical-align:middle;margin-right:8px;border-radius:6px" />
        <span style="font-size:18px;font-weight:bold;color:#303133">毛球寻觅</span>
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
        <el-button type="primary" size="small" @click="router.push('/login')">登录</el-button>
        <el-button size="small" @click="router.push('/register')" style="margin-left:8px">注册</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useUserStore } from "@/stores/user"
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from "element-plus"

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

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
  router.push("/")
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

function handleClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside)
})
onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside)
})
</script>

<style scoped>
.navbar-wrapper {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
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
  color: #303133;
  outline: none;
  font-size: 14px;
}
.user-trigger:hover {
  color: #409EFF;
}
.user-dropdown {
  position: relative;
}
.dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 140px;
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
</style>
