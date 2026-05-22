<template>
  <div class="my-reviews-page">
    <h3 class="page-title">我的评价</h3>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <div v-else-if="list.length > 0" class="review-list">
      <div v-for="rv in list" :key="rv.id" class="review-card">
        <div class="review-header">
          <div class="review-stars">
            <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= rv.rating }">★</span>
          </div>
          <span class="review-time">{{ formatTime(rv.createdAt) }}</span>
        </div>
        <div v-if="rv.content" class="review-content">{{ rv.content }}</div>
        <div v-if="rv.images && rv.images.length > 0" class="review-images">
          <el-image v-for="(img, idx) in rv.images" :key="idx" :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px;cursor:pointer" :preview-src-list="rv.images" :initial-index="idx" preview-teleported />
        </div>
        <div class="review-footer">
          <el-tag size="small">{{ rv.rating }}星</el-tag>
          <el-button text size="small" @click="goProduct(rv.productId)">查看商品 ›</el-button>
        </div>
      </div>
      <div v-if="list.length >= pageSize" class="load-more">
        <el-button text @click="pageNo++; loadList()">加载更多</el-button>
      </div>
    </div>

    <div v-else class="empty-tip">
      <el-empty description="还没有评价过商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { getMyReviews } from '@/api/review'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const pageNo = ref(1)
const pageSize = ref(20)

async function loadList() {
  loading.value = true
  try {
    const res = await getMyReviews({ page: pageNo.value, size: pageSize.value })
    if (pageNo.value === 1) list.value = res || []
    else if (res) list.value = list.value.concat(res)
  } catch {} finally { loading.value = false }
}

function goProduct(productId) {
  router.push('/mall/products/' + productId + '?tab=reviews')
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
}

onMounted(loadList)
</script>

<style scoped>
.my-reviews-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0;
}
.page-title {
  font-size: 18px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}
.loading-center {
  text-align: center;
  padding: 60px 0;
}
.empty-tip {
  padding: 60px 0;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.review-card {
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 16px 20px;
  box-shadow: var(--yc-shadow-card);
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.star { font-size: 18px; color: #ddd; }
.star.active { color: #f0c040; }
.review-time { font-size: 12px; color: var(--yc-text-tertiary); }
.review-content { font-size: 14px; color: var(--yc-text-primary); line-height: 1.5; margin-bottom: 8px; }
.review-images { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.review-footer { display: flex; justify-content: space-between; align-items: center; }
.load-more { text-align: center; padding: 16px 0; }
</style>
