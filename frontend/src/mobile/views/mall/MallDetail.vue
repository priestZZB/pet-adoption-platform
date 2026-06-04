<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="product">
      <!-- 图片轮播 -->
      <div class="carousel-wrap">
        <el-carousel height="100%">
          <el-carousel-item v-for="(img, idx) in images" :key="idx">
            <el-image :src="img" fit="cover" style="width:100%;height:100%" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 信息 -->
      <div class="card-wrap">
        <MobileCard>
          <h2 class="prod-name">{{ product.name }}</h2>
          <div class="price-row">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice > product.price" class="orig-price">¥{{ product.originalPrice }}</span>
          </div>
          <p v-if="product.description" class="desc">{{ product.description }}</p>
        </MobileCard>
      </div>

      <!-- 评价预览 -->
      <div class="card-wrap" v-if="reviewStats">
        <MobileCard>
          <div class="review-summary" @click="goReviews">
            <span class="rate-label">好评率</span>
            <span class="rate-value">{{ reviewStats.positiveRate || 0 }}%</span>
            <span class="rate-total">({{ reviewStats.total || 0 }} 条评价) ›</span>
          </div>
        </MobileCard>
      </div>
    </template>

    <!-- 底部购买栏 -->
    <div class="bottom-bar" v-if="product">
      <div class="bar-info">
        <span class="bar-price">¥{{ product.price }}</span>
      </div>
      <button class="btn-cart" @click="addCart">加入购物车</button>
      <button class="btn-buy" @click="buyNow">立即购买</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMallProductDetail, addToCart, buyNow as buyNowApi } from '@/api/mall'
import { getReviewStats } from '@/api/review'
import { useUserStore } from '@/stores/user'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter(); const userStore = useUserStore()
const product = ref(null); const images = ref([]); const loading = ref(true); const reviewStats = ref(null)

onMounted(async () => {
  try {
    product.value = await getMallProductDetail(route.params.id)
    images.value = product.value.images?.length ? product.value.images.map(i => i.url || i) : [product.value.coverImage].filter(Boolean)
  } catch {} finally { loading.value = false }
  try { reviewStats.value = await getReviewStats(route.params.id) } catch {}
})

function checkAuth() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); router.push('/login'); return false }
  return true
}

async function addCart() {
  if (!checkAuth()) return
  try { await addToCart({ productId: product.value.id, quantity: 1 }); ElMessage.success('已加入购物车') } catch {}
}

async function buyNow() {
  if (!checkAuth()) return
  try { const r = await buyNowApi({ productId: product.value.id, quantity: 1 }); router.push('/mall/pay/' + r.id) } catch {}
}

function goReviews() { router.push('/mall/products/' + product.value.id + '/reviews') }
</script>

<style scoped>
.detail-page { padding-bottom: 80px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.carousel-wrap { width: 100%; aspect-ratio: 4 / 3; overflow: hidden; background: #e8ddd0; }
.carousel-wrap :deep(.el-carousel) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__container) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__item) { height: 100% !important; }
.card-wrap { padding: 12px 16px 0; }
.prod-name { font-size: 18px; font-weight: 600; color: #5a4a42; margin: 0 0 8px; }
.price-row { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; }
.price { font-size: 22px; font-weight: 700; color: #e8564a; }
.orig-price { font-size: 14px; color: #b5a898; text-decoration: line-through; }
.desc { font-size: 14px; color: #a09080; line-height: 1.6; margin: 0; }
.review-summary { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.rate-label { font-size: 13px; color: #a09080; }
.rate-value { font-size: 16px; font-weight: 600; color: #8ab8a0; }
.rate-total { flex: 1; text-align: right; font-size: 13px; color: #a09080; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; display: flex; align-items: center; gap: 8px; padding: 8px 16px; padding-bottom: calc(8px + env(safe-area-inset-bottom, 0)); background: rgba(254,250,245,0.97); backdrop-filter: blur(8px); border-top: 1px solid #ece4d8; }
.bar-info { flex-shrink: 0; }
.bar-price { font-size: 20px; font-weight: 700; color: #e8564a; }
.btn-cart { flex: 1; height: 42px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.btn-cart:active { background: #f0e8dc; }
.btn-buy { flex: 1; height: 42px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-buy:active { opacity: 0.9; }
</style>
