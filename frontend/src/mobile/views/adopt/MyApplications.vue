<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-clipboard-list" description="暂无领养申请" />
    <div v-else class="list">
      <MobileCard v-for="app in list" :key="app.id" class="item" clickable @click="goDetail(app.id)">
        <div class="app-header">
          <span class="app-name">{{ app.petName || '宠物' }}</span>
          <span class="app-status" :style="{ color: statusColor(app.status) }">{{ app.status || '审核中' }}</span>
        </div>
        <div class="app-meta">申请时间：{{ app.createdAt?.slice(0, 10) || '-' }}</div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyApplications } from '@/api/adopt'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const loading = ref(true)

onMounted(async () => { try { list.value = await getMyApplications() } catch {} finally { loading.value = false } })

function goDetail(id) { router.push('/user/adopt-application/' + id) }

function statusColor(s) {
  const map = { '待审核': '#e6a23c', '已通过': '#8ab8a0', '已拒绝': '#e8564a', '已完成': '#67c23a' }
  return map[s] || '#a09080'
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 10px; }
.app-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.app-name { font-size: 15px; font-weight: 600; color: #5a4a42; }
.app-status { font-size: 12px; font-weight: 500; }
.app-meta { font-size: 12px; color: #a09080; }
</style>
