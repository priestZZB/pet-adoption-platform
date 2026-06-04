<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else>
      <div class="stat-grid">
        <div class="stat-card" v-for="s in stats" :key="s.label">
          <span class="stat-value">{{ s.value }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/admin'

const loading = ref(true)
const stats = ref([])

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = [
      { label: '用户数', value: res.userCount || 0 },
      { label: '宠物数', value: res.petCount || 0 },
      { label: '领养申请', value: res.adoptCount || 0 },
      { label: '订单数', value: res.orderCount || 0 },
      { label: '志愿者', value: res.volunteerCount || 0 },
      { label: '送养人', value: res.donorCount || 0 },
    ]
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.page { padding: 12px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.stat-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.stat-card { background: #fff; border-radius: 12px; padding: 18px 14px; text-align: center; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.stat-value { display: block; font-size: 28px; font-weight: 700; color: #409EFF; }
.stat-label { display: block; font-size: 13px; color: #909399; margin-top: 4px; }
</style>
