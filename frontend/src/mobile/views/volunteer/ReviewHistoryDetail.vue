<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="pet">
      <div class="card-wrap">
        <MobileCard>
          <img :src="pet.coverImage" class="cover-img" />
          <h2 class="pet-name">{{ pet.name }}</h2>
          <p class="pet-desc">{{ pet.breed }} · {{ pet.age }}</p>
        </MobileCard>
      </div>
      <div class="card-wrap" v-if="records.length > 0">
        <MobileCard>
          <div class="section-title">审核记录</div>
          <div v-for="r in records" :key="r.id" class="record-item">
            <div class="rec-header">
              <span class="rec-action" :class="r.action === 'approve' ? 'approve' : 'reject'">{{ r.action === 'approve' ? '通过' : '拒绝' }}</span>
              <span class="rec-time">{{ r.createdAt?.slice(0, 10) }}</span>
            </div>
            <p class="rec-comment" v-if="r.comment">{{ r.comment }}</p>
          </div>
        </MobileCard>
      </div>
      <MobileEmpty v-else icon="fas fa-history" description="暂无审核记录" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPetDetail } from '@/api/pet'
import request from '@/api/request'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const route = useRoute()
const pet = ref(null); const records = ref([]); const loading = ref(true)

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.id)
    records.value = await request.get('/volunteer/pets/' + route.params.id + '/review-records')
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.cover-img { width: 100%; border-radius: 10px; margin-bottom: 12px; }
.pet-name { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0 0 4px; }
.pet-desc { font-size: 14px; color: #a09080; margin: 0; }
.section-title { font-size: 15px; font-weight: 600; color: #5a4a42; margin-bottom: 12px; }
.record-item { padding: 12px 0; border-bottom: 1px solid #f5f0e8; }
.record-item:last-child { border-bottom: none; }
.rec-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.rec-action { font-size: 13px; font-weight: 600; padding: 2px 10px; border-radius: 4px; }
.rec-action.approve { background: rgba(103,194,58,0.1); color: #67c23a; }
.rec-action.reject { background: rgba(245,108,108,0.1); color: #e8564a; }
.rec-time { font-size: 12px; color: #a09080; }
.rec-comment { font-size: 14px; color: #5a4a42; line-height: 1.5; margin: 0; }
</style>
