<template>
  <div class="pay-page">
    <div v-if="loading" class="loading-center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <template v-else-if="order">
      <el-card class="pay-card">
        <div class="pay-header">
          <el-icon :size="48" color="#67C23A"><CircleCheck /></el-icon>
          <h2>订单提交成功</h2>
          <p class="order-no">订单编号：{{ order.orderNo }}</p>
        </div>

        <el-divider />

        <div class="pay-amount">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ order.totalAmount }}</span>
        </div>

        <el-divider />

        <div class="pay-info">
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

        <el-divider />

        <div class="pay-action">
          <el-button
            type="primary"
            size="large"
            :loading="paying"
            @click="handlePay"
          >
            模拟支付
          </el-button>
          <el-button size="large" @click="goOrderDetail">
            查看订单详情
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck } from '@element-plus/icons-vue'
import { getOrderDetail, payOrder } from '@/api/mall'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const paying = ref(false)

async function loadOrder() {
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
  paying.value = true
  try {
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    router.replace('/user/orders/' + order.value.id)
  } catch {
    // 请求拦截器统一处理
  } finally {
    paying.value = false
  }
}

function goOrderDetail() {
  router.push('/user/orders/' + order.value.id)
}

onMounted(loadOrder)
</script>

<style scoped>
.pay-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 0;
}
.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.empty-tip {
  padding: 60px 0;
}

.pay-card {
  text-align: center;
}
.pay-header {
  padding: 20px 0 10px;
}
.pay-header h2 {
  margin: 12px 0 8px;
  font-size: 20px;
  color: #303133;
}
.order-no {
  font-size: 14px;
  color: #909399;
}

.pay-amount {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 12px;
  padding: 10px 0;
}
.pay-amount .label {
  font-size: 14px;
  color: #909399;
}
.pay-amount .amount {
  font-size: 28px;
  font-weight: bold;
  color: #F56C6C;
}

.pay-info {
  text-align: left;
  padding: 0 20px;
}
.info-row {
  display: flex;
  gap: 12px;
  padding: 6px 0;
  font-size: 14px;
  color: #303133;
}
.info-row .label {
  width: 70px;
  color: #909399;
  flex-shrink: 0;
}

.pay-action {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-bottom: 10px;
}
</style>
