<template>
  <div class="mobile-shell">
    <MobileHeader />
    <main class="mobile-content">
      <router-view />
    </main>
    <MobileTabBar />
  </div>
</template>

<script setup>
import MobileHeader from '../components/MobileHeader.vue'
import MobileTabBar from '../components/MobileTabBar.vue'
</script>

<style scoped>
.mobile-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-height: 100dvh;
  background: #f7f1e5;
}
.mobile-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
