<template>
  <div class="cart-page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="cartList.length === 0" icon="fas fa-shopping-cart" description="购物车是空的" action-label="去商城" @action="$router.push('/mall')" />
    <template v-else>
      <MobileCard v-for="item in cartList" :key="item.id" class="cart-item">
        <div class="cart-row">
          <img :src="item.productImage || item.coverImage" class="cart-img" />
          <div class="cart-info">
            <h4>{{ item.productName }}</h4>
            <span class="cart-price">¥{{ item.price }}</span>
          </div>
          <div class="qty-ctrl">
            <button :disabled="item.quantity <= 1" @click="changeQty(item, item.quantity - 1)">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="changeQty(item, item.quantity + 1)">+</button>
          </div>
        </div>
      </MobileCard>

      <div class="bottom-bar">
        <div class="bar-total">合计 <span class="total-price">¥{{ totalPrice }}</span></div>
        <button class="btn-checkout" @click="goCheckout">去结算 ({{ totalCount }})</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCart, updateCartItem, removeCartItem } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const cartList = ref([])
const loading = ref(true)

const totalPrice = computed(() => cartList.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2))
const totalCount = computed(() => cartList.value.reduce((s, i) => s + i.quantity, 0))

async function loadCart() { loading.value = true; try { cartList.value = await getCart() } catch {} finally { loading.value = false } }

async function changeQty(item, qty) {
  if (qty < 1) { try { await removeCartItem(item.id); cartList.value = cartList.value.filter(i => i.id !== item.id) } catch {} return }
  try { await updateCartItem(item.id, qty); item.quantity = qty } catch {}
}

function goCheckout() { router.push('/mall/checkout') }
onMounted(loadCart)
</script>

<style scoped>
.cart-page { padding: 12px 16px 100px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.cart-item { margin-bottom: 10px; }
.cart-item :deep(.card-body) { padding: 12px; }
.cart-row { display: flex; gap: 10px; align-items: center; }
.cart-img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.cart-info { flex: 1; min-width: 0; }
.cart-info h4 { font-size: 14px; font-weight: 600; color: #5a4a42; margin: 0 0 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cart-price { font-size: 15px; font-weight: 600; color: #e8564a; }
.qty-ctrl { display: flex; align-items: center; gap: 0; flex-shrink: 0; }
.qty-ctrl button { width: 30px; height: 30px; border: 1px solid #d1e7dd; background: #fefaf5; color: #5a4a42; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.qty-ctrl button:first-child { border-radius: 6px 0 0 6px; }
.qty-ctrl button:last-child { border-radius: 0 6px 6px 0; }
.qty-ctrl button:active { background: #f0e8dc; }
.qty-ctrl button:disabled { color: #c0c4cc; }
.qty-ctrl span { width: 36px; text-align: center; font-size: 14px; color: #5a4a42; font-weight: 500; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; display: flex; align-items: center; gap: 12px; padding: 10px 16px; padding-bottom: calc(10px + env(safe-area-inset-bottom, 0)); background: rgba(254,250,245,0.97); backdrop-filter: blur(8px); border-top: 1px solid #ece4d8; }
.bar-total { flex: 1; font-size: 14px; color: #5a4a42; }
.total-price { font-size: 20px; font-weight: 700; color: #e8564a; }
.btn-checkout { height: 42px; padding: 0 24px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
</style>
