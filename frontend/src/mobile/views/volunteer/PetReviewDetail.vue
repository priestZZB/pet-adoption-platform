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
          <h2>{{ pet.name }}</h2>
          <div class="info-grid">
            <div class="info-item"><span class="lbl">品种</span><span>{{ pet.breed || '-' }}</span></div>
            <div class="info-item"><span class="lbl">性别</span><span>{{ genderMap[pet.gender] || '-' }}</span></div>
            <div class="info-item"><span class="lbl">年龄</span><span>{{ pet.age || '-' }}</span></div>
          </div>
          <p v-if="pet.description" class="desc">{{ pet.description }}</p>
          <div class="form-group" v-if="pet.status === '待审核'"><label>审核意见</label><textarea v-model="comment" placeholder="可选" rows="2"></textarea></div>
        </MobileCard>
      </div>
      <div class="act-row" v-if="pet.status === '待审核'">
        <button class="btn-approve" :disabled="submitting" @click="submitReview('approve')">通过</button>
        <button class="btn-reject" :disabled="submitting" @click="submitReview('reject')">拒绝</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPetDetail } from '@/api/pet'
import request from '@/api/request'
import { GENDER_MAP } from '@/utils/constants'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const pet = ref(null); const images = ref([]); const loading = ref(true)
const comment = ref(''); const submitting = ref(false)
const genderMap = GENDER_MAP || { 1: '公', 2: '母' }

onMounted(async () => {
  try {
    pet.value = await getPetDetail(route.params.id)
    images.value = pet.value.images?.length ? pet.value.images.map(i => i.url || i) : [pet.value.coverImage].filter(Boolean)
  } catch {} finally { loading.value = false }
})

async function submitReview(action) {
  submitting.value = true
  try {
    await request.post('/volunteer/pets/' + route.params.id + '/review', { action, comment: comment.value })
    ElMessage.success(action === 'approve' ? '已通过' : '已拒绝')
    router.back()
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px 100px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.carousel-wrap { width: 100%; aspect-ratio: 4/3; overflow: hidden; border-radius: 10px; margin-bottom: 14px; background: #e8ddd0; }
.carousel-wrap :deep(.el-carousel) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__container) { height: 100% !important; }
.carousel-wrap :deep(.el-carousel__item) { height: 100% !important; }
h2 { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0 0 12px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; margin-bottom: 12px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.lbl { font-size: 12px; color: #a09080; }
.info-item span:last-child { font-size: 14px; color: #5a4a42; font-weight: 500; }
.desc { font-size: 14px; color: #a09080; line-height: 1.6; margin: 0; }
.form-group { margin-top: 14px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
.act-row { display: flex; gap: 10px; padding: 0 16px; }
.btn-approve { flex: 1; height: 48px; border: none; border-radius: 12px; background: #8ab8a0; color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.btn-reject { flex: 1; height: 48px; border: 1px solid #e8564a; border-radius: 12px; background: #fefaf5; color: #e8564a; font-size: 16px; font-weight: 600; cursor: pointer; }
.btn-approve:disabled, .btn-reject:disabled { opacity: 0.5; }
</style>
