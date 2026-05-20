<template>
  <router-view />
  <!-- 未读公告弹窗 -->
  <el-dialog v-model="noticeVisible" title="📢 平台公告" width="500px" :close-on-click-modal="false" :show-close="false" class="notice-dialog">
    <div v-if="currentNotice" class="notice-body">
      <h3 class="notice-title">{{ currentNotice.title }}</h3>
      <div class="notice-content">{{ currentNotice.content }}</div>
    </div>
    <template #footer>
      <el-button size="large" style="width:100%" class="notice-dialog-btn" @click="handleNoticeRead">我知道了</el-button>
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
watch(() => userStore.token, async (newToken) => {
  if (newToken) {
    // 确保 userInfo 已加载
    if (!userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
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
  color: var(--yc-text-primary);
}
.notice-content {
  font-size: 14px;
  color: var(--yc-text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 全局 dialog 暖色风格 */
.notice-dialog .el-dialog {
  border-radius: var(--yc-radius-card);
}
.notice-dialog .el-dialog__header {
  background: var(--yc-bg-page);
  border-radius: var(--yc-radius-card) var(--yc-radius-card) 0 0;
  margin: 0;
  padding: 18px 20px 14px;
}
.notice-dialog .el-dialog__title {
  color: var(--yc-text-primary);
  font-size: 17px;
}
.notice-dialog .el-dialog__body {
  padding: 8px 20px 12px;
}
.notice-dialog .el-dialog__footer {
  padding: 0 20px 18px;
  border-top: 1px solid var(--yc-border);
}

.notice-dialog-btn {
  height: 42px;
  border-radius: var(--yc-radius-btn);
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}
.notice-dialog-btn:hover {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}

/* ===== 全局 body 背景（防止 fixed 导航栏脱离布局后漏出白色）===== */
html, body {
  margin: 0;
  padding: 0;
  background: var(--yc-bg-page);
}
</style>
