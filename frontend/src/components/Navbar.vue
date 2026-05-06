<template>
  <el-menu mode="horizontal" :router="true" class="navbar">
    <!-- 左上：Logo + 平台名称 -->
    <div class="navbar-brand">
      <el-icon :size="24" style="vertical-align:middle;margin-right:6px">
        <StarFilled style="color:#409EFF" />
      </el-icon>
      <span style="font-size:18px;font-weight:bold;color:#303133">宠物领养救助平台</span>
    </div>

    <!-- 中间导航菜单 -->
    <el-menu-item index="/">首页</el-menu-item>
    <el-menu-item index="/">宠物</el-menu-item>
    <el-menu-item index="/mall">商城</el-menu-item>
    <el-menu-item index="/notices">公告</el-menu-item>

    <div style="flex:1" />

    <!-- 右侧：登录/注册 或 用户下拉 -->
    <template v-if="userStore.isLogin">
      <el-sub-menu>
        <template #title>
          <el-avatar :size="28" :src="userStore.userInfo?.avatar" style="margin-right:6px">
            {{ userStore.userInfo?.nickname?.[0] || 'U' }}
          </el-avatar>
          {{ userStore.userInfo?.nickname || '用户' }}
        </template>
        <el-menu-item index="/user/profile">个人中心</el-menu-item>
        <el-menu-item index="/user/password">修改密码</el-menu-item>
        <el-menu-item v-if="userStore.isDonor" index="/donate/pets">我的发布</el-menu-item>
        <el-menu-item v-if="userStore.isVolunteer" index="/volunteer/pending">待审核</el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin">后台管理</el-menu-item>
        <el-menu-item @click="handleLogout">退出</el-menu-item>
      </el-sub-menu>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
      <el-menu-item index="/register">注册</el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  padding: 0 20px;
  display: flex;
  align-items: center;
}
.navbar-brand {
  margin-right: 30px;
  display: flex;
  align-items: center;
  cursor: default;
}
</style>


