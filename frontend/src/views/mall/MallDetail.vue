<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="product">
      <div class="detail-layout">
        <!-- 商品图片 -->
        <div class="detail-left">
          <el-image
            :key="currentImgIndex"
            :src="currentImage"
            fit="contain"
            style="width:100%;height:400px;border-radius:8px"
            :preview-src-list="imageList"
            :initial-index="currentImgIndex"
            preview-teleported
          >
            <template #error>
              <div class="img-placeholder">暂无图片</div>
            </template>
          </el-image>
          <div v-if="imageList.length > 1" class="thumbnails">
            <div
              v-for="(img, i) in imageList"
              :key="i"
              class="thumb-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img; currentImgIndex = i"
            >
              <el-image :src="img" fit="cover" style="width:100%;height:100%" />
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-right">
          <h2 class="product-name">{{ product.name }}</h2>
          <p class="product-desc">{{ product.description || '暂无描述' }}</p>

          <el-divider />

          <div class="price-row">
            <span class="label">价格</span>
            <span class="price">¥{{ product.price }}</span>
          </div>

          <div class="info-row">
            <span class="label">分类</span>
            <span>{{ product.categoryName || '—' }}</span>
          </div>

          <div class="info-row">
            <span class="label">库存</span>
            <span :class="product.stock > 0 ? 'in-stock' : 'out-stock'">
              {{ product.stock > 0 ? '有货（' + product.stock + '件）' : '暂时缺货' }}
            </span>
          </div>

          <el-divider />

          <!-- 数量选择 + 加入购物车 -->
          <div class="action-row">
            <div class="quantity-wrapper">
              <span class="label">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="Math.max(product.stock, 1)"
                size="large"
              />
            </div>
            <div class="btn-wrap">
              <el-button
                class="buy-now-btn"
                size="large"
                :disabled="product.stock <= 0"
                @click="handleBuyNow"
              >
                立即购买
              </el-button>
            </div>
            <div class="btn-wrap">
              <el-button
                class="add-cart-btn"
                size="large"
                :icon="ShoppingCart"
                :disabled="product.stock <= 0"
                @click="handleAddCart"
              >
                加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 商品评价 ===== -->
      <div class="review-section">
        <div class="review-header">
          <h3>商品评价</h3>
        </div>

        <!-- 评分概览 -->
        <div v-if="reviewStats" class="review-stats-bar">
          <div class="stats-left">
            <div class="avg-score">{{ reviewStats.average }}</div>
            <div class="avg-stars">
              <span v-for="i in 5" :key="i" class="star">{{ i <= Math.round(reviewStats.average) ? '⭐' : '☆' }}</span>
            </div>
            <div class="avg-text">{{ reviewStats.goodRate }}%好评</div>
          </div>
          <div class="stats-right">
            <div v-for="i in 5" :key="i" class="stat-row">
              <span class="stat-label">{{ 6 - i }}星</span>
              <div class="stat-bar-bg">
                <div class="stat-bar-fill" :style="{ width: (reviewStats.distribution[6-i] / Math.max(reviewStats.total, 1) * 100) + '%' }"></div>
              </div>
              <span class="stat-count">{{ reviewStats.distribution[6-i] }}</span>
            </div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="review-filter-bar">
          <el-button :type="reviewRating === 'all' ? 'primary' : 'default'" size="small" @click="reviewRating = 'all'; loadReviews()">全部({{ reviewStats?.total || 0 }})</el-button>
          <el-button :type="reviewRating === 'good' ? 'primary' : 'default'" size="small" @click="reviewRating = 'good'; loadReviews()">好评</el-button>
          <el-button :type="reviewRating === 'mid' ? 'primary' : 'default'" size="small" @click="reviewRating = 'mid'; loadReviews()">中评</el-button>
          <el-button :type="reviewRating === 'bad' ? 'primary' : 'default'" size="small" @click="reviewRating = 'bad'; loadReviews()">差评</el-button>
          <el-button :type="reviewHasImage ? 'primary' : 'default'" size="small" @click="reviewHasImage = !reviewHasImage; loadReviews()">有图</el-button>
          <el-select v-model="reviewSort" size="small" style="width:110px;margin-left:auto" @change="loadReviews">
            <el-option label="最新" value="latest" />
            <el-option label="最高分" value="highest" />
            <el-option label="最低分" value="lowest" />
            <el-option label="有图优先" value="imageFirst" />
          </el-select>
        </div>

        <!-- 评价列表 -->
        <div v-if="reviewList.length > 0" class="review-list">
          <div v-for="rv in reviewList" :key="rv.id" class="review-item">
            <div class="review-user">
              <el-avatar :size="32" :src="rv.avatar" />
              <span class="review-nickname">{{ rv.nickname || '匿名用户' }}</span>
              <span class="review-time">{{ formatTime(rv.createdAt) }}</span>
            </div>
            <div class="review-stars">
              <span v-for="i in 5" :key="i" class="star" :class="{ active: i <= rv.rating }">★</span>
            </div>
            <div v-if="rv.tags && rv.tags.length > 0" class="review-tags">
              <el-tag v-for="t in rv.tags" :key="t" size="small" round>{{ t }}</el-tag>
            </div>
            <div class="review-content">{{ rv.content }}</div>
            <div v-if="rv.images && rv.images.length > 0" class="review-images">
              <el-image v-for="(img, idx) in rv.images" :key="idx" :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px;cursor:pointer" :preview-src-list="rv.images" :initial-index="idx" preview-teleported />
            </div>
            <div v-if="rv.additions && rv.additions.length > 0" class="review-additions">
              <div v-for="add in rv.additions" :key="add.id" class="addition-item">
                <span class="addition-label">追评：</span>
                <span>{{ add.content }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="review-empty">暂无评价</div>
      </div>

    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="商品不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, ShoppingCart } from '@element-plus/icons-vue'
import { getMallProductDetail, addToCart } from '@/api/mall'
import { getProductReviews, getReviewStats } from '@/api/review'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
const currentImage = ref('')
const currentImgIndex = ref(0)

const imageList = computed(() => {
  const img = product.value?.image
  if (!img) return []
  return img.split(',').filter(Boolean)
})

async function loadDetail() {
  loading.value = true
  try {
    product.value = await getMallProductDetail(route.params.id)
    currentImage.value = imageList.value[0] || ''
    currentImgIndex.value = 0
    loadReviews()
    loadReviewStats()
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

// ===== 评价区 =====
const reviewList = ref([])
const reviewStats = ref(null)
const reviewRating = ref('all')
const reviewHasImage = ref(false)
const reviewSort = ref('latest')

async function loadReviews() {
  try {
    const params = { sort: reviewSort.value, page: 1, size: 20 }
    if (reviewRating.value !== 'all') params.rating = reviewRating.value
    if (reviewHasImage.value) params.hasImage = true
    const res = await getProductReviews(route.params.id, params)
    reviewList.value = res.list || []
  } catch {}
}

async function loadReviewStats() {
  try {
    reviewStats.value = await getReviewStats(route.params.id)
  } catch {}
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
}

async function handleAddCart() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (userStore.isAdmin) {
    ElMessage.warning('管理员不能执行此操作')
    return
  }

  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch {
    // 请求拦截器统一处理
  }
}

async function handleBuyNow() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (userStore.isAdmin) {
    ElMessage.warning('管理员不能执行此操作')
    return
  }

  try {
    // 先加入购物车，然后跳转结算页（跟购物车点结算同一页面）
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    router.push('/mall/checkout')
  } catch {
    // 请求拦截器统一处理
  }
}

onMounted(loadDetail)

// 轮询：每30秒刷新评论
let reviewTimer = null
onMounted(() => {
  reviewTimer = setInterval(() => {
    if (product.value) {
      loadReviews()
      loadReviewStats()
    }
  }, 10000)
})
onUnmounted(() => {
  if (reviewTimer) clearInterval(reviewTimer)
})
</script>

<style scoped>
.detail-page {
  max-width: 1000px;
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

.detail-layout {
  display: flex;
  gap: 40px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 28px;
  box-shadow: var(--yc-shadow-card);
}
.detail-left {
  flex: 1;
  min-width: 0;
}
.detail-right {
  width: 420px;
  flex-shrink: 0;
}
.img-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--yc-bg-card);
  color: var(--yc-text-tertiary);
  font-size: 14px;
  border-radius: 8px;
}

.product-name {
  margin: 0 0 10px;
  font-size: 22px;
  color: var(--yc-text-primary);
}
.product-desc {
  margin: 0;
  font-size: 14px;
  color: var(--yc-text-secondary);
  line-height: 1.6;
}

.price-row,
.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.price-row .label,
.info-row .label {
  font-size: 14px;
  color: var(--yc-text-tertiary);
  width: 50px;
  flex-shrink: 0;
}
.price {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}
.in-stock {
  color: #67C23A;
  font-size: 14px;
}
.out-stock {
  color: #F56C6C;
  font-size: 14px;
}

.action-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}
.btn-wrap {
  width: 100%;
}
.btn-wrap :deep(.el-button) {
  width: 100%;
}
.quantity-wrapper .label {
  font-size: 14px;
  color: var(--yc-text-tertiary);
}

/* 立即购买按钮 */
:deep(.buy-now-btn) {
  background: #c19a6b;
  border: 1px solid #c19a6b;
  color: #fff;
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 24px;
  font-size: 15px;
}
:deep(.buy-now-btn:hover) {
  background: #b0895a;
  border-color: #b0895a;
  color: #fff;
}

/* 加入购物车按钮暖色 */
:deep(.add-cart-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 24px;
  font-size: 15px;
}
:deep(.add-cart-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}

.thumbnails {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.thumb-item {
  width: 64px;
  height: 64px;
  border-radius: var(--yc-radius-tag);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--yc-border);
  transition: border-color 0.2s;
}
.thumb-item.active {
  border-color: var(--yc-accent);
}
.thumb-item:hover {
  border-color: var(--yc-accent);
}

/* ===== 评价区 ===== */
.review-section {
  margin-top: 24px;
  background: var(--yc-bg-card);
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  padding: 24px 28px;
  box-shadow: var(--yc-shadow-card);
}
.review-header h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--yc-text-primary);
}
.review-stats-bar {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: var(--yc-bg-page);
  border-radius: 8px;
  margin-bottom: 16px;
}
.stats-left {
  text-align: center;
  min-width: 100px;
}
.avg-score {
  font-size: 36px;
  font-weight: bold;
  color: #F56C6C;
  line-height: 1;
}
.avg-stars {
  margin: 4px 0;
}
.avg-text {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.stats-right {
  flex: 1;
}
.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 12px;
  color: var(--yc-text-tertiary);
  width: 30px;
}
.stat-bar-bg {
  flex: 1;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}
.stat-bar-fill {
  height: 100%;
  background: #f0c040;
  border-radius: 4px;
  transition: width 0.3s;
}
.stat-count {
  font-size: 12px;
  color: var(--yc-text-tertiary);
  width: 20px;
  text-align: right;
}
.review-filter-bar {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--yc-border);
}
.review-item:last-child {
  border-bottom: none;
}
.review-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.review-nickname {
  font-size: 14px;
  font-weight: 500;
  color: var(--yc-text-primary);
}
.review-time {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.review-stars {
  margin-bottom: 4px;
}
.review-stars .star {
  font-size: 18px;
  color: #ccc;
}
.review-stars .star.active {
  color: #f0c040;
}
.review-tags {
  margin-bottom: 6px;
}
.review-content {
  font-size: 14px;
  color: var(--yc-text-primary);
  line-height: 1.5;
  margin-bottom: 6px;
}
.review-images {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.review-additions {
  margin-top: 8px;
  padding: 8px 10px;
  background: var(--yc-bg-page);
  border-radius: 6px;
  font-size: 13px;
  color: var(--yc-text-secondary);
}
.addition-label {
  font-weight: 500;
  color: var(--yc-accent);
}
.review-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--yc-text-tertiary);
  font-size: 14px;
}
</style>
