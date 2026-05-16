<template>
  <div class="ai-chat-page">
    <!-- 侧边栏：对话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h4>对话历史</h4>
      </div>

      <div class="sidebar-new">
        <el-button class="new-chat-btn" style="width:100%" @click="handleNewChat">
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
        <el-button size="small" class="clear-all-btn" style="width:100%" @click="handleClearAll">
          清空全部
        </el-button>
      </div>
    </div>

    <!-- 主对话区 -->
    <div class="chat-main">
      <div class="chat-messages" ref="messagesRef" @scroll="onScroll">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="msg-row"
          :class="msg.role"
        >
          <el-avatar
            v-if="msg.role === 'assistant'"
            :size="32"
            src="/images/ai-avatar.jpg"
            class="msg-avatar"
          />
          <div class="msg-bubble" @mouseenter="hoveredMsg = idx" @mouseleave="hoveredMsg = null">
            <div class="msg-content" v-html="renderMarkdown(msg.content)"></div>
            <button
              v-if="msg.role === 'assistant' && hoveredMsg === idx"
              class="copy-btn"
              title="复制"
              @click="copyText(msg.content)"
            >
              <i class="fas fa-copy"></i>
            </button>
          </div>
          <el-avatar
            v-if="msg.role === 'user'"
            :size="32"
            :src="userStore.userInfo?.avatar || ''"
            class="msg-avatar"
          >
            {{ (userStore.userInfo?.nickname?.[0] || 'U').toUpperCase() }}
          </el-avatar>
        </div>

        <div v-if="waiting" class="msg-row assistant">
          <el-avatar :size="32" src="/images/ai-avatar.jpg" class="msg-avatar" />
          <div class="msg-bubble thinking">
            <el-icon class="is-loading"><Loading /></el-icon> AI正在思考...
          </div>
        </div>

      </div>

      <!-- 回到底部按钮（固定在聊天区右下角，不在滚动容器内） -->
      <transition name="fade">
        <button
          v-if="showScrollBtn"
          class="scroll-bottom-btn"
          @click="scrollToBottom"
        >
          <el-icon><ArrowDown /></el-icon>
        </button>
      </transition>

      <div class="chat-input">
        <div class="chat-input-row">
          <el-input
            ref="inputRef"
            v-model="question"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入你想了解的问题... (Enter发送, Shift+Enter换行)"
            :disabled="waiting"
            class="chat-input-field"
            @keydown="handleKeydown"
          />
          <el-button
            class="send-msg-btn"
            :disabled="!question.trim() || waiting"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Delete, ArrowDown } from '@element-plus/icons-vue'
import { chat, getSessions, getSessionMessages, clearChatHistory, deleteSession } from '@/api/ai'
import { marked } from 'marked'

const userStore = useUserStore()
const messagesRef = ref(null)
const inputRef = ref(null)
const question = ref('')
const messages = ref([
  { role: 'assistant', content: '你好呀！我是小宠，有宠平台的AI助手 🐾\n\n我可以帮你解答宠物领养、救助、日常养护等方面的问题，也可以帮你了解平台功能。有什么需要帮忙的吗？' }
])
const waiting = ref(false)
const sessions = ref([])
const currentSessionId = ref('')
const hoveredMsg = ref(null)
const showScrollBtn = ref(false)
const isAtBottom = ref(true)

function renderMarkdown(text) {
  if (!text) return ''
  // 1. 统一换行符 + 压缩连续空行
  let t = text
    .replace(/\r\n/g, '\n').replace(/\r/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
  // 2. 转义 HTML 特殊字符
  t = t.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 3. 处理行内格式
  t = t.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  t = t.replace(/`([^`]+)`/g, '<code>$1</code>')
  t = t.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  // 4. 代码块
  t = t.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
  // 5. 引用块
  t = t.replace(/^&gt; (.+)$/gm, '<blockquote>$1</blockquote>')
  // 6. 双换行先占位，再替换单换行，最后还原占位
  t = t.replace(/\n\n/g, '\x00')
  t = t.replace(/\n/g, '<br>')
  t = t.replace(/\x00/g, '<br><br>')
  return t
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function onScroll() {
  const el = messagesRef.value
  if (!el) return
  const dist = el.scrollHeight - el.scrollTop - el.clientHeight
  isAtBottom.value = dist < 60
  showScrollBtn.value = dist > 100
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function newSessionId() {
  return crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36) + Math.random().toString(36).slice(2)
}

function scrollToBottom(isAuto) {
  nextTick(() => {
    const el = messagesRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
      // 自动滚动时不显示回底按钮（用户自己滚上去了才显示）
      if (isAuto) {
        showScrollBtn.value = false
      }
    }
  })
}

async function loadSessions() {
  try {
    sessions.value = await getSessions() || []
  } catch {
    sessions.value = []
  }
}

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

async function handleNewChat() {
  currentSessionId.value = newSessionId()
  messages.value = [
    { role: 'assistant', content: '你好呀！我是小宠，有宠平台的AI助手 🐾\n\n我可以帮你解答宠物领养、救助、日常养护等方面的问题，也可以帮你了解平台功能。有什么需要帮忙的吗？' }
  ]
  await loadSessions()
}

async function sendMessage() {
  const q = question.value.trim()
  if (!q || waiting.value) return

  if (!currentSessionId.value) {
    currentSessionId.value = newSessionId()
  }

  messages.value.push({ role: 'user', content: q })
  question.value = ''
  waiting.value = true
  scrollToBottom(true)

  try {
    const res = await chat({ question: q, sessionId: currentSessionId.value })
    messages.value.push({ role: 'assistant', content: res.answer })
    loadSessions()
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    waiting.value = false
    scrollToBottom(true)
    nextTick(() => inputRef.value?.focus())
  }
}

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

/* ===== 侧边栏 ===== */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #d0d9e8;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.sidebar-header {
  padding: 14px 16px 0;
}
.sidebar-header h4 {
  margin: 0;
  font-size: 14px;
  color: #1a2332;
}
.sidebar-new {
  padding: 10px 16px;
  border-bottom: 1px solid #d0d9e8;
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
  border-top: 1px solid #d0d9e8;
}
.session-item {
  display: flex;
  align-items: center;
  padding: 10px 12px 10px 16px;
  cursor: pointer;
  border-left: 3px solid transparent;
  border-bottom: 1px solid #e8edf5;
  transition: all 0.2s;
  gap: 6px;
}
.session-item:hover {
  background: #f5f8ff;
}
.session-item.active {
  background: #eef4ff;
  border-left-color: #3B82F6;
}
.session-info {
  flex: 1;
  min-width: 0;
}
.session-title {
  margin: 0 0 2px;
  font-size: 13px;
  color: #1a2332;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-meta {
  font-size: 11px;
  color: #909399;
}

/* ===== 主对话区 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #d0d9e8;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  position: relative;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.msg-row {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  align-items: flex-start;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}
.msg-bubble {
  max-width: 65%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* AI 气泡 - 浅蓝灰 */
.msg-row.assistant .msg-bubble {
  background: #f0f4f8;
  color: #1a2332;
  border-bottom-left-radius: 4px;
}

/* 用户气泡 - 科技蓝 */
.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59,130,246,0.2);
}

.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399 !important;
  background: #f0f4f8 !important;
}

/* ===== 输入框 ===== */
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #d0d9e8;
}

.chat-input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.chat-input-field {
  flex: 1;
}

/* ===== 按钮 ===== */
:deep(.new-chat-btn) {
  background: #3B82F6;
  border: none;
  color: #fff;
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 16px;
  height: 36px;
  transition: all 0.2s;
}
:deep(.new-chat-btn:hover) {
  background: #2563EB;
  box-shadow: 0 2px 8px rgba(59,130,246,0.3);
  color: #fff;
}
:deep(.send-msg-btn) {
  background: #3B82F6 !important;
  border: 1px solid #3B82F6 !important;
  color: #fff !important;
  font-weight: 600;
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  flex-shrink: 0;
}
:deep(.send-msg-btn:hover) {
  background: #2563EB !important;
  border-color: #2563EB !important;
  color: #fff !important;
}
:deep(.clear-all-btn) {
  border: 1px solid #d0d9e8;
  border-radius: 8px;
  color: #606266;
}
:deep(.clear-all-btn:hover) {
  border-color: #f56c6c;
  color: #f56c6c;
}

/* ===== 复制按钮 ===== */
.msg-bubble {
  position: relative;
}
.copy-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255,255,255,0.9);
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  transition: all 0.15s;
}
.copy-btn:hover {
  background: #fff;
  color: #3B82F6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* ===== 回到底部按钮（固定在聊天区右下角）===== */
.chat-main {
  position: relative;
}
.scroll-bottom-btn {
  position: absolute;
  bottom: 76px;
  right: 24px;
  width: 36px;
  height: 36px;
  border: 1px solid #d0d9e8;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
  color: #3B82F6;
  transition: all 0.2s;
  z-index: 10;
}
.scroll-bottom-btn:hover {
  background: #3B82F6;
  color: #fff;
  border-color: #3B82F6;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ===== 输入框多行 ===== */
.chat-input-field :deep(.el-textarea__inner) {
  border: 1px solid #d0d9e8;
  border-radius: 10px;
  box-shadow: none;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
}
.chat-input-field :deep(.el-textarea__inner:focus) {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}
</style>

<!-- v-html 渲染的 Markdown 内容用非 scoped 样式确保生效 -->
<style>
.msg-content {
  line-height: 1.6;
}
.msg-content strong {
  font-weight: 700;
}
.msg-content ul,
.msg-content ol {
  margin: 4px 0;
  padding-left: 20px;
}
.msg-content li {
  margin-bottom: 2px;
}
.msg-content code {
  background: rgba(0,0,0,0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
}
.msg-content pre {
  background: #1a2332;
  color: #e8e8e8;
  padding: 12px 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}
.msg-content pre code {
  background: none;
  padding: 0;
  color: inherit;
}
.msg-content a {
  color: #3B82F6;
  text-decoration: underline;
}
.msg-content blockquote {
  border-left: 3px solid #d0d9e8;
  margin: 8px 0;
  padding: 4px 12px;
  color: #606266;
}
</style>
