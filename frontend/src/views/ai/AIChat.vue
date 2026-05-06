<template>
  <div class="ai-chat-page">
    <!-- 侧边栏：历史记录 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h4>对话历史</h4>
        <el-button
          v-if="historyList.length > 0"
          size="small"
          type="danger"
          plain
          @click="handleClear"
        >
          清空记录
        </el-button>
      </div>

      <div class="sidebar-list">
        <div
          v-for="h in historyList"
          :key="h.id"
          class="history-item"
          :class="{ active: activeHistoryId === h.id }"
          @click="loadHistoryChat(h)"
        >
          <p class="history-question">{{ h.question }}</p>
          <span class="history-time">{{ h.createdAt }}</span>
        </div>
        <div v-if="historyList.length === 0" class="sidebar-empty">
          暂无对话记录
        </div>
      </div>
    </div>

    <!-- 主对话区 -->
    <div class="chat-main">
      <div class="chat-messages" ref="messagesRef">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="msg-row"
          :class="msg.role"
        >
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>

        <!-- 加载中 -->
        <div v-if="waiting" class="msg-row assistant">
          <div class="msg-bubble thinking">
            <el-icon class="is-loading"><Loading /></el-icon> AI正在思考...
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="question"
          placeholder="输入你想了解的问题..."
          size="large"
          :disabled="waiting"
          @keyup.enter="sendMessage"
        >
          <template #append>
            <el-button
              type="primary"
              :disabled="!question.trim() || waiting"
              @click="sendMessage"
            >
              发送
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { chat, getChatHistory, clearChatHistory } from '@/api/ai'

const messagesRef = ref(null)
const question = ref('')
const messages = ref([
  { role: 'assistant', content: '你好！我是AI助手，可以帮你解答关于宠物领养、救助等方面的问题。有什么需要帮助的吗？' }
])
const waiting = ref(false)
const historyList = ref([])
const activeHistoryId = ref(null)

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const q = question.value.trim()
  if (!q || waiting.value) return

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  waiting.value = true
  scrollToBottom()

  try {
    const res = await chat({ question: q })
    messages.value.push({ role: 'assistant', content: res.answer })
    activeHistoryId.value = res.id
    loadHistory()
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    waiting.value = false
    scrollToBottom()
  }
}

function loadHistoryChat(h) {
  activeHistoryId.value = h.id
  messages.value = [
    { role: 'user', content: h.question },
    { role: 'assistant', content: h.answer }
  ]
  scrollToBottom()
}

async function loadHistory() {
  try {
    const res = await getChatHistory({ page: 1, size: 50 })
    historyList.value = res.list || []
  } catch {
    historyList.value = []
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空所有对话记录？', '提示')
    await clearChatHistory()
    historyList.value = []
    activeHistoryId.value = null
    messages.value = [
      { role: 'assistant', content: '你好！我是AI助手，有什么可以帮助你的？' }
    ]
    ElMessage.success('已清空')
  } catch {
    // 取消
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  height: calc(100vh - 140px);
  gap: 16px;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}
.sidebar-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}
.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.sidebar-empty {
  text-align: center;
  font-size: 13px;
  color: #909399;
  padding: 40px 0;
}
.history-item {
  padding: 10px 16px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background 0.2s;
}
.history-item:hover,
.history-item.active {
  background: #ecf5ff;
  border-left-color: #409EFF;
}
.history-question {
  margin: 0 0 4px;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.history-time {
  font-size: 11px;
  color: #909399;
}

/* 主对话区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.msg-row {
  display: flex;
  margin-bottom: 16px;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.msg-row.assistant .msg-bubble {
  background: #f0f2f5;
  color: #303133;
  border-bottom-left-radius: 4px;
}
.msg-row.user .msg-bubble {
  background: #409EFF;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399 !important;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
