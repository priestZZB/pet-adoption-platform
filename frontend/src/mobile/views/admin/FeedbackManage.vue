<template>
  <div class="page">
    <div v-if="loading" class="loading-wrap"><i class="fas fa-spinner fa-pulse"></i></div>
    <MobileEmpty v-else-if="list.length === 0" description="暂无反馈" />
    <div v-else class="list">
      <MobileCard v-for="f in list" :key="f.id" class="item">
        <div class="row"><span class="user">{{ f.nickname || f.username }}</span><span class="time">{{ f.createdAt?.slice(0, 10) }}</span></div>
        <p class="content">{{ f.content }}</p>
        <p v-if="f.reply" class="reply">回复：{{ f.reply }}</p>
        <div class="acts" v-if="!f.reply"><button @click="openReply(f)">回复</button></div>
      </MobileCard>
    </div>
    <MobileDialog v-model="replyVisible" title="回复反馈">
      <div class="form-group"><label>回复内容</label><el-input v-model="replyText" placeholder="输入回复内容" /></div>
      <template #footer><button class="save-btn" @click="confirmReply">确认回复</button></template>
    </MobileDialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllFeedbacks, replyFeedback } from '@/api/admin'
import MobileCard from '../../components/MobileCard.vue'
import MobileEmpty from '../../components/MobileEmpty.vue'
import MobileDialog from '../../components/MobileDialog.vue'

const list = ref([]); const loading = ref(true)
const replyVisible = ref(false); const replyId = ref(null); const replyText = ref('')

onMounted(async () => { try { list.value = await getAllFeedbacks() } catch {} finally { loading.value = false } })
function openReply(f) { replyId.value = f.id; replyText.value = ''; replyVisible.value = true }
async function confirmReply() {
  try { await replyFeedback(replyId.value, replyText.value); ElMessage.success('已回复'); replyVisible.value = false; list.value = list.value.map(i => i.id === replyId.value ? { ...i, reply: replyText.value } : i) } catch {}
}
</script>

<style scoped>
.page { padding: 12px 12px 40px; }
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; font-size: 28px; color: #d1e7dd; }
.item { margin-bottom: 8px; }
.row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.user { font-size: 14px; font-weight: 600; color: #303133; }
.time { font-size: 12px; color: #909399; }
.content { font-size: 14px; color: #303133; line-height: 1.5; margin: 0 0 4px; }
.reply { font-size: 13px; color: #409EFF; background: rgba(64,158,255,0.05); padding: 8px 10px; border-radius: 6px; margin: 6px 0 0; }
.acts { margin-top: 6px; }
.acts button { height: 28px; padding: 0 12px; border: 1px solid #409EFF; border-radius: 6px; background: #fff; color: #409EFF; font-size: 12px; cursor: pointer; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: #909399; margin-bottom: 4px; }
.form-group :deep(.el-input__wrapper) { height: 42px; border-radius: 8px; }
.save-btn { width: 100%; height: 44px; border: none; border-radius: 10px; background: #409EFF; color: #fff; font-size: 15px; cursor: pointer; }
</style>
