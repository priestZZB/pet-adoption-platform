<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <Navbar />

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>

    <!-- 页脚 -->
    <Footer />

    <!-- 浮动AI助手按钮 -->
    <AIChatWidget v-if="userStore.isLogin" />
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import AIChatWidget from '@/components/AIChatWidget.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding: 20px;
  background: #f5f7fa;
}
</style>
