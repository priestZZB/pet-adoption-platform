<template>
  <div class="page">
    <div class="content">
      <i class="fas fa-wifi icon"></i>
      <h2>网络连接失败</h2>
      <p>请检查你的网络连接后重试</p>
      <button @click="reload">重新加载</button>
    </div>
  </div>
</template>

<script setup>
function reload() { window.location.reload() }
</script>

<style scoped>
.page { min-height: 100vh; min-height: 100dvh; display: flex; align-items: center; justify-content: center; background: #f7f1e5; }
.content { text-align: center; padding: 32px; }
.icon { font-size: 64px; color: #d1e7dd; margin-bottom: 16px; }
h2 { font-size: 20px; font-weight: 600; color: #5a4a42; margin: 0 0 8px; }
p { font-size: 14px; color: #a09080; margin: 0 0 24px; }
button { height: 44px; padding: 0 32px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
</style>
