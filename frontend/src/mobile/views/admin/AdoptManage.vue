<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无领养申请" />
    <div v-else class="list">
      <MobileCard v-for="a in list" :key="a.id" class="item" clickable @click="goDetail(a.id)">
        <div class="row"><span class="name">{{ a.petName || '-' }}</span><span :class="['status', statClass(a.status)]">{{ a.status }}</span></div>
        <div class="meta">申请人：{{ a.applicantName }} | {{ a.createdAt?.slice(0, 10) }}</div>
      </MobileCard>
    </div>
    <div class="more" v-if="list.length < total"><button @click="loadMore">加载更多</button></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllApplications } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const total = ref(0); const loading = ref(true); const page = ref(1)

async function loadData(reset = false) {
  if (reset) { page.value = 1; loading.value = true }
  try { const res = await getAllApplications({ page: page.value, size: 10 }); if (reset) list.value = res.records || []; else list.value.push(...(res.records || [])); total.value = res.total || 0 } catch {} finally { loading.value = false }
}
async function loadMore() { page.value++; await loadData(false) }
function goDetail(id) { router.push('/admin/review/' + id) }
function statClass(s) { const m = { '待审核': 'warn', '已通过': 'ok', '已拒绝': 'err' }; return m[s] || '' }

onMounted(() => loadData(true))
</script>

<style scoped>
.page { padding: 12px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.status.warn { background: rgba(230,162,60,0.1); color: #e6a23c; }
.status.ok { background: rgba(103,194,58,0.1); color: #67c23a; }
.status.err { background: rgba(245,108,108,0.1); color: #e8564a; }
.meta { font-size: 12px; color: #909399; }
.more { text-align: center; padding: 16px 0; }
.more button { height: 36px; padding: 0 24px; border: 1px solid #dcdfe6; border-radius: 8px; background: #fff; color: #606266; cursor: pointer; }
</style>
