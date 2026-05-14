<template>
  <div class="admin-page">
    <h3 class="page-title">AI对话记录</h3>

    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="title" label="对话标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="msgCount" label="消息数" width="80" align="center" />
        <el-table-column prop="createdAt" label="开始时间" width="180" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showSession(row)">
              查看对话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 对话详情弹窗 -->
    <el-dialog v-model="detailVisible" title="对话详情" width="720px">
      <div v-if="sessionMessages.length === 0" style="text-align:center;color:#909399;padding:40px">
        加载中...
      </div>
      <div v-else class="session-detail">
        <div
          v-for="(msg, idx) in sessionMessages"
          :key="idx"
          class="msg-row"
          :class="msg.role"
        >
          <div class="msg-label">{{ msg.role === 'user' ? '用户' : 'AI' }}</div>
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminSessions, getAdminSessionMessages } from '@/api/admin'

const list = ref([])
const loading = ref(false)

const detailVisible = ref(false)
const sessionMessages = ref([])

function showSession(row) {
  sessionMessages.value = []
  detailVisible.value = true
  loadMessages(row.sessionId)
}

async function loadMessages(sessionId) {
  try {
    const list = await getAdminSessionMessages(sessionId) || []
    sessionMessages.value = []
    for (const item of list) {
      sessionMessages.value.push({ role: 'user', content: item.question })
      sessionMessages.value.push({ role: 'assistant', content: item.answer })
    }
  } catch {
    sessionMessages.value = [{ role: 'assistant', content: '加载失败' }]
  }
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getAdminSessions() || []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-title { font-size: 20px; color: #303133; margin: 0 0 20px; }

.session-detail {
  max-height: 500px;
  overflow-y: auto;
  padding: 8px 0;
}
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.msg-row.user {
  flex-direction: row;
}
.msg-row.assistant {
  flex-direction: row;
}
.msg-label {
  width: 32px;
  flex-shrink: 0;
  font-size: 12px;
  color: #909399;
  text-align: right;
  padding-top: 4px;
}
.msg-bubble {
  flex: 1;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.msg-row.assistant .msg-bubble {
  background: #ecf5ff;
  color: #303133;
}
.msg-row.user .msg-bubble {
  background: #f5f7fa;
  color: #303133;
}
</style>
