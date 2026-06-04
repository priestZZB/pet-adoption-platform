<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <template v-else-if="app">
      <div class="card-wrap">
        <MobileCard>
          <div class="status-bar" :style="{ color: statusColor(app.status) }">{{ app.status || '审核中' }}</div>
          <div class="info-line"><span>宠物</span><span>{{ app.petName || '-' }}</span></div>
          <div class="info-line"><span>申请人</span><span>{{ app.applicantName || '-' }}</span></div>
          <div class="info-line"><span>申请时间</span><span>{{ app.createdAt?.slice(0, 10) || '-' }}</span></div>
        </MobileCard>
      </div>
      <div class="card-wrap">
        <MobileCard>
          <div class="section-title">申请信息</div>
          <div class="info-line"><span>居住环境</span><span>{{ app.housing || '-' }}</span></div>
          <div class="info-line"><span>养宠经验</span><span>{{ app.hasExperience ? '有' : '无' }}</span></div>
          <div class="info-block"><label>申请理由</label><p>{{ app.reason || '-' }}</p></div>
        </MobileCard>
      </div>
      <div v-if="app.reply" class="card-wrap">
        <MobileCard>
          <div class="section-title">审核回复</div>
          <p class="reply-text">{{ app.reply }}</p>
        </MobileCard>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getApplicationDetail } from '@/api/adopt'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute()
const app = ref(null); const loading = ref(true)

onMounted(async () => { try { app.value = await getApplicationDetail(route.params.id) } catch {} finally { loading.value = false } })

function statusColor(s) {
  const map = { '待审核': '#e6a23c', '已通过': '#8ab8a0', '已拒绝': '#e8564a', '已完成': '#67c23a' }
  return map[s] || '#a09080'
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.card-wrap { margin-bottom: 12px; }
.status-bar { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.section-title { font-size: 15px; font-weight: 600; color: #5a4a42; margin-bottom: 12px; }
.info-line { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.info-line span:first-child { color: #a09080; }
.info-line span:last-child { color: #5a4a42; }
.info-block { padding: 6px 0; }
.info-block label { font-size: 13px; color: #a09080; display: block; margin-bottom: 4px; }
.info-block p { font-size: 14px; color: #5a4a42; line-height: 1.6; margin: 0; }
.reply-text { font-size: 14px; color: #5a4a42; line-height: 1.6; margin: 0; }
</style>
