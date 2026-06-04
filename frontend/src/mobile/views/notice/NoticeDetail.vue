<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="notice">
      <div class="card-wrap">
        <MobileCard>
          <h2 class="title">{{ notice.title }}</h2>
          <div class="meta">发布于 {{ formatDate(notice.createdAt) }}</div>
          <div class="content" v-html="notice.content"></div>
        </MobileCard>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNoticeDetail } from '@/api/notice'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute()
const notice = ref(null); const loading = ref(true)

onMounted(async () => { try { notice.value = await getNoticeDetail(route.params.id) } catch {} finally { loading.value = false } })
function formatDate(s) { if (!s) return ''; const d = new Date(s); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` }
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.title { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0 0 8px; }
.meta { font-size: 12px; color: #b5a898; margin-bottom: 16px; }
.content :deep(p) { font-size: 15px; color: #5a4a42; line-height: 1.7; margin: 0 0 8px; }
.content :deep(img) { max-width: 100%; border-radius: 8px; margin: 8px 0; }
</style>
