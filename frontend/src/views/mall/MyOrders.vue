<template>
  <div class="orders-page">
    <h3 class="page-title">我的订单</h3>

    <!-- 状态 Tab -->
    <el-tabs v-model="status" @tab-change="loadOrders">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="PENDING_PAY" />
      <el-tab-pane label="已支付" name="PAID" />
      <el-tab-pane label="已发货" name="SHIPPED" />
      <el-tab-pane label="已完成" name="RECEIVED" />
    </el-tabs>

    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else>
      <div v-if="orderList.length === 0" class="empty-tip">
        <el-empty description="暂无订单" />
      </div>

      <div v-else class="order-list">
        <el-card v-for="order in orderList" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="ORDER_STATUS[order.status]?.type || 'info'" size="small">
              {{ ORDER_STATUS[order.status]?.label || order.status }}
            </el-tag>
          </div>

          <el-divider style="margin:10px 0" />

          <div
            v-for="item in order.items"
            :key="item.id"
            class="order-item"
          >
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

          <el-divider style="margin:10px 0" />

          <div v-if="order.status === 'SHIPPED'" class="logistics-row">
            <el-icon :size="16" style="margin-right:4px;color:#409EFF"><Van /></el-icon>
            <span class="logistics-text">
              本次快递由【<b>{{ order.courierCompany || '快递公司' }}</b>】为您配送
              <span v-if="order.logisticsNo" style="margin-left:4px;color:#909399">({{ order.logisticsNo }})</span>
            </span>
          </div>
          <div v-if="order.status === 'SHIPPED'" class="logistics-row" style="margin-top:4px">
            <span style="margin-left:20px;font-size:13px;color:#606266">
              物流状态：<b style="color:#409EFF">{{ order.logisticsStatus || '已发货' }}</b>
            </span>
          </div>

          <el-divider style="margin:10px 0" />

          <div class="order-footer">
            <span class="order-total">
              共 {{ order.items?.length || 0 }} 件 · 合计
              <b class="total-price">¥{{ order.totalAmount }}</b>
            </span>
            <div class="order-actions">
              <el-button
                v-if="order.status === 'PENDING_PAY'"
                class="order-pay-btn"
                size="small"
                @click="handlePay(order)"
              >
                去支付
              </el-button>
              <el-button
                v-if="order.status === 'PENDING_PAY'"
                class="order-cancel-btn"
                size="small"
                @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.status === 'SHIPPED'"
                class="order-receive-btn"
                size="small"
                @click="handleReceive(order)"
              >
                确认收货
              </el-button>
              <el-button
                class="order-detail-link"
                size="small"
                @click="goDetail(order.id)"
              >
                订单详情
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Van } from '@element-plus/icons-vue'
import { getMyOrders, payOrder, receiveOrder, cancelOrder } from '@/api/mall'
import { ORDER_STATUS } from '@/utils/constants'

const router = useRouter()
const orderList = ref([])
const loading = ref(true)
const status = ref('')

async function loadOrders() {
  loading.value = true
  try {
    const params = {}
    if (status.value) params.status = status.value
    orderList.value = await getMyOrders(params)
  } catch {
    orderList.value = []
  } finally {
    loading.value = false
  }
}

async function handlePay(order) {
  router.push('/mall/pay/' + order.id)
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示')
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {
    // 取消或失败
  }
}

async function handleReceive(order) {
  // 包裹未送达时弹出提示
  if (order.logisticsStatus && order.logisticsStatus !== '已送达') {
    try {
      await ElMessageBox.confirm(
        '您的包裹还未送达，确定要确认收货吗？',
        '确认收货',
        {
          confirmButtonText: '确认收货',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return // 用户点了取消
    }
  } else {
    try {
      await ElMessageBox.confirm('确认已收到商品？', '提示')
    } catch {
      return
    }
  }
  try {
    await receiveOrder(order.id)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch {
    // 失败由拦截器处理
  }
}

function goDetail(id) {
  router.push('/user/orders/' + id)
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.order-card {
  margin-bottom: 16px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-no {
  font-size: 13px;
  color: var(--yc-text-tertiary);
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
}
.order-item + .order-item {
  border-top: 1px solid var(--yc-border);
  margin-top: 6px;
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

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-total {
  font-size: 13px;
  color: var(--yc-text-secondary);
}
.total-price {
  font-size: 16px;
  color: #F56C6C;
}
.order-actions {
  display: flex;
  gap: 8px;
}

.logistics-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--yc-text-secondary);
}
.logistics-text b {
  color: var(--yc-accent);
}

/* Tabs 暖色 */
:deep(.el-tabs__active-bar) {
  background: var(--yc-accent);
}
:deep(.el-tabs__item.is-active) {
  color: var(--yc-text-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--yc-accent);
}

/* 按钮暖色 */
:deep(.order-pay-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.order-pay-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.order-cancel-btn) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.order-cancel-btn:hover) {
  border-color: #f56c6c;
  color: #f56c6c;
}
:deep(.order-receive-btn) {
  background: var(--yc-accent);
  border: 1px solid var(--yc-accent);
  color: #fff;
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.order-receive-btn:hover) {
  background: #7aaa92;
  border-color: #7aaa92;
}
:deep(.order-detail-link) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-btn);
  color: var(--yc-text-primary);
}
:deep(.order-detail-link:hover) {
  border-color: var(--yc-border-hover);
  color: var(--yc-accent);
}
</style>
