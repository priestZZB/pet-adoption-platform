<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无送养人申请" />
    <div v-else class="list">
      <MobileCard v-for="d in list" :key="d.id" class="item">
        <div class="row"><span class="name">{{ d.nickname || d.username }}</span><span :class="['status', statusClass(d.status)]">{{ d.status || '待审核' }}</span></div>
        <div class="meta">{{ d.phone }} | {{ d.createdAt?.slice(0, 10) }}</div>
        <div class="acts" v-if="d.status === '待审核'">
          <button class="approve" @click="handleReview(d.id, 'approve')">通过</button>
          <button class="reject" @click="openReject(d)">驳回</button>
        </div>
      </MobileCard>
    </div>
    <MobileDialog v-model="rejectVisible" title="驳回原因">
      <div class="form-group"><label>原因</label><el-input v-model="rejectReason" placeholder="可选" /></div>
      <template #footer><button class="save-btn" @click="confirmReject">确认驳回</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDonorApplies, reviewDonor } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const loading = ref(true)
const rejectVisible = ref(false); const rejectId = ref(null); const rejectReason = ref('')

onMounted(async () => { try { list.value = await getDonorApplies() } catch {} finally { loading.value = false } })
function statusClass(s) { const m = { '待审核': 'warn', '已通过': 'ok', '已驳回': 'err' }; return m[s] || '' }
function openReject(d) { rejectId.value = d.id; rejectReason.value = ''; rejectVisible.value = true }
async function handleReview(id, action) {
  try { await reviewDonor(id, { action }); ElMessage.success('操作成功'); list.value = list.value.filter(i => i.id !== id) } catch {}
}
async function confirmReject() {
  try { await reviewDonor(rejectId.value, { action: 'reject', reason: rejectReason.value }); ElMessage.success('已驳回'); rejectVisible.value = false; list.value = list.value.filter(i => i.id !== rejectId.value) } catch {}
}
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.name { font-size: 15px; font-weight: 600; color: #303133; }
.status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.status.warn { background: rgba(230,162,60,0.1); color: #e6a23c; }
.status.ok { background: rgba(103,194,58,0.1); color: #67c23a; }
.status.err { background: rgba(245,108,108,0.1); color: #e8564a; }
.meta { font-size: 12px; color: #909399; margin-bottom: 6px; }
.acts { display: flex; gap: 6px; }
.acts button { height: 28px; padding: 0 12px; border-radius: 6px; font-size: 12px; cursor: pointer; }
.acts .approve { border: 1px solid #67c23a; background: #fff; color: #67c23a; }
.acts .reject { border: 1px solid #e8564a; background: #fff; color: #e8564a; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #e8564a; color: #fff; font-size: 15px; cursor: pointer; }
</style>
