<template>
  <div class="page">
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title">添加回访记录</div>
        <div class="form-group"><label>宠物</label>
          <el-select v-model="form.petId" placeholder="选择宠物" style="width:100%">
            <el-option v-for="p in pets" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>
        <div class="form-group"><label>回访内容</label><textarea v-model="form.content" placeholder="记录回访情况..." rows="4"></textarea></div>
        <button class="save-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交记录' }}</button>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitVisit, getSelectablePets } from '@/api/volunteer'
import MobileCard from '../../components/MobileCard.vue'

const router = useRouter()
const pets = ref([])
const submitting = ref(false)
const form = reactive({ petId: null, content: '' })

onMounted(async () => { try { pets.value = await getSelectablePets() } catch {} })

async function handleSubmit() {
  if (!form.petId) { ElMessage.warning('请选择宠物'); return }
  if (!form.content.trim()) { ElMessage.warning('请填写回访内容'); return }
  submitting.value = true
  try { await submitVisit(form); ElMessage.success('记录已提交'); router.back() } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 600; color: #5a4a42; margin-bottom: 16px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
.form-group :deep(.el-input__wrapper), .form-group :deep(.el-select__wrapper) { height: 46px; border-radius: 10px; border: 1px solid #d1e7dd; box-shadow: none !important; background: #fefaf5; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
textarea:focus { border-color: #8ab8a0; }
.save-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
</style>
