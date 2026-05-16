<template>
  <div class="notice-detail-page">
    <el-page-header :icon="ArrowLeft" title="返回公告列表" @back="$router.push('/notices')" />

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="notice">
      <el-card class="detail-card">
        <h2 class="detail-title">{{ notice.title }}</h2>
        <p class="detail-time">发布时间：{{ notice.createdAt }}</p>
        <el-divider />
        <div class="detail-content">{{ notice.content }}</div>
      </el-card>
    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="公告不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { getNoticeDetail } from '@/api/notice'

const route = useRoute()
const notice = ref(null)
const loading = ref(true)

async function loadDetail() {
  loading.value = true
  try {
    notice.value = await getNoticeDetail(route.params.id)
  } catch {
    notice.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.notice-detail-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.detail-card {
  margin-top: 20px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}
.detail-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: var(--yc-text-primary);
}
.detail-time {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-tertiary);
}
.detail-content {
  font-size: 15px;
  color: var(--yc-text-primary);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
