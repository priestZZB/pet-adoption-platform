<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="app">
      <div class="card-wrap"><MobileCard>
        <div class="section-title">申请信息</div>
        <div class="line"><span>宠物</span><span>{{ app.petName }}</span></div>
        <div class="line"><span>申请人</span><span>{{ app.applicantName }}</span></div>
        <div class="line"><span>状态</span><span>{{ app.status }}</span></div>
        <div class="line"><span>理由</span><span>{{ app.reason || '-' }}</span></div>
      </MobileCard></div>
      <div class="act-row" v-if="app.status === '待审核'">
        <button class="btn-approve" @click="handleReview('approve')">通过</button>
        <button class="btn-reject" @click="handleReview('reject')">拒绝</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminAppDetail, adminReviewApplication } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const app = ref(null); const loading = ref(true)

onMounted(async () => { try { app.value = await getAdminAppDetail(route.params.id) } catch {} finally { loading.value = false } })
async function handleReview(action) {
  try { await adminReviewApplication(app.value.id, action); ElMessage.success('操作成功'); router.back() } catch {}
}
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.line { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; border-bottom: 1px solid #f5f5f5; }
.line span:first-child { color: #909399; }
.line span:last-child { color: #303133; text-align: right; }
.act-row { display: flex; gap: 10px; padding: 0 12px; }
.btn-approve { flex: 1; height: 46px; border: none; border-radius: 10px; background: #67c23a; color: #fff; font-size: 15px; cursor: pointer; }
.btn-reject { flex: 1; height: 46px; border: 1px solid #e8564a; border-radius: 10px; background: #fff; color: #e8564a; font-size: 15px; cursor: pointer; }
</style>
