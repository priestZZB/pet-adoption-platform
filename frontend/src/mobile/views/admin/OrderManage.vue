<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无订单" />
    <div v-else class="list">
      <MobileCard v-for="o in list" :key="o.id" class="item">
        <div class="row"><span class="no">{{ o.orderNo }}</span><span class="status">{{ o.status }}</span></div>
        <div class="meta">¥{{ o.totalAmount }} | {{ o.contact }} | {{ o.createdAt?.slice(0, 10) }}</div>
        <div class="acts" v-if="o.status === '待发货'"><button @click="handleShip(o)">发货</button></div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
    <MobileDialog v-model="shipVisible" title="发货">
      <div class="form-group"><label>物流单号</label><el-input v-model="trackingNo" placeholder="输入快递单号" /></div>
      <template #footer><button class="save-btn" @click="confirmShip">确认发货</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllOrders, shipOrder } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const total = ref(0); const loading = ref(true); const page = ref(1)
const shipVisible = ref(false); const shipId = ref(null); const trackingNo = ref('')

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getAllOrders({ page: page.value, size: 10 }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
async function loadMore() { page.value++; await loadData(false) }
function handleShip(o) { shipId.value = o.id; trackingNo.value = ''; shipVisible.value = true }
async function confirmShip() {
  try { await shipOrder(shipId.value, { trackingNo: trackingNo.value }); ElMessage.success('已发货'); shipVisible.value = false; loadData(true) } catch {}
}
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.no { font-size: 14px; font-weight: 600; color: #303133; }
.status { font-size: 12px; color: #409EFF; }
.meta { font-size: 12px; color: #909399; margin-bottom: 6px; }
.acts button { height: 28px; padding: 0 12px; border: 1px solid #409EFF; border-radius: 6px; background: #fff; color: #409EFF; font-size: 12px; cursor: pointer; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #409EFF; color: #fff; font-size: 15px; cursor: pointer; }
</style>
