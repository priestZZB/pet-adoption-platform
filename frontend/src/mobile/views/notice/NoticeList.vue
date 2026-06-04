<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-bullhorn" description="暂无公告" />
    <div v-else class="list">
      <MobileCard v-for="n in list" :key="n.id" class="item" clickable @click="goDetail(n.id)">
        <h4>{{ n.title }}</h4>
        <p class="nc-preview">{{ n.content?.replace(/<[^>]+>/g, '').slice(0, 80) || '' }}</p>
        <span class="nc-time">{{ formatDate(n.createdAt) }}</span>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticeList } from '@/api/notice'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const loading = ref(true)

onMounted(async () => { try { list.value = await getNoticeList() } catch {} finally { loading.value = false } })
function goDetail(id) { router.push('/notices/' + id) }
function formatDate(s) { if (!s) return ''; const d = new Date(s); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 10px; }
.item :deep(h4) { font-size: 15px; font-weight: 600; color: #5a4a42; margin: 0 0 6px; }
.nc-preview { font-size: 13px; color: #a09080; line-height: 1.4; margin: 0 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nc-time { font-size: 12px; color: #b5a898; }
</style>
