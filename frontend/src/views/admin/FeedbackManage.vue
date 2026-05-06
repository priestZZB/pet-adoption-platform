<template>
  <div class="admin-page">
    <h3 class="page-title">反馈管理</h3>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="70" />
        <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.images" :src="row.images.split(',')[0]" fit="cover" style="width:40px;height:40px;border-radius:4px;cursor:pointer" :preview-src-list="row.images.split(',')" preview-teleported>
              <template #error><div class="img-xs" /></template>
            </el-image>
            <span v-else class="na">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已回复' : '未回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" @click="openReply(row)">回复</el-button>
            <span v-else class="na">已回复</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复反馈" width="500px">
      <p class="reply-content">{{ currentFeedback?.content }}</p>
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="输入回复内容..." />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllFeedbacks, replyFeedback } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const replyVisible = ref(false)
const currentFeedback = ref(null)
const replyText = ref('')
const replying = ref(false)

async function loadList() {
  loading.value = true
  try { list.value = await getAllFeedbacks() }
  catch { list.value = [] }
  finally { loading.value = false }
}

function openReply(row) {
  currentFeedback.value = row
  replyText.value = ''
  replyVisible.value = true
}

async function handleReply() {
  if (!replyText.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  replying.value = true
  try {
    await replyFeedback(currentFeedback.value.id, replyText.value)
    ElMessage.success('回复成功')
    replyVisible.value = false; loadList()
  } finally { replying.value = false }
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }
.img-xs { width: 40px; height: 40px; background: #f5f7fa; border-radius: 4px; }
.na { color: #909399; font-size: 13px; }
.reply-content { padding: 12px; background: #f5f7fa; border-radius: 6px; font-size: 14px; color: #606266; margin-bottom: 16px; }
</style>
