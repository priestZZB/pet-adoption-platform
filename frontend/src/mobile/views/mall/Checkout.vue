<template>
  <div class="checkout-page">
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title"><i class="fas fa-map-marker-alt"></i> 收货地址</div>
        <div v-if="address" class="address-info" @click="selectAddress">
          <div class="addr-top">{{ address.contact }} {{ address.phone }}</div>
          <div class="addr-detail">{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detail }}</div>
        </div>
        <div v-else class="no-addr" @click="selectAddress">请选择收货地址 ›</div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <MobileCard>
        <div class="section-title"><i class="fas fa-box"></i> 商品清单</div>
        <div v-for="item in items" :key="item.id" class="order-item">
          <img :src="item.productImage || item.coverImage" class="item-img" />
          <div class="item-info">
            <span class="item-name">{{ item.productName }}</span>
            <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
          </div>
        </div>
      </MobileCard>
    </div>

    <div class="card-wrap">
      <MobileCard>
        <div class="total-row">合计 <span class="total-price">¥{{ totalPrice }}</span></div>
        <button class="submit-btn" :disabled="!address || submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交订单' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCart, createOrder } from '@/api/mall'
import { getDefaultAddress } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'

const router = useRouter()
const address = ref(null)
const items = ref([])
const submitting = ref(false)

const totalPrice = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2))

onMounted(async () => {
  try { address.value = await getDefaultAddress() } catch {}
  try { items.value = await getCart() } catch {}
})

function selectAddress() { router.push('/mall/address-select') }

async function handleSubmit() {
  if (!address.value) { ElMessage.warning('请选择收货地址'); return }
  submitting.value = true
  try {
    const r = await createOrder({ addressId: address.value.id })
    router.push('/mall/pay/' + r.id)
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.checkout-page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 15px; font-weight: 600; color: #5a4a42; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }
.address-info { cursor: pointer; }
.addr-top { font-size: 15px; font-weight: 500; color: #5a4a42; margin-bottom: 4px; }
.addr-detail { font-size: 13px; color: #a09080; line-height: 1.4; }
.no-addr { font-size: 14px; color: #8ab8a0; cursor: pointer; }
.order-item { display: flex; gap: 10px; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f0e8; }
.order-item:last-child { border-bottom: none; }
.item-img { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.item-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.item-name { font-size: 14px; color: #5a4a42; }
.item-price { font-size: 13px; color: #a09080; }

.total-row { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #5a4a42; margin-bottom: 16px; }
.total-price { font-size: 22px; color: #e8564a; }
.submit-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.submit-btn:active { transform: scale(0.98); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
</style>
