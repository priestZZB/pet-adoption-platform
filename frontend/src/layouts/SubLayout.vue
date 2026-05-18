<template>
  <div class="sub-layout">
    <!-- 顶部 -->
    <div class="sub-header">
      <div class="sub-header-left">
        <el-button text :icon="ArrowLeft" size="large" @click="goBack" style="font-size:16px">
          返回
        </el-button>
      </div>
      <div class="sub-header-title">
        <el-image src="/images/logo.jpg" fit="contain" style="width:24px;height:24px;vertical-align:middle;margin-right:6px;border-radius:4px" />
        <span style="font-size:16px;font-weight:bold;color:#303133">有宠</span>
      </div>
    </div>

    <!-- 背景层（同登录页暖色纹理） -->
    <div class="auth-bg"></div>

    <!-- 主内容 -->
    <div class="sub-content">
      <router-view />
    </div>

    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup>
import { ArrowLeft, StarFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/Footer.vue'

const router = useRouter()

function goBack() {
  const path = router.currentRoute.value.path

  // ===== 精确匹配首页级别的 =====
  if (path === '/user/chats') { router.push('/'); return }
  if (path === '/user/adopt-applications') { router.push('/'); return }
  if (path === '/volunteer/pending') { router.push('/'); return }
  if (path === '/volunteer/reviewed') { router.push('/'); return }
  if (path === '/volunteer/visits') { router.push('/'); return }
  if (path === '/donate/pets') { router.push('/'); return }
  if (path === '/donate/publish') { router.push('/donate/pets'); return }

  // ===== 子页面 → 父页面 =====
  // 聊天
  if (path === '/user/chat') { router.push('/user/chats'); return }
  // 志愿者子页
  if (path.startsWith('/volunteer/review/')) { router.push('/volunteer/pending'); return }
  if (path.startsWith('/volunteer/review-history/')) { router.push('/volunteer/reviewed'); return }
  if (path.startsWith('/volunteer/visits/')) { router.push('/volunteer/pending'); return }
  // 送养人子页
  if (path.startsWith('/donate/detail/')) { router.push('/donate/pets'); return }
  if (path.startsWith('/donate/pets/')) { router.push('/donate/pets'); return }
  // 领养子页
  if (path.startsWith('/user/adopt-application/')) { router.push('/user/adopt-applications'); return }
  // 用户子页（所有 /user/* 没有匹配到的）
  if (path.startsWith('/user/')) { router.push('/'); return }
  // 领养考试/申请
  if (path.startsWith('/adopt/')) { router.push('/'); return }
  // 宠物详情
  if (path.startsWith('/pets/')) { router.push('/'); return }
  // 商城
  if (path.startsWith('/mall/')) { router.push('/mall'); return }

  // 兜底
  router.back()
}
</script>

<style scoped>
.sub-layout {
  min-height: 100vh;
  background: var(--yc-bg-page);
  position: relative;
}
.auth-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: linear-gradient(135deg, rgba(247,241,229,0.82) 0%, rgba(254,250,245,0.9) 100%),
              url('/images/bg.png') center/cover no-repeat;
  pointer-events: none;
}
.sub-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  background: var(--yc-bg-card);
  border-bottom: 1px solid var(--yc-border);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.sub-header-left {
  flex: 1;
}
.sub-header-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}
.sub-content {
  position: relative;
  z-index: 1;
  padding: 76px 20px 0;
  max-width: 800px;
  margin: 0 auto;
}

/* 去掉子页面多余的底部留白 */
.sub-content :deep(.detail-page),
.sub-content :deep(.exam-page),
.sub-content :deep(.apply-page),
.sub-content :deep(.publish-page),
.sub-content :deep(.applications-page),
.sub-content :deep(.favorites-page),
.sub-content :deep(.pending-page),
.sub-content :deep(.history-page),
.sub-content :deep(.add-visit-page),
.sub-content :deep(.visits-page),
.sub-content :deep(.profile-page),
.sub-content :deep(.edit-page),
.sub-content :deep(.realname-page),
.sub-content :deep(.password-page),
.sub-content :deep(.feedback-page),
.sub-content :deep(.checkout-page),
.sub-content :deep(.pay-page),
.sub-content :deep(.donate-list-page),
.sub-content :deep(.notice-detail-page) {
  padding-bottom: 0 !important;
}
</style>
