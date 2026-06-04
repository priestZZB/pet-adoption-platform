<template>
  <div class="pay-page">
    <div class="card-wrap">
      <MobileCard>
        <div class="pay-title">订单金额</div>
        <div class="pay-amount">¥{{ order?.totalAmount || '0.00' }}</div>
        <div class="pay-info">订单号：{{ order?.orderNo || '-' }}</div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <MobileCard>
        <div class="pay-title">支付方式</div>
        <div class="pay-option active">
          <i class="fas fa-check-circle"></i>
          <span>模拟支付</span>
        </div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <button class="pay-btn" :disabled="paying" @click="handlePay">{{ paying ? '支付中...' : '确认支付' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, payOrder } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const order = ref(null); const paying = ref(false)

onMounted(async () => { try { order.value = await getOrderDetail(route.params.id) } catch {} })

async function handlePay() {
  paying.value = true
  try { await payOrder(order.value.id); ElMessage.success('支付成功'); router.push('/user/orders/' + order.value.id) } catch {} finally { paying.value = false }
}
</script>

<style scoped>
.pay-page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.pay-title { font-size: 14px; font-weight: 600; color: #5a4a42; margin-bottom: 8px; }
.pay-amount { font-size: 32px; font-weight: 700; color: #e8564a; text-align: center; padding: 16px 0; }
.pay-info { font-size: 13px; color: #a09080; text-align: center; }
.pay-option { display: flex; align-items: center; gap: 8px; padding: 12px 0; font-size: 15px; color: #5a4a42; }
.pay-option i { color: #8ab8a0; font-size: 18px; }
.pay-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.pay-btn:active { transform: scale(0.98); }
.pay-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
</style>
