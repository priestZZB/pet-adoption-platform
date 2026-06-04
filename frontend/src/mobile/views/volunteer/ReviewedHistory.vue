<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-history" description="暂无审核记录" />
    <div v-else class="list">
      <MobileCard v-for="pet in list" :key="pet.id" class="item" clickable @click="goDetail(pet.id)">
        <div class="pet-row">
          <img :src="pet.coverImage" class="pet-img" />
          <div class="pet-info">
            <h4>{{ pet.name }}</h4>
            <p>{{ pet.breed }} · {{ pet.age }}</p>
            <span class="time">{{ pet.reviewedAt?.slice(0, 10) || '-' }}</span>
          </div>
          <i class="fas fa-chevron-right arrow"></i>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const list = ref([]); const loading = ref(true)

onMounted(async () => { try { list.value = await request.get('/volunteer/pets/reviewed') } catch {} finally { loading.value = false } })
function goDetail(id) { router.push('/volunteer/review-history/' + id) }
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 10px; }
.pet-row { display: flex; gap: 10px; align-items: center; }
.pet-img { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.pet-info { flex: 1; }
.pet-info h4 { font-size: 15px; font-weight: 600; color: #5a4a42; margin: 0 0 2px; }
.pet-info p { font-size: 13px; color: #a09080; margin: 0 0 2px; }
.time { font-size: 12px; color: #8ab8a0; }
.arrow { color: #c0b8a8; font-size: 14px; }
</style>
