<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title">领养申请</div>
        <div class="form-group"><label>申请理由</label><textarea v-model="form.reason" placeholder="请说明你的领养动机、家庭环境、养宠经验等..." rows="4"></textarea></div>
        <div class="form-group"><label>居住环境</label><el-input v-model="form.housing" placeholder="例如：自有房屋/租房/与家人同住" /></div>
        <div class="form-group"><label>是否有养宠经验</label>
          <div class="radio-row">
            <button :class="['radio', { active: form.hasExperience === 1 }]" @click="form.hasExperience = 1">有</button>
            <button :class="['radio', { active: form.hasExperience === 0 }]" @click="form.hasExperience = 0">无</button>
          </div>
        </div>
        <button class="save-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交申请' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitApplication } from '@/api/adopt'
import MobileCard from '../../components/MobileCard.vue'

const route = useRoute(); const router = useRouter()
const submitting = ref(false)

const form = reactive({
  petId: Number(route.params.petId),
  reason: '',
  housing: '',
  hasExperience: 0
})

async function handleSubmit() {
  if (!form.reason.trim()) { ElMessage.warning('请填写申请理由'); return }
  submitting.value = true
  try { await submitApplication(form); ElMessage.success('申请已提交'); router.push('/user/adopt-applications') } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 600; color: #5a4a42; margin-bottom: 16px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
textarea:focus { border-color: #8ab8a0; }
.radio-row { display: flex; gap: 10px; }
.radio { flex: 1; height: 42px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.radio.active { background: #8ab8a0; border-color: #8ab8a0; color: #fff; }
.save-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
