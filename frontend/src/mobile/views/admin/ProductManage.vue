<template>
  <div class="page">
    <div class="search-bar"><el-input v-model="keyword" placeholder="搜索商品" clearable @keyup.enter="search" @clear="search" /></div>
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无商品" />
    <div v-else class="list">
      <MobileCard v-for="p in list" :key="p.id" class="item">
        <div class="prod-row"><img :src="p.coverImage" class="prod-img" /><div class="info"><div class="name">{{ p.name }}</div><div class="meta">¥{{ p.price }} | {{ p.status || '上架' }}</div></div></div>
        <div class="acts"><button @click="toggleStatus(p)">{{ p.status === '已上架' ? '下架' : '上架' }}</button></div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminProducts, toggleProductStatus } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const list = ref([]); const total = ref(0); const loading = ref(true); const keyword = ref(''); const page = ref(1)

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getAdminProducts({ page: page.value, size: 10, keyword: keyword.value }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
function search() { loadData(true) }
async function loadMore() { page.value++; await loadData(false) }
async function toggleStatus(p) { try { await toggleProductStatus(p.id); ElMessage.success('已更新'); loadData(true) } catch {} }
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px; }
.search-bar { margin-bottom: 10px; }
.search-bar :deep(.el-input__wrapper) { height: 40px; border-radius: 8px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.prod-row { display: flex; gap: 10px; align-items: center; margin-bottom: 8px; }
.prod-img { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.meta { font-size: 12px; color: #909399; }
.acts button { height: 28px; padding: 0 12px; border: 1px solid #dcdfe6; border-radius: 6px; background: #fff; font-size: 12px; cursor: pointer; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
