<template>
  <div class="order-detail-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="order">
      <div class="card-wrap">
        <MobileCard>
          <div class="status-bar">{{ order.status }}</div>
          <div class="info-line"><span>订单号</span><span>{{ order.orderNo }}</span></div>
          <div class="info-line"><span>创建时间</span><span>{{ order.createdAt }}</span></div>
        </MobileCard>
      </div>

      <div class="card-wrap">
        <MobileCard>
          <div class="section-title">商品信息</div>
          <div v-for="item in (order.items || [])" :key="item.id" class="prod-row">
            <img :src="item.productImage" class="prod-img" />
            <div class="prod-info">
              <span class="prod-name">{{ item.productName }}</span>
              <span class="prod-price">¥{{ item.price }} × {{ item.quantity }}</span>
            </div>
          </div>
        </MobileCard>
      </div>

      <div class="card-wrap">
        <MobileCard>
          <div class="info-line"><span>收货人</span><span>{{ order.contact }}</span></div>
          <div class="info-line"><span>电话</span><span>{{ order.phone }}</span></div>
          <div class="info-line"><span>地址</span><span>{{ order.address }}</span></div>
        </MobileCard>
      </div>

      <div class="card-wrap">
        <MobileCard>
          <div class="total-row">实付金额 <span class="total-price">¥{{ order.totalAmount }}</span></div>
          <div class="action-row" v-if="order.status === '待付款'">
            <button class="btn-primary" @click="goPay">去支付</button>
          </div>
        </MobileCard>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const order = ref(null); const loading = ref(true)

onMounted(async () => { try { order.value = await getOrderDetail(route.params.id) } catch {} finally { loading.value = false } })

function goPay() { router.push('/mall/pay/' + order.value.id) }
</script>

<style scoped>
.order-detail-page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.status-bar { font-size: 16px; font-weight: 600; color: #8ab8a0; margin-bottom: 12px; }
.section-title { font-size: 14px; font-weight: 600; color: #5a4a42; margin-bottom: 10px; }
.info-line { display: flex; justify-content: space-between; align-items: flex-start; padding: 6px 0; font-size: 14px; }
.info-line span:first-child { color: #a09080; flex-shrink: 0; }
.info-line span:last-child { color: #5a4a42; text-align: right; word-break: break-all; }
.prod-row { display: flex; gap: 10px; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f0e8; }
.prod-row:last-child { border-bottom: none; }
.prod-img { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; }
.prod-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.prod-name { font-size: 14px; color: #5a4a42; }
.prod-price { font-size: 13px; color: #a09080; }
.total-row { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #5a4a42; }
.total-price { font-size: 22px; color: #e8564a; }
.action-row { margin-top: 16px; }
.btn-primary { width: 100%; height: 44px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
</style>
