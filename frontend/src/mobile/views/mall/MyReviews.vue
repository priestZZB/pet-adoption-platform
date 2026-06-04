<template>
  <div class="reviews-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="reviews.length === 0" icon="fas fa-star" description="暂无评价" />
    <div v-else class="review-list">
      <MobileCard v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-header">
          <div class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</div>
          <span class="review-time">{{ r.createdAt?.slice(0, 10) }}</span>
        </div>
        <div class="review-prod" v-if="r.productName">{{ r.productName }}</div>
        <p class="review-content" v-if="r.content">{{ r.content }}</p>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyReviews } from '@/api/review'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const reviews = ref([]); const loading = ref(true)

onMounted(async () => { try { reviews.value = await getMyReviews() } catch {} finally { loading.value = false } })
</script>

<style scoped>
.reviews-page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.review-item { margin-bottom: 10px; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.stars { font-size: 14px; color: #e6a23c; letter-spacing: 2px; }
.review-time { font-size: 12px; color: #a09080; }
.review-prod { font-size: 12px; color: #8ab8a0; margin-bottom: 4px; }
.review-content { font-size: 14px; color: #5a4a42; line-height: 1.5; margin: 0; }
</style>
