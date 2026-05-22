<template>
  <div class="review-page">

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="order">
      <el-card class="section-card">
        <template #header>
          <span>订单信息</span>
        </template>
        <div class="order-info">
          <div class="info-row"><span class="label">订单号</span><span>{{ order.orderNo }}</span></div>
          <div class="info-row"><span class="label">下单时间</span><span>{{ order.createdAt }}</span></div>
          <div class="info-row"><span class="label">收货地址</span><span>{{ order.receiverAddress }}</span></div>
        </div>
      </el-card>

      <el-card class="section-card" v-for="(item, idx) in orderItems" :key="idx">
        <template #header>
          <span>商品 {{ idx + 1 }}</span>
          <el-tag v-if="item._reviewed" type="success" size="small" style="float:right">已评价</el-tag>
        </template>
        <div class="product-info">
          <el-image v-if="item.productImage" :src="item.productImage" fit="cover" style="width:80px;height:80px;border-radius:6px;flex-shrink:0" />
          <div class="product-detail">
            <div class="product-name">{{ item.productName }}</div>
            <div class="product-meta">
              <span>¥{{ item.price }} × {{ item.quantity }}</span>
              <span class="product-total">小计：¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="section-card">
        <template #header><span>商品评价</span></template>

        <div class="review-form">
          <div class="form-label">整体评分</div>
          <div class="star-select">
            <span v-for="s in 5" :key="s" class="star-btn" :class="{ active: s <= (hoverRating || rating) }" @click="rating = s" @mouseenter="hoverRating = s" @mouseleave="hoverRating = 0">★</span>
            <span class="star-hint">{{ ['', '很差', '较差', '一般', '满意', '非常满意'][hoverRating || rating] }}</span>
          </div>

          <div class="form-label">评价标签（可多选）</div>
          <div class="tag-select">
            <el-tag v-for="t in allTags" :key="t" :type="selectedTags.includes(t) ? 'primary' : 'info'" class="tag-option" @click="toggleTag(t)">{{ t }}</el-tag>
          </div>

          <div class="form-label">文字评价</div>
          <el-input v-model="content" type="textarea" :rows="4" placeholder="说说使用感受，分享给其他小伙伴..." maxlength="500" show-word-limit />

          <div class="form-label">晒图（最多9张）</div>
          <div class="upload-area">
            <el-button size="small" @click="triggerUpload">+ 添加图片</el-button>
            <input ref="fileInput" type="file" multiple accept="image/*" style="display:none" @change="handleFileChange" />
          </div>
          <div v-if="images.length > 0" class="img-preview">
            <div v-for="(img, i) in images" :key="i" class="img-item">
              <el-image :src="img" fit="cover" style="width:64px;height:64px;border-radius:6px" />
              <span class="img-remove" @click="images.splice(i, 1)">✕</span>
            </div>
          </div>

          <el-checkbox v-model="isAnonymous" style="margin-top:12px">匿名评价</el-checkbox>

          <el-divider />

          <div class="form-actions">
            <el-button size="large" @click="$router.push('/user/orders')">取消</el-button>
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交评价</el-button>
          </div>
        </div>
      </el-card>
    </template>

    <template v-else>
      <el-empty description="订单不存在" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/mall'
import { addReview } from '@/api/review'
import { uploadFile } from '@/api/file'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const order = ref(null)
const orderItems = ref([])
const rating = ref(5)
const hoverRating = ref(0)
const content = ref('')
const images = ref([])
const selectedTags = ref([])
const isAnonymous = ref(false)
const submitting = ref(false)
const fileInput = ref(null)

const allTags = ['质量好', '性价比高', '味道好', '包装好', '推荐购买', '物流快', '分量足', '会回购']

function toggleTag(t) {
  const idx = selectedTags.value.indexOf(t)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else if (selectedTags.value.length < 3) selectedTags.value.push(t)
}

function triggerUpload() { fileInput.value?.click() }

async function handleFileChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  for (const f of files) {
    try {
      const res = await uploadFile(f, 'review')
      if (res && res.url) images.value.push(res.url)
    } catch {}
  }
  e.target.value = ''
}

async function handleSubmit() {
  if (orderItems.value.length === 0) { ElMessage.warning('没有可评价的商品'); return }
  submitting.value = true
  try {
    const item = orderItems.value[0]
    await addReview({
      orderItemId: item.id,
      rating: rating.value,
      content: content.value,
      images: images.value,
      tags: selectedTags.value,
      isAnonymous: isAnonymous.value ? 1 : 0
    })
    ElMessage.success('评价成功')
    router.push('/user/orders')
  } catch {} finally { submitting.value = false }
}

onMounted(async () => {
  try {
    order.value = await getOrderDetail(route.params.id)
    if (order.value && order.value.items) {
      orderItems.value = order.value.items
    }
  } catch { order.value = null }
  finally { loading.value = false }
})
</script>

<style scoped>
.review-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 60px;
}
.loading-center { text-align: center; padding: 80px 0; }
.section-card { margin-top: 16px; }
.order-info { font-size: 14px; color: var(--yc-text-primary); }
.info-row { display: flex; gap: 16px; margin-bottom: 6px; }
.info-row .label { color: var(--yc-text-tertiary); width: 70px; flex-shrink: 0; }
.product-info { display: flex; gap: 14px; }
.product-detail { display: flex; flex-direction: column; justify-content: center; gap: 4px; }
.product-name { font-size: 15px; font-weight: 500; color: var(--yc-text-primary); }
.product-meta { font-size: 13px; color: var(--yc-text-tertiary); }
.product-total { margin-left: 12px; color: #F56C6C; font-weight: 500; }
.review-form { font-size: 14px; }
.form-label { font-size: 13px; color: var(--yc-text-secondary); margin-bottom: 6px; margin-top: 14px; }
.star-select { display: flex; align-items: center; gap: 4px; margin-bottom: 4px; }
.star-btn { font-size: 30px; color: #ddd; cursor: pointer; transition: color 0.15s; }
.star-btn.active { color: #f0c040; }
.star-btn:hover { color: #f0c040; }
.star-hint { margin-left: 8px; font-size: 13px; color: var(--yc-text-tertiary); }
.tag-select { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-option { cursor: pointer; }
.upload-area { margin-bottom: 8px; }
.img-preview { display: flex; gap: 8px; flex-wrap: wrap; }
.img-item { position: relative; }
.img-remove { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: #F56C6C; color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; }
</style>
