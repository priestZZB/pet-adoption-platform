<template>
  <div class="favorites-page">
    <!-- 加载 -->
    <div v-if="loading" class="loading-wrap">
      <i class="fas fa-spinner fa-pulse"></i>
    </div>

    <!-- 空状态 -->
    <MobileEmpty
      v-else-if="petList.length === 0"
      icon="fas fa-star"
      description="还没有收藏任何宠物"
      action-label="去首页看看"
      @action="router.push('/')"
    />

    <!-- 宠物网格 2 列 -->
    <div v-else class="pet-grid">
      <MobileCard
        v-for="pet in petList"
        :key="pet.id"
        clickable
        @click="goDetail(pet.id)"
      >
        <template #image>
          <img :src="pet.coverImage" class="pet-img" />
        </template>
        <h3>{{ pet.name }}</h3>
        <p>{{ pet.breed }} · {{ pet.age }}</p>
        <div class="pet-row">
          <button class="unfav-btn" @click.stop="handleUnfav(pet.id)">
            <i class="fas fa-star"></i> 取消收藏
          </button>
        </div>
      </MobileCard>
    </div>

    <div class="bottom-spacer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, unfavorite } from '@/api/pet'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const router = useRouter()
const petList = ref([])
const loading = ref(true)

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getFavorites()
    petList.value = res || []
  } catch {} finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push('/pets/' + id)
}

async function handleUnfav(id) {
  try {
    await unfavorite(id)
    petList.value = petList.value.filter(p => p.id !== id)
  } catch {}
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page { padding: 12px 16px; }

.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }

.pet-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.pet-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}
.pet-grid :deep(h3) { font-size: 15px; margin-bottom: 2px; }
.pet-grid :deep(p) { font-size: 12px; margin-bottom: 8px; }

.pet-row { display: flex; justify-content: flex-end; }
.unfav-btn {
  display: flex; align-items: center; gap: 4px;
  height: 28px; padding: 0 10px;
  border: 1px solid #d1e7dd; border-radius: 14px;
  background: #fefaf5; color: #e6a23c; font-size: 12px; cursor: pointer;
}
.unfav-btn:active { background: #fdf6ec; }

.bottom-spacer { height: 80px; }
</style>
