<template>
  <router-view />
</template>

<script setup>
// 移动端根组件 — 纯路由出口
// 共享桌面端的 api/ stores/ utils/ composables/ assets/
// 独立：mobile/ 下的 router views layouts components
</script>

<style>
/* 移动端全局重置 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  -webkit-tap-highlight-color: transparent;
  -webkit-font-smoothing: antialiased;
}

/* 禁止 iOS 双击缩放 */
button, a, input, select, textarea {
  touch-action: manipulation;
}

/* 统一盒模型 */
*, *::before, *::after {
  box-sizing: border-box;
}
</style>
