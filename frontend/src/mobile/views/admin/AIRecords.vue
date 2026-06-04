<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无AI记录" />
    <div v-else class="list">
      <MobileCard v-for="r in list" :key="r.id" class="item">
        <div class="row"><span class="user">{{ r.nickname || r.username || '用户' }}</span><span class="time">{{ r.createdAt?.slice(0, 16) }}</span></div>
        <p class="q">Q: {{ r.question || r.content }}</p>
        <p class="a" v-if="r.reply">A: {{ r.reply?.slice(0, 100) }}</p>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAIRecords } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const list = ref([]); const total = ref(0); const loading = ref(true); const page = ref(1)

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getAIRecords({ page: page.value, size: 10 }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
async function loadMore() { page.value++; await loadData(false) }
onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.user { font-size: 14px; font-weight: 600; color: #303133; }
.time { font-size: 12px; color: #909399; }
.q { font-size: 14px; color: #303133; line-height: 1.5; margin: 0 0 4px; }
.a { font-size: 13px; color: #409EFF; line-height: 1.5; margin: 0; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
