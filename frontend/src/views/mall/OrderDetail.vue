<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="order">

      <!-- 订单状态步骤条 -->
      <el-card class="section-card" style="margin-top:16px">
        <el-steps :active="stepActive" align-center finish-status="success">
          <el-step title="提交订单" />
          <el-step title="支付完成" />
          <el-step :title="step3Title" />
          <el-step :title="step4Title" />
        </el-steps>
        <div class="step-status">
          <el-tag :type="ORDER_STATUS[order.status]?.type || 'info'" size="large">
            {{ displayStatus }}
          </el-tag>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card class="section-card">
        <template #header><span>收货信息</span></template>
        <div class="info-grid">
          <div class="info-row">
            <span class="label">收货人</span>
            <span>{{ order.receiverName }}</span>
          </div>
          <div class="info-row">
            <span class="label">手机号</span>
            <span>{{ order.receiverPhone }}</span>
          </div>
          <div class="info-row">
            <span class="label">收货地址</span>
            <span>{{ order.receiverAddress }}</span>
          </div>
        </div>
      </el-card>

      <!-- 物流信息 -->
      <el-card v-if="order.logisticsNo" class="section-card">
        <template #header><span>物流信息</span></template>
        <div class="info-grid">
          <div class="info-row">
            <span class="label">物流单号</span>
            <span>{{ order.logisticsNo }}</span>
          </div>
          <div class="info-row" v-if="order.logisticsStatus">
            <span class="label">物流状态</span>
            <span>{{ order.logisticsStatus }}</span>
          </div>
        </div>
      </el-card>

      <!-- 商品清单 -->
      <el-card class="section-card">
        <template #header><span>商品清单</span></template>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <el-image
              :src="item.productImage"
              fit="cover"
              style="width:60px;height:60px;border-radius:4px"
            >
              <template #error>
                <div class="img-placeholder-xs" />
              </template>
            </el-image>
            <div class="item-info">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
            <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 金额汇总 -->
      <el-card class="section-card">
        <div class="summary">
          <div class="summary-row">
            <span>订单编号</span>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="summary-row">
            <span>下单时间</span>
            <span>{{ order.createdAt }}</span>
          </div>
          <div class="summary-row total">
            <span>合计</span>
            <span class="total-price">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <el-button
            v-if="order.status === 'PENDING_PAY'"
            class="od-pay-btn"
            size="large"
            @click="handlePay"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.status === 'PENDING_PAY'"
            class="od-cancel-btn"
            size="large"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'SHIPPED'"
            class="od-receive-btn"
            size="large"
            @click="handleReceive"
          >
            确认收货
          </el-button>
          <el-button
            v-if="order.status === 'RECEIVED' && isReviewed"
            class="od-review-btn"
            size="large"
            @click="goToProductReviews(order)"
          >
            查看评价
          </el-button>
          <el-button
            v-if="order.status === 'RECEIVED' && !isReviewed"
            class="od-review-btn"
            size="large"
            @click="router.push('/user/review/' + order.id)"
          >
            去评价
          </el-button>
        </div>
      </el-card>

      <!-- 评价弹窗 -->
      <el-dialog v-model="reviewDialogVisible" title="评价商品" width="500px" top="10vh">
        <div v-for="(item, idx) in orderItems" :key="idx" class="review-item-block">
          <div class="review-product-name">{{ item.productName }}</div>
          <div class="star-select">
            <span
              v-for="s in 5"
              :key="s"
              class="star-select-btn"
              :class="{ active: s <= reviewRating }"
              @click="reviewRating = s"
            >★</span>
            <span class="star-hint">{{ ['', '很差', '较差', '一般', '满意', '非常满意'][reviewRating] }}</span>
          </div>
          <el-input v-model="reviewContent" type="textarea" :rows="3" placeholder="说说使用感受..." maxlength="500" show-word-limit />
          <div class="review-dialog-actions">
            <el-button size="small" @click="triggerReviewUpload">📷 添加图片</el-button>
            <input ref="reviewFileInput" type="file" multiple accept="image/*" style="display:none" @change="handleReviewFileChange" />
            <span v-if="reviewImages.length > 0" class="comment-img-count">{{ reviewImages.length }}张</span>
          </div>
          <div v-if="reviewImages.length > 0" class="comment-img-preview">
            <div v-for="(img, i) in reviewImages" :key="i" class="comment-img-item">
              <el-image :src="img" fit="cover" style="width:60px;height:60px;border-radius:4px" />
              <span class="comment-img-remove" @click="reviewImages.splice(i, 1)">✕</span>
            </div>
          </div>
          <el-checkbox v-model="reviewAnonymous">匿名评价</el-checkbox>
        </div>
        <template #footer>
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
        </template>
      </el-dialog>

    </template>

    <template v-else>
      <div class="empty-tip">
        <el-empty description="订单不存在" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { getOrderDetail, payOrder, receiveOrder, cancelOrder } from '@/api/mall'
import { addReview } from '@/api/review'
import { uploadFile } from '@/api/file'
import { ORDER_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)

// 状态 → steps 激活索引
const STATUS_STEP_MAP = {
  PENDING_PAY: 0,
  PAID: 1,
  SHIPPED: 2,
  RECEIVED: 4
}

const stepActive = computed(() => {
  if (!order.value) return 0
  if (order.value.status === 'CANCELLED') return -1
  // 已发货：第3步亮绿（物流状态只影响标题，不改变步数）
  if (order.value.status === 'SHIPPED') return 2
  // 已收货：全部变绿（active >= step数）
  if (order.value.status === 'RECEIVED') return 4
  return STATUS_STEP_MAP[order.value.status] ?? 0
})

// 第3步标题：动态显示当前物流状态
const step3Title = computed(() => {
  if (!order.value) return '已发货'
  if (order.value.status === 'SHIPPED' && order.value.logisticsStatus) {
    return order.value.logisticsStatus
  }
  if (order.value.status === 'RECEIVED') return '已送达'
  return '已发货'
})

// 第4步标题
const step4Title = computed(() => {
  if (!order.value) return '确认收货'
  if (order.value.status === 'RECEIVED') return '已收货'
  return '确认收货'
})

// 显示的当前状态文字
const displayStatus = computed(() => {
  if (!order.value) return ''
  if (order.value.status === 'SHIPPED' && order.value.logisticsStatus) {
    return order.value.logisticsStatus
  }
  return ORDER_STATUS[order.value.status]?.label || order.value.status
})

async function loadDetail() {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  router.push('/mall/pay/' + order.value.id)
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示')
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    loadDetail()
  } catch {
    // 取消或失败
  }
}

async function handleReceive() {
  let confirmMsg
  if (order.value.logisticsStatus && order.value.logisticsStatus !== '已送达') {
    confirmMsg = '当前物流状态：' + order.value.logisticsStatus + '，您还未收到货，确认收货吗？'
  } else {
    confirmMsg = '请确保包装无破损、商品无误后再确认收货，确认收货吗？'
  }
  try {
    await ElMessageBox.confirm(confirmMsg, '提示')
    await receiveOrder(order.value.id)
    ElMessage.success('已确认收货')
    loadDetail()
  } catch {
    // 取消或失败
  }
}

onMounted(loadDetail)

// ===== 评价弹窗 =====
const reviewDialogVisible = ref(false)
const orderItems = ref([])
const isReviewed = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewImages = ref([])
const reviewAnonymous = ref(false)
const reviewSubmitting = ref(false)
const reviewFileInput = ref(null)

function triggerReviewUpload() { reviewFileInput.value?.click() }

async function handleReviewFileChange(e) {
  const files = e.target.files
  if (!files || files.length === 0) return
  for (const f of files) {
    try {
      const res = await uploadFile(f, 'review')
      if (res && res.url) reviewImages.value.push(res.url)
    } catch {}
  }
  e.target.value = ''
}

async function submitReview() {
  if (reviewRating.value < 1) { ElMessage.warning('请选择评分'); return }
  if (orderItems.value.length === 0) { ElMessage.warning('没有可评价的商品'); return }
  reviewSubmitting.value = true
  try {
    const item = orderItems.value[0]
    await addReview({
      orderItemId: item.id,
      rating: reviewRating.value,
      content: reviewContent.value,
      images: reviewImages.value,
      isAnonymous: reviewAnonymous.value ? 1 : 0
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    isReviewed.value = true
    reviewContent.value = ''
    reviewImages.value = []
  } catch {} finally { reviewSubmitting.value = false }
}

function goToProductReviews(order) {
  if (order.items && order.items.length > 0) {
    router.push('/mall/' + order.items[0].productId + '?tab=reviews')
  }
}

// 加载订单后获取订单项
const origLoad = loadDetail
loadDetail = async function() {
  await origLoad()
  if (order.value && order.value.items) {
    orderItems.value = order.value.items
  }
}
</script>

<style scoped>
.detail-page {
  max-width: 800px;
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

.section-card {
  margin-top: 16px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}

.step-status {
  text-align: center;
  margin-top: 20px;
}

/* steps 暖色 */
:deep(.el-step.is-success .el-step__head) {
  color: var(--yc-accent);
  border-color: var(--yc-accent);
}
:deep(.el-step.is-success .el-step__title) {
  color: var(--yc-accent);
}
:deep(.el-step.is-process .el-step__head.is-process) {
  color: var(--yc-accent);
  border-color: var(--yc-accent);
}
:deep(.el-step.is-process .el-step__title.is-process) {
  color: var(--yc-text-primary);
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.info-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: var(--yc-text-primary);
}
.info-row .label {
  width: 80px;
  color: var(--yc-text-tertiary);
  flex-shrink: 0;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
}
.order-item + .order-item {
  border-top: 1px solid var(--yc-border);
  padding-top: 12px;
}
.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.item-name {
  font-size: 14px;
  color: var(--yc-text-primary);
}
.item-price {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.item-subtotal {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}
.img-placeholder-xs {
  width: 60px;
  height: 60px;
  background: var(--yc-bg-card);
  border-radius: 4px;
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--yc-text-secondary);
}
.summary-row.total {
  font-size: 16px;
  color: var(--yc-text-primary);
  font-weight: 500;
  border-top: 1px solid var(--yc-border);
  padding-top: 8px;
}
.total-price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 按钮暖色 */
:deep(.od-pay-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.od-pay-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.od-cancel-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.od-cancel-btn:hover) {
  border-color: #f56c6c;
  color: #f56c6c;
}
:deep(.od-review-btn) {
  background: #c19a6b;
  border: 1px solid #c19a6b;
  color: #fff;
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.od-review-btn:hover) {
  background: #b0895a;
  border-color: #b0895a;
  color: #fff;
}
:deep(.od-receive-btn) {
  background: var(--yc-accent);
  border: 1px solid var(--yc-accent);
  color: #fff;
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.od-receive-btn:hover) {
  background: #7aaa92;
  border-color: #7aaa92;
}
</style>
