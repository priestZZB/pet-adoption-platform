<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  // 启动时校验 token 是否有效
  // 如果数据库清空、用户不存在或 token 过期，自动清除，避免 Navbar 显示"幽灵用户"
  if (userStore.token) {
    await userStore.fetchUserInfo()
    // fetchUserInfo 内部已 catch 异常，失败时 userInfo 为 null
    // 若 userInfo 为空则 token 无效，清除登录态
    if (!userStore.userInfo) {
      userStore.logout()
    }
  }
})
</script>
