<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="apply-header">
          <i class="fas fa-hand-holding-heart icon"></i>
          <h2>申请成为送养人</h2>
          <p>送养人可发布待领养宠物信息，帮助流浪动物找到温暖的家。</p>
        </div>
        <div v-if="status" class="status-card">
          <div class="status-label">申请状态</div>
          <div class="status-value">{{ status.status || '审核中' }}</div>
          <p class="status-desc" v-if="status.reason">审核意见：{{ status.reason }}</p>
        </div>
        <button v-if="!status || status.status === '已拒绝'" class="save-btn" :disabled="applying" @click="handleApply">{{ applying ? '提交中...' : '提交申请' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { donorApply, getDonorStatus } from '@/api/user'
import MobileCard from '../../components/MobileCard.vue'

const status = ref(null); const applying = ref(false)

async function loadStatus() { try { status.value = await getDonorStatus() } catch {} }

async function handleApply() {
  applying.value = true
  try { await donorApply(); ElMessage.success('申请已提交'); loadStatus() } catch {} finally { applying.value = false }
}

onMounted(loadStatus)
</script>

<style scoped>
.page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.apply-header { text-align: center; padding: 8px 0 16px; }
.icon { font-size: 48px; color: #8ab8a0; margin-bottom: 12px; }
.apply-header h2 { font-size: 18px; font-weight: 600; color: #5a4a42; margin: 0 0 8px; }
.apply-header p { font-size: 14px; color: #a09080; line-height: 1.5; margin: 0; }
.status-card { padding: 14px; background: rgba(139,184,160,0.06); border-radius: 10px; margin-bottom: 16px; text-align: center; }
.status-label { font-size: 12px; color: #a09080; margin-bottom: 4px; }
.status-value { font-size: 18px; font-weight: 600; color: #8ab8a0; }
.status-desc { font-size: 13px; color: #a09080; margin: 4px 0 0; }
.save-btn { width: 100%; height: 46px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
