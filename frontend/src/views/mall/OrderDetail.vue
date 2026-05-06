<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="order">
      <el-page-header :icon="ArrowLeft" title="返回订单列表" @back="$router.push('/user/orders')" />

      <!-- 订单状态步骤条 -->
      <el-card class="section-card" style="margin-top:16px">
        <el-steps :active="stepActive" align-center finish-status="success">
          <el-step title="提交订单" />
          <el-step title="支付完成" />
          <el-step title="已发货" />
          <el-step title="确认收货" />
        </el-steps>
        <div class="step-status">
          <el-tag :type="ORDER_STATUS[order.status]?.type || 'info'" size="large">
            {{ ORDER_STATUS[order.status]?.label || order.status }}
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
            type="primary"
            size="large"
            @click="handlePay"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.status === 'PENDING_PAY'"
            size="large"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'SHIPPED'"
            type="success"
            size="large"
            @click="handleReceive"
          >
            确认收货
          </el-button>
        </div>
      </el-card>
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
  RECEIVED: 3
}

const stepActive = computed(() => {
  if (!order.value) return 0
  if (order.value.status === 'CANCELLED') return -1
  return STATUS_STEP_MAP[order.value.status] ?? 0
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
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示')
    await receiveOrder(order.value.id)
    ElMessage.success('已确认收货')
    loadDetail()
  } catch {
    // 取消或失败
  }
}

onMounted(loadDetail)
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
}

.step-status {
  text-align: center;
  margin-top: 20px;
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
  color: #303133;
}
.info-row .label {
  width: 80px;
  color: #909399;
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
  border-top: 1px solid #f5f7fa;
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
  color: #303133;
}
.item-price {
  font-size: 12px;
  color: #909399;
}
.item-subtotal {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}
.img-placeholder-xs {
  width: 60px;
  height: 60px;
  background: #f5f7fa;
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
  color: #606266;
}
.summary-row.total {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  border-top: 1px solid #ebeef5;
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
</style>
