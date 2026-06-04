<template>
  <div class="page">
    <!-- 提交反馈 -->
    <div class="card-wrap">
      <MobileCard>
        <div class="form-group"><label>反馈内容</label><textarea v-model="content" placeholder="请描述你的问题或建议..." rows="3"></textarea></div>
        <button class="save-btn" :disabled="submitting || !content.trim()" @click="handleSubmit">{{ submitting ? '提交中...' : '提交反馈' }}</button>
      </MobileCard>
    </div>

    <!-- 反馈列表 -->
    <div class="card-wrap">
      <MobileCard>
        <div class="section-title">我的反馈</div>
        <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
        <MobileEmpty v-else-if="list.length === 0" icon="fas fa-comment" description="暂无反馈" />
        <div v-else class="feedback-list">
          <div v-for="f in list" :key="f.id" class="fb-item">
            <div class="fb-header"><span class="fb-status">{{ f.status || '待处理' }}</span><span class="fb-time">{{ f.createdAt?.slice(0, 10) }}</span></div>
            <p class="fb-content">{{ f.content }}</p>
            <p v-if="f.reply" class="fb-reply">回复：{{ f.reply }}</p>
          </div>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback, getMyFeedback } from '@/api/feedback'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'

const content = ref(''); const submitting = ref(false)
const list = ref([]); const loading = ref(true)

async function loadList() { loading.value = true; try { list.value = await getMyFeedback() } catch {} finally { loading.value = false } }

async function handleSubmit() {
  submitting.value = true
  try { await submitFeedback({ content: content.value }); ElMessage.success('反馈已提交'); content.value = ''; loadList() } catch {} finally { submitting.value = false }
}

onMounted(loadList)
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.section-title { font-size: 15px; font-weight: 600; color: #5a4a42; margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #a09080; margin-bottom: 6px; }
textarea { width: 100%; padding: 10px 12px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; font-size: 14px; color: #5a4a42; resize: none; outline: none; box-sizing: border-box; }
textarea:focus { border-color: #8ab8a0; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
.save-btn:disabled { opacity: 0.5; }
.loading-wrap { display: flex; justify-content: center; padding: 32px 0; font-size: 20px; color: #d1e7dd; }
.feedback-list { display: flex; flex-direction: column; gap: 0; }
.fb-item { padding: 12px 0; border-bottom: 1px solid #f5f0e8; }
.fb-item:last-child { border-bottom: none; }
.fb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.fb-status { font-size: 12px; padding: 2px 8px; border-radius: 4px; background: rgba(139,184,160,0.12); color: #8ab8a0; }
.fb-time { font-size: 12px; color: #a09080; }
.fb-content { font-size: 14px; color: #5a4a42; line-height: 1.5; margin: 0; }
.fb-reply { font-size: 13px; color: #8ab8a0; background: rgba(139,184,160,0.06); padding: 8px 12px; border-radius: 8px; margin: 8px 0 0; }
</style>
