<template>
  <div class="orders-page">
    <!-- Tab 切换 -->
    <div class="tab-row">
      <button v-for="t in tabs" :key="t.key" :class="['tab', { active: activeTab === t.key }]" @click="switchTab(t.key)">{{ t.label }}</button>
    </div>

    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="orders.length === 0" icon="fas fa-receipt" description="暂无订单" />

    <div v-else class="order-list">
      <MobileCard v-for="o in orders" :key="o.id" class="order-item" clickable @click="goDetail(o.id)">
        <div class="order-header">
          <span class="order-no">订单号：{{ o.orderNo }}</span>
          <span class="order-status">{{ o.status }}</span>
        </div>
        <div class="order-products">
          <img v-for="(img, idx) in (o.items || []).slice(0, 3)" :key="idx" :src="img.productImage" class="order-img" />
        </div>
        <div class="order-footer">
          <span class="order-total">合计 ¥{{ o.totalAmount }}</span>
          <div class="order-actions">
            <button v-if="o.status === '待付款'" class="act-btn primary" @click.stop="goPay(o.id)">付款</button>
            <button v-if="o.status === '待收货'" class="act-btn primary" @click.stop="handleReceive(o.id)">确认收货</button>
            <button v-if="o.status === '待付款'" class="act-btn" @click.stop="handleCancel(o.id)">取消</button>
          </div>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOrders, payOrder, receiveOrder, cancelOrder } from '@/api/mall'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const orders = ref([]); const loading = ref(true); const activeTab = ref('')

const tabs = [
  { key: '', label: '全部' },
  { key: '待付款', label: '待付款' },
  { key: '待发货', label: '待发货' },
  { key: '待收货', label: '待收货' },
  { key: '已完成', label: '已完成' },
]

async function loadOrders() {
  loading.value = true
  try { orders.value = await getMyOrders({ status: activeTab.value || undefined }) } catch {} finally { loading.value = false }
}

function switchTab(k) { activeTab.value = k; loadOrders() }

function goDetail(id) { router.push('/user/orders/' + id) }
function goPay(id) { router.push('/mall/pay/' + id) }

async function handleReceive(id) {
  try { await ElMessageBox.confirm('确认已收到商品？', '确认收货'); await receiveOrder(id); ElMessage.success('已确认收货'); loadOrders() } catch {}
}

async function handleCancel(id) {
  try { await ElMessageBox.confirm('确定取消该订单？', '取消订单'); await cancelOrder(id); ElMessage.success('已取消'); loadOrders() } catch {}
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { padding: 12px 16px 80px; }
.tab-row { display: flex; gap: 0; margin-bottom: 12px; overflow-x: auto; -webkit-overflow-scrolling: touch; }
.tab-row::-webkit-scrollbar { display: none; }
.tab { flex-shrink: 0; height: 34px; padding: 0 14px; border: none; background: none; color: #a09080; font-size: 14px; cursor: pointer; position: relative; }
.tab.active { color: #5a4a42; font-weight: 600; }
.tab.active::after { content: ''; position: absolute; bottom: 0; left: 14px; right: 14px; height: 2px; background: #8ab8a0; border-radius: 1px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.order-item { margin-bottom: 10px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-no { font-size: 12px; color: #a09080; }
.order-status { font-size: 12px; color: #8ab8a0; font-weight: 500; }
.order-products { display: flex; gap: 6px; margin-bottom: 8px; }
.order-img { width: 56px; height: 56px; border-radius: 6px; object-fit: cover; }
.order-footer { display: flex; justify-content: space-between; align-items: center; }
.order-total { font-size: 14px; font-weight: 600; color: #5a4a42; }
.order-actions { display: flex; gap: 8px; }
.act-btn { height: 30px; padding: 0 12px; border: 1px solid #d1e7dd; border-radius: 15px; background: #fefaf5; color: #5a4a42; font-size: 12px; cursor: pointer; }
.act-btn.primary { background: #8ab8a0; border-color: #8ab8a0; color: #fff; }
</style>
