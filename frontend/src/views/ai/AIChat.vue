<template>
  <div class="ai-chat-page">
    <!-- 侧边栏：对话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h4>对话历史</h4>
      </div>

      <div class="sidebar-new">
        <el-button type="primary" style="width:100%" @click="handleNewChat">
          + 新对话
        </el-button>
      </div>

      <div class="sidebar-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: currentSessionId === s.sessionId }"
          @click="switchSession(s.sessionId)"
        >
          <div class="session-info">
            <p class="session-title">{{ s.title || '新对话' }}</p>
            <span class="session-meta">{{ s.msgCount }}条 · {{ s.createdAt }}</span>
          </div>
          <el-button
            text
            type="danger"
            size="small"
            :icon="Delete"
            @click.stop="handleDeleteSession(s.sessionId)"
          />
        </div>
        <div v-if="sessions.length === 0" class="sidebar-empty">
          暂无对话记录
        </div>
      </div>

      <div v-if="sessions.length > 0" class="sidebar-footer">
        <el-button size="small" type="danger" plain style="width:100%" @click="handleClearAll">
          清空全部
        </el-button>
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

        <div v-if="waiting" class="msg-row assistant">
          <div class="msg-bubble thinking">
            <el-icon class="is-loading"><Loading /></el-icon> AI正在思考...
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          ref="inputRef"
          v-model="question"
          placeholder="输入你想了解的问题..."
          size="large"
          :disabled="waiting"
          @keyup.enter="sendMessage"
        >
          <template #append>
            <el-button
              type="primary"
              style="background:#409EFF;color:#fff;font-weight:600"
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
import { Loading, Delete } from '@element-plus/icons-vue'
import { chat, getSessions, getSessionMessages, clearChatHistory, deleteSession } from '@/api/ai'

const messagesRef = ref(null)
const inputRef = ref(null)
const question = ref('')
const messages = ref([
  { role: 'assistant', content: '你好！我是AI助手，可以帮你解答关于宠物领养、救助等方面的问题。有什么需要帮助的吗？' }
])
const waiting = ref(false)
const sessions = ref([])
const currentSessionId = ref('')

// 生成唯一 sessionId
function newSessionId() {
  return crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36) + Math.random().toString(36).slice(2)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 刷新侧边栏对话列表
async function loadSessions() {
  try {
    sessions.value = await getSessions() || []
  } catch {
    sessions.value = []
  }
}

// 切换到某次对话
async function switchSession(sessionId) {
  if (!sessionId) return
  currentSessionId.value = sessionId
  try {
    const list = await getSessionMessages(sessionId) || []
    messages.value = []
    for (const item of list) {
      messages.value.push({ role: 'user', content: item.question })
      messages.value.push({ role: 'assistant', content: item.answer })
    }
  } catch {
    messages.value = [{ role: 'assistant', content: '加载失败，请重试' }]
  }
  scrollToBottom()
}

// 新对话
async function handleNewChat() {
  currentSessionId.value = newSessionId()
  messages.value = [
    { role: 'assistant', content: '你好！我是AI助手，可以帮你解答关于宠物领养、救助等方面的问题。有什么需要帮助的吗？' }
  ]
  await loadSessions()
}

// 发送消息
async function sendMessage() {
  const q = question.value.trim()
  if (!q || waiting.value) return

  // 如果还没有 sessionId（理论不会），生成一个
  if (!currentSessionId.value) {
    currentSessionId.value = newSessionId()
  }

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  waiting.value = true
  scrollToBottom()

  try {
    const res = await chat({ question: q, sessionId: currentSessionId.value })
    messages.value.push({ role: 'assistant', content: res.answer })
    loadSessions() // 刷新侧边栏
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    waiting.value = false
    scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}

// 删除某次对话
async function handleDeleteSession(sessionId) {
  try {
    await ElMessageBox.confirm('确定删除该对话？', '提示')
    await deleteSession(sessionId)
    if (currentSessionId.value === sessionId) {
      handleNewChat()
    }
    loadSessions()
    ElMessage.success('已删除')
  } catch { /* 取消 */ }
}

// 清空全部
async function handleClearAll() {
  try {
    await ElMessageBox.confirm('确定清空所有对话？', '提示')
    await clearChatHistory()
    sessions.value = []
    handleNewChat()
    ElMessage.success('已清空')
  } catch { /* 取消 */ }
}

onMounted(() => {
  loadSessions()
  currentSessionId.value = newSessionId()
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  height: calc(100vh - 140px);
  gap: 16px;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  padding: 14px 16px 0;
}
.sidebar-header h4 {
  margin: 0;
  font-size: 14px;
  color: #303133;
}
.sidebar-new {
  padding: 10px 16px;
  border-bottom: 1px solid #ebeef5;
}
.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}
.sidebar-empty {
  text-align: center;
  font-size: 13px;
  color: #909399;
  padding: 40px 0;
}
.sidebar-footer {
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
}
.session-item {
  display: flex;
  align-items: center;
  padding: 10px 12px 10px 16px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: background 0.2s;
  gap: 6px;
}
.session-item:hover,
.session-item.active {
  background: #ecf5ff;
  border-left-color: #409EFF;
}
.session-info {
  flex: 1;
  min-width: 0;
}
.session-title {
  margin: 0 0 2px;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-meta {
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
