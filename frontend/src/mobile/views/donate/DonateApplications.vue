<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" icon="fas fa-clipboard-list" description="暂无领养申请" />
    <div v-else class="list">
      <MobileCard v-for="app in list" :key="app.id" class="item">
        <div class="app-header">
          <span class="app-name">{{ app.applicantName || '申请人' }}</span>
          <span class="app-status" :style="{ color: statusColor(app.status) }">{{ app.status || '待审核' }}</span>
        </div>
        <div class="app-meta" v-if="app.reason">{{ app.reason }}</div>
        <div class="app-actions" v-if="app.status === '待审核'">
          <button class="act-approve" @click="handleReview(app.id, 'approve')">同意</button>
          <button class="act-reject" @click="handleReview(app.id, 'reject')">拒绝</button>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPetApplications, reviewApplication } from '@/api/pet'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const route = useRoute()
const list = ref([]); const loading = ref(true)

async function loadList() {
  loading.value = true
  try { list.value = await getPetApplications(route.params.id) } catch {} finally { loading.value = false }
}

async function handleReview(id, action) {
  const label = action === 'approve' ? '同意' : '拒绝'
  try { await ElMessageBox.confirm('确定' + label + '该申请？', '审核'); await reviewApplication(id, action); ElMessage.success('审核完成'); loadList() } catch {}
}

function statusColor(s) {
  const map = { '待审核': '#e6a23c', '已通过': '#8ab8a0', '已拒绝': '#e8564a' }
  return map[s] || '#a09080'
}

onMounted(loadList)
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 10px; }
.app-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.app-name { font-size: 15px; font-weight: 600; color: #5a4a42; }
.app-status { font-size: 12px; font-weight: 500; }
.app-meta { font-size: 13px; color: #a09080; line-height: 1.4; margin-bottom: 8px; }
.app-actions { display: flex; gap: 8px; }
.act-approve { height: 32px; padding: 0 16px; border: none; border-radius: 8px; background: #8ab8a0; color: #fff; font-size: 13px; cursor: pointer; }
.act-reject { height: 32px; padding: 0 16px; border: 1px solid #d1e7dd; border-radius: 8px; background: #fefaf5; color: #e8564a; font-size: 13px; cursor: pointer; }
</style>
