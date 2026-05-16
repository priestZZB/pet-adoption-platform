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
  padding: 76px 20px 0;
  background: var(--yc-bg-page);
}

/* 去掉子页面多余的底部留白（Footer 自带 margin-top 已提供间距） */
.main-content :deep(.home-page),
.main-content :deep(.mall-page),
.main-content :deep(.cart-page),
.main-content :deep(.orders-page),
.main-content :deep(.notice-page),
.main-content :deep(.dashboard),
.main-content :deep(.ai-chat-page) {
  padding-bottom: 0 !important;
}
</style>
