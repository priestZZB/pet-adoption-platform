<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="pet">
      <div class="card-wrap">
        <MobileCard>
          <div class="carousel-wrap">
            <el-carousel height="100%">
              <el-carousel-item v-for="(img, idx) in images" :key="idx">
                <el-image :src="img" fit="cover" style="width:100%;height:100%" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="pet-header"><h2>{{ pet.name }}</h2><span class="pet-status">{{ pet.status }}</span></div>
          <div class="info-grid">
            <div class="info-item"><span class="lbl">品种</span><span>{{ pet.breed || '-' }}</span></div>
            <div class="info-item"><span class="lbl">性别</span><span>{{ genderMap[pet.gender] || '-' }}</span></div>
            <div class="info-item"><span class="lbl">年龄</span><span>{{ pet.age || '-' }}</span></div>
          </div>
          <p v-if="pet.description" class="desc">{{ pet.description }}</p>
          <div class="act-row" v-if="pet.status === '待审核'">
            <button class="btn-secondary" @click="handleWithdraw">撤回</button>
          </div>
          <div class="act-row" v-if="pet.status === '待领养' || pet.status === '已通过'">
            <button class="btn-secondary" @click="goApplications">查看申请</button>
            <button class="btn-warn" @click="handleOffline">下架</button>
          </div>
        </MobileCard>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPetDetail, offlinePet, withdrawPet } from '@/api/pet'
import { GENDER_MAP } from '@/utils/constants'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const pet = ref(null); const images = ref([]); const loading = ref(true)
const genderMap = GENDER_MAP || { 1: '公', 2: '母' }

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.id)
    images.value = pet.value.images?.length ? pet.value.images.map(i => i.url || i) : [pet.value.coverImage].filter(Boolean)
  } catch {} finally { loading.value = false }
})

function goApplications() { router.push('/donate/pets/' + pet.value.id + '/applications') }

async function handleOffline() {
  try { await ElMessageBox.confirm('确定下架该宠物？'); await offlinePet(pet.value.id); ElMessage.success('已下架'); pet.value.status = '已下架' } catch {}
}

async function handleWithdraw() {
  try { await ElMessageBox.confirm('确定撤回该送养？'); await withdrawPet(pet.value.id); ElMessage.success('已撤回'); router.back() } catch {}
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.carousel-wrap { width: 100%; aspect-ratio: 4/3; overflow: hidden; border-radius: 10px; margin-bottom: 14px; background: #e8ddd0; }
.carousel-wrap :deep(.el-carousel) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__container) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__item) { height: 100% !important; }
.pet-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.pet-header h2 { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0; flex: 1; }
.pet-status { font-size: 12px; padding: 3px 10px; border-radius: 4px; background: rgba(139,184,160,0.12); color: #8ab8a0; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; margin-bottom: 12px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.lbl { font-size: 12px; color: #a09080; }
.info-item span:last-child { font-size: 14px; color: #5a4a42; font-weight: 500; }
.desc { font-size: 14px; color: #a09080; line-height: 1.6; margin: 0; }
.act-row { display: flex; gap: 10px; margin-top: 16px; }
.btn-secondary { flex: 1; height: 42px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.btn-warn { flex: 1; height: 42px; border: 1px solid #e8564a; border-radius: 10px; background: #fefaf5; color: #e8564a; font-size: 14px; cursor: pointer; }
</style>
