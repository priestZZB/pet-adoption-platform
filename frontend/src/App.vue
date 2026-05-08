<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  // 启动时校验 token 是否有效
  // 如果数据库清空或 token 过期，自动清除，避免 Navbar 显示"幽灵用户"
  if (userStore.token) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
    }
  }
})
</script>
