<template>
  <div class="admin-shell">
    <!-- 顶栏 -->
    <header class="admin-header">
      <button v-if="showBack" class="header-back" @click="goBack">
        <i class="fas fa-chevron-left"></i>
      </button>
      <h1 class="header-title">{{ pageTitle }}</h1>
      <button class="header-menu" @click="menuOpen = !menuOpen">
        <i class="fas fa-bars"></i>
      </button>
    </header>

    <!-- 侧边菜单抽屉 -->
    <div v-if="menuOpen" class="menu-overlay" @click="menuOpen = false" />
    <nav class="admin-menu" :class="{ open: menuOpen }">
      <div class="menu-title">管理后台</div>
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="'/admin' + item.path"
        class="menu-item"
        :class="{ active: isActive(item.path) }"
        @click="menuOpen = false"
      >
        <i :class="item.icon"></i>
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- 内容区 -->
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)

const showBack = computed(() => route.path !== '/admin')

const pageTitle = computed(() => {
  return route.meta?.title || '管理后台'
})

const menuItems = [
  { path: '',                label: '仪表盘',    icon: 'fas fa-chart-line'   },
  { path: '/users',          label: '用户管理',  icon: 'fas fa-users'        },
  { path: '/volunteer',      label: '志愿者管理', icon: 'fas fa-hands-helping' },
  { path: '/donor',          label: '送养人管理', icon: 'fas fa-hand-holding-heart' },
  { path: '/pet-categories', label: '宠物分类',  icon: 'fas fa-tags'         },
  { path: '/pets',           label: '宠物管理',  icon: 'fas fa-paw'          },
  { path: '/questions',      label: '考题管理',  icon: 'fas fa-question-circle' },
  { path: '/adopt-applications', label: '领养审核', icon: 'fas fa-clipboard-check' },
  { path: '/mall-categories', label: '商城分类', icon: 'fas fa-th-large'      },
  { path: '/mall-products',  label: '商品管理',  icon: 'fas fa-box'           },
  { path: '/mall-orders',    label: '订单管理',  icon: 'fas fa-receipt'       },
  { path: '/notices',        label: '公告管理',  icon: 'fas fa-bullhorn'      },
  { path: '/feedback',       label: '反馈管理',  icon: 'fas fa-comment-dots'  },
  { path: '/logs',           label: '操作日志',  icon: 'fas fa-history'       },
  { path: '/ai-records',     label: 'AI 记录',   icon: 'fas fa-robot'         },
  { path: '/banners',        label: '轮播管理',  icon: 'fas fa-images'        },
]

function isActive(itemPath) {
  if (itemPath === '') return route.path === '/admin'
  return route.path.startsWith('/admin' + itemPath)
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.admin-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: #f0f2f5;
}

/* 顶栏 */
.admin-header {
  position: sticky;
  top: 0;
  z-index: 998;
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  background: #1a2332;
  color: #fff;
  flex-shrink: 0;
}
.header-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin-right: 8px;
  border: none;
  background: none;
  color: #bfcbd9;
  font-size: 18px;
  cursor: pointer;
  border-radius: 8px;
  flex-shrink: 0;
}
.header-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #fff;
}
.header-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  color: #bfcbd9;
  font-size: 18px;
  cursor: pointer;
  border-radius: 8px;
  flex-shrink: 0;
}

/* 菜单遮罩 */
.menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
}

/* 侧边菜单 */
.admin-menu {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1001;
  width: 240px;
  background: #1a2332;
  padding: 16px 0;
  overflow-y: auto;
  transform: translateX(-100%);
  transition: transform 0.25s;
}
.admin-menu.open {
  transform: translateX(0);
}
.menu-title {
  font-size: 13px;
  font-weight: 600;
  color: #5a6a7e;
  padding: 0 20px 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  font-size: 14px;
  color: #bfcbd9;
  text-decoration: none;
  transition: all 0.15s;
}
.menu-item:active,
.menu-item.active {
  background: rgba(64, 158, 255, 0.12);
  color: #409EFF;
}
.menu-item i {
  width: 20px;
  text-align: center;
  font-size: 14px;
}

/* 内容区 */
.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
</style>
