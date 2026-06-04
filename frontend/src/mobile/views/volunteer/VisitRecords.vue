<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-clipboard-list" description="暂无回访记录" action-label="添加回访" @action="$router.push('/volunteer/visits/add')" />
    <div v-else class="list">
      <MobileCard v-for="v in list" :key="v.id" class="item">
        <div class="visit-header">
          <span class="visit-pet">{{ v.petName || '宠物' }}</span>
          <span class="visit-time">{{ v.createdAt?.slice(0, 10) }}</span>
        </div>
        <p class="visit-desc" v-if="v.content">{{ v.content }}</p>
        <div class="visit-imgs" v-if="v.images?.length">
          <img v-for="(img, idx) in v.images.slice(0, 3)" :key="idx" :src="img.url || img" class="v-img" />
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyVisits } from '@/api/volunteer'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const list = ref([]); const loading = ref(true)

onMounted(async () => { try { list.value = await getMyVisits() } catch {} finally { loading.value = false } })
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 10px; }
.visit-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.visit-pet { font-size: 15px; font-weight: 600; color: #5a4a42; }
.visit-time { font-size: 12px; color: #a09080; }
.visit-desc { font-size: 14px; color: #5a4a42; line-height: 1.5; margin: 0; }
.visit-imgs { display: flex; gap: 6px; margin-top: 8px; }
.v-img { width: 72px; height: 72px; border-radius: 6px; object-fit: cover; }
</style>
