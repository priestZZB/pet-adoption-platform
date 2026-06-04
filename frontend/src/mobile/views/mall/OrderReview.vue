<template>
  <div class="review-page">
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title">商品评价</div>
        <div class="prod-info" v-if="product">
          <img :src="product.coverImage" class="prod-img" />
          <span>{{ product.name }}</span>
        </div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <MobileCard>
        <div class="form-group">
          <label>评分</label>
          <div class="star-row">
            <button v-for="s in 5" :key="s" class="star" :class="{ active: rating >= s }" @click="rating = s">
              <i :class="rating >= s ? 'fas fa-star' : 'far fa-star'"></i>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>评价内容</label>
          <textarea v-model="content" placeholder="分享你的使用体验..." rows="3"></textarea>
        </div>
        <button class="submit-btn" :disabled="!rating || submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交评价' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addReview } from '@/api/review'
import { getMallProductDetail } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const product = ref(null)
const rating = ref(0)
const content = ref('')
const submitting = ref(false)

import { onMounted } from 'vue'
onMounted(async () => {
  try { product.value = await getMallProductDetail(route.params.id) } catch {}
})

async function handleSubmit() {
  if (!rating.value) { ElMessage.warning('请评分'); return }
  submitting.value = true
  try {
    await addReview({ productId: Number(route.params.id), rating: rating.value, content: content.value })
    ElMessage.success('评价成功')
    router.back()
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.review-page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 15px; font-weight: 600; color: #5a4a42; margin-bottom: 12px; }
.prod-info { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #5a4a42; }
.prod-img { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; color: #5a4a42; margin-bottom: 8px; }
.star-row { display: flex; gap: 4px; }
.star { border: none; background: none; font-size: 28px; cursor: pointer; color: #d1e7dd; padding: 0; }
.star.active { color: #e6a23c; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
textarea:focus { border-color: #8ab8a0; }
.submit-btn { width: 100%; height: 46px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.submit-btn:active { transform: scale(0.98); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
</style>
