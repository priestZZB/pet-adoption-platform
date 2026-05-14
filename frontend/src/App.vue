<template>
  <router-view />
  <!-- 未读公告弹窗 -->
  <el-dialog v-model="noticeVisible" title="📢 平台公告" width="500px" :close-on-click-modal="false" :show-close="false">
    <div v-if="currentNotice" class="notice-body">
      <h3 class="notice-title">{{ currentNotice.title }}</h3>
      <div class="notice-content">{{ currentNotice.content }}</div>
    </div>
    <template #footer>
      <el-button type="primary" size="large" style="width:100%" @click="handleNoticeRead">我知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUnreadNotices, markNoticeRead } from '@/api/notice'

const userStore = useUserStore()
const noticeVisible = ref(false)
const currentNotice = ref(null)
const unreadList = ref([])

async function checkUnread() {
  if (!userStore.token || !userStore.userInfo) return
  // 管理员不弹窗
  if (userStore.isAdmin) return
  try {
    unreadList.value = await getUnreadNotices() || []
    if (unreadList.value.length > 0) {
      showNextNotice()
    }
  } catch {
    // 忽略
  }
}

function showNextNotice() {
  if (unreadList.value.length > 0) {
    currentNotice.value = unreadList.value.shift()
    noticeVisible.value = true
  }
}

async function handleNoticeRead() {
  if (currentNotice.value) {
    try {
      await markNoticeRead(currentNotice.value.id)
    } catch { /* 忽略 */ }
  }
  noticeVisible.value = false
  // 弹出下一条未读公告
  if (unreadList.value.length > 0) {
    showNextNotice()
  }
}

onMounted(async () => {
  if (userStore.token) {
    await userStore.fetchUserInfo()
    if (!userStore.userInfo) {
      userStore.logout()
    } else {
      checkUnread()
    }
  }
})

// 监听登录状态变化（用户在页面内登录时触发）
watch(() => userStore.token, (newToken) => {
  if (newToken && userStore.userInfo) {
    checkUnread()
  }
})
</script>

<style>
.notice-body {
  padding: 8px 0;
}
.notice-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}
.notice-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
